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
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


/**
 * This sample demonstrates populating content controls
 * from a custom xml part (based on the xpaths given
 * in the content controls).
 * 
 * Word does this itself automatically, but if you
 * have a Word document containing content controls,
 * this sample demonstrates how you could
 * populate those programmatically.  You might
 * then use docx4j to generate a pdf or an html.
 */
public class CustomXmlBinding {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String example_value_to_inject = "Flynnie";

		// Convenient to read from .xml file,
		// so it is easy to manually edit it (ie without having to unzip etc etc) 
		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/MedicalChartSample.xml";
//		String inputfilepath = System.getProperty("user.dir") + "/tmp/MedicalChartSample.docx";
			// To get the sample file, google for "Content Control Toolkit"
			// I've saved as xml, but docx works as well

		String outputfilepath = System.getProperty("user.dir") + "/tmp/MedicalChartSample-OUT.docx";
		
		String itemId = "{DD6E220C-54BC-47B3-8AE8-A0A61D4934FF}".toLowerCase();

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
			// In a real program what you might do is populate this with your own data.
			// You could replace the whole part, or as we show below, just set some
			// particular value
		
				
		// Change its contents
		((CustomXmlDataStorageImpl)customXmlDataStorage).setNodeValueAtXPath("/ns0:chart[1]/ns0:personal[1]/ns0:name[1]", example_value_to_inject,
				"xmlns:ns0='http://schemas.medchart'"); 
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
		BindingHandler.applyBindings(wordMLPackage.getMainDocumentPart());
		
		// If you inspect the output, you should see your data in 2 places:
		// 1. the custom xml part 
		// 2. (more importantly) the main document part
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);
		
		// Display as a PDF
//		wordMLPackage.setFontMapper(new IdentityPlusMapper());		
//		org.docx4j.convert.out.pdf.PdfConversion c 
//			= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
//			c.view();
		
		wordMLPackage.save(new java.io.File(outputfilepath) );
		System.out.println("..done");
	}
		

}
