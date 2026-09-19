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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
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
	public static void extractText(Object o, Writer w) throws Docx4JException {

		extractText(o, w, Context.jc);
	}
	
	/**
	 * Extract contents of descendant <w:t> elements. 
	 * 
	 * @param o
	 * @param jc JAXBContext
	 * @return
	 * @throws Docx4JException 
	 */
	public static void extractText(Object o, Writer w, JAXBContext jc) throws Docx4JException  {
		
		if (o==null) {
			throw new Docx4JException("Can't extractText from null object");
		}
		try {
			Marshaller marshaller=jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());
			marshaller.marshal(o, new TextExtractor(w));
		} catch (JAXBException e) {
			throw new Docx4JException("JAXB error marshalling to extractText", e);
		}

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
			String uri, String local, Class declaredType) throws Docx4JException {
		
		try {
			Marshaller marshaller=jc.createMarshaller();
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());
			marshaller.marshal(
					new JAXBElement(new QName(uri,local), declaredType, o ), 
					new TextExtractor(w));		
		} catch (JAXBException e) {
			throw new Docx4JException("JAXB error marshalling to extractText", e);
		}
	}
	
	
	/**
	 * A SAX ContentHandler that writes all #PCDATA onto a java.io.Writer
	 * 
	 * From http://www.cafeconleche.org/books/xmljava/chapters/ch06s03.html
	 *
	 */
	/**
	 * Writes the character content it is handed.
	 * 
	 * <p>mc:AlternateContent (since 17.1.1, CR-021): the text of ONE branch - the one
	 * {@link org.docx4j.jaxb.McSelection} draws - is written; the other branches are
	 * skipped, so a text box's text appears once.  Being a stream, this cannot apply
	 * McSelection's last resort (an element with Choices but no Fallback takes its
	 * first Choice): such an element, which Word never writes, contributes nothing.</p>
	 */
	public static class TextExtractor extends DefaultHandler {

		private static final String MC_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";

		private Writer out;

		/** One entry per open mc:AlternateContent: whether a branch has been taken. */
		private final java.util.ArrayDeque<boolean[]> alternates = new java.util.ArrayDeque<boolean[]>();
		/** Depth inside a skipped branch; 0 when writing. */
		private int skipping = 0;

		public TextExtractor(Writer out) {
			this.out = out;
		}

		@Override
		public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts)
				throws SAXException {
			if (skipping > 0) {
				skipping++;
				return;
			}
			if (MC_NS.equals(uri)) {
				if ("AlternateContent".equals(localName)) {
					alternates.push(new boolean[] { false });
				} else if ("Choice".equals(localName)) {
					boolean[] taken = alternates.peek();
					String requires = atts.getValue("Requires");
					if (requires == null) requires = atts.getValue("", "Requires");
					if (taken != null && !taken[0] && org.docx4j.jaxb.McSelection.prefersChoice(requires)) {
						taken[0] = true;
					} else {
						skipping = 1;
					}
				} else if ("Fallback".equals(localName)) {
					boolean[] taken = alternates.peek();
					if (taken != null && !taken[0]) {
						taken[0] = true;
					} else {
						skipping = 1;
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (skipping > 0) {
				skipping--;
				return;
			}
			if (MC_NS.equals(uri) && "AlternateContent".equals(localName) && !alternates.isEmpty()) {
				alternates.pop();
			}
		}

		public void characters(char[] text, int start, int length) throws SAXException {
			if (skipping > 0) return;
			try {
				out.write(text, start, length);
			} catch (IOException e) {
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

