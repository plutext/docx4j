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

import java.util.HashMap;

import org.apache.fop.fo.ElementMapping;

/**
 * Registers the docx4j layout namespace with FOP so that its attributes on
 * fo:block are accepted (FOP rejects attributes in unknown namespaces as
 * invalid properties) and kept as the block's foreign attributes, where
 * {@link WordLineLayoutManager} reads them:
 * <ul>
 * <li>{@code docx4j:line-box}: the height of the text box of each line (Word's
 * single-spacing pitch for the paragraph's font: ascent, descent and external
 * leading); the difference between the block's line-height and this is the
 * extra leading Word puts below the text, which is dropped at the bottom of a
 * page;</li>
 * <li>{@code docx4j:baseline}: where Word puts the baseline within that box
 * (ascent plus external leading for "auto" spacing);</li>
 * <li>{@code docx4j:line-rule}: auto, exact or atLeast (w:lineRule), which
 * decides where any difference between the block's line-height and the box
 * goes: below as droppable leading, above, or nowhere (clipped).</li>
 * </ul>
 * No elements are defined.  Found by FOP through META-INF/services
 * (org.apache.fop.fo.ElementMapping); docx4j-export-fo learns the namespace
 * from {@link WordLayoutCustomizer#extensionNamespace()}.
 *
 * @since 17.0.5
 */
public class WordLayoutElementMapping extends ElementMapping {

	public static final String URI = org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer.WORD_LAYOUT_NAMESPACE;

	public static final String LINE_BOX = "line-box";
	public static final String BASELINE = "baseline";
	/** auto | exact | atLeast: how the block's line-height relates to the box. */
	public static final String LINE_RULE = "line-rule";
	/** on a list item's first block: the label's natural ascent, part of its first line */
	public static final String LABEL_ASCENT = "label-ascent";

	public WordLayoutElementMapping() {
		namespaceURI = URI;
	}

	@Override
	protected void initialize() {
		if (foObjs == null) {
			foObjs = new HashMap<String, Maker>(); // attributes only
		}
	}

	@Override
	public String getStandardPrefix() {
		return "docx4j";
	}
}
