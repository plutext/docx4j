package org.docx4j.model.datastorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
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
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

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

		public static DocumentFragment xpathInjectImage(WordprocessingMLPackage wmlPackage,
				JaxbXmlPart sourcePart,
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings, String sdtParent) {

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
		        Inline inline = imagePart.createImageInline( filenameHint, altText, 
		    			id1, id2, false);
		        
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
