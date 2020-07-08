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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.Docx4jUnmarshallerListener;
import org.docx4j.jaxb.JAXBAssociation;
import org.docx4j.jaxb.JAXBImplementation;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartTooLargeException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
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
			PartStore partStore = this.getPackage().getSourcePartStore();
			
			InputStream is = null;
			try {
				String name = this.getPartName().getName();
				
				try {
					this.setContentLengthAsLoaded(
							partStore.getPartSize( name.substring(1)));
				} catch (UnsupportedOperationException uoe) {}
				
				if (MAX_BYTES_Unmarshal_Error>-1
						&& this.getContentLengthAsLoaded()>MAX_BYTES_Unmarshal_Error) {
					throw new PartTooLargeException(this.getPartName() + ", length " + this.getContentLengthAsLoaded() + " exceeds your configured maximum allowed size for unmarshal.");
				}
				
				is = partStore.loadPart( 
						name.substring(1));
				if (is==null) {
					log.warn(name + " missing from part store");
				} else {
					log.debug("Lazily unmarshalling " + name);
					unmarshal( is, true ); // we need the DOM doc
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

	/** You can't use this override to create/update a binder, since this would set the
	 * jaxbElement field to something different to the object being passed in 
	 * (as a consequence of the process to create a binder).
	 * 
	 * We don't want that, because calling code may then continue to manipulate
	 * the field, without effect..
	 * 
	 * See instead createBinderAndJaxbElement
	 */
	@Override
	public void setJaxbElement(E jaxbElement) {
		super.setJaxbElement(jaxbElement);
		binder=null; // any existing binder is invalid
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
		log.debug("creating binder");
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(source);
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
	 * see https://github.com/javaee/jaxb-v2/issues/459
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
	 * see https://github.com/javaee/jaxb-v2/issues/459
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
	 * see https://github.com/javaee/jaxb-v2/issues/459
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

		
		Binder<Node> binder = getBinder(); // do this first!
		return XmlUtils.getJAXBAssociationsForXPath(binder, getJaxbElement(), xpathExpr, refreshXmlFirst);
		
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
	
	private void unwrapUsually(Binder<Node> binder, Node doc) throws JAXBException {
		
		jaxbElement =  (E) XmlUtils.unwrap(binder.unmarshal( doc ));
		// Unwrap, so we have eg CTEndnotes, not JAXBElement
	
		// .. but we do need to leave it wrapped, 
		// if there is not @XmlRootElement annotation 
		if (jaxbElement instanceof org.docx4j.dml.chartDrawing.CTDrawing) {
			// Check it
			Object tmp = binder.unmarshal( doc );
			if (tmp instanceof javax.xml.bind.JAXBElement) {
				QName qname = ((javax.xml.bind.JAXBElement)tmp).getName();
				if (qname.equals( org.docx4j.dml.chart.ObjectFactory._UserShapes_QNAME)) {
					jaxbElement=(E)tmp;	
				}
			}
		}
				
	}

	private void unwrapUsually(Object tmp) throws JAXBException {
	
		// ..  we do need to leave it wrapped, 
		// if there is no @XmlRootElement annotation 
		if (tmp instanceof javax.xml.bind.JAXBElement) {
			QName qname = ((javax.xml.bind.JAXBElement)tmp).getName();
			if (qname.equals( org.docx4j.dml.chart.ObjectFactory._UserShapes_QNAME)) {
				jaxbElement=(E)tmp;	
				return;
			}
		}
		
		jaxbElement =  (E) XmlUtils.unwrap(tmp);
		// Unwrap, so we have eg CTEndnotes, not JAXBElement
				
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

		return unmarshal(is, false);
	}
	
	/**
	 * Unmarshalling via DOM document can be 4x slower than unmarshalling
	 * the inputstream using XMLStreamReader, so we avoid doing that where possible.
	 * 
	 * @param is
	 * @param forceBinder
	 * @return
	 * @throws JAXBException
	 */
	private E unmarshal( java.io.InputStream is, boolean forceBinder ) throws JAXBException {
		
//		long start = System.currentTimeMillis();
		
		try {
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			org.w3c.dom.Document doc = null;
			
			try {
				
				boolean wantBinder = forceBinder;
				
				if (!forceBinder) {
					if (this instanceof MainDocumentPart) {
						wantBinder = Docx4jProperties.getProperty(
								"docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart", false);
					} else {
						wantBinder = Docx4jProperties.getProperty(
								"docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.OtherParts", false);					
					}
				}

				if (wantBinder) {
					log.debug("For " + this.getClass().getName() + ", unmarshall via binder");
					
					if (Context.jaxbImplementation==JAXBImplementation.ECLIPSELINK_MOXy) {
						log.debug("MOXy: checking whether binder workaround is necessary");
						/* Workaround for MOXy issue 

							I can set a ValidationEventHandler on an Unmarshaller, which triggers on an unexpected element ValidationEvent, then catch an UnmarshalException to handle that. That all works as expected :-)
							
							If instead I set a ValidationEventHandler on a Binder then call unmarshal, an unexpected element ValidationEvent never triggers.
							
							I tested MOXy 2.5.2 and 2.6.3.
							
							Looking at the MOXy source code on GitHub, at a high level it looks like it ought to work.
							
							JAXBBinder lets you setEventHandler on XMLBinder, and that in turn invokes setErrorHandler on the unmarshallers.
						 */
						String contents = IOUtils.toString(is, "UTF-8");  
						IOUtils.closeQuietly(is);
						try (InputStream is2 = new ByteArrayInputStream(contents.getBytes("UTF-8"))) {
						
							if (contents.contains("AlternateContent")) {
								// looks like we need to do the workaround
								// 3.4.0: this needs to be refined, since we can now handle
								// alternate content in w:r (so than in itself is ok)
								log.debug("MOXy: yes, performing workaround");
								// Get object with mc content resolved
								// could do super.unmarshal(is);
								// but better
								try {
									Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
									DOMResult result = new DOMResult();
									
									// Guard against XXE
							        XMLInputFactory xif = XMLInputFactory.newInstance();
							        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
							        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
						        	XMLStreamReader xsr = xif.createXMLStreamReader(is2);
									
									XmlUtils.transform(new StAXSource(xsr), 
											mcPreprocessorXslt, null, result);
									doc = (org.w3c.dom.Document) result.getNode();
								} catch (Exception e) {
									throw new JAXBException("Preprocessing exception", e);
								}
								
							} else {
								// continue with a new is
								log.debug("MOXy: no, looks ok");
							}
						
						}
					}
				
					// InputStream to Document
					if (doc==null) // we didn't do the MOXy workaround above 
					{						
						doc = XmlUtils.getNewDocumentBuilder().parse(is); // this also guards against XXE
					}
	
					// 
					binder = jc.createBinder();
					
					log.debug("info: " + binder.getClass().getName());
					
//					eventHandler.setContinue(false);
					binder.setEventHandler(eventHandler);
					
					unwrapUsually(binder,  doc);  // unlikely to need this in the code below
					
				} else {
					log.debug("For " + this.getClass().getName() + ", unmarshall (no binder)");
					
					// Guard against XXE
			        XMLInputFactory xif = XMLInputFactory.newInstance();
			        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
			        XMLStreamReader xsr = xif.createXMLStreamReader(is);			
				    
			        // XMLStreamReaderWrapper xsrw = new XMLStreamReaderWrapper(this, xsr)
					Unmarshaller u = jc.createUnmarshaller();
					
//					if (is.markSupported()) {
//						// Only fail hard if we know we can restart
//						eventHandler.setContinue(false);
//					}
					u.setEventHandler(eventHandler);
					
					Unmarshaller.Listener docx4jUnmarshallerListener = new Docx4jUnmarshallerListener(this);
					u.setListener(docx4jUnmarshallerListener);
					
					unwrapUsually(u.unmarshal( xsr ));						
					
				}
			} catch (org.xml.sax.SAXParseException e) {
				
				/*
					org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 10; DOCTYPE is disallowed when the feature "http://apache.org/xml/features/disallow-doctype-decl" set to true.
						at com.sun.org.apache.xerces.internal.parsers.DOMParser.parse(Unknown Source)
						at com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl.parse(Unknown Source)
						at javax.xml.parsers.DocumentBuilder.parse(Unknown Source)
					 */
				log.error(e.getMessage(), e);
				throw e;
					
			} catch (Exception ue) {

				if (ue instanceof UnmarshalException) {
					// Usually..
					
					if (((UnmarshalException)ue).getLinkedException()!=null) {

						log.warn(((UnmarshalException)ue).getLinkedException().getMessage());	
						
						if (((UnmarshalException)ue).getLinkedException().getMessage().contains("entity")) {
						
							/*
								Caused by: javax.xml.stream.XMLStreamException: ParseError at [row,col]:[10,19]
								Message: The entity "xxe" was referenced, but not declared.
									at com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl.next(Unknown Source)
									at com.sun.xml.internal.bind.v2.runtime.unmarshaller.StAXStreamConnector.bridge(Unknown Source)
								 */
							log.error(ue.getMessage(), ue);
							throw ue;
						}

						if (((UnmarshalException)ue).getLinkedException().getMessage().contains("EmptyPrefixedAttName?prefix=\"xmlns")) {
							// eg xmlns:ns0=""
							
							/*
								Caused by: org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 600; The value of the attribute "prefix="xmlns",localpart="ns0",rawname="xmlns:ns0"" is invalid. Prefixed namespace bindings may not be empty.
									at com.sun.org.apache.xerces.internal.parsers.DOMParser.parse(DOMParser.java:257)
									at com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl.parse(DocumentBuilderImpl.java:339)
									at javax.xml.parsers.DocumentBuilder.parse(DocumentBuilder.java:121)
							 */
							log.error(ue.getMessage(), ue);
							if (is.markSupported() ) {
								is.reset();
								
								// read it to string
								String theString = IOUtils.toString(is, "UTF-8");
								
								// remove xmlns:ns0=""
								String fixed = theString.replace("xmlns:ns0=\"\"", "");
								if (theString.equals(fixed)) {
									// no change
									throw ue;									
								} else {
									is = new ByteArrayInputStream(fixed.getBytes("UTF-8"));
									return this.unmarshal(is, forceBinder);
								}
							} else {
								throw ue;								
							}
						}
					}
				} else {
					// eg java.lang.NumberFormatException
					log.warn( ue.getMessage(), ue);
				}
				
				if (is.markSupported() ) {
					
					try {
						is.reset();
					} catch (IOException ioe) {
						log.error(ioe.getLocalizedMessage());
						log.warn("problem in " + this.getPartName() ); 					
						log.warn(ue.getMessage(), ue);
						log.warn(".. and mark not supported");
						log.warn(ioe.getLocalizedMessage());
						throw ue;
					}
					// when reading from zip, we use a ByteArrayInputStream, which does support mark.
					log.info("encountered unexpected content in " + this.getPartName() + "; pre-processing");
									
					/* Always try our preprocessor, since if what is first encountered is w14:whatever
					 * the error would be:
					 *  
					 *    unexpected element (uri:"http://schemas.microsoft.com/office/word/2010/wordml", local:"whatever")
					 *
					 * but there could well be mc:AlternateContent somewhere 
					 * further down in the document.
					 */
					
					// If we get here, we always use DOM source and binder
					doc = XmlUtils.getNewDocumentBuilder().parse(is); // this also guards against XXE
						
					// There is no JAXBResult(binder),
					// so use a 
					DOMResult result = new DOMResult();
					
					Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
					XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
					
					doc = (org.w3c.dom.Document)result.getNode();
					
					try {
						// mimic docx4j 2.7.0 and earlier behaviour; this will 
						// drop things we don't have a content model for; the preprocessor doesn't need to 
						// do that				
						eventHandler.setContinue(true);
						binder = jc.createBinder();
						binder.setEventHandler(eventHandler);
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
							:
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
						eventHandler.setContinue(true);
						u.setEventHandler(eventHandler);
						unwrapUsually(u.unmarshal( doc ));		
						
					}
				} else {
					log.warn("problem in " + this.getPartName() ); 					
					log.warn(ue.getMessage(), ue);
					log.warn(".. and mark not supported");
					throw ue;
				}
			}
			
//			long finish = System.currentTimeMillis();
//			long diff = finish - start;
//			System.out.println("Time taken " + diff + " for " + this.getPartName().getName() );
			
			return jaxbElement;
			
		} catch (Exception e ) {
			
			// The XmlUtils.getNewDocumentBuilder().parse(is) case
			
			/* java.lang.NullPointerException
				at com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor.startDTD(Unknown Source)
				at com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl.scanDTDInternalSubset(Unknown Source)
				at com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl$DTDDriver.dispatch(Unknown Source)
				at com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl$DTDDriver.next(Unknown Source)
				at com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl$PrologDriver.next(Unknown Source)
			 */
			for ( int i=0 ; i<e.getStackTrace().length; i++) {
				if (e.getStackTrace()[i].getClassName().contains("DTD")
						|| e.getStackTrace()[i].getMethodName().contains("DTD")) {
					// Mimic Word 2010 message
					throw new JAXBException("DTD is prohibited", e);
				}
			}
			
			throw new JAXBException(e.getMessage(), e);
		}
    }

    /**
     * @since 2.7.1
     */		
	@Override
    public E unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {
			log.debug("For " + this.getClass().getName() + ", unmarshall via binder");				

			if (Context.jaxbImplementation==JAXBImplementation.ECLIPSELINK_MOXy) {
				log.debug("MOXy: pre-emptively transforming");
				// Workaround for MOXy issue http://stackoverflow.com/questions/37225221/moxy-validationevents-triggered-by-unmarshaller-but-not-binder
				// Always transform; rather than converting to String, testing that etc
				try {
					Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
					DOMResult result = new DOMResult();
					XmlUtils.transform(new DOMSource(el), 
							mcPreprocessorXslt, null, result);
					org.w3c.dom.Document doc = (org.w3c.dom.Document) result.getNode();
					el = doc.getDocumentElement();
				} catch (Exception e) {
					throw new JAXBException("Preprocessing exception", e);
				}
			}
			
			binder = jc.createBinder();
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			binder.setEventHandler(eventHandler);
			
			try {
//				jaxbElement =  (E) XmlUtils.unwrap(binder.unmarshal( el ));
				unwrapUsually(binder,  el);  // unlikely to need this in the code below
				
				
			} catch (Exception ue) {
				if (ue instanceof UnmarshalException) {
					// Usually..
				} else {
					// eg java.lang.NumberFormatException
					log.warn( ue.getMessage(), ue);
					log.info(".. can recover if problem is w:tblW/@w:w");					
				}
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
					eventHandler.setContinue(true);
					u.setEventHandler(eventHandler);
					jaxbElement = (E) XmlUtils.unwrap(u.unmarshal( doc ));		
				} catch (Exception e) {
					throw new JAXBException("Preprocessing exception", e);
				}
			}
			return jaxbElement;
			
		} catch (JAXBException e) {
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
