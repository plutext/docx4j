/*
 *  Copyright 2015, Plutext Pty Ltd.
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

import org.docx4j.Docx4J;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.ProtectDocument;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.STDocProtect;

/**
 * Create a docx which says "hello world"
 * 
 * @author Jason Harrop
 * @since 3.3.0
 */
public class NewDocxHelloWorld extends AbstractSample {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");
		
//		ProtectDocument protection = new ProtectDocument(wordMLPackage);
//		protection.restrictEditing(STDocProtect.READ_ONLY, "foobaa");
		
		// Optionally, set compatibilityMode to 15 to avoid Word 365/2016 saying "Compatibility Mode" 
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart(true);
		CTCompat compat = Context.getWmlObjectFactory().createCTCompat(); 
		dsp.getContents().setCompat(compat);
	    compat.setCompatSetting("compatibilityMode", "http://schemas.microsoft.com/office/word", "15");
		
		String filename = System.getProperty("user.dir") + "/OUT_hello_new.docx";
		Docx4J.save(wordMLPackage, new java.io.File(filename), Docx4J.FLAG_SAVE_ZIP_FILE); 
		System.out.println("Saved " + filename);
						
	}
	
}
