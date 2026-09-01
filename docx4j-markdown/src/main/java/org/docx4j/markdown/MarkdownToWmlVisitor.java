package org.docx4j.markdown;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.ext.footnotes.InlineFootnote;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walks the commonmark AST, building wml block content (no HTML detour):
 * CommonMark core plus the GFM extensions (tables, strikethrough, task list
 * items, footnotes) and YAML front matter (skipped here; the importer maps it
 * to core document properties).
 */
class MarkdownToWmlVisitor extends AbstractVisitor {

	private static final Logger log = LoggerFactory.getLogger(MarkdownToWmlVisitor.class);

	private static final int TWIPS_PER_LEVEL = 720;

	private final ObjectFactory factory = Context.getWmlObjectFactory();
	private final WordprocessingMLPackage pkg;
	private final MainDocumentPart mdp;
	private final MarkdownImportOptions options;
	private final ImportStyles styles;
	private final ImportNumbering numbering;
	private final ImportFootnotes footnotes;

	private List<Object> results = new ArrayList<>();

	// footnote definitions by label (collected by the importer up front)
	private java.util.Map<String, FootnoteDefinition> footnoteDefinitions = java.util.Collections.emptyMap();
	private final java.util.Map<String, BigInteger> footnoteIdsByLabel = new java.util.HashMap<>();

	// inline state
	private P currentP;
	private P.Hyperlink currentHyperlink;
	private int boldDepth;
	private int italicDepth;
	private int strikeDepth;
	private String pendingTaskMarker; // glyph to prepend to the item's first paragraph

	// block context
	private int quoteDepth;

	private static final class ListCtx {
		final BigInteger numId;
		final int ilvl;
		final boolean tight;
		boolean itemPending; // the next paragraph is the item's first: it carries the numPr
		ListCtx(BigInteger numId, int ilvl, boolean tight) {
			this.numId = numId;
			this.ilvl = ilvl;
			this.tight = tight;
		}
	}
	private Deque<ListCtx> listStack = new ArrayDeque<>();

	MarkdownToWmlVisitor(WordprocessingMLPackage pkg, MarkdownImportOptions options)
			throws Docx4JException {
		this.pkg = pkg;
		this.mdp = pkg.getMainDocumentPart();
		this.options = options;
		this.styles = new ImportStyles(pkg);
		this.numbering = new ImportNumbering(pkg);
		this.footnotes = new ImportFootnotes(pkg);
	}

	List<Object> getResults() {
		return results;
	}

	void setFootnoteDefinitions(java.util.Map<String, FootnoteDefinition> definitions) {
		this.footnoteDefinitions = definitions;
	}

	// ---------------------------------------------------------------- blocks

	@Override
	public void visit(Heading heading) {
		int level = Math.min(heading.getLevel(), 6);
		String styleId = ImportStyles.HEADING_PREFIX + level;
		styles.ensureKnown(styleId);
		newParagraph(pPrWithStyle(styleId));
		visitChildren(heading);
		endParagraph();
	}

	@Override
	public void visit(Paragraph paragraph) {
		// a paragraph that IS a single $$...$$ (single-line form parses as
		// an inline node with a display hint) gets display treatment
		if (paragraph.getFirstChild() instanceof org.docx4j.markdown.math.InlineMath
				&& paragraph.getFirstChild() == paragraph.getLastChild()
				&& ((org.docx4j.markdown.math.InlineMath) paragraph.getFirstChild()).isDisplayHint()
				&& pendingTaskMarker == null) {
			displayMath(((org.docx4j.markdown.math.InlineMath) paragraph.getFirstChild()).getLiteral());
			return;
		}
		newParagraph(contextPPr());
		if (pendingTaskMarker != null) {
			addText(pendingTaskMarker);
			pendingTaskMarker = null;
		}
		visitChildren(paragraph);
		endParagraph();
	}

	@Override
	public void visit(BlockQuote blockQuote) {
		quoteDepth++;
		visitChildren(blockQuote);
		quoteDepth--;
	}

	@Override
	public void visit(BulletList bulletList) {
		visitList(bulletList);
	}

	@Override
	public void visit(OrderedList orderedList) {
		visitList(orderedList);
	}

	private void visitList(ListBlock list) {
		BigInteger numId;
		int ilvl;
		if (listStack.isEmpty()) {
			numId = numbering.numIdFor(list);
			ilvl = 0;
		} else {
			ListCtx parent = listStack.peek();
			numId = parent.numId;
			ilvl = Math.min(parent.ilvl + 1, 8);
		}
		listStack.push(new ListCtx(numId, ilvl, list.isTight()));
		visitChildren(list);
		listStack.pop();
	}

	@Override
	public void visit(ListItem listItem) {
		if (!listStack.isEmpty()) {
			listStack.peek().itemPending = true;
		}
		visitChildren(listItem);
	}

	@Override
	public void visit(FencedCodeBlock fencedCodeBlock) {
		codeBlock(fencedCodeBlock.getLiteral());
	}

	@Override
	public void visit(IndentedCodeBlock indentedCodeBlock) {
		codeBlock(indentedCodeBlock.getLiteral());
	}

	private void codeBlock(String literal) {
		try {
			styles.ensureSourceCode();
		} catch (Docx4JException e) {
			log.warn("Could not create SourceCode style", e);
		}
		String[] lines = literal.replace("\r", "").split("\n", -1);
		int count = lines.length;
		if (count > 0 && lines[count - 1].isEmpty()) {
			count--; // the literal's trailing newline is not an empty last line
		}
		if (options.getCodeBlockShape() == MarkdownImportOptions.CodeBlockShape.PARAGRAPH_PER_LINE) {
			for (int i = 0; i < count; i++) {
				newParagraph(pPrWithStyle(ImportStyles.SOURCE_CODE));
				addCodeLine(lines[i]);
				endParagraph();
			}
		} else {
			newParagraph(pPrWithStyle(ImportStyles.SOURCE_CODE));
			for (int i = 0; i < count; i++) {
				if (i > 0) {
					R r = factory.createR();
					r.getContent().add(factory.createBr());
					currentP.getContent().add(r);
				}
				addCodeLine(lines[i]);
			}
			endParagraph();
		}
	}

	private void addCodeLine(String line) {
		R r = factory.createR();
		org.docx4j.wml.Text t = factory.createText();
		t.setValue(line);
		t.setSpace("preserve"); // indentation matters in code
		r.getContent().add(t);
		currentP.getContent().add(r);
	}

	@Override
	public void visit(ThematicBreak thematicBreak) {
		PPr pPr = factory.createPPr();
		PPrBase.PBdr pBdr = factory.createPPrBasePBdr();
		CTBorder border = factory.createCTBorder();
		border.setVal(STBorder.SINGLE);
		border.setSz(BigInteger.valueOf(6));
		border.setSpace(BigInteger.valueOf(1));
		border.setColor("auto");
		pBdr.setBottom(border);
		pPr.setPBdr(pBdr);
		newParagraph(pPr);
		endParagraph();
	}

	@Override
	public void visit(HtmlBlock htmlBlock) {
		switch (options.getHtmlPolicy()) {
		case LITERAL:
			newParagraph(contextPPr());
			String[] lines = htmlBlock.getLiteral().replace("\r", "").split("\n");
			for (int i = 0; i < lines.length; i++) {
				if (i > 0) {
					R r = factory.createR();
					r.getContent().add(factory.createBr());
					currentP.getContent().add(r);
				}
				addText(lines[i]);
			}
			endParagraph();
			break;
		case IMPORT_XHTML:
			List<Object> converted = XhtmlFallback.tryConvert(pkg, htmlBlock.getLiteral());
			if (converted != null) {
				results.addAll(converted);
			}
			break;
		case DROP:
		default:
			// dropped
		}
	}

	@Override
	public void visit(HtmlInline htmlInline) {
		switch (options.getHtmlPolicy()) {
		case LITERAL:
			addText(htmlInline.getLiteral());
			break;
		case IMPORT_XHTML:
			// inline HTML arrives tag-by-tag (not as well-formed fragments),
			// so it can't be routed through ImportXHTML; dropped
			log.debug("IMPORT_XHTML applies to HTML blocks only; dropping inline HTML");
			break;
		case DROP:
		default:
			// dropped
		}
	}

	// ---------------------------------------------------------------- inlines

	@Override
	public void visit(Text text) {
		addText(text.getLiteral());
	}

	@Override
	public void visit(Emphasis emphasis) {
		italicDepth++;
		visitChildren(emphasis);
		italicDepth--;
	}

	@Override
	public void visit(org.commonmark.node.StrongEmphasis strongEmphasis) {
		boldDepth++;
		visitChildren(strongEmphasis);
		boldDepth--;
	}

	@Override
	public void visit(Code code) {
		try {
			styles.ensureCodeChar();
		} catch (Docx4JException e) {
			log.warn("Could not create CodeChar style", e);
		}
		R r = factory.createR();
		RPr rPr = runRPr();
		if (rPr == null) {
			rPr = factory.createRPr();
		}
		RStyle rStyle = factory.createRStyle();
		rStyle.setVal(ImportStyles.CODE_CHAR);
		rPr.setRStyle(rStyle);
		r.setRPr(rPr);
		addTextTo(r, code.getLiteral());
		runTarget().add(r);
	}

	@Override
	public void visit(SoftLineBreak softLineBreak) {
		addText(" ");
	}

	@Override
	public void visit(HardLineBreak hardLineBreak) {
		R r = factory.createR();
		Br br = factory.createBr();
		r.getContent().add(br);
		runTarget().add(r);
	}

	@Override
	public void visit(Link link) {
		inlineHyperlink(link, link.getDestination());
	}

	/**
	 * The image handler decides how to realise the image (the default embeds
	 * data URIs and local files, and never fetches remote URLs); if it
	 * declines, the image degrades to its alt text hyperlinked to the
	 * destination.
	 */
	@Override
	public void visit(Image image) {
		MarkdownImageHandler handler = options.getImageHandler();
		if (handler != null && image.getDestination() != null && !image.getDestination().isEmpty()) {
			Object runContent = handler.toRunContent(
					image.getDestination(), image.getTitle(), altTextOf(image), pkg);
			if (runContent != null) {
				R r = factory.createR();
				r.getContent().add(runContent);
				runTarget().add(r);
				return;
			}
		}
		inlineHyperlink(image, image.getDestination());
	}

	private static String altTextOf(Image image) {
		StringBuilder sb = new StringBuilder();
		appendLiterals(image, sb);
		return sb.toString();
	}

	private static void appendLiterals(org.commonmark.node.Node node, StringBuilder sb) {
		for (org.commonmark.node.Node c = node.getFirstChild(); c != null; c = c.getNext()) {
			if (c instanceof org.commonmark.node.Text) {
				sb.append(((org.commonmark.node.Text) c).getLiteral());
			} else if (c instanceof Code) {
				sb.append(((Code) c).getLiteral());
			} else {
				appendLiterals(c, sb);
			}
		}
	}

	private void inlineHyperlink(org.commonmark.node.Node parent, String destination) {

		if (currentHyperlink != null // markdown doesn't nest links; be safe
				|| destination == null || destination.isEmpty()) {
			visitChildren(parent);
			return;
		}

		P.Hyperlink hyperlink;
		try {
			org.docx4j.relationships.ObjectFactory relFactory =
					new org.docx4j.relationships.ObjectFactory();
			org.docx4j.relationships.Relationship rel = relFactory.createRelationship();
			rel.setType(Namespaces.HYPERLINK);
			rel.setTarget(destination);
			rel.setTargetMode("External");
			mdp.getRelationshipsPart().addRelationship(rel);

			hyperlink = factory.createPHyperlink();
			hyperlink.setId(rel.getId());
		} catch (Exception e) {
			log.warn("Could not create hyperlink to {}", destination, e);
			visitChildren(parent);
			return;
		}

		styles.ensureKnown(ImportStyles.HYPERLINK);
		ensureParagraph();
		currentP.getContent().add(hyperlink);
		currentHyperlink = hyperlink;
		visitChildren(parent);
		currentHyperlink = null;
	}

	// ------------------------------------------------------------ extensions

	@Override
	public void visit(CustomBlock customBlock) {
		if (customBlock instanceof TableBlock) {
			table((TableBlock) customBlock);
		} else if (customBlock instanceof org.docx4j.markdown.math.DisplayMath) {
			displayMath((org.docx4j.markdown.math.DisplayMath) customBlock);
		} else if (customBlock instanceof FootnoteDefinition) {
			// realised on demand when a FootnoteReference points at it
		} else if (customBlock instanceof YamlFrontMatterBlock) {
			// mapped to core document properties by the importer
		} else {
			visitChildren(customBlock);
		}
	}

	@Override
	public void visit(CustomNode customNode) {
		if (customNode instanceof Strikethrough) {
			strikeDepth++;
			visitChildren(customNode);
			strikeDepth--;
		} else if (customNode instanceof TaskListItemMarker) {
			// held until the item's first paragraph exists (the marker precedes it)
			pendingTaskMarker = ((TaskListItemMarker) customNode).isChecked() ? "☒ " : "☐ ";
		} else if (customNode instanceof org.docx4j.markdown.math.InlineMath) {
			inlineMath((org.docx4j.markdown.math.InlineMath) customNode);
		} else if (customNode instanceof FootnoteReference) {
			footnoteReference((FootnoteReference) customNode);
		} else if (customNode instanceof InlineFootnote) {
			inlineFootnote((InlineFootnote) customNode);
		} else if (customNode instanceof YamlFrontMatterNode) {
			// mapped to core document properties by the importer
		} else {
			visitChildren(customNode);
		}
	}

	// ------------------------------------------------------------ math

	/**
	 * CR-markdown-math: the supported LaTeX subset becomes native OMML
	 * (unless MathPolicy.LITERAL); anything outside it degrades — loudly,
	 * via the issue listener — to its literal source in the CodeChar style,
	 * delimiters preserved.
	 */
	private void inlineMath(org.docx4j.markdown.math.InlineMath math) {
		if (options.getMathPolicy() == MarkdownImportOptions.MathPolicy.OMML) {
			try {
				org.docx4j.math.CTOMath oMath =
						new org.docx4j.markdown.math.LatexToOmml().convertInline(math.getLiteral());
				runTarget().add(new org.docx4j.math.ObjectFactory().createOMath(oMath));
				return;
			} catch (org.docx4j.markdown.math.LatexMathException e) {
				issue("inline math", math.getLiteral(),
						"unsupported LaTeX: " + e.getMessage() + "; emitted literal source");
			}
		}
		String delimiter = math.isDisplayHint() ? "$$" : "$";
		addLiteralMathRun(delimiter + math.getLiteral().replace('\n', ' ') + delimiter);
	}

	private void displayMath(org.docx4j.markdown.math.DisplayMath math) {
		displayMath(math.getLiteral());
	}

	private void displayMath(String literal) {
		if (options.getMathPolicy() == MarkdownImportOptions.MathPolicy.OMML) {
			try {
				org.docx4j.math.CTOMathPara oMathPara =
						new org.docx4j.markdown.math.LatexToOmml().convertDisplay(literal);
				newParagraph(contextPPr());
				currentP.getContent().add(new org.docx4j.math.ObjectFactory().createOMathPara(oMathPara));
				endParagraph();
				return;
			} catch (org.docx4j.markdown.math.LatexMathException e) {
				issue("display math", literal,
						"unsupported LaTeX: " + e.getMessage() + "; emitted literal source");
			}
		}
		literalDisplayMath(literal);
	}

	private void literalDisplayMath(String literal) {
		newParagraph(null);
		String[] lines = ("$$\n" + literal + "\n$$").split("\n", -1);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				R br = factory.createR();
				br.getContent().add(factory.createBr());
				currentP.getContent().add(br);
			}
			addLiteralMathRun(lines[i]);
		}
		endParagraph();
	}

	private void addLiteralMathRun(String text) {
		try {
			styles.ensureCodeChar();
		} catch (Docx4JException e) {
			log.warn("Could not create CodeChar style", e);
		}
		R r = factory.createR();
		RPr rPr = runRPr();
		if (rPr == null) {
			rPr = factory.createRPr();
		}
		RStyle rStyle = factory.createRStyle();
		rStyle.setVal(ImportStyles.CODE_CHAR);
		rPr.setRStyle(rStyle);
		r.setRPr(rPr);
		org.docx4j.wml.Text t = factory.createText();
		t.setValue(text);
		t.setSpace("preserve");
		r.getContent().add(t);
		runTarget().add(r);
	}

	private void issue(String construct, String source, String reason) {
		MarkdownImportIssueListener listener = options.getIssueListener();
		if (listener != null) {
			listener.onIssue(new MarkdownImportIssue(construct, source, reason));
		}
	}

	// ------------------------------------------------------------ tables

	private void table(TableBlock tableBlock) {

		try {
			styles.ensureTableGrid();
		} catch (Docx4JException e) {
			log.warn("Could not create TableGrid style", e);
		}

		Tbl tbl = factory.createTbl();
		TblPr tblPr = factory.createTblPr();
		CTTblPrBase.TblStyle tblStyle = factory.createCTTblPrBaseTblStyle();
		tblStyle.setVal(ImportStyles.TABLE_GRID);
		tblPr.setTblStyle(tblStyle);
		TblWidth tblW = factory.createTblWidth();
		tblW.setW(BigInteger.ZERO);
		tblW.setType("auto");
		tblPr.setTblW(tblW);
		tbl.setTblPr(tblPr);

		int cols = 0;
		for (org.commonmark.node.Node section = tableBlock.getFirstChild();
				section != null; section = section.getNext()) {
			boolean header = section instanceof TableHead;
			for (org.commonmark.node.Node rowNode = section.getFirstChild();
					rowNode != null; rowNode = rowNode.getNext()) {
				if (!(rowNode instanceof TableRow)) {
					continue;
				}
				Tr tr = factory.createTr();
				if (header) {
					TrPr trPr = factory.createTrPr();
					trPr.getCnfStyleOrDivIdOrGridBefore().add(
							factory.createCTTrPrBaseTblHeader(factory.createBooleanDefaultTrue()));
					tr.setTrPr(trPr);
				}
				int rowCols = 0;
				for (org.commonmark.node.Node cellNode = rowNode.getFirstChild();
						cellNode != null; cellNode = cellNode.getNext()) {
					if (!(cellNode instanceof TableCell)) {
						continue;
					}
					tr.getContent().add(tableCell((TableCell) cellNode, header));
					rowCols++;
				}
				cols = Math.max(cols, rowCols);
				tbl.getContent().add(tr);
			}
		}

		// equal column widths: Word would tolerate widthless gridCols, but
		// the FO/HTML table writers expect w:w (and Word autofits anyway)
		TblGrid tblGrid = factory.createTblGrid();
		int columnWidth = (cols > 0) ? writableWidthTwips() / cols : 0;
		for (int i = 0; i < cols; i++) {
			TblGridCol gridCol = factory.createTblGridCol();
			gridCol.setW(BigInteger.valueOf(columnWidth));
			tblGrid.getGridCol().add(gridCol);
		}
		tbl.setTblGrid(tblGrid);

		results.add(tbl);
	}

	/** The section's page width minus margins; A4 with 1in margins if unstated. */
	private int writableWidthTwips() {
		try {
			org.docx4j.wml.SectPr sectPr = mdp.getContents().getBody().getSectPr();
			if (sectPr != null && sectPr.getPgSz() != null && sectPr.getPgSz().getW() != null) {
				long width = sectPr.getPgSz().getW().longValue();
				long left = 1440;
				long right = 1440;
				if (sectPr.getPgMar() != null) {
					if (sectPr.getPgMar().getLeft() != null) {
						left = sectPr.getPgMar().getLeft().longValue();
					}
					if (sectPr.getPgMar().getRight() != null) {
						right = sectPr.getPgMar().getRight().longValue();
					}
				}
				long writable = width - left - right;
				if (writable > 0) {
					return (int) writable;
				}
			}
		} catch (Docx4JException e) {
			log.warn("Could not read section properties for table width", e);
		}
		return 9026; // A4 minus 1in margins
	}

	private Tc tableCell(TableCell cell, boolean header) {

		Tc tc = factory.createTc();
		P cellP = factory.createP();

		TableCell.Alignment alignment = cell.getAlignment();
		if (alignment != null && alignment != TableCell.Alignment.LEFT) {
			PPr pPr = factory.createPPr();
			Jc jc = factory.createJc();
			jc.setVal(alignment == TableCell.Alignment.CENTER
					? JcEnumeration.CENTER : JcEnumeration.RIGHT);
			pPr.setJc(jc);
			cellP.setPPr(pPr);
		}

		// cell content is inline-only in GFM; build it into the cell's paragraph
		P savedP = currentP;
		P.Hyperlink savedHyperlink = currentHyperlink;
		currentP = cellP;
		currentHyperlink = null;
		if (header) {
			boldDepth++; // TableGrid has no header formatting of its own
		}
		visitChildren(cell);
		if (header) {
			boldDepth--;
		}
		currentP = savedP;
		currentHyperlink = savedHyperlink;

		tc.getContent().add(cellP);
		return tc;
	}

	// ------------------------------------------------------------ footnotes

	private void footnoteReference(FootnoteReference ref) {

		String label = ref.getLabel();
		BigInteger id = footnoteIdsByLabel.get(label);
		if (id == null) {
			FootnoteDefinition definition = footnoteDefinitions.get(label);
			if (definition == null) {
				log.warn("No footnote definition for label {}", label);
				addText("[^" + label + "]");
				return;
			}
			id = realiseFootnote(() -> visitChildren(definition));
			if (id == null) {
				addText("[^" + label + "]");
				return;
			}
			footnoteIdsByLabel.put(label, id);
		}
		addFootnoteRefRun(id);
	}

	private void inlineFootnote(InlineFootnote inlineFootnote) {
		// inline content ^[like this] becomes a one-paragraph footnote
		BigInteger id = realiseFootnote(() -> {
			newParagraph(null);
			visitChildren(inlineFootnote);
			endParagraph();
		});
		if (id != null) {
			addFootnoteRefRun(id);
		}
	}

	private BigInteger realiseFootnote(Runnable body) {
		styles.ensureKnown(ImportFootnotes.FOOTNOTE_TEXT);
		styles.ensureKnown(ImportFootnotes.FOOTNOTE_REFERENCE);
		List<Object> blocks = collectBlocks(body);
		try {
			return footnotes.add(blocks);
		} catch (Docx4JException e) {
			log.warn("Could not add footnote", e);
			return null;
		}
	}

	private void addFootnoteRefRun(BigInteger id) {
		R r = factory.createR();
		RPr rPr = runRPr();
		if (rPr == null) {
			rPr = factory.createRPr();
		}
		RStyle rStyle = factory.createRStyle();
		rStyle.setVal(ImportFootnotes.FOOTNOTE_REFERENCE);
		rPr.setRStyle(rStyle);
		r.setRPr(rPr);
		CTFtnEdnRef ref = factory.createCTFtnEdnRef();
		ref.setId(id);
		r.getContent().add(factory.createRFootnoteReference(ref));
		runTarget().add(r);
	}

	/**
	 * Run the given body with fresh output and block context (as for content
	 * destined for another part, eg a footnote), returning what it built.
	 */
	private List<Object> collectBlocks(Runnable body) {
		List<Object> savedResults = results;
		P savedP = currentP;
		P.Hyperlink savedHyperlink = currentHyperlink;
		int savedQuoteDepth = quoteDepth;
		Deque<ListCtx> savedListStack = listStack;

		results = new ArrayList<>();
		currentP = null;
		currentHyperlink = null;
		quoteDepth = 0;
		listStack = new ArrayDeque<>();

		body.run();

		List<Object> out = results;
		results = savedResults;
		currentP = savedP;
		currentHyperlink = savedHyperlink;
		quoteDepth = savedQuoteDepth;
		listStack = savedListStack;
		return out;
	}

	// ---------------------------------------------------------------- helpers

	private void newParagraph(PPr pPr) {
		currentP = factory.createP();
		if (pPr != null) {
			currentP.setPPr(pPr);
		}
		results.add(currentP);
	}

	private void endParagraph() {
		currentP = null;
	}

	private void ensureParagraph() {
		if (currentP == null) {
			newParagraph(contextPPr());
		}
	}

	private PPr pPrWithStyle(String styleId) {
		PPr pPr = factory.createPPr();
		PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
		pStyle.setVal(styleId);
		pPr.setPStyle(pStyle);
		return pPr;
	}

	/**
	 * pPr for a plain paragraph in the current block context (quote, list
	 * item, or none).  Quote wins over list: block quotes inside list items
	 * are styled as quotes and don't carry numbering.
	 */
	private PPr contextPPr() {

		if (quoteDepth > 0) {
			styles.ensureKnown(ImportStyles.QUOTE);
			PPr pPr = pPrWithStyle(ImportStyles.QUOTE);
			if (quoteDepth > 1) {
				PPrBase.Ind ind = factory.createPPrBaseInd();
				ind.setLeft(BigInteger.valueOf((long) TWIPS_PER_LEVEL * (quoteDepth - 1)));
				pPr.setInd(ind);
			}
			return pPr;
		}

		if (!listStack.isEmpty()) {
			ListCtx ctx = listStack.peek();
			styles.ensureKnown(ImportStyles.LIST_PARAGRAPH);
			PPr pPr = pPrWithStyle(ImportStyles.LIST_PARAGRAPH);
			if (ctx.itemPending) {
				ctx.itemPending = false;
				PPrBase.NumPr numPr = factory.createPPrBaseNumPr();
				PPrBase.NumPr.Ilvl ilvl = factory.createPPrBaseNumPrIlvl();
				ilvl.setVal(BigInteger.valueOf(ctx.ilvl));
				numPr.setIlvl(ilvl);
				PPrBase.NumPr.NumId numId = factory.createPPrBaseNumPrNumId();
				numId.setVal(ctx.numId);
				numPr.setNumId(numId);
				pPr.setNumPr(numPr);
			} else {
				// follow-on paragraph within the item: indent to match, no number
				PPrBase.Ind ind = factory.createPPrBaseInd();
				ind.setLeft(BigInteger.valueOf((long) TWIPS_PER_LEVEL * (ctx.ilvl + 1)));
				pPr.setInd(ind);
			}
			// tight lists suppress inter-item spacing; loose lists keep it
			BooleanDefaultTrue contextualSpacing = factory.createBooleanDefaultTrue();
			contextualSpacing.setVal(ctx.tight);
			pPr.setContextualSpacing(contextualSpacing);
			return pPr;
		}

		return null; // Normal (the document default)
	}

	private void addText(String s) {
		if (s == null || s.isEmpty()) {
			return;
		}
		R r = factory.createR();
		RPr rPr = runRPr();
		if (rPr != null) {
			r.setRPr(rPr);
		}
		addTextTo(r, s);
		runTarget().add(r);
	}

	private void addTextTo(R r, String s) {
		org.docx4j.wml.Text t = factory.createText();
		t.setValue(s);
		if (s.startsWith(" ") || s.endsWith(" ")) {
			t.setSpace("preserve");
		}
		r.getContent().add(t);
	}

	/** rPr for the current inline formatting state, or null if none applies. */
	private RPr runRPr() {
		if (boldDepth == 0 && italicDepth == 0 && strikeDepth == 0 && currentHyperlink == null) {
			return null;
		}
		RPr rPr = factory.createRPr();
		if (currentHyperlink != null) {
			RStyle rStyle = factory.createRStyle();
			rStyle.setVal(ImportStyles.HYPERLINK);
			rPr.setRStyle(rStyle);
		}
		if (boldDepth > 0) {
			rPr.setB(factory.createBooleanDefaultTrue());
		}
		if (italicDepth > 0) {
			rPr.setI(factory.createBooleanDefaultTrue());
		}
		if (strikeDepth > 0) {
			rPr.setStrike(factory.createBooleanDefaultTrue());
		}
		return rPr;
	}

	private List<Object> runTarget() {
		if (currentHyperlink != null) {
			return currentHyperlink.getContent();
		}
		ensureParagraph();
		return currentP.getContent();
	}

}
