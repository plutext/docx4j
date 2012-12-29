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

import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Sld;
import org.w3c.dom.Node;



public final class SlidePart extends JaxbPmlPart<Sld> {
	
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

	private Binder<Node> binder;
	
	
	/**
	 * Enables synchronization between XML infoset nodes and JAXB objects 
	 * representing same XML document.
	 * 
	 * An instance of this class maintains the association between XML nodes
	 * of an infoset preserving view and a JAXB representation of an XML document. 
	 * Navigation between the two views is provided by the methods 
	 * getXMLNode(Object) and getJAXBNode(Object) .
	 * 
	 * In theory, modifications can be made to either the infoset preserving view or 
	 * the JAXB representation of the document while the other view remains
	 * unmodified. The binder ought to be able to synchronize the changes made in
	 * the modified view back into the other view using the appropriate
	 * Binder update methods, #updateXML(Object, Object) or #updateJAXB(Object).
	 * 
	 * But JAXB doesn't currently work as advertised .. access to this
	 * object is offered for advanced users on an experimental basis only.
	 */
	public Binder<Node> getBinder() {
		
//		if (binder ==null) {
//			binder = jc.createBinder();			
//		}		
		return binder;
	}
	
	/**
	 * Fetch JAXB Nodes matching an XPath (for example "//w:p").
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   
	 * 
	 * @param xpathExpr
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 */	
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException {
		
		return XmlUtils.getJAXBNodesViaXPath(binder, getJaxbElement(), xpathExpr, refreshXmlFirst);
	}	

	/**
	 * Fetch JAXB Nodes matching an XPath (for example ".//w:p" - note the dot,
	 * which is necessary for this sort of relative path).
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   

	 * @param xpathExpr
	 * @param someJaxbElement
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 */
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, Object someJaxbElement, boolean refreshXmlFirst) 
		throws JAXBException {

		return XmlUtils.getJAXBNodesViaXPath(binder, someJaxbElement, xpathExpr, refreshXmlFirst);
	}	

    /**
     * Unmarshal XML data from the specified InputStream and return the 
     * resulting content tree.  Validation event location information may
     * be incomplete when using this form of the unmarshal API.
     *
     * <p>
     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
     * 
     * @param is the InputStream to unmarshal XML data from
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
	@Override
    public Sld unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
			
			log.info("For SlidePart, unmarshall via binder");
			// InputStream to Document
			javax.xml.parsers.DocumentBuilderFactory dbf 
				= DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(is);

			// 
			binder = jc.createBinder();
			
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			binder.setEventHandler(eventHandler);
			
			try {
				jaxbElement =  (Sld) binder.unmarshal( doc );
			} catch (UnmarshalException ue) {
				log.info("encountered unexpected content; pre-processing");
				/* Always try our preprocessor, since if what is first encountered is
				 * eg:
				 * 
			          <w14:glow w14:rad="101600"> ...
				 *
				 * the error would be:
				 *  
				 *    unexpected element (uri:"http://schemas.microsoft.com/office/word/2010/wordml", local:"glow")
				 *
				 * but there could well be mc:AlternateContent somewhere 
				 * further down in the document.
				 */

				// mimic docx4j 2.7.0 and earlier behaviour; this will 
				// drop w14:glow etc; the preprocessor doesn't need to 
				// do that
				eventHandler.setContinue(true);
				
				// There is no JAXBResult(binder),
				// so use a 
				DOMResult result = new DOMResult();
				
				Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
				XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
				
				doc = (org.w3c.dom.Document)result.getNode();
				
				try {
					jaxbElement =  (Sld) binder.unmarshal( doc );
				} catch (ClassCastException cce) {
 
					log.warn("Binder not available for this slide");
					Unmarshaller u = jc.createUnmarshaller();
					jaxbElement = (Sld) u.unmarshal( doc );					
					/* 
					 * Work around for issue with JAXB binder, in Java 1.6 
					 * encountered with /src/test/resources/jaxb-binder-issue.docx 
					 * See http://old.nabble.com/BinderImpl.associativeUnmarshal-ClassCastException-casting-to-JAXBElement-td32456585.html
					 * and  http://java.net/jira/browse/JAXB-874
					 * 
					 * java.lang.ClassCastException: org.docx4j.wml.PPr cannot be cast to javax.xml.bind.JAXBElement
						at com.sun.xml.internal.bind.v2.runtime.ElementBeanInfoImpl$IntercepterLoader.intercept(Unknown Source)
						at com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext.endElement(Unknown Source)
						at com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor.endElement(Unknown Source)
						at com.sun.xml.internal.bind.v2.runtime.unmarshaller.SAXConnector.endElement(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.visit(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.scan(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.scan(Unknown Source)
						at com.sun.xml.internal.bind.unmarshaller.DOMScanner.scan(Unknown Source)
						at com.sun.xml.internal.bind.v2.runtime.BinderImpl.associativeUnmarshal(Unknown Source)
						at com.sun.xml.internal.bind.v2.runtime.BinderImpl.unmarshal(Unknown Source)
						at org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.unmarshal(MainDocumentPart.java:321)
					 */
				}
				
			}
			
			return jaxbElement;
			
		} catch (Exception e ) {
			e.printStackTrace();
			return null;
		}
    }

    public Sld unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {

			binder = jc.createBinder();
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			binder.setEventHandler(eventHandler);
			
			try {
				jaxbElement =  (Sld) binder.unmarshal( el );
			} catch (UnmarshalException ue) {
				log.info("encountered unexpected content; pre-processing");
				try {
					org.w3c.dom.Document doc;
					if (el instanceof org.w3c.dom.Document) {
						doc = (org.w3c.dom.Document) el;
					} else {
						// Hope for the best. Dodgy though; what if this is
						// being used on something deep in the tree?
						// TODO: revisit
						doc = el.getOwnerDocument();
					}
					eventHandler.setContinue(true);
					DOMResult result = new DOMResult();
					Templates mcPreprocessorXslt = JaxbValidationEventHandler
							.getMcPreprocessor();
					XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
					doc = (org.w3c.dom.Document) result.getNode();
					jaxbElement = (Sld) binder
							.unmarshal(doc);
				} catch (Exception e) {
					throw new JAXBException("Preprocessing exception", e);
				}
			}
			return jaxbElement;
			
		} catch (JAXBException e) {
			log.error(e);
			throw e;
		}
	}	
    
    NotesSlidePart notes;
    SlideLayoutPart layout;
    
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
			notes = (NotesSlidePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PRESENTATIONML_SLIDE_LAYOUT)) {
			layout = (SlideLayoutPart)part;
			return true;					
		} else {	
			return false;
		}
	}
	
	public NotesSlidePart getNotesSlidePart() {
		return notes;
	}
	public SlideLayoutPart getSlideLayoutPart() {
		return layout;
	}
	
    
}
