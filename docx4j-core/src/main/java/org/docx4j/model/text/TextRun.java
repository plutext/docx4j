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

	@Override
	public String toString() {
		return kind + (text.isEmpty() ? "" : " '" + text + "'") + (revision == null ? "" : " [" + revision + "]");
	}
}
