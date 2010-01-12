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

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

public class CreatePdf {
	    
	    public static void main(String[] args) 
	            throws Exception {

	    	boolean save = false;
	    	
//	    	String inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.xml";
	    	String inputfilepath = System.getProperty("user.dir") 
	    		+ "/sample-docs/test-docs/header-footer/header_first.xml";	    	
	    	
			WordprocessingMLPackage wordMLPackage;
			if (inputfilepath==null) {
				
				// If this is to be saved..
				inputfilepath = System.getProperty("user.dir") + "/tmp/output";
				/*
				 * NB, this currently works nicely with
				 * viaIText, and viaXSLFO (provided
				 * you view with Acrobat Reader .. it
				 * seems to overwhelm pdfviewer, which
				 * is weird, since viaIText works in both).
				 */
				
				 wordMLPackage = new WordprocessingMLPackage();
				MainDocumentPart wordDocumentPart = new MainDocumentPart();		
				wordMLPackage.addTargetPart(wordDocumentPart);
				org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
				org.docx4j.wml.Body  body = factory.createBody();
				org.docx4j.wml.Document wmlDocumentEl = factory.createDocument();
				wmlDocumentEl.setBody(body);
						
				// Put the content in the part
				((org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart)wordDocumentPart).setJaxbElement(wmlDocumentEl);
	
				createContent(wordDocumentPart);	
			} else {
				// Load .docx or Flat OPC .xml
				wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
			}
			// Need document content first..
			wordMLPackage.setFontMapper(new IdentityPlusMapper());
			
			/* Choose which of the three methods you want to use...
			 * 
			 * .. viaHTML uses the old docX2HTML.xslt and xhtmlrenderer, 
			 *    and supports numbering, images,
			 *    and tables, but is pretty hard to understand
			 *    
			 *    It is a trivial change to instead use 
			 *    HTMLExporterNG, but that should produce
			 *    the same output as viaXSLFO, so we don't
			 *    do that. 
			 *    
			 * .. viaXSLFO uses docx2fo.xslt and FOP.  It is
			 *    coming along, with support for
			 *    headers/footers, images and tables
			 *    
			 * .. viaItext - for developers who don't like xslt
			 *    at all! Or want to use iText's features..
			 *    Displays images, but as at 2009 03 19.
			 *    doesn't try to scale them.
			 *    
			 * Fonts should work pretty well via any of these
			 * methods!
			 */
			org.docx4j.convert.out.pdf.PdfConversion c 
//				= new org.docx4j.convert.out.pdf.viaHTML.Conversion(wordMLPackage);
				= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
//				= new org.docx4j.convert.out.pdf.viaIText.Conversion(wordMLPackage);
			
			if (save) {
				((org.docx4j.convert.out.pdf.viaXSLFO.Conversion)c).setSaveFO(
						new java.io.File(inputfilepath + ".fo"));
				OutputStream os = new java.io.FileOutputStream(inputfilepath + ".pdf");			
				c.output(os);
				System.out.println("Saved " + inputfilepath + ".pdf");
			} else {
				c.view();
			}    
	    }
	    
	    public static void createContent(MainDocumentPart wordDocumentPart ) {
	    	
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
				// TODO Auto-generated catch block
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