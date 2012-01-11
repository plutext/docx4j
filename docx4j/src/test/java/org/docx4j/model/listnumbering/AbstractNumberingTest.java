/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

public class AbstractNumberingTest {
	
	final static String EXPECT_START="[expect]";
	final static String EXPECT_END="[/expect]";	

	protected void testNumbering(WordprocessingMLPackage wordMLPackage) {
		NumberingDefinitionsPart ndp = wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart();
		
		Emulator listNumberingEmulator = ndp.getEmulator();
		
		// Iterate through the paragraphs
		int assertionCount=0;
		for (Object o : wordMLPackage.getMainDocumentPart().getContent() ) {
		
			if (o instanceof P) {
				
				P p = (P)o;
				
				if (p.getPPr()!=null) {
					
					// Set up values required for call to Emulator's getNumber
					String pStyleVal = null;
					if (p.getPPr().getPStyle()!=null) {
						pStyleVal = p.getPPr().getPStyle().getVal();
					}
					
					String numId = null;
					String levelId = null;
					if (p.getPPr().getNumPr()!=null) {
						if (p.getPPr().getNumPr().getNumId()!=null) {
							numId = p.getPPr().getNumPr().getNumId().getVal().toString();
						}
						if (p.getPPr().getNumPr().getIlvl()!=null) {
							levelId = p.getPPr().getNumPr().getIlvl().getVal().toString();
						}
					}
					
					ResultTriple rt = Emulator.getNumber(wordMLPackage, pStyleVal, numId, levelId);
					
					Text text = (Text)XmlUtils.unwrap(
							((R)p.getContent().get(0)).getContent().get(0));
					String content = text.getValue();
					
					// If contains [expect] [/expect], then test for this
					if (content.contains(EXPECT_START)) {
						int start = content.indexOf(EXPECT_START) + EXPECT_START.length();
						int end = content.indexOf(EXPECT_END);
						String expectedResult = content.substring(start, end);
						
						assertTrue("Expected " + expectedResult + " but got " + rt.numString,
								expectedResult.equals(rt.numString));
						assertionCount++;
					}
				}
				
			}
			
			
		}
		System.out.println("Assertions tested: " + assertionCount);
		assertTrue("No assertions were tested", assertionCount>0);
	}
	
}
