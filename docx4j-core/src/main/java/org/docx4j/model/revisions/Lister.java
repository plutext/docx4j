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
package org.docx4j.model.revisions;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.docx4j.math.CTCtrlPr;
import org.docx4j.math.CTOMathArg;
import org.docx4j.math.CTR;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSplice;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.revisions.TrackedChange.CellT;
import org.docx4j.model.revisions.TrackedChange.FieldT;
import org.docx4j.model.revisions.TrackedChange.GroupKind;
import org.docx4j.model.revisions.TrackedChange.GroupT;
import org.docx4j.model.revisions.TrackedChange.HolderT;
import org.docx4j.model.revisions.TrackedChange.MarkPropsT;
import org.docx4j.model.revisions.TrackedChange.MarkT;
import org.docx4j.model.revisions.TrackedChange.MathRunT;
import org.docx4j.model.revisions.TrackedChange.MathStructT;
import org.docx4j.model.revisions.TrackedChange.Para;
import org.docx4j.model.revisions.TrackedChange.ParaPropsT;
import org.docx4j.model.revisions.TrackedChange.RowT;
import org.docx4j.model.revisions.TrackedChange.RunPropsT;
import org.docx4j.model.revisions.TrackedChange.RunT;
import org.docx4j.model.revisions.TrackedChange.SectionT;
import org.docx4j.model.revisions.TrackedChange.TableT;
import org.docx4j.model.revisions.Trees.Revision;
import org.docx4j.model.revisions.Trees.View;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CTMathRunTrackChange;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Comments;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;

/**
 * Lists a package's tracked changes (CR-043 section 3.1): each story walked in document order, its
 * single changes ("pieces") grouped as Office JS groups them. A port of docx4j-core-ts's
 * {@code Body.getTrackedChanges} and {@code TrackedChange.mts}'s collecting half, extended to every
 * story part, text boxes (both {@code mc:AlternateContent} branches) and math.
 */
final class Lister {

	/** Between two changes: something unrevised that keeps them apart. */
	static final Object BREAK = new Object() {
		@Override public String toString() { return "BREAK"; }
	};

	private final WordprocessingMLPackage pkg;
	/**
	 * True for the public listing, where a text box's {@code mc:Fallback} changes are attached to
	 * the {@code mc:Choice}'s as twins; false for acceptAll's, where every branch's are listed.
	 */
	private final boolean attachTwins;

	Lister(WordprocessingMLPackage pkg, boolean attachTwins) {
		this.pkg = pkg;
		this.attachTwins = attachTwins;
	}

	/** Every change, story by story: main document, footnotes, endnotes, comments, headers and footers. */
	List<TrackedChange> list() {
		List<TrackedChange> out = new ArrayList<TrackedChange>();
		for (Story story : stories()) listStory(story, out);
		return out;
	}

	// --- the stories -------------------------------------------------------------------------------

	List<Story> stories() {
		List<Story> out = new ArrayList<Story>();
		MainDocumentPart main = pkg.getMainDocumentPart();
		if (main == null || main.getJaxbElement() == null) return out;
		Body body = main.getJaxbElement().getBody();
		if (body != null) out.add(new Story(pkg, main, body.getContent(), body, false));
		FootnotesPart footnotes = main.getFootnotesPart();
		if (footnotes != null && footnotes.getJaxbElement() != null) {
			for (CTFtnEdn note : footnotes.getJaxbElement().getFootnote()) out.add(new Story(pkg, footnotes, note.getContent(), note, false));
		}
		EndnotesPart endnotes = main.getEndNotesPart();
		if (endnotes != null && endnotes.getJaxbElement() != null) {
			for (CTFtnEdn note : endnotes.getJaxbElement().getEndnote()) out.add(new Story(pkg, endnotes, note.getContent(), note, false));
		}
		CommentsPart comments = main.getCommentsPart();
		if (comments != null && comments.getJaxbElement() != null) {
			for (Comments.Comment comment : comments.getJaxbElement().getComment()) out.add(new Story(pkg, comments, comment.getContent(), comment, false));
		}
		for (JaxbXmlPart<?> part : headersAndFooters(main)) {
			Object root = part.getJaxbElement();
			List<Object> items = Trees.blockItems(root);
			if (items != null) out.add(new Story(pkg, part, items, root, false));
		}
		return out;
	}

	/** The headers and footers in the order the sections reference them, then any the main part relates to and none references. */
	private static List<JaxbXmlPart<?>> headersAndFooters(MainDocumentPart main) {
		Set<JaxbXmlPart<?>> out = new LinkedHashSet<JaxbXmlPart<?>>();
		RelationshipsPart rels = main.getRelationshipsPart(false);
		if (rels == null) return new ArrayList<JaxbXmlPart<?>>();
		List<SectPr> sections = new ArrayList<SectPr>();
		Body body = main.getJaxbElement().getBody();
		Trees.walk(body, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (value instanceof SectPr) { sections.add((SectPr) value); return false; }
				return !(value instanceof R);
			}
		});
		for (SectPr sectPr : sections) {
			for (CTRel ref : sectPr.getEGHdrFtrReferences()) {
				Part part = ref.getId() == null ? null : rels.getPart(ref.getId());
				if (part instanceof HeaderPart || part instanceof FooterPart) out.add((JaxbXmlPart<?>) part);
			}
		}
		for (Relationship rel : rels.getRelationships().getRelationship()) {
			if ("External".equals(rel.getTargetMode())) continue;
			Part part = rels.getPart(rel);
			if (part instanceof HeaderPart || part instanceof FooterPart) out.add((JaxbXmlPart<?>) part);
		}
		return new ArrayList<JaxbXmlPart<?>>(out);
	}

	/** A text box found in a story, with its twins in the other branches of the {@code mc:AlternateContent} it is in. */
	private static final class Box {
		final Story story;
		final List<Story> twins = new ArrayList<Story>();
		Box(Story story) {
			this.story = story;
		}
	}

	/** What the walk of one story carries. */
	private static final class Ctx {
		final Story story;
		final List<Box> boxes = new ArrayList<Box>();
		Ctx(Story story) {
			this.story = story;
		}
	}

	private void listStory(Story story, List<TrackedChange> out) {
		Ctx ctx = new Ctx(story);
		List<Object> tokens = new ArrayList<Object>();
		visitBlocks(story.root, tokens, ctx);
		if (story.rootOwner instanceof Body) {
			SectPr sectPr = ((Body) story.rootOwner).getSectPr();
			if (sectPr != null && sectPr.getSectPrChange() != null) {
				tokens.add(BREAK);
				tokens.add(change(new SectionT(sectPr, sectionText(story.root, story.root.size())), null, ctx));
			}
		}
		out.addAll(groupTouching(tokens, ctx));
		// a text box is a story of its own (Word's text frame story), listed after the one it is in
		for (Box box : ctx.boxes) {
			List<TrackedChange> primary = new ArrayList<TrackedChange>();
			listStory(box.story, primary);
			for (Story twin : box.twins) {
				List<TrackedChange> twinChanges = new ArrayList<TrackedChange>();
				listStory(twin, twinChanges);
				if (attachTwins) pairTwins(primary, twinChanges);
				else out.addAll(twinChanges);
			}
			out.addAll(primary);
		}
	}

	/**
	 * A text box's {@code mc:Fallback} changes attached to the {@code mc:Choice}'s, by position: the
	 * n-th change of a kind with the n-th of that kind, never by id (Word gives the twins different
	 * ids, C22; CR-043 section 10 item 2).
	 */
	private static void pairTwins(List<TrackedChange> primary, List<TrackedChange> twins) {
		Map<TrackedChange.Kind, List<TrackedChange>> byKind = new EnumMap<TrackedChange.Kind, List<TrackedChange>>(TrackedChange.Kind.class);
		for (TrackedChange twin : twins) {
			List<TrackedChange> list = byKind.get(twin.getKind());
			if (list == null) byKind.put(twin.getKind(), list = new ArrayList<TrackedChange>());
			list.add(twin);
		}
		Map<TrackedChange.Kind, Integer> seen = new EnumMap<TrackedChange.Kind, Integer>(TrackedChange.Kind.class);
		for (TrackedChange change : primary) {
			int n = seen.containsKey(change.getKind()) ? seen.get(change.getKind()) : 0;
			seen.put(change.getKind(), n + 1);
			List<TrackedChange> list = byKind.get(change.getKind());
			if (list != null && n < list.size()) change.twins.add(list.get(n));
		}
	}

	private TrackedChange change(TrackedChange.Target target, Para para, Ctx ctx) {
		return new TrackedChange(target, para, ctx.story);
	}

	// --- block level ------------------------------------------------------------------------------

	private void visitBlocks(List<Object> items, List<Object> into, Ctx ctx) {
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			Object v = Trees.unwrap(item);
			if (v instanceof P) {
				paragraphTokens(new Para((P) v, item, items), into, ctx);
				continue;
			}
			// a block control or custom XML element that is itself an insertion (check 30): one piece,
			// grouping with the insertions around and inside it
			TrackedChange inserted = holderInsertionAt(items, i, null, ctx);
			if (inserted != null) {
				into.add(inserted);
				visitBlocks(Trees.children(Trees.unwrap(items.get(i + 1))), into, ctx);
				i += 2;
				continue;
			}
			if (v instanceof Tbl) {
				tableTokens((Tbl) v, item, items, into, ctx);
				continue;
			}
			if (Trees.revisionOf(item) != null) {
				// run-level markup standing at block level (EG_BlockLevelElts admits it): a piece of its own
				into.add(BREAK);
				revisionTokens(null, items, i, into, ctx);
				into.add(BREAK);
				continue;
			}
			if (v instanceof AlternateContent) {
				// the branch Word reads, its changes kept apart; the other branches' as twins (CR-051 section 10.3, item 3)
				alternateBlocks((AlternateContent) v, into, ctx);
				continue;
			}
			// a block-level control or custom XML element keeps what is inside it apart; a marker
			// between paragraphs (a bookmark, a range end) keeps nothing apart
			List<Object> children = Trees.blockItems(v);
			if (children != null) {
				into.add(BREAK);
				visitBlocks(children, into, ctx);
				into.add(BREAK);
			}
		}
	}

	/** A row and where it stands. */
	static final class Row {
		final Tr tr;
		final Object item;
		final List<Object> owner;
		Row(Tr tr, Object item, List<Object> owner) {
			this.tr = tr;
			this.item = item;
			this.owner = owner;
		}
	}

	/**
	 * A table's rows, through row-level content controls and custom XML, and through a row-level
	 * {@code mc:AlternateContent} by the branch Word reads ({@link Trees#blockItems}).
	 */
	static List<Row> rowsOf(Tbl tbl) {
		List<Row> out = new ArrayList<Row>();
		rows(tbl.getContent(), out);
		return out;
	}

	private static void rows(List<Object> items, List<Row> out) {
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof Tr) out.add(new Row((Tr) v, item, items));
			else if (v != null && !(v instanceof Tc)) {
				List<Object> children = Trees.blockItems(v);
				if (children != null) rows(children, out);
			}
		}
	}

	/**
	 * Whether any branch of a table's content holds a row (CR-051 phase 2): the test for a table left
	 * with no rows, which {@link #rowsOf} cannot be, since it reads one branch of a row-level
	 * {@code mc:AlternateContent}, and a row in another branch still makes the table one.
	 */
	static boolean hasRows(Tbl tbl) {
		return hasRows(tbl.getContent());
	}

	private static boolean hasRows(List<Object> items) {
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof Tr) return true;
			if (v instanceof AlternateContent) {
				for (Object branch : McSelection.branches((AlternateContent) v, McMode.ALL)) {
					if (hasRows(McSplice.contentOf(branch))) return true;
				}
			} else if (v != null && !(v instanceof Tc)) {
				List<Object> children = Trees.blockItems(v);
				if (children != null && hasRows(children)) return true;
			}
		}
		return false;
	}

	/** A row's cells and where they stand, through cell-level content controls and custom XML, and through a cell-level element by the branch Word reads. */
	static List<Row> cellsOf(Tr tr) {
		List<Row> out = new ArrayList<Row>();
		cells(tr.getContent(), tr, out);
		return out;
	}

	private static void cells(List<Object> items, Tr tr, List<Row> out) {
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof Tc) out.add(new Row(tr, item, items));
			else if (v != null) {
				List<Object> children = Trees.blockItems(v);
				if (children != null) cells(children, tr, out);
			}
		}
	}

	private void tableTokens(Tbl tbl, Object tblItem, List<Object> tblOwner, List<Object> into, Ctx ctx) {
		// Its property records are one change of their own where the table stands (core-ts CR-002
		// section 35), kept apart; records that change nothing are not listed, and the sweep drops them.
		CTTrackChange record = Tables.recordOf(tbl);
		if (record != null) {
			into.add(BREAK);
			into.add(change(new TableT(tbl, record), null, ctx));
			into.add(BREAK);
		}
		rowItems(tbl.getContent(), tblItem, tblOwner, into, ctx);
	}

	/**
	 * A table's rows in order, through row-level content controls and custom XML; a row-level
	 * {@code mc:AlternateContent} kept whole (CR-051 phase 2) lists the branch Word reads, with the
	 * other branches' changes as twins ({@link #alternateRows}).
	 */
	private void rowItems(List<Object> items, Object tblItem, List<Object> tblOwner, List<Object> into, Ctx ctx) {
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof Tr) {
				rowTokens(new Row((Tr) v, item, items), tblItem, tblOwner, into, ctx);
			} else if (v instanceof AlternateContent) {
				alternateRows((AlternateContent) v, tblItem, tblOwner, into, ctx);
			} else if (v != null && !(v instanceof Tc)) {
				List<Object> children = Trees.blockItems(v);
				if (children != null) rowItems(children, tblItem, tblOwner, into, ctx);
			}
		}
	}

	private void rowTokens(Row row, Object tblItem, List<Object> tblOwner, List<Object> into, Ctx ctx) {
		// A table is no break of its own: an inserted or deleted row groups with the rows and the
		// inserted text touching it, as Office JS lists them (check 31).
		List<Object> tokens = new ArrayList<Object>();
		cellItems(row.tr.getContent(), row.tr, tokens, ctx);
		List<TrackedChange> inner = groupTouching(tokens, ctx);
		// A row that is itself a revision reports as one change, as Office JS does: the markup its
		// cells carry belongs to that change, which accepts or rejects it with the row.
		List<TrackedChange> rowChanges = new ArrayList<TrackedChange>();
		if (row.tr.getTrPr() != null && row.tr.getTrPr().getIns() != null) {
			rowChanges.add(change(new RowT(true, row.tr, row.item, row.owner, row.tr.getTrPr().getIns(), inner, tblItem, tblOwner), null, ctx));
		}
		if (row.tr.getTrPr() != null && row.tr.getTrPr().getDel() != null) {
			rowChanges.add(change(new RowT(false, row.tr, row.item, row.owner, row.tr.getTrPr().getDel(), inner, tblItem, tblOwner), null, ctx));
		}
		if (!rowChanges.isEmpty()) {
			into.addAll(rowChanges);
		} else {
			into.add(BREAK);
			for (TrackedChange c : inner) {
				into.add(c);
				into.add(BREAK);
			}
		}
	}

	/**
	 * A row's cells in order, through cell-level content controls and custom XML; a cell-level
	 * {@code mc:AlternateContent} kept whole (CR-051 phase 2) lists the branch Word reads, with the
	 * other branches' changes as twins ({@link #alternateCells}).
	 */
	private void cellItems(List<Object> items, Tr tr, List<Object> tokens, Ctx ctx) {
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof Tc) {
				cellTokens((Tc) v, tr, tokens, ctx);
			} else if (v instanceof AlternateContent) {
				alternateCells((AlternateContent) v, tr, tokens, ctx);
			} else if (v != null) {
				List<Object> children = Trees.blockItems(v);
				if (children != null) cellItems(children, tr, tokens, ctx);
			}
		}
	}

	private void cellTokens(Tc tc, Tr tr, List<Object> tokens, Ctx ctx) {
		TcPr tcPr = tc.getTcPr();
		if (tcPr != null && tcPr.getCellIns() != null) { tokens.add(change(new CellT(true, tc, tr), null, ctx)); tokens.add(BREAK); }
		if (tcPr != null && tcPr.getCellDel() != null) { tokens.add(change(new CellT(false, tc, tr), null, ctx)); tokens.add(BREAK); }
		visitBlocks(tc.getContent(), tokens, ctx);
		tokens.add(BREAK);
	}

	// --- paragraphs and run level --------------------------------------------------------------------

	/**
	 * A paragraph's single changes in document order, its mark last, with a BREAK wherever
	 * unrevised content stands between two of them (core-ts trackedChangeTokensOfParagraph).
	 */
	private void paragraphTokens(Para para, List<Object> out, Ctx ctx) {
		P p = para.p;
		PPr pPr = p.getPPr();
		if (pPr != null && pPr.getPPrChange() != null) out.add(change(new ParaPropsT(pPr.getPPrChange()), para, ctx));
		runLevel(para, p.getContent(), out, ctx);
		ParaRPr rPr = pPr == null ? null : pPr.getRPr();
		// a mark whose formatting changed is a piece, grouping with its runs' (check 27)
		if (rPr != null && rPr.getRPrChange() != null) out.add(change(new MarkPropsT(rPr.getRPrChange()), para, ctx));
		if (rPr != null && rPr.getIns() != null) out.add(change(new MarkT(true, rPr.getIns()), para, ctx));
		if (rPr != null && rPr.getDel() != null) out.add(change(new MarkT(false, rPr.getDel()), para, ctx));
		if (rPr == null || (rPr.getIns() == null && rPr.getDel() == null && rPr.getRPrChange() == null)) out.add(BREAK);
		SectPr sectPr = pPr == null ? null : pPr.getSectPr();
		if (sectPr != null && sectPr.getSectPrChange() != null) {
			int at = Trees.indexOf(para.container, para.item);
			out.add(change(new SectionT(sectPr, sectionText(para.container, at + 1) + "\f"), para, ctx));
		}
	}

	/** The text of the section ending at {@code end} (exclusive): its paragraphs', {@code \r} apart, back to the previous break. */
	private static String sectionText(List<Object> items, int end) {
		int from = end - 1;
		while (from > 0) {
			Object prev = Trees.unwrap(items.get(from - 1));
			if (prev instanceof P && ((P) prev).getPPr() != null && ((P) prev).getPPr().getSectPr() != null) break;
			from--;
		}
		StringBuilder out = new StringBuilder();
		boolean first = true;
		for (int i = Math.max(from, 0); i < end; i++) {
			Object v = Trees.unwrap(items.get(i));
			if (!(v instanceof P)) continue;
			if (!first) out.append('\r');
			first = false;
			out.append(Trees.paragraphText((P) v, View.ACCEPTED));
		}
		return out.toString();
	}

	private void runLevel(Para para, List<Object> items, List<Object> out, Ctx ctx) {
		if (items == null) return;
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			Object v = Trees.unwrap(item);
			// an inline control or custom XML element that is itself an insertion: a piece, then what is in it
			TrackedChange inserted = holderInsertionAt(items, i, para, ctx);
			if (inserted != null) {
				out.add(inserted);
				runLevel(para, Trees.children(Trees.unwrap(items.get(i + 1))), out, ctx);
				i += 2;
				continue;
			}
			// a field Word inserted, its separator and result plain between two insertions: one piece
			TrackedChange field = insertedFieldAt(items, i, para, ctx);
			if (field != null) {
				out.add(field);
				i += ((FieldT) field.target).span.size() - 1;
				continue;
			}
			if (Trees.revisionOf(item) != null) {
				revisionTokens(para, items, i, out, ctx);
				continue;
			}
			if (v instanceof R) {
				R run = (R) v;
				findTextBoxes(run, ctx);
				// a run whose formatting changed is a piece of its own; one that did not keeps pieces
				// apart if it has text: a run holding only a comment reference does not (check 32)
				if (run.getRPr() != null && run.getRPr().getRPrChange() != null) out.add(change(new RunPropsT(run, run.getRPr().getRPrChange()), para, ctx));
				else if (Trees.hasText(run)) out.add(BREAK);
				continue;
			}
			if (isMath(v)) {
				math(para, v, item, items, new ArrayDeque<Object[]>(), out, ctx);
				continue;
			}
			if (v instanceof AlternateContent) {
				// a paragraph-level element: the branch Word reads, the other branches' changes as twins (CR-051 section 10.5, item 3)
				alternateRuns((AlternateContent) v, para, out, ctx);
				continue;
			}
			// a run holder that is not itself an insertion - a hyperlink, a simple field, a control
			// that was there - keeps the insertions inside it apart from those outside (check 30)
			List<Object> nested = Trees.runItems(v);
			if (nested != null) {
				out.add(BREAK);
				runLevel(para, nested, out, ctx);
				out.add(BREAK);
			}
		}
	}

	/** {@code items[i]}, a run-level revision: its piece, then what is nested in it. */
	private void revisionTokens(Para para, List<Object> items, int i, List<Object> out, Ctx ctx) {
		Object item = items.get(i);
		Revision revision = Trees.revisionOf(item);
		// moved text is content Office JS cannot list (checks 16, 17), and is kept apart
		boolean move = revision == Revision.MOVE_FROM || revision == Revision.MOVE_TO;
		if (move) out.add(BREAK);
		out.add(change(new RunT(revision, item, items), para, ctx));
		if (move) out.add(BREAK);
		List<Object> nested = Trees.runItems(Trees.unwrap(item));
		if (nested == null) return;
		// a revision inside a revision (a deletion of inserted text, check 22) is listed apart
		List<Object> inner = new ArrayList<Object>();
		runLevel(para, nested, inner, ctx);
		boolean anyRun = false;
		for (Object token : inner) if (token instanceof TrackedChange && ((TrackedChange) token).target instanceof RunT) anyRun = true;
		if (anyRun) {
			out.add(BREAK);
			out.addAll(inner);
			out.add(BREAK);
		} else {
			for (Object token : inner) if (token != BREAK) out.add(token);
		}
	}

	// --- content controls and fields inserted ------------------------------------------------------------

	/**
	 * Word's form for a content control or custom XML element inserted under tracking (check 30):
	 * {@code w:customXmlInsRangeStart}, the element - whose content starts with that marker's
	 * {@code w:customXmlInsRangeEnd} and ends with a second start - and that second start's end.
	 */
	private TrackedChange holderInsertionAt(List<Object> items, int i, Para para, Ctx ctx) {
		Object start = items.get(i);
		if (!"customXmlInsRangeStart".equals(Trees.name(start)) || i + 2 >= items.size()) return null;
		Object holder = items.get(i + 1);
		Object outerEnd = items.get(i + 2);
		if (!Trees.isHolder(Trees.unwrap(holder)) || !"customXmlInsRangeEnd".equals(Trees.name(outerEnd))) return null;
		List<Object> inner = Trees.children(Trees.unwrap(holder));
		if (inner == null || inner.size() < 2) return null;
		Object innerEnd = inner.get(0);
		Object innerStart = inner.get(inner.size() - 1);
		if (!"customXmlInsRangeEnd".equals(Trees.name(innerEnd)) || !"customXmlInsRangeStart".equals(Trees.name(innerStart))) return null;
		if (!Objects.equals(idOf(innerEnd), idOf(start)) || !Objects.equals(idOf(outerEnd), idOf(innerStart))) return null;
		return change(new HolderT(holder, items, start, outerEnd, innerEnd, innerStart), para, ctx);
	}

	private static Object idOf(Object marker) {
		Object v = Trees.unwrap(marker);
		return v instanceof CTMarkup ? ((CTMarkup) v).getId() : null;
	}

	private enum FieldState { OPEN, CLOSED, OTHER }

	/**
	 * Word's form for a field inserted under tracking, when {@code items[i]} starts it (check 30,
	 * C01, C16): a {@code w:ins} that opens a field, then plain runs - the separator and the
	 * result - and markers, then a {@code w:ins} by the same author in which that field ends.
	 */
	private TrackedChange insertedFieldAt(List<Object> items, int i, Para para, Ctx ctx) {
		Object first = items.get(i);
		if (Trees.revisionOf(first) != Revision.INS) return null;
		String author = ((CTTrackChange) Trees.unwrap(first)).getAuthor();
		int[] depth = { 0 };
		if (scan(first, depth) != FieldState.OPEN || depth[0] == 0) return null;
		for (int j = i + 1; j < items.size(); j++) {
			Object el = items.get(j);
			Revision kind = Trees.revisionOf(el);
			if (kind == Revision.INS) {
				if (!Objects.equals(((CTTrackChange) Trees.unwrap(el)).getAuthor(), author)) return null;
				FieldState state = scan(el, depth);
				if (state == FieldState.OTHER) return null;
				if (state == FieldState.CLOSED) return change(new FieldT(new ArrayList<Object>(items.subList(i, j + 1)), items), para, ctx);
				continue;
			}
			if (kind != null) return null;
			Object v = Trees.unwrap(el);
			if (v instanceof R) {
				if ((((R) v).getRPr() != null && ((R) v).getRPr().getRPrChange() != null) || scan(el, depth) != FieldState.OPEN) return null;
				continue;
			}
			if (Trees.runItems(v) != null) return null;
		}
		return null;
	}

	/** The field characters of an element's runs in order: CLOSED when the field the span opened ends. */
	private static FieldState scan(Object el, int[] depth) {
		Object v = Trees.unwrap(el);
		List<Object> runs = new ArrayList<Object>();
		if (v instanceof R) runs.add(el);
		else if (Trees.runItems(v) != null) runs.addAll(Trees.runItems(v));
		for (Object run : runs) {
			Object rv = Trees.unwrap(run);
			if (!(rv instanceof R)) continue;
			for (Object item : ((R) rv).getContent()) {
				Object iv = Trees.unwrap(item);
				if (!(iv instanceof FldChar)) continue;
				STFldCharType type = ((FldChar) iv).getFldCharType();
				if (type == STFldCharType.BEGIN) depth[0]++;
				else if (type == STFldCharType.END) {
					depth[0]--;
					if (depth[0] < 0) return FieldState.OTHER;
					if (depth[0] == 0) return FieldState.CLOSED;
				}
			}
		}
		return FieldState.OPEN;
	}

	// --- math ------------------------------------------------------------------------------------------

	static boolean isMath(Object v) {
		return v != null && v.getClass().getPackage() == CTR.class.getPackage();
	}

	/**
	 * An equation's pieces (CR-043 section 3.2): {@code w:ins}/{@code w:del} inside an {@code m:r}, a
	 * structure inserted or deleted (its {@code m:ctrlPr}'s {@code w:ins}/{@code w:del}), and run-level
	 * revisions holding math. {@code stack} holds {value, item, owner} of the objects above.
	 */
	private void math(Para para, Object value, Object item, List<Object> owner, Deque<Object[]> stack, List<Object> out, Ctx ctx) {
		if (value instanceof CTR) {
			CTR run = (CTR) value;
			for (Object c : run.getContent()) {
				Object cv = Trees.unwrap(c);
				if (cv instanceof CTMathRunTrackChange) out.add(change(new MathRunT("ins".equals(Trees.name(c)), run, c, item, owner), para, ctx));
				else if (Trees.itemText(c) != null) out.add(BREAK);
			}
			return;
		}
		if (value instanceof CTCtrlPr) {
			CTCtrlPr ctrlPr = (CTCtrlPr) value;
			if (ctrlPr.getIns() == null && ctrlPr.getDel() == null) return;
			Object[] parent = stack.peek();
			Object structItem = null;
			List<Object> structOwner = null;
			if (parent != null && !(parent[0] instanceof CTOMathArg)) {
				// m:ctrlPr in m:sSupPr in m:sSup: the structure is two up
				java.util.Iterator<Object[]> up = stack.iterator();
				up.next();
				if (up.hasNext()) {
					Object[] grand = up.next();
					structItem = grand[1];
					@SuppressWarnings("unchecked")
					List<Object> o = (List<Object>) grand[2];
					structOwner = o;
				}
			}
			if (ctrlPr.getIns() != null) out.add(change(new MathStructT(true, ctrlPr, structItem, structOwner), para, ctx));
			if (ctrlPr.getDel() != null) out.add(change(new MathStructT(false, ctrlPr, structItem, structOwner), para, ctx));
			return;
		}
		stack.push(new Object[] { value, item, owner });
		try {
			for (Field f : Trees.fieldsOf(value.getClass())) {
				Object fv;
				try {
					fv = f.get(value);
				} catch (IllegalAccessException e) {
					continue;
				}
				if (fv == null || fv instanceof String) continue;
				if (fv instanceof List) {
					@SuppressWarnings("unchecked")
					List<Object> list = (List<Object>) fv;
					for (int i = 0; i < list.size(); i++) {
						Object c = list.get(i);
						if (Trees.revisionOf(c) != null) {
							revisionTokens(para, list, i, out, ctx);
							continue;
						}
						Object cv = Trees.unwrap(c);
						if (cv instanceof R) {
							runLevel(para, list.subList(i, i + 1), out, ctx);
							continue;
						}
						if (cv != null && isMath(cv)) math(para, cv, c, list, stack, out, ctx);
					}
				} else if (isMath(Trees.unwrap(fv))) {
					math(para, Trees.unwrap(fv), fv, null, stack, out, ctx);
				}
			}
		} finally {
			stack.pop();
		}
	}

	// --- text boxes ---------------------------------------------------------------------------------

	// --- block-, paragraph-, row- and cell-level mc:AlternateContent (CR-051) ------------------------------

	/**
	 * A block-level element's changes: the branch the text model reads ({@link McPurpose#TEXT}, the
	 * one Word shows) listed where the element stands, kept apart from what is around it as a block
	 * control's content is; each other branch's changes attached to them as twins, by kind and
	 * position, as a text box's {@code mc:Fallback} changes are ({@link #pairTwins}), so that
	 * accepting or rejecting a listed change resolves its twins too.  For acceptAll's listing the
	 * twins are listed on their own.
	 */
	private void alternateBlocks(AlternateContent ac, List<Object> into, Ctx ctx) {
		Object primary = McSelection.selectedBranch(McPurpose.TEXT, ac);
		if (primary == null) return;
		int boxesBefore = ctx.boxes.size();
		List<Object> tokens = new ArrayList<Object>();
		visitBlocks(McSplice.contentOf(primary), tokens, ctx);
		List<TrackedChange> changes = groupTouching(tokens, ctx);
		for (Object branch : McSelection.branches(ac, McMode.ALL)) {
			if (branch == primary) continue;
			Ctx twinCtx = new Ctx(new Story(pkg, ctx.story.part, McSplice.contentOf(branch), branch, true));
			List<Object> twinTokens = new ArrayList<Object>();
			visitBlocks(twinCtx.story.root, twinTokens, twinCtx);
			twins(changes, groupTouching(twinTokens, twinCtx), twinCtx, ctx, boxesBefore, into);
		}
		into.add(BREAK);
		for (TrackedChange c : changes) {
			into.add(c);
			into.add(BREAK);
		}
	}

	/** As {@link #alternateBlocks}, for an element among a paragraph's runs: each branch holds run-level content. */
	private void alternateRuns(AlternateContent ac, Para para, List<Object> out, Ctx ctx) {
		Object primary = McSelection.selectedBranch(McPurpose.TEXT, ac);
		if (primary == null) return;
		int boxesBefore = ctx.boxes.size();
		List<Object> tokens = new ArrayList<Object>();
		runLevel(para, McSplice.contentOf(primary), tokens, ctx);
		List<TrackedChange> changes = groupTouching(tokens, ctx);
		for (Object branch : McSelection.branches(ac, McMode.ALL)) {
			if (branch == primary) continue;
			Ctx twinCtx = new Ctx(new Story(pkg, ctx.story.part, McSplice.contentOf(branch), branch, true));
			List<Object> twinTokens = new ArrayList<Object>();
			runLevel(para, twinCtx.story.root, twinTokens, twinCtx);
			twins(changes, groupTouching(twinTokens, twinCtx), twinCtx, ctx, boxesBefore, out);
		}
		out.add(BREAK);
		for (TrackedChange c : changes) {
			out.add(c);
			out.add(BREAK);
		}
	}

	/**
	 * As {@link #alternateBlocks}, for an element among a table's rows (CR-051 phase 2): each branch
	 * holds rows.  The listed branch's rows are listed where the element stands; a row of another
	 * branch that is itself a revision becomes the twin of the listed branch's, by kind and position,
	 * so accepting or rejecting one resolves both.
	 */
	private void alternateRows(AlternateContent ac, Object tblItem, List<Object> tblOwner, List<Object> into, Ctx ctx) {
		Object primary = McSelection.selectedBranch(McPurpose.TEXT, ac);
		if (primary == null) return;
		int boxesBefore = ctx.boxes.size();
		List<Object> tokens = new ArrayList<Object>();
		rowItems(McSplice.contentOf(primary), tblItem, tblOwner, tokens, ctx);
		List<TrackedChange> changes = groupTouching(tokens, ctx);
		for (Object branch : McSelection.branches(ac, McMode.ALL)) {
			if (branch == primary) continue;
			Ctx twinCtx = new Ctx(new Story(pkg, ctx.story.part, McSplice.contentOf(branch), branch, true));
			List<Object> twinTokens = new ArrayList<Object>();
			rowItems(twinCtx.story.root, tblItem, tblOwner, twinTokens, twinCtx);
			twins(changes, groupTouching(twinTokens, twinCtx), twinCtx, ctx, boxesBefore, into);
		}
		into.add(BREAK);
		for (TrackedChange c : changes) {
			into.add(c);
			into.add(BREAK);
		}
	}

	/** As {@link #alternateRows}, for an element among a row's cells: each branch holds cells. */
	private void alternateCells(AlternateContent ac, Tr tr, List<Object> into, Ctx ctx) {
		Object primary = McSelection.selectedBranch(McPurpose.TEXT, ac);
		if (primary == null) return;
		int boxesBefore = ctx.boxes.size();
		List<Object> tokens = new ArrayList<Object>();
		cellItems(McSplice.contentOf(primary), tr, tokens, ctx);
		List<TrackedChange> changes = groupTouching(tokens, ctx);
		for (Object branch : McSelection.branches(ac, McMode.ALL)) {
			if (branch == primary) continue;
			Ctx twinCtx = new Ctx(new Story(pkg, ctx.story.part, McSplice.contentOf(branch), branch, true));
			List<Object> twinTokens = new ArrayList<Object>();
			cellItems(twinCtx.story.root, tr, twinTokens, twinCtx);
			twins(changes, groupTouching(twinTokens, twinCtx), twinCtx, ctx, boxesBefore, into);
		}
		into.add(BREAK);
		for (TrackedChange c : changes) {
			into.add(c);
			into.add(BREAK);
		}
	}

	/**
	 * A branch's changes made the twins of the listed branch's, or listed on their own for acceptAll;
	 * the text boxes found in it become the twins of the listed branch's boxes, by position, and any
	 * beyond those are stories of their own.
	 */
	private void twins(List<TrackedChange> primary, List<TrackedChange> twinChanges, Ctx twinCtx, Ctx ctx, int boxesBefore, List<Object> into) {
		if (attachTwins) {
			pairTwins(primary, twinChanges);
		} else {
			into.add(BREAK);
			for (TrackedChange c : twinChanges) {
				into.add(c);
				into.add(BREAK);
			}
		}
		for (int i = 0; i < twinCtx.boxes.size(); i++) {
			Box box = twinCtx.boxes.get(i);
			if (boxesBefore + i < ctx.boxes.size()) ctx.boxes.get(boxesBefore + i).twins.add(box.story);
			else ctx.boxes.add(box);
		}
	}

	/** The text boxes in a run, each a story; in {@code mc:AlternateContent}, the first branch's, with the other branches' as twins. */
	private void findTextBoxes(R run, Ctx ctx) {
		Trees.walk(run, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (value instanceof AlternateContent) {
					alternate((AlternateContent) value, ctx);
					return false;
				}
				if (value instanceof CTTxbxContent) {
					CTTxbxContent box = (CTTxbxContent) value;
					ctx.boxes.add(new Box(new Story(pkg, ctx.story.part, box.getContent(), box, ctx.story.twin)));
					return false;
				}
				return true;
			}
		});
	}

	private void alternate(AlternateContent ac, Ctx ctx) {
		List<List<CTTxbxContent>> branches = new ArrayList<List<CTTxbxContent>>();
		for (AlternateContent.Choice choice : ac.getChoice()) branches.add(boxesIn(choice));
		if (ac.getFallback() != null) branches.add(boxesIn(ac.getFallback()));
		List<CTTxbxContent> primary = null;
		for (List<CTTxbxContent> branch : branches) {
			if (!branch.isEmpty()) { primary = branch; break; }
		}
		if (primary == null) return;
		for (int i = 0; i < primary.size(); i++) {
			CTTxbxContent box = primary.get(i);
			Box b = new Box(new Story(pkg, ctx.story.part, box.getContent(), box, ctx.story.twin));
			for (List<CTTxbxContent> branch : branches) {
				if (branch == primary || branch.size() <= i) continue;
				CTTxbxContent twin = branch.get(i);
				b.twins.add(new Story(pkg, ctx.story.part, twin.getContent(), twin, true));
			}
			ctx.boxes.add(b);
		}
	}

	private static List<CTTxbxContent> boxesIn(Object branch) {
		List<CTTxbxContent> out = new ArrayList<CTTxbxContent>();
		Trees.walk(branch, new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (value instanceof CTTxbxContent) {
					out.add((CTTxbxContent) value);
					return false;
				}
				return true;
			}
		});
		return out;
	}

	// --- grouping -----------------------------------------------------------------------------------

	/**
	 * What a piece groups as: an insertion or deletion of runs, marks, rows, fields, controls, math;
	 * a run's or a mark's formatting. Moves, a paragraph's properties and the table and section
	 * records do not group.
	 */
	static GroupKind groupable(TrackedChange change) {
		TrackedChange.Target t = change.target;
		if (t instanceof MarkT) return ((MarkT) t).ins ? GroupKind.INS : GroupKind.DEL;
		if (t instanceof RunT) {
			Revision r = ((RunT) t).revision;
			return r == Revision.INS ? GroupKind.INS : r == Revision.DEL ? GroupKind.DEL : null;
		}
		if (t instanceof RunPropsT || t instanceof MarkPropsT) return GroupKind.FORMAT;
		if (t instanceof HolderT || t instanceof FieldT) return GroupKind.INS;
		if (t instanceof RowT) return ((RowT) t).ins ? GroupKind.INS : GroupKind.DEL;
		if (t instanceof MathRunT) return ((MathRunT) t).ins ? GroupKind.INS : GroupKind.DEL;
		if (t instanceof MathStructT) return ((MathStructT) t).ins ? GroupKind.INS : GroupKind.DEL;
		return null;
	}

	/**
	 * Tokens made into the changes Office JS lists: consecutive pieces of one kind by one author,
	 * nothing unrevised between them, are one change (checks 22, 23, 28); a change that does not
	 * group is listed where it stands and does not break a group.
	 */
	private List<TrackedChange> groupTouching(List<Object> tokens, Ctx ctx) {
		List<Object> out = new ArrayList<Object>();
		List<TrackedChange> open = null;
		for (Object token : tokens) {
			if (token == BREAK) {
				open = null;
				continue;
			}
			TrackedChange change = (TrackedChange) token;
			GroupKind kind = groupable(change);
			if (kind == null) {
				out.add(change);
				continue;
			}
			TrackedChange last = open == null ? null : open.get(open.size() - 1);
			if (last != null && groupable(last) == kind && Objects.equals(last.getAuthor(), change.getAuthor())) {
				open.add(change);
				continue;
			}
			open = new ArrayList<TrackedChange>();
			open.add(change);
			out.add(open);
		}
		List<TrackedChange> result = new ArrayList<TrackedChange>();
		for (Object o : out) {
			if (o instanceof TrackedChange) {
				result.add((TrackedChange) o);
				continue;
			}
			@SuppressWarnings("unchecked")
			List<TrackedChange> group = (List<TrackedChange>) o;
			TrackedChange first = group.get(0);
			if (group.size() == 1) result.add(first);
			else result.add(new TrackedChange(new GroupT(groupable(first), group), first.para, first.story));
		}
		return result;
	}
}
