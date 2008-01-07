/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Plutext-Server.

    Plutext-Server is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    Plutext-Server is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with Plutext-Server.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */


package org.docx4j;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.docx4j.jaxbcontexts.DocumentContext;

import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XmlUtils {
		
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

	/** Unmarshal a Dom4j element as an object in the package org.docx4j.jaxb.document */ 
	public static Object unmarshalDom4jDoc(org.dom4j.Document doc) {
				
		JAXBContext jc = DocumentContext.jc;
			
		Object o = null;
		try {				
		    org.dom4j.io.DOMWriter writer = new org.dom4j.io.DOMWriter();
			org.w3c.dom.Document w3cDoc = writer.write(doc);

//			JAXBContext jc = JAXBContext
//					.newInstance("org.docx4j.jaxb.document");
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.JaxbValidationEventHandler());

			o = u.unmarshal( w3cDoc );

			System.out.println("unmarshalled ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return o;
	}
	
	/** Unmarshal a Dom4j element as an object in the package org.docx4j.jaxb.document */ 
	public static Object unmarshalDom4jEl(Element el) {
		Object o = null;
		try {				

			JAXBContext jc = DocumentContext.jc;

			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.JaxbValidationEventHandler());

			o = u.unmarshal( org.docx4j.XmlUtils.getInputStreamFromDom4jEl(el) );

			System.out.println("unmarshalled ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return o;
	}

	/** Unmarshal a String as an object in the package org.docx4j.jaxb.document */ 
	public static Object unmarshalString(String str) {
		Object o = null;
		try {				
			
			JAXBContext jc = DocumentContext.jc;

			Unmarshaller u = jc.createUnmarshaller();
						
			u.setEventHandler(new org.docx4j.JaxbValidationEventHandler());

			o = u.unmarshal( new javax.xml.transform.stream.StreamSource(
					new java.io.StringReader(str)) );

			System.out.println("unmarshalled ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return o;
	}
	
	/** Marshal to a Dom4j document */ 
	public static org.dom4j.Document marshaltoDom4jDocument(Object o) {
		// TODO - refactor this.
		try {
			JAXBContext jc = DocumentContext.jc;

			Marshaller marshaller=jc.createMarshaller();
			
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();

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

		JAXBContext jc = DocumentContext.jc;
				
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
	
	/** Clone this JAXB object */ 
	public static Object deepCopy(Object in) {

		JAXBContext jc = DocumentContext.jc;
		
		Object o = null;
		try {				

			Unmarshaller u = jc.createUnmarshaller();
			
			// Temp
			u.setEventHandler(new org.docx4j.JaxbValidationEventHandler());

			o = u.unmarshal( new javax.xml.bind.util.JAXBSource(jc, in) );

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return o;
		
	}
	
}
