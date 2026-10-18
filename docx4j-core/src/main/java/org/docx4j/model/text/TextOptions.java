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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * What a text includes and how it is written: one setter per option of WD section 10, under the
 * WD's names in {@link #of(Map)} (REQ-1001). Mutable; {@link Texts#of} reads it once.
 *
 * <p>Every option name and value of WD section 10 is accepted. Two values are not implemented,
 * and are rejected by {@link Texts#of} with a message naming REQ-1001 rather than silently ignored
 * (CR-046 section 2.6): {@code math: unicodemath} (docx4j has no OMML-to-UnicodeMath converter) and
 * {@code symbols: unicode} (docx4j has no glyph tables for Symbol and Wingdings).</p>
 *
 * <p>Under {@code fields: update} (REQ-506), docx4j computes DOCPROPERTY, the core-property fields
 * (AUTHOR, TITLE, SUBJECT, KEYWORDS, COMMENTS, LASTSAVEDBY, REVNUM, CREATEDATE, SAVEDATE), DOCVARIABLE,
 * SEQ, REF to a bookmark and QUOTE, with their {@code \*}, {@code \#} and {@code \@} switches, and
 * FORMTEXT as Word's update leaves it: its default text, or five en spaces where it has none, but what
 * was typed in a document protected for forms. It computes no MERGEFIELD (no data is supplied), and
 * never a field with {@code w:fldLock}; every other field keeps its cached result.</p>
 *
 * <p>Beyond WD section 10, a host may give the {@link ImageDescriber} (REQ-514).</p>
 *
 * @since 17.3.4 (CR-046 phase 1; phase 2 implemented the values)
 */
public final class TextOptions {

	/** The stories {@code include} adds (WD REQ-501, REQ-502). */
	public enum Include { HEADERS, FOOTERS, COMMENTS, GLOSSARY }

	/** {@code fields} (REQ-506). */
	public enum Fields { CACHED, UPDATE }

	/** {@code bindings} (REQ-509). */
	public enum Bindings { BOUND, SURFACE }

	/** {@code symbols} (REQ-601). */
	public enum Symbols { ASIS, UNICODE }

	/** {@code math} (REQ-604). */
	public enum MathFormat { LATEX, UNICODEMATH }

	/** {@code tables} (REQ-605). */
	public enum Tables { AUTO, TSV, GRID }

	/** {@code markup_detail} (REQ-702). */
	public enum MarkupDetail { TYPE, AUTHOR, AUTHOR_DATE }

	/** {@code layout} (REQ-709). */
	public enum Layout { NONE, INDENT }

	/** The option names of WD section 10, in its order. */
	public static final List<String> NAMES = Collections.unmodifiableList(java.util.Arrays.asList(
			"view", "include", "text_boxes", "notes", "note_references", "comments", "hidden", "field_codes",
			"fields", "toc", "placeholders", "bindings", "list_labels", "charts", "smartart", "embedded", "altchunks",
			"alt_text", "metadata", "symbols", "math", "tables", "markup_detail", "page_markers", "layout",
			"warnings"));

	private TextView view = TextView.ACCEPTED;
	private final EnumSet<Include> include = EnumSet.noneOf(Include.class);
	private boolean textBoxes = true;
	private boolean notes = true;
	private boolean noteReferences = true;
	private boolean comments = false;
	private boolean hidden = false;
	private boolean fieldCodes = false;
	private Fields fields = Fields.CACHED;
	private boolean toc = false;
	private boolean placeholders = true;
	private Bindings bindings = Bindings.BOUND;
	private boolean listLabels = false;
	private boolean charts = true;
	private boolean smartart = true;
	private boolean embedded = false;
	private boolean altchunks = true;
	private boolean altText = false;
	private boolean metadata = false;
	private Symbols symbols = Symbols.ASIS;
	private MathFormat math = MathFormat.LATEX;
	private Tables tables = Tables.AUTO;
	private MarkupDetail markupDetail = MarkupDetail.TYPE;
	private boolean pageMarkers = false;
	private Layout layout = Layout.NONE;
	private boolean warnings = true;
	private ImageDescriber imageDescriber = AltTextDescriber.INSTANCE;

	/** The defaults of WD section 10. */
	public TextOptions() {}

	/** The defaults, in a view. */
	public static TextOptions of(TextView view) {
		return new TextOptions().view(view);
	}

	/**
	 * Options from the WD's names and values, verbatim (REQ-1001), as docx4j-mcp passes its
	 * parameters: a boolean as a {@link Boolean} or {@code "true"}/{@code "false"}; {@code include} as
	 * a collection of names or a string of them separated by commas or spaces; any other value as its
	 * name, case-insensitively. Options not named keep their defaults.
	 *
	 * @throws IllegalArgumentException for a name or value the WD does not define
	 */
	public static TextOptions of(Map<String, ?> options) {
		TextOptions o = new TextOptions();
		if (options == null) return o;
		for (Map.Entry<String, ?> e : options.entrySet()) o.set(e.getKey(), e.getValue());
		return o;
	}

	/**
	 * Sets one option by its WD name and value.
	 *
	 * @throws IllegalArgumentException for a name or value the WD does not define
	 */
	public TextOptions set(String name, Object value) {
		String n = name == null ? "" : name.trim();
		switch (n) {
		case "view": view = value instanceof TextView ? (TextView) value : TextView.of(string(n, value)); break;
		case "include": include(n, value); break;
		case "text_boxes": textBoxes = bool(n, value); break;
		case "notes": notes = bool(n, value); break;
		case "note_references": noteReferences = bool(n, value); break;
		case "comments": comments = bool(n, value); break;
		case "hidden": hidden = bool(n, value); break;
		case "field_codes": fieldCodes = bool(n, value); break;
		case "fields": fields = choice(n, value, Fields.class); break;
		case "toc": toc = bool(n, value); break;
		case "placeholders": placeholders = bool(n, value); break;
		case "bindings": bindings = choice(n, value, Bindings.class); break;
		case "list_labels": listLabels = bool(n, value); break;
		case "charts": charts = bool(n, value); break;
		case "smartart": smartart = bool(n, value); break;
		case "embedded": embedded = bool(n, value); break;
		case "altchunks": altchunks = bool(n, value); break;
		case "alt_text": altText = bool(n, value); break;
		case "metadata": metadata = bool(n, value); break;
		case "symbols": symbols = choice(n, value, Symbols.class); break;
		case "math": math = choice(n, value, MathFormat.class); break;
		case "tables": tables = choice(n, value, Tables.class); break;
		case "markup_detail": markupDetail = choice(n, value, MarkupDetail.class); break;
		case "page_markers": pageMarkers = bool(n, value); break;
		case "layout": layout = choice(n, value, Layout.class); break;
		case "warnings": warnings = bool(n, value); break;
		default:
			throw new IllegalArgumentException("Unknown text option '" + name + "'; the options are " + NAMES + " (WD REQ-1001)");
		}
		return this;
	}

	private static String string(String name, Object value) {
		if (value == null) throw new IllegalArgumentException(name + ": no value (WD REQ-1001)");
		return value.toString().trim();
	}

	private static boolean bool(String name, Object value) {
		if (value instanceof Boolean) return (Boolean) value;
		String s = string(name, value).toLowerCase(Locale.ROOT);
		if (s.equals("true")) return true;
		if (s.equals("false")) return false;
		throw new IllegalArgumentException(name + ": '" + value + "' is not true or false (WD REQ-1001)");
	}

	private static <E extends Enum<E>> E choice(String name, Object value, Class<E> type) {
		if (type.isInstance(value)) return type.cast(value);
		String s = string(name, value).toUpperCase(Locale.ROOT);
		for (E e : type.getEnumConstants()) if (e.name().equals(s)) return e;
		List<String> names = new ArrayList<String>();
		for (E e : type.getEnumConstants()) names.add(e.name().toLowerCase(Locale.ROOT));
		throw new IllegalArgumentException(name + ": '" + value + "' is not one of " + names + " (WD REQ-1001)");
	}

	private void include(String name, Object value) {
		include.clear();
		List<Object> items = new ArrayList<Object>();
		if (value instanceof Collection) items.addAll((Collection<?>) value);
		else if (value != null) for (String s : value.toString().split("[,\\s]+")) if (!s.isEmpty()) items.add(s);
		for (Object item : items) include.add(choice(name, item, Include.class));
	}

	// --- the builder -------------------------------------------------------------------------------

	public TextOptions view(TextView v) { this.view = v == null ? TextView.ACCEPTED : v; return this; }
	public TextOptions include(Include... stories) { for (Include s : stories) include.add(s); return this; }
	public TextOptions textBoxes(boolean b) { this.textBoxes = b; return this; }
	public TextOptions notes(boolean b) { this.notes = b; return this; }
	public TextOptions noteReferences(boolean b) { this.noteReferences = b; return this; }
	public TextOptions comments(boolean b) { this.comments = b; return this; }
	public TextOptions hidden(boolean b) { this.hidden = b; return this; }
	public TextOptions fieldCodes(boolean b) { this.fieldCodes = b; return this; }
	public TextOptions fields(Fields f) { this.fields = f; return this; }
	/** Whether a table of contents is read (CR-046 D12): see {@link #isToc()}. */
	public TextOptions toc(boolean b) { this.toc = b; return this; }
	public TextOptions placeholders(boolean b) { this.placeholders = b; return this; }
	public TextOptions bindings(Bindings b) { this.bindings = b; return this; }
	public TextOptions listLabels(boolean b) { this.listLabels = b; return this; }
	public TextOptions charts(boolean b) { this.charts = b; return this; }
	public TextOptions smartart(boolean b) { this.smartart = b; return this; }
	public TextOptions embedded(boolean b) { this.embedded = b; return this; }
	public TextOptions altchunks(boolean b) { this.altchunks = b; return this; }
	public TextOptions altText(boolean b) { this.altText = b; return this; }
	public TextOptions metadata(boolean b) { this.metadata = b; return this; }
	public TextOptions symbols(Symbols s) { this.symbols = s; return this; }
	public TextOptions math(MathFormat m) { this.math = m; return this; }
	public TextOptions tables(Tables t) { this.tables = t; return this; }
	public TextOptions markupDetail(MarkupDetail d) { this.markupDetail = d; return this; }
	/**
	 * {@code page_markers} (REQ-708): {@code [page n]} at each {@code w:lastRenderedPageBreak}. The
	 * markers are where Word's pages began when it last laid the document out: they are current only
	 * when the package was last paginated (saved by Word) after its last edit, and an extractor
	 * cannot tell whether it was.
	 */
	public TextOptions pageMarkers(boolean b) { this.pageMarkers = b; return this; }
	public TextOptions layout(Layout l) { this.layout = l; return this; }
	public TextOptions warnings(boolean b) { this.warnings = b; return this; }
	/** The describer images take their text from (REQ-514); {@link AltTextDescriber} by default, and for null. */
	public TextOptions imageDescriber(ImageDescriber d) { this.imageDescriber = d == null ? AltTextDescriber.INSTANCE : d; return this; }

	public TextView getView() { return view; }
	/** The stories {@code include} names; {@code comments: true} adds {@link Include#COMMENTS}. */
	public Set<Include> getInclude() {
		EnumSet<Include> set = EnumSet.copyOf(include);
		if (comments) set.add(Include.COMMENTS);
		return Collections.unmodifiableSet(set);
	}
	public boolean isTextBoxes() { return textBoxes; }
	public boolean isNotes() { return notes; }
	public boolean isNoteReferences() { return noteReferences; }
	/** {@code comments: true} or {@code include: comments}. */
	public boolean isComments() { return comments || include.contains(Include.COMMENTS); }
	public boolean isHidden() { return hidden; }
	public boolean isFieldCodes() { return fieldCodes; }
	public Fields getFields() { return fields; }
	/**
	 * Whether a table of contents is read: false by default (WD REQ-520; CR-046 D12). A table of
	 * contents is the result of a TOC field (a table of figures too, {@code TOC \c}); under
	 * {@code toc: false} nothing of the field is read, its code included under {@code field_codes}, and
	 * a paragraph with nothing else in it is no paragraph. A TOC heading is not the field's, so stays;
	 * a table of contents whose field was unlinked is the document's text.
	 */
	public boolean isToc() { return toc; }
	public boolean isPlaceholders() { return placeholders; }
	public Bindings getBindings() { return bindings; }
	public boolean isListLabels() { return listLabels; }
	public boolean isCharts() { return charts; }
	public boolean isSmartart() { return smartart; }
	public boolean isEmbedded() { return embedded; }
	public boolean isAltchunks() { return altchunks; }
	public boolean isAltText() { return altText; }
	public boolean isMetadata() { return metadata; }
	public Symbols getSymbols() { return symbols; }
	public MathFormat getMath() { return math; }
	public Tables getTables() { return tables; }
	public MarkupDetail getMarkupDetail() { return markupDetail; }
	public boolean isPageMarkers() { return pageMarkers; }
	public Layout getLayout() { return layout; }
	public boolean isWarnings() { return warnings; }
	public ImageDescriber getImageDescriber() { return imageDescriber; }

	/** A copy, for a reading one level down (an embedded package's, an {@code altChunk}'s; REQ-512, REQ-513). */
	TextOptions copy() {
		TextOptions o = new TextOptions();
		o.view = view;
		o.include.addAll(include);
		o.textBoxes = textBoxes;
		o.notes = notes;
		o.noteReferences = noteReferences;
		o.comments = comments;
		o.hidden = hidden;
		o.fieldCodes = fieldCodes;
		o.fields = fields;
		o.toc = toc;
		o.placeholders = placeholders;
		o.bindings = bindings;
		o.listLabels = listLabels;
		o.charts = charts;
		o.smartart = smartart;
		o.embedded = embedded;
		o.altchunks = altchunks;
		o.altText = altText;
		o.metadata = metadata;
		o.symbols = symbols;
		o.math = math;
		o.tables = tables;
		o.markupDetail = markupDetail;
		o.pageMarkers = pageMarkers;
		o.layout = layout;
		o.warnings = warnings;
		o.imageDescriber = imageDescriber;
		return o;
	}

	/** The options as the WD names them, with their values, in WD section 10's order. */
	public Map<String, Object> toMap() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("view", view.wdName());
		List<String> inc = new ArrayList<String>();
		for (Include i : include) inc.add(i.name().toLowerCase(Locale.ROOT));
		m.put("include", inc);
		m.put("text_boxes", textBoxes);
		m.put("notes", notes);
		m.put("note_references", noteReferences);
		m.put("comments", comments);
		m.put("hidden", hidden);
		m.put("field_codes", fieldCodes);
		m.put("fields", lower(fields));
		m.put("toc", toc);
		m.put("placeholders", placeholders);
		m.put("bindings", lower(bindings));
		m.put("list_labels", listLabels);
		m.put("charts", charts);
		m.put("smartart", smartart);
		m.put("embedded", embedded);
		m.put("altchunks", altchunks);
		m.put("alt_text", altText);
		m.put("metadata", metadata);
		m.put("symbols", lower(symbols));
		m.put("math", lower(math));
		m.put("tables", lower(tables));
		m.put("markup_detail", lower(markupDetail));
		m.put("page_markers", pageMarkers);
		m.put("layout", lower(layout));
		m.put("warnings", warnings);
		return m;
	}

	private static String lower(Enum<?> e) {
		return e.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		return toMap().toString();
	}

	/**
	 * Throws for a value this implementation does not honour (REQ-1001: documented, and refused
	 * rather than ignored). See the class description for the two.
	 */
	void checkImplemented() {
		List<String> never = new ArrayList<String>();
		if (math == MathFormat.UNICODEMATH) never.add("math: unicodemath");
		if (symbols == Symbols.UNICODE) never.add("symbols: unicode");
		if (never.isEmpty()) return;
		throw new UnsupportedOperationException("Not implemented (WD REQ-1001): " + String.join(", ", never)
				+ " (CR-046 section 2.6: no converter)");
	}
}
