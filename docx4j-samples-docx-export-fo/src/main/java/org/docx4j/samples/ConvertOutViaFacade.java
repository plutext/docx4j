/*
 *  Copyright 2022, Plutext Pty Ltd.
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
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;

import org.docx4j.fonts.Mapper;
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
 * @author jharrop
 *
 */
public class ConvertOutViaFacade {
	
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
	
	protected static String inputfilepath;	
	protected static String outputfilepath;
	
	
	// Config for non-command line use
	static {
		
		inputfilepath = null; // to generate a docx (and PDF output) containing font samples
		
    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";
	}
		
    public static void main(String[] args) 
            throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
		}

		// Document loading (required)
		WordprocessingMLPackage wordMLPackage;
		if (inputfilepath==null) {
			// Create a docx
			System.out.println("No input path passed, creating dummy document");
			 wordMLPackage = WordprocessingMLPackage.createPackage();
			 SampleDocumentGenerator.createContent(wordMLPackage.getMainDocumentPart());	
		} else {
			// Load .docx or Flat OPC .xml
			System.out.println("Loading file from " + inputfilepath);
			wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		}
		
		// Set up font mapper (optional)
//		Mapper fontMapper = new IdentityPlusMapper();  // Only for Windows, unless you have Microsoft's fonts installed
		Mapper fontMapper = new BestMatchingMapper();  // Good for Linux (and OSX?)
		wordMLPackage.setFontMapper(fontMapper);		
		
		// exporter writes to an OutputStream.		
		String outputfilepath;
		if (inputfilepath==null) {
			outputfilepath = System.getProperty("user.dir") + "/OUT_FontContent.pdf";			
		} else {
			outputfilepath = inputfilepath + ".pdf";
		}
		OutputStream os = new java.io.FileOutputStream(outputfilepath);
		

		Docx4J.toPDF(wordMLPackage, os);
		
    }
    
	protected static void getInputFilePath(String[] args) throws IllegalArgumentException {

		if (args.length==0) throw new IllegalArgumentException("Input file arg missing");

		inputfilepath = args[0];
	}
	
	protected static void getOutputFilePath(String[] args) throws IllegalArgumentException {

		if (args.length<2) throw new IllegalArgumentException("Output file arg missing");

		outputfilepath = args[1];
	}	
   
}