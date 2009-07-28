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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TextUtils {
	
	private static Logger log = Logger.getLogger(TextUtils.class);	
		
	/**
	 * Extract contents of <w:t> elements. 
	 * 
	 * @param o
	 * @param jc JAXBContext
	 * @return
	 */
	public static void extractText(Object o, Writer w) throws Exception {

		extractText(o, w, Context.jc);
	}
	
	/**
	 * Extract contents of <w:t> elements. 
	 * 
	 * @param o
	 * @param jc JAXBContext
	 * @return
	 */
	public static void extractText(Object o, Writer w, JAXBContext jc) throws Exception {
		
		Marshaller marshaller=jc.createMarshaller();
		
		try { 
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
					new org.docx4j.jaxb.NamespacePrefixMapper() ); 
			// Reference implementation appears to be present (in endorsed dir?)
			log.info("using com.sun.xml.bind.namespacePrefixMapper");
			
		} catch (javax.xml.bind.PropertyException cnfe) {
			
			log.error(cnfe);
			log.info("attempting to use com.sun.xml.INTERNAL.bind.namespacePrefixMapper");			
			// Use JAXB distributed in Java 6 - note 'internal' 
			marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", 
					new org.docx4j.jaxb.NamespacePrefixMapperSunInternal() ); 			
		}
				
		marshaller.marshal(o, new TextExtractor(w));
		
	}
	
	/**
	 * A SAX ContentHandler that writes all #PCDATA onto a java.io.Writer
	 * 
	 * From http://www.cafeconleche.org/books/xmljava/chapters/ch06s03.html
	 *
	 */
	static class TextExtractor extends DefaultHandler {

		  private Writer out;
		  
		  public TextExtractor(Writer out) {
		    this.out = out;   
		  }
		    
		  public void characters(char[] text, int start, int length)
		   throws SAXException {
		     
		    try {
		      out.write(text, start, length); 
		    }
		    catch (IOException e) {
		      throw new SAXException(e);   
		    }
		    
		  }  
		    
		} // end TextExtractor	
	
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Table.docx";
		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Word2007-fonts.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();		
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		
		Writer out = new OutputStreamWriter(System.out);
		
		extractText(wmlDocumentEl, out);
		
		//out.flush();
		out.close();
		

	}
	
}

