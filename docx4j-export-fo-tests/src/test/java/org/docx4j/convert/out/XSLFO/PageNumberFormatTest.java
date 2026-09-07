package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

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
 * {@code w:pgNumType/@w:fmt} is the section's page number format, and reaches the
 * {@code fo:page-sequence} as {@code format=}.
 *
 * <p>It never did where the section's PAGE fields carried a {@code \*} switch that names
 * no number format.  The switch collector recorded the empty value of a bare
 * {@code PAGE}, and the {@code MERGEFORMAT} of {@code PAGE \* MERGEFORMAT}, and either,
 * being non-null, masked the section's own format.  Measured against Word 365 on a
 * document declaring {@code <w:pgNumType w:fmt="upperRoman"/>} whose two footers hold
 * exactly those two fields: Word's footer prints I, II, ... on all 25 pages where docx4j
 * printed 1, 2, ... - essentially that document's whole parity loss.  9 documents of
 * three corpora declare a non-decimal {@code w:fmt}.</p>
 *
 * @since 17.1.0
 */
public class PageNumberFormatTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	private static String pageSequenceFormat(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
		NodeList nl = doc.getElementsByTagNameNS(FO, "page-sequence");
		if (nl.getLength() == 0) return null;
		return ((Element) nl.item(0)).getAttribute("format");
	}

	private static String sectPr(String fmt) {
		return "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"720\"/>"
				+ (fmt == null ? "" : "<w:pgNumType w:fmt=\"" + fmt + "\"/>")
				+ "</w:sectPr>";
	}

	private static String bodyWith(String fmt, String instr) {
		return "<w:p><w:fldSimple w:instr=\"" + instr + "\"><w:r><w:t>1</w:t></w:r></w:fldSimple></w:p>"
				+ sectPr(fmt);
	}

	@Test
	public void upperRomanReachesThePageSequence() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "I",
					pageSequenceFormat(bodyWith("upperRoman", " PAGE "), flag));
		}
	}

	/** MERGEFORMAT says what to do with the field's character formatting, and names no
	 *  number format, so it must not mask the section's. */
	@Test
	public void mergeformatDoesNotMaskTheSectionFormat() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "i",
					pageSequenceFormat(bodyWith("lowerRoman", " PAGE   \\* MERGEFORMAT "), flag));
		}
	}

	/** A format switch which does name one still wins. */
	@Test
	public void aFormatSwitchOnTheFieldStillWins() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "A",
					pageSequenceFormat(bodyWith("lowerRoman", " PAGE \\* ALPHABETIC "), flag));
		}
	}

	/** With no w:pgNumType the page-sequence carries no format at all, as before. */
	@Test
	public void noFormatWithoutPgNumType() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "", pageSequenceFormat(bodyWith(null, " PAGE "), flag));
		}
	}
}
