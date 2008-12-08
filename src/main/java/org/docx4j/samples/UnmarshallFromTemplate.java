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


import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class UnmarshallFromTemplate {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/unmarshallFromTemplateExample.docx";
		
		boolean save = true;
		String outputfilepath = System.getProperty("user.dir") + "/test-out.docx";		
		
		
		// Open a document from the file system
		// 1. Load the Package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// 2. Fetch the document part 		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
	      org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
	            .getJaxbElement();
	      
	      //xml --> string
	      String xml = XmlUtils.marshaltoString(wmlDocumentEl, true);
	      
	      HashMap<String, String> mappings = new HashMap<String, String>();
	      
	      mappings.put("colour", "green");
	      mappings.put("icecream", "chocolate");
	      
	      //valorize template
	      Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);
	      
	      //change  JaxbElement
	      documentPart.setJaxbElement(obj);

	      // Save it		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
			System.out.println( "Saved output to:" + outputfilepath );
		} else {
			// Display the Main Document Part.
			
		}
	}
	

}
