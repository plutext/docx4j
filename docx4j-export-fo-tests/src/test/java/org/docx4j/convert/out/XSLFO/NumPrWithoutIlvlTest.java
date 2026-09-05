package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;

/**
 * w:numPr may carry a w:numId without a w:ilvl (and w:ilvl may itself have no w:val);
 * Word numbers such a paragraph at level 0.  docx4j used to throw a NullPointerException
 * reading the level, which aborted the export.
 *
 * @since 17.0.5
 */
public class NumPrWithoutIlvlTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** level 0 numbers with "L0-1.", level 1 with "L1-a." - so we can see which was used */
	private static final String NUMBERING =
			"<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\">"
			+ "  <w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "    <w:lvlText w:val=\"L0-%1.\"/><w:lvlJc w:val=\"left\"/></w:lvl>"
			+ "  <w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/>"
			+ "    <w:lvlText w:val=\"L1-%2.\"/><w:lvlJc w:val=\"left\"/></w:lvl>"
			+ "</w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "</w:numbering>";

	private static String fo(String numPr, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering)XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + numPr + "</w:pPr><w:r><w:t>numbered item</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private void check(int flags) throws Exception {

		// w:ilvl absent
		String fo = fo("<w:numPr><w:numId w:val=\"1\"/></w:numPr>", flags);
		assertTrue("should be a list:\n" + fo, fo.contains("list-block"));
		assertTrue("without w:ilvl, the level is 0:\n" + fo, fo.contains("L0-1."));
		assertTrue(fo.contains("numbered item"));
		assertTrue(fo.contains("after"));

		// w:ilvl present but with no w:val
		fo = fo("<w:numPr><w:ilvl/><w:numId w:val=\"1\"/></w:numPr>", flags);
		assertTrue("w:ilvl without w:val is level 0 too:\n" + fo, fo.contains("L0-1."));

		// w:numId without w:val: nothing to number with, but the export must complete
		fo = fo("<w:numPr><w:numId/></w:numPr>", flags);
		assertTrue(fo.contains("numbered item"));
		assertTrue(fo.contains("after"));

		// the usual case still works
		fo = fo("<w:numPr><w:ilvl w:val=\"1\"/><w:numId w:val=\"1\"/></w:numPr>", flags);
		assertTrue("level 1 should number with its own format:\n" + fo, fo.contains("L1-a."));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
