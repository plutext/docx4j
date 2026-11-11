/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  docx4j is licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A leading tab's fixed leader, marked {@code docx4j:tab="fixed"} with its block's stops where the renderer has
 * {@link FopCapabilities.Capability#FIXED_TAB_BESIDE_OBSTACLE}, so that the renderer can measure it from the margin
 * on a line beside an obstacle, as Word does (round fop-cr024-bothsides-r4); its length as before; and nothing
 * marked without the hook, which a renderer would take for a tab it lays out itself.  A tab after a check box, and
 * one after text, are laid-out tabs as before.
 */
public class LeadingTabMarkTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String WORD_LAYOUT_NS = "http://docx4j.org/fop/word-layout";

	@After
	public void reset() {
		XsltFOFunctions.fixedTabBesideObstacleForTest = null;
	}

	private static org.w3c.dom.Document fo(String paragraphContent, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>" + paragraphContent + "</w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element onlyLeader(org.w3c.dom.Document doc) {
		NodeList leaders = doc.getElementsByTagNameNS(FO, "leader");
		assertEquals(1, leaders.getLength());
		return (Element) leaders.item(0);
	}

	private static Element blockOf(Element leader) {
		org.w3c.dom.Node block = leader;
		while (!"block".equals(block.getLocalName())) block = block.getParentNode();
		return (Element) block;
	}

	private static final String LEADING_TAB = "<w:r><w:tab/><w:t>label</w:t></w:r>";

	private static String checkBox() {
		String r = "<w:r><w:rPr><w:sz w:val=\"20\"/></w:rPr>";
		return r + "<w:fldChar w:fldCharType=\"begin\"><w:ffData><w:name w:val=\"Check\"/><w:enabled/>"
				+ "<w:checkBox><w:sizeAuto/><w:default w:val=\"0\"/></w:checkBox></w:ffData></w:fldChar></w:r>"
				+ r + "<w:instrText xml:space=\"preserve\"> FORMCHECKBOX </w:instrText></w:r>"
				+ r + "<w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ r + "<w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private void check(int flags) throws Exception {
		WordLayoutOn.assume();

		// without the hook: the fixed leader as before, no mark, no stops on the block
		XsltFOFunctions.fixedTabBesideObstacleForTest = false;
		Element before = onlyLeader(fo(LEADING_TAB, flags));
		String length = before.getAttribute("leader-length");
		assertTrue(length, Double.parseDouble(length.replace("pt", "")) > 0);
		assertEquals("unmarked", "", before.getAttributeNS(WORD_LAYOUT_NS, "tab"));
		assertFalse("no stops", blockOf(before).hasAttributeNS(WORD_LAYOUT_NS, "tab-default"));

		// with it: the same length, marked fixed, and the block's stops for the renderer
		XsltFOFunctions.fixedTabBesideObstacleForTest = true;
		Element marked = onlyLeader(fo(LEADING_TAB, flags));
		assertEquals("its length stands", length, marked.getAttribute("leader-length"));
		assertEquals("space", marked.getAttribute("leader-pattern"));
		assertEquals(XsltFOFunctions.TAB_FIXED, marked.getAttributeNS(WORD_LAYOUT_NS, "tab"));
		Element block = blockOf(marked);
		assertTrue("the stops", block.hasAttributeNS(WORD_LAYOUT_NS, "tabs"));
		assertEquals("the default stop", "720", block.getAttributeNS(WORD_LAYOUT_NS, "tab-default"));
		assertTrue("the indent", block.hasAttributeNS(WORD_LAYOUT_NS, "tab-ind"));

		// a tab after a check box is laid out from the box's end, as before
		Element afterBox = onlyLeader(fo(checkBox() + LEADING_TAB, flags));
		assertEquals("0pt", afterBox.getAttribute("leader-length"));
		assertEquals("a laid-out tab", "1", afterBox.getAttributeNS(WORD_LAYOUT_NS, "tab"));

		// a tab after text is a laid-out tab, with the hook or without it
		for (boolean hook : new boolean[] { false, true }) {
			XsltFOFunctions.fixedTabBesideObstacleForTest = hook;
			Element mid = onlyLeader(fo("<w:r><w:t>label</w:t><w:tab/><w:t>x</w:t></w:r>", flags));
			assertEquals("hook " + hook, "1", mid.getAttributeNS(WORD_LAYOUT_NS, "tab"));
		}
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
