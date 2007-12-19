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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.docx4j.jaxb.document.Sdt;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XmlUtils {
	
	/** Make a dom4j element into something JAXB can unmarshall */
	public static java.io.InputStream getInputStreamFromDom4jEl(Element el) {
		
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
	public static Object unmarshalDom4jEl(Element el) {
		Object o = null;
		try {				

			// TODO - reuse this object 
			JAXBContext jc = JAXBContext
					.newInstance("org.docx4j.jaxb.document");
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

			// TODO - reuse this object 
			JAXBContext jc = JAXBContext
					.newInstance("org.docx4j.jaxb.document");
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
			JAXBContext jc = JAXBContext.newInstance("org.docx4j.jaxb.document");
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
}
