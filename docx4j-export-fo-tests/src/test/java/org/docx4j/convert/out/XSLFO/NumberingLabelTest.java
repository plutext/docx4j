package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * What a list label says and what it is set in (CR-001, real documents).
 *
 * <ul>
 * <li><b>{@code w:isLgl}</b> (Word's "Legal style numbering") displays every level of the
 * number in decimal, at whatever level it is stated - not only at ilvl 1, and not only for
 * {@code %1}.  Measured on a document whose abstractNum carries it at ilvl 1, 2 and 3 with
 * {@code w:lvlText} "%1.%2." over an upperRoman ilvl 0: Word prints "3.6.2." where docx4j
 * printed "III.6.2.", so every second- and third-level heading failed to match.</li>
 * <li><b>A level {@code w:rPr} which names no font</b> - Word writes
 * {@code <w:rFonts w:hint="default"/>} - leaves the label in the paragraph's own font.
 * Measured on the same document: Word draws the whole line in Arial-BoldMT, where docx4j
 * drew the label in the document default beside an Arial heading.</li>
 * <li><b>Wingdings 0xFC</b> is U+2713 CHECK MARK in Word's own PDF, not U+2714 HEAVY CHECK
 * MARK (the drawn widths agree to 0.1pt either way, so only the text layer differed).</li>
 * </ul>
 *
 * @since 17.1.0
 */
public class NumberingLabelTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/**
	 * abstractNum 0: upperRoman at ilvl 0, decimal at ilvl 1 and 2, both of which carry
	 * w:isLgl; the levels' rPr names no font.  abstractNum 1: a Wingdings 0xFC bullet.
	 */
	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:multiLevelType w:val=\"multilevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"upperRoman\"/>"
			+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"360\" w:hanging=\"360\"/></w:pPr>"
			+ "<w:rPr><w:rFonts w:hint=\"default\"/><w:b/><w:i w:val=\"0\"/></w:rPr></w:lvl>"
			+ "<w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:isLgl/>"
			+ "<w:lvlText w:val=\"%1.%2.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl>"
			+ "<w:lvl w:ilvl=\"2\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:isLgl/>"
			+ "<w:lvlText w:val=\"%1.%2.%3.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"1080\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
			+ "<w:abstractNum w:abstractNumId=\"1\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"\uF0FC\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>"
			+ "<w:rPr><w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/></w:rPr>"
			+ "</w:lvl></w:abstractNum>"
			// abstractNum 2: a decimal level whose own w:rPr is bold and red
			+ "<w:abstractNum w:abstractNumId=\"2\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>"
			+ "<w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>"
			+ "<w:rPr><w:b/><w:color w:val=\"FF0000\"/></w:rPr></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "<w:num w:numId=\"2\"><w:abstractNumId w:val=\"1\"/></w:num>"
			+ "<w:num w:numId=\"3\"><w:abstractNumId w:val=\"2\"/></w:num>"
			+ "</w:numbering>";

	private static String item(String numId, String ilvl, String text) {
		return "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"" + ilvl + "\"/>"
				+ "<w:numId w:val=\"" + numId + "\"/></w:numPr>"
				+ "<w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:b/></w:rPr></w:pPr>"
				+ "<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:b/></w:rPr>"
				+ "<w:t>" + text + "</w:t></w:r></w:p>";
	}

	/** an item whose paragraph states no formatting of its own */
	private static String plainItem(String numId, String text) {
		return "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/>"
				+ "<w:numId w:val=\"" + numId + "\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering)XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The label blocks, in document order. */
	private static Element label(org.w3c.dom.Document doc, int n) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "list-item-label");
		assertNotNull(nl);
		Element el = (Element) nl.item(n);
		assertNotNull("no list item " + n, el);
		return (Element) el.getElementsByTagNameNS(FO, "block").item(0);
	}

	/** The body block of the n-th list item. */
	private static Element body(org.w3c.dom.Document doc, int n) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "list-item-body");
		Element el = (Element) nl.item(n);
		return (Element) el.getElementsByTagNameNS(FO, "block").item(0);
	}

	private void check(int flags) throws Exception {

		// III at ilvl 0, then 3.1. and 3.1.1.: w:isLgl puts every level in decimal
		String body = item("1", "0", "one") + item("1", "0", "two") + item("1", "0", "three")
				+ item("1", "1", "sub") + item("1", "2", "subsub");
		org.w3c.dom.Document doc = fo(body, flags);
		assertEquals("III.", label(doc, 2).getTextContent());
		assertEquals("3.1.", label(doc, 3).getTextContent());
		assertEquals("3.1.1.", label(doc, 4).getTextContent());

		// the level's w:rFonts names no font, so the label is in the paragraph's
		assertEquals("the label is not in the paragraph's font",
				body(doc, 0).getAttribute("font-family"), label(doc, 0).getAttribute("font-family"));

		// Wingdings 0xFC
		assertEquals("\u2713", label(fo(item("2", "0", "checked"), flags), 0).getTextContent());
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
	 * A numbering level's {@code w:rPr} formats the <b>number</b> alone (ECMA-376
	 * 17.9.24); the paragraph's text keeps the paragraph's own formatting.  Until 17.1.0
	 * docx4j gave label and body one rPr, so a level {@code <w:b/>} made the whole
	 * paragraph bold - measured on a real document, Word draws the number in Tahoma-Bold
	 * and the text after it in Tahoma, and reading the level's rPr for both cost that
	 * document 0.074 of line parity.
	 *
	 * @since 17.1.0
	 */
	@Test
	public void levelRPrFormatsTheLabelAlone() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			org.w3c.dom.Document doc = fo(plainItem("3", "not bold"), flags);

			Element labelHolder = (Element) doc.getElementsByTagNameNS(FO, "list-item-label").item(0);
			assertEquals("bold", labelHolder.getAttribute("font-weight"));
			assertEquals("#ff0000", labelHolder.getAttribute("color").toLowerCase());

			// nothing of the level's rPr reaches the item's text
			Element bodyHolder = (Element) doc.getElementsByTagNameNS(FO, "list-item-body").item(0);
			assertEquals("", bodyHolder.getAttribute("font-weight"));
			assertEquals("", bodyHolder.getAttribute("color"));
			Element bodyBlock = body(doc, 0);
			assertEquals("", bodyBlock.getAttribute("font-weight"));
			assertEquals("", bodyBlock.getAttribute("color"));
			assertEquals("not bold", bodyBlock.getTextContent().trim());
		}
	}
}
