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


package org.docx4j.samples;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

/**
 * Example of adding the INCLUDEPICTURE field, via simple and complex field approaches.
 * 
 * @author Jason Harrop
 */
public class FieldINCLUDEPICTURE extends AbstractSample {

	public static void main(String[] args) throws Exception {
		
				
		boolean save = true; 

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Example 1: add text in Title style
		mdp.addStyledParagraphOfText("Title", "Select all, then hit F9 in Word to see your pictures, or programmatically add them first");
		
		mdp.createParagraphOfText("simple field:");
		
		P p = new P(); 
		p.getContent().add( 
				createSimpleField( " INCLUDEPICTURE  \"file:///C:/Users/jharrop/git/plutext/docx4j/src/test/resources/images/greentick.png\"  \\* MERGEFORMAT ")
				);
		mdp.getContent().add(p);

		mdp.createParagraphOfText("complex field:");
		
		p = new P(); 
		addComplexField(p, " INCLUDEPICTURE  \"file:///C:/Users/jharrop/git/plutext/docx4j/src/test/resources/images/greentick.png\"  \\* MERGEFORMAT ");
		mdp.getContent().add(p);
	    
		
	   	// Pretty print the main document part
		System.out.println(
				XmlUtils.marshaltoString(mdp.getJaxbElement(), true, true) );
		
		// Optionally save it
		if (save) {
			String filename = System.getProperty("user.dir") + "/OUT_FieldINCLUDEPICTURE.docx";
			wordMLPackage.save(new java.io.File(filename) );
			System.out.println("Saved " + filename);
		}
						
	}
	
	private static CTSimpleField createSimpleField(String val) {
		
		CTSimpleField field = new CTSimpleField();
		field.setInstr(val);
	    return field;
	}

	private static void addComplexField(P p, String instrText) {
		
		org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

		// Create object for r
	    R r = wmlObjectFactory.createR(); 
	    p.getContent().add( r); 
	        // Create object for fldChar (wrapped in JAXBElement) 
	        FldChar fldchar = wmlObjectFactory.createFldChar(); 
	        JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped = wmlObjectFactory.createRFldChar(fldchar); 
	        r.getContent().add( fldcharWrapped); 
	            fldchar.setFldCharType(org.docx4j.wml.STFldCharType.BEGIN);
	        // Create object for instrText (wrapped in JAXBElement) 
	        Text text = wmlObjectFactory.createText(); 
	        JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRInstrText(text); 
	        r.getContent().add( textWrapped); 
	            text.setValue( instrText); 
	            text.setSpace( "preserve"); 	

	        // Create object for fldChar (wrapped in JAXBElement) 
	        fldchar = wmlObjectFactory.createFldChar(); 
	        fldcharWrapped = wmlObjectFactory.createRFldChar(fldchar); 
	        r.getContent().add( fldcharWrapped); 
	            fldchar.setFldCharType(org.docx4j.wml.STFldCharType.END);
		
	}
	
}
