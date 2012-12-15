package org.docx4j.model.datastorage;

import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
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
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTSdtDate;
import org.docx4j.wml.Color;
import org.docx4j.wml.P;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.P.Hyperlink;
import org.opendope.xpaths.Xpaths.Xpath;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class BindingTraverserXSLT implements BindingTraverserInterface {
	
	private static Logger log = Logger.getLogger(BindingTraverserXSLT.class);		
	

	static Templates xslt;			
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
		
	}
	
	
	/**
	 * @param part
	 * @param pkg
	 * @param doc
	 * @param xPathsPart
	 * @throws Docx4JException
	 */
	public Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			XPathsPart xPathsPart)
			throws Docx4JException {
		
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				part.getJaxbElement() ); 	
		
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
			
			//part.unmarshal( ((org.w3c.dom.Document)result.getNode()).getDocumentElement() );
			return XmlUtils.unmarshal(((org.w3c.dom.Document)result.getNode()) );
			
		} catch (Exception e) {
			throw new Docx4JException("Problems applying bindings", e);			
		}
	}
	
	public static void log(String message ) {
		
		log.info(message);
	}

	public static void log(NodeIterator nodeIterator ) {
		
		Node n = nodeIterator.nextNode();		
		log.info(XmlUtils.w3CDomNodeToString(n));
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
			Map<String, CustomXmlPart> customXmlDataStorageParts,
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
					
		String r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpathExp, prefixMappings);
		if (r==null) return null;
		
		try {
			//String unescaped = StringEscapeUtils.unescapeHtml(r);
			//log.info("Unescaped: " + unescaped);
			
			// It comes to us unescaped, so the above is unnecessary.

			org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
			DocumentFragment docfrag = docContainer.createDocumentFragment();
			
			XHTMLImporter.setHyperlinkStyle(BindingHandler.getHyperlinkStyleId());
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
			Map<String, CustomXmlPart> customXmlDataStorageParts,
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

		String r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
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
		
		if (pos==NOT_FOUND || BindingHandler.getHyperlinkStyleId() == null) {				
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
        "<w:rStyle w:val=\"" + BindingHandler.getHyperlinkStyleId() + "\" />" +  // TODO: enable this style in the document!
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
			String xpResult = part.getData().xpathGetString(xpath, prefixMappings);
			log.debug(xpath + " yielded result " + xpResult);
			
			// Base64 decode it
			byte[] bytes = Base64.decodeBase64( xpResult.getBytes("UTF8") );
			
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
					document = XmlUtils.marshaltoW3CDomDocument(run);						
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
			Map<String, CustomXmlPart> customXmlDataStorageParts,
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
	
	public static String getRepeatPositionCondition(
			XPathsPart xPathsPart,				
			String odTag) {

		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(odTag, true);
		
		String xpathId = map.get("od:RptPosCon");
		
		log.info("Looking for xpath by id: " + xpathId);
		Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
		
		String expression =xpath.getDataBinding().getXpath() ;
		log.info(expression);

		return expression;		
	}
	
	public static DocumentFragment nullResultParagraph(String sdtParent, String message) {

		try
		{
			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			org.docx4j.wml.R  run = factory.createR();	
			org.docx4j.wml.Text text = factory.createText();
			text.setValue(message);
			run.getContent().add(text);
				
			org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
			if (sdtParent.equals("p")) {
				// Stuff it in a run
				docContainer = XmlUtils.marshaltoW3CDomDocument(run);						
			} else {
				// Stuff it in a p
				org.docx4j.wml.P  p   = factory.createP();
				p.getContent().add(run);
				docContainer = XmlUtils.marshaltoW3CDomDocument(p);						
			}
			
			DocumentFragment docfrag = docContainer.createDocumentFragment();
			docfrag.appendChild(docContainer.getDocumentElement());
		
			return docfrag;
			
		} catch (Exception e) {
			log.error(e);
			return null;
		}
		
	}
	
	public static DocumentFragment xpathDate(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings, 
			String sdtParent,
			String contentChild,
			NodeIterator dateNodeIt) {
		
		CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		
		try {
			String r = part.getData().xpathGetString(xpath, prefixMappings);
			log.debug(xpath + " yielded result " + r);
			if (r==null) return nullResultParagraph(sdtParent, "[missing!]");
			
			CTSdtDate sdtDate = null;
			Node dateNode = dateNodeIt.nextNode();
			if (dateNode!=null) {
				//sdtDate = (CTSdtDate)XmlUtils.unmarshal(dateNode);
				sdtDate = (CTSdtDate)XmlUtils.unmarshal(dateNode, Context.jc, CTSdtDate.class);
			}
			
			/*
		        <w:date w:fullDate="2012-08-19T00:00:00Z">
		          <w:dateFormat w:val="d/MM/yyyy"/>
		          <w:lid w:val="en-AU"/>
		          <w:storeMappedDataAs w:val="dateTime"/>
		          <w:calendar w:val="gregorian"/>
		        </w:date>
		        
		        Assume our String r contains something like "2012-08-19T00:00:00Z"
		        
		        We need to convert it to the given dateFormat string.
		        
			 */
			// Drop the Z
			if (r.indexOf("Z")>0) {
				r = r.substring(0, r.indexOf("Z")-1);
				log.warn("date now " + r);
			}
			
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			
			String format = sdtDate.getDateFormat().getVal();
			System.out.println("Using format: " + format);
			
			// C# dddd (eg "Monday') needs translation
			// to "EEEE"
			if (format.contains("dddd")) {
				format = format.replace("dddd", "EEEE");
			}
			
			Format formatter = new SimpleDateFormat(format);
			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			
			Date date;
			RPr rPr = null;
			try {
				date = (Date)dateTimeFormat.parse(r);
			} catch (ParseException e) {
				try {
					// 2012-08-28
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					date = (Date) dateFormat.parse(r);
				} catch (ParseException e2) {
					log.warn(e.getMessage());
					date = new Date();

					// <w:color w:val="FF0000"/>
					rPr = factory.createRPr();
					Color colorRed = factory.createColor();
					colorRed.setVal("FF0000");
					rPr.setColor(colorRed);
				}
			}

			String result = formatter.format(date);
			
			org.docx4j.wml.R  run = factory.createR();	
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			org.docx4j.wml.Text text = factory.createText();
			text.setValue(result);
			run.getContent().add(text);
				
			org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
			if (sdtParent.equals("p")) {
				// Stuff it in a run
				docContainer = XmlUtils.marshaltoW3CDomDocument(run);						
			} else {
				// Stuff it in a p
				org.docx4j.wml.P  p   = factory.createP();
				p.getContent().add(run);
				docContainer = XmlUtils.marshaltoW3CDomDocument(p);						
			}
			
			DocumentFragment docfrag = docContainer.createDocumentFragment();
			docfrag.appendChild(docContainer.getDocumentElement());
		
			return docfrag;
			
		} catch (Exception e) {
			log.error(e);
			return null;
		}
		
	}	
}
