/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.openpackaging.parts;


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
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


public final class CustomXmlDataStoragePart extends Part {
	
	private static Logger log = Logger.getLogger(CustomXmlDataStoragePart.class);		
	
	public static void log(String message ) {
		
		log.info(message);
	}
	
	/*
	 * If this contains XML which is bound in an sdt
	 * via w:sdtPr/w:dataBinding, then its rels
	 * will point to CustomXmlDataStoragePropertiesPart
	 * which gives its datastore itemID.
	 * 
	 * (The datastore itemID is used in the w:dataBinding)
	 *  
	 * The Package contains a hashmap<String, CustomXmlDataStoragePart>
	 * to make it easy to get the part to which we apply the 
	 * XPath.  The part is registered as the document is loaded.
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
	 *    
	 * A user who wishes to set up the bindings is advised
	 * to use the Content Control Toolkit.
	 */
	
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

	public CustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
//	public CustomXmlDataStoragePart() throws InvalidFormatException {
//		super(new PartName("/customXml/item1.xml"));
//		init();
//	}

	/**
	 * @param parts The parts present in the package to which this will be added.
	 * If for example /customXml/item1.xml already exists, this allows
	 * the name /customXml/item2.xml to be generated.
	 * @throws InvalidFormatException
	 */
	public CustomXmlDataStoragePart(Parts parts) throws InvalidFormatException {
		
		int partNum = 1;
		if (parts!=null) {
			while (parts.get(new PartName("/customXml/item" + partNum + ".xml"))!=null) {
				partNum++;			
			}
		}
		
		this.partName = new PartName("/customXml/item" + partNum + ".xml");
		log.info("Using PartName /customXml/item" + partNum + ".xml");
		init();
	}
	
	
	public void init() {		
	
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
		
	}

	private CustomXmlDataStorage data;
	/**
	 * @return the data
	 */
	public CustomXmlDataStorage getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(CustomXmlDataStorage data) {
		this.data = data;
	}
	
	
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
	public static String getXPath(HashMap<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings) {
		
		CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId);
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			if (log.isDebugEnabled() ) {
				String r = part.getData().getXPath(xpath, prefixMappings);
				log.debug(xpath + " yielded result " + r);
				return r;
			} else {
				return part.getData().getXPath(xpath, prefixMappings);
			}
		} catch (Docx4JException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/* ---------------------------------------------------------------------------
	 * Pre-processing of content controls which have a tag containing "bindingrole"
	 * 
	 * 
	 */
	
}
