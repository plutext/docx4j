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
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.SdtElement;

/**
 * How {@link ManualRedline} converts (CR-068 section 3.1): the author and date its tracked changes
 * carry, whether a separating space goes with a change (D13), and the opt-ins of CR-068 section 3.3,
 * each off by default: strike from a style, underline and named colours as insertions, the comments
 * story, a range, the audit property.
 *
 * <pre>
 * ManualRedline.toTrackedChanges(pkg, new ManualRedlineOptions().author("Legal review")
 *         .underlineAsInsertion(true).colourAsInsertion("FF0000").withinBookmark("Schedule2"));
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
	private boolean styleStrike;
	private boolean underlineAsInsertion;
	private Set<String> colours = Collections.emptySet();
	private boolean comments;
	private SdtElement within;
	private String withinBookmark;
	private Set<Part> parts;
	private boolean auditProperty;

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
	 * as Word's own deletion of a word does. An insertion (the opt-ins) takes its space alike, so that
	 * Reject All leaves one; the one space between a deletion and the insertion that replaces it is
	 * written both inserted and deleted, so that it goes in either outcome.
	 */
	public ManualRedlineOptions separatorSpace(boolean separatorSpace) {
		this.separatorSpace = separatorSpace;
		return this;
	}

	/**
	 * Strike that comes from a style only converts too (D5's opt-in): a run with no strike of its own
	 * that its character or paragraph style strikes. The converted run is given its own
	 * {@code w:strike w:val="0"} (or {@code w:dstrike}), so that Reject All shows it plain (D6).
	 */
	public ManualRedlineOptions styleStrike(boolean styleStrike) {
		this.styleStrike = styleStrike;
		return this;
	}

	/**
	 * A run underlined by its own {@code w:u} (a pattern other than {@code none}; a {@code w:u} with no
	 * pattern inherits, and is not taken) becomes a tracked insertion, its underline removed (D7). A
	 * run both struck and underlined is a deletion: the strike wins, and the report counts it.
	 */
	public ManualRedlineOptions underlineAsInsertion(boolean underlineAsInsertion) {
		this.underlineAsInsertion = underlineAsInsertion;
		return this;
	}

	/**
	 * A run whose own {@code w:color} is one of these (hex {@code RRGGBB}; not {@code auto}, not a
	 * theme colour) and which carries no other mark of its own (no strike, underline, highlight or
	 * shading) becomes a tracked insertion, its colour removed (D7). No colour by default: red text is
	 * also just red text, so the caller names it.
	 */
	public ManualRedlineOptions colourAsInsertion(String... hex) {
		Set<String> set = new LinkedHashSet<String>();
		for (String h : hex) {
			if (h == null || !h.matches("[0-9A-Fa-f]{6}")) throw new IllegalArgumentException("Not an RRGGBB colour: " + h);
			set.add(h.toUpperCase(Locale.ROOT));
		}
		this.colours = Collections.unmodifiableSet(set);
		return this;
	}

	/**
	 * The comments story converts too (D2's opt-in). In a comment the converted run keeps its
	 * strike, underline or colour (D14). Word 365's comment card shows no tracked change (CR-068
	 * section 12.7): a deleted or inserted run without formatting it shows as plain text, so that a
	 * withdrawn remark would look kept; one that keeps its strike or underline it does not draw, but
	 * shows a "Missing content" notice pointing to the Revisions pane, which shows the run struck or
	 * underlined. Accept All and Reject All resolve either form as Word does.
	 */
	public ManualRedlineOptions comments(boolean comments) {
		this.comments = comments;
		return this;
	}

	/** Only what is inside this content control's content (the control as it stands in the package), as {@link TrackedChangeFilter#within}. */
	public ManualRedlineOptions within(SdtElement control) {
		this.within = control;
		this.withinBookmark = null;
		return this;
	}

	/** Only what is between the start and the end of the bookmark of this name, as {@link TrackedChangeFilter#withinBookmark}. */
	public ManualRedlineOptions withinBookmark(String name) {
		this.withinBookmark = name;
		this.within = null;
		return this;
	}

	/** Only these story parts (the main document part, a header, the footnotes, the comments, ...). */
	public ManualRedlineOptions parts(Part... parts) {
		Set<Part> set = Collections.newSetFromMap(new IdentityHashMap<Part, Boolean>());
		set.addAll(Arrays.asList(parts));
		this.parts = set;
		return this;
	}

	/**
	 * A custom document property {@code docx4j:manualRedline}, its value the date and these options,
	 * records the conversion (D11; off by default: the author name is the record a reviewer sees).
	 */
	public ManualRedlineOptions auditProperty(boolean auditProperty) {
		this.auditProperty = auditProperty;
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

	public boolean isStyleStrike() {
		return styleStrike;
	}

	public boolean isUnderlineAsInsertion() {
		return underlineAsInsertion;
	}

	/** The colours named, upper case; empty for none. */
	public Set<String> getColours() {
		return colours;
	}

	public boolean isComments() {
		return comments;
	}

	public SdtElement getWithin() {
		return within;
	}

	public String getWithinBookmark() {
		return withinBookmark;
	}

	/** The parts named; null for every story. */
	public Set<Part> getParts() {
		return parts;
	}

	public boolean isAuditProperty() {
		return auditProperty;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("author=" + author + ", date=" + (dateSet ? String.valueOf(date) : "now")
				+ ", separatorSpace=" + separatorSpace);
		if (styleStrike) sb.append(", styleStrike=true");
		if (underlineAsInsertion) sb.append(", underlineAsInsertion=true");
		if (!colours.isEmpty()) sb.append(", colourAsInsertion=").append(String.join(" ", colours));
		if (comments) sb.append(", comments=true");
		if (within != null) sb.append(", within=content control");
		if (withinBookmark != null) sb.append(", withinBookmark=").append(withinBookmark);
		if (parts != null) {
			sb.append(", parts=");
			boolean first = true;
			for (Part p : parts) {
				if (!first) sb.append(' ');
				first = false;
				sb.append(p.getPartName().getName());
			}
		}
		if (auditProperty) sb.append(", auditProperty=true");
		return sb.toString();
	}
}
