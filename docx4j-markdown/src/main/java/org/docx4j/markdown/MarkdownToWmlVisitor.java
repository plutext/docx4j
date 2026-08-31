package org.docx4j.markdown;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
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
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walks the commonmark AST, building wml block content (no HTML detour).
 * CommonMark core constructs only; the GFM extensions are phase 2.
 */
class MarkdownToWmlVisitor extends AbstractVisitor {

	private static final Logger log = LoggerFactory.getLogger(MarkdownToWmlVisitor.class);

	private static final int TWIPS_PER_LEVEL = 720;

	private final ObjectFactory factory = Context.getWmlObjectFactory();
	private final MainDocumentPart mdp;
	private final MarkdownImportOptions options;
	private final ImportStyles styles;
	private final ImportNumbering numbering;

	private final List<Object> results = new ArrayList<>();

	// inline state
	private P currentP;
	private P.Hyperlink currentHyperlink;
	private int boldDepth;
	private int italicDepth;

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
	private final Deque<ListCtx> listStack = new ArrayDeque<>();

	MarkdownToWmlVisitor(WordprocessingMLPackage pkg, MarkdownImportOptions options)
			throws Docx4JException {
		this.mdp = pkg.getMainDocumentPart();
		this.options = options;
		this.styles = new ImportStyles(pkg);
		this.numbering = new ImportNumbering(pkg);
	}

	List<Object> getResults() {
		return results;
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
		newParagraph(contextPPr());
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
			log.warn("HtmlPolicy.IMPORT_XHTML is not implemented yet (phase 2); dropping HTML block");
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
			log.warn("HtmlPolicy.IMPORT_XHTML is not implemented yet (phase 2); dropping inline HTML");
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
	 * Phase 1: an image becomes its alt text, hyperlinked to the image
	 * location (remote images are never fetched).  The pluggable image
	 * handler which can embed images is phase 2.
	 */
	@Override
	public void visit(Image image) {
		inlineHyperlink(image, image.getDestination());
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
		if (boldDepth == 0 && italicDepth == 0 && currentHyperlink == null) {
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
