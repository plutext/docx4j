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
package org.docx4j.model.revisions;

import java.time.Instant;

/**
 * How {@link ManualRedline} converts (CR-068 section 3.1): the author and date its tracked changes
 * carry, and whether a separating space goes with a deletion (D13). The opt-ins of CR-068 section
 * 3.3 (style strike, underline and colour as insertions, comments, a range, the audit property) come
 * with its phase 2.
 *
 * <pre>
 * ManualRedline.toTrackedChanges(pkg, new ManualRedlineOptions().author("Legal review").date(Instant.parse("2026-11-01T09:00:00Z")));
 * </pre>
 *
 * @since 17.3.5
 */
public final class ManualRedlineOptions {

	/** The author by default (D3): what a reviewer sees in Word's Review pane, and the record of the conversion (D11). */
	public static final String DEFAULT_AUTHOR = "manual redline";

	private String author = DEFAULT_AUTHOR;
	private Instant date;
	private boolean dateSet;
	private boolean separatorSpace = true;

	/** The {@code w:author} of every change written; not null or empty. */
	public ManualRedlineOptions author(String author) {
		if (author == null || author.isEmpty()) throw new IllegalArgumentException("An author is needed");
		this.author = author;
		return this;
	}

	/**
	 * The instant every change written carries (D4), to the second; by default the operation's own
	 * time. {@code null} writes no date. Written as Word writes it (CR-068 section 8.5): {@code w:date}
	 * the instant's wall-clock time in the package's zone ({@code OpcPackage.effectiveDateTimeZone()})
	 * with a {@code Z}, which is the one Word shows, and {@code w16du:dateUtc} the instant.
	 */
	public ManualRedlineOptions date(Instant date) {
		this.date = date;
		this.dateSet = true;
		return this;
	}

	/**
	 * Whether a space that separated the struck text from its neighbours goes with the deletion (D13,
	 * on by default): where the struck text has whitespace (or the paragraph's start) before it and a
	 * space after it, that space; where it ends the paragraph or meets punctuation, the space before
	 * it. Accept All then leaves one space where the author's hand-made redline had two words apart,
	 * as Word's own deletion of a word does.
	 */
	public ManualRedlineOptions separatorSpace(boolean separatorSpace) {
		this.separatorSpace = separatorSpace;
		return this;
	}

	public String getAuthor() {
		return author;
	}

	/** The date set by {@link #date(Instant)}; null where none was set (the operation's time) or none is wanted. */
	public Instant getDate() {
		return date;
	}

	/** Whether {@link #date(Instant)} was called; if not, the operation's time is written. */
	public boolean isDateSet() {
		return dateSet;
	}

	public boolean isSeparatorSpace() {
		return separatorSpace;
	}

	@Override
	public String toString() {
		return "author=" + author + ", date=" + (dateSet ? String.valueOf(date) : "now") + ", separatorSpace=" + separatorSpace;
	}
}
