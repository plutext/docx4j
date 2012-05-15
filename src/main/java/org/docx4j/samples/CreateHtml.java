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

import org.docx4j.convert.out.Containerization;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.convert.out.html.SdtWriter;
import org.docx4j.convert.out.html.TagClass;
import org.docx4j.convert.out.html.TagSingleBox;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * If the source docx contained a WMF, that
 * will get converted to inline SVG.  In order
 * to see the SVG in your browser, you'll need 
 * to rename the file to .xml or serve
 * it with MIME type application/xhtml+xml
 *
 */
public class CreateHtml extends AbstractSample {
	    
	    public static void main(String[] args) 
	            throws Exception {
	    	
			try {
				getInputFilePath(args);
			} catch (IllegalArgumentException e) {
		    	//inputfilepath = System.getProperty("user.dir") + "/docs/Docx4j_GettingStarted.xml";
		    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
			}
			System.out.println(inputfilepath);	    	
	    	
	    	boolean save = true;	    	
			
			// Load .docx or Flat OPC .xml
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
	    	
			AbstractHtmlExporter exporter = new HtmlExporterNG2(); 	
			
	    	HtmlSettings htmlSettings = new HtmlSettings();
	    	
	    	htmlSettings.setImageDirPath(inputfilepath + "_files"); 
	    	htmlSettings.setImageTargetUri(inputfilepath.substring(inputfilepath.lastIndexOf("/")+1) 
	    			+ "_files");
	    	
	    	htmlSettings.setUserBodyTop("<H1>TOP!</H1>");
	    	htmlSettings.setUserBodyTail("<H1>TAIL!</H1>");
			
			// Sample sdt tag handler (tag handlers insert specific
			// html depending on the contents of an sdt's tag).  
			// This will only have an effect if the sdt tag contains
			// the string @class=XXX
//			SdtWriter.registerTagHandler("@class", new TagClass() );
			
			SdtWriter.registerTagHandler(Containerization.TAG_BORDERS, new TagSingleBox() );
			SdtWriter.registerTagHandler(Containerization.TAG_SHADING, new TagSingleBox() );
			
			OutputStream os; 
			if (save) {
				os = new java.io.FileOutputStream(inputfilepath + ".html");
			} else {
				os = System.out;

			}
			
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(os);
			exporter.html(wordMLPackage, result, htmlSettings);
			if (save) {
				System.out.println("Saved: " + inputfilepath + ".html using " +  exporter.getClass().getName() );
			}
	        	        
	    }
	}