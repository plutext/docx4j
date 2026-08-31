package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Br;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * Phase 1 (import core) mapping assertions, one construct per test.
 */
public class MarkdownImportTest {

	private WordprocessingMLPackage convert(String md) throws Exception {
		return new MarkdownImporter().createPackage(md);
	}

	private WordprocessingMLPackage convert(String md, MarkdownImportOptions options) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(options).importToMainDocumentPart(md, pkg);
		return pkg;
	}

	private static List<Object> content(WordprocessingMLPackage pkg) {
		return pkg.getMainDocumentPart().getContent();
	}

	private static P p(WordprocessingMLPackage pkg, int i) {
		return (P) content(pkg).get(i);
	}

	private static String pStyle(P p) {
		return (p.getPPr() == null || p.getPPr().getPStyle() == null)
				? null : p.getPPr().getPStyle().getVal();
	}

	private static R firstRun(P p) {
		for (Object o : p.getContent()) {
			if (o instanceof R) {
				return (R) o;
			}
		}
		return null;
	}

	/** All the w:t text in the paragraph, including inside hyperlinks. */
	private static String text(Object o) {
		StringBuilder sb = new StringBuilder();
		org.docx4j.TraversalUtil.visit(o, new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object child) {
				child = org.docx4j.XmlUtils.unwrap(child);
				if (child instanceof Text) {
					sb.append(((Text) child).getValue());
				}
				return null;
			}
		});
		return sb.toString();
	}

	private static Style style(WordprocessingMLPackage pkg, String styleId) {
		for (Style s : pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.getJaxbElement().getStyle()) {
			if (styleId.equals(s.getStyleId())) {
				return s;
			}
		}
		return null;
	}

	// ------------------------------------------------------------- headings

	@Test
	public void headings() throws Exception {
		WordprocessingMLPackage pkg = convert("# One\n\n### Three\n");
		assertEquals("Heading1", pStyle(p(pkg, 0)));
		assertEquals("One", text(p(pkg, 0)));
		assertEquals("Heading3", pStyle(p(pkg, 1)));
		// the styles were activated in the styles part
		assertNotNull(style(pkg, "Heading1"));
		assertNotNull(style(pkg, "Heading3"));
	}

	// ------------------------------------------------------------- emphasis

	@Test
	public void emphasisAndStrong() throws Exception {
		WordprocessingMLPackage pkg = convert("plain *italic* **bold** ***both***\n");
		P p = p(pkg, 0);
		assertEquals("plain italic bold both", text(p));

		R plain = (R) p.getContent().get(0);
		assertNull(plain.getRPr());

		R italic = (R) p.getContent().get(1);
		assertNull(italic.getRPr().getB());
		assertTrue(italic.getRPr().getI().isVal());

		R bold = (R) p.getContent().get(3);
		assertTrue(bold.getRPr().getB().isVal());
		assertNull(bold.getRPr().getI());

		R both = (R) p.getContent().get(5);
		assertTrue(both.getRPr().getB().isVal());
		assertTrue(both.getRPr().getI().isVal());
	}

	// ------------------------------------------------------------- code

	@Test
	public void inlineCode() throws Exception {
		WordprocessingMLPackage pkg = convert("some `mono` here\n");
		P p = p(pkg, 0);
		R code = (R) p.getContent().get(1);
		assertEquals("CodeChar", code.getRPr().getRStyle().getVal());
		// the character style was created
		Style codeChar = style(pkg, "CodeChar");
		assertNotNull(codeChar);
		assertEquals("character", codeChar.getType());
	}

	@Test
	public void codeBlockSingleParagraph() throws Exception {
		WordprocessingMLPackage pkg = convert("```\nline1\n  line2\n```\n");
		assertEquals(1, content(pkg).size());
		P p = p(pkg, 0);
		assertEquals("SourceCode", pStyle(p));
		assertEquals("line1  line2", text(p));
		// one w:br between the two lines
		int brs = 0;
		for (Object o : p.getContent()) {
			for (Object rc : ((R) o).getContent()) {
				if (org.docx4j.XmlUtils.unwrap(rc) instanceof Br) {
					brs++;
				}
			}
		}
		assertEquals(1, brs);
		// indentation preserved
		R indented = (R) p.getContent().get(2);
		Text t = (Text) org.docx4j.XmlUtils.unwrap(indented.getContent().get(0));
		assertEquals("  line2", t.getValue());
		assertEquals("preserve", t.getSpace());

		Style sourceCode = style(pkg, "SourceCode");
		assertNotNull(sourceCode);
		assertEquals("paragraph", sourceCode.getType());
	}

	@Test
	public void codeBlockParagraphPerLine() throws Exception {
		WordprocessingMLPackage pkg = convert("```\nline1\nline2\n```\n",
				new MarkdownImportOptions()
						.setCodeBlockShape(MarkdownImportOptions.CodeBlockShape.PARAGRAPH_PER_LINE));
		assertEquals(2, content(pkg).size());
		assertEquals("SourceCode", pStyle(p(pkg, 0)));
		assertEquals("line1", text(p(pkg, 0)));
		assertEquals("line2", text(p(pkg, 1)));
	}

	// ------------------------------------------------------------- quotes

	@Test
	public void blockQuote() throws Exception {
		WordprocessingMLPackage pkg = convert("> quoted\n>\n> > deeper\n");
		assertEquals("Quote", pStyle(p(pkg, 0)));
		assertEquals("quoted", text(p(pkg, 0)));
		assertNotNull(style(pkg, "Quote"));
		// nested quote: same style, extra indent
		P deeper = p(pkg, 1);
		assertEquals("Quote", pStyle(deeper));
		assertEquals(BigInteger.valueOf(720), deeper.getPPr().getInd().getLeft());
	}

	// ------------------------------------------------------------- lists

	private static BigInteger numIdOf(P p) {
		return p.getPPr().getNumPr().getNumId().getVal();
	}

	private static BigInteger ilvlOf(P p) {
		return p.getPPr().getNumPr().getIlvl().getVal();
	}

	private Lvl lvl(WordprocessingMLPackage pkg, BigInteger numId, int ilvl) {
		NumberingDefinitionsPart ndp = pkg.getMainDocumentPart().getNumberingDefinitionsPart();
		Numbering numbering = ndp.getJaxbElement();
		BigInteger abstractNumId = null;
		for (Numbering.Num num : numbering.getNum()) {
			if (num.getNumId().equals(numId)) {
				abstractNumId = num.getAbstractNumId().getVal();
			}
		}
		assertNotNull(abstractNumId);
		for (Numbering.AbstractNum an : numbering.getAbstractNum()) {
			if (an.getAbstractNumId().equals(abstractNumId)) {
				return an.getLvl().get(ilvl);
			}
		}
		return null;
	}

	@Test
	public void bulletListRealNumbering() throws Exception {
		WordprocessingMLPackage pkg = convert("- one\n- two\n  - nested\n");
		P first = p(pkg, 0);
		assertEquals("ListParagraph", pStyle(first));
		assertEquals(BigInteger.ZERO, ilvlOf(first));
		P nested = p(pkg, 2);
		assertEquals(BigInteger.ONE, ilvlOf(nested));
		assertEquals(numIdOf(first), numIdOf(nested));

		assertEquals(NumberFormat.BULLET, lvl(pkg, numIdOf(first), 0).getNumFmt().getVal());
		assertNotNull(style(pkg, "ListParagraph"));
	}

	@Test
	public void orderedListsRestartAndStart() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"1. a\n2. b\n\nbetween\n\n3. c\n4. d\n");
		P firstList = p(pkg, 0);
		P secondList = p(pkg, 3);
		assertEquals(NumberFormat.DECIMAL, lvl(pkg, numIdOf(firstList), 0).getNumFmt().getVal());
		// separate top-level ordered lists get separate nums, so numbering restarts
		assertNotEquals(numIdOf(firstList), numIdOf(secondList));
		// and the second list's start value (3) is honoured
		assertEquals(BigInteger.valueOf(1), lvl(pkg, numIdOf(firstList), 0).getStart().getVal());
		assertEquals(BigInteger.valueOf(3), lvl(pkg, numIdOf(secondList), 0).getStart().getVal());
	}

	@Test
	public void mixedNestingSignature() throws Exception {
		WordprocessingMLPackage pkg = convert("1. a\n   - bullet inside ordered\n");
		P outer = p(pkg, 0);
		assertEquals(NumberFormat.DECIMAL, lvl(pkg, numIdOf(outer), 0).getNumFmt().getVal());
		assertEquals(NumberFormat.BULLET, lvl(pkg, numIdOf(outer), 1).getNumFmt().getVal());
	}

	@Test
	public void tightVsLooseSpacing() throws Exception {
		// tight list: contextualSpacing true; loose list (blank line between items): false
		WordprocessingMLPackage tight = convert("- a\n- b\n");
		assertTrue(p(tight, 0).getPPr().getContextualSpacing().isVal());

		WordprocessingMLPackage loose = convert("- a\n\n- b\n");
		assertFalse(p(loose, 0).getPPr().getContextualSpacing().isVal());
	}

	@Test
	public void looseItemSecondParagraph() throws Exception {
		WordprocessingMLPackage pkg = convert("- first para\n\n  second para\n");
		P first = p(pkg, 0);
		assertNotNull(first.getPPr().getNumPr());
		P second = p(pkg, 1);
		// follow-on paragraph in the item: no number, but indented to match
		assertNull(second.getPPr().getNumPr());
		assertEquals(BigInteger.valueOf(720), second.getPPr().getInd().getLeft());
	}

	// ------------------------------------------------------------- links

	@Test
	public void externalHyperlink() throws Exception {
		WordprocessingMLPackage pkg = convert("See [docx4j](https://www.docx4java.org/) now\n");
		P p = p(pkg, 0);
		P.Hyperlink h = null;
		for (Object o : p.getContent()) {
			if (o instanceof P.Hyperlink) {
				h = (P.Hyperlink) o;
			}
		}
		assertNotNull(h);
		assertEquals("docx4j", text(h));
		R linkRun = (R) h.getContent().get(0);
		assertEquals("Hyperlink", linkRun.getRPr().getRStyle().getVal());
		assertNotNull(style(pkg, "Hyperlink"));

		Relationship rel = pkg.getMainDocumentPart().getRelationshipsPart()
				.getRelationshipByID(h.getId());
		assertEquals("https://www.docx4java.org/", rel.getTarget());
		assertEquals("External", rel.getTargetMode());
	}

	@Test
	public void imageBecomesHyperlinkedAltText() throws Exception {
		WordprocessingMLPackage pkg = convert("![alt text](https://example.com/i.png)\n");
		P p = p(pkg, 0);
		P.Hyperlink h = (P.Hyperlink) p.getContent().get(0);
		assertEquals("alt text", text(h));
		Relationship rel = pkg.getMainDocumentPart().getRelationshipsPart()
				.getRelationshipByID(h.getId());
		assertEquals("https://example.com/i.png", rel.getTarget());
	}

	// ------------------------------------------------------------- breaks

	@Test
	public void thematicBreak() throws Exception {
		WordprocessingMLPackage pkg = convert("above\n\n---\n\nbelow\n");
		P hr = p(pkg, 1);
		assertNotNull(hr.getPPr().getPBdr().getBottom());
		assertTrue(hr.getContent().isEmpty());
	}

	@Test
	public void hardAndSoftLineBreaks() throws Exception {
		WordprocessingMLPackage pkg = convert("hard\\\nbreak\n\nsoft\nbreak\n");
		P hard = p(pkg, 0);
		boolean sawBr = false;
		for (Object o : hard.getContent()) {
			for (Object rc : ((R) o).getContent()) {
				if (org.docx4j.XmlUtils.unwrap(rc) instanceof Br) {
					sawBr = true;
				}
			}
		}
		assertTrue(sawBr);
		assertEquals("soft break", text(p(pkg, 1)));
	}

	// ------------------------------------------------------------- html policy

	@Test
	public void htmlDroppedByDefault() throws Exception {
		WordprocessingMLPackage pkg = convert("before\n\n<div>block</div>\n\nafter <b>inline</b> end\n");
		assertEquals(2, content(pkg).size());
		assertEquals("before", text(p(pkg, 0)));
		assertEquals("after inline end", text(p(pkg, 1)));
	}

	@Test
	public void htmlLiteralPolicy() throws Exception {
		WordprocessingMLPackage pkg = convert("<div>block</div>\n\nafter <b>x</b>\n",
				new MarkdownImportOptions().setHtmlPolicy(MarkdownImportOptions.HtmlPolicy.LITERAL));
		assertEquals("<div>block</div>", text(p(pkg, 0)));
		assertEquals("after <b>x</b>", text(p(pkg, 1)));
	}

	// ------------------------------------------------------------- styles template

	@Test
	public void stylesTemplateNotClobbered() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		// the "template" defines its own Quote (not among the default part's styles)
		Style custom = org.docx4j.jaxb.Context.getWmlObjectFactory().createStyle();
		custom.setType("paragraph");
		custom.setStyleId("Quote");
		Style.Name name = org.docx4j.jaxb.Context.getWmlObjectFactory().createStyleName();
		name.setVal("My House Quote");
		custom.setName(name);
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(custom);

		new MarkdownImporter().importToMainDocumentPart("> quoted\n", pkg);

		int count = 0;
		for (Style s : pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.getJaxbElement().getStyle()) {
			if ("Quote".equals(s.getStyleId())) {
				count++;
			}
		}
		assertEquals(1, count); // not duplicated
		assertEquals("My House Quote", style(pkg, "Quote").getName().getVal());
	}

	// ------------------------------------------------------------- output validity

	@Test
	public void outputMarshalsAndSaves() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"# Title\n\nPara **b** *i* `c` [l](https://example.org/)\n\n"
				+ "- one\n  1. nested\n\n> quote\n\n```\ncode\n```\n\n---\n");
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		pkg.save(baos); // marshals everything; fails on broken content
		assertTrue(baos.size() > 0);
	}

}
