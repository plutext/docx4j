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

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.JAXBAssociation;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.pptx4j.jaxb.Context;
import org.w3c.dom.Node;



public abstract class JaxbPmlPart<E> extends JaxbXmlPart<E> {
	
	protected static Logger log = Logger.getLogger(JaxbPmlPart.class);
	
	
	public final static String COMMON_SLIDE_DATA = 
	    "<p:cSld  xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
		    + "<p:spTree>"
		        + "<p:nvGrpSpPr>"
		            + "<p:cNvPr id=\"1\" name=\"\"/>"
		            + "<p:cNvGrpSpPr/>"
		            + "<p:nvPr/>"
		        + "</p:nvGrpSpPr>"
		        + "<p:grpSpPr>"
		            + "<a:xfrm>"
		                + "<a:off x=\"0\" y=\"0\"/>"
		                + "<a:ext cx=\"0\" cy=\"0\"/>"
		                + "<a:chOff x=\"0\" y=\"0\"/>"
		                + "<a:chExt cx=\"0\" cy=\"0\"/>"
		            + "</a:xfrm>"
		        + "</p:grpSpPr>"
		    + "</p:spTree>"
		+ "</p:cSld>";	
	
	protected final static String COLOR_MAPPING = "<p:clrMap xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" bg1=\"lt1\" tx1=\"dk1\" bg2=\"lt2\" tx2=\"dk2\" accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" hlink=\"hlink\" folHlink=\"folHlink\"/>";
	
	protected static Random random = new Random();
	
	public static long getSlideLayoutOrMasterId() {
		// See spec 4.8.18 (ST_SlideLayoutId) and 4.8.20 (ST_SlideMasterId)
		long val = random.nextInt(2147483647) + 2147483648l;
		return val;
	}
	protected long getSlideId() {
		// See spec 4.8.17 (ST_SlideId)
		long val = random.nextInt(2147483392) + 256;
		return val;
	}
	
	public JaxbPmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		setJAXBContext(Context.jcPML);						
	}

	public JaxbPmlPart() throws InvalidFormatException {
		super(new PartName("/ppt/presentation.xml"));
		setJAXBContext(Context.jcPML);						
	}

	public static Part newPartForContentType(String contentType, String partName)
	throws InvalidFormatException, PartUnrecognisedException {
		
		if (contentType.equals(ContentTypes.PRESENTATIONML_MAIN)
				|| contentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE) ) {
			return new MainPresentationPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE)) {
			return new SlidePart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE_MASTER)) {
			return new SlideMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE_LAYOUT)) {
			return new SlideLayoutPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_TABLE_STYLES)) {
			return new TableStylesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_PRES_PROPS)) {
			return new PresentationPropertiesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_VIEW_PROPS)) {
			return new ViewPropertiesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_TAGS)) {
			return new TagsPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_HANDOUT_MASTER)) {
			return new HandoutMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_NOTES_MASTER)) {
			return new NotesMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_NOTES_SLIDE)) {
			return new NotesSlidePart(new PartName(partName));
		} else {
			throw new PartUnrecognisedException("No subclass found for " 
					+ partName + " (content type '" + contentType + "')");					
		}
	}	
	
	protected Binder<Node> binder;
	
	
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
		
		if (jaxbElement == null) {
			// Test jaxbElement, since we don't want to do the
			// below if jaxbElement has already been set
			// using setJaxbElement (which doesn't create 
			// binder)
			PartStore partStore = this.getPackage().getPartStore();
			try {
				String name = this.partName.getName();
				InputStream is = partStore.loadPart( 
						name.substring(1));
				if (is==null) {
					log.warn(name + " missing from part store");
				} else {
					log.info("Lazily unmarshalling " + name);
					unmarshal( is );
				}
			} catch (JAXBException e) {
				log.error(e);
			} catch (Docx4JException e) {
				log.error(e);
			}
		}
		
		return binder;
	}
	
	/**
	 * Set the JAXBElement for this part, and a corresponding
	 * binder, based on the object provided.  Returns the new
	 * JAXBElement, so calling code can manipulate it.  Beware
	 * that this object is different to the one passed in!
	 * @param source
	 * @return
	 * @throws JAXBException
	 * @since 3.0.0
	 */
	public E createBinderAndJaxbElement(E source) throws JAXBException {
		
		// In order to create binder:-
		log.info("creating binder");
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(jaxbElement);
		unmarshal(doc.getDocumentElement());
		// return the newly created object, so calling code can use it in place
		// of their source object
		return jaxbElement;
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
	 * @throws XPathBinderAssociationIsPartialException 
	 */	
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException, XPathBinderAssociationIsPartialException {
		
		return XmlUtils.getJAXBNodesViaXPath(getBinder(), getJaxbElement(), xpathExpr, refreshXmlFirst);
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
	 * @throws XPathBinderAssociationIsPartialException 
	 */
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, Object someJaxbElement, boolean refreshXmlFirst) 
		throws JAXBException, XPathBinderAssociationIsPartialException {

		return XmlUtils.getJAXBNodesViaXPath(getBinder(), someJaxbElement, xpathExpr, refreshXmlFirst);
	}	

	/**
	 * Fetch DOM node / JAXB object pairs matching an XPath (for example "//w:p").
	 * 
	 * In JAXB, this association is partial; not all XML elements have associated JAXB objects, 
	 * and not all JAXB objects have associated XML elements.  
	 * 
	 * If the XPath returns an element which isn't associated
	 * with a JAXB object, the element's pair will be null.
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   
	 * 
	 * @param binder
	 * @param jaxbElement
	 * @param xpathExpr
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 * @throws XPathBinderAssociationIsPartialException
	 * @since 3.0.0
	 */
	public List<JAXBAssociation> getJAXBAssociationsForXPath(
			String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException, XPathBinderAssociationIsPartialException {

		return XmlUtils.getJAXBAssociationsForXPath(getBinder(), getJaxbElement(), xpathExpr, refreshXmlFirst);
		
	}	
	
	/**
	 * Fetch DOM node / JAXB object pairs matching an XPath (for example ".//w:p" - note the dot,
	 * which is necessary for this sort of relative path).
	 * 
	 * In JAXB, this association is partial; not all XML elements have associated JAXB objects, 
	 * and not all JAXB objects have associated XML elements.  
	 * 
	 * If the XPath returns an element which isn't associated
	 * with a JAXB object, the element's pair will be null.
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   
	 * 
	 * @param binder
	 * @param jaxbElement
	 * @param xpathExpr
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 * @throws XPathBinderAssociationIsPartialException
	 * @since 3.0.0
	 */
	public List<JAXBAssociation> getJAXBAssociationsForXPath(
			Object someJaxbElement, String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException, XPathBinderAssociationIsPartialException {

		return XmlUtils.getJAXBAssociationsForXPath(getBinder(), someJaxbElement, xpathExpr, refreshXmlFirst);
		
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
    public E unmarshal( java.io.InputStream is ) throws JAXBException {
		try {
			
			log.info("For " + this.getClass().getName() + ", unmarshall via binder");
			// InputStream to Document
			javax.xml.parsers.DocumentBuilderFactory dbf 
				= DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(is);

			// 
			binder = jc.createBinder();
			
			log.debug("info: " + binder.getClass().getName());
			
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			binder.setEventHandler(eventHandler);
			
			try {
				jaxbElement =  (E) XmlUtils.unwrap(binder.unmarshal( doc ));
			} catch (UnmarshalException ue) {
				
				if (is.markSupported() ) {
					log.info("encountered unexpected content; pre-processing");
					eventHandler.setContinue(true);
					DOMResult result = new DOMResult();
					
					Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
					XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
					
					doc = (org.w3c.dom.Document)result.getNode();
					try {				
						jaxbElement =  (E) XmlUtils.unwrap(binder.unmarshal( doc ));
					} catch (ClassCastException cce) {
						log.warn("Binder not available for this docx");
						Unmarshaller u = jc.createUnmarshaller();
						jaxbElement = (E) XmlUtils.unwrap(u.unmarshal( doc ));		
						
					}
				} else {
					log.error(ue);
					log.error(".. and mark not supported");
					throw ue;
				}
			}
			
			return jaxbElement;
			
		} catch (Exception e ) {
			e.printStackTrace();
			return null;
		}
    }

	@Override
    public E unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {

			binder = jc.createBinder();
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			binder.setEventHandler(eventHandler);
			
			try {
				jaxbElement =  (E) XmlUtils.unwrap(binder.unmarshal( el ));
			} catch (UnmarshalException ue) {
				log.info("encountered unexpected content; pre-processing");
				org.w3c.dom.Document doc = null;
				try {
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
					jaxbElement = (E) XmlUtils.unwrap(binder.unmarshal(doc));
				} catch (ClassCastException cce) {
					log.warn("Binder not available for this docx");
					Unmarshaller u = jc.createUnmarshaller();
					jaxbElement = (E) XmlUtils.unwrap(u.unmarshal( doc ));		
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
    
	
}
