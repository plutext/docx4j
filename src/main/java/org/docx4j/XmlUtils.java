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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.xalan.trace.PrintTraceListener;
import org.apache.xalan.trace.TraceManager;
import org.apache.xalan.transformer.TransformerImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {
	
	private static Logger log = Logger.getLogger(XmlUtils.class);	
		
	// See http://www.edankert.com/jaxpimplementations.html for
	// a helpful list.
		
	public static String TRANSFORMER_FACTORY_PROCESSOR_XALAN = "org.apache.xalan.processor.TransformerFactoryImpl";
	// TRANSFORMER_FACTORY_PROCESSOR_SUN .. JDK/JRE does not include anything like com.sun.org.apache.xalan.TransformerFactoryImpl
	
//	public static String TRANSFORMER_FACTORY_SAXON = "net.sf.saxon.TransformerFactoryImpl";

	// *.xsltc.trax.TransformerImpl don't
	// work with our extension functions in their current form.
	//public static String TRANSFORMER_FACTORY_XSLTC_XALAN = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
	//public static String TRANSFORMER_FACTORY_XSLTC_SUN = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
	
	private static javax.xml.transform.TransformerFactory transformerFactory; 
	/**
	 * @since 2.8.1
	 */
	public static TransformerFactory getTransformerFactory() {
		return transformerFactory;
	}

	private static DocumentBuilderFactory documentBuilderFactory;
	/**
	 * @since 2.8.1
	 * 
	 * TODO replace the various DocumentBuilderFactory.newInstance()
	 * throughout docx4j with a call to this.
	 */
	public static DocumentBuilderFactory getDocumentBuilderFactory() {
		return documentBuilderFactory;
	}
	
	static {
		// JAXP factories
		
		// javax.xml.transform.TransformerFactory
		instantiateTransformerFactory();
		
		log.debug( System.getProperty("java.vendor") );
		log.debug( System.getProperty("java.version") );
		
		// javax.xml.parsers.SAXParserFactory
		String sp = Docx4jProperties.getProperty("javax.xml.parsers.SAXParserFactory");
		if (sp!=null) {
			System.setProperty("javax.xml.parsers.SAXParserFactory",sp);
			log.info("Using " + sp + " (from docx4j.properties)");
		} 
		
		// CONSIDER using Xerces if present?
		//		System.setProperty("javax.xml.parsers.SAXParserFactory",
		//				"org.apache.xerces.jaxp.SAXParserFactoryImpl");
		
		
    	// Crimson fails to parse the HTML XSLT, so use Xerces ..
		// .. this one is available in Java 6.	
		// System.out.println(System.getProperty("java.vendor"));
		// System.out.println(System.getProperty("java.version"));
		else if ((System.getProperty("java.version").startsWith("1.6")
						&& System.getProperty("java.vendor").startsWith("Sun"))
				|| (System.getProperty("java.version").startsWith("1.7")
						&& System.getProperty("java.vendor").startsWith("Oracle"))) {
		
			System.setProperty("javax.xml.parsers.SAXParserFactory", 
					"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");

			log.info("Using com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
			
		} else {
			log.warn("Using default SAXParserFactory: " + System.getProperty("javax.xml.parsers.SAXParserFactory" ));
		}
		// Note that we don't restore the value to its original setting (unlike TransformerFactory),
		// since we want to avoid Crimson being used for the life of the application.

		// javax.xml.parsers.DocumentBuilderFactory

		// Crimson doesn't support setTextContent; this.writeDocument also fails.
		// We've already worked around the problem with setTextContent,
		// but rather than do the same for writeDocument,
		// let's just stop using it.

		String dbf = Docx4jProperties.getProperty("javax.xml.parsers.DocumentBuilderFactory");
		if (dbf!=null) {
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",dbf);
			log.info("Using " + dbf + " (from docx4j.properties)");
		} 
		
		// Consider using Xerces if present?  Not necessary...
		// System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
		//		"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		// which is what we used to do in XmlPart.
		else if ((System.getProperty("java.version").startsWith("1.6")
						&& System.getProperty("java.vendor").startsWith("Sun"))
				|| (System.getProperty("java.version").startsWith("1.7")
						&& System.getProperty("java.vendor").startsWith("Oracle"))) {
		
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
					"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

			log.info("Using com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
			
		} else {
			log.warn("Using default DocumentBuilderFactory: " 
					+ System.getProperty("javax.xml.parsers.DocumentBuilderFactory" ));
		}
		
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		// Note that we don't restore the value to its original setting (unlike TransformerFactory).
		// Maybe we could, if docx4j always used this documentBuilderFactory.
		
	}
	
	
	
	private static void instantiateTransformerFactory() {
		
		// docx4j requires real Xalan
		// See further docs/JAXP_TransformerFactory_XSLT_notes.txt
		String originalSystemProperty = System.getProperty("javax.xml.transform.TransformerFactory");
				
		try {
			System.setProperty("javax.xml.transform.TransformerFactory",
					TRANSFORMER_FACTORY_PROCESSOR_XALAN);
//					TRANSFORMER_FACTORY_SAXON);
			
			transformerFactory = javax.xml.transform.TransformerFactory
					.newInstance();
			
			// We've got our factory now, so set it back again!
			if (originalSystemProperty == null) {
				System.clearProperty("javax.xml.transform.TransformerFactory");
			} else {
				System.setProperty("javax.xml.transform.TransformerFactory",
						originalSystemProperty);
			}
		} catch (javax.xml.transform.TransformerFactoryConfigurationError e) {
			
			// Provider org.apache.xalan.processor.TransformerFactoryImpl not found
			log.error("Warning: Xalan jar missing from classpath; xslt not supported",e);
			
			// so try using whatever TransformerFactory is available
			if (originalSystemProperty == null) {
				System.clearProperty("javax.xml.transform.TransformerFactory");
			} else {
				System.setProperty("javax.xml.transform.TransformerFactory",
						originalSystemProperty);
			}
			
			transformerFactory = javax.xml.transform.TransformerFactory
			.newInstance();
		}
		
		LoggingErrorListener errorListener = new LoggingErrorListener(false);
		transformerFactory.setErrorListener(errorListener);
		
	}
	

	/**
	 * If an object is wrapped in a JAXBElement, return the object.
	 * Warning: be careful with this. If you are copying objects
	 * into your document (rather than just reading them), you'll
	 * probably want the object to remain wrapped (JAXB usually wraps them
	 * for a reason; without the wrapper, you'll (probably?) need an 
	 * @XmlRootElement annotation in order to be able to marshall ie save your
	 * document).
	 * 
	 * @param o
	 * @return
	 */
	public static Object unwrap(Object o) {
		
		if (o==null) return null;
		
		if (o instanceof javax.xml.bind.JAXBElement) {
			log.debug("Unwrapped " + ((JAXBElement)o).getDeclaredType().getName() );
			log.debug("name: " + ((JAXBElement)o).getName() );
			return ((JAXBElement)o).getValue();
		} else {
			return o;
		}
		
		/*
		 * Interestingly, DocumentSettingsPart (settings.xml) gets unwrapped,
		 * and CTSettings does not have @XmlRootElement, but this does
		 * not cause a problem when it is marshalled.
		 */
	}

//	public static Object unwrap(Object o, Class<?> x) {
//		
//		if (o instanceof x) {
//			
//		}
//		else if (o instanceof javax.xml.bind.JAXBElement) {
//			log.debug("Unwrapped " + ((JAXBElement)o).getDeclaredType().getName() );
//			return ((JAXBElement)o).getValue();
//		} else {
//			return o;
//		}
//	}

	
	public static String JAXBElementDebug(javax.xml.bind.JAXBElement o)  {
				
		String prefix = null;
		if (o.getName().getNamespaceURI()!=null) {
			try {
				prefix = NamespacePrefixMapperUtils.getPreferredPrefix(o.getName().getNamespaceURI(), null, false);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		if (prefix!=null) {
			return  prefix + ':' + o.getName().getLocalPart() 
				+ " is a javax.xml.bind.JAXBElement; it has declared type " 
				+ o.getDeclaredType().getName(); 
		} else {
			return  o.getName() + " is a javax.xml.bind.JAXBElement; it has declared type " 
				+ o.getDeclaredType().getName(); 			
		}
		
	}

	

	/** Unmarshal an InputStream as an object in the package org.docx4j.jaxb.document.
	 *  Note: you should ensure you include a namespace declaration for w: and
	 *  any other namespace in the xml string.
	 *  Also, the object you are attempting to unmarshall to might need to
	 *  have an @XmlRootElement annotation for things to work.  */ 
	public static Object unmarshal(InputStream is) throws JAXBException {	
		return unmarshal(is, Context.jc);
	}

	public static Object unmarshal(InputStream is, JAXBContext jc) throws JAXBException {
		Object o = null;
		Unmarshaller u = jc.createUnmarshaller();						
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
		o = u.unmarshal( is );
		return o;
	}
	
	
	/** Unmarshal a String as an object in the package org.docx4j.jaxb.document.
	 *  Note: you should ensure you include a namespace declaration for w: and
	 *  any other namespace in the xml string.
	 *  Also, the object you are attempting to unmarshall to might need to
	 *  have an @XmlRootElement annotation for things to work.  */ 
	public static Object unmarshalString(String str) throws JAXBException {		
		return unmarshalString(str, Context.jc);
	}
	
	public static Object unmarshalString(String str, JAXBContext jc, Class declaredType) throws JAXBException {		
		Unmarshaller u = jc.createUnmarshaller();						
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
		Object o = u.unmarshal( new javax.xml.transform.stream.StreamSource(new java.io.StringReader(str)),
				declaredType);
		if (o instanceof JAXBElement) {
			return ((JAXBElement)o).getValue();
		} else {
			return o;
		}
	}
	

	public static Object unmarshalString(String str, JAXBContext jc) throws JAXBException {
		log.debug("Unmarshalling '" + str + "'");			
		Unmarshaller u = jc.createUnmarshaller();						
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
		return u.unmarshal( new javax.xml.transform.stream.StreamSource(
				new java.io.StringReader(str)) );
	}

	public static Object unmarshal(Node n) throws JAXBException {
			
		Unmarshaller u = Context.jc.createUnmarshaller();					
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		return u.unmarshal( n );
	}

	public static Object unmarshal(Node n, JAXBContext jc, Class declaredType) throws JAXBException {
		
		// THIS DOESN"T WORK PROPERLY WITH 1.6.0.03 JAXB RI
		// (at least with CTTextParagraphProperties in 
		//  a Xalan org.apache.xml.dtm.ref.DTMNodeProxy)
		// but converting the Node to a String and
		// unmarshalling that is fine!
		
		Unmarshaller u = jc.createUnmarshaller();					
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
		Object o = u.unmarshal(n,
				declaredType);
		if ( o instanceof javax.xml.bind.JAXBElement) {
			return ((JAXBElement)o).getValue();
		} else {
			return o;
		}
	}
	
    /**
	 * Give a string of wml containing ${key1}, ${key2}, return a suitable
	 * object.
	 * 
	 * @param wmlTemplateString
	 * @param mappings
	 * @return
	 */
	public static Object unmarshallFromTemplate(String wmlTemplateString, 
			java.util.HashMap<String, String> mappings) throws JAXBException {
	    return unmarshallFromTemplate(wmlTemplateString, mappings, Context.jc);
	 }
	
	public static Object unmarshallFromTemplate(String wmlTemplateString, 
			java.util.HashMap<String, String> mappings, JAXBContext jc) throws JAXBException {
	    String wmlString = replace(wmlTemplateString, 0, new StringBuilder(), mappings).toString();
	    log.debug("Results of substitution: " + wmlString);
	    return unmarshalString(wmlString, jc);
	 }
	
	public static Object unmarshallFromTemplate(String wmlTemplateString, 
			java.util.HashMap<String, String> mappings, JAXBContext jc, Class<?> declaredType) throws JAXBException {
	      String wmlString = replace(wmlTemplateString, 0, new StringBuilder(), mappings).toString();
	      return unmarshalString(wmlString, jc, declaredType);
	   }
	
	
	 private static StringBuilder replace(String s, int offset, StringBuilder b, java.util.HashMap<String, String> mappings) {
	    int startKey = s.indexOf("${", offset);
	    if (startKey == -1)
	       return b.append(s.substring(offset));
	    else {
	       b.append(s.substring(offset, startKey));
	       int keyEnd = s.indexOf('}', startKey);
	       String key = s.substring(startKey + 2, keyEnd);
	       String val = mappings.get(key);
	       if (val==null) {
	    	   log.warn("Invalid key '" + key + "' or key not mapped to a value");
	    	   b.append(key );
	       } else {
	    	   b.append(val  );
	       }
	       return replace(s, keyEnd + 1, b, mappings);
	    }
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
	public static String marshaltoString(Object o, boolean suppressDeclaration, boolean prettyprint, 
			JAXBContext jc ) {
				
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
		
		if(o==null) {
			return null;			
		}
		
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
                    throw new RuntimeException(e);
		}
	}

	/** Marshal to a String, for object
	 *  missing an @XmlRootElement annotation.  */
	public static String marshaltoString(Object o, boolean suppressDeclaration, boolean prettyprint,
			JAXBContext jc,
			String uri, String local, Class declaredType) {
		// TODO - refactor this.
		try {

			Marshaller m = jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(m, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			
			if (prettyprint) {
				m.setProperty("jaxb.formatted.output", true);
			}
			
			if (suppressDeclaration) {
				m.setProperty(Marshaller.JAXB_FRAGMENT,true);
			}			
			
			StringWriter sWriter = new StringWriter();

			m.marshal( 
					new JAXBElement(new QName(uri,local), declaredType, o ),
					sWriter);

			return sWriter.toString();
			
		} catch (JAXBException e) {
                    throw new RuntimeException(e);
		} 
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
			throw new RuntimeException(e);
		}
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
		    throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
		    throw new RuntimeException(e);
		}
	}

	/** Marshal to a W3C document, for object
	 *  missing an @XmlRootElement annotation.  */
	public static org.w3c.dom.Document marshaltoW3CDomDocument(Object o, JAXBContext jc,
			String uri, String local, Class declaredType) {
		// TODO - refactor this.
		try {

			Marshaller marshaller = jc.createMarshaller();

			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
					.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();

			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			

			// See http://weblogs.java.net/blog/kohsuke/archive/2006/03/why_does_jaxb_p.html
			marshaller.marshal( 
					new JAXBElement(new QName(uri,local), declaredType, o ),
					doc);

			return doc;
		} catch (JAXBException e) {
		    throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
		    throw new RuntimeException(e);
		}
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
		
		if (value==null) {
			throw new IllegalArgumentException("Can't clone a null argument");
		}
		
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

			Marshaller mar = jc.createMarshaller();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(256);
			mar.marshal(elem, bout);

			Unmarshaller unmar = jc.createUnmarshaller();
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
//			log.info("deep copy success!");
			return res;
		} catch (JAXBException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	
    public static String w3CDomNodeToString(Node n) {
   	 
		// Why doesn't Java have a nice neat way of getting 
		// the XML as a String??  
   	 		
		StringWriter sw = new StringWriter();
		try {
				Transformer serializer = transformerFactory.newTransformer();
				serializer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
				//serializer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
				serializer.transform( new DOMSource(n) , new StreamResult(sw) );				
				return sw.toString();
				//log.debug("serialised:" + n);
			} catch (Exception e) {
				// Unexpected!
				e.printStackTrace();
				return null;
			} 
    }

	/** Use DocumentBuilderFactory to create and return a new w3c dom Document. */ 
	public static org.w3c.dom.Document neww3cDomDocument() {
		
		try {
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
		}		
		
	}
	
	  /**
	   * @param docBuilder
	   *          the parser
	   * @param parent
	   *          node to add fragment to
	   * @param fragment
	   *          a well formed XML fragment
	 * @throws ParserConfigurationException 
	   */
	public static void appendXmlFragment(Document document, Node parent, String fragment)
			throws IOException, SAXException, ParserConfigurationException {

		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		Node fragmentNode = docBuilder.parse(
				new InputSource(new StringReader(fragment)))
				.getDocumentElement();
		
		fragmentNode = document.importNode(fragmentNode, true);
		
		parent.appendChild(fragmentNode);
	}
	
	/**
	 * Prepare a JAXB transformation result for some given context.
	 * @param context The JAXB context.
	 * @return The result data structure created.
	 * @throws Docx4JException In case of configuration errors.
	 */
	public static JAXBResult prepareJAXBResult(final JAXBContext context)
			throws Docx4JException {

		final JAXBResult result;
		try {
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshaller.setEventHandler(new JaxbValidationEventHandler());
			result = new JAXBResult(unmarshaller);

		} catch (JAXBException e) {
			throw new Docx4JException("Error preparing empty JAXB result", e);
		}
		return result;
	}
    
    public static void transform(org.w3c.dom.Document doc,
    		javax.xml.transform.Templates template, 
			  Map<String, Object> transformParameters, 
			  javax.xml.transform.Result result) throws Docx4JException {

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
    	    
    	return transformerFactory.newTemplates(xsltSource);
    }    

    /**
     * 
     * Transform an input document using XSLT
     * 
     * @param doc
     * @param xslt
     * @param transformParameters
     * @param result
     * @throws Docx4JException In case serious transformation errors occur
     */
    public static void transform(javax.xml.transform.Source source,
    					  javax.xml.transform.Templates template, 
    					  Map<String, Object> transformParameters, 
    					  javax.xml.transform.Result result) throws Docx4JException {
    	
    	if (source == null ) {
    		Throwable t = new Throwable();
    		throw new Docx4JException( "Null Source doc", t);
    	}
		
		// Use the template to create a transformer
		// A Transformer may not be used in multiple threads running concurrently. 
		// Different Transformers may be used concurrently by different threads.
		// A Transformer may be used multiple times. Parameters and output properties 
		// are preserved across transformations.		
		javax.xml.transform.Transformer xformer;
    try {
      xformer = template.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new Docx4JException("The Transformer is ill-configured", e);
    }
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

        /* SUPER DEBUGGING
            // http://xml.apache.org/xalan-j/usagepatterns.html#debugging
            // debugging
            // Set up a PrintTraceListener object to print to a file.
            java.io.FileWriter fw = new java.io.FileWriter("/tmp/xslt-events" + xsltCount++ + ".log");
            java.io.PrintWriter pw = new java.io.PrintWriter(fw);
            PrintTraceListener ptl = new PrintTraceListener(pw);

            // Print information as each node is 'executed' in the stylesheet.
            ptl.m_traceElements = true;
            // Print information after each result-tree generation event.
            ptl.m_traceGeneration = true;
            // Print information after each selection event.
            ptl.m_traceSelection = true;
            // Print information whenever a template is invoked.
            ptl.m_traceTemplates = true;
            // Print information whenever an extension is called.
            ptl.m_traceExtension = true;
            TransformerImpl transformerImpl = (TransformerImpl)xformer;

              // Register the TraceListener with the TraceManager associated
              // with the TransformerImpl.
              TraceManager trMgr = transformerImpl.getTraceManager();
              trMgr.addTraceListener(ptl);

*/
		// DEBUGGING
		// use the identity transform if you want to send wordDocument;
		// otherwise you'll get the XHTML
		// javax.xml.transform.Transformer xformer = tfactory.newTransformer();
        try {
            xformer.transform(source, result);
        } catch (TransformerException e) {
          throw new Docx4JException("Cannot perform the transformation", e);
        } finally {
            //pw.flush();
        }
    	
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
	 * @param binder
	 * @param jaxbElement
	 * @param refreshXmlFirst
	 * @param xpathExpr
	 * @return
	 * @throws JAXBException
	 */
	public static List<Object> getJAXBNodesViaXPath(Binder<Node> binder, 
			Object jaxbElement, String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException {
		
		Node node;
		if (refreshXmlFirst) 
			node = binder.updateXML(jaxbElement);
		node = binder.getXMLNode(jaxbElement);
		
		//log.debug("XPath will execute against: " + XmlUtils.w3CDomNodeToString(node));
		
        List<Object> resultList = new ArrayList<Object>();
        for( Node n : xpath(node, xpathExpr) ) {
        	Object o = binder.getJAXBNode(n);
        	if (o==null) {
        		log.warn("no object association for xpath result!");
        	} else {
        		if (o instanceof javax.xml.bind.JAXBElement) {
        			log.debug("added " + JAXBElementDebug((JAXBElement)o) );
        		} else {
        			log.debug("added " + o.getClass().getName() );        			
        		}
        		resultList.add(o);        		
        	}
        }
        return resultList;
    }
	
    public static List<Node> xpath(Node node, String xpathExpression) {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();

        NamespaceContext nsContext = new NamespacePrefixMappings();
        
        return xpath(node, xpathExpression, nsContext);
        
    }	

    public static List<Node> xpath(Node node, String xpathExpression, NamespaceContext nsContext) {
    	
//    	log.info("Using XPathFactory: " + XPathFactory.DEFAULT_PROPERTY_NAME + ": " 
//    			+ System.getProperty(XPathFactory.DEFAULT_PROPERTY_NAME));    
//        System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME, 
//        		"org.apache.xpath.jaxp.XPathFactoryImpl");
        
        // create XPath
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();

		xpath.setNamespaceContext(nsContext);
        
        try {
            List<Node> result = new ArrayList<Node>();
            NodeList nl = (NodeList) xpath.evaluate(xpathExpression, node, XPathConstants.NODESET);
            log.debug("evaluate returned " + nl.getLength() );
            for( int i=0; i<nl.getLength(); i++ ) {
                result.add(nl.item(i));
            }
            return result;
        } catch (XPathExpressionException e) {
            log.error(e);
            throw new RuntimeException(e);
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
     
     
	/**
	 * Copy a node from one DOM document to another.  Used
	 * to avoid relying on an underlying implementation which might 
	 * not support importNode 
	 * (eg Xalan's org.apache.xml.dtm.ref.DTMNodeProxy).
	 * 
	 * WARNING: doesn't fully support namespaces!
	 * 
	 * @param sourceNode
	 * @param destParent
	 */
	public static void treeCopy( Node sourceNode, Node destParent ) {
		
		// http://osdir.com/ml/text.xml.xerces-j.devel/2004-04/msg00066.html
		// suggests the problem has been fixed?
		
		// source node maybe org.apache.xml.dtm.ref.DTMNodeProxy
		// (if its xslt output we are copying)
		// or com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
		// (if its marshalled JAXB)
	    	
    	log.debug("node type" + sourceNode.getNodeType());
    	
        switch (sourceNode.getNodeType() ) {

        	case Node.DOCUMENT_NODE: // type 9
        
//        		log.debug("DOCUMENT:" + w3CDomNodeToString(sourceNode) );
//        		if (sourceNode.getChildNodes().getLength()==0) {
//        			log.debug("..no children!");
//        		}
        		
                // recurse on each child
                NodeList nodes = sourceNode.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                    	log.debug("child " + i + "of DOCUMENT_NODE");
                    	//treeCopy((DTMNodeProxy)nodes.item(i), destParent);
                    	treeCopy((Node)nodes.item(i), destParent);
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                
                // Copy of the node itself
        		log.debug("copying: " + sourceNode.getNodeName() );
        		Node newChild;
        		if ( destParent instanceof Document ) {
        			newChild = ((Document)destParent).createElementNS(
        				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );
        		} else if (sourceNode.getNamespaceURI()!=null) {
        			newChild = destParent.getOwnerDocument().createElementNS(
            				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );	            			
        		} else {
        			newChild = destParent.getOwnerDocument().createElement(
            				sourceNode.getNodeName() );	            			
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
                				attr.getName(), attr.getValue() );
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
            	
            	// Where destParent is com.sun.org.apache.xerces.internal.dom.DocumentImpl,
            	// destParent.getOwnerDocument() returns null.
            	// #document ; com.sun.org.apache.xerces.internal.dom.DocumentImpl
            	
//            	System.out.println(sourceNode.getNodeValue());
            	
            	//System.out.println(destParent.getNodeName() + " ; " + destParent.getClass().getName() );
            	if (destParent.getOwnerDocument()==null
            			&& destParent.getNodeName().equals("#document")) {
	            	Node textNode = ((Document)destParent).createTextNode(sourceNode.getNodeValue());   
	            	destParent.appendChild(textNode);
            	} else {
	            	Node textNode = destParent.getOwnerDocument().createTextNode(sourceNode.getNodeValue());   
            		// Warning: If you attempt to write a single "&" character, it will be converted to &amp; 
            		// even if it doesn't look like that with getNodeValue() or getTextContent()!
	            	// So avoid any tricks with entities! See notes in docx2xhtmlNG2.xslt
	            	Node appended = destParent.appendChild(textNode);
	            	
            	}
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
