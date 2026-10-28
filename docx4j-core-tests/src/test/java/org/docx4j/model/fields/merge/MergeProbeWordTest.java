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
package org.docx4j.model.fields.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.After;
import org.junit.Test;

/**
 * CR-067 phase 2, the merge conditionals against Word: the probe template {@code fields/cr067-merge/merge-probe-r1.docx}
 * merged by {@link MailMerger} over the five records of {@code merge-data-r1.docx} gives, letter by letter and case by
 * case, what Word 365 (version 2608, build 20326.20232, 2026-10-28) wrote into {@code merge-probe-r1-merged.docx}
 * (Finish and Merge, Edit Individual Documents, the ASK and FILLIN defaults accepted). Three letters: record 1;
 * record 3 whose second half pulls record 4 (NEXTIF); record 5; record 2 skipped (SKIPIF). Read with a tolerance: M08
 * (Word printed {@code $ 150.00}, a space after the sign, under {@code \# "$#,##0.00"}; docx4j prints none) and M15
 * (a MERGEFIELD naming no column: Word stopped with its Invalid Merge Field dialog and Jason replaced it with Name;
 * docx4j's REMOVED fate drops the field).
 */
public class MergeProbeWordTest {

	@After
	public void reset() {
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
	}

	static WordprocessingMLPackage load(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-merge/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	/** The five records of merge-data-r1.docx. */
	static List<Map<DataFieldName, String>> records() {
		String[][] rows = { { "Ann", "", "150", "Sydney" }, { "Bob", "J", "50", "Melbourne" }, { "Cat", "", "200", "Perth" },
				{ "Dan", "K", "300", "Hobart" }, { "Eve", "", "120", "Darwin" } };
		List<Map<DataFieldName, String>> data = new ArrayList<Map<DataFieldName, String>>();
		for (String[] r : rows) {
			Map<DataFieldName, String> m = new HashMap<DataFieldName, String>();
			m.put(new DataFieldName("Name"), r[0]);
			m.put(new DataFieldName("Middle"), r[1]);
			m.put(new DataFieldName("Amount"), r[2]);
			m.put(new DataFieldName("City"), r[3]);
			data.add(m);
		}
		return data;
	}

	/** The letters (sections) of a merged document: each case's text by token, the field's result or its unlinked text. */
	static List<Map<String, String>> letters(WordprocessingMLPackage pkg) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		List<Map<String, String>> out = new ArrayList<Map<String, String>>();
		Map<String, String> letter = new LinkedHashMap<String, String>();
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) {
			String p = m.group();
			// the token and its text as shown (Word's merge writes an unlinked case as one run, "M17_REF_GREETING = Hello")
			Matcher t = Pattern.compile("^(M\\d\\d_[A-Z0-9_]+) = (.*)$", Pattern.DOTALL).matcher(shownText(p));
			if (t.find()) letter.put(t.group(1), t.group(2));
			if (p.contains("<w:sectPr")) {
				out.add(letter);
				letter = new LinkedHashMap<String, String>();
			}
		}
		if (!letter.isEmpty()) out.add(letter);
		return out;
	}

	/** The paragraph's text as shown: outside any field, or an outermost field's result. */
	static String shownText(String p) {
		Matcher m = Pattern.compile("fldCharType=\"(begin|separate|end)\"|<w:t(?: [^>]*)?>([^<]*)</w:t>").matcher(p);
		int depth = 0;
		boolean inResult = false;
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			String kind = m.group(1);
			if ("begin".equals(kind)) depth++;
			else if ("separate".equals(kind)) { if (depth == 1) inResult = true; }
			else if ("end".equals(kind)) { if (depth == 1) inResult = false; depth--; }
			else if (depth == 0 || (inResult && depth == 1)) sb.append(m.group(2));
		}
		return sb.toString();
	}

	@Test
	public void theMergerGivesWordsLetters() throws Exception {
		List<Map<String, String>> word = letters(load("merge-probe-r1-merged.docx"));
		assertEquals("three letters: record 2 skipped, record 4 pulled into record 3's", 3, word.size());
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
		WordprocessingMLPackage merged = new MailMerger(load("merge-probe-r1.docx")).getConsolidatedResultCrude(records(), false);
		List<Map<String, String>> ours = letters(merged);
		assertEquals(3, ours.size());
		int compared = 0;
		for (int i = 0; i < 3; i++) {
			assertEquals("letter " + (i + 1) + " cases", 27, word.get(i).size());
			for (Map.Entry<String, String> e : word.get(i).entrySet()) {
				String token = e.getKey(), expected = e.getValue(), actual = ours.get(i).get(token);
				String where = "letter " + (i + 1) + " " + token;
				if (token.startsWith("M08_")) assertEquals(where + " (Word: a space after $)", expected.replace(" ", ""), actual.replace(" ", ""));
				else if (token.startsWith("M15_")) assertEquals(where + " (no column: dropped)", "", actual);
				else assertEquals(where, expected, actual);
				compared++;
			}
		}
		assertEquals(81, compared);
		// the record numbers, before and after the NEXTIF
		assertEquals("4", ours.get(1).get("M25_MERGEREC_AFTER"));
		assertEquals("2", ours.get(1).get("M26_MERGESEQ_AFTER"));
		assertEquals("Dan", ours.get(1).get("M24_AFTER_NEXTIF"));
		// unlinked, as Word's merged document is: no field left in the letters' cases
		String xml = XmlUtils.marshaltoString(merged.getMainDocumentPart().getJaxbElement(), true, false);
		assertFalse(xml.contains(" SKIPIF "));
		assertFalse(xml.contains(" NEXTIF "));
		assertFalse(xml.contains(" MERGEREC "));
		assertFalse(xml.contains(" ASK "));
		assertFalse(xml.contains(" SET "));
		assertFalse(xml.contains("fldChar"));
	}

	@Test
	public void keepMergefieldKeepsTheFieldsWithTheirResults() throws Exception {
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.KEEP_MERGEFIELD);
		WordprocessingMLPackage merged = new MailMerger(load("merge-probe-r1.docx")).getConsolidatedResultCrude(records(), false);
		List<Map<String, String>> ours = letters(merged);
		assertEquals(3, ours.size());
		assertEquals("Cat", ours.get(1).get("M03_NAME"));
		assertEquals("Dan", ours.get(1).get("M24_AFTER_NEXTIF"));
		assertEquals("201", ours.get(1).get("M14_REF_TOTAL"));
		assertEquals("Hello", ours.get(1).get("M17_REF_GREETING"));
		String xml = XmlUtils.marshaltoString(merged.getMainDocumentPart().getJaxbElement(), true, false);
		assertTrue(xml.contains(" MERGEFIELD Name "));
		assertTrue(xml.contains(" MERGEREC "));
		assertTrue("a NEXTIF or SKIPIF is consumed whatever the fate", !xml.contains(" SKIPIF ") && !xml.contains(" NEXTIF "));
	}

	@Test
	public void answersReachAskAndFillin() throws Exception {
		MailMerger merger = new MailMerger(load("merge-probe-r1.docx"));
		merger.getFieldContext().answers().put("Greeting", "G'day");
		merger.getFieldContext().answers().put("Your name?", "Jason");
		List<Map<String, String>> ours = letters(merger.getConsolidatedResultCrude(records(), false));
		assertEquals("G'day", ours.get(0).get("M17_REF_GREETING"));
		assertEquals("Jason", ours.get(2).get("M20_FILLIN_ONCE"));
		assertEquals("the default where no answer is given", "Hi", ours.get(2).get("M19_REF_PROMPT2"));
	}
}
