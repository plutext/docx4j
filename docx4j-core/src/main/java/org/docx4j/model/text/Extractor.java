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

import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.NumberingState;
import org.docx4j.model.listnumbering.NumberingStates;
import org.docx4j.model.revisions.ResolvedView;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.w15.CTCommentEx;
import org.docx4j.wml.Body;
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

	/** Fonts whose glyphs are all one width, for the block model's {@code monospace}. */
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
		if (pkg != null && pkg.getMainDocumentPart() != null) {
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
		metadata.put("fields_update_on_open", fieldsUpdateOnOpen());
		if (main == null || main.getJaxbElement() == null || main.getJaxbElement().getBody() == null) {
			return new TextResult(out, comments, warnings, metadata, options);
		}
		Body body = main.getJaxbElement().getBody();

		// the main story, each text box after its anchor; sections recorded as they end
		Story story = new Story(TextBlock.Story.MAIN, "body", main);
		story.sections = new ArrayList<SectPr>();
		List<TextBlock> mainBlocks = new ArrayList<TextBlock>();
		blocks(body.getContent(), "body", story, mainBlocks);
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

		// comments, rendered at their references (REQ-504)
		if (commentsIncluded()) comments(main, comments);

		return new TextResult(out, comments, warnings, metadata, options);
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
		/** Text boxes found since the last flush, to follow their anchor (or its table). */
		final List<Box> pending = new ArrayList<Box>();
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

	/** A text box: its content, and the address it is reached by. */
	static final class Box {
		final CTTxbxContent content;
		final String prefix;
		Box(CTTxbxContent content, String prefix) {
			this.content = content;
			this.prefix = prefix;
		}
	}

	/** An open complex field. */
	static final class FieldState {
		final StringBuilder instr = new StringBuilder();
		final StringBuilder result = new StringBuilder();
		boolean separated;
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
			} else if (v instanceof SdtElement) {
				SdtElement sdt = (SdtElement) v;
				boolean placeholder = showingPlaceholder(sdt.getSdtPr());
				if (placeholder && !options.isPlaceholders()) continue;
				if (sdt.getSdtContent() != null) blocks(sdt.getSdtContent().getContent(), address, story, sink,
						placeholder ? state.withPlaceholder() : state);
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
		for (int c = 0; c < chain.size(); c++) {
			P p = chain.get(c);
			block.revisions += markRevisions(p);
			Paragraph para = new Paragraph(block, p, addresses.get(c), story);
			runs(p.getContent(), para, state);
			if (para.monospace != null) monospace = monospace == null ? para.monospace : monospace && para.monospace;
		}
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
		if (options.isListLabels() && pkg != null) block.listLabel = listLabel(last, story);
		block.section = story.sections == null ? 0 : story.sections.size();
		sink.add(block);
		flushIfOutsideTable(story, sink);
		endSection(last, story);
	}

	private void endSection(P last, Story story) {
		if (story.sections != null && story.tableDepth == 0 && last.getPPr() != null && last.getPPr().getSectPr() != null) {
			story.sections.add(last.getPPr().getSectPr());
		}
	}

	private void flushIfOutsideTable(Story story, List<TextBlock> sink) {
		if (story.tableDepth > 0 || story.pending.isEmpty()) return;
		List<Box> boxes = new ArrayList<Box>(story.pending);
		story.pending.clear();
		for (Box box : boxes) {
			Story inner = new Story(TextBlock.Story.TEXT_BOX, box.prefix, story.part);
			inner.noteType = story.noteType;
			inner.noteNumber = story.noteNumber;
			inner.noteId = story.noteId;
			List<TextBlock> boxBlocks = new ArrayList<TextBlock>();
			blocks(box.content.getContent(), box.prefix, inner, boxBlocks);
			int section = story.sections == null ? 0 : story.sections.size();
			for (TextBlock b : boxBlocks) b.section = section;
			sink.addAll(boxBlocks);
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
		if (propertyResolver == null) return;
		try {
			PPr effective = propertyResolver.getEffectivePPr(p.getPPr());
			if (effective == null) return;
			PPrBase.Ind ind = effective.getInd();
			if (ind != null) {
				block.indentLeft = twips(ind.getLeft() != null ? ind.getLeft() : ind.getStart());
				block.indentHanging = twips(ind.getHanging());
				block.indentFirstLine = twips(ind.getFirstLine());
			}
			Jc jc = effective.getJc();
			if (jc != null && jc.getVal() != null) block.alignment = jc.getVal().value();
		} catch (Exception e) {
			log.debug("Couldn't resolve paragraph properties of " + block.address + ": " + e.getMessage());
		}
	}

	private static Integer twips(BigInteger value) {
		return value == null ? null : value.intValue();
	}

	private String listLabel(P p, Story story) {
		try {
			Emulator.ResultTriple triple = Emulator.getNumber(pkg, p.getPPr(), story.numbering());
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
		if (!block.rows.isEmpty()) sink.add(block); // a table left with no rows goes (CR-043)
		flushIfOutsideTable(story, sink);
	}

	private void rows(List<Object> items, String prefix, Story story, TextBlock table, RunState state) {
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			if (removes(item)) continue;
			Object v = unwrap(item);
			String address = prefix + "/" + i;
			if (v instanceof Tr) {
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
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			if (removes(item)) continue;
			Object v = unwrap(item);
			String address = prefix + "/" + i;
			if (v instanceof Tc) {
				Tc tc = (Tc) v;
				TextCell cell = new TextCell(address);
				TcPr tcPr = tc.getTcPr();
				RunState cellState = state;
				if (tcPr != null) {
					if (tcPr.getGridSpan() != null && tcPr.getGridSpan().getVal() != null) cell.gridSpan = tcPr.getGridSpan().getVal().intValue();
					if (tcPr.getVMerge() != null && !"restart".equals(tcPr.getVMerge().getVal())) cell.mergeContinue = true;
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
		int boxes;
		/** Whether every run with text so far is in a monospaced font; null before the first. */
		Boolean monospace;
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
			} else if (v instanceof SdtElement) {
				SdtElement sdt = (SdtElement) v;
				boolean placeholder = showingPlaceholder(sdt.getSdtPr());
				if (placeholder && !options.isPlaceholders()) continue;
				if (sdt.getSdtContent() != null) runs(sdt.getSdtContent().getContent(), para, placeholder ? state.withPlaceholder() : state);
			} else if (v instanceof CTSimpleField) {
				simpleField((CTSimpleField) v, para, state);
			} else if (isMath(v)) {
				math(v, para, state);
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
		for (Object item : r.getContent()) runItem(item, r, hidden, para, state);
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
				fields.push(new FieldState());
			} else if (type == STFldCharType.SEPARATE) {
				if (!fields.isEmpty()) fields.peek().separated = true;
			} else if (type == STFldCharType.END) {
				if (!fields.isEmpty()) {
					FieldState f = fields.pop();
					endField(f.instr.toString(), f.separated ? f.result.toString() : null, para);
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
			if (options.isFieldCodes()) addText(para, s, TextRun.Kind.FIELD_CODE, hidden, state);
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
			if (inCode(para.story)) return;
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
			if (!commentsIncluded() || inCode(para.story)) return;
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
			if (options.isTextBoxes() && !inCode(para.story) && !(hidden && !options.isHidden())) findBoxes(v, para);
			return;
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
		if (!(hidden && !options.isHidden())) {
			for (FieldState f : para.story.fields) f.result.append(s);
		}
		addText(para, s, TextRun.Kind.TEXT, hidden, state);
	}

	private void addText(Paragraph para, String s, TextRun.Kind kind, boolean hidden, RunState state) {
		List<TextRun> runs = para.block.runs;
		if (!runs.isEmpty()) {
			TextRun last = runs.get(runs.size() - 1);
			if (last.kind == kind && last.hidden == hidden && last.placeholder == state.placeholder
					&& java.util.Objects.equals(last.revision, state.revision)) {
				TextRun merged = new TextRun(kind, last.text + s, last.revision);
				merged.hidden = hidden;
				merged.placeholder = last.placeholder;
				runs.set(runs.size() - 1, merged);
				return;
			}
		}
		TextRun run = new TextRun(kind, s, state.revision);
		run.hidden = hidden;
		run.placeholder = state.placeholder;
		runs.add(run);
	}

	private void endField(String instr, String result, Paragraph para) {
		para.block.fields.add(new TextField(instr, result));
		if (result == null) warn(TextWarning.FIELD_NO_RESULT, para.block.address, null);
	}

	private void simpleField(CTSimpleField field, Paragraph para, RunState state) {
		String instr = field.getInstr() == null ? "" : field.getInstr();
		if (options.isFieldCodes() && !inCode(para.story)) addText(para, instr, TextRun.Kind.FIELD_CODE, false, state);
		int before = para.block.runs.size();
		String text = resultOf(field.getContent(), para, state);
		boolean hasResult = text != null && !text.isEmpty() || para.block.runs.size() > before;
		para.block.fields.add(new TextField(instr, hasResult ? text : null));
		if (!hasResult) warn(TextWarning.FIELD_NO_RESULT, para.block.address, null);
	}

	/** The runs of a simple field's result, added to the paragraph; their text. */
	private String resultOf(List<Object> content, Paragraph para, RunState state) {
		FieldState capture = new FieldState();
		capture.separated = true;
		para.story.fields.addLast(capture);
		try {
			runs(content, para, state);
		} finally {
			para.story.fields.removeLastOccurrence(capture);
		}
		return capture.result.toString();
	}

	// --- math: phase 1 reads the runs' text (LaTeX is CR-046 phase 2, REQ-604) ------------------------

	private static boolean isMath(Object v) {
		return v instanceof org.docx4j.math.CTOMath || v instanceof org.docx4j.math.CTOMathPara;
	}

	private void math(Object v, Paragraph para, RunState state) {
		StringBuilder sb = new StringBuilder();
		mathText(v, sb, 0);
		text(para, sb.toString(), false, state);
	}

	private void mathText(Object v, StringBuilder sb, int depth) {
		if (v == null || depth > 64) return;
		if (removes(v)) return;
		if (v instanceof org.docx4j.math.CTR) {
			for (Object o : ((org.docx4j.math.CTR) v).getContent()) {
				Object ov = unwrap(o);
				if (removes(o)) continue;
				if (ov instanceof org.docx4j.wml.CTMathRunTrackChange) {
					// a plan has removed what its view does not keep; a resolved copy has none left
					boolean ins = "ins".equals(name(o));
					boolean shown = mode != Mode.PLANLESS || view == TextView.MARKUP || ins == (view == TextView.ACCEPTED);
					if (shown) for (Object inner : ((org.docx4j.wml.CTMathRunTrackChange) ov).getRest()) mathItem(inner, sb);
					continue;
				}
				mathItem(o, sb);
			}
			return;
		}
		if (v instanceof org.docx4j.math.CTCtrlPr) return;
		for (Object child : children(v)) mathText(unwrap(child), sb, depth + 1);
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

	// --- text boxes (REQ-518) ------------------------------------------------------------------------

	/** The text boxes under a run's drawing, picture or object, in document order, branches selected for text. */
	private void findBoxes(Object v, Paragraph para) {
		walk(v, 0, new Visitor() {
			@Override public boolean enter(Object value) {
				if (value instanceof CTTxbxContent) {
					para.story.pending.add(new Box((CTTxbxContent) value, para.address + "/textbox:" + para.boxes++));
					return false;
				}
				if (value instanceof AlternateContent) {
					AlternateContent ac = (AlternateContent) value;
					compareBranches(ac, para.block.address);
					for (Object o : McSelection.select(McPurpose.TEXT, ac)) walk(unwrap(o), 1, this);
					return false;
				}
				return true;
			}
		});
	}

	// --- markup compatibility: REQ-307 -----------------------------------------------------------------

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
		TextWarning w = new TextWarning(kind, address, branch);
		if (warned.add(w)) warnings.add(w);
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
		Object root = null;
		if (part instanceof HeaderPart) root = ((HeaderPart) part).getJaxbElement();
		else if (part instanceof FooterPart) root = ((FooterPart) part).getJaxbElement();
		if (root == null) return;
		String prefix = (kind == TextBlock.Story.HEADER ? "header:" : "footer:") + rId;
		Story story = new Story(kind, prefix, part);
		blocks(blockItems(root), prefix, story, out);
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
			String prefix = type + ":" + e.getKey();
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
		for (Comments.Comment c : part.getJaxbElement().getComment()) {
			if (plan != null && plan.removesComment(c.getId())) continue;
			String id = c.getId() == null ? "" : c.getId().toString();
			TextComment comment = new TextComment(id);
			comment.author = c.getAuthor();
			comment.initials = c.getInitials();
			comment.date = c.getDate() == null ? null : c.getDate().toXMLFormat();
			String prefix = "comment:" + id;
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

	private static String lastParaId(List<Object> content) {
		String id = null;
		for (Object o : content) {
			Object v = unwrap(o);
			if (v instanceof P && ((P) v).getParaId() != null) id = ((P) v).getParaId();
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
		try {
			Field f = CTFtnEdnRef.class.getDeclaredField("customMarkFollows");
			f.setAccessible(true);
			Object v = f.get(ref);
			return Boolean.TRUE.equals(v);
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

	/** The run's font for {@code monospace}: its own {@code w:rFonts/@w:ascii}, else its styles'. */
	private String runFont(R r, P p) {
		String font = asciiFont(r.getRPr());
		if (font != null) return font;
		return asciiFont(styleRPr(r, p));
	}

	/** The effective run properties of the run's paragraph style and character style, cached by their ids; null without styles. */
	private RPr styleRPr(R r, P p) {
		if (propertyResolver == null) return null;
		RPr rPr = r.getRPr();
		String pStyle = p.getPPr() != null && p.getPPr().getPStyle() != null ? p.getPPr().getPStyle().getVal() : null;
		String rStyle = rPr != null && rPr.getRStyle() != null ? rPr.getRStyle().getVal() : null;
		String key = pStyle + "|" + rStyle;
		if (styleRPr.containsKey(key)) return styleRPr.get(key);
		RPr effective = null;
		try {
			RPr styleOnly = null;
			if (rStyle != null) {
				styleOnly = new RPr();
				styleOnly.setRStyle(rPr.getRStyle());
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
