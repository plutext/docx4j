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

import java.io.OutputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class CreateHtml {
	    
	    public static void main(String[] args) 
	            throws Exception {

			String inputfilepath = System.getProperty("user.dir") + "/sample-docs/numbering-multilevel.docx";
			
			System.out.println(inputfilepath);
			
//			String inputfilepath = "/home/jharrop/tmp/wordml2html.docx";
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));	    				
			
			//OutputStream os = System.out;
			OutputStream os = new java.io.FileOutputStream(inputfilepath + ".html");			
			
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(os);
   			org.docx4j.convert.out.html.HtmlExporter.html(wordMLPackage, result, 
   					inputfilepath + "_files");
	        	        
	    }
	}