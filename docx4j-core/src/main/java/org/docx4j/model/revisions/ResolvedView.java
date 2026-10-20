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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.model.revisions.Resolver.Moves;
import org.docx4j.model.revisions.Resolver.Moves.Kind;
import org.docx4j.model.revisions.Resolver.Moves.Piece;
import org.docx4j.model.revisions.TrackedChange.CellT;
import org.docx4j.model.revisions.TrackedChange.FieldT;
import org.docx4j.model.revisions.TrackedChange.GroupT;
import org.docx4j.model.revisions.TrackedChange.HolderT;
import org.docx4j.model.revisions.TrackedChange.MarkT;
import org.docx4j.model.revisions.TrackedChange.MathRunT;
import org.docx4j.model.revisions.TrackedChange.MathStructT;
import org.docx4j.model.revisions.TrackedChange.Para;
import org.docx4j.model.revisions.TrackedChange.RowT;
import org.docx4j.model.revisions.TrackedChange.RunT;
import org.docx4j.model.revisions.Trees.Revision;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

/**
 * What Word's Accept All or Reject All would leave of a package, as {@link TrackedChanges#acceptAll}
 * and {@link TrackedChanges#rejectAll} leave it, worked out without resolving anything: the content
 * each removes and the paragraphs each joins (CR-046 D2). A reader walks the unresolved tree and asks
 * this view what is there, so that the text it reads in a view is the text of the resolved document
 * (WD REQ-402), and the package is untouched (WD REQ-204).
 *
 * <p>It runs CR-043's own listing ({@code Lister}, every branch of a text box's
 * {@code mc:AlternateContent}) and records, change by change, what {@code Resolver} would do to the
 * text: a revision taken away, a move's halves and the text typed into a rejected move's destination,
 * an inserted field rejected whole, a row or cell taken away, an equation's run-level revision or
 * structure, a paragraph mark taken away, and an inserted content control rejected (its content left
 * in its place). What a resolution does to properties, range markers, comments' entries and parts is
 * not recorded. The agreement test ({@code ViewAgreementTest}, WD
 * REQ-406) holds a walk over this view equal to the text of a resolved copy; where they differ, this
 * class is wrong.</p>
 *
 * <p>A snapshot: valid until the package changes.</p>
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class ResolvedView {

	private final boolean accepted;
	/** List members gone, by identity of their values (a {@code JAXBElement}'s value, or the member). */
	private final Set<Object> removed = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
	/** Content controls and custom XML elements gone, their content standing in their place. */
	private final Set<Object> unwrapped = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
	/** Paragraphs whose mark is gone. */
	private final Set<Object> marksGone = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
	/** Moved paragraphs whose mark is gone: gone themselves when nothing is left in them, else joined. */
	private final Set<Object> movedMarksGone = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

	/** Comments whose range held a revision taken away: gone with it once nothing with text is left in their range. */
	private final List<CommentCandidate> commentCandidates = new ArrayList<CommentCandidate>();
	/** The same, with the range's start and end in different paragraphs (CR-043 U23). */
	private final List<SpanCandidate> spanCandidates = new ArrayList<SpanCandidate>();
	private final Set<BigInteger> commentsGone = new LinkedHashSet<BigInteger>();

	private ResolvedView(WordprocessingMLPackage pkg, boolean accepted) {
		this.accepted = accepted;
		List<TrackedChange> changes = new Lister(pkg, false).list();
		// moves first, story by story, in Resolver's order: their outcome depends on it
		Map<Story, List<TrackedChange>> moves = new LinkedHashMap<Story, List<TrackedChange>>();
		for (TrackedChange change : changes) collectMoves(change, moves);
		for (Map.Entry<Story, List<TrackedChange>> e : moves.entrySet()) new MoveReplay(e.getKey(), e.getValue()).run();
		for (TrackedChange change : changes) plan(change);
		for (CommentCandidate c : commentCandidates) if (c.rangeEmpty()) commentsGone.add(c.id);
		for (SpanCandidate c : spanCandidates) if (c.rangeEmpty()) commentsGone.add(c.id);
	}

	private static void collectMoves(TrackedChange change, Map<Story, List<TrackedChange>> moves) {
		if (change.target instanceof GroupT) {
			for (TrackedChange piece : ((GroupT) change.target).pieces) collectMoves(piece, moves);
		} else if (change.target instanceof RowT) {
			for (TrackedChange inner : ((RowT) change.target).inner) collectMoves(inner, moves);
		} else if (isMove(change)) {
			List<TrackedChange> list = moves.get(change.story);
			if (list == null) moves.put(change.story, list = new ArrayList<TrackedChange>());
			list.add(change);
		}
	}

	private static boolean isMove(TrackedChange change) {
		if (!(change.target instanceof RunT)) return false;
		Revision r = ((RunT) change.target).revision;
		return r == Revision.MOVE_FROM || r == Revision.MOVE_TO;
	}

	/** The document after Accept All. */
	public static ResolvedView accepted(WordprocessingMLPackage pkg) {
		return new ResolvedView(pkg, true);
	}

	/** The document after Reject All. */
	public static ResolvedView original(WordprocessingMLPackage pkg) {
		return new ResolvedView(pkg, false);
	}

	/** True for the view after Accept All, false for Reject All. */
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * Whether a list member is gone in this view: a run-level revision ({@code w:ins}, {@code w:del},
	 * {@code w:moveFrom}, {@code w:moveTo}) taken away with its content, a run, a row, a cell, a
	 * paragraph, an equation's run-level revision or structure. A revision that is not gone is kept:
	 * its content is read as if it were not there. A {@code JAXBElement} and its value alike.
	 */
	public boolean removes(Object item) {
		Object v = Trees.unwrap(item);
		if (v == null) return false;
		if (removed.contains(v)) return true;
		return v instanceof P && movedMarksGone.contains(v) && !remainsIn((P) v);
	}

	/**
	 * Whether a content control or custom XML element is gone in this view with its content left in its
	 * place: one inserted under tracking ({@code w:customXmlInsRangeStart} around it), rejected. Its
	 * properties go with it, a data binding among them (CR-046 phase 2, REQ-509).
	 */
	public boolean unwraps(Object item) {
		Object v = Trees.unwrap(item);
		return v != null && unwrapped.contains(v);
	}

	/**
	 * Whether a paragraph's mark is gone in this view: a deletion accepted, an insertion rejected, a
	 * move's mark on the side not kept. Such a paragraph joins the next one in its list, past range
	 * markers and what this view removes, when that is a paragraph; with no paragraph next (a table,
	 * the end of a cell or a story) only the mark goes (CR-043 U1).
	 */
	public boolean removesMark(P p) {
		return p != null && (marksGone.contains(p) || movedMarksGone.contains(p));
	}

	/**
	 * Whether a comment goes in this view, entry and reference: one whose range started on a revision
	 * taken away, or whose markers lie either side of one, once nothing with text is left in its range
	 * (CR-043, check 32; Word 365, 21 October 2026).
	 */
	public boolean removesComment(BigInteger id) {
		return id != null && commentsGone.contains(id);
	}

	// --- the plan, change by change, as Resolver.Op.run resolves each ---------------------------------

	private void plan(TrackedChange change) {
		TrackedChange.Target t = change.target;
		if (t instanceof RunT) {
			runRevision(change, (RunT) t);
		} else if (t instanceof MarkT) {
			if (change.para != null && ((MarkT) t).ins != accepted) marksGone.add(change.para.p);
		} else if (t instanceof RowT) {
			RowT row = (RowT) t;
			if (row.ins == accepted) {
				for (TrackedChange inner : row.inner) plan(inner);
			} else {
				removed.add(row.tr);
			}
		} else if (t instanceof GroupT) {
			for (TrackedChange piece : ((GroupT) t).pieces) plan(piece);
		} else if (t instanceof FieldT) {
			// Word's inserted field: rejected, the whole field goes, its separator and result too
			for (Object el : ((FieldT) t).span) {
				if (Trees.revisionOf(el) == Revision.INS) {
					if (!accepted) removeRevision(el, ((FieldT) t).owner);
				} else if (!accepted && Trees.unwrap(el) instanceof R) {
					removed.add(Trees.unwrap(el));
				}
			}
		} else if (t instanceof MathRunT) {
			MathRunT m = (MathRunT) t;
			if (m.ins != accepted) removed.add(Trees.unwrap(m.item));
		} else if (t instanceof MathStructT) {
			MathStructT m = (MathStructT) t;
			if (m.ins != accepted && m.structItem != null) removed.add(Trees.unwrap(m.structItem));
		} else if (t instanceof CellT) {
			CellT c = (CellT) t;
			if (c.ins != accepted) removed.add(c.tc);
		}
		else if (t instanceof HolderT) {
			// a content control inserted keeps its content either way; rejected, the control itself goes
			if (!accepted) unwrapped.add(Trees.unwrap(((HolderT) t).holder));
		}
		// property records change no text
	}

	private void runRevision(TrackedChange change, RunT t) {
		if (t.revision == Revision.MOVE_FROM || t.revision == Revision.MOVE_TO) return; // replayed
		boolean kept = (t.revision == Revision.INS || t.revision == Revision.MOVE_TO) == accepted;
		if (!kept) removeRevision(t.item, t.owner);
	}

	/**
	 * {@code Resolver.removeRevision}: the revision goes, and the comments whose range started on it,
	 * or whose markers lie either side of it in its list, are candidates; so is one with a marker in
	 * its list on the revision's side and the other in another paragraph (U23).
	 */
	private void removeRevision(Object item, List<Object> owner) {
		Object v = Trees.unwrap(item);
		removed.add(v);
		List<Object> inner = Trees.runItems(v);
		if (inner == null || owner == null) return;
		int at = Trees.indexOf(owner, item);
		if (at < 0) return;
		for (Object o : inner) {
			Object ov = Trees.unwrap(o);
			if (!(ov instanceof CommentRangeStart)) continue;
			BigInteger id = ((CommentRangeStart) ov).getId();
			if (Resolver.markerAt(owner, at + 1, owner.size(), CommentRangeEnd.class, id) >= 0) commentCandidates.add(new CommentCandidate(id, owner, at));
			else spanCandidates.add(new SpanCandidate(id, item, owner, at, true));
		}
		for (int s = 0; s < at; s++) {
			Object sv = Trees.unwrap(owner.get(s));
			if (!(sv instanceof CommentRangeStart)) continue;
			BigInteger id = ((CommentRangeStart) sv).getId();
			if (Resolver.markerAt(owner, s + 1, owner.size(), CommentRangeEnd.class, id) < 0) spanCandidates.add(new SpanCandidate(id, item, owner, s + 1, true));
			else if (Resolver.markerAt(owner, at + 1, owner.size(), CommentRangeEnd.class, id) >= 0) commentCandidates.add(new CommentCandidate(id, owner, s));
		}
		for (int e = at + 1; e < owner.size(); e++) {
			Object ev = Trees.unwrap(owner.get(e));
			if (ev instanceof CommentRangeEnd && Resolver.markerAt(owner, 0, e, CommentRangeStart.class, ((CommentRangeEnd) ev).getId()) < 0) {
				spanCandidates.add(new SpanCandidate(((CommentRangeEnd) ev).getId(), item, owner, e, false));
			}
		}
	}

	/**
	 * A comment whose range held a revision taken away, its start and end in different paragraphs of one
	 * list: gone, as {@code Removals.removeCommentIfEmptied} judges it once the joins are made, when every
	 * paragraph from the start's up to the end's loses its mark (so that they join), with nothing between
	 * them but range markers and what this view removes, and nothing with text in the range.
	 */
	private final class SpanCandidate {
		final BigInteger id;
		final Object revision;
		final List<Object> owner;
		/** In {@code owner}: where the range's text begins to be looked for (the start's side), or the end's index. */
		final int at;
		final boolean startHere;
		SpanCandidate(BigInteger id, Object revision, List<Object> owner, int at, boolean startHere) {
			this.id = id;
			this.revision = revision;
			this.owner = owner;
			this.at = at;
			this.startHere = startHere;
		}

		boolean rangeEmpty() {
			Object pv = Trees.parentOf(revision);
			if (!(pv instanceof P) || ((P) pv).getContent() != owner) return false;
			P here = (P) pv;
			List<Object> container = Resolver.listOf(Trees.parentOf(here));
			int k = indexOfValue(container, here);
			if (k < 0) return false;
			if (startHere) {
				for (int i = at; i < owner.size(); i++) if (hasText(owner.get(i))) return false;
				P p = here;
				while (true) {
					if (!removesMark(p)) return false; // a mark left between start and end (U24)
					k++;
					while (k < container.size() && skipped(container.get(k))) {
						if (isMarker(container.get(k), CommentRangeEnd.class)) return true;
						k++;
					}
					if (k >= container.size() || !(Trees.unwrap(container.get(k)) instanceof P)) return false;
					p = (P) Trees.unwrap(container.get(k));
					int end = Resolver.markerAt(p.getContent(), 0, p.getContent().size(), CommentRangeEnd.class, id);
					int to = end < 0 ? p.getContent().size() : end;
					for (int i = 0; i < to; i++) if (hasText(p.getContent().get(i))) return false;
					if (end >= 0) return true;
				}
			} else {
				for (int i = 0; i < at; i++) if (hasText(owner.get(i))) return false;
				while (true) {
					k--;
					while (k >= 0 && skipped(container.get(k))) {
						if (isMarker(container.get(k), CommentRangeStart.class)) return true;
						k--;
					}
					if (k < 0 || !(Trees.unwrap(container.get(k)) instanceof P)) return false;
					P p = (P) Trees.unwrap(container.get(k));
					if (!removesMark(p)) return false; // a mark left between start and end (U24)
					int start = Resolver.markerAt(p.getContent(), 0, p.getContent().size(), CommentRangeStart.class, id);
					for (int i = start + 1; i < p.getContent().size(); i++) if (hasText(p.getContent().get(i))) return false;
					if (start >= 0) return true;
				}
			}
		}

		/** A range marker between paragraphs, which a join carries along, or a member this view removes. */
		private boolean skipped(Object item) {
			String name = Trees.name(item);
			return name.endsWith("RangeStart") || name.endsWith("RangeEnd") || "bookmarkStart".equals(name)
					|| "bookmarkEnd".equals(name) || "permStart".equals(name) || "permEnd".equals(name)
					|| "proofErr".equals(name) || (!(Trees.unwrap(item) instanceof P) && removes(item));
		}

		private boolean isMarker(Object item, Class<?> kind) {
			Object v = Trees.unwrap(item);
			if (!kind.isInstance(v)) return false;
			BigInteger at = v instanceof CommentRangeStart ? ((CommentRangeStart) v).getId() : ((CommentRangeEnd) v).getId();
			return java.util.Objects.equals(at, id);
		}
	}

	private static int indexOfValue(List<Object> list, Object value) {
		if (list == null) return -1;
		for (int i = 0; i < list.size(); i++) if (Trees.unwrap(list.get(i)) == value) return i;
		return -1;
	}

	/** A comment whose range held a revision taken away, and where its range's text begins to be looked for. */
	private final class CommentCandidate {
		final BigInteger id;
		final List<Object> owner;
		final int at;
		CommentCandidate(BigInteger id, List<Object> owner, int at) {
			this.id = id;
			this.owner = owner;
			this.at = at;
		}

		/** {@code Resolver.rangeIsEmpty}, on what this view leaves: nothing with text before the range's end, in the same list. */
		boolean rangeEmpty() {
			for (int i = at + 1; i < owner.size(); i++) {
				Object item = owner.get(i);
				Object v = Trees.unwrap(item);
				if (v instanceof CommentRangeEnd && java.util.Objects.equals(((CommentRangeEnd) v).getId(), id)) return true;
				if (hasText(item)) return false;
			}
			return false; // the end is elsewhere: text may be
		}
	}

	/** Whether run-level content has text in this view, once its removals are made. */
	private boolean hasText(Object item) {
		Object v = Trees.unwrap(item);
		if (v == null || removed.contains(v)) return false;
		if (v instanceof R) return Trees.hasText((R) v);
		List<Object> items = Trees.runItems(v);
		if (items == null) return false;
		for (Object o : items) if (hasText(o)) return true;
		return false;
	}

	/**
	 * The moves of one story resolved as {@code TrackedChanges.resolveAll} resolves them: last half
	 * first; a half in a named range resolves its whole move ({@code Moves.resolve}), a half in none
	 * is an insertion or a deletion ({@code Moves.resolveRangeless}), and after a broken move what is
	 * left unpaired becomes the plain {@code w:ins} or {@code w:del} it stands for
	 * ({@code Moves.dissolveBroken}), resolved as such. Which half comes first decides the outcome:
	 * a destination whose source is gone is kept either way when it is resolved first, but becomes a
	 * plain insertion when a source in no range after it is (check 20, {@code moveFrom-no-ranges}).
	 */
	private final class MoveReplay {
		final List<TrackedChange> halves;
		final List<Piece> pieces;
		/** Pieces resolved, or turned plain: no longer move markup. */
		final Set<Piece> done = Collections.newSetFromMap(new IdentityHashMap<Piece, Boolean>());
		/** Halves and marks turned into plain revisions: their sides. */
		final Map<Piece, Boolean> plain = new IdentityHashMap<Piece, Boolean>();

		MoveReplay(Story story, List<TrackedChange> halves) {
			this.halves = halves;
			this.pieces = Moves.piecesOf(story.root);
		}

		void run() {
			for (int i = halves.size() - 1; i >= 0; i--) {
				TrackedChange change = halves.get(i);
				Piece half = halfOf(((RunT) change.target).item);
				if (half == null || done.contains(half)) continue;
				if (!half.names.isEmpty()) named(half);
				else rangeless(half, change.para);
			}
			// what became plain is resolved as plain markup, in the next round
			for (Map.Entry<Piece, Boolean> e : plain.entrySet()) {
				Piece piece = e.getKey();
				boolean ins = !piece.from;
				if (piece.kind == Kind.HALF) {
					if (ins != accepted) removeRevision(piece.item, piece.owner);
				} else if (piece.kind == Kind.MARK) {
					if (ins != accepted) marksGone.add(piece.para.p);
				}
			}
		}

		private Piece halfOf(Object item) {
			for (Piece piece : pieces) if (piece.kind == Kind.HALF && piece.item == item) return piece;
			return null;
		}

		private boolean live(Piece piece) {
			return !done.contains(piece);
		}

		/** {@code Moves.resolve}. */
		private void named(Piece half) {
			String name = half.names.get(0);
			Set<BigInteger> ids = new LinkedHashSet<BigInteger>();
			for (Piece piece : pieces) if (live(piece) && piece.kind == Kind.START && name.equals(piece.name)) ids.add(piece.id);
			List<Piece> mine = new ArrayList<Piece>();
			for (Piece piece : pieces) {
				if (!live(piece)) continue;
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
			boolean keptFrom = !(accepted || orphan);
			boolean broken = orphan || !anyTo;
			for (Piece piece : mine) {
				done.add(piece);
				if (piece.kind == Kind.INNER && !piece.from && keptFrom) removed.add(Trees.unwrap(piece.item));
				if (piece.kind == Kind.HALF && piece.from != keptFrom) removed.add(Trees.unwrap(piece.item));
				if (piece.kind == Kind.MARK && piece.from != keptFrom) movedMarksGone.add(piece.para.p);
			}
			if (broken) dissolveBroken();
		}

		/** {@code Moves.resolveRangeless}. */
		private void rangeless(Piece half, Para para) {
			boolean keep = (!half.from) == accepted;
			done.add(half);
			if (!keep) removed.add(Trees.unwrap(half.item));
			if (para != null) {
				for (Piece piece : pieces) {
					if (!live(piece) || piece.kind != Kind.MARK || piece.from != half.from || piece.para.p != para.p || !piece.names.isEmpty()) continue;
					done.add(piece);
					if (!keep) movedMarksGone.add(piece.para.p);
					break;
				}
			}
			dissolveBroken();
		}

		/** {@code Moves.dissolveBroken}: every half and mark that cannot be paired becomes plain. */
		private void dissolveBroken() {
			Set<String> namedFrom = new LinkedHashSet<String>(), namedTo = new LinkedHashSet<String>();
			for (Piece piece : pieces) if (live(piece) && piece.kind == Kind.START) (piece.from ? namedFrom : namedTo).add(piece.name);
			for (Piece piece : pieces) {
				if (!live(piece) || (piece.kind != Kind.HALF && piece.kind != Kind.MARK)) continue;
				Set<String> other = piece.from ? namedTo : namedFrom;
				boolean unpaired = true;
				for (String n : piece.names) if (other.contains(n)) unpaired = false;
				if (!unpaired) continue;
				done.add(piece);
				plain.put(piece, Boolean.TRUE);
			}
		}
	}

	/**
	 * Whether anything is left in a paragraph once this view's removals are made: {@code Resolver}
	 * removes an emptied moved paragraph and joins one that is not. A comment's range markers inside
	 * a revision taken away stay behind it.
	 */
	private boolean remainsIn(P p) {
		for (Object item : p.getContent()) {
			Object v = Trees.unwrap(item);
			if (!removed.contains(v)) return true;
			List<Object> inner = Trees.runItems(v);
			if (inner != null && Trees.revisionOf(item) != null) {
				for (Object o : inner) {
					Object ov = Trees.unwrap(o);
					if (ov instanceof CommentRangeStart || ov instanceof CommentRangeEnd) return true;
				}
			}
		}
		return false;
	}
}
