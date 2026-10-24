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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.HtmlCssHelper;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RPr;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * CR-062: a run's underline (its style, thickness and colour), strike and double strike drawn together, and its
 * emphasis mark, as CSS draws them (D1, D2, D4); PDF draws underline and strike together (D3).
 */
public class DecorationAndEmphasisTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkg;

	private static WordprocessingMLPackage pkg() throws Exception {
		if (pkg == null) pkg = WordprocessingMLPackage.createPackage();
		return pkg;
	}

	private static RPr rPr(String inner) throws Exception {
		return (RPr) XmlUtils.unmarshalString("<w:rPr " + W + ">" + inner + "</w:rPr>");
	}

	/** The run's CSS as both HTML writers build it, spaces removed */
	static String css(String inner) throws Exception {
		StringBuilder sb = new StringBuilder();
		HtmlCssHelper.createCss(pkg(), rPr(inner), sb);
		return sb.toString().replace(" ", "");
	}

	/** The FO writer's text-decoration for the run */
	static String fo(String inner) throws Exception {
		Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("inline");
		for (Property p : PropertyFactory.createProperties(pkg(), rPr(inner))) p.setXslFO(e);
		return e.getAttribute("text-decoration");
	}

	@Test
	public void eachUnderlineStyleAsCss() throws Exception {
		String[][] table = {
			{ "single", "solid", "auto" }, { "words", "solid", "auto" }, { "double", "double", "auto" },
			{ "thick", "solid", "0.15em" }, { "dotted", "dotted", "auto" }, { "dottedHeavy", "dotted", "0.15em" },
			{ "dash", "dashed", "auto" }, { "dashedHeavy", "dashed", "0.15em" }, { "dashLong", "dashed", "auto" },
			{ "dashLongHeavy", "dashed", "0.15em" }, { "dotDash", "dashed", "auto" }, { "dashDotHeavy", "dashed", "0.15em" },
			{ "dotDotDash", "dashed", "auto" }, { "dashDotDotHeavy", "dashed", "0.15em" }, { "wave", "wavy", "auto" },
			{ "wavyHeavy", "wavy", "0.15em" }, { "wavyDouble", "wavy", "auto" } };
		for (String[] row : table) {
			String c = css("<w:u w:val=\"" + row[0] + "\"/>");
			assertTrue(row[0] + ": " + c, c.contains("text-decoration-line:underline;"));
			if (row[1].equals("solid")) assertFalse(row[0] + ": " + c, c.contains("text-decoration-style"));
			else assertTrue(row[0] + ": " + c, c.contains("text-decoration-style:" + row[1] + ";"));
			if (row[2].equals("auto")) assertFalse(row[0] + ": " + c, c.contains("text-decoration-thickness"));
			else assertTrue(row[0] + ": " + c, c.contains("text-decoration-thickness:" + row[2] + ";"));
		}
		assertFalse(css("<w:u w:val=\"none\"/>"), css("<w:u w:val=\"none\"/>").contains("underline"));
	}

	@Test
	public void anUnderlinesColour() throws Exception {
		assertTrue(css("<w:u w:val=\"single\" w:color=\"FF0000\"/>").contains("text-decoration-color:#FF0000;"));
		assertTrue(css("<w:u w:val=\"single\" w:color=\"red\"/>").contains("text-decoration-color:#FF0000;"));
		assertFalse(css("<w:u w:val=\"single\" w:color=\"auto\"/>").contains("text-decoration-color"));
		assertFalse(css("<w:u w:val=\"single\"/>").contains("text-decoration-color"));
		// a colour alone colours an underline beneath (a character style's) and draws none itself
		String colourOnly = css("<w:u w:color=\"000080\"/>");
		assertTrue(colourOnly, colourOnly.contains("text-decoration-color:#000080;") && !colourOnly.contains("underline"));
		String theme = css("<w:u w:val=\"single\" w:color=\"FFFFFF\" w:themeColor=\"accent2\"/>");
		assertTrue(theme, theme.contains("text-decoration-color:#") && !theme.contains("text-decoration-color:#FFFFFF"));
	}

	@Test
	public void strikeAndUnderlineTogether() throws Exception {
		String both = css("<w:strike/><w:u w:val=\"single\"/>");
		assertTrue(both, both.contains("text-decoration-line:underlineline-through;"));
		String dstrike = css("<w:dstrike/>");
		assertTrue(dstrike, dstrike.contains("text-decoration-line:line-through;") && dstrike.contains("text-decoration-style:double;"));
		String dstrikeUnder = css("<w:dstrike/><w:u w:val=\"single\"/>");
		assertTrue(dstrikeUnder, dstrikeUnder.contains("text-decoration-line:underlineline-through;")
				&& !dstrikeUnder.contains("text-decoration-style")); // the underline's style for both (D2)
		String red = css("<w:strike/><w:u w:val=\"single\" w:color=\"FF0000\"/>");
		assertTrue(red, red.contains("text-decoration-color:#FF0000;"));
		assertTrue(css("<w:strike w:val=\"0\"/>"), css("<w:strike w:val=\"0\"/>").contains("text-decoration-line:none;"));
		// only one text-decoration declaration set: the second no longer replaces the first
		assertEquals(both, 1, both.split("text-decoration-line:", -1).length - 1);
	}

	@Test
	public void pdfDrawsUnderlineAndStrikeTogether() throws Exception {
		assertEquals("underline line-through", fo("<w:strike/><w:u w:val=\"single\"/>"));
		assertEquals("line-through", fo("<w:dstrike/>"));
		assertEquals("underline", fo("<w:u w:val=\"double\"/>"));
		assertEquals("line-through", fo("<w:strike/>"));
	}

	@Test
	public void emphasisMarksAsCss() throws Exception {
		assertTrue(css("<w:em w:val=\"dot\"/>"), css("<w:em w:val=\"dot\"/>").contains("text-emphasis-style:filleddot;"));
		assertTrue(css("<w:em w:val=\"comma\"/>"), css("<w:em w:val=\"comma\"/>").contains("text-emphasis-style:filledsesame;"));
		assertTrue(css("<w:em w:val=\"circle\"/>"), css("<w:em w:val=\"circle\"/>").contains("text-emphasis-style:opencircle;"));
		String under = css("<w:em w:val=\"underDot\"/>");
		assertTrue(under, under.contains("text-emphasis-style:filleddot;") && under.contains("text-emphasis-position:underright;"));
		assertFalse(css("<w:em w:val=\"none\"/>"), css("<w:em w:val=\"none\"/>").contains("filled"));
	}
}
