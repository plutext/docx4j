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
package org.docx4j.textextraction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * Word's document-property controls (Insert, Quick Parts, Document Property), their properties changed
 * after the last save: the text model's bound view shows the property, as Word does (measured in Word
 * 365 on docx4j-portfolio's bind-props probe, 2026-10-22, the fixture here), for the core properties,
 * the extended properties and the cover-page properties. Until 17.3.4 a control bound to a cover-page
 * property showed its stored surface: the cover-page part is a {@code JaxbCustomXmlDataStoragePart}, not
 * a {@code CustomXmlDataStoragePart}, and {@code BoundValues} read only the latter (docx4j-portfolio's
 * finding).
 */
public class BoundPropertiesTest {

	private static String text(TextOptions options) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
				System.getProperty("user.dir") + "/src/test/resources/text-extraction/bind-props.docx"));
		return Texts.plainText(pkg, options);
	}

	@Test
	public void everyDocumentPropertyControlShowsItsProperty() throws Exception {
		String text = text(TextOptions.of(TextView.ACCEPTED));
		assertTrue(text, text.contains("PR1_CORE_TITLE") && text.contains("PR2_CORE_AUTHOR")
				&& text.contains("PR3_APP_COMPANY") && text.contains("PR4_COVER_ABSTRACT"));
		assertFalse(text, text.contains("PR1_STALE_TITLE") || text.contains("PR2_STALE_AUTHOR")
				|| text.contains("PR3_STALE_COMPANY") || text.contains("PR4_STALE_ABSTRACT"));
	}

	@Test
	public void theSurfaceViewShowsWhatWasStored() throws Exception {
		String text = text(TextOptions.of(TextView.ACCEPTED).set("bindings", "surface"));
		assertTrue(text, text.contains("PR4_STALE_ABSTRACT") && text.contains("PR1_STALE_TITLE"));
		assertFalse(text, text.contains("PR4_COVER_ABSTRACT"));
	}
}
