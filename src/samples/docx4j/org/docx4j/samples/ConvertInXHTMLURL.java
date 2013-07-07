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

import java.net.URL;

import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;

/**
 * This sample retrieves an XHTML Web page URL and converts it to docx content.
 * 
 * If your page is not well-formed XML, you will need to tidy it first.
 * 
 * For best results, be sure to include src/main/resources on your classpath.
 *  
 */
public class ConvertInXHTMLURL {

    public static void main(String[] args) throws Exception {
        
    	// Must tidy first :-(
        //URL url = new URL("http://stackoverflow.com/questions/10887580/how-to-convert-a-webpage-from-an-intranet-wiki-to-an-office-document");
    	
    	
        //URL url = new URL("http://en.wikipedia.org/wiki/Office_Open_XML");
    	//URL url = new URL("http://en.wikipedia.org/w/index.php?title=Office_Open_XML&printable=yes");
    	URL url = new URL("http://en.wikipedia.org/w/index.php?title=Microsoft_Word&printable=yes");
                        
        XHTMLImporter.setHyperlinkStyle("Hyperlink");
        
        // Create an empty docx package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();		
					
		// Convert the XHTML, and add it into the empty docx we made
		wordMLPackage.getMainDocumentPart().getContent().addAll( 
				XHTMLImporter.convert(url , wordMLPackage) );
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_ConvertInXHTMLURL.docx") );
      
  }
	
}
