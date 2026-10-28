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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * The IF-field probe (CR-067, fields/cr067-iffield; Jason's question 2026-10-29) through docx4j's extractors: the text
 * model's cached mode is the stored result, its update mode the computed one, and the HTML writer paints the stored
 * result where there is one and evaluates a field that stores none, a nested field folded into its outer's instruction
 * rather than painted beside it (the combiner, fixed the same day).
 */
public class IfFieldProbeWritersTest {

	static WordprocessingMLPackage load() throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-iffield/iffield-probe-r1.docx")) {
			return WordprocessingMLPackage.load(is);
		}
	}

	/** The case texts by token, from a text whose lines are "Cnn_LABEL = value". */
	static Map<String, String> cases(String text) {
		Map<String, String> m = new LinkedHashMap<String, String>();
		Matcher mt = Pattern.compile("(C\\d\\d_[A-Z_]+) = (.*?)(?=C\\d\\d_[A-Z_]+ = |End of the probe|$)", Pattern.DOTALL).matcher(text);
		while (mt.find()) m.put(mt.group(1), mt.group(2).replaceAll("\\s+", " ").trim());
		return m;
	}

	static final String[][] EXPECTED = { // token, cached, update, html
			{ "C01_LITERAL_IF", "REJECTED", "APPROVED", "REJECTED" },
			{ "C02_SIMPLE_FIELD", "REJECTED", "APPROVED", "REJECTED" },
			{ "C03_DOCPROPERTY_COND", "TITLE_MISMATCH", "TITLE_MATCH", "TITLE_MISMATCH" },
			{ "C04_DATE_COND", "STALE_OUTER", "EXPIRED", "STALE_OUTER" },
			{ "C05_FORMULA", "3", "2", "3" },
			{ "C06_COMPARE", "0", "1", "0" },
			{ "C07_LOCKED", "REJECTED", "REJECTED", "REJECTED" },
			{ "C08_NO_RESULT", "", "APPROVED", "APPROVED" },
			{ "C09_NO_SEPARATE", "", "APPROVED", "APPROVED" },
			{ "C10_NESTED_BRANCH", "LIT", "Probe", "LIT" },
			{ "C11_REF_EDITED", "REF_OLD", "REF_NEW", "REF_OLD" },
			{ "C12_HIDDEN_TRUE", "VISIBLE_STALE", "", "VISIBLE_STALE" }, // the hidden true branch: nothing, as Word (measured 2026-10-29)
			{ "C13_HAND_EDITED", "NEITHER_BRANCH_TEXT", "APPROVED", "NEITHER_BRANCH_TEXT" },
			{ "C14_CONTROL_TRUE", "APPROVED", "APPROVED", "APPROVED" },
			{ "C15_SIMPLE_EMPTY", "", "APPROVED", "APPROVED" },
			{ "C16_WILDCARD", "WILD_MISS", "WILD_MATCH", "WILD_MISS" } };

	@Test
	public void textModelCachedAndUpdate() throws Exception {
		Map<String, String> cached = cases(PlainText.of(Texts.of(load(), new TextOptions())));
		Map<String, String> update = cases(PlainText.of(Texts.of(load(), new TextOptions().fields(TextOptions.Fields.UPDATE))));
		for (String[] c : EXPECTED) {
			assertEquals(c[0] + " cached", c[1], cached.get(c[0]));
			assertEquals(c[0] + " update", c[2], update.get(c[0]));
		}
	}

	@Test
	public void htmlWriterPaintsStoredElseEvaluates() throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(load());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
		Map<String, String> html = cases(baos.toString("UTF-8").replaceAll("</p>", "\n").replaceAll("<[^>]+>", ""));
		for (String[] c : EXPECTED) assertEquals(c[0] + " html", c[3], html.get(c[0]));
	}
}
