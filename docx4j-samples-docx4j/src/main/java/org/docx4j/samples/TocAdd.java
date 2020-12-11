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


import java.io.File;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocGenerator;

/**
 * This example uses docx4j's internal capabilities
 * to generate a ToC.  
 * 
 * To add indicative page numbers, put export-fo on your classpath.
 *  
 * Note: If you have Word available, you can use it to populate (and/or update) the ToC.
 * That uses a different code path; please see the TocOperations example in 
 * docx4j-samples-export-documents4j-local
 * 
 */
public class TocAdd  { 
	
	static String inputfilepath = System.getProperty("user.dir") + "/input.docx";
	static String outputfilepath = System.getProperty("user.dir") + "/OUT_TocAdd.docx";
	
    public static final String TOC_STYLE_MASK = "TOC%s";
    
    public static void main(String[] args) throws Exception{
    	
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(inputfilepath));

        
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        
        //tocGenerator.generateToc( 0,    "TOC \\h \\z \\t \"comh1,1,comh2,2,comh3,3,comh4,4\" ", true);
        
        tocGenerator.generateToc( 0,    "TOC \\o \"1-3\" \\h \\z \\u ", true);
        
        wordMLPackage.save(new java.io.File(outputfilepath) );
        
        

    }


}
