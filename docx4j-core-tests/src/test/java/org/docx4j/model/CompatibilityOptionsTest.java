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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.jaxb.Context;
import org.docx4j.model.CompatibilityOptions.Flag;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTSettings;
import org.junit.Test;

/**
 * A w:compat flag resolves to the value the document states, and where it states none, to
 * the value its compatibilityMode implies (word-layout-settings.md &sect;2).
 */
public class CompatibilityOptionsTest {

	// ---------------------------------------------------------------- helpers

	/** A package at this mode (0 = no compatibilityMode setting at all). */
	private static WordprocessingMLPackage pkg(int mode) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart(true);
		if (mode > 0) dsp.setWordCompatSetting("compatibilityMode", Integer.toString(mode));
		return pkg;
	}

	private static CTCompat compat(WordprocessingMLPackage pkg) throws Exception {
		CTSettings s = pkg.getMainDocumentPart().getDocumentSettingsPart().getContents();
		if (s.getCompat() == null) s.setCompat(Context.getWmlObjectFactory().createCTCompat());
		return s.getCompat();
	}

	private static BooleanDefaultTrue onOff(boolean val) {
		BooleanDefaultTrue b = new BooleanDefaultTrue();
		b.setVal(Boolean.valueOf(val));
		return b;
	}

	// ------------------------------------------------- the mode supplies the default

	@Test
	public void modeSuppliesTheDefault() throws Exception {
		/* A legacy flag the document does not state is off in every mode, measured: the
		 * bundle table says what Word writes at each mode, not what its engine applies to
		 * a document which leaves the flag out. */
		for (int mode : new int[] { 11, 12, 14, 15 }) {
			CompatibilityOptions o = CompatibilityOptions.ofMode(mode);
			assertFalse("growAutofit at mode " + mode, o.is(Flag.GROW_AUTOFIT));
			assertFalse("useWord2002TableStyleRules at mode " + mode,
					o.is(Flag.USE_WORD2002_TABLE_STYLE_RULES));
			assertFalse("forgetLastTabAlignment at mode " + mode,
					o.is(Flag.FORGET_LAST_TAB_ALIGNMENT));
			assertFalse("doNotExpandShiftReturn at mode " + mode,
					o.is(Flag.DO_NOT_EXPAND_SHIFT_RETURN));
		}

		// suppressSpBfAfterPgBrk is the one that arrives with the newer engine
		assertFalse(CompatibilityOptions.ofMode(11).is(Flag.SUPPRESS_SP_BF_AFTER_PG_BRK));
		assertFalse(CompatibilityOptions.ofMode(14).is(Flag.SUPPRESS_SP_BF_AFTER_PG_BRK));
		assertTrue(CompatibilityOptions.ofMode(15).is(Flag.SUPPRESS_SP_BF_AFTER_PG_BRK));

		// splitPgBreakAndParaMark: what Word was measured doing in every mode
		for (int mode : new int[] { 11, 12, 14, 15 }) {
			assertTrue("mode " + mode,
					CompatibilityOptions.ofMode(mode).is(Flag.SPLIT_PG_BREAK_AND_PARA_MARK));
		}

		// and the flags no Word version writes are off everywhere
		for (int mode : new int[] { 11, 12, 14, 15 }) {
			CompatibilityOptions o = CompatibilityOptions.ofMode(mode);
			assertFalse("mode " + mode, o.is(Flag.NO_LEADING));
			assertFalse("mode " + mode, o.is(Flag.NO_TAB_HANG_IND));
			assertFalse("mode " + mode, o.is(Flag.DO_NOT_USE_INDENT_AS_NUMBERING_TAB_STOP));
			assertFalse("mode " + mode, o.is(Flag.ALLOW_SPACE_OF_SAME_STYLE_IN_TABLE));
		}
	}

	@Test
	public void modeIsReadFromThePackage() throws Exception {
		for (int mode : new int[] { 11, 12, 14, 15 }) {
			CompatibilityOptions o = CompatibilityOptions.of(pkg(mode));
			assertEquals(mode, o.mode());
			assertFalse("growAutofit at mode " + mode, o.is(Flag.GROW_AUTOFIT));
			assertFalse("doNotExpandShiftReturn at mode " + mode,
					o.is(Flag.DO_NOT_EXPAND_SHIFT_RETURN));
			assertEquals("suppressSpBfAfterPgBrk at mode " + mode,
					Boolean.valueOf(mode >= 15),
					Boolean.valueOf(o.is(Flag.SUPPRESS_SP_BF_AFTER_PG_BRK)));
			assertFalse(o.isStated(Flag.GROW_AUTOFIT));
		}
	}

	/** A settings part which names no compatibilityMode is mode 12, as Word opens it. */
	@Test
	public void noCompatibilityModeSettingIsMode12() throws Exception {
		CompatibilityOptions o = CompatibilityOptions.of(pkg(0));
		assertEquals(12, o.mode());
		assertFalse(o.is(Flag.SUPPRESS_SP_BF_AFTER_PG_BRK));
	}

	// ------------------------------------------------------- the explicit flag wins

	@Test
	public void explicitFlagWins() throws Exception {
		// stated true where the mode says off
		WordprocessingMLPackage on = pkg(15);
		compat(on).setGrowAutofit(onOff(true));
		compat(on).setDoNotExpandShiftReturn(onOff(true));
		compat(on).setNoLeading(onOff(true));
		CompatibilityOptions o = CompatibilityOptions.of(on);
		assertTrue(o.is(Flag.GROW_AUTOFIT));
		assertTrue(o.is(Flag.DO_NOT_EXPAND_SHIFT_RETURN));
		assertTrue(o.is(Flag.NO_LEADING));
		assertTrue(o.isStated(Flag.GROW_AUTOFIT));
		assertFalse(o.isStated(Flag.NO_TAB_HANG_IND));

		/* Stated false where the mode says on: "absent" and "explicitly false" must not be
		 * the same thing (BooleanDefaultTrue.isVal() cannot tell them apart).  The mode
		 * that says on here is 15 for suppressSpBfAfterPgBrk; the legacy flags are off in
		 * every mode, so stating them false only confirms the state is recorded. */
		WordprocessingMLPackage off = pkg(11);
		compat(off).setSuppressSpBfAfterPgBrk(onOff(false));
		compat(off).setGrowAutofit(onOff(false));
		compat(off).setDoNotExpandShiftReturn(onOff(false));
		CompatibilityOptions p = CompatibilityOptions.of(off);
		assertTrue(p.isStated(Flag.GROW_AUTOFIT));
		assertFalse(p.is(Flag.GROW_AUTOFIT));
		assertFalse(p.is(Flag.DO_NOT_EXPAND_SHIFT_RETURN));
		// and one it does not state takes mode 11's default, which is off
		assertFalse(p.is(Flag.FORGET_LAST_TAB_ALIGNMENT));
		assertFalse(p.isStated(Flag.FORGET_LAST_TAB_ALIGNMENT));
	}

	/** An element with no w:val is true, as CT_OnOff says. */
	@Test
	public void statedWithNoValIsTrue() throws Exception {
		WordprocessingMLPackage pkg = pkg(15);
		compat(pkg).setNoLeading(new BooleanDefaultTrue());
		assertTrue(CompatibilityOptions.of(pkg).is(Flag.NO_LEADING));
	}

	// --------------------------------------------------------- no settings part at all

	@Test
	public void noSettingsPartTakesWord365Defaults() throws Exception {
		WordprocessingMLPackage pkg = new WordprocessingMLPackage();
		MainDocumentPart mdp = new MainDocumentPart();
		mdp.setJaxbElement(Context.getWmlObjectFactory().createDocument());
		pkg.addTargetPart(mdp);
		assertNull(mdp.getDocumentSettingsPart());

		CompatibilityOptions o = CompatibilityOptions.of(pkg);
		for (Flag f : Flag.values()) {
			assertEquals(f.name(), Boolean.valueOf(f.defaultAt(15)), Boolean.valueOf(o.is(f)));
			assertFalse(f.name(), o.isStated(f));
		}
		assertFalse(o.is(Flag.DO_NOT_EXPAND_SHIFT_RETURN));
		assertFalse(o.is(Flag.GROW_AUTOFIT));
		assertTrue(o.is(Flag.SUPPRESS_SP_BF_AFTER_PG_BRK));
	}

	@Test
	public void nullPackageDoesNotThrow() {
		CompatibilityOptions o = CompatibilityOptions.of(null);
		assertFalse(o.is(Flag.GROW_AUTOFIT));
	}

	// ------------------------------------------------------------ w:compatSetting

	@Test
	public void compatSettingByName() throws Exception {
		WordprocessingMLPackage pkg = pkg(15);
		// createPackage sets overrideTableStyleFontSizeAndJustification to 1
		assertTrue(CompatibilityOptions.of(pkg)
				.setting("overrideTableStyleFontSizeAndJustification", false));
		assertFalse(CompatibilityOptions.of(pkg).setting("noSuchSetting", false));
		assertTrue(CompatibilityOptions.of(pkg).setting("noSuchSetting", true));

		pkg.getMainDocumentPart().getDocumentSettingsPart()
				.setWordCompatSetting("differentiateMultirowTableHeaders", "0");
		assertFalse(CompatibilityOptions.of(pkg)
				.setting("differentiateMultirowTableHeaders", true));
	}
}
