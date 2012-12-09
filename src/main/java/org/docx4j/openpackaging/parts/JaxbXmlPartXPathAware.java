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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.AltChunkFinder;
import org.docx4j.utils.AltChunkFinder.LocatedChunk;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ContentAccessor;
import org.w3c.dom.Node;

/**
 * @author jharrop
 * @since 2.8
 */
public abstract class JaxbXmlPartXPathAware<E> extends JaxbXmlPart<E> implements AltChunkInterface {

	public JaxbXmlPartXPathAware(PartName partName)
			throws InvalidFormatException {
		super(partName);
		// TODO Auto-generated constructor stub
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
		
		return XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, xpathExpr, refreshXmlFirst);
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
			
			log.debug("binder: " + binder.getClass().getName());
			
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
     * @since 2.7.1
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

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(byte[])
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes)  throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();
		
		afiPart.setBinaryData(bytes); 		
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (this instanceof ContentAccessor) {
		 ((ContentAccessor)this).getContent().add(ac); 
		} else {
			throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}
		
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(java.io.InputStream)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is)   throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();		
		
		afiPart.setBinaryData(is); 
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (this instanceof ContentAccessor) {
		 ((ContentAccessor)this).getContent().add(ac); 
		} else {
			throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}
		
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(byte[], org.docx4j.wml.ContentAccessor)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes,
			ContentAccessor attachmentPoint)   throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();
		
		afiPart.setBinaryData(bytes); 		
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		attachmentPoint.getContent().add(ac);
					
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(java.io.InputStream, org.docx4j.wml.ContentAccessor)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is,
			ContentAccessor attachmentPoint) throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();		
		
		afiPart.setBinaryData(is); 
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		attachmentPoint.getContent().add(ac);
					
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#processAltChunksOfTypeHTML()
	 */
	@Override
	public WordprocessingMLPackage convertAltChunks() throws Docx4JException {
		
		// TODO: Currently only processes AltChunks in main document part.

		if (!(this instanceof ContentAccessor)) {
				throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}	
		PartName partName = this.getPartName();
				
		WordprocessingMLPackage clonePkg = (WordprocessingMLPackage)this.getPackage().clone(); // consistent with MergeDocx approach
		JaxbXmlPartXPathAware clonedPart = (JaxbXmlPartXPathAware)clonePkg.getParts().get(partName); 
				
		List<Object> contentList = ((ContentAccessor)clonedPart).getContent();
		
	    AltChunkFinder bf = new AltChunkFinder();
		new TraversalUtil(contentList, bf);

		CTAltChunk altChunk;
		boolean encounteredDocxAltChunk = false;
		for (LocatedChunk locatedChunk : bf.getAltChunks()) {
			
			altChunk = locatedChunk.getAltChunk();
			AlternativeFormatInputPart afip 
				=  (AlternativeFormatInputPart)clonedPart.getRelationshipsPart().getPart(
						altChunk.getId() );
			
			// Can we process it?
			AltChunkType type = afip.getAltChunkType();

			if (type.equals(AltChunkType.Xhtml) ) {
				
	            List<Object> results = null;
				try {
					results = XHTMLImporter.convert(toString(afip.getBuffer()), 
							null, clonePkg);
				} catch (UnsupportedEncodingException e) {
					log.error(e.getMessage(), e);
					// Skip this one
					continue;
				}
				
				int index = locatedChunk.getIndex(); 
				locatedChunk.getContentList().remove(index); // handles case where it is nested eg in a tc
				locatedChunk.getContentList().addAll(index, results);	
				
				log.info("Converted altChunk of type XHTML ");
				
			} else if (type.equals(AltChunkType.Mht) ) {
				log.warn("Skipping altChunk of type MHT ");
				continue;
			} else if (type.equals(AltChunkType.Xml) ) {
				log.warn("Skipping altChunk of type XML "); // what does Word do??
				continue;
			} else if (type.equals(AltChunkType.TextPlain) ) {
				
				String result= null;
				try {
					result = toString(afip.getBuffer());
				} catch (UnsupportedEncodingException e) {
					log.error(e.getMessage(), e);
					// Skip this one
					continue;
				}
				
				if (result!=null) {
					int index = locatedChunk.getIndex();
					locatedChunk.getContentList().remove(index); // handles case where it is nested eg in a tc
					
					org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
					org.docx4j.wml.P  para = factory.createP();
					locatedChunk.getContentList().add(index, para);	
				
					org.docx4j.wml.R  run = factory.createR();
					para.getContent().add(run);

					org.docx4j.wml.Text  t = factory.createText();
					t.setValue(result);
					run.getContent().add(t);		
					
					
					log.info("Converted altChunk of type text ");
				}

			} else if (type.equals(AltChunkType.WordprocessingML)
					 || type.equals(AltChunkType.OfficeWordMacroEnabled)
					 || type.equals(AltChunkType.OfficeWordTemplate)
					 ||type.equals(AltChunkType.OfficeWordMacroEnabledTemplate) ) {
				encounteredDocxAltChunk = true;
				continue;
				
			} else if (type.equals(AltChunkType.Rtf) ) {
				log.warn("Skipping altChunk of type RTF ");
				continue;
			} else if (type.equals(AltChunkType.Html) ) {
				log.warn("Skipping altChunk of type HTML ");
				continue;
				// if there was a pretty printer on class path,
				// could use it via reflection?
			}
						
		}
		
		if (encounteredDocxAltChunk) {
			
			// Docx AltChunks are handled by MergeDocx, if available
			try {
				// Use reflection, so docx4j can be built
				// by users who don't have the MergeDocx utility
				Class<?> documentBuilder = Class.forName("com.plutext.merge.ProcessAltChunk");			
				//Method method = documentBuilder.getMethod("merge", wmlPkgList.getClass());			
				Method[] methods = documentBuilder.getMethods(); 
				Method method = null;
				for (int j=0; j<methods.length; j++) {
					System.out.println(methods[j].getName());
					if (methods[j].getName().equals("process")) {
						method = methods[j];
						break;
					}
				}			
				if (method==null) {
					// User doesn't have MergeDocx
					throw new NoSuchMethodException();
				}
				
				// User has MergeDocx
				return (WordprocessingMLPackage)method.invoke(null, clonePkg);
				
			} catch (SecurityException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} catch (ClassNotFoundException e) {
				extensionMissing(e);
				return clonePkg;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} catch (NoSuchMethodException e) {
				extensionMissing(e);
				return clonePkg;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} 
			
		} else {
			return clonePkg;
		}
	}
	
	private void extensionMissing(Exception e) {
		log.warn("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		log.warn("* Skipping altChunk of type docx ");
		log.warn("* You don't appear to have the MergeDocx paid extension,");
		log.warn("* which is necessary to merge docx, or process altChunk.");
		log.warn("* Purchases of this extension support the docx4j project.");
		log.warn("* Please email sales@plutext.com or visit www.plutext.com if you want to buy it.");
	}
	
	private String toString(ByteBuffer bb) throws UnsupportedEncodingException {

		byte[] bytes = null;
        bytes = new byte[bb.limit()];
        bb.get(bytes);	        				
		return new String(bytes, "UTF-8");
	}
	
}
