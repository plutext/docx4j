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
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.model.revisions.TrackedChange;
import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-067 §12.2 against Word: the date-forms probe {@code fields/cr067-dates/date-forms-r1.docx} (a date alone, a time
 * with no offset, a UTC time; each in a core property, a revision and a date control), as Word 365 (version 2608,
 * build 20326.20232, 2026-10-28, in Sydney's zone) read it on F9 and in its balloons, per {@code results.txt}, and as
 * its save ({@code date-forms-r1-word365.docx}) rewrote the forms. A core property's bare value is UTC (D13); a
 * revision's is the local clock; PRINTDATE on F9 is cp:lastPrinted (D14).
 */
public class DateFormsProbeWordTest {

	static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

	/** Word's field results, by token: parsed from results.txt so the file stays the record. */
	static Map<String, String> wordResults() throws Exception {
		Map<String, String> out = new LinkedHashMap<String, String>();
		try (InputStream is = ResourceUtils.getResource("fields/cr067-dates/results.txt")) {
			String text = new String(is.readAllBytes(), "UTF-8");
			Matcher m = Pattern.compile("^(D\\d\\d_[A-Z_]+) = (.*)$", Pattern.MULTILINE).matcher(text);
			while (m.find()) out.put(m.group(1), m.group(2).trim());
		}
		return out;
	}

	static WordprocessingMLPackage load(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-dates/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	private static String paragraph(WordprocessingMLPackage pkg, String token) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + token + " = <")) return m.group();
		throw new AssertionError(token + " not in the document");
	}

	private static String resultText(String paragraph) {
		int i = paragraph.indexOf("fldCharType=\"separate\"");
		int j = paragraph.indexOf("fldCharType=\"end\"");
		StringBuilder sb = new StringBuilder();
		Matcher m = Pattern.compile("<w:t(?: [^>]*)?>([^<]*)</w:t>").matcher(paragraph.substring(i, j));
		while (m.find()) sb.append(m.group(1));
		return sb.toString();
	}

	private static List<String> attributeValues(WordprocessingMLPackage pkg, String attribute) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile(attribute + "=\"([^\"]*)\"").matcher(xml);
		List<String> out = new ArrayList<String>();
		while (m.find()) out.add(m.group(1));
		return out;
	}

	/** Part 1: CREATEDATE, SAVEDATE and PRINTDATE through FieldUpdater, in the package's zone (D12), give Word's six texts. */
	@Test
	public void thePropertyFieldsGiveWordsResults() throws Exception {
		Map<String, String> word = wordResults();
		assertEquals(6, word.size());
		WordprocessingMLPackage pkg = load("date-forms-r1.docx");
		pkg.setDateTimeZone(SYDNEY);
		FieldUpdater updater = new FieldUpdater(pkg);
		updater.getContext().language("en-US");
		updater.update(false);
		for (Map.Entry<String, String> e : word.entrySet()) {
			assertEquals(e.getKey(), e.getValue(), resultText(paragraph(pkg, e.getKey())));
		}
	}

	/** The readings behind Part 1: a bare core-property value is UTC (D13); PRINTDATE is cp:lastPrinted, else kept (D14). */
	@Test
	public void aBareCorePropertyValueIsUtcAndPrintdateIsLastPrinted() throws Exception {
		assertEquals(Instant.parse("2024-02-03T00:00:00Z").toEpochMilli(), FieldSources.parseW3CDTF("2024-02-03").getTime());
		assertEquals(Instant.parse("2025-06-07T08:09:10Z").toEpochMilli(), FieldSources.parseW3CDTF("2025-06-07T08:09:10").getTime());
		assertEquals(Instant.parse("2025-01-02T03:04:05Z").toEpochMilli(), FieldSources.parseW3CDTF("2025-01-02T03:04:05Z").getTime());
		WordprocessingMLPackage pkg = load("date-forms-r1.docx");
		pkg.setDateTimeZone(SYDNEY);
		FieldContext ctx = FieldContext.of(pkg).language("en-US");
		assertEquals("1/2/2025 2:04:00 PM", FieldEvaluators.evaluate(" PRINTDATE ", ctx));
		assertEquals("2025-01-02 14:04:00", FieldEvaluators.evaluate(" PRINTDATE \\@ \"yyyy-MM-dd HH:mm:ss\" ", ctx));
		assertEquals("2025-01-02 12:04:00", FieldEvaluators.evaluate(" PRINTDATE \\@ \"yyyy-MM-dd HH:mm:ss\" ", ctx.zone(ZoneId.of("Asia/Tokyo"))));
		assertEquals("2024-02-03 11:00:00", FieldEvaluators.evaluate(" CREATEDATE \\@ \"yyyy-MM-dd HH:mm:ss\" ", ctx.zone(null)));
		pkg.getDocPropsCorePart().getJaxbElement().setLastPrinted(null);
		assertNull("never printed: the cached result is kept", FieldEvaluators.evaluate(" PRINTDATE ", ctx));
	}

	/** Part 2: a revision's w:date is the local clock in every form; a date alone is midnight; the Z is ignored. */
	@Test
	public void theRevisionsAreWordsLocalClock() throws Exception {
		WordprocessingMLPackage pkg = load("date-forms-r1.docx");
		pkg.setDateTimeZone(SYDNEY);
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(3, changes.size());
		assertEquals("R01: 3/02/2024 12:00:00 AM in Sydney (UTC+11)", Instant.parse("2024-02-02T13:00:00Z"), changes.get(0).getDate());
		assertEquals("R02: 7/06/2025 8:09 AM in Sydney (UTC+10)", Instant.parse("2025-06-06T22:09:10Z"), changes.get(1).getDate());
		assertEquals("R03: 2/01/2025 3:04 AM in Sydney (UTC+11), the Z ignored", Instant.parse("2025-01-01T16:04:05Z"), changes.get(2).getDate());
	}

	/** Word's save: every w:date and w:fullDate rewritten as its nominal clock to the minute labelled Z; lastPrinted to the minute. */
	@Test
	public void wordsSaveWritesTheNominalClockToTheMinute() throws Exception {
		WordprocessingMLPackage saved = load("date-forms-r1-word365.docx");
		List<String> forms = Arrays.asList("2024-02-03T00:00:00Z", "2025-06-07T08:09:00Z", "2025-01-02T03:04:00Z");
		assertEquals(forms, attributeValues(saved, "w:date"));
		assertEquals(forms, attributeValues(saved, "w:fullDate"));
		assertEquals("2025-01-02T03:04:00Z", saved.getDocPropsCorePart().getJaxbElement().getLastPrinted().toXMLFormat());
	}
}
