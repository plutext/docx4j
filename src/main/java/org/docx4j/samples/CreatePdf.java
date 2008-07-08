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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class CreatePdf {
	    
	    public static void main(String[] args) 
	            throws Exception {

			String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/TableAndPng.docx";
				//"/home/jharrop/tmp/Styles-lots.docx";
//			String inputfilepath = "/home/jharrop/tmp/wordml2html.docx";
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
	    							
		     // Create temp file.
	        java.io.File temp = java.io.File.createTempFile("output", ".pdf");
	    
	        // Delete temp file when program exits.
	        temp.deleteOnExit();			

			OutputStream os = new java.io.FileOutputStream(temp);
	        
			// Could write to a ByteBuffer and avoid the temp file if:
			// 1. com.sun.pdfview.PDFViewer had an appropriate open method
			// 2. We knew how big to make the buffer
//			java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(15000); //15kb
//			OutputStream os = newOutputStream(buf);			
			
			wordMLPackage.pdf(os);
			
			os.close();
			
			com.sun.pdfview.PDFViewer pv = new com.sun.pdfview.PDFViewer(true); 
//			pv.openFile(buf, "some name"); // requires modified com.sun.pdfview.PDFViewer
			pv.openFile(temp);
	        	        
	    }
	    
	    /** Returns an output stream for a ByteBuffer. 
	     *  The write() methods use the relative ByteBuffer put() methods.
	     *  
	     *  From http://exampledepot.com/egs/java.nio/Buffer2Stream.html
	     * 
	     * @param buf
	     * @return
	     */	    
	    public static OutputStream newOutputStream(final ByteBuffer buf) {
	        return new OutputStream() {
	            public synchronized void write(int b) throws IOException {
	                buf.put((byte)b);
	            }
	    
	            public synchronized void write(byte[] bytes, int off, int len) throws IOException {
	                buf.put(bytes, off, len);
	            }
	        };
	    }	
	    
	    
	    
	}