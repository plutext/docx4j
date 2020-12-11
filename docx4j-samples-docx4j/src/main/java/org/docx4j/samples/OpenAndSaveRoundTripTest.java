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

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * Simple example of opening an existing docx, and saving it
 * again.
 * 
 * Notice that the file size might be different.  
 * http://www.docx4java.org/forums/docx-java-f6/file-size-differences-t1091.html
 * explains why.
 * 
 * @author jharrop
 *
 */
public class OpenAndSaveRoundTripTest extends AbstractSample {
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		}
		System.out.println(inputfilepath);	    	
		
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
				
		wordMLPackage.getMainDocumentPart().addParagraphOfText("hello");
		
		wordMLPackage.getMainDocumentPart().marshal(System.out);
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_OpenAndSaveRoundTripTest.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
	}
		

}
