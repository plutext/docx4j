package org.docx4j.convert.out.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.fonts.FontUtils;
import org.docx4j.fonts.Substituter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.lowagie.text.pdf.BaseFont;

/**
 * There are 3 ways a package can be converted to PDF:
 * 
 * 1. via HTML, using docX2HTML.xslt
 * 
 * 2. via XSL FO
 * 
 * 3. via iText
 * 
 * Since none of these methods produce perfect results (yet),
 * all three are provided.  You can use (and extend) the technique
 * you are most comfortable with.
 * 
 * @author jharrop
 *
 */
public abstract class PdfConversion  {
	
	// can't implement Output interface, because PDF output isn't XML
	// so instead, have something similar:	
	public abstract void output(OutputStream os) throws Docx4JException;
		
	protected static Logger log = Logger.getLogger(PdfConversion.class);	
	
	protected WordprocessingMLPackage wordMLPackage;

	public PdfConversion(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}
	
	// Method to visualise using Flying Saucer.
	// TODO - comment out, since we don't distribute that ...	
	public void view() throws Exception {
		
		File tmpFile = File.createTempFile("output", ".pdf");
		// Delete the temporary file when program exits.
		tmpFile.deleteOnExit();
		
		OutputStream os = new java.io.FileOutputStream(tmpFile);

		// Could write to a ByteBuffer and avoid the temp file if:
		// 1. com.sun.pdfview.PDFViewer had an appropriate open method
		// 2. We knew how big to make the buffer
		// java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(15000);
		// //15kb
		// OutputStream os = newOutputStream(buf);
		
		this.output(os);
		
		com.sun.pdfview.PDFViewer pv = new com.sun.pdfview.PDFViewer(true); 	
//		pv.openFile(buf, "some name"); // requires modified com.sun.pdfview.PDFViewer		
		pv.openFile(tmpFile);
		
		
	}
	
//    /** Returns an output stream for a ByteBuffer. 
//     *  The write() methods use the relative ByteBuffer put() methods.
//     *  
//     *  From http://exampledepot.com/egs/java.nio/Buffer2Stream.html
//     * 
//     * @param buf
//     * @return
//     */	    
//    public static OutputStream newOutputStream(final ByteBuffer buf) {
//        return new OutputStream() {
//            public synchronized void write(int b) throws IOException {
//                buf.put((byte)b);
//            }
//    
//            public synchronized void write(byte[] bytes, int off, int len) throws IOException {
//                buf.put(bytes, off, len);
//            }
//        };
//    }	
 	
	
	/**
	 * Used by HTML and iText based PDF conversions
	 * 
	 * @param renderer
	 * @param fontName
	 * @param fm
	 */
	protected void embed(org.xhtmlrenderer.pdf.ITextRenderer renderer,
			String fontName, Map fontMappings) {
		Substituter.FontMapping fm = (Substituter.FontMapping)fontMappings.get( fontName );
		
		if (fm == null) {
			log.warn("No mapping found for: " + fontName);
		} else if (fm.getPhysicalFont()!=null) {
			try {
				if (fm.getPhysicalFont().getEmbeddedFile().endsWith(".pfb")) {
					
//						String afm = fm.getPhysicalFont().getEmbeddedFile().substring(5, fm.getPhysicalFont().getEmbeddedFile().length()-4 ) + ".afm";  // drop the 'file:'
					String afm = FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile());
					afm = afm.substring(0, afm.length()-4 ) + ".afm";  // drop the 'file:'
					log.info("Looking for: " + afm);
					
					// Given the check in substituter, we expect to find one or the other.
					File f = new File(afm);
			        if (f.exists()) {				
			        	log.info("Got it");
//			        	renderer.getFontResolver().addFont(afm, BaseFont.CP1252, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile()));  // drop the 'file:'	
			        	renderer.getFontResolver().addFont(afm, BaseFont.IDENTITY_H, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile()));  // drop the 'file:'	
						log.info("Substituting " + fontName + " with embedding " + fm.getPhysicalFont().getFamilyName() + " from " + fm.getPhysicalFont().getEmbeddedFile() );
			        } else {
			        	// Should we be doing afm first, or pfm?
						String pfm = FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile());
						pfm = pfm.substring(0, pfm.length()-4 ) + ".pfm";  // drop the 'file:'
						log.info("Looking for: " + pfm);
						f = new File(pfm);
				        if (f.exists()) {				
				        	log.info("Got it");
//				        	renderer.getFontResolver().addFont(pfm, BaseFont.CP1252, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile() ));  // drop the 'file:'
				        	renderer.getFontResolver().addFont(pfm, BaseFont.IDENTITY_H, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile() ));  // drop the 'file:'
							log.info("Substituting " + fontName + " with embedding " + fm.getPhysicalFont().getFamilyName() + " from " + fm.getPhysicalFont().getEmbeddedFile() );
				        } else {
				        	// Shouldn't happen.
				        	log.error("Couldn't find afm or pfm corresponding to " + fm.getPhysicalFont().getEmbeddedFile());
				        }
			        }
				} else {				
					renderer.getFontResolver().addFont(FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile()), true);
					log.info("Substituting " + fontName + " with embedding " + fm.getPhysicalFont().getFamilyName() + " from " + fm.getPhysicalFont().getEmbeddedFile() );
				}
			} catch (java.io.IOException e) {
			
			/* 
			 * [AWT-EventQueue-0] INFO  packages.WordprocessingMLPackage - Substituting symbol with standardsymbolsl from file:/usr/share/fonts/type1/gsfonts/s050000l.pfb 
java.io.IOException: Unsupported font type
at org.xhtmlrenderer.pdf.ITextFontResolver.addFont(ITextFontResolver.java:199)

.pfb not supported, even with iText 2.0.8

			 */
				e.printStackTrace();
				log.warn("Shouldn't happen - should have been detected upstream ... " +  e.getMessage() + ": " + fm.getPhysicalFont().getEmbeddedFile()); 
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Shouldn't happen - should have been detected upstream ... " + e.getMessage()); 
			}
		} else {
			log.warn("Can't addFont for: " + fontName); 
		}
	}	
	
	
}
