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


import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPartAltChunkHost;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.QuestionsPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;



public abstract class DocumentPart<E> extends JaxbXmlPartAltChunkHost<E> {
	
	protected static Logger log = LoggerFactory.getLogger(DocumentPart.class);
	
	
	/** Parts which can be the target of a relationship from either
	 *  the Main Document or the Glossary Document
	 *  
	 *  Microsoft's Microsoft.Office.DocumentFormat.OpenXML has
	 *  no equivalent of this.  
	 *  
	 */ 
	
	protected CommentsPart commentsPart; 	
	protected CommentsExtendedPart commentsExtendedPart; 	
	protected CommentsIdsPart commentsIdsPart; 	
	protected DocumentSettingsPart documentSettingsPart;	
	protected EndnotesPart endNotesPart; 	
	protected FontTablePart fontTablePart; 
	protected PeoplePart peoplePart;  
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
		} else if (relationshipType.equals(Namespaces.COMMENTS_EXTENDED)) {
			commentsExtendedPart = (CommentsExtendedPart)part;
			return true;	
		} else if (relationshipType.equals(Namespaces.COMMENTS_IDS)) {
			commentsIdsPart = (CommentsIdsPart)part;
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
		} else if (relationshipType.equals(Namespaces.OFFICE_2011_PEOPLE)) {
			peoplePart = (PeoplePart)part;
			return true;	
		} else if (part instanceof ConditionsPart) {
			conditionsPart = ((ConditionsPart)part);
			return true;
		} else if (part instanceof QuestionsPart) {
			questionsPart = ((QuestionsPart)part);
			return true;
		} else if (part instanceof XPathsPart) {
			xPathsPart = ((XPathsPart)part);
			return true;
		} else if (part instanceof ComponentsPart) {
			componentsPart = ((ComponentsPart)part);
			return true;
		} else if (part instanceof BibliographyPart) {
			bibliographyPart = ((BibliographyPart)part);
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

	public CommentsExtendedPart getCommentsExtendedPart() {
		return commentsExtendedPart;
	}
	
	public CommentsIdsPart getCommentsIdsPart() {
		return commentsIdsPart;
	}

	public DocumentSettingsPart getDocumentSettingsPart() {
		return documentSettingsPart;
	}
	
	/**
	 * @param createIfAbsent
	 * @return
	 * @throws InvalidFormatException 
	 * @since 3.3.0
	 */
	public DocumentSettingsPart getDocumentSettingsPart(boolean createIfAbsent) throws InvalidFormatException {
		if (documentSettingsPart==null) {
			if (createIfAbsent) {
				documentSettingsPart = new DocumentSettingsPart();
				this.addTargetPart(documentSettingsPart);
				
				documentSettingsPart.setContents(new CTSettings());
			} else {
				return null;
			}
		} 
		return documentSettingsPart;		
	}
	

	


	public EndnotesPart getEndNotesPart() {
		return endNotesPart;
	}
	
	/**
	 * Does this package contain an endnotes part, with real endnotes
	 * in it?
	 * @return
	 */
	public boolean hasEndnotesPart() {
		if (getEndNotesPart()==null) {
			return false;
		} else {
			// Word seems to add an endnotes part when it adds a footnotes part,
			// so existence of part is not determinative
			CTEndnotes endnotes = getEndNotesPart().getJaxbElement();
			
			if (endnotes.getEndnote().size()<3) {
				// id's 0 & 1 are:
				// <w:endnote w:type="separator" w:id="0">
				// <w:endnote w:type="continuationSeparator" w:id="1">
				return false;
			}
			return true;
		}
	}

	public FootnotesPart getFootnotesPart() {
		return footnotesPart;
	}
	
	public boolean hasFootnotesPart() {
		if (getFootnotesPart()==null) {
			return false;
		} else {
			return true;
		}
	}

	@Deprecated // see instead org.docx4j.convert.out.Converter
	public static Node getFootnote(WordprocessingMLPackage wmlPackage, String id) {	
		
		CTFootnotes footnotes = wmlPackage.getMainDocumentPart().getFootnotesPart().getJaxbElement();
		int pos = Integer.parseInt(id);
		
		// No @XmlRootElement on CTFtnEdn, so .. 
		CTFtnEdn ftn = (CTFtnEdn)footnotes.getFootnote().get(pos);
		Document d = XmlUtils.marshaltoW3CDomDocument( ftn,
				Context.jc, Namespaces.NS_WORD12, "footnote",  CTFtnEdn.class );
		log.debug("Footnote " + id + ": " + XmlUtils.w3CDomNodeToString(d));
		return d;
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

	public PeoplePart getPeoplePart() {
		return peoplePart;
	}

	public StyleDefinitionsPart getStyleDefinitionsPart() {
		return getStyleDefinitionsPart(false);
	}
	
	public StyleDefinitionsPart getStyleDefinitionsPart(boolean create) {
		if (styleDefinitionsPart==null
				&& create) {
			// HTML, PDF output won't work without this
			log.info("No StyleDefinitionsPart detected. Adding default part.");
			try {
				styleDefinitionsPart = new StyleDefinitionsPart();
				styleDefinitionsPart.unmarshalDefaultStyles();
				this.addTargetPart(styleDefinitionsPart); 			
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return styleDefinitionsPart;
	}

	public ThemePart getThemePart() {
		return themePart;
	}
	

	public WebSettingsPart getWebSettingsPart() {
		return webSettingsPart;
	}

	private ConditionsPart conditionsPart;
	public ConditionsPart getConditionsPart() {
		return conditionsPart;
	}
//	public void setConditionsPart(ConditionsPart conditionsPart) {
//		this.conditionsPart = conditionsPart;
//	}

	private XPathsPart xPathsPart;
	public XPathsPart getXPathsPart() {
		return xPathsPart;
	}
//	public void setXPathsPart(XPathsPart xPathsPart) {
//		this.xPathsPart = xPathsPart;
//	}
	
	private QuestionsPart questionsPart;
	public QuestionsPart getQuestionsPart() {
		return questionsPart;
	}
	
	private ComponentsPart componentsPart;
	public ComponentsPart getComponentsPart() {
		return componentsPart;
	}
	
	private BibliographyPart bibliographyPart;
	public BibliographyPart getBibliographyPart() {
		return bibliographyPart;
	}
	
}
