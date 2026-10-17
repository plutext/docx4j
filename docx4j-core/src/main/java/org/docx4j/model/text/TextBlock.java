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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A block of the block model (WD REQ-706): a paragraph, or a table whose cells hold blocks. A
 * paragraph's text is its runs' in the view; what an extractor adds in plain text (note numbers, a
 * table-format note) is not in it, but carried as fields (REQ-707).
 *
 * <p>Addresses are docx4j-core-ts CR-002 section 3.3's ordinal form, on the unresolved document
 * (REQ-405, REQ-518): {@code body/3} (the fourth child of the body), {@code body/4/1/0/2} (a
 * table's row 1, cell 0, block 2), {@code header:rId3/0}, {@code footnote:2/0},
 * {@code comment:5/0}, and for a text box {@code body/3/textbox:0/1} (the proposed form: the first
 * box anchored in {@code body/3}, its second block). A paragraph joined to the next in the
 * {@code accepted} view (a deleted mark) takes the first paragraph's address and {@code w14:paraId}.</p>
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextBlock {

	public enum Kind { PARAGRAPH, TABLE }

	/** The story a block is in (WD section 5.1). */
	public enum Story { MAIN, TEXT_BOX, FOOTNOTE, ENDNOTE, HEADER, FOOTER, COMMENT, GLOSSARY }

	final Kind kind;
	final Story story;
	final String address;
	String paraId;
	String style;
	final List<TextRun> runs = new ArrayList<TextRun>();
	final List<TextField> fields = new ArrayList<TextField>();
	final List<TextRow> rows = new ArrayList<TextRow>();
	boolean hidden;
	int revisions;
	Integer indentLeft, indentHanging, indentFirstLine;
	String alignment;
	boolean monospace;
	String listLabel;
	String noteType;
	int noteNumber;
	String noteId;
	/** The section the block ends or is in (main story only). */
	int section;

	TextBlock(Kind kind, Story story, String address) {
		this.kind = kind;
		this.story = story;
		this.address = address;
	}

	public Kind getKind() { return kind; }
	public Story getStory() { return story; }
	public String getAddress() { return address; }
	/** The paragraph's {@code w14:paraId} where Word wrote one; else null. */
	public String getParaId() { return paraId; }
	/** The paragraph's style id ({@code w:pStyle}), or null. */
	public String getStyle() { return style; }
	public List<TextRun> getRuns() { return Collections.unmodifiableList(runs); }
	/** The fields whose result or instruction is in the block (REQ-505). */
	public List<TextField> getFields() { return Collections.unmodifiableList(fields); }
	/** A table's rows; empty for a paragraph. */
	public List<TextRow> getRows() { return Collections.unmodifiableList(rows); }

	/** True when the block holds placeholder text (REQ-508). */
	public boolean isPlaceholder() {
		for (TextRun r : runs) if (r.placeholder) return true;
		return false;
	}

	/** True for a hidden paragraph included under {@code hidden: true}: its runs and mark are hidden (REQ-507). */
	public boolean isHidden() { return hidden; }
	/** True for a block in a text box (REQ-518). */
	public boolean isTextbox() { return story == Story.TEXT_BOX; }
	/** True for a decorative picture's block; always false until images are blocks (CR-046 phase 2). */
	public boolean isDecorative() { return false; }
	/** How many revisions (run-level revisions and paragraph marks) the block's paragraphs hold, in any view. */
	public int getRevisions() { return revisions; }
	/** The effective left indent in twips, or null. */
	public Integer getIndentLeft() { return indentLeft; }
	/** The effective hanging indent in twips, or null. */
	public Integer getIndentHanging() { return indentHanging; }
	/** The effective first-line indent in twips, or null. */
	public Integer getIndentFirstLine() { return indentFirstLine; }
	/** The effective alignment ({@code w:jc}'s value), or null. */
	public String getAlignment() { return alignment; }
	/** True when every text run is in a monospaced font. */
	public boolean isMonospace() { return monospace; }
	/** The paragraph's list label from the numbering model, under {@code list_labels: true}; else null. */
	public String getListLabel() { return listLabel; }
	/** For a note's block: {@link TextRun#FOOTNOTE} or {@link TextRun#ENDNOTE}; else null. */
	public String getNoteType() { return noteType; }
	/** For a note's block: the note's number in reference order; else 0. */
	public int getNoteNumber() { return noteNumber; }
	/** For a note's block: the note's {@code w:id}; else null. */
	public String getNoteId() { return noteId; }

	/**
	 * The block's text in the view: its text runs (and field codes, where included); for a table, its
	 * rows a line apart, their cells a tab apart. No list label, note number or comment.
	 */
	public String getText() {
		StringBuilder sb = new StringBuilder();
		if (kind == Kind.TABLE) {
			for (int r = 0; r < rows.size(); r++) {
				if (r > 0) sb.append('\n');
				List<TextCell> cells = rows.get(r).cells;
				for (int c = 0; c < cells.size(); c++) {
					if (c > 0) sb.append('\t');
					List<TextBlock> blocks = cells.get(c).blocks;
					for (int b = 0; b < blocks.size(); b++) {
						if (b > 0) sb.append(' ');
						sb.append(blocks.get(b).getText());
					}
				}
			}
			return sb.toString();
		}
		for (TextRun r : runs) if (r.kind == TextRun.Kind.TEXT || r.kind == TextRun.Kind.FIELD_CODE) sb.append(r.text);
		return sb.toString();
	}

	@Override
	public String toString() {
		return address + " " + story + " '" + getText() + "'";
	}
}
