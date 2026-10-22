/*
 *  Copyright 2026, Plutext Pty Ltd.
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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McLoadMode;
import org.docx4j.jaxb.McSelection;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.NodeList;

/**
 * CR-051 section 4, the FO exporters: in {@code word} mode the branch Word reads is drawn; in
 * {@code preserve} mode the Fallback by default and the Choice under {@code preferChoice}; and a
 * section break in a branch's paragraph gives the header and footer pre-pass and the real pass the
 * same sections (D6), so the document lays out on two pages with each section's header.
 */
public class AlternateContentSectionTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";
	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String sectPr(String headerId) {
		return "<w:sectPr><w:headerReference w:type=\"default\" r:id=\"" + headerId + "\"/>"
				+ "<w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\"/></w:sectPr>";
	}

	/**
	 * Two headers; a section break (ending the first section, header ONE) in each branch's
	 * paragraph, with different text in each; the body's section (header TWO) after it.  Built,
	 * saved and loaded in the mode, so that the load path's splice applies as it would to a file.
	 */
	private static WordprocessingMLPackage pkg(McLoadMode mode) throws Exception {
		WordprocessingMLPackage built = WordprocessingMLPackage.createPackage();
		Relationship one = built.getMainDocumentPart().addTargetPart(header("/word/header1.xml", "HEADER_ONE"));
		Relationship two = built.getMainDocumentPart().addTargetPart(header("/word/header2.xml", "HEADER_TWO"));
		String choice = "<w:p><w:pPr>" + sectPr(one.getId()) + "</w:pPr><w:r><w:t>CHOICE_W14</w:t></w:r></w:p>";
		String fallback = "<w:p><w:pPr>" + sectPr(one.getId()) + "</w:pPr><w:r><w:t>FALLBACK_TEXT</w:t></w:r></w:p>";
		built.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>"
				+ p("first section")
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + choice + "</mc:Choice><mc:Fallback>" + fallback + "</mc:Fallback></mc:AlternateContent>"
				+ p("second section") + sectPr(two.getId()) + "</w:body></w:document>"));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Docx4J.save(built, bytes);
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(bytes.toByteArray()), mode);
		pkg.getMainDocumentPart().getContents();
		return pkg;
	}

	private static HeaderPart header(String name, String text) throws Exception {
		HeaderPart header = new HeaderPart(new PartName(name));
		header.setJaxbElement((Hdr) XmlUtils.unmarshalString("<w:hdr " + NS + ">" + p(text) + "</w:hdr>", org.docx4j.jaxb.Context.jc, Hdr.class));
		return header;
	}

	private static int pages(org.w3c.dom.Document areaTree) {
		NodeList pages = areaTree.getElementsByTagName("pageViewport");
		return pages.getLength();
	}

	private static String text(org.w3c.dom.Document areaTree) {
		StringBuilder sb = new StringBuilder();
		NodeList words = areaTree.getElementsByTagName("word");
		for (int i = 0; i < words.getLength(); i++) sb.append(words.item(i).getTextContent()).append(' ');
		NodeList texts = areaTree.getElementsByTagName("text");
		for (int i = 0; i < texts.getLength(); i++) sb.append(texts.item(i).getTextContent()).append(' ');
		return sb.toString();
	}

	private String preferBefore;

	@Before
	public void remember() {
		preferBefore = Docx4jProperties.getProperty(McSelection.PROPERTY);
		Docx4jProperties.setProperty(McSelection.PROPERTY, "");
	}

	@After
	public void restore() {
		Docx4jProperties.setProperty(McSelection.PROPERTY, preferBefore == null ? "" : preferBefore);
	}

	/** word mode: the w14 Choice's paragraph and its section break, on two pages with both headers. */
	@Test
	public void wordModeDrawsTheBranchWordReads() throws Exception {
		for (int flag : FLAGS) {
			org.w3c.dom.Document tree = areaTree(pkg(McLoadMode.WORD), flag);
			String text = text(tree);
			assertEquals(flag + ": " + text, 2, pages(tree));
			assertTrue(text, text.contains("CHOICE_W14") && text.contains("HEADER_ONE") && text.contains("HEADER_TWO"));
			assertFalse(text, text.contains("FALLBACK_TEXT"));
		}
	}

	/** preserve mode: the Fallback by default, with the section break it carries seen by both passes; the Choice under preferChoice. */
	@Test
	public void preserveModeDrawsTheFallbackOrThePreferredChoiceWithItsSection() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = pkg(McLoadMode.PRESERVE);
			org.w3c.dom.Document tree = areaTree(pkg, flag);
			String text = text(tree);
			assertEquals(flag + ": " + text, 2, pages(tree));
			assertTrue(text, text.contains("FALLBACK_TEXT") && text.contains("HEADER_ONE") && text.contains("HEADER_TWO"));
			assertFalse(text, text.contains("CHOICE_W14"));
			// the caller's package keeps every branch
			String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
			assertTrue(xml, xml.contains("<mc:Choice") && xml.contains("<mc:Fallback"));

			Docx4jProperties.setProperty(McSelection.PROPERTY, "w14");
			try {
				tree = areaTree(pkg(McLoadMode.PRESERVE), flag);
				text = text(tree);
				assertEquals(flag + ": " + text, 2, pages(tree));
				assertTrue(text, text.contains("CHOICE_W14") && text.contains("HEADER_ONE") && text.contains("HEADER_TWO"));
				assertFalse(text, text.contains("FALLBACK_TEXT"));
			} finally {
				Docx4jProperties.setProperty(McSelection.PROPERTY, "");
			}
		}
	}
}
