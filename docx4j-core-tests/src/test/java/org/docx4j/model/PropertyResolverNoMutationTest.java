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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Before;
import org.junit.Test;

/**
 * Resolution reads the styles part and writes nothing into it, and the objects it
 * returns share no leaf with the style definitions (CR-015 phase 3).  Until 17.1.1
 * constructing the resolver wrote w:sz 20 into an rPrDefault that had none, resolving a
 * heading rewrote its w:outlineLvl, resolving a style whose w:numPr lacked a w:numId
 * wrote the inherited id into it, and an effective rPr's w:b was the style's own w:b
 * object.
 *
 * @since 17.1.1
 */
public class PropertyResolverNoMutationTest {

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	private Styles styles;
	private WordprocessingMLPackage pkg;

	@Before
	public void setUp() throws Exception {
		styles = F.createStyles();
		DocDefaults dd = F.createDocDefaults();
		DocDefaults.RPrDefault rd = F.createDocDefaultsRPrDefault();
		RPr ddr = F.createRPr();
		RFonts rf = F.createRFonts();
		rf.setAscii("Calibri");
		ddr.setRFonts(rf);          // no w:sz: the 10pt default must stay out of the part
		rd.setRPr(ddr);
		dd.setRPrDefault(rd);
		styles.setDocDefaults(dd);

		Style normal = style("paragraph", "Normal", null, true);
		RPr nr = F.createRPr();
		nr.setSz(hps(28));
		normal.setRPr(nr);
		style("character", "DefaultParagraphFont", null, true);

		// a heading whose declared outline level contradicts its built-in name, under a localised id
		Style h2 = style("paragraph", "berschrift2", "Normal", false);
		h2.getName().setVal("heading 2");
		PPr h2p = F.createPPr();
		PPrBase.OutlineLvl ol = F.createPPrBaseOutlineLvl();
		ol.setVal(BigInteger.valueOf(5));
		h2p.setOutlineLvl(ol);
		h2.setPPr(h2p);

		// a numbered style and one based on it stating only the level
		Style l = style("paragraph", "L", "Normal", false);
		PPr lp = F.createPPr();
		lp.setNumPr(numPr(90, 0));
		l.setPPr(lp);
		Style l2 = style("paragraph", "L2", "L", false);
		PPr l2p = F.createPPr();
		l2p.setNumPr(numPr(null, 1));
		l2.setPPr(l2p);

		Style s = style("character", "S", "DefaultParagraphFont", false);
		RPr sr = F.createRPr();
		sr.setB(on());
		s.setRPr(sr);

		pkg = PropertyResolverTestUtils.createdPkgWithStyles(styles);
	}

	@Test
	public void resolvingEverythingLeavesTheStylesPartByteForByteAsItWas() throws Exception {
		String before = XmlUtils.marshaltoString(styles, true, true);

		PropertyResolver pr = new PropertyResolver(pkg);
		for (Style s : styles.getStyle()) {
			if ("paragraph".equals(s.getType())) {
				pr.getEffectivePPr(s.getStyleId());
				pr.getEffectiveRPr(s.getStyleId());
			} else {
				pr.getEffectiveRPr(s.getStyleId());
			}
		}
		pr.getEffectivePPr(pPr("berschrift2"));
		pr.getEffectiveRPr(rPr("S"), pPr("L2"));
		pr.getEffectiveParagraphMarkRPr(pPr("Normal"));
		pr.getEffectiveRPr((RPr) null, pPr("Missing"));

		assertEquals(before, XmlUtils.marshaltoString(styles, true, true));
		assertNull("the 10pt default is the resolver's, not the part's", styles.getDocDefaults().getRPrDefault().getRPr().getSz());
	}

	@Test
	public void theDefaultSizeAppliesWithoutBeingWritten() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		assertEquals(BigInteger.valueOf(20), pr.getDocumentDefaultRPr().getSz().getVal());
		assertNull(styles.getDocDefaults().getRPrDefault().getRPr().getSz());
	}

	@Test
	public void aHeadingsOutlineLevelComesFromItsNameInTheResolvedPPrOnly() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		PPr effective = pr.getEffectivePPr("berschrift2");
		assertEquals("level 1 for 'heading 2', whatever the style declares", BigInteger.ONE, effective.getOutlineLvl().getVal());
		assertEquals("the style still declares 5", BigInteger.valueOf(5), find("berschrift2").getPPr().getOutlineLvl().getVal());
		assertEquals(2, PropertyResolver.headingLevelByName(find("berschrift2")));
		assertEquals(-1, PropertyResolver.headingLevelByName(find("Normal")));
	}

	@Test
	public void anInheritedNumIdReachesTheEffectivePPrWithoutBeingWrittenIntoTheStyle() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		PPr effective = pr.getEffectivePPr("L2");
		assertEquals(BigInteger.valueOf(90), effective.getNumPr().getNumId().getVal());
		assertEquals(BigInteger.ONE, effective.getNumPr().getIlvl().getVal());
		assertNull("L2's own w:numPr still names no w:numId", find("L2").getPPr().getNumPr().getNumId());
	}

	@Test
	public void effectiveObjectsShareNoLeafWithTheStyleDefinitions() throws Exception {
		PropertyResolver pr = new PropertyResolver(pkg);
		RPr effective = pr.getEffectiveRPr(rPr("S"), pPr("Normal"));
		assertNotNull(effective.getB());
		assertNotSame("w:b is a copy of the style's", find("S").getRPr().getB(), effective.getB());
		assertNotSame("w:sz is a copy of Normal's", find("Normal").getRPr().getSz(), effective.getSz());
		assertNotSame("w:rFonts is a copy of the defaults'", styles.getDocDefaults().getRPrDefault().getRPr().getRFonts(), effective.getRFonts());

		PPr effectivePPr = pr.getEffectivePPr("L2");
		assertNotSame("w:numId is a copy of L's", find("L").getPPr().getNumPr().getNumId(), effectivePPr.getNumPr().getNumId());
		assertNotSame("w:ilvl is a copy of L2's", find("L2").getPPr().getNumPr().getIlvl(), effectivePPr.getNumPr().getIlvl());

		// and editing the effective object changes nothing in the part
		effective.getB().setVal(Boolean.FALSE);
		assertEquals(true, find("S").getRPr().getB().isVal());
	}

	// ---------------------------------------------------------------- helpers

	private Style find(String id) {
		for (Style s : styles.getStyle()) if (id.equals(s.getStyleId())) return s;
		return null;
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
