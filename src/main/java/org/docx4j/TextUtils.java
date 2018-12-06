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
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TextUtils {
	
	private static Logger log = LoggerFactory.getLogger(TextUtils.class);	

	/**
	 * Extract contents of descendant <w:t> elements. 
	 * 
	 * @param o
	 * @return String
	 * @since 6.0.0
	 */
	public static String getText(Object o)  {

		StringWriter w = new StringWriter();
		try {
			extractText(o, w, Context.jc);
		} catch (Exception e) {
			log.warn(e.getMessage());
			return null;
		}
		return w.toString();
	}
	
	/**
	 * Extract contents of descendant <w:t> elements. 
	 * 
	 * @param o
	 * @param jcSVG JAXBContext
	 * @return
	 */
	public static void extractText(Object o, Writer w) throws Exception {

		extractText(o, w, Context.jc);
	}
	
	/**
	 * Extract contents of descendant <w:t> elements. 
	 * 
	 * @param o
	 * @param jc JAXBContext
	 * @return
	 */
	public static void extractText(Object o, Writer w, JAXBContext jc) throws Exception {
		
		if (o==null) {
			throw new Docx4JException("Can't extractText from null object");
		}
		
		Marshaller marshaller=jc.createMarshaller();
		NamespacePrefixMapperUtils.setProperty(marshaller, 
				NamespacePrefixMapperUtils.getPrefixMapper());
		marshaller.marshal(o, new TextExtractor(w));
		
	}

	/**
	 * Extract contents of descendant <w:t> elements.
	 * Use this for objects which don't have @XmlRootElement
	 * 
	 * @param o
	 * @param w
	 * @param jc
	 * @param uri
	 * @param local
	 * @param declaredType
	 * @throws Exception
	 */
	public static void extractText(Object o, Writer w, JAXBContext jc,
			String uri, String local, Class declaredType) throws Exception {
		
		Marshaller marshaller=jc.createMarshaller();
		NamespacePrefixMapperUtils.setProperty(marshaller, 
				NamespacePrefixMapperUtils.getPrefixMapper());
		marshaller.marshal(
				new JAXBElement(new QName(uri,local), declaredType, o ), 
				new TextExtractor(w));		
	}
	
	
	/**
	 * A SAX ContentHandler that writes all #PCDATA onto a java.io.Writer
	 * 
	 * From http://www.cafeconleche.org/books/xmljava/chapters/ch06s03.html
	 *
	 */
	public static class TextExtractor extends DefaultHandler {

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

