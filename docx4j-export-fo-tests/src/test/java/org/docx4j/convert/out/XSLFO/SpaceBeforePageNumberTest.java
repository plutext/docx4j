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
 * FOP treats the end of an {@code fo:inline} as a place where a trailing space can be
 * collapsed away, so {@code <w:t xml:space="preserve">Seite </w:t>} followed by a
 * {@code PAGE} field lost its space: measured against Word 365 in a corpus footer,
 * Word's "Seite ii" runs 526.1..547.6 = 21.5pt and ours read "Seiteii" at
 * 527.6..547.4 = 19.8, exactly the 1.81pt an 8pt Carlito space is.
 *
 * <p>A zero-width space after it - the workaround FldSimpleWriter already uses at the
 * other end of an {@code fo:page-number-citation-last} - keeps the space and leaves the
 * text a reader or a text extractor sees unchanged.</p>
 *
 * @since 17.1.0
 */
public class SpaceBeforePageNumberTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String XML = "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " " + XML + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** The body flow's text: the page masters and the header/footer placeholders carry
	 *  white space of their own. */
	private static String flowText(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "flow");
		return nl.getLength() == 0 ? "" : nl.item(0).getTextContent();
	}

	private static Element firstPageNumber(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "page-number");
		return nl.getLength() == 0 ? null : (Element) nl.item(0);
	}

	@Test
	public void theSpaceInFrontOfAPageNumberSurvives() throws Exception {
		String body = "<w:p>"
				+ "<w:r><w:t xml:space=\"preserve\">Seite </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" PAGE \"><w:r><w:t>1</w:t></w:r></w:fldSimple>"
				+ "</w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, flag);
			assertNotNull(flagName(flag) + ": no fo:page-number", firstPageNumber(doc));
			String text = flowText(doc);
			assertTrue(flagName(flag) + ": the space before the page number is gone: \"" + text + "\"",
					text.contains("Seite \u200b"));
			assertTrue(flagName(flag) + ": a no-break space would change the text a reader sees",
					!text.contains("\u00a0"));
		}
	}

	/** Nothing is added where the text does not end in a space. */
	@Test
	public void noSpaceNoMarker() throws Exception {
		String body = "<w:p>"
				+ "<w:r><w:t>Seite</w:t></w:r>"
				+ "<w:fldSimple w:instr=\" PAGE \"><w:r><w:t>1</w:t></w:r></w:fldSimple>"
				+ "</w:p>";
		for (int flag : FLAGS) {
			String text = flowText(fo(body, flag));
			assertTrue(flagName(flag) + ": unexpected marker in \"" + text + "\"",
					!text.contains("\u200b"));
		}
	}

	/** A page number that opens its block picks nothing up from the block before it. */
	@Test
	public void aPageNumberAtTheStartOfABlockIsLeftAlone() throws Exception {
		String body = "<w:p><w:r><w:t xml:space=\"preserve\">before </w:t></w:r></w:p>"
				+ "<w:p><w:fldSimple w:instr=\" PAGE \"><w:r><w:t>1</w:t></w:r></w:fldSimple></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, flag);
			assertTrue(flagName(flag) + ": the previous block was touched",
					!flowText(doc).contains("\u200b"));
		}
	}
}
