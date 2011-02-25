package org.docx4j.model.datastorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.RPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class BindingHandler {
	
	private static Logger log = Logger.getLogger(BindingHandler.class);		
	
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
		
		public static void applyBindings(JaxbXmlPart part) throws Docx4JException {
			
			org.docx4j.openpackaging.packages.OpcPackage pkg 
				= part.getPackage();		
				// Binding is a concept which applies more broadly
				// than just Word documents.
			
			org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
					part.getJaxbElement() ); 	
			
			JAXBContext jc = Context.jc;
			try {
				// Use constructor which takes Unmarshaller, rather than JAXBContext,
				// so we can set JaxbValidationEventHandler
				Unmarshaller u = jc.createUnmarshaller();
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
				javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(u );
				
				Map<String, Object> transformParameters = new HashMap<String, Object>();
				transformParameters.put("customXmlDataStorageParts", 
						part.getPackage().getCustomXmlDataStorageParts());			
				transformParameters.put("wmlPackage", (WordprocessingMLPackage)pkg);			
				transformParameters.put("sourcePart", part);			
						
				org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
				
				part.setJaxbElement(result);
			} catch (Exception e) {
				throw new Docx4JException("Problems applying bindings", e);			
			}
					
		}
		
		/**
		 * Used by OpenDoPE handler, but not by bind.xslt anymore.
		 * Not multiLine aware.
		 * 
		 * @param customXmlDataStorageParts
		 * @param storeItemId
		 * @param xpath
		 * @param prefixMappings a string such as "xmlns:ns0='http://schemas.medchart'"
		 * @return
		 */
		public static String xpathGetString(Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings) {
			
			CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
			if (part==null) {
				log.error("Couldn't locate part by storeItemId " + storeItemId);
				return null;
			}
			try {
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

		
		/**
		 */
		public static DocumentFragment xpathGenerateRuns(Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings,
				NodeIterator rPrNodeIt, boolean multiLine) {
			
			/**
			 * TODO test cases:
			 * 
			 * - multiline data, including cases which start/end with empty token
			 * - multiline data with w:multiLine absent or set to 0 ie false
			 * - cases with and without rPr
			 * - inline and block level sdt
			 */

			CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
			if (part==null) {
				log.error("Couldn't locate part by storeItemId " + storeItemId);
				return null;
			}

			DocumentFragment docfrag = null; // = document.createDocumentFragment();
			Document fragdoc = null;
			
			try {
				String r = part.getData().xpathGetString(xpath, prefixMappings);
				log.debug(xpath + " yielded result " + r);
				
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

						org.docx4j.wml.R  run = factory.createR();		
						if (rPr!=null) {
							run.setRPr(rPr);
						}
						if (firsttoken) {
							firsttoken = false;
						} else {
							run.getRunContent().add(factory.createBr());
						}
						org.docx4j.wml.Text text = factory.createText();
						run.getRunContent().add(text);
						if (line.startsWith(" ") || line.endsWith(" ") ) {
							// TODO: tab character?
							text.setSpace("preserve");
						}
						text.setValue(line);
						
						Document document = XmlUtils.marshaltoW3CDomDocument(run);
						if (docfrag == null) { // will be for first line
							fragdoc = document;
							docfrag = document.createDocumentFragment();
							docfrag.appendChild(document.getDocumentElement());
						} else {
							// try to avoid WRONG_DOCUMENT_ERR: A node is used in a different document than the one that created it.
							// but  NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation. 
							// at com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.importNode
							// docfrag.appendChild(fragdoc.importNode(document, true));
							// so:
							XmlUtils.treeCopy(document.getDocumentElement(), docfrag);
						}
					}
					
				} else {
					// not multiline, so remove any CRLF in data;
					// our docfrag wil contain a single run
					StringBuilder sb = new StringBuilder();
					while (st.hasMoreTokens()) {						
						sb.append( st.nextToken() );
					}
					String line = sb.toString();
					
					org.docx4j.wml.R  run = factory.createR();		
					if (rPr!=null) {
						run.setRPr(rPr);
					}
					org.docx4j.wml.Text text = factory.createText();
					run.getRunContent().add(text);
					if (line.startsWith(" ") || line.endsWith(" ") ) {
						// TODO: tab character?
						text.setSpace("preserve");
					}
					text.setValue(line);
					
					Document document = XmlUtils.marshaltoW3CDomDocument(run);
					docfrag = document.createDocumentFragment();
					docfrag.appendChild(document.getDocumentElement());
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			return docfrag;			
		}
		
		public static DocumentFragment xpathInjectImage(WordprocessingMLPackage wmlPackage,
				JaxbXmlPart sourcePart,
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings, String sdtParent,
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
		        	cyl = Long.parseLong(cx);
		        } catch (Exception e) {}
		        if (cxl==0 || cxl==0) {
		        	// Let BPAI work out size
		        	log.debug("image size - from image");
			        inline = imagePart.createImageInline( filenameHint, altText, 
			    			id1, id2, false);
		        } else {
		        	// Use existing size
		        	log.debug("image size - from content control size");
			        inline = imagePart.createImageInline( filenameHint, altText, 
			    			id1, id2, cxl, cyl, false);		        	
		        }
		        
		        // Now add the inline in w:p/w:r/w:drawing
				org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
				org.docx4j.wml.Tc tc  = factory.createTc();
				org.docx4j.wml.P  p   = factory.createP();
				if (sdtParent.equals("tr")) {
					tc.getEGBlockLevelElts().add(p);
				}
				org.docx4j.wml.R  run = factory.createR();		
				if (sdtParent.equals("body")
						|| sdtParent.equals("tr") 
						|| sdtParent.equals("tc") ) {
					p.getParagraphContent().add(run);
				}
				org.docx4j.wml.Drawing drawing = factory.createDrawing();		
				run.getRunContent().add(drawing);		
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

	

}
