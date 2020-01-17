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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.NotImplementedException;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.pptx4j.Pptx4jException;
import org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main.CTSection;
import org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main.CTSectionList;
import org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main.CTSectionSlideIdListEntry;
import org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main.CTExtendedGuideList;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.SlideSizesWellKnown;
import org.pptx4j.pml.CTExtension;
import org.pptx4j.pml.CTExtensionList;
import org.pptx4j.pml.CTNotesMasterIdList;
import org.pptx4j.pml.CTNotesMasterIdListEntry;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Presentation;
import org.pptx4j.pml.Presentation.SldIdLst.SldId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public final class MainPresentationPart extends JaxbPmlPart<Presentation> {
	
	protected static Logger log = LoggerFactory.getLogger(MainPresentationPart.class);
	
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
	

	private CommentAuthorsPart commentAuthorsPart;
	
	private ThemePart themePart;
	
	private NotesMasterPart notesMasterPart; 
	
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
		
		if (relationshipType.equals(Namespaces.PRESENTATIONML_COMMENT_AUTHORS)) {
			commentAuthorsPart = (CommentAuthorsPart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.THEME)) {
			themePart = (ThemePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PRESENTATIONML_NOTES_MASTER)) {
			notesMasterPart = (NotesMasterPart)part;
			return true;			
		} else {	
			return false;
		}
	}

	
	/**
	 * @since 3.2.0
	 */
	public CommentAuthorsPart getCommentAuthorsPart() {
		return commentAuthorsPart;
	}

	/**
	 * @since 8.1.3
	 */
	public ThemePart getThemePart() {
		return themePart;
	}
	
	/**
	 * getNotesMasterPart creating if createIfAbsent flag allows
	 * 
	 * @throws Pptx4jException 
	 * @since 8.1.3
	 */
	public NotesMasterPart getNotesMasterPart(boolean createIfAbsent) throws Pptx4jException {
		
		if (notesMasterPart==null && createIfAbsent) {
					
			Relationship ppRelNmp = null;
			ThemePart themePart2 = null;
			try {
				notesMasterPart = new NotesMasterPart();
				notesMasterPart.addDefaultContent();
				// .. connect it to /ppt/presentation.xml
				ppRelNmp = this.addTargetPart(notesMasterPart);
				
				/*
				 *  <p:notesMasterIdLst>
		                <p:notesMasterId r:id="rId3"/>
		            </p:notesMasterIdLst>
				 */
				this.getJaxbElement().setNotesMasterIdLst(createNotesMasterIdListPlusEntry(ppRelNmp.getId()));
				
				// .. NotesMasterPart has a rel to a theme 
				// moreover, it needs to be an additional theme part (!), but it is ok for it to have the same contents
				// so clone the existing theme
				themePart2 = new ThemePart(new PartName("/ppt/theme/theme2.xml"));
				
			} catch (Exception e) {
				// Shouldn't happen
				log.error(e.getMessage(), e);
				return null;
			}
			
			if (getThemePart()==null) {
				throw new Pptx4jException("Missing theme part");
			}
			try {
				themePart2.setContents(XmlUtils.deepCopy(getThemePart().getContents(), Context.jcPML));
				notesMasterPart.addTargetPart(themePart2, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
			} catch (Docx4JException e) {
				throw new Pptx4jException(e.getMessage(), e);
			}
			
		}
		
		return notesMasterPart;
		
	}
	
	private CTNotesMasterIdList createNotesMasterIdListPlusEntry(String relId) {

		org.pptx4j.pml.ObjectFactory pmlObjectFactory = new org.pptx4j.pml.ObjectFactory();

		CTNotesMasterIdList notesmasteridlist = pmlObjectFactory.createCTNotesMasterIdList(); 
		    // Create object for notesMasterId
		    CTNotesMasterIdListEntry notesmasteridlistentry = pmlObjectFactory.createCTNotesMasterIdListEntry(); 
		    notesmasteridlist.setNotesMasterId(notesmasteridlistentry); 
		        notesmasteridlistentry.setId( relId); 

		return notesmasteridlist;
		}
	
	
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
	@Deprecated 
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
	
	/**
	 * Append the slide at the end of the presentation.
	 * @param slidePart
	 * @throws Pptx4jException
	 * @since 3.0
	 */
	public boolean addSlide(SlidePart slidePart) throws Pptx4jException {
		
		/* Powerpoint 2010 can't open a pptx in which a slide appears
		 * several times, for example:
		 * 
			  <p:sldIdLst>
			    <p:sldId id="256" r:id="rId2"/>
			    <p:sldId id="257" r:id="rId3"/>
			    <p:sldId id="258" r:id="rId2"/> <----- can't use rId2 again
			  </p:sldIdLst>		
		 * 
		 * Nor can 2 distinct relIds target the same part.
		 */
		
		try {
			Relationship rel = this.addTargetPart(slidePart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
			return this.getJaxbElement().getSldIdLst().getSldId().add(createSlideIdListEntry(rel));
		} catch (InvalidFormatException e) {
			throw new Pptx4jException(e.getMessage(), e);
		}
	}

	/**
	 * Inserts the slide at the specified position in the presentation. 
	 * Shifts the element currently at that position (if any) and any subsequent elements to the 
	 * right (adds one to their indices).
	 * 
	 * @param index
	 * @param slidePart
	 * @throws Pptx4jException
	 * @since 3.0
	 */
	public void addSlide(int index, SlidePart slidePart) throws Pptx4jException {

		List<SldId> sldIds = this.getJaxbElement().getSldIdLst().getSldId();
		
		int zeroBasedCount = sldIds.size(); 

		if (index< 0 || index>zeroBasedCount) {
			throw new Pptx4jException("Can't add slide at index " + index + ".  (There are " + sldIds.size() + " slides) ");			
		}
		
		try {
			Relationship rel = this.addTargetPart(slidePart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
			sldIds.add(index, createSlideIdListEntry(rel));
		} catch (InvalidFormatException e) {
			throw new Pptx4jException(e.getMessage(), e);
		}
	}
	
	private Presentation.SldIdLst.SldId createSlideIdListEntry(Relationship rel) throws InvalidFormatException {	
			
		Presentation.SldIdLst.SldId entry = Context.getpmlObjectFactory().createPresentationSldIdLstSldId();
		
		entry.setId( this.getSlideId() );
		entry.setRid(rel.getId());
		
		return entry;
	}
	
	/**
	 * @param index
	 * @throws Pptx4jException 
	 * @since 3.0
	 */
	public void removeSlide(int index) throws Pptx4jException {
		
		List<SldId> sldIds = this.getJaxbElement().getSldIdLst().getSldId();
		
		int zeroBasedCount = sldIds.size() -1; 

		if (index< 0 || index>zeroBasedCount) {
			throw new Pptx4jException("No slide at index " + index + ".  (There are " + sldIds.size() + " slides) ");			
		}
		
		Presentation.SldIdLst.SldId entry = this.getJaxbElement().getSldIdLst().getSldId().remove(index);
		
		Relationship rel = this.getRelationshipsPart().getRelationshipByID(entry.getRid());
		
		Part part = this.getRelationshipsPart().getPart(rel);
		
		this.getPackage().getParts().remove(part.getPartName());
		this.getRelationshipsPart().removeRelationship(rel);
	}
	
	/**
	 * @param rel
	 * @throws Pptx4jException 
	 * @since 3.0
	 */
	public void removeSlide(Relationship rel) throws Pptx4jException {
		
		if (rel==null) throw new Pptx4jException("Null relationship.");
		
		int index = -1;
		int i=0;
		for (Presentation.SldIdLst.SldId entry : this.getJaxbElement().getSldIdLst().getSldId()) {
			
			if (entry.getRid().equals(rel.getId())) {
				index = i;
				break;
			}
			i++;
		}
		
		if (index>-1) {
			removeSlide(index);
		} else {
			throw new Pptx4jException("No slide is the target of that relationship.");
		}
	}
	
	/**
	 * @param index
	 * @throws Pptx4jException 
	 * @since 3.0.1
	 */
	public SlidePart getSlide(int index) throws Pptx4jException {

		ensureContent();
		ensureSldIdLst();
		
		List<SldId> sldIds = this.getJaxbElement().getSldIdLst().getSldId();
		
		int zeroBasedCount = sldIds.size() -1; 

		if (index< 0 || index>zeroBasedCount) {
			throw new Pptx4jException("No slide at index " + index + ".  (There are " + sldIds.size() + " slides) ");			
		}

		try {
			Presentation.SldIdLst.SldId entry = this.getJaxbElement().getSldIdLst().getSldId().get(index);
			return (SlidePart)this.getRelationshipsPart().getPart(entry.getRid());
		} catch (Exception e) {
			throw new Pptx4jException("Slide " + index + " not found", e);
		}
		
	}

	/**
	 * @throws Pptx4jException 
	 * @since 3.4
	 */
	public List<SlidePart> getSlideParts() throws Pptx4jException {

		ensureContent();
		ensureSldIdLst();

		List<SlidePart> slideParts = new ArrayList<SlidePart>();

		for (Presentation.SldIdLst.SldId entry : this.getJaxbElement().getSldIdLst().getSldId()) {
			try {
				slideParts.add( (SlidePart)this.getRelationshipsPart().getPart(entry.getRid()));
			} catch (Exception e) {
				throw new Pptx4jException("Slide " + entry.getRid() + " not found", e);
			}
		}
		
		return slideParts;
		
	}
	
	private void ensureContent() throws Pptx4jException {
		try {
			if (this.getContents()==null ) {
				throw new Pptx4jException("MainPresentationPart has no content");
			}
			
		} catch (Docx4JException e) {
			throw new Pptx4jException(e.getMessage(), e);
		}		
	}

	private void ensureSldIdLst() throws Pptx4jException {
		try {
			if (this.getContents().getSldIdLst()==null) {
				throw new Pptx4jException("SldIdLst missing from MainPresentationPart");
			}
			
		} catch (Docx4JException e) {
			throw new Pptx4jException(e.getMessage(), e);
		}		
	}
	
	/**
	 * @param index
	 * @throws Pptx4jException 
	 * @since 3.2.0
	 */
	public int getSlideCount() throws Pptx4jException {

		ensureContent();
		ensureSldIdLst();
		
		List<SldId> sldIds = this.getJaxbElement().getSldIdLst().getSldId();
		
		return sldIds.size(); 

	}

	/**
	 * get p:ext containing specified class
	 * @param extLst
	 * @param containingClass
	 * @return
	 * @since 8.1.0
	 */
	public CTExtension getExtContaining(Class containingClass) {

        CTExtensionList extLst = getJaxbElement().getExtLst();
        if (extLst==null) return null;
		
		for (CTExtension ext : extLst.getExt()) {
			
			if (ext.getAny()!=null) {
				
				Object o = ext.getAny();
//				System.out.println(o.getClass().getName());
				if (o instanceof JAXBElement) {
					Object o2 = XmlUtils.unwrap(o);
//					System.out.println(o2.getClass().getName());
					if (o2.getClass().getName().equals(containingClass.getName())) {
						return ext;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * remove the p:ext containing specified class
	 * @param containingClass
	 * @return
	 * @since 8.1.0
	 */
	public boolean removeExt(Class containingClass) {
		
    	CTExtension ext = getExtContaining(containingClass);
    	if (ext!=null) {
            return getJaxbElement().getExtLst().getExt().remove(ext);
    	}
        return false;
	}
	
    private static final String EXT_URI_FOR_SECTION_LST = "{521415D9-36F7-43E2-AB2F-B90AF26B5E84}"; 	
	
	/**
	 * @since 8.1.0
	 */
	public CTSectionList getSectionList(boolean createIfAbsent) {

		CTExtension ext = getExtContaining(CTSectionList.class);
		
		if (ext==null) {
			
			if (createIfAbsent) {
				
		        CTExtensionList extLst = getExtensionList(true);
				
				ext = new CTExtension();
				ext.setUri(EXT_URI_FOR_SECTION_LST);// Powerpoint 2016 doesn't show it if you use something else!
				
				extLst.getExt().add(ext);
				
			} else {			
				return null;
			}
		}
		if (ext.getAny()!=null) {
			
			Object o = ext.getAny();
			return (CTSectionList)XmlUtils.unwrap(o);
		}
		
		if (createIfAbsent) {
			
			CTSectionList ctSectionList = new CTSectionList();
			ext.setAny(ctSectionList);
			return ctSectionList;
		}
		return null;
		
	}
	
	/**
	 * @since 8.1.0
	 */
	public CTExtendedGuideList getExtendedGuideList(boolean createIfAbsent) {

		CTExtension ext = getExtContaining(CTExtendedGuideList.class);
		
		if (ext==null) {
			
			if (createIfAbsent) {
				
		        CTExtensionList extLst = getExtensionList(true);
				
				ext = new CTExtension();
				String newUUID = "{" + UUID.randomUUID().toString().toUpperCase() + "}";
				ext.setUri(newUUID);
				
				extLst.getExt().add(ext);
				
			} else {			
				return null;
			}
		}
		if (ext.getAny()!=null) {
			
			Object o = ext.getAny();
			return (CTExtendedGuideList)XmlUtils.unwrap(o);
		}
		
		if (createIfAbsent) {
			
			CTExtendedGuideList ctExtendedGuideList = new CTExtendedGuideList();
			ext.setAny(ctExtendedGuideList);
			return ctExtendedGuideList;
		}
		return null;
		
		
	}

	/**
	 * @since 8.1.0
	 */
	public CTExtensionList getExtensionList(boolean createIfAbsent) {
		
        CTExtensionList extLst = getJaxbElement().getExtLst();
        if (extLst==null) {
			if (createIfAbsent) {
	        	extLst = new CTExtensionList(); 
	        	getJaxbElement().setExtLst(extLst);
	        	return extLst;
			} else {
				return null;
			}
        }
			
		return extLst;
	}
	
	/**
	 * @since 8.1.0
	 */
	public CTSection getSection(SlidePart sp) {
		
		Relationship r = sp.getSourceRelationships().get(0);
		SldId sldId = getSldIdByRelId(r.getId());

		// Now use the id to find the section
		CTExtension ext = getExtContaining(CTSectionList.class);
		if (ext==null) {
			log.debug("Couldn't find ext containing sectionLst");
			return null;
		}
		if (ext.getAny()!=null) {
			
			Object o = ext.getAny();
			CTSectionList sectLst = (CTSectionList)XmlUtils.unwrap(o);
			
			return getSection(sectLst, sldId.getId());
		}
		return null;
		
	}

	/**
	 * @since 8.1.0
	 */
	private CTSection getSection(CTSectionList sectLst, long sldId) {
		
		for (CTSection p14Section : sectLst.getSection()) {
			if (p14Section.getSldIdLst()==null) continue;
			for (CTSectionSlideIdListEntry p14SldId : p14Section.getSldIdLst().getSldId()) {
				if(p14SldId.getId()== sldId) return p14Section;
			}
		}
		return null;
	}
	
	/**
	 * Get a SdlId from SldIdLst by its relId
	 * @param relId
	 * @return
	 * @since 8.1.0
	 */
	public SldId getSldIdByRelId(String relId) {

		for( SldId sldId : this.getJaxbElement().getSldIdLst().getSldId() ) {
			if (sldId.getRid().equals(relId)) return sldId;
		}
		return null;
	}
}
