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
package org.docx4j.model.listnumbering;

import java.util.IdentityHashMap;
import java.util.Map;

import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * The numbering states of one traversal, one per <em>story</em>: Word numbers
 * each story from its own counters.  Measured on CR-014 probe P7 (2026-09-12):
 * the body is one story; a section's header and footer share one (header 1 2 3,
 * footer 4 5 6); the footnotes part is one (the second footnote continues the
 * first's count); the endnotes part another; each text box and each comment
 * counts on its own; and the body's count runs past all of them untouched.
 *
 * <p>Only one section was probed, so headers and footers of every section are
 * folded into one story here until a document says otherwise.
 *
 * @since 17.1.1 (CR-014 phase 4)
 */
public final class NumberingStates {

	private final NumberingState main = new NumberingState();
	private NumberingState headersFooters;
	private final Map<Part, NumberingState> byPart = new IdentityHashMap<Part, NumberingState>();

	/** The main document's story. */
	public NumberingState main() {
		return main;
	}

	/**
	 * The story the given part's paragraphs number in: the main document (also
	 * for null, and for any part not listed here), the one shared by headers and
	 * footers, or the footnotes, endnotes or comments part's own.
	 */
	public NumberingState forPart(Part part) {
		if (part == null || part instanceof MainDocumentPart) return main;
		if (part instanceof HeaderPart || part instanceof FooterPart) {
			if (headersFooters == null) headersFooters = new NumberingState();
			return headersFooters;
		}
		if (part instanceof FootnotesPart || part instanceof EndnotesPart || part instanceof CommentsPart) {
			NumberingState s = byPart.get(part);
			if (s == null) {
				s = new NumberingState();
				byPart.put(part, s);
			}
			return s;
		}
		return main;
	}

	/** A fresh story: a text box's, or a comment's when comments are numbered one
	 *  by one. */
	public NumberingState newStory() {
		return new NumberingState();
	}
}
