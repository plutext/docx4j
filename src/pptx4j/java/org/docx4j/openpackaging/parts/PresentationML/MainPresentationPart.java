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

import org.apache.commons.lang.NotImplementedException;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.SlideSizesWellKnown;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Presentation;



public final class MainPresentationPart extends JaxbPmlPart<Presentation> {
	
	public MainPresentationPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public MainPresentationPart() throws InvalidFormatException {
		super(new PartName("/ppt/presentation.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_MAIN));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_MAIN);
		
	}
	
	private final static String DEFAULT_SLIDE_SIZE = "<p:sldSz xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"" +
			" cx=\"9144000\" cy=\"6858000\" type=\"screen4x3\"/>";
	
	
	private final static String DEFAULT_NOTES_SIZE = "<p:notesSz xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" " +
			"cx=\"6858000\" cy=\"9144000\"/>";

	/**
	 * @since 2.7
	 */
	public static Presentation.SldSz createSlideSize(SlideSizesWellKnown sz,
			boolean landscape) {

		Presentation.SldSz sldSz = Context.getpmlObjectFactory()
				.createPresentationSldSz();

		if (sz.equals(SlideSizesWellKnown.LETTER)) {
			sldSz.setType("letter");
			if (landscape) {
				sldSz.setCx(9144000);
				sldSz.setCy(6858000);
			} else {
				sldSz.setCx(6858000);
				sldSz.setCy(9144000);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.A3)) {
			sldSz.setType("A3");
			if (landscape) {
				sldSz.setCx(12801600);
				sldSz.setCy(9601200);
			} else {
				sldSz.setCx(9601200);
				sldSz.setCy(12801600);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.A4)) {
			sldSz.setType("A4");
			if (landscape) {
				sldSz.setCx(9906000);
				sldSz.setCy(6858000);
			} else {
				sldSz.setCx(6858000);
				sldSz.setCy(9906000);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.SCREEN4x3)) {
			sldSz.setType("screen4x3");
			if (landscape) {
				sldSz.setCx(9144000);
				sldSz.setCy(6858000);
			} else {
				sldSz.setCx(6858000);
				sldSz.setCy(9144000);

			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.SCREEN16x9)) {
			sldSz.setType("screen16x9");
			if (landscape) {
				sldSz.setCx(9144000);
				sldSz.setCy(5143500);
			} else {
				sldSz.setCx(5143500);
				sldSz.setCy(9144000);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.SCREEN16x10)) {
			sldSz.setType("screen16x10");
			if (landscape) {
				sldSz.setCx(9144000);
				sldSz.setCy(5715000);
			} else {
				sldSz.setCx(5715000);
				sldSz.setCy(9144000);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.LEDGER)) {
			sldSz.setType("ledger");
			if (landscape) {
				sldSz.setCx(12179300);
				sldSz.setCy(9134475);
			} else {
				sldSz.setCx(9134475);
				sldSz.setCy(12179300);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.B4ISO)) {
			sldSz.setType("B4ISO");
			if (landscape) {
				sldSz.setCx(10826750);
				sldSz.setCy(8120063);
			} else {
				sldSz.setCx(8120063);
				sldSz.setCy(10826750);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.B5ISO)) {
			sldSz.setType("B5ISO");
			if (landscape) {
				sldSz.setCx(7169150);
				sldSz.setCy(5376863);
			} else {
				sldSz.setCx(5376863);
				sldSz.setCy(7169150);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.MM35)) {
			sldSz.setType("35mm");
			if (landscape) {
				sldSz.setCx(10287000);
				sldSz.setCy(6858000);
			} else {
				sldSz.setCx(6858000);
				sldSz.setCy(10287000);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.OVERHEAD)) {
			sldSz.setType("overhead");
			if (landscape) {
				sldSz.setCx(9144000);
				sldSz.setCy(6858000);
			} else {
				sldSz.setCx(6858000);
				sldSz.setCy(9144000);
			}
			return sldSz;
		}

		if (sz.equals(SlideSizesWellKnown.BANNER)) {
			sldSz.setType("banner");
			if (landscape) {
				sldSz.setCx(7315200);
				sldSz.setCy(914400);
			} else {
				sldSz.setCx(914400);
				sldSz.setCy(7315200);
			}
			return sldSz;
		}

		throw new NotImplementedException("No support for slide size "
				+ sz.value());
	}

	public static Presentation createJaxbPresentationElement() throws JAXBException {
		
		return createJaxbPresentationElement(null, true);
	}
	
	/**
	 * @since 2.7
	 */	
	public static Presentation createJaxbPresentationElement(SlideSizesWellKnown sz, boolean landscape) throws JAXBException {

		ObjectFactory factory = Context.getpmlObjectFactory(); 
		Presentation presentation = factory.createPresentation();

		// Create empty lists
		Presentation.SldMasterIdLst masterIds = factory.createPresentationSldMasterIdLst();
		Presentation.SldIdLst slideIds = factory.createPresentationSldIdLst();		
		presentation.setSldMasterIdLst(masterIds);
		presentation.setSldIdLst(slideIds);
		
		presentation.setNotesSz( 
				(CTPositiveSize2D)XmlUtils.unmarshalString(DEFAULT_NOTES_SIZE, Context.jcPML, CTPositiveSize2D.class) );
		
		if (sz==null) {
			presentation.setSldSz(
					(Presentation.SldSz)XmlUtils.unmarshalString(DEFAULT_SLIDE_SIZE, Context.jcPML, Presentation.SldSz.class));
		} else {
			presentation.setSldSz(	MainPresentationPart.createSlideSize(sz, landscape) );		
		}
		return presentation;
	}

	
	/**
	 * Add a slide to this presentation.
	 * 
	 * @param slidePart
	 * @return
	 * @throws InvalidFormatException
	 */
	public Presentation.SldIdLst.SldId addSlideIdListEntry(SlidePart slidePart) throws InvalidFormatException {
		return addSlideIdListEntry(slidePart, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS);
	}

	
	/**
	 * @since 2.8.1
	 */		
	public Presentation.SldIdLst.SldId addSlideIdListEntry(SlidePart slidePart, AddPartBehaviour mode) 
		throws InvalidFormatException {	

		Relationship rel = this.addTargetPart(slidePart, mode);
		
		Presentation.SldIdLst.SldId entry = Context.getpmlObjectFactory().createPresentationSldIdLstSldId();
		
		entry.setId( this.getSlideId() );
		entry.setRid(rel.getId());
		
		this.getJaxbElement().getSldIdLst().getSldId().add(entry);
		
		return entry;
		
	}
	
	public Presentation.SldMasterIdLst.SldMasterId addSlideMasterIdListEntry(SlideMasterPart slideMasterPart) 
		throws InvalidFormatException {	

		Relationship rel = this.addTargetPart(slideMasterPart);
		
		Presentation.SldMasterIdLst.SldMasterId entry = Context.getpmlObjectFactory().createPresentationSldMasterIdLstSldMasterId();
		
		entry.setId( new Long(this.getSlideLayoutOrMasterId()) );
		entry.setRid(rel.getId());

		this.getJaxbElement().getSldMasterIdLst().getSldMasterId().add(entry);
		
		return entry;
			
		}
	
}
