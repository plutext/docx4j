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

import java.util.StringTokenizer;

import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

public class IndentationTest {
	
	final static String EXPECT_START="[expect]";
	final static String EXPECT_END="[/expect]";	
	
    

	@Test	
	public void testIndents() throws Docx4JException {
		
    	String inputfilepath = System.getProperty("user.dir") + "/src/test/java/org/docx4j/model/listnumbering/NumberingIndents";
    	
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath + ".docx"));
	
		PropertyResolver propertyResolver = 
	    			wordMLPackage.getMainDocumentPart().getPropertyResolver();
		
		// Iterate through the paragraphs
		int assertionCount=0;
		for (Object o : wordMLPackage.getMainDocumentPart().getContent() ) {
		
			if (o instanceof P) {
				
				P p = (P)o;
				
				if (p.getPPr()!=null) {
					
    				PPr pPr = propertyResolver.getEffectivePPr(p.getPPr());  
					
    				if (pPr.getInd()!=null) {

    					String actual = removeNamespaces(XmlUtils.marshaltoString(pPr.getInd(), true));
				
    					Text text = (Text)XmlUtils.unwrap(
    							((R)p.getContent().get(0)).getContent().get(0));
    					String content = text.getValue();

    					// If contains [expect] [/expect], then test for this
    					if (content.contains(EXPECT_START)) {
    						int start = content.indexOf(EXPECT_START) + EXPECT_START.length();
    						int end = content.indexOf(EXPECT_END);
    						String expectedResult = content.substring(start, end);
    						
    						assertTrue("Expected " + expectedResult + " but got " + actual,
    								expectedResult.equals(actual));
    						assertionCount++;
    					}
    					
    				}
					
				}
				
			}
			
			
		}
		System.out.println("Assertions tested: " + assertionCount);
		assertTrue("No assertions were tested", assertionCount>0);
	}

	protected String removeNamespaces(String in) {

		
		StringTokenizer st = new StringTokenizer(in, " ");
		
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens() ) {
			
			String token = st.nextToken();
			if (token.contains("xmlns:")) {
				
				if (token.contains(">")) 
					sb.append(">");
				
			} else {
				sb.append(token + " ");
			}			
		}
		return sb.toString();
	}
	
}
