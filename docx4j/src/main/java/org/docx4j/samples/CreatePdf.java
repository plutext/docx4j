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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class CreatePdf {
	    
	    public static void main(String[] args) 
	            throws Exception {
			
	    	boolean save = true;
	    	
//			String inputfilepath = System.getProperty("user.dir") + "/tmp/Slovenian.docx";
//			String inputfilepath = "/home/dev/workspace/docx4all/sample-docs/docx4all-CurrentDocxFeatures.docx";
//			String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\workspace\\docx4j-2009\\sample-docs\\Word2007-fonts.docx";
			String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\My Documents\\tmp-test-docs\\Slovenian.docx";
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

			/* Choose which of the three methods you want to use...
			 * 
			 * .. viaHTML uses docX2HTML.xslt and xhtmlrenderer, 
			 *    and supports numbering, images,
			 *    and tables, but is pretty hard to understand
			 *    
			 * .. viaXSLFO uses docx2fo.xslt and FOP.  It is
			 *    rudimentary right now, but should be
			 *    easy enough to extend to include a basic
			 *    feature set
			 *    
			 * .. viaItext - for developers who don't like xslt
			 *    at all! Or want to use iText's features..
			 *    Displays images, but as at 2009 03 19.
			 *    doesn't try to scale them.
			 */
			org.docx4j.convert.out.pdf.PdfConversion c 
//				= new org.docx4j.convert.out.pdf.viaHTML.Conversion(wordMLPackage);
				= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
//				= new org.docx4j.convert.out.pdf.viaIText.Conversion(wordMLPackage);
			
			if (save) {
				OutputStream os = new java.io.FileOutputStream(inputfilepath + ".pdf");			
				c.output(os);
				System.out.println("Saved " + inputfilepath + ".pdf");
			} else {
				c.view();
			}    
	    }
	    
	    
	}