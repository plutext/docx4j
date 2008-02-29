/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.parts.WordprocessingML;

//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URI;
//
//import javax.xml.bind.JAXBContext;
//import java.net.URI;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
//import org.docx4j.openpackaging.exceptions.Docx4JException;
//import org.docx4j.openpackaging.parts.DocPropsCorePart;
//import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.ThemePart;
//import org.dom4j.Document;



public abstract class DocumentPart extends JaxbXmlPart {
	
	/** Parts which can be the target of a relationship from either
	 *  the Main Document or the Glossary Document
	 *  
	 *  Microsoft's Microsoft.Office.DocumentFormat.OpenXML has
	 *  no equivalent of this.  
	 *  
	 */ 
	
	protected CommentsPart commentsPart; //done
	
	protected DocumentSettingsPart documentSettingsPart;
	
	protected EndnotesPart endNotesPart; //done
	
	protected FontTablePart fontTablePart; // done (ie setup below and in SchemaType_
	protected ThemePart themePart; // done 
	
	
	protected FootnotesPart footnotesPart; //done
	
	//protected List headerParts;
	//protected List footerParts;
	
	protected NumberingDefinitionsPart numberingDefinitionsPart; //done
	
	protected StyleDefinitionsPart styleDefinitionsPart; //done
	
	protected WebSettingsPart webSettingsPart; //done
	
	public boolean setPartShortcut(Part part, String relationshipType) {
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


	public FontTablePart getFontTablePart() {
		return fontTablePart;
	}


//	public List getFooterParts() {
//		return footerPart;
//	}


	public FootnotesPart getFootnotesPart() {
		return footnotesPart;
	}


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
