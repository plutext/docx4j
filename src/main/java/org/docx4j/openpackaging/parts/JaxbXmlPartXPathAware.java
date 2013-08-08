/**
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.openpackaging.parts;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.JAXBAssociation;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author jharrop
 * @since 2.8
 */
public abstract class JaxbXmlPartXPathAware<E> extends JaxbXmlPart<E> 
implements XPathEnabled<E> {
	
	protected static Logger log = LoggerFactory.getLogger(JaxbXmlPartXPathAware.class);

	public JaxbXmlPartXPathAware(PartName partName)
			throws InvalidFormatException {
		super(partName);
		// TODO Auto-generated constructor stub
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
			
			InputStream is = null;
			try {
				String name = this.partName.getName();
				
				try {
					this.setContentLengthAsLoaded(
							partStore.getPartSize( name.substring(1)));
				} catch (UnsupportedOperationException uoe) {}
				
				is = partStore.loadPart( 
						name.substring(1));
				if (is==null) {
					log.warn(name + " missing from part store");
				} else {
					log.info("Lazily unmarshalling " + name);
					unmarshal( is );
				}
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			} catch (Docx4JException e) {
				log.error(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(is);
			}		
		} else if (binder==null) {
			// User might have set jaxb element, without creating a binder	
			try {
				log.debug("creating binder for " + this.getJaxbElement().getClass().getName());
				org.w3c.dom.Document doc =  XmlUtils.neww3cDomDocument();
				this.marshal(doc);
				unmarshal( doc.getDocumentElement() );
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			} 
		}
		
		return binder;
	}

	/* Don't override setJaxbElement(E jaxbElement) to create	  	
	 * binder here, since that would set the
	 * jaxbElement field to something different to
	 * the object being passed in, leading to
	 * calling code doing something different to what it thinks it 
	 * is doing! (ie backwards compatibility would be broken).
	 * 
	 * That's why we have this new method createBinderAndJaxbElement
	 */
	
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
		
		Binder<Node> binder = getBinder();
		E el = getJaxbElement();
		return XmlUtils.getJAXBNodesViaXPath(binder, el, xpathExpr, refreshXmlFirst);
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

		E el = getJaxbElement();
		return XmlUtils.getJAXBAssociationsForXPath(getBinder(), el, xpathExpr, refreshXmlFirst);
		
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
					// Unwrap, so we have eg CTEndnotes, not JAXBElement
			} catch (UnmarshalException ue) {
				
				if (is.markSupported() ) {
					// When reading from zip, we use a ByteArrayInputStream,
					// which does support this.
				
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
						jaxbElement =  (E) XmlUtils.unwrap(binder.unmarshal( doc ));
					} catch (ClassCastException cce) {
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
	
						log.warn("Binder not available for this docx");
						Unmarshaller u = jc.createUnmarshaller();
						jaxbElement = (E) XmlUtils.unwrap(u.unmarshal( doc ));		
						
					}
				} else {
					log.error(ue.getMessage(), ue);
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

    /**
     * @since 2.7.1
     */		
	@Override
    public E unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {
			log.info("For " + this.getClass().getName() + ", unmarshall via binder");

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
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	
//	private String toString(ByteBuffer bb) throws UnsupportedEncodingException {
//
//		byte[] bytes = null;
//        bytes = new byte[bb.limit()];
//        bb.get(bytes);	        				
//		return new String(bytes, "UTF-8");
//	}
	
}
