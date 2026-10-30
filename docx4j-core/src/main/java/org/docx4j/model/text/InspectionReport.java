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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An {@link Inspection} written for a person, as Markdown (CR-050 phase 5).
 *
 * <pre>
 * String report = InspectionReport.markdown(Inspect.of(pkg), "contract.docx");
 * </pre>
 *
 * <p>The report takes the findings in their groups for one output, the plain-text extraction under the
 * default options unless the caller names another: first what Office would fetch or run (active), then
 * what is in that text but not seen by a person (unseen), then what the file holds that the text leaves
 * out (omitted), then the rest (info); then what the inspection skipped or could not decide, and what no
 * static inspection detects. Each finding is one line: its kind, a phrase saying what the kind is, where
 * it is (its package chain, part, element path and range, and the block of the output that holds it where
 * one does), its text or what the kind names instead, whether a person sees it, and how to obtain it
 * (its reveal). Findings keep the inspection's order within a group.</p>
 *
 * <p>The report's wording is docx4j's and not normative; the identifiers, groups and reveals it carries
 * are the WD's (section 9). Markdown is the one source: an HTML page is a conversion of it. Everything
 * taken from the document (an excerpt, a target, a name, a path) is written in a code span, so that it
 * cannot form Markdown of its own.</p>
 *
 * @since 17.3.5 (CR-050 phase 5)
 */
public final class InspectionReport {

	private InspectionReport() {}

	/** The report for the plain-text extraction under the default options, with the class path's capabilities. */
	public static String markdown(Inspection inspection) {
		return markdown(inspection, null);
	}

	/** As {@link #markdown(Inspection)}, titled (a file name, say). */
	public static String markdown(Inspection inspection, String title) {
		return markdown(inspection, new TextOptions(), Inspection.Format.PLAIN, Inspection.capabilitiesOnClassPath(), title);
	}

	/**
	 * The report for an output: the groups and counts are for an extraction under these options, in this
	 * format, with these capabilities ({@link InspectFinding#group}).
	 *
	 * @param title what the report is of (a file name), or null for none
	 */
	public static String markdown(Inspection inspection, TextOptions options, Inspection.Format format,
			Set<Inspection.Capability> capabilities, String title) {
		if (inspection == null) throw new IllegalArgumentException("No inspection");
		if (options == null) options = new TextOptions();
		if (format == null) format = Inspection.Format.PLAIN;
		if (capabilities == null) capabilities = Collections.emptySet();

		Map<Group, StringBuilder> sections = new EnumMap<Group, StringBuilder>(Group.class);
		for (Group g : Group.values()) sections.put(g, new StringBuilder());
		Map<Group, Integer> counted = new EnumMap<Group, Integer>(Group.class);
		for (Group g : Group.values()) counted.put(g, 0);
		for (InspectFinding f : inspection.findings()) {
			Group g = f.group(options, format, capabilities);
			sections.get(g).append(line(f));
			counted.put(g, counted.get(g) + 1);
		}

		StringBuilder b = new StringBuilder();
		b.append("# Inspection").append(title == null || title.isEmpty() ? "" : ": " + plain(title)).append("\n\n");
		Inspection.Counts counts = inspection.counts(options, format, capabilities);
		if (inspection.status() == Inspection.Status.COMPLETE) {
			b.append("**Complete**: every story, part and payload was inspected.");
		} else {
			b.append("**Partial**: a limit stopped the inspection, or a payload could not be read (see Skipped and undecided); "
					+ "the counts are lower bounds.");
		}
		b.append(" Findings: ").append(inspection.findings().size()).append(".\n\n");
		b.append("Groups and counts are for ").append(formatName(format)).append(" extracted under ").append(optionsPhrase(options));
		if (!capabilities.isEmpty()) {
			b.append(" with ");
			String sep = "";
			for (Inspection.Capability c : capabilities) {
				b.append(sep).append(code(c.name().toLowerCase(java.util.Locale.ROOT)));
				sep = ", ";
			}
		}
		b.append(": unseen ").append(counts.getUnseen()).append(", omitted ").append(counts.getOmitted()).append(", active ")
				.append(counts.getActive()).append(", info ").append(counts.getInfo()).append(counts.isLowerBound() ? " (lower bounds)" : "")
				.append(". The wording of this report is docx4j's and not normative; the identifiers, groups and reveals are the WD's.\n");

		section(b, "Active", counted.get(Group.ACTIVE), "what Office would fetch or run, whatever the options", sections.get(Group.ACTIVE));
		section(b, "Unseen", counted.get(Group.UNSEEN), "in the text, and not seen by a person as the text says it", sections.get(Group.UNSEEN));
		section(b, "Omitted", counted.get(Group.OMITTED), "in the file, and left out of the text", sections.get(Group.OMITTED));
		section(b, "Info", counted.get(Group.INFO), "the inventory, and content in the text that a person sees", sections.get(Group.INFO));

		b.append("\n## Skipped and undecided\n\n");
		if (inspection.skipped().isEmpty() && inspection.skippedBeyond() == 0 && inspection.undecided().isEmpty()) {
			b.append("None.\n");
		} else {
			for (Inspection.Entry e : inspection.skipped())
				b.append("- skipped: ").append(code(e.getReason())).append(" at ").append(code(e.getLocation())).append('\n');
			if (inspection.skippedBeyond() > 0) b.append("- and ").append(inspection.skippedBeyond()).append(" more skipped\n");
			for (Inspection.Entry e : inspection.undecided())
				b.append("- undecided: ").append(code(e.getReason())).append(" at ").append(code(e.getLocation())).append('\n');
		}

		b.append("\n## Not detected\n\n");
		for (String limitation : inspection.limitations()) {
			int colon = limitation.indexOf(": ");
			if (colon > 0) b.append("- ").append(code(limitation.substring(0, colon))).append(": ").append(plain(limitation.substring(colon + 2)));
			else b.append("- ").append(plain(limitation));
			b.append('\n');
		}
		return b.toString();
	}

	private static void section(StringBuilder b, String name, int count, String meaning, StringBuilder lines) {
		b.append("\n## ").append(name).append(" (").append(count).append("): ").append(meaning).append("\n\n");
		b.append(lines.length() == 0 ? "None.\n" : lines);
	}

	/** The default options, or the default options with the ones that differ, {@code name=value} each. */
	private static String optionsPhrase(TextOptions options) {
		Map<String, Object> defaults = new TextOptions().toMap();
		StringBuilder b = new StringBuilder("the default options");
		String sep = " with ";
		for (Map.Entry<String, Object> e : options.toMap().entrySet()) {
			if (java.util.Objects.equals(e.getValue(), defaults.get(e.getKey()))) continue;
			b.append(sep).append(code(e.getKey() + "=" + e.getValue()));
			sep = ", ";
		}
		return b.toString();
	}

	private static String formatName(Inspection.Format format) {
		switch (format) {
		case MARKDOWN: return "Markdown";
		case BLOCKS: return "the block model";
		default: return "plain text";
		}
	}

	/** One finding, one line. */
	static String line(InspectFinding f) {
		StringBuilder b = new StringBuilder("- ").append(code(f.getKind()));
		String phrase = PHRASES.get(f.getKind());
		if (phrase != null) b.append(" (").append(phrase).append(")");
		b.append(", ").append(where(f.getLocation()));
		if (f.getAddress() != null) b.append(", block ").append(code(f.getAddress()));
		if (!f.getExcerpt().isEmpty()) {
			b.append(": ").append(code(f.getExcerpt()));
			if (f.isExcerptCut()) b.append(" … (").append(f.getExcerptLength()).append(" characters)");
			String invisible = codePoints(f.getExcerpt());
			if (invisible != null) b.append(" = ").append(invisible);
		} else if (!f.getSummary().isEmpty()) {
			b.append(": ").append(code(f.getSummary()));
		}
		if (f.getSubject() != null && !f.getSubject().equals(f.getExcerpt())) b.append("; names ").append(code(f.getSubject()));
		if (f.getParsed() != null) b.append("; parsed as ").append(code(f.getParsed()));
		if (f.getVisible() != Visible.NOT_CONTENT) b.append("; visible: ").append(f.getVisible().id());
		b.append("; reveal: ").append(reveal(f.getReveal()));
		return b.append('\n').toString();
	}

	/** The code points of an excerpt that holds a character no page shows as itself (a format, control, private-use or unassigned character, a line or paragraph separator), the first {@value #CODE_POINTS} at most; null where it holds none. */
	static String codePoints(String excerpt) {
		boolean invisible = false;
		for (int i = 0; i < excerpt.length() && !invisible; i = excerpt.offsetByCodePoints(i, 1)) invisible = invisible(excerpt.codePointAt(i));
		if (!invisible) return null;
		StringBuilder b = new StringBuilder();
		int n = 0, total = excerpt.codePointCount(0, excerpt.length());
		for (int i = 0; i < excerpt.length() && n < CODE_POINTS; i = excerpt.offsetByCodePoints(i, 1), n++) {
			if (n > 0) b.append(' ');
			b.append(String.format("U+%04X", excerpt.codePointAt(i)));
		}
		if (total > CODE_POINTS) b.append(" … (").append(total).append(" code points)");
		return b.toString();
	}

	private static final int CODE_POINTS = 24;

	private static boolean invisible(int cp) {
		if (cp == '\n' || cp == '\r' || cp == '\t') return false; // made spaces in the code span
		switch (Character.getType(cp)) {
		case Character.FORMAT: case Character.CONTROL: case Character.PRIVATE_USE: case Character.UNASSIGNED:
		case Character.LINE_SEPARATOR: case Character.PARAGRAPH_SEPARATOR:
			return true;
		default:
			return false;
		}
	}

	private static String where(InspectFinding.Location l) {
		StringBuilder b = new StringBuilder("in ");
		for (String p : l.getChain()) b.append(code(p)).append(" > ");
		b.append(code(l.getPart()));
		if (!l.getPath().isEmpty()) b.append(" at ").append(code(l.getPath()));
		if (l.getStart() != null) {
			b.append(", code points ").append(l.getStart()).append(" to ").append(l.getEnd());
			if (!l.getEndPath().isEmpty()) b.append(" in ").append(code(l.getEndPath()));
		}
		return b.toString();
	}

	private static String reveal(Reveal r) {
		switch (r.getForm()) {
		case INCLUDED: return "in the default extraction";
		case UNEXTRACTABLE: return "no extraction shows it";
		default: {
			StringBuilder b = new StringBuilder("extract with ");
			String sep = "";
			for (String o : r.getOptions()) {
				b.append(sep).append(code(o));
				sep = ", ";
			}
			return b.toString();
		}
		}
	}

	/**
	 * A code span holding any text: a run of backticks one longer than the longest run inside it, a
	 * space inside each end where the text starts or ends with a backtick, controls made spaces.
	 */
	static String code(String s) {
		if (s == null) s = "";
		StringBuilder t = new StringBuilder(s.length());
		int longest = 0, run = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '`') {
				run++;
				if (run > longest) longest = run;
			} else {
				run = 0;
			}
			t.append(c < 0x20 || c == 0x7f || c == ' ' || c == ' ' ? ' ' : c);
		}
		if (t.length() == 0) return "` `";
		String fence = "`".repeat(longest + 1);
		boolean pad = t.charAt(0) == '`' || t.charAt(t.length() - 1) == '`' || t.charAt(0) == ' ' && t.charAt(t.length() - 1) == ' ';
		return fence + (pad ? " " : "") + t + (pad ? " " : "") + fence;
	}

	/** Prose from the document or the model, its Markdown punctuation escaped. */
	static String plain(String s) {
		StringBuilder b = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ("\\`*_{}[]()#+-.!<>|~".indexOf(c) >= 0 && (c != '-' && c != '.' && c != '+' && c != '!' && c != '(' && c != ')' || i == 0)) b.append('\\');
			b.append(c < 0x20 || c == 0x7f ? ' ' : c);
		}
		return b.toString();
	}

	/** What each kind is, in a phrase, from CR-050 section 2.3's table; not normative. */
	static final Map<String, String> PHRASES;
	static {
		Map<String, String> m = new HashMap<String, String>();
		m.put(InspectFinding.TEXT_NEAR_BACKGROUND, "text in a colour within the threshold of its background");
		m.put(InspectFinding.TEXT_TINY, "text drawn under 2pt");
		m.put(InspectFinding.CHARS_ZERO_WIDTH, "zero-width characters");
		m.put(InspectFinding.CHARS_TAG, "tag characters, U+E0000 to U+E007F");
		m.put(InspectFinding.CHARS_BIDI, "bidirectional control characters");
		m.put(InspectFinding.CHARS_PRIVATE_USE, "private-use characters outside a symbol font");
		m.put(InspectFinding.FONT_GLYPH_BLANK, "a character an embedded font draws as nothing");
		m.put(InspectFinding.FONT_GLYPH_MISMATCH, "a character an embedded font draws as another");
		m.put(InspectFinding.PARAGRAPH_COLLAPSED, "the body of a heading collapsed on open");
		m.put(InspectFinding.SHAPE_ZERO_SIZE, "a shape of no extent");
		m.put(InspectFinding.SHAPE_OFF_PAGE, "a shape wholly off the page");
		m.put(InspectFinding.SHAPE_HIDDEN, "a shape marked hidden");
		m.put(InspectFinding.SMARTART_DATA, "SmartArt data its drawing does not show");
		m.put(InspectFinding.TEXT_HIDDEN, "hidden text");
		m.put(InspectFinding.PARAGRAPH_HIDDEN, "a hidden paragraph");
		m.put(InspectFinding.REVISION_DELETED, "deleted text, a tracked change");
		m.put(InspectFinding.REVISION_MOVED_FROM, "text moved from here, a tracked change");
		m.put(InspectFinding.MCE_BRANCH, "content in an alternative an older Office would show");
		m.put(InspectFinding.STORY_UNUSED, "a header, footer, note or comment nothing references");
		m.put(InspectFinding.SDT_STORED_SURFACE, "a bound control's stored text, differing from its value");
		m.put(InspectFinding.FIELD_CODE, "a field's instruction");
		m.put(InspectFinding.FIELDS_AUTO_UPDATE, "a field Word recomputes on open");
		m.put(InspectFinding.FIELDS_PRINT_UPDATE, "fields updated before printing");
		m.put(InspectFinding.GLOSSARY, "a glossary entry");
		m.put(InspectFinding.ALT_TEXT_DESCRIPTION, "a picture's or shape's description");
		m.put(InspectFinding.ALT_TEXT_TITLE, "a picture's or shape's title");
		m.put(InspectFinding.ALT_TEXT_NAME, "a picture's or shape's name");
		m.put(InspectFinding.IMAGE_SVG_TEXT, "text an SVG image draws");
		m.put(InspectFinding.IMAGE_METAFILE_TEXT, "text a WMF or EMF image draws");
		m.put(InspectFinding.EMBEDDED_SHEET_HIDDEN, "a hidden sheet of an embedded workbook");
		m.put(InspectFinding.EMBEDDED_SHEET_VERY_HIDDEN, "a very hidden sheet of an embedded workbook");
		m.put(InspectFinding.EMBEDDED_SLIDE_HIDDEN, "a hidden slide of an embedded presentation");
		m.put(InspectFinding.EMBEDDED_ROW_HIDDEN, "a hidden row of an embedded workbook");
		m.put(InspectFinding.EMBEDDED_COLUMN_HIDDEN, "a hidden column of an embedded workbook");
		m.put(InspectFinding.EMBEDDED_NOTES, "speaker notes of an embedded presentation");
		m.put(InspectFinding.COMMENT, "a comment");
		m.put(InspectFinding.HEADER_FOOTER, "a header or footer a section uses");
		m.put(InspectFinding.WATERMARK, "a watermark");
		m.put(InspectFinding.DOC_VARS, "document variables");
		m.put(InspectFinding.CUSTOM_XML, "a custom XML part");
		m.put(InspectFinding.PROPERTIES, "the document's properties");
		m.put(InspectFinding.FIELD_EXTERNAL, "a field that fetches from outside the file");
		m.put(InspectFinding.SETTINGS_UPDATE_FIELDS, "fields updated on open");
		m.put(InspectFinding.RELATIONSHIP_EXTERNAL, "an external target: a linked image, template, data source, link or subdocument");
		m.put(InspectFinding.PART_MACRO, "a macro project");
		m.put(InspectFinding.PART_ACTIVEX, "an ActiveX control");
		m.put(InspectFinding.ALTCHUNK_SCRIPT, "a script or event handler in an HTML chunk");
		m.put(InspectFinding.ALTCHUNK_EXTERNAL_REF, "an external reference in an HTML chunk");
		m.put(InspectFinding.HYPERLINK_WEB, "a web or mail link");
		m.put(InspectFinding.NOTE, "a footnote or endnote");
		m.put(InspectFinding.SDT_PLACEHOLDER, "a control showing its placeholder");
		m.put(InspectFinding.CHART_CACHED, "a chart's cached strings");
		m.put(InspectFinding.TEXT_WEB_HIDDEN, "text hidden in Web Layout only");
		m.put(InspectFinding.ALTCHUNK, "an imported chunk");
		m.put(InspectFinding.OBJECT_EMBEDDED, "an embedded object");
		m.put(InspectFinding.FONT_EMBEDDED, "an embedded font the text is drawn in");
		m.put(InspectFinding.INK, "an ink part, perhaps handwriting");
		m.put(InspectFinding.PEOPLE, "the authors' identities");
		m.put(InspectFinding.REVISION_AUTHOR, "an author of tracked changes");
		m.put(InspectFinding.RSIDS, "revision-session identifiers");
		m.put(InspectFinding.PART_UNKNOWN, "a part of an unknown kind");
		m.put(InspectFinding.VALUE_OUT_OF_RANGE, "an attribute value outside the width Word stores it in");
		PHRASES = Collections.unmodifiableMap(m);
	}
}
