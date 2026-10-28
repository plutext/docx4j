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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The evaluators by field name (CR-067 D1), and the one way to evaluate a field through them: the
 * evaluator's result with the formatting switches applied, or null where the field keeps its
 * cached result (D5). A caller may {@link #register} an evaluator of its own, for a field docx4j
 * does not compute or to replace one it does.
 *
 * <p>Registered here: DOCPROPERTY and DOCVARIABLE; the document-information fields AUTHOR,
 * COMMENTS, KEYWORDS, LASTSAVEDBY, SUBJECT, TITLE, REVNUM, CREATEDATE and SAVEDATE (one given a
 * text argument shows that text, as Word sets the property from it); SEQ (counted in the
 * context's pass; {@code \r}, {@code \c}, {@code \h} honoured, {@code \s} not); REF to a bookmark
 * the context has read (its text; with {@code \n}, {@code \r}, {@code \w}, {@code \p} or
 * {@code \f}, which need numbering or layout, the cached result is kept); QUOTE; DATE, TIME and
 * PRINTDATE at the context's clock; CREATEDATE and SAVEDATE from the core properties; INFO as the field it
 * names; SECTION where the context says which; NOTEREF to a bookmark on a note reference; USERNAME,
 * USERINITIALS and USERADDRESS from the context (phase 1). Not here: MERGEFIELD (a merge supplies the data), FORMTEXT
 * (its result is in its form-field data), the layout fields (PAGE, NUMPAGES, SECTIONPAGES,
 * PAGEREF, STYLEREF, TOC: the renderer's), and the external fields (D8).</p>
 *
 * @since 17.3.5
 */
public final class FieldEvaluators {

	private static final Logger log = LoggerFactory.getLogger(FieldEvaluators.class);

	private static final Map<String, FieldEvaluator> EVALUATORS = new ConcurrentHashMap<String, FieldEvaluator>();

	/** The document-information fields and the property each shows, as DOCPROPERTY names it. */
	static final Map<String, String> DOC_INFO;
	static {
		Map<String, String> m = new HashMap<String, String>();
		m.put("AUTHOR", "Author");
		m.put("COMMENTS", "Comments");
		m.put("KEYWORDS", "Keywords");
		m.put("LASTSAVEDBY", "LastSavedBy");
		m.put("SUBJECT", "Subject");
		m.put("TITLE", "Title");
		m.put("REVNUM", "RevisionNumber");
		m.put("CREATEDATE", "CreateTime");
		m.put("SAVEDATE", "LastSavedTime");
		DOC_INFO = Collections.unmodifiableMap(m);
	}

	private static final Set<String> REF_UNSUPPORTED = new HashSet<String>(Arrays.asList("\\n", "\\r", "\\w", "\\p", "\\f"));

	static {
		register("DOCPROPERTY", (instr, ctx) -> {
			String key = instr.getArgument();
			return key == null ? null : ctx.properties().getValue(key);
		});
		register("DOCVARIABLE", (instr, ctx) -> {
			String key = instr.getArgument();
			return key == null ? null : ctx.variables().getValue(key);
		});
		for (final String name : DOC_INFO.keySet()) {
			register(name, (instr, ctx) -> {
				String argument = instr.getArgument();
				if (argument != null) return argument; // AUTHOR "Jo": Word sets the property from it, and shows it
				return ctx.properties().getValue(DOC_INFO.get(name));
			});
		}
		register("SEQ", FieldEvaluators::sequence);
		register("REF", (instr, ctx) -> {
			String bookmark = instr.getArgument();
			if (bookmark == null) return null;
			for (String p : instr.getParameters()) {
				if (REF_UNSUPPORTED.contains(p.toLowerCase(Locale.ROOT))) return null;
			}
			return ctx.bookmarks().get(bookmark.toLowerCase(Locale.ROOT));
		});
		register("QUOTE", (instr, ctx) -> instr.getArgument());
		FieldEvaluator date = new FieldEvaluator() {
			@Override
			public String evaluate(FieldInstruction instruction, FieldContext context) {
				return FormattingSwitchHelper.formatDate(instruction, context.now(), context.getLanguage());
			}
			@Override
			public boolean formatsItself() {
				return true;
			}
		};
		register("DATE", date);
		register("TIME", date);
		register("PRINTDATE", date); // the conversion is the printing
		// tier 1 (CR-067 phase 1)
		register("CREATEDATE", new CoreDate("CreateTime"));
		register("SAVEDATE", new CoreDate("LastSavedTime"));
		register("INFO", (instr, ctx) -> {
			// INFO <name> [switches]: the named document-information field itself
			String text = instr.getParameterText();
			if (instr.getArgument() == null || text == null) return null;
			FieldInstruction named = FieldInstruction.parse(text);
			if (named.getName() == null || "INFO".equals(named.getName()) || !has(named.getName())) return null;
			return evaluate(named, ctx);
		});
		register("SECTION", (instr, ctx) -> ctx.getSection() == null ? null : String.valueOf(ctx.getSection()));
		register("NOTEREF", (instr, ctx) -> {
			String bookmark = instr.getArgument();
			return bookmark == null ? null : ctx.noteNumbers().get(bookmark.toLowerCase(Locale.ROOT));
		});
		register("USERNAME", (instr, ctx) -> ctx.getUserName());
		register("USERINITIALS", (instr, ctx) -> ctx.getUserInitials());
		register("USERADDRESS", (instr, ctx) -> ctx.getUserAddress());
	}

	/**
	 * CREATEDATE and SAVEDATE: the core property's W3CDTF value as a date, formatted by the field's {@code \@}
	 * switch, or without one as the date fields are (phase 1: the default Word shows is the probe's to measure).
	 */
	private static final class CoreDate implements FieldEvaluator {
		private final String property;
		CoreDate(String property) {
			this.property = property;
		}
		@Override
		public String evaluate(FieldInstruction instruction, FieldContext context) throws Exception {
			String value = context.properties().getValue(property);
			java.util.Date date = FieldSources.parseW3CDTF(value);
			if (date == null) return null;
			return FormattingSwitchHelper.formatDate(instruction, date, context.getLanguage());
		}
		@Override
		public boolean formatsItself() {
			return true;
		}
	}

	private FieldEvaluators() {
	}

	/** Registers (or replaces) the evaluator for the field name (case does not matter). */
	public static void register(String fieldName, FieldEvaluator evaluator) {
		if (fieldName == null || evaluator == null) throw new IllegalArgumentException("a name and an evaluator");
		EVALUATORS.put(fieldName.toUpperCase(Locale.ROOT), evaluator);
	}

	/** The evaluator for the field name, or null. */
	public static FieldEvaluator get(String fieldName) {
		return fieldName == null ? null : EVALUATORS.get(fieldName.toUpperCase(Locale.ROOT));
	}

	/** Whether a field of that name is evaluated here. */
	public static boolean has(String fieldName) {
		return get(fieldName) != null;
	}

	/** The names evaluated here, upper case. */
	public static Set<String> names() {
		return Collections.unmodifiableSet(EVALUATORS.keySet());
	}

	/**
	 * The field's result as it is to be shown, or null where the field keeps its cached result: no
	 * evaluator for its name, or the evaluator could not compute it.
	 *
	 * @throws Docx4JException from the formatting switches (a {@code \#} on a bookmark, say), as
	 *         {@link FormattingSwitchHelper} throws it
	 */
	public static String evaluate(FieldInstruction instruction, FieldContext context) throws Docx4JException {
		if (instruction == null || context == null) return null;
		FieldEvaluator evaluator = get(instruction.getName());
		if (evaluator == null) return null;
		String value;
		try {
			value = evaluator.evaluate(instruction, context);
		} catch (FieldValueException e) {
			log.debug("No value for the field " + instruction.getText() + ": " + e.getMessage());
			return null;
		} catch (Docx4JException e) {
			throw e;
		} catch (Exception e) {
			log.debug("No value for the field " + instruction.getText() + ": " + e, e);
			return null;
		}
		if (value == null || evaluator.formatsItself()) return value;
		return FormattingSwitchHelper.applyFormattingSwitch(context.getPackage(), instruction, value, context.getLanguage());
	}

	/** {@link #evaluate(FieldInstruction, FieldContext)} of the instruction text. */
	public static String evaluate(String instruction, FieldContext context) throws Docx4JException {
		return evaluate(FieldInstruction.parse(instruction), context);
	}

	private static String sequence(FieldInstruction instr, FieldContext ctx) {
		String identifier = instr.getArgument();
		if (identifier == null) return null;
		String key = identifier.toUpperCase(Locale.ROOT);
		Integer current = ctx.sequences().get(key);
		int n = current == null ? 0 : current;
		List<String> params = instr.getParameters();
		String reset = instr.hasSwitch("\\r") ? instr.getSwitchValue("\\r") : null;
		if (reset != null && reset.length() > 0) {
			try {
				n = Integer.parseInt(reset.trim());
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (!params.contains("\\c")) {
			n++;
		}
		ctx.sequences().put(key, n);
		if (params.contains("\\h")) return "";
		return String.valueOf(n);
	}
}
