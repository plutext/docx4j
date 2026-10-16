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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.math.CTR;
import org.docx4j.model.revisions.TrackedChange.FieldT;
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
import org.docx4j.model.revisions.Trees.Revision;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CTMathRunTrackChange;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.DelText;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.docx4j.wml.TrPr;

/**
 * Accepting and rejecting one change (CR-043 section 3.2): a port of docx4j-core-ts's
 * {@code TrackedChange.accept}/{@code reject} onto the JAXB tree, with Word's own rules where its
 * saves differ (fields and content controls go whole, a note goes with its reference, a bookmark
 * touching removed content goes whole) and the forms core-ts does not reach (math, text boxes).
 */
final class Resolver {

	private static final ObjectFactory WML = new ObjectFactory();

	private Resolver() {}

	static void resolve(TrackedChange change, boolean accept, Removals removals) {
		new Resolver.Op(change, removals).run(accept);
	}

	/** One resolution: the change, and where what it removes is recorded. */
	private static final class Op {
		final TrackedChange change;
		final Removals removals;
		final Story story;
		Op(TrackedChange change, Removals removals) {
			this.change = change;
			this.removals = removals;
			this.story = change.story;
		}

		void run(boolean accept) {
			TrackedChange.Target t = change.target;
			if (t instanceof RunT) runRevision((RunT) t, accept);
			else if (t instanceof MarkT) mark((MarkT) t, accept);
			else if (t instanceof RunPropsT) runProperties((RunPropsT) t, accept);
			else if (t instanceof ParaPropsT) paragraphProperties((ParaPropsT) t, accept);
			else if (t instanceof MarkPropsT) markProperties((MarkPropsT) t, accept);
			else if (t instanceof RowT) row((RowT) t, accept);
			else if (t instanceof GroupT) pieces(((GroupT) t).pieces, accept, removals);
			else if (t instanceof FieldT) field((FieldT) t, accept);
			else if (t instanceof HolderT) holder((HolderT) t, accept);
			else if (t instanceof MathRunT) mathRun((MathRunT) t, accept);
			else if (t instanceof MathStructT) mathStructure((MathStructT) t, accept);
			else throw new UnsupportedOperationException(change.getKind() + " (CR-043 phase 2)");
		}

		// --- runs ----------------------------------------------------------------------------------

		void runRevision(RunT t, boolean accept) {
			boolean move = t.revision == Revision.MOVE_FROM || t.revision == Revision.MOVE_TO;
			if (move && (Moves.resolve(this, t, accept) || Moves.resolveRangeless(this, t, accept))) return;
			boolean kept = (t.revision == Revision.INS || t.revision == Revision.MOVE_TO) == accept;
			if (kept) {
				if (t.revision == Revision.DEL || t.revision == Revision.MOVE_FROM) restoreDeleted(t.item, t.owner);
				else unwrap(t.item, t.owner);
			} else {
				removeRevision(t.item, t.owner);
			}
		}

		void mark(MarkT t, boolean accept) {
			if (change.para == null) return;
			if (t.ins == accept) dropMark(change.para, t.ins);
			else {
				dropMark(change.para, t.ins);
				joinWithNext(change.para);
			}
		}

		void runProperties(RunPropsT t, boolean accept) {
			if (accept) {
				if (t.run.getRPr() != null) t.run.getRPr().setRPrChange(null);
				Props.pruneRun(t.run);
			} else {
				Props.restoreRPr(t.run, t.value);
			}
		}

		void paragraphProperties(ParaPropsT t, boolean accept) {
			if (change.para == null) return;
			P p = change.para.p;
			if (accept) {
				if (p.getPPr() != null) p.getPPr().setPPrChange(null);
				Props.pruneParagraph(p);
			} else {
				Props.restorePPr(change.para, t.value.getPPr(), story.pkg);
			}
		}

		void markProperties(MarkPropsT t, boolean accept) {
			if (change.para == null) return;
			P p = change.para.p;
			if (accept) {
				if (p.getPPr() != null && p.getPPr().getRPr() != null) p.getPPr().getRPr().setRPrChange(null);
				Props.pruneParagraph(p);
			} else {
				Props.restoreMarkProperties(change.para, t.value);
			}
		}

		// --- rows ----------------------------------------------------------------------------------

		/**
		 * An accepted insertion keeps the row, so its content's own revisions go with the
		 * {@code w:trPr/w:ins}; an accepted deletion takes the row and everything in it away; a table
		 * left with no rows goes (check 30, C13, C25).
		 */
		void row(RowT t, boolean accept) {
			if (t.ins == accept) {
				TrPr trPr = t.tr.getTrPr();
				if (trPr != null) {
					if (t.ins) trPr.setIns(null);
					else trPr.setDel(null);
					if (Trees.isEmpty(trPr)) t.tr.setTrPr(null);
				}
				for (int i = t.inner.size() - 1; i >= 0; i--) resolve(t.inner.get(i), accept, removals);
			} else {
				remove(t.owner, t.item);
				Object tblValue = Trees.unwrap(t.tblItem);
				if (tblValue instanceof Tbl && Lister.rowsOf((Tbl) tblValue).isEmpty()) remove(t.tblOwner, t.tblItem);
			}
		}

		// --- fields and controls --------------------------------------------------------------------

		/**
		 * Word's inserted field: accepted, its {@code w:ins} pieces are unwrapped; rejected, the whole
		 * field goes, its separator and result too (C01, C16), last first; a bookmark, a comment's range
		 * or a proofing mark between its pieces stays where it stood.
		 */
		void field(FieldT t, boolean accept) {
			for (int i = t.span.size() - 1; i >= 0; i--) {
				Object el = t.span.get(i);
				List<Object> owner = currentOwner(t.owner, el);
				if (Trees.revisionOf(el) == Revision.INS) {
					if (accept) unwrap(el, owner);
					else removeRevision(el, owner);
				} else if (!accept && Trees.unwrap(el) instanceof R) {
					remove(owner, el);
				}
			}
		}

		/**
		 * A content control or custom XML element inserted: accepted, its four markers go; rejected,
		 * the element goes and what is left of its content stands in its place - nothing, once the
		 * insertions inside it have been rejected with it (check 30, C16b).
		 */
		void holder(HolderT t, boolean accept) {
			remove(t.owner, t.outerStart);
			remove(t.owner, t.outerEnd);
			List<Object> items = Trees.children(Trees.unwrap(t.holder));
			if (items != null) {
				removeQuietly(items, t.innerEnd);
				removeQuietly(items, t.innerStart);
			}
			if (accept) return;
			List<Object> owner = currentOwner(t.owner, t.holder);
			int i = Trees.indexOf(owner, t.holder);
			if (i < 0) return;
			Object parent = Trees.parentOf(t.holder);
			List<Object> moved = items == null ? new ArrayList<Object>() : new ArrayList<Object>(items);
			if (items != null) items.clear();
			owner.remove(i);
			owner.addAll(i, moved);
			Trees.setParents(moved, parent);
		}

		// --- math ----------------------------------------------------------------------------------

		/**
		 * {@code w:ins}/{@code w:del} inside an {@code m:r}: kept, its content is unwrapped into the run;
		 * not kept, it goes, and a run left with no text goes with it (CR-043 section 3.2).
		 */
		void mathRun(MathRunT t, boolean accept) {
			List<Object> content = t.run.getContent();
			int i = Trees.indexOf(content, t.item);
			if (i < 0) return;
			if (t.ins == accept) {
				List<Object> moved = new ArrayList<Object>();
				for (Object item : t.value.getRest()) moved.add(asMathRunItem(item));
				if (!t.ins) for (int k = 0; k < moved.size(); k++) moved.set(k, restoredText(moved.get(k)));
				content.remove(i);
				content.addAll(i, moved);
				Trees.setParents(moved, t.run);
			} else {
				removals.discard(story, t.item);
				content.remove(i);
				boolean text = false;
				for (Object item : content) {
					if (Trees.itemText(item) != null || Trees.unwrap(item) instanceof CTMathRunTrackChange) text = true;
				}
				if (!text && t.runOwner != null) remove(t.runOwner, t.runItem);
			}
		}

		/** An item of a {@code w:ins} in an {@code m:r}, as the {@code m:r}'s own (its {@code m:rPr} and {@code m:t} are typed by scope). */
		private Object asMathRunItem(Object item) {
			if (item instanceof CTMathRunTrackChange.RPrMath) return new CTR.RPrMath(((CTMathRunTrackChange.RPrMath) item).getValue());
			if (item instanceof CTMathRunTrackChange.TMath) return new CTR.TMath(((CTMathRunTrackChange.TMath) item).getValue());
			return item;
		}

		/**
		 * An equation structure inserted or deleted (its {@code m:ctrlPr}'s {@code w:ins}/{@code w:del}):
		 * kept, the marker goes and its recorded run properties become the control's; not kept, the
		 * structure goes whole, from the list that holds it, as Word's Accept All and Reject All did
		 * (an inserted {@code m:sSup}, a deleted {@code m:acc}: {@code tracked-changes-equations.docx}).
		 */
		void mathStructure(MathStructT t, boolean accept) {
			if (t.ins == accept || t.structItem == null) {
				org.docx4j.wml.CTRPrChange marker = t.ins ? t.ctrlPr.getIns() : t.ctrlPr.getDel();
				if (marker != null && t.ctrlPr.getRPr() == null) {
					org.docx4j.wml.RPr rPr = Props.rPrOf(marker.getRPr());
					if (rPr != null) {
						rPr.setParent(t.ctrlPr);
						t.ctrlPr.setRPr(rPr);
					}
				}
				if (t.ins) t.ctrlPr.setIns(null);
				else t.ctrlPr.setDel(null);
			} else {
				remove(t.structOwner, t.structItem);
			}
		}

		// --- the operations on lists ---------------------------------------------------------------------

		/** The list {@code item} is in now: {@code owner} as it was listed, else the one its parent keeps. */
		List<Object> currentOwner(List<Object> owner, Object item) {
			if (Trees.indexOf(owner, item) >= 0) return owner;
			Object parent = Trees.parentOf(item);
			List<Object> actual = listOf(parent);
			return actual != null && Trees.indexOf(actual, item) >= 0 ? actual : owner;
		}

		void remove(List<Object> owner, Object item) {
			if (owner == null) return;
			List<Object> list = currentOwner(owner, item);
			int i = Trees.indexOf(list, item);
			if (i < 0) return;
			removals.discard(story, item);
			list.remove(i);
			Object v = Trees.unwrap(item);
			if (v instanceof R || Trees.revisionOf(item) != null) dropEmptiedBookmarks(list, i);
		}

		/**
		 * A bookmark around content that went, left with nothing in it, goes too: Word removed one put
		 * on inserted text when it rejected the insertion (C11b). Only a bookmark whose start stood
		 * before the content and whose end after it: one that was empty already stays.
		 */
		void dropEmptiedBookmarks(List<Object> list, int at) {
			for (int s = at - 1; s >= 0; s--) {
				Object start = list.get(s);
				if (hasText(start)) break;
				if (!"bookmarkStart".equals(Trees.name(start))) continue;
				BigInteger id = ((CTMarkup) Trees.unwrap(start)).getId();
				for (int e = at; e < list.size(); e++) {
					Object end = list.get(e);
					if ("bookmarkEnd".equals(Trees.name(end)) && Objects.equals(((CTMarkup) Trees.unwrap(end)).getId(), id)) {
						list.remove(e);
						list.remove(s);
						at--;
						break;
					}
					if (hasText(end)) break;
				}
			}
		}

		/** A {@code w:ins} or {@code w:moveTo} kept: its content takes its place. */
		void unwrap(Object item, List<Object> owner) {
			List<Object> list = currentOwner(owner, item);
			int i = Trees.indexOf(list, item);
			if (i < 0) return;
			List<Object> items = Trees.runItems(Trees.unwrap(item));
			List<Object> moved = items == null ? new ArrayList<Object>() : new ArrayList<Object>(items);
			Object parent = Trees.parentOf(item);
			list.remove(i);
			list.addAll(i, moved);
			Trees.setParents(moved, parent != null ? parent : ownerObject(list, moved));
		}

		/** A {@code w:del} or {@code w:moveFrom} kept: its runs come back, deleted text become text again. */
		void restoreDeleted(Object item, List<Object> owner) {
			restoreText(Trees.runItems(Trees.unwrap(item)));
			unwrap(item, owner);
		}

		/**
		 * A revision taken away with its content - an insertion rejected, a deletion accepted - and the
		 * comments on it, by where their markers were (core-ts {@code removeRevision}, check 32): a
		 * comment whose range start was inside goes with the text it was on, markers, reference and
		 * entry, once nothing with text is left in its range; one whose markers lie outside loses only
		 * its range markers when its range is left empty (Word 2010 and 15, the editor's
		 * {@code review-for-word} saves).
		 */
		void removeRevision(Object item, List<Object> owner) {
			List<Object> list = currentOwner(owner, item);
			int i = Trees.indexOf(list, item);
			if (i < 0) return;
			List<Object> inner = Trees.runItems(Trees.unwrap(item));
			List<Object> kept = new ArrayList<Object>();
			if (inner != null) {
				for (Object o : inner) {
					Object v = Trees.unwrap(o);
					if (v instanceof CommentRangeStart || v instanceof CommentRangeEnd) kept.add(o);
				}
			}
			Set<BigInteger> inside = new LinkedHashSet<BigInteger>();
			for (Object o : kept) if (Trees.unwrap(o) instanceof CommentRangeStart) inside.add(((CommentRangeStart) Trees.unwrap(o)).getId());
			Set<BigInteger> outside = new LinkedHashSet<BigInteger>();
			for (int s = 0; s < i; s++) {
				Object v = Trees.unwrap(list.get(s));
				if (!(v instanceof CommentRangeStart)) continue;
				BigInteger id = ((CommentRangeStart) v).getId();
				for (int e = i + 1; e < list.size(); e++) {
					Object ev = Trees.unwrap(list.get(e));
					if (ev instanceof CommentRangeEnd && Objects.equals(((CommentRangeEnd) ev).getId(), id)) outside.add(id);
				}
			}
			if (inner != null) inner.removeAll(kept);
			Object parent = Trees.parentOf(item);
			removals.discard(story, item);
			list.remove(i);
			list.addAll(i, kept);
			Trees.setParents(kept, parent);
			dropEmptiedBookmarks(list, i);
			Set<BigInteger> all = new LinkedHashSet<BigInteger>(inside);
			all.addAll(outside);
			for (BigInteger id : all) {
				int start = -1;
				for (int s = 0; s < list.size(); s++) {
					Object v = Trees.unwrap(list.get(s));
					if (v instanceof CommentRangeStart && Objects.equals(((CommentRangeStart) v).getId(), id)) { start = s; break; }
				}
				if (start < 0 || !rangeIsEmpty(list, start, id)) continue;
				if (inside.contains(id)) {
					removals.removeComment(id);
				} else {
					for (int s = list.size() - 1; s >= 0; s--) {
						Object v = Trees.unwrap(list.get(s));
						if ((v instanceof CommentRangeStart && Objects.equals(((CommentRangeStart) v).getId(), id))
								|| (v instanceof CommentRangeEnd && Objects.equals(((CommentRangeEnd) v).getId(), id))) list.remove(s);
					}
				}
			}
		}

		/** True when nothing with text stands between a comment's range start and its end in the same list. */
		private boolean rangeIsEmpty(List<Object> list, int from, BigInteger id) {
			for (int i = from + 1; i < list.size(); i++) {
				Object v = Trees.unwrap(list.get(i));
				if (v instanceof CommentRangeEnd && Objects.equals(((CommentRangeEnd) v).getId(), id)) return true;
				if (hasText(list.get(i))) return false;
			}
			return false; // the end is elsewhere: text may be
		}

		// --- paragraph marks ---------------------------------------------------------------------------

		void dropMark(Para para, boolean ins) {
			PPr pPr = para.p.getPPr();
			if (pPr == null || pPr.getRPr() == null) return;
			if (ins) pPr.getRPr().setIns(null);
			else pPr.getRPr().setDel(null);
			Props.pruneParagraph(para.p);
		}

		/**
		 * A paragraph mark taken away (a deletion accepted, an insertion rejected): the paragraph joins
		 * the next one in its list, and the result keeps the next paragraph's properties and
		 * {@code w14:paraId}, for markup Word did not write as well (checks 18, 22, 33; C10, C10b). Range
		 * markers between the two (a bookmark's end, a comment's) go into the joined paragraph where
		 * they stood. With no paragraph next in the same list - a story's, a cell's, a control's last, or
		 * a table next - the mark is only dropped: a join never crosses a container's boundary (U1).
		 */
		void joinWithNext(Para para) {
			List<Object> container = currentOwner(para.container, para.item);
			int i = Trees.indexOf(container, para.item);
			if (i < 0) return;
			int j = i + 1;
			while (j < container.size() && isMarker(container.get(j))) j++;
			Object nextItem = j < container.size() ? container.get(j) : null;
			if (!(Trees.unwrap(nextItem) instanceof P)) {
				ParaRPr rPr = para.p.getPPr() == null ? null : para.p.getPPr().getRPr();
				if (rPr != null) {
					rPr.setIns(null);
					rPr.setDel(null);
				}
				Props.pruneParagraph(para.p);
				return;
			}
			P next = (P) Trees.unwrap(nextItem);
			List<Object> moved = new ArrayList<Object>(para.p.getContent());
			for (int k = i + 1; k < j; k++) moved.add(container.get(k));
			// the properties the first paragraph loses: a section break's header and footer
			// references among them (C06), whose parts may now be unreferenced (D7)
			if (para.p.getPPr() != null) removals.discard(story, para.p.getPPr());
			para.p.getContent().clear();
			next.getContent().addAll(0, moved);
			Trees.setParents(moved, next);
			for (int k = j - 1; k >= i; k--) container.remove(k);
		}

		private boolean isMarker(Object item) {
			String name = Trees.name(item);
			return name.endsWith("RangeStart") || name.endsWith("RangeEnd") || "bookmarkStart".equals(name)
					|| "bookmarkEnd".equals(name) || "permStart".equals(name) || "permEnd".equals(name)
					|| "proofErr".equals(name);
		}
	}

	// --- helpers ------------------------------------------------------------------------------------

	/** The changes of a group, or the cells of a row: runs last first, then marks last first, so that a join never strands a piece still to do. */
	static void pieces(List<TrackedChange> pieces, boolean accept, Removals removals) {
		List<TrackedChange> marks = new ArrayList<TrackedChange>();
		List<TrackedChange> rest = new ArrayList<TrackedChange>();
		for (TrackedChange piece : pieces) {
			if (piece.target instanceof MarkT) marks.add(piece);
			else rest.add(piece);
		}
		for (int i = rest.size() - 1; i >= 0; i--) resolve(rest.get(i), accept, removals);
		for (int i = marks.size() - 1; i >= 0; i--) resolve(marks.get(i), accept, removals);
	}

	/** The live list a parent object keeps its children in, or null. */
	static List<Object> listOf(Object parent) {
		if (parent == null) return null;
		List<Object> list = Trees.children(parent);
		if (list != null) return list;
		if (parent instanceof org.docx4j.wml.SdtContent) return ((org.docx4j.wml.SdtContent) parent).getContent();
		if (parent instanceof ContentAccessor) return ((ContentAccessor) parent).getContent();
		return null;
	}

	private static Object ownerObject(List<Object> list, List<Object> moved) {
		for (Object o : list) {
			if (moved.contains(o)) continue;
			Object p = Trees.parentOf(o);
			if (p != null) return p;
		}
		return null;
	}

	static void removeQuietly(List<Object> list, Object item) {
		int i = Trees.indexOf(list, item);
		if (i >= 0) list.remove(i);
	}

	/** True when run-level content shows text in the accepted view. */
	static boolean hasText(Object item) {
		Object v = Trees.unwrap(item);
		if (v instanceof R) return Trees.hasText((R) v);
		Revision r = Trees.revisionOf(item);
		if (r == Revision.DEL || r == Revision.MOVE_FROM) return false;
		List<Object> items = Trees.runItems(v);
		if (items == null) return false;
		for (Object o : items) if (hasText(o)) return true;
		return false;
	}

	// --- w:t <-> w:delText -----------------------------------------------------------------------

	/** Deleted text become text again in every run under {@code items}: {@code w:delText} to {@code w:t}, {@code w:delInstrText} to {@code w:instrText}. */
	static void restoreText(List<Object> items) {
		if (items == null) return;
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof R) {
				List<Object> content = ((R) v).getContent();
				for (int i = 0; i < content.size(); i++) {
					Object restored = restoredText(content.get(i));
					if (restored != content.get(i)) {
						content.set(i, restored);
						Trees.setParent(restored, v);
					}
				}
			} else if (Trees.revisionOf(item) == null) {
				restoreText(Trees.runItems(v));
			}
		}
	}

	static Object restoredText(Object item) {
		Object v = Trees.unwrap(item);
		if (v instanceof DelText) {
			Text t = new Text();
			t.setValue(((DelText) v).getValue());
			t.setSpace(((DelText) v).getSpace());
			return WML.createRT(t);
		}
		if ("delInstrText".equals(Trees.name(item)) && v instanceof Text) {
			return WML.createRInstrText((Text) v);
		}
		return item;
	}

	/** Text become deleted text in every run under {@code items}. */
	static void deleteText(List<Object> items) {
		if (items == null) return;
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof R) {
				List<Object> content = ((R) v).getContent();
				for (int i = 0; i < content.size(); i++) {
					Object o = content.get(i);
					Object ov = Trees.unwrap(o);
					String name = Trees.name(o);
					if ("t".equals(name) && ov instanceof Text) {
						DelText d = new DelText();
						d.setValue(((Text) ov).getValue());
						d.setSpace(((Text) ov).getSpace());
						d.setParent(v);
						content.set(i, d);
					} else if ("instrText".equals(name) && ov instanceof Text) {
						JAXBElement<Text> d = WML.createRDelInstrText((Text) ov);
						content.set(i, d);
					}
				}
			} else if (Trees.revisionOf(item) == null) {
				deleteText(Trees.runItems(v));
			}
		}
	}

	// --- moves ---------------------------------------------------------------------------------------

	/**
	 * Moves (core-ts {@code resolveMove}, check 18 and check 20): Word pairs the halves by the
	 * {@code w:name} on their range starts; a half belongs to the move whose range of its side is
	 * open where it stands. Either half accepts or rejects the whole move.
	 */
	static final class Moves {

		private Moves() {}

		enum Kind { START, END, HALF, MARK, INNER }

		static final class Piece {
			final Kind kind;
			final boolean from;
			BigInteger id;
			String name;
			Object item;
			List<Object> owner;
			List<String> names = new ArrayList<String>();
			Para para;
			Piece(Kind kind, boolean from) {
				this.kind = kind;
				this.from = from;
			}
		}

		/** Every piece of move markup in a story, in document order, each half and mark with the names of the ranges open over it. */
		static List<Piece> piecesOf(List<Object> story) {
			List<Piece> out = new ArrayList<Piece>();
			Map<BigInteger, String> openFrom = new HashMap<BigInteger, String>();
			Map<BigInteger, String> openTo = new HashMap<BigInteger, String>();
			visit(story, false, openFrom, openTo, out);
			return out;
		}

		private static void visit(List<Object> items, boolean runLevel, Map<BigInteger, String> openFrom, Map<BigInteger, String> openTo, List<Piece> out) {
			for (Object item : items) {
				String name = Trees.name(item);
				Object v = Trees.unwrap(item);
				if (name.startsWith("moveFromRange") || name.startsWith("moveToRange")) {
					boolean from = name.startsWith("moveFrom");
					boolean start = name.endsWith("Start");
					BigInteger id = v instanceof CTMarkup ? ((CTMarkup) v).getId() : null;
					Piece piece = new Piece(start ? Kind.START : Kind.END, from);
					piece.id = id;
					piece.item = item;
					piece.owner = items;
					if (start) {
						piece.name = v instanceof CTBookmark && ((CTBookmark) v).getName() != null ? ((CTBookmark) v).getName() : "";
						(from ? openFrom : openTo).put(id, piece.name);
					} else {
						(from ? openFrom : openTo).remove(id);
					}
					out.add(piece);
					continue;
				}
				Revision revision = Trees.revisionOf(item);
				if (revision == Revision.MOVE_FROM || revision == Revision.MOVE_TO) {
					boolean from = revision == Revision.MOVE_FROM;
					Piece piece = new Piece(Kind.HALF, from);
					piece.item = item;
					piece.owner = items;
					piece.names.addAll((from ? openFrom : openTo).values());
					out.add(piece);
				} else if (runLevel) {
					for (boolean from : new boolean[] { true, false }) {
						Map<BigInteger, String> open = from ? openFrom : openTo;
						if (open.isEmpty()) continue;
						Piece piece = new Piece(Kind.INNER, from);
						piece.item = item;
						piece.owner = items;
						piece.names.addAll(open.values());
						out.add(piece);
					}
				}
				if (v == null) continue;
				List<Object> children = Trees.children(v);
				if (children != null) visit(children, v instanceof P, openFrom, openTo, out);
				if (v instanceof P) {
					ParaRPr rPr = ((P) v).getPPr() == null ? null : ((P) v).getPPr().getRPr();
					for (boolean from : new boolean[] { true, false }) {
						if (rPr == null || (from ? rPr.getMoveFrom() : rPr.getMoveTo()) == null) continue;
						Piece piece = new Piece(Kind.MARK, from);
						piece.para = new Para((P) v, item, items);
						piece.names.addAll((from ? openFrom : openTo).values());
						out.add(piece);
					}
				}
			}
		}

		private static Piece halfOf(List<Piece> pieces, Object item) {
			for (Piece piece : pieces) if (piece.kind == Kind.HALF && piece.item == item) return piece;
			return null;
		}

		/**
		 * Accepting keeps the destination and removes the source, paragraph marks and all; rejecting
		 * the other way round, and takes with it what was typed into the destination (check 20's
		 * "keep original location text"); the four range markers go either way. A destination whose
		 * source is gone is kept either way (check 20 case 12). False for a half in no named range.
		 */
		static boolean resolve(Op op, RunT t, boolean accept) {
			List<Piece> pieces = piecesOf(op.story.root);
			Piece half = halfOf(pieces, t.item);
			if (half == null || half.names.isEmpty()) return false;
			String name = half.names.get(0);
			Set<BigInteger> ids = new LinkedHashSet<BigInteger>();
			for (Piece piece : pieces) if (piece.kind == Kind.START && name.equals(piece.name)) ids.add(piece.id);
			List<Piece> mine = new ArrayList<Piece>();
			for (Piece piece : pieces) {
				if ((piece.kind == Kind.START && name.equals(piece.name)) || (piece.kind == Kind.END && ids.contains(piece.id))
						|| ((piece.kind == Kind.HALF || piece.kind == Kind.MARK || piece.kind == Kind.INNER) && piece.names.contains(name))) {
					mine.add(piece);
				}
			}
			boolean anyFrom = false, anyTo = false;
			for (Piece piece : mine) {
				if (piece.from) anyFrom = true;
				else anyTo = true;
			}
			boolean orphan = !anyFrom;
			boolean keptFrom = !(accept || orphan);
			boolean broken = orphan || !anyTo;

			// the markers first: a range end between two paragraphs would stop a join below
			for (Piece piece : mine) if (piece.kind == Kind.START || piece.kind == Kind.END) removeMarker(op, piece);
			if (keptFrom) {
				for (Piece piece : mine) if (piece.kind == Kind.INNER && !piece.from) op.remove(piece.owner, piece.item);
			}
			for (Piece piece : mine) {
				if (piece.kind != Kind.HALF) continue;
				if (piece.from == keptFrom) {
					if (piece.from) op.restoreDeleted(piece.item, piece.owner);
					else op.unwrap(piece.item, piece.owner);
				} else {
					op.remove(piece.owner, piece.item);
				}
			}
			// the paragraph marks last, last first, so that a join never disturbs one still to do
			List<Piece> marks = new ArrayList<Piece>();
			for (Piece piece : mine) if (piece.kind == Kind.MARK) marks.add(piece);
			for (int i = marks.size() - 1; i >= 0; i--) resolveMark(op, marks.get(i), marks.get(i).from == keptFrom);
			if (broken) dissolveBroken(op);
			return true;
		}

		private static void removeMarker(Op op, Piece piece) {
			List<Object> list = op.currentOwner(piece.owner, piece.item);
			removeQuietly(list, piece.item);
		}

		/** A moved paragraph's mark: kept, the marker goes; not kept, the paragraph goes when it is empty, else joins the next. */
		private static void resolveMark(Op op, Piece mark, boolean kept) {
			P p = mark.para.p;
			ParaRPr rPr = p.getPPr() == null ? null : p.getPPr().getRPr();
			if (rPr != null) {
				if (mark.from) rPr.setMoveFrom(null);
				else rPr.setMoveTo(null);
			}
			if (kept) {
				Props.pruneParagraph(p);
			} else if (p.getContent().isEmpty()) {
				op.remove(mark.para.container, mark.para.item); // the moved paragraph, emptied: gone, as in Word
			} else {
				op.joinWithNext(mark.para);
			}
		}

		/**
		 * A move half in no named range (check 20 cases 13 and 14): a {@code w:moveTo} resolved as an
		 * insertion, a {@code w:moveFrom} as a deletion, its paragraph's mark with it; then what is left
		 * of the broken pair is dissolved.
		 */
		static boolean resolveRangeless(Op op, RunT t, boolean accept) {
			List<Piece> pieces = piecesOf(op.story.root);
			Piece half = halfOf(pieces, t.item);
			if (half == null || !half.names.isEmpty()) return false;
			boolean keep = (!half.from) == accept;
			if (!keep) op.remove(t.owner, t.item);
			else if (!half.from) op.unwrap(t.item, t.owner);
			else op.restoreDeleted(t.item, t.owner);
			Para para = op.change.para;
			if (para != null) {
				for (Piece piece : pieces) {
					if (piece.kind != Kind.MARK || piece.from != half.from || piece.para.p != para.p || !piece.names.isEmpty()) continue;
					resolveMark(op, piece, keep);
					break;
				}
			}
			dissolveBroken(op);
			return true;
		}

		/**
		 * Every move half and moved mark in the story that cannot be paired - in no named range, or in
		 * one whose name no range of the other side carries - turned into the plain {@code w:ins} or
		 * {@code w:del} it stands for, as Word saved each broken pair in check 20.
		 */
		static void dissolveBroken(Op op) {
			List<Piece> pieces = piecesOf(op.story.root);
			Set<String> namedFrom = new LinkedHashSet<String>(), namedTo = new LinkedHashSet<String>();
			for (Piece piece : pieces) if (piece.kind == Kind.START) (piece.from ? namedFrom : namedTo).add(piece.name);
			for (Piece piece : pieces) {
				Set<String> other = piece.from ? namedTo : namedFrom;
				boolean unpaired = true;
				for (String n : piece.names) if (other.contains(n)) unpaired = false;
				if (!unpaired) continue;
				if (piece.kind == Kind.HALF) toPlainRevision(piece.item, piece.owner, piece.from);
				if (piece.kind == Kind.MARK) {
					ParaRPr rPr = piece.para.p.getPPr().getRPr();
					if (piece.from && rPr.getMoveFrom() != null) {
						rPr.setDel(rPr.getMoveFrom());
						rPr.setMoveFrom(null);
					}
					if (!piece.from && rPr.getMoveTo() != null) {
						rPr.setIns(rPr.getMoveTo());
						rPr.setMoveTo(null);
					}
				}
			}
		}

		/** A {@code w:moveFrom} become the plain {@code w:del} of the same runs, or a {@code w:moveTo} the plain {@code w:ins}; it keeps its id. */
		private static void toPlainRevision(Object item, List<Object> owner, boolean from) {
			int i = Trees.indexOf(owner, item);
			if (i < 0) return;
			CTTrackChange value = (CTTrackChange) Trees.unwrap(item);
			List<Object> items = new ArrayList<Object>(Trees.runItems(value));
			Object plain;
			List<Object> plainItems;
			if (from) {
				deleteText(items);
				RunDel del = WML.createRunDel();
				plain = del;
				plainItems = del.getCustomXmlOrSmartTagOrSdt();
			} else {
				RunIns ins = WML.createRunIns();
				plain = ins;
				plainItems = ins.getCustomXmlOrSmartTagOrSdt();
			}
			CTTrackChange p = (CTTrackChange) plain;
			p.setId(value.getId());
			p.setAuthor(value.getAuthor());
			p.setDate(value.getDate());
			p.setDateUtc(value.getDateUtc());
			plainItems.addAll(items);
			Trees.setParents(items, plain);
			p.setParent(value.getParent());
			owner.set(i, plain);
		}
	}
}
