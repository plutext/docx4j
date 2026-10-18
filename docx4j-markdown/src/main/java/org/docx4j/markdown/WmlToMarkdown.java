package org.docx4j.markdown;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Link;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.docx4j.model.text.ImageDescription;
import org.docx4j.model.text.TextBlock;
import org.docx4j.model.text.TextCell;
import org.docx4j.model.text.TextComment;
import org.docx4j.model.text.TextNumbering;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextRevision;
import org.docx4j.model.text.TextRow;
import org.docx4j.model.text.TextRun;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.PPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the text model's block model ({@link TextResult}) as a commonmark AST, which
 * MarkdownRenderer then serializes - so markdown escaping is the reference implementation's job,
 * not ours.
 *
 * <p>Since 17.3.4 (CR-046 phase 3) the text is the text model's (WD REQ-202): its view, its
 * stories (the main story, each text box after its anchor, the notes), its hidden text left out,
 * its fields' results, its bound values, charts and equations. This class decides only how a block
 * is written (WD REQ-703): a heading from its outline level, not its style's name; a list from its
 * numbering; bold, italic, struck-through and code text from what a run adds to its paragraph's
 * style (so a Heading's inherent bold doesn't become **markers**); links from a {@code w:hyperlink}
 * or a HYPERLINK field; images with the describer's text; notes as {@code [^n]}; in the
 * {@code markup} view, CriticMarkup ({@code {++ ++}}, {@code {-- --}}, {@code {>> <<}}).  A table of
 * contents is the text model's to leave out ({@code toc: false}, the default), as it has been
 * Markdown's since 17.2.0.</p>
 */
class WmlToMarkdown {

	private static final Logger log = LoggerFactory.getLogger(WmlToMarkdown.class);

	private static final int TWIPS_PER_LEVEL = 720;

	private final TextResult result;
	private final TextOptions textOptions;
	private final MarkdownExportOptions options;
	/** For a style's own indent (a block quote's depth); may be null. */
	private final WordprocessingMLPackage pkg;

	// open-list state (spans consecutive numbered paragraphs)
	private static final class OpenList {
		final ListBlock node;
		final String numId;
		ListItem lastItem;
		OpenList(ListBlock node, String numId) {
			this.node = node;
			this.numId = numId;
		}
	}
	private final Deque<OpenList> openLists = new ArrayDeque<>();

	// open-quote state (spans consecutive Quote paragraphs)
	private BlockQuote openQuote;
	private int openQuoteDepth;

	// >0 while building a GFM table header row (its bold is convention, not markup)
	private int tableHeaderDepth;

	private int imageCounter;

	WmlToMarkdown(TextResult result, WordprocessingMLPackage pkg, MarkdownExportOptions options) {
		this.result = result;
		this.textOptions = result.options();
		this.options = options;
		this.pkg = pkg;
	}

	Document convert() {
		Document document = new Document();
		List<TextBlock> blocks = result.blocks();
		for (int i = 0; i < blocks.size(); i++) {
			TextBlock block = blocks.get(i);
			if (isNote(block)) {
				// a note's blocks are its definition, at the place the text model reads it (after the main story)
				closeLists();
				closeQuote();
				int last = i;
				while (last + 1 < blocks.size() && sameNote(block, blocks.get(last + 1))) last++;
				FootnoteDefinition definition = new FootnoteDefinition(
						noteLabel(block.getNoteType(), block.getNoteNumber(), block.getNoteId()));
				body(blocks.subList(i, last + 1), definition);
				closeLists();
				closeQuote();
				trimLeadingMarkerSpace(definition);
				document.appendChild(definition);
				i = last;
			} else {
				i = block(blocks, i, document);
			}
		}
		closeLists();
		closeQuote();
		return document;
	}

	// ---------------------------------------------------------------- blocks

	private void body(List<TextBlock> blocks, Node container) {
		for (int i = 0; i < blocks.size(); i++) i = block(blocks, i, container);
	}

	/** @return the index of the last block consumed (code blocks span several) */
	private int block(List<TextBlock> blocks, int index, Node container) {
		TextBlock block = blocks.get(index);
		switch (block.getKind()) {
		case TABLE:
			closeLists();
			closeQuote();
			table(block, container);
			return index;
		case CHART:
			closeLists();
			closeQuote();
			chart(block, container);
			return index;
		default:
			return paragraph(blocks, index, container);
		}
	}

	private int paragraph(List<TextBlock> blocks, int index, Node container) {

		TextBlock block = blocks.get(index);
		String style = block.getStyle();

		// thematic break: an empty paragraph carrying a bottom border of its own
		if (block.hasBottomBorder() && isEmpty(block)) {
			closeLists();
			closeQuote();
			ThematicBreak thematicBreak = new ThematicBreak();
			thematicBreak.setLiteral("---"); // else the renderer picks its own idiom
			container.appendChild(thematicBreak);
			return index;
		}

		// display math: a paragraph which IS one m:oMathPara
		TextRun displayMath = soleDisplayMath(block);
		if (displayMath != null) {
			closeLists();
			closeQuote();
			org.docx4j.markdown.math.DisplayMath node = new org.docx4j.markdown.math.DisplayMath();
			node.setLiteral(latex(displayMath));
			container.appendChild(node);
			return index;
		}

		// code block: a code-styled paragraph, or one whose paragraph style's own font is mono;
		// consecutive ones merged into one fence, one line each
		if (isCodeParagraph(block)) {
			closeLists();
			closeQuote();
			StringBuilder literal = new StringBuilder();
			int last = index;
			for (int j = index; j < blocks.size(); j++) {
				TextBlock b = blocks.get(j);
				if (b.getKind() != TextBlock.Kind.PARAGRAPH || !isCodeParagraph(b) || !sameStory(block, b)) break;
				literal.append(codeText(b)).append('\n');
				last = j;
			}
			FencedCodeBlock code = new FencedCodeBlock();
			code.setLiteral(literal.toString());
			container.appendChild(code);
			return last;
		}

		// heading via effective outlineLvl (0..5 -> #..######); checked BEFORE numbering, since
		// built-in Heading styles can carry legacy numPr
		Integer outlineLevel = block.getOutlineLevel();
		if (outlineLevel != null && outlineLevel <= 5) {
			closeLists();
			closeQuote();
			Heading heading = new Heading();
			heading.setLevel(outlineLevel + 1);
			inlines(block, heading, true);
			container.appendChild(heading);
			return index;
		}

		// list paragraph?
		TextNumbering numbering = block.getNumbering();
		if (numbering != null) {
			closeQuote();
			if (numbering.isLabel()) {
				// a level with numFmt none, or a lvlText with no %n, is a label ("NOTE", "EXAMPLE"),
				// not a list: emit it bold at the head of the paragraph
				closeLists();
				String label = numbering.getLevelText() == null ? "" : numbering.getLevelText().trim();
				org.commonmark.node.Paragraph labelled = inlineParagraph(block, false);
				if (label.length() > 0) {
					if (labelled.getFirstChild() != null) {
						labelled.prependChild(new Text(" "));
					}
					StrongEmphasis strong = new StrongEmphasis();
					strong.appendChild(new Text(label));
					labelled.prependChild(strong);
				}
				if (labelled.getFirstChild() != null) {
					container.appendChild(labelled);
				}
				return index;
			}
			listParagraph(block, numbering, container);
			return index;
		}

		// follow-on paragraph inside a loose list item (indented, unnumbered)
		if (!openLists.isEmpty() && ImportStyles.LIST_PARAGRAPH.equals(style)) {
			OpenList top = openLists.peek();
			if (top.lastItem != null) {
				top.node.setTight(false);
				top.lastItem.appendChild(inlineParagraph(block, true));
				return index;
			}
		}

		closeLists();

		// block quote (nesting depth from the indent beyond the quote style's own)
		if (ImportStyles.QUOTE.equals(style) || "IntenseQuote".equals(style)) {
			int beyond = value(block.getIndentLeft()) - styleIndentLeft(style);
			int depth = 1 + Math.max(0, beyond) / TWIPS_PER_LEVEL;
			appendToQuote(depth, inlineParagraph(block, true), container);
			return index;
		}

		closeQuote();

		org.commonmark.node.Paragraph paragraph = inlineParagraph(block, true);
		if (paragraph.getFirstChild() != null) {
			container.appendChild(paragraph);
		} // markdown has no empty paragraphs; drop them
		return index;
	}

	private org.commonmark.node.Paragraph inlineParagraph(TextBlock block, boolean withListLabel) {
		org.commonmark.node.Paragraph paragraph = new org.commonmark.node.Paragraph();
		inlines(block, paragraph, withListLabel);
		return paragraph;
	}

	private static boolean isEmpty(TextBlock block) {
		for (TextRun r : block.getRuns()) {
			if (r.getKind() == TextRun.Kind.TEXT && !r.getText().isEmpty()) return false;
			if (r.getKind() == TextRun.Kind.IMAGE || r.getKind() == TextRun.Kind.NOTE_REFERENCE) return false;
		}
		return true;
	}

	/** The paragraph's one run, a display equation with no revision; else null. */
	private static TextRun soleDisplayMath(TextBlock block) {
		TextRun found = null;
		for (TextRun r : block.getRuns()) {
			if (r.getKind() == TextRun.Kind.TEXT && r.getText().isEmpty()) continue;
			if (found != null || !r.isDisplayMath() || r.getRevision() != null) return null;
			found = r;
		}
		return found;
	}

	/** An equation's LaTeX: its text without the delimiters. */
	private static String latex(TextRun run) {
		String s = run.getText();
		int d = run.isDisplayMath() ? 2 : 1;
		if (s.length() >= 2 * d) return s.substring(d, s.length() - d);
		return s;
	}

	/** Paragraph styles which are code blocks by name, whatever their font. */
	private static final Set<String> CODE_STYLES = new HashSet<>(Arrays.asList(
			ImportStyles.SOURCE_CODE, "Code", "HTMLPreformatted", "MacroText"));

	/**
	 * A code paragraph: styled as one, or in a named paragraph style whose own font is mono (every
	 * run monospaced, none of them beyond the style).  Inline code is what a run adds to its
	 * paragraph style, so a paragraph whose style is already mono would get no code marks at all;
	 * this is the paragraph-level counterpart.  A mono <em>document default</em> does not count - a
	 * document set in Courier New throughout is prose, not one fence - which is why a named style
	 * is required.
	 *
	 * @since 17.2.0 (CR-005, the export enhancements of 2026-09-18)
	 */
	private static boolean isCodeParagraph(TextBlock block) {
		String style = block.getStyle();
		if (style == null) {
			return false;
		}
		if (CODE_STYLES.contains(style)) {
			return true;
		}
		if (!block.isMonospace()) {
			return false;
		}
		for (TextRun r : block.getRuns()) {
			if (r.getKind() == TextRun.Kind.TEXT && r.isMonospace()) return false;
		}
		return true;
	}

	private static boolean sameStory(TextBlock a, TextBlock b) {
		return a.getStory() == b.getStory() && Objects.equals(a.getNoteId(), b.getNoteId());
	}

	/** A code paragraph's text, unescaped; in {@code markup}, its revisions marked. */
	private String codeText(TextBlock block) {
		StringBuilder sb = new StringBuilder();
		List<TextRun> runs = block.getRuns();
		int i = 0;
		while (i < runs.size()) {
			TextRevision revision = runs.get(i).getRevision();
			int j = i;
			while (j < runs.size() && sameMarker(revision, runs.get(j).getRevision())) j++;
			if (revision != null) sb.append(open(revision));
			for (int k = i; k < j; k++) {
				TextRun r = runs.get(k);
				if (r.getKind() == TextRun.Kind.TEXT || r.getKind() == TextRun.Kind.FIELD_CODE) sb.append(r.getText());
			}
			if (revision != null) sb.append(close(revision));
			i = j;
		}
		return sb.toString();
	}

	private static int value(Integer i) {
		return i == null ? 0 : i;
	}

	private final Map<String, Integer> styleIndents = new HashMap<>();

	/** A paragraph style's own left indent, in twips: 0 where it has none, or there are no styles to ask. */
	private int styleIndentLeft(String styleId) {
		Integer cached = styleIndents.get(styleId);
		if (cached != null) return cached;
		int left = 0;
		try {
			// asking a package with no styles part for a resolver would add one to it
			if (pkg != null && pkg.getMainDocumentPart() != null
					&& pkg.getMainDocumentPart().getStyleDefinitionsPart(false) != null) {
				PPr pPr = new PPr();
				PPr.PStyle pStyle = new PPr.PStyle();
				pStyle.setVal(styleId);
				pPr.setPStyle(pStyle);
				PPr effective = pkg.getMainDocumentPart().getPropertyResolver().getEffectivePPr(pPr);
				if (effective != null && effective.getInd() != null) {
					java.math.BigInteger l = effective.getInd().getLeft() != null ? effective.getInd().getLeft()
							: effective.getInd().getStart();
					if (l != null) left = l.intValue();
				}
			}
		} catch (Exception e) {
			log.debug("No indent for style {}: {}", styleId, e.getMessage());
		}
		styleIndents.put(styleId, left);
		return left;
	}

	// ---------------------------------------------------------------- quotes

	private void appendToQuote(int depth, org.commonmark.node.Paragraph paragraph, Node container) {
		if (openQuote == null || depth != openQuoteDepth) {
			closeQuote();
			BlockQuote outer = new BlockQuote();
			BlockQuote inner = outer;
			for (int d = 1; d < depth; d++) {
				BlockQuote next = new BlockQuote();
				inner.appendChild(next);
				inner = next;
			}
			container.appendChild(outer);
			openQuote = inner;
			openQuoteDepth = depth;
		}
		openQuote.appendChild(paragraph);
	}

	private void closeQuote() {
		openQuote = null;
		openQuoteDepth = 0;
	}

	// ---------------------------------------------------------------- lists

	private void listParagraph(TextBlock block, TextNumbering numbering, Node container) {

		int ilvl = numbering.getLevel();
		String numId = numbering.getNumId();

		// close any deeper (or same-level-but-different-list) contexts
		while (openLists.size() > ilvl + 1) {
			openLists.pop();
		}
		if (openLists.size() == ilvl + 1 && !openLists.peek().numId.equals(numId)
				&& ilvl == 0) {
			openLists.pop(); // a different top-level list: restart
		}

		boolean tight = !Boolean.FALSE.equals(block.getContextualSpacing());

		// open missing levels (a level skipped over takes the paragraph's kind of list)
		while (openLists.size() < ilvl + 1) {
			int level = openLists.size();
			ListBlock listBlock = createListBlock(numbering, level == ilvl, tight);
			if (openLists.isEmpty()) {
				container.appendChild(listBlock);
			} else {
				OpenList parent = openLists.peek();
				if (parent.lastItem == null) {
					parent.lastItem = new ListItem();
					parent.node.appendChild(parent.lastItem);
				}
				parent.lastItem.appendChild(listBlock);
			}
			openLists.push(new OpenList(listBlock, numId));
		}

		OpenList target = openLists.peek();
		if (!tight) {
			target.node.setTight(false);
		}
		ListItem item = new ListItem();
		item.appendChild(inlineParagraph(block, false));
		target.node.appendChild(item);
		target.lastItem = item;
	}

	/** A list block; the start number only for the paragraph's own level, at the top level. */
	private ListBlock createListBlock(TextNumbering numbering, boolean own, boolean tight) {
		// a level the numbering definitions don't give is a bullet list
		boolean bullet = numbering.isBullet() || numbering.getFormat() == null;
		ListBlock listBlock;
		if (bullet) {
			listBlock = new BulletList();
		} else {
			OrderedList ordered = new OrderedList();
			if (own && numbering.getLevel() == 0 && numbering.getStart() > 1) {
				ordered.setMarkerStartNumber(numbering.getStart());
			}
			listBlock = ordered;
		}
		listBlock.setTight(tight);
		return listBlock;
	}

	private void closeLists() {
		openLists.clear();
	}

	// ---------------------------------------------------------------- tables

	/**
	 * GFM pipes: the header row is the table's {@code w:tblHeader} row, else its first (several
	 * {@code w:tblHeader} rows are one header, their cells' texts {@code  / } apart, as plain text's
	 * listed format joins them); block content in cells flattens to inline with &lt;br&gt;, a
	 * nested table's paragraphs included; vMerge/gridSpan degrade (top-left wins, empty cells
	 * elsewhere) - GFM has no spans.
	 *
	 * <p>The table style's formatting is left out on purpose (CR-030 decision 4): a header row the
	 * style makes bold is not written as **bold** (its being the GFM header says as much), and GFM
	 * has no table styling for the rest.</p>
	 */
	private void table(TextBlock table, Node container) {

		TableBlock tableBlock = new TableBlock();
		TableHead head = new TableHead();
		TableBody body = new TableBody();
		tableBlock.appendChild(head);

		List<TextRow> rows = table.getRows();
		int headerRows = 0;
		while (headerRows < rows.size() && rows.get(headerRows).isHeader()) headerRows++;
		if (headerRows == 0 && !rows.isEmpty()) headerRows = 1;

		if (headerRows > 0) {
			head.appendChild(headerRow(rows.subList(0, headerRows)));
		}
		for (int r = headerRows; r < rows.size(); r++) {
			TableRow row = new TableRow();
			for (TextCell cell : rows.get(r).getCells()) {
				row.appendChild(tableCell(cell, false));
				// gridSpan: pad with empty cells so columns stay aligned
				for (int s = 1; s < cell.getGridSpan(); s++) {
					row.appendChild(emptyCell(false));
				}
			}
			body.appendChild(row);
		}
		if (body.getFirstChild() != null) {
			tableBlock.appendChild(body);
		}
		container.appendChild(tableBlock);
	}

	/** The header: one row's cells, or several rows' cells by grid column, {@code  / } apart. */
	private TableRow headerRow(List<TextRow> rows) {
		TableRow row = new TableRow();
		if (rows.size() == 1) {
			for (TextCell cell : rows.get(0).getCells()) {
				row.appendChild(tableCell(cell, true));
				for (int s = 1; s < cell.getGridSpan(); s++) {
					row.appendChild(emptyCell(true));
				}
			}
			return row;
		}
		// each grid column's cells, from the top: a cell is in the column it starts in
		int width = 0;
		for (TextRow r : rows) {
			int w = 0;
			for (TextCell c : r.getCells()) w += Math.max(1, c.getGridSpan());
			width = Math.max(width, w);
		}
		List<List<TextCell>> columns = new ArrayList<>();
		for (int g = 0; g < width; g++) columns.add(new ArrayList<>());
		for (TextRow r : rows) {
			int g = 0;
			for (TextCell c : r.getCells()) {
				if (!c.isMergeContinue()) columns.get(g).add(c);
				g += Math.max(1, c.getGridSpan());
			}
		}
		for (List<TextCell> column : columns) {
			TableCell cell = emptyCell(true);
			// the column's alignment is its topmost header cell's, as with one header row
			TableCell.Alignment alignment = null;
			if (!column.isEmpty()) {
				List<TextBlock> first = paragraphsOf(column.get(0));
				if (!first.isEmpty()) alignment = cellAlignment(first.get(0));
			}
			InlineSink sink = new InlineSink(cell);
			tableHeaderDepth++;
			try {
				boolean first = true;
				for (TextCell c : column) {
					if (isEmpty(c)) continue;
					if (!first) sink.text(" / ", false, false, false);
					first = false;
					cellContent(c, cell, sink);
				}
			} finally {
				tableHeaderDepth--;
			}
			sink.flush();
			cell.setAlignment(alignment);
			row.appendChild(cell);
		}
		return row;
	}

	private static boolean isEmpty(TextCell cell) {
		for (TextBlock b : cell.getBlocks()) {
			if (b.getKind() == TextBlock.Kind.TABLE) {
				for (TextRow r : b.getRows()) for (TextCell c : r.getCells()) if (!isEmpty(c)) return false;
			} else if (b.getKind() == TextBlock.Kind.PARAGRAPH && !isEmpty(b)) {
				return false;
			}
		}
		return true;
	}

	private TableCell tableCell(TextCell textCell, boolean header) {

		TableCell cell = emptyCell(header);

		// vMerge continuation: the merge's top cell already holds the content
		if (textCell.isMergeContinue()) {
			return cell;
		}

		InlineSink sink = new InlineSink(cell);
		if (header) {
			tableHeaderDepth++;
		}
		try {
			cellContent(textCell, cell, sink);
		} finally {
			if (header) {
				tableHeaderDepth--;
			}
		}
		sink.flush();
		return cell;
	}

	/**
	 * A cell's paragraphs, a nested table's included, &lt;br&gt; apart (GFM cells are single-line);
	 * its empty paragraphs left out, as plain text leaves them out (Word ends every cell with a
	 * paragraph, so a nested table or a block-level control comes with an empty one after it).
	 *
	 * <p>A cell's alignment is its first paragraph's effective {@code w:jc}, empty or not; GFM aligns
	 * a column, from its header cell's (the renderer's delimiter row), so a body cell's is not seen.</p>
	 */
	private void cellContent(TextCell textCell, TableCell cell, InlineSink sink) {
		boolean first = true;
		List<TextBlock> paragraphs = paragraphsOf(textCell);
		if (cell.getAlignment() == null && !paragraphs.isEmpty()) {
			cell.setAlignment(cellAlignment(paragraphs.get(0)));
		}
		for (TextBlock b : paragraphs) {
			if (isEmpty(b)) continue;
			if (!first) {
				sink.node(htmlBr());
			}
			first = false;
			runs(b, sink);
		}
	}

	private static List<TextBlock> paragraphsOf(TextCell cell) {
		List<TextBlock> out = new ArrayList<>();
		for (TextBlock b : cell.getBlocks()) {
			if (b.getKind() == TextBlock.Kind.TABLE) {
				for (TextRow r : b.getRows()) for (TextCell c : r.getCells()) out.addAll(paragraphsOf(c));
			} else if (b.getKind() == TextBlock.Kind.PARAGRAPH) {
				out.add(b);
			}
		}
		return out;
	}

	private TableCell emptyCell(boolean header) {
		TableCell cell = new TableCell();
		cell.setHeader(header);
		return cell;
	}

	private static HtmlInline htmlBr() {
		return raw("<br>");
	}

	private static HtmlInline raw(String literal) {
		HtmlInline node = new HtmlInline();
		node.setLiteral(literal);
		return node;
	}

	private static TableCell.Alignment cellAlignment(TextBlock block) {
		if (block.getAlignment() == null) {
			return null;
		}
		switch (block.getAlignment()) {
		case "center":
			return TableCell.Alignment.CENTER;
		case "right":
		case "end":
			return TableCell.Alignment.RIGHT;
		default:
			return null; // LEFT is GFM's default
		}
	}

	// ---------------------------------------------------------------- charts

	/**
	 * A chart: its note (CR-047: {@code [chart: clustered column; value axis: Revenue]}), its title
	 * and its data as a table (CR-035 phase 3), after the paragraph it is in, the categories down
	 * and the series across, numbers formatted by their format codes
	 * ({@link org.docx4j.model.chart.ChartDescription}, {@link org.docx4j.model.chart.ChartTable}).
	 */
	private void chart(TextBlock block, Node container) {
		if (block.getChart() != null) {
			org.commonmark.node.Paragraph note = new org.commonmark.node.Paragraph();
			note.appendChild(new Text(block.getChart().getNote()));
			container.appendChild(note);
		}
		if (block.getChartTitle() != null && !block.getChartTitle().isEmpty()) {
			org.commonmark.node.Paragraph title = new org.commonmark.node.Paragraph();
			title.appendChild(new Text(block.getChartTitle()));
			container.appendChild(title);
		}
		if (block.getChartHeader().isEmpty()) return;
		TableBlock table = new TableBlock();
		TableHead head = new TableHead();
		head.appendChild(chartRow(block.getChartHeader(), true, block.getChartHeader()));
		table.appendChild(head);
		if (!block.getChartRows().isEmpty()) {
			TableBody body = new TableBody();
			for (List<String> r : block.getChartRows()) body.appendChild(chartRow(r, false, block.getChartHeader()));
			table.appendChild(body);
		}
		container.appendChild(table);
	}

	private static TableRow chartRow(List<String> cells, boolean header, List<String> headings) {
		TableRow row = new TableRow();
		int labels = 0;
		while (labels < headings.size() && headings.get(labels).isEmpty()) labels++;
		for (int i = 0; i < cells.size(); i++) {
			TableCell cell = new TableCell();
			cell.setHeader(header);
			if (i >= Math.max(labels, 1)) cell.setAlignment(TableCell.Alignment.RIGHT);
			if (!cells.get(i).isEmpty()) cell.appendChild(new Text(cells.get(i)));
			row.appendChild(cell);
		}
		return row;
	}

	// ---------------------------------------------------------------- notes

	private static boolean isNote(TextBlock block) {
		return block.getNoteType() != null
				&& (block.getStory() == TextBlock.Story.FOOTNOTE || block.getStory() == TextBlock.Story.ENDNOTE
						|| block.getStory() == TextBlock.Story.TEXT_BOX);
	}

	private static boolean sameNote(TextBlock a, TextBlock b) {
		return isNote(b) && a.getNoteType().equals(b.getNoteType()) && Objects.equals(a.getNoteId(), b.getNoteId());
	}

	/**
	 * A note's label: a footnote's number ({@code [^1]}), an endnote's prefixed {@code e}
	 * ({@code [^e1]}), since the two are numbered apart (WD REQ-503); a note whose reference
	 * carries a custom mark has no number, so its {@code w:id} labels it ({@code [^f-5]},
	 * {@code [^e-5]}).
	 */
	private static String noteLabel(String type, int number, String id) {
		boolean foot = TextRun.FOOTNOTE.equals(type);
		if (number > 0) return foot ? Integer.toString(number) : "e" + number;
		return (foot ? "f-" : "e-") + id;
	}

	/** Drop the leading space left behind by the note's own mark. */
	private static void trimLeadingMarkerSpace(FootnoteDefinition definition) {
		Node paragraph = definition.getFirstChild();
		if (paragraph instanceof org.commonmark.node.Paragraph
				&& paragraph.getFirstChild() instanceof Text) {
			Text text = (Text) paragraph.getFirstChild();
			if (text.getLiteral() != null && text.getLiteral().startsWith(" ")) {
				text.setLiteral(text.getLiteral().substring(1));
			}
		}
	}

	// ---------------------------------------------------------------- inlines

	/** Accumulates same-formatted text, wrapping it on flush. */
	private static final class InlineSink {
		final Node parent;
		final StringBuilder buf = new StringBuilder();
		boolean bold;
		boolean italic;
		boolean strike;
		InlineSink(Node parent) {
			this.parent = parent;
		}
		void text(String s, boolean b, boolean i, boolean st) {
			if ((b != bold || i != italic || st != strike) && buf.length() > 0) {
				flush();
			}
			bold = b;
			italic = i;
			strike = st;
			buf.append(s);
		}
		void node(Node n) {
			flush();
			parent.appendChild(n);
		}
		void flush() {
			if (buf.length() == 0) {
				return;
			}
			Node n = new Text(buf.toString());
			// strong inside emphasis: the renderer then emits ***both*** rather
			// than switching the inner marker to _underscores_
			if (bold) {
				StrongEmphasis strong = new StrongEmphasis();
				strong.appendChild(n);
				n = strong;
			}
			if (italic) {
				Emphasis em = new Emphasis();
				em.appendChild(n);
				n = em;
			}
			if (strike) {
				Strikethrough st = new Strikethrough("~~");
				st.appendChild(n);
				n = st;
			}
			parent.appendChild(n);
			buf.setLength(0);
		}
	}

	private void inlines(TextBlock block, Node parent, boolean withListLabel) {
		InlineSink sink = new InlineSink(parent);
		// under list_labels: true, the label the numbering model computes leads a paragraph that
		// isn't written as a list item (a numbered heading's "1.2")
		if (withListLabel && block.getListLabel() != null && !block.getListLabel().trim().isEmpty()) {
			sink.text(block.getListLabel().replace('\t', ' '), false, false, false);
		}
		runs(block, sink);
		sink.flush();
	}

	/** A block's runs, in {@code markup} each revision's between its CriticMarkup marks (REQ-703, REQ-702). */
	private void runs(TextBlock block, InlineSink sink) {
		List<TextRun> runs = block.getRuns();
		int i = 0;
		while (i < runs.size()) {
			TextRevision revision = runs.get(i).getRevision();
			int j = i;
			while (j < runs.size() && sameMarker(revision, runs.get(j).getRevision())) j++;
			if (revision != null) sink.node(raw(open(revision)));
			linked(runs.subList(i, j), sink);
			if (revision != null) sink.node(raw(close(revision)));
			i = j;
		}
	}

	/** Runs, those with the same link target in one link. */
	private void linked(List<TextRun> runs, InlineSink sink) {
		int i = 0;
		while (i < runs.size()) {
			String target = runs.get(i).getLink();
			int j = i;
			while (j < runs.size() && Objects.equals(target, runs.get(j).getLink())) j++;
			if (target == null) {
				for (int k = i; k < j; k++) run(runs.get(k), sink);
			} else {
				Link link = new Link(target, null);
				InlineSink linkSink = new InlineSink(link);
				for (int k = i; k < j; k++) run(runs.get(k), linkSink);
				linkSink.flush();
				sink.node(link);
			}
			i = j;
		}
	}

	private void run(TextRun run, InlineSink sink) {
		switch (run.getKind()) {
		case TEXT:
			text(run, sink);
			return;
		case FIELD_CODE:
		case LIST_LABEL:
			sink.text(run.getText(), false, false, false);
			return;
		case NOTE_REFERENCE:
			if (textOptions.isNotes() && textOptions.isNoteReferences()) {
				sink.node(new FootnoteReference(noteLabel(run.getNoteType(), run.getNoteNumber(), run.getNoteId())));
			}
			return;
		case COMMENT_REFERENCE: {
			TextComment comment = result.comments().get(run.getCommentId());
			// a reply is written after its parent, never at its own reference (REQ-504)
			if (comment == null || comment.getParentId() != null && result.comments().containsKey(comment.getParentId())) return;
			comment(comment, sink);
			for (TextComment reply : comment.getReplies()) comment(reply, sink);
			return;
		}
		case IMAGE:
			image(run, sink);
			return;
		case PAGE_MARKER:
			if (textOptions.isPageMarkers()) sink.text("[page " + run.getPageNumber() + "]", false, false, false);
			return;
		default:
			// a note's own mark (its definition's label carries it), an object (no text)
		}
	}

	private void text(TextRun run, InlineSink sink) {
		String value = run.getText();
		if (value.isEmpty()) return;
		if (run.isMath()) {
			String latex = latex(run);
			if (!latex.isEmpty()) sink.node(new org.docx4j.markdown.math.InlineMath(latex, false));
			return;
		}
		boolean bold = run.isBold() && tableHeaderDepth == 0; // header-row bold is convention, not markup
		String[] lines = value.split("\n", -1);
		for (int l = 0; l < lines.length; l++) {
			if (l > 0) sink.node(new HardLineBreak());
			if (lines[l].isEmpty()) continue;
			if (run.isMonospace()) {
				sink.node(new Code(lines[l]));
			} else {
				sink.text(lines[l], bold, run.isItalic(), run.isStrike());
			}
		}
	}

	private void comment(TextComment comment, InlineSink sink) {
		sink.node(raw("{>>" + detail(comment.getAuthor(), comment.getDate())));
		sink.text(comment.getText(), false, false, false);
		sink.node(raw("<<}"));
	}

	/** Runs under one marker: the same type, and the same author and date where the detail shows them. */
	private boolean sameMarker(TextRevision a, TextRevision b) {
		if (a == null || b == null) return a == b;
		if (a.getType() != b.getType()) return false;
		switch (textOptions.getMarkupDetail()) {
		case AUTHOR: return Objects.equals(a.getAuthor(), b.getAuthor());
		case AUTHOR_DATE: return Objects.equals(a.getAuthor(), b.getAuthor()) && Objects.equals(a.getDate(), b.getDate());
		default: return true;
		}
	}

	private String open(TextRevision revision) {
		return (revision.getType() == TextRevision.Type.INSERTION ? "{++" : "{--") + detail(revision.getAuthor(), revision.getDate());
	}

	private static String close(TextRevision revision) {
		return revision.getType() == TextRevision.Type.INSERTION ? "++}" : "--}";
	}

	private String detail(String author, String date) {
		switch (textOptions.getMarkupDetail()) {
		case AUTHOR:
			return (author == null ? "" : author) + ": ";
		case AUTHOR_DATE:
			return (author == null ? "" : author) + ", " + (date == null ? "" : date) + ": ";
		default:
			return "";
		}
	}

	// ---------------------------------------------------------------- images

	/** A picture with the describer's text (REQ-703, REQ-514); one that isn't in an image part is dropped. */
	private void image(TextRun run, InlineSink sink) {
		Part part = run.getImage() == null ? null : run.getImage().getPart();
		if (!(part instanceof BinaryPartAbstractImage)) {
			log.debug("Dropping a picture with no image part");
			return;
		}
		String destination;
		try {
			destination = imageDestination((BinaryPartAbstractImage) part);
		} catch (Exception e) {
			log.warn("Could not extract image; dropping it", e);
			return;
		}
		ImageDescription description = run.getDescription();
		org.commonmark.node.Image image = new org.commonmark.node.Image(destination, null);
		image.appendChild(new Text(description == null ? "" : description.getText()));
		sink.node(image);
	}

	/** Data URI by default; a file in imageDirPath (with a relative link) when set. */
	private String imageDestination(BinaryPartAbstractImage imagePart) throws Exception {

		byte[] bytes = imagePart.getBytes();
		String contentType = imagePart.getContentType();

		if (options.getImageDirPath() == null) {
			return "data:" + contentType + ";base64,"
					+ java.util.Base64.getEncoder().encodeToString(bytes);
		}

		String ext = extensionFor(contentType, imagePart);
		String fileName = "image" + (++imageCounter) + "." + ext;
		java.io.File dir = new java.io.File(options.getImageDirPath());
		dir.mkdirs();
		try (java.io.FileOutputStream out = new java.io.FileOutputStream(new java.io.File(dir, fileName))) {
			out.write(bytes);
		}
		return (options.getImageTargetUri() == null || options.getImageTargetUri().isEmpty())
				? fileName
				: options.getImageTargetUri() + "/" + fileName;
	}

	private static String extensionFor(String contentType, BinaryPartAbstractImage part) {
		switch (contentType == null ? "" : contentType) {
		case "image/png": return "png";
		case "image/jpeg": return "jpg";
		case "image/gif": return "gif";
		case "image/bmp": return "bmp";
		case "image/tiff": return "tif";
		case "image/svg+xml": return "svg";
		case "image/x-emf": return "emf";
		case "image/x-wmf": return "wmf";
		default:
			String name = part.getPartName().getName();
			int dot = name.lastIndexOf('.');
			return (dot >= 0) ? name.substring(dot + 1) : "bin";
		}
	}
}
