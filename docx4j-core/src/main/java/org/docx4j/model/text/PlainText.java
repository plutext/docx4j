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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Plain text from the block model (WD section 7.1): each paragraph one line, a break one line more
 * (REQ-603); the text exactly as the document holds it (REQ-602), U+000A between lines and after the
 * last.
 *
 * <p>Tables (REQ-605): a simple table (no merged cells, no nested table, one paragraph a cell, every
 * row the same number of cells) a row a line, its cells a tab apart; any other in the listed format,
 * after the note line {@code [table: listed format]}, a row a line as {@code Header: value; Header:
 * value}, the headers from the {@code w:tblHeader} rows else the first row, a cell's paragraphs (a
 * nested table's included) {@code ; } apart; a row whose one cell spans the grid is its text alone,
 * and a table with no labels has its cells a tab apart. {@code tables: tsv} writes every table in the simple
 * form, a cell's paragraphs a space apart; {@code tables: grid} as a pandoc grid table, its spans
 * drawn. A chart is its note ({@code [chart: clustered column; value axis: Revenue]}, CR-047's
 * {@code ChartDescription}), its title, then its table's rows, a tab between cells (REQ-511).</p>
 *
 * <p>What the extractor adds follows the WD's bracket convention (REQ-707): {@code [n]} at a note's
 * reference and at the start of the note; a chart's {@code [chart: ...]}; {@code [image: ...]} (or {@code [image, generated: ...]})
 * under {@code alt_text: true}; {@code [page n]} under {@code page_markers: true}; and under
 * {@code metadata: true}, before the body, one line per kind of property that has any:
 * {@code [core properties: title: ...; subject: ...]}, {@code [application properties: Company: ...]},
 * {@code [custom properties: name: value]} (REQ-515). In the {@code markup} view, insertions are
 * {@code {++text++}}, deletions {@code {--text--}} and comments {@code {>>text<<}}, at their
 * references with their replies after them (REQ-702, REQ-504); under {@code markup_detail} the
 * marker's opening is followed by {@code author:} or {@code author, date:}.</p>
 *
 * <p>Under {@code layout: indent} (REQ-709) a paragraph outside a table begins with four spaces for
 * each tab stop (720 twips) of its first line's indent, its left indent plus its first-line indent
 * less its hanging indent, rounded to the nearest stop; its tabs are kept.</p>
 *
 * @since 17.3.4 (CR-046 phase 1; phase 2 added the table forms, charts, images, page markers,
 *        metadata and indents)
 */
public final class PlainText {

	/** The spaces for each 720-twip tab stop of indent under {@code layout: indent} (REQ-709). */
	public static final int INDENT_SPACES = 4;

	/** The note line before a table in the listed format (REQ-605, REQ-707). */
	public static final String LISTED_FORMAT_NOTE = "[table: listed format]";

	private PlainText() {}

	/** The plain text of a result. */
	public static String of(TextResult result) {
		StringBuilder sb = new StringBuilder();
		Writer w = new Writer(result, sb);
		if (result.options().isMetadata()) w.metadata(result.metadata());
		for (TextBlock block : result.blocks()) {
			w.top(block);
			sb.append('\n');
		}
		return sb.toString();
	}

	private static final class Writer {
		final TextResult result;
		final TextOptions options;
		StringBuilder sb;

		Writer(TextResult result, StringBuilder sb) {
			this.result = result;
			this.options = result.options();
			this.sb = sb;
		}

		// --- the metadata block (REQ-515) ----------------------------------------------------------

		void metadata(Map<String, Object> metadata) {
			group("core properties", metadata.get(TextResult.CORE));
			group("application properties", metadata.get(TextResult.APP));
			group("custom properties", metadata.get(TextResult.CUSTOM));
		}

		private void group(String label, Object properties) {
			if (!(properties instanceof Map) || ((Map<?, ?>) properties).isEmpty()) return;
			sb.append('[').append(label).append(": ");
			boolean first = true;
			for (Map.Entry<?, ?> e : ((Map<?, ?>) properties).entrySet()) {
				if (!first) sb.append("; ");
				first = false;
				sb.append(e.getKey()).append(": ").append(e.getValue());
			}
			sb.append("]\n");
		}

		// --- blocks --------------------------------------------------------------------------------

		/** A block of the story: a paragraph with its indent, a table, a chart. */
		void top(TextBlock block) {
			if (block.getKind() == TextBlock.Kind.PARAGRAPH && options.getLayout() == TextOptions.Layout.INDENT) indent(block);
			block(block);
		}

		private void indent(TextBlock block) {
			int twips = value(block.getIndentLeft()) + value(block.getIndentFirstLine()) - value(block.getIndentHanging());
			long stops = Math.round(twips / 720.0);
			for (long i = 0; i < stops * INDENT_SPACES; i++) sb.append(' ');
		}

		private static int value(Integer i) {
			return i == null ? 0 : i;
		}

		void block(TextBlock block) {
			switch (block.getKind()) {
			case TABLE:
				table(block);
				return;
			case CHART:
				chart(block);
				return;
			default:
				paragraph(block);
			}
		}

		private void chart(TextBlock block) {
			boolean any = false;
			if (block.getChart() != null) {
				sb.append(block.getChart().getNote());
				any = true;
			}
			if (block.getChartTitle() != null && !block.getChartTitle().isEmpty()) {
				if (any) sb.append('\n');
				sb.append(block.getChartTitle());
				any = true;
			}
			List<List<String>> lines = new ArrayList<List<String>>();
			if (!block.getChartHeader().isEmpty()) lines.add(block.getChartHeader());
			lines.addAll(block.getChartRows());
			for (List<String> line : lines) {
				if (any) sb.append('\n');
				sb.append(String.join("\t", line));
				any = true;
			}
		}

		private void paragraph(TextBlock block) {
			if (block.getListLabel() != null) sb.append(block.getListLabel());
			List<TextRun> runs = block.getRuns();
			int i = 0;
			while (i < runs.size()) {
				TextRevision revision = runs.get(i).getRevision();
				int j = i;
				while (j < runs.size() && sameMarker(revision, runs.get(j).getRevision())) j++;
				if (revision != null) {
					sb.append(revision.getType() == TextRevision.Type.INSERTION ? "{++" : "{--");
					detail(revision.getAuthor(), revision.getDate());
				}
				for (int k = i; k < j; k++) run(runs.get(k));
				if (revision != null) sb.append(revision.getType() == TextRevision.Type.INSERTION ? "++}" : "--}");
				i = j;
			}
		}

		/** Runs under one marker: the same type, and the same author and date where the detail shows them. */
		private boolean sameMarker(TextRevision a, TextRevision b) {
			if (a == null || b == null) return a == b;
			if (a.getType() != b.getType()) return false;
			switch (options.getMarkupDetail()) {
			case AUTHOR: return Objects.equals(a.getAuthor(), b.getAuthor());
			case AUTHOR_DATE: return Objects.equals(a.getAuthor(), b.getAuthor()) && Objects.equals(a.getDate(), b.getDate());
			default: return true;
			}
		}

		private void detail(String author, String date) {
			switch (options.getMarkupDetail()) {
			case AUTHOR:
				sb.append(author == null ? "" : author).append(": ");
				break;
			case AUTHOR_DATE:
				sb.append(author == null ? "" : author).append(", ").append(date == null ? "" : date).append(": ");
				break;
			default:
			}
		}

		void run(TextRun run) {
			switch (run.getKind()) {
			case TEXT:
			case FIELD_CODE:
			case LIST_LABEL:
				sb.append(run.getText());
				break;
			case NOTE_REFERENCE:
				if (options.isNoteReferences() && options.isNotes() && run.getNoteNumber() > 0) sb.append('[').append(run.getNoteNumber()).append(']');
				break;
			case NOTE_MARK:
				if (run.getNoteNumber() > 0) sb.append('[').append(run.getNoteNumber()).append(']');
				break;
			case COMMENT_REFERENCE: {
				TextComment comment = result.comments().get(run.getCommentId());
				if (comment == null || comment.getParentId() != null && result.comments().containsKey(comment.getParentId())) break;
				comment(comment);
				for (TextComment reply : comment.getReplies()) comment(reply);
				break;
			}
			case IMAGE: {
				ImageDescription d = run.getDescription();
				if (!options.isAltText() || d == null) break;
				sb.append(d.getProvenance() == ImageDescription.Provenance.GENERATED ? "[image, generated: " : "[image: ")
						.append(d.getText()).append(']');
				break;
			}
			case PAGE_MARKER:
				if (options.isPageMarkers()) sb.append("[page ").append(run.getPageNumber()).append(']');
				break;
			default:
			}
		}

		private void comment(TextComment comment) {
			sb.append("{>>");
			detail(comment.getAuthor(), comment.getDate());
			sb.append(comment.getText()).append("<<}");
		}

		/** A block written on its own, for a table cell's value. */
		private String text(TextBlock block) {
			StringBuilder saved = sb;
			sb = new StringBuilder();
			try {
				block(block);
				return sb.toString();
			} finally {
				sb = saved;
			}
		}

		// --- tables (REQ-605) ---------------------------------------------------------------------

		private void table(TextBlock table) {
			TextOptions.Tables form = options.getTables();
			if (table.isSimpleForm()) form = TextOptions.Tables.TSV;
			if (form == TextOptions.Tables.GRID) {
				grid(table);
			} else if (form == TextOptions.Tables.TSV || isSimple(table)) {
				tabSeparated(table);
			} else {
				listed(table);
			}
		}

		/**
		 * No merged or split cells, no nested table, one paragraph a cell. A cell is merged or split
		 * when it crosses a column edge another row has, or is in a vertical merge; a cell spanning
		 * grid columns that every row spans alike merges nothing (Word leaves such columns behind).
		 * A cell's empty paragraphs don't count: Word ends every cell with a paragraph, so a
		 * block-level control in a cell comes with an empty one after it.
		 */
		static boolean isSimple(TextBlock table) {
			java.util.Set<Integer> edges = null;
			for (TextRow row : table.getRows()) {
				java.util.Set<Integer> own = edges(row);
				if (edges != null && !edges.equals(own)) return false;
				edges = own;
				for (TextCell cell : row.getCells()) {
					if (cell.isMergeContinue() || cell.isMergeRestart()) return false;
					int paragraphs = 0;
					for (TextBlock b : cell.getBlocks()) {
						if (b.getKind() != TextBlock.Kind.PARAGRAPH) return false;
						if (!b.getRuns().isEmpty() || b.getListLabel() != null) paragraphs++;
					}
					if (paragraphs > 1) return false;
				}
			}
			return true;
		}

		private void tabSeparated(TextBlock table) {
			List<TextRow> rows = table.getRows();
			for (int r = 0; r < rows.size(); r++) {
				if (r > 0) sb.append('\n');
				List<TextCell> cells = rows.get(r).getCells();
				for (int c = 0; c < cells.size(); c++) {
					if (c > 0) sb.append('\t');
					cellOnOneLine(cells.get(c));
				}
			}
		}

		/** A cell's paragraphs, a nested table's included, a space apart; its empty paragraphs left out. */
		private void cellOnOneLine(TextCell cell) {
			List<String> paragraphs = new ArrayList<String>();
			values(cell, paragraphs);
			sb.append(String.join(" ", paragraphs));
		}

		/** A row's cell edges in grid columns, by gridSpan: 0, and each cell's end. */
		private static java.util.Set<Integer> edges(TextRow row) {
			java.util.Set<Integer> edges = new java.util.TreeSet<Integer>();
			int col = 0;
			edges.add(col);
			for (TextCell c : row.getCells()) edges.add(col += Math.max(1, c.getGridSpan()));
			return edges;
		}

		/** The grid columns each cell of a row starts at, by gridSpan. */
		private static int[] starts(TextRow row) {
			int[] starts = new int[row.getCells().size()];
			int col = 0;
			for (int c = 0; c < starts.length; c++) {
				starts[c] = col;
				col += Math.max(1, row.getCells().get(c).getGridSpan());
			}
			return starts;
		}

		/** The header rows: those marked {@code w:tblHeader}, else the first. */
		private static int headerRows(List<TextRow> rows) {
			int n = 0;
			while (n < rows.size() && rows.get(n).isHeader()) n++;
			return n > 0 ? n : Math.min(1, rows.size());
		}

		/**
		 * The listed format (REQ-605). Every cell is labelled by the header over its first grid column, or
		 * the first of its columns' headers that has text; several header rows' texts are joined
		 * {@code  / }; a cell under a header with no text is its value alone. A row whose one cell spans
		 * every grid column, a header row or a body row, is that cell's text alone, and labels nothing.
		 * A cell with no value is left out; a vertical merge's value is on every row it covers. Labelled
		 * cells are {@code ; } apart; in a table with no labels (a table of header rows only, or whose
		 * headers span the grid or have no text) cells are a tab apart, as in the simple form.
		 */
		private void listed(TextBlock table) {
			sb.append(LISTED_FORMAT_NOTE);
			List<TextRow> rows = table.getRows();
			int width = width(rows);
			int headerRows = headerRows(rows);
			// a table of header rows only is written unlabelled, its rows as body rows
			boolean headersOnly = headerRows >= rows.size();
			// each grid column's header; a full-width header row is written once, as its text
			Map<Integer, String> headers = new java.util.HashMap<Integer, String>();
			if (!headersOnly) for (int r = 0; r < headerRows; r++) {
				TextRow row = rows.get(r);
				if (fullWidth(row, width)) {
					String text = value(row.getCells().get(0));
					if (!text.isEmpty()) sb.append('\n').append(text);
					continue;
				}
				int[] starts = starts(row);
				for (int c = 0; c < starts.length; c++) {
					TextCell cell = row.getCells().get(c);
					String text = value(cell);
					if (text.isEmpty()) continue;
					for (int g = starts[c]; g < starts[c] + Math.max(1, cell.getGridSpan()); g++) {
						String h = headers.get(g);
						headers.put(g, h == null ? text : h + " / " + text);
					}
				}
			}
			boolean labelled = !headers.isEmpty();
			List<TextRow> body = headersOnly ? rows : rows.subList(headerRows, rows.size());
			Map<Integer, String> restarts = new java.util.HashMap<Integer, String>();
			for (TextRow row : body) {
				sb.append('\n');
				if (fullWidth(row, width)) {
					sb.append(value(row.getCells().get(0)));
					continue;
				}
				int[] starts = starts(row);
				List<String> parts = new ArrayList<String>();
				for (int c = 0; c < starts.length; c++) {
					TextCell cell = row.getCells().get(c);
					String value;
					if (cell.isMergeContinue()) {
						value = restarts.containsKey(starts[c]) ? restarts.get(starts[c]) : "";
					} else {
						value = value(cell);
						if (cell.isMergeRestart()) restarts.put(starts[c], value);
					}
					if (value.isEmpty()) continue;
					parts.add(labelled ? label(header(headers, starts[c], Math.max(1, cell.getGridSpan())), value) : value);
				}
				sb.append(String.join(labelled ? "; " : "\t", parts));
			}
		}

		/** The grid's width: the most grid columns a row spans. */
		private static int width(List<TextRow> rows) {
			int width = 0;
			for (TextRow row : rows) {
				int w = 0;
				for (TextCell cell : row.getCells()) w += Math.max(1, cell.getGridSpan());
				width = Math.max(width, w);
			}
			return width;
		}

		/** A row of one cell spanning every grid column: a title, or a divider such as "Part A". */
		private static boolean fullWidth(TextRow row, int width) {
			return row.getCells().size() == 1 && Math.max(1, row.getCells().get(0).getGridSpan()) == width;
		}

		/** The first header with text over a cell's grid columns, or null. */
		private static String header(Map<Integer, String> headers, int start, int span) {
			for (int g = start; g < start + span; g++) {
				String h = headers.get(g);
				if (h != null && !h.isEmpty()) return h;
			}
			return null;
		}

		private static String label(String header, CharSequence value) {
			return header == null || header.isEmpty() ? value.toString() : header + ": " + value;
		}

		/** A cell's paragraphs, a nested table's included, {@code ; } apart, empty ones left out. */
		private String value(TextCell cell) {
			List<String> paragraphs = new ArrayList<String>();
			values(cell, paragraphs);
			return String.join("; ", paragraphs);
		}

		private void values(TextCell cell, List<String> out) {
			for (TextBlock b : cell.getBlocks()) {
				if (b.getKind() == TextBlock.Kind.TABLE) {
					for (TextRow r : b.getRows()) for (TextCell c : r.getCells()) values(c, out);
				} else {
					String s = text(b);
					if (!s.isEmpty()) out.add(s);
				}
			}
		}

		// --- pandoc grid tables -------------------------------------------------------------------

		/** A cell placed on the grid. */
		private static final class Placed {
			final int row, col, span;
			int rowSpan = 1;
			final List<String> lines;
			Placed(int row, int col, int span, List<String> lines) {
				this.row = row;
				this.col = col;
				this.span = span;
				this.lines = lines;
			}
		}

		private void grid(TextBlock table) {
			for (String line : gridLines(table)) {
				if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') sb.append('\n');
				sb.append(line);
			}
		}

		private List<String> gridLines(TextBlock table) {
			List<TextRow> rows = table.getRows();
			List<Placed> placed = new ArrayList<Placed>();
			Placed[][] at = new Placed[rows.size()][];
			// the grid's columns are those some row's cell edges mark, so a span every row shares draws as one column
			java.util.TreeSet<Integer> union = new java.util.TreeSet<Integer>();
			for (TextRow row : rows) union.addAll(edges(row));
			List<Integer> edgeList = new ArrayList<Integer>(union);
			int columns = edgeList.size() - 1;
			if (columns <= 0) return new ArrayList<String>();
			Map<Integer, Placed> open = new java.util.HashMap<Integer, Placed>();
			for (int r = 0; r < rows.size(); r++) {
				at[r] = new Placed[columns];
				TextRow row = rows.get(r);
				int[] starts = starts(row);
				for (int c = 0; c < starts.length; c++) {
					TextCell cell = row.getCells().get(c);
					int first = edgeList.indexOf(starts[c]);
					int span = edgeList.indexOf(starts[c] + Math.max(1, cell.getGridSpan())) - first;
					Placed p;
					if (cell.isMergeContinue() && open.get(first) != null && open.get(first).span == span) {
						p = open.get(first);
						p.rowSpan++;
					} else {
						p = new Placed(r, first, span, cellLines(cell));
						placed.add(p);
						if (cell.isMergeRestart()) open.put(first, p);
						else open.remove(first);
					}
					for (int g = first; g < first + span && g < columns; g++) at[r][g] = p;
				}
			}
			// column widths: single-column cells first, then spans widen their last column
			int[] widths = new int[columns];
			java.util.Arrays.fill(widths, 3);
			for (Placed p : placed) if (p.span == 1) for (String l : p.lines) widths[p.col] = Math.max(widths[p.col], l.length());
			for (Placed p : placed) {
				if (p.span == 1) continue;
				int last = Math.min(columns, p.col + p.span) - 1;
				int have = 0;
				for (int g = p.col; g <= last; g++) have += widths[g];
				have += 3 * (last - p.col);
				for (String l : p.lines) if (l.length() > have) { widths[last] += l.length() - have; have = l.length(); }
			}
			// row heights: a cell's lines over the rows it spans (the borders it crosses hold none)
			int[] heights = new int[rows.size()];
			java.util.Arrays.fill(heights, 1);
			for (Placed p : placed) if (p.rowSpan == 1) heights[p.row] = Math.max(heights[p.row], p.lines.size());
			for (Placed p : placed) {
				if (p.rowSpan == 1) continue;
				int have = 0;
				for (int r = p.row; r < p.row + p.rowSpan; r++) have += heights[r];
				if (p.lines.size() > have) heights[p.row + p.rowSpan - 1] += p.lines.size() - have;
			}
			int header = rows.size() > 1 ? headerRows(rows) : 0;
			List<String> out = new ArrayList<String>();
			out.add(border(null, at.length > 0 ? at[0] : null, widths, '-'));
			Map<Placed, Integer> written = new IdentityHashMap<Placed, Integer>();
			for (int r = 0; r < rows.size(); r++) {
				for (int line = 0; line < heights[r]; line++) {
					StringBuilder l = new StringBuilder("|");
					int g = 0;
					while (g < columns) {
						Placed p = at[r][g];
						int span = p == null ? 1 : Math.min(p.span, columns - g);
						int width = 0;
						for (int k = g; k < g + span; k++) width += widths[k];
						width += 3 * (span - 1);
						String text = "";
						if (p != null) {
							int n = written.containsKey(p) ? written.get(p) : 0;
							if (n < p.lines.size()) text = p.lines.get(n);
							written.put(p, n + 1);
						}
						l.append(' ').append(text);
						for (int k = text.length(); k < width; k++) l.append(' ');
						l.append(" |");
						g += span;
					}
					out.add(l.toString());
				}
				if (r < rows.size() - 1) out.add(border(at[r], at[r + 1], widths, r == header - 1 ? '=' : '-'));
			}
			out.add(border(at[rows.size() - 1], null, widths, '-'));
			return out;
		}

		/** The border between two rows: open where a cell spans both. */
		private static String border(Placed[] above, Placed[] below, int[] widths, char fill) {
			StringBuilder l = new StringBuilder();
			for (int g = 0; g < widths.length; g++) {
				boolean through = above != null && below != null && above[g] != null && above[g] == below[g];
				boolean joinLeft = g > 0 && above != null && below != null && above[g] != null && above[g] == below[g]
						&& above[g - 1] == above[g];
				l.append(g == 0 ? (through ? '|' : '+') : joinLeft ? ' ' : '+');
				for (int k = 0; k < widths[g] + 2; k++) l.append(through ? ' ' : fill);
			}
			boolean throughLast = above != null && below != null && above[widths.length - 1] != null
					&& above[widths.length - 1] == below[widths.length - 1];
			l.append(throughLast ? '|' : '+');
			return l.toString();
		}

		/** A cell's lines: each paragraph's lines, a blank line between paragraphs, a nested table drawn as a grid; empty paragraphs left out. */
		private List<String> cellLines(TextCell cell) {
			List<String> lines = new ArrayList<String>();
			for (TextBlock b : cell.getBlocks()) {
				List<String> own = new ArrayList<String>();
				if (b.getKind() == TextBlock.Kind.TABLE) {
					own.addAll(gridLines(b));
				} else {
					String s = text(b);
					if (s.isEmpty()) continue;
					for (String line : s.split("\n", -1)) own.add(line);
				}
				if (!lines.isEmpty()) lines.add("");
				lines.addAll(own);
			}
			return lines;
		}
	}
}
