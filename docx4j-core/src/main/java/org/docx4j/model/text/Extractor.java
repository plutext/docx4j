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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTOfficeArtExtension;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSplice;
import org.docx4j.jaxb.ResolvedAlternateContent;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.chart.ChartDescription;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartTable;
import org.docx4j.model.math.OmmlMathException;
import org.docx4j.model.math.OmmlToLatex;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.NumberingState;
import org.docx4j.model.listnumbering.NumberingStates;
import org.docx4j.model.revisions.ResolvedView;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.w15.CTCommentEx;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTControl;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTDocPart;
import org.docx4j.wml.CTDocProtect;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTFFTextInput;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTRuby;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Comments;
import org.docx4j.wml.DelText;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.Jc;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.STDocProtect;
import org.docx4j.wml.STFFTextType;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The text model's walk (CR-046 section 2.3): {@code scope(view(resolve(package)))} in one pass over
 * the unresolved tree, producing the block model. Branches of {@code mc:AlternateContent} are
 * selected for {@link McPurpose#TEXT}; the {@code accepted} and {@code original} views ask a
 * {@link ResolvedView} what Accept All or Reject All would remove or join; {@code markup} reads every
 * run with its revision. Nothing in the package is changed (REQ-204).
 */
final class Extractor {

	private static final Logger log = LoggerFactory.getLogger(Extractor.class);

	private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	/**
	 * Fonts whose glyphs are all one width, for the block model's {@code monospace} and Markdown's code.
	 * The WD's definition of a monospaced font (1.3.2, from 0.11) is this list: a change here goes to
	 * the portfolio for the WD, and the ports follow.
	 */
	private static final Set<String> MONOSPACE = new LinkedHashSet<String>(java.util.Arrays.asList(
			"courier new", "courier", "consolas", "lucida console", "lucida sans typewriter", "menlo", "monaco",
			"cascadia mono", "cascadia code", "source code pro", "dejavu sans mono", "liberation mono",
			"andale mono", "fira code", "fira mono", "inconsolata", "roboto mono", "ubuntu mono", "jetbrains mono",
			"noto sans mono", "ocr a extended", "letter gothic", "pt mono", "sf mono", "ibm plex mono"));

	final WordprocessingMLPackage pkg;
	final TextOptions options;
	final TextView view;
	/** What a view removes and joins; null in {@code markup}, and in {@link Mode#RESOLVED} and {@link Mode#PLANLESS}. */
	final ResolvedView plan;
	final Mode mode;
	final PropertyResolver propertyResolver;

	final List<TextWarning> warnings = new ArrayList<TextWarning>();
	private final Set<TextWarning> warned = new LinkedHashSet<TextWarning>();

	/** Notes referenced in the view, in reference order: id to number, 0 for a note with a custom mark. */
	final Map<String, Integer> footnotes = new LinkedHashMap<String, Integer>();
	final Map<String, Integer> endnotes = new LinkedHashMap<String, Integer>();
	private int footnoteCount, endnoteCount;

	/** What every address this extraction makes starts with: empty, or an embedded package's or chunk's address and a slash. */
	String root = "";
	/** True for a reading one level down (an embedded package's, an {@code altChunk}'s): none of its own are expanded (REQ-512, REQ-513). */
	boolean nested;
	/**
	 * The inspector's record (CR-050): each source object (a paragraph, a table, a WordArt shape, a
	 * chart part, a workbook cell, a slide's shape) to the address of the first block made from it;
	 * null when no inspection asks.
	 */
	Map<Object, String> sources;
	/** The packages an inspection loaded one level down, by the part holding each: read here as they are. */
	Map<Part, org.docx4j.openpackaging.packages.OpcPackage> packages;
	/**
	 * An inspection's reading (CR-053 phase 3): no importer is called; an HTML, XHTML or MHT chunk is read
	 * from the inspector's import of it, by its part, and not at all where there is none. Null otherwise.
	 */
	Map<Part, WordprocessingMLPackage> chunks;
	private BoundValues boundValues;
	private FieldValues fieldValues;
	/** The {@code w:lastRenderedPageBreak}s of the main story so far (REQ-708). */
	private int pageMarkers;
	/** Bookmarks open in the walk, by id: their name, and their text so far (REQ-506's REF). */
	private final Map<String, Bookmark> openBookmarks = new LinkedHashMap<String, Bookmark>();

	private final NumberingStates numberingStates = new NumberingStates();
	/** The run properties a paragraph style and character style give, by their ids. */
	private final Map<String, RPr> styleRPr = new HashMap<String, RPr>();

	/** How the view is read. */
	enum Mode {
		/** The view's rules: a plan for accepted and original, revisions marked for markup. */
		VIEW,
		/** A package already resolved by Accept All or Reject All (the copy route, REQ-406): no view logic. */
		RESOLVED,
		/** Content with no package to plan over: run-level revisions only, no joins or moves. */
		PLANLESS
	}

	Extractor(WordprocessingMLPackage pkg, TextOptions options, Mode mode) {
		this.pkg = pkg;
		this.options = options;
		this.view = options.getView();
		this.mode = pkg == null && mode == Mode.VIEW ? Mode.PLANLESS : mode;
		if (this.mode == Mode.VIEW && view != TextView.MARKUP) {
			plan = view == TextView.ACCEPTED ? ResolvedView.accepted(pkg) : ResolvedView.original(pkg);
		} else {
			plan = null;
		}
		PropertyResolver pr = null;
		// a package with no styles part has nothing to resolve, and asking for a resolver would
		// add a default styles part to it (REQ-204)
		if (pkg != null && pkg.getMainDocumentPart() != null && pkg.getMainDocumentPart().getStyleDefinitionsPart(false) != null) {
			try {
				pr = pkg.getMainDocumentPart().getPropertyResolver();
			} catch (Exception e) {
				log.warn("No property resolver: hidden text and indents are not resolved through styles (" + e.getMessage() + ")");
			}
		}
		this.propertyResolver = pr;
	}

	// --- the package ---------------------------------------------------------------------------------

	TextResult extract() {
		MainDocumentPart main = pkg.getMainDocumentPart();
		List<TextBlock> out = new ArrayList<TextBlock>();
		Map<String, TextComment> comments = new LinkedHashMap<String, TextComment>();
		Map<String, Object> metadata = new LinkedHashMap<String, Object>();
		metadata.put(TextResult.FIELDS_UPDATE_ON_OPEN, fieldsUpdateOnOpen());
		if (!nested) Metadata.read(pkg, metadata);
		if (main == null || main.getJaxbElement() == null || main.getJaxbElement().getBody() == null) {
			return new TextResult(out, comments, warnings, metadata, options);
		}
		Body body = main.getJaxbElement().getBody();
		if (options.getFields() == TextOptions.Fields.UPDATE && mode != Mode.RESOLVED) firstReading();

		// the main story, each text box after its anchor; sections recorded as they end
		Story story = new Story(TextBlock.Story.MAIN, root + "body", main);
		story.sections = new ArrayList<SectPr>();
		List<TextBlock> mainBlocks = new ArrayList<TextBlock>();
		blocks(body.getContent(), root + "body", story, mainBlocks);
		story.sections.add(body.getSectPr());

		boolean headers = options.getInclude().contains(TextOptions.Include.HEADERS);
		boolean footers = options.getInclude().contains(TextOptions.Include.FOOTERS);
		if (headers || footers) {
			sectionsWithHeaders(main, story.sections, mainBlocks, headers, footers, out);
		} else {
			out.addAll(mainBlocks);
		}

		// the notes, in reference order (REQ-501, REQ-503)
		if (options.isNotes()) {
			notes(main.getFootnotesPart() == null ? null : main.getFootnotesPart().getJaxbElement().getFootnote(),
					footnotes, TextRun.FOOTNOTE, main.getFootnotesPart(), out);
			notes(main.getEndNotesPart() == null ? null : main.getEndNotesPart().getJaxbElement().getEndnote(),
					endnotes, TextRun.ENDNOTE, main.getEndNotesPart(), out);
		}

		// the glossary, last (REQ-501, REQ-516)
		if (options.getInclude().contains(TextOptions.Include.GLOSSARY)) glossary(main.getGlossaryDocumentPart(), out);

		// comments, rendered at their references (REQ-504)
		if (commentsIncluded()) comments(main, comments);

		return new TextResult(out, comments, warnings, metadata, options);
	}

	/**
	 * Under {@code fields: update}, the bookmarks' text for REF (REQ-506), from a reading of the
	 * document with its cached results, since a REF may come before its bookmark.
	 */
	private void firstReading() {
		TextOptions o = options.copy();
		o.fields(TextOptions.Fields.CACHED);
		o.embedded(false);
		o.altchunks(false);
		Extractor first = new Extractor(pkg, o, mode);
		first.nested = true;
		first.collectBookmarks = true;
		first.extract();
		fieldValues().bookmarks.putAll(first.bookmarkTexts);
	}

	/** Whether this reading keeps its bookmarks' text (the first reading under {@code fields: update}). */
	private boolean collectBookmarks;
	private final Map<String, String> bookmarkTexts = new HashMap<String, String>();

	private FieldValues fieldValues() {
		if (fieldValues == null) fieldValues = new FieldValues(pkg);
		return fieldValues;
	}

	private BoundValues boundValues() {
		if (boundValues == null) boundValues = new BoundValues(pkg);
		return boundValues;
	}

	// --- the glossary (REQ-516) ---------------------------------------------------------------------

	private void glossary(GlossaryDocumentPart part, List<TextBlock> out) {
		if (part == null || part.getJaxbElement() == null || part.getJaxbElement().getDocParts() == null) return;
		List<CTDocPart> parts = part.getJaxbElement().getDocParts().getDocPart();
		for (int i = 0; i < parts.size(); i++) {
			Body body = parts.get(i).getDocPartBody();
			if (body == null) continue;
			String prefix = root + "glossary:" + i;
			blocks(body.getContent(), prefix, new Story(TextBlock.Story.GLOSSARY, prefix, part), out);
		}
	}

	// --- one level down (REQ-512, REQ-513) -----------------------------------------------------------

	/**
	 * A package read one level down, an embedded document's or a chunk's: the same options, but none
	 * of its own embedded packages or chunks expanded (its chunks raising {@code altchunk.not_expanded}),
	 * and no metadata. Its addresses start {@code prefix/}, its warnings join this result's, and its
	 * main story's blocks take {@code story}.
	 */
	List<TextBlock> nested(WordprocessingMLPackage nestedPkg, String prefix, TextBlock.Story story) {
		TextOptions o = options.copy();
		o.embedded(false);
		o.altchunks(false);
		o.metadata(false);
		Extractor x = new Extractor(nestedPkg, o, Mode.VIEW);
		x.root = prefix + "/";
		x.nested = true;
		x.sources = sources;
		x.packages = packages;
		x.chunks = chunks;
		TextResult r = x.extract();
		for (TextWarning w : r.warnings()) warn(w);
		List<TextBlock> blocks = new ArrayList<TextBlock>(r.blocks());
		if (story != null) restory(blocks, story);
		return blocks;
	}

	private static void restory(List<TextBlock> blocks, TextBlock.Story story) {
		for (TextBlock b : blocks) {
			if (b.story == TextBlock.Story.MAIN) b.story = story;
			for (TextRow r : b.rows) for (TextCell c : r.cells) restory(c.blocks, story);
		}
	}

	/** A part's or object's content as one story, for {@link Texts#of(Object, TextOptions)}. */
	TextResult extract(Object content, TextBlock.Story kind, String prefix, Part part) {
		List<TextBlock> out = new ArrayList<TextBlock>();
		Story story = new Story(kind, prefix, part);
		Object v = unwrap(content);
		List<Object> items = blockItems(v);
		if (items != null && !(v instanceof P)) {
			blocks(items, prefix, story, out);
		} else {
			List<Object> one = new ArrayList<Object>();
			one.add(content);
			blocks(one, prefix, story, out);
		}
		Map<String, Object> metadata = new LinkedHashMap<String, Object>();
		if (pkg != null) metadata.put("fields_update_on_open", fieldsUpdateOnOpen());
		return new TextResult(out, new LinkedHashMap<String, TextComment>(), warnings, metadata, options);
	}

	private boolean fieldsUpdateOnOpen() {
		if (pkg == null || pkg.getMainDocumentPart() == null) return false;
		DocumentSettingsPart settings = pkg.getMainDocumentPart().getDocumentSettingsPart();
		if (settings == null || settings.getJaxbElement() == null) return false;
		org.docx4j.wml.BooleanDefaultTrue update = settings.getJaxbElement().getUpdateFields();
		return update != null && update.isVal();
	}

	boolean commentsIncluded() {
		return view == TextView.MARKUP || options.isComments();
	}

	// --- stories -------------------------------------------------------------------------------------

	/** What the walk of one story carries. */
	final class Story {
		final TextBlock.Story kind;
		final String prefix;
		final Part part;
		/** Open complex fields, outermost first; a field may span paragraphs. */
		final Deque<FieldState> fields = new ArrayDeque<FieldState>();
		/** What was anchored since the last flush (text boxes, charts, SmartArt, embedded packages), to follow its anchor (or its table). */
		final List<Anchored> pending = new ArrayList<Anchored>();
		int tableDepth;
		/** The sections ended so far (the main story only). */
		List<SectPr> sections;
		String noteType;
		int noteNumber;
		String noteId;
		NumberingState numbering;

		Story(TextBlock.Story kind, String prefix, Part part) {
			this.kind = kind;
			this.prefix = prefix;
			this.part = part;
		}

		NumberingState numbering() {
			if (numbering == null) numbering = part == null ? numberingStates.main() : numberingStates.forPart(part);
			return numbering;
		}
	}

	/**
	 * What follows its anchoring paragraph, or the table its anchor is in (REQ-511, REQ-512,
	 * REQ-518), in anchor order: a text box, legacy WordArt, a chart, SmartArt, an embedded package.
	 */
	abstract static class Anchored {
		final String prefix;
		Anchored(String prefix) {
			this.prefix = prefix;
		}
		/** The address of the block that anchors it. */
		String anchor() {
			return prefix.substring(0, prefix.lastIndexOf('/'));
		}
		/** Its blocks, in the story that anchors it. */
		abstract void emit(Extractor x, Story anchoring, List<TextBlock> sink);
	}

	/** A text box: its content, read as a story of its own. */
	static final class Box extends Anchored {
		final CTTxbxContent content;
		Box(CTTxbxContent content, String prefix) {
			super(prefix);
			this.content = content;
		}
		@Override void emit(Extractor x, Story anchoring, List<TextBlock> sink) {
			Story inner = x.new Story(TextBlock.Story.TEXT_BOX, prefix, anchoring.part);
			inner.noteType = anchoring.noteType;
			inner.noteNumber = anchoring.noteNumber;
			inner.noteId = anchoring.noteId;
			x.blocks(content.getContent(), prefix, inner, sink);
		}
	}

	/** Legacy VML WordArt: a one-paragraph text box holding its {@code v:textpath/@string} (REQ-511). */
	static final class WordArt extends Anchored {
		final String text;
		final Object shape;
		WordArt(String text, Object shape, String prefix) {
			super(prefix);
			this.text = text;
			this.shape = shape;
		}
		@Override void emit(Extractor x, Story anchoring, List<TextBlock> sink) {
			x.record(shape, prefix + "/0");
			sink.add(Embedded.paragraph(TextBlock.Story.TEXT_BOX, prefix + "/0", text));
		}
	}

	/** A chart: its description, title and {@link ChartTable} (REQ-511; CR-047). */
	static final class Chart extends Anchored {
		final ChartRef ref;
		Chart(ChartRef ref, String prefix) {
			super(prefix);
			this.ref = ref;
		}
		@Override void emit(Extractor x, Story anchoring, List<TextBlock> sink) {
			ChartDescription d;
			try {
				d = ref.isChartEx() ? ChartDescription.of(ref.readChartEx()) : ChartDescription.of(ref.read());
			} catch (Exception e) {
				x.notRead(anchor(), ref.getChartPart(), "the chart", e);
				return;
			}
			ChartTable t = d.getTable();
			TextBlock b = new TextBlock(TextBlock.Kind.CHART, anchoring.kind, prefix);
			b.chart = d;
			b.chartTitle = t.getTitle();
			b.chartHeader = new ArrayList<String>(t.getHeader());
			List<List<String>> rows = new ArrayList<List<String>>();
			for (List<String> r : t.getRows()) rows.add(new ArrayList<String>(r));
			b.chartRows = rows;
			x.record(ref.getChartPart(), prefix);
			sink.add(b);
		}
	}

	/** SmartArt: its data part's text in reading order, a line a paragraph (REQ-511). */
	static final class SmartArt extends Anchored {
		final DiagramDataPart part;
		SmartArt(DiagramDataPart part, String prefix) {
			super(prefix);
			this.part = part;
		}
		@Override void emit(Extractor x, Story anchoring, List<TextBlock> sink) {
			List<String> lines;
			Map<Object, Integer> starts = x.sources == null ? null : new IdentityHashMap<Object, Integer>();
			try {
				org.docx4j.dml.diagram.CTDataModel model = part.getJaxbElement();
				if (model == null) throw new IllegalStateException("no data model");
				lines = Drawings.smartArt(model, starts);
			} catch (RuntimeException e) {
				x.notRead(anchor(), part, "the SmartArt data part", e);
				return;
			}
			if (starts != null) for (Map.Entry<Object, Integer> e : starts.entrySet()) x.record(e.getKey(), prefix + "/" + e.getValue());
			for (int i = 0; i < lines.size(); i++) sink.add(Embedded.paragraph(TextBlock.Story.SMARTART, prefix + "/" + i, lines.get(i)));
		}
	}

	/** An embedded package or OLE 1.0 package, read under {@code embedded: true} (REQ-512). */
	static final class EmbeddedObject extends Anchored {
		final BinaryPart part;
		EmbeddedObject(BinaryPart part, String prefix) {
			super(prefix);
			this.part = part;
		}
		@Override void emit(Extractor x, Story anchoring, List<TextBlock> sink) {
			List<TextBlock> blocks;
			try {
				byte[] bytes = part.getBytes();
				blocks = part instanceof EmbeddedPackagePart ? Embedded.pkg(x, part, bytes, prefix) : Embedded.ole10Native(bytes, prefix);
			} catch (Exception e) {
				x.notRead(anchor(), part, "the embedded object", e);
				return;
			}
			sink.addAll(blocks);
		}
	}

	/** A bookmark open in the walk. */
	static final class Bookmark {
		final String name;
		final StringBuilder text = new StringBuilder();
		boolean paragraphEnded;
		Bookmark(String name) {
			this.name = name;
		}
	}

	/** An open complex field. */
	static final class FieldState {
		final StringBuilder instr = new StringBuilder();
		final StringBuilder result = new StringBuilder();
		boolean separated;
		/** {@code w:fldLock} on its beginning: never recomputed (REQ-506). */
		boolean locked;
		/** Its result computed under {@code fields: update} (REQ-506): the cached result's text is not read. */
		String computed;
		/** A form field's data, on its beginning: a FORMTEXT's default text (REQ-506). */
		CTFFData ffData;
		/** A HYPERLINK field's target, once its code is read: its result's text is in the link (REQ-703). */
		String link;
		/** A table of contents under {@code toc: false} (D12): nothing of it is read. */
		boolean toc;
		/** Where its code's runs begin, for a table of contents' to be taken out again. */
		TextBlock codeBlock;
		int codeStart;
		/** The paragraph whose merge floor its code raised, and the floor before: put back once the code is taken out. */
		Paragraph floorParagraph;
		int previousFloor;
	}

	/** The state of a run-level walk: the revision and the control the runs are in. */
	static final class RunState {
		/** markup: the innermost revision; else null. */
		final TextRevision revision;
		/** accepted, original: inside a revision holder the view keeps (its runs are text whatever their elements). */
		final boolean kept;
		final boolean placeholder;
		RunState(TextRevision revision, boolean kept, boolean placeholder) {
			this.revision = revision;
			this.kept = kept;
			this.placeholder = placeholder;
		}
		RunState withRevision(TextRevision r, boolean k) { return new RunState(r, k, placeholder); }
		RunState withPlaceholder() { return new RunState(revision, kept, true); }
	}

	private static final RunState PLAIN = new RunState(null, false, false);

	// --- block level ---------------------------------------------------------------------------------

	/** Block-level content into {@code sink}: paragraphs (joined in a view), tables, and what is transparent. */
	void blocks(List<Object> items, String prefix, Story story, List<TextBlock> sink) {
		blocks(items, prefix, story, sink, PLAIN);
	}

	private void blocks(List<Object> items, String prefix, Story story, List<TextBlock> sink, RunState state) {
		if (items == null) return;
		resolvedAlternateContent(items, prefix, story);
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			if (removes(item)) continue;
			Object v = unwrap(item);
			String address = prefix + "/" + i;
			if (v instanceof P) {
				List<P> chain = new ArrayList<P>();
				List<String> addresses = new ArrayList<String>();
				chain.add((P) v);
				addresses.add(address);
				int j = i;
				while (joinsNext(chain.get(chain.size() - 1))) {
					int k = nextParagraph(items, j);
					if (k < 0) break; // no paragraph next: only the mark goes
					chain.add((P) unwrap(items.get(k)));
					addresses.add(prefix + "/" + k);
					j = k;
				}
				paragraph(chain, addresses, story, sink, state);
				i = j;
			} else if (v instanceof Tbl) {
				table((Tbl) v, address, story, sink, state);
			} else if (v instanceof SdtElement && unwraps(item)) {
				// an inserted control rejected: its content in its place, its properties gone
				SdtElement sdt = (SdtElement) v;
				if (sdt.getSdtContent() != null) blocks(sdt.getSdtContent().getContent(), address, story, sink, state);
			} else if (v instanceof SdtElement) {
				SdtElement sdt = (SdtElement) v;
				String bound = bound(sdt, address);
				if (bound != null) {
					// a plain-text control bound at block level: its bound value as one paragraph (REQ-509)
					TextBlock block = Embedded.paragraph(story.kind, address, bound);
					block.section = story.sections == null ? 0 : story.sections.size();
					record(sdt, address);
					sink.add(block);
					flushIfOutsideTable(story, sink);
					continue;
				}
				boolean placeholder = showingPlaceholder(sdt.getSdtPr());
				if (placeholder && !options.isPlaceholders()) continue;
				if (sdt.getSdtContent() != null) blocks(sdt.getSdtContent().getContent(), address, story, sink,
						placeholder ? state.withPlaceholder() : state);
			} else if (v instanceof CTAltChunk) {
				altChunk((CTAltChunk) v, address, story, sink);
			} else if (isBookmarkMarker(item)) {
				bookmark(item);
			} else if (v instanceof AlternateContent) {
				AlternateContent ac = (AlternateContent) v;
				compareBranches(ac, address);
				blocks(McSelection.select(McPurpose.TEXT, ac), address, story, sink, state);
			} else if (revisionHolder(item) != null) {
				blocks(runItemsOf(v), address, story, sink, holderState(item, state));
			} else {
				List<Object> children = blockItems(v);
				if (children != null) blocks(children, address, story, sink, state);
			}
		}
	}

	/** The index of the paragraph a paragraph joins: the next in the list past markers and what the view removes, or -1. */
	private int nextParagraph(List<Object> items, int from) {
		for (int k = from + 1; k < items.size(); k++) {
			Object item = items.get(k);
			if (isMarker(item) || removes(item)) continue;
			return unwrap(item) instanceof P ? k : -1;
		}
		return -1;
	}

	private void paragraph(List<P> chain, List<String> addresses, Story story, List<TextBlock> sink, RunState state) {
		P first = chain.get(0);
		P last = chain.get(chain.size() - 1);
		TextBlock block = new TextBlock(TextBlock.Kind.PARAGRAPH, story.kind, addresses.get(0));
		block.paraId = first.getParaId();
		// a joined paragraph keeps the last one's properties (CR-043's join)
		PPr pPr = last.getPPr();
		block.style = pPr != null && pPr.getPStyle() != null ? pPr.getPStyle().getVal() : null;
		block.noteType = story.noteType;
		block.noteNumber = story.noteNumber;
		block.noteId = story.noteId;
		Boolean monospace = null;
		boolean toc = false;
		for (int c = 0; c < chain.size(); c++) {
			P p = chain.get(c);
			block.revisions += markRevisions(p);
			Paragraph para = new Paragraph(block, p, addresses.get(c), story);
			boolean startsInToc = inToc(story);
			runs(p.getContent(), para, state);
			if (startsInToc || para.toc || inToc(story)) toc = true;
			if (para.monospace != null) monospace = monospace == null ? para.monospace : monospace && para.monospace;
		}
		for (Bookmark b : openBookmarks.values()) b.paragraphEnded = true;
		block.monospace = Boolean.TRUE.equals(monospace);
		properties(block, last);
		boolean markHidden = isHiddenMark(last.getPPr());
		boolean anyVisible = false;
		for (TextRun r : block.runs) if (!r.hidden) anyVisible = true;
		boolean hiddenParagraph = markHidden && !anyVisible;
		if (hiddenParagraph && !options.isHidden()) {
			// runs and mark hidden: no line at all (REQ-507)
			block.runs.clear();
			flushIfOutsideTable(story, sink);
			endSection(last, story);
			return;
		}
		block.hidden = hiddenParagraph;
		if (!options.isHidden()) {
			for (int k = block.runs.size() - 1; k >= 0; k--) if (block.runs.get(k).hidden) block.runs.remove(k);
		}
		if (toc && block.runs.isEmpty()) {
			// a table of contents' paragraph, under toc: false (D12): no line at all
			flushIfOutsideTable(story, sink);
			endSection(last, story);
			return;
		}
		// one count per paragraph: the label (list_labels) and the numbering's value come from it
		if (pkg != null && (options.isListLabels() || block.numbering != null)) {
			Emulator.NumberingResult counted = count(last, story);
			if (options.isListLabels()) block.listLabel = listLabel(counted);
			if (block.numbering != null && counted != null) block.numbering.value = counted.getValue();
		}
		block.section = story.sections == null ? 0 : story.sections.size();
		for (P p : chain) record(p, block.address);
		sink.add(block);
		flushIfOutsideTable(story, sink);
		endSection(last, story);
	}

	/** The inspector's record: the first block made from a source object (CR-050). */
	void record(Object source, String address) {
		if (sources != null && source != null) sources.putIfAbsent(source, address);
	}

	private void endSection(P last, Story story) {
		if (story.sections != null && story.tableDepth == 0 && last.getPPr() != null && last.getPPr().getSectPr() != null) {
			story.sections.add(last.getPPr().getSectPr());
		}
	}

	private void flushIfOutsideTable(Story story, List<TextBlock> sink) {
		if (story.tableDepth > 0 || story.pending.isEmpty()) return;
		List<Anchored> items = new ArrayList<Anchored>(story.pending);
		story.pending.clear();
		for (Anchored item : items) {
			List<TextBlock> blocks = new ArrayList<TextBlock>();
			item.emit(this, story, blocks);
			int section = story.sections == null ? 0 : story.sections.size();
			for (TextBlock b : blocks) b.section = section;
			sink.addAll(blocks);
		}
	}

	// --- altChunk (REQ-513) ----------------------------------------------------------------------------

	private void altChunk(CTAltChunk chunk, String address, Story story, List<TextBlock> sink) {
		Part part = story.part == null || chunk.getId() == null || story.part.getRelationshipsPart(false) == null ? null
				: story.part.getRelationshipsPart(false).getPart(chunk.getId());
		if (!(part instanceof AlternativeFormatInputPart)) return;
		AlternativeFormatInputPart afip = (AlternativeFormatInputPart) part;
		String contentType = AltChunks.contentType(afip);
		List<TextBlock> blocks = options.isAltchunks() ? AltChunks.expand(this, afip, story.kind, address + "/altchunk:0") : null;
		if (blocks == null) {
			warn(TextWarning.ALTCHUNK_NOT_EXPANDED, address, null, contentType);
			return;
		}
		int section = story.sections == null ? 0 : story.sections.size();
		for (TextBlock b : blocks) b.section = section;
		sink.addAll(blocks);
	}

	// --- bound values (REQ-509) ------------------------------------------------------------------------

	/**
	 * A plain-text control's bound value where its binding resolves and is what Word shows: not where
	 * the value is empty and the control shows its placeholder, nor under {@code bindings: surface}.
	 * Null to read the surface. Raises {@code sdt.binding_differs} where the two differ.
	 */
	private String bound(SdtElement sdt, String address) {
		SdtPr pr = sdt.getSdtPr();
		if (pkg == null || pr == null || !isPlainText(pr)) return null;
		CTDataBinding binding = pr.getDataBinding();
		if (binding == null) return null;
		String value = boundValues().value(binding);
		if (value == null) return null;
		boolean placeholder = showingPlaceholder(pr);
		if (value.isEmpty() && placeholder) return null;
		String surface = placeholder ? null : surfaceText(sdt);
		if (!value.equals(surface)) warn(TextWarning.SDT_BINDING_DIFFERS, address, null);
		return options.getBindings() == TextOptions.Bindings.SURFACE ? null : value;
	}

	private static boolean isPlainText(SdtPr pr) {
		for (Object o : pr.getRPrOrAliasOrLock()) {
			if (o instanceof JAXBElement && "text".equals(((JAXBElement<?>) o).getName().getLocalPart())) return true;
		}
		return false;
	}

	/** A control's surface in the view: its {@code w:t}, tabs and breaks, what the view removes left out. */
	private String surfaceText(SdtElement sdt) {
		StringBuilder sb = new StringBuilder();
		if (sdt.getSdtContent() == null) return "";
		for (Object o : sdt.getSdtContent().getContent()) {
			if (removes(o)) continue;
			walk(unwrap(o), 0, new Visitor() {
				@Override boolean item(Object item) {
					if (removes(item)) return false;
					String name = name(item);
					Object v = unwrap(item);
					if ("t".equals(name) && v instanceof Text) sb.append(((Text) v).getValue());
					else if ("tab".equals(name)) sb.append('\t');
					else if ("br".equals(name) || "cr".equals(name)) sb.append('\n');
					return !"instrText".equals(name);
				}
			});
		}
		return sb.toString();
	}

	// --- bookmarks, for REF under fields: update (REQ-506) ----------------------------------------------

	private static boolean isBookmarkMarker(Object item) {
		String name = name(item);
		return "bookmarkStart".equals(name) || "bookmarkEnd".equals(name);
	}

	private void bookmark(Object item) {
		if (options.getFields() != TextOptions.Fields.UPDATE && !collectBookmarks) return;
		Object v = unwrap(item);
		if (v instanceof CTBookmark && "bookmarkStart".equals(name(item))) {
			CTBookmark b = (CTBookmark) v;
			if (b.getId() != null && b.getName() != null) openBookmarks.put(b.getId().toString(), new Bookmark(b.getName()));
		} else if (v instanceof CTMarkupRange && ((CTMarkupRange) v).getId() != null) {
			Bookmark b = openBookmarks.remove(((CTMarkupRange) v).getId().toString());
			if (b != null) {
				String key = b.name.toLowerCase(Locale.ROOT);
				if (collectBookmarks) bookmarkTexts.put(key, b.text.toString());
				// for a later REF, on every route (a part's or content's has no first reading)
				if (options.getFields() == TextOptions.Fields.UPDATE && pkg != null) fieldValues().bookmarks.put(key, b.text.toString());
			}
		}
	}

	private int markRevisions(P p) {
		int n = 0;
		if (p.getPPr() != null && p.getPPr().getRPr() != null) {
			if (p.getPPr().getRPr().getIns() != null) n++;
			if (p.getPPr().getRPr().getDel() != null) n++;
			if (p.getPPr().getRPr().getMoveFrom() != null) n++;
			if (p.getPPr().getRPr().getMoveTo() != null) n++;
		}
		return n;
	}

	private void properties(TextBlock block, P p) {
		PPr own = p.getPPr();
		block.bottomBorder = own != null && own.getPBdr() != null && own.getPBdr().getBottom() != null;
		PPr effective = own;
		if (propertyResolver != null) {
			try {
				effective = propertyResolver.getEffectivePPr(own);
			} catch (Exception e) {
				log.debug("Couldn't resolve paragraph properties of " + block.address + ": " + e.getMessage());
			}
		}
		if (effective == null) return;
		PPrBase.Ind ind = effective.getInd();
		if (ind != null) {
			block.indentLeft = twips(ind.getLeft() != null ? ind.getLeft() : ind.getStart());
			block.indentHanging = twips(ind.getHanging());
			block.indentFirstLine = twips(ind.getFirstLine());
		}
		Jc jc = effective.getJc();
		if (jc != null && jc.getVal() != null) block.alignment = jc.getVal().value();
		if (effective.getOutlineLvl() != null && effective.getOutlineLvl().getVal() != null) {
			int level = effective.getOutlineLvl().getVal().intValue();
			if (level >= 0 && level <= 8) block.outlineLevel = level; // 9 is body text
		}
		if (effective.getContextualSpacing() != null) block.contextualSpacing = effective.getContextualSpacing().isVal();
		boolean directNumId = own != null && own.getNumPr() != null && own.getNumPr().getNumId() != null;
		block.numbering = numbering(effective.getNumPr(), directNumId, own != null && own.getPStyle() != null ? own.getPStyle().getVal() : null);
	}

	/**
	 * A paragraph's numbering from its effective {@code w:numPr} and the numbering definitions; null for
	 * none, for {@code w:numId} 0, and where Word gives the paragraph no number: a {@code w:numId} its
	 * style brought, whose level belongs to another paragraph style (the one its {@code w:lvl/w:pStyle}
	 * names, else the first in styles.xml carrying it; not the paragraph's own or one it is based on), the
	 * list labels' rule ({@code Emulator}, word-layout-rules section 2.8).
	 */
	private TextNumbering numbering(PPrBase.NumPr numPr, boolean directNumId, String pStyle) {
		if (numPr == null || numPr.getNumId() == null || numPr.getNumId().getVal() == null) return null;
		BigInteger numId = numPr.getNumId().getVal();
		if (BigInteger.ZERO.equals(numId)) return null;
		int ilvl = numPr.getIlvl() != null && numPr.getIlvl().getVal() != null ? numPr.getIlvl().getVal().intValue() : 0;
		String format = null, text = null, abstractNumId = null;
		int start = 1;
		try {
			org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp = pkg == null || pkg.getMainDocumentPart() == null
					? null : pkg.getMainDocumentPart().getNumberingDefinitionsPart();
			if (ndp != null && !directNumId && linkedElsewhere(ndp, numId.toString(), Integer.toString(ilvl), pStyle)) return null;
			org.docx4j.model.listnumbering.ListNumberingDefinition lnd = ndp == null ? null : ndp.getInstanceListDefinitions().get(numId.toString());
			if (lnd != null && lnd.getAbstractListDefinition() != null) abstractNumId = lnd.getAbstractListDefinition().getID();
			// the instance's level, its w:lvlOverride (a start override, an overriding w:lvl) applied
			org.docx4j.model.listnumbering.ListLevel level = lnd == null ? null : lnd.getLevel(ilvl);
			if (level == null && lnd != null && lnd.getAbstractListDefinition() != null) {
				level = lnd.getAbstractListDefinition().getListLevels().get(Integer.toString(ilvl));
			}
			if (level != null) {
				format = level.getNumFmt() == null ? (level.isBullet() ? "bullet" : null) : level.getNumFmt().value();
				text = level.getLevelText();
				// ListLevel's start value is w:start less one, a counter's
				if (level.getStartValue() != null) start = level.getStartValue().intValue() + 1;
			}
		} catch (RuntimeException e) {
			log.debug("Couldn't read the numbering of " + numId + ": " + e.getMessage());
		}
		TextNumbering numbering = new TextNumbering(numId.toString(), ilvl, format, text, start);
		numbering.abstractNumId = abstractNumId;
		return numbering;
	}

	/** Whether the level belongs to a paragraph style that is neither the paragraph's nor one its style is
	 *  based on: the style its {@code w:lvl/w:pStyle} names, else the first in styles.xml carrying it
	 *  ({@link Emulator#levelStyleId}). */
	private boolean linkedElsewhere(org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp,
			String numId, String ilvl, String pStyle) {
		String linked = Emulator.levelStyleId(ndp, numId, ilvl);
		if (linked == null || linked.isEmpty()) return false;
		// a paragraph naming no style is in the default paragraph style, which can own a level
		if (pStyle == null && propertyResolver != null) pStyle = propertyResolver.getDefaultParagraphStyleId();
		java.util.Set<String> seen = new java.util.HashSet<String>();
		for (String id = pStyle; id != null && seen.add(id); ) {
			if (linked.equals(id)) return false;
			org.docx4j.wml.Style s = propertyResolver == null ? null : propertyResolver.getStyle(id);
			id = s == null || s.getBasedOn() == null ? null : s.getBasedOn().getVal();
		}
		return true;
	}

	private static Integer twips(BigInteger value) {
		return value == null ? null : value.intValue();
	}

	/** The paragraph counted in its story's numbering state; null where it is not numbered. */
	private Emulator.NumberingResult count(P p, Story story) {
		try {
			return Emulator.getNumber(pkg, p.getPPr(), story.numbering());
		} catch (RuntimeException e) {
			log.debug("Couldn't count a paragraph's numbering: " + e.getMessage());
			return null;
		}
	}

	private static String listLabel(Emulator.NumberingResult triple) {
		try {
			if (triple == null) return null;
			String label = triple.getNumString();
			if (label == null || label.isEmpty()) label = triple.getBullet();
			if (label == null) return null;
			String suffix = "\t";
			if (triple.getLvl() != null && triple.getLvl().getSuff() != null && triple.getLvl().getSuff().getVal() != null) {
				String s = triple.getLvl().getSuff().getVal();
				if ("space".equals(s)) suffix = " ";
				else if ("nothing".equals(s)) suffix = "";
			}
			return label + suffix;
		} catch (RuntimeException e) {
			log.debug("No list label for a paragraph: " + e.getMessage());
			return null;
		}
	}

	// --- tables --------------------------------------------------------------------------------------

	private void table(Tbl tbl, String address, Story story, List<TextBlock> sink, RunState state) {
		TextBlock block = new TextBlock(TextBlock.Kind.TABLE, story.kind, address);
		block.section = story.sections == null ? 0 : story.sections.size();
		story.tableDepth++;
		try {
			rows(tbl.getContent(), address, story, block, state);
		} finally {
			story.tableDepth--;
		}
		if (!block.rows.isEmpty()) {
			record(tbl, address);
			sink.add(block); // a table left with no rows goes (CR-043)
		}
		flushIfOutsideTable(story, sink);
	}

	private void rows(List<Object> items, String prefix, Story story, TextBlock table, RunState state) {
		resolvedAlternateContent(items, prefix, story);
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			if (removes(item)) continue;
			Object v = unwrap(item);
			String address = prefix + "/" + i;
			if (v instanceof AlternateContent) {
				// a row-level element kept whole (CR-051 phase 2): the branch Word reads, compared with the others
				AlternateContent ac = (AlternateContent) v;
				compareBranches(ac, address);
				rows(McSelection.select(McPurpose.TEXT, ac), address, story, table, state);
			} else if (v instanceof Tr) {
				Tr tr = (Tr) v;
				TextRow row = new TextRow(address);
				TrPr trPr = tr.getTrPr();
				RunState rowState = state;
				if (trPr != null) {
					for (JAXBElement<?> e : trPr.getCnfStyleOrDivIdOrGridBefore()) {
						if ("tblHeader".equals(e.getName().getLocalPart())) {
							Object hv = e.getValue();
							row.header = !(hv instanceof org.docx4j.wml.BooleanDefaultTrue) || ((org.docx4j.wml.BooleanDefaultTrue) hv).isVal();
						}
					}
					if (view == TextView.MARKUP && mode == Mode.VIEW) {
						if (trPr.getIns() != null) rowState = state.withRevision(revision(trPr.getIns(), TextRevision.Type.INSERTION), false);
						if (trPr.getDel() != null) rowState = state.withRevision(revision(trPr.getDel(), TextRevision.Type.DELETION), false);
					}
				}
				cells(tr.getContent(), address, story, row, rowState);
				table.rows.add(row);
			} else if (v instanceof SdtElement) {
				SdtElement sdt = (SdtElement) v;
				if (sdt.getSdtContent() != null) rows(sdt.getSdtContent().getContent(), address, story, table, state);
			} else {
				List<Object> children = blockItems(v);
				if (children != null && !(v instanceof Tc)) rows(children, address, story, table, state);
			}
		}
	}

	private void cells(List<Object> items, String prefix, Story story, TextRow row, RunState state) {
		resolvedAlternateContent(items, prefix, story);
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			if (removes(item)) continue;
			Object v = unwrap(item);
			String address = prefix + "/" + i;
			if (v instanceof AlternateContent) {
				// a cell-level element kept whole (CR-051 phase 2): the branch Word reads, compared with the others
				AlternateContent ac = (AlternateContent) v;
				compareBranches(ac, address);
				cells(McSelection.select(McPurpose.TEXT, ac), address, story, row, state);
			} else if (v instanceof Tc) {
				Tc tc = (Tc) v;
				TextCell cell = new TextCell(address);
				TcPr tcPr = tc.getTcPr();
				RunState cellState = state;
				if (tcPr != null) {
					if (tcPr.getGridSpan() != null && tcPr.getGridSpan().getVal() != null) cell.gridSpan = tcPr.getGridSpan().getVal().intValue();
					if (tcPr.getVMerge() != null) {
						if ("restart".equals(tcPr.getVMerge().getVal())) cell.mergeRestart = true;
						else cell.mergeContinue = true;
					}
					if (view == TextView.MARKUP && mode == Mode.VIEW) {
						if (tcPr.getCellIns() != null) cellState = state.withRevision(revision(tcPr.getCellIns(), TextRevision.Type.INSERTION), false);
						if (tcPr.getCellDel() != null) cellState = state.withRevision(revision(tcPr.getCellDel(), TextRevision.Type.DELETION), false);
					}
				}
				blocks(tc.getContent(), address, story, cell.blocks, cellState);
				row.cells.add(cell);
			} else if (v instanceof SdtElement) {
				SdtElement sdt = (SdtElement) v;
				if (sdt.getSdtContent() != null) cells(sdt.getSdtContent().getContent(), address, story, row, state);
			} else {
				List<Object> children = blockItems(v);
				if (children != null) cells(children, address, story, row, state);
			}
		}
	}

	// --- run level -----------------------------------------------------------------------------------

	/** A paragraph being read: the block its runs go to, its address, its text boxes so far. */
	final class Paragraph {
		final TextBlock block;
		final P p;
		final String address;
		final Story story;
		/** The text boxes (WordArt among them), charts, SmartArt and objects anchored in it so far, each counted apart. */
		int boxes, charts, smartArts, objects;
		/** Whether every run with text so far is in a monospaced font; null before the first. */
		Boolean monospace;
		/** The formatting of the run being read (a run carrying only it), or null outside a run. */
		TextRun format;
		/** The target of the {@code w:hyperlink} being read, or null. */
		String link;
		/** An equation being written, and whether it is a display one. */
		boolean math, displayMath;
		/** Whether a table of contents left out (D12) is in the paragraph: one with nothing else in it is no paragraph. */
		boolean toc;
		/** The first of the block's runs a run may be merged into: a field's code starts a run of its own where it may be taken out. */
		int mergeFloor;
		Paragraph(TextBlock block, P p, String address, Story story) {
			this.block = block;
			this.p = p;
			this.address = address;
			this.story = story;
		}
	}

	private void runs(List<Object> items, Paragraph para, RunState state) {
		if (items == null) return;
		for (Object item : items) {
			if (removes(item)) continue;
			Object v = unwrap(item);
			if (revisionHolder(item) != null) {
				para.block.revisions++;
				runs(runItemsOf(v), para, holderState(item, state));
			} else if (v instanceof R) {
				run((R) v, para, state);
			} else if (v instanceof AlternateContent) {
				AlternateContent ac = (AlternateContent) v;
				compareBranches(ac, para.block.address);
				runs(McSelection.select(McPurpose.TEXT, ac), para, state);
			} else if (v instanceof SdtElement && unwraps(item)) {
				SdtElement sdt = (SdtElement) v;
				if (sdt.getSdtContent() != null) runs(sdt.getSdtContent().getContent(), para, state);
			} else if (v instanceof SdtElement) {
				SdtElement sdt = (SdtElement) v;
				String bound = bound(sdt, para.block.address);
				if (bound != null) {
					text(para, bound, false, state);
					continue;
				}
				boolean placeholder = showingPlaceholder(sdt.getSdtPr());
				if (placeholder && !options.isPlaceholders()) continue;
				if (sdt.getSdtContent() != null) runs(sdt.getSdtContent().getContent(), para, placeholder ? state.withPlaceholder() : state);
			} else if (isBookmarkMarker(item)) {
				bookmark(item);
			} else if (v instanceof CTSimpleField) {
				simpleField((CTSimpleField) v, para, state);
			} else if (isMath(v)) {
				math(v, para, state);
			} else if (v instanceof P.Hyperlink) {
				String saved = para.link;
				String target = hyperlinkTarget((P.Hyperlink) v, para.story);
				if (target != null) para.link = target;
				try {
					runs(((P.Hyperlink) v).getContent(), para, state);
				} finally {
					para.link = saved;
				}
			} else {
				List<Object> children = runItemsOf(v);
				if (children != null) runs(children, para, state);
			}
		}
	}

	private RunState holderState(Object item, RunState state) {
		if (view == TextView.MARKUP && mode == Mode.VIEW) {
			CTTrackChange tc = revisionHolder(item);
			boolean ins = item instanceof JAXBElement ? isInsertionName(((JAXBElement<?>) item).getName().getLocalPart())
					: unwrap(item) instanceof RunIns;
			return state.withRevision(revision(tc, ins ? TextRevision.Type.INSERTION : TextRevision.Type.DELETION), false);
		}
		return state.withRevision(null, true);
	}

	private static boolean isInsertionName(String name) {
		return "ins".equals(name) || "moveTo".equals(name);
	}

	private static TextRevision revision(CTTrackChange tc, TextRevision.Type type) {
		String date = null;
		if (tc.getDate() != null) date = tc.getDate().toXMLFormat();
		return new TextRevision(type, tc.getAuthor(), date);
	}

	private void run(R r, Paragraph para, RunState state) {
		boolean hidden = isHiddenRun(r, para.p);
		int before = textLength(para.block);
		TextRun saved = para.format;
		para.format = formatOf(r, para.p);
		try {
			for (Object item : r.getContent()) runItem(item, r, hidden, para, state);
		} finally {
			para.format = saved;
		}
		if (textLength(para.block) > before) {
			boolean mono = isMonospace(runFont(r, para.p));
			para.monospace = para.monospace == null ? mono : para.monospace && mono;
		}
	}

	private static int textLength(TextBlock block) {
		int n = 0;
		for (TextRun run : block.runs) if (run.kind == TextRun.Kind.TEXT) n += run.text.length();
		return n;
	}

	private void runItem(Object item, R r, boolean hidden, Paragraph para, RunState state) {
		Object v = unwrap(item);
		String name = name(item);
		switch (name) {
		case "fldChar": {
			FldChar fc = (FldChar) v;
			STFldCharType type = fc.getFldCharType();
			Deque<FieldState> fields = para.story.fields;
			if (type == STFldCharType.BEGIN) {
				FieldState f = new FieldState();
				f.locked = written(fc, "fldLock");
				f.ffData = fc.getFfData();
				f.codeBlock = para.block;
				f.codeStart = para.block.runs.size();
				if (!options.isToc() && options.isFieldCodes()) {
					f.floorParagraph = para;
					f.previousFloor = para.mergeFloor;
					para.mergeFloor = para.block.runs.size();
				}
				fields.push(f);
			} else if (type == STFldCharType.SEPARATE) {
				if (!fields.isEmpty()) {
					FieldState f = fields.peek();
					f.separated = true;
					f.link = fieldHyperlinkTarget(f.instr.toString());
					if (!options.isToc() && !suppressed(para.story) && isTocField(f.instr.toString())) {
						f.toc = true;
						para.toc = true;
						removeFieldCode(f, para);
						return;
					}
					String value = suppressed(para.story) ? null : update(f);
					if (value != null) {
						text(para, value, hidden, state);
						f.computed = value; // the cached result that follows is not read
					}
				}
			} else if (type == STFldCharType.END) {
				if (!fields.isEmpty()) {
					FieldState f = fields.pop();
					// a field inside a computed field's cached result is not read, nor recorded
					if (suppressed(para.story)) return;
					if (f.toc || !options.isToc() && isTocField(f.instr.toString())) {
						if (!f.toc) removeFieldCode(f, para);
						para.toc = true;
						return;
					}
					if (!f.separated) {
						String value = update(f);
						if (value != null) {
							text(para, value, hidden, state);
							f.computed = value;
						}
					}
					if (f.computed != null) para.block.fields.add(new TextField(f.instr.toString(), f.computed, true));
					else endField(f.instr.toString(), f.separated ? f.result.toString() : null, para);
				}
			}
			return;
		}
		case "instrText":
		case "delInstrText": {
			if ("delInstrText".equals(name) && !state.kept && state.revision == null && view == TextView.ACCEPTED) return;
			String s = v instanceof Text ? ((Text) v).getValue() : null;
			if (s == null) return;
			Deque<FieldState> fields = para.story.fields;
			if (!fields.isEmpty()) fields.peek().instr.append(s);
			if (options.isFieldCodes() && !suppressed(para.story)) addText(para, s, TextRun.Kind.FIELD_CODE, hidden, state);
			return;
		}
		case "t": {
			String s = v instanceof Text ? ((Text) v).getValue() : null;
			text(para, s, hidden, state);
			return;
		}
		case "delText": {
			// the holder decides; outside one, the element: no text after Accept All (REQ-404)
			if (!state.kept && state.revision == null) {
				if (view == TextView.ACCEPTED) return;
				if (view == TextView.MARKUP && mode == Mode.VIEW) {
					state = state.withRevision(new TextRevision(TextRevision.Type.DELETION, null, null), false);
				}
			}
			String s = v instanceof DelText ? ((DelText) v).getValue() : v instanceof Text ? ((Text) v).getValue() : null;
			text(para, s, hidden, state);
			return;
		}
		case "tab":
		case "ptab":
			text(para, "\t", hidden, state);
			return;
		case "br":
		case "cr":
			text(para, "\n", hidden, state);
			return;
		case "noBreakHyphen":
			text(para, "‑", hidden, state);
			return;
		case "softHyphen":
			text(para, "­", hidden, state);
			return;
		case "sym": {
			if (v instanceof R.Sym) {
				String code = ((R.Sym) v).getChar();
				if (code != null && code.matches("[0-9A-Fa-f]{1,6}")) {
					int cp = Integer.parseInt(code, 16);
					if (Character.isValidCodePoint(cp)) text(para, new String(Character.toChars(cp)), hidden, state);
				}
			}
			return;
		}
		case "footnoteReference":
		case "endnoteReference": {
			// in a computed field's cached result, a copy of a reference: neither written nor numbered
			if (inCode(para.story) || suppressed(para.story)) return;
			boolean foot = "footnoteReference".equals(name);
			CTFtnEdnRef ref = (CTFtnEdnRef) v;
			String id = ref.getId() == null ? "" : ref.getId().toString();
			TextRun run = new TextRun(TextRun.Kind.NOTE_REFERENCE, "", state.revision);
			run.hidden = hidden;
			run.noteType = foot ? TextRun.FOOTNOTE : TextRun.ENDNOTE;
			run.noteId = id;
			if (!(hidden && !options.isHidden())) {
				// a reference carrying a custom mark is given no number (REQ-503); its note is still read
				Map<String, Integer> numbers = foot ? footnotes : endnotes;
				Integer n = numbers.get(id);
				if (n == null) {
					n = customMarkFollows(ref) ? 0 : foot ? ++footnoteCount : ++endnoteCount;
					numbers.put(id, n);
				}
				run.noteNumber = n;
			}
			para.block.runs.add(run);
			return;
		}
		case "footnoteRef":
		case "endnoteRef": {
			if (para.story.noteType == null) return;
			TextRun run = new TextRun(TextRun.Kind.NOTE_MARK, "", state.revision);
			run.noteType = para.story.noteType;
			run.noteNumber = para.story.noteNumber;
			run.noteId = para.story.noteId;
			para.block.runs.add(run);
			return;
		}
		case "commentReference": {
			if (!commentsIncluded() || inCode(para.story) || suppressed(para.story)) return;
			R.CommentReference ref = (R.CommentReference) v;
			if (plan != null && plan.removesComment(ref.getId())) return; // gone with the revision it was on
			TextRun run = new TextRun(TextRun.Kind.COMMENT_REFERENCE, "", state.revision);
			run.hidden = hidden;
			run.commentId = ref.getId() == null ? "" : ref.getId().toString();
			para.block.runs.add(run);
			return;
		}
		case "drawing":
		case "pict":
		case "object":
			if (!inCode(para.story) && !suppressed(para.story) && !(hidden && !options.isHidden())) anchored(v, para, state);
			return;
		case "lastRenderedPageBreak": {
			if (para.story.kind != TextBlock.Story.MAIN || inCode(para.story) || hidden && !options.isHidden()) return;
			if (suppressed(para.story)) return;
			TextRun run = new TextRun(TextRun.Kind.PAGE_MARKER, "", state.revision);
			run.pageNumber = ++pageMarkers + 1;
			para.block.runs.add(run);
			return;
		}
		case "AlternateContent": {
			AlternateContent ac = (AlternateContent) v;
			compareBranches(ac, para.block.address);
			for (Object o : McSelection.select(McPurpose.TEXT, ac)) runItem(o, r, hidden, para, state);
			return;
		}
		case "ruby": {
			CTRuby ruby = (CTRuby) v;
			if (ruby.getRubyBase() != null) runs(ruby.getRubyBase().getEGRubyContent(), para, state);
			return;
		}
		default:
			// a run within a run (w:r in m:r, a ruby base's) or nothing that is text: page numbers,
			// separators, the last rendered page break, dates
			if (v instanceof R) run((R) v, para, state);
		}
	}

	private static boolean inCode(Story story) {
		for (FieldState f : story.fields) if (!f.separated) return true;
		return false;
	}

	/** Document text: a field's code where a field is in its code (shown only under {@code field_codes}), else text. */
	private void text(Paragraph para, String s, boolean hidden, RunState state) {
		if (s == null || s.isEmpty()) return;
		if (inCode(para.story)) {
			if (options.isFieldCodes()) addText(para, s, TextRun.Kind.FIELD_CODE, hidden, state);
			return;
		}
		if (suppressed(para.story)) return;
		if (!(hidden && !options.isHidden())) {
			for (FieldState f : para.story.fields) f.result.append(s);
			for (Bookmark b : openBookmarks.values()) {
				if (b.paragraphEnded && b.text.length() > 0) b.text.append('\n');
				b.paragraphEnded = false;
				b.text.append(s);
			}
		}
		addText(para, s, TextRun.Kind.TEXT, hidden, state);
	}

	/**
	 * Inside the cached result of a field whose result was computed (REQ-506): nothing there is read,
	 * text, note and comment references, pictures, page markers, field codes and nested fields alike.
	 */
	private static boolean suppressed(Story story) {
		for (FieldState f : story.fields) if (f.computed != null || f.toc) return true;
		return false;
	}

	/** Inside a table of contents left out (D12). */
	private static boolean inToc(Story story) {
		for (FieldState f : story.fields) if (f.toc) return true;
		return false;
	}

	/** A TOC field's instruction: a table of contents, of figures ({@code \c}), of anything it collects. */
	static boolean isTocField(String instr) {
		List<String> tokens = fieldTokens(instr);
		return !tokens.isEmpty() && "TOC".equalsIgnoreCase(tokens.get(0));
	}

	/** A table of contents' code, shown under {@code field_codes} until it was known to be one, taken out again. */
	private static void removeFieldCode(FieldState f, Paragraph para) {
		// what follows the field may merge with what preceded it, as it would with no field codes
		if (f.floorParagraph != null) f.floorParagraph.mergeFloor = f.previousFloor;
		if (f.codeBlock != null) {
			List<TextRun> runs = f.codeBlock.runs;
			for (int k = runs.size() - 1; k >= Math.min(f.codeStart, runs.size()); k--) {
				if (runs.get(k).kind == TextRun.Kind.FIELD_CODE) runs.remove(k);
			}
		}
		if (para.block != f.codeBlock) {
			// a code over more than one paragraph: this one's code too
			List<TextRun> runs = para.block.runs;
			for (int k = runs.size() - 1; k >= 0; k--) if (runs.get(k).kind == TextRun.Kind.FIELD_CODE) runs.remove(k);
		}
	}

	/** A field's result under {@code fields: update}, or null to keep its cached one (REQ-506). */
	private String update(FieldState f) {
		if (options.getFields() != TextOptions.Fields.UPDATE || f.locked || pkg == null) return null;
		String instr = f.instr.toString().trim();
		if (f.ffData != null && instr.regionMatches(true, 0, "FORMTEXT", 0, 8)) return formText(f.ffData);
		return fieldValues().compute(instr);
	}

	/** What Word's update writes in a FORMTEXT with no default text: five en spaces (measured). */
	static final String EMPTY_FORM_FIELD = "\u2002\u2002\u2002\u2002\u2002";

	/**
	 * A FORMTEXT's result under {@code fields: update}, as Word's update gives it (REQ-506; measured in
	 * Word, CR-046 phase 2): its default text, or five en spaces where it has none, replacing what was
	 * typed; but in a document protected for filling in forms Word keeps what was typed, and so does
	 * this (null). Null too for a text input that isn't regular text or has a format, which Word's
	 * update hasn't been seen to treat.
	 */
	private String formText(CTFFData ffData) {
		if (formsProtected()) return null;
		CTFFTextInput input = null;
		for (JAXBElement<?> j : ffData.getNameOrEnabledOrCalcOnExit()) {
			if (j.getValue() instanceof CTFFTextInput) input = (CTFFTextInput) j.getValue();
		}
		if (input == null) return null;
		if (input.getType() != null && input.getType().getVal() != null && input.getType().getVal() != STFFTextType.REGULAR) return null;
		if (input.getFormat() != null && input.getFormat().getVal() != null && !input.getFormat().getVal().isEmpty()) return null;
		String text = input.getDefault() == null ? null : input.getDefault().getVal();
		return text == null || text.isEmpty() ? EMPTY_FORM_FIELD : text;
	}

	private Boolean formsProtected;

	/** {@code w:documentProtection w:edit="forms"}, enforced (the attribute as written: its getter answers true when it's absent). */
	private boolean formsProtected() {
		if (formsProtected == null) {
			DocumentSettingsPart settings = pkg.getMainDocumentPart() == null ? null : pkg.getMainDocumentPart().getDocumentSettingsPart();
			CTDocProtect protect = settings == null || settings.getJaxbElement() == null ? null : settings.getJaxbElement().getDocumentProtection();
			formsProtected = protect != null && protect.getEdit() == STDocProtect.FORMS && written(protect, "enforcement");
		}
		return formsProtected;
	}

	private void addText(Paragraph para, String s, TextRun.Kind kind, boolean hidden, RunState state) {
		TextRun run = new TextRun(kind, s, state.revision);
		run.hidden = hidden;
		run.placeholder = state.placeholder;
		if (kind == TextRun.Kind.TEXT) {
			if (para.format != null) run.copyFormat(para.format);
			run.link = link(para);
			run.math = para.math;
			run.displayMath = para.displayMath;
		}
		List<TextRun> runs = para.block.runs;
		if (runs.size() > para.mergeFloor && !run.math) {
			TextRun last = runs.get(runs.size() - 1);
			if (last.kind == kind && last.hidden == hidden && last.placeholder == state.placeholder
					&& java.util.Objects.equals(last.revision, state.revision) && last.sameFormat(run)) {
				TextRun merged = new TextRun(kind, last.text + s, last.revision);
				merged.hidden = hidden;
				merged.placeholder = last.placeholder;
				merged.copyFormat(last);
				runs.set(runs.size() - 1, merged);
				return;
			}
		}
		runs.add(run);
	}

	/** The link text being read is in: the {@code w:hyperlink}'s, else the innermost HYPERLINK field's whose result it is. */
	private static String link(Paragraph para) {
		if (para.link != null) return para.link;
		for (FieldState f : para.story.fields) if (f.separated && f.link != null) return f.link;
		return null;
	}

	/** A {@code w:hyperlink}'s target: its relationship's target, and {@code #} and its anchor; else {@code #} and its anchor. */
	private static String hyperlinkTarget(P.Hyperlink h, Story story) {
		String target = null;
		if (h.getId() != null && story.part != null && story.part.getRelationshipsPart(false) != null) {
			org.docx4j.relationships.Relationship rel = story.part.getRelationshipsPart(false).getRelationshipByID(h.getId());
			if (rel != null && rel.getTarget() != null && !rel.getTarget().isEmpty()) target = rel.getTarget();
		}
		String anchor = h.getAnchor() == null || h.getAnchor().isEmpty() ? null : h.getAnchor();
		if (anchor != null) return target == null ? "#" + anchor : target + "#" + anchor;
		return target;
	}

	/**
	 * A HYPERLINK field's target from its instruction (WD REQ-703): its address, and {@code #} and the
	 * bookmark its {@code \l} names; null for any other field, or one with neither.
	 */
	static String fieldHyperlinkTarget(String instr) {
		List<String> tokens = fieldTokens(instr);
		if (tokens.isEmpty() || !"HYPERLINK".equalsIgnoreCase(tokens.get(0))) return null;
		String address = null, bookmark = null;
		for (int i = 1; i < tokens.size(); i++) {
			String t = tokens.get(i);
			if (t.length() == 2 && t.charAt(0) == '\\') {
				char sw = Character.toLowerCase(t.charAt(1));
				// \l, \o, \t take an argument; \m and \n don't
				if ((sw == 'l' || sw == 'o' || sw == 't') && i + 1 < tokens.size()) {
					String arg = tokens.get(++i);
					if (sw == 'l') bookmark = arg;
				}
			} else if (address == null) {
				address = t;
			}
		}
		if (address != null && address.isEmpty()) address = null;
		if (bookmark != null && bookmark.isEmpty()) bookmark = null;
		if (bookmark != null) return address == null ? "#" + bookmark : address + "#" + bookmark;
		return address;
	}

	/** A field instruction's words: a quoted argument is one, its quotes removed and {@code \\} and {@code \"} unescaped. */
	private static List<String> fieldTokens(String instr) {
		List<String> tokens = new ArrayList<String>();
		if (instr == null) return tokens;
		int i = 0, n = instr.length();
		while (i < n) {
			char c = instr.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
			} else if (c == '"') {
				StringBuilder sb = new StringBuilder();
				i++;
				while (i < n && instr.charAt(i) != '"') {
					char d = instr.charAt(i);
					if (d == '\\' && i + 1 < n && (instr.charAt(i + 1) == '\\' || instr.charAt(i + 1) == '"')) d = instr.charAt(++i);
					sb.append(d);
					i++;
				}
				i++; // the closing quote
				tokens.add(sb.toString());
			} else {
				int j = i;
				while (j < n && !Character.isWhitespace(instr.charAt(j)) && instr.charAt(j) != '"') j++;
				tokens.add(instr.substring(i, j));
				i = j;
			}
		}
		return tokens;
	}

	private void endField(String instr, String result, Paragraph para) {
		para.block.fields.add(new TextField(instr, result));
		if (result == null) warn(TextWarning.FIELD_NO_RESULT, para.block.address, null);
	}

	private void simpleField(CTSimpleField field, Paragraph para, RunState state) {
		if (suppressed(para.story)) return; // inside a computed field's cached result
		String instr = field.getInstr() == null ? "" : field.getInstr();
		if (!options.isToc() && isTocField(instr)) {
			para.toc = true; // a table of contents left out (D12): nothing of it is read
			return;
		}
		if (options.isFieldCodes() && !inCode(para.story)) addText(para, instr, TextRun.Kind.FIELD_CODE, false, state);
		if (options.getFields() == TextOptions.Fields.UPDATE && !written(field, "fldLock") && pkg != null && !inCode(para.story)
				&& !suppressed(para.story)) {
			String value = fieldValues().compute(instr);
			if (value != null) {
				text(para, value, false, state);
				para.block.fields.add(new TextField(instr, value, true));
				return;
			}
		}
		int before = para.block.runs.size();
		String text = resultOf(field.getContent(), para, state, fieldHyperlinkTarget(instr));
		boolean hasResult = text != null && !text.isEmpty() || para.block.runs.size() > before;
		para.block.fields.add(new TextField(instr, hasResult ? text : null));
		if (!hasResult) warn(TextWarning.FIELD_NO_RESULT, para.block.address, null);
	}

	/** The runs of a simple field's result, added to the paragraph; their text. */
	private String resultOf(List<Object> content, Paragraph para, RunState state, String link) {
		FieldState capture = new FieldState();
		capture.separated = true;
		capture.link = link;
		para.story.fields.addLast(capture);
		try {
			runs(content, para, state);
		} finally {
			para.story.fields.removeLastOccurrence(capture);
		}
		return capture.result.toString();
	}

	// --- math (REQ-604) ---------------------------------------------------------------------------------

	private static boolean isMath(Object v) {
		return v instanceof org.docx4j.math.CTOMath || v instanceof org.docx4j.math.CTOMathPara;
	}

	/**
	 * An equation as LaTeX, {@code $...$} ({@code $$...$$} for an {@code m:oMathPara}), by
	 * {@link OmmlToLatex} in the view. In {@code markup}, an equation with tracked changes is its
	 * Reject All form deleted and its Accept All form inserted, each as CR-043's plan for that view
	 * leaves it (a structure inserted whole goes with its runs). An equation outside the converter's
	 * subset is its runs' text, delimited the same way.
	 */
	private void math(Object v, Paragraph para, RunState state) {
		boolean display = v instanceof org.docx4j.math.CTOMathPara;
		boolean savedMath = para.math, savedDisplay = para.displayMath;
		para.math = true;
		para.displayMath = display;
		try {
			mathText(v, display, para, state);
		} finally {
			para.math = savedMath;
			para.displayMath = savedDisplay;
		}
	}

	private void mathText(Object v, boolean display, Paragraph para, RunState state) {
		if (view == TextView.MARKUP && mode == Mode.VIEW) {
			TextRevision[] revisions = mathRevisions(v);
			if (revisions[0] != null || revisions[1] != null) {
				ResolvedView original = markupPlan(false), accepted = markupPlan(true);
				String before = latex(v, o -> !original.removes(o));
				String after = latex(v, o -> !accepted.removes(o));
				if (!before.equals(after)) {
					if (!before.isEmpty()) text(para, delimit(before, display), false, state.withRevision(
							revisions[0] != null ? revisions[0] : new TextRevision(TextRevision.Type.DELETION, null, null), false));
					if (!after.isEmpty()) text(para, delimit(after, display), false, state.withRevision(
							revisions[1] != null ? revisions[1] : new TextRevision(TextRevision.Type.INSERTION, null, null), false));
					return;
				}
				if (!after.isEmpty()) text(para, delimit(after, display), false, state);
				return;
			}
		}
		String latex = latex(v, this::keepMathInView);
		if (!latex.isEmpty()) text(para, delimit(latex, display), false, state);
	}

	private static String delimit(String latex, boolean display) {
		return display ? "$$" + latex + "$$" : "$" + latex + "$";
	}

	private String latex(Object v, java.util.function.Predicate<Object> keep) {
		OmmlToLatex converter = new OmmlToLatex().keep(keep);
		try {
			return v instanceof org.docx4j.math.CTOMathPara ? converter.convertOMathPara((org.docx4j.math.CTOMathPara) v)
					: converter.convertOMath((org.docx4j.math.CTOMath) v);
		} catch (OmmlMathException | RuntimeException e) {
			log.debug("An equation outside the LaTeX converter's subset is read as its text: " + e.getMessage());
			StringBuilder sb = new StringBuilder();
			mathText(v, sb, 0, keep);
			return sb.toString();
		}
	}

	/** The view's rule for an equation's list members: what the plan removes goes; without a plan, a revision by its type. */
	private boolean keepMathInView(Object o) {
		if (removes(o)) return false;
		if (mode != Mode.PLANLESS || view == TextView.MARKUP) return true;
		Boolean ins = mathRevisionType(o);
		return ins == null || ins == (view == TextView.ACCEPTED);
	}

	private ResolvedView markupAccepted, markupOriginal;

	/** In {@code markup}, the plans of the other two views, made when an equation with tracked changes needs them. */
	private ResolvedView markupPlan(boolean accepted) {
		if (accepted) {
			if (markupAccepted == null) markupAccepted = ResolvedView.accepted(pkg);
			return markupAccepted;
		}
		if (markupOriginal == null) markupOriginal = ResolvedView.original(pkg);
		return markupOriginal;
	}

	/** For a {@code w:ins} or {@code w:del} in an equation's run, whether it is an insertion; else null. */
	private static Boolean mathRevisionType(Object o) {
		if (o instanceof JAXBElement && ((JAXBElement<?>) o).getValue() instanceof org.docx4j.wml.CTMathRunTrackChange) {
			return "ins".equals(((JAXBElement<?>) o).getName().getLocalPart());
		}
		return null;
	}

	/** The first deletion's and the first insertion's revision in an equation, each null where there is none. */
	private static TextRevision[] mathRevisions(Object v) {
		TextRevision[] out = new TextRevision[2];
		walk(v, 0, new Visitor() {
			@Override boolean item(Object item) {
				Boolean ins = mathRevisionType(item);
				if (ins != null) {
					org.docx4j.wml.CTMathRunTrackChange tc = (org.docx4j.wml.CTMathRunTrackChange) ((JAXBElement<?>) item).getValue();
					int i = ins ? 1 : 0;
					if (out[i] == null) out[i] = new TextRevision(ins ? TextRevision.Type.INSERTION : TextRevision.Type.DELETION,
							tc.getAuthor(), tc.getDate() == null ? null : tc.getDate().toXMLFormat());
				}
				return true;
			}
		});
		return out;
	}

	/** An equation's runs' text, for one outside the converter's subset. */
	private static void mathText(Object v, StringBuilder sb, int depth, java.util.function.Predicate<Object> keep) {
		if (v == null || depth > 64) return;
		if (v instanceof org.docx4j.math.CTR) {
			for (Object o : ((org.docx4j.math.CTR) v).getContent()) {
				if (!keep.test(o)) continue;
				Object ov = unwrap(o);
				if (ov instanceof org.docx4j.wml.CTMathRunTrackChange) {
					for (Object inner : ((org.docx4j.wml.CTMathRunTrackChange) ov).getRest()) mathItem(inner, sb);
					continue;
				}
				mathItem(o, sb);
			}
			return;
		}
		if (v instanceof org.docx4j.math.CTCtrlPr) return;
		for (Object child : children(v)) {
			if (!keep.test(child)) continue;
			mathText(unwrap(child), sb, depth + 1, keep);
		}
	}

	private static void mathItem(Object o, StringBuilder sb) {
		Object v = unwrap(o);
		if (v instanceof org.docx4j.math.CTText) {
			String s = ((org.docx4j.math.CTText) v).getValue();
			if (s != null) sb.append(s);
		} else if (v instanceof Text && "t".equals(name(o))) {
			String s = ((Text) v).getValue();
			if (s != null) sb.append(s);
		}
	}

	// --- what a run anchors: text boxes (REQ-518), charts, SmartArt and WordArt (REQ-511), pictures (REQ-514), objects (REQ-512)

	private static final String DECORATIVE_EXT = "{C183D7F6-B498-43B3-948B-1728B52AA6E4}";
	private static final String DIAGRAM_NS = "http://schemas.openxmlformats.org/drawingml/2006/diagram";

	/**
	 * What a run's drawing, picture or object holds, in document order, branches selected for text:
	 * text boxes, WordArt, charts, SmartArt and embedded packages to follow the paragraph; pictures
	 * and objects as runs where they are.
	 */
	private void anchored(Object v, Paragraph para, RunState state) {
		walk(v, 0, new Visitor() {
			@Override public boolean enter(Object value) {
				if (value instanceof CTTxbxContent) {
					if (options.isTextBoxes()) para.story.pending.add(new Box((CTTxbxContent) value, para.address + "/textbox:" + para.boxes++));
					return false;
				}
				if (value instanceof AlternateContent) {
					AlternateContent ac = (AlternateContent) value;
					compareBranches(ac, para.block.address);
					for (Object o : McSelection.select(McPurpose.TEXT, ac)) walk(unwrap(o), 1, this);
					return false;
				}
				if (value instanceof CTObject) {
					object((CTObject) value, para, state);
					return false;
				}
				if (value instanceof CTControl) {
					objectRun(para, null, state);
					return false;
				}
				if (value instanceof Inline || value instanceof Anchor) return graphic(value, para, state);
				if (value instanceof Pic) {
					Pic pic = (Pic) value;
					image(pic.getNvPicPr() == null ? null : pic.getNvPicPr().getCNvPr(), blipPart(pic, para), false, pic, para, state);
					return false;
				}
				if (value instanceof org.docx4j.vml.CTShape) return vmlShape((org.docx4j.vml.CTShape) value, para, state);
				return true;
			}
		});
	}

	/** A {@code wp:inline} or {@code wp:anchor}: a chart, SmartArt, a picture, or a shape to walk into. */
	private boolean graphic(Object anchor, Paragraph para, RunState state) {
		ChartRef chart = ChartRef.of(para.story.part, anchor);
		if (chart != null) {
			if (options.isCharts()) para.story.pending.add(new Chart(chart, para.address + "/chart:" + para.charts++));
			return false;
		}
		Graphic graphic = anchor instanceof Inline ? ((Inline) anchor).getGraphic() : ((Anchor) anchor).getGraphic();
		CTNonVisualDrawingProps docPr = anchor instanceof Inline ? ((Inline) anchor).getDocPr() : ((Anchor) anchor).getDocPr();
		if (graphic == null || graphic.getGraphicData() == null) return false;
		for (Object o : graphic.getGraphicData().getAny()) {
			Object v = unwrap(o);
			if (v instanceof org.docx4j.dml.diagram.CTRelIds) {
				if (options.isSmartart()) {
					Part dm = related(para.story.part, ((org.docx4j.dml.diagram.CTRelIds) v).getDm());
					if (dm instanceof DiagramDataPart) para.story.pending.add(new SmartArt((DiagramDataPart) dm, para.address + "/smartart:" + para.smartArts++));
					else if (dm != null) notRead(para.address, dm, "the SmartArt data part", null);
				}
				return false;
			}
			if (v instanceof Pic) {
				image(docPr, blipPart((Pic) v, para), false, anchor, para, state);
				return false;
			}
		}
		return true; // a shape, group or canvas: its text boxes and pictures
	}

	/** A VML shape: legacy WordArt, a picture, or a shape to walk into for its text box. */
	private boolean vmlShape(org.docx4j.vml.CTShape shape, Paragraph para, RunState state) {
		org.docx4j.vml.CTImageData imageData = null;
		for (JAXBElement<?> e : shape.getPathOrFormulasOrHandles()) {
			Object v = e.getValue();
			if (v instanceof org.docx4j.vml.CTTextPath) {
				String string = ((org.docx4j.vml.CTTextPath) v).getString();
				if (string != null && !string.isEmpty()) {
					if (options.isTextBoxes()) para.story.pending.add(new WordArt(string, shape, para.address + "/textbox:" + para.boxes++));
					return false;
				}
			} else if (v instanceof org.docx4j.vml.CTImageData) {
				imageData = (org.docx4j.vml.CTImageData) v;
			}
		}
		if (imageData != null) image(shape.getAlt(), null, null, false, true,
				related(para.story.part, imageData.getId()), shape, para, state);
		return true;
	}

	/** A {@code w:object}: an object run with its ProgID, and under {@code embedded: true} its package to follow (REQ-512). */
	private void object(CTObject object, Paragraph para, RunState state) {
		if (object.getControl() != null) {
			objectRun(para, null, state);
			return;
		}
		for (Object o : object.getAnyAndAny()) {
			Object v = unwrap(o);
			if (v instanceof org.docx4j.vml.officedrawing.CTOLEObject) {
				org.docx4j.vml.officedrawing.CTOLEObject ole = (org.docx4j.vml.officedrawing.CTOLEObject) v;
				objectRun(para, ole.getProgID(), state);
				if (!options.isEmbedded() || nested) return;
				Part part = related(para.story.part, ole.getId());
				boolean ole1 = ole.getProgID() != null && ole.getProgID().toLowerCase(Locale.ROOT).startsWith("package");
				if (part instanceof EmbeddedPackagePart || ole1 && part instanceof BinaryPart) {
					para.story.pending.add(new EmbeddedObject((BinaryPart) part, para.address + "/object:" + para.objects++));
				}
				return;
			}
			if (v instanceof CTControl) {
				objectRun(para, null, state);
				return;
			}
		}
	}

	private void objectRun(Paragraph para, String progId, RunState state) {
		TextRun run = new TextRun(TextRun.Kind.OBJECT, "", state.revision);
		run.progId = progId;
		para.block.runs.add(run);
	}

	/** A DrawingML picture: its {@code wp:docPr} (or a nested picture's {@code pic:cNvPr}). */
	private void image(CTNonVisualDrawingProps nv, Part part, boolean vml, Object source, Paragraph para, RunState state) {
		image(nv == null ? null : nv.getDescr(), nv == null ? null : nv.getName(), nv == null ? null : nv.getTitle(),
				decorative(nv), vml, part, source, para, state);
	}

	private void image(String descr, String name, String title, boolean decorative, boolean vml, Part part, Object source,
			Paragraph para, RunState state) {
		TextImage image = new TextImage(descr, name, title, decorative, vml, part, source);
		TextRun run = new TextRun(TextRun.Kind.IMAGE, "", state.revision);
		run.image = image;
		try {
			run.description = options.getImageDescriber().describe(image);
		} catch (RuntimeException e) {
			log.warn("The image describer failed at " + para.block.address + ": " + e.getMessage());
			warn(new TextWarning(TextWarning.IMAGE_NOT_DESCRIBED, para.block.address, null, null, partName(part)));
		}
		run.placeholder = state.placeholder;
		para.block.runs.add(run);
	}

	/** {@code adec:decorative val="1"} among the properties' extensions. */
	private static boolean decorative(CTNonVisualDrawingProps nv) {
		if (nv == null || nv.getExtLst() == null) return false;
		for (CTOfficeArtExtension ext : nv.getExtLst().getExt()) {
			if (!DECORATIVE_EXT.equalsIgnoreCase(ext.getUri())) continue;
			Object any = ext.getAny();
			String val = null;
			if (any instanceof org.w3c.dom.Element) {
				val = ((org.w3c.dom.Element) any).getAttribute("val");
			} else if (any != null) {
				// adec:decorative is bound (CTDecorative, isVal); read reflectively, as its package is the binding's
				Object d = unwrap(any);
				for (String getter : new String[] { "isVal", "getVal" }) {
					try {
						Object x = d.getClass().getMethod(getter).invoke(d);
						val = x == null ? null : x.toString();
						break;
					} catch (ReflectiveOperationException | RuntimeException e) {
						val = null;
					}
				}
			}
			if ("1".equals(val) || "true".equalsIgnoreCase(val)) return true;
		}
		return false;
	}

	private static Part blipPart(Pic pic, Paragraph para) {
		if (pic.getBlipFill() == null || pic.getBlipFill().getBlip() == null) return null;
		org.docx4j.dml.CTBlip blip = pic.getBlipFill().getBlip();
		Part part = related(para.story.part, blip.getEmbed());
		// an SVG picture with no PNG copy (r:embed is optional; the ImageAddSVG sample writes it so): its svgBlip's (#699)
		return part != null ? part : related(para.story.part, svgBlipEmbed(blip));
	}

	/** The extension Office 2016 writes an SVG picture's SVG in, beside the PNG copy {@code a:blip/@r:embed} names. */
	static final String SVG_BLIP_EXTENSION = "{96DAC541-7B7A-43D3-8B79-37D633B846F1}";

	/** An {@code a:blip}'s {@code asvg:svgBlip/@r:embed}, or null. */
	static String svgBlipEmbed(org.docx4j.dml.CTBlip blip) {
		if (blip == null || blip.getExtLst() == null) return null;
		for (org.docx4j.dml.CTOfficeArtExtension ext : blip.getExtLst().getExt()) {
			if (!SVG_BLIP_EXTENSION.equalsIgnoreCase(ext.getUri())) continue;
			Object v = unwrap(ext.getAny());
			if (v instanceof org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main.CTSVGBlip) {
				return ((org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main.CTSVGBlip) v).getEmbed();
			}
			if (v instanceof org.w3c.dom.Element) {
				String id = ((org.w3c.dom.Element) v).getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed");
				if (!id.isEmpty()) return id;
			}
		}
		return null;
	}

	/** The part a relationship of {@code host} names, without adding a relationships part. */
	private static Part related(Part host, String rId) {
		if (host == null || rId == null || rId.isEmpty() || host.getRelationshipsPart(false) == null) return null;
		return host.getRelationshipsPart(false).getPart(rId);
	}

	// --- markup compatibility: REQ-307 -----------------------------------------------------------------

	/**
	 * The record of {@code word} mode (CR-051 section 3.2): each block-, row- or cell-level
	 * {@code mc:AlternateContent} a load took out of this container (a block container, a table's
	 * rows or a row's cells), resolved to the branch Word reads, is compared with its other branches
	 * ({@code mce.branches_differ}) at the address of the promoted content, as the element's would be
	 * in {@code preserve} mode.  A container is matched by
	 * its address from the part's root ({@link McSplice#address}), worked out at the splice and again
	 * here; a part with no record costs nothing.
	 */
	private void resolvedAlternateContent(List<Object> items, String prefix, Story story) {
		if (items.isEmpty() || !(story.part instanceof JaxbXmlPart)) return;
		List<ResolvedAlternateContent> records = ((JaxbXmlPart<?>) story.part).getResolvedAlternateContent();
		if (records.isEmpty()) return;
		Object first = unwrap(items.get(0));
		Object owner = first instanceof Child ? ((Child) first).getParent() : null;
		if (owner == null) return;
		String here = McSplice.address(owner);
		for (ResolvedAlternateContent r : records) {
			if (r.getBranch() == null || !here.equals(r.getAddress())) continue;
			compareBranches(r.getElement(), prefix + "/" + r.getIndex());
		}
	}

	/**
	 * {@code mce.branches_differ}: where the selected branch and another both hold text, and the texts
	 * differ after REQ-1201's normalisation.
	 */
	private void compareBranches(AlternateContent ac, String address) {
		if (!options.isWarnings()) return;
		Object selected = McSelection.selectedBranch(McPurpose.TEXT, ac);
		if (selected == null) return;
		String mine = normalized(selected);
		if (mine.isEmpty()) return;
		List<Object> branches = new ArrayList<Object>(ac.getChoice());
		if (ac.getFallback() != null) branches.add(ac.getFallback());
		for (Object other : branches) {
			if (other == selected) continue;
			String theirs = normalized(other);
			if (!theirs.isEmpty() && !theirs.equals(mine)) {
				String branch = selected instanceof AlternateContent.Choice
						? "mc:Choice Requires=\"" + ((AlternateContent.Choice) selected).getRequires() + "\"" : "mc:Fallback";
				warn(TextWarning.MCE_BRANCHES_DIFFER, address, branch);
				return;
			}
		}
	}

	/** A branch's text ({@code w:t}, {@code a:t}, {@code m:t}), normalised as REQ-1201 normalises. */
	private static String normalized(Object branch) {
		StringBuilder sb = new StringBuilder();
		walk(branch, 0, new Visitor() {
			@Override public boolean item(Object item) {
				Object v = unwrap(item);
				if (v instanceof Text && "t".equals(name(item))) sb.append(((Text) v).getValue());
				else if (v instanceof org.docx4j.math.CTText) sb.append(((org.docx4j.math.CTText) v).getValue());
				else if (v instanceof org.docx4j.dml.CTRegularTextRun) sb.append(((org.docx4j.dml.CTRegularTextRun) v).getT());
				return true;
			}
		});
		return Texts.normalize(sb.toString());
	}

	private void warn(String kind, String address, String branch) {
		warn(new TextWarning(kind, address, branch));
	}

	private void warn(String kind, String address, String branch, String contentType) {
		warn(new TextWarning(kind, address, branch, contentType));
	}

	private void warn(TextWarning w) {
		if (warned.add(w)) warnings.add(w);
	}

	/** Content that can't be read contributes nothing and raises {@code content.not_read} (REQ-801). */
	private void notRead(String anchor, Part part, String what, Exception e) {
		log.warn("Couldn't read " + what + " at " + anchor + (e == null ? "" : ": " + e.getMessage()));
		warn(new TextWarning(TextWarning.CONTENT_NOT_READ, anchor, null, null, partName(part)));
	}

	private static String partName(Part part) {
		return part == null || part.getPartName() == null ? null : part.getPartName().getName();
	}

	// --- headers and footers by section (REQ-502) ---------------------------------------------------------

	private void sectionsWithHeaders(MainDocumentPart main, List<SectPr> sections, List<TextBlock> mainBlocks,
			boolean headers, boolean footers, List<TextBlock> out) {
		boolean evenAndOdd = false;
		DocumentSettingsPart settings = main.getDocumentSettingsPart();
		if (settings != null && settings.getJaxbElement() != null && settings.getJaxbElement().getEvenAndOddHeaders() != null) {
			evenAndOdd = settings.getJaxbElement().getEvenAndOddHeaders().isVal();
		}
		RelationshipsPart rels = main.getRelationshipsPart(false);
		Set<String> emitted = new LinkedHashSet<String>();
		Map<HdrFtrRef, String> lastHeader = new HashMap<HdrFtrRef, String>();
		Map<HdrFtrRef, String> lastFooter = new HashMap<HdrFtrRef, String>();
		int b = 0;
		for (int s = 0; s < sections.size(); s++) {
			SectPr sectPr = sections.get(s);
			Map<HdrFtrRef, String> ownHeaders = new HashMap<HdrFtrRef, String>();
			Map<HdrFtrRef, String> ownFooters = new HashMap<HdrFtrRef, String>();
			boolean titlePg = false;
			if (sectPr != null) {
				for (CTRel ref : sectPr.getEGHdrFtrReferences()) {
					if (ref instanceof org.docx4j.wml.HeaderReference) ownHeaders.put(((org.docx4j.wml.HeaderReference) ref).getType(), ref.getId());
					else if (ref instanceof org.docx4j.wml.FooterReference) ownFooters.put(((org.docx4j.wml.FooterReference) ref).getType(), ref.getId());
				}
				titlePg = sectPr.getTitlePg() != null && sectPr.getTitlePg().isVal();
			}
			lastHeader.putAll(ownHeaders);
			lastFooter.putAll(ownFooters);
			List<HdrFtrRef> shown = new ArrayList<HdrFtrRef>();
			if (titlePg) shown.add(HdrFtrRef.FIRST);
			shown.add(HdrFtrRef.DEFAULT);
			if (evenAndOdd) shown.add(HdrFtrRef.EVEN);
			if (headers) for (HdrFtrRef type : shown) headerOrFooter(rels, lastHeader.get(type), TextBlock.Story.HEADER, emitted, out);
			while (b < mainBlocks.size() && mainBlocks.get(b).section <= s) out.add(mainBlocks.get(b++));
			if (s == sections.size() - 1) while (b < mainBlocks.size()) out.add(mainBlocks.get(b++));
			if (footers) for (HdrFtrRef type : shown) headerOrFooter(rels, lastFooter.get(type), TextBlock.Story.FOOTER, emitted, out);
		}
	}

	private void headerOrFooter(RelationshipsPart rels, String rId, TextBlock.Story kind, Set<String> emitted, List<TextBlock> out) {
		if (rId == null || rels == null || !emitted.add(rId)) return;
		Part part = rels.getPart(rId);
		Object content = null;
		if (part instanceof HeaderPart) content = ((HeaderPart) part).getJaxbElement();
		else if (part instanceof FooterPart) content = ((FooterPart) part).getJaxbElement();
		if (content == null) return;
		// the address prefix is the field root (an embedded package's), not the part's content (CR-050 phase 0 found it here)
		String prefix = root + (kind == TextBlock.Story.HEADER ? "header:" : "footer:") + rId;
		Story story = new Story(kind, prefix, part);
		blocks(blockItems(content), prefix, story, out);
	}

	// --- notes (REQ-503) -------------------------------------------------------------------------------

	private void notes(List<CTFtnEdn> notes, Map<String, Integer> numbers, String type, Part part, List<TextBlock> out) {
		if (notes == null || numbers.isEmpty()) return;
		Map<String, CTFtnEdn> byId = new HashMap<String, CTFtnEdn>();
		for (CTFtnEdn note : notes) if (note.getId() != null) byId.put(note.getId().toString(), note);
		for (Map.Entry<String, Integer> e : numbers.entrySet()) {
			CTFtnEdn note = byId.get(e.getKey());
			if (note == null) continue;
			if (note.getType() != null && !"normal".equals(note.getType().value())) continue;
			String prefix = root + type + ":" + e.getKey();
			Story story = new Story(TextBlock.Story.valueOf(type.toUpperCase(Locale.ROOT)), prefix, part);
			story.noteType = type;
			story.noteNumber = e.getValue();
			story.noteId = e.getKey();
			List<TextBlock> blocks = new ArrayList<TextBlock>();
			blocks(note.getContent(), prefix, story, blocks);
			if (blocks.isEmpty()) continue;
			// the note is prefixed [n]: its own mark, or one added at its start
			boolean marked = false;
			for (TextBlock b : blocks) for (TextRun r : b.runs) if (r.kind == TextRun.Kind.NOTE_MARK) marked = true;
			if (!marked) {
				for (TextBlock b : blocks) {
					if (b.kind != TextBlock.Kind.PARAGRAPH) continue;
					TextRun mark = new TextRun(TextRun.Kind.NOTE_MARK, "", null);
					mark.noteType = type;
					mark.noteNumber = e.getValue();
					mark.noteId = e.getKey();
					b.runs.add(0, mark);
					break;
				}
			}
			// every block of the note says whose it is, a table's and a chart's too
			for (TextBlock b : blocks) {
				if (b.noteType != null) continue;
				b.noteType = type;
				b.noteNumber = e.getValue();
				b.noteId = e.getKey();
			}
			out.addAll(blocks);
		}
	}

	// --- comments (REQ-504) ------------------------------------------------------------------------------

	private void comments(MainDocumentPart main, Map<String, TextComment> out) {
		CommentsPart part = main.getCommentsPart();
		if (part == null || part.getJaxbElement() == null) return;
		Map<String, CTCommentEx> extended = new HashMap<String, CTCommentEx>();
		CommentsExtendedPart ext = main.getCommentsExtendedPart();
		if (ext != null && ext.getJaxbElement() != null) {
			for (CTCommentEx ex : ext.getJaxbElement().getCommentEx()) if (ex.getParaId() != null) extended.put(ex.getParaId().toUpperCase(Locale.ROOT), ex);
		}
		Map<String, String> idByParaId = new HashMap<String, String>();
		Set<String> anchored = anchoredComments(main);
		for (Comments.Comment c : part.getJaxbElement().getComment()) {
			if (plan != null && plan.removesComment(c.getId())) continue;
			String id = c.getId() == null ? "" : c.getId().toString();
			// a comment with no anchor in the document (no reference, no range) is none of its text: Word drops it
			// on save, and the inspector reports it as an unused story (CR-050's ruling before phase 2)
			if (!anchored.contains(id)) continue;
			TextComment comment = new TextComment(id);
			comment.author = c.getAuthor();
			comment.initials = c.getInitials();
			comment.date = c.getDate() == null ? null : c.getDate().toXMLFormat();
			String prefix = root + "comment:" + id;
			blocks(c.getContent(), prefix, new Story(TextBlock.Story.COMMENT, prefix, part), comment.blocks);
			String paraId = lastParaId(c.getContent());
			if (paraId != null) {
				idByParaId.put(paraId.toUpperCase(Locale.ROOT), id);
				CTCommentEx ex = extended.get(paraId.toUpperCase(Locale.ROOT));
				if (ex != null) {
					comment.resolved = "1".equals(ex.getDone()) || "true".equals(ex.getDone());
					comment.parentId = ex.getParaIdParent();
				}
			}
			out.put(id, comment);
		}
		for (TextComment comment : out.values()) {
			if (comment.parentId == null) continue;
			String parent = idByParaId.get(comment.parentId.toUpperCase(Locale.ROOT));
			comment.parentId = parent;
			if (parent != null && out.get(parent) != null) out.get(parent).replies.add(comment);
		}
	}

	/**
	 * The ids of the comments anchored in the document: a {@code w:commentReference} or a
	 * {@code w:commentRangeStart} in the main document, a header or footer, or a note, in any branch.
	 */
	private static Set<String> anchoredComments(MainDocumentPart main) {
		Set<String> ids = new java.util.HashSet<String>();
		List<Object> roots = new ArrayList<Object>();
		roots.add(main.getJaxbElement());
		RelationshipsPart rels = main.getRelationshipsPart(false);
		if (rels != null) {
			for (org.docx4j.relationships.Relationship rel : rels.getRelationships().getRelationship()) {
				Part p = "External".equals(rel.getTargetMode()) ? null : rels.getPart(rel);
				if (p instanceof HeaderPart) roots.add(((HeaderPart) p).getJaxbElement());
				else if (p instanceof FooterPart) roots.add(((FooterPart) p).getJaxbElement());
				else if (p instanceof FootnotesPart) roots.add(((FootnotesPart) p).getJaxbElement().getFootnote());
				else if (p instanceof EndnotesPart) roots.add(((EndnotesPart) p).getJaxbElement().getEndnote());
			}
		}
		org.docx4j.TraversalUtil.CallbackImpl anchors = new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				Object v = unwrap(o);
				BigInteger id = v instanceof R.CommentReference ? ((R.CommentReference) v).getId()
						: v instanceof org.docx4j.wml.CommentRangeStart ? ((org.docx4j.wml.CommentRangeStart) v).getId() : null;
				if (id != null) ids.add(id.toString());
				return null;
			}
		};
		anchors.setMcMode(org.docx4j.jaxb.McMode.ALL);
		for (Object root : roots) if (root != null) new org.docx4j.TraversalUtil(root, anchors);
		return ids;
	}

	private static String lastParaId(List<Object> content) {
		String id = null;
		for (Object o : content) {
			Object v = unwrap(o);
			if (v instanceof P && ((P) v).getParaId() != null) id = ((P) v).getParaId();
			else if (v instanceof AlternateContent) {
				// the comment's last paragraph may stand in a branch (CR-051 section 10.3, item 8)
				String inner = lastParaId(McSelection.select(McPurpose.TEXT, (AlternateContent) v));
				if (inner != null) id = inner;
			}
		}
		return id;
	}

	// --- the view --------------------------------------------------------------------------------------

	/** Whether the view removes a list member (always false in markup and on a resolved copy). */
	boolean removes(Object item) {
		if (plan != null) return plan.removes(item);
		if (mode == Mode.PLANLESS && view != TextView.MARKUP) {
			CTTrackChange holder = revisionHolder(item);
			if (holder == null) return false;
			boolean ins = item instanceof JAXBElement ? isInsertionName(((JAXBElement<?>) item).getName().getLocalPart())
					: unwrap(item) instanceof RunIns;
			return ins != (view == TextView.ACCEPTED);
		}
		return false;
	}

	/** Whether the view takes a content control away and leaves its content (an inserted one, rejected). */
	private boolean unwraps(Object item) {
		return plan != null && plan.unwraps(item);
	}

	private boolean joinsNext(P p) {
		return plan != null && plan.removesMark(p);
	}

	/** The {@code w:ins}, {@code w:del}, {@code w:moveFrom} or {@code w:moveTo} a list member is, or null. */
	static CTTrackChange revisionHolder(Object item) {
		Object v = unwrap(item);
		if (v instanceof RunIns || v instanceof RunDel || v instanceof RunTrackChange) return (CTTrackChange) v;
		return null;
	}

	private static List<Object> runItemsOf(Object v) {
		if (v instanceof RunIns) return ((RunIns) v).getCustomXmlOrSmartTagOrSdt();
		if (v instanceof RunDel) return ((RunDel) v).getCustomXmlOrSmartTagOrSdt();
		if (v instanceof RunTrackChange) return ((RunTrackChange) v).getAccOrBarOrBox();
		if (v instanceof P.Hyperlink) return ((P.Hyperlink) v).getContent();
		if (v instanceof org.docx4j.wml.CTSmartTagRun) return ((org.docx4j.wml.CTSmartTagRun) v).getContent();
		if (v instanceof org.docx4j.wml.CTCustomXmlRun) return ((org.docx4j.wml.CTCustomXmlRun) v).getContent();
		if (v instanceof P.Bdo) return ((P.Bdo) v).getContent();
		if (v instanceof P.Dir) return ((P.Dir) v).getContent();
		if (v instanceof org.docx4j.wml.ContentAccessor && !(v instanceof P)) return ((org.docx4j.wml.ContentAccessor) v).getContent();
		return null;
	}

	private static List<Object> blockItems(Object v) {
		if (v instanceof Body) return ((Body) v).getContent();
		if (v instanceof org.docx4j.wml.Hdr) return ((org.docx4j.wml.Hdr) v).getContent();
		if (v instanceof org.docx4j.wml.Ftr) return ((org.docx4j.wml.Ftr) v).getContent();
		if (v instanceof CTFtnEdn) return ((CTFtnEdn) v).getContent();
		if (v instanceof Comments.Comment) return ((Comments.Comment) v).getContent();
		if (v instanceof Tc) return ((Tc) v).getContent();
		if (v instanceof CTTxbxContent) return ((CTTxbxContent) v).getContent();
		if (v instanceof org.docx4j.wml.CTCustomXmlBlock) return ((org.docx4j.wml.CTCustomXmlBlock) v).getContent();
		if (v instanceof org.docx4j.wml.CTCustomXmlRow) return ((org.docx4j.wml.CTCustomXmlRow) v).getContent();
		if (v instanceof org.docx4j.wml.CTCustomXmlCell) return ((org.docx4j.wml.CTCustomXmlCell) v).getContent();
		if (v instanceof org.docx4j.wml.Document && ((org.docx4j.wml.Document) v).getBody() != null) return ((org.docx4j.wml.Document) v).getBody().getContent();
		return null;
	}

	/** A range marker, bookmark, permission or proofing mark: a join passes over it. */
	private static boolean isMarker(Object item) {
		String name = name(item);
		return name.endsWith("RangeStart") || name.endsWith("RangeEnd") || "bookmarkStart".equals(name)
				|| "bookmarkEnd".equals(name) || "permStart".equals(name) || "permEnd".equals(name)
				|| "proofErr".equals(name);
	}

	private static boolean showingPlaceholder(SdtPr sdtPr) {
		if (sdtPr == null) return false;
		for (Object o : sdtPr.getRPrOrAliasOrLock()) {
			if (o instanceof JAXBElement && "showingPlcHdr".equals(((JAXBElement<?>) o).getName().getLocalPart())) {
				Object v = ((JAXBElement<?>) o).getValue();
				return !(v instanceof org.docx4j.wml.BooleanDefaultTrue) || ((org.docx4j.wml.BooleanDefaultTrue) v).isVal();
			}
		}
		return false;
	}

	/** {@code w:customMarkFollows} as written: the generated getter answers true when it is absent. */
	private static boolean customMarkFollows(CTFtnEdnRef ref) {
		return written(ref, "customMarkFollows");
	}

	/**
	 * An on/off attribute as written, false when absent: the generated getters of
	 * {@code w:customMarkFollows} and {@code w:fldLock} answer true when it is absent.
	 */
	private static boolean written(Object o, String attribute) {
		try {
			Field f = o.getClass().getDeclaredField(attribute);
			f.setAccessible(true);
			return Boolean.TRUE.equals(f.get(o));
		} catch (ReflectiveOperationException | RuntimeException e) {
			return false;
		}
	}

	// --- hidden text (REQ-507) ---------------------------------------------------------------------------

	private boolean isHiddenRun(R r, P p) {
		RPr rPr = r.getRPr();
		if (rPr != null && rPr.getVanish() != null) return rPr.getVanish().isVal();
		RPr style = styleRPr(r, p);
		return style != null && style.getVanish() != null && style.getVanish().isVal();
	}

	/**
	 * A run's formatting beyond its paragraph style's (CR-046 phase 3): bold, italic, struck through,
	 * monospaced, each as the run's own property gives it, else its styles', where the paragraph style
	 * alone doesn't give it. A run carrying only that.
	 */
	private TextRun formatOf(R r, P p) {
		RPr own = r.getRPr();
		RPr styles = styleRPr(r, p);
		RPr paragraph = paragraphStyleRPr(p);
		TextRun f = new TextRun(TextRun.Kind.TEXT, "", null);
		f.bold = on(own == null ? null : own.getB(), styles == null ? null : styles.getB())
				&& !on(null, paragraph == null ? null : paragraph.getB());
		f.italic = on(own == null ? null : own.getI(), styles == null ? null : styles.getI())
				&& !on(null, paragraph == null ? null : paragraph.getI());
		f.strike = on(own == null ? null : own.getStrike(), styles == null ? null : styles.getStrike())
				&& !on(null, paragraph == null ? null : paragraph.getStrike());
		f.monospace = isMonospace(runFont(r, p)) && !isMonospace(asciiFont(paragraph));
		return f;
	}

	/** An on/off property: the run's own where it has one, else its styles'. */
	private static boolean on(org.docx4j.wml.BooleanDefaultTrue own, org.docx4j.wml.BooleanDefaultTrue styles) {
		if (own != null) return own.isVal();
		return styles != null && styles.isVal();
	}

	/** The run's font for {@code monospace}: its own {@code w:rFonts/@w:ascii}, else its styles'. */
	private String runFont(R r, P p) {
		String font = asciiFont(r.getRPr());
		if (font != null) return font;
		return asciiFont(styleRPr(r, p));
	}

	/** The effective run properties of the run's paragraph style and character style, cached by their ids; null without styles. */
	private RPr styleRPr(R r, P p) {
		return styleRPr(r.getRPr() == null ? null : r.getRPr().getRStyle(), p);
	}

	/** The effective run properties of the paragraph's style alone (with the document defaults); null without styles. */
	private RPr paragraphStyleRPr(P p) {
		return styleRPr((org.docx4j.wml.RStyle) null, p);
	}

	private RPr styleRPr(org.docx4j.wml.RStyle rStyleElement, P p) {
		if (propertyResolver == null) return null;
		String pStyle = p.getPPr() != null && p.getPPr().getPStyle() != null ? p.getPPr().getPStyle().getVal() : null;
		String rStyle = rStyleElement == null ? null : rStyleElement.getVal();
		String key = pStyle + "|" + rStyle;
		if (styleRPr.containsKey(key)) return styleRPr.get(key);
		RPr effective = null;
		try {
			RPr styleOnly = null;
			if (rStyle != null) {
				styleOnly = new RPr();
				styleOnly.setRStyle(rStyleElement);
			}
			PPr pPrStyle = null;
			if (pStyle != null) {
				pPrStyle = new PPr();
				pPrStyle.setPStyle(p.getPPr().getPStyle());
			}
			effective = propertyResolver.getEffectiveRPr(styleOnly, pPrStyle);
		} catch (Exception e) {
			log.debug("Couldn't resolve a run's style properties: " + e.getMessage());
		}
		styleRPr.put(key, effective);
		return effective;
	}

	private boolean isHiddenMark(PPr pPr) {
		if (pPr != null && pPr.getRPr() != null && pPr.getRPr().getVanish() != null) return pPr.getRPr().getVanish().isVal();
		if (propertyResolver == null) return false;
		try {
			RPr effective = propertyResolver.getEffectiveParagraphMarkRPr(pPr);
			return effective != null && effective.getVanish() != null && effective.getVanish().isVal();
		} catch (Exception e) {
			return false;
		}
	}

	// --- names and the reflective walk -------------------------------------------------------------------

	static Object unwrap(Object item) {
		return item instanceof JAXBElement ? ((JAXBElement<?>) item).getValue() : item;
	}

	private static final Map<Class<?>, String> ROOT_NAMES = new ConcurrentHashMap<Class<?>, String>();

	/** A list member's element name: its {@code JAXBElement}'s, else its class's {@code @XmlRootElement}. */
	static String name(Object item) {
		if (item instanceof JAXBElement) return ((JAXBElement<?>) item).getName().getLocalPart();
		if (item == null) return "";
		return ROOT_NAMES.computeIfAbsent(item.getClass(), c -> {
			XmlRootElement root = c.getAnnotation(XmlRootElement.class);
			return root == null ? "" : root.name();
		});
	}

	/** What the reflective walk does at each object. */
	abstract static class Visitor {
		/** A JAXB object reached; false to go no deeper. */
		boolean enter(Object value) { return true; }
		/** A list member or field value reached, before its value is entered; false to skip it. */
		boolean item(Object item) { return true; }
	}

	private static final Field[] NONE = new Field[0];
	private static final Map<Class<?>, Field[]> FIELDS = new ConcurrentHashMap<Class<?>, Field[]>();

	/** A reflective walk of the JAXB objects under {@code value}, in field and list order; DOM is not entered. */
	static void walk(Object value, int depth, Visitor visitor) {
		if (value == null || depth > 200 || !isJaxb(value.getClass())) return;
		if (!visitor.enter(value)) return;
		for (Field f : fieldsOf(value.getClass())) {
			Object fv;
			try {
				fv = f.get(value);
			} catch (IllegalAccessException e) {
				continue;
			}
			if (fv instanceof List) {
				for (Object o : new ArrayList<Object>((List<?>) fv)) {
					if (!visitor.item(o)) continue;
					walk(unwrap(o), depth + 1, visitor);
				}
			} else if (fv != null) {
				if (!visitor.item(fv)) continue;
				walk(unwrap(fv), depth + 1, visitor);
			}
		}
	}

	/** The JAXB objects directly under {@code value}: its list members and object fields. */
	static List<Object> children(Object value) {
		List<Object> out = new ArrayList<Object>();
		if (value == null || !isJaxb(value.getClass())) return out;
		for (Field f : fieldsOf(value.getClass())) {
			Object fv;
			try {
				fv = f.get(value);
			} catch (IllegalAccessException e) {
				continue;
			}
			if (fv instanceof List) out.addAll((List<?>) fv);
			else if (fv != null) out.add(fv);
		}
		return out;
	}

	private static boolean isJaxb(Class<?> c) {
		Package p = c.getPackage();
		return p != null && (p.getName().startsWith("org.docx4j.") || p.getName().startsWith("org.pptx4j.")
				|| p.getName().startsWith("org.xlsx4j.") || p.getName().startsWith("org.opendope."))
				&& !p.getName().startsWith("org.docx4j.openpackaging");
	}

	private static Field[] fieldsOf(Class<?> c) {
		return FIELDS.computeIfAbsent(c, k -> {
			List<Class<?>> chain = new ArrayList<Class<?>>();
			for (Class<?> x = k; x != null && x != Object.class; x = x.getSuperclass()) chain.add(0, x);
			List<Field> out = new ArrayList<Field>();
			for (Class<?> x : chain) {
				for (Field f : x.getDeclaredFields()) {
					int m = f.getModifiers();
					if (Modifier.isStatic(m) || Modifier.isTransient(m)) continue;
					if ("parent".equals(f.getName())) continue;
					Class<?> t = f.getType();
					if (t.isPrimitive() || t == String.class || Number.class.isAssignableFrom(t) || t == Boolean.class || t.isEnum()
							|| XMLGregorianCalendar.class.isAssignableFrom(t) || t == QName.class || t == byte[].class) continue;
					try {
						f.setAccessible(true);
					} catch (RuntimeException e) {
						continue;
					}
					out.add(f);
				}
			}
			return out.isEmpty() ? NONE : out.toArray(NONE);
		});
	}

	static boolean isMonospace(String font) {
		return font != null && MONOSPACE.contains(font.trim().toLowerCase(Locale.ROOT));
	}

	static String asciiFont(RPr rPr) {
		if (rPr == null) return null;
		RFonts f = rPr.getRFonts();
		return f == null ? null : f.getAscii();
	}
}
