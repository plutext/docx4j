/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.NodeList;

/**
 * A line break (w:br, w:cr) inside a footnote. Outside one, a break is written as a block inside the run's
 * inline; XSL 1.1 6.6.7 forbids that under fo:footnote, and FOP refuses the whole document with a
 * ValidationException (measured 2026-10-29 on a manual whose footnote held two URLs on a w:br, the TOC update
 * and the PDF both lost). In a footnote the break is the U+2028 LINE SEPARATOR, an explicit break to FOP's line
 * breaker, on both pathways.
 */
public class FootnoteLineBreakTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r><w:t xml:space=\"preserve\">Body text</w:t></w:r>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteReference w:id=\"1\"/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> with a break of its own</w:t></w:r>"
				+ "<w:r><w:br/><w:t>on the next line.</w:t></w:r>"
				+ "</w:p></w:body></w:document>"));
		FootnotesPart fp = new FootnotesPart();
		fp.setJaxbElement((CTFootnotes)XmlUtils.unmarshalString(
				"<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p>"
				+   "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteRef/></w:r>"
				+   "<w:r><w:t xml:space=\"preserve\"> http://example.com/first</w:t></w:r>"
				+   "<w:r><w:br/><w:t>http://example.com/second</w:t></w:r>"
				+   "<w:r><w:cr/><w:t>http://example.com/third</w:t></w:r>"
				+ "</w:p></w:footnote>"
				+ "</w:footnotes>", Context.jc, CTFootnotes.class));
		pkg.getMainDocumentPart().addTargetPart(fp);
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static NodeList nodes(org.w3c.dom.Document doc, String xpath) throws Exception {
		XPath xp = XPathFactory.newInstance().newXPath();
		xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
			public String getNamespaceURI(String prefix) { return "fo".equals(prefix) ? "http://www.w3.org/1999/XSL/Format" : null; }
			public String getPrefix(String uri) { return null; }
			public java.util.Iterator<String> getPrefixes(String uri) { return null; }
		});
		return (NodeList)xp.evaluate(xpath, doc, XPathConstants.NODESET);
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		assertEquals("one footnote", 1, nodes(doc, "//fo:footnote").getLength());
		assertEquals("no block inside an inline under the footnote (XSL 1.1 6.6.7)", 0,
				nodes(doc, "//fo:footnote//fo:inline//fo:block").getLength());
		String body = nodes(doc, "//fo:footnote-body").item(0).getTextContent();
		assertEquals("the w:br and the w:cr are each a line separator", 2, body.split(" ", -1).length - 1);
		assertTrue(body, body.contains("first http://example.com/second http://example.com/third"));
		// the body's own break keeps the block idiom
		assertEquals("the body's break is still a block in its inline", 1,
				nodes(doc, "/fo:root/fo:page-sequence/fo:flow/fo:block/fo:inline/fo:block").getLength());
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_NONXSL);
	}

	/** FOP accepts the FO and lays the three URLs out on three lines. */
	@Test
	public void pdfRenders() throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		assertTrue("a PDF", baos.size() > 1000);
	}
}
