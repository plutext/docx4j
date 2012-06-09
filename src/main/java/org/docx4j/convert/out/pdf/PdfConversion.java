/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.convert.out.pdf;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

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