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


import java.util.List;

import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * CustomXmlDataStoragePart contains user custom xml, 
 * to which content control bindings can point.
 * 
 * This is the modern best practice approach for
 * injecting text/data into a docx.
 * 
 * See the Getting Started guide for a general overview,
 * and additional references, or use the docx4j
 * website to search for "custom xml binding".
 * 
 * Once you have included this part, and bound content
 * controls to it, nothing further needs to be done for
 * Word to display your data (unless you are using
 * conditional|repeat - see below).
 * 
 * However, if you want your data to show up in
 * docx4j PDF|HTML output, you need to run the 
 * BindingHandler.applyBindings method first. (TODO: do this 
 * automatically) 
 * 
 * The actual XML data is stored in a CustomXmlDataStorage
 * object, for which accessor is get|setData.  (Ths is
 * useful if you want to generate n docx documents, each
 * with different XML.)
 * 
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
 * This class supports content control extensions
 * (ie od:condition, od:repeat). Use the 
 * OpenDoPEHandler class to process these.
 * 
 * @author jharrop
 *
 */
public final class CustomXmlDataStoragePart extends Part implements CustomXmlPart {
	
	private static Logger log = LoggerFactory.getLogger(CustomXmlDataStoragePart.class);		
	
	public CustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
	public CustomXmlDataStoragePart() throws InvalidFormatException {
		super(new PartName("/customXml/item1.xml"));
		init();
	}

	/**
	 * @param parts The parts present in the package to which this will be added.
	 * If for example /customXml/item1.xml already exists, this allows
	 * the name /customXml/item2.xml to be generated.
	 * @throws InvalidFormatException
	 */
	@Deprecated // since we now have AddPartBehaviour.RENAME_IF_NAME_EXISTS
	public CustomXmlDataStoragePart(Parts parts) throws InvalidFormatException {
		
		int partNum = 1;
		if (parts!=null) {
			while (parts.get(new PartName("/customXml/item" + partNum + ".xml"))!=null) {
				partNum++;			
			}
		}
		
		this.setPartName(new PartName("/customXml/item" + partNum + ".xml"));
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

	private CustomXmlDataStorage data;  // probably org.docx4j.model.datastorage.CustomXmlDataStorageImpl
	/**
	 * @return the data
	 */
	public CustomXmlDataStorage getData() {
//		if (log.isDebugEnabled()) {
//			log.debug(data.getClass().getName());
//		}
		// typically org.docx4j.model.datastorage.CustomXmlDataStorageImpl
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(CustomXmlDataStorage data) {
		this.data = data;
	}

    public boolean isContentEqual(Part other) throws Docx4JException {

    	if (!(other instanceof CustomXmlDataStoragePart))
    		return false;
    	
    	Document doc1 = data.getDocument();
    	Document doc2 = ((CustomXmlDataStoragePart)other).data.getDocument();
    	
    	return doc1.isEqualNode(doc2);

    }

	@Override
	public String xpathGetString(String xpath, String prefixMappings)
			throws Docx4JException {
		return getData().xpathGetString(xpath, prefixMappings);
	}
	
	/**
	 * @since 3.3.1
	 */
	@Override
	public String cachedXPathGetString(String xpath, String prefixMappings) throws Docx4JException {
		return getData().cachedXPathGetString(xpath, prefixMappings);		
	}
	
	@Override
	public void discardCacheXPathObject() {
		getData().discardCacheXPathObject();
		
	}	

	@Override
	public List<Node> xpathGetNodes(String xpathString, String prefixMappings)
			throws Docx4JException {
		return getData().xpathGetNodes(xpathString, prefixMappings);
	}

	@Override
	public boolean setNodeValueAtXPath(String xpath, String value, String prefixMappings) throws Docx4JException {
		return data.setNodeValueAtXPath(xpath, value, prefixMappings);
		
	}
	
	/**
	 * Get the XML as a String.
	 * 
	 * @since 3.0.1
	 */
	public String getXML() throws Docx4JException {
		return data.getXML();
	}

	/**
	 * Set the XML
	 * 
	 * @since 6.0.0
	 */
	@Override
	public void setXML(Document xmlDocument) throws Docx4JException {
		getData().setDocument(xmlDocument);
	}
	
	/**
	 * @since 3.0.2
	 */
	public String getItemId() {
		
		if (this.getRelationshipsPart()==null) { 
			return null; 
		} else {
			// Look in its rels for rel of @Type customXmlProps (eg @Target="itemProps1.xml")
			Relationship r = this.getRelationshipsPart().getRelationshipByType(
					Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES);
			if (r==null) {
				log.warn(".. but that doesn't point to a  customXmlProps part");
				return null;
			}
			CustomXmlDataStoragePropertiesPart customXmlProps = 
				(CustomXmlDataStoragePropertiesPart)this.getRelationshipsPart().getPart(r);
			if (customXmlProps==null) {
				log.warn(".. but the target seems to be missing?");
				return null;
			} else {
				return customXmlProps.getItemId().toLowerCase();
			}
		}
	}	
	
    /**
     * Remove this part from the pkg. Beware: it is up to you to make sure
     * your content doesn't rely on this part being present!  A symptom of
     * that would be that Office now reports your file to be corrupt or in 
     * need of repair.   
     * 
     * @since 3.0.2
     */
	@Override	
    public void remove() {
		
		String itemId = this.getItemId(); 
		if (itemId!=null) {
			log.debug("removing from CustomXmlDataStorageParts " + itemId);
			this.getPackage().getCustomXmlDataStorageParts().remove(itemId);
		}		
		
		super.remove();
    	
    }

}
