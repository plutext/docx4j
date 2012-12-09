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

package org.docx4j.model.datastorage.migration;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;


/**
 * There are at least 3 approaches for replacing variables in 
 * a docx.
 * 
 * 1. as shown in this example
 * 2. using Merge Fields (see org.docx4j.model.fields.merge.MailMerger)
 * 3. binding content controls to an XML Part (via XPath)
 * 
 * Approach 3 is the recommended one when using docx4j. See the 
 * ContentControl* examples, Getting Started, and the subforum.
 * 
 * Approach 1, works in simple cases
 * only. 
 * 
 * It won't work if your KEY is split across separate
 * runs in your docx (which often happens), or if you want 
 * to insert images, or multiple rows in a table.
 * 
 * This class tidies up your document, so your keys should
 * not be split across separate runs.
 */
public class VariablePrepare {
	
	private static Logger log = Logger.getLogger(VariablePrepare.class);			
	
	/**
	 * @param wmlPackage
	 * @throws Exception
	 */
	public static void prepare(WordprocessingMLPackage wmlPackage) throws Exception {
	
		// Apply the filter
		WordprocessingMLPackage.FilterSettings filterSettings = new WordprocessingMLPackage.FilterSettings();
		filterSettings.setRemoveProofErrors(true);
		filterSettings.setRemoveContentControls(true);
		filterSettings.setRemoveRsids(true);
		wmlPackage.filter(filterSettings);
		// Note the filter is deprecated, since its questionable whether this
		// is important enough to live in WordprocessingMLPackage,
		// and in any case probably should be replaced with a TraversalUtil
		// approach (which wouldn't involve marshal/unmarshall, and 
		// so should be more efficient).

		log.info(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		// Now clean up some more
		org.docx4j.wml.Document wmlDocumentEl = wmlPackage.getMainDocumentPart().getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
						
		SingleTraversalUtilVisitorCallback paragraphVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilParagraphVisitor());
		paragraphVisitor.walkJAXBElements(body);
		
		log.info(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
	}
	

	public static class TraversalUtilParagraphVisitor extends TraversalUtilVisitor<P> {
		
		@Override
		public void apply(P p, Object parent, List<Object> siblings) {
			
			List<Object> existingContents = p.getContent();
			List<Object> newContents = new ArrayList<Object>();
			
			R currentR = null;
			String currentRPrString = null;
			
			// First join up runs with same run properties
			for (Object o : existingContents) {
				
				if (o instanceof R) {
					
					if (currentR==null) { // first object, or after something not a run
						currentR=(R)o;
						if (currentR.getRPr()!=null) {
							currentRPrString = XmlUtils.marshaltoString(currentR.getRPr(), true);
						}
						newContents.add(currentR);
					} else {
						RPr other = ((R)o).getRPr();
						
						boolean makeNewRun = true; // unless proven otherwise
						
						if (currentRPrString==null && other==null) makeNewRun=false;
						if (currentRPrString!=null && other!=null) {
							// Simple minded notion of equality
							if ( XmlUtils.marshaltoString(other, true).equals(currentRPrString) )  makeNewRun=false; 
						}
						
						if (makeNewRun) {
							currentR=(R)o;
							if (currentR.getRPr()==null) {
								currentRPrString = null;
							} else {
								currentRPrString = XmlUtils.marshaltoString(currentR.getRPr(), true);
							}
							newContents.add(currentR);
						} else {
							currentR.getContent().addAll( ((R)o).getContent() );
						}
					}
					
				} else {
					// not a run (eg w:ins) .. just add it and move on
					newContents.add(o);
					currentR = null;
					currentRPrString = null;
				}
				
			}
			
			// Now, in each run, join up adjacent text nodes
			for (Object o : newContents) {
				
				if (o instanceof R) {
					
					List<Object> newRunContents = new ArrayList<Object>();	
					Text currentT = null;
					for ( Object rc : ((R)o).getContent() ) {
						
						rc = XmlUtils.unwrap(rc);
						if (rc instanceof Text) {
							
							if (currentT==null) { // first object, or after something not a run
								currentT=(Text)rc;
								newRunContents.add(currentT);
							} else {
								String val = currentT.getValue();
								currentT.setValue(val + ((Text)rc).getValue() );								
							}
							
							// <w:t xml:space="preserve">
							if (((Text)rc).getSpace()!=null
									&& ((Text)rc).getSpace().equals("preserve")) { // any of them
								currentT.setSpace("preserve");
							}
							
						} else {
							System.out.println(rc.getClass().getName());
							// not text .. just add it and move on
							newRunContents.add(rc);
							currentT = null;
						}
					
					}
					
					((R)o).getContent().clear();
					((R)o).getContent().addAll(newRunContents);
					
				}
			
			}
			
			// Now replace w:p contents
			p.getContent().clear();
			p.getContent().addAll(newContents);
		}
	
	}
	
//	public static class MySingleTraversalUtilVisitorCallback extends SingleTraversalUtilVisitorCallback {
//		
//		public MySingleTraversalUtilVisitorCallback(TraversalUtilVisitor visitor) {
//			super(visitor);
//		}
//		
//		@Override
//		public boolean shouldTraverse(Object o) {
//			return !(o instanceof P); 
//		}
//		
//	}

	public static void main(String[] args) throws Exception {

		boolean save=true;
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/unmarshallFromTemplateDirtyExample.docx";
		
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// Before .. note attributes w:rsidRDefault="00D15781" w:rsidR="00D15781"
		System.out.println(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		prepare(wmlPackage);
		
		System.out.println(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));

		// Save it
		if (save) {
			SaveToZipFile saver = new SaveToZipFile(wmlPackage);
			saver.save(System.getProperty("user.dir") + "/OUT_VariablePrepare.docx");
			System.out.println("Saved");
		} 
	}

}
