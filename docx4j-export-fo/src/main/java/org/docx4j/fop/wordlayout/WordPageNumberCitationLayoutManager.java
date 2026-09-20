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

import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.layoutmgr.inline.PageNumberCitationLayoutManager;
import org.apache.fop.traits.MinOptMax;

/**
 * An {@code fo:page-number-citation} whose page is not yet known is measured as a page
 * number rather than as three capital Ms.
 *
 * <p>FOP cannot know the page a citation points forward to - every entry of a table of
 * contents cites a page that has not been laid out yet - so it measures the citation as
 * the placeholder {@code "MMM"}
 * ({@code AbstractPageNumberCitationLayoutManager.determineCitationString}) and corrects
 * the width when the reference resolves ({@code UnresolvedPageNumber.resolveIDRef}, and
 * {@code WordLineLayoutManager.TabPageNumberWidth} for the tab that aligns the number on
 * its stop).  The correction comes too late for the line break, which was decided against
 * the placeholder: {@code "MMM"} is 32.0pt in Times 12pt where {@code "23"} is 12.0pt and
 * {@code "133"} 18.0pt, so a table-of-contents entry gives up as much as 20pt of its
 * measure to a number that will not use it.
 *
 * <p><b>Measured.</b>  On the shape of a corpus document's 222 pages of {@code toc 2}
 * entries - a number, a tab to the hanging indent, a title ending in a long token, then a
 * right dot-leader stop at the margin and the page number - the entry's token has to be
 * three characters shorter before it fits the first line with FOP's placeholder than with
 * the number itself, and the digits give two of those three characters back
 * ({@code PageNumberCitationWidthTest}; the third is the 6pt by which {@code "000"} is
 * wider than a two-digit number).  In that document Word sets the whole entry on one line
 * and docx4j moved the token down, which put every following page one out.  The leader's
 * own {@code leader-length.minimum} makes no difference to that break at all: measured at
 * 12pt (what {@code XsltFOFunctions.tabToFO} writes) and at 0pt, the token moves down at
 * exactly the same token length.
 *
 * <p>So the placeholder keeps FOP's three characters and makes them digits
 * ({@link WordLayoutCustomizer#PAGE_NUMBER_PLACEHOLDER}, default {@code "000"}), and the
 * wider of the two is never taken: this can only narrow what a citation reserves, never
 * widen it.  Where the citation <em>is</em> resolved FOP has measured the real number and
 * that width stands.
 *
 * <p>{@code fo:page-number-citation-last} (docx4j writes one for {@code NUMPAGES}) has
 * the same placeholder and is deliberately left alone here: the corpus evidence is the
 * table-of-contents shape.
 *
 * <p>A workaround for FOP, which offers no way to hint the width of an unresolved
 * citation; it goes if FOP grows one.
 *
 * @since 17.2.0
 */
public class WordPageNumberCitationLayoutManager extends PageNumberCitationLayoutManager {

	public WordPageNumberCitationLayoutManager(PageNumberCitation node) {
		super(node);
	}

	/**
	 * FOP's allocation, except that an unresolved citation is measured as a page number.
	 *
	 * <p>{@code super} is always called: it is what settles FOP's own citation string,
	 * which the area this manager builds is then made from.
	 */
	@Override
	protected MinOptMax getAllocationIPD(int refIPD) {
		MinOptMax fops = super.getAllocationIPD(refIPD);
		if (getCitedPage() != null) return fops;    // FOP measured the number itself
		String placeholder = WordLayoutCustomizer.pageNumberPlaceholder("000");
		if (placeholder == null || placeholder.length() == 0 || font == null) return fops;
		int width = 0;
		for (int i = 0; i < placeholder.length(); i++) {
			width += font.getCharWidth(placeholder.charAt(i));
		}
		return width < fops.getOpt() ? MinOptMax.getInstance(width) : fops;
	}
}
