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
package org.docx4j.model.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.FontColor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Color;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * WordprocessingML theme colours in the outputs and through {@link PropertyResolver} (CR-034
 * phase 2): Word draws the theme reference, and the value beside it is a cache which can be
 * stale (CR-034 §11, P1).  The document is a new one, in the Office 2023 theme ({@code accent1}
 * 156082, {@code accent2} E97132, {@code accent4} 0F9ED5, {@code accent5} A02B93,
 * {@code accent6} 4EA72E); its stale values are Office 2007's {@code accent1}, 4F81BD, which
 * should therefore appear in no output.  The FO side is export-fo-tests' ThemeColourFoTest.
 */
public class ThemeColourOutputTest {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** The fixture: each paragraph's text names what it tests */
	static WordprocessingMLPackage doc() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Style> styles = mdp.getStyleDefinitionsPart().getContents().getStyle();
		styles.add(style("StaleColour", "<w:rPr><w:color w:val=\"4F81BD\" w:themeColor=\"accent1\"/></w:rPr>"));
		styles.add(style("ThemeOnly", "<w:rPr><w:color w:themeColor=\"accent2\"/></w:rPr>"));
		styles.add(style("CurrentCache", "<w:rPr><w:color w:val=\"156082\" w:themeColor=\"accent1\"/></w:rPr>"));
		List<Object> body = mdp.getContent();
		body.add(p("<w:pPr><w:pStyle w:val=\"StaleColour\"/></w:pPr><w:r><w:t>STALE</w:t></w:r>"));
		body.add(p("<w:pPr><w:pStyle w:val=\"StaleColour\"/></w:pPr>"
				+ "<w:r><w:rPr><w:color w:val=\"FF0000\"/></w:rPr><w:t>RED</w:t></w:r>"));
		body.add(p("<w:pPr><w:pStyle w:val=\"ThemeOnly\"/></w:pPr><w:r><w:t>THEMEONLY</w:t></w:r>"));
		body.add(p("<w:pPr><w:pStyle w:val=\"CurrentCache\"/></w:pPr><w:r><w:t>CURRENT</w:t></w:r>"));
		body.add(p("<w:r><w:rPr><w:color w:val=\"auto\" w:themeColor=\"accent5\"/></w:rPr><w:t>AUTO</w:t></w:r>"));
		body.add(p("<w:pPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"auto\" w:themeFill=\"accent2\"/></w:pPr>"
				+ "<w:r><w:t>SHADEDAUTO</w:t></w:r>"));
		body.add(p("<w:r><w:t>UNBORDERED</w:t></w:r>"));
		body.add(p("<w:pPr><w:pBdr><w:top w:val=\"single\" w:sz=\"8\" w:space=\"1\" w:color=\"4F81BD\" w:themeColor=\"accent6\"/></w:pBdr></w:pPr>"
				+ "<w:r><w:t>BORDERED</w:t></w:r>"));
		body.add(XmlUtils.unmarshalString("<w:tbl " + W + "><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/>"
				+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"4F81BD\" w:themeFill=\"accent4\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>CELL</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"));
		body.add(p("<w:r><w:t>END</w:t></w:r>"));
		return pkg;
	}

	private static Style style(String id, String pr) throws Exception {
		return (Style) XmlUtils.unmarshalString("<w:style " + W + " w:type=\"paragraph\" w:styleId=\"" + id + "\">"
				+ "<w:name w:val=\"" + id + "\"/><w:basedOn w:val=\"Normal\"/>" + pr + "</w:style>");
	}

	private static P p(String content) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p " + W + ">" + content + "</w:p>");
	}

	private static P paragraph(WordprocessingMLPackage pkg, int i) {
		return (P) pkg.getMainDocumentPart().getContent().get(i);
	}

	private static RPr effectiveRPr(WordprocessingMLPackage pkg, int i) throws Exception {
		P p = paragraph(pkg, i);
		R r = (R) p.getContent().get(0);
		return pkg.getMainDocumentPart().getPropertyResolver().getEffectiveRPr(r.getRPr(), p.getPPr());
	}

	@Test
	public void resolverGivesTheColourWordDraws() throws Exception {
		WordprocessingMLPackage pkg = doc();
		PropertyResolver pr = pkg.getMainDocumentPart().getPropertyResolver();
		assertEquals("156082", pr.resolveColor(effectiveRPr(pkg, 0).getColor()).toHex()); // STALE
		assertEquals("FF0000", pr.resolveColor(effectiveRPr(pkg, 1).getColor()).toHex()); // RED
		assertEquals("E97132", pr.resolveColor(effectiveRPr(pkg, 2).getColor()).toHex()); // THEMEONLY
		assertEquals("156082", pr.resolveColor(effectiveRPr(pkg, 3).getColor()).toHex()); // CURRENT
		assertEquals("A02B93", pr.resolveColor(effectiveRPr(pkg, 4).getColor()).toHex()); // AUTO
		PPr shaded = pr.getEffectivePPr(paragraph(pkg, 5).getPPr());
		assertEquals("E97132", pr.resolveFill(shaded.getShd()).toHex());
		assertNull(pr.resolvePatternColor(shaded.getShd())); // automatic
		PPr bordered = pr.getEffectivePPr(paragraph(pkg, 7).getPPr());
		assertEquals("4EA72E", pr.resolveColor(bordered.getPBdr().getTop()).toHex());
		assertTrue(pr.getThemeColors() == pr.getThemeColors()); // made once
	}

	/** A run's own value, stated without a theme reference, replaces the style's reference with it */
	@Test
	public void aDirectValueReplacesTheStylesThemeReference() throws Exception {
		Color c = effectiveRPr(doc(), 1).getColor();
		assertEquals("FF0000", c.getVal());
		assertNull(c.getThemeColor());
	}

	/** A style's w:color stating only a theme colour is inherited (it used to be dropped as empty) */
	@Test
	public void aThemeColourWithNoValueIsInherited() throws Exception {
		Color c = effectiveRPr(doc(), 2).getColor();
		assertEquals("accent2", c.getThemeColor().value());
	}

	/** Without theme colours set, a property writes the value beside the reference, as before 17.3.3 */
	@Test
	public void propertiesWriteTheDrawnColourOnceGivenTheTheme() throws Exception {
		WordprocessingMLPackage pkg = doc();
		ThemeColors theme = pkg.getMainDocumentPart().getPropertyResolver().getThemeColors();
		FontColor stale = new FontColor(effectiveRPr(pkg, 0).getColor());
		assertEquals("color: #4F81BD;", stale.getCssProperty());
		stale.setThemeColors(theme);
		assertEquals("color: #156082;", stale.getCssProperty());
		FontColor current = new FontColor(effectiveRPr(pkg, 3).getColor());
		current.setThemeColors(theme);
		assertEquals("color: #156082;", current.getCssProperty());
		PShading shading = new PShading(paragraph(pkg, 5).getPPr().getShd());
		assertEquals("", shading.getCssProperty());
		shading.setThemeColors(theme);
		assertEquals("background-color: #E97132;", shading.getCssProperty());
	}

	@Test
	public void htmlDrawsTheThemeColours() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			String html = html(doc(), flags);
			assertFalse(pathway + "a stale value written", html.toUpperCase().contains("4F81BD"));
			// not the border's 4EA72E: the HTML outputs draw no paragraph border here (nor did they
			// before 17.3.3); ThemeColourFoTest checks it in FO
			for (String expected : new String[] { "156082", "FF0000", "E97132", "A02B93", "0F9ED5" }) {
				assertTrue(pathway + expected, html.toUpperCase().contains("#" + expected));
			}
			assertTrue(pathway + "AUTO", span(html, "AUTO").toUpperCase().contains("COLOR: #A02B93"));
			assertTrue(pathway + "RED", span(html, "RED").toUpperCase().contains("COLOR: #FF0000"));
		}
	}

	static String html(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flags);
		return os.toString("UTF-8");
	}

	/** The start tag of the span holding a run's text */
	private static String span(String html, String text) {
		int at = html.indexOf(">" + text + "<");
		assertTrue(text, at > 0);
		return html.substring(html.lastIndexOf("<span", at), at);
	}
}
