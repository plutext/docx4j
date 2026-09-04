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

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.layoutmgr.FlowLayoutManager;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LayoutManagerMapping;

/**
 * The {@code LayoutManagerMaker} docx4j gives FOP
 * ({@code FopFactoryBuilder.setLayoutManagerMakerOverride}): FOP's own
 * {@link LayoutManagerMapping} with {@link WordBlockLayoutManager} for
 * {@code fo:block}.
 *
 * It also supplies {@link WordFlowLayoutManager} for the flow.
 *
 * FOP holds the override at factory level and never tells it the
 * {@code FOUserAgent}, which the standard makers need (TextLayoutManager asks
 * it about accessibility).  So this object is only a front: the real mapping
 * is created per user agent, from the node being laid out, the first time it
 * is seen.
 */
public class WordLayoutManagerMaker extends LayoutManagerMapping {

	private FOUserAgent currentAgent;
	private LayoutManagerMapping delegate;

	public WordLayoutManagerMaker() {
		super(null);
	}

	private synchronized LayoutManagerMapping delegate(FOUserAgent agent) {
		if (delegate == null || agent != currentAgent) {
			currentAgent = agent;
			delegate = new Mapping(agent);
		}
		return delegate;
	}

	@Override
	public void makeLayoutManagers(FONode node, List lms) {
		delegate(node.getUserAgent()).makeLayoutManagers(node, lms);
	}

	@Override
	public LayoutManager makeLayoutManager(FONode node) {
		return delegate(node.getUserAgent()).makeLayoutManager(node);
	}

	@Override
	public FlowLayoutManager makeFlowLayoutManager(PageSequenceLayoutManager pslm, Flow flow) {
		return new WordFlowLayoutManager(pslm, flow);
	}

	/** The per-agent mapping: FOP's, with the block maker replaced. */
	static class Mapping extends LayoutManagerMapping {

		Mapping(FOUserAgent agent) {
			super(agent);
		}

		@Override
		public FlowLayoutManager makeFlowLayoutManager(PageSequenceLayoutManager pslm, Flow flow) {
			return new WordFlowLayoutManager(pslm, flow);
		}

		@Override
		protected void initialize() {
			super.initialize();
			registerMaker(Block.class, new Maker() {
				@Override
				public void make(FONode node, List lms, FOUserAgent userAgent) {
					lms.add(new WordBlockLayoutManager((Block) node));
				}
			});
		}
	}
}
