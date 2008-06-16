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
package org.docx4j.diff;

import static org.junit.Assert.*;
import org.docx4j.diff.ParagraphDifferencer;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.wml.P;
import org.junit.Test;

public class ParagraphDifferencerTest {
	
	final static String BASE_DIR = "/home/dev/workspace/docx4j/src/test/java/org/docx4j/diff/";

	final static String[] testparagraphs = { "t2R", "t2RR", "t3L", "t3R", "t4"}; // "t1L", "t1R", 
	
	@Test
	public void testDiff() throws Exception {
		
		for (int i=0; i<testparagraphs.length-1; i++){

			for (int j=i+1; j<testparagraphs.length; j++){
			
				System.out.println("\n\n TESTING " + testparagraphs[i] 
				        + ", " + testparagraphs[j]);
				
				// Test setup
				P pl = ParagraphDifferencer.loadParagraph(BASE_DIR + testparagraphs[i]);
				P pr = ParagraphDifferencer.loadParagraph(BASE_DIR + testparagraphs[j]);
				
				// Result format
				StreamResult result = new StreamResult(System.out);
		
				// Run the diff
				ParagraphDifferencer.diff(pl, pr, result);
				
			}
		}
	}

}
