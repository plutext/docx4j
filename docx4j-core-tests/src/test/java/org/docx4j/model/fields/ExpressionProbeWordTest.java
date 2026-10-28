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
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-067 phase 2 against Word: the expression probe {@code fields/cr067-phase2/expr-probe-r1.docx} (115 formula,
 * COMPARE, IF, SET/REF cases and a table of cell formulas) updated by FieldUpdater gives, case for case, what Word 365
 * (version 2608, build 20326.20232, 2026-10-28, regional format en-AU) wrote into {@code expr-probe-r1-word365.docx}
 * on F9, that file being the oracle (D6). Two cases are read with a tolerance: F07 ({@code =2^0.5}: Word shows 1.4,
 * unexplained, section 9; this gives 1.41) and F20 (Word's text carries two stray characters after
 * {@code !Undefined Bookmark, NOPE}). The text model's {@code fields: update} gives the same for every paragraph case.
 */
public class ExpressionProbeWordTest {

	static WordprocessingMLPackage load(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-phase2/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	/** Each case's outermost field result, by token, in document order. */
	static Map<String, String> results(WordprocessingMLPackage pkg) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) {
			Matcher t = Pattern.compile(">([A-Z]\\d\\d_[A-Z0-9_]+) = <").matcher(m.group());
			if (t.find()) out.put(t.group(1), outerResult(m.group()));
		}
		return out;
	}

	/** The text between the outermost field's separate and end. */
	static String outerResult(String xml) {
		Matcher m = Pattern.compile("fldCharType=\"(begin|separate|end)\"|<w:t(?: [^>]*)?>([^<]*)</w:t>").matcher(xml);
		int depth = 0;
		boolean inResult = false;
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			String kind = m.group(1);
			if ("begin".equals(kind)) depth++;
			else if ("separate".equals(kind)) { if (depth == 1) inResult = true; }
			else if ("end".equals(kind)) { if (depth == 1) inResult = false; depth--; }
			else if (inResult && depth == 1) sb.append(m.group(2));
		}
		return sb.toString();
	}

	/** The table's cells' texts (a formula cell's result), row by row. */
	static List<List<String>> table(WordprocessingMLPackage pkg) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher tbl = Pattern.compile("<w:tbl>.*?</w:tbl>", Pattern.DOTALL).matcher(xml);
		assertTrue(tbl.find());
		List<List<String>> rows = new ArrayList<List<String>>();
		Matcher tr = Pattern.compile("<w:tr[ >].*?</w:tr>", Pattern.DOTALL).matcher(tbl.group());
		while (tr.find()) {
			List<String> cells = new ArrayList<String>();
			Matcher tc = Pattern.compile("<w:tc>.*?</w:tc>", Pattern.DOTALL).matcher(tr.group());
			while (tc.find()) {
				String c = tc.group();
				if (c.contains("fldChar")) {
					cells.add(outerResult(c));
				} else {
					StringBuilder sb = new StringBuilder();
					Matcher t = Pattern.compile("<w:t(?: [^>]*)?>([^<]*)</w:t>").matcher(c);
					while (t.find()) sb.append(t.group(1));
					cells.add(sb.toString());
				}
			}
			rows.add(cells);
		}
		return rows;
	}

	@Test
	public void fieldUpdaterGivesWordsResults() throws Exception {
		Map<String, String> word = results(load("expr-probe-r1-word365.docx"));
		assertEquals(115, word.size());
		WordprocessingMLPackage pkg = load("expr-probe-r1.docx");
		FieldUpdater updater = new FieldUpdater(pkg);
		updater.getContext().language("en-US");
		updater.update(false);
		Map<String, String> ours = results(pkg);
		int compared = 0;
		for (Map.Entry<String, String> e : word.entrySet()) {
			String token = e.getKey(), expected = e.getValue(), actual = ours.get(token);
			if (token.startsWith("F07_")) {
				assertEquals(token + " (Word: 1.4, unexplained; section 9)", "1.41", actual);
			} else if (token.startsWith("F20_")) {
				assertTrue(token + ": " + expected, expected.startsWith("!Undefined Bookmark, NOPE"));
				assertEquals(token, "!Undefined Bookmark, NOPE", actual);
			} else {
				assertEquals(token, expected, actual);
			}
			compared++;
		}
		assertEquals(115, compared);
		// the table: every formula cell as Word computed it (text and empty cells skipped; formulas over formula results)
		assertEquals(table(load("expr-probe-r1-word365.docx")), table(pkg));
		// SET's value is stored under a bookmark of its name, as Word stores it
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		assertTrue(xml.contains("w:name=\"total\""));
		assertTrue(xml.contains("w:name=\"greeting\""));
		assertTrue(xml.contains("w:name=\"n\""));
	}

	/** The text model computes the same for every paragraph case (no cells in the walk: the table is not compared). */
	@Test
	public void textExtractionGivesWordsResults() throws Exception {
		String word = PlainText.of(Texts.of(load("expr-probe-r1-word365.docx"), new TextOptions()));
		String ours = PlainText.of(Texts.of(load("expr-probe-r1.docx"), new TextOptions().fields(TextOptions.Fields.UPDATE)));
		Map<String, String> wordLines = tokenLines(word), ourLines = tokenLines(ours);
		assertEquals(115, wordLines.size());
		for (Map.Entry<String, String> e : wordLines.entrySet()) {
			String token = e.getKey();
			if (token.startsWith("F07_")) assertEquals(token, "1.41", ourLines.get(token));
			else if (token.startsWith("F20_")) assertEquals(token, "!Undefined Bookmark, NOPE", ourLines.get(token));
			else assertEquals(token, e.getValue(), ourLines.get(token));
		}
		// a SET's stored value is never shown, as Word shows nothing for it: Word's file reads blank there too
		assertEquals("", wordLines.get("S01_SET"));
	}

	static Map<String, String> tokenLines(String text) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		for (String line : text.split("\n")) {
			Matcher m = Pattern.compile("^([A-Z]\\d\\d_[A-Z0-9_]+) = (.*)$").matcher(line);
			if (m.find()) out.put(m.group(1), m.group(2).replace("齯", "").replace("î", ""));
		}
		return out;
	}
}
