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

package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.document.wordprocessingml.styledefinitionspart.DocDefaults;
import org.docx4j.document.wordprocessingml.styledefinitionspart.LatentStyles;
import org.docx4j.document.wordprocessingml.styledefinitionspart.Style;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Dom4jXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public final class StyleDefinitionsPart extends Dom4jXmlPart {
	
	private static Logger log = Logger.getLogger(StyleDefinitionsPart.class);		
	

	public StyleDefinitionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_STYLES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.STYLES);

	}
	
//	public StyleDefinitionsPart(PartName partName, Document contents) throws InvalidFormatException {
//		super(partName);
//		setDocument(contents);
//	}
	
//	public void setDocument(Document document) {
//		this.document = document;
//		unmarshall(document);
//	}
	
	public void setDocument(InputStream in ) {
		
		super.setDocument(in);
		
		unmarshall(document);		
		
	}
	
	
	public Document getDocument() {
		return marshall();
	}
	
	/** Styles document looks something like:
	 * 
	 * <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
	 * 
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:asciiTheme="minorHAnsi" w:eastAsiaTheme="minorHAnsi" w:hAnsiTheme="minorHAnsi" w:cstheme="minorBidi"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:lang w:val="en-US" w:eastAsia="en-US" w:bidi="ar-SA"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:after="200" w:line="276" w:lineRule="auto"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
  
  
  <w:latentStyles w:defLockedState="0" w:defUIPriority="99" w:defSemiHidden="1" w:defUnhideWhenUsed="1" w:defQFormat="0" w:count="267">
    <w:lsdException w:name="Normal" w:semiHidden="0" w:uiPriority="0" w:unhideWhenUsed="0" w:qFormat="1"/>
    <w:lsdException w:name="heading 1" w:semiHidden="0" w:uiPriority="9" w:unhideWhenUsed="0" w:qFormat="1"/>
     :
  </w:latentStyles>
  
  
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
    <w:qFormat/>
    <w:rsid w:val="00A15872"/>
  </w:style>
  <w:style w:type="character" w:default="1" w:styleId="DefaultParagraphFont">
    <w:name w:val="Default Paragraph Font"/>
    <w:uiPriority w:val="1"/>
    <w:semiHidden/>
    <w:unhideWhenUsed/>
  </w:style>
  <w:style w:type="table" w:default="1" w:styleId="TableNormal">
    <w:name w:val="Normal Table"/>
    <w:uiPriority w:val="99"/>
    <w:semiHidden/>
    <w:unhideWhenUsed/>
    <w:qFormat/>
    <w:tblPr>
    :
    </w:tblPr>
  </w:style>
  :
</w:styles>

	 */

	private DocDefaults docDefaults;
	
	private LatentStyles latentStyles;
	
	private java.util.HashMap styles; 
	
	
	public void registerStyle(Style s) {
		styles.put( s.getId(), s );
	}

	/**
	 * Load the content of this part.
	 * 
	 * @param ios
	 *            The XML document to load.
	 * @return <b>true</b> if the content has been successfully loaded, else
	 *         <b>false</b>.
	 */
	private boolean unmarshall(Document doc) {
		
		try {
			
			styles = new HashMap();
		    Element root = doc.getRootElement();
		    //Node bodyNode = root.element("body");
		    
			// We iterate through the block level elements.
			// If its a p, we make a p object.
			// Otherwise, we just stick the XML fragment into the ArrayList.
		    Iterator elementIterator = (root).elementIterator();
		    while(elementIterator.hasNext()){
		      Element element = (Element)elementIterator.next();
		      
			  if (element.getName()=="docDefaults" ) {
				  docDefaults = new DocDefaults(element);
			  } else if (element.getName()=="latentStyles" ) {
				  latentStyles = new LatentStyles(element);
			  } else if (element.getName()=="style" ) {
				  Style s = new Style(element);
				  
				  log.debug("Registered style " + s.getId());
				  
				  // TO DO - use the appropriate subclass
				  
				  registerStyle(s);
			  } else {
			    log.warn("Unrecognised XML  '" + element.getName() + "'");
				OutputFormat format = OutputFormat.createPrettyPrint();
				StringWriter sWriter = new StringWriter();			 					
			    XMLWriter xmlWriter = new XMLWriter( sWriter, format );
			    xmlWriter.write( element );
			    log.warn(sWriter.toString() );
			  }
		    }

		} catch (Exception e ) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/* Create an XML document from the Java object tree
	 * 
	 */
	private Document marshall() {
		
		// create a new Document with root element
		//		  <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" 
		//			 	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">

		Document doc = DocumentHelper.createDocument();
		Namespace nsWordprocessingML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element elDocument = doc.addElement(new QName("styles",
				nsWordprocessingML));
		// TODO add xmlns:r
		
		List docChildren = elDocument.content();
		
		// <w:docDefaults>
		docChildren.add( docDefaults.marshall() );
		
		// <w:latentStyles w:defLockedState="0" w:defUIPriority="99" w:defSemiHidden="1" w:defUnhideWhenUsed="1" w:defQFormat="0" w:count="267">
		docChildren.add( latentStyles.marshall() );

	    log.debug("Now Marshalling styles... ");
		
		Iterator i = styles.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			if (e != null) {
				
				Style s = (Style)e.getValue();
			    log.debug("Marshalling style " + s.getId());
				docChildren.add( s.marshall() );				
			} 
		} 		
		
		return doc;
		
	}
	
	
}
