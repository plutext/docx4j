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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;

/**
 * Creates a WordprocessingML document from scratch. 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CreateWordprocessingMLDocument {

	public static void main(String[] args) throws Exception {
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createTestPackage();
		
		wordMLPackage.getMainDocumentPart().addParagraphOfText("Hello world, from docx4j!");
		
		System.out.println( ".. done!");
		
		//injectDocPropsCustomPart(wordMLPackage);
		
		// Now save it 
		wordMLPackage.save(new java.io.File("/tmp/result.docx") );
		
		System.out.println("Done.");
				
	}
	
	public static void injectDocPropsCustomPart(WordprocessingMLPackage wordMLPackage) {
		
		try {
			org.docx4j.openpackaging.parts.DocPropsCustomPart docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
			
			java.io.InputStream is = new java.io.FileInputStream("/tmp/custompart.xml" );
			
			docPropsCustomPart.unmarshal(is);
			
			wordMLPackage.addTargetPart(docPropsCustomPart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
