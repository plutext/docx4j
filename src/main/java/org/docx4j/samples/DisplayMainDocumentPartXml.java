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


import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class DisplayMainDocumentPartXml {


		/**
		 * @param args
		 */
		public static void main(String[] args) throws Exception {

			//String inputfilepath = "/home/jharrop/tmp/simple.docx";
			String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/TableAndPng.docx";
			
			// Open a document from the file system
			// 1. Load the Package
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
			
			// 2. Fetch the document part 		
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
			
			// Display its contents 
			System.out.println( "\n\n OUTPUT " );
			System.out.println( "====== \n\n " );	
			
			org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();

	        String xml = org.docx4j.XmlUtils.marshaltoString(wmlDocumentEl, true);
	        
	        System.out.println(xml);
			
		}
		
		

	}
