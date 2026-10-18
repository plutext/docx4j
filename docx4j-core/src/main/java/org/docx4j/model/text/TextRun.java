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
 * A piece of a block's content (WD REQ-706): document text, or a typed item whose text, if any, the
 * serialiser adds (a note reference is written {@code [n]} in plain text, REQ-707; it is a field of
 * the run here, never text).
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextRun {

	public enum Kind {
		/** Document text: {@code w:t}, tabs and breaks as REQ-601 maps them, a field's result. */
		TEXT,
		/** A field's instruction, present only under {@code field_codes: true} (REQ-505). */
		FIELD_CODE,
		/** A footnote or endnote reference in the story ({@link #getNoteNumber()}; REQ-503). */
		NOTE_REFERENCE,
		/** The note's own mark at its start ({@code w:footnoteRef}, or added where it has none). */
		NOTE_MARK,
		/** A comment's reference, where the comment is rendered in plain text ({@link #getCommentId()}; REQ-504). */
		COMMENT_REFERENCE,
		/** A list label from the numbering model, under {@code list_labels: true} (REQ-510). */
		LIST_LABEL,
		/** A picture ({@link #getImage()}), with its description where the describer gives one ({@link #getDescription()}; REQ-514). */
		IMAGE,
		/** An embedded object, OLE link or ActiveX control ({@code w:object}; {@link #getProgId()}; REQ-512): no text. */
		OBJECT,
		/** A {@code w:lastRenderedPageBreak}: where the page {@link #getPageNumber()} began when Word last laid the document out (REQ-708). */
		PAGE_MARKER
	}

	/** {@code footnote} or {@code endnote}. */
	public static final String FOOTNOTE = "footnote", ENDNOTE = "endnote";

	final Kind kind;
	final String text;
	final TextRevision revision;
	boolean hidden;
	boolean placeholder;
	String noteType;
	int noteNumber;
	String noteId;
	String commentId;
	TextImage image;
	ImageDescription description;
	String progId;
	int pageNumber;
	boolean bold, italic, strike, monospace;
	String link;
	boolean math, displayMath;

	TextRun(Kind kind, String text, TextRevision revision) {
		this.kind = kind;
		this.text = text == null ? "" : text;
		this.revision = revision;
	}

	public Kind getKind() { return kind; }

	/** The text: the document's for {@link Kind#TEXT}, {@link Kind#FIELD_CODE} and {@link Kind#LIST_LABEL}; empty for the others. */
	public String getText() { return text; }

	/** The run's revision in the {@code markup} view; null in the other views and for an unrevised run. */
	public TextRevision getRevision() { return revision; }

	/** True for hidden text included under {@code hidden: true} (REQ-507). */
	public boolean isHidden() { return hidden; }

	/** True for a content control's placeholder text (REQ-508). */
	public boolean isPlaceholder() { return placeholder; }

	/** For a note reference or mark: {@link #FOOTNOTE} or {@link #ENDNOTE}; else null. */
	public String getNoteType() { return noteType; }

	/** For a note reference or mark: its number in reference order, from 1; 0 for a custom mark, which has none. */
	public int getNoteNumber() { return noteNumber; }

	/** For a note reference or mark: the note's {@code w:id}. */
	public String getNoteId() { return noteId; }

	/** For a comment reference: the comment's {@code w:id}. */
	public String getCommentId() { return commentId; }

	/** For an image: what the document says of it (alt text, name, whether it is decorative). */
	public TextImage getImage() { return image; }

	/** For an image: the describer's description, with its provenance; null for none (a decorative picture's). */
	public ImageDescription getDescription() { return description; }

	/** For an object: its ProgID ({@code o:OLEObject/@ProgID}), or null (an ActiveX control's is in its part). */
	public String getProgId() { return progId; }

	/** For a page marker: the number of the page that begins there, from 2 (the first marker starts the second page). */
	public int getPageNumber() { return pageNumber; }

	/**
	 * True for text that is bold where its paragraph's style is not: emphasis, as Markdown writes it
	 * (a heading's own bold is not). The run's {@code w:b}, else its styles' (CR-046 phase 3).
	 */
	public boolean isBold() { return bold; }

	/** True for text that is italic where its paragraph's style is not ({@code w:i}; see {@link #isBold()}). */
	public boolean isItalic() { return italic; }

	/** True for text struck through where its paragraph's style is not ({@code w:strike}; see {@link #isBold()}). */
	public boolean isStrike() { return strike; }

	/**
	 * True for text in a monospaced font where its paragraph's style is not in one: code, as Markdown
	 * writes it. A paragraph whose style is monospaced has {@link TextBlock#isMonospace()} instead.
	 */
	public boolean isMonospace() { return monospace; }

	/**
	 * For text in a link: its target, whether the link is a {@code w:hyperlink} (its relationship's
	 * target, else {@code #} and its {@code w:anchor}) or a HYPERLINK field's result (its instruction's
	 * address, and {@code #} and its {@code \l} bookmark), whatever {@code field_codes} says (WD REQ-703);
	 * else null.
	 */
	public String getLink() { return link; }

	/** True for an equation (WD REQ-604): the text is its LaTeX, delimited {@code $...$}, or {@code $$...$$} where {@link #isDisplayMath()}. */
	public boolean isMath() { return math; }

	/** True for a display equation ({@code m:oMathPara}), delimited {@code $$...$$}. */
	public boolean isDisplayMath() { return displayMath; }

	/** Whether two runs' text can be one run: the same formatting, link and equation. */
	boolean sameFormat(TextRun o) {
		return bold == o.bold && italic == o.italic && strike == o.strike && monospace == o.monospace
				&& java.util.Objects.equals(link, o.link) && math == o.math && displayMath == o.displayMath;
	}

	void copyFormat(TextRun from) {
		bold = from.bold;
		italic = from.italic;
		strike = from.strike;
		monospace = from.monospace;
		link = from.link;
		math = from.math;
		displayMath = from.displayMath;
	}

	@Override
	public String toString() {
		return kind + (text.isEmpty() ? "" : " '" + text + "'") + (revision == null ? "" : " [" + revision + "]");
	}
}
