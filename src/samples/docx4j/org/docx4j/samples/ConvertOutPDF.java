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

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Demo of PDF output.
 * 
 * PDF output is via XSL FO.
 * First XSL FO is created, then FOP
 * is used to convert that to PDF.
 * 
 * Don't worry if you get a class not
 * found warning relating to batik. It
 * doesn't matter.
 * 
 * If you don't have log4j configured, 
 * your PDF will say "TO HIDE THESE MESSAGES, 
 * TURN OFF log4j debug level logging for 
 * org.docx4j.convert.out.pdf.viaXSLFO".  The thinking is
 * that you need to be able to be warned if there
 * are things in your docx which the PDF output
 * doesn't support...
 * 
 * docx4j used to also support creating
 * PDF via iText and via HTML. As of docx4j 2.5.0, 
 * only viaXSLFO is supported.  The viaIText and 
 * viaHTML source code can be found in src/docx4j-extras directory
 * 
 * @author jharrop
 *
 */
public class ConvertOutPDF extends AbstractSample {
	
	// Config for non-command line use
	static {
		
		inputfilepath = null; // to generate a docx (and PDF output) containing font samples
		
    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docxv2.docx";
		
    	saveFO = false;
	}
	
	
	// For demo/debugging purposes, save the intermediate XSL FO
	// Don't do this in production!
	static boolean saveFO;
	
    public static void main(String[] args) 
            throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
		}
		
		// Font regex (optional)
		// Set regex if you want to restrict to some defined subset of fonts
		// Here we have to do this before calling createContent,
		// since that discovers fonts
		String regex = null;
		// Windows:
		// String
		// regex=".*(calibri|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*";
		// Mac
		// String
		// regex=".*(Courier New|Arial|Times New Roman|Comic Sans|Georgia|Impact|Lucida Console|Lucida Sans Unicode|Palatino Linotype|Tahoma|Trebuchet|Verdana|Symbol|Webdings|Wingdings|MS Sans Serif|MS Serif).*";
		PhysicalFonts.setRegex(regex);

		// Document loading (required)
		WordprocessingMLPackage wordMLPackage;
		if (inputfilepath==null) {
			// Create a docx
			System.out.println("No imput path passed, creating dummy document");
			 wordMLPackage = WordprocessingMLPackage.createPackage();
			 SampleDocument.createContent(wordMLPackage.getMainDocumentPart());	
		} else {
			// Load .docx or Flat OPC .xml
			System.out.println("Loading file from " + inputfilepath);
			wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		}
		
		// Set up font mapper (optional)
		Mapper fontMapper = new IdentityPlusMapper();
		wordMLPackage.setFontMapper(fontMapper);
		// .. example of mapping missing font Algerian to installed font Comic Sans MS
		PhysicalFont font 
				= PhysicalFonts.getPhysicalFonts().get("Comic Sans MS");
		fontMapper.getFontMappings().put("Algerian", font);
		

		// FO exporter setup (required)
		// .. the FOSettings object
    	FOSettings foSettings = Docx4J.createFOSettings();
		if (saveFO) {
			foSettings.setFoDumpFile(new java.io.File(inputfilepath + ".fo"));
		}
		foSettings.setWmlPackage(wordMLPackage);
		// Document format: 
		// The default implementation of the FORenderer that uses Apache Fop will output
		// a PDF document if nothing is passed via 
		// foSettings.setApacheFopMime(apacheFopMime)
		// apacheFopMime can be any of the output formats defined in org.apache.fop.apps.MimeConstants or
		// FOSettings.INTERNAL_FO_MIME if you want the fo document as the result.
		
		
		// exporter writes to an OutputStream.		
		String outputfilepath;
		if (inputfilepath==null) {
			outputfilepath = System.getProperty("user.dir") + "/OUT_FontContent.pdf";			
		} else {
			outputfilepath = inputfilepath + ".pdf";
		}
		OutputStream os = new java.io.FileOutputStream(outputfilepath);
    	

		//Don't care what type of exporter you use
		Docx4J.toFO(foSettings, os, Docx4J.FLAG_NONE);
		//Prefer the exporter, that uses a xsl transformation
		//Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
		//Prefer the exporter, that doesn't use a xsl transformation (= uses a visitor)
		//Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
    	
		System.out.println("Saved: " + outputfilepath);
    }
    
    
}