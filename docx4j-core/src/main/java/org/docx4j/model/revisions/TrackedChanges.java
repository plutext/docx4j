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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.CTPPrChange;
import org.docx4j.wml.CTRPrChange;
import org.docx4j.wml.CTSectPrChange;
import org.docx4j.wml.CTTblGridChange;
import org.docx4j.wml.CTTblPrChange;
import org.docx4j.wml.CTTblPrExChange;
import org.docx4j.wml.CTTcPrChange;
import org.docx4j.wml.CTTrPrChange;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPrChange;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TcPr;

/**
 * Listing, accepting and rejecting a document's tracked changes, in every story: the main
 * document, headers, footers, footnotes, endnotes and comments, and inside content controls, text
 * boxes (both branches of {@code mc:AlternateContent}) and equations (CR-043). Word 365's own
 * Accept All and Reject All are the oracle; the rules, and where each comes from, are CR-043's
 * section 3.2.
 *
 * <pre>
 * for (TrackedChange change : TrackedChanges.in(pkg)) System.out.println(change);
 * TrackedChanges.acceptAll(pkg);   // or rejectAll
 * TrackedChanges.rejectAll(pkg, new TrackedChangeFilter().authors("Author B"));
 * </pre>
 *
 * @since 17.3.3
 */
public final class TrackedChanges {

	private TrackedChanges() {}

	/**
	 * Every tracked change, story by story (main document, footnotes, endnotes, comments, headers
	 * and footers; a text box's after the story it is in), each in document order, grouped as Office
	 * JS groups them. A text box's changes are listed once, from its {@code mc:Choice}; resolving one
	 * resolves its {@code mc:Fallback} twin too. The views are valid until the next accept or reject
	 * on the package.
	 */
	public static List<TrackedChange> in(WordprocessingMLPackage pkg) {
		List<TrackedChange> changes = new Lister(pkg, true).list();
		Generation generation = Generation.of(pkg);
		for (TrackedChange change : changes) stamp(change, generation);
		return changes;
	}

	/** The changes {@link #in(WordprocessingMLPackage)} lists that the filter matches (any of their pieces). */
	public static List<TrackedChange> in(WordprocessingMLPackage pkg, TrackedChangeFilter filter) {
		Set<Object> range = filter.range(pkg);
		List<TrackedChange> out = new ArrayList<TrackedChange>();
		for (TrackedChange change : in(pkg)) if (!matching(change, filter, range).isEmpty()) out.add(change);
		return out;
	}

	/** A change, and the pieces it groups (each a view that can be resolved on its own), valid from now. */
	private static void stamp(TrackedChange change, Generation generation) {
		change.generation = generation;
		if (change.target instanceof TrackedChange.GroupT) {
			for (TrackedChange piece : ((TrackedChange.GroupT) change.target).pieces) stamp(piece, generation);
		}
	}

	/** Accepts every change; returns how many {@link #in} listed. */
	public static int acceptAll(WordprocessingMLPackage pkg) {
		return resolveAll(pkg, true, null, false);
	}

	/** Rejects every change; returns how many {@link #in} listed. */
	public static int rejectAll(WordprocessingMLPackage pkg) {
		return resolveAll(pkg, false, null, false);
	}

	/**
	 * Accepts every change in a copy of a document made for a conversion, which is laid out and not
	 * saved ({@code ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES} on the exporter's working
	 * copy): as {@link #acceptAll(WordprocessingMLPackage)}, except that nothing outside the story
	 * parts is touched - no part, relationship, content type or extended comment entry is removed,
	 * since a working copy shares them with the document it was copied from - and a paragraph joined
	 * with the next keeps the first one's {@code w14:paraId}, by which a layout keyed by paragraph
	 * (Paginate) knows it.
	 */
	public static int acceptAllForConversion(WordprocessingMLPackage pkg) {
		return resolveAll(pkg, true, null, true);
	}

	/** Accepts the changes the filter matches; returns how many of those {@link #in} listed. */
	public static int acceptAll(WordprocessingMLPackage pkg, TrackedChangeFilter filter) {
		return resolveAll(pkg, true, filter, false);
	}

	/** Rejects the changes the filter matches; returns how many of those {@link #in} listed. */
	public static int rejectAll(WordprocessingMLPackage pkg, TrackedChangeFilter filter) {
		return resolveAll(pkg, false, filter, false);
	}

	/** Counts by kind, author and part. */
	public static TrackedChangesReport report(WordprocessingMLPackage pkg) {
		return new TrackedChangesReport(in(pkg));
	}

	/** The pieces of a change the filter matches (all of them with no filter). */
	private static List<TrackedChange> matching(TrackedChange change, TrackedChangeFilter filter, Set<Object> range) {
		List<TrackedChange> out = new ArrayList<TrackedChange>();
		for (TrackedChange piece : change.getPieces()) if (filter == null || filter.matches(piece, range)) out.add(piece);
		return out;
	}

	/**
	 * Last change first, so that a paragraph join never disturbs a change still to do; then the list
	 * is read again and the pass repeated until nothing is left, since resolving a move can carry a
	 * deletion nested in it out as a change of its own, and resolving a broken move leaves plain
	 * {@code w:ins}/{@code w:del} (core-ts {@code Body.resolveAll}); then, with no filter, what is never
	 * listed is swept. Both branches of a text box's {@code mc:AlternateContent} are walked. A filter's
	 * range is read once, before anything is resolved.
	 */
	private static int resolveAll(WordprocessingMLPackage pkg, boolean accept, TrackedChangeFilter filter, boolean conversion) {
		Set<Object> range = filter == null ? null : filter.range(pkg);
		int count = 0;
		for (TrackedChange change : new Lister(pkg, true).list()) if (!matching(change, filter, range).isEmpty()) count++;
		Removals removals = new Removals(pkg, conversion);
		int left = Integer.MAX_VALUE;
		int stalled = 0;
		for (int round = 0; round < 20 && stalled < 2; round++) {
			List<TrackedChange> changes = new Lister(pkg, false).list();
			int matched = 0;
			for (int i = changes.size() - 1; i >= 0; i--) {
				TrackedChange change = changes.get(i);
				List<TrackedChange> pieces = matching(change, filter, range);
				if (pieces.isEmpty()) continue;
				matched++;
				if (pieces.size() == change.getPieces().size()) Resolver.resolve(change, accept, removals);
				else Resolver.pieces(pieces, accept, removals);
			}
			if (matched == 0) break;
			stalled = matched >= left ? stalled + 1 : 0;
			left = matched;
		}
		if (filter == null) sweep(pkg, accept, removals);
		removals.flush();
		Generation.of(pkg).advance();
		return count;
	}

	/**
	 * What is never listed (core-ts {@code resolveUnlisted}): a table's records that change nothing,
	 * dropped either way; a {@code w:cellMerge}, which Office JS cannot list, a rejected one giving the
	 * merge it recorded back (U8); a {@code w:numberingChange}, which records the old number's text and
	 * not its definition, dropped either way; a {@code w:numPr/w:ins}, accepted dropped, rejected taking
	 * its {@code w:numPr} with it (U9); a {@code w:customXmlInsRangeStart} or {@code End} left without
	 * the form of an inserted control, which marks nothing that can be found.
	 */
	private static void sweep(WordprocessingMLPackage pkg, boolean accept, Removals removals) {
		List<JaxbXmlPart<?>> done = new ArrayList<JaxbXmlPart<?>>();
		for (Story story : new Lister(pkg, false).stories()) {
			if (done.contains(story.part)) continue;
			done.add(story.part);
			Object root = story.part.getJaxbElement();
			removals.removeFrom(root, item -> {
				String name = Trees.name(item);
				return "customXmlInsRangeStart".equals(name) || "customXmlInsRangeEnd".equals(name);
			}, null);
			Trees.walk(root, new Trees.Visitor() {
				@Override boolean enter(Object value, Object item) {
					// a record's own content is what was, not what is
					if (value instanceof CTPPrChange || value instanceof CTRPrChange || value instanceof ParaRPrChange
							|| value instanceof CTSectPrChange || value instanceof CTTblPrChange || value instanceof CTTrPrChange
							|| value instanceof CTTcPrChange || value instanceof CTTblPrExChange || value instanceof CTTblGridChange) return false;
					if (value instanceof Tbl && Tables.hasRecords((Tbl) value)) Tables.accept((Tbl) value);
					if (value instanceof TcPr && ((TcPr) value).getCellMerge() != null) Tables.resolveCellMerge((TcPr) value, accept);
					if (value instanceof FldChar) ((FldChar) value).setNumberingChange(null);
					if (value instanceof PPrBase && ((PPrBase) value).getNumPr() != null) {
						PPrBase pPr = (PPrBase) value;
						PPrBase.NumPr numPr = pPr.getNumPr();
						numPr.setNumberingChange(null);
						if (numPr.getIns() != null) {
							if (accept) numPr.setIns(null);
							else pPr.setNumPr(null);
						}
					}
					return true;
				}
			});
		}
	}
}
