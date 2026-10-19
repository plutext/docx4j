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
package org.docx4j.model.text;

/**
 * Whether a person sees an inspection finding's content (WD REQ-901, CR-050 D15), under the display
 * profile: Word 365 opening the file at its installation defaults (Print Layout; revisions as Simple
 * Markup, deleted text not shown; comments as margin markers; hidden text and field codes not shown;
 * collapsed headings collapsed).
 *
 * @since 17.3.4 (CR-050 phase 1)
 */
public enum Visible {

	/** Seen, as stored. */
	YES("yes"),
	/** Not seen. */
	NO("no"),
	/** There, but not as its characters say: text in its background's colour, 1pt text, a glyph drawn as another. */
	DIFFERENTLY("differently"),
	/** The document or a user option decides what the profile shows, and the file cannot settle it. */
	INDETERMINATE("indeterminate"),
	/** Not content a person reads (a macro, an external relationship, an embedded face): {@code -} in the WD. */
	NOT_CONTENT("-");

	private final String id;

	Visible(String id) {
		this.id = id;
	}

	/** The WD's identifier: {@code yes}, {@code no}, {@code differently}, {@code indeterminate} or {@code -}. */
	public String id() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}
}
