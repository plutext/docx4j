package org.docx4j.model.datastorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentPart;

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
		
		public static void applyBindings(DocumentPart documentPart) throws Docx4JException {
			
			// TODO: need to be able to apply to other parts eg header|footer
			
			// Get the package from documentPart
			org.docx4j.openpackaging.packages.OpcPackage pkg 
				= documentPart.getPackage();		
				// Binding is a concept which applies more broadly
				// than just Word documents.
			
			org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
					documentPart.getJaxbElement() ); 	
			
			JAXBContext jc = Context.jc;
			try {
				javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(jc );
				
				Map<String, Object> transformParameters = new HashMap<String, Object>();
				transformParameters.put("customXmlDataStorageParts", 
						documentPart.getPackage().getCustomXmlDataStorageParts());			
						
				org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
				
				//javax.xml.bind.JAXBElement je = (javax.xml.bind.JAXBElement)result.getResult();
				org.docx4j.wml.Document d = (org.docx4j.wml.Document)result.getResult();
				documentPart.setJaxbElement(d);
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


	

}
