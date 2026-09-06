package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
 * A table-of-contents entry's stretching leader ends on the entry's own right dot stop,
 * not on the paragraph's right indent.
 *
 * <p>{@code text-align-last="justify"} stretches the leader to the block's end-indent,
 * which is the paragraph's right indent.  Word stretches it to the stop the tab reaches,
 * and where that stop lies outside the text column Word lets the entry overhang the
 * margin.  Measured against Word 365 on an A4 document with 72pt margins - a right edge
 * at x=523.35 - whose TOC style declares
 * {@code <w:tab w:val="right" w:leader="dot" w:pos="9350"/>} (x=539.5): all 308 of
 * Word's entry lines end at 539.6 where docx4j's ended at 523.3, and being 16.2pt short
 * of Word's measure six entries took two lines where Word takes one.  9 documents of
 * three corpora declare a TOC stop more than 2pt from their text column; in the rest the
 * stop is the right edge and nothing changes.</p>
 *
 * @since 17.0.6
 */
public class TocLeaderStopTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** A4 portrait, 72pt margins: the text column ends 9027 twips from the left margin. */
	private static org.w3c.dom.Document fo(int stopTwips, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:tabs>"
				+ "<w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"" + stopTwips + "\"/>"
				+ "</w:tabs></w:pPr>"
				+ "<w:r><w:t>Introduction</w:t></w:r><w:r><w:tab/></w:r><w:r><w:t>3</w:t></w:r>"
				+ "</w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"720\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** The entry's block: the one carrying the stretching leader's text-align-last. */
	private static Element entry(org.w3c.dom.Document doc) {
		NodeList blocks = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if ("justify".equals(b.getAttribute("text-align-last"))) return b;
		}
		return null;
	}

	/** A stop 320 twips (16pt) past the text column: the block overhangs the margin. */
	@Test
	public void theLeaderReachesAStopPastTheTextColumn() throws Exception {
		for (int flag : FLAGS) {
			Element block = entry(fo(9347, flag));
			assertNotNull(flagName(flag) + ": no stretching TOC leader", block);
			String endIndent = block.getAttribute("end-indent");
			assertTrue(flagName(flag) + ": end-indent is \"" + endIndent + "\", not the -16pt"
					+ " the stop is past the text column", endIndent.startsWith("-16"));
		}
	}

	/** A stop at the text column's own edge changes nothing. */
	@Test
	public void aStopAtTheRightEdgeChangesNothing() throws Exception {
		for (int flag : FLAGS) {
			Element block = entry(fo(9027, flag));
			assertNotNull(flagName(flag) + ": no stretching TOC leader", block);
			assertTrue(flagName(flag) + ": end-indent \"" + block.getAttribute("end-indent")
					+ "\" was written for a stop which is the right edge",
					block.getAttribute("end-indent").length() == 0);
		}
	}

	/** A stop well inside the text column changes nothing: it is the paragraph's
	 *  <em>first</em> stop, and a later one - or the right indent - is what the entry's
	 *  tab reaches, so the dots still run to the indent.  Only an overhang is applied. */
	@Test
	public void aStopInsideTheTextColumnChangesNothing() throws Exception {
		for (int flag : FLAGS) {
			Element block = entry(fo(8027, flag));
			assertNotNull(flagName(flag) + ": no stretching TOC leader", block);
			assertTrue(flagName(flag) + ": end-indent \"" + block.getAttribute("end-indent")
					+ "\" pulled the leader in from the right indent",
					block.getAttribute("end-indent").length() == 0);
		}
	}
}
