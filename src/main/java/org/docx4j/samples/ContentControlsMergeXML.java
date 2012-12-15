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
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.datastorage.OpenDoPEIntegrity;
import org.docx4j.model.datastorage.RemovalHandler;
import org.docx4j.model.datastorage.RemovalHandler.Quantifier;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.samples.ImageConvertEmbeddedToLinked.TraversalUtilBlipVisitor;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.SdtElement;


/** 
 * This sample demonstrates populating content controls
 * from a custom xml part (based on the xpaths given
 * in the content controls)
 * 
 * In this example, the XML part is injected at runtime,
 * and OpenDoPE extensions are supported (if present).
 * 
 * So this example is like 
 * See https://github.com/plutext/OpenDoPE-WAR/blob/master/webapp-simple/src/main/java/org/opendope/webapp/SubmitBoth.java
*/
public class ContentControlsMergeXML {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 
	
	private final static boolean DEBUG = true;
	private final static boolean SAVE = true;
	

	public static void main(String[] args) throws Exception {
			
		// the docx 'template'
		String input_DOCX = System.getProperty("user.dir") + "/sample-docs/word/databinding/binding-simple.docx";
		
		// the instance data
		String input_XML = System.getProperty("user.dir") + "/sample-docs/word/databinding/binding-simple-data.xml";
		
		// resulting docx
		String OUTPUT_DOCX = System.getProperty("user.dir") + "/OUT_ContentControlsMergeXML.docx";

		// Load input_template.docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(input_DOCX));
		
		// Find custom xml item id
		String itemId = getCustomXmlItemId(wordMLPackage).toLowerCase();
		System.out.println("Looking for item id: " + itemId);
		
		// Inject data_file.xml
		// (this code assumes it is not a StandardisedAnswersPart)
		CustomXmlDataStoragePart customXmlDataStoragePart 
			= (CustomXmlDataStoragePart)wordMLPackage.getCustomXmlDataStorageParts().get(itemId);
		if (customXmlDataStoragePart==null) {
			System.out.println("Couldn't find CustomXmlDataStoragePart! exiting..");
			return;			
		}
		System.out.println("Getting " + input_XML);
		FileInputStream fis = new FileInputStream(new File(input_XML));
		customXmlDataStoragePart.getData().setDocument(fis);
		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		try {
			// Process conditionals and repeats
			OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
			odh.preprocess();
			
			OpenDoPEIntegrity odi = new OpenDoPEIntegrity();
			odi.process(wordMLPackage);
			
			if (DEBUG) {
				String save_preprocessed; 						
				if (OUTPUT_DOCX.lastIndexOf(".")==-1) {
					save_preprocessed = OUTPUT_DOCX + "_INT.docx"; 
				} else {
					save_preprocessed = OUTPUT_DOCX.substring(0, OUTPUT_DOCX.lastIndexOf(".") ) + "_INT.docx"; 
				}
//				System.out.println(
//						XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
//						);		
				saver.save(save_preprocessed);
				System.out.println("Saved: " + save_preprocessed);
			}
			
		} catch (Docx4JException d) {
			// Probably this docx doesn't contain OpenDoPE convention parts
			System.out.println(d.getMessage());
		}
		
		
		// Apply the bindings
		//BindingHandler.setHyperlinkStyle("Hyperlink");
		BindingHandler.applyBindings(wordMLPackage);
		// If you inspect the output, you should see your data in 2 places:
		// 1. the custom xml part 
		// 2. (more importantly) the main document part
//		System.out.println(
//				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
//				);
		
		// Strip content controls: you MUST do this 
		// if you are processing hyperlinks
		RemovalHandler rh = new RemovalHandler();
		rh.removeSDTs(wordMLPackage, Quantifier.ALL);
		
		saver.save(OUTPUT_DOCX);
		System.out.println("Saved: " + OUTPUT_DOCX);
		
	}
	
	/**
	 * We need the item id of the custom xml part.  
	 * 
	 * There are several strategies we could use to find it,
	 * including searching the docx for a bind element, but
	 * here, we simply look in the xpaths part. 
	 * 
	 * @param wordMLPackage
	 * @return
	 */
	private static String getCustomXmlItemId(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();			
		if (wordMLPackage.getMainDocumentPart().getXPathsPart()==null) {
			// Can't do it the easy way, so look up the binding on the first content control
			TraversalUtilCCVisitor visitor = new TraversalUtilCCVisitor();
			SingleTraversalUtilVisitorCallback ccFinder 
			= new SingleTraversalUtilVisitorCallback(visitor);
			ccFinder.walkJAXBElements(
				wordMLPackage.getMainDocumentPart().getJaxbElement().getBody());
			return visitor.storeItemID;
			
		} else {
	
			org.opendope.xpaths.Xpaths xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart().getJaxbElement();
			return xPaths.getXpath().get(0).getDataBinding().getStoreItemID();
		}
	}

	public static class TraversalUtilCCVisitor extends TraversalUtilVisitor<SdtElement> {
		
		String storeItemID = null;
		
		@Override
		public void apply(SdtElement element, Object parent, List<Object> siblings) {

			if (element.getSdtPr()!=null
					&& element.getSdtPr().getDataBinding()!=null) {
				storeItemID = element.getSdtPr().getDataBinding().getStoreItemID();
			}
		}
	
	}
	
}
