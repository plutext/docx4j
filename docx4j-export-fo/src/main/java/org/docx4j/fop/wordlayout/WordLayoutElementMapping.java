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
 * and one on {@code fo:root}:
 * <ul>
 * <li>{@code docx4j:space-shrink}: how far the spaces of a justified line may be
 * compressed to pull a word in (see {@link #SPACE_SHRINK});</li>
 * <li>{@code docx4j:hyphenation-zone}, {@code docx4j:hyphen-limit} and
 * {@code docx4j:hyphenate-caps}: the document's hyphenation settings (see
 * {@link #HYPHENATION_ZONE}).</li>
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
	/** on fo:root: how far this document's Word engine may compress the spaces of a
	 *  justified line to pull a word in, as a fraction of their natural width.  Word
	 *  only does that from compatibility mode 15 (the Word 2013 layout engine); for
	 *  an older document docx4j writes 0.  @since 17.0.5 */
	public static final String SPACE_SHRINK = "space-shrink";

	/** on fo:root: w:hyphenationZone in twips - the largest gap this document's Word
	 *  engine tolerates at the end of a line before it hyphenates the next word.
	 *  @since 17.0.6 */
	public static final String HYPHENATION_ZONE = "hyphenation-zone";
	/** on fo:root: w:consecutiveHyphenLimit - how many lines in a row may end in a
	 *  hyphen.  Absent, or 0, means no limit.  @since 17.0.6 */
	public static final String HYPHEN_LIMIT = "hyphen-limit";
	/** on fo:root: "false" for w:doNotHyphenateCaps - a word in all capitals is not
	 *  hyphenated.  @since 17.0.6 */
	public static final String HYPHENATE_CAPS = "hyphenate-caps";

	/** on an fo:leader: it stands in for a w:tab, and {@link WordLineLayoutManager}
	 *  gives it the width from the x it starts at to the tab stop it reaches. */
	public static final String TAB = "tab";
	/** on a paragraph's fo:block: its custom tab stops, "pos:align:leader;..." in
	 *  twips from the left margin (w:val="clear" stops included: they clear the
	 *  default stops before them). */
	public static final String TABS = "tabs";
	/** with {@link #TABS}: the default tab interval in twips (w:defaultTabStop). */
	public static final String TAB_DEFAULT = "tab-default";
	/** with {@link #TABS}: "left:firstLineOffset" in twips, the paragraph's indents,
	 *  which put the stops and the running x in one frame. */
	public static final String TAB_IND = "tab-ind";

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
