/*
 *  Copyright 2007-2018, Plutext Pty Ltd.
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


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.Location;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.Docx4jMarshallerListener;
import org.docx4j.jaxb.Docx4jUnmarshallerListener;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.LocationAwareXMLStreamException;
import org.docx4j.openpackaging.exceptions.PartTooLargeException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.io3.stores.ZipPartStore.ByteArray;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPartFilterOutputStream;
import org.docx4j.org.apache.xml.security.Init;
import org.docx4j.org.apache.xml.security.c14n.Canonicalizer;
import org.docx4j.utils.XMLStreamWriterWrapper;
import org.docx4j.utils.XMLStreamWriterWrapperIndenting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
public abstract class JaxbXmlPart<E> /* used directly only by DocProps parts, Rels part, FontTablePart */ 
	extends Part {
	
	protected static Logger log = LoggerFactory.getLogger(JaxbXmlPart.class);
	
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
	
	protected static long MAX_BYTES_Unmarshal_Error = -1;
	
	static {
		MAX_BYTES_Unmarshal_Error = Docx4jProperties.getPropertyLong("docx4j.openpackaging.parts.MAX_BYTES.unmarshal.error", -1);
	}
	
	
	
	/** The content tree (ie JAXB representation of the Part) */
	protected E jaxbElement = null;

	/**
	 * Get the live contents of this part (the JAXB object model).
	 * (An alias/synonym for older getJaxbElement(), but now throws exception)
	 * @throws Docx4JException
	 * @return
	 * @since 3.0
	 */
	public E getContents() throws Docx4JException {
		
		// Lazy unmarshal
		InputStream is = null;
		if (jaxbElement==null) {
			if (this.getPackage()==null) {
				log.warn("This part not added to a package, so its contents can't be retrieved. " );
				return null;
			}
			PartStore partStore = this.getPackage().getSourcePartStore();
			if (partStore==null) {
				log.info("No PartStore defined for this package (it was probably created, not loaded). " );
				log.info(this.getPartName().getName() + ": did you initialise its contents to something?");
				return null;
				// or we could create it, with a bit of effort;
				// as to which see http://stackoverflow.com/questions/1090458/instantiating-a-generic-class-in-java
			} 			
			
			try {
				String name = this.getPartName().getName();
				
				try {
					if (partStore!=null) {
						this.setContentLengthAsLoaded(
								partStore.getPartSize( name.substring(1)));
					}
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
					unmarshal( is );
				}
			} catch (JAXBException e) {
				throw new Docx4JException("Problem with part " + this.getPartName(), e);
//			} catch (Docx4JException e) {
//				log.error(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(is);
			}			
		}
		return jaxbElement;
	}
	
	/**
	 * Get the live contents of this part.
	 * (getContents() is preferred, this is the older/less friendly method name)
	 * @return
	 */
	public E getJaxbElement() {
		try {
			return getContents();
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
			return null;
		} 
	}

	public void setJaxbElement(E jaxbElement) {
		this.jaxbElement = jaxbElement;
	}
	/**
	 * Set the  contents of this part.
	 * (Just an alias/synonym for setJaxbElement())
	 * @param jaxbElement
	 * @since 3.0
	 */
	public void setContents(E jaxbElement) {
		this.jaxbElement = jaxbElement;
	}
	
	public void setJaxbElement(JAXBResult result) throws JAXBException {
		
		setJaxbElement((E)result.getResult());
	}
	
	/**
	 * See your content as XML.  An easy way to invoke XmlUtils.marshaltoString 
	 *  
	 * @return
	 * @since 3.0.0
	 */
	public String getXML() {
		return XmlUtils.marshaltoString( getJaxbElement(), true, true, jc );
	}
	
	public boolean isUnmarshalled(){
		return jaxbElement!=null;
	}
	
	/**
	 * unmarshallFromTemplate.  Where jaxbElement has not been
	 * unmarshalled yet, this is more efficient (3 times
	 * faster, in some testing) than calling
	 * XmlUtils.marshaltoString directly, since it avoids
	 * some JAXB processing.  
	 * 
	 * @param mappings
	 * @throws JAXBException
	 * @throws Docx4JException
	 * 
	 * @since 3.0.0
	 */
	public void variableReplace(java.util.Map<String, String> mappings) throws JAXBException, Docx4JException {
		
		// Get the contents as a string
		String wmlTemplateString = null;
		if (jaxbElement==null) {

			PartStore partStore = this.getPackage().getSourcePartStore();
			String name = this.getPartName().getName();
			InputStream is = partStore.loadPart( 
					name.substring(1));
			if (is==null) {
				log.warn(name + " missing from part store");
				throw new Docx4JException(name + " missing from part store");
			} else {
				log.info("Lazily unmarshalling " + name);
//				wmlTemplateString = convertStreamToString(is);
				
				// This seems to be about 5% faster than the Scanner approach
				try {
					wmlTemplateString = IOUtils.toString(is, "UTF-8");
				} catch (IOException e) {
					throw new Docx4JException(e.getMessage(), e);
				}
			}
			
		} else {
			
			wmlTemplateString = XmlUtils.marshaltoString(jaxbElement, true, false, jc);
			
		}
		
		// Do the replacement
		jaxbElement = (E)XmlUtils.unwrap(
							XmlUtils.unmarshallFromTemplate(wmlTemplateString, mappings, jc));
		
	}
	
    /**
     * Use an XSLT to alter the contents of this part.  You can transform to whatever
     * you like (ie it doesn't have to be WordML content), which is why the API design
     * is that you provide the Result object. 
     *  
     * If you do want to replace the content in this part, convert your result to
     * an element or input stream, then invoke unmarshal on it, then setContents. 
     * (Unmarshal takes care of any unexpected content, sidestepping the issue of 
     *  whether to do that before the transform (where reading the part directly),
     *  or after).
     * 
     * @param xslt
     * @param transformParameters
     * @throws Exception
	 * @since 3.3.6
     */    
    public void transform(Templates xslt,
			  Map<String, Object> transformParameters, Result result) throws Docx4JException {

		//JAXBResult result = XmlUtils.prepareJAXBResult(jc);
    	
		if (jaxbElement==null) {

			PartStore partStore = this.getPackage().getSourcePartStore();
			String name = this.getPartName().getName();
			try (InputStream is = partStore.loadPart( name.substring(1))) {
				
				if (is==null) {
					log.warn(name + " missing from part store");
					throw new Docx4JException(name + " missing from part store");
				} 
				
				// Guard against XXE
		        XMLInputFactory xif = XMLInputFactory.newInstance();
		        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
	        	XMLStreamReader xsr = xif.createXMLStreamReader(is);
				XmlUtils.transform(new StAXSource(xsr), xslt, transformParameters, result);
				
			} catch (IOException e /* closing InputStream */ ) {
				throw new Docx4JException(e.getMessage(), e);
			} catch (XMLStreamException e) {
				throw new Docx4JException(e.getMessage(), e);
			}			
		} else {
			org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();			
			try {
				this.marshal(doc);
			} catch (JAXBException e) {
				// shouldn't happen
				throw new Docx4JException("Marshalling exception preparing content for transform", e);
			}
			org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
			
		}

//		try {
//			return (E) XmlUtils.unwrap(result.getResult() );
//		} catch (JAXBException e) {
//			throw new Docx4JException("Problem with transform result", e);
//		}	


	}
	
	
//	private static String convertStreamToString(java.io.InputStream is) {
//	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
//	    return s.hasNext() ? s.next() : "";
//	}
	
	
	/**
	 * Replace the contents of this part with the output of passing it through your SAXHandler. 
	 * This is offered as an alternative to the similar methods which use StAX.  If you are
	 * unsure which to use, you should probably use the StAX approach.
	 * 
	 * This is most efficient in the case where there has been no need for JAXB to unmarshal
	 * the contents.  In this case, it is possible to process then save the contents without
	 * incurring JAXB overhead (you may see 1/4 heap usage).
	 * 
	 * @param saxHandler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws Docx4JException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void pipe(SAXHandler saxHandler) throws ParserConfigurationException, SAXException, Docx4JException, IOException, JAXBException {
		
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();		
		
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setContentHandler(saxHandler);
	    
	    PartStore partStore = null;
	    
		if (jaxbElement==null) {

			partStore = this.getPackage().getSourcePartStore();
			String name = this.getPartName().getName();
			InputStream is = partStore.loadPart( 
					name.substring(1));
			if (is==null) {
				log.warn(name + " missing from part store");
				throw new Docx4JException(name + " missing from part store");
			} else {
				log.info("Fetching from part store " + name);
					
			    xmlReader.parse(new InputSource(is));	
			}
			
		} else {
			
			// TODO inefficient? But for now..
		    xmlReader.parse(new InputSource(
		    		XmlUtils.marshaltoInputStream(jaxbElement, true, this.jc)));            
		}
		
        //log.debug((new String(baos.toByteArray())).substring(0, 4000) );
		
		if (jaxbElement==null
				&& partStore instanceof ZipPartStore ) {
			
			log.debug("Just update the entry in the ZipPartStore");
			
			// Just update the entry in the ZipPartStore
			ByteArray byteArray = ((ZipPartStore)partStore).getByteArray(this.getPartName().getName().substring(1));
			byteArray.setBytes(saxHandler.getOutputStream().toByteArray());
			
		} else {
			
			if (jaxbElement==null
					&& log.isInfoEnabled()) {				
				log.info(partStore.getClass().getName() + ": can't update in place, so unmarshalling.");
			} else {			
				log.debug("unmarshalling");
			}
			jaxbElement = this.unmarshal( new ByteArrayInputStream(saxHandler.getOutputStream().toByteArray()) );  // so much for avoiding JAXB!
			
		}
	}
	
	/**
	 * Replace the contents of this part with the output of passing it through your StAXHandler. 
	 * 
	 * This is most efficient in the case where there has been no need for JAXB to unmarshal
	 * the contents.  In this case, it is possible to process then save the contents without
	 * incurring JAXB overhead (you may see 1/4 heap usage).
	 * 
	 * @param handler
	 * @throws XMLStreamException
	 * @throws Docx4JException
	 * @throws JAXBException
	 */
	public void pipe(StAXHandlerInterface handler) throws XMLStreamException, Docx4JException, JAXBException {
		
		pipe( handler, null);
	}
	
	/**
	 * Replace the contents of this part with the output of passing it through your StAXHandler. 
	 * 
	 * This is most efficient in the case where there has been no need for JAXB to unmarshal
	 * the contents.  In this case, it is possible to process then save the contents without
	 * incurring JAXB overhead (you may see 1/4 heap usage).
	 * 
	 * @param handler
	 * @param filter
	 * @throws XMLStreamException
	 * @throws Docx4JException
	 * @throws JAXBException
	 */
	public void pipe(StAXHandlerInterface handler, StreamFilter filter) throws XMLStreamException, Docx4JException, JAXBException {
		
	       XMLInputFactory xmlif = null;
	        xmlif = XMLInputFactory.newInstance();
	        xmlif.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
	        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
	        xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
	        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
	        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
	        
	        // Nonfatal errors and warnings
	        xmlif.setXMLReporter(
	        		(new XMLReporter() {

	            @Override
	            public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
	    			log.warn("Error:" + errorType + ", " + message + " at line " + location.getLineNumber() + ", col " +   location.getColumnNumber());
	            }
	        })
	        );
	        
	        // xmlif.setProperty(XMLInputFactory.RESOLVER
	        // xmlif.setProperty(XMLInputFactory.ALLOCATOR
	    
	    // First, set up stream reader
	    XMLStreamReader xmlr = null;
	    PartStore partStore = null;

		if (jaxbElement==null) {

			partStore = this.getPackage().getSourcePartStore();
			String name = this.getPartName().getName();
			InputStream is = partStore.loadPart( 
					name.substring(1));
			if (is==null) {
				log.warn(name + " missing from part store");
				throw new Docx4JException(name + " missing from part store");
			} else {
				log.info("Fetching from part store " + name);
					
		        if (filter==null) {
		        	xmlr = xmlif.createXMLStreamReader(is);
		        } else {
		        	xmlr = xmlif.createFilteredReader(xmlif.createXMLStreamReader(is), filter);            
		        }
					
			}
			
		} else {
			
			// TODO marshal to XmlStreamWriter or event, then read from that.
			// But for now..
	        if (filter==null) {
	        	xmlr = xmlif.createXMLStreamReader(XmlUtils.marshaltoInputStream(jaxbElement, true, this.jc));
	        } else {
	        	xmlr = xmlif.createFilteredReader(
	        			xmlif.createXMLStreamReader(
	        					XmlUtils.marshaltoInputStream(jaxbElement, true, this.jc)), filter);            
	        }			
		}
		
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();            
        XMLStreamWriter xmlWriter = null; // outputFactory.createXMLStreamWriter(outputFile);    
        ByteArrayOutputStream baos = null;
        
		if (jaxbElement==null) {
			baos = new ByteArrayOutputStream(); 
			xmlWriter = outputFactory.createXMLStreamWriter(baos, "UTF-8");
				// Avoid  Underlying stream encoding 'Cp1252' and input paramter for writeStartDocument() method 'UTF-8' do not match.
				// (Which StAX implementation is that?)
				// See further http://stackoverflow.com/questions/2943605/stax-setting-the-version-and-encoding-using-xmlstreamwriter
			
		} else {
			
			// TODO make a XmlStreamWriter we can read from
			// But for now
			baos = new ByteArrayOutputStream(); 
			xmlWriter = outputFactory.createXMLStreamWriter(baos, "UTF-8");
		}
		
		try {
			log.debug("StAX implementation details:");
			log.debug(xmlr.getClass().getName());
			log.debug(xmlWriter.getClass().getName());
			handler.handle(xmlr, xmlWriter);
			
		} catch (/* fatal error */ LocationAwareXMLStreamException e) {
			
			log.error(e.getMessage() + " at line " + e.getLocation().getLineNumber() + ", col " +   e.getLocation().getColumnNumber());
			e.getCause().printStackTrace();
			
			// now print that out
			InputStream is = null;
			if (jaxbElement==null) {
				

				partStore = this.getPackage().getSourcePartStore();
				String name = this.getPartName().getName();
				is = partStore.loadPart( 
						name.substring(1));			

				if (is==null) {
					log.warn(name + " missing from part store");
					throw new Docx4JException(name + " missing from part store");
				}
				
			} else {
				
				is = XmlUtils.marshaltoInputStream(jaxbElement, true, this.jc);            
			}
			if (is!=null) {

				try {
					List<String> lines = IOUtils.readLines(is);
					String line = lines.get(
							e.getLocation().getLineNumber()-1);
					
					int PRIOR_CHARS = 100;
					
					int start = 0;
					if (e.getLocation().getColumnNumber()>PRIOR_CHARS) {
						start = e.getLocation().getColumnNumber() - PRIOR_CHARS;
					}
					int end = e.getLocation().getColumnNumber() + PRIOR_CHARS;
					if (end > line.length()-1 ) {
						end = line.length()-1;
					}
					
					log.error("error is at pos " + PRIOR_CHARS + " in " + line.substring(start, end));
					
//					if (e.getMessage().contains("NamespaceURI")) {
//						// lets print them
//						if (lines.get(0).endsWith("?>")) {
//							// assume at start of line 1
//							line = lines.get(1);
//						} else {
//							// assume at start of line 0
//							line = lines.get(0);
//						}
//						end = line.indexOf(">");
//						if (end<0) end = 2000;
//						if (end > line.length()-1 ) {
//							end = line.length()-1;
//						}
//						log.error("Namespace decs: " + line.substring(0, end));
//					}
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			// now rethrow
			throw (XMLStreamException)e.getCause();
			
		}
        
        xmlr.close();
        xmlWriter.flush();
        xmlWriter.close();  
        
        //log.debug((new String(baos.toByteArray())).substring(0, 4000) );
		
		if (jaxbElement==null
				&& partStore instanceof ZipPartStore ) {
			
			log.debug("Just update the entry in the ZipPartStore");
			
			// Just update the entry in the ZipPartStore
			ByteArray byteArray = ((ZipPartStore)partStore).getByteArray(this.getPartName().getName().substring(1));
			byteArray.setBytes(baos.toByteArray());
			
		} else {
			
			if (jaxbElement==null
					&& log.isInfoEnabled()) {				
				log.info(partStore.getClass().getName() + ": can't update in place, so unmarshalling.");
			} else {			
				log.debug("unmarshalling");
			}
			jaxbElement = this.unmarshal( new ByteArrayInputStream(baos.toByteArray()) );  // so much for avoiding JAXB!
			
		}
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
//			marshaller.setProperty("com.sun.xml.internal.bind.c14n",true);
			
//			marshaller.setListener(Docx4jMarshallerListener);
			
			NamespacePrefixMapperUtils.setProperty(marshaller, namespacePrefixMapper);
			getContents();
	    	setMceIgnorable( (McIgnorableNamespaceDeclarator) namespacePrefixMapper);
	    		// this method needs to be suitably overridden in a subclass,
	    		// to .setMcIgnorable
	    	
	    	// Commented out, since I'm not sure it is ever useful to do this?
//			if (Docx4jProperties.getProperty("docx4j.jaxb.marshal.canonicalize", false)) {
//	    	
//				org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(jaxbElement, jc);
//				node.appendChild(
//						node.getOwnerDocument().importNode(doc.getDocumentElement(), true));
//				
//			} else {

				marshaller.marshal(jaxbElement, node);
//			}
			
			// Now unset it
			((McIgnorableNamespaceDeclarator) namespacePrefixMapper).setMcIgnorable(null);
			
		} catch (Docx4JException e) {
			throw new JAXBException(e);  // avoid change to method signature
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
			if (Docx4jProperties.getProperty("docx4j.jaxb.formatted.output", true)) {
				marshaller.setProperty("jaxb.formatted.output", true);
			}
//			marshaller.setProperty("com.sun.xml.internal.bind.c14n",true);
			
			NamespacePrefixMapperUtils.setProperty(marshaller, namespacePrefixMapper);
			
			log.debug("marshalling " + this.getClass().getName() );	
			getContents();
//			if (jaxbElement==null) {
//				log.error("No JAXBElement has been created for this part, yet!");
//				throw new JAXBException("No JAXBElement has been created for this part, yet!");
//			}
	    	setMceIgnorable( (McIgnorableNamespaceDeclarator) namespacePrefixMapper);
	    	
	    	String mceIgnorable = "";
	    	if (this.getMceIgnorable()!=null) {
	    		mceIgnorable = this.getMceIgnorable();
	    	}
	    	
			if (Docx4jProperties.getProperty("docx4j.jaxb.marshal.canonicalize", false)) {
				
				// We currently canonicalize twice:
				// 1. trim namespaces
				// 2. add mcIgnorable
	    		
	    		Document doc = XmlUtils.marshaltoW3CDomDocument(jaxbElement, jc); // NB that code trims namespaces

	    		// Now, we need to add back in the mcIgnorable ones
	    		NamespacePrefixMapperUtils.declareNamespaces(mceIgnorable + getMcChoiceNamespaces(), doc);
	    		/* that generalises the following:
	    		if (this.getMceIgnorable().contains("w15")) {
		    		doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/" ,
		    				"xmlns:w15", "http://schemas.microsoft.com/office/word/2012/wordml");
	    			
	    		}
	    		*/
	    		
	    		log.debug("Input to Canonicalizer: " + XmlUtils.w3CDomNodeToString(doc));
	    		
	    		Init.init();
	    		Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE); 
	    		/* EXCLUSIVE works, with 
	    		
						byte[] bytes = c.canonicalizeSubtree(doc, this.getMceIgnorable());
						
				   EXCLUSIVE does not work, if you use	    	
				   
				   		c.canonicalizeSubtree(doc, null) or 
				   		c.canonicalizeSubtree(doc, null)
				   		
				   since org.docx4j.org.apache.xml.security.c14n.Canonicalizer then pushes w15 etc down the tree
				   
				   INCLUSIVE works c.canonicalizeSubtree(doc)
				   
				   In effect, the choice between EXCLUSIVE and INCLUSIVE comes down to
				   the handling of attributes in the XML namespace, such as xml:lang and xml:space.
				   
				   TODO revisit which is preferable.
				*/
	    		
	    		
	    		log.debug( "canonicalizeSubtree with inclusiveNamespaces {}, {}", this.getMceIgnorable(), getMcChoiceNamespaces() );
	    		
	    		byte[] bytes = c.canonicalizeSubtree(doc, this.getMceIgnorable()  + getMcChoiceNamespaces());
	    		//byte[] bytes = c.canonicalizeSubtree(doc); // for INCLUSIVE
	    		
	    		
	    		IOUtils.write(bytes, os);
	    		
	    	} else if (!Docx4jProperties.getProperty(
	    			"docx4j.openpackaging.parts.JaxbXmlPart.MarshalToOutputStreamViaXMLStreamWriter", false)){
	    		// The default (except in v6.0.x)
	    		log.debug("Marshalling to " + os.getClass().getName());
	    		
				((McIgnorableNamespaceDeclarator) namespacePrefixMapper).setMcIgnorable(
						mceIgnorable + getMcChoiceNamespaces());  
				
				if (this instanceof MainDocumentPart) {
					
					OutputStream filteredOS;
					
//					if (os instanceof org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream) {
						
						BufferedOutputStream buffered = new BufferedOutputStream(os);
						
						boolean isNew = (this.getPackage()==null) ? true : this.getPackage().isNew(); // handle edge cases eg unit test where there is no package 
						
						filteredOS = 
								new MainDocumentPartFilterOutputStream(
										buffered, 
										isNew); // BufferedOutputStream helps a lot here
						
						marshaller.marshal(jaxbElement, filteredOS);
						
						buffered.flush(); // Critical for Sun/Oracle JAXB in 1.8.0_181, otherwise there is content loss at the end of the part 
						
//					} else {
//						filteredOS = new MainDocumentPartFilterOutputStream(os, this.getPackage().isNew());
//							// Avoid BufferedOutputStream, especially where Sun/Oracle JAXB is writing to a ByteArrayOutputStream
//							// which can prevent content from appearing
//						marshaller.marshal(jaxbElement, filteredOS);
//					}
					
					
				} else {
					// no huge need for BufferedOutputStream; MOXy may be a little faster; Sun/Oracle a little slower
					// Would require further testing
//					BufferedOutputStream buffered = new BufferedOutputStream(os);
//					marshaller.marshal(jaxbElement, buffered); 
//					buffered.flush();
					
					marshaller.marshal(jaxbElement, os); 
					
				}
	    		
	    	} else {
	    		// Typically unused in v6.1.0
	    		log.debug("MarshalToOutputStreamViaXMLStreamWriter set; " + os.getClass().getName());

	    		// Possible extension point: let user use their own XMLStreamWriter	    		
	    		
	    		XMLOutputFactory xof = XMLOutputFactory.newFactory();
	            XMLStreamWriter xsw = xof.createXMLStreamWriter(os, "UTF-8");
	            
	            // get rid of xmlns="" which com.sun.xml.internal.stream.writers.XMLStreamWriterImpl writes
	            XMLStreamWriterWrapper xsww = null;
	            
	            // An XMLStreamWriter is normally unable to produce empty elements,
	            // but ours do.
				if (Docx4jProperties.getProperty("docx4j.jaxb.formatted.output", true)) {
					xsww = new XMLStreamWriterWrapperIndenting(this, xsw); 
				} else {
					xsww = new XMLStreamWriterWrapper(this, xsw); 
				}
				// It would also be possible to give user the choice between using
				// empty-element (aka self-closing) tags or not, but there has
				// been no demand for HTML style no empty-elements

	            marshaller.setListener(new Docx4jMarshallerListener(xsww, this.getPackage().isNew()));
	            
	    		// Word requires certain namespaces to be written at the document level;
	            // these are the ones used in the top level ignorable attribute, plus any
	            // used in attribute values in the mc elements themselves.
	            // JAXB reference implementation and MOXy do this
	            // with namespace handler's getPreDeclaredNamespaceUris 
	            
				((McIgnorableNamespaceDeclarator) namespacePrefixMapper).setMcIgnorable(mceIgnorable + getMcChoiceNamespaces());
	            // If necessary, we could do it in our streamwriter; there is some 
	            // code in git history to do that 
//	    		xsww.setIgnorableNamespaces(mceIgnorable + getMcChoiceNamespaces());
	            
	            marshaller.marshal(jaxbElement, xsww);
	            
	            xsww.close();
	            xsw.close();
	    	}
	    	
			// Now unset it - prob not nec
			((McIgnorableNamespaceDeclarator) namespacePrefixMapper).setMcIgnorable(null);
	    	

		} catch (Docx4JException e) {
			throw new JAXBException(e);  // avoid change to method signature
		} catch (Exception e) {
			throw new JAXBException(e);  // avoid change to method signature
		}
	}
    
    public String getMceIgnorable() {
    	return "";
    }
    
    /**
     * Where the mc:Ignorable attribute is present,
     * ensure its contents matches the ignorable namespaces
     * actually present.
     */
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {
    /*	
    	ECMA-376, Office Open XML File Formats, Part 3 deals with "Markup Compatibility and Extensibility", 
    	and specifies mc:Ignorable.

    	@mc:Ignorable, for example, in:

	    	<Circles xmlns="http://schemas.openxmlformats.org/Circles/v1"
	    	xmlns:mc="http://schemas.openxmlformats.org/markupcompatibility/
	    	2006"
	    	xmlns:v2="http://schemas.openxmlformats.org/Circles/v2"
	    	xmlns:v3="http://schemas.openxmlformats.org/Circles/v3"
	    	mc:Ignorable="v2 v3">
	    	:

    	is a whitespace-delimited list of namespace prefixes, where each namespace prefix identifies an 
    	ignorable namespace.

    	JAXB might not include a namespace declaration using prefix "v2", unless that namespace is required 
    	in the resulting XML document.

    	The problem is that if "v2" is included in the value of @mc:Ignorable, and there is no declaration 
    	of that prefix, then Microsoft Word 2010 and 2013 will report the document to be corrupt. (Powerpoint 2010
    	is the same)

    	So the challenge is, when marshalling, how to populate the mc:Ignorable attribute, and guarantee 
    	a matching set of namespace declarations will be present.
    	
    	Suppose I have a set of ignorable prefixes (known a priori), some or all of which are used in the
    	XML document. I want to either set the value of @mc:ignorable to the ignorable prefixes actually 
    	used in the XML document (in which case JAXB will provide a matching set of namespace declarations), 
    	or set the value of @mc:ignorable to the entire set of ignorable prefixes and force JAXB to declare 
    	each of those prefixes. 
    	
    	Either approach is OK. In the Document Settings part, we use the first approach.  In the Main 
    	Document Part, we use a hybrid approach.
    */	    	
	}

	/**
	 * Prefixes specified in an mc:Choice element. These must be declared, or Word
	 * can't open the docx. They are collected by Docx4jUnmarshallerListener
	 * 
	 * @since 6.0.0
	 */
	private Set<String> mcChoiceNamespaces = new HashSet<String>();

	public String getMcChoiceNamespaces() {
		
		if (mcChoiceNamespaces.isEmpty() ) return "";
		
		StringBuilder sb = new StringBuilder();
		for (String s : mcChoiceNamespaces) {
			sb.append(" " + s);
		}
		return sb.toString();
	}

	public void addMcChoiceNamespace(String mcChoiceNamespace) {
		this.mcChoiceNamespaces.add(mcChoiceNamespace);
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
			/* To avoid possible XML External Entity Injection attack,
			 * we need to configure the processor.
			 * 
			 * We use STAX XMLInputFactory to do that.
			 * 
			 * createXMLStreamReader(is) is 40% slower than unmarshal(is).
			 * 
			 * But it seems to be the best we can do ... 
			 * 
			 *   org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(is)
			 *   unmarshal(doc)
			 * 
			 * ie DOM is 5x slower than unmarshal(is)
			 * 
			 */
	        XMLInputFactory xif = XMLInputFactory.newInstance();
	        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
	        XMLStreamReader xsr = xif.createXMLStreamReader(is);			
		    
			Unmarshaller u = jc.createUnmarshaller();
			
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
//			if (is.markSupported()) {
//				// Only fail hard if we know we can restart
//				eventHandler.setContinue(false);
//			}
			u.setEventHandler(eventHandler);
			
			Unmarshaller.Listener docx4jUnmarshallerListener = new Docx4jUnmarshallerListener(this);
			u.setListener(docx4jUnmarshallerListener);
						
			try {
				jaxbElement = (E) XmlUtils.unwrap(
						u.unmarshal( xsr ));						
			} catch (UnmarshalException ue) {
				
				if (ue.getLinkedException()!=null 
						&& ue.getLinkedException().getMessage().contains("entity")) {
					
					/*
						Caused by: javax.xml.stream.XMLStreamException: ParseError at [row,col]:[10,19]
						Message: The entity "xxe" was referenced, but not declared.
							at com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl.next(Unknown Source)
							at com.sun.xml.internal.bind.v2.runtime.unmarshaller.StAXStreamConnector.bridge(Unknown Source)
						 */
					throw ue;
				}
				
				if (is.markSupported() ) {
					// When reading from zip, we use a ByteArrayInputStream,
					// which does support this.
				
					log.info("encountered unexpected content; pre-processing");
					eventHandler.setContinue(true);
										
					try {
						Templates mcPreprocessorXslt = JaxbValidationEventHandler.getMcPreprocessor();
						is.reset();
						xsr = xif.createXMLStreamReader(is);
						JAXBResult result = XmlUtils.prepareJAXBResult(jc);
						XmlUtils.transform(new StAXSource(xsr), 
								mcPreprocessorXslt, null, result);
						jaxbElement = (E) XmlUtils.unwrap(
								result.getResult() );	
					} catch (Exception e) {
						throw new JAXBException("Preprocessing exception", e);
					}
											
				} else {
					throw new UnmarshalException("Mark not supported",ue);
				}
			}
			

		} catch (XMLStreamException e1) {
			throw new JAXBException(e1);
		}
		return jaxbElement;
    	
    }	
    
    public E unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {

			Unmarshaller u = jc.createUnmarshaller();
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			u.setEventHandler(eventHandler);
			
			Unmarshaller.Listener docx4jUnmarshallerListener = new Docx4jUnmarshallerListener(this);
			u.setListener(docx4jUnmarshallerListener);
			
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
			log.error(e.getMessage(), e);
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
