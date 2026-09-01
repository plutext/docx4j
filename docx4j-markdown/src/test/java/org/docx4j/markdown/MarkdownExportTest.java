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

}
