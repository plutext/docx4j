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
package org.docx4j.fonts;

import static org.docx4j.fonts.FontsTestSupport.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.docx4j.Docx4jProperties;
import org.docx4j.Docx4jProperties.DefaultTheme;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Test;

/**
 * A theme reference in a document with no theme part resolves to the Office theme's face,
 * at the run level as for the document default: Word supplies that theme to such a
 * document (CR-016 probe fonts-missing-slots (b), where the explicit w:ascii beside the
 * reference is unused).  Until 17.2.0 the run fell to the document default font.
 *
 * <p>Which faces the Office theme names depends on the version of Word, and
 * {@code docx4j.fonts.defaultTheme} says which docx4j answers for.  Measured on a Word 365
 * rendering of a package with no theme part at all (probe theme-fonts-no-theme-part,
 * CR-001 batch 49): the minor slot draws ten digits 58.865pt wide and a-z 137.923pt at
 * 11pt, which is Aptos and not the 55.28 / 130.32 of an explicit Calibri on the same page;
 * the major slot is 57.40 / 129.16, Aptos Display, against 61.00 / 139.55 for Cambria.
 * Office 2013 to 2022 named Calibri Light / Calibri and Office 2007 to 2010 Cambria /
 * Calibri, which is the answer docx4j gave up to 17.1.0.
 */
public class RunFontSelectorNoThemePartTest {

	@After
	public void restoreDefault() {
		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, DefaultTheme.THEME_2023.value());
	}

	/** Three paragraphs: a minorHAnsi reference with an explicit w:ascii beside it (which
	 *  Word ignores), a majorHAnsi reference, and an explicit face as the control. */
	private static WordprocessingMLPackage themeless(String minorFace, String majorFace) throws Exception {
		return packageWith(
				styles("<w:sz w:val=\"22\"/>", null), null, null,
				p("<w:rFonts w:asciiTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" w:ascii=\"FontA\" w:hAnsi=\"FontA\"/>", "Hello")
				+ p("<w:rFonts w:asciiTheme=\"majorHAnsi\" w:hAnsiTheme=\"majorHAnsi\"/>", "Hello")
				+ p("<w:rFonts w:ascii=\"FontA\" w:hAnsi=\"FontA\"/>", "Hello"),
				minorFace, SANS, majorFace, SERIF, "FontA", SERIF, "Times New Roman", SERIF);
	}

	private void check(DefaultTheme theme) throws Exception {
		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, theme.value());
		assertEquals(theme, Docx4jProperties.getDefaultTheme());
		WordprocessingMLPackage pkg = themeless(theme.minorLatin(), theme.majorLatin());
		assertEquals(theme.value() + ": minorHAnsi is " + theme.minorLatin()
				+ ", not the explicit name beside it", Arrays.asList(SANS), families(pkg, 0));
		assertEquals(theme.value() + ": majorHAnsi is " + theme.majorLatin(),
				Arrays.asList(SERIF), families(pkg, 1));
		assertEquals(theme.value() + ": an explicit face is untouched",
				Arrays.asList(SERIF), families(pkg, 2));
	}

	/** The default: Word 365's Aptos Display / Aptos. */
	@Test
	public void theme2023IsTheDefault() throws Exception {
		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, "");
		assertEquals(DefaultTheme.THEME_2023, Docx4jProperties.getDefaultTheme());
		check(DefaultTheme.THEME_2023);
	}

	@Test
	public void theme2013IsCalibriLightAndCalibri() throws Exception {
		check(DefaultTheme.THEME_2013);
	}

	/** docx4j's answer up to 17.1.0, and Office 2007 to 2010's. */
	@Test
	public void theme2007IsCambriaAndCalibri() throws Exception {
		check(DefaultTheme.THEME_2007);
	}

	/** A value that names no theme falls back to the default rather than throwing. */
	@Test
	public void anUnknownValueFallsBackToTheDefault() throws Exception {
		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, "1997");
		assertEquals(DefaultTheme.THEME_2023, Docx4jProperties.getDefaultTheme());
	}
}
