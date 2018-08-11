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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.DocxFetcher;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.datastorage.OpenDoPEHandlerComponents;
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
public class ContentControlBindingExtensionsOld {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	static String filepathprefix;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/invoice.docx";
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/CountryRegions.xml";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		
		filepathprefix = inputfilepath.substring(0, inputfilepath.lastIndexOf("."));
		System.out.println(filepathprefix);
		
		StringBuilder timingSummary = new StringBuilder();
		
		// Process components
		
		// Docx4j 6.1: simplified component process model:
		// 1. components don't have to be at the top paragraph level of the content tree,
		// BUT:
		// 2. component processing is now done before condition/repeat processing
		// 3. component processing is not recursive anymore
		// 4. components typically use the "main" answer file
		long startTime = System.currentTimeMillis();
		OpenDoPEHandlerComponents componentsHandler =
				new OpenDoPEHandlerComponents(wordMLPackage);
		componentsHandler.setDocxFetcher(new MyDocxFetcher() );
		wordMLPackage = componentsHandler.fetchComponents();
		long endTime = System.currentTimeMillis();
		timingSummary.append("Component processing: " + (endTime-startTime));
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
				);	
		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(filepathprefix + "_components_done.docx");
		System.out.println("Saved: " + filepathprefix + "_components_done.docx");				

		// Process conditionals and repeats
		startTime = System.currentTimeMillis();
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		wordMLPackage = odh.preprocess();
		endTime = System.currentTimeMillis();
		timingSummary.append("\nOpenDoPEHandler: " + (endTime-startTime));
		
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
		saver = new SaveToZipFile(wordMLPackage);
		saver.save(filepathprefix + "_preprocessed.docx");
		System.out.println("Saved: " + filepathprefix + "_preprocessed.docx");
		
		// Apply the bindings
		BindingHandler.setHyperlinkStyle("Hyperlink");						
		startTime = System.currentTimeMillis();
		
		AtomicInteger bookmarkId = odh.getNextBookmarkId();
		BindingHandler bh = new BindingHandler(wordMLPackage);
		bh.setStartingIdForNewBookmarks(bookmarkId);
		bh.applyBindings(wordMLPackage.getMainDocumentPart());
		
		endTime = System.currentTimeMillis();
		timingSummary.append("\nBindingHandler.applyBindings: " + (endTime-startTime));
		String bound = XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true);
		System.out.println(
				);
		saver.save(filepathprefix + "_bound.docx");
		System.out.println("Saved: " + filepathprefix + "_bound.docx");
		
		// process altChunk
		if (bound.contains("altChunk")) {
			try {
				// Use reflection, so docx4j can be built
				// by users who don't have the MergeDocx utility
				Class<?> documentBuilder = Class
						.forName("com.plutext.merge.ProcessAltChunk");
				// Method method = documentBuilder.getMethod("merge",
				// wmlPkgList.getClass());
				Method[] methods = documentBuilder.getMethods();
				Method processMethod = null;
				for (int j = 0; j < methods.length; j++) {
	//				log.debug(methods[j].getName());
					if (methods[j].getName().equals("process")) {
						processMethod = methods[j];
					}
				}
				if (processMethod == null )
					throw new NoSuchMethodException();
				
				startTime = System.currentTimeMillis();
				
				wordMLPackage = (WordprocessingMLPackage) processMethod.invoke(null,
						wordMLPackage);
	
				endTime = System.currentTimeMillis();
				timingSummary.append("\nBindingHandler.applyBindings: " + (endTime-startTime));
				System.out.println(
						XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
						);
				saver.save(filepathprefix + "_altChunksMerged.docx");
				System.out.println("Saved: " + filepathprefix + "_altChunksMerged.docx");
	
				
			} catch (ClassNotFoundException e) {
			} catch (NoSuchMethodException e) {
			} catch (Exception e) {
				throw new Docx4JException("Problem processing w:altChunk", e);
			}	
		}
		
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
				
	public void extensionMissing(Exception e) {
		System.out.println("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		System.out.println("* You don't appear to have the MergeDocx paid extension,");
		System.out.println("* which is necessary to merge docx, or process altChunk.");
		System.out.println("* Purchases of this extension support the docx4j project.");
		System.out.println("* Please visit www.plutext.com if you want to buy it.");
	}
	
	
	static class MyDocxFetcher implements DocxFetcher {

		@Override
		public InputStream getDocxFromIRI(String iri) throws Docx4JException {
			
			try {
				return FileUtils.openInputStream(new File(iri));
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
		}
		
	}

}
