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
package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * The "one file, many readings" memo (CR-067 §14.7, fields/cr067-fields-fun) through docx4j: cached mode is the stored
 * text, update mode is Word's F9 text except where the WD never computes (DATE, PAGE) and where Word's F9 text is
 * itself hidden; an IF whose true branch is a hidden run gives the empty result Word gives (measured 2026-10-29).
 */
public class FieldsFunMemoTest {

	static final String[] KEYS = { "Prepared in", "Payment terms", "Amount due", "Discount", "Supplier code", "Subtotal check",
			"Approval status", "Printed on page", "Reviewer note", "Reference" };

	static WordprocessingMLPackage load() throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-fields-fun/one-file-many-readings.docx")) {
			return WordprocessingMLPackage.load(is);
		}
	}

	static Map<String, String> sentences(String text) {
		Map<String, String> m = new LinkedHashMap<String, String>();
		for (String line : text.split("\\r?\\n")) for (String k : KEYS) if (line.contains(k) && !m.containsKey(k)) m.put(k, line.substring(line.indexOf(k)).trim());
		return m;
	}

	@Test
	public void cachedIsStoredAndUpdateIsWordsF9() throws Exception {
		Map<String, String> cached = sentences(PlainText.of(Texts.of(load(), new TextOptions())));
		Map<String, String> update = sentences(PlainText.of(Texts.of(load(), new TextOptions().fields(TextOptions.Fields.UPDATE))));
		String[][] expected = { // key, cached (Word's plain text of the file as built), update (Word's F9 text, two exceptions)
				{ "Prepared in", "Prepared in 2020.", "Prepared in 2020." }, // the WD never computes DATE (Word: 2026)
				{ "Payment terms", "Payment terms: Net 90 days.", "Payment terms: Net 30 days." },
				{ "Amount due", "Amount due: $500.", "Amount due: $750." },
				{ "Discount", "Discount: 10%.", "Discount: 10%." },
				{ "Supplier code", "Supplier code matches our approved list: no.", "Supplier code matches our approved list: yes." },
				{ "Subtotal check", "Subtotal check: 2 + 2 = 5.", "Subtotal check: 2 + 2 = 4." },
				{ "Approval status", "Approval status: rejected.", "Approval status: rejected." },
				{ "Printed on page", "Printed on page two or later.", "Printed on page two or later." }, // PAGE is not computed (Word's F9: one)
				{ "Reviewer note", "Reviewer note: none.", "Reviewer note: ." }, // the hidden true branch: nothing, as Word
				{ "Reference", "Reference: Purchase approval.", "Reference: Purchase approval." } };
		for (String[] e : expected) {
			assertEquals(e[0] + " cached", e[1], cached.get(e[0]));
			assertEquals(e[0] + " update", e[2], update.get(e[0]));
		}
		// the updater agrees with the text model's update
		WordprocessingMLPackage pkg = load();
		new FieldUpdater(pkg).update(false);
		Map<String, String> updated = sentences(PlainText.of(Texts.of(pkg, new TextOptions())));
		for (String[] e : expected) if (!e[0].equals("Prepared in")) assertEquals(e[0] + " updater", e[2], updated.get(e[0]));
	}
}
