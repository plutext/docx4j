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
 * A block of the block model (WD REQ-706): a paragraph, a table whose cells hold blocks, or a chart.
 * A paragraph's text is its runs' in the view; what an extractor adds in plain text (note numbers, a
 * table-format note, an image's bracket) is not in it, but carried as fields (REQ-707).
 *
 * <p>Addresses are docx4j-core-ts CR-002 section 3.3's ordinal form, on the unresolved document
 * (REQ-405, REQ-518): {@code body/3} (the fourth child of the body), {@code body/4/1/0/2} (a
 * table's row 1, cell 0, block 2), {@code header:rId3/0}, {@code footnote:2/0},
 * {@code comment:5/0}, and for a text box {@code body/3/textbox:0/1} (the proposed form: the first
 * box anchored in {@code body/3}, its second block). A paragraph joined to the next in the
 * {@code accepted} view (a deleted mark) takes the first paragraph's address and {@code w14:paraId}.</p>
 *
 * <p>What follows an anchoring paragraph is addressed from it in the same way, each kind counted
 * apart (proposed forms, CR-046 phase 2): a chart {@code body/3/chart:0}; SmartArt's lines
 * {@code body/3/smartart:0/1}; legacy WordArt, a text box, {@code body/3/textbox:1/0}; an embedded
 * package's content {@code body/3/object:0/} and its own address ({@code body/0} in a document,
 * {@code sheet:0} and {@code sheet:0/table} in a workbook, {@code slide:0/2} in a presentation). An
 * {@code altChunk}'s content is addressed from the chunk, {@code body/7/altchunk:0/} and its own address.
 * The glossary's blocks are {@code glossary:0/1} (the first entry's second block).</p>
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextBlock {

	public enum Kind {
		PARAGRAPH,
		TABLE,
		/** A chart (REQ-511): {@link #getChartTitle()}, {@link #getChartHeader()} and {@link #getChartRows()}, after its anchoring paragraph. */
		CHART
	}

	/**
	 * The story a block is in (WD section 5.1). SmartArt's text and an embedded package's content
	 * follow their anchoring paragraph but are kept apart from its story; an {@code altChunk}'s
	 * content is in the story the chunk is in, as Word expands it there.
	 */
	public enum Story { MAIN, TEXT_BOX, FOOTNOTE, ENDNOTE, HEADER, FOOTER, COMMENT, GLOSSARY, SMARTART, EMBEDDED }

	final Kind kind;
	/** Not final: the content of an embedded package or an {@code altChunk} takes the story it is read into. */
	Story story;
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
	Integer outlineLevel;
	TextNumbering numbering;
	Boolean contextualSpacing;
	boolean bottomBorder;
	String noteType;
	int noteNumber;
	String noteId;
	/** The section the block ends or is in (main story only). */
	int section;
	/** A table written in REQ-605's simple form whatever its shape (an embedded workbook's sheet). */
	boolean simpleForm;
	String chartTitle;
	org.docx4j.model.chart.ChartDescription chart;
	List<String> chartHeader = Collections.emptyList();
	List<List<String>> chartRows = Collections.emptyList();

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
	/** True for a paragraph whose only content is one or more pictures marked decorative (REQ-514). */
	public boolean isDecorative() {
		boolean decorative = false;
		for (TextRun r : runs) {
			if (r.kind == TextRun.Kind.IMAGE) {
				if (r.image == null || !r.image.isDecorative()) return false;
				decorative = true;
			} else if (r.kind == TextRun.Kind.TEXT && !r.text.isEmpty()) {
				return false;
			}
		}
		return decorative;
	}
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
	/** The effective outline level ({@code w:outlineLvl}), 0 to 8, or null for body text (CR-046 phase 3). */
	public Integer getOutlineLevel() { return outlineLevel; }
	/** The paragraph's numbering (its effective {@code w:numPr} with a {@code w:numId} other than 0), or null (CR-046 phase 3). */
	public TextNumbering getNumbering() { return numbering; }
	/** The effective {@code w:contextualSpacing}, or null where neither the paragraph nor its styles set it (CR-046 phase 3). */
	public Boolean getContextualSpacing() { return contextualSpacing; }
	/** True when the paragraph has a bottom border of its own ({@code w:pBdr/w:bottom} in its {@code w:pPr}; CR-046 phase 3). */
	public boolean hasBottomBorder() { return bottomBorder; }
	/** For a note's block: {@link TextRun#FOOTNOTE} or {@link TextRun#ENDNOTE}; else null. */
	public String getNoteType() { return noteType; }
	/** For a note's block: the note's number in reference order; else 0. */
	public int getNoteNumber() { return noteNumber; }
	/** For a note's block: the note's {@code w:id}; else null. */
	public String getNoteId() { return noteId; }

	/** For a table: true when it is always written in REQ-605's simple form (an embedded workbook's sheet). */
	public boolean isSimpleForm() { return simpleForm; }

	/** For a chart: its title as Office shows it ({@code ChartTable.getTitle()}), or null for none. */
	public String getChartTitle() { return chartTitle; }

	/**
	 * For a chart: its description (CR-047), whose kind, axes and secondary series are the chart
	 * block's fields of WD REQ-706 ({@link org.docx4j.model.chart.ChartDescription#getKind()},
	 * {@code getAxes()}, {@code getSecondarySeries()}); null for any other block. Its note, which plain
	 * text writes before the title, is not in the block's text (REQ-707).
	 */
	public org.docx4j.model.chart.ChartDescription getChart() { return chart; }

	/** For a chart: its table's column headings ({@code ChartTable.getHeader()}): the categories' columns, then the series shown. */
	public List<String> getChartHeader() { return Collections.unmodifiableList(chartHeader); }

	/** For a chart: its table's rows, each number in its format code ({@code ChartTable.getRows()}). */
	public List<List<String>> getChartRows() { return Collections.unmodifiableList(chartRows); }

	/**
	 * The block's text in the view: its text runs (and field codes, where included); for a table, its
	 * rows a line apart, their cells a tab apart. No list label, note number or comment.
	 */
	public String getText() {
		StringBuilder sb = new StringBuilder();
		if (kind == Kind.CHART) {
			if (chartTitle != null && !chartTitle.isEmpty()) sb.append(chartTitle);
			if (!chartHeader.isEmpty()) {
				if (sb.length() > 0) sb.append('\n');
				sb.append(String.join("\t", chartHeader));
			}
			for (List<String> row : chartRows) sb.append('\n').append(String.join("\t", row));
			return sb.toString();
		}
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
