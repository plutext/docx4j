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
package org.docx4j.model.styles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTSignedHpsMeasure;
import org.docx4j.wml.CTTblPPr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STHAnchor;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STThemeColor;
import org.docx4j.wml.STVAnchor;
import org.docx4j.wml.Style;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.Test;

/**
 * The merge rules CR-015 phase 1 corrected, each against the Word answer of the
 * styles-* probe that settled it (docx4j-layout-fidelity/goldens/word) or the spec,
 * through StyleUtil.apply and through PropertyResolver.
 *
 * @since 17.1.1
 */
public class StyleUtilMergeRulesTest {

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	// ---------------------------------------------------------------- w:spacing / w:lineRule (probe styles-linerule)

	@Test
	public void aDirectSpacingStatingOnlyAfterKeepsTheInheritedExactLineRule() {
		PPrBase.Spacing inherited = F.createPPrBaseSpacing();
		inherited.setLine(BigInteger.valueOf(480));
		inherited.setLineRule(STLineSpacingRule.EXACT);
		PPrBase.Spacing direct = F.createPPrBaseSpacing();
		direct.setAfter(BigInteger.ZERO);

		PPrBase.Spacing merged = StyleUtil.apply(direct, inherited);
		assertEquals(BigInteger.valueOf(480), merged.getLine());
		assertEquals("Word keeps the 24pt pitch (golden styles-linerule (b))", STLineSpacingRule.EXACT, merged.getLineRule());
		assertEquals(BigInteger.ZERO, merged.getAfter());
	}

	@Test
	public void aDirectLineWithoutARuleMeansAuto() {
		PPrBase.Spacing inherited = F.createPPrBaseSpacing();
		inherited.setLine(BigInteger.valueOf(480));
		inherited.setLineRule(STLineSpacingRule.EXACT);
		PPrBase.Spacing direct = F.createPPrBaseSpacing();
		direct.setLine(BigInteger.valueOf(240));

		PPrBase.Spacing merged = StyleUtil.apply(direct, inherited);
		assertEquals(BigInteger.valueOf(240), merged.getLine());
		assertEquals("single auto (golden styles-linerule (c))", STLineSpacingRule.AUTO, merged.getLineRule());
	}

	@Test
	public void lineRuleThroughTheResolver() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Style x = paragraphStyle(pkg, "X", "Normal");
		PPr xp = F.createPPr();
		PPrBase.Spacing sp = F.createPPrBaseSpacing();
		sp.setLine(BigInteger.valueOf(480));
		sp.setLineRule(STLineSpacingRule.EXACT);
		xp.setSpacing(sp);
		x.setPPr(xp);

		PPr direct = pPr("X");
		PPrBase.Spacing after0 = F.createPPrBaseSpacing();
		after0.setAfter(BigInteger.ZERO);
		direct.setSpacing(after0);

		PPr effective = new PropertyResolver(pkg).getEffectivePPr(direct);
		assertEquals(BigInteger.valueOf(480), effective.getSpacing().getLine());
		assertEquals(STLineSpacingRule.EXACT, effective.getSpacing().getLineRule());
	}

	// ---------------------------------------------------------------- w:numPr (probe styles-numpr-ilvl-only)

	@Test
	public void aDirectNumPrOfIlvlOnlyTakesThatLevelOfTheStylesList() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Style l = paragraphStyle(pkg, "L", "Normal");
		PPr lp = F.createPPr();
		lp.setNumPr(numPr(90, 0));
		l.setPPr(lp);

		PPr direct = pPr("L");
		direct.setNumPr(numPr(null, 1));

		PPr effective = new PropertyResolver(pkg).getEffectivePPr(direct);
		assertNotNull(effective.getNumPr());
		assertEquals("numId inherited from the style", BigInteger.valueOf(90), effective.getNumPr().getNumId().getVal());
		assertEquals("level 1 (golden styles-numpr-ilvl-only (b): 1.1.)", BigInteger.ONE, effective.getNumPr().getIlvl().getVal());
	}

	@Test
	public void aDirectNumPrOfNumIdOnlyKeepsTheStylesLevel() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Style l = paragraphStyle(pkg, "L", "Normal");
		PPr lp = F.createPPr();
		lp.setNumPr(numPr(90, 1));
		l.setPPr(lp);

		PPr direct = pPr("L");
		direct.setNumPr(numPr(90, null));

		PPr effective = new PropertyResolver(pkg).getEffectivePPr(direct);
		assertEquals(BigInteger.valueOf(90), effective.getNumPr().getNumId().getVal());
		assertEquals(BigInteger.ONE, effective.getNumPr().getIlvl().getVal());
	}

	@Test
	public void numPrEmptinessCountsEitherElement() {
		assertTrue(StyleUtil.isEmpty((PPrBase.NumPr) null));
		assertTrue(StyleUtil.isEmpty(F.createPPrBaseNumPr()));
		assertTrue(!StyleUtil.isEmpty(numPr(null, 1)));
		assertTrue(!StyleUtil.isEmpty(numPr(5, null)));
	}

	// ---------------------------------------------------------------- w:lang

	@Test
	public void langMergesPerAttribute() {
		CTLanguage inherited = F.createCTLanguage();
		inherited.setVal("en-US");
		inherited.setEastAsia("ja-JP");
		CTLanguage direct = F.createCTLanguage();
		direct.setBidi("he-IL");

		CTLanguage merged = StyleUtil.apply(direct, inherited);
		assertEquals("en-US", merged.getVal());
		assertEquals("ja-JP", merged.getEastAsia());
		assertEquals("he-IL", merged.getBidi());
	}

	@Test
	public void aStyleStatingOnlyLangIsApplied() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Style bidi = characterStyle(pkg, "Bidi");
		RPr br = F.createRPr();
		CTLanguage bl = F.createCTLanguage();
		bl.setBidi("he-IL");
		br.setLang(bl);
		bidi.setRPr(br);

		RPr effective = new PropertyResolver(pkg).getEffectiveRPr(rPr("Bidi"), pPr("Normal"));
		assertNotNull(effective.getLang());
		assertEquals("he-IL", effective.getLang().getBidi());
		assertEquals("the document default's w:val survives", "en-US", effective.getLang().getVal());
	}

	// ---------------------------------------------------------------- direct formatting outside the old whitelist

	@Test
	public void rtlOnlyDirectFormattingReachesTheEffectiveRPr() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		RPr rtl = F.createRPr();
		rtl.setRtl(on());
		RPr effective = new PropertyResolver(pkg).getEffectiveRPr(rtl, pPr("Normal"));
		assertNotNull("w:rtl", effective.getRtl());

		RPr pos = F.createRPr();
		CTSignedHpsMeasure six = F.createCTSignedHpsMeasure();
		six.setVal(BigInteger.valueOf(6));
		pos.setPosition(six);
		effective = new PropertyResolver(pkg).getEffectiveRPr(pos, pPr("Normal"));
		assertNotNull("w:position", effective.getPosition());
	}

	@Test
	public void mirrorIndentsOnlyDirectFormattingReachesTheEffectivePPr() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		PPr direct = pPr("Normal");
		direct.setMirrorIndents(on());
		PPr effective = new PropertyResolver(pkg).getEffectivePPr(direct);
		assertNotNull(effective.getMirrorIndents());
	}

	// ---------------------------------------------------------------- w:tblpPr, w:shd, w:u

	@Test
	public void tblpPrStatingOnlyXKeepsTheInheritedAnchors() {
		CTTblPPr inherited = F.createCTTblPPr();
		inherited.setVertAnchor(STVAnchor.PAGE);
		inherited.setHorzAnchor(STHAnchor.MARGIN);
		CTTblPPr direct = F.createCTTblPPr();
		direct.setTblpX(BigInteger.valueOf(100));

		CTTblPPr merged = StyleUtil.apply(direct, inherited);
		assertEquals(STVAnchor.PAGE, merged.getVertAnchor());
		assertEquals(STHAnchor.MARGIN, merged.getHorzAnchor());
		assertEquals(BigInteger.valueOf(100), merged.getTblpX());
	}

	@Test
	public void shdKeepsAnInheritedThemeFillUnlessRestated() {
		CTShd inherited = F.createCTShd();
		inherited.setVal(STShd.CLEAR);
		inherited.setThemeFill(STThemeColor.ACCENT_1);
		CTShd direct = F.createCTShd();
		direct.setVal(STShd.SOLID);

		CTShd merged = StyleUtil.apply(direct, inherited);
		assertEquals(STShd.SOLID, merged.getVal());
		assertEquals(STThemeColor.ACCENT_1, merged.getThemeFill());
	}

	@Test
	public void underlineColourInherits() {
		U inherited = F.createU();
		inherited.setVal(UnderlineEnumeration.SINGLE);
		inherited.setColor("FF0000");
		U direct = F.createU();
		direct.setVal(UnderlineEnumeration.DOUBLE);

		U merged = StyleUtil.apply(direct, inherited);
		assertEquals(UnderlineEnumeration.DOUBLE, merged.getVal());
		assertEquals("FF0000", merged.getColor());
		assertNull(StyleUtil.apply((U) null, (U) null));
	}

	// ---------------------------------------------------------------- helpers

	private static BooleanDefaultTrue on() {
		BooleanDefaultTrue b = F.createBooleanDefaultTrue();
		b.setVal(true);
		return b;
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

	private static PPrBase.NumPr numPr(Integer numId, Integer ilvl) {
		PPrBase.NumPr np = F.createPPrBaseNumPr();
		if (numId != null) {
			PPrBase.NumPr.NumId id = F.createPPrBaseNumPrNumId();
			id.setVal(BigInteger.valueOf(numId));
			np.setNumId(id);
		}
		if (ilvl != null) {
			PPrBase.NumPr.Ilvl lvl = F.createPPrBaseNumPrIlvl();
			lvl.setVal(BigInteger.valueOf(ilvl));
			np.setIlvl(lvl);
		}
		return np;
	}

	private static Style paragraphStyle(WordprocessingMLPackage pkg, String styleId, String basedOn) {
		return style(pkg, "paragraph", styleId, basedOn);
	}

	private static Style characterStyle(WordprocessingMLPackage pkg, String styleId) {
		return style(pkg, "character", styleId, "DefaultParagraphFont");
	}

	private static Style style(WordprocessingMLPackage pkg, String type, String styleId, String basedOn) {
		Style s = F.createStyle();
		s.setType(type);
		s.setStyleId(styleId);
		Style.Name n = F.createStyleName();
		n.setVal(styleId);
		s.setName(n);
		if (basedOn != null) {
			Style.BasedOn b = F.createStyleBasedOn();
			b.setVal(basedOn);
			s.setBasedOn(b);
		}
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
		return s;
	}
}
