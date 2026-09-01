package org.docx4j.markdown;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.Link;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.STFldCharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walks the JAXB content tree of the main document part, building a
 * commonmark AST (which MarkdownRenderer then serializes — so markdown
 * escaping is the reference implementation's job, not ours).
 *
 * <p>Detection choices (see the CR): headings via effective outlineLvl, not
 * style names; bold/italic/code from effective rPr relative to the paragraph
 * style's baseline (so a Heading's inherent bold doesn't become **markers**);
 * lists via the numbering model.  Content controls contribute their content;
 * fields their cached result text; headers/footers are out of scope.</p>
 */
class WmlToMarkdown {

	private static final Logger log = LoggerFactory.getLogger(WmlToMarkdown.class);

	private static final int TWIPS_PER_LEVEL = 720;

	private static final Set<String> MONO_FONTS = new HashSet<>(Arrays.asList(
			"consolas", "courier new", "courier", "lucida console", "monaco",
			"menlo", "dejavu sans mono", "source code pro", "fira code"));

	private final WordprocessingMLPackage pkg;
	private final MarkdownExportOptions options;
	private final MainDocumentPart mdp;
	private PropertyResolver resolver;
	private NumberingDefinitionsPart ndp;

	// open-list state (spans consecutive numbered paragraphs)
	private static final class OpenList {
		final ListBlock node;
		final BigInteger numId;
		ListItem lastItem;
		OpenList(ListBlock node, BigInteger numId) {
			this.node = node;
			this.numId = numId;
		}
	}
	private final Deque<OpenList> openLists = new ArrayDeque<>();

	// open-quote state (spans consecutive Quote paragraphs)
	private BlockQuote openQuote;
	private int openQuoteDepth;

	// field state: emit only cached results, skip instructions
	private int fieldDepth;
	private int awaitingSeparate;

	WmlToMarkdown(WordprocessingMLPackage pkg, MarkdownExportOptions options) {
		this.pkg = pkg;
		this.options = options;
		this.mdp = pkg.getMainDocumentPart();
	}

	Document convert() throws Docx4JException {
		resolver = mdp.getPropertyResolver();
		ndp = mdp.getNumberingDefinitionsPart();

		Document document = new Document();
		processBlocks(mdp.getContent(), document);
		closeLists();
		closeQuote();
		return document;
	}

	// ---------------------------------------------------------------- blocks

	private void processBlocks(List<Object> blocks, Node container) throws Docx4JException {

		for (int i = 0; i < blocks.size(); i++) {
			Object block = XmlUtils.unwrap(blocks.get(i));

			if (block instanceof P) {
				i = processParagraph(blocks, i, container);
			} else if (block instanceof SdtElement) {
				// content controls contribute their content
				closeLists();
				closeQuote();
				processBlocks(((SdtElement) block).getSdtContent().getContent(), container);
			} else if (block instanceof org.docx4j.wml.Tbl) {
				closeLists();
				closeQuote();
				log.warn("Table export is phase 4; dropping table");
			} else {
				log.debug("Dropping unmapped block {}", block.getClass().getSimpleName());
			}
		}
	}

	/** @return the index of the last paragraph consumed (code blocks span several) */
	private int processParagraph(List<Object> blocks, int index, Node container)
			throws Docx4JException {

		P p = (P) XmlUtils.unwrap(blocks.get(index));
		PPr directPPr = p.getPPr();
		PPr effPPr = resolver.getEffectivePPr(directPPr);
		String pStyleId = (directPPr != null && directPPr.getPStyle() != null)
				? directPPr.getPStyle().getVal() : null;

		// thematic break: empty paragraph carrying a bottom border
		if (isThematicBreak(p, directPPr)) {
			closeLists();
			closeQuote();
			ThematicBreak thematicBreak = new ThematicBreak();
			thematicBreak.setLiteral("---"); // else the renderer picks its own idiom
			container.appendChild(thematicBreak);
			return index;
		}

		// code block: SourceCode-styled paragraphs, consecutive ones merged
		if (ImportStyles.SOURCE_CODE.equals(pStyleId)) {
			closeLists();
			closeQuote();
			StringBuilder literal = new StringBuilder();
			int last = index;
			for (int j = index; j < blocks.size(); j++) {
				Object o = XmlUtils.unwrap(blocks.get(j));
				if (!(o instanceof P) || !hasPStyle((P) o, ImportStyles.SOURCE_CODE)) {
					break;
				}
				appendCodeLines((P) o, literal);
				last = j;
			}
			FencedCodeBlock code = new FencedCodeBlock();
			code.setLiteral(literal.toString());
			container.appendChild(code);
			return last;
		}

		// heading via effective outlineLvl (0..5 -> #..######); checked BEFORE
		// numbering, since built-in Heading styles can carry legacy numPr
		Integer outlineLvl = outlineLevel(effPPr);
		if (outlineLvl != null && outlineLvl <= 5) {
			closeLists();
			closeQuote();
			Heading heading = new Heading();
			heading.setLevel(outlineLvl + 1);
			processInlines(p.getContent(), heading, p.getPPr());
			container.appendChild(heading);
			return index;
		}

		// list paragraph?
		PPrBase.NumPr numPr = (effPPr != null) ? effPPr.getNumPr() : null;
		BigInteger numId = (numPr != null && numPr.getNumId() != null)
				? numPr.getNumId().getVal() : null;
		if (numId != null && !BigInteger.ZERO.equals(numId)) {
			closeQuote();
			int ilvl = (numPr.getIlvl() != null) ? numPr.getIlvl().getVal().intValue() : 0;
			listParagraph(p, effPPr, numId, ilvl, container);
			return index;
		}

		// follow-on paragraph inside a loose list item (indented, unnumbered)
		if (!openLists.isEmpty() && ImportStyles.LIST_PARAGRAPH.equals(pStyleId)) {
			OpenList top = openLists.peek();
			if (top.lastItem != null) {
				top.node.setTight(false);
				top.lastItem.appendChild(inlineParagraph(p));
				return index;
			}
		}

		closeLists();

		// block quote (nesting depth from the direct indent)
		if (ImportStyles.QUOTE.equals(pStyleId) || "IntenseQuote".equals(pStyleId)) {
			int depth = 1;
			if (directPPr != null && directPPr.getInd() != null
					&& directPPr.getInd().getLeft() != null) {
				depth += directPPr.getInd().getLeft().intValue() / TWIPS_PER_LEVEL;
			}
			appendToQuote(depth, inlineParagraph(p), container);
			return index;
		}

		closeQuote();

		org.commonmark.node.Paragraph paragraph = inlineParagraph(p);
		if (paragraph.getFirstChild() != null) {
			container.appendChild(paragraph);
		} // markdown has no empty paragraphs; drop them
		return index;
	}

	private org.commonmark.node.Paragraph inlineParagraph(P p) throws Docx4JException {
		org.commonmark.node.Paragraph paragraph = new org.commonmark.node.Paragraph();
		// NB the DIRECT pPr: getEffectiveRPr resolves the paragraph style via pPr.getPStyle()
		processInlines(p.getContent(), paragraph, p.getPPr());
		return paragraph;
	}

	private boolean isThematicBreak(P p, PPr directPPr) {
		if (directPPr == null || directPPr.getPBdr() == null
				|| directPPr.getPBdr().getBottom() == null) {
			return false;
		}
		return textOf(p).isEmpty();
	}

	private static boolean hasPStyle(P p, String styleId) {
		return p.getPPr() != null && p.getPPr().getPStyle() != null
				&& styleId.equals(p.getPPr().getPStyle().getVal());
	}

	private void appendCodeLines(P p, StringBuilder literal) {
		StringBuilder line = new StringBuilder();
		for (Object o : p.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof R) {
				for (Object rc : ((R) u).getContent()) {
					Object ru = XmlUtils.unwrap(rc);
					if (ru instanceof org.docx4j.wml.Text) {
						line.append(((org.docx4j.wml.Text) ru).getValue());
					} else if (ru instanceof Br) {
						literal.append(line).append('\n');
						line.setLength(0);
					}
				}
			}
		}
		literal.append(line).append('\n');
	}

	private Integer outlineLevel(PPr effPPr) {
		if (effPPr == null || effPPr.getOutlineLvl() == null
				|| effPPr.getOutlineLvl().getVal() == null) {
			return null;
		}
		return effPPr.getOutlineLvl().getVal().intValue();
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

	private void listParagraph(P p, PPr effPPr, BigInteger numId, int ilvl, Node container)
			throws Docx4JException {

		// close any deeper (or same-level-but-different-list) contexts
		while (openLists.size() > ilvl + 1) {
			openLists.pop();
		}
		if (openLists.size() == ilvl + 1 && !openLists.peek().numId.equals(numId)
				&& ilvl == 0) {
			openLists.pop(); // a different top-level list: restart
		}

		boolean tight = isTight(effPPr);

		// open missing levels
		while (openLists.size() < ilvl + 1) {
			int level = openLists.size();
			ListBlock listBlock = createListBlock(numId, level, tight);
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
		item.appendChild(inlineParagraph(p));
		target.node.appendChild(item);
		target.lastItem = item;
	}

	private ListBlock createListBlock(BigInteger numId, int level, boolean tight) {
		ListLevel listLevel = listLevel(numId, level);
		boolean bullet = (listLevel == null) || listLevel.IsBullet();
		ListBlock listBlock;
		if (bullet) {
			listBlock = new BulletList();
		} else {
			OrderedList ordered = new OrderedList();
			// NB ListLevel.getStartValue() is w:start minus one (counter semantics)
			if (level == 0 && listLevel.getStartValue() != null
					&& listLevel.getStartValue().intValue() + 1 > 1) {
				ordered.setMarkerStartNumber(listLevel.getStartValue().intValue() + 1);
			}
			listBlock = ordered;
		}
		listBlock.setTight(tight);
		return listBlock;
	}

	private ListLevel listLevel(BigInteger numId, int ilvl) {
		if (ndp == null) {
			return null;
		}
		ListNumberingDefinition lnd = ndp.getInstanceListDefinitions().get(numId.toString());
		if (lnd == null || lnd.getAbstractListDefinition() == null) {
			return null;
		}
		return lnd.getAbstractListDefinition().getListLevels().get(Integer.toString(ilvl));
	}

	private boolean isTight(PPr effPPr) {
		// our import writes contextualSpacing true for tight, false for loose;
		// absent -> assume tight (most Word lists read best tight)
		if (effPPr == null || effPPr.getContextualSpacing() == null) {
			return true;
		}
		return effPPr.getContextualSpacing().isVal();
	}

	private void closeLists() {
		openLists.clear();
	}

	// ---------------------------------------------------------------- inlines

	/** Accumulates same-formatted text, wrapping it on flush. */
	private static final class InlineSink {
		final Node parent;
		final StringBuilder buf = new StringBuilder();
		boolean bold;
		boolean italic;
		InlineSink(Node parent) {
			this.parent = parent;
		}
		void text(String s, boolean b, boolean i) {
			if ((b != bold || i != italic) && buf.length() > 0) {
				flush();
			}
			bold = b;
			italic = i;
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
			parent.appendChild(n);
			buf.setLength(0);
		}
	}

	private void processInlines(List<Object> content, Node parent, PPr directPPr)
			throws Docx4JException {

		// the paragraph style's own formatting is the baseline: only
		// formatting beyond it becomes markdown markers
		RPr baseline = resolver.getEffectiveRPr(null, directPPr);

		InlineSink sink = new InlineSink(parent);
		processInlines(content, sink, directPPr, baseline);
		sink.flush();
	}

	private void processInlines(List<Object> content, InlineSink sink, PPr directPPr, RPr baseline)
			throws Docx4JException {

		for (Object o : content) {
			Object u = XmlUtils.unwrap(o);

			if (u instanceof R) {
				run((R) u, sink, directPPr, baseline);
			} else if (u instanceof P.Hyperlink) {
				P.Hyperlink h = (P.Hyperlink) u;
				String target = hyperlinkTarget(h);
				if (target == null) {
					processInlines(h.getContent(), sink, directPPr, baseline);
				} else {
					Link link = new Link(target, null);
					InlineSink linkSink = new InlineSink(link);
					processInlines(h.getContent(), linkSink, directPPr, baseline);
					linkSink.flush();
					sink.node(link);
				}
			} else if (u instanceof CTSimpleField) {
				// a field's content is its cached result
				processInlines(((CTSimpleField) u).getContent(), sink, directPPr, baseline);
			} else if (u instanceof SdtElement) {
				processInlines(((SdtElement) u).getSdtContent().getContent(), sink, directPPr, baseline);
			} else {
				log.debug("Dropping unmapped inline {}", u.getClass().getSimpleName());
			}
		}
	}

	private void run(R r, InlineSink sink, PPr directPPr, RPr baseline) throws Docx4JException {

		RPr effRPr = resolver.getEffectiveRPr(r.getRPr(), directPPr);

		boolean code = isCode(r.getRPr(), effRPr, baseline);
		boolean bold = isOn(effRPr == null ? null : effRPr.getB())
				&& !isOn(baseline == null ? null : baseline.getB());
		boolean italic = isOn(effRPr == null ? null : effRPr.getI())
				&& !isOn(baseline == null ? null : baseline.getI());

		for (Object rc : r.getContent()) {
			Object u = XmlUtils.unwrap(rc);

			if (u instanceof FldChar) {
				STFldCharType type = ((FldChar) u).getFldCharType();
				if (type == STFldCharType.BEGIN) {
					fieldDepth++;
					awaitingSeparate++;
				} else if (type == STFldCharType.SEPARATE) {
					if (awaitingSeparate > 0) {
						awaitingSeparate--;
					}
				} else if (type == STFldCharType.END) {
					if (fieldDepth > 0) {
						fieldDepth--;
					}
					if (awaitingSeparate > fieldDepth) {
						awaitingSeparate = fieldDepth;
					}
				}
			} else if (awaitingSeparate > 0) {
				// between a field's begin and separate: instruction, not result
				continue;
			} else if (u instanceof org.docx4j.wml.Text) {
				String value = ((org.docx4j.wml.Text) u).getValue();
				if (value != null && !value.isEmpty()) {
					if (code) {
						sink.node(new Code(value));
					} else {
						sink.text(value, bold, italic);
					}
				}
			} else if (u instanceof Br) {
				sink.node(new HardLineBreak());
			} else if (u instanceof R.Tab) {
				sink.text("\t", bold, italic);
			}
			// instrText and other run content dropped
		}
	}

	private boolean isCode(RPr directRPr, RPr effRPr, RPr baseline) {
		if (directRPr != null && directRPr.getRStyle() != null
				&& ImportStyles.CODE_CHAR.equals(directRPr.getRStyle().getVal())) {
			return true;
		}
		return isMono(effRPr) && !isMono(baseline);
	}

	private static boolean isMono(RPr rPr) {
		if (rPr == null || rPr.getRFonts() == null || rPr.getRFonts().getAscii() == null) {
			return false;
		}
		return MONO_FONTS.contains(rPr.getRFonts().getAscii().toLowerCase(Locale.ROOT));
	}

	private String hyperlinkTarget(P.Hyperlink h) {
		if (h.getId() != null && mdp.getRelationshipsPart() != null) {
			Relationship rel = mdp.getRelationshipsPart().getRelationshipByID(h.getId());
			if (rel != null) {
				return rel.getTarget();
			}
		}
		if (h.getAnchor() != null) {
			return "#" + h.getAnchor();
		}
		return null;
	}

	private static boolean isOn(BooleanDefaultTrue b) {
		return b != null && b.isVal();
	}

	private String textOf(P p) {
		StringBuilder sb = new StringBuilder();
		for (Object o : p.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof R) {
				for (Object rc : ((R) u).getContent()) {
					Object ru = XmlUtils.unwrap(rc);
					if (ru instanceof org.docx4j.wml.Text) {
						sb.append(((org.docx4j.wml.Text) ru).getValue());
					}
				}
			}
		}
		return sb.toString();
	}

}
