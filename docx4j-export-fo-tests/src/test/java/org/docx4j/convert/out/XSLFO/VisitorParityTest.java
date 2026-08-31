package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Feature parity between the two FO exporters: the same assertions are run against
 * the output of FOExporterXslt (the reference) and FOExporterVisitor.
 *
 * See docs/developer/change-requests/CR-fo-exporter-parity.md; test methods are
 * grouped by that CR's phases.
 */
public class VisitorParityTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final int[] FLAGS = new int[] {
			Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static String flagName(int flag) {
		return (flag==Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "NONXSL");
	}

	/* ------------------------------------------------------------------
	 * Phase 1: previously dropped content
	 * ------------------------------------------------------------------ */

	/** softHyphen, noBreakHyphen, cr, ptab, ins, delText in one body */
	private WordprocessingMLPackage phase1Pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r>"
				+   "<w:t>be</w:t><w:softHyphen/><w:t>fore</w:t>"
				+   "<w:t xml:space=\"preserve\"> non</w:t><w:noBreakHyphen/><w:t>breaking</w:t>"
				+   "<w:t xml:space=\"preserve\"> line one</w:t><w:cr/><w:t>line two</w:t>"
				+ "</w:r></w:p>"
				+ "<w:p><w:r><w:t>left</w:t></w:r>"
				+   "<w:r><w:ptab w:relativeTo=\"margin\" w:alignment=\"right\" w:leader=\"none\"/><w:t>right</w:t></w:r>"
				+ "</w:p>"
				+ "<w:p>"
				+   "<w:ins w:id=\"1\" w:author=\"a\" w:date=\"2026-08-31T00:00:00Z\">"
				+     "<w:r><w:t>inserted</w:t></w:r>"
				+   "</w:ins>"
				+   "<w:del w:id=\"2\" w:author=\"a\" w:date=\"2026-08-31T00:00:00Z\">"
				+     "<w:r><w:delText>deleted</w:delText></w:r>"
				+   "</w:del>"
				+ "</w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testPhase1DroppedContent() throws Exception {

		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(phase1Pkg(), flag));
			String text = doc.getDocumentElement().getTextContent();
			String impl = flagName(flag) + ": ";

			assertTrue(impl + "soft hyphen lost",
					text.contains("be\u00ADfore"));
			assertTrue(impl + "no-break hyphen lost",
					text.contains("non-\uFEFFbreaking"));
			assertTrue(impl + "w:cr line break lost",
					isPresent(doc, "//fo:block[contains(.,'line one')]"
							+ "//fo:block[@white-space-treatment='preserve']"));
			assertTrue(impl + "ptab leader lost", isPresent(doc,
					"//fo:leader[@leader-pattern='space'][@leader-alignment='reference-area']"));
			assertTrue(impl + "ptab needs text-align-last on its block", isPresent(doc,
					"//fo:block[@text-align-last='justify']"
					+ "//fo:leader[@leader-pattern='space']"));
			assertTrue(impl + "inserted text not marked", isPresent(doc,
					"//fo:inline[@color='blue'][@text-decoration='underline'][contains(.,'inserted')]"));
			assertTrue(impl + "deleted text lost or not marked", isPresent(doc,
					"//fo:inline[@color='red'][@text-decoration='line-through'][.='deleted']"));
		}
	}

	/** two sections, the second of type oddPage */
	private WordprocessingMLPackage sectionsPkg() throws Exception {

		String pg = "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/>";

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Section one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr>" + pg + "</w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>Section two</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"oddPage\"/>" + pg + "</w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testPhase1ForcePageCount() throws Exception {

		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(sectionsPkg(), flag));
			String impl = flagName(flag) + ": ";

			// next section starts on an odd page, so this one must end on an even one
			assertTrue(impl + "first page-sequence should end-on-even", isPresent(doc,
					"//fo:page-sequence[1][@force-page-count='end-on-even']"));
			assertTrue(impl + "last page-sequence should be no-force", isPresent(doc,
					"//fo:page-sequence[2][@force-page-count='no-force']"));
		}
	}

	@Test
	public void testPhase1Hyphenate() throws Exception {

		try {
			Docx4jProperties.setProperty("docx4j.convert.out.fo.hyphenate", "true");
			for (int flag : FLAGS) {
				org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(phase1Pkg(), flag));
				assertTrue(flagName(flag) + ": hyphenate property not honoured",
						isPresent(doc, "//fo:block[@hyphenate='true']"));
			}
		} finally {
			Docx4jProperties.setProperty("docx4j.convert.out.fo.hyphenate", "false");
		}
	}

	/* ------------------------------------------------------------------
	 * Phase 2: Containerization containers (paragraph borders/shading, run borders)
	 * ------------------------------------------------------------------ */

	/** two adjacent paragraphs with the same borders, two shaded ones, and two
	 *  adjacent runs with the same run border */
	private WordprocessingMLPackage phase2Pkg() throws Exception {

		String pbdr = "<w:pBdr>"
				+ "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"FF0000\"/>"
				+ "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"FF0000\"/>"
				+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"FF0000\"/>"
				+ "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"FF0000\"/>"
				+ "</w:pBdr>";
		String shd = "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FFFF00\"/>";
		String rbdr = "<w:rPr><w:bdr w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/></w:rPr>";

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + pbdr + "</w:pPr><w:r><w:t>bordered one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + pbdr + "</w:pPr><w:r><w:t>bordered two</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + shd + "</w:pPr><w:r><w:t>shaded one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + shd + "</w:pPr><w:r><w:t>shaded two</w:t></w:r></w:p>"
				+ "<w:p>"
				+   "<w:r>" + rbdr + "<w:t>boxed one</w:t></w:r>"
				+   "<w:r>" + rbdr + "<w:t xml:space=\"preserve\"> boxed two</w:t></w:r>"
				+ "</w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testPhase2Containers() throws Exception {

		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(phase2Pkg(), flag));
			String impl = flagName(flag) + ": ";

			// the borders sit on one container block around both paragraphs
			assertTrue(impl + "paragraph borders lost", isPresent(doc,
					"//fo:block[@border-top-style='solid']"
					+ "[contains(.,'bordered one')][contains(.,'bordered two')]"));
			assertTrue(impl + "top border repeated on the inner paragraphs", isAbsent(doc,
					"//fo:block[@border-top-style]//fo:block[@border-top-style]"));

			// the shading container has zero margins (no white strip between paragraphs)
			assertTrue(impl + "shading container lost", isPresent(doc,
					"//fo:block[@background-color='#FFFF00'][@margin-top='0in']"
					+ "[contains(.,'shaded one')][contains(.,'shaded two')]"));

			// run borders sit on one container inline around both runs
			assertTrue(impl + "run border container lost", isPresent(doc,
					"//fo:inline[@border-style='solid']"
					+ "[contains(.,'boxed one')][contains(.,'boxed two')]"));
		}
	}

	/* ------------------------------------------------------------------ */

	private byte[] toFO(WordprocessingMLPackage wordMLPackage, int flag) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(wordMLPackage);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flag);
		return baos.toByteArray();
	}
}
