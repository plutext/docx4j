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
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Sld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class SlidePart extends JaxbPmlPart<Sld> {
	
	protected static Logger log = LoggerFactory.getLogger(SlidePart.class);	
	
	public SlidePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public SlidePart() throws InvalidFormatException {
		super(new PartName("/ppt/slides/slide1.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_SLIDE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_SLIDE);
		
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.JaxbXmlPart#setMceIgnorable(org.docx4j.jaxb.McIgnorableNamespaceDeclarator)
	 * 
	 * @since 3.3.0
	 */
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {
		
		/* Ensure we declare the namespace.  Otherwise Powerpoint 2010 treats the pptx as corrupt.
		 * 2013 and Mac version might repair it, at leat..
		 * 
            <mc:AlternateContent xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006">
              <mc:Choice xmlns:v="urn:schemas-microsoft-com:vml" Requires="v">
		 */
		
		
		// Unlike MainDocumentPart, there is no this.getJaxbElement().getIgnorable()
		
		namespacePrefixMapper.setMcIgnorable("v");
	}		
	
	
	public static Sld createSld() throws JAXBException {

		ObjectFactory factory = Context.getpmlObjectFactory(); 
		Sld sld = factory.createSld();
		sld.setCSld( 
				(CommonSlideData)XmlUtils.unmarshalString(COMMON_SLIDE_DATA, Context.jcPML, CommonSlideData.class) );
		// sld.setClrMapOvr(value)
		
		return sld;		
	}
	
	private ResolvedLayout resolvedLayout;
	public ResolvedLayout getResolvedLayout() {
		if (resolvedLayout!=null) {
			return resolvedLayout;		
		}
		
		resolvedLayout = ResolvedLayout.resolveSlideLayout(this);
		return resolvedLayout;
	}	


//    /**
//	 * Marshal the content tree rooted at <tt>jaxbElement</tt> into an output
//	 * stream
//	 * 
//	 * @param os
//	 *            XML will be added to this stream.
//	 * @param namespacePrefixMapper
//	 *            namespacePrefixMapper
//	 * 
//	 * @throws JAXBException
//	 *             If any unexpected problem occurs during the marshalling.
//	 */
//	@Override
//    public void marshal(java.io.OutputStream os, Object namespacePrefixMapper) throws JAXBException {
//
//		// Add xmlns:v="urn:schemas-microsoft-com:vml" eg in
//        // <mc:AlternateContent xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006">
//        // <mc:Choice xmlns:v="urn:schemas-microsoft-com:vml" Requires="v">		
//		// How?  Could marshall to a DOM doc, but there is no way to force the xmlns to be included
//		// where it is not required.
//		// So do string manipulation
//    	
//		String xmlString = XmlUtils.marshaltoString( getJaxbElement(), false, true, jc ); 
//			// include the XML declaration; it'll be UTF-8
//		int pos = xmlString.indexOf(":sld ");
//		xmlString = xmlString.substring(0, pos + 5 ) + "xmlns:v=\"urn:schemas-microsoft-com:vml\" " 
//						+ xmlString.substring(pos + 5 );
//		
//		try {
//			IOUtils.write(xmlString, os, "UTF-8"); // be sure to write UTF-8 irrespective of default encoding
//			/* FIX confirmed by running a presentation containing eg mög
//			 * through RoundTripTest, 
//			 * with run configuration setting -Dfile.encoding=ISO-8859-1,
//			 * verified Powerpoint (2010) can open it.
//			 */
//		} catch (IOException e) {
//			throw new JAXBException(e.getMessage(), e);
//		}
//			
//	}	
//	
//    /**
//     * Unmarshal XML data from the specified InputStream and return the 
//     * resulting content tree.  Validation event location information may
//     * be incomplete when using this form of the unmarshal API.
//     *
//     * <p>
//     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
//     * 
//     * @param is the InputStream to unmarshal XML data from
//     * @return the newly created root object of the java content tree 
//     *
//     * @throws JAXBException 
//     *     If any unexpected errors occur while unmarshalling
//     */
//	@Override
//    public Sld unmarshal( java.io.InputStream is ) throws JAXBException {
//    	
//		try {
//			
//			// InputStream to Document
//			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(is);
//
//			
//			/* Note: 2013 04 25
//			 * 
//			 * If a slide contains:
//			 * 
//		          <a:graphicData uri="http://schemas.openxmlformats.org/presentationml/2006/ole">
//		            <mc:AlternateContent xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006">
//		              <mc:Choice xmlns:v="urn:schemas-microsoft-com:vml" Requires="v">
//		                <p:oleObj spid="_x0000_s574471" name="Slide" 
//		                          r:id="rId4" imgW="4657680" imgH="3492360" progId="PowerPoint.Slide.8">
//		                  <p:embed/>
//		                </p:oleObj>
//		              </mc:Choice>			 
//		     * 
//		     * this alternate content wouldn't get stripped by:
//		     * 
//		     * 		(Sld) binder.unmarshal( doc );
//		     * 
//		     * because the content model for a:graphicData is:
//		     * 
//		     *     <xsd:sequence>
//				      <xsd:any minOccurs="0" maxOccurs="unbounded" processContents="strict"/>
//				    </xsd:sequence>
//		     * 
//		     * The problem with this is that JAXB marshalls it as:
//		     * 
//		          <a:graphicData uri="http://schemas.openxmlformats.org/presentationml/2006/ole">
//		            <mc:AlternateContent>
//		              <mc:Choice Requires="v">
//		                <p:oleObj xmlns:v="urn:schemas-microsoft-com:vml" imgH="3492360" imgW="4657680" name="Slide" progId="PowerPoint.Slide.8" r:id="rId4" spid="_x0000_s574471">
//		                  <p:embed/>
//		                </p:oleObj>
//		              </mc:Choice>
//		     *
//		     * (note the namespace declaration is legitimately missing from the mc:Choice element;
//		     *  but this causes Powerpoint 2010 to say the file needs to be repaired!!!).
//		     *  
//		     *  I don't think there's a way to cajole JAXB to add a namespace where it is not necessary.
//		     *  After marshalling, we could post process to add it back in (not with XSLT, since
//		     *  that'll do its own thing with namespaces, but we could with regex).
//		     *  
//		     *  But it is better, I think to always get rid of the alternate content entirely.
//			 */
//			
//			log.info("proactively pre-processing to remove any AlternateContent");
//			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
//			eventHandler.setContinue(true);
//			
//			// There is no JAXBResult(binder),
//			// so use a 
//			DOMResult result = new DOMResult();
//			
//			Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
//			XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
//			
//			doc = (org.w3c.dom.Document)result.getNode();
//			
//			try {
//				binder = jc.createBinder();
////				eventHandler.setContinue(false); // review 
//				binder.setEventHandler(eventHandler);
//				jaxbElement =  (Sld) binder.unmarshal( doc );
//			} catch (ClassCastException cce) {
// 
//				log.warn("Binder not available for this slide");
//				Unmarshaller u = jc.createUnmarshaller();
//				jaxbElement = (Sld) u.unmarshal( doc );					
//				/* 
//				 * Work around for issue with JAXB binder, in Java 1.6 
//				 * encountered with /src/test/resources/jaxb-binder-issue.docx 
//				 * See http://old.nabble.com/BinderImpl.associativeUnmarshal-ClassCastException-casting-to-JAXBElement-td32456585.html
//				 * and  http://java.net/jira/browse/JAXB-874
//				 * 
//				 * java.lang.ClassCastException: org.docx4j.wml.PPr cannot be cast to javax.xml.bind.JAXBElement
//					at com.sun.xml.internal.bind.v2.runtime.ElementBeanInfoImpl$IntercepterLoader.intercept(Unknown Source)
//					at com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext.endElement(Unknown Source)
//					at com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor.endElement(Unknown Source)
//					at com.sun.xml.internal.bind.v2.runtime.unmarshaller.SAXConnector.endElement(Unknown Source)
//					at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
//					at com.sun.xml.internal.bind.unmarshaller.DOMScanner.scan(Unknown Source)
//					at com.sun.xml.internal.bind.v2.runtime.BinderImpl.associativeUnmarshal(Unknown Source)
//					at com.sun.xml.internal.bind.v2.runtime.BinderImpl.unmarshal(Unknown Source)
//					at org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.unmarshal(MainDocumentPart.java:321)
//				 */
//			}
//			
//			return jaxbElement;
//			
//		} catch (Exception e ) {
//			e.printStackTrace();
//			return null;
//		}
//    }
//
//    public Sld unmarshal(org.w3c.dom.Element el) throws JAXBException {
//
//    	// Note comments above about AlternateContent.  
//    	// unmarshalling here from an Element doesn't implement that fix, so beware.
//    	
//		try {
//
//			binder = jc.createBinder();
//			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
//			eventHandler.setContinue(false);
//			binder.setEventHandler(eventHandler);
//			
//			try {
//				jaxbElement =  (Sld) binder.unmarshal( el );
//			} catch (UnmarshalException ue) {
//				log.info("encountered unexpected content; pre-processing");
//				try {
//					org.w3c.dom.Document doc;
//					if (el instanceof org.w3c.dom.Document) {
//						doc = (org.w3c.dom.Document) el;
//					} else {
//						// Hope for the best. Dodgy though; what if this is
//						// being used on something deep in the tree?
//						// TODO: revisit
//						doc = el.getOwnerDocument();
//					}
//					eventHandler.setContinue(true);
//					DOMResult result = new DOMResult();
//					Templates mcPreprocessorXslt = JaxbValidationEventHandler
//							.getMcPreprocessor();
//					XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
//					doc = (org.w3c.dom.Document) result.getNode();
//					jaxbElement = (Sld) binder
//							.unmarshal(doc);
//				} catch (Exception e) {
//					throw new JAXBException("Preprocessing exception", e);
//				}
//			}
//			return jaxbElement;
//			
//		} catch (JAXBException e) {
//			throw e;
//		}
//	}	
    
    NotesSlidePart notesSlidePart;
    SlideLayoutPart layout;
    CommentsPart comments;
    
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
		
		if (relationshipType.equals(Namespaces.PRESENTATIONML_NOTES_SLIDE)) {
			notesSlidePart = (NotesSlidePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PRESENTATIONML_SLIDE_LAYOUT)) {
			layout = (SlideLayoutPart)part;
			return true;					
		} else if (relationshipType.equals(Namespaces.PRESENTATIONML_COMMENTS)) {
			comments = (CommentsPart)part;
			return true;					
		} else {	
			return false;
		}
	}
	
	public NotesSlidePart getNotesSlidePart() {
		return notesSlidePart;
	}
	public SlideLayoutPart getSlideLayoutPart() {
		return layout;
	}
	
	/**
	 * @since 3.2.0
	 */
	public CommentsPart getCommentsPart() {
		return comments;
	}
	
	/**
	 * @since 8.1.3, 11.1.3
	 */
	public NotesSlidePart createNotesSlidePart(NotesMasterPart nmp) throws InvalidFormatException, JAXBException {
		
		if (notesSlidePart==null) {

			notesSlidePart= new NotesSlidePart();
			Notes notes =  NotesSlidePart.createNotes();
			notesSlidePart.setJaxbElement(notes);
			
			// .. connect it to the slide
			this.addTargetPart(notesSlidePart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
			// .. it also has a rel to the slide
			notesSlidePart.addTargetPart(this);
		}
		
		if (nmp!=null && notesSlidePart.getNotesMasterPart()==null) {
			// .. and the note master
			notesSlidePart.addTargetPart(nmp);
		}
		return notesSlidePart;
	}	
    
}
