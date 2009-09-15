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


package org.docx4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
	
	private static Logger log = Logger.getLogger(XmlUtils.class);	
		
	/** Make a dom4j element into something JAXB can unmarshall */
	private static java.io.InputStream getInputStreamFromDom4jEl(Element el) {
		
		// Write it to an output stream
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    	OutputFormat format = OutputFormat.createPrettyPrint();
	    try {
		    XMLWriter xmlWriter = new XMLWriter( out, format );
		    xmlWriter.write(el);
		    xmlWriter.flush();
	    } catch (IOException e) {
			e.printStackTrace();
			return null;
		}   	    
		
	    byte[] bytes = out.toByteArray();
	    
		// Now return an input stream
	    return new java.io.ByteArrayInputStream(bytes);
		
	}
	
	public static String TRANSFORMER_FACTORY_ORIGINAL;
	public static String TRANSFORMER_FACTORY_SUPPORTING_EXTENSIONS;
	
	public static javax.xml.transform.TransformerFactory tfactory; 
	
	static {
		
		// Whenever we flush Preferences in a Swing application on Linux, 
		// < Java 6u10 RC (as of b23)
		// we'll get java.util.prefs.BackingStoreException: java.lang.IllegalArgumentException: Not supported: indent-number
		// if we are using our Xalan jar.
		// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6396599
		// So Swing applications will need to use the original 
		// setting, which we record for their convenience here.
		// eg com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
		// It would be nice to reset to the original whenever we finish
		// using, but that goal seems to be elusive!
		
		
		javax.xml.transform.TransformerFactory tmpfactory = javax.xml.transform.TransformerFactory.newInstance();			
		TRANSFORMER_FACTORY_ORIGINAL = tmpfactory.getClass().getName();
		tmpfactory = null;
		log.debug("Set TRANSFORMER_FACTORY_ORIGINAL to " + TRANSFORMER_FACTORY_ORIGINAL);
		
		// com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl won't
		// work with our extension functions.		
		TRANSFORMER_FACTORY_SUPPORTING_EXTENSIONS = "org.apache.xalan.processor.TransformerFactoryImpl";
		
		setTFactory();
		
    	// Crimson fails to parse the HTML XSLT, so use Xerces ..
		// .. this one is available in Java 6.		
		if (System.getProperty("java.version").startsWith("1.6")
				&& System.getProperty("java.vendor").startsWith("Sun") ) {
		
			System.setProperty("javax.xml.parsers.SAXParserFactory", 
			"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
	//		System.setProperty("javax.xml.parsers.SAXParserFactory",
	//				"org.apache.xerces.jaxp.SAXParserFactoryImpl");
			
			// Not needed
	//    	System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
	//				"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		
		} else {
			log.warn("Using default SAXParserFactory: " + System.getProperty("javax.xml.parsers.SAXParserFactory" ));
		}
		
	}
	
	private static void setTFactory() {
		
		System.setProperty("javax.xml.transform.TransformerFactory",
				TRANSFORMER_FACTORY_SUPPORTING_EXTENSIONS);
		tfactory = javax.xml.transform.TransformerFactory
				.newInstance();
		
		LoggingErrorListener errorListener = new LoggingErrorListener(false);
		tfactory.setErrorListener(errorListener);
		
		
		// We've got our factory now, so set it back again!
		System.setProperty("javax.xml.transform.TransformerFactory",
				TRANSFORMER_FACTORY_ORIGINAL);
		
	}
	

	/** Unmarshal a Dom4j element as JAXB object using  JAXBContext Context.jc */ 
	@Deprecated
	public static Object unmarshalDom4jDoc(org.dom4j.Document doc) {
				
		JAXBContext jc = Context.jc;
			
		Object o = null;
		try {				
		    org.dom4j.io.DOMWriter writer = new org.dom4j.io.DOMWriter();
			org.w3c.dom.Document w3cDoc = writer.write(doc);

//			JAXBContext jc = JAXBContext
//					.newInstance("org.docx4j.jaxb.document");
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			o = u.unmarshal( w3cDoc );

			System.out.println("unmarshalled ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return o;
	}
	
	/** Unmarshal a Dom4j element as an object in the package org.docx4j.jaxb.document.
	 * 
	 *  Since most of our types don't have an XmlRootElement, here we use
	 *  the version of the unmarshal method that takes the 'expectedType' argument.
	 *  See https://jaxb.dev.java.net/guide/_XmlRootElement_and_unmarshalling.html  
	 *  
	 *  */ 
	@Deprecated
	public static  <T> JAXBElement<T> unmarshalDom4jEl(Element el, Class<T> declaredType) {
		
		JAXBElement<T> o = null;
		//Object o = null;
		try {	

			JAXBContext jc = Context.jc;

			Unmarshaller u = jc.createUnmarshaller();
						
			//u.setSchema(null);
			//u.setValidating( false );
			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

//			// Convert dom4j el to W3C
//			org.dom4j.io.DOMWriter writer = new org.dom4j.io.DOMWriter();
//			org.w3c.dom.Document w3cDoc = writer.write(el);  // Only takes Document objects :(
						
			o = u.unmarshal(new StreamSource(org.docx4j.XmlUtils.getInputStreamFromDom4jEl(el)), declaredType);

			System.out.println("unmarshalled ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return o;
	}

	/** Unmarshal a String as an object in the package org.docx4j.jaxb.document */ 
	public static Object unmarshalString(String str) {		
		return unmarshalString(str, Context.jc);
	}

	public static Object unmarshalString(String str, JAXBContext jc) {
		Object o = null;
		try {				
			
			Unmarshaller u = jc.createUnmarshaller();
						
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			o = u.unmarshal( new javax.xml.transform.stream.StreamSource(
					new java.io.StringReader(str)) );

			System.out.println("unmarshalled ");

		} catch (Exception ex) {
			log.error("Caught and ignored: \n" + ex);
		}		
		return o;
	}

	public static Object unmarshal(Node n) throws JAXBException {
			
		Unmarshaller u = Context.jc.createUnmarshaller();					
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		return u.unmarshal( n );
	}
	
	/** Marshal to a Dom4j document */ 
	@Deprecated
	public static org.dom4j.Document marshaltoDom4jDocument(Object o) {
		// TODO - refactor this.
		try {
			JAXBContext jc = Context.jc;

			Marshaller marshaller=jc.createMarshaller();
			
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			marshaller.marshal(o, doc);
			
			// Now convert the W3C document to a dom4j document
			org.dom4j.io.DOMReader xmlReader = new org.dom4j.io.DOMReader();
			
			/*  Should be able to do ..
			 * 
			 *  dom4j has DocumentResult that extends Result, so you can do:

				DocumentResult dr = new DocumentResult();
				marshaller.marshal( object, dr );
				o = dr.getDocument();

			 * 
			 * 
			 */
		    return xmlReader.read(doc);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;				
	}

	/** Marshal to a String */ 
	public static String marshaltoString(Object o, boolean suppressDeclaration ) {

		JAXBContext jc = Context.jc;
		return marshaltoString(o, suppressDeclaration, false, jc );
	}

	/** Marshal to a String */ 
	public static String marshaltoString(Object o, boolean suppressDeclaration, JAXBContext jc ) {

		return marshaltoString(o, suppressDeclaration, false, jc );

	}
	
	/** Marshal to a String */ 
	public static String marshaltoString(Object o, boolean suppressDeclaration, boolean prettyprint ) {

		JAXBContext jc = Context.jc;
		return marshaltoString(o, suppressDeclaration, prettyprint, jc );
	}
	
	/** Marshal to a String */ 
	public static String marshaltoString(Object o, boolean suppressDeclaration, boolean prettyprint, JAXBContext jc ) {
				
		/* http://weblogs.java.net/blog/kohsuke/archive/2005/10/101_ways_to_mar.html
		 * 
		 * If you are writing to a file, a socket, or memory, then you should use
		 * the version that takes OutputStream. Unless you change the target 
		 * encoding to something else (default is UTF-8), there's a special 
		 * marshaller codepath for OutputStream, which makes it run really fast.
		 * You also don't have to use BufferedOutputStream, since the JAXB RI 
		 * does the adequate buffering.
		 * 
		 * You can also write to Writer, but in this case you'll be responsible 
		 * for encoding characters, so in general you need to be careful. If 
		 * you want to marshal XML into an encoding other than UTF-8, it's best
		 *  to use the JAXB_ENCODING property and then write to OutputStream, 
		 *  as it escapes characters to things like &#x1824; correctly. 
		 */
		
		try {			
			Marshaller m=jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(m, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			
			if (prettyprint) {
				m.setProperty("jaxb.formatted.output", true);
			}
			
			/* Fix for:
			 * 		<t tstamp='1198193417585' snum='1' op='update'>
			 * ~~~~~~~>	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
			 * 				<ns1:sdt xmlns:ns1="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
			 * 					<ns1:sdtPr><ns1:id ns1:val="1814108031"/><ns1:tag ns1:val="1"/></ns1:sdtPr><ns1:sdtContent><ns1:p><ns1:r><ns1:t>Lookin</ns1:t></ns1:r><ns1:r><ns1:t> pretty</ns1:t></ns1:r></ns1:p></ns1:sdtContent></ns1:sdt></t>
			 */
			
			/* http://weblogs.java.net/blog/kohsuke/archive/2005/10/101_ways_to_mar.html
			 * 
			 * JAXB_FRAGMENT prevents the marshaller from producing an XML declaration, 
			 * so the above works just fine. The downside of this approach is that if 
			 * the ancestor elements declare the namespaces, JAXB won't be able to take 
			 * advantage of them.
			 */
			
			if (suppressDeclaration) {
				m.setProperty(Marshaller.JAXB_FRAGMENT,true);
			}			
			
			StringWriter sWriter = new StringWriter();
			m.marshal(o, sWriter);
			return sWriter.toString();
			
/*          Alternative implementation
 
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();			
			marshaller.marshal(o, out);			
		    byte[] bytes = out.toByteArray();
		    return new String(bytes);
 */	
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;				
	}

	public static java.io.InputStream marshaltoInputStream(Object o, boolean suppressDeclaration, JAXBContext jc ) {
		
		/* http://weblogs.java.net/blog/kohsuke/archive/2005/10/101_ways_to_mar.html
		 * 
		 * If you are writing to a file, a socket, or memory, then you should use
		 * the version that takes OutputStream. Unless you change the target 
		 * encoding to something else (default is UTF-8), there's a special 
		 * marshaller codepath for OutputStream, which makes it run really fast.
		 * You also don't have to use BufferedOutputStream, since the JAXB RI 
		 * does the adequate buffering.
		 * 
		 */
		
		try {			
			Marshaller m=jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(m, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
						
			if (suppressDeclaration) {
				m.setProperty(Marshaller.JAXB_FRAGMENT,true);
			}
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.marshal(o, os);			

			// Now copy from the outputstream to inputstream
			// See http://ostermiller.org/convert_java_outputstream_inputstream.html
			
			return new java.io.ByteArrayInputStream(os.toByteArray());
			
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;				
	}

	
	
	/** Marshal to a W3C document */ 
	public static org.w3c.dom.Document marshaltoW3CDomDocument(Object o) {

		return marshaltoW3CDomDocument(o, Context.jc);
	}

	/** Marshal to a W3C document */
	public static org.w3c.dom.Document marshaltoW3CDomDocument(Object o, JAXBContext jc) {
		// TODO - refactor this.
		try {

			Marshaller marshaller = jc.createMarshaller();

			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
					.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();

			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			

			marshaller.marshal(o, doc);

			return doc;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** Clone this JAXB object, using default JAXBContext. */ 
	public static <T> T deepCopy(T value) {		
		return deepCopy(value, Context.jc);		
	}
	
	/** Clone this JAXB object
	 * @param value
	 * @param jc
	 * @return
	 */
	public static <T> T deepCopy(T value, JAXBContext jc) {
		try {
			JAXBElement<?> elem;
			Class<?> valueClass;
			if (value instanceof JAXBElement<?>) {
				log.debug("deep copy of JAXBElement..");
				elem = (JAXBElement<?>) value;
				valueClass = elem.getDeclaredType();
			} else {
				log.debug("deep copy of " + value.getClass().getName() );
				@SuppressWarnings("unchecked")
				Class<T> classT = (Class<T>) value.getClass();
				elem = new JAXBElement<T>(new QName("temp"), classT, value);
				valueClass = classT;
			}

			Marshaller mar = Context.jc.createMarshaller();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(256);
			mar.marshal(elem, bout);

			Unmarshaller unmar = Context.jc.createUnmarshaller();
			elem = unmar.unmarshal(new StreamSource(new ByteArrayInputStream(
					bout.toByteArray())), valueClass);

			T res;
			if (value instanceof JAXBElement<?>) {
				@SuppressWarnings("unchecked")
				T resT = (T) elem;
				res = resT;
			} else {
				@SuppressWarnings("unchecked")
				T resT = (T) elem.getValue();
				res = resT;
			}
			log.info("deep copy success!");
			return res;
		} catch (JAXBException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	/** Use DocumentBuilderFactory to create and return a new w3c dom Document. */ 
	public static org.w3c.dom.Document neww3cDomDocument() {
		
		try {
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
		
	}

    
    public static void transform(org.w3c.dom.Document doc,
    		javax.xml.transform.Templates template, 
			  Map<String, Object> transformParameters, 
			  javax.xml.transform.Result result) throws Exception {

    	if (doc == null ) {
    		Throwable t = new Throwable();
    		throw new Docx4JException( "Null DOM Doc", t);
    	}
    	
		javax.xml.transform.dom.DOMSource domSource = new javax.xml.transform.dom.DOMSource(doc);
		
    	transform(domSource,
    			template, 
    			 transformParameters, 
    			 result);
    }
    

    public static Templates getTransformerTemplate(
			  javax.xml.transform.Source xsltSource) throws TransformerConfigurationException {
    	    
    	return tfactory.newTemplates(xsltSource);
    }    
    
    /**
     * 
     * Transform an input document using XSLT
     * 
     * @param doc
     * @param xslt
     * @param transformParameters
     * @param result
     * @throws Exception
     */
    public static void transform(javax.xml.transform.Source source,
    					  javax.xml.transform.Templates template, 
    					  Map<String, Object> transformParameters, 
    					  javax.xml.transform.Result result) throws Exception {
    	
    	if (source == null ) {
    		Throwable t = new Throwable();
    		throw new Docx4JException( "Null Source doc", t);
    	}
		
		// Use the template to create a transformer
		// A Transformer may not be used in multiple threads running concurrently. 
		// Different Transformers may be used concurrently by different threads.
		// A Transformer may be used multiple times. Parameters and output properties 
		// are preserved across transformations.		
		javax.xml.transform.Transformer xformer = template.newTransformer();
		if (!xformer.getClass().getName().equals(
				"org.apache.xalan.transformer.TransformerImpl")) {
			log
					.error("Detected "
							+ xformer.getClass().getName()
							+ ", but require org.apache.xalan.transformer.TransformerImpl. "
							+ "Ensure Xalan 2.7.0 is on your classpath!");
		}
		LoggingErrorListener errorListener = new LoggingErrorListener(false);
		xformer.setErrorListener(errorListener);

		if (transformParameters != null) {
			Iterator parameterIterator = transformParameters.entrySet()
					.iterator();
			while (parameterIterator.hasNext()) {
				Map.Entry pairs = (Map.Entry) parameterIterator.next();

				if (pairs.getKey() == null) {
					log.info("Skipped null key");
					// pairs = (Map.Entry)parameterIterator.next();
					continue;
				}

				if (pairs.getValue() == null) {
					log.warn("parameter '" + pairs.getKey() + "' was null.");
				} else {
					xformer.setParameter((String) pairs.getKey(), pairs
							.getValue());
				}
			}
		}

		// DEBUGGING
		// use the identity transform if you want to send wordDocument;
		// otherwise you'll get the XHTML
		// javax.xml.transform.Transformer xformer = tfactory.newTransformer();

		xformer.transform(source, result);
    	
    }
   
     /**
		 * Give a string of wml containing ${key1}, ${key2}, return a suitable
		 * object.
		 * 
		 * @param wmlTemplateString
		 * @param mappings
		 * @return
		 */
    public static Object unmarshallFromTemplate(String wmlTemplateString, java.util.HashMap<String, String> mappings) {
        return unmarshallFromTemplate(wmlTemplateString, mappings, Context.jc);
     }

    public static Object unmarshallFromTemplate(String wmlTemplateString, 
    		java.util.HashMap<String, String> mappings, JAXBContext jc) {
        String wmlString = replace(wmlTemplateString, 0, new StringBuilder(), mappings).toString();
        log.debug("Results of substitution: " + wmlString);
        return unmarshalString(wmlString, jc);
     }

     private static StringBuilder replace(String s, int offset, StringBuilder b, java.util.HashMap<String, String> mappings) {
        int startKey = s.indexOf("${", offset);
        if (startKey == -1)
           return b.append(s.substring(offset));
        else {
           b.append(s.substring(offset, startKey));
           int keyEnd = s.indexOf('}', startKey);
           String key = s.substring(startKey + 2, keyEnd);
           b.append( mappings.get(key) );
           return replace(s, keyEnd + 1, b, mappings);
        }
     }

	
     public static String w3CDomNodeToString(Node n) {
    	 
 		// Why doesn't Java have a nice neat way of getting 
 		// the XML as a String??  
    	 		
 		StringWriter sw = new StringWriter();
 		try {
				Transformer serializer = tfactory.newTransformer();
				serializer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
				serializer.transform( new DOMSource(n) , new StreamResult(sw) );				
				return sw.toString();
				//log.debug("serialised:" + n);
			} catch (Exception e) {
				// Unexpected!
				e.printStackTrace();
				return null;
			} 
     }
	
     static class LoggingErrorListener implements ErrorListener {
    	 
    	 // See http://www.cafeconleche.org/slides/sd2003west/xmlandjava/346.html
    	  
    	 boolean strict;
    	 
    	  public LoggingErrorListener(boolean strict) {
    	  }
    	  
    	  public void warning(TransformerException exception) {
    	   
    	    log.warn(exception.getMessage(), exception);
    	   
    	    // Don't throw an exception and stop the processor
    	    // just for a warning; but do log the problem
    	  }
    	  
    	  public void error(TransformerException exception)
    	   throws TransformerException {
    	    
      	    log.error(exception.getMessage(), exception);
    	    
      	    // XSLT is not as draconian as XML. There are numerous errors
    	    // which the processor may but does not have to recover from; 
    	    // e.g. multiple templates that match a node with the same
    	    // priority. If I do not want to allow that,  I'd throw this 
    	    // exception here.
      	    if (strict) {
      	    	throw exception;
      	    }
    	    
    	  }
    	  
    	  public void fatalError(TransformerException exception)
    	   throws TransformerException {
    	    
       	    log.error(exception.getMessage(), exception);

    	    // This is an error which the processor cannot recover from; 
    	    // e.g. a malformed stylesheet or input document
    	    // so I must throw this exception here.
    	    throw exception;
    	    
    	  }
    	     
    	}

 	public static void treeCopy( NodeList sourceNodes, Node destParent ) { 		
        for (int i=0; i<sourceNodes.getLength(); i++) {
        	treeCopy((Node)sourceNodes.item(i), destParent);
        } 		
 	}
     
     
	public static void treeCopy( Node sourceNode, Node destParent ) {
		// source node maybe org.apache.xml.dtm.ref.DTMNodeProxy
		// (if its xslt output we are copying)
		// or com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
		// (if its marshalled JAXB)
	    	
    	//log.debug("node type" + sourceNode.getNodeType());
    	
        switch (sourceNode.getNodeType() ) {

        	case Node.DOCUMENT_NODE: // type 9
        
                // recurse on each child
                NodeList nodes = sourceNode.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                    	//treeCopy((DTMNodeProxy)nodes.item(i), destParent);
                    	treeCopy((Node)nodes.item(i), destParent);
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                
                // Copy of the node itself
        		//log.debug("copying: " + sourceNode.getNodeName() );
        		Node newChild;
        		if ( destParent instanceof Document ) {
        			newChild = ((Document)destParent).createElementNS(
        				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );
        		} else {
        			newChild = destParent.getOwnerDocument().createElementNS(
            				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );	            			
        		}
        		destParent.appendChild(newChild);
        		
        		// .. its attributes
            	NamedNodeMap atts = sourceNode.getAttributes();
            	for (int i = 0 ; i < atts.getLength() ; i++ ) {
            		
            		Attr attr = (Attr)atts.item(i);
            		
//            		log.debug("attr.getNodeName(): " + attr.getNodeName());
//            		log.debug("attr.getNamespaceURI(): " + attr.getNamespaceURI());
//            		log.debug("attr.getLocalName(): " + attr.getLocalName());
//            		log.debug("attr.getPrefix(): " + attr.getPrefix());
            		
            		if ( attr.getNodeName().startsWith("xmlns:")) {
            			/* A document created from a dom4j document using dom4j 1.6.1's io.domWriter
                			does this ?!
                			attr.getNodeName(): xmlns:w 
                			attr.getNamespaceURI(): null
                			attr.getLocalName(): null
                			attr.getPrefix(): null
                			
                			unless i'm doing something wrong, this is another reason to
                			remove use of dom4j from docx4j
            			*/ 
                		; 
                		// this is a namespace declaration. not our problem
            		} else if (attr.getNamespaceURI()==null) {
                		//log.debug("attr.getLocalName(): " + attr.getLocalName() + "=" + attr.getValue());
            			((org.w3c.dom.Element)newChild).setAttribute(
                				attr.getLocalName(), attr.getValue() );
            		} else if ( attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) {
                		; // this is a namespace declaration. not our problem
            		} else  {
                		((org.w3c.dom.Element)newChild).setAttributeNS(attr.getNamespaceURI(), 
                				attr.getLocalName(), attr.getValue() );	                			
            		}
            	}

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                if (children != null) {
                    for (int i=0; i<children.getLength(); i++) {
                    	//treeCopy( (DTMNodeProxy)children.item(i), newChild);
                    	treeCopy( (Node)children.item(i), newChild);
                    }
                }

                break;

            case Node.TEXT_NODE:
            	Node textNode = destParent.getOwnerDocument().createTextNode(sourceNode.getNodeValue());       
            	destParent.appendChild(textNode);
                break;

//                case Node.CDATA_SECTION_NODE:
//                    writer.write("<![CDATA[" +
//                                 node.getNodeValue() + "]]>");
//                    break;
//
//                case Node.COMMENT_NODE:
//                    writer.write(indentLevel + "<!-- " +
//                                 node.getNodeValue() + " -->");
//                    writer.write(lineSeparator);
//                    break;
//
//                case Node.PROCESSING_INSTRUCTION_NODE:
//                    writer.write("<?" + node.getNodeName() +
//                                 " " + node.getNodeValue() +
//                                 "?>");
//                    writer.write(lineSeparator);
//                    break;
//
//                case Node.ENTITY_REFERENCE_NODE:
//                    writer.write("&" + node.getNodeName() + ";");
//                    break;
//
//                case Node.DOCUMENT_TYPE_NODE:
//                    DocumentType docType = (DocumentType)node;
//                    writer.write("<!DOCTYPE " + docType.getName());
//                    if (docType.getPublicId() != null)  {
//                        System.out.print(" PUBLIC \"" +
//                            docType.getPublicId() + "\" ");
//                    } else {
//                        writer.write(" SYSTEM ");
//                    }
//                    writer.write("\"" + docType.getSystemId() + "\">");
//                    writer.write(lineSeparator);
//                    break;
        }
    }
	
}

/*		
 * 		We want to use plain old Xalan J, not xsltc
 * 
 * 		Following would not be necessary provided Xalan is on the classpath
 * 
		System.setProperty("javax.xml.transform.TransformerFactory", "FQCN");

		examples of FQCN:
		
		  org.apache.xalan.processor.TransformerFactoryImpl (this is the one we want)
		  com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
		  org.apache.xalan.xsltc.trax.TransformerFactoryImpl
		  net.sf.saxon.TransformerFactoryImpl
		  
		HOWEVER, docx4all encounters http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6396599
		
			java.util.prefs.FileSystemPreferences syncWorld
			WARNING: Couldn't flush user prefs: java.util.prefs.BackingStoreException: java.lang.IllegalArgumentException: Not supported: indent-number
		
		every 30 seconds (on Linux, with JDK < Java 6u10 RC (as of b23)

		The workaround implemented is to remove META-INF/services from the xalan jar 
		to prevent xalan being picked up as the default provider for jaxp transform,
		so we have to use it explicitly.
		
		.. which means 

			System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");

		  (unfortunately, there is no com.sun.org.apache.xalan.processor.TransformerFactoryImpl,
		   so we have to bundle xalan jar, which is 2.7 MB
		   
		   But we can make it smaller:
		   
		   	org/apache/xalan/lib$ rm sql -rf
		    org/apache/xalan$ rm xsltc -rf

          That gets us from 2.7 MB to 1.85 MB.
          
          Sun already has:
          
			com.sun.org.apache.xpath;			
			com.sun.org.apache.xml.internal.dtm;			
			com.sun.org.apache.xalan.internal.extensions|lib|res
		   
		  so you might think we can refactor Xalan to point to those, and them out of our jar.
		  
		  well, it turns out that its too messy leaving out org.apache.xpath or xalan.extensions	 
		  
		  so you have to keep xalan.extensions, processor, serialize, trace, transformer

		  leaving out just org.apache.xalan.resources and org.apache.xpath.resources
		  only gets us down to 1.5 MB. (and that's with just jar cvf xalan-minimal.jar org/apache/xalan org/apache/xpath
			- we'd still need to include org/apache/xml)
			
			ie once you include the whole of org.apache.xpath, you may as well just go with the 1.85 MB  :(
			
*/		
