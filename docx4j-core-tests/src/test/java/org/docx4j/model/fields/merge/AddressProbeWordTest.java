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
import org.docx4j.model.fields.FieldContext;
import org.docx4j.model.fields.FieldInstruction;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.toc.TocHelper;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.P;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-067 phase 3 (section 14.2): ADDRESSBLOCK and GREETINGLINE merged by {@link MailMerger} over the four records of
 * {@code address-data-r1.docx} against Word 365's merge ({@code address-probe-r1-merged.docx}). Word's run had no
 * Postal Code column matched (its Match Fields dialog), and its MERGEFIELD "First Name" was replaced by Title (Word
 * names the column First_Name); the records here mirror that. One measured deviation is asserted as docx4j's: G03 (Word
 * fell back to {@code \e} for every record; docx4j fills the tokens). A hand-written {@code \n} inside a token prints the
 * letter n, as Word printed it; Word's own format ({@code address-probe-r1-attached.docx}, round 2, section 14.4) breaks
 * its lines with real line breaks, w:cr in the instruction.
 */
public class AddressProbeWordTest {

	static WordprocessingMLPackage load(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-phase3/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	static final String[] COLUMNS = { "Title", "First Name", "Last Name", "Suffix", "Company", "Address 1", "Address 2", "City", "State", "Postal Code", "Country" };
	static final String[][] ROWS = {
			{ "Mr", "John", "Smith", "Jr", "Acme Pty Ltd", "1 Main St", "Suite 2", "Sydney", "NSW", "", "Australia" },
			{ "Ms", "Jane", "Doe", "", "", "2 High St", "", "Auckland", "", "", "New Zealand" },
			{ "", "Pat", "Lee", "", "Beta Inc", "3 Low Rd", "", "Perth", "WA", "", "Australia" },
			{ "", "Sam", "", "", "", "4 Side Ln", "", "Hobart", "TAS", "", "Australia" },
	};

	/** The four records as Word merged them (no postal code: the column was not matched). */
	static List<Map<DataFieldName, String>> records() {
		List<Map<DataFieldName, String>> data = new ArrayList<Map<DataFieldName, String>>();
		for (String[] r : ROWS) {
			Map<DataFieldName, String> m = new HashMap<DataFieldName, String>();
			for (int i = 0; i < COLUMNS.length; i++) m.put(new DataFieldName(COLUMNS[i]), r[i]);
			data.add(m);
		}
		return data;
	}

	/** The letters: each case's shown text by token; a multi-line block is its paragraphs (Word) or its line breaks (docx4j) joined by \n. */
	static List<Map<String, String>> letters(WordprocessingMLPackage pkg) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		List<Map<String, String>> out = new ArrayList<Map<String, String>>();
		Map<String, String> letter = new LinkedHashMap<String, String>();
		String token = null;
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) {
			String p = m.group();
			String shown = MergeProbeWordTest.shownText(p.replace("<w:br/>", "<w:t>\n</w:t>"));
			Matcher t = Pattern.compile("^([AGM]\\d\\d_[A-Z0-9_]+) = (.*)$", Pattern.DOTALL).matcher(shown);
			if (t.find()) {
				token = t.group(1);
				letter.put(token, t.group(2));
			} else if (token != null && !shown.isEmpty() && !shown.startsWith("End of") && !p.contains("w:pStyle w:val=\"Title\"")) {
				letter.put(token, letter.get(token) + "\n" + shown);
			}
			if (p.contains("<w:sectPr")) {
				out.add(letter);
				letter = new LinkedHashMap<String, String>();
				token = null;
			}
		}
		if (!letter.isEmpty()) out.add(letter);
		return out;
	}

	@Test
	public void addressFieldsAsWord() throws Exception {
		List<Map<String, String>> word = letters(load("address-probe-r1-merged.docx"));
		assertEquals(4, word.size());
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
		WordprocessingMLPackage merged = new MailMerger(load("address-probe-r1.docx")).getConsolidatedResultCrude(records(), false);
		List<Map<String, String>> ours = letters(merged);
		assertEquals(4, ours.size());
		for (int i = 0; i < 4; i++) {
			Map<String, String> w = word.get(i), o = ours.get(i);
			assertEquals("letter " + (i + 1), w.keySet(), o.keySet());
			for (String token : w.keySet()) {
				if (token.equals("M01_FIRST_NAME")) continue; // Word's run merged Title there (Jason's replacement)
				assertEquals("letter " + (i + 1) + " " + token, expectedOf(token, i, w.get(token)), o.get(token));
			}
		}
		// the fallbacks of a greeting (measured): title and last name; first and last without a title; the first name alone
		assertEquals("Dear Mr Smith,", ours.get(0).get("G01_GREETING"));
		assertEquals("Dear Pat Lee,", ours.get(2).get("G01_GREETING"));
		assertEquals("Dear Sam,", ours.get(3).get("G01_GREETING"));
		assertEquals("", ours.get(0).get("G04_NO_SWITCHES"));
	}

	/** The lines after "End of the letter." in each letter: Word's own Address Block and Greeting Line as rendered. */
	static List<List<String>> tails(WordprocessingMLPackage pkg) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		List<List<String>> out = new ArrayList<List<String>>();
		List<String> tail = null;
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) {
			String p = m.group();
			String shown = MergeProbeWordTest.shownText(p.replace("<w:br/>", "<w:t>\n</w:t>"));
			if (shown.startsWith("End of the letter")) {
				tail = new ArrayList<String>();
			} else if (tail != null && !shown.isEmpty()) {
				for (String line : shown.split("\n")) if (!line.isEmpty()) tail.add(line);
			}
			if (p.contains("<w:sectPr") && tail != null) {
				out.add(tail);
				tail = null;
			}
		}
		if (tail != null) out.add(tail);
		return out;
	}

	/**
	 * Round 3 (Jason's merge of the attached file, 2026-10-29): Word's own Address Block and Greeting Line merged by
	 * docx4j over the four records give Word's lines (the block as line breaks in one paragraph where Word writes
	 * paragraphs; the greeting, a w:fldSimple in Word's file, on one line).
	 */
	@Test
	public void wordsOwnFieldsMergedAsWord() throws Exception {
		List<List<String>> word = tails(load("address-probe-r1-attached-merged.docx"));
		assertEquals(4, word.size());
		assertEquals(java.util.Arrays.asList("Acme Pty Ltd", "Mr John Smith Jr", "1 Main St", "Suite 2", "Sydney NSW", "Dear Mr Smith,"), word.get(0));
		assertEquals(java.util.Arrays.asList("Ms Jane Doe", "2 High St", "Auckland", "New Zealand", "Dear Ms Doe,"), word.get(1));
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
		WordprocessingMLPackage merged = new MailMerger(load("address-probe-r1-attached.docx")).getConsolidatedResultCrude(records(), false);
		assertEquals(word, tails(merged));
	}

	/**
	 * Word's own Address Block and Greeting Line (inserted by Word 365 over the attached list, round 2): the block's format
	 * breaks its lines with w:cr inside the instruction runs, read as line breaks; the greeting's (a w:fldSimple) carries a
	 * line break before its {@code _AFTER_} token, which prints on one line.
	 */
	@Test
	public void wordsOwnFormats() throws Exception {
		WordprocessingMLPackage attached = load("address-probe-r1-attached.docx");
		Map<String, String> record = new HashMap<String, String>();
		for (int i = 0; i < COLUMNS.length; i++) record.put(COLUMNS[i], ROWS[0][i]);
		FieldContext ctx = FieldContext.of(attached).mergeRecord(record);
		String block = null, greeting = null;
		for (Object o : attached.getMainDocumentPart().getContent()) {
			if (!(o instanceof P)) continue;
			List<FieldRef> refs = new ArrayList<FieldRef>();
			P p = FieldsPreprocessor.canonicalise((P) o, refs);
			for (FieldRef fr : refs) if (fr.getInstr() != null && fr.getInstr().contains("\\l 3081")) block = fr.getInstr();
			for (Object f : TocHelper.getAllElementsFromObject(p, CTSimpleField.class)) {
				String instr = ((CTSimpleField) f).getInstr();
				if (instr != null && instr.contains("GREETINGLINE")) greeting = instr;
			}
		}
		assertTrue(block, block.contains("<<_COMPANY_\n>>") && block.contains("<<\n_COUNTRY_>>") && !block.contains("\\n"));
		assertEquals("Acme Pty Ltd\nMr John Smith Jr\n1 Main St\nSuite 2\nSydney NSW",
				AddressFields.addressBlock(FieldInstruction.parse(block), ctx)); // \c 2 \e Australia: the country left off
		assertTrue(greeting, greeting.contains("<< _LAST0_>>\n<<_AFTER_ ,>>"));
		assertEquals("Dear Mr Smith,", AddressFields.greetingLine(FieldInstruction.parse(greeting), ctx));
		// a hand-written \n: the letter n, inside a token and outside one (measured, section 14.2)
		assertEquals("Acme Pty LtdnnJohn", AddressFields.fill("<<_COMPANY_\\n>>\\n<<_FIRST0_>>", AddressFields.valuesOf(record)));
	}

	/** Word's text, or docx4j's where the two differ by a recorded deviation. */
	static String expectedOf(String token, int letter, String word) {
		if (token.equals("G03_FULL")) {
			// Word: "To whom it may concern:" for every record (unexplained, section 9); docx4j fills the tokens
			String[] ours = { "To Mr John Smith:", "To Ms Jane Doe:", "To Pat Lee:", "To Sam:" };
			return ours[letter];
		}
		if (token.equals("A01_ADDRESSBLOCK") && letter == 1) {
			// \d: Word re-laid the one foreign address (New Zealand, not the \e country) in that country's own format, name,
			// street, city, country on four lines (section 14.4); docx4j ignores \d (the country formats are unmeasured)
			return "Ms Jane Doen2 High StnAucklandnNew Zealand";
		}
		if (token.startsWith("A01") || token.startsWith("A02") || token.startsWith("A03")) {
			// Word broke the paragraph after the name line, before the letter n of the first hand-written \n, in the cases with
			// \c and \e and not in A04 (unexplained, section 9); docx4j prints the n alone
			return word.replace("\nn", "n");
		}
		return word;
	}

	@Test
	public void columnsByWordsNames() throws Exception {
		Map<String, String> record = new HashMap<String, String>();
		record.put("First_Name", "Ann");
		record.put("Last_Name", "Lee");
		record.put("ZIP", "2000");
		record.put("Country or Region", "Australia");
		Map<String, String> values = AddressFields.valuesOf(record);
		assertEquals("Ann", values.get("_FIRST0_"));
		assertEquals("Lee", values.get("_LAST0_"));
		assertEquals("2000", values.get("_POSTAL_"));
		assertEquals("Australia", values.get("_COUNTRY_"));
	}
}
