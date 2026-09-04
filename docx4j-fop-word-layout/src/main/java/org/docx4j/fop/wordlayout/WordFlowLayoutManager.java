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
 * @since 17.0.5
 */
public class WordFlowLayoutManager extends FlowLayoutManager {

	public WordFlowLayoutManager(PageSequenceLayoutManager pslm, Flow node) {
		super(pslm, node);
	}

	@Override
	public List<ListElement> getNextKnuthElements(LayoutContext context, int alignment) {
		List<ListElement> elements = super.getNextKnuthElements(context, alignment);
		moveLeadingBehindBreaks(elements);
		return elements;
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
			for (int i = glueIndex + 1; i < elements.size(); i++) {
				ListElement e = elements.get(i);
				if (e instanceof KnuthElement && ((KnuthElement) e).isBox()
						&& !(((KnuthElement) e).isAuxiliary() && ((KnuthElement) e).getWidth() == 0)) break;
				if (e instanceof KnuthPenalty && ((KnuthPenalty) e).getPenalty() < KnuthElement.INFINITE) {
					target = i;
					break;
				}
			}
			if (target > glueIndex + 1 || target == glueIndex + 1) {
				elements.remove(glueIndex);
				elements.add(target, el); // target shifted down by one: now right after the penalty
				it = elements.listIterator(target + 1);
			}
		}
	}
}
