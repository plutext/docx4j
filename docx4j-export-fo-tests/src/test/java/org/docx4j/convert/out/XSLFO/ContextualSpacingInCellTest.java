package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A cell holding a single paragraph with {@code w:contextualSpacing} (CR-001 &#xa7;3).
 *
 * <p>From compatibility mode 15 a paragraph's space-after applies at the bottom of its
 * cell, which XSL-FO drops at a reference-area end - so {@code WordLayoutFixups} pins it
 * with {@code space-after.conditionality="retain"}.  The pass that cancels it where
 * {@code w:contextualSpacing} applies walked the cell's paragraphs in pairs, so a cell
 * holding exactly <em>one</em> paragraph was never examined and kept the docDefaults
 * space-after.  Measured on a planner whose cells hold one contextual paragraph each
 * against docDefaults {@code w:after="200"}: Word's row pitch is 10.1pt (the 9.199pt line
 * box plus w:trHeight 199) and docx4j's was 19.9pt - +9.0pt on the first row and +9.5 on
 * every row after it, turning Word's 37 pages into 43.</p>
 *
 * @since 17.0.6
 */
public class ContextualSpacingInCellTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String CONTEXTUAL_P =
			"<w:p><w:pPr><w:pStyle w:val=\"Cell\"/><w:contextualSpacing/></w:pPr>"
			+ "<w:r><w:t>one paragraph in this cell</w:t></w:r></w:p>";

	private static final String PLAIN_P =
			"<w:p><w:pPr><w:pStyle w:val=\"Cell\"/></w:pPr>"
			+ "<w:r><w:t>one paragraph in this cell</w:t></w:r></w:p>";

	private static WordprocessingMLPackage pkg(String paragraph) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		// docDefaults space-after of 200tw = 10pt, as Word's own default template has
		org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)
				pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
		styles.getDocDefaults().getPPrDefault().getPPr()
				.setSpacing(spacing());
		org.docx4j.wml.Style cell = (org.docx4j.wml.Style)XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Cell\">"
				+ "<w:name w:val=\"Cell\"/></w:style>", org.docx4j.jaxb.Context.jc,
				org.docx4j.wml.Style.class);
		styles.getStyle().add(cell);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:tbl>"
				+ "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9000\"/></w:tblGrid>"
				+ "<w:tr><w:tc>" + paragraph + "</w:tc></w:tr>"
				+ "</w:tbl><w:p/>" + SECT_PR + "</w:body></w:document>"));
		setCompatibilityMode(pkg, 15);
		return pkg;
	}

	private static org.docx4j.wml.PPrBase.Spacing spacing() throws Exception {
		return (org.docx4j.wml.PPrBase.Spacing)XmlUtils.unmarshalString(
				"<w:spacing " + W + " w:after=\"200\"/>", org.docx4j.jaxb.Context.jc,
				org.docx4j.wml.PPrBase.Spacing.class);
	}

	private static void setCompatibilityMode(WordprocessingMLPackage pkg, int mode) throws Exception {
		org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart settings =
				new org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart();
		settings.setJaxbElement((org.docx4j.wml.CTSettings)XmlUtils.unmarshalString(
				"<w:settings " + W + "><w:compat>"
				+ "<w:compatSetting w:name=\"compatibilityMode\""
				+ " w:uri=\"http://schemas.microsoft.com/office/word\" w:val=\"" + mode + "\"/>"
				+ "</w:compat></w:settings>", org.docx4j.jaxb.Context.jc,
				org.docx4j.wml.CTSettings.class));
		pkg.getMainDocumentPart().addTargetPart(settings);
	}

	private static Element cellBlock(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc =
				XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
		NodeList cells = doc.getElementsByTagNameNS(FO, "table-cell");
		assertTrue("no table-cell", cells.getLength() > 0);
		NodeList blocks = ((Element) cells.item(0)).getElementsByTagNameNS(FO, "block");
		assertTrue("no block in the cell", blocks.getLength() > 0);
		return (Element) blocks.item(0);
	}

	private void theOnlyParagraphOfACellIsSeen(int flags) throws Exception {
		Element block = cellBlock(pkg(CONTEXTUAL_P), flags);
		assertEquals("w:contextualSpacing must cancel the space at the cell's bottom edge",
				"0pt", block.getAttribute("space-after"));
		assertEquals("and nothing should be pinning it there",
				"", block.getAttribute("space-after.conditionality"));
	}

	@Test
	public void theOnlyParagraphOfACellIsSeenVisitor() throws Exception {
		theOnlyParagraphOfACellIsSeen(Docx4J.FLAG_NONE);
	}

	@Test
	public void theOnlyParagraphOfACellIsSeenXslt() throws Exception {
		theOnlyParagraphOfACellIsSeen(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Without w:contextualSpacing the space is kept, and pinned, as &#xa7;3.5 says. */
	private void withoutContextualSpacingTheSpaceIsRetained(int flags) throws Exception {
		Element block = cellBlock(pkg(PLAIN_P), flags);
		assertEquals("retain", block.getAttribute("space-after.conditionality"));
		assertTrue("the docDefaults 10pt should still be there, not " + block.getAttribute("space-after"),
				block.getAttribute("space-after").startsWith("10"));
	}

	@Test
	public void withoutContextualSpacingTheSpaceIsRetainedVisitor() throws Exception {
		withoutContextualSpacingTheSpaceIsRetained(Docx4J.FLAG_NONE);
	}

	@Test
	public void withoutContextualSpacingTheSpaceIsRetainedXslt() throws Exception {
		withoutContextualSpacingTheSpaceIsRetained(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
