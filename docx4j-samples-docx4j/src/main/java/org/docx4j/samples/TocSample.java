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


import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocGenerator;

/**
 * This is an end-to-end example showing you how to 
 * to add and populate a ToC, then update it.
 * 
 *  It uses export-fo to generate page numbers.
 *  
 * To use Word to populate (and/or update) the ToC,
 * please see the TocOperations example in docx4j-samples-export-documents4j-local
 * 
 */
public class TocSample  { 
	
	static boolean update = false;
	static boolean createPDF = false;
	
	static String outputDir = System.getProperty("user.dir");
	
    public static final String TOC_STYLE_MASK = "TOC%s";
    
    public static void main(String[] args) throws Exception{
    	
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        
        for(int i = 1; i < 10; i++){
            documentPart.getPropertyResolver().activateStyle(String.format(TOC_STYLE_MASK, i));
        }
        
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");

        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");
        
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        
        tocGenerator.generateToc( 0, " TOC \\o \"1-3\" \\h \\z \\u ", false);
        
        wordMLPackage.save(new java.io.File(outputDir + "/OUT_TocSample_Generated.docx") );
        
        
        if (update) {
        	
	        documentPart.addStyledParagraphOfText("Heading2", "Hello 12");
	        fillPageWithContent(documentPart, "Hello 12");
	        documentPart.addStyledParagraphOfText("Heading1", "Hello 21");
	        fillPageWithContent(documentPart, "Hello 21");
	        documentPart.addStyledParagraphOfText("Heading2", "Hello 22");
	        fillPageWithContent(documentPart, "Hello 22");
	        documentPart.addStyledParagraphOfText("Heading3", "Hello 23");
	        fillPageWithContent(documentPart, "Hello 23");
	        
	        tocGenerator.updateToc(false);
	        
	        wordMLPackage.save(new java.io.File(outputDir + "/OUT_TocSample_Updated.docx") );
        }
        
        if (createPDF) {
			
			OutputStream os = new java.io.FileOutputStream(outputDir + "/OUT_TocSample_Updated.pdf");
			Docx4J.toPDF(wordMLPackage, os);
        }
    }

    private static void fillPageWithContent(MainDocumentPart documentPart, String content){
        for(int i = 0; i < 10; i++){
            documentPart.addStyledParagraphOfText("Normal", content + " paragraph " + i);
        }
    }
}
