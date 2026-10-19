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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One finding of an {@link Inspection} (WD REQ-901, CR-050 section 2.3): a kind at a stored location.
 * It says whether a person sees the content ({@link #getVisible()}), how to obtain it
 * ({@link #getReveal()}), its excerpt (at most {@value Inspect#EXCERPT} code points) or summary, and
 * the extraction address of the block that holds it in the output its reveal names. Its group is not
 * a field: it follows from the finding and an output ({@link #group}).
 *
 * <p>A finding is a channel, not a verdict: a run of white text is reported whatever it says.</p>
 *
 * @since 17.3.4 (CR-050 phase 1)
 */
public final class InspectFinding {

	// --- the kinds (WD REQ-902): unseen in the text ---------------------------------------------------

	/** A run whose colour is within WCAG 1.5:1 of its effective background (CR-050 D3). */
	public static final String TEXT_NEAR_BACKGROUND = "text.near_background";
	/** A run every non-space character of which is drawn under 2pt. */
	public static final String TEXT_TINY = "text.tiny";
	/** U+200B to U+200F, U+2060, U+FEFF. */
	public static final String CHARS_ZERO_WIDTH = "chars.zero_width";
	/** U+E0000 to U+E007F. */
	public static final String CHARS_TAG = "chars.tag";
	/** U+202A to U+202E, U+2066 to U+2069. */
	public static final String CHARS_BIDI = "chars.bidi";
	/** The private-use area, outside a symbol or icon font (D17). */
	public static final String CHARS_PRIVATE_USE = "chars.private_use";
	/** A character an embedded face maps to a glyph with no outline (D18). */
	public static final String FONT_GLYPH_BLANK = "font.glyph_blank";
	/** A letter or digit an embedded face draws as another, on the face's own evidence (D18). */
	public static final String FONT_GLYPH_MISMATCH = "font.glyph_mismatch";
	/** The body of a heading marked collapsed (D13). */
	public static final String PARAGRAPH_COLLAPSED = "paragraph.collapsed";
	public static final String SHAPE_ZERO_SIZE = "shape.zero_size";
	/** An anchored shape wholly off the page for every position its anchor can have (D4). */
	public static final String SHAPE_OFF_PAGE = "shape.off_page";
	public static final String SHAPE_HIDDEN = "shape.hidden";
	public static final String SMARTART_DATA = "smartart.data";

	// --- omitted from the text ---------------------------------------------------------------------

	public static final String TEXT_HIDDEN = "text.hidden";
	public static final String PARAGRAPH_HIDDEN = "paragraph.hidden";
	public static final String REVISION_DELETED = "revision.deleted";
	public static final String REVISION_MOVED_FROM = "revision.moved_from";
	/** An {@code mc:Choice} or {@code mc:Fallback} the text model does not select. */
	public static final String MCE_BRANCH = "mce.branch";
	/** A header or footer no section uses, a note no reference reaches, a comment with no anchor. */
	public static final String STORY_UNUSED = "story.unused";
	public static final String SDT_STORED_SURFACE = "sdt.stored_surface";
	public static final String FIELD_CODE = "field.code";
	public static final String GLOSSARY = "glossary";
	public static final String ALT_TEXT_DESCRIPTION = "alt_text.description";
	public static final String ALT_TEXT_TITLE = "alt_text.title";
	public static final String ALT_TEXT_NAME = "alt_text.name";
	public static final String EMBEDDED_SHEET_HIDDEN = "embedded.sheet_hidden";
	public static final String EMBEDDED_SHEET_VERY_HIDDEN = "embedded.sheet_very_hidden";
	public static final String EMBEDDED_SLIDE_HIDDEN = "embedded.slide_hidden";
	public static final String EMBEDDED_ROW_HIDDEN = "embedded.row_hidden";
	public static final String EMBEDDED_COLUMN_HIDDEN = "embedded.column_hidden";
	public static final String EMBEDDED_NOTES = "embedded.notes";
	public static final String COMMENT = "comment";
	public static final String HEADER_FOOTER = "header_footer";
	public static final String WATERMARK = "watermark";
	public static final String DOC_VARS = "doc_vars";
	public static final String CUSTOM_XML = "custom_xml";
	public static final String PROPERTIES = "properties";

	// --- active: what Office would fetch or run --------------------------------------------------------

	public static final String FIELD_EXTERNAL = "field.external";
	public static final String SETTINGS_UPDATE_FIELDS = "settings.update_fields";
	public static final String RELATIONSHIP_EXTERNAL = "relationship.external";
	public static final String PART_MACRO = "part.macro";
	public static final String PART_ACTIVEX = "part.activex";
	public static final String ALTCHUNK_SCRIPT = "altchunk.script";
	public static final String ALTCHUNK_EXTERNAL_REF = "altchunk.external_ref";

	// --- info: seen, or an inventory ------------------------------------------------------------------

	public static final String HYPERLINK_WEB = "hyperlink.web";
	public static final String NOTE = "note";
	public static final String SDT_PLACEHOLDER = "sdt.placeholder";
	public static final String CHART_CACHED = "chart.cached";
	public static final String TEXT_WEB_HIDDEN = "text.web_hidden";
	public static final String ALTCHUNK = "altchunk";
	public static final String OBJECT_EMBEDDED = "object.embedded";
	public static final String FONT_EMBEDDED = "font.embedded";
	public static final String INK = "ink";
	public static final String PEOPLE = "people";
	public static final String REVISION_AUTHOR = "revision.author";
	public static final String RSIDS = "rsids";
	public static final String PART_UNKNOWN = "part.unknown";

	/** The kinds that are active whatever the output (REQ-905). */
	static final Set<String> ACTIVE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(FIELD_EXTERNAL,
			SETTINGS_UPDATE_FIELDS, RELATIONSHIP_EXTERNAL, PART_MACRO, PART_ACTIVEX, ALTCHUNK_SCRIPT, ALTCHUNK_EXTERNAL_REF)));
	/** The inventory kinds, info whatever the output (REQ-905). */
	static final Set<String> INVENTORY = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(ALTCHUNK,
			OBJECT_EMBEDDED, FONT_EMBEDDED, INK, REVISION_AUTHOR, RSIDS, PEOPLE, PART_UNKNOWN, HYPERLINK_WEB)));

	// --- what a container needs (REQ-905) ---------------------------------------------------------------

	/** In no output: an unselected branch, an unused story, an RTF chunk, a title. */
	static final String NEVER = "never";
	static final String VIEW_ORIGINAL = "view:original";
	static final String VIEW_MARKUP = "view:markup";
	/** Inserted or moved-to content: not in the original view (the defaults meet it, so no reveal names it). */
	static final String NOT_ORIGINAL = "not:original";
	static final String HEADERS = "include:headers";
	static final String FOOTERS = "include:footers";
	static final String GLOSSARY_SCOPE = "include:glossary";
	static final String TEXT_BOXES = "text_boxes";
	static final String NOTES = "notes";
	static final String COMMENTS = "comments";
	static final String HIDDEN = "hidden";
	static final String FIELD_CODES = "field_codes";
	static final String TOC = "toc";
	static final String PLACEHOLDERS = "placeholders";
	static final String SURFACE = "bindings:surface";
	static final String CHARTS = "charts";
	static final String SMARTART = "smartart";
	static final String EMBEDDED = "embedded";
	static final String ALTCHUNKS = "altchunks";
	static final String ALT_TEXT = "alt_text";
	static final String METADATA = "metadata";
	static final String MARKUP_DETAIL = "markup_detail";
	static final String IMPORTER = "importer";

	/** Each need a reveal names, in the WD's section 10 order, with its text; the defaults meet the rest. */
	private static final String[][] REVEAL = { { VIEW_ORIGINAL, "view: original" }, { VIEW_MARKUP, "view: markup" },
			{ HEADERS, "include: headers" }, { FOOTERS, "include: footers" }, { GLOSSARY_SCOPE, "include: glossary" },
			{ TEXT_BOXES, "text_boxes: true" }, { NOTES, "notes: true" }, { COMMENTS, "comments: true" }, { HIDDEN, "hidden: true" },
			{ FIELD_CODES, "field_codes: true" }, { TOC, "toc: true" }, { PLACEHOLDERS, "placeholders: true" },
			{ SURFACE, "bindings: surface" }, { CHARTS, "charts: true" }, { SMARTART, "smartart: true" },
			{ EMBEDDED, "embedded: true" }, { ALTCHUNKS, "altchunks: true" }, { ALT_TEXT, "alt_text: true" },
			{ METADATA, "metadata: true" }, { MARKUP_DETAIL, "markup_detail: author_date" }, { IMPORTER, "xhtml importer: present" } };
	/** The needs the default options meet. */
	static final Set<String> DEFAULT_ON = new HashSet<String>(Arrays.asList(NOTES, TEXT_BOXES, PLACEHOLDERS,
			ALTCHUNKS, CHARTS, SMARTART, NOT_ORIGINAL));

	/**
	 * Where a finding is stored (WD REQ-901): the chain of containing packages, outermost first (empty
	 * for the document itself; an embedded package or {@code altChunk} part is one level), the part name
	 * within the innermost, an element path with every step positioned under the canonical prefixes
	 * ({@code /w:document[1]/w:body[1]/w:p[12]/w:r[2]}, empty for a whole part), and for a finding
	 * within runs a half-open range of code points: in the first run's text from {@link #getStart()},
	 * to {@link #getEnd()} in the run at {@link #getEndPath()} (empty where that is the first).
	 */
	public static final class Location {
		private final List<String> chain;
		private final String part;
		private final String path;
		private final Integer start;
		private final String endPath;
		private final Integer end;

		Location(List<String> chain, String part, String path, Integer start, String endPath, Integer end) {
			this.chain = Collections.unmodifiableList(new ArrayList<String>(chain));
			this.part = part;
			this.path = path == null ? "" : path;
			this.start = start;
			this.endPath = endPath == null ? "" : endPath;
			this.end = end;
		}

		/** The containing packages' part names, outermost first; empty for the document itself. */
		public List<String> getChain() { return chain; }
		public String getPart() { return part; }
		/** The element path, or empty for a whole part. */
		public String getPath() { return path; }
		/** The range's start in the first run's text, in code points; null for no range. */
		public Integer getStart() { return start; }
		/** The last run's (or block's) path where the finding ends elsewhere; else empty. */
		public String getEndPath() { return endPath; }
		/** The range's end, in code points, in the last run's text; null for no range. */
		public Integer getEnd() { return end; }

		/** {@code chain|part|path}, the chain's parts {@code >} apart, as a skipped entry writes a location. */
		@Override
		public String toString() {
			return String.join(">", chain) + "|" + part + "|" + path;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Location)) return false;
			Location l = (Location) o;
			return chain.equals(l.chain) && part.equals(l.part) && path.equals(l.path) && Objects.equals(start, l.start)
					&& endPath.equals(l.endPath) && Objects.equals(end, l.end);
		}

		@Override
		public int hashCode() {
			return Objects.hash(chain, part, path, start, endPath, end);
		}
	}

	private final String kind;
	private final Location location;
	private final String address;
	private final Visible visible;
	private final String excerpt;
	private final int excerptLength;
	private final String summary;
	private final String subject;
	final Set<String> needs;

	InspectFinding(String kind, Location location, String address, Visible visible, String excerpt, String summary,
			String subject, Set<String> needs) {
		this(kind, location, address, visible, excerpt == null ? "" : excerpt,
				excerpt == null ? 0 : excerpt.codePointCount(0, excerpt.length()), summary, subject, needs);
	}

	private InspectFinding(String kind, Location location, String address, Visible visible, String excerpt, int excerptLength,
			String summary, String subject, Set<String> needs) {
		this.kind = kind;
		this.location = location;
		this.address = address;
		this.visible = visible;
		this.excerptLength = excerptLength;
		this.excerpt = excerpt.codePointCount(0, excerpt.length()) > Inspect.EXCERPT
				? excerpt.substring(0, excerpt.offsetByCodePoints(0, Inspect.EXCERPT)) : excerpt;
		this.summary = summary == null ? "" : summary;
		this.subject = subject;
		this.needs = Collections.unmodifiableSet(new LinkedHashSet<String>(needs));
	}

	InspectFinding withAddress(String address) {
		return new InspectFinding(kind, location, address, visible, excerpt, excerptLength, summary, subject, needs);
	}

	/** Its identifier (WD REQ-902): one of the constants of this class. */
	public String getKind() { return kind; }

	public Location getLocation() { return location; }

	/**
	 * The address (REQ-518, REQ-519) of the block that holds the content in the output its reveal
	 * names, as docx4j's text model gives it; null where the content is unextractable or no block holds
	 * it (a properties part, an author), and in a report a global limit stopped.
	 */
	public String getAddress() { return address; }

	public Visible getVisible() { return visible; }

	/** How to obtain the content (REQ-901): the options every container on its chain needs. */
	public Reveal getReveal() {
		if (needs.contains(NEVER)) return Reveal.UNEXTRACTABLE;
		List<String> options = new ArrayList<String>();
		for (String[] r : REVEAL) if (needs.contains(r[0]) && !DEFAULT_ON.contains(r[0])) options.add(r[1]);
		return Reveal.options(options);
	}

	/** The content's text, or what the kind names instead (an attribute's value, a target), cut at {@value Inspect#EXCERPT} code points. */
	public String getExcerpt() { return excerpt; }

	/** The excerpt's length in code points before it was cut. */
	public int getExcerptLength() { return excerptLength; }

	/** Whether the excerpt was cut. */
	public boolean isExcerptCut() { return excerptLength > Inspect.EXCERPT; }

	/** What a kind with no text of its own says instead (a content type, a ProgID, a number), or empty. */
	public String getSummary() { return summary; }

	/**
	 * What the finding names, where its kind names something: a link's or relationship's target, a
	 * revision's author, an embedded face's font name; else null.
	 */
	public String getSubject() { return subject; }

	/**
	 * Whether an output holds the content (REQ-905): every container on its chain lets it through
	 * under these options, in this format, with these capabilities.
	 */
	public boolean isIn(TextOptions options, Inspection.Format format, Set<Inspection.Capability> capabilities) {
		TextOptions o = options == null ? new TextOptions() : options;
		TextView view = o.getView();
		for (String need : needs) {
			boolean met;
			switch (need) {
			case NEVER: met = false; break;
			case VIEW_ORIGINAL: met = view == TextView.ORIGINAL || view == TextView.MARKUP; break;
			case VIEW_MARKUP: met = view == TextView.MARKUP; break;
			case NOT_ORIGINAL: met = view != TextView.ORIGINAL; break;
			case HEADERS: met = o.getInclude().contains(TextOptions.Include.HEADERS); break;
			case FOOTERS: met = o.getInclude().contains(TextOptions.Include.FOOTERS); break;
			case GLOSSARY_SCOPE: met = o.getInclude().contains(TextOptions.Include.GLOSSARY); break;
			case TEXT_BOXES: met = o.isTextBoxes(); break;
			case NOTES: met = o.isNotes(); break;
			case COMMENTS: met = o.isComments() || view == TextView.MARKUP; break;
			case HIDDEN: met = o.isHidden(); break;
			case FIELD_CODES: met = o.isFieldCodes(); break;
			case TOC: met = o.isToc(); break;
			case PLACEHOLDERS: met = o.isPlaceholders(); break;
			case SURFACE: met = o.getBindings() == TextOptions.Bindings.SURFACE; break;
			case CHARTS: met = o.isCharts(); break;
			case SMARTART: met = o.isSmartart(); break;
			case EMBEDDED: met = o.isEmbedded(); break;
			case ALTCHUNKS: met = o.isAltchunks(); break;
			case ALT_TEXT: met = format != Inspection.Format.PLAIN || o.isAltText(); break;
			case METADATA: met = format == Inspection.Format.BLOCKS || o.isMetadata(); break;
			case MARKUP_DETAIL: met = o.getMarkupDetail() != TextOptions.MarkupDetail.TYPE; break;
			case IMPORTER: met = capabilities != null && capabilities.contains(Inspection.Capability.XHTML_IMPORTER); break;
			default: throw new IllegalStateException("Unknown need: " + need);
			}
			if (!met) return false;
		}
		return true;
	}

	/**
	 * Its group for an output (REQ-905), in this order of precedence: active for the active kinds;
	 * info for the inventory kinds; for the properties, info while in the output (labelled metadata)
	 * and omitted otherwise; unseen when in the output and not plainly seen; omitted when not in the
	 * output and it has text; else info.
	 */
	public Group group(TextOptions options, Inspection.Format format, Set<Inspection.Capability> capabilities) {
		if (ACTIVE.contains(kind)) return Group.ACTIVE;
		if (INVENTORY.contains(kind)) return Group.INFO;
		boolean in = isIn(options, format, capabilities);
		if (PROPERTIES.equals(kind)) return in ? Group.INFO : Group.OMITTED;
		if (in && (visible == Visible.NO || visible == Visible.DIFFERENTLY || visible == Visible.INDETERMINATE)) return Group.UNSEEN;
		if (!in && excerptLength > 0) return Group.OMITTED;
		return Group.INFO;
	}

	@Override
	public String toString() {
		return kind + " at " + location + (location.getStart() == null ? "" : "[" + location.getStart() + ","
				+ (location.getEndPath().isEmpty() ? "" : location.getEndPath() + ":") + location.getEnd() + ")");
	}
}
