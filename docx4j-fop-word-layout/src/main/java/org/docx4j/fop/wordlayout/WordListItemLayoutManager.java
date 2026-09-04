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

import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;
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

	private static final Field BODY_LIST;
	static {
		try {
			BODY_LIST = ListItemLayoutManager.class.getDeclaredField("bodyList");
			BODY_LIST.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's ListItemLayoutManager has changed; docx4j-fop-word-layout needs updating", e);
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
