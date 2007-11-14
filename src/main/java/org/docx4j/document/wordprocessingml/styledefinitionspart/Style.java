/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4j.document.wordprocessingml.styledefinitionspart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.document.wordprocessingml.Paragraph;
import org.docx4j.document.wordprocessingml.RunProperties;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


/**
 *
 * @author Jason Harrop
 */
public class Style {

	private static Logger log = Logger.getLogger(Style.class);		

	/** Example of a w:style element
	 * 
  <w:style w:type="paragraph" w:styleId="Subtitle">
    <w:name w:val="Subtitle"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:link w:val="SubtitleChar"/>
    <w:uiPriority w:val="11"/>
    <w:qFormat/>
    <w:rsid w:val="00637B47"/>
    <w:pPr>
      <w:numPr>
        <w:ilvl w:val="1"/>
      </w:numPr>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:asciiTheme="majorHAnsi" w:eastAsiaTheme="majorEastAsia" w:hAnsiTheme="majorHAnsi" w:cstheme="majorBidi"/>
      <w:i/>
      <w:iCs/>
      <w:color w:val="4F81BD" w:themeColor="accent1"/>
      <w:spacing w:val="15"/>
      <w:sz w:val="24"/>
      <w:szCs w:val="24"/>
    </w:rPr>
  </w:style>
  
  <w:style w:type="character" w:customStyle="1" w:styleId="SubtitleChar">
    <w:name w:val="Subtitle Char"/>
    <w:basedOn w:val="DefaultParagraphFont"/>
    <w:link w:val="Subtitle"/>
    <w:uiPriority w:val="11"/>
    <w:rsid w:val="00637B47"/>
    <w:rPr>
      <w:rFonts w:asciiTheme="majorHAnsi" w:eastAsiaTheme="majorEastAsia" w:hAnsiTheme="majorHAnsi" w:cstheme="majorBidi"/>
      <w:i/>
      <w:iCs/>
      <w:color w:val="4F81BD" w:themeColor="accent1"/>
      <w:spacing w:val="15"/>
      <w:sz w:val="24"/>
      <w:szCs w:val="24"/>
    </w:rPr>
  </w:style>
  
	 */
	
	// w:type is the subclass
	
	String styleId;
	String name;
	
	String basedOn;
	String next;	
	String link;
	
	int uiPriority;
	
	// Not sure whether a table style can have this, but both paragraph and character styles can
	RunProperties rPr;

	protected HashMap styleChildren = new HashMap();
	
	private Element e;

	
	public Style( Element e) {
		
		this.e = e;
		
		styleId = e.attributeValue("styleId");
		//log.info("styleId:" + styleId);
		
	    Iterator elementIterator = ((Element)e).elementIterator();
	    while(elementIterator.hasNext()){
	      Element element = (Element)elementIterator.next();

		  if (element.getName()=="name" ) {
			  name = element.attributeValue("val");  
			  //log.info("Style name was " + name);
		  } else if (element.getName()=="basedOn" ) {
			  basedOn = element.attributeValue("val");  
		  } else if (element.getName()=="next" ) {
			  next = element.attributeValue("val");  
		  } else if (element.getName()=="link" ) {
			  link = element.attributeValue("val");  
		  } else if (element.getName()=="uiPriority" ) {
			  //log.debug("uiPriority = " + element.attributeValue("val") );
			  if ( element.attributeValue("val")!=null ) {
				  uiPriority = new Integer(element.attributeValue("val")).intValue();
			  }
		  } else if (element.getName()=="rPr" ) {
			  //log.debug("found RunProperties...");
			  rPr = new RunProperties(element);  
		  } else if (element.getName()=="pPr" ) {
			  log.warn("found ParagraphProperties...TODO - process this in the subclass");
		  } else {
		      log.debug("Adding raw XML for '" + element.getName() + "'");
		  }

		  // For the moment, in all cases, put the raw XML into the HashMap
	      styleChildren.put( element.getName(), element );

	    }
	    
	}
	
	public String getId() {
		
		return styleId;
	}

	public String getName() {
		
		return name;
	}
	
	/* TODO - once we get to the point where styles can be manipulated,
	 * will need to a proper marshall function!
	 */
	public Element marshall() {
		return e;
	}


}
