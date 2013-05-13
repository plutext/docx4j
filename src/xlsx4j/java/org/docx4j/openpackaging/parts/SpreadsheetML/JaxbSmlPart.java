package org.docx4j.openpackaging.parts.SpreadsheetML;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

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
import org.w3c.dom.Node;
import org.xlsx4j.jaxb.Context;

public abstract class JaxbSmlPart<E>  extends JaxbXmlPart<E> {

	public JaxbSmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		setJAXBContext(Context.jcSML);						
	}

	public JaxbSmlPart() throws InvalidFormatException {
		super(new PartName("/xl/blagh.xml"));
		setJAXBContext(Context.jcSML);						
	}

	public static Part newPartForContentType(String contentType, String partName)
	throws InvalidFormatException, PartUnrecognisedException {
				
		if (contentType.equals(ContentTypes.SPREADSHEETML_PRINTER_SETTINGS)) {
			return new PrinterSettings(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_STYLES)) {
			return new Styles(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_WORKSHEET)) {
			return new WorksheetPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_CALC_CHAIN)) {
			return new CalcChain(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_SHARED_STRINGS)) {
			return new SharedStrings(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_PIVOT_TABLE)) {
			return new PivotTable(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_PIVOT_CACHE_DEFINITION)) {
			return new PivotCacheDefinition(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_PIVOT_CACHE_RECORDS)) {
			return new PivotCacheRecords(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_COMMENTS)) {
			return new CommentsPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_CONNECTIONS)) {
			return new ConnectionsPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_TABLE)) {
			return new TablePart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_QUERY_TABLE)) {
			return new QueryTablePart(new PartName(partName));
		} else {
			throw new PartUnrecognisedException("No subclass found for "
					+ partName + " (content type '" + contentType + "')");
		}
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
	 * 
	 * @since 3.0.0
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
	 * Fetch JAXB Nodes matching an XPath (for example "//s:row").
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
	 * 
	 * @since 3.0.0
	 */	
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException, XPathBinderAssociationIsPartialException {
		
		E el = getJaxbElement();
		return XmlUtils.getJAXBNodesViaXPath(getBinder(), el, xpathExpr, refreshXmlFirst);
	}	

	/**
	 * Fetch JAXB Nodes matching an XPath (for example ".//s:row" - note the dot,
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
	 * 
	 * @since 3.0.0
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
     *     
	 * @since 3.0.0
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

    /**
	 * @since 3.0.0
     */		
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
