package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * Phase 3 export-specific assertions: detection is by effective properties
 * (outlineLvl, rPr), not by our own import's conventions; fields contribute
 * their cached result; content controls their content.
 */
public class MarkdownExportTest {

	private static String export(WordprocessingMLPackage pkg) throws Exception {
		return new MarkdownExporter().export(pkg);
	}

	@Test
	public void headingViaOutlineLvlNotStyleName() throws Exception {
		// a custom style with outlineLvl but a non-Heading name must still export as #
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		String styleXml = "<w:style xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " w:type=\"paragraph\" w:styleId=\"Overskrift2\">"
				+ "<w:name w:val=\"overskrift 2\"/>"
				+ "<w:pPr><w:outlineLvl w:val=\"1\"/></w:pPr>"
				+ "</w:style>";
		mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(org.docx4j.wml.Style) XmlUtils.unmarshalString(styleXml, Context.jc, org.docx4j.wml.Style.class));
		mdp.addStyledParagraphOfText("Overskrift2", "Localized heading");

		assertEquals("## Localized heading\n", export(pkg));
	}

	@Test
	public void headingBoldDoesNotBecomeMarkers() throws Exception {
		// Heading1's own bold is baseline, not **markers**
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addStyledParagraphOfText("Heading1", "The Title");
		String md = export(pkg);
		assertEquals("# The Title\n", md);
		assertFalse(md.contains("**"));
	}

	@Test
	public void fieldCachedResultText() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String pXml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:r><w:t xml:space=\"preserve\">Page </w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> PAGE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>7</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> of many.</w:t></w:r>"
				+ "</w:p>";
		pkg.getMainDocumentPart().getContent().add(
				(P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));

		assertEquals("Page 7 of many.\n", export(pkg));
	}

	@Test
	public void contentControlContributesContent() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String sdtXml = "<w:sdt xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:sdtPr><w:tag w:val=\"x\"/></w:sdtPr>"
				+ "<w:sdtContent><w:p><w:r><w:t>Inside the control</w:t></w:r></w:p></w:sdtContent>"
				+ "</w:sdt>";
		pkg.getMainDocumentPart().getContent().add(
				XmlUtils.unmarshalString(sdtXml, Context.jc, org.docx4j.wml.SdtBlock.class));

		assertEquals("Inside the control\n", export(pkg));
	}

	@Test
	public void monoFontRunBecomesInlineCode() throws Exception {
		// code detection from effective rPr, not just our CodeChar style
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String pXml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:r><w:t xml:space=\"preserve\">run </w:t></w:r>"
				+ "<w:r><w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/></w:rPr>"
				+ "<w:t>mono()</w:t></w:r>"
				+ "</w:p>";
		pkg.getMainDocumentPart().getContent().add(
				(P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));

		assertEquals("run `mono()`\n", export(pkg));
	}

	@Test
	public void emptyParagraphsDropped() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("first");
		pkg.getMainDocumentPart().getContent().add(Context.getWmlObjectFactory().createP());
		pkg.getMainDocumentPart().addParagraphOfText("second");

		assertEquals("first\n\nsecond\n", export(pkg));
	}

	@Test
	public void markdownSignificantTextIsEscaped() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("2 * 3 = 6 and #tag [x]");
		String md = export(pkg);
		// re-importing must reproduce the same plain text (escaping worked)
		WordprocessingMLPackage back = new MarkdownImporter().createPackage(md);
		P p = (P) back.getMainDocumentPart().getContent().get(0);
		StringBuilder sb = new StringBuilder();
		org.docx4j.TraversalUtil.visit(p, new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public java.util.List<Object> apply(Object child) {
				child = XmlUtils.unwrap(child);
				if (child instanceof org.docx4j.wml.Text) {
					sb.append(((org.docx4j.wml.Text) child).getValue());
				}
				return null;
			}
		});
		assertEquals("2 * 3 = 6 and #tag [x]", sb.toString());
		assertTrue(md.endsWith("\n"));
	}


	// ---- CR-005 export enhancements of 2026-09-18 (found on the OpenDoPE Specification v3 draft)

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static void addStyle(WordprocessingMLPackage pkg, String styleXml) throws Exception {
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(org.docx4j.wml.Style) XmlUtils.unmarshalString(styleXml, Context.jc, org.docx4j.wml.Style.class));
	}

	private static void addP(WordprocessingMLPackage pkg, String pXml) throws Exception {
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));
	}

	/** A paragraph style whose own font is mono is a code block: consecutive paragraphs
	 *  merge into one fence, one line each, a w:br is a newline, leading spaces survive
	 *  and nothing inside is markdown-escaped. */
	@Test
	public void monoParagraphStyleIsAFencedCodeBlock() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Code\"><w:name w:val=\"Code\"/>"
				+ "<w:rPr><w:rFonts w:ascii=\"Consolas\" w:hAnsi=\"Consolas\"/><w:sz w:val=\"18\"/></w:rPr></w:style>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Code\"/></w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">&lt;xpaths xmlns=\"http://opendope.org/xpaths\"&gt;</w:t></w:r></w:p>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Code\"/></w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">  &lt;xpath id=\"x1\"&gt;</w:t><w:br/>"
				+ "<w:t xml:space=\"preserve\">    *not emphasis*</w:t></w:r></w:p>");
		pkg.getMainDocumentPart().addParagraphOfText("after");
		String md = export(pkg);
		assertEquals("```\n<xpaths xmlns=\"http://opendope.org/xpaths\">\n  <xpath id=\"x1\">\n    *not emphasis*\n```\n\nafter\n", md);
	}

	/** A mono document default is not a code block: a document set in Courier New
	 *  throughout is prose, escaped as prose. */
	@Test
	public void monoDocumentDefaultStaysProse() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.wml.Styles styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
		org.docx4j.wml.RFonts rf = new org.docx4j.wml.RFonts();
		rf.setAscii("Courier New");
		rf.setHAnsi("Courier New");
		styles.getDocDefaults().getRPrDefault().getRPr().setRFonts(rf);
		pkg.getMainDocumentPart().addParagraphOfText("a * b");
		assertEquals("a \\* b\n", export(pkg));
	}

	/** A numbering level with numFmt none and a lvlText with no %n is a label, not a
	 *  list: the label leads the paragraph in bold and no list is opened. */
	@Test
	public void labelLevelIsNotAList() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((org.docx4j.wml.Numbering) XmlUtils.unmarshalString(
				"<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"7\"><w:multiLevelType w:val=\"singleLevel\"/>"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"none\"/><w:suff w:val=\"tab\"/>"
				+ "<w:lvlText w:val=\"NOTE\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"1134\" w:hanging=\"1134\"/></w:pPr></w:lvl>"
				+ "</w:abstractNum>"
				+ "<w:abstractNum w:abstractNumId=\"8\"><w:multiLevelType w:val=\"singleLevel\"/>"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
				+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/></w:lvl>"
				+ "</w:abstractNum>"
				+ "<w:num w:numId=\"7\"><w:abstractNumId w:val=\"7\"/></w:num>"
				+ "<w:num w:numId=\"8\"><w:abstractNumId w:val=\"8\"/></w:num>"
				+ "</w:numbering>", Context.jc, org.docx4j.wml.Numbering.class));
		addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"7\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>The label is not a number.</w:t></w:r></w:p>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"8\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>But this is a list.</w:t></w:r></w:p>");
		String md = export(pkg);
		assertEquals("**NOTE** The label is not a number.\n\n1. But this is a list.\n", md);
	}

	/** A table-of-contents entry is dropped: its link goes to a Word bookmark no
	 *  markdown renderer has, and headings are navigable anyway. */
	@Test
	public void tocEntriesAreDropped() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"TOC1\"><w:name w:val=\"toc 1\"/></w:style>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>"
				+ "<w:hyperlink w:anchor=\"_Toc1004\"><w:r><w:t>1.1</w:t></w:r><w:r><w:tab/></w:r>"
				+ "<w:r><w:t>Overview</w:t></w:r><w:r><w:tab/></w:r><w:r><w:t>3</w:t></w:r></w:hyperlink></w:p>");
		pkg.getMainDocumentPart().addStyledParagraphOfText("Heading1", "Overview");
		assertEquals("# Overview\n", export(pkg));
	}
}
