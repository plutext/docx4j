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

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * Toggle properties (ECMA-376-1 17.7.3): where the same one is stated at more than one
 * <b>level</b> of the style hierarchy its effective value is the XOR of them, so bold on a
 * table style's first column and bold again on the character style of a run inside it is
 * not bold.
 *
 * <p>Measured on a corpus document which does exactly that: Word draws 216 regular lines
 * and 16 bold ones, and before this rule we drew 174 and 60 (CR-001 batch 46 item 4).</p>
 *
 * @since 17.1.1
 */
public class TogglePropertiesTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	// ------------------------------------------------------------------ the twelve

	@Test
	public void theTwelveTogglesAreTheTwelveTheSpecNames() {
		assertEquals("[b, bCs, caps, emboss, i, iCs, imprint, outline, shadow, smallCaps, strike, vanish]",
				PropertyCatalogue.TOGGLE_NAMES.toString());
		assertEquals(12, PropertyCatalogue.TOGGLES.size());
		for (PropertyCatalogue.Property<Object, BooleanDefaultTrue> p : PropertyCatalogue.TOGGLES) {
			assertTrue(p.name(), PropertyCatalogue.TOGGLE_NAMES.contains(p.name()));
		}
		// Boolean, but not toggles: the last value in the order wins for these
		for (String notAToggle : new String[] { "dstrike", "noProof", "snapToGrid", "webHidden",
				"rtl", "cs", "specVanish", "oMath" }) {
			assertNotNull(notAToggle, PropertyCatalogue.named(PropertyCatalogue.RUN, notAToggle));
			assertFalse(notAToggle, PropertyCatalogue.TOGGLE_NAMES.contains(notAToggle));
		}
	}

	// ------------------------------------------------------------------ the rule itself

	private static BooleanDefaultTrue on() {
		BooleanDefaultTrue b = F.createBooleanDefaultTrue();
		b.setVal(Boolean.TRUE);
		return b;
	}

	private static BooleanDefaultTrue off() {
		BooleanDefaultTrue b = F.createBooleanDefaultTrue();
		b.setVal(Boolean.FALSE);
		return b;
	}

	@Test
	public void twoLevelsSayingTrueCancel() {
		assertFalse(StyleUtil.toggle(on(), on(), null).isVal());
	}

	@Test
	public void oneLevelSayingTrueApplies() {
		assertTrue(StyleUtil.toggle(on(), null, null).isVal());
		assertTrue(StyleUtil.toggle(null, on(), null).isVal());
	}

	@Test
	public void threeLevelsSayingTrueApply() {
		// val(table) XOR val(paragraph) XOR val(character): true for an odd number
		BooleanDefaultTrue twoLevels = StyleUtil.toggle(on(), on(), null);
		assertTrue(StyleUtil.toggle(on(), twoLevels, null).isVal());
	}

	/** An explicit false at a level is a term of the XOR like any other value, which is
	 *  what Word's {@code toggle-levels} golden says (it draws a run in a character style
	 *  stating {@code <w:b w:val="0"/>} over a bold paragraph style BOLD).  17.1.1 shipped
	 *  the other reading - the false applied as it stands - and the golden refuted it
	 *  (CR-001 batch 47 item 0b). */
	@Test
	public void aLevelStatingFalseContributesFalseToTheXor() {
		assertTrue("false XOR true = true: the lower level stands",
				StyleUtil.toggle(off(), on(), null).isVal());
		assertFalse("false XOR false = false", StyleUtil.toggle(off(), off(), null).isVal());
		// 17.7.3's XOR over a single level is that level's own value, and an absent property
		// is not the same as a false one: the exporters then write no font-weight at all and
		// an HTML span inherits its cell's or paragraph's
		assertFalse("the only level stating it states false, so false stands",
				StyleUtil.toggle(off(), null, null).isVal());
	}

	@Test
	public void aLevelSayingNothingLeavesWhatIsBeneathAlone() {
		assertNull(StyleUtil.toggle(null, null, null));
		assertFalse(StyleUtil.toggle(null, off(), null).isVal());
	}

	@Test
	public void theDocumentDefaultsTrueWins() {
		// "If the value specified by the document defaults is true, the effective value is true"
		assertTrue(StyleUtil.toggle(on(), on(), on()).isVal());
		assertTrue(StyleUtil.toggle(off(), off(), on()).isVal());
		// a document default of false is no default at all for this rule
		assertFalse(StyleUtil.toggle(on(), on(), off()).isVal());
	}

	/** The narrowing on {@link StyleUtil#toggle} which Word's {@code toggle-levels-docdefaults}
	 *  golden <b>confirms</b>: the document defaults' rule sits under "if the value appears
	 *  at multiple levels", so a boundary whose upper level says nothing about the property
	 *  is no level and does not raise it.  Word draws bold defaults with a paragraph style
	 *  stating {@code <w:b w:val="0"/>} regular, and regular still when the run names a
	 *  character style which sets only {@code w:color} - where the letter would have made
	 *  that silent style a level and come out bold. */
	@Test
	public void aSilentUpperLevelDoesNotLetTheDocumentDefaultsForceTrueBack() {
		assertFalse(StyleUtil.toggle(null, off(), on()).isVal());
	}

	@Test
	public void aSilentUpperLevelLeavesATrueLowerAlone() {
		assertTrue(StyleUtil.toggle(null, on(), on()).isVal());
		assertTrue(StyleUtil.toggle(null, on(), null).isVal());
	}

	@Test
	public void applyStyleLevelXorsTheTogglesAndOverridesEverythingElse() throws Exception {
		RPr level = (RPr) XmlUtils.unmarshalString("<w:rPr " + W + "><w:b/><w:i/>"
				+ "<w:dstrike/><w:sz w:val=\"24\"/></w:rPr>", Context.jc, RPr.class);
		RPr beneath = (RPr) XmlUtils.unmarshalString("<w:rPr " + W + "><w:b/>"
				+ "<w:dstrike/><w:sz w:val=\"20\"/></w:rPr>", Context.jc, RPr.class);

		StyleUtil.applyStyleLevel(level, beneath, null);

		assertFalse("b is a toggle: true XOR true", beneath.getB().isVal());
		assertTrue("i is stated at one level only", beneath.getI().isVal());
		assertTrue("dstrike is NOT a toggle: the last value wins", beneath.getDstrike().isVal());
		assertEquals("nor is the size", 24, beneath.getSz().getVal().intValue());
	}

	// ------------------------------------------------------------------ through the resolver

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"20\"/></w:rPr></w:rPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"BoldPara\"><w:name w:val=\"Bold Para\"/>"
			+ "<w:basedOn w:val=\"Normal\"/><w:rPr><w:b/></w:rPr></w:style>"
			// a chain WITHIN one level: both say bold, and 17.7.3 does not XOR a basedOn chain
			+ "<w:style w:type=\"paragraph\" w:styleId=\"BoldChild\"><w:name w:val=\"Bold Child\"/>"
			+ "<w:basedOn w:val=\"BoldPara\"/><w:rPr><w:b/></w:rPr></w:style>"
			+ "<w:style w:type=\"character\" w:styleId=\"Strong\"><w:name w:val=\"Strong\"/>"
			+ "<w:rPr><w:b/><w:bCs/></w:rPr></w:style>"
			+ "<w:style w:type=\"character\" w:styleId=\"NotStrong\"><w:name w:val=\"Not Strong\"/>"
			+ "<w:rPr><w:b w:val=\"0\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"BoldFirstCol\"><w:name w:val=\"Bold First Col\"/>"
			+ "<w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:tblStylePr w:type=\"firstCol\"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr>"
			+ "</w:style>"
			+ "</w:styles>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.setContents((Styles) XmlUtils.unmarshalString(STYLES));
		return pkg;
	}

	private static RPr effective(WordprocessingMLPackage pkg, String pStyle, String runRPr)
			throws Exception {
		PPr pPr = null;
		if (pStyle != null) {
			pPr = (PPr) XmlUtils.unmarshalString("<w:pPr " + W + "><w:pStyle w:val=\""
					+ pStyle + "\"/></w:pPr>", Context.jc, PPr.class);
		}
		RPr rPr = runRPr == null ? null
				: (RPr) XmlUtils.unmarshalString("<w:rPr " + W + ">" + runRPr + "</w:rPr>",
						Context.jc, RPr.class);
		return new PropertyResolver(pkg).getEffectiveRPr(rPr, pPr);
	}

	@Test
	public void aCharacterStyleOverABoldParagraphStyleCancelsIt() throws Exception {
		WordprocessingMLPackage pkg = pkg("<w:p/>");
		assertTrue("the paragraph style alone", effective(pkg, "BoldPara", null).getB().isVal());
		assertTrue("the character style alone", effective(pkg, null,
				"<w:rStyle w:val=\"Strong\"/>").getB().isVal());
		assertFalse("both levels: bold XOR bold",
				effective(pkg, "BoldPara", "<w:rStyle w:val=\"Strong\"/>").getB().isVal());
		// w:bCs is its own toggle and the paragraph style says nothing about it, so the
		// character style's is the only level that states it and it survives
		assertTrue("w:bCs is counted on its own, not with w:b",
				effective(pkg, "BoldPara", "<w:rStyle w:val=\"Strong\"/>").getBCs().isVal());
	}

	@Test
	public void aBasedOnChainIsOneLevelAndIsNotXored() throws Exception {
		// "Attempt to read the value in the style.  If it does not exist and the style has
		// a basedOn element ... repeat" - the first value, not the parity of the chain
		WordprocessingMLPackage pkg = pkg("<w:p/>");
		assertTrue(effective(pkg, "BoldChild", null).getB().isVal());
		assertFalse("and it still cancels one character style",
				effective(pkg, "BoldChild", "<w:rStyle w:val=\"Strong\"/>").getB().isVal());
	}

	/** Case 1 of the {@code toggle-levels} golden, and the one reading of 17.1.1's two that
	 *  Word refutes: a character style stating {@code <w:b w:val="0"/>} over a bold
	 *  paragraph style is BOLD in Word's PDF ({@code TimesNewRomanPS-BoldMT}), because the
	 *  false is a term of the XOR and false XOR true = true.  A run that wants the weight
	 *  off has to say so in direct formatting, which is not a level (below). */
	@Test
	public void aCharacterStyleStatingFalseDoesNotUnboldABoldParagraphStyle() throws Exception {
		WordprocessingMLPackage pkg = pkg("<w:p/>");
		assertTrue(effective(pkg, "BoldPara", "<w:rStyle w:val=\"NotStrong\"/>").getB().isVal());
	}

	@Test
	public void directFormattingIsNotALevelAndIsUsedAsItStands() throws Exception {
		// "If a toggle property is explicitly set in direct formatting applied to a given
		// piece of content, then its value in the direct formatting shall be used"
		WordprocessingMLPackage pkg = pkg("<w:p/>");
		assertFalse("direct off over a bold paragraph style",
				effective(pkg, "BoldPara", "<w:b w:val=\"0\"/>").getB().isVal());
		assertTrue("direct on over a bold paragraph style and a bold character style",
				effective(pkg, "BoldPara", "<w:rStyle w:val=\"Strong\"/><w:b/>").getB().isVal());
	}

	// ------------------------------------------------------------------ the table level

	/** The corpus shape: a table style whose firstCol condition is bold, and a run in the
	 *  first column carrying a character style which is bold as well. */
	private static final String TABLE = "<w:tbl><w:tblPr><w:tblStyle w:val=\"BoldFirstCol\"/>"
			+ "<w:tblLook w:val=\"0000\" w:firstRow=\"0\" w:lastRow=\"0\" w:firstColumn=\"1\""
			+ " w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>"
			+ "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>"
			+ "<w:p><w:r><w:rPr><w:rStyle w:val=\"Strong\"/></w:rPr><w:t>first column strong</w:t></w:r></w:p>"
			+ "<w:p><w:r><w:t>first column plain</w:t></w:r></w:p></w:tc>"
			+ "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>"
			+ "<w:p><w:r><w:rPr><w:rStyle w:val=\"Strong\"/></w:rPr><w:t>second column strong</w:t></w:r></w:p></w:tc>"
			+ "</w:tr></w:tbl>";

	private static P para(WordprocessingMLPackage pkg, String text) throws Exception {
		org.docx4j.finders.ClassFinder finder = new org.docx4j.finders.ClassFinder(P.class);
		new org.docx4j.TraversalUtil(pkg.getMainDocumentPart().getContents(), finder);
		for (Object o : finder.results) {
			P p = (P) o;
			if (org.docx4j.TextUtils.getText(p).startsWith(text)) return p;
		}
		throw new AssertionError("no such paragraph");
	}

	private static RPr resolved(WordprocessingMLPackage pkg, String text) throws Exception {
		P p = para(pkg, text);
		R r = null;
		for (Object o : p.getContent()) {
			if (o instanceof R) { r = (R) o; break; }
		}
		assertNotNull(r);
		return new PropertyResolver(pkg).getEffectiveRPr(r.getRPr(), p.getPPr());
	}

	@Test
	public void aTableConditionAndACharacterStyleCancelEachOther() throws Exception {
		WordprocessingMLPackage pkg = pkg(TABLE);
		ParagraphStylesInTableFix.process(pkg);

		assertFalse("the first column is bold and so is Strong: 17.7.3 cancels them",
				resolved(pkg, "first column strong").getB().isVal());
		assertTrue("the same cell without the character style stays bold",
				resolved(pkg, "first column plain").getB().isVal());
		assertTrue("and outside the first column the character style is all there is",
				resolved(pkg, "second column strong").getB().isVal());
	}
}
