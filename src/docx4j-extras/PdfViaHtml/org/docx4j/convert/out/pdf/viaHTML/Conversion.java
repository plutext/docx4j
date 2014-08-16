package org.docx4j.convert.out.pdf.viaHTML;

import java.io.File;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Output;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.HtmlExporter;
import org.docx4j.convert.out.html.HtmlExporterNG;
import org.docx4j.fonts.FontUtils;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	
	public Conversion(WordprocessingMLPackage wordMLPackage) {
		super(wordMLPackage);
	}

	/** Create a pdf version of the document. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */     
    public void output(OutputStream os) throws Docx4JException {
    	
    					
        try {
			// Put the html in result
			org.w3c.dom.Document xhtmlDoc = org.docx4j.XmlUtils.neww3cDomDocument();
			javax.xml.transform.dom.DOMResult result = new javax.xml.transform.dom.DOMResult(xhtmlDoc);
			
			AbstractHtmlExporter exporter = new HtmlExporter();
			exporter.html(wordMLPackage, result, false,
					System.getProperty("java.io.tmpdir") ); // false -> don't use HTML fonts.
					
			// Now render the XHTML
			org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();
					
			// 4.  Use addFont code like that below as necessary for the fonts
			
				// See https://xhtmlrenderer.dev.java.net/guide/users-guide-r7.html#xil_32
			org.xhtmlrenderer.extend.FontResolver resolver = renderer.getFontResolver();		
					
			Map fontsInUse = wordMLPackage.getMainDocumentPart().fontsInUse();
			Iterator fontMappingsIterator = fontsInUse.entrySet().iterator();
			while (fontMappingsIterator.hasNext()) {
			    Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
			    if(pairs.getKey()==null) {
			    	log.info("Skipped null key");
			    	pairs = (Map.Entry)fontMappingsIterator.next();
			    }
			    
			    String fontName = (String)pairs.getKey();
			    
			    PhysicalFont pf = wordMLPackage.getFontMapper().getFontMappings().get(fontName);
			    
			    if (pf==null) {
			    	log.error("Document font " + fontName + " is not mapped to a physical font!");
			    	continue;
			    }
			    
			    embed(renderer, pf);	        
			    // For any font we embed, also embed the bold, italic, and bold italic substitute
			    // .. at present, we can't tell which of these forms are actually used, so add them all
			    // bold, italic etc
			    PhysicalFont pfVariation = PhysicalFonts.getBoldForm(pf);
			    if (pfVariation!=null) {
				    embed(renderer, pfVariation);	        
			    }
			    pfVariation = PhysicalFonts.getBoldItalicForm(pf);
			    if (pfVariation!=null) {
				    embed(renderer, pfVariation);	        
			    }
			    pfVariation = PhysicalFonts.getItalicForm(pf);
			    if (pfVariation!=null) {
				    embed(renderer, pfVariation);	        
			    }
			    
			}
			
			// TESTING
//	    xhtmlDoc = org.docx4j.XmlUtils.neww3cDomDocument();
//	    try {
//			xhtmlDoc = XmlUtils.getNewDocumentBuilder().parse(new File("C:\\Users\\jharrop\\workspace\\docx4all\\sample-docs\\comic.html"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } 	    
			
			renderer.setDocument(xhtmlDoc, null);
			renderer.layout();
			
			renderer.createPDF(os);
		} catch (Exception e) {
			throw new Docx4JException("Failed creating PDF via HTML " ,e);
		}
		
		
	}
	
	/**
	 * Used by HTML and iText based PDF conversions
	 * 
	 * @param renderer
	 * @param fontName
	 * @param fm
	 */
	protected void embed(org.xhtmlrenderer.pdf.ITextRenderer renderer,
			PhysicalFont pf) {
		
			try {
				if (pf.getEmbeddedFile().endsWith(".pfb")) {
					
//						String afm = fm.getPhysicalFont().getEmbeddedFile().substring(5, fm.getPhysicalFont().getEmbeddedFile().length()-4 ) + ".afm";  // drop the 'file:'
					String afm = FontUtils.pathFromURL(pf.getEmbeddedFile());
					afm = afm.substring(0, afm.length()-4 ) + ".afm";  // drop the 'file:'
					
					// Given the check in substituter, we expect to find one or the other.
					File f = new File(afm);
			        if (f.exists()) {				
//			        	renderer.getFontResolver().addFont(afm, BaseFont.CP1252, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile()));  // drop the 'file:'	
			        	try {
							renderer.getFontResolver().addFont(afm, 
									BaseFont.IDENTITY_H, true, 
									FontUtils.pathFromURL(pf.getEmbeddedFile()));  // drop the 'file:'	
						} catch (java.io.UnsupportedEncodingException uee) {
							uee.printStackTrace();
							// Not a lot we can do here, unless we change
							// the encoding of the HTML input
							log.error(pf.getName() + " does not support UTF encoding");
							// This shouldn't have been added to PhysicalFonts 
							// in the first place
						}
			        } else {
			        	// Should we be doing afm first, or pfm?
						String pfm = FontUtils.pathFromURL(pf.getEmbeddedFile());
						pfm = pfm.substring(0, pfm.length()-4 ) + ".pfm";  // drop the 'file:'
						log.info("Looking for: " + pfm);
						f = new File(pfm);
				        if (f.exists()) {				
				        	try {
//					        	renderer.getFontResolver().addFont(pfm, BaseFont.CP1252, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile() ));  // drop the 'file:'
					        	renderer.getFontResolver().addFont(pfm, 
					        			BaseFont.IDENTITY_H, true, 
					        			FontUtils.pathFromURL(pf.getEmbeddedFile() ));  // drop the 'file:'
							} catch (java.io.UnsupportedEncodingException uee) {
								uee.printStackTrace();
								// Not a lot we can do here, unless we change
								// the encoding of the HTML input
								log.error(pf.getName() + " does not support UTF encoding");
								// This shouldn't have been added to PhysicalFonts 
								// in the first place
							}
				        } else {
				        	// Shouldn't happen.
				        	log.error("Couldn't find afm or pfm corresponding to " + pf.getEmbeddedFile());
				        }
			        }
				} else {				
//					renderer.getFontResolver().addFont(FontUtils.pathFromURL(pf.getEmbeddedFile()), true);
					renderer.getFontResolver().addFont(FontUtils.pathFromURL(pf.getEmbeddedFile()), 
							BaseFont.IDENTITY_H, 
							BaseFont.NOT_EMBEDDED);
				}
			} catch (java.io.IOException e) {
			
			/* 
			 * [AWT-EventQueue-0] INFO  packages.WordprocessingMLPackage - Substituting symbol with standardsymbolsl from file:/usr/share/fonts/type1/gsfonts/s050000l.pfb 
java.io.IOException: Unsupported font type
at org.xhtmlrenderer.pdf.ITextFontResolver.addFont(ITextFontResolver.java:199)

.pfb not supported, even with iText 2.0.8

			 */
				e.printStackTrace();
				log.warn("Shouldn't happen - should have been detected upstream ... " +  e.getMessage() + ": " + pf.getEmbeddedFile()); 
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Shouldn't happen - should have been detected upstream ... " + e.getMessage()); 
			}
	}
}
