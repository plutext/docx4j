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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.PropertyCatalogue.Property;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTCnf;
import org.docx4j.wml.CTEastAsianLayout;
import org.docx4j.wml.CTEm;
import org.docx4j.wml.CTFitText;
import org.docx4j.wml.CTFramePr;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTSignedHpsMeasure;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.CTTextEffect;
import org.docx4j.wml.CTTextScale;
import org.docx4j.wml.CTTextboxTightWrap;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.Color;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STEm;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTextEffect;
import org.docx4j.wml.STTextboxTightWrap;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.TextDirection;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.Test;

/**
 * Every member of the two catalogues, one at a time: a source stating only that member
 * is not empty, is direct formatting (unless flagged otherwise), is carried by apply
 * into an empty destination, and is removed by unset.  Runs are checked in both
 * directions between w:rPr and the paragraph mark's w:rPr.  The catalogues must also
 * cover every generated member: a member added to the schema classes and not to the
 * catalogue is what the four hand-kept lists this replaces used to miss.
 *
 * CR-015 phase 1.
 * @since 17.1.1
 */
public class PropertyCatalogueTest {

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	private static final Set<String> RUN_NOT_FORMATTING = new HashSet<String>(Arrays.asList("rStyle"));
	private static final Set<String> PARAGRAPH_NOT_FORMATTING = new HashSet<String>(
			Arrays.asList("pStyle", "divId", "cnfStyle", "collapsed"));

	@Test
	public void everyRunMemberRoundTrips() {
		for (Property<Object, ?> p : PropertyCatalogue.RUN) {
			roundTripRun(p);
		}
	}

	private <V> void roundTripRun(Property<Object, V> p) {
		V sample = sample(p);
		assertFalse(p.name() + ": the sample must not be empty", p.isEmpty(sample));

		// w:rPr -> w:rPr
		RPr source = F.createRPr();
		p.set(source, sample);
		assertFalse(p.name() + ": a source stating it is not empty", StyleUtil.isEmpty(source));
		assertEquals(p.name() + ": direct formatting", !RUN_NOT_FORMATTING.contains(p.name()),
				StyleUtil.hasDirectFormatting(source));
		RPr destination = StyleUtil.apply(source, F.createRPr());
		assertNotNull(p.name() + ": apply carries it", p.get(destination));
		assertFalse(p.name() + ": carried non-empty", p.isEmpty(p.get(destination)));
		StyleUtil.unset(source, destination);
		assertNull(p.name() + ": unset removes it", p.get(destination));

		// w:rPr -> paragraph mark, and back
		ParaRPr mark = StyleUtil.apply(source, F.createParaRPr());
		assertNotNull(p.name() + ": carried into a ParaRPr", p.get(mark));
		assertFalse(p.name() + ": a mark stating it is not empty", StyleUtil.isEmpty(mark));
		assertEquals(p.name() + ": direct formatting on the mark", !RUN_NOT_FORMATTING.contains(p.name()),
				StyleUtil.hasDirectFormatting(mark));
		RPr back = StyleUtil.apply(mark, F.createRPr());
		assertNotNull(p.name() + ": carried back from a ParaRPr", p.get(back));
		ParaRPr mark2 = StyleUtil.apply(mark, F.createParaRPr());
		assertNotNull(p.name() + ": carried ParaRPr to ParaRPr", p.get(mark2));
	}

	@Test
	public void everyParagraphMemberRoundTrips() {
		for (Property<PPrBase, ?> p : PropertyCatalogue.PARAGRAPH) {
			roundTripParagraph(p);
		}
	}

	private <V> void roundTripParagraph(Property<PPrBase, V> p) {
		V sample = sample(p);
		assertFalse(p.name() + ": the sample must not be empty", p.isEmpty(sample));
		PPr source = F.createPPr();
		p.set(source, sample);
		assertFalse(p.name() + ": a source stating it is not empty", StyleUtil.isEmpty((PPrBase) source));
		assertEquals(p.name() + ": direct formatting", !PARAGRAPH_NOT_FORMATTING.contains(p.name()),
				StyleUtil.hasDirectFormatting(source));
		PPr destination = F.createPPr();
		StyleUtil.apply((PPrBase) source, (PPrBase) destination);
		assertNotNull(p.name() + ": apply carries it", p.get(destination));
		assertFalse(p.name() + ": carried non-empty", p.isEmpty(p.get(destination)));
		StyleUtil.unset((PPrBase) source, (PPrBase) destination);
		assertNull(p.name() + ": unset removes it", p.get(destination));
	}

	@Test
	public void emptyElementsAreEmptyAndNothingIsDirect() {
		assertTrue(StyleUtil.isEmpty(F.createRPr()));
		assertTrue(StyleUtil.isEmpty(F.createParaRPr()));
		assertTrue(StyleUtil.isEmpty((PPrBase) F.createPPr()));
		assertTrue(StyleUtil.isEmpty((RPr) null));
		assertFalse(StyleUtil.hasDirectFormatting(F.createRPr()));
		assertFalse(StyleUtil.hasDirectFormatting((RPr) null));
		assertFalse(StyleUtil.hasDirectFormatting(F.createPPr()));
	}

	/** The catalogues name every settable member of the generated classes (bar the revision records and the parent pointer). */
	@Test
	public void cataloguesCoverTheGeneratedMembers() {
		Set<String> runSetters = setterNames(RPr.class);
		runSetters.remove("rprchange");
		assertCovered("RUN", PropertyCatalogue.RUN, runSetters);

		Set<String> markSetters = setterNames(ParaRPr.class);
		markSetters.removeAll(Arrays.asList("rprchange", "ins", "del", "movefrom", "moveto"));
		assertCovered("RUN (as ParaRPr)", PropertyCatalogue.RUN, markSetters);

		Set<String> paragraphSetters = setterNames(PPrBase.class);
		assertCovered("PARAGRAPH", PropertyCatalogue.PARAGRAPH, paragraphSetters);
	}

	private static <O> void assertCovered(String which, List<? extends Property<O, ?>> catalogue, Set<String> setters) {
		Set<String> names = new HashSet<String>();
		for (Property<O, ?> p : catalogue) names.add(p.name().toLowerCase());
		Set<String> missing = new HashSet<String>();
		for (String s : setters) if (!names.contains(s.toLowerCase())) missing.add(s);
		assertTrue(which + " lacks " + missing, missing.isEmpty());
	}

	private static Set<String> setterNames(Class<?> c) {
		Set<String> names = new HashSet<String>();
		for (java.lang.reflect.Method m : c.getMethods()) {
			if (m.getName().startsWith("set") && m.getParameterCount() == 1
					&& m.getDeclaringClass() == c && !m.getName().equals("setParent")) {
				names.add(m.getName().substring(3).toLowerCase());
			}
		}
		return names;
	}

	// ---------------------------------------------------------------- samples

	@SuppressWarnings("unchecked")
	private static <V> V sample(Property<?, V> p) {
		Class<V> t = p.type();
		Object v;
		if (t == BooleanDefaultTrue.class) {
			BooleanDefaultTrue b = F.createBooleanDefaultTrue(); b.setVal(true); v = b;
		} else if (t == HpsMeasure.class) {
			HpsMeasure m = F.createHpsMeasure(); m.setVal(BigInteger.valueOf(28)); v = m;
		} else if (t == CTSignedHpsMeasure.class) {
			CTSignedHpsMeasure m = F.createCTSignedHpsMeasure(); m.setVal(BigInteger.valueOf(6)); v = m;
		} else if (t == CTSignedTwipsMeasure.class) {
			CTSignedTwipsMeasure m = F.createCTSignedTwipsMeasure(); m.setVal(BigInteger.valueOf(20)); v = m;
		} else if (t == CTTextScale.class) {
			CTTextScale m = F.createCTTextScale(); m.setVal(80); v = m;
		} else if (t == Color.class) {
			Color c = F.createColor(); c.setVal("FF0000"); v = c;
		} else if (t == Highlight.class) {
			Highlight h = F.createHighlight(); h.setVal("yellow"); v = h;
		} else if (t == U.class) {
			U u = F.createU(); u.setVal(UnderlineEnumeration.SINGLE); v = u;
		} else if (t == CTTextEffect.class) {
			CTTextEffect e = F.createCTTextEffect(); e.setVal(STTextEffect.BLINK_BACKGROUND); v = e;
		} else if (t == CTBorder.class) {
			CTBorder b = F.createCTBorder(); b.setVal(STBorder.SINGLE); v = b;
		} else if (t == CTShd.class) {
			CTShd s = F.createCTShd(); s.setVal(STShd.CLEAR); s.setFill("FFFF00"); v = s;
		} else if (t == CTFitText.class) {
			CTFitText f = F.createCTFitText(); f.setVal(BigInteger.valueOf(1440)); v = f;
		} else if (t == CTVerticalAlignRun.class) {
			CTVerticalAlignRun a = F.createCTVerticalAlignRun(); a.setVal(STVerticalAlignRun.SUPERSCRIPT); v = a;
		} else if (t == CTEm.class) {
			CTEm e = F.createCTEm(); e.setVal(STEm.DOT); v = e;
		} else if (t == CTLanguage.class) {
			CTLanguage l = F.createCTLanguage(); l.setBidi("he-IL"); v = l;
		} else if (t == CTEastAsianLayout.class) {
			CTEastAsianLayout l = F.createCTEastAsianLayout(); l.setVert(Boolean.TRUE); v = l;
		} else if (t == RFonts.class) {
			RFonts r = F.createRFonts(); r.setAscii("Arial"); v = r;
		} else if (t == RStyle.class) {
			RStyle r = F.createRStyle(); r.setVal("Strong"); v = r;
		} else if (t == PPrBase.PStyle.class) {
			PPrBase.PStyle s = F.createPPrBasePStyle(); s.setVal("Heading1"); v = s;
		} else if (t == CTFramePr.class) {
			CTFramePr f = F.createCTFramePr(); f.setW(BigInteger.valueOf(3600)); v = f;
		} else if (t == PPrBase.NumPr.class) {
			PPrBase.NumPr n = F.createPPrBaseNumPr(); PPrBase.NumPr.Ilvl i = F.createPPrBaseNumPrIlvl();
			i.setVal(BigInteger.ONE); n.setIlvl(i); v = n;   // ilvl alone: the case the old isEmpty dropped
		} else if (t == PPrBase.PBdr.class) {
			PPrBase.PBdr b = F.createPPrBasePBdr(); CTBorder top = F.createCTBorder(); top.setVal(STBorder.SINGLE); b.setTop(top); v = b;
		} else if (t == Tabs.class) {
			Tabs tabs = F.createTabs(); CTTabStop stop = F.createCTTabStop(); stop.setVal(STTabJc.LEFT); stop.setPos(BigInteger.valueOf(720)); tabs.getTab().add(stop); v = tabs;
		} else if (t == PPrBase.Spacing.class) {
			PPrBase.Spacing s = F.createPPrBaseSpacing(); s.setAfter(BigInteger.valueOf(120)); v = s;
		} else if (t == PPrBase.Ind.class) {
			PPrBase.Ind i = F.createPPrBaseInd(); i.setLeft(BigInteger.valueOf(720)); v = i;
		} else if (t == Jc.class) {
			Jc j = F.createJc(); j.setVal(JcEnumeration.CENTER); v = j;
		} else if (t == TextDirection.class) {
			TextDirection d = F.createTextDirection(); d.setVal("btLr"); v = d;
		} else if (t == PPrBase.TextAlignment.class) {
			PPrBase.TextAlignment a = F.createPPrBaseTextAlignment(); a.setVal("center"); v = a;
		} else if (t == CTTextboxTightWrap.class) {
			CTTextboxTightWrap w = F.createCTTextboxTightWrap(); w.setVal(STTextboxTightWrap.ALL_LINES); v = w;
		} else if (t == PPrBase.OutlineLvl.class) {
			PPrBase.OutlineLvl o = F.createPPrBaseOutlineLvl(); o.setVal(BigInteger.valueOf(2)); v = o;
		} else if (t == PPrBase.DivId.class) {
			PPrBase.DivId d = F.createPPrBaseDivId(); d.setVal(BigInteger.valueOf(123)); v = d;
		} else if (t == CTCnf.class) {
			CTCnf c = F.createCTCnf(); c.setVal("100000000000"); v = c;
		} else {
			// the w14 members: any instance is a statement
			try {
				v = t.getConstructor().newInstance();
			} catch (Exception e) {
				throw new AssertionError(p.name() + ": no sample for " + t.getName(), e);
			}
		}
		return (V) v;
	}
}
