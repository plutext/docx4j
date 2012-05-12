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


package org.docx4j.samples;

import java.io.File;
import java.util.UUID;

import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;

/**
 * Creates a docx containing a CustomXml part.
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CreateDocxWithCustomXml {

	public static void main(String[] args) throws Exception {
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		wordMLPackage.getMainDocumentPart()
			.addStyledParagraphOfText("Title", "Hello world");

		wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");
		
	    			
		CustomXmlDataStoragePart customXmlDataStoragePart = injectCustomXmlDataStoragePart(wordMLPackage.getMainDocumentPart(),
				wordMLPackage.getParts() );
		
		addProperties(customXmlDataStoragePart);
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/customxml2.docx") );
		
		System.out.println("Done.");
				
	}
	
	
	public static CustomXmlDataStoragePart injectCustomXmlDataStoragePart(Part parent, Parts parts) throws Exception {
		
			org.docx4j.openpackaging.parts.CustomXmlDataStoragePart customXmlDataStoragePart = 
				new org.docx4j.openpackaging.parts.CustomXmlDataStoragePart(parts);
				// Defaults to /customXml/item1.xml
			
			CustomXmlDataStorage data = new CustomXmlDataStorageImpl();
			data.setDocument(createCustomXmlDocument());
			
			customXmlDataStoragePart.setData(data);
			
//			customXmlDataStoragePart.setDocument( createCustomXmlDocument() );
					
			parent.addTargetPart(customXmlDataStoragePart);

			return customXmlDataStoragePart;

	}
	
	public static void addProperties(CustomXmlDataStoragePart customXmlDataStoragePart) throws InvalidFormatException {

		CustomXmlDataStoragePropertiesPart part = new CustomXmlDataStoragePropertiesPart();
		
		org.docx4j.customXmlProperties.ObjectFactory of = new org.docx4j.customXmlProperties.ObjectFactory();
		
		DatastoreItem dsi = of.createDatastoreItem();
		String newItemId = "{" + UUID.randomUUID().toString() + "}";					
		dsi.setItemID(newItemId);
		
		part.setJaxbElement(dsi );
		
		customXmlDataStoragePart.addTargetPart(part);
	}
	
	public static org.w3c.dom.Document createCustomXmlDocument() {
		
		// TODO: implement
		
		return null;
		
	}
	
}
