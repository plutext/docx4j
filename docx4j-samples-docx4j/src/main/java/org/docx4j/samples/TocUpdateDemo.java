/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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


import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.samples.AbstractSample;
import org.docx4j.toc.Toc;
import org.docx4j.toc.TocGenerator;

public class TocUpdateDemo  {
	
	static boolean update = false;

    public static void main(String[] args) throws Exception{
    	
		String input_DOCX = System.getProperty("user.dir") + "/sample-docs/toc.docx";
		
		// Load input_template.docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(input_DOCX));    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        

        
        
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);

        // If you want to automatically fix any broken bookmarks
        Docx4jProperties.setProperty("docx4j.toc.BookmarksIntegrity.remediate", true);
        
//        	Toc.setTocHeadingText("Sumário");
        	tocGenerator.updateToc(); // including page numbers 
        	// to generate page numbers, you should install your own local instance of Plutext PDF Converter, 
        	// from https://converter-eval.plutext.com/ and point to that in docx4j.properties
        	// (or try docx4j-export-fo)
	        
	        wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_TocUpdateDemo.docx") );
        
    }


}
