/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.junit.Test;

/**
 * A table's w:jc in HTML (CR-041 phase 1): center gives auto margins on both sides, right
 * and end an auto left margin, so a browser places the table in its container as Word
 * places it on the page; w:tblInd is dropped for all three, as Word ignores it; a
 * w:bidiVisual table is left as it was (CR-041 phase 3).  Both pathways.
 */
public class HtmlTableAlignmentTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String html(String tblPrExtra, int flags) throws Exception {
		String body = "<w:tbl><w:tblPr><w:tblW w:w=\"4500\" w:type=\"dxa\"/>" + tblPrExtra + "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/></w:tblGrid><w:tr>"
				+ "<w:tc><w:p><w:r><w:t>a</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>b</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>c</w:t></w:r></w:p></w:tc></w:tr></w:tbl><w:p/>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getJaxbElement().setBody(
				(Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">" + body + "</w:body>")));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flags);
		return os.toString("UTF-8");
	}

	/** The style attribute of the first table element. */
	private static String tableStyle(String html) {
		int at = html.indexOf("<table");
		assertTrue("no table", at >= 0);
		String tag = html.substring(at, html.indexOf('>', at));
		int s = tag.indexOf("style=\"");
		return s < 0 ? "" : tag.substring(s + 7, tag.indexOf('"', s + 7)).replace(" ", "");
	}

	private static String pathway(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
	}

	@Test
	public void centredHasAutoMarginsBothSides() throws Exception {
		for (int flags : FLAGS) {
			String style = tableStyle(html("<w:jc w:val=\"center\"/><w:tblInd w:w=\"1440\" w:type=\"dxa\"/>", flags));
			assertTrue(pathway(flags) + style, style.contains("margin-left:auto") && style.contains("margin-right:auto"));
			assertFalse(pathway(flags) + style, style.contains("margin-left:1in"));
		}
	}

	@Test
	public void rightAndEndHaveAnAutoLeftMargin() throws Exception {
		for (int flags : FLAGS) {
			for (String jc : new String[] { "right", "end" }) {
				String style = tableStyle(html("<w:jc w:val=\"" + jc + "\"/><w:tblInd w:w=\"1440\" w:type=\"dxa\"/>", flags));
				assertTrue(pathway(flags) + jc + ": " + style,
						style.contains("margin-left:auto") && style.contains("margin-right:0"));
				assertFalse(pathway(flags) + jc + ": w:tblInd dropped: " + style, style.contains("margin-left:1in"));
			}
		}
	}

	@Test
	public void startKeepsItsIndent() throws Exception {
		for (int flags : FLAGS) {
			String style = tableStyle(html("<w:jc w:val=\"start\"/><w:tblInd w:w=\"1440\" w:type=\"dxa\"/>", flags));
			assertFalse(pathway(flags) + style, style.contains("margin-left:auto"));
			assertTrue(pathway(flags) + style, style.contains("margin-left:1in"));
		}
	}

	@Test
	public void bidiVisualLeftForPhase3() throws Exception {
		for (int flags : FLAGS) {
			String style = tableStyle(html("<w:bidiVisual/><w:jc w:val=\"right\"/>", flags));
			assertFalse(pathway(flags) + style, style.contains("margin-left:auto"));
		}
	}
}
