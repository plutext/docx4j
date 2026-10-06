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
package org.docx4j.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.Document;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.Styles;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.Test;

/**
 * Table styles resolved by {@link PropertyResolver} given a paragraph's {@link CellContext}
 * (CR-030 phase 1): the same answers {@code ParagraphStylesInTableFix} gives through its
 * synthetic styles (the fixture and the expected values of
 * {@code ParagraphStylesInTableFixConditionalTest}, and a paragraph-by-paragraph comparison
 * with the preprocess), the [MS-DOCX] exception as Word 365 applies it (probes T5 and T6), the
 * name rule for which table style applies (T1, T2, T7), and the ways of finding the context.
 */
public class TableContextResolutionTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\"";

	// ------------------------------------------------ ParagraphStylesInTableFixConditionalTest's fixture

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"20\"/></w:rPr></w:rPrDefault>"
			+ "<w:pPrDefault><w:pPr><w:spacing w:after=\"200\"/></w:pPr></w:pPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"NoBold\"><w:name w:val=\"No Bold\"/>"
			+ "<w:basedOn w:val=\"Normal\"/><w:rPr><w:b w:val=\"0\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"Base\"><w:name w:val=\"Base\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:rPr><w:sz w:val=\"18\"/></w:rPr>"
			// band sizes, as Word's own banded styles state them (with none, Word bands nothing: CR-030 T10)
			+ "<w:tblPr><w:tblStyleRowBandSize w:val=\"1\"/><w:tblStyleColBandSize w:val=\"1\"/></w:tblPr>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/><w:color w:val=\"FF0000\"/></w:rPr>"
			+ "<w:trPr><w:tblHeader/></w:trPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"firstCol\"><w:rPr><w:i/></w:rPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"band1Horz\"><w:rPr><w:u w:val=\"single\"/></w:rPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"lastRow\"><w:rPr><w:strike/></w:rPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"nwCell\"><w:rPr><w:color w:val=\"0000FF\"/></w:rPr></w:tblStylePr>"
			+ "</w:style>"
			+ "<w:style w:type=\"table\" w:customStyle=\"1\" w:styleId=\"Child\"><w:name w:val=\"Child\"/>"
			+ "<w:basedOn w:val=\"Base\"/>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b w:val=\"0\"/></w:rPr></w:tblStylePr>"
			+ "</w:style>"
			+ "</w:styles>";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String p(String pStyle, String runRPr, String text) {
		return "<w:p>" + (pStyle == null ? "" : "<w:pPr><w:pStyle w:val=\"" + pStyle + "\"/></w:pPr>")
				+ "<w:r>" + (runRPr == null ? "" : "<w:rPr>" + runRPr + "</w:rPr>") + "<w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String tc(String content) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>" + content + "</w:tc>";
	}

	private static final String TABLE1 = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Child\"/>"
			+ "<w:tblLook w:val=\"0000\" w:firstRow=\"1\" w:lastRow=\"1\" w:firstColumn=\"1\" w:lastColumn=\"0\" w:noHBand=\"0\" w:noVBand=\"1\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("t1 r0 c0")) + tc(p("t1 r0 c1")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t1 r1 c0")) + tc(
					p(null, "<w:u w:val=\"none\"/>", "t1 r1 c1 direct")
					+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>"
					+ "<w:tr>" + tc(p("nested r0 c0")) + "</w:tr></w:tbl>"
					+ p("t1 r1 c1 after")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t1 r2 c0")) + tc(p("t1 r2 c1")) + "</w:tr>"
			+ "</w:tbl>";

	private static final String TABLE2 = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/>"
			+ "<w:tblLook w:val=\"0000\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("t2 r0 c0")) + tc(p("t2 r0 c1")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t2 r1 c0")) + tc(p("t2 r1 c1")) + "</w:tr>"
			+ "</w:tbl>";

	private static final String TABLE3 = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("t3 r0 c0")) + tc(p("NoBold", null, "t3 r0 c1")) + "</w:tr>"
			+ "<w:tr><w:trPr><w:cnfStyle w:val=\"100000000000\"/></w:trPr>"
			+ tc(p("t3 r1 c0")) + tc(p("t3 r1 c1")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t3 r2 c0")) + tc(p("t3 r2 c1")) + "</w:tr>"
			+ "</w:tbl>";

	private static final String DOC = "<w:document " + W + "><w:body>"
			+ p("outside") + TABLE1 + p("between") + TABLE2 + p("between") + TABLE3
			+ "</w:body></w:document>";

	private static WordprocessingMLPackage pkg(String doc, String styles) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(doc));
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setContents((Styles) XmlUtils.unmarshalString(styles));
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
		return pkg;
	}

	/** Every paragraph of the main document in walk order, with its context from a tracker. */
	private static Map<P, CellContext> walk(WordprocessingMLPackage pkg) throws Exception {
		final PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		final Map<P, CellContext> out = new IdentityHashMap<P, CellContext>();
		final List<P> order = new ArrayList<P>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TableContextTracker(resolver) {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) {
					out.put((P) o, cellContext((P) o));
					order.add((P) o);
				}
				return null;
			}
		});
		Map<P, CellContext> ordered = new LinkedHashMap<P, CellContext>();
		for (P p : order) ordered.put(p, out.get(p));
		return ordered;
	}

	private static P para(Map<P, CellContext> paras, String text) {
		for (P p : paras.keySet()) {
			if (TextUtils.getText(p).startsWith(text)) return p;
		}
		throw new AssertionError("no paragraph " + text);
	}

	private static R firstRun(P p) {
		for (Object o : p.getContent()) {
			if (o instanceof R) return (R) o;
		}
		return null;
	}

	private static RPr run(WordprocessingMLPackage pkg, Map<P, CellContext> paras, String text) throws Exception {
		P p = para(paras, text);
		return pkg.getMainDocumentPart().getPropertyResolver()
				.getEffectiveRPr(firstRun(p).getRPr(), p.getPPr(), paras.get(p));
	}

	@Test
	public void precedenceAndInheritance() throws Exception {
		WordprocessingMLPackage pkg = pkg(DOC, STYLES);
		Map<P, CellContext> paras = walk(pkg);

		assertNull(paras.get(para(paras, "outside")));

		// top-left: the Base's 9pt; firstCol's italic; firstRow's bold restated off by Child;
		// nwCell's blue over firstRow's red
		RPr rPr = run(pkg, paras, "t1 r0 c0");
		assertEquals(18, rPr.getSz().getVal().intValue());
		assertTrue(rPr.getI().isVal());
		assertFalse(rPr.getB().isVal());
		assertEquals("0000FF", rPr.getColor().getVal());
		assertNull(rPr.getU());

		rPr = run(pkg, paras, "t1 r0 c1");
		assertFalse(rPr.getB().isVal());
		assertEquals("FF0000", rPr.getColor().getVal());
		assertNull(rPr.getI());

		rPr = run(pkg, paras, "t1 r1 c0");
		assertEquals(UnderlineEnumeration.SINGLE, rPr.getU().getVal());
		assertTrue(rPr.getI().isVal());
		assertNull(rPr.getB());
		assertNull(rPr.getColor());

		rPr = run(pkg, paras, "t1 r2 c0");
		assertTrue(rPr.getStrike().isVal());
		assertTrue(rPr.getI().isVal());
		assertNull(rPr.getU());

		// the document defaults are beneath the table level
		P p = para(paras, "t1 r0 c1");
		PPr pPr = pkg.getMainDocumentPart().getPropertyResolver().getEffectivePPr(p.getPPr(), paras.get(p));
		assertEquals(200, pPr.getSpacing().getAfter().intValue());
	}

	@Test
	public void tblLookGatesTheConditionsAndTheCacheWins() throws Exception {
		WordprocessingMLPackage pkg = pkg(DOC, STYLES);
		Map<P, CellContext> paras = walk(pkg);

		// table 2, w:val="0000": no first row or column, so row 0 is the first band
		RPr rPr = run(pkg, paras, "t2 r0 c0");
		assertNull(rPr.getB());
		assertNull(rPr.getI());
		assertEquals(UnderlineEnumeration.SINGLE, rPr.getU().getVal());
		rPr = run(pkg, paras, "t2 r1 c0"); // band2Horz is not defined: the Base's 9pt alone
		assertNull(rPr.getU());
		assertEquals(18, rPr.getSz().getVal().intValue());

		// table 3, no w:tblLook: Word's default look; row 1's w:cnfStyle says firstRow
		assertTrue(run(pkg, paras, "t3 r0 c0").getB().isVal());
		assertTrue(run(pkg, paras, "t3 r1 c0").getB().isVal());
		assertEquals(paras.get(para(paras, "t3 r1 c0")).getConditions(),
				java.util.EnumSet.of(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.FIRST_COL,
						STTblStyleOverrideType.NW_CELL));
		rPr = run(pkg, paras, "t3 r2 c1");
		assertNull(rPr.getB());
		assertNull(rPr.getI());
	}

	@Test
	public void paragraphStyleIsALevelAndDirectFormattingWins() throws Exception {
		WordprocessingMLPackage pkg = pkg(DOC, STYLES);
		Map<P, CellContext> paras = walk(pkg);
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();

		// NoBold's b=0 over firstRow's b: two levels, so false XOR true = true (17.7.3)
		RPr rPr = run(pkg, paras, "t3 r0 c1");
		assertTrue(rPr.getB().isVal());
		assertEquals("FF0000", rPr.getColor().getVal());
		// direct formatting is not a level, and wins outright
		P p = para(paras, "t3 r0 c1");
		RPr off = (RPr) XmlUtils.unmarshalString("<w:rPr " + W + "><w:b w:val=\"0\"/></w:rPr>", Context.jc, RPr.class);
		assertFalse(resolver.getEffectiveRPr(off, p.getPPr(), paras.get(p)).getB().isVal());

		rPr = run(pkg, paras, "t1 r1 c1 direct");
		assertEquals(UnderlineEnumeration.NONE, rPr.getU().getVal());
		assertEquals(18, rPr.getSz().getVal().intValue());
		// after the nested table, the outer cell is still in the outer table's band
		assertEquals(UnderlineEnumeration.SINGLE, run(pkg, paras, "t1 r1 c1 after").getU().getVal());
		// and the nested table uses its own style and position
		assertTrue(run(pkg, paras, "nested r0 c0").getB().isVal());
		assertEquals("Base", paras.get(para(paras, "nested r0 c0")).getTableStyleId());
	}

	/**
	 * Paragraph by paragraph, run by run and mark by mark: the answer in context equals the
	 * one the preprocess's synthetic style gives, on a second copy of the same document.
	 */
	@Test
	public void theSameAnswersAsTheSyntheticStyles() throws Exception {
		WordprocessingMLPackage pkg = pkg(DOC, STYLES);
		Map<P, CellContext> paras = walk(pkg);
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		List<String> inContext = new ArrayList<String>();
		for (Map.Entry<P, CellContext> e : paras.entrySet()) {
			P p = e.getKey();
			inContext.add(TextUtils.getText(p)
					+ " pPr " + xml(resolver.getEffectivePPr(p.getPPr(), e.getValue()))
					+ " mark " + xml(resolver.getEffectiveParagraphMarkRPr(p.getPPr(), e.getValue()))
					+ " run " + xml(resolver.getEffectiveRPr(firstRun(p).getRPr(), p.getPPr(), e.getValue())));
		}

		WordprocessingMLPackage copy = pkg(DOC, STYLES);
		ParagraphStylesInTableFix.process(copy);
		PropertyResolver plain = copy.getMainDocumentPart().getPropertyResolver();
		List<String> synthetic = new ArrayList<String>();
		for (P p : walk(copy).keySet()) {
			synthetic.add(TextUtils.getText(p)
					+ " pPr " + xml(plain.getEffectivePPr(p.getPPr()))
					+ " mark " + xml(plain.getEffectiveParagraphMarkRPr(p.getPPr()))
					+ " run " + xml(plain.getEffectiveRPr(firstRun(p).getRPr(), p.getPPr())));
		}
		assertEquals(synthetic.size(), inContext.size());
		for (int i = 0; i < synthetic.size(); i++) {
			assertEquals(synthetic.get(i), inContext.get(i));
		}
	}

	/** An effective pPr or rPr without its w:pStyle (the synthetic styles' ids differ by design). */
	private static String xml(Object o) {
		if (o instanceof PPr) {
			PPr copy = XmlUtils.deepCopy((PPr) o);
			copy.setPStyle(null);
			o = copy;
		}
		return XmlUtils.marshaltoString(o, true, false, Context.jc).replaceAll("\\s+", " ");
	}

	// ------------------------------------------------ [MS-DOCX] overrideTableStyleFontSizeAndJustification

	/** CR-030 probe T5/T6's document: Normal states 12pt and left; the table style 9pt right,
	 *  its firstRow 14pt centred; ProbeA inherits 10pt from ProbeB; ProbeC states 16pt. */
	private static final String COMPAT_STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"24\"/></w:rPr></w:rPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/>"
			+ "<w:pPr><w:jc w:val=\"left\"/></w:pPr><w:rPr><w:sz w:val=\"24\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"ProbeB\"><w:name w:val=\"ProbeB\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:rPr><w:sz w:val=\"20\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"ProbeA\"><w:name w:val=\"ProbeA\"/><w:basedOn w:val=\"ProbeB\"/></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"ProbeC\"><w:name w:val=\"ProbeC\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:rPr><w:sz w:val=\"32\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"ProbeCompat\"><w:name w:val=\"ProbeCompat\"/>"
			+ "<w:pPr><w:jc w:val=\"right\"/></w:pPr><w:rPr><w:sz w:val=\"18\"/></w:rPr>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:rPr><w:sz w:val=\"28\"/></w:rPr></w:tblStylePr>"
			+ "</w:style></w:styles>";

	private static final String COMPAT_DOC = "<w:document " + W + "><w:body>"
			+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"ProbeCompat\"/>"
			+ "<w:tblLook w:val=\"0620\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("(a) header")) + tc(p("ProbeA", null, "(b) header")) + tc(p("ProbeC", null, "(c) header")) + "</w:tr>"
			+ "<w:tr>" + tc(p("(a) body")) + tc(p("ProbeA", null, "(b) body")) + tc(p("ProbeC", null, "(c) body")) + "</w:tr>"
			+ "</w:tbl></w:body></w:document>";

	/** The compat probe in a mode (null: no compatibilityMode at all, as Word 2007 writes it)
	 *  with the setting (null: absent). */
	private static WordprocessingMLPackage compatPkg(Integer mode, String setting) throws Exception {
		WordprocessingMLPackage pkg = pkg(COMPAT_DOC, COMPAT_STYLES);
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		CTCompat compat = dsp.getContents().getCompat();
		compat.getCompatSetting().removeIf(cs -> "compatibilityMode".equals(cs.getName())
				|| "overrideTableStyleFontSizeAndJustification".equals(cs.getName()));
		if (mode != null) dsp.setWordCompatSetting("compatibilityMode", String.valueOf(mode));
		if (setting != null) dsp.setWordCompatSetting("overrideTableStyleFontSizeAndJustification", setting);
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
		return pkg;
	}

	/** {size in half-points, justification} of a cell's paragraph. */
	private static void assertCell(WordprocessingMLPackage pkg, Map<P, CellContext> paras, String text,
			int sz, JcEnumeration jc) throws Exception {
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		P p = para(paras, text);
		assertEquals(text + " size", sz, resolver.getEffectiveRPr(firstRun(p).getRPr(), p.getPPr(), paras.get(p))
				.getSz().getVal().intValue());
		assertEquals(text + " jc", jc, resolver.getEffectivePPr(p.getPPr(), paras.get(p)).getJc().getVal());
	}

	/** T5 and T6's mode 15 documents: ECMA-376's order whatever the setting says. */
	@Test
	public void noExceptionInMode15() throws Exception {
		for (String setting : new String[] { "0", null, "1" }) {
			WordprocessingMLPackage pkg = compatPkg(15, setting);
			assertFalse(pkg.getMainDocumentPart().getPropertyResolver().appliesTableStyleSizeJcException());
			Map<P, CellContext> paras = walk(pkg);
			assertCell(pkg, paras, "(a) header", 24, JcEnumeration.LEFT);
			assertCell(pkg, paras, "(a) body", 24, JcEnumeration.LEFT);
			assertCell(pkg, paras, "(b) header", 20, JcEnumeration.LEFT);
			assertCell(pkg, paras, "(c) body", 32, JcEnumeration.LEFT);
		}
	}

	/** T6's mode 12 and 14 documents, setting absent: the exception, as Word 365 draws it. */
	@Test
	public void theExceptionBelowMode15() throws Exception {
		for (Integer mode : new Integer[] { null, 12, 14 }) {
			WordprocessingMLPackage pkg = compatPkg(mode, null);
			assertTrue(pkg.getMainDocumentPart().getPropertyResolver().appliesTableStyleSizeJcException());
			Map<P, CellContext> paras = walk(pkg);
			assertCell(pkg, paras, "(a) header", 28, JcEnumeration.CENTER);
			assertCell(pkg, paras, "(a) body", 18, JcEnumeration.RIGHT);
			// a style of the paragraph's own which resolves to another size keeps it
			assertCell(pkg, paras, "(b) header", 20, JcEnumeration.CENTER);
			assertCell(pkg, paras, "(b) body", 20, JcEnumeration.RIGHT);
			assertCell(pkg, paras, "(c) header", 32, JcEnumeration.CENTER);
			assertCell(pkg, paras, "(c) body", 32, JcEnumeration.RIGHT);
		}
		// the setting on turns it off below mode 15 too
		WordprocessingMLPackage pkg = compatPkg(14, "1");
		assertFalse(pkg.getMainDocumentPart().getPropertyResolver().appliesTableStyleSizeJcException());
		assertCell(pkg, walk(pkg), "(a) header", 24, JcEnumeration.LEFT);
	}

	/** The table-style-size-trigger probes' document (CR-001 batch 53, corpus document 1912):
	 *  docDefaults 11pt, Normal 12pt, one two-row table per style. */
	private static final String TRIGGER_STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"22\"/></w:rPr></w:rPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/>"
			+ "<w:rPr><w:sz w:val=\"24\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"TableGrid\"><w:name w:val=\"Table Grid\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"PlainGrid\"><w:name w:val=\"PlainGrid\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:tblPr><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders></w:tblPr></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"RPrColour\"><w:name w:val=\"RPrColour\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:rPr><w:color w:val=\"1F3864\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"FirstRowOnly\"><w:name w:val=\"FirstRowOnly\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/></w:rPr></w:tblStylePr></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"Nothing\"><w:name w:val=\"Nothing\"/><w:basedOn w:val=\"TableNormal\"/></w:style>"
			+ "</w:styles>";

	private static final String[] TRIGGER_TABLES = { "TableGrid", "PlainGrid", "RPrColour", "FirstRowOnly", "Nothing" };

	private static String triggerDoc() {
		StringBuilder sb = new StringBuilder("<w:document " + W + "><w:body>");
		for (String style : TRIGGER_TABLES) {
			sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"").append(style).append("\"/>")
					.append("<w:tblLook w:val=\"04A0\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>")
					.append("</w:tblPr><w:tblGrid><w:gridCol w:w=\"3000\"/></w:tblGrid>")
					.append("<w:tr>").append(tc(p(style + " row 1"))).append("</w:tr>")
					.append("<w:tr>").append(tc(p(style + " row 2"))).append("</w:tr></w:tbl>").append(p("after " + style));
		}
		return sb.append("</w:body></w:document>").toString();
	}

	/**
	 * Below mode 15 the size exception applies wherever the table style formats text at all -
	 * a w:pPr, an rPr without a size, a conditional format, whether or not it applies to the
	 * row - and gives the table level's size, docDefaults' where the style states none.  A style
	 * which formats no text leaves Normal's 12pt.  In mode 15 every table takes Normal's.
	 * Measured: the table-style-size-trigger probes (CR-001 batch 53).  And where the
	 * docDefaults state no size either, Normal keeps its 12pt (corpus documents 10244 and
	 * 12723, gate b148).  @since 17.3.1
	 */
	@Test
	public void theExceptionWhereTheTableStyleFormatsTextWithoutASize() throws Exception {
		for (boolean docDefaultsSize : new boolean[] { true, false })
		for (Integer mode : new Integer[] { 12, 15 }) {
			String styles = docDefaultsSize ? TRIGGER_STYLES
					: TRIGGER_STYLES.replace("<w:rPrDefault><w:rPr><w:sz w:val=\"22\"/></w:rPr></w:rPrDefault>", "");
			WordprocessingMLPackage pkg = pkg(triggerDoc(), styles);
			DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
			dsp.getContents().getCompat().getCompatSetting().removeIf(cs -> "compatibilityMode".equals(cs.getName())
					|| "overrideTableStyleFontSizeAndJustification".equals(cs.getName()));
			dsp.setWordCompatSetting("compatibilityMode", String.valueOf(mode));
			PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
			resolver.refresh();
			Map<P, CellContext> paras = walk(pkg);
			for (String style : TRIGGER_TABLES) {
				boolean formats = !style.equals("PlainGrid") && !style.equals("Nothing");
				int expected = mode == 12 && formats && docDefaultsSize ? 22 : 24;
				for (String row : new String[] { " row 1", " row 2" }) {
					P p = para(paras, style + row);
					assertEquals("mode " + mode + (docDefaultsSize ? "" : ", no docDefaults size") + ", " + style + row, expected,
							resolver.getEffectiveRPr(firstRun(p).getRPr(), p.getPPr(), paras.get(p)).getSz().getVal().intValue());
				}
			}
			P after = para(paras, "after TableGrid");
			assertEquals("outside a table, Normal's", 24,
					resolver.getEffectiveRPr(firstRun(after).getRPr(), after.getPPr(), paras.get(after)).getSz().getVal().intValue());
		}
	}

	// ------------------------------------------------ which table style applies (T1, T2, T7)

	private static String namesStyles(String defaultName) {
		return "<w:styles " + W + ">"
				+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"24\"/></w:rPr></w:rPrDefault></w:docDefaults>"
				+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
				+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"" + defaultName + "\"/>"
				+ "<w:rPr><w:sz w:val=\"40\"/></w:rPr></w:style>"
				+ "<w:style w:type=\"table\" w:styleId=\"OtherNormal\"><w:name w:val=\"Normal Table\"/>"
				+ "<w:rPr><w:sz w:val=\"40\"/></w:rPr></w:style>"
				+ "<w:style w:type=\"table\" w:styleId=\"Grid2\"><w:name w:val=\"Grid2\"/><w:basedOn w:val=\"TableNormal\"/>"
				+ "<w:rPr><w:i/></w:rPr></w:style>"
				+ "</w:styles>";
	}

	private static String namesDoc() {
		String look = "<w:tblLook w:val=\"0000\"/>";
		return "<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"OtherNormal\"/>" + look + "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr>" + tc(p("other")) + "</w:tr></w:tbl>"
				+ "<w:tbl><w:tblPr>" + look + "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr>" + tc(p("none")) + "</w:tr></w:tbl>"
				+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"Grid2\"/>" + look + "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr>" + tc(p("grid2")) + "</w:tr></w:tbl>"
				+ "</w:body></w:document>";
	}

	@Test
	public void aStyleNamedNormalTableGivesTextNothing() throws Exception {
		// T1 and T2: the default named Normal Table, and a non-default style so named
		WordprocessingMLPackage pkg = pkg(namesDoc(), namesStyles("Normal Table"));
		Map<P, CellContext> paras = walk(pkg);
		assertEquals(24, run(pkg, paras, "other").getSz().getVal().intValue());
		assertEquals(24, run(pkg, paras, "none").getSz().getVal().intValue());
		// a chain through a style named Normal Table ends below it
		RPr grid2 = run(pkg, paras, "grid2");
		assertEquals(24, grid2.getSz().getVal().intValue());
		assertTrue(grid2.getI().isVal());
	}

	@Test
	public void aRenamedDefaultAppliesAsWritten() throws Exception {
		// T2 and T7: the default table style named otherwise applies, to a table naming none
		// and through a style based on it
		WordprocessingMLPackage pkg = pkg(namesDoc(), namesStyles("My Default"));
		Map<P, CellContext> paras = walk(pkg);
		assertEquals(24, run(pkg, paras, "other").getSz().getVal().intValue());
		assertEquals("TableNormal", paras.get(para(paras, "none")).getTableStyleId());
		assertEquals(40, run(pkg, paras, "none").getSz().getVal().intValue());
		RPr grid2 = run(pkg, paras, "grid2");
		assertEquals(40, grid2.getSz().getVal().intValue());
		assertTrue(grid2.getI().isVal());
	}

	// ------------------------------------------------ finding the context

	private static final String FIND_DOC = "<w:document " + W + "><w:body>"
			+ p("body")
			+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/>"
			+ "<w:tblLook w:val=\"0620\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("header")
					+ "<w:p><w:r><w:t>anchor</w:t></w:r><w:r><w:pict><v:shape style=\"width:100pt;height:50pt\"><v:textbox><w:txbxContent>"
					+ p("in the box") + "</w:txbxContent></v:textbox></v:shape></w:pict></w:r></w:p>"
					+ p("after the box"))
			+ "<w:sdt><w:sdtPr/><w:sdtContent>" + tc("<w:sdt><w:sdtPr/><w:sdtContent>" + p("header in sdt") + "</w:sdtContent></w:sdt>") + "</w:sdtContent></w:sdt>"
			+ "</w:tr>"
			+ "<w:tr>" + tc(p("body row")) + tc(p("body row 2")) + "</w:tr>"
			+ "</w:tbl></w:body></w:document>";

	@Test
	public void theTrackerAndCellContextOfAgree() throws Exception {
		WordprocessingMLPackage pkg = pkg(FIND_DOC, STYLES);
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		Map<P, CellContext> paras = walk(pkg);

		assertNull(paras.get(para(paras, "body")));
		assertNull(resolver.cellContextOf(para(paras, "body")));
		for (String text : new String[] { "header", "after the box", "header in sdt" }) {
			P p = para(paras, text);
			assertNotNull(text, paras.get(p));
			assertTrue(text, paras.get(p).getConditions().contains(STTblStyleOverrideType.FIRST_ROW));
			assertEquals(text, paras.get(p).getKey(), resolver.cellContextOf(p).getKey());
			assertTrue(text, run(pkg, paras, text).getB().isVal());
		}
		P body = para(paras, "body row 2");
		assertTrue(paras.get(body).getConditions().isEmpty());
		assertEquals(paras.get(body).getKey(), resolver.cellContextOf(body).getKey());

		// a text box is a story of its own (CR-030 T3): no table context inside it, in the
		// walk or through the parents
		P inBox = para(paras, "in the box");
		assertNull(paras.get(inBox));
		assertNull(resolver.cellContextOf(inBox));
		assertNull(run(pkg, paras, "in the box").getB());
	}

	@Test
	public void aParagraphWithoutParentPointersHasNoContext() throws Exception {
		WordprocessingMLPackage pkg = pkg(FIND_DOC, STYLES);
		P created = Context.getWmlObjectFactory().createP();
		org.docx4j.wml.Tc tc = Context.getWmlObjectFactory().createTc();
		tc.getContent().add(created);
		assertNull(pkg.getMainDocumentPart().getPropertyResolver().cellContextOf(created));
	}

	// ------------------------------------------------ CR-030 D8 and D9

	@Test
	public void hyphenatedIdsDoNotCollide() throws Exception {
		// paragraph style A-B in table style C, and A in B-C: the preprocess's ids were both A-B-C-BR
		String styles = "<w:styles " + W + ">"
				+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"24\"/></w:rPr></w:rPrDefault></w:docDefaults>"
				+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
				+ "<w:style w:type=\"paragraph\" w:styleId=\"A-B\"><w:name w:val=\"A-B\"/><w:rPr><w:color w:val=\"FF0000\"/></w:rPr></w:style>"
				+ "<w:style w:type=\"paragraph\" w:styleId=\"A\"><w:name w:val=\"A\"/></w:style>"
				+ "<w:style w:type=\"table\" w:styleId=\"C\"><w:name w:val=\"C\"/><w:rPr><w:i/></w:rPr></w:style>"
				+ "<w:style w:type=\"table\" w:styleId=\"B-C\"><w:name w:val=\"B-C\"/><w:rPr><w:b/></w:rPr></w:style>"
				+ "</w:styles>";
		String doc = "<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"C\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr>" + tc(p("A-B", null, "first")) + "</w:tr></w:tbl>"
				+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"B-C\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr>" + tc(p("A", null, "second")) + "</w:tr></w:tbl>"
				+ "</w:body></w:document>";
		WordprocessingMLPackage pkg = pkg(doc, styles);
		Map<P, CellContext> paras = walk(pkg);
		RPr second = run(pkg, paras, "second");
		assertTrue(second.getB().isVal());
		assertNull(second.getI());
		assertNull(second.getColor());
		RPr first = run(pkg, paras, "first");
		assertNull(first.getB());
		assertTrue(first.getI().isVal());
		assertEquals("FF0000", first.getColor().getVal());
	}

	@Test
	public void aMissingStyleInACellResolvesAsTheDefault() throws Exception {
		WordprocessingMLPackage pkg = pkg(FIND_DOC.replace(p("body row"), p("Gone", null, "body row")), STYLES);
		Map<P, CellContext> paras = walk(pkg);
		RPr rPr = run(pkg, paras, "body row");
		assertEquals(18, rPr.getSz().getVal().intValue()); // the Base's 9pt, over Normal
		// and the paragraphs after it are resolved too (the preprocess stopped at it: D9)
		assertEquals(18, run(pkg, paras, "body row 2").getSz().getVal().intValue());
	}

	@Test
	public void refreshSeesAChangedTableStyle() throws Exception {
		WordprocessingMLPackage pkg = pkg(DOC, STYLES);
		Map<P, CellContext> paras = walk(pkg);
		assertEquals(18, run(pkg, paras, "t2 r1 c0").getSz().getVal().intValue());
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getStyleById("Base").getRPr().getSz()
				.setVal(java.math.BigInteger.valueOf(30));
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
		paras = walk(pkg);
		assertEquals(30, run(pkg, paras, "t2 r1 c0").getSz().getVal().intValue());
	}
}
