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

import javax.xml.bind.JAXBContext;

import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.datastorage.OpenDoPEIntegrity;
import org.docx4j.model.datastorage.OpenDoPEReverter;
import org.docx4j.model.datastorage.RemovalHandler;
import org.docx4j.model.datastorage.RemovalHandler.Quantifier;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


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
 * In practice you'll want the XML part to be injected at runtime.  
 * For that, see the sample: ContentControlsMergeXML
*/
public class ContentControlBindingExtensions {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	static String filepathprefix;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/invoice2.docx";
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/CountryRegions.xml";
		String inputfilepath = System.getProperty("user.dir") + "/Conditional cell formatting.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		
		filepathprefix = inputfilepath.substring(0, inputfilepath.lastIndexOf("."));
		System.out.println(filepathprefix);
		
		StringBuilder timingSummary = new StringBuilder();
		

		// Process conditionals and repeats
		long startTime = System.currentTimeMillis();
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		odh.preprocess();
		long endTime = System.currentTimeMillis();
		timingSummary.append("OpenDoPEHandler: " + (endTime-startTime));

		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);		
		
		startTime = System.currentTimeMillis();
		OpenDoPEIntegrity odi = new OpenDoPEIntegrity();
		odi.process(wordMLPackage);
		endTime = System.currentTimeMillis();
		timingSummary.append("\nOpenDoPEIntegrity: " + (endTime-startTime));
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(filepathprefix + "_preprocessed.docx");
		System.out.println("Saved: " + filepathprefix + "_preprocessed.docx");
		
		// Apply the bindings
		BindingHandler.setHyperlinkStyle("Hyperlink");						
		startTime = System.currentTimeMillis();
		BindingHandler.applyBindings(wordMLPackage.getMainDocumentPart());
		endTime = System.currentTimeMillis();
		timingSummary.append("\nBindingHandler.applyBindings: " + (endTime-startTime));
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);
		saver.save(filepathprefix + "_bound.docx");
		System.out.println("Saved: " + filepathprefix + "_bound.docx");
		
		// Either demonstrate reverter, or stripping of controls;
		// you can't do both. So comment out one or the other.
//		reverter(inputfilepath, filepathprefix + "_bound.docx");
//		
		// Strip content controls
		startTime = System.currentTimeMillis();
		RemovalHandler rh = new RemovalHandler();
		rh.removeSDTs(wordMLPackage, Quantifier.ALL);
		endTime = System.currentTimeMillis();
		timingSummary.append("\nRemovalHandler: " + (endTime-startTime));

		saver.save(filepathprefix + "_stripped.docx");
		System.out.println("Saved: " + filepathprefix + "_stripped.docx");
		
		System.out.println(timingSummary);
	}	
	
	public static void reverter(String inputfilepath, String instancePath) throws Docx4JException {
		
		WordprocessingMLPackage instancePkg = WordprocessingMLPackage.load(new java.io.File(instancePath));		
		
		OpenDoPEReverter reverter = new OpenDoPEReverter(
				WordprocessingMLPackage.load(new java.io.File(inputfilepath)), 
				instancePkg);
		
		System.out.println("reverted? " + reverter.revert() );
		
		SaveToZipFile saver = new SaveToZipFile(instancePkg);
		saver.save(filepathprefix + "_reverted.docx");
		System.out.println("Saved: " + filepathprefix + "_reverted.docx");
		
	}
				

}
