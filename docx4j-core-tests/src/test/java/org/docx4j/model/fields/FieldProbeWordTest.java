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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-067 phase 1 against Word: the probe {@code fields/cr067-phase1/field-probe-r1.docx} updated by FieldUpdater
 * gives, case for case, what Word 365 (version 2608, build 20326.20232, 2026-10-28) gave on F9, as
 * {@code results.txt} records it and {@code field-probe-r1-word365.docx} holds it; the error-text cases excepted,
 * where D5 keeps the cached result. Word's VM is in Sydney's zone; the dates are compared there.
 */
public class FieldProbeWordTest {

	/** Word's results, by token: parsed from results.txt so the file stays the record. */
	static Map<String, String> wordResults() throws Exception {
		Map<String, String> out = new LinkedHashMap<String, String>();
		try (InputStream is = ResourceUtils.getResource("fields/cr067-phase1/results.txt")) {
			String text = new String(is.readAllBytes(), "UTF-8");
			int from = text.indexOf("Results:");
			Matcher m = Pattern.compile("^(P\\d\\d_[A-Z0-9_]+) = (.*)$", Pattern.MULTILINE).matcher(text.substring(from));
			while (m.find()) out.put(m.group(1), m.group(2).trim());
		}
		return out;
	}

	private static String paragraph(WordprocessingMLPackage pkg, String token) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + token + " = <")) return m.group();
		throw new AssertionError(token + " not in the document");
	}

	/** The field's result text: what is between the separate and the end (or the fldSimple's content). */
	private static String resultText(String paragraph) {
		int i = paragraph.indexOf("fldCharType=\"separate\"");
		int j = paragraph.indexOf("fldCharType=\"end\"");
		String seg = i >= 0 ? paragraph.substring(i, j) : paragraph.substring(paragraph.indexOf("<w:fldSimple"));
		StringBuilder sb = new StringBuilder();
		Matcher m = Pattern.compile("<w:t(?: [^>]*)?>([^<]*)</w:t>").matcher(seg);
		while (m.find()) sb.append(m.group(1));
		return sb.toString();
	}

	@Test
	public void fieldUpdaterGivesWordsResults() throws Exception {
		Map<String, String> word = wordResults();
		assertEquals(25, word.size());
		TimeZone zone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("Australia/Sydney"));
		try {
			WordprocessingMLPackage pkg;
			try (InputStream is = ResourceUtils.getResource("fields/cr067-phase1/field-probe-r1.docx")) {
				pkg = WordprocessingMLPackage.load(is);
			}
			FieldUpdater updater = new FieldUpdater(pkg);
			updater.getContext().user("Author A", "AA", null).language("en-US");
			updater.update(false);
			int compared = 0;
			for (Map.Entry<String, String> e : word.entrySet()) {
				String token = e.getKey();
				String expected = e.getValue();
				if (expected.startsWith("Error!")) {
					// D5: the cached result is kept where Word writes its error text (INFO, REF nope, NOTEREF nope)
					assertEquals(token, "OLD", resultText(paragraph(pkg, token)));
					continue;
				}
				assertEquals(token, expected, resultText(paragraph(pkg, token)));
				compared++;
			}
			assertEquals(21, compared);
			// the formatting Word keeps: a REF's result in the bookmark's bold, a NOTEREF \f in the reference style
			assertTrue(paragraph(pkg, "P13_REF").contains("<w:b/>"));
			assertTrue(paragraph(pkg, "P18_NOTEREF_F").contains("FootnoteReference"));
		} finally {
			TimeZone.setDefault(zone);
		}
	}

	/** The evaluators alone, with Word's texts: the two dates in Word's default format, read to the minute. */
	@Test
	public void theDatesInWordsDefaultFormat() throws Exception {
		TimeZone zone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("Australia/Sydney"));
		try {
			WordprocessingMLPackage pkg;
			try (InputStream is = ResourceUtils.getResource("fields/cr067-phase1/field-probe-r1.docx")) {
				pkg = WordprocessingMLPackage.load(is);
			}
			FieldContext ctx = FieldContext.of(pkg).language("en-US");
			assertEquals("2/3/2024 3:05:00 PM", FieldEvaluators.evaluate(" CREATEDATE ", ctx));
			assertEquals("6/7/2025 6:09:00 PM", FieldEvaluators.evaluate(" SAVEDATE ", ctx));
			assertEquals("2024-02-03 15:05", FieldEvaluators.evaluate(" CREATEDATE \\@ \"yyyy-MM-dd HH:mm\" ", ctx));
			assertEquals("", FieldEvaluators.evaluate(" QUOTE ", ctx));
			assertEquals("", FieldEvaluators.evaluate(" USERADDRESS ", ctx.user("Author A", "AA", null)));
		} finally {
			TimeZone.setDefault(zone);
		}
	}
}
