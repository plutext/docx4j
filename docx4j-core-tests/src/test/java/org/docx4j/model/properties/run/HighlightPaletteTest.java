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
package org.docx4j.model.properties.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.wml.Highlight;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * A highlight is drawn in Word's palette (WdColorIndex), as Word 365 reports it through Office JS (docx4j-core-ts
 * check 61, Jason's run, 2026-10-24). Before 17.3.5, Highlight.getHexVal() gave CSS's colours of the same names, and
 * gold for darkYellow, so HTML and PDF drew nine of the sixteen in colours Word doesn't.
 */
public class HighlightPaletteTest {

	private static final String[][] WORD = { { "black", "000000" }, { "blue", "0000FF" }, { "cyan", "00FFFF" },
			{ "green", "00FF00" }, { "magenta", "FF00FF" }, { "red", "FF0000" }, { "yellow", "FFFF00" },
			{ "white", "FFFFFF" }, { "darkBlue", "000080" }, { "darkCyan", "008080" }, { "darkGreen", "008000" },
			{ "darkMagenta", "800080" }, { "darkRed", "800000" }, { "darkYellow", "808000" }, { "darkGray", "808080" },
			{ "lightGray", "C0C0C0" } };

	private static Highlight highlight(String val) {
		Highlight h = new Highlight();
		h.setVal(val);
		return h;
	}

	@Test
	public void wordsPalette() {
		for (String[] c : WORD) assertEquals(c[0], "#" + c[1], highlight(c[0]).getHexVal());
	}

	@Test
	public void htmlAndPdfDrawIt() throws Exception {
		HighlightColor dy = new HighlightColor(highlight("darkYellow"));
		assertEquals("background-color:#808000;", dy.getCssProperty().replace(" ", ""));
		Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("inline");
		dy.setXslFO(e);
		assertEquals("#808000", e.getAttribute("background-color"));
	}

	@Test
	public void aHexValueTakesWordsAndTheFormerColours() {
		assertEquals("darkYellow", highlight("#808000").getVal());
		assertEquals("lightGray", highlight("#c0c0c0").getVal());
		assertEquals("green", highlight("#00FF00").getVal());
		assertEquals("darkGreen", highlight("#008000").getVal()); // was green, CSS's green
		// what getHexVal gave before 17.3.5, so a document or CSS that took them still maps
		assertEquals("darkYellow", highlight("#FFD700").getVal());
		assertEquals("lightGray", highlight("#D3D3D3").getVal());
		assertEquals("darkGray", highlight("#A9A9A9").getVal());
		assertNull(highlight("#123456").getVal());
		assertEquals("none", highlight("none").getVal()); // was null
	}
}
