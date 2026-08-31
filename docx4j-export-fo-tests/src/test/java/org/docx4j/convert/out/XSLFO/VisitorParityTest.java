package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
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

	/* ------------------------------------------------------------------
	 * Phase 3: footnotes and endnotes
	 * ------------------------------------------------------------------ */

	/** body text with a footnote and an endnote (cf GeneratedTextFontTest) */
	private WordprocessingMLPackage notesPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r><w:t xml:space=\"preserve\">Body text </w:t></w:r>"
				+ "<w:r><w:footnoteReference w:id=\"2\"/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> and </w:t></w:r>"
				+ "<w:r><w:endnoteReference w:id=\"2\"/></w:r>"
				+ "</w:p></w:body></w:document>"));

		FootnotesPart fp = new FootnotesPart();
		fp.setJaxbElement((CTFootnotes)XmlUtils.unmarshalString(
				"<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"0\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				// NB the footnote lookup indexes by position, so this has to be here for the ids to line up
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"1\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"2\"><w:p>"
				+   "<w:r><w:footnoteRef/></w:r>"
				+   "<w:r><w:t xml:space=\"preserve\"> The footnote.</w:t></w:r>"
				+ "</w:p></w:footnote></w:footnotes>", Context.jc, CTFootnotes.class));
		pkg.getMainDocumentPart().addTargetPart(fp);

		EndnotesPart ep = new EndnotesPart();
		ep.setJaxbElement((CTEndnotes)XmlUtils.unmarshalString(
				"<w:endnotes " + W + ">"
				+ "<w:endnote w:type=\"separator\" w:id=\"0\"><w:p><w:r><w:separator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:type=\"continuationSeparator\" w:id=\"1\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:id=\"2\"><w:p>"
				+   "<w:r><w:endnoteRef/></w:r>"
				+   "<w:r><w:t xml:space=\"preserve\"> The endnote.</w:t></w:r>"
				+ "</w:p></w:endnote></w:endnotes>", Context.jc, CTEndnotes.class));
		pkg.getMainDocumentPart().addTargetPart(ep);

		return pkg;
	}

	@Test
	public void testPhase3FootnotesEndnotes() throws Exception {

		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(notesPkg(), flag));
			String impl = flagName(flag) + ": ";

			// the separator rule
			assertTrue(impl + "footnote separator static-content lost", isPresent(doc,
					"//fo:static-content[@flow-name='xsl-footnote-separator']"
					+ "//fo:leader[@leader-pattern='rule']"));

			// fo:footnote with superscript marker and the note body as a list-block
			assertTrue(impl + "footnote lost", isPresent(doc,
					"//fo:footnote/fo:inline[@baseline-shift='super'][normalize-space(.)='1']"));
			assertTrue(impl + "footnote body lost", isPresent(doc,
					"//fo:footnote/fo:footnote-body//fo:list-item-body"
					+ "//fo:block[contains(.,'The footnote.')]"));
			assertTrue(impl + "footnote number label lost", isPresent(doc,
					"//fo:footnote//fo:list-item-label//fo:block[normalize-space(.)='1']"));

			// superscript endnote reference in the text
			assertTrue(impl + "endnote reference lost", isPresent(doc,
					"//fo:flow//fo:inline[@baseline-shift='super'][normalize-space(.)='1']"
					+ "[not(ancestor::fo:footnote)][not(ancestor::fo:list-block)]"));

			// the Endnotes block at the end of the flow
			assertTrue(impl + "Endnotes heading lost", isPresent(doc,
					"//fo:flow/fo:block[@font-weight='bold'][normalize-space(.)='Endnotes']"));
			assertTrue(impl + "endnote body lost", isPresent(doc,
					"//fo:flow//fo:block[contains(.,'The endnote.')]"));
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
