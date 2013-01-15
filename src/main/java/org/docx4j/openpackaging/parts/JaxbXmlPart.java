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
package org.docx4j.openpackaging.parts;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.wml.Numbering;

/** OPC Parts are either XML, or binary (or text) documents.
 * 
 *  Most are XML documents.
 *  
 *  docx4j aims to represent XML parts using JAXB.  
 *  
 *  Any XML Part for which we have a JAXB representation (eg the main
 *  document part) should extend this Part.  
 *  
 *  This class provides only one of the methods for serializing (marshalling) the 
 *  Java content tree back into XML data found in 
 *  javax.xml.bind.Marshaller interface.  You can always use 
 *  any of the others by getting the jaxbElement required by those
 *  methods.
 *  
 *  Insofar as unmarshalling is concerned, at present it doesn't 
 *  contain all the methods in javax.xml.bind.unmarshaller interface.
 *  This is because the content always comes from the same place
 *  (ie from a zip file or JCR via org.docx4j.io.*).  
 *  TODO - what is the best thing to unmarshall from?
 *  
 *  @param <E> type of the content tree object
 * */
public abstract class JaxbXmlPart<E> extends Part {
	
	protected static Logger log = Logger.getLogger(JaxbXmlPart.class);
	
	// This class is abstract
	// Most applications ought to be able to instantiate
	// any part as the relevant subclass.
	// If it was not abstract, users would have to
	// take care to set its content type and
	// relationship type when adding the part.
	
	public JaxbXmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		setJAXBContext(Context.jc);						
	}

	public JaxbXmlPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName);
		setJAXBContext(jc);
	}
	
	protected JAXBContext jc;
	public void setJAXBContext(JAXBContext jc) {
		this.jc = jc;
	}
	/**
	 * @since 2.7
	 */
	public JAXBContext getJAXBContext() {
		return jc;
	}
	
	
	/** The content tree (ie JAXB representation of the Part) */
	protected E jaxbElement = null;

	public E getJaxbElement() {
		
		// Lazy unmarshal
		if (jaxbElement==null) {
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
		return jaxbElement;
	}

	public void setJaxbElement(E jaxbElement) {
		this.jaxbElement = jaxbElement;
	}
	
	public void setJaxbElement(JAXBResult result) throws JAXBException {
		
		setJaxbElement((E)result.getResult());
	}
	
	public boolean isUnmarshalled(){
		return jaxbElement!=null;
	}
	
	
    /**
     * Marshal the content tree rooted at <tt>jaxbElement</tt> into a DOM tree.
     * 
     * @param node
     *      DOM nodes will be added as children of this node.
     *      This parameter must be a Node that accepts children
     *      ({@link org.w3c.dom.Document},
     *      {@link  org.w3c.dom.DocumentFragment}, or
     *      {@link  org.w3c.dom.Element})
     * 
     * @throws JAXBException
     *      If any unexpected problem occurs during the marshalling.
     */
    public void marshal(org.w3c.dom.Node node) throws JAXBException {
    	
		marshal(node, NamespacePrefixMapperUtils.getPrefixMapper()  );
    	
	}

    /**
     * Marshal the content tree rooted at <tt>jaxbElement</tt> into a DOM tree.
     * 
     * @param node
     *      DOM nodes will be added as children of this node.
     *      This parameter must be a Node that accepts children
     *      ({@link org.w3c.dom.Document},
     *      {@link  org.w3c.dom.DocumentFragment}, or
     *      {@link  org.w3c.dom.Element})
     * 
     * @throws JAXBException
     *      If any unexpected problem occurs during the marshalling.
     */
    public void marshal(org.w3c.dom.Node node, 
    		Object namespacePrefixMapper) throws JAXBException {

		try {
			Marshaller marshaller = jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(marshaller, namespacePrefixMapper);
			getJaxbElement();
			marshaller.marshal(jaxbElement, node);

		} catch (JAXBException e) {
//			e.printStackTrace();
			log.error(e);
			throw e;
		}
	}
    
    /**
	 * Marshal the content tree rooted at <tt>jaxbElement</tt> into an output
	 * stream, using org.docx4j.jaxb.NamespacePrefixMapper.
	 * 
	 * @param os
	 *            XML will be added to this stream.
	 * 
	 * @throws JAXBException
	 *             If any unexpected problem occurs during the marshalling.
	 */
    public void marshal(java.io.OutputStream os) throws JAXBException {
		
		marshal(os, NamespacePrefixMapperUtils.getPrefixMapper()  );
	}

    /**
	 * Marshal the content tree rooted at <tt>jaxbElement</tt> into an output
	 * stream
	 * 
	 * @param os
	 *            XML will be added to this stream.
	 * @param namespacePrefixMapper
	 *            namespacePrefixMapper
	 * 
	 * @throws JAXBException
	 *             If any unexpected problem occurs during the marshalling.
	 */
    public void marshal(java.io.OutputStream os, Object namespacePrefixMapper) throws JAXBException {

		try {
			Marshaller marshaller = jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(marshaller, namespacePrefixMapper);
			
			log.info("marshalling " + this.getClass().getName() );	
			getJaxbElement();
			if (jaxbElement==null) {
				log.error("No JAXBElement has been created for this part, yet!");
				throw new JAXBException("No JAXBElement has been created for this part, yet!");
			}
			marshaller.marshal(jaxbElement, os);

		} catch (JAXBException e) {
			//e.printStackTrace();
			log.error(e);
			throw e;
		}
	}
    
    /**
	 * Unmarshal XML data from the specified InputStream and return the
	 * resulting content tree. Validation event location information may be
	 * incomplete when using this form of the unmarshal API.
	 * 
	 * <p>
	 * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
	 * 
	 * @param is
	 *            the InputStream to unmarshal XML data from
	 * @return the newly created root object of the java content tree
	 * 
	 * @throws JAXBException
	 *             If any unexpected errors occur while unmarshalling
	 */
    public E unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
		    
			Unmarshaller u = jc.createUnmarshaller();
			
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			if (is.markSupported()) {
				// Only fail hard if we know we can restart
				eventHandler.setContinue(false);
			}
			u.setEventHandler(eventHandler);
			
			try {
				jaxbElement = (E) XmlUtils.unwrap(
						u.unmarshal( is ));						
			} catch (UnmarshalException ue) {
				
				if (is.markSupported() ) {
					// When reading from zip, we use a ByteArrayInputStream,
					// which does support this.
				
					log.info("encountered unexpected content; pre-processing");
					eventHandler.setContinue(true);
										
					try {
						Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
						is.reset();
						JAXBResult result = XmlUtils.prepareJAXBResult(jc);
						XmlUtils.transform(new StreamSource(is), 
								mcPreprocessorXslt, null, result);
						jaxbElement = (E) XmlUtils.unwrap(
								result.getResult() );	
					} catch (Exception e) {
						throw new JAXBException("Preprocessing exception", e);
					}
											
				} else {
					log.error(ue);
					log.error(".. and mark not supported");
					throw ue;
				}
			}
			

		} catch (JAXBException e ) {
			log.error(e);
			throw e;
		}
    	
		return jaxbElement;
    	
    }	
    
    public E unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {

			Unmarshaller u = jc.createUnmarshaller();
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			u.setEventHandler(eventHandler);
			
			try {
				jaxbElement = (E) XmlUtils.unwrap(
						u.unmarshal( el ) );
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
					JAXBResult result = XmlUtils.prepareJAXBResult(jc);
					Templates mcPreprocessorXslt = JaxbValidationEventHandler
							.getMcPreprocessor();
					XmlUtils.transform(doc, mcPreprocessorXslt, null, result);
					jaxbElement = (E) XmlUtils.unwrap(
							result.getResult() );	
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
    

    public boolean isContentEqual(Part other) throws Docx4JException {

		log.debug("Comparing " + getPartName().getName() + " : " + other.getPartName().getName() );
    	
    	if (!(other instanceof JaxbXmlPart)) {
    		log.debug(other.getPartName().getName() + " is not a JaxbXmlPart");
    		return false;
    	}
    	
    	/* Implementation notes: Either we implement
    	 * a notion of equality for all content trees
    	 * (ie wml, dml etc), or we marshal to something
    	 * and compare that.
    	 * 
    	 * Let's take the marshal approach.
    	 * 
    	 * Question becomes, what is it most efficient
    	 * to marshal to?
    	 * 
    	 * Looking at JAXB, probably SAX or a stream.
    	 * 
    	 * Then we want an equality test for one of those,
    	 * which returns as soon as inequality is established.
    	 * 
    	 * diffx contains a method boolean equivalent(InputStream xml1, InputStream xml2)
    	 * which will do what we want.
    	 * 
    	 * We marshal to an output stream, then need a 
    	 * way to get an input stream from that.
    	 * 
    	 * Since for now I'm just going to use a byte array for that
    	 * (though pipes would be more efficient and possibly worth it for large 
    	 * MainDocumentPart - calling code could just assume that part is different
    	 * though?),
    	 * I'll just test the equality of the byte arrays
    	 * and be done with it. 
    	 */
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    	ByteArrayOutputStream baos2 = new ByteArrayOutputStream(); 
    	try {
        	marshal(baos);
			((JaxbXmlPart)other).marshal(baos2);
		} catch (JAXBException e) {
			throw new Docx4JException("Error marshalling parts", e);
		}
    	
    	return java.util.Arrays.equals(baos.toByteArray(), baos2.toByteArray());

    }
	
}
