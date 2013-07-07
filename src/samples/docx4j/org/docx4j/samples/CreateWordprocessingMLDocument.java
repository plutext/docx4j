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

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;

/**
 * Creates a WordprocessingML document from scratch,
 * and show several different ways of adding basic 
 * content. 
 * 
 * @author Jason Harrop
 */
public class CreateWordprocessingMLDocument extends AbstractSample {

	public static void main(String[] args) throws Exception {
		
				
		boolean save = true; 

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Example 1: add text in Title style
		mdp.addStyledParagraphOfText("Title", "Example 1");

		// Example 2: add normal paragraph (no explicit style)
		mdp.addParagraphOfText("Example 2");
		
		// Example 3a: bold text
		// To get bold text, you must set the run's rPr@w:b,
	    // so you can't use the addParagraphOfText convenience method
		
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.P  p = factory.createP();

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("Example 3a (bold)");

		org.docx4j.wml.R  run = factory.createR();
		run.getContent().add(t);		
		
		p.getContent().add(run);
		
		
		org.docx4j.wml.RPr rpr = factory.createRPr();		
		org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
	    b.setVal(true);	    
	    rpr.setB(b);
	    
		run.setRPr(rpr);
		
		// Optionally, set pPr/rPr@w:b		
	    org.docx4j.wml.PPr ppr = factory.createPPr();	    
	    p.setPPr( ppr );
	    org.docx4j.wml.ParaRPr paraRpr = factory.createParaRPr();
	    ppr.setRPr(paraRpr);	    
	    rpr.setB(b);
	    
	    mdp.getJaxbElement().getBody().getContent().add(p);
	    // or just:
	    // mdp.getContent().add(p);
	    // but:
	    // mdp.addObject(p);
	    // is a better alternative if you are using a new style, since it 
	    // will ensure that the style is activated  

		// Example 3b: bold text
	    // Well, actually you can use addParagraphOfText:
		P p3b = mdp.addParagraphOfText("Example 3b (bold)");
		R r3b = (R)p3b.getContent().get(0);
	    // .. now set rPr (I'll just reuse the above object)
		r3b.setRPr(rpr);

	    
	    // Example 4: Here is an easier way:
	    String str = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" ><w:r><w:rPr><w:b /></w:rPr><w:t>Example 4</w:t></w:r></w:p>";
	    
	    mdp.addObject(
	    			org.docx4j.XmlUtils.unmarshalString(str) );

	    
	    // Example 5: Let's add a table
	    int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
	    int cols = 3;
	    int cellWidthTwips = new Double( 
	    							Math.floor( (writableWidthTwips/cols ))
	    								).intValue();
	    
	    Tbl tbl = TblFactory.createTable(3, 3, cellWidthTwips);
	    mdp.addObject(tbl);
	    
		
	   	// Pretty print the main document part
		System.out.println(
				XmlUtils.marshaltoString(mdp.getJaxbElement(), true, true) );
		
		// Optionally save it
		if (save) {
			String filename = System.getProperty("user.dir") + "/OUT_CreateWordprocessingMLDocument.docx";
			wordMLPackage.save(new java.io.File(filename) );
			System.out.println("Saved " + filename);
		}
						
	}
	
}
