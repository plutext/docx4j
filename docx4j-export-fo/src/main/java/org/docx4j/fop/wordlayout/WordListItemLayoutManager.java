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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Stack;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;

/**
 * FOP's ListItemLayoutManager, with the last line's leading left discardable.
 *
 * The list item manager combines the label's and the body's element lists
 * into boxes of its own, one per step, so the {@link LeadingGlue} the body's
 * last line ends with becomes part of the last box: at the foot of a page the
 * item would then need its leading to fit, where Word drops it (a bulleted
 * paragraph fits the page when its text does; measured, CR-001 §6.10).  The
 * body's trailing leading is taken out of the last combined box and put back
 * after it as glue, which {@link WordFlowLayoutManager} then moves behind
 * the break possibility that follows, as for any block.
 *
 * @since 17.0.5
 */
public class WordListItemLayoutManager extends ListItemLayoutManager {

	private static final org.slf4j.Logger log
			= org.slf4j.LoggerFactory.getLogger(WordListItemLayoutManager.class);

	private static final Field BODY_LIST;
	static {
		try {
			BODY_LIST = ListItemLayoutManager.class.getDeclaredField("bodyList");
			BODY_LIST.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's ListItemLayoutManager has changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	public WordListItemLayoutManager(ListItem node) {
		super(node);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack,
			Position restartPosition, LayoutManager restartAtLM) {
		List<ListElement> result = super.getNextKnuthElements(context, alignment, lmStack, restartPosition, restartAtLM);
		exposeTrailingLeading(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private void exposeTrailingLeading(List<ListElement> result) {
		if (result == null || result.isEmpty()) return;
		List<ListElement> bodyList;
		try {
			bodyList = (List<ListElement>) BODY_LIST.get(this);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		int leading = trailingLeading(bodyList);
		if (leading <= 0) return;
		for (int i = result.size() - 1; i >= 0; i--) {
			ListElement el = result.get(i);
			if (!(el instanceof KnuthBox)) continue;
			KnuthBox box = (KnuthBox) el;
			if (box.getWidth() < leading) return;
			result.set(i, new KnuthBox(box.getWidth() - leading, box.getPosition(), box.isAuxiliary()));
			// as WordLineLayoutManager ends a block: no break between box and glue,
			// and a box after the glue so the list does not end in glue
			result.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, true));
			result.add(new LeadingGlue(leading));
			result.add(new KnuthBox(0, null, true));
			return;
		}
	}

	// ---- the w:suff separator in the text layer ------------------------------------

	/** the label's and the body's block areas of the fragment being added, in the order
	 *  {@link ListItemLayoutManager#addAreas} adds them */
	private Block labelArea, bodyArea;

	/**
	 * {@inheritDoc}
	 *
	 * <p>Word writes the {@code w:suff} separator as one space glyph, in the paragraph
	 * mark's font, in a text object of its own ({@code 1 0 0 1 134.45 723.07 Tm [( )] TJ}
	 * between a corpus heading's number and its text).  Here the label and the text are two
	 * blocks of an {@code fo:list-item} and nothing was written between them, so a numbered
	 * paragraph's label fused with the word after it.  The
	 * {@code styles-numpr-ilvl-only} golden probe extracted as
	 * {@code 1.1.(b) L, direct w:numPr of w:ilvl 1 only} where Word's golden reads
	 * {@code 1.1. (b) L, ...}, and 847 lines over the three real-document corpora differ
	 * from Word's by that one space (CR-001 batch 45).
	 *
	 * <p>The space is added to the label's line once both areas exist, with the measured
	 * gap as its width, so it is the text layer alone that changes: the body block is
	 * placed from {@code body-start()} and never from the label's width, and a line area
	 * is rendered by walking its children, so no glyph moves.</p>
	 */
	@Override
	public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
		labelArea = null;
		bodyArea = null;
		super.addAreas(parentIter, layoutContext);
		try {
			if (WordLayoutCustomizer.labelSuffixSpace()) addLabelSuffixSpace();
		} catch (RuntimeException e) {
			// the text layer is not worth failing a render for
			log.warn("list label suffix space: " + e.getMessage(), e);
		}
		labelArea = null;
		bodyArea = null;
	}

	@Override
	public void addChildArea(Area childArea) {
		super.addChildArea(childArea);
		if (!(childArea instanceof Block)) return;
		if (labelArea == null) {
			labelArea = (Block) childArea;
		} else if (bodyArea == null) {
			bodyArea = (Block) childArea;
		}
	}

	/** The space glyph Word writes for the level's {@code w:suff}, appended to the
	 *  label's first line with the width of the gap to the text. */
	private void addLabelSuffixSpace() {

		if (labelArea == null || bodyArea == null) return;   // a body-only fragment
		if (labelSuffix() == null) return;                   // "nothing", or not ours

		LineAt label = firstLine(labelArea, 0);
		LineAt body = firstLine(bodyArea, 0);
		if (label == null || body == null) return;
		// right to left is Word's own arrangement and is not measured here
		if (label.line.getBidiLevel() > 0 || body.line.getBidiLevel() > 0) return;
		/* Word writes no space where the numbering tab's leader filled the gap: the
		 * tab-leader-in-cell-2 golden's dots end at 149.76 and its text begins at 149.83.
		 * The test is whether the leader painted a character, not whether the FO asked for
		 * one: over the first probe's 2pt advance - under one cell of Word's grid - neither
		 * side paints a dot and Word's separator space is still there (measured, that probe
		 * falls 1.0000 -> 0.9231 without this distinction, its label fusing with the word
		 * after it as it did before batch 45).  @since 17.1.1 (CR-001 batch 47 item 5b) */
		if (leaderPaints(label.line)) return;

		int content = 0;
		TextArea model = null;
		for (Object o : label.line.getInlineAreas()) {
			if (!(o instanceof InlineArea)) continue;
			InlineArea ia = (InlineArea) o;
			content += ia.getAllocIPD();
			TextArea t = lastTextArea(ia);
			if (t != null) model = t;
		}
		if (model == null) return;                           // a picture bullet, or empty

		int gap = body.x - (label.x + content);
		if (gap <= 0) return;                                // the label fills the column

		TextArea space = space(model, gap);
		if (space != null) label.line.addChildArea(space);
	}

	/** Whether a leader on this line paints at least one character: an
	 *  {@code fo:leader} narrower than its own repeating unit draws nothing, which is
	 *  what Word does over an advance shorter than one cell of its grid.
	 *  @since 17.1.1 (CR-001 batch 47 item 5b) */
	private static boolean leaderPaints(LineArea line) {
		for (Object o : line.getInlineAreas()) {
			if (o instanceof InlineArea && leaderPaints((InlineArea) o)) return true;
		}
		return false;
	}

	private static boolean leaderPaints(InlineArea area) {
		InlineArea a = LBP.phasedLeader(area);
		if (a instanceof org.apache.fop.area.inline.FilledArea) {
			org.apache.fop.area.inline.FilledArea run = (org.apache.fop.area.inline.FilledArea) a;
			return run.getUnitWidth() > 0 && run.getIPD() >= run.getUnitWidth();
		}
		if (a instanceof InlineParent) {
			for (Object child : ((InlineParent) a).getChildAreas()) {
				if (child instanceof InlineArea && leaderPaints((InlineArea) child)) return true;
			}
		}
		return false;
	}

	/** The {@code w:suff} the level asks for, from the label block's
	 *  {@code docx4j:label-suffix}, or null where none was written. */
	private String labelSuffix() {
		ListItem item = getListItemFO();
		if (item == null || item.getLabel() == null) return null;
		for (FONode.FONodeIterator it = item.getLabel().getChildNodes(); it != null && it.hasNext(); ) {
			FONode child = it.next();
			if (!(child instanceof FObj)) continue;
			String suffix = WordLineLayoutManager.foreignAttribute(
					(FObj) child, WordLayoutElementMapping.LABEL_SUFFIX);
			if (suffix != null && suffix.length() > 0) return suffix;
		}
		return null;
	}

	/** A text area of one space character, drawn in the label's font and given the gap's
	 *  width by a character-spacing adjustment (PDF {@code Tc}), so that what the text
	 *  layer reports for it is the separator Word wrote. */
	private TextArea space(TextArea model, int gap) {

		FontTriplet triplet = (FontTriplet) model.getTrait(Trait.FONT);
		Object size = model.getTrait(Trait.FONT_SIZE);
		if (triplet == null || !(size instanceof Integer)) return null;
		FontInfo fontInfo = getFObj() == null || getFObj().getFOEventHandler() == null
				? null : getFObj().getFOEventHandler().getFontInfo();
		if (fontInfo == null) return null;
		Font font = fontInfo.getFontInstance(triplet, ((Integer) size).intValue());
		if (font == null || !font.hasChar(' ')) return null;

		TextArea space = new TextArea();
		space.addTrait(Trait.FONT, triplet);
		space.addTrait(Trait.FONT_SIZE, size);
		Object colour = model.getTrait(Trait.COLOR);
		if (colour != null) space.addTrait(Trait.COLOR, colour);
		// the label's own structure element, so that a tagged PDF has somewhere to put it
		Object struct = model.getTrait(Trait.STRUCTURE_TREE_ELEMENT);
		if (struct != null) space.addTrait(Trait.STRUCTURE_TREE_ELEMENT, struct);
		space.setIPD(gap);
		space.setBPD(model.getBPD());
		space.setBlockProgressionOffset(model.getBlockProgressionOffset());
		space.setBaselineOffset(model.getBaselineOffset());
		if (model.getBidiLevel() >= 0) space.setBidiLevel(model.getBidiLevel());
		// the glyph is blank, so the adjustment moves nothing: it makes the space's own
		// quad the separator's advance, which is what Word's PDF reports for it
		space.setTextLetterSpaceAdjust(gap - font.getCharWidth(' '));
		space.addWord(" ", gap, null, null, null, 0);
		return space;
	}

	/** a line area and the x it is rendered at, both in millipoints from the item's own
	 *  start edge (AbstractRenderer.renderBlocks: a block's own start-indent reaches its
	 *  line children, and a nested block starts again from its container's position) */
	private static final class LineAt {
		private final LineArea line;
		private final int x;
		LineAt(LineArea line, int x) {
			this.line = line;
			this.x = x;
		}
	}

	private static LineAt firstLine(Block block, int x) {
		List<?> children = block.getChildAreas();
		if (children == null) return null;
		int base = x + block.getXOffset();
		for (Object child : children) {
			if (child instanceof LineArea) {
				LineArea line = (LineArea) child;
				return new LineAt(line, base + block.getStartIndent() + line.getStartIndent());
			}
			if (child instanceof Block) {
				LineAt found = firstLine((Block) child, base);
				if (found != null) return found;
			}
		}
		return null;
	}

	/** the last text area under an inline area, whose font and baseline the space takes */
	private static TextArea lastTextArea(InlineArea area) {
		if (area instanceof TextArea) return (TextArea) area;
		if (!(area instanceof InlineParent)) return null;
		TextArea found = null;
		for (Object child : ((InlineParent) area).getChildAreas()) {
			if (!(child instanceof InlineArea)) continue;
			TextArea t = lastTextArea((InlineArea) child);
			if (t != null) found = t;
		}
		return found;
	}

	/** the LeadingGlue a block's element list ends with (after any aux box or penalty), or 0 */
	static int trailingLeading(List<ListElement> elements) {
		if (elements == null) return 0;
		for (int i = elements.size() - 1; i >= 0; i--) {
			ListElement el = elements.get(i);
			if (el instanceof LeadingGlue) return ((LeadingGlue) el).getWidth();
			if (el instanceof KnuthPenalty) continue;
			if (el instanceof KnuthBox && ((KnuthBox) el).isAuxiliary() && ((KnuthBox) el).getWidth() == 0) continue;
			return 0;
		}
		return 0;
	}
}
