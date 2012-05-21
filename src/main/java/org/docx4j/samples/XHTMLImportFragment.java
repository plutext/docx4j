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

import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * This sample converts a fragment of XHTML to docx.  The fragment should
 * be one or more block level objects.
 * 
 * For best results, be sure to include src/main/resources on your classpath. 
 *
 */
public class XHTMLImportFragment {

    public static void main(String[] args) throws Exception {
        
    	//String xhtml= "<div><p>The <b>quick</b> <span style=\"font-size: 14pt;\">brown</span> fox...</p><p>Paragraph 2</p></div>";
    	
        String xhtml= "<table class=\"reference\" style=\"width:60%\">\n" +
                "\t<tr>\n" +
                "\t\t<td>Apples</td>\n" +
                "\t\t<td>44%</td>\n" +
                "\t</tr>\n" +
                "\t<tr>\n" +
                "\t\t<td>Bananas</td>\n" +
                "\t\t<td>23%</td>\n" +
                "\t</tr>\n" +
                "\t<tr>\n" +
                "\t\t<td>Oranges</td>\n" +
                "\t\t<td>13%</td>\n" +
                "\t</tr>\n" +
                "\t<tr>\n" +
                "\t\t<td>Other</td>\n" +
                "\t\t<td>10%</td>\n" +
                "\t</tr>\n" +
                "</table>\n";
        		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		wordMLPackage.getMainDocumentPart().getContent().addAll( 
				XHTMLImporter.convert( xhtml, null, wordMLPackage) );
	
	System.out.println(
			XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
      
  }
	
}
