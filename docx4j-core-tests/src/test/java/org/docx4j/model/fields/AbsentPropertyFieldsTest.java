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

 */package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A property field with no value (CR-067 section 16). The text model under {@code fields: update} reads it as Word does, the
 * part gone or holding nothing alike; an updater and the output path keep the cached result (D5). The files are
 * {@code fields/cr067-absent-properties/}, each field with a stale result of its own, and Word's own text saves of them are the oracle.
 */
public class AbsentPropertyFieldsTest {

	private static final String[] FILES = { "fp-00-control", "fp-01-no-core", "fp-02-no-app", "fp-03-no-custom", "fp-04-no-properties", "fp-05-properties-missing" };

	/** What docx4j computes as Word does, in every file. The rest are in {@link #wordsOwnKeepTheCachedResult()} and the CR. */
	private static final String[] AS_WORD = { "TITLE", "SUBJECT", "KEYWORDS", "COMMENTS", "LASTSAVEDBY", "REVNUM", "INFO_TITLE",
			"DOCPROPERTY_TITLE", "DOCPROPERTY_SUBJECT", "DOCPROPERTY_AUTHOR", "DOCPROPERTY_KEYWORDS", "DOCPROPERTY_COMMENTS", "DOCPROPERTY_LASTSAVEDBY",
			"DOCPROPERTY_REVISIONNUMBER", "DOCPROPERTY_COMPANY", "DOCPROPERTY_MANAGER", "DOCPROPERTY_HYPERLINKBASE", "DOCPROPERTY_TOTALEDITINGTIME",
			"DOCPROPERTY_CUSTOM1", "DOCPROPERTY_NOSUCHNAME" };

	private static File file(String name) throws Exception {
		return new File(AbsentPropertyFieldsTest.class.getClassLoader().getResource("fields/cr067-absent-properties/" + name).toURI());
	}

	private static WordprocessingMLPackage load(String name) throws Exception {
		return WordprocessingMLPackage.load(file(name + ".docx"));
	}

	private static Map<String, String> lines(String text) {
		Map<String, String> lines = new HashMap<String, String>();
		for (String line : text.split("\r?\n")) {
			int at = line.indexOf(" =");
			if (at > 0) lines.put(line.substring(0, at), line.substring(at + 2).trim());
		}
		return lines;
	}

	/** Each line's field and its result, as a text reading gives them. */
	private static Map<String, String> read(WordprocessingMLPackage pkg, TextOptions.Fields fields) {
		return lines(Texts.plainText(pkg, new TextOptions().fields(fields)));
	}

	private static Map<String, String> updated(String name) throws Exception {
		return read(load(name), TextOptions.Fields.UPDATE);
	}

	private static Map<String, String> word(String name) throws Exception {
		return lines(new String(java.nio.file.Files.readAllBytes(file(name + "-word365.txt").toPath()), "UTF-8"));
	}

	@Test
	public void readsAsWordsOwnTextSaves() throws Exception {
		for (String name : FILES) {
			Map<String, String> word = word(name), docx4j = updated(name);
			for (String field : AS_WORD) assertEquals(name + " " + field, word.get(field), docx4j.get(field));
		}
	}

	/** Absent, these two are empty; present, Word writes a date in its locale, which docx4j doesn't. */
	@Test
	public void aDatePropertyWithNoValueIsEmpty() throws Exception {
		for (String name : new String[] { "fp-01-no-core", "fp-04-no-properties", "fp-05-properties-missing" }) {
			Map<String, String> word = word(name), docx4j = updated(name);
			for (String field : new String[] { "DOCPROPERTY_LASTSAVEDTIME", "DOCPROPERTY_LASTPRINTED" }) {
				assertEquals("", word.get(field));
				assertEquals(name + " " + field, "", docx4j.get(field));
			}
		}
	}

	/** The field form and DOCPROPERTY of one property differ, and the part being there with nothing in it is the same as its going. */
	@Test
	public void theTwoFormsDifferAndThePartMakesNoDifference() throws Exception {
		for (String name : new String[] { "fp-01-no-core", "fp-04-no-properties", "fp-05-properties-missing" }) {
			Map<String, String> t = updated(name);
			assertEquals("", t.get("TITLE"));
			assertEquals("Error! Unknown document property name.", t.get("DOCPROPERTY_TITLE"));
		}
	}

	/** Word's user, its clock and its own values: an extractor has none of them, so the cached result stands (WD REQ-506). */
	@Test
	public void wordsOwnKeepTheCachedResult() throws Exception {
		Map<String, String> t = updated("fp-04-no-properties");
		for (String field : new String[] { "AUTHOR", "CREATEDATE", "SAVEDATE", "PRINTDATE", "DOCPROPERTY_CREATETIME", "DOCPROPERTY_TEMPLATE",
				"DOCPROPERTY_NAMEOFAPPLICATION", "DOCPROPERTY_PAGES", "DOCPROPERTY_WORDS", "DOCPROPERTY_CHARACTERS", "DOCPROPERTY_CHARACTERSWITHSPACES",
				"DOCPROPERTY_LINES", "DOCPROPERTY_PARAGRAPHS", "DOCPROPERTY_SECURITY" }) {
			assertEquals(field, "STALE_" + field, t.get(field));
		}
	}

	/** D5: nothing here may reach a caller's document or a rendering. */
	@Test
	public void anUpdaterKeepsTheCachedResult() throws Exception {
		WordprocessingMLPackage pkg = load("fp-04-no-properties");
		new FieldUpdater(pkg).update(true);
		Map<String, String> t = read(pkg, TextOptions.Fields.CACHED);
		assertEquals("STALE_TITLE", t.get("TITLE"));
		assertEquals("STALE_DOCPROPERTY_COMPANY", t.get("DOCPROPERTY_COMPANY"));
		assertEquals("STALE_DOCPROPERTY_CUSTOM1", t.get("DOCPROPERTY_CUSTOM1"));
	}

	/** The output path and a merge come through this: null keeps the cached result. */
	@Test
	public void theRegistrysAnswerIsNull() throws Exception {
		FieldContext context = FieldContext.of(load("fp-04-no-properties"));
		assertNull(FieldEvaluators.evaluate("TITLE", context));
		assertNull(FieldEvaluators.evaluate("INFO Title", context));
		assertNull(FieldEvaluators.evaluate("DOCPROPERTY Company", context));
		assertNull(FieldEvaluators.evaluate("DOCPROPERTY Custom1", context));
	}
}
