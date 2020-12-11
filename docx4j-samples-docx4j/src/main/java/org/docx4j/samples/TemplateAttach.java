/*
 *  Copyright 2007-2018, Plutext Pty Ltd.
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

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 *Shows how to creates a WordprocessingML document from a template (dotx file),
 * and how to attach a template (for example, instead of Normal.dot)
 * 
 * @author Jason Harrop
 */
public class TemplateAttach extends AbstractSample {
	
	public static void main(String[] args) throws Exception {
		
		String dotxPath = "C:\\Users\\jharrop\\AppData\\Roaming\\Microsoft\\Templates\\mytemplate.dotx";
		String templatePath = "file:///" + dotxPath;
		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/Normal.dotm";	    	
		}
		
    	String outputfilepath = System.getProperty("user.dir") + "/OUT_TemplateAttach.docx";	    	
		
		WordprocessingMLPackage docx = null;
		if (inputfilepath.endsWith(".dotx")
				|| inputfilepath.endsWith(".dotm") ) {
			// If its a dotx, make a docx out of it
			System.out.println("Converting template to docx...");
			WordprocessingMLPackage dotx = Docx4J.load(new File(inputfilepath));
			docx = (WordprocessingMLPackage) dotx.cloneAs(ContentTypes.WORDPROCESSINGML_DOCUMENT);
				// or WORDPROCESSINGML_DOCUMENT_MACROENABLED if you wish
				// TODO if it was a dotm, you may need to remove the macro parts from the clone

			// The dotx should be unchanged
			dotx.save(new File(System.getProperty("user.dir") + "/OUT_TemplateAttach_RTT.dotx"));
		} else {
			docx = Docx4J.load(new File(inputfilepath));
		}
		
		// attach the template.  This example uses the path you've specified, not inputfilepath
		System.out.println("Attaching template to docx...");
		docx.attachTemplate(templatePath);
		
		// Now save it
		if (outputfilepath!=null) {
			docx.save(new java.io.File(outputfilepath) );
		} 
	}
	
	
}
