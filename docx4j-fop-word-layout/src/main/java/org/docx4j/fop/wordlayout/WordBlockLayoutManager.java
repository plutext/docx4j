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

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.BlockLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;

/**
 * FOP's BlockLayoutManager with {@link WordLineLayoutManager} as its line
 * manager.  BlockLayoutManager creates its LineLayoutManager in a private
 * method, so the public {@code createNextChildLMs} is overridden with the same
 * logic; the line metrics it needs (lead, follow, line-height) are private
 * too, so they are recomputed here exactly as BlockLayoutManager.initialize
 * does.
 */
public class WordBlockLayoutManager extends BlockLayoutManager {

	private int lead = 12000;
	private int follow = 2000;
	private Length lineHeight;

	public WordBlockLayoutManager(Block inBlock) {
		super(inBlock);
	}

	@Override
	public void initialize() {
		super.initialize();
		Block fo = getBlockFO();
		FontInfo fi = fo.getFOEventHandler().getFontInfo();
		FontTriplet[] fontkeys = fo.getCommonFont().getFontState(fi);
		Font initFont = fi.getFontInstance(fontkeys[0], fo.getCommonFont().fontSize.getValue(this));
		lead = initFont.getAscender();
		follow = -initFont.getDescender();
		lineHeight = fo.getLineHeight().getOptimum(this).getLength();
	}

	@Override
	public boolean createNextChildLMs(int pos) {
		while (proxyLMiter.hasNext()) {
			LayoutManager lm = proxyLMiter.next();
			if (lm instanceof InlineLevelLayoutManager) {
				addChildLM(createWordLineManager(lm));
			} else {
				addChildLM(lm);
			}
			if (pos < childLMs.size()) {
				return true;
			}
		}
		return false;
	}

	private LineLayoutManager createWordLineManager(LayoutManager firstlm) {
		WordLineLayoutManager llm = new WordLineLayoutManager(getBlockFO(), lineHeight, lead, follow);
		List<LayoutManager> inlines = new ArrayList<LayoutManager>();
		inlines.add(firstlm);
		while (proxyLMiter.hasNext()) {
			LayoutManager lm = proxyLMiter.next();
			if (lm instanceof InlineLevelLayoutManager) {
				inlines.add(lm);
			} else {
				proxyLMiter.previous();
				break;
			}
		}
		llm.addChildLMs(inlines);
		return llm;
	}
}
