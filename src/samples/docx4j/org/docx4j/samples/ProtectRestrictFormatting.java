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


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.ProtectDocument;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


/**
 * Restrict formatting to an allowed set of styles. 
 * @author jharrop
 *
 */
public class ProtectRestrictFormatting extends AbstractSample {


	public static void main(String[] args) throws Exception {

		inputfilepath = System.getProperty("user.dir") + "/The cat.docx";
		
		WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)OpcPackage
				.load(new java.io.File(inputfilepath), "foobaaQ");
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		List<String> allowedStyleNames = new ArrayList<String>();
		allowedStyleNames.add("heading 2");
		allowedStyleNames.add("heading 3");
		
		ProtectDocument protection = new ProtectDocument(wordMLPackage);
		protection.restrictFormatting(allowedStyleNames, true, //remove!
				false, true, false);
		
		
		String filename = System.getProperty("user.dir") + "/OUT_restrict_formatting_remove.docx";
		
		Docx4J.save(wordMLPackage, new java.io.File(filename)); // , Docx4J.FLAG_SAVE_ENCRYPTED_AGILE, "foobaa");
		
		System.out.println("Saved " + filename);
		}

	}

