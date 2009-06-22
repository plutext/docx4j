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


import java.io.FileInputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class CustomXmlBinding {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// Convenient to read from .xml file,
		// so it is easy to manually edit it (ie without having to unzip etc etc) 
		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/MedicalChartSample.xml";
		String itemId = "{DD6E220C-54BC-47B3-8AE8-A0A61D4934FF}";
		
//		boolean save = true;
//		String outputfilepath = "/home/dev/workspace/docx4j/sample-docs/MedicalChartSample-out.docx";		
		
		
		// Load the Package
		WordprocessingMLPackage wordMLPackage;
		if (inputfilepath.endsWith(".xml")) {
			
			JAXBContext jc = Context.jcXmlPackage;
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
					new javax.xml.transform.stream.StreamSource(new FileInputStream(inputfilepath)))).getValue(); 

			org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 

			wordMLPackage = (WordprocessingMLPackage)xmlPackage.get(); 
		
		} else {
			wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		}		
		
		
		// Get the part
		CustomXmlDataStoragePart customXmlDataStoragePart = wordMLPackage.getCustomXmlDataStorageParts().get(itemId);
		
		// Get the contents
		
		CustomXmlDataStorage customXmlDataStorage = customXmlDataStoragePart.getData();
		
		customXmlDataStorage.setNamespaceContext("FIXME");
		
		
		// Change its contents
		/*<chart xmlns="http://schemas.medchart" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		  <personal>
		    <id/>
		    <ssn/>
		    <name>Fred</name>
		    <address>Fitzroy</address>
		    <homePhone>9419</homePhone>
		    <insurance/>
		    <dob/>
		    <officePhone/>
		    <occupation/>
		    <employer/>
		    <photo/>
		    <maritalstatus/> 
		  </personal> ... */
		System.out.println(XmlUtils.w3CDomNodeToString(customXmlDataStorage.getDocument()));
		
		// Apply the bindings
		customXmlDataStoragePart.applyBindings(wordMLPackage.getMainDocumentPart());
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);
		
		// Display as a PDF
//		wordMLPackage.setFontMapper(new IdentityPlusMapper());		
//		org.docx4j.convert.out.pdf.PdfConversion c 
//			= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
//			c.view();
	}
		

}
