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


import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class DocProps extends AbstractSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {


		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/docProps.docx";		
		}
		
		
		boolean save = false;
		try {
			getOutputFilePath(args);
			save = true;
		} catch (IllegalArgumentException e) {
			outputfilepath = System.getProperty("user.dir") + "/sample-docs/docProps-out.docx";		
			
//			save = true;
		}
		
		// Open a document from the file system
		// 1. Load the Package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		// Let's look at the core properties
		org.docx4j.openpackaging.parts.DocPropsCorePart docPropsCorePart = wordMLPackage.getDocPropsCorePart();
		org.docx4j.docProps.core.CoreProperties coreProps = (org.docx4j.docProps.core.CoreProperties)docPropsCorePart.getJaxbElement();
		
		// What is the title of the document?
		System.out.println("'dc:title' is " + coreProps.getTitle().getValue().getContent().get(0));
			// Yuck! TODO: Simplify the dc schema.

		System.out.println(coreProps.getTitle().getValue().getClass().getName() );
		// returns org.docx4j.docProps.core.dc.elements.SimpleLiteral as expected
		
		System.out.println("'dcterms:created' is " + coreProps.getCreated().getClass().getName() );
		
		
		//System.out.println(coreProps.getTitle().getValue() instanceof )
		
		
		// Let's look at the extended properties
		org.docx4j.openpackaging.parts.DocPropsExtendedPart docPropsExtendedPart = wordMLPackage.getDocPropsExtendedPart();
		org.docx4j.docProps.extended.Properties extendedProps = (org.docx4j.docProps.extended.Properties)docPropsExtendedPart.getJaxbElement(); 
		
		// What application was the document created with?
		System.out.println("'Application' is " + extendedProps.getApplication() + " v." + extendedProps.getAppVersion());
		
		
		// Finally, the custom properties
		org.docx4j.openpackaging.parts.DocPropsCustomPart docPropsCustomPart = wordMLPackage.getDocPropsCustomPart();
		org.docx4j.docProps.custom.Properties customProps = (org.docx4j.docProps.custom.Properties)docPropsCustomPart.getJaxbElement();
		
		for (org.docx4j.docProps.custom.Properties.Property prop: customProps.getProperty() ) {
			
			System.out.println(prop.getName());
			
			// At the moment, you need to know what sort of value it has.
			// Could create a generic Object getValue() method.
			
			System.out.println(prop.getLpwstr());
			
		}
		
		// Ok, let's add one.
		org.docx4j.docProps.custom.ObjectFactory factory = new org.docx4j.docProps.custom.ObjectFactory();
		org.docx4j.docProps.custom.Properties.Property newProp = factory.createPropertiesProperty();
		
		// .. set it up
		newProp.setName("mynewcustomprop");
		newProp.setFmtid(docPropsCustomPart.fmtidValLpwstr ); // Magic string
		newProp.setPid( customProps.getNextId() ); 
		newProp.setLpwstr("SomeValue");
		
		// .. add it
		customProps.getProperty().add(newProp);
		
		/* Example of setting some extended properties
		DocPropsExtendedPart extendedPart = new DocPropsExtendedPart(); 
		wordMLPackage.addTargetPart(extendedPart);
		// Add content type
		ContentTypeManager ctm = wordMLPackage.getContentTypeManager();
		ctm.addOverrideContentType(extendedPart.getPartName().getURI(), extendedPart.getContentType());
		// Now the properties
		org.docx4j.docProps.extended.ObjectFactory factory = new org.docx4j.docProps.extended.ObjectFactory();
		Properties props = factory.createProperties();
		props.setApplication("Microsoft Word 12.1.0");
		props.setAppVersion("12.0256");	
		extendedPart.setJaxbElement(props);`
		//Required for docx4j < 2.2.0
		extendedPart.setJAXBContext(Context.jcDocPropsExtended);
		*/		
				
		// Save the revised document
		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
			System.out.println("Document saved as " + outputfilepath);
		}
		
		System.out.println("Done.");
		
	}
	
	

}
