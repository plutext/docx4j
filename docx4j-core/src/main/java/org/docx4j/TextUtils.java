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
	 * <p>mc:AlternateContent (since 17.2.0, CR-021): the text of ONE branch - the one
	 * {@link org.docx4j.jaxb.McSelection} draws - is written; the other branches are
	 * skipped, so a text box's text appears once.  The rule is McSelection's exactly:
	 * the first Choice whose Requires prefixes are all preferred, else the Fallback,
	 * else (Choices but no Fallback) the first Choice.  Being a stream, this cannot
	 * know a Fallback is absent until the element ends, so the first unpreferred
	 * Choice's text is held back and written only then, if nothing else was taken.</p>
	 */
	public static class TextExtractor extends DefaultHandler {

		private static final String MC_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";

		private Writer out;

		/** One open mc:AlternateContent. */
		private static final class Alternate {
			boolean taken;
			StringBuilder firstChoice;   // held-back text of the first unpreferred Choice
			int firstChoiceDepth;        // the depth of that Choice element
		}

		private final java.util.ArrayDeque<Alternate> alternates = new java.util.ArrayDeque<Alternate>();
		/** Where characters go: the innermost held-back Choice's buffer, else the writer. */
		private final java.util.ArrayDeque<StringBuilder> captures = new java.util.ArrayDeque<StringBuilder>();
		/** Depth inside a skipped branch; 0 when writing. */
		private int skipping = 0;
		private int depth = 0;

		public TextExtractor(Writer out) {
			this.out = out;
		}

		private void emit(CharSequence text) throws SAXException {
			if (captures.isEmpty()) {
				try {
					out.write(text.toString());
				} catch (IOException e) {
					throw new SAXException(e);
				}
			} else {
				captures.peek().append(text);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts)
				throws SAXException {
			depth++;
			if (skipping > 0) {
				skipping++;
				return;
			}
			if (!MC_NS.equals(uri)) return;
			if ("AlternateContent".equals(localName)) {
				alternates.push(new Alternate());
			} else if ("Choice".equals(localName)) {
				Alternate alt = alternates.peek();
				String requires = atts.getValue("Requires");
				if (requires == null) requires = atts.getValue("", "Requires");
				if (alt == null) return; // a stray Choice: written through
				if (!alt.taken && org.docx4j.jaxb.McSelection.prefersChoice(requires)) {
					alt.taken = true;
					alt.firstChoice = null;
				} else if (!alt.taken && alt.firstChoice == null) {
					// the last resort, held back until the element ends
					alt.firstChoice = new StringBuilder();
					alt.firstChoiceDepth = depth;
					captures.push(alt.firstChoice);
				} else {
					skipping = 1;
				}
			} else if ("Fallback".equals(localName)) {
				Alternate alt = alternates.peek();
				if (alt == null) return;
				if (!alt.taken) {
					alt.taken = true;
					alt.firstChoice = null; // the held-back Choice is not needed
				} else {
					skipping = 1;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (skipping > 0) {
				skipping--;
				depth--;
				return;
			}
			if (MC_NS.equals(uri)) {
				if ("Choice".equals(localName)) {
					Alternate alt = alternates.peek();
					if (alt != null && alt.firstChoiceDepth == depth && !captures.isEmpty()
							&& (alt.firstChoice == null || captures.peek() == alt.firstChoice)) {
						captures.pop();
					}
				} else if ("AlternateContent".equals(localName) && !alternates.isEmpty()) {
					Alternate alt = alternates.pop();
					if (!alt.taken && alt.firstChoice != null) {
						emit(alt.firstChoice);
					}
				}
			}
			depth--;
		}

		public void characters(char[] text, int start, int length) throws SAXException {
			if (skipping > 0) return;
			emit(new String(text, start, length));
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

