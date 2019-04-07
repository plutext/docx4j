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

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * @author jharrop
 *
 */
public class IsLglTest extends AbstractNumberingTest {
		
	@Test
	public void testIsLgl() throws Docx4JException {
		
		// Load the docx
		String inputfilepath = System.getProperty("user.dir") 
				+ "/src/test/java/org/docx4j/model/listnumbering/article-section-isLgl.docx";	    				
			
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		
		testNumbering(wordMLPackage);
	}

	@Test
	public void testNotIsLgl() throws Docx4JException {
		
		// Load the docx
		String inputfilepath = System.getProperty("user.dir") 
				+ "/src/test/java/org/docx4j/model/listnumbering/article-section-NotIsLgl.docx";	    				
			
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		
		testNumbering(wordMLPackage);
	}
	
}

