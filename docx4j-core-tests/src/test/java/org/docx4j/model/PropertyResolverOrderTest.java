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
package org.docx4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Before;
import org.junit.Test;

/**
 * The resolution order, ECMA-376 17.7.2 as Word applies it (CR-015 phase 2, goldens
 * styles-default-pstyle (a)-(g)): document defaults, the paragraph style's chain - the
 * default paragraph style for a paragraph naming none or naming a missing one - the
 * character style's chain, then direct formatting; the paragraph mark's rPr on the mark
 * only; and no answer depending on which caller came first.
 *
 * The styles part: docDefaults Calibri 11pt lang en-US; Normal (default) 14pt Serif;
 * H based on Normal, Arial 20pt; character style S (w:b) and DefaultParagraphFont.
 *
 * @since 17.1.1
 */
public class PropertyResolverOrderTest {

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	private Styles styles;
	private WordprocessingMLPackage pkg;

	@Before
	public void setUp() throws Exception {
		styles = F.createStyles();
		DocDefaults dd = F.createDocDefaults();
		DocDefaults.RPrDefault rd = F.createDocDefaultsRPrDefault();
		RPr ddr = F.createRPr();
		ddr.setRFonts(fonts("Calibri"));
		ddr.setSz(hps(22));
		CTLanguage lang = F.createCTLanguage();
		lang.setVal("en-US");
		ddr.setLang(lang);
		rd.setRPr(ddr);
		dd.setRPrDefault(rd);
		DocDefaults.PPrDefault pd = F.createDocDefaultsPPrDefault();
		PPr ddp = F.createPPr();
		PPrBase.Spacing sp = F.createPPrBaseSpacing();
		sp.setAfter(BigInteger.valueOf(160));
		ddp.setSpacing(sp);
		pd.setPPr(ddp);
		dd.setPPrDefault(pd);
		styles.setDocDefaults(dd);

		Style normal = style("paragraph", "Normal", null, true);
		RPr nr = F.createRPr();
		nr.setRFonts(fonts("Times New Roman"));
		nr.setSz(hps(28));
		normal.setRPr(nr);

		style("character", "DefaultParagraphFont", null, true);

		Style h = style("paragraph", "H", "Normal", false);
		RPr hr = F.createRPr();
		hr.setRFonts(fonts("Arial"));
		hr.setSz(hps(40));
		h.setRPr(hr);

		Style s = style("character", "S", "DefaultParagraphFont", false);
		RPr sr = F.createRPr();
		sr.setB(on());
		s.setRPr(sr);

		pkg = PropertyResolverTestUtils.createdPkgWithStyles(styles);
	}

	// ---------------------------------------------------------------- the default paragraph style (P1 a-d)

	@Test
	public void aRunInAParagraphNamingNoStyleGetsTheDefaultStylesRunProperties() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		assertEquals("null pPr", 28, sz(pr.getEffectiveRPr((RPr) null, (PPr) null)));
		assertEquals("pPr without w:pStyle", 28, sz(pr.getEffectiveRPr((RPr) null, F.createPPr())));
		assertEquals("w:pStyle Normal", 28, sz(pr.getEffectiveRPr((RPr) null, pPr("Normal"))));
		RPr bold = F.createRPr();
		bold.setB(on());
		RPr effective = pr.getEffectiveRPr(bold, F.createPPr());
		assertEquals("a bold run, no w:pStyle (golden (c))", 28, sz(effective));
		assertEquals("Times New Roman", effective.getRFonts().getAscii());
		assertNotNull(effective.getB());
	}

	@Test
	public void aParagraphNamingAMissingStyleResolvesAsTheDefaultStyle() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		assertEquals("run (golden (d))", 28, sz(pr.getEffectiveRPr((RPr) null, pPr("Missing"))));
		PPr effective = pr.getEffectivePPr(pPr("Missing"));
		assertNotNull("pPr no longer null", effective);
		assertEquals("the document defaults are in it", BigInteger.valueOf(160), effective.getSpacing().getAfter());
		assertSame("and it is the default style's own entry", pr.getEffectivePPr("Normal"), effective);
		PPr direct = pPr("Missing");
		direct.setJc(F.createJc());
		assertEquals("with direct formatting too", BigInteger.valueOf(160), pr.getEffectivePPr(direct).getSpacing().getAfter());
	}

	// ---------------------------------------------------------------- character styles (P1 e-g)

	@Test
	public void aCharacterStyleInAStyledParagraphKeepsTheParagraphStylesFontAndSize() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr effective = pr.getEffectiveRPr(rPr("S"), pPr("H"));
		assertEquals("Arial", effective.getRFonts().getAscii());
		assertEquals("20pt (golden (e))", 40, sz(effective));
		assertNotNull(effective.getB());
		assertEquals("en-US", effective.getLang().getVal());
	}

	@Test
	public void theAnswerDoesNotDependOnWhichCallerCameFirst() throws Exception {
		// the public one-arg overload first, then the run: was Calibri 11pt (the cache poisoned)
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr byStyle = pr.getEffectiveRPr("S");
		assertEquals("a character style on its own: the document defaults under it", "Calibri", byStyle.getRFonts().getAscii());
		assertEquals(22, sz(byStyle));
		RPr effective = pr.getEffectiveRPr(rPr("S"), pPr("H"));
		assertEquals("Arial", effective.getRFonts().getAscii());
		assertEquals(40, sz(effective));

		// and the other way round: getEffectiveRPr("S") used to come back with no rFonts and no sz
		PropertyResolver pr2 = new PropertyResolver(pkg);
		pr2.getEffectiveRPr(rPr("S"), pPr("H"));
		RPr byStyle2 = pr2.getEffectiveRPr("S");
		assertEquals("Calibri", byStyle2.getRFonts().getAscii());
		assertEquals(22, sz(byStyle2));
	}

	@Test
	public void defaultParagraphFontAddsNothing() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr effective = pr.getEffectiveRPr(rPr("DefaultParagraphFont"), pPr("H"));
		assertEquals("20pt, not Normal's 14pt (golden (f))", 40, sz(effective));
		assertEquals("Arial", effective.getRFonts().getAscii());
	}

	@Test
	public void aMissingCharacterStyleIsIgnored() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr effective = pr.getEffectiveRPr(rPr("Nope"), pPr("H"));
		assertEquals(40, sz(effective));
		assertNull(pr.getEffectiveRPr("Nope"));
	}

	// ---------------------------------------------------------------- the paragraph mark

	@Test
	public void theParagraphMarkFormatsTheMarkNotTheRuns() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		PPr marked = F.createPPr();
		ParaRPr mark = F.createParaRPr();
		mark.setB(on());
		mark.setSz(hps(40));
		marked.setRPr(mark);

		RPr run = pr.getEffectiveRPr((RPr) null, marked);
		assertNull("a run with no rPr is not bold (ECMA 17.3.1.29; measured 17.0.5)", run.getB());
		assertEquals(28, sz(run));

		RPr markRPr = pr.getEffectiveParagraphMarkRPr(marked);
		assertNotNull(markRPr.getB());
		assertEquals("the mark: defaults, Normal, then its own rPr", 40, sz(markRPr));
		assertEquals("Times New Roman", markRPr.getRFonts().getAscii());

		assertEquals("a mark with no rPr is the paragraph's run baseline", 28, sz(pr.getEffectiveParagraphMarkRPr(F.createPPr())));
		assertEquals(28, sz(pr.getEffectiveParagraphMarkRPr(null)));
	}

	// ---------------------------------------------------------------- the other overloads

	@Test
	public void theNoParagraphOverloadUsesTheDefaultParagraphStyle() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr effective = pr.getEffectiveRPr(rPr("S"));
		assertEquals("Times New Roman", effective.getRFonts().getAscii());
		assertEquals(28, sz(effective));
		assertNotNull(effective.getB());
		assertEquals("with no rStyle either", 28, sz(pr.getEffectiveRPr(F.createRPr())));
	}

	@Test
	public void styleOverloadsIncludeTheDocumentDefaults() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr h = pr.getEffectiveRPr("H");
		assertEquals(40, sz(h));
		assertEquals("en-US", h.getLang().getVal());
		PPr hp = pr.getEffectivePPr("H");
		assertEquals(BigInteger.valueOf(160), hp.getSpacing().getAfter());
		assertSame("cached", hp, pr.getEffectivePPr("H"));
		assertSame("getResolvedDefaultParagraphStyle is the default style's entry", pr.getEffectivePPr("Normal"), pr.getResolvedDefaultParagraphStyle());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void theFlaggedOverloadStillAnswersAndNoLongerCaches() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr withoutDefaults = pr.getEffectiveRPr("S", false, false, false);
		assertNull(withoutDefaults.getRFonts() == null ? null : withoutDefaults.getRFonts().getAscii());
		assertNull(withoutDefaults.getSz());
		assertNotNull(withoutDefaults.getB());
		RPr withDefaults = pr.getEffectiveRPr("S", true, true, true);
		assertEquals("Calibri", withDefaults.getRFonts().getAscii());
		assertEquals(22, sz(withDefaults));
		assertEquals("the one-arg overload is unaffected", 22, sz(pr.getEffectiveRPr("S")));
		assertTrue(pr.getEffectiveRPr("S").getRFonts().getAscii().equals("Calibri"));
	}

	// ---------------------------------------------------------------- helpers

	private static int sz(RPr r) {
		assertNotNull("an rPr", r);
		assertNotNull("a w:sz", r.getSz());
		return r.getSz().getVal().intValue();
	}

	private static HpsMeasure hps(int v) {
		HpsMeasure m = F.createHpsMeasure();
		m.setVal(BigInteger.valueOf(v));
		return m;
	}

	private static BooleanDefaultTrue on() {
		BooleanDefaultTrue b = F.createBooleanDefaultTrue();
		b.setVal(true);
		return b;
	}

	private static RFonts fonts(String ascii) {
		RFonts r = F.createRFonts();
		r.setAscii(ascii);
		r.setHAnsi(ascii);
		return r;
	}

	private static PPr pPr(String pStyle) {
		PPr p = F.createPPr();
		PPrBase.PStyle ps = F.createPPrBasePStyle();
		ps.setVal(pStyle);
		p.setPStyle(ps);
		return p;
	}

	private static RPr rPr(String rStyle) {
		RPr r = F.createRPr();
		RStyle rs = F.createRStyle();
		rs.setVal(rStyle);
		r.setRStyle(rs);
		return r;
	}

	private Style style(String type, String id, String basedOn, boolean dflt) {
		Style s = F.createStyle();
		s.setType(type);
		s.setStyleId(id);
		Style.Name n = F.createStyleName();
		n.setVal(id);
		s.setName(n);
		if (basedOn != null) {
			Style.BasedOn b = F.createStyleBasedOn();
			b.setVal(basedOn);
			s.setBasedOn(b);
		}
		if (dflt) s.setDefault(true);
		styles.getStyle().add(s);
		return s;
	}
}
