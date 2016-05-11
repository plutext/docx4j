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


import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.samples.AbstractSample;
import org.docx4j.toc.Toc;
import org.docx4j.toc.TocGenerator;

public class TocUpdateDemo  {
	
	static boolean update = false;

    public static void main(String[] args) throws Exception{
    	
		String input_DOCX = System.getProperty("user.dir") + "/reportBefore.docx";
		
		// Load input_template.docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(input_DOCX));    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        

        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        
//        tocGenerator.setDocumentServicesEndpoint("http://192.168.2.16:9015/v1/00000000-0000-0000-0000-000000000000/convert");
        
//        	Toc.setTocHeadingText("Sumário");
        tocGenerator.updateToc( false); // true --> skip page numbering; its currently much faster
	        
	        wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_TocUpdateDemo.docx") );
        
    }


}
