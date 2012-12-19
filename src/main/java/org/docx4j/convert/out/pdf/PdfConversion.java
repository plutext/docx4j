package org.docx4j.convert.out.pdf;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * There are 3 ways a package can be converted to PDF:
 * 
 * 1. via XSL FO
 * 1a - using XSLT to generate the XSL FO
 * 1b - using TraversalUtils to generate the XSL FO
 * 
 * 2. via HTML, using docX2HTML.xslt
 * 
 * 3. via iText
 * 
 * Method 1a is the standard way of doing things
 * Method 1b is experimental (Dec 2012)
 * 
 * Methods 2 & 3 are in docx4j extras
 * 
 * @author jharrop
 *
 */
public abstract class PdfConversion  {
		
	// can't implement Output interface, because PDF output isn't XML
	// so instead, have something similar:	
	public abstract void output(OutputStream os, PdfSettings settings) throws Docx4JException;
		
	protected static Logger log = Logger.getLogger(PdfConversion.class);	
	
	// For XSLT logging
	public static void log(String message ) {
		
		log.info(message);
	}
	
	protected WordprocessingMLPackage wordMLPackage;
	
	public PdfConversion(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}
	
//	// Method to visualise using pdf-renderer
//	public void view() throws Exception {
//		
//		File tmpFile = File.createTempFile("output", ".pdf");
//		// Delete the temporary file when program exits.
//		tmpFile.deleteOnExit();
//		
//		OutputStream os = new java.io.FileOutputStream(tmpFile);
//
//		// Could write to a ByteBuffer and avoid the temp file if:
//		// 1. com.sun.pdfview.PDFViewer had an appropriate open method
//		// 2. We knew how big to make the buffer
//		// java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(15000);
//		// //15kb
//		// OutputStream os = newOutputStream(buf);
//		
//		this.output(os);
//		
//		com.sun.pdfview.PDFViewer pv = new com.sun.pdfview.PDFViewer(true); 	
////		pv.openFile(buf, "some name"); // requires modified com.sun.pdfview.PDFViewer		
//		pv.openFile(tmpFile);
//		
//	}

	
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
 	
	

}