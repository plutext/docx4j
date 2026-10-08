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
package org.docx4j.fop.wordlayout;

import java.util.List;
import java.util.ListIterator;

import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.layoutmgr.FlowLayoutManager;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;

/**
 * FOP's FlowLayoutManager, which additionally moves each paragraph's trailing
 * {@link LeadingGlue} behind the break possibility that follows the
 * paragraph.  The line manager can only put that glue at the end of its own
 * list; the break between blocks is added by the flow (or another block
 * container) after it, and glue before a taken break is counted on the page,
 * so Word's rule (the last line's leading may hang below the margin) needs
 * the glue after the penalty.  Space resolution has already turned the
 * BreakElements into penalties when this runs; zero-width auxiliary boxes
 * (the one the line manager ends its list with, FOP's own from space
 * resolution) are passed over on the way to the penalty.
 *
 * <p>At a <b>forced</b> break the glue is dropped rather than moved: a forced
 * break discards glue at the start of the page or column it opens, so the two
 * come to the same thing on the page, but a glue after a forced break which
 * ends the element list makes FOP's {@code ElementListUtils.endsWithForcedBreak}
 * false and the break is then not taken at all (17.1.0).</p>
 *
 * @since 17.0.5
 */
public class WordFlowLayoutManager extends FlowLayoutManager {

	private static final org.slf4j.Logger LOG =
			org.slf4j.LoggerFactory.getLogger(WordFlowLayoutManager.class);

	public WordFlowLayoutManager(PageSequenceLayoutManager pslm, Flow node) {
		super(pslm, node);
	}

	@Override
	public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {
		List<ListElement> elements = super.getNextKnuthElements(context, alignment);
		moveLeadingBehindBreaks(elements);
		boundKeepChains(elements, availableBPD());
		return elements;
	}

	// ---------------------------------------------------- an infeasible keep chain

	/**
	 * <b>Word drops a keep it cannot satisfy; FOP overflows the page.</b>  A
	 * {@code w:keepNext} becomes {@code keep-with-next.within-page="always"}, which
	 * {@code BlockStackingLayoutManager.addInBetweenBreak} writes as a penalty of
	 * {@link KnuthElement#INFINITE} between the two blocks.  Where a whole run of
	 * paragraphs carries it - a numbered clause list whose every item keeps with the
	 * next, which is how a contract template is written - the breaker has no legal break
	 * anywhere in the run, and rather than break it FOP puts the lot on one page and
	 * lets it run off the bottom.  Measured on a corpus document with 88
	 * {@code keep-with-next="always"}: <b>one page held 1037 lines and ran to
	 * y=7693.5 on a 792pt page</b>, where Word spreads that content over 14 pages; a
	 * second, with 10011 of them, is 42 pages short of Word (Word's page 50 ends at
	 * y=521.7, ours at 675.2).  Between them, 52 pages of the corpus's page deficit.
	 *
	 * <p>Word applies {@code w:keepNext} locally: the paragraph is kept with the next
	 * one where the two fit on a page together, and where they do not the keep is
	 * simply ignored at that point - the heading stays with the first lines of its
	 * paragraph and the rest flows on.  So a keep chain is bounded here: where the
	 * height accumulated since the last break the breaker may take is several times
	 * the page's available BPD (below), the infinite penalties of that chain are
	 * reduced to a large but finite one.  The breaker can then break inside the chain, and because the
	 * penalty is still large it breaks there only where it must - which is Word's
	 * rule.  Nothing changes for a keep chain that fits, and that is every keep in a
	 * document Word lays out the same way.</p>
	 *
	 * <p><b>The chain has to exceed the page several times over.</b>  The height summed
	 * here is an over-estimate of what the page must hold - it is the flow's own
	 * element list, where a table's rows are boxes beside the block they are in, and
	 * where line heights that are each a little taller than Word's accumulate - so a
	 * chain only somewhat over the page is one Word (and FOP) does fit.  Measured on a
	 * document of 71 Word pages: its widest keep chain sums to 726859 against a 650900
	 * body, 12% over, and Word puts all 166 lines of it on one page; bounding that
	 * chain gave the document a 72nd page.  At three times the page both that document
	 * and the 1037-line overflow come out at Word's own page count exactly (45/45,
	 * where it was 35), and 2.5x, 4x and 6x are each worse on one of the two.  The
	 * tolerance is {@code docx4j.convert.out.fo.wordLayout.keepChainTolerance},
	 * default 3.0.</p>
	 *
	 * <p>{@code docx4j.convert.out.fo.wordLayout.boundKeepChains=false} turns it off;
	 * {@code docx4j.convert.out.fo.wordLayout.keepChainPenalty} is the penalty the
	 * keep is reduced to (default 900, against FOP's infinite 1000).</p>
	 *
	 * @param available the page's available block-progression dimension, in millipoints;
	 *                  zero or less leaves the list alone
	 * @since 17.1.0
	 */
	static void boundKeepChains(List<ListElement> elements, int available) {

		if (available <= 0 || elements == null || elements.isEmpty()) return;
		if (!org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.wordLayout.boundKeepChains", true)) return;
		int reduced;
		try {
			reduced = Integer.parseInt(org.docx4j.Docx4jProperties.getProperty(
					"docx4j.convert.out.fo.wordLayout.keepChainPenalty", "900").trim());
		} catch (NumberFormatException e) {
			reduced = 900;
		}
		if (reduced >= KnuthElement.INFINITE) reduced = KnuthElement.INFINITE - 1;
		double tolerance;
		try {
			tolerance = Double.parseDouble(org.docx4j.Docx4jProperties.getProperty(
					"docx4j.convert.out.fo.wordLayout.keepChainTolerance", "3.0").trim());
		} catch (NumberFormatException e) {
			tolerance = 3.0;
		}
		if (tolerance < 1.0) tolerance = 1.0;
		long limit = (long) (available * tolerance);
		/* A chain made only of a table's row keeps is bounded at the page itself.  Its height
		 * is the rows' own, not the over-estimate the 3x allows for, and Word's rule is
		 * measured: on the table-keeps probe (30 exact 30pt rows each keeping with the next,
		 * with or without w:cantSplit, started two thirds down a page; corpus document 7396's
		 * shape) Word moves the table to a fresh page, fills it and breaks it there, at
		 * modes 12, 14 and 15 alike.  Bounded at 3x the chain stayed whole and FOP ran it off
		 * the page: rows 27-30 and the paragraph after the table were not painted at all.
		 * Bounded at the page, every case is on Word's pages.
		 * Property docx4j.convert.out.fo.wordLayout.keepChainRowTolerance, default 1.0.
		 * @since 17.3.1 */
		double rowTolerance;
		try {
			rowTolerance = Double.parseDouble(org.docx4j.Docx4jProperties.getProperty(
					"docx4j.convert.out.fo.wordLayout.keepChainRowTolerance", "1.0").trim());
		} catch (NumberFormatException e) {
			rowTolerance = 1.0;
		}
		if (rowTolerance < 1.0) rowTolerance = 1.0;
		long rowLimit = (long) (available * rowTolerance);

		// the infinite penalties passed since the last break the breaker may take, the
		// height accumulated over them, and whether every one of them is a table's
		List<KnuthPenalty> chain = new java.util.ArrayList<KnuthPenalty>();
		int height = 0;
		boolean rowsOnly = true;

		for (ListElement el : elements) {
			if (!(el instanceof KnuthElement)) continue;
			KnuthElement k = (KnuthElement) el;
			if (k.isBox() || k.isGlue()) {
				height += k.getWidth();
				continue;
			}
			if (!k.isPenalty()) continue;
			KnuthPenalty p = (KnuthPenalty) k;
			if (p.getPenalty() < KnuthElement.INFINITE) {
				// a legal break: the segment since the last one must sit on one page whole,
				// its last member included (the check below, made at each keep, sees only
				// what precedes the keep: a heading's keep before a table never counted the
				// table's header and first row, so a chain of heading, 481pt picture, heading
				// and a 130pt header row ran 119pt off 12301's page 11 where Word breaks
				// before the table)
				bound(chain, height, rowsOnly ? rowLimit : limit, reduced, available);
				chain.clear();
				height = 0;
				rowsOnly = true;
				continue;
			}
			/* An auxiliary penalty with no position is space resolution's (SpaceResolver.generate):
			 * it keeps a space, padding or border with its content and is no keep of the
			 * document's, so it neither joins the chain - reducing it would let a page break fall
			 * between the padding and what it pads - nor makes a table's chain a mixed one.  The
			 * block WordLayoutFixups puts round a bordered table, padded by half its outer
			 * borders, adds two; counted, a table whose rows keep with the next ran off the page
			 * in it (corpus document 10730, rows lost).  ElementListUtils.removeLegalBreaks's
			 * inhibitors (a table's or list's widow and orphan content limits) are not auxiliary
			 * and still count.  @since 17.3.1 */
			if (p.getPosition() == null && p.isAuxiliary()) continue;
			chain.add(p);
			rowsOnly &= isTableContent(p);
			if (height > (rowsOnly ? rowLimit : limit)) {
				// this chain cannot be satisfied on any page; let the breaker into it
				bound(chain, height, rowsOnly ? rowLimit : limit, reduced, available);
				chain.clear();
				height = 0;
				rowsOnly = true;
			}
		}
		bound(chain, height, rowsOnly ? rowLimit : limit, reduced, available);
	}

	/** Reduce the chain's keeps to the finite penalty where the segment's height exceeds the limit. */
	private static void bound(List<KnuthPenalty> chain, int height, long limit, int reduced, int available) {
		if (chain.isEmpty() || height <= limit) return;
		if (LOG.isDebugEnabled()) {
			LOG.debug("keep chain of " + chain.size() + " bounded: " + height + " > " + limit + " (page " + available + ")");
		}
		for (KnuthPenalty each : chain) {
			each.setPenalty(reduced);
		}
	}

	/** Whether a penalty is a table's own - the stepper's break between two of its rows - by
	 *  its positions, walked inward through every wrapper: the flow's and the blocks'
	 *  NonLeafPositions and space resolution's SpaceHandlingBreakPosition around the table
	 *  manager's TableHFPenaltyPosition (package-private, so known by its manager).
	 *  @since 17.3.1 */
	static boolean isTableContent(KnuthPenalty p) {
		org.apache.fop.layoutmgr.Position pos = p.getPosition();
		for (int depth = 0; pos != null && depth < 16; depth++) {
			if (pos instanceof org.apache.fop.layoutmgr.table.TableContentPosition
					|| pos.getLM() instanceof org.apache.fop.layoutmgr.table.TableContentLayoutManager
					|| pos.getLM() instanceof org.apache.fop.layoutmgr.table.TableLayoutManager) {
				return true;
			}
			org.apache.fop.layoutmgr.Position inner = pos.getPosition();
			if (inner == pos) break;
			pos = inner;
		}
		return false;
	}

	/** The block-progression dimension of the page body, in millipoints; 0 where the
	 *  page is not available yet (the pass is then skipped). */
	private int availableBPD() {
		try {
			org.apache.fop.area.PageViewport pv = getCurrentPV();
			return pv == null || pv.getBodyRegion() == null ? 0 : pv.getBodyRegion().getBPD();
		} catch (RuntimeException e) {
			return 0;
		}
	}

	/**
	 * For each LeadingGlue followed, before any box, by a penalty at which a
	 * break may be taken (not infinite), move the glue right after that
	 * penalty.  Between lines the line manager already emits the glue after
	 * the break; this is for the last line of a paragraph.
	 */
	static void moveLeadingBehindBreaks(List<ListElement> elements) {
		ListIterator<ListElement> it = elements.listIterator();
		while (it.hasNext()) {
			ListElement el = it.next();
			if (!(el instanceof LeadingGlue)) continue;
			int glueIndex = it.previousIndex();
			int target = -1;
			boolean boxFollows = false;
			for (int i = glueIndex + 1; i < elements.size(); i++) {
				ListElement e = elements.get(i);
				if (e instanceof KnuthElement && ((KnuthElement) e).isBox()
						&& !(((KnuthElement) e).isAuxiliary() && ((KnuthElement) e).getWidth() == 0)) {
					boxFollows = true;
					break;
				}
				if (e instanceof KnuthPenalty && ((KnuthPenalty) e).getPenalty() < KnuthElement.INFINITE) {
					target = i;
					break;
				}
			}
			if (target >= glueIndex + 1 && forcedBreak(elements.get(target))) {
				/* A forced break - a w:br type="page" or type="column", or a section
				 * break - discards glue at the start of the page or column it opens, so
				 * moving the leading behind it and dropping it come to the same thing on
				 * the page.  They are not the same to FOP: where the forced break ends
				 * the element list (which is how a break between two blocks reaches the
				 * flow), a glue after it makes ElementListUtils.endsWithForcedBreak false,
				 * AbstractBreaker does not end the block sequence there, and the break is
				 * not taken at all.  Measured on a two-column corpus document whose
				 * w:br w:type="column" was ignored: Word's column 2 opens with "Epsum
				 * factorial" at x=315.4, ours carried on in column 1 at x=72.0 and spilled
				 * Word's one page onto two.  @since 17.1.0 */
				it.remove();
			} else if (target > glueIndex + 1 || target == glueIndex + 1) {
				elements.remove(glueIndex);
				elements.add(target, el); // target shifted down by one: now right after the penalty
				it = elements.listIterator(target + 1);
			} else if (target < 0 && !boxFollows) {
				// the flow's last line: its leading would count towards the page before the
				// forced break the page breaker appends, where Word drops it (a page whose
				// text fits is not broken for the leading below its last line)
				it.remove();
			}
		}
	}

	/** Whether this element is a break which is always taken. */
	private static boolean forcedBreak(ListElement e) {
		return e instanceof KnuthPenalty
				&& ((KnuthPenalty) e).getPenalty() == -KnuthElement.INFINITE;
	}
}
