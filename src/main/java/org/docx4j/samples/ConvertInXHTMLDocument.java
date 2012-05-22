/*
 *  Copyright 2011-2012, Plutext Pty Ltd.
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

import java.io.File;
import java.io.OutputStream;

import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;

/**
 * This sample converts an XHTML document to docx.
 *
 * For best results, be sure to include src/main/resources on your classpath.
 *  
 */
public class ConvertInXHTMLDocument {

    public static void main(String[] args) throws Exception {
        
    	// The input would generally be an XHTML document,
    	// but for convenience, this sample can convert a 
    	// docx to XHTML first (ie round trip).
        String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";

        XHTMLImporter.setHyperlinkStyle("Hyperlink");
        
        // Create an empty docx package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();		
		
		if (inputfilepath.endsWith("html")) {
			
			// Convert the XHTML, and add it into the empty docx we made
			wordMLPackage.getMainDocumentPart().getContent().addAll( 
					XHTMLImporter.convert(new File(inputfilepath), null, wordMLPackage) );
			
		} else if (inputfilepath.endsWith("docx")) {
			//Round trip docx -> XHTML -> docx
			WordprocessingMLPackage docx = WordprocessingMLPackage.load( new File(inputfilepath));	    	
			AbstractHtmlExporter exporter = new HtmlExporterNG2();
			
			// Use file system, so there is somewhere to save images (if any)
			OutputStream os = new java.io.FileOutputStream(inputfilepath + ".html");	
			
	    	HtmlSettings htmlSettings = new HtmlSettings();
	    	htmlSettings.setImageDirPath(inputfilepath + "_files"); 
	    	htmlSettings.setImageTargetUri(inputfilepath.substring(inputfilepath.lastIndexOf("/")+1) 
	    			  + "_files");
			
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(os);
			exporter.html(docx, result, htmlSettings );			
			
			// Now after all that, we have XHTML we can convert 
			wordMLPackage.getMainDocumentPart().getContent().addAll( 
					XHTMLImporter.convert( new File(inputfilepath + ".html"), null, wordMLPackage) );
		} else {
			return;
		}
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/html_output.docx") );
      
  }
	
}
