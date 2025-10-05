/*
 *  Copyright 2007-2025, Plutext Pty Ltd.
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
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;

/**
 * Example of how to remove w:pageBreakBefore in
 * 
        <w:p>
            <w:pPr>
                <w:pageBreakBefore/>
            </w:pPr>
 */
public class TraverseRemovePageBreakBefore {

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/fb2/2.docx";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		

		TraverseRemovePageBreakBefore remover = new TraverseRemovePageBreakBefore();
		remover.removePageBreaks(wordMLPackage);
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_TraverseRemovePageBreakBefore.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); 
		
		System.out.println("Saved: " + outputfilepath);
		
	}
	
	void removePageBreaks(WordprocessingMLPackage wordMLPackage) {

		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
						
		SingleTraversalUtilVisitorCallback pVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilPVisitor());
		pVisitor.walkJAXBElements(body);
		
		System.out.println("setPageBreakBefore(falseVal) " + TraversalUtilPVisitor.count + " times");
	}
	public static class TraversalUtilPVisitor extends TraversalUtilVisitor<P> {
		
		BooleanDefaultTrue falseVal;
		
		TraversalUtilPVisitor() {
			falseVal= new BooleanDefaultTrue();
			falseVal.setVal(Boolean.FALSE);
		}
		
		public static int count = 0;
		
		@Override
		public void apply(P p, Object parent, List<Object> siblings) {

			if (p.getPPr()!=null
					&& p.getPPr().getPageBreakBefore()!=null
							
					) {
				
				System.out.println(XmlUtils.marshaltoString(p.getPPr()));
				
				if (p.getPPr().getPageBreakBefore().isVal()) {
				
					p.getPPr().setPageBreakBefore(falseVal);
					System.out.println("setPageBreakBefore(falseVal)");
					count++;
				} else {
					System.out.println("setPageBreakBefore was already false ");
					
				}
								
			}
		}
	
	}	
		
}
