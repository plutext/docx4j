/*
 *  Copyright 2014, Plutext Pty Ltd.
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

package org.xlsx4j.samples;


import java.io.File;
import java.util.HashMap;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.JaxbSmlPart;



public class VariableReplace {
	
	public static void main(String[] args) throws Exception {
		
		// Input xslx has variables in it: ${title1}
		String inputfilepath = System.getProperty("user.dir") + "/sample2.xlsx";

		boolean save = false;
		String outputfilepath = System.getProperty("user.dir")
				+ "/OUT_VariableReplace.xlsx";
		
		SpreadsheetMLPackage opcPackagepkg = SpreadsheetMLPackage.load(new File(inputfilepath));
		
		// Be sure to get the part which actually contains your variables!
		JaxbSmlPart smlPart = (JaxbSmlPart)opcPackagepkg.getParts().get(new PartName("/xl/sharedStrings.xml"));
		//JaxbSmlPart smlPart = (JaxbSmlPart)opcPackagepkg.getParts().get(new PartName("/xl/worksheets/sheet1.xml"));
		
		
		System.out.println("\n\nBEFORE\n\n:" + smlPart.getXML());
		
		HashMap<String, String> mappings = new HashMap<String, String>();
		mappings.put("title1", "This is a title");

		smlPart.variableReplace(mappings);


		// Save it
		if (save) {
			opcPackagepkg.save(new File(outputfilepath));		
		} else {
			System.out.println("\n\nAFTER\n\n:" + smlPart.getXML());
		}
	}

}
