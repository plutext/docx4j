/*
 *  Copyright 2019, Plutext Pty Ltd.
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

import javax.xml.bind.JAXBContext;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * This sample removes all content controls from the input docx, keeping their content.
*/
public class ContentControlRemove {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	static String filepathprefix;
	
	public static void main(String[] args) throws Exception {

		
		String input_DOCX = System.getProperty("user.dir") + "/some.docx";
		
		// resulting docx
		String OUTPUT_DOCX = System.getProperty("user.dir") + "/OUT_ContentControlRemove.docx";
		
		// Load input_template.docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(input_DOCX));

		// There is no xml stream
		FileInputStream xmlStream = null;

		Docx4J.bind(wordMLPackage, xmlStream, Docx4J.FLAG_BIND_REMOVE_SDT);
		
		//Save the document 
		Docx4J.save(wordMLPackage, new File(OUTPUT_DOCX), Docx4J.FLAG_NONE);
		System.out.println("Saved: " + OUTPUT_DOCX);
	}	
	
				

}
