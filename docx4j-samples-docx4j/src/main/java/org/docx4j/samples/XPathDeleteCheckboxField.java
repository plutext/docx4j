/*
 *  Copyright 2007-2019, Plutext Pty Ltd.
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
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;


/**
 * This deletes complex fields of type checkBox.
 * 
 * It assumes there is only one complex field per paragraph.
 * It also assumes that the run which begins the field doesn't have any wanted content before the fldChar begin.
 * Similarly, it assumes that the run which ends the field doesn't have any wanted content after the fldChar end.
 * @author jharrop
 *
 */
public class XPathDeleteCheckboxField {
	
	public static void main(String[] args) throws Exception {

	    String inputfilepath = System.getProperty("user.dir") + "/19pr2.docx";
		
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Find the checkbox
		// Here we look for checkbox, then climb the tree to the paragraph containing it.
		// Probably it would be neater to look for fldChar begin, then check whether its ffData contains a checkBox
		List<Object> listz = documentPart.getJAXBNodesViaXPath("//w:checkBox",false);
		
		System.out.println("Found: " + listz.size() );
	   for (Object c : listz) {
		   
		   // From the w:checkBox, find the containing p
		   c = XmlUtils.unwrap(c);
		   
		   org.docx4j.wml.CTFFCheckBox checkBox = (org.docx4j.wml.CTFFCheckBox)c;
		   org.docx4j.wml.CTFFData ffData = (org.docx4j.wml.CTFFData)checkBox.getParent();
		   org.docx4j.wml.FldChar fldChar = (org.docx4j.wml.FldChar)ffData.getParent();
		   
		   R r = (R)fldChar.getParent();
		   P p = (P)r.getParent();
		   
		   // Now delete the complex field in this p
		   deleteComplexField(p);		   
	   }
		   
		   
		// Save it
//		String outputfilepath = System.getProperty("user.dir") + "/OUT_XPathDeleteCheckboxField.docx";
//		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
//		
//		System.out.println("Saved: " + outputfilepath);
	}

	public static void deleteComplexField(P p) {
		
		List<R> runsToDelete = new ArrayList<R>();
		boolean inField = false;
		
		
		for (Object o : p.getContent()) {

			o = XmlUtils.unwrap(o);			
			if (!(o instanceof R)) continue; // keep bookmark, proofErr etc in this sample code
			
			if (!inField) {
				inField = doesRcontainField( (R)o, STFldCharType.BEGIN );
			}
			if (inField) {
				runsToDelete.add( (R)o );
				
				if (doesRcontainField( (R)o, STFldCharType.END )) {
					break;
				}
			}
		}
		
		// Now we know what to delete, so do it
		for (R r : runsToDelete ) {
		
			System.out.println("deleting " + XmlUtils.marshaltoString(r));
			p.getContent().remove(r);
		}
		
		
	}

	public static boolean doesRcontainField(R r, STFldCharType fldCharType ) {
				
		for (Object o : r.getContent() ) {
			
			o = XmlUtils.unwrap(o);			
			if (o instanceof org.docx4j.wml.FldChar) {
				
				org.docx4j.wml.FldChar fldChar = (org.docx4j.wml.FldChar)o;
				return (fldChar.getFldCharType()==fldCharType);
			}
		}
		return false;
	}
	
}
