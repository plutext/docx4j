/*
 *  Copyright 2012, Plutext Pty Ltd.
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

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.XSLFO.XSLFOExporterNonXSLT;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Document;

/**
 * 
 * Running Xalan extension functions on Android is problematic:
 * 
 *   http://stackoverflow.com/questions/10579339/is-it-possible-to-call-a-java-extension-function-from-xalan-on-android
 * 
 * and generating the XSL FO via Xalan + extension functions 
 * is a bit slow,   
 * so this uses TraversalUtils to generate XSL FO output
 * without any need for Xalan or XSLT.
 * 
 */
public class FONonXSLT {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath;	
		String outputfilepath;		

		inputfilepath = System.getProperty("user.dir")
//				+ "/hlink.docx";
//		+ "/OpenXML_1ed_Part4.docx";
//		+ "/sample-docs/word/sample-docx.docx";
//		+ "/sample-docs/word/2003/word2003-vml.docx";
//				+ "/table-nested.docx";
//		+ "/sample-docs/word/headers.docx";
		+ "/sample-docs/word/sample-docxv2.docx";		
		
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		
		FOSettings pdfSettings = new FOSettings();
		pdfSettings.setOpcPackage(wmlPackage);
		
		
		XSLFOExporterNonXSLT withoutXSLT = new XSLFOExporterNonXSLT(wmlPackage, 
				pdfSettings);		
//		new PDFConversionImageHandler(settings.getImageDirPath(), true) : 
//				new HTMLConversionImageHandler("c:\\temp", "/bar", true) );
		

		
		long startTime = System.currentTimeMillis();				
		Document xslfo = withoutXSLT.export();
		long endTime = System.currentTimeMillis();
		System.out.println("done.  elapsed time: " + Math.round((endTime-startTime)/1000) );

		System.out.println(XmlUtils.w3CDomNodeToString(xslfo));
		
		
		outputfilepath = inputfilepath + "K.pdf";
		OutputStream os = new java.io.FileOutputStream(outputfilepath);
		
		// OK, do it...
		withoutXSLT.output(xslfo, os, null);
		System.out.println("Saved " + outputfilepath);	
	}
}
