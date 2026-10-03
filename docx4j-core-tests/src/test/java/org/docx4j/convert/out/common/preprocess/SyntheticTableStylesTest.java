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
package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * {@link ParagraphStylesInTableFix} since CR-030 phase 2: the synthetic styles are named and
 * built by {@link PropertyResolver} (styleIdFor, syntheticStyle, sourceStyleOf), and the
 * preprocess walks with a TableContextTracker.  One test per defect the move closes.
 */
public class SyntheticTableStylesTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\"";

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"22\"/></w:rPr></w:rPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"ListNumber\"><w:name w:val=\"List Number\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:pPr><w:numPr><w:numId w:val=\"1\"/></w:numPr></w:pPr></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"A-B\"><w:name w:val=\"A-B\"/><w:rPr><w:color w:val=\"FF0000\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"A\"><w:name w:val=\"A\"/></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"Grid\"><w:name w:val=\"Grid\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:pPr><w:spacing w:after=\"0\"/></w:pPr>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/></w:rPr></w:tblStylePr></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"C\"><w:name w:val=\"C\"/><w:rPr><w:i/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"B-C\"><w:name w:val=\"B-C\"/><w:rPr><w:b/></w:rPr></w:style>"
			+ "</w:styles>";

	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:pStyle w:val=\"ListNumber\"/><w:lvlText w:val=\"%1.\"/></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>";

	private static final String LOOK = "<w:tblLook w:val=\"0620\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\""
			+ " w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>";

	private static String p(String pStyle, String text) {
		return "<w:p>" + (pStyle == null ? "" : "<w:pPr><w:pStyle w:val=\"" + pStyle + "\"/></w:pPr>")
				+ "<w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String table(String style, String... rows) {
		StringBuilder sb = new StringBuilder("<w:tbl><w:tblPr><w:tblStyle w:val=\"" + style + "\"/>" + LOOK
				+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>");
		for (String row : rows) {
			sb.append("<w:tr><w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr>").append(row).append("</w:tc></w:tr>");
		}
		return sb.append("</w:tbl>").toString();
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setContents((Styles) XmlUtils.unmarshalString(STYLES));
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
		return pkg;
	}

	private static List<P> paragraphs(WordprocessingMLPackage pkg) {
		final List<P> out = new ArrayList<P>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) out.add((P) o);
				return null;
			}
		});
		return out;
	}

	private static P para(WordprocessingMLPackage pkg, String text) {
		for (P p : paragraphs(pkg)) {
			if (TextUtils.getText(p).startsWith(text)) return p;
		}
		throw new AssertionError("no paragraph " + text);
	}

	private static String pStyle(P p) {
		return p.getPPr() == null || p.getPPr().getPStyle() == null ? null : p.getPPr().getPStyle().getVal();
	}

	private static RPr runRPr(WordprocessingMLPackage pkg, P p) throws Exception {
		return pkg.getMainDocumentPart().getPropertyResolver().getEffectiveRPr(null, p.getPPr());
	}

	/** D1: a style-numbered paragraph in a cell keeps its number, and the count runs on. */
	@Test
	public void numberedParagraphsInCellsKeepTheirNumbers() throws Exception {
		WordprocessingMLPackage pkg = pkg(p("ListNumber", "outside one")
				+ table("Grid", p("ListNumber", "header item"), p("ListNumber", "body item"))
				+ p("ListNumber", "outside two"));
		ParagraphStylesInTableFix.process(pkg);
		assertEquals("ListNumber-Grid-firstRow-BR", pStyle(para(pkg, "header item")));
		assertEquals("ListNumber", pkg.getMainDocumentPart().getPropertyResolver().sourceStyleOf("ListNumber-Grid-firstRow-BR"));
		String[] expected = { "1.", "2.", "3.", "4." };
		String[] texts = { "outside one", "header item", "body item", "outside two" };
		for (int i = 0; i < texts.length; i++) {
			Emulator.ResultTriple t = Emulator.getNumber(pkg, para(pkg, texts[i]).getPPr());
			assertEquals(texts[i], expected[i], t == null ? null : t.getNumString());
		}
	}

	/** D2: a text box anchored in a cell is a story of its own: not renamed, not bold. */
	@Test
	public void aTextBoxInACellIsLeftAlone() throws Exception {
		WordprocessingMLPackage pkg = pkg(table("Grid",
				"<w:p><w:r><w:t>anchor</w:t></w:r><w:r><w:pict><v:shape style=\"width:100pt;height:50pt\"><v:textbox>"
				+ "<w:txbxContent>" + p(null, "in the box") + "</w:txbxContent></v:textbox></v:shape></w:pict></w:r></w:p>"));
		ParagraphStylesInTableFix.process(pkg);
		assertEquals("Normal-Grid-firstRow-BR", pStyle(para(pkg, "anchor")));
		assertTrue(runRPr(pkg, para(pkg, "anchor")).getB().isVal());
		P inBox = para(pkg, "in the box");
		assertNull(pStyle(inBox));
		assertNull(runRPr(pkg, inBox).getB());
	}

	/** D9: a missing style is the default's, and the walk goes on past it. */
	@Test
	public void aMissingStyleDoesNotStopTheWalk() throws Exception {
		WordprocessingMLPackage pkg = pkg(table("Grid", p(null, "first"), p("Gone", "missing"), p(null, "third"))
				+ table("Grid", p(null, "later table")));
		ParagraphStylesInTableFix.process(pkg);
		assertEquals("Normal-Grid-firstRow-BR", pStyle(para(pkg, "first")));
		assertEquals("Normal-Grid-BR", pStyle(para(pkg, "missing")));
		assertEquals("Normal-Grid-BR", pStyle(para(pkg, "third")));
		assertEquals("Normal-Grid-firstRow-BR", pStyle(para(pkg, "later table")));
	}

	/** D8: A-B in C and A in B-C would both be A-B-C-BR; each gets its own style. */
	@Test
	public void collidingNamesGetDistinctIds() throws Exception {
		WordprocessingMLPackage pkg = pkg(table("C", p("A-B", "first")) + table("B-C", p("A", "second")));
		ParagraphStylesInTableFix.process(pkg);
		String first = pStyle(para(pkg, "first"));
		String second = pStyle(para(pkg, "second"));
		assertNotEquals(first, second);
		RPr one = runRPr(pkg, para(pkg, "first"));
		assertTrue(one.getI().isVal());
		assertEquals("FF0000", one.getColor().getVal());
		assertNull(one.getB());
		RPr two = runRPr(pkg, para(pkg, "second"));
		assertTrue(two.getB().isVal());
		assertNull(two.getI());
		assertNull(two.getColor());
	}

	/** A second run (FOPAreaTreeHelper's guard aside) gives the same ids, and the settings
	 *  part is not rewritten. */
	@Test
	public void aSecondRunGivesTheSameIdsAndTheSettingsAreUntouched() throws Exception {
		WordprocessingMLPackage pkg = pkg(table("Grid", p(null, "header"), p("ListNumber", "body")));
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		dsp.setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "0");
		ParagraphStylesInTableFix.process(pkg);
		String header = pStyle(para(pkg, "header"));
		String body = pStyle(para(pkg, "body"));
		ParagraphStylesInTableFix.process(pkg);
		assertEquals(header, pStyle(para(pkg, "header")));
		assertEquals(body, pStyle(para(pkg, "body")));
		assertEquals("0", dsp.getWordCompatSetting("overrideTableStyleFontSizeAndJustification").getVal());
	}

	/** D6: in compatibility mode 15 the [MS-DOCX] exception does not apply, so a 12pt Normal
	 *  keeps its size over the table style's (CR-030 T5). */
	@Test
	public void noSizeExceptionInMode15() throws Exception {
		String styles = STYLES.replace("<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>",
				"<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/><w:rPr><w:sz w:val=\"24\"/></w:rPr></w:style>")
				.replace("<w:style w:type=\"table\" w:styleId=\"C\"><w:name w:val=\"C\"/><w:rPr><w:i/></w:rPr></w:style>",
				"<w:style w:type=\"table\" w:styleId=\"C\"><w:name w:val=\"C\"/><w:rPr><w:sz w:val=\"18\"/></w:rPr></w:style>");
		for (String mode : new String[] { "15", "14" }) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(
					"<w:document " + W + "><w:body>" + table("C", p(null, "cell")) + "</w:body></w:document>"));
			pkg.getMainDocumentPart().getStyleDefinitionsPart().setContents((Styles) XmlUtils.unmarshalString(styles));
			DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
			dsp.setWordCompatSetting("compatibilityMode", mode);
			dsp.setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "0");
			pkg.getMainDocumentPart().getPropertyResolver().refresh();
			ParagraphStylesInTableFix.process(pkg);
			int sz = runRPr(pkg, para(pkg, "cell")).getSz().getVal().intValue();
			assertEquals("mode " + mode, "15".equals(mode) ? 24 : 18, sz);
		}
	}

	/** A paragraph in a table which takes Table Normal (no w:tblStyle) is left as written. */
	@Test
	public void aTableTakingNormalTableIsNotRenamed() throws Exception {
		WordprocessingMLPackage pkg = pkg("<w:tbl><w:tblPr>" + LOOK + "</w:tblPr><w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>"
				+ "<w:tr><w:tc>" + p(null, "plain") + "</w:tc></w:tr></w:tbl>");
		ParagraphStylesInTableFix.process(pkg);
		assertNull(pStyle(para(pkg, "plain")));
		assertFalse(pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().stream()
				.anyMatch(s -> s.getStyleId().endsWith("-BR")));
	}
}
