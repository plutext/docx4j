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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.docx4j.model.fields.FieldContext;
import org.docx4j.model.fields.FieldEvaluator;
import org.docx4j.model.fields.FieldContext;
import org.docx4j.model.fields.FieldEvaluators;
import org.docx4j.model.fields.FieldInstruction;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Field results computed under {@code fields: update} (WD REQ-506), in the walk and so without a
 * copy of the package: only the fields that need neither layout nor external access, through
 * {@link FieldEvaluators} (CR-067) with this walk's {@link FieldContext}.
 *
 * <ul>
 * <li>DOCPROPERTY (core, application and custom properties) and DOCVARIABLE;</li>
 * <li>the core-property fields AUTHOR, TITLE, SUBJECT, KEYWORDS, COMMENTS, LASTSAVEDBY, REVNUM,
 * CREATEDATE and SAVEDATE (one given a text argument gives that text, as Word sets the property);</li>
 * <li>SEQ, counted in reading order ({@code \r}, {@code \c} and {@code \h} honoured; {@code \s} not);</li>
 * <li>REF to a bookmark, its text in the view (with {@code \n}, {@code \r}, {@code \w}, {@code \p}
 * or {@code \f}, which need numbering or layout, it keeps its cached result);</li>
 * <li>QUOTE, its argument;</li>
 * <li>since CR-067 phase 1: INFO as the field it names, SECTION (the main story's, counted at the walk), NOTEREF
 * (the number of the note whose reference the bookmark holds), USERNAME, USERINITIALS and USERADDRESS where the
 * caller's context supplies the user (never by default);</li>
 * <li>since CR-067 phase 2: {@code =} (a formula; one naming a table cell keeps its cached result, the walk supplying
 * no cells), COMPARE, IF, and SET (empty, as Word shows it; its value then reaches a REF, a formula or an IF after it).
 * A nested field's result is an operand of the outer's instruction (D4).</li>
 * </ul>
 *
 * Nothing else (FORMTEXT is the walk's, from its form field data): not MERGEFIELD (no data is supplied), DATE,
 * TIME, PRINTDATE, the layout fields or the external ones (nor an INFO naming one of them: {@code INFO Date} keeps its
 * cached result as DATE does), and never a field with {@code w:fldLock}.
 * A field whose value can't be found keeps its cached result.
 */
final class FieldValues {
	private static final Logger log = LoggerFactory.getLogger(FieldValues.class);

	/** The names computed here: the WD's set, which CR-067's phases widen through the WD. */
	private static final Set<String> COMPUTED;
	static {
		Set<String> s = new HashSet<String>(Arrays.asList("DOCPROPERTY", "DOCVARIABLE", "SEQ", "REF", "QUOTE",
				"AUTHOR", "TITLE", "SUBJECT", "COMMENTS", "LASTSAVEDBY", "REVNUM", "CREATEDATE", "SAVEDATE", "KEYWORDS",
				// CR-067 phase 1
				"INFO", "SECTION", "NOTEREF", "USERNAME", "USERINITIALS", "USERADDRESS",
				// CR-067 phase 2 (a formula naming a table cell keeps its cached result: no cells here)
				"=", "COMPARE", "IF", "SET",
				// CR-067 phase 3 (FILENAME and FILESIZE keep the cached result: the walk has no file)
				"NUMWORDS", "NUMCHARS", "TEMPLATE", "FILENAME", "FILESIZE"));
		COMPUTED = Collections.unmodifiableSet(s);
	}

	private final FieldContext context;
	/** Bookmarks' text by name (lower case): from a first reading, then as this one reads them. */
	final Map<String, String> bookmarks;

	FieldValues(WordprocessingMLPackage pkg) {
		this.context = pkg == null ? null : FieldContext.of(pkg);
		this.bookmarks = context == null ? new HashMap<String, String>() : context.bookmarks();
	}

	/** The field's computed result, or null to keep its cached one. */
	String compute(String instr) {
		return compute(instr, null);
	}

	/**
	 * The field an INFO names, upper case, or "" where it names none: {@code INFO Date} keeps its cached result here as
	 * DATE does, where the registry's INFO would evaluate it at the clock (python-64's reading, 2026-10-28, CR-067 §12.3).
	 */
	private static String infoName(FieldInstruction info) {
		String text = info.getParameterText();
		if (info.getArgument() == null || text == null) return "";
		String named = FieldInstruction.parse(text).getName();
		return named == null ? "" : named;
	}

	/** The same, for a field in the given section of the main story (from 1), or null where unknown. */
	String compute(String instr, Integer section) {
		if (context == null || instr == null) return null;
		context.section(section);
		FieldInstruction instruction = FieldInstruction.parse(instr);
		String name = instruction.getName();
		if (name == null || !COMPUTED.contains(name)) return null;
		if ("INFO".equals(name) && !COMPUTED.contains(infoName(instruction))) return null; // INFO X as X is here, not the registry's X
		FieldEvaluator evaluator = FieldEvaluators.get(name);
		if (evaluator == null) return null;
		String value;
		try {
			value = evaluator.evaluate(instruction, context);
		} catch (Exception e) {
			log.debug("No value for the field " + instr.trim() + ": " + e.getMessage());
			return null;
		}
		if ("SET".equals(name)) return value == null ? null : ""; // its value is in the context's bookmarks; Word shows nothing
		if (value == null || evaluator.formatsItself()) return value;
		try {
			return FormattingSwitchHelper.applyFormattingSwitch(context.getPackage(), instruction, value, null);
		} catch (Exception e) {
			log.debug("Couldn't apply the switches of " + instr.trim() + ": " + e.getMessage());
			return value;
		}
	}
}
