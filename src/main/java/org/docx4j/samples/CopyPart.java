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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.docx4j.JcrNodeMapper.NodeMapper;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.dml.Inline;
import org.docx4j.docProps.extended.Properties;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.io.SaveToZipFile;

import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * Import foreign parts 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CopyPart {

	public static void main(String[] args) throws Exception {
		
		// Source package		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/embedded-spreadsheet.docx";
		WordprocessingMLPackage wordMLPackage1;// = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		if (inputfilepath.endsWith(".xml")) {
			// You can create one of these in Word 2007, by 
			// choosing Save As .xml
			// These are easier to look at / edit in a text editor than a zipped up docx
			JAXBContext jc = Context.jcXmlPackage;
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
					new javax.xml.transform.stream.StreamSource(new FileInputStream(inputfilepath)))).getValue(); 

			org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 

			wordMLPackage1 = (WordprocessingMLPackage)xmlPackage.get(); 
		
		} else {
			// Its just a docx
			wordMLPackage1 = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		}
		
		// Destination
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage2 = WordprocessingMLPackage.createPackage();
		
		// Find the part we want to copy
		RelationshipsPart rp = wordMLPackage1.getMainDocumentPart().getRelationshipsPart();
		Relationship rel = rp.getRelationshipByType(Namespaces.EMBEDDED_PKG);		
		Part p = rp.getPart(rel);
		
		System.out.println(p.getPartName().getName() );
		
		p.setPartName( new PartName("/word/embeddings/MySpreadsheet.xlsx") );
		
//		System.out.println(p.getContentType() );
//		System.out.println(p.getRelationshipType() );
		
		// Now try adding it
		wordMLPackage2.getMainDocumentPart().addTargetPart(p);
		System.out.println("Done" );
		
//		p.setOwningRelationshipPart(owningRelationshipPart)
//		p.setPackage(pack)
		
				
	}
	
}
