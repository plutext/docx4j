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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;

/**
 * This sample converts XHTML to docx content.
 * 
 * If the XHTML is escaped (as required for OpenDoPE input), it
 * is unescaped first.
 *
 * For best results, be sure to include src/main/resources on your classpath.
 *  
 */
public class ConvertInXHTMLFile {

    public static void main(String[] args) throws Exception {
        
        String inputfilepath = System.getProperty("user.dir") + "/tmp/2.xhtml";
        
        String stringFromFile = FileUtils.readFileToString(new File(inputfilepath), "UTF-8");
        
        String unescaped = stringFromFile;
        if (stringFromFile.contains("&lt;/") ) {
    		unescaped = StringEscapeUtils.unescapeHtml(stringFromFile);        	
        }
        
		
		System.out.println("Unescaped: " + unescaped);
        
        XHTMLImporter.setHyperlinkStyle("Hyperlink");
        
        // Create an empty docx package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();		
					
		// Convert the XHTML, and add it into the empty docx we made
		wordMLPackage.getMainDocumentPart().getContent().addAll( 
				XHTMLImporter.convert(unescaped, null, wordMLPackage) );
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/html_output.docx") );
      
  }
	
}
