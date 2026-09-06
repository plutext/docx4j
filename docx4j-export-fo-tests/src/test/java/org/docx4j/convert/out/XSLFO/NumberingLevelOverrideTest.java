package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

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
 * A numbering instance's own level definition ({@code w:num/w:lvlOverride/w:lvl}) supplies
 * the list's indent, and a paragraph's partial {@code w:ind} merges with it rather than
 * replacing it (CR-001, real documents).
 *
 * <p>{@code Emulator} read the <em>abstract</em> level only, so an override's {@code w:ind}
 * and {@code w:rPr} were lost.  Measured on a document whose {@code w:num} 32 overrides
 * ilvl 0 with {@code <w:ind w:left="397" w:hanging="113"/>} and whose paragraph states only
 * {@code <w:ind w:right="22"/>}: Word puts the bullet at 14.2pt and the item's text at
 * 19.85pt, where docx4j had neither indent nor hanging indent to work from, fell back to
 * the paragraph's first tab stop - a {@code w:val="clear"} entry at 9072 twips, which is
 * not a stop at all - and gave the label a width of 453.6pt.  The list text then ran off
 * the page and Word's two pages came out as four.</p>
 *
 * @since 17.0.6
 */
public class NumberingLevelOverrideTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** ilvl 0 at 720/360 in the abstract definition, overridden to 397/113 by numId 32. */
	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"-\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"32\"><w:abstractNumId w:val=\"0\"/>"
			+ "<w:lvlOverride w:ilvl=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"-\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"397\" w:hanging=\"113\"/></w:pPr></w:lvl></w:lvlOverride></w:num>"
			+ "<w:num w:numId=\"33\"><w:abstractNumId w:val=\"0\"/></w:num>"
			// numId 34: a level with a left indent but no hanging one, so the label's
			// width comes from the paragraph's tab stops
			+ "<w:num w:numId=\"34\"><w:abstractNumId w:val=\"0\"/>"
			+ "<w:lvlOverride w:ilvl=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"0\"/></w:pPr></w:lvl></w:lvlOverride></w:num>"
			+ "</w:numbering>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering)XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static Element listBlock(String body, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(body));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(
				new ByteArrayInputStream(baos.toByteArray()));
		NodeList nl = doc.getElementsByTagNameNS(FO, "list-block");
		assertEquals(1, nl.getLength());
		return (Element) nl.item(0);
	}

	private static String numbered(String numId, String pPrExtra) {
		return "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"" + numId + "\"/></w:numPr>"
				+ pPrExtra + "</w:pPr><w:r><w:t>Magnetabscheider angebracht</w:t></w:r></w:p>";
	}

	private void check(int flags) throws Exception {

		// the instance's own level: bullet at 397 - 113 = 284 twips, text at 397
		Element overridden = listBlock(numbered("32", ""), flags);
		assertEquals("14.2pt", overridden.getAttribute("start-indent"));
		assertEquals("5.65pt", overridden.getAttribute("provisional-distance-between-starts"));

		// the abstract level where the instance overrides nothing
		Element plain = listBlock(numbered("33", ""), flags);
		assertEquals("18pt", plain.getAttribute("start-indent"));
		assertEquals("18pt", plain.getAttribute("provisional-distance-between-starts"));

		// a w:ind stating only w:right merges with the level's left and hanging, and a
		// w:val="clear" tab stop is not a stop the label could take its width from
		Element merged = listBlock(numbered("32",
				"<w:tabs><w:tab w:val=\"clear\" w:pos=\"9072\"/>"
				+ "<w:tab w:val=\"left\" w:pos=\"4536\"/></w:tabs><w:ind w:right=\"22\"/>"), flags);
		assertEquals("14.2pt", merged.getAttribute("start-indent"));
		assertEquals("5.65pt", merged.getAttribute("provisional-distance-between-starts"));
		assertEquals("1.1pt", merged.getAttribute("end-indent"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Where the level has no hanging indent the label's width does come from the
	 * paragraph's tab stops - and there a {@code w:val="clear"} entry must be skipped and
	 * the nearest stop past the label taken, whatever order the {@code w:tabs} are in.
	 */
	@Test
	public void labelWidthSkipsClearedStops() throws Exception {
		String tabs = "<w:tabs><w:tab w:val=\"clear\" w:pos=\"9072\"/>"
				+ "<w:tab w:val=\"left\" w:pos=\"4536\"/>"
				+ "<w:tab w:val=\"left\" w:pos=\"2268\"/></w:tabs>";
		Element list = listBlock(numbered("34", tabs), Docx4J.FLAG_NONE);
		// 2268 twips is the nearest stop past the label: 2268 - 0 = 113.4pt
		assertEquals("113.4pt", list.getAttribute("provisional-distance-between-starts"));
	}

}
