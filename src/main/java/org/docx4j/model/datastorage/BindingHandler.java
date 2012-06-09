/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.model.datastorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.RPr;
import org.opendope.xpaths.Xpaths.Xpath;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class BindingHandler {
	
	private static Logger log = Logger.getLogger(BindingHandler.class);		
	
	static Templates xslt;			
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/model/datastorage/bind.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();		
	}


	/**
	 * Configure, how the handler handles links found in Custom XML.
	 * 
	 * If hyperlinkStyleId is set to <code>null</code>, strings 
	 * containing 'http://' or 'https://' are not converted to 
	 * w:hyperlink. This is the default behavior.
	 * 
	 * If hyperlinkStyleId is set to <code>"someWordHyperlinkStyleName"</code>, 
	 * strings containing 'http://' or 'https://' or or 'mailto:' are converted to w:hyperlink.  
	 * The default Word hyperlink style name is "Hyperlink".
	 * If you do this, you will need to post-process with RemovalHandler, since a
	 * content control with SdtPr w:dataBinding and w:text
	 * which contains a w:hyperlink will prevent Word 2007 from
	 * opening the docx.
	 * 
	 * Due to the architecture of this class, this is a static flag changing the
	 * behavior of all following calls to {@link #applyBindings}.
	 * 
	 * @param hyperlinkStyleID
	 *            The style to use for hyperlinks (eg Hyperlink)
	 */
	public static void setHyperlinkStyle (
			String hyperlinkStyleID) {
		hyperlinkStyleId = hyperlinkStyleID;
	}
	private static String hyperlinkStyleId = null;
	
	
	public static void log(String message ) {
		
		log.info(message);
	}

	/* ---------------------------------------------------------------------------
	 * Apply bindings
	 * 
	 * There are only 2 things we need to do here:
	 * 
	 * 1. inject the XML (form data) into the package.
	 *    - this is simply a matter of attaching it to this part.
	 *    
	 * 2. copy the XML data into the sdt's, so it is there
	 *    for PDF, HTML output.  (we don't actually need to
	 *    do anything for it to appear in the Word 2007 UI - 
	 *    Word does this step itself).  This will be a new
	 *    static method in this class.
	 */
		
		public static void applyBindings(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

			// A component can apply in both the main document part,
			// and in headers/footers. See further
			// http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html
			
			if (hyperlinkStyleId !=null) {
					wordMLPackage.getMainDocumentPart().getPropertyResolver().activateStyle(hyperlinkStyleId);
			}			

			applyBindings(wordMLPackage.getMainDocumentPart());
	
			// Add headers/footers
			RelationshipsPart rp = wordMLPackage.getMainDocumentPart()
					.getRelationshipsPart();
			for (Relationship r : rp.getRelationships().getRelationship()) {
	
				if (r.getType().equals(Namespaces.HEADER)) {
					applyBindings((HeaderPart) rp.getPart(r));
				} else if (r.getType().equals(Namespaces.FOOTER)) {
					applyBindings((FooterPart) rp.getPart(r));
				}
			}
		}
	
		public static void applyBindings(JaxbXmlPart part) throws Docx4JException {
			
			org.docx4j.openpackaging.packages.OpcPackage pkg 
				= part.getPackage();		
				// Binding is a concept which applies more broadly
				// than just Word documents.
			
			if (hyperlinkStyleId !=null && pkg instanceof WordprocessingMLPackage) {
				((WordprocessingMLPackage)pkg).getMainDocumentPart().getPropertyResolver().activateStyle(hyperlinkStyleId);
			}
			
			org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
					part.getJaxbElement() ); 	
			
			XPathsPart xPathsPart = null;
			
			if ( ((WordprocessingMLPackage)pkg).getMainDocumentPart().getXPathsPart() == null) {
				log.error("OpenDoPE XPaths part missing");
				//throw new Docx4JException("OpenDoPE XPaths part missing");
			} else {
				xPathsPart = ((WordprocessingMLPackage)pkg).getMainDocumentPart().getXPathsPart();
				//log.debug(XmlUtils.marshaltoString(xPaths, true, true));
			}
			
			
			JAXBContext jc = Context.jc;
			try {
				// We used to use a JAXBResult, which 
				// but its better to use DOMResult
				// so we can use part.unmarshal, which should create a binder where possible
				DOMResult result = new DOMResult(); 
				
				Map<String, Object> transformParameters = new HashMap<String, Object>();
				transformParameters.put("customXmlDataStorageParts", 
						part.getPackage().getCustomXmlDataStorageParts());			
				transformParameters.put("wmlPackage", (WordprocessingMLPackage)pkg);			
				transformParameters.put("sourcePart", part);			
				transformParameters.put("xPathsPart", xPathsPart);			
						
				org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
				
				part.unmarshal( ((org.w3c.dom.Document)result.getNode()).getDocumentElement() );
			} catch (Exception e) {
				throw new Docx4JException("Problems applying bindings", e);			
			}
					
		}

		// These Store Item ID's come from Building Blocks.dotx glossary document
		// Of these, only CoverPageProps is documented as an "Office Well Defined Custom XML Part",
		// but even then, that documentation does not allocate a store item ID.
		public static final String CORE_PROPERTIES_STOREITEMID = 		"{6C3C8BC8-F283-45AE-878A-BAB7291924A1}";
		public static final String EXTENDED_PROPERTIES_STOREITEMID = 	"{6668398D-A668-4E3E-A5EB-62B293D839F1}";
		public static final String COVERPAGE_PROPERTIES_STOREITEMID = 	"{55AF091B-3C7A-41E3-B477-F2FDAA23CFDA}";
		
		
		/**
		 * Used by OpenDoPE handler, but not directly by bind.xslt anymore.
		 * Not multiLine aware.
		 * 
		 * @param customXmlDataStorageParts
		 * @param storeItemId
		 * @param xpath
		 * @param prefixMappings a string such as "xmlns:ns0='http://schemas.medchart'"
		 * @return
		 */
		public static String xpathGetString(
				WordprocessingMLPackage pkg, Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings) {
			
			try {
				
				if (storeItemId.toUpperCase().equals(CORE_PROPERTIES_STOREITEMID)  ) {
					
					return pkg.getDocPropsCorePart().xpathGetString(xpath, prefixMappings);
					
				} else if (storeItemId.toUpperCase().equals(EXTENDED_PROPERTIES_STOREITEMID) ) {
					
					return pkg.getDocPropsExtendedPart().xpathGetString(xpath, prefixMappings);
				} 
				
				CustomXmlDataStoragePart part  = customXmlDataStorageParts.get(storeItemId.toLowerCase());
					// Also handles cover page properties (since we've allocated it a store item id)
					// Note that Word does not create that part until the user provides one or more prop values
				
				if (part==null) {
					log.error("Couldn't locate part by storeItemId " + storeItemId);
					return null;
				}
				
				if (log.isDebugEnabled() ) {
					String r = part.getData().xpathGetString(xpath, prefixMappings);
					log.debug(xpath + " yielded result " + r);
					return r;
				} else {
					return part.getData().xpathGetString(xpath, prefixMappings);
				}
			} catch (Docx4JException e) {
				e.printStackTrace();
				return null;
			}
		}

		//&lt;html&gt;&lt;body&gt;  &lt;p&gt;hello &lt;/p&gt; &lt;/body&gt;&lt;/html&gt;
		
		/**
		 * Convert the input XHTML into a WordML w3c DocumentFragment, which Xalan 
		 * can insert into XSLT output.
		 *
		 * Note that the input XHTML must be suitable for the context 
		 * ie you can't insert block level stuff (eg p) into a run level sdt.
		 */
		public static DocumentFragment convertXHTML(
				WordprocessingMLPackage pkg, 
				JaxbXmlPart sourcePart,				
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				//String storeItemId, String xpath, String prefixMappings,
				XPathsPart xPathsPart,				
				String sdtParent,
				String contentChild,				
				NodeIterator rPrNodeIt, 
				String tag) {

			log.debug("convertXHTML extension function for: " + sdtParent + "/w:sdt/w:sdtContent/" + contentChild);
			
			QueryString qs = new QueryString();
			HashMap<String, String> map = qs.parseQueryString(tag, true);
			
			String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
			
			log.info("Looking for xpath by id: " + xpathId);
		
			
			Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
			
			if (xpath==null) {
				log.warn("Couldn't find xpath with id: " + xpathId);
				return null;
			}
			
			String storeItemId = xpath.getDataBinding().getStoreItemID();
			String xpathExp = xpath.getDataBinding().getXpath();
			String prefixMappings = xpath.getDataBinding().getPrefixMappings();
						
			String r = xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpathExp, prefixMappings);
			if (r==null) return null;
			
			try {
				//String unescaped = StringEscapeUtils.unescapeHtml(r);
				//log.info("Unescaped: " + unescaped);
				
				// It comes to us unescaped, so the above is unnecessary.

				org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
				DocumentFragment docfrag = docContainer.createDocumentFragment();
				
				XHTMLImporter.setHyperlinkStyle(hyperlinkStyleId);
				String baseUrl = null;
				List<Object> results = null;
				try {
					results = XHTMLImporter.convert(r, baseUrl, pkg );
				} catch (Exception e) {
					if (e instanceof NullPointerException) {
						((NullPointerException)e).printStackTrace();
					}
					log.error(e);
					log.error("with XHTML: " + r);
					//throw new Docx4JException("Problem converting XHTML", e);
					
					String errMsg = e.getMessage() + " with XHTML from " + xpathExp + " : " + r; 

					org.w3c.dom.Element wr = docContainer.createElementNS(Namespaces.NS_WORD12, "r");
					org.w3c.dom.Element wt = docContainer.createElementNS(Namespaces.NS_WORD12, "t");
					wt.setTextContent(errMsg);
					wr.appendChild(wt);
					
					if (sdtParent.equals("p")) {
						docfrag.appendChild(wr);
						return docfrag;
					} else if (sdtParent.equals("tbl")) {
						
						org.w3c.dom.Element wtr = docContainer.createElementNS(Namespaces.NS_WORD12, "tr");
						docfrag.appendChild(wtr);
						
						org.w3c.dom.Element wtc = docContainer.createElementNS(Namespaces.NS_WORD12, "tc");
						wtr.appendChild(wtc);
						
						org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
						wtc.appendChild(wp);
						
						wp.appendChild(wr);
						
						return docfrag;
					} else if (sdtParent.equals("tr")) {
						org.w3c.dom.Element wtc = docContainer.createElementNS(Namespaces.NS_WORD12, "tc");
						docfrag.appendChild(wtc);
						
						org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
						wtc.appendChild(wp);
						
						wp.appendChild(wr);
						return docfrag;
					} else if (sdtParent.equals("tc")) {
						org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
						docfrag.appendChild(wp);
						
						wp.appendChild(wr);
						return docfrag;
					} else {
						org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
						docfrag.appendChild(wp);
						
						wp.appendChild(wr);
						return docfrag;
	
					}
				}

				
				if (results==null) {
					log.error("Couldn't convert " + r);
					return docfrag;
				}
				
				log.info("Got results: " + results.size() );				
				if (results.size()>0  
						&& results.get(0) instanceof P
						&& sdtParent.equals("p")) {
					// Importer class always returns run-level content wrapped in a w:p 
					// so extract contents
					for (Object o : ((P)results.get(0)).getContent() ) {							
						Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(o);
						XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);													
					}
					
				} else {
	
					
					for(Object o : results) {
						
						String debug = XmlUtils.marshaltoString(o, true);
						log.debug("Conversion result: " + debug);
						
						Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(o);
						XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
						
					}
				}
				
				System.out.println("returning...");
				
				return docfrag;			
				
			} catch (Exception e) {
				log.error(e);
				return null;
			}
		}
		
		
		/**
		 */
		public static DocumentFragment xpathGenerateRuns(
				WordprocessingMLPackage pkg, 
				JaxbXmlPart sourcePart,				
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings,
				String sdtParent,
				String contentChild,				
				NodeIterator rPrNodeIt, boolean multiLine,
				String tag) {
			
			/**
			 * TODO test cases:
			 * 
			 * - multiline data, including cases which start/end with empty token
			 * - multiline data with w:multiLine absent or set to 0 ie false
			 * - cases with and without rPr
			 * - inline and block level sdt
			 */

			String r = xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
			if (r==null) return null;

			org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
			DocumentFragment docfrag = docContainer.createDocumentFragment();
			
			try {
				log.info(xpath + " yielded result " + r);
				
				RPr rPr = null;
				Node rPrNode = rPrNodeIt.nextNode();
				if (rPrNode!=null) {
					rPr = (RPr)XmlUtils.unmarshal(rPrNode);
				}

				org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
				
				StringTokenizer st = new StringTokenizer(r, "\n\r\f"); // tokenize on the newline character, the carriage-return character, and the form-feed character
				
				if (multiLine) {
					// our docfrag may contain several runs
					boolean firsttoken = true;
					while (st.hasMoreTokens()) {						
						String line = (String) st.nextToken();
						
						if (firsttoken) {
							firsttoken = false;
						} else {
							addBrRunToDocFrag(docfrag, rPr);
						}
						
						processString(sourcePart, docfrag, line, rPr);						
					}
					
				} else {
					// not multiline, so remove any CRLF in data;
					// our docfrag wil contain a single run
					StringBuilder sb = new StringBuilder();
					while (st.hasMoreTokens()) {						
						sb.append( st.nextToken() );
					}
					
					processString(sourcePart, docfrag, sb.toString(), rPr);
				}				
				
			} catch (Exception e) {
				log.error(e);
				return null;
			}
			
			return docfrag;			
		}

		private static void addBrRunToDocFrag(DocumentFragment docfrag, RPr rPr) throws JAXBException {
			
			// Not sure whether there is ever anything of interest in the rPr, 
			// but add it anyway
			org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			run.getRunContent().add(Context.getWmlObjectFactory().createBr());
			
			Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(run);
			XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
		}
		
		private static void processString(JaxbXmlPart sourcePart, DocumentFragment docfrag, String text, RPr rPr) throws JAXBException {
			
			// Since we'll calculate min, we don't want -1 for no match
			int NOT_FOUND = 99999;
			int pos1 = text.indexOf("http://")==-1 ? NOT_FOUND : text.indexOf("http://");
			int pos2 = text.indexOf("https://")==-1 ? NOT_FOUND : text.indexOf("https://");
			int pos3 = text.indexOf("mailto:")==-1 ? NOT_FOUND : text.indexOf("mailto:");
			
			int pos = Math.min(pos1,  Math.min(pos2, pos3));
			
			if (pos==NOT_FOUND || hyperlinkStyleId == null) {				
				addRunToDocFrag(sourcePart, docfrag,  text,  rPr);
				return;
			} 
			
			// There is a hyperlink to deal with
			
			if (pos==0) {
				int spacePos = text.indexOf(" ");
				if (spacePos==-1) {
					addHyperlinkToDocFrag(sourcePart, docfrag,  text);
					return;					
				}
				
				// Could contain more than one hyperlink, so process recursively					
				String first = text.substring(0, spacePos);
				String rest = text.substring(spacePos);
				
				addHyperlinkToDocFrag( sourcePart,  docfrag,  first);
				// .. now the recursive bit ..
				processString(sourcePart,  docfrag,  rest,  rPr);	
				return;
			}
			
			String first = text.substring(0, pos);
			String rest = text.substring(pos);
			
			addRunToDocFrag( sourcePart,  docfrag,  first, rPr);
			// .. now the recursive bit ..
			processString(sourcePart,  docfrag,  rest,  rPr);				
		}
		
		private static void addRunToDocFrag(JaxbXmlPart sourcePart, DocumentFragment docfrag, String string, RPr rPr) {
			
			org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
			run.getRunContent().add(text);
			if (string.startsWith(" ") || string.endsWith(" ") ) {
				// TODO: tab character?
				text.setSpace("preserve");
			}
			text.setValue(string);
						
			Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(run);
			
			// avoid WRONG_DOCUMENT_ERR: A node is used in a different document than the one that created it.
			// but  NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation. 
			// at com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.importNode
			// docfrag.appendChild(fragdoc.importNode(document, true));
			// so:			
			XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
		}
		
		private static void addHyperlinkToDocFrag(JaxbXmlPart sourcePart, DocumentFragment docfrag, String url) throws JAXBException {
			
			// We need to add a relationship to word/_rels/document.xml.rels
			// but since its external, we don't use the 
			// usual wordMLPackage.getMainDocumentPart().addTargetPart
			// mechanism
			org.docx4j.relationships.ObjectFactory factory =
				new org.docx4j.relationships.ObjectFactory();
			
			org.docx4j.relationships.Relationship rel = factory.createRelationship();
			rel.setType( Namespaces.HYPERLINK  );
			rel.setTarget(url);
			rel.setTargetMode("External");  
									
			sourcePart.getRelationshipsPart().addRelationship(rel);
			
			// addRelationship sets the rel's @Id
			
			String hpl = "<w:hyperlink r:id=\"" + rel.getId() + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >" +
            "<w:r>" +
            "<w:rPr>" +
            "<w:rStyle w:val=\"" + hyperlinkStyleId + "\" />" +  // TODO: enable this style in the document!
            "</w:rPr>" +
            "<w:t>" + url + "</w:t>" +
            "</w:r>" +
            "</w:hyperlink>";

			Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(
					(Hyperlink)XmlUtils.unmarshalString(hpl));
			XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
		}
		
		
		public static DocumentFragment xpathInjectImage(WordprocessingMLPackage wmlPackage,
				JaxbXmlPart sourcePart,
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings, 
				String sdtParent,
				String contentChild,
				String cx, String cy) {

			log.debug("sdt's parent: " + sdtParent);
			
			// TODO: remove any images in package which are no longer used.
			// Needs to be done once after BindingHandler has been done
			// for all parts for which it is to be called (eg mdp, header parts etc).
			
			CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
			if (part==null) {
				log.error("Couldn't locate part by storeItemId " + storeItemId);
				return null;
			}
			try {
				String r = part.getData().xpathGetString(xpath, prefixMappings);
				log.debug(xpath + " yielded result " + r);
				
				// Base64 decode it
				byte[] bytes = Base64.decodeBase64( r.getBytes("UTF8") );
				
				// Create image part and add it
		        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wmlPackage, sourcePart, bytes);
				
		        String filenameHint = null;
		        String altText = null;
		        int id1 = 0;
		        int id2 = 1;		        		
		        Inline inline = null;
		        long cxl = 0;
		        long cyl = 0;
		        try {
		        	cxl = Long.parseLong(cx);
		        	cyl = Long.parseLong(cy);
		        } catch (Exception e) {}
		        if (cxl==0 || cyl==0) {
		        	// Let BPAI work out size
		        	log.debug("image size - from image");
			        inline = imagePart.createImageInline( filenameHint, altText, 
			    			id1, id2, false);
		        } else {
		        	// Use existing size
		        	log.debug("image size - from content control size");
                    // Respect aspect ratio of injected image
                    ImageSize size = imagePart.getImageInfo().getSize();
                    double ratio = (double) size.getHeightPx() / (double) size.getWidthPx();
                    log.debug("fit ratio: " + ratio);
                    if (ratio > 1) {
                        cxl =  (long)((double) cyl / ratio);
                    } else {
                        cyl =  (long)((double) cxl * ratio);
                    }
			        inline = imagePart.createImageInline( filenameHint, altText, 
			    			id1, id2, cxl, cyl, false);		        	
		        }
		        
		        // Now add the inline in w:p/w:r/w:drawing
				org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
				org.docx4j.wml.Tc tc  = factory.createTc();
				org.docx4j.wml.P  p   = factory.createP();
				if (sdtParent.equals("tr")) {
					tc.getContent().add(p);
				}
				org.docx4j.wml.R  run = factory.createR();		
				if (sdtParent.equals("body")
						|| sdtParent.equals("tr") 
						|| sdtParent.equals("tc") ) {
					p.getContent().add(run);
				}
				org.docx4j.wml.Drawing drawing = factory.createDrawing();		
				run.getContent().add(drawing);		
				drawing.getAnchorOrInline().add(inline);
				
				
				/* return following node
				 * 
				 *     <w:p>
				          <w:r>
				            <w:drawing>
				              <wp:inline distT="0" distB="0" distL="0" distR="0">
				              	etc
 				 */
				
				//System.out.println(XmlUtils.marshaltoString(run, false));
				
				Document document = null;
				
				if (sdtParent.equals("body")
						|| sdtParent.equals("tc") ) {
					document = XmlUtils.marshaltoW3CDomDocument(p);
				} else if ( sdtParent.equals("tr") ) {
					document = XmlUtils.marshaltoW3CDomDocument(tc);
				} else if ( sdtParent.equals("p") ) {
					document = XmlUtils.marshaltoW3CDomDocument(run);
				} else if ( sdtParent.equals("sdtContent") ) {					
					log.info("contentChild: " + contentChild);
					if (contentChild.equals("p")) {
						p.getContent().add(run);
						document = XmlUtils.marshaltoW3CDomDocument(p);						
					} else if (contentChild.equals("r")) {
						document = XmlUtils.marshaltoW3CDomDocument(r);						
					} else {
						log.error("how to inject image for unexpected sdt's content: " + contentChild);					
					}
				} else {
					log.error("how to inject image for unexpected sdt's parent: " + sdtParent);
				}
				
				DocumentFragment docfrag = document.createDocumentFragment();
				docfrag.appendChild(document.getDocumentElement());

				return docfrag;
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} 
		}
		
		public static DocumentFragment xpathGenerateRuns(
				WordprocessingMLPackage pkg, 
				JaxbXmlPart sourcePart,				
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				XPathsPart xPathsPart,
				String odTag, 
				String sdtParent,
				String contentChild,				
				NodeIterator rPrNodeIt, boolean multiLine) {
			
			QueryString qs = new QueryString();
			HashMap<String, String> map = qs.parseQueryString(odTag, true);
			
			String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
			
			log.info("Looking for xpath by id: " + xpathId);
		
			
			Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
			
			if (xpath==null) {
				log.warn("Couldn't find xpath with id: " + xpathId);
				return null;
			}
			
			String storeItemId = xpath.getDataBinding().getStoreItemID();
			String xpathExp = xpath.getDataBinding().getXpath();
			String prefixMappings = xpath.getDataBinding().getPrefixMappings();
			
			return xpathGenerateRuns(
					 pkg, 
					 sourcePart,				
					 customXmlDataStorageParts,
					 storeItemId,  xpathExp,  prefixMappings,
					 sdtParent, contentChild,
					 rPrNodeIt,  multiLine, odTag);
			
		}
		
		

}
