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
 * A paragraph's numbering, as its effective {@code w:numPr} and the numbering definitions give it:
 * what Markdown's list markers are made from (WD REQ-510, REQ-703). The label itself, which the
 * numbering emulator computes, is {@link TextBlock#getListLabel()}, under {@code list_labels: true}.
 *
 * @since 17.3.4 (CR-046 phase 3)
 */
public final class TextNumbering {

	private final String numId;
	private final int level;
	private final String format;
	private final String levelText;
	private final int start;
	/** Set by the extractor once the numbering emulator has counted the paragraph. */
	Integer value;
	/** Set by the extractor from the numbering definitions. */
	String abstractNumId;

	TextNumbering(String numId, int level, String format, String levelText, int start) {
		this.numId = numId;
		this.level = level;
		this.format = format;
		this.levelText = levelText;
		this.start = start;
	}

	/** The {@code w:numId}. */
	public String getNumId() { return numId; }

	/** The level, {@code w:ilvl}: 0 to 8. */
	public int getLevel() { return level; }

	/** The level's {@code w:numFmt} value ({@code decimal}, {@code bullet}, {@code none}, ...), or null where the definitions don't give one. */
	public String getFormat() { return format; }

	/** The level's {@code w:lvlText} value ({@code %1.}, a bullet character, or a label such as {@code NOTE}), or null. */
	public String getLevelText() { return levelText; }

	/** The level's {@code w:start}: 1 where it has none. */
	public int getStart() { return start; }

	/**
	 * The count the paragraph's level shows, as the list labels count it: the 3 of "1.3."
	 * (a start override, a w:lvlRestart, a paragraph Word does not count, all applied). Null
	 * where the definitions don't let it be counted.
	 *
	 * @since 17.3.4
	 */
	public Integer getValue() { return value; }

	/**
	 * The list the paragraph belongs to: its {@code w:abstractNum}'s id. Two {@code w:num} on one
	 * {@code w:abstractNum} are one list, counting on from each other (CR-014 P1); a {@code w:numStyleLink}
	 * definition is a list of its own (P2). Null where the definitions don't give one.
	 *
	 * @since 17.3.4
	 */
	public String getAbstractNumId() { return abstractNumId; }

	/** True for a bullet level. */
	public boolean isBullet() { return "bullet".equals(format); }

	/**
	 * True for a level that is a label rather than a number: not a bullet, and its format is
	 * {@code none} or its text has no {@code %n} placeholder ({@code NOTE}, {@code EXAMPLE}).
	 */
	public boolean isLabel() {
		if (isBullet() || format == null && levelText == null) return false; // a level the definitions don't give is a list's
		return "none".equals(format) || levelText == null || levelText.indexOf('%') < 0;
	}

	@Override
	public String toString() {
		return numId + "/" + level + " " + format + " '" + levelText + "' from " + start + (value == null ? "" : " = " + value);
	}
}
