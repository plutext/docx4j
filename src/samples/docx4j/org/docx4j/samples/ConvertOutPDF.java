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

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.model.fields.FieldUpdater;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.services.client.ConversionException;
import org.docx4j.services.client.ConversionRateLimitException;

/**
 * Demo of PDF output.
 * 
 * From v3.3.0, PDF output is by default via Plutext's commercial PDF Converter.
 * 
 * In 6.1.0 or later, it defaults to a local instance you'd need to install.
 * 
 * There is an evaluation instance at:
 * 
 *  	https://converter-eval.plutext.com:443/v1/00000000-0000-0000-0000-000000000000/convert
 * 
 * but if you want to use it, you'll need to explicitly set that endpoint.
 *   
 * To specify your own instance, please set docx4j.properties property: 
 * 
 *      com.plutext.converter.URL=http://your.host:80/v1/00000000-0000-0000-0000-000000000000/convert
 * 
 * If you don't want to use Plutext's PDF Converter, you can still use XSL FO and Apache FOP;
 * just put docx4j-export-fo and its dependencies on your classpath and use Docx4J.toFO
 * as per the example below.
 * 
 * @author jharrop
 */
public class ConvertOutPDF extends AbstractSample {
	
	
	// Config for non-command line use
	static {
		
		inputfilepath = null; // to generate a docx (and PDF output) containing font samples
		
    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";
		
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
		
		
		if (!Docx4J.pdfViaFO()) {
			
			// Since 3.3.0, Plutext's PDF Converter is used by default

			System.out.println("Using Plutext's PDF Converter; add docx4j-export-fo if you don't want that");
			
			try {
				Docx4J.toPDF(wordMLPackage, os);
			} catch (Docx4JException e) {
				if (e.getCause()!=null) {

					if ( e.getCause() instanceof ConversionRateLimitException) {
						// Possible if you send too many requests to converter-eval.plutext.com
						// or a commercial SAAS endpoint.  Please install your own instance. 
						System.err.println("API rate limit exceeded");
						System.err.println("Maybe you sent too many requests to a third party instance? Please install your own instance. ");
					
					} else if ( e.getCause() instanceof ConversionException) {
					
						ConversionException ce = (ConversionException)e.getCause();
						ce.printStackTrace();
					}
				} else {
					// What did we write?
					IOUtils.closeQuietly(os);
					System.out.println(
							FileUtils.readFileToString(new File(outputfilepath)));
					e.printStackTrace();
					
				}
				return;
			}
			System.out.println("Saved: " + outputfilepath);

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
		foSettings.setWmlPackage(wordMLPackage);
		
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