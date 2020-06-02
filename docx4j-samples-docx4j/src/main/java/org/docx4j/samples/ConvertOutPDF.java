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
import org.docx4j.model.fields.FieldUpdater;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Demo of PDF output.
 * 
 * In docx4j 8.2.0, there are 2 main ways of getting PDF output:
 * 
 * 1.  If you have Microsoft Word available, locally or remotely, you 
 *     can use it, via documents4j.  To do so, put docx4j-documents4j-local 
 *     or docx4j-documents4j-remote on your classpath.  For more,
 *     please see the sample projects docx4j-samples-documents4j-local 
 *     or docx4j-samples-documents4j-remote 
 *     
 * 2.  If not, you can use XSL FO and Apache FOP; just put docx4j-export-fo 
 *     and its dependencies on your classpath and configure the converter (see below) 
 *     
 * @author jharrop
 */
public class ConvertOutPDF extends AbstractSample {
	
	
	// Config for non-command line use
	static {
		
		inputfilepath = null; // to generate a docx (and PDF output) containing font samples
		
    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		
    	// URL of converter instance; 
		//Docx4jProperties.setProperty("com.plutext.converter.URL", 
		// .. install your own at 			
//				"http://localhost:9016/v1/00000000-0000-0000-0000-000000000000/convert");    	
		// .. or perform a quick test against 
//				"https://converter-eval.plutext.com:443/v1/00000000-0000-0000-0000-000000000000/convert");    	
    	
    	// XSL-FO only
    	saveFO = true;
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
		
		if (Docx4J.pdfViaFO()) {
			// Font regex (optional)
			// Set regex if you want to restrict to some defined subset of fonts
			// Here we have to do this before calling createContent,
			// since that discovers fonts
			String regex = null;
			// Windows:
			// String
			// regex=".*(calibri|camb|cour|arial|symb|times|Times|zapf).*";
			//regex=".*(calibri|camb|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*";
			// Mac
			// String
			// regex=".*(Courier New|Arial|Times New Roman|Comic Sans|Georgia|Impact|Lucida Console|Lucida Sans Unicode|Palatino Linotype|Tahoma|Trebuchet|Verdana|Symbol|Webdings|Wingdings|MS Sans Serif|MS Serif).*";
			PhysicalFonts.setRegex(regex);
		}

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
		
		// Refresh the values of DOCPROPERTY fields 
		FieldUpdater updater = new FieldUpdater(wordMLPackage);
		updater.update(true);

		String outputfilepath;
		if (inputfilepath==null) {
			outputfilepath = System.getProperty("user.dir") + "/OUT_FontContent.pdf";			
		} else {
			outputfilepath = inputfilepath + ".pdf";
		}
		
		// All methods write to an output stream
		OutputStream os = new java.io.FileOutputStream(outputfilepath);
		
		if (Docx4J.pdfViaDocuments4jLocal() || 
				Docx4J.pdfViaDocuments4jRemote() ) {
					
			System.out.println("Using documents4j");
			Docx4J.toPDF(wordMLPackage, os); 
			System.out.println("Saved: " + outputfilepath);
			return;
		}
		
		if (!Docx4J.pdfViaFO() ) {
			
			System.out.println("No PDF converter configured!");
			System.out.println("Please add docx4j-documents4j-local, or docx4j-documents4j-remote or docx4j-export-fo to your classpath.");
			return;
		}
		
		System.out.println("Attempting to use XSL FO");
		
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
		 * If you don't have logging configured, 
		 * your PDF will say "TO HIDE THESE MESSAGES, 
		 * TURN OFF debug level logging for 
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
		 */
		
		
		/*
		 * NOT WORKING?
		 * 
		 * If you are getting:
		 * 
		 *   "fo:layout-master-set" must be declared before "fo:page-sequence"
		 * 
		 * please check:
		 * 
		 * 1.  the jaxb-xslfo jar is on your classpath
		 * 
		 * 2.  that there is no stack trace earlier in the logs
		 * 
		 * 3.  your JVM has adequate memory, eg
		 * 
		 *           -Xmx1G -XX:MaxPermSize=128m
		 * 
		 */
		
		
		// Set up font mapper (optional)
		Mapper fontMapper = new IdentityPlusMapper();
		wordMLPackage.setFontMapper(fontMapper);
		
		// .. example of mapping font Times New Roman which doesn't have certain Arabic glyphs
		// eg Glyph "ي" (0x64a, afii57450) not available in font "TimesNewRomanPS-ItalicMT".
		// eg Glyph "ج" (0x62c, afii57420) not available in font "TimesNewRomanPS-ItalicMT".
		// to a font which does
		PhysicalFont font 
				= PhysicalFonts.get("Arial Unicode MS"); 
			// make sure this is in your regex (if any)!!!
//		if (font!=null) {
//			fontMapper.put("Times New Roman", font);
//			fontMapper.put("Arial", font);
//		}
//		fontMapper.put("Libian SC Regular", PhysicalFonts.get("SimSun"));

		// FO exporter setup (required)
		// .. the FOSettings object
    	FOSettings foSettings = Docx4J.createFOSettings();
		if (saveFO) {
			foSettings.setFoDumpFile(new java.io.File(inputfilepath + ".fo"));
		}
		foSettings.setOpcPackage(wordMLPackage);
		
		// Document format: 
		// The default implementation of the FORenderer that uses Apache Fop will output
		// a PDF document if nothing is passed via 
		// foSettings.setApacheFopMime(apacheFopMime)
		// apacheFopMime can be any of the output formats defined in org.apache.fop.apps.MimeConstants eg org.apache.fop.apps.MimeConstants.MIME_FOP_IF or
		// FOSettings.INTERNAL_FO_MIME if you want the fo document as the result.
		//foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		
		// Specify whether PDF export uses XSLT or not to create the FO
		// (XSLT takes longer, but is more complete).
		
		// Don't care what type of exporter you use
		Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
		
		// Prefer the exporter, that uses a xsl transformation
		// Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
		
		// Prefer the exporter, that doesn't use a xsl transformation (= uses a visitor)
		// .. faster, but not yet at feature parity
		// Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
    	
		System.out.println("Saved: " + outputfilepath);

		// Clean up, so any ObfuscatedFontPart temp files can be deleted 
		if (wordMLPackage.getMainDocumentPart().getFontTablePart()!=null) {
			wordMLPackage.getMainDocumentPart().getFontTablePart().deleteEmbeddedFontTempFiles();
		}		
		// This would also do it, via finalize() methods
		updater = null;
		foSettings = null;
		wordMLPackage = null;
		
		
    }
    
    
}