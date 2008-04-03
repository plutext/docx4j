/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.samples;



import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


public class ExportInPackageFormat {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/fonts-modesOfApplication.docx";
				
		// Open a document from the file system
		// 1. Load the Package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// 2. Fetch the document part 		

	   	// Create a org.docx4j.wml.Package object
    	org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
    	org.docx4j.wml.Package pkg = factory.createPackage();
    	
    	// Set its parts
    	
    	// .. the main document part
    	org.docx4j.wml.Package.Part pkgPartDocument = factory.createPackagePart();
    	    	
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
    	pkgPartDocument.setName(documentPart.getPartName().getName());
    	pkgPartDocument.setContentType(documentPart.getContentType() );
		
    	org.docx4j.wml.Package.Part.XmlData XmlDataDoc = factory.createPackagePartXmlData();
    	
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		
		XmlDataDoc.setDocument(wmlDocumentEl);
		pkgPartDocument.setXmlData(XmlDataDoc);
		pkg.getPart().add(pkgPartDocument);
				
    	// .. the style part
    	org.docx4j.wml.Package.Part pkgPartStyles = factory.createPackagePart();

    	org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart stylesPart = documentPart.getStyleDefinitionsPart();
    	
    	pkgPartDocument.setName(stylesPart.getPartName().getName());
    	pkgPartDocument.setContentType(stylesPart.getContentType() );
    	
    	org.docx4j.wml.Package.Part.XmlData XmlDataStyles = factory.createPackagePartXmlData();
    	
    	org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)stylesPart.getJaxbElement();
    	
		XmlDataStyles.setStyles(styles);
		pkgPartStyles.setXmlData(XmlDataStyles);
		pkg.getPart().add(pkgPartStyles);    	
    	
    	// Now marshall it
		JAXBContext jc = Context.jc;
		Marshaller marshaller=jc.createMarshaller();
		
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", 
				new org.docx4j.jaxb.NamespacePrefixMapper() ); // Must use 'internal' for Java 6
		

		// Display its contents 
		System.out.println( "\n\n OUTPUT " );
		System.out.println( "====== \n\n " );	
		
		//org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();		
		marshaller.marshal(pkg, System.out);		
				
	}
	
	

}
