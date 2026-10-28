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
 * {@code \f}, which need numbering or layout, the cached result is kept); QUOTE; DATE and TIME at
 * the context's clock; CREATEDATE, SAVEDATE and PRINTDATE from the core properties; INFO as the field it
 * names; SECTION where the context says which; NOTEREF to a bookmark on a note reference; USERNAME,
 * USERINITIALS and USERADDRESS from the context (phase 1); {@code =} ({@link Formula}), COMPARE and IF
 * ({@link Conditional}) and SET (phase 2); MERGEFIELD, MERGEREC and MERGESEQ from the context's merge record and
 * numbers (the mergers set them; outside a merge the cached result is kept), ASK and FILLIN from the context's
 * answers, else their {@code \d} default (phase 2). Not here: FORMTEXT
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
		// CR-067 phase 3 (measured 2026-10-28, section 14.4): the list numbers, computed for the document in order (ListNumbers),
		// and SYMBOL's character; Word paints both live and writes no result for them
		for (String name : ListNumbers.NAMES) register(name, FieldEvaluators::listNumber);
		register("SYMBOL", new FieldEvaluator() {
			@Override
			public String evaluate(FieldInstruction instruction, FieldContext context) {
				return SymbolField.of(instruction).getText();
			}
			@Override
			public boolean formatsItself() {
				return true; // its switches are its own (f, s, u, a, h)
			}
		});
		register("REF", (instr, ctx) -> {
			String bookmark = instr.getArgument();
			if (bookmark == null) return null;
			for (String p : instr.getParameters()) {
				if (REF_UNSUPPORTED.contains(p.toLowerCase(Locale.ROOT))) return null;
			}
			return ctx.bookmarks().get(bookmark.toLowerCase(Locale.ROOT));
		});
		register("QUOTE", (instr, ctx) -> instr.getArgument() == null ? "" : instr.getArgument()); // no argument: empty (measured 2026-10-28)
		FieldEvaluator date = new FieldEvaluator() {
			@Override
			public String evaluate(FieldInstruction instruction, FieldContext context) {
				return FormattingSwitchHelper.formatDate(instruction, context.now(), context.getLanguage(), context.getTimeZone());
			}
			@Override
			public boolean formatsItself() {
				return true;
			}
		};
		register("DATE", date);
		register("TIME", date);
		// cp:lastPrinted, as Word's F9 shows it (measured 2026-10-28, CR-067 section 12.2, D14); the output path's own
		// PRINTDATE handler is the clock, the conversion being the printing
		register("PRINTDATE", new CoreDate("LastPrinted"));
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
		// a user supplied without initials or an address shows them empty, as Word shows a blank one (measured 2026-10-28)
		register("USERNAME", (instr, ctx) -> ctx.getUserName());
		register("USERINITIALS", (instr, ctx) -> ctx.getUserName() == null ? null : (ctx.getUserInitials() == null ? "" : ctx.getUserInitials()));
		register("USERADDRESS", (instr, ctx) -> ctx.getUserName() == null ? null : (ctx.getUserAddress() == null ? "" : ctx.getUserAddress()));
		// tier 3 (CR-067 phase 3, measured 2026-10-28, section 14.2)
		register("NUMWORDS", (instr, ctx) -> String.valueOf(FieldSources.storyCounts(ctx.getPackage())[0]));
		register("NUMCHARS", (instr, ctx) -> String.valueOf(FieldSources.storyCounts(ctx.getPackage())[1]));
		register("FILENAME", (instr, ctx) -> {
			// the name as given (a path if the caller gave one): \p shows all of it, else the last segment
			String name = ctx.getFileName();
			if (name == null) return null;
			if (instr.hasSwitch("\\p")) return name;
			int cut = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
			return cut < 0 ? name : name.substring(cut + 1);
		});
		register("TEMPLATE", (instr, ctx) -> {
			// app.xml's Template (Normal.dotm, measured); \p (the template's path) is not known here: the cached result stays
			if (instr.hasSwitch("\\p")) return null;
			if (ctx.getPackage() == null || ctx.getPackage().getDocPropsExtendedPart() == null) return null;
			org.docx4j.docProps.extended.Properties props = ctx.getPackage().getDocPropsExtendedPart().getJaxbElement();
			return props == null || props.getTemplate() == null ? null : props.getTemplate();
		});
		register("FILESIZE", (instr, ctx) -> {
			// bytes; \k kilobytes and \m megabytes rounded (16820 bytes showed 17 and 0, measured: a thousand, not 1024)
			Long size = ctx.getFileSize();
			if (size == null) return null;
			if (instr.hasSwitch("\\k")) return String.valueOf(Math.round(size / 1000.0));
			if (instr.hasSwitch("\\m")) return String.valueOf(Math.round(size / 1000000.0));
			return String.valueOf(size);
		});
		// INDEX is block-level: FieldUpdater regenerates it through IndexGenerator; here it keeps its cached result
		register("INDEX", (instr, ctx) -> null);
		// the address fields of a merge (the mergers supply the record; outside a merge the cached result is kept)
		register("ADDRESSBLOCK", (instr, ctx) -> org.docx4j.model.fields.merge.AddressFields.addressBlock(instr, ctx));
		register("GREETINGLINE", (instr, ctx) -> org.docx4j.model.fields.merge.AddressFields.greetingLine(instr, ctx));
		// tier 2 (CR-067 phase 2, measured 2026-10-28, section 13.2)
		register("=", new FieldEvaluator() {
			@Override
			public String evaluate(FieldInstruction instruction, FieldContext context) throws Exception {
				return Formula.evaluate(instruction, context); // its \# from the unrounded value, then its \*
			}
			@Override
			public boolean formatsItself() {
				return true;
			}
		});
		register("COMPARE", (instr, ctx) -> Conditional.compare(instr, ctx));
		register("IF", (instr, ctx) -> Conditional.ifField(instr, ctx));
		// SET name value: the value is the result (Word stores it under a bookmark of that name and shows nothing);
		// the context's bookmarks learn it, so a REF, a formula or an IF after it reads it
		register("SET", (instr, ctx) -> {
			java.util.List<String> p = instr.getParameters();
			if (p.isEmpty()) return null;
			String value = p.size() > 1 && !p.get(1).startsWith("\\") ? FieldInstruction.unquote(p.get(1)) : "";
			ctx.bookmarks().put(FieldInstruction.unquote(p.get(0)).toLowerCase(Locale.ROOT), value);
			return value;
		});
		// the merge fields (CR-067 phase 2, measured 2026-10-28, section 13.4): read from the context's merge record,
		// which the mergers set per record; outside a merge they keep their cached result
		register("MERGEFIELD", (instr, ctx) -> {
			Map<String, String> record = ctx.getMergeRecord();
			if (record == null || instr.getArgument() == null) return null;
			String name = FieldInstruction.unquote(instr.getArgument());
			String value = record.get(name);
			if (value == null) {
				for (Map.Entry<String, String> e : record.entrySet()) if (e.getKey().equalsIgnoreCase(name)) value = e.getValue();
			}
			if (value == null || value.trim().isEmpty()) return ""; // \b and \f print only around a value
			String before = instr.getSwitchValue("\\b"), after = instr.getSwitchValue("\\f");
			return (before == null ? "" : FieldInstruction.unquote(before)) + value + (after == null ? "" : FieldInstruction.unquote(after));
		});
		register("MERGEREC", (instr, ctx) -> ctx.getMergeRecordNumber() == null ? null : String.valueOf(ctx.getMergeRecordNumber()));
		register("MERGESEQ", (instr, ctx) -> ctx.getMergeSequence() == null ? null : String.valueOf(ctx.getMergeSequence()));
		// ASK bookmark "prompt" \d default: the answer (the context's, else the default) under the bookmark; shows nothing
		register("ASK", (instr, ctx) -> {
			if (instr.getArgument() == null) return null;
			String bookmark = FieldInstruction.unquote(instr.getArgument());
			String answer = ctx.answers().get(bookmark);
			if (answer == null) answer = instr.getSwitchValue("\\d");
			answer = answer == null ? "" : FieldInstruction.unquote(answer);
			ctx.bookmarks().put(bookmark.toLowerCase(Locale.ROOT), answer);
			return "";
		});
		// FILLIN "prompt" \d default: the answer (the context's by the prompt, else the default) is the result
		register("FILLIN", (instr, ctx) -> {
			String prompt = instr.getArgument() == null ? "" : FieldInstruction.unquote(instr.getArgument());
			String answer = ctx.answers().get(prompt);
			if (answer == null) answer = instr.getSwitchValue("\\d");
			return answer == null ? "" : FieldInstruction.unquote(answer);
		});
	}

	/**
	 * A complex field's instruction with each nested field replaced by its result (D4: inside-out), or null where a
	 * nested field has no evaluator or no result (the outer then keeps its cached result). A nested field's own
	 * stored result is not rewritten (the canonical form gives it no result slot); Word rewrites it on its own update.
	 * The context's section and cells are set from the field's position where a part is given.
	 *
	 * @since 17.3.5 (CR-067 phase 2)
	 */
	public static String instructionOf(FieldRef fr, FieldContext context, org.docx4j.openpackaging.parts.Part part) throws Docx4JException {
		if (fr.getInstructions().isEmpty()) return null;
		StringBuilder sb = new StringBuilder();
		for (Object o : fr.getInstructions()) {
			Object u = org.docx4j.XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.wml.Text) {
				sb.append(((org.docx4j.wml.Text) u).getValue());
			} else if (u instanceof FieldRef) {
				FieldRef nested = (FieldRef) u;
				String instr = instructionOf(nested, context, part);
				if (instr == null) return null;
				FieldInstruction instruction = FieldInstruction.parse(instr);
				if (instruction.getName() == null || !has(instruction.getName())) return null;
				if (part != null) context.section(FieldContext.sectionOf(part, fr.getParent()));
				context.cells(FieldSources.cellsOf(fr.getParent()));
				String value = evaluate(instruction, context);
				if (value == null) return null;
				sb.append(FieldContext.asOperand(value));
			} else if (u instanceof org.docx4j.wml.R.Cr || u instanceof org.docx4j.wml.Br) {
				sb.append('\n'); // a line break in the instruction (Word's own ADDRESSBLOCK format; CR-067 section 14.4)
			} else {
				return null;
			}
		}
		return sb.toString();
	}

	/**
	 * CREATEDATE, SAVEDATE and PRINTDATE: the core property's W3CDTF value as a date (a value with no offset or a date
	 * alone is UTC, as Word reads it: CR-067 section 12.2), formatted by the field's {@code \@} switch, or without one
	 * in Word's default format (section 11.3). Null, the cached result kept, where the property is absent (PRINTDATE
	 * in a document never printed).
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
			// to the minute, as Word reads a file's time (04:05:06Z shows as :05:00; measured 2026-10-28, CR-067 section 11.3)
			date = new java.util.Date(date.getTime() - Math.floorMod(date.getTime(), 60000L));
			if (instruction.hasSwitch("\\@")) return FormattingSwitchHelper.formatDate(instruction, date, context.getLanguage(), context.getTimeZone());
			// Word's default for the field: the short date and the long time, 2/3/2024 3:05:00 PM for en-US (measured, the
			// same day, Word 365 build 20326.20232); another language gets the locale's short date and medium time, unmeasured
			String lang = context.getLanguage();
			java.util.Locale locale = lang == null ? java.util.Locale.US : java.util.Locale.forLanguageTag(lang);
			java.text.DateFormat f = "en".equals(locale.getLanguage()) || locale.getLanguage().isEmpty()
					? new java.text.SimpleDateFormat("M/d/yyyy h:mm:ss a", java.util.Locale.US)
					: java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM, locale);
			f.setTimeZone(context.getTimeZone()); // the package's client zone, else the machine's (D12)
			return f.format(date);
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

	/**
	 * LISTNUM, AUTONUM, AUTONUMLGL, AUTONUMOUT: the field's number from the context's {@link ListNumbers}, by the field's
	 * node, else the next in document order; null (the cached result kept) outside the main document part.
	 */
	private static String listNumber(FieldInstruction instr, FieldContext ctx) {
		if (ctx.getPart() != null && ctx.getPackage() != null && ctx.getPart() != ctx.getPackage().getMainDocumentPart()) return null;
		Object node = ctx.getNode();
		String v = ctx.listNumbers().resultOf(node);
		if (v == null && node != null) v = ctx.listNumbers(true).resultOf(node); // the tree changed since the scan (canonicalised)
		if (v == null) v = ctx.listNumbers().resultAt(ctx.nextListNumberOrdinal());
		return v;
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
