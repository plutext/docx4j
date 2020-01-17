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

import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTFtnDocProps;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.R;

/**
 * How to add a footnote 
 * 
 * @author Jason Harrop
 */
public class FootnoteAdd  {
	
	static org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Setup FootnotesPart if necessary,
		// along with DocumentSettings
		FootnotesPart footnotesPart = mdp.getFootnotesPart();
		if (footnotesPart==null) { // that'll be the case in this example
			// initialise it
			footnotesPart = new FootnotesPart();
			mdp.addTargetPart(footnotesPart);
			
			CTFootnotes footnotes = (CTFootnotes)XmlUtils.unwrap(
					XmlUtils.unmarshalString(footnotePartXML));	
			footnotesPart.setJaxbElement(footnotes);
			
			// Usually the settings part contains footnote properties;
			// so add these if not present
			DocumentSettingsPart dsp =mdp.getDocumentSettingsPart();
			if (dsp==null) {
				// create it
				dsp = new DocumentSettingsPart();
				mdp.addTargetPart(dsp);
			} 
			CTSettings settings = dsp.getContents();
			if (settings ==null) {
				settings = wmlObjectFactory.createCTSettings(); 
				dsp.setJaxbElement(settings);				
			}
			
			CTFtnDocProps ftndocprops = settings.getFootnotePr();
			if (ftndocprops==null ) {
				String openXML = "<w:footnotePr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			            + "<w:footnote w:id=\"-1\"/>" // these 2 numbers are special, and correspond with string footnotePartXML below
			            + "<w:footnote w:id=\"0\"/>"
			        +"</w:footnotePr>";
				settings.setFootnotePr(
						(CTFtnDocProps)XmlUtils.unmarshalString(openXML, Context.jc, CTFtnDocProps.class	 ));
			}
		}
		
		// Example
		// Create and add p
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.P  p = factory.createP();
		mdp.getContent().add(p);
		
		// Add a run
		R r = new R();
		p.getContent().add(r);		

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("Hello world");
		r.getContent().add(t);
		
		// OK, add a footnote
		addFootnote(1, "my footnote", footnotesPart, r);  
			// Note: your footnote ids must be distinct; they don't need to be ordered (though Word will do that when you open the docx)
		
		// Save it
		String filename = System.getProperty("user.dir") + "/OUT_FootnoteAdd.docx";
		wordMLPackage.save(new java.io.File(filename) );
		System.out.println("Saved " + filename);
						
	}
	
	public static void addFootnote(int i, String text, FootnotesPart footnotesPart, R r) throws JAXBException, Docx4JException {
		
		// Add the note number in the run
	    CTFtnEdnRef ftnednref = wmlObjectFactory.createCTFtnEdnRef(); 
	    JAXBElement<org.docx4j.wml.CTFtnEdnRef> ftnednrefWrapped = wmlObjectFactory.createRFootnoteReference(ftnednref); 
	    r.getContent().add( ftnednrefWrapped); 
	        ftnednref.setId( BigInteger.valueOf( i) );
	        
	    // Create a footnote in the footnotesPart
	        String openXML = "<w:footnote w:id=\"" + i + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">"
	                + "<w:p>"
	                    + "<w:pPr>"
	                        + "<w:pStyle w:val=\"FootnoteText\"/>"
	                        + "<w:rPr>"
	                            + "<w:lang w:val=\"en-AU\"/>"
	                        +"</w:rPr>"
	                    +"</w:pPr>"
	                    + "<w:r>"
	                        + "<w:rPr>"
	                            + "<w:rStyle w:val=\"FootnoteReference\"/>"
	                        +"</w:rPr>"
	                        + "<w:footnoteRef/>"
	                    +"</w:r>"
	                    + "<w:r>"
	                        + "<w:t xml:space=\"preserve\"> </w:t>"
	                    +"</w:r>"
	                    + "<w:r>"
	                        + "<w:rPr>"
	                            + "<w:lang w:val=\"en-AU\"/>"
	                        +"</w:rPr>"
	                        + "<w:t>" + text +"</w:t>"
	                    +"</w:r>"
	                +"</w:p>"
	            +"</w:footnote>";	        
		
	        CTFtnEdn ftnedn = (CTFtnEdn)XmlUtils.unmarshalString(openXML, Context.jc, CTFtnEdn.class);	        
	        footnotesPart.getContents().getFootnote().add(ftnedn);
	}
	
	static String footnotePartXML = "<w:footnotes mc:Ignorable=\"w14 wp14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
            + "<w:footnote w:id=\"-1\" w:type=\"separator\">"  // matching CTFtnDocProps above
                + "<w:p>"
                    + "<w:pPr>"
                        + "<w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/>"
                    +"</w:pPr>"
                    + "<w:r>"
                        + "<w:separator/>"
                    +"</w:r>"
                +"</w:p>"
            +"</w:footnote>"
            + "<w:footnote w:id=\"0\" w:type=\"continuationSeparator\">"
                + "<w:p>"
                    + "<w:pPr>"
                        + "<w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/>"
                    +"</w:pPr>"
                    + "<w:r>"
                        + "<w:continuationSeparator/>"
                    +"</w:r>"
                +"</w:p>"
            +"</w:footnote>"           
        +"</w:footnotes>";
	
	
	
}
