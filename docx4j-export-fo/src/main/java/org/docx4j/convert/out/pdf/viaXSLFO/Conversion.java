/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.File;
import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @deprecated
 */
public class Conversion extends PdfConversion {
	public static Logger log = LoggerFactory.getLogger(Conversion.class);
	protected WordprocessingMLPackage wordMLPackage = null;
	
	public Conversion(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}
	
	private static File saveFO;
	public void setSaveFO(File save) {
		saveFO = save;
	}
	
//	public void output(OutputStream os, FOSettings settings) throws Docx4JException {
//		setupSettings(settings, FOSettings.INTERNAL_FO_MIME);
//		Docx4J.toFO(settings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
//	}
	
	public void outputXSLFO(OutputStream os, PdfSettings settings) throws Docx4JException {
		setupSettings(settings, FOSettings.MIME_PDF);
		Docx4J.toFO(settings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Create a pdf version of the document, using XSL FO. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * @param settings
	 *            The configuration for the conversion 
	 * 
	 * */     
	public void output(OutputStream os, PdfSettings settings) throws Docx4JException {
		setupSettings(settings, FOSettings.INTERNAL_FO_MIME);
		Docx4J.toPDF(wordMLPackage, os);
	}
	
	protected void setupSettings(FOSettings settings, String mime) {
		if ((wordMLPackage != null) && (settings.getOpcPackage() == null)) {
			settings.setOpcPackage(wordMLPackage);
		}
		if ((saveFO != null) && (settings.getFoDumpFile() == null)) {
			settings.setFoDumpFile(saveFO);
		}
		settings.setApacheFopMime(mime);
	}
}
    
