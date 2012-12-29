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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;

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
		
    	//inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
    	//inputfilepath = System.getProperty("user.dir") + "/docs/Docx4j_GettingStarted.xml";
    	//inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/tables.docx";
    	
//		inputfilepath = System.getProperty("user.dir") + "/OpenXML_1ed_Part4_500_sections_1.docx";
//		inputfilepath = System.getProperty("user.dir") + "/OpenXML_1ed_Part4_500_sections_every_50p_no_TOC.docx";
		inputfilepath = System.getProperty("user.dir") + "/NoSectPr.docx";
		
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
    	
		System.out.println(inputfilepath);

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

		WordprocessingMLPackage wordMLPackage;
		if (inputfilepath==null) {
			// Create a docx			
			 wordMLPackage = WordprocessingMLPackage.createPackage();
			createContent(wordMLPackage.getMainDocumentPart());	
		} else {
			// Load .docx or Flat OPC .xml
			wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		}
		
		
		// Workaround for https://issues.apache.org/bugzilla/show_bug.cgi?id=54094 
		// (apparently fixed post 1.1, but that's difficult to use right now)
		Object o = wordMLPackage.getMainDocumentPart().getContent().get(0);
		
		System.out.println("First block: " + o.getClass().getName() );
		
		if (o instanceof P
				&& ((P)o).getPPr()!=null) {
			PPr pPr = ((P)o).getPPr();
			BooleanDefaultTrue val = new BooleanDefaultTrue();
			val.setVal(Boolean.FALSE);
			pPr.setPageBreakBefore(val);
		}
		
		
		// Set up font mapper
		Mapper fontMapper = new IdentityPlusMapper();
		wordMLPackage.setFontMapper(fontMapper);
		// .. example of mapping missing font Algerian to installed font Comic Sans MS
		PhysicalFont font 
				= PhysicalFonts.getPhysicalFonts().get("Comic Sans MS");
		fontMapper.getFontMappings().put("Algerian", font);
		
		// the PdfConversion object
		org.docx4j.convert.out.pdf.PdfConversion c 
//				= new org.docx4j.convert.out.pdf.viaHTML.Conversion(wordMLPackage);
			= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
//				= new org.docx4j.convert.out.pdf.viaIText.Conversion(wordMLPackage);
		
		if (saveFO) {
			((org.docx4j.convert.out.pdf.viaXSLFO.Conversion)c).setSaveFO(
					new java.io.File(inputfilepath + ".fo"));
		}
		
		// PdfConversion writes to an output stream
		String outputfilepath;
		if (inputfilepath==null) {
			outputfilepath = System.getProperty("user.dir") + "/OUT_FontContent.pdf";			
		} else {
			outputfilepath = inputfilepath + "2.pdf";
		}
		OutputStream os = new java.io.FileOutputStream(outputfilepath);
		
		// OK, do it...
		c.output(os, new PdfSettings() );
		System.out.println("Saved " + outputfilepath);
    }
	    
    public static void createContent(MainDocumentPart wordDocumentPart ) {
		/*
		 * NB, this currently works nicely with
		 * viaIText, and viaXSLFO (provided
		 * you view with Acrobat Reader .. it
		 * seems to overwhelm pdfviewer, which
		 * is weird, since viaIText works in both).
		 */
    	
    	try {
    		// Do this explicitly, since we need
    		// it in order to create our content
			PhysicalFonts.discoverPhysicalFonts(); 						
															
			Map<String, PhysicalFont> physicalFontMap = PhysicalFonts.getPhysicalFonts();			
			Iterator physicalFontMapIterator = physicalFontMap.entrySet().iterator();
			while (physicalFontMapIterator.hasNext()) {
			    Map.Entry pairs = (Map.Entry)physicalFontMapIterator.next();
			    if(pairs.getKey()==null) {
			    	pairs = (Map.Entry)physicalFontMapIterator.next();
			    }
			    String fontName = (String)pairs.getKey();
			    PhysicalFont pf = (PhysicalFont)pairs.getValue();
			    
			    System.out.println("Added paragraph for " + fontName);
			    addObject(wordDocumentPart, sampleText, fontName );

			    // bold, italic etc
			    PhysicalFont pfVariation = PhysicalFonts.getBoldForm(pf);
			    if (pfVariation!=null) {
				    addObject(wordDocumentPart, sampleTextBold, pfVariation.getName() );
			    }
			    pfVariation = PhysicalFonts.getBoldItalicForm(pf);
			    if (pfVariation!=null) {
				    addObject(wordDocumentPart, sampleTextBoldItalic, pfVariation.getName() );
			    }
			    pfVariation = PhysicalFonts.getItalicForm(pf);
			    if (pfVariation!=null) {
				    addObject(wordDocumentPart, sampleTextItalic, pfVariation.getName() );
			    }
			    
			}
		} catch (Exception e) {
			e.printStackTrace();
		}    		    
    	
    }
    
    static void addObject(MainDocumentPart wordDocumentPart, String template, String fontName ) throws JAXBException {
    	
	    HashMap substitution = new HashMap();
	    substitution.put("fontname", fontName);
	    Object o = XmlUtils.unmarshallFromTemplate(template, substitution);
	    wordDocumentPart.addObject(o);    		    
    	
    }
    
    final static String sampleText = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
		+"<w:r>"
		+"<w:rPr>"
			+"<w:rFonts w:ascii=\"${fontname}\" w:eastAsia=\"${fontname}\" w:hAnsi=\"${fontname}\" w:cs=\"${fontname}\" />"
		+"</w:rPr>"
		+"<w:t xml:space=\"preserve\">${fontname}</w:t>"
	+"</w:r>"
	+"</w:p>";
    final static String sampleTextBold =	"<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"	+"<w:r>"
		+"<w:rPr>"
			+"<w:rFonts w:ascii=\"${fontname}\" w:eastAsia=\"${fontname}\" w:hAnsi=\"${fontname}\" w:cs=\"${fontname}\" />"
			+"<w:b />"
		+"</w:rPr>"
		+"<w:t>${fontname} bold;</w:t>"
	+"</w:r>"
	+"</w:p>";
    final static String sampleTextItalic =	"<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"	+"<w:r>"
		+"<w:rPr>"
			+"<w:rFonts w:ascii=\"${fontname}\" w:eastAsia=\"${fontname}\" w:hAnsi=\"${fontname}\" w:cs=\"${fontname}\" />"
			+"<w:i />"
		+"</w:rPr>"
		+"<w:t>${fontname} italic; </w:t>"
	+"</w:r>"
	+"</w:p>";
    final static String sampleTextBoldItalic ="<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
    	+"<w:r>"
		+"<w:rPr>"
			+"<w:rFonts w:ascii=\"${fontname}\" w:eastAsia=\"${fontname}\" w:hAnsi=\"${fontname}\" w:cs=\"${fontname}\" />"
			+"<w:b />"
			+"<w:i />"
		+"</w:rPr>"
		+"<w:t>${fontname} bold italic</w:t>"
	+"</w:r>"
+"</w:p>";
    
    
}