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
package org.docx4j.openpackaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.Docx4jProperties.DefaultTheme;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.STTheme;
import org.junit.After;
import org.junit.Test;

/**
 * A package docx4j creates carries a theme part, as one Word creates does.
 *
 * <p>Until 17.2.0 it did not, while its own docDefaults reference the theme fonts
 * (w:rFonts w:asciiTheme="minorHAnsi" ...), so every consumer - docx4j's own exporters
 * included - had to guess which faces the Office theme names.  Word's answer has changed
 * twice, so which theme is added is {@code docx4j.fonts.defaultTheme}'s to say.
 */
public class CreatePackageThemePartTest {

	private static final PartName THEME = partName();

	private static PartName partName() {
		try {
			return new PartName("/word/theme/theme1.xml");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@After
	public void restoreDefault() {
		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, DefaultTheme.THEME_2023.value());
	}

	private static ThemePart themePartOf(WordprocessingMLPackage pkg) {
		return (ThemePart) pkg.getParts().get(THEME);
	}

	/** The part is there, is related from the main document part, and carries the theme
	 *  content type - the three things a consumer and Word both look for. */
	@Test
	public void createPackageAddsAThemePart() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();

		ThemePart theme = themePartOf(pkg);
		assertNotNull("createPackage added no theme part", theme);
		assertEquals(ContentTypes.OFFICEDOCUMENT_THEME, theme.getContentType());
		assertEquals(Namespaces.THEME, theme.getRelationshipType());
		assertNotNull("the theme part is not related from the main document part",
				pkg.getMainDocumentPart().getRelationshipsPart().getRelationshipByType(Namespaces.THEME));
	}

	/** Each property value brings its own theme, and the faces it names are the ones
	 *  RunFontSelector answers for a package that has none. */
	@Test
	public void theThemeIsTheOneThePropertyNames() throws Exception {

		for (DefaultTheme t : DefaultTheme.values()) {
			Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, t.value());
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			ThemePart theme = themePartOf(pkg);
			assertNotNull(t.value() + ": no theme part", theme);
			assertEquals(t.value() + ": the minor Latin face", t.minorLatin(),
					theme.getFont(STTheme.MINOR_H_ANSI, null));
			assertEquals(t.value() + ": the major Latin face", t.majorLatin(),
					theme.getFont(STTheme.MAJOR_H_ANSI, null));
		}
	}

	/** Saved and loaded again, the part is still there with the same faces: it is a real
	 *  part of the zip, not an object graph fiction. */
	@Test
	public void theThemeSurvivesASaveAndLoad() throws Exception {

		Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, DefaultTheme.THEME_2013.value());
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		assertTrue("nothing was written", baos.size() > 0);

		WordprocessingMLPackage loaded =
				WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
		ThemePart theme = themePartOf(loaded);
		assertNotNull("the theme part did not survive the round trip", theme);
		assertEquals("Calibri", theme.getFont(STTheme.MINOR_H_ANSI, null));
		assertEquals("Calibri Light", theme.getFont(STTheme.MAJOR_H_ANSI, null));
	}
}
