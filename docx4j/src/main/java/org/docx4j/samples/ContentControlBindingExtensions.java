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
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.model.datastorage.OpenDoPEHandler;
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
 * Word does this itself automatically, for if there is
 * a w:databinding element in the sdtPr.
 * 
 * However, out of the box, Word doesn't allow for
 * repeating things (table rows, paragraphs etc), nor
 * conditional inclusion/exclusion.
 * 
 * The OpenDoPE conventions support that; and this sample 
 * demonstrates docx4j's implementation of that.
 *  
 */
public class ContentControlBindingExtensions {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/invoice.docx";
		String save_preprocessed = System.getProperty("user.dir") + "/sample-docs/word/databinding/invoice_preprocessed.xml";
		String save_bound = System.getProperty("user.dir") + "/sample-docs/word/databinding/invoice_bound.xml";

//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/CountryRegions.xml";
//		String save_preprocessed = System.getProperty("user.dir") + "/sample-docs/word/databinding/CountryRegions_preprocessed.xml";
//		String save_bound = System.getProperty("user.dir") + "/sample-docs/word/databinding/CountryRegions_bound.xml";

//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/IT inventory.docx";
//		String save_preprocessed = System.getProperty("user.dir") + "/sample-docs/word/databinding/IT_inventory_preprocessed.xml";
//		String save_bound = System.getProperty("user.dir") + "/sample-docs/word/databinding/IT_inventory_bound.xml";
		
//		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/word/databinding/MedicalChartSample.docx";
//		String save_preprocessed = System.getProperty("user.dir") + "/sample-docs/word/databinding/MedicalChartSample_preprocessed.xml";
//		String save_bound = System.getProperty("user.dir") + "/sample-docs/word/databinding/MedicalChartSample_bound.xml";
		
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/repeat-containing-condition.docx";
//		String save_preprocessed = System.getProperty("user.dir") + "/sample-docs/word/databinding/repeat-containing-condition_preprocessed.xml";
//		String save_bound = System.getProperty("user.dir") + "/sample-docs/word/databinding/repeat-containing-condition_bound.xml";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		

		// Process conditionals and repeats
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		odh.preprocess();
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(save_preprocessed);
		System.out.println("Saved: " + save_preprocessed);
		
		// Apply the bindings
		BindingHandler.applyBindings(wordMLPackage.getMainDocumentPart());
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);
		saver.save(save_bound);
		System.out.println("Saved: " + save_bound);
		
	}
		

}
