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

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * Shows how to access a docx's:
 * - Core properties (Dublin Core title, creator etc) 
 * - Extended properties (app.xml)
 * - Custom properties (where you can store your own data, as shown here)
 * 
 * Note there is often also /word/settings.xml
 * (see also TemplateAttach for how to set a dotx in that part)
 * 
 */
public class DocProps extends AbstractSample {

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
			outputfilepath = System.getProperty("user.dir") + "/OUT_DocProps.docx";		
		}
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		// Let's look at the core properties
		org.docx4j.openpackaging.parts.DocPropsCorePart docPropsCorePart = wordMLPackage.getDocPropsCorePart();
		org.docx4j.docProps.core.CoreProperties coreProps = (org.docx4j.docProps.core.CoreProperties)docPropsCorePart.getJaxbElement();
		
		// What is the title of the document?
		// Note: Word for Mac 2010 doesn't set title
		String title = "Missing";
		List<String> list = coreProps.getTitle().getValue().getContent();
		if (list.size() > 0) {
			title = list.get(0);
		}
		System.out.println("'dc:title' is " + title);

		//System.out.println(coreProps.getTitle().getValue().getClass().getName() );
		// returns org.docx4j.docProps.core.dc.elements.SimpleLiteral as expected
		
		//System.out.println("'dcterms:created' is " + coreProps.getCreated().getClass().getName() );
		
		
		// Let's look at the extended properties
		org.docx4j.openpackaging.parts.DocPropsExtendedPart docPropsExtendedPart = wordMLPackage.getDocPropsExtendedPart();
		org.docx4j.docProps.extended.Properties extendedProps = (org.docx4j.docProps.extended.Properties)docPropsExtendedPart.getJaxbElement(); 
		
		// What application was the document created with?
		System.out.println("'Application' is " + extendedProps.getApplication() + " v." + extendedProps.getAppVersion());
		
		
		// Finally, the custom properties
		org.docx4j.openpackaging.parts.DocPropsCustomPart docPropsCustomPart = wordMLPackage.getDocPropsCustomPart();
		if(docPropsCustomPart==null){
			System.out.println("No DocPropsCustomPart");
		} else {
			org.docx4j.docProps.custom.Properties customProps = (org.docx4j.docProps.custom.Properties)docPropsCustomPart.getJaxbElement();
			
			for (org.docx4j.docProps.custom.Properties.Property prop: customProps.getProperty() ) {
				
				// At the moment, you need to know what sort of value it has.
				// Could create a generic Object getValue() method.
				if (prop.getLpwstr()!=null) {
					System.out.println(prop.getName() + " = " + prop.getLpwstr());
				} else {
					System.out.println(prop.getName() + ": \n " + XmlUtils.marshaltoString(prop, true, Context.jcDocPropsCustom));
				}
				
			}
			
			// Ok, let's add a custom property.
			org.docx4j.docProps.custom.ObjectFactory factory = new org.docx4j.docProps.custom.ObjectFactory();
			org.docx4j.docProps.custom.Properties.Property newProp = factory.createPropertiesProperty();
			
			// .. set it up
			newProp.setName("mynewcustomprop");
			newProp.setFmtid(docPropsCustomPart.fmtidValLpwstr ); // Magic string
			newProp.setPid( customProps.getNextId() ); 
			newProp.setLpwstr("SomeValue");
			
			// .. add it
			customProps.getProperty().add(newProp);
		}
		
		// Save the revised document		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
			System.out.println("Document saved as " + outputfilepath);
		}
		
	}

}
