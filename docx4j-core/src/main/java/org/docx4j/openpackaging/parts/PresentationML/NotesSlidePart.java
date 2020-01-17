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

package org.docx4j.openpackaging.parts.PresentationML;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class NotesSlidePart extends JaxbPmlPart<Notes> {  // p:notes

	protected static Logger log = LoggerFactory.getLogger(NotesSlidePart.class);	
	
	private static final String COMMON_SLIDE_NOTES = 
		"<p:notes xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"\n" + 
		"	xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" + 
		"	xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">\n" + 
		"	<p:cSld>\n" + 
		"		<p:spTree>\n" + 
		"			<p:nvGrpSpPr>\n" + 
		"				<p:cNvPr id=\"1\" name=\"\" />\n" + 
		"				<p:cNvGrpSpPr />\n" + 
		"				<p:nvPr />\n" + 
		"			</p:nvGrpSpPr>\n" + 
		"			<p:grpSpPr>\n" + 
		"				<a:xfrm>\n" + 
		"					<a:off x=\"0\" y=\"0\" />\n" + 
		"					<a:ext cx=\"0\" cy=\"0\" />\n" + 
		"					<a:chOff x=\"0\" y=\"0\" />\n" + 
		"					<a:chExt cx=\"0\" cy=\"0\" />\n" + 
		"				</a:xfrm>\n" + 
		"			</p:grpSpPr>\n" + 
		"			<p:sp>\n" + 
		"				<p:nvSpPr>\n" + 
		"					<p:cNvPr id=\"2\" name=\"Notes Placeholder 1\" />\n" + 
		"					<p:cNvSpPr>\n" + 
		"						<a:spLocks noGrp=\"1\" />\n" + 
		"					</p:cNvSpPr>\n" + 
		"					<p:nvPr>\n" + 
		"						<p:ph type=\"body\" idx=\"1\" />\n" + 
		"					</p:nvPr>\n" + 
		"				</p:nvSpPr>\n" + 
		"				<p:spPr />\n" + 
		"				<p:txBody>\n" + 
		"					<a:bodyPr>\n" + 
		"						<a:normAutofit />\n" + 
		"					</a:bodyPr>\n" + 
		"					<a:lstStyle />\n" + 
		"					<a:p>\n" + 
//		"						<a:r>\n" + 
//		"							<a:rPr smtClean=\"0\" />\n" + 
//		"							<a:t></a:t>\n" + 
//		"						</a:r>\n" + 
		"						<a:endParaRPr />\n" + 
		"					</a:p>\n" + 
		"				</p:txBody>\n" + 
		"			</p:sp>\n" + 
		"		</p:spTree>\n" + 
		"	</p:cSld>\n" + 
		"	<p:clrMapOvr>\n" + 
		"		<a:masterClrMapping />\n" + 
		"	</p:clrMapOvr>\n" + 
		"</p:notes>";
	
	public NotesSlidePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public NotesSlidePart() throws InvalidFormatException {
		super(new PartName("/ppt/notesSlides/notesSlide1.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_NOTES_SLIDE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_NOTES_SLIDE);
		
	}

	public static Notes createNotes() throws JAXBException {
		return (Notes)XmlUtils.unmarshalString(COMMON_SLIDE_NOTES, Context.jcPML, Notes.class);
	}
	
	public static Notes createNotesMinimal() throws JAXBException {

		ObjectFactory factory = Context.getpmlObjectFactory(); 
		Notes notes = factory.createNotes();
		notes.setCSld( 
				(CommonSlideData)XmlUtils.unmarshalString(COMMON_SLIDE_DATA, Context.jcPML, CommonSlideData.class) );
		// sld.setClrMapOvr(value)
		
		return notes;	 
	}
	
	private NotesMasterPart notesMasterPart;
	
	public boolean setPartShortcut(Part part) {
		
		if (part == null ){
			return false;
		} else {
			return setPartShortcut(part, part.getRelationshipType() );
		}
		
	}	
		
	public boolean setPartShortcut(Part part, String relationshipType) {
		
		if (relationshipType==null) {
			log.warn("trying to set part shortcut against a null relationship type.");
			return false;
		}
		
		if (relationshipType.equals(Namespaces.PRESENTATIONML_NOTES_MASTER)) {
			notesMasterPart = (NotesMasterPart)part;
			return true;			
		} else {	
			return false;
		}
	}
	
	
	/**
	 * @since 8.1.3, 11.1.3
	 */
	public NotesMasterPart getNotesMasterPart() {
		
		return notesMasterPart;
	}
	
	/**
	 * @since 8.1.3, 11.1.3
	 */	
	public void addNoteTextPara(String noteText, String lang) {
		
		// eg  "en-AU"
		
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		CTTextParagraph textparagraph = dmlObjectFactory.createCTTextParagraph(); 
		    // Create object for endParaRPr
		    CTRegularTextRun regulartextrun = dmlObjectFactory.createCTRegularTextRun(); 
		    textparagraph.getEGTextRun().add( regulartextrun); 
		        // Create object for rPr
		        CTTextCharacterProperties textcharacterproperties = dmlObjectFactory.createCTTextCharacterProperties(); 
		        regulartextrun.setRPr(textcharacterproperties); 
		            textcharacterproperties.setLang(lang); 
		            textcharacterproperties.setSmtId( Long.valueOf(0) );
		        regulartextrun.setT( noteText ); 
		    // Create object for endParaRPr
		    CTTextCharacterProperties textcharacterproperties2 = dmlObjectFactory.createCTTextCharacterProperties(); 
		    textparagraph.setEndParaRPr(textcharacterproperties2); 
		        textcharacterproperties2.setLang( lang); 
		        textcharacterproperties2.setSmtId( Long.valueOf(0) );		
		
        // Now 
        Shape notesPlaceholder = (Shape)this.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().get(0);
        notesPlaceholder.getTxBody().getP().add(textparagraph);
	}
	
}
