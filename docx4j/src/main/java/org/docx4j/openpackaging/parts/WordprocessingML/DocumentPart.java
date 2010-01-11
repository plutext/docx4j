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

package org.docx4j.openpackaging.parts.WordprocessingML;

//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URI;
//
//import javax.xml.bind.JAXBContext;
//import java.net.URI;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Hdr;
import org.w3c.dom.Node;



public abstract class DocumentPart<E> extends JaxbXmlPart<E> {
	
	/** Parts which can be the target of a relationship from either
	 *  the Main Document or the Glossary Document
	 *  
	 *  Microsoft's Microsoft.Office.DocumentFormat.OpenXML has
	 *  no equivalent of this.  
	 *  
	 */ 
	
	protected CommentsPart commentsPart; 	
	protected DocumentSettingsPart documentSettingsPart;	
	protected EndnotesPart endNotesPart; 	
	protected FontTablePart fontTablePart; 
	protected ThemePart themePart;  
	protected FootnotesPart footnotesPart; 
	protected NumberingDefinitionsPart numberingDefinitionsPart; 	
	protected StyleDefinitionsPart styleDefinitionsPart; 	
	protected WebSettingsPart webSettingsPart;
	
	// To access headerParts and footerParts, instead
	// use the DocumentModel.


	public boolean setPartShortcut(Part part) {
		
		if (part == null ){
			return false;
		} else {
			return setPartShortcut(part, part.getRelationshipType() );
		}
		
	}	
		
	public boolean setPartShortcut(Part part, String relationshipType) {
		
		// Since each part knows its relationshipsType,
		// why is this passed in as an arg?
		// Answer: where the relationshipType is ascertained
		// from the rel itself, it is the most authoritative.
		// Note that we normally use the info in [Content_Types]
		// to create a part of the correct type.  This info
		// will not necessary correspond to the info in the rel!
		
		if (relationshipType==null) {
			log.warn("trying to set part shortcut against a null relationship type.");
			return false;
		}
		
		if (relationshipType.equals(Namespaces.FONT_TABLE)) {
			fontTablePart = (FontTablePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.THEME)) {
			themePart = (ThemePart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.STYLES)) {
			styleDefinitionsPart = (StyleDefinitionsPart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.WEB_SETTINGS)) {
			webSettingsPart = (WebSettingsPart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.SETTINGS)) {
			documentSettingsPart = (DocumentSettingsPart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.COMMENTS)) {
			commentsPart = (CommentsPart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.ENDNOTES)) {
			endNotesPart = (EndnotesPart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.FOOTNOTES)) {
			footnotesPart = (FootnotesPart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.NUMBERING)) {
			numberingDefinitionsPart = (NumberingDefinitionsPart)part;
			return true;	
			
			// TODO - to be completed.
		} else {	
			return false;
		}
	}
	
	
	/** Other characteristics which both the Main Document and the
	 *  Glossary Document have.
	 */ 
	//private VML background; // [1.2.1]
	
	
	public DocumentPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}

	

	public CommentsPart getCommentsPart() {
		return commentsPart;
	}


	public DocumentSettingsPart getDocumentSettingsPart() {
		return documentSettingsPart;
	}


	public EndnotesPart getEndNotesPart() {
		return endNotesPart;
	}
	public static Node getEndnotes(WordprocessingMLPackage wmlPackage) {
		return XmlUtils.marshaltoW3CDomDocument(
				wmlPackage.getMainDocumentPart().getEndNotesPart().getJaxbElement());		
	}
	
	public static boolean hasEndnotesPart(WordprocessingMLPackage wmlPackage) {
		if (wmlPackage.getMainDocumentPart().getEndNotesPart()==null) {
			return false;
		} else {
			return true;
		}
	}

	public FootnotesPart getFootnotesPart() {
		return footnotesPart;
	}

	public static Node getFootnotes(WordprocessingMLPackage wmlPackage) {		
		return XmlUtils.marshaltoW3CDomDocument(
				wmlPackage.getMainDocumentPart().getFootnotesPart().getJaxbElement());		
	}
	
	public static boolean hasFootnotesPart(WordprocessingMLPackage wmlPackage) {
		if (wmlPackage.getMainDocumentPart().getFootnotesPart()==null) {
			return false;
		} else {
			return true;
		}
	}

	public FontTablePart getFontTablePart() {
		return fontTablePart;
	}


//	public List getFooterParts() {
//		return footerPart;
//	}
	
//	public List getHeaderParts() {
//		return headerPart;
//	}


	public NumberingDefinitionsPart getNumberingDefinitionsPart() {
		return numberingDefinitionsPart;
	}


	public StyleDefinitionsPart getStyleDefinitionsPart() {
		return styleDefinitionsPart;
	}

	public ThemePart getThemePart() {
		return themePart;
	}
	

	public WebSettingsPart getWebSettingsPart() {
		return webSettingsPart;
	}

	
}
