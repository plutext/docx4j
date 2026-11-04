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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * {@code profile} (WD 0.41, REQ-1002; Jason: "profile: word-text makes sense", 2026-10-29): a preset applied before the
 * caller's own options, whichever came first; {@code word-text} is Word's Save As plain text as the options reach it.
 */
public class TextOptionsProfileTest {

	@Test
	public void wordTextIsWordsPlainTextAsTheOptionsReachIt() {
		TextOptions o = new TextOptions().profile(TextOptions.Profile.WORD_TEXT);
		assertEquals(TextView.ACCEPTED, o.getView());
		assertTrue(o.isHidden());
		assertTrue(o.getInclude().contains(TextOptions.Include.HEADERS) && o.getInclude().contains(TextOptions.Include.FOOTERS));
		assertTrue(o.isComments());
		assertFalse(o.isTextBoxes());
		assertTrue(o.isNotes() && o.isNoteReferences());
		assertTrue("Word's Save As Plain Text writes list labels (WD 0.75; label-separator probe)", o.isListLabels());
		assertEquals(TextOptions.Fields.CACHED, o.getFields());
		assertFalse(o.isFieldCodes());
		assertTrue(o.isToc());
		assertTrue(o.isPlaceholders());
		assertEquals(TextOptions.Bindings.BOUND, o.getBindings());
		assertTrue(o.isAltchunks());
		assertEquals(TextOptions.Symbols.ASIS, o.getSymbols());
		assertEquals(TextOptions.Tables.TSV, o.getTables());
		assertFalse(o.isPageMarkers());
		assertEquals(TextOptions.Layout.NONE, o.getLayout());
		assertFalse(o.isAltText() || o.isMetadata() || o.isCharts() || o.isSmartart() || o.isEmbedded());
		assertEquals(TextOptions.MathFormat.LATEX, o.getMath()); // Word's linear math is not produced (unicodemath not implemented)
		assertEquals("word-text", o.toMap().get("profile"));
	}

	@Test
	public void theCallersOptionOverridesTheProfileWhicheverCameFirst() {
		TextOptions after = new TextOptions().profile(TextOptions.Profile.WORD_TEXT).fields(TextOptions.Fields.UPDATE).tables(TextOptions.Tables.GRID);
		assertEquals(TextOptions.Fields.UPDATE, after.getFields());
		assertEquals(TextOptions.Tables.GRID, after.getTables());
		TextOptions before = new TextOptions().fields(TextOptions.Fields.UPDATE).tables(TextOptions.Tables.GRID).profile(TextOptions.Profile.WORD_TEXT);
		assertEquals(TextOptions.Fields.UPDATE, before.getFields());
		assertEquals(TextOptions.Tables.GRID, before.getTables());
		assertTrue(before.isHidden()); // the rest of the preset applied
		// by WD name, in either map order, as docx4j-mcp passes them
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("fields", "update");
		m.put("profile", "word-text");
		TextOptions fromMap = TextOptions.of(m);
		assertEquals(TextOptions.Fields.UPDATE, fromMap.getFields());
		assertTrue(fromMap.isHidden() && fromMap.isToc());
		Map<String, Object> m2 = new LinkedHashMap<String, Object>();
		m2.put("profile", "WORD_TEXT");
		m2.put("toc", false);
		assertFalse(TextOptions.of(m2).isToc());
		assertTrue(TextOptions.of(m2).isHidden());
	}

	@Test(expected = IllegalArgumentException.class)
	public void anUnknownProfileIsRefused() {
		TextOptions.of(java.util.Collections.singletonMap("profile", "libreoffice"));
	}

	/** Under the preset a document reads as Word's plain text where the two agree (the memo's stored results). */
	@Test
	public void theMemoUnderThePreset() throws Exception {
		WordprocessingMLPackage pkg;
		try (InputStream is = ResourceUtils.getResource("fields/cr067-fields-fun/one-file-many-readings.docx")) {
			pkg = WordprocessingMLPackage.load(is);
		}
		String text = PlainText.of(Texts.of(pkg, new TextOptions().profile(TextOptions.Profile.WORD_TEXT)));
		assertTrue(text, text.contains("Payment terms: Net 90 days.") && text.contains("Discount: 10%.") && text.contains("Reference: Purchase approval."));
	}
}
