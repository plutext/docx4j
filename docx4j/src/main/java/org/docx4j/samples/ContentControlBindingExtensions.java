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
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
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
public class ContentControlBindingExtensions {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/invoice.docx";
		String save_preprocessed = System.getProperty("user.dir") + "/sample-docs/databinding/invoice_preprocessed.xml";
		String save_bound = System.getProperty("user.dir") + "/sample-docs/databinding/invoice_bound.xml";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		

		// Process conditionals and repeats
		CustomXmlDataStoragePart.preprocess(wordMLPackage);
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(save_preprocessed);
		System.out.println("Saved: " + save_preprocessed);
		
		// Apply the bindings
		CustomXmlDataStoragePart.applyBindings(wordMLPackage.getMainDocumentPart());
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);
		saver.save(save_bound);
		System.out.println("Saved: " + save_bound);
		
	}
		

}
