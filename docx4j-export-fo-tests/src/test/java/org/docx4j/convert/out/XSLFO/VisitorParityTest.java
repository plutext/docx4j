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
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;

/**
 * Feature parity between the two FO exporters: the same assertions are run against
 * the output of FOExporterXslt (the reference) and FOExporterVisitor.
 *
 * See docs/developer/change-requests/CR-002-fo-exporter-parity.md; test methods are
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

	/* ------------------------------------------------------------------
	 * Phase 4: paragraph fidelity (empty paragraphs, generated-text fonts,
	 * paragraph-mark size, list structure)
	 * ------------------------------------------------------------------ */

	private static final String FONT = "Courier New";
	private static final String RPR =
			"<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>";

	private WordprocessingMLPackage phase4Pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				// a dot leader tab and a plain tab
				+ "<w:p><w:pPr><w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9016\"/></w:tabs></w:pPr>"
				+   "<w:r>" + RPR + "<w:t>Chapter one</w:t></w:r>"
				+   "<w:r>" + RPR + "<w:tab/></w:r>"
				+   "<w:r>" + RPR + "<w:t>7</w:t></w:r>"
				+ "</w:p>"
				+ "<w:p>"
				+   "<w:r>" + RPR + "<w:t>Before</w:t></w:r>"
				+   "<w:r>" + RPR + "<w:tab/></w:r>"
				+   "<w:r>" + RPR + "<w:t>After</w:t></w:r>"
				+ "</w:p>"
				// empty, with font and size on the paragraph mark
				+ "<w:p><w:pPr><w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/>"
				+   "<w:sz w:val=\"48\"/></w:rPr></w:pPr></w:p>"
				// sz on the paragraph mark contributes to the block's line height
				+ "<w:p><w:pPr><w:rPr><w:sz w:val=\"48\"/></w:rPr></w:pPr>"
				+   "<w:r><w:t>sized text</w:t></w:r></w:p>"
				// a numbered list item
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+   "<w:r><w:t>item text</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));

		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((Numbering)XmlUtils.unmarshalString(
				"<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"1\">"
				+   "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
				+     "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
				+     "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl>"
				+ "</w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/></w:num>"
				+ "</w:numbering>", Context.jc, Numbering.class));
		pkg.getMainDocumentPart().addTargetPart(ndp);

		return pkg;
	}

	/** the font-family the ordinary text was actually mapped to (a physical font
	 *  name, which need not equal the document's font name) */
	private String textFont(org.w3c.dom.Document doc, String startsWith) {

		org.w3c.dom.NodeList nl = doc.getElementsByTagNameNS(
				"http://www.w3.org/1999/XSL/Format", "inline");
		for (int i=0; i<nl.getLength(); i++) {
			org.w3c.dom.Element el = (org.w3c.dom.Element)nl.item(i);
			if (el.getTextContent().startsWith(startsWith)
					&& el.getAttribute("font-family").length()>0) {
				return el.getAttribute("font-family");
			}
		}
		return null;
	}

	@Test
	public void testPhase4ParagraphFidelity() throws Exception {

		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(phase4Pkg(), flag));
			String impl = flagName(flag) + ": ";

			String font = textFont(doc, "Chapter one");
			assertTrue(impl + "no font on the ordinary text", font!=null);

			// generated text (leader dots, tab spaces) carries the run's font
			assertTrue(impl + "dot leader has no or wrong font", isPresent(doc,
					"//fo:leader[@leader-pattern='dots'][@font-family='" + font + "']"));
			assertTrue(impl + "tab spaces have no or wrong font", isPresent(doc,
					"//fo:inline[.='\u00A0\u00A0\u00A0'][@font-family='" + font + "']"));

			// the empty paragraph is preserved, in the paragraph mark's font
			assertTrue(impl + "empty paragraph collapsed or unstyled", isPresent(doc,
					"//fo:block[@white-space-treatment='preserve'][@font-family='" + font + "']"));

						// paragraph-mark w:sz must NOT size the block of a paragraph that has text:
			// Word ignores it there (measured, CR-001 line-mixed probe), and FOP would
			// use it as a floor for every line.  Since 17.0.5 the block takes the font,
			// size and Word line-height of its dominant run instead.
			assertTrue(impl + "paragraph-mark sz applied to the block", isAbsent(doc,
					"//fo:block[starts-with(@font-size,'24')][contains(.,'sized text')]"));
			assertTrue(impl + "block of sized text has no line-height", isPresent(doc,
					"//fo:block[contains(@line-height,'pt')][contains(.,'sized text')]"));

			// list structure: list-block at flow level (no extra wrapper block),
			// number label, and the text in the item body
			assertTrue(impl + "list-block missing or wrapped", isPresent(doc,
					"//fo:flow/fo:list-block"));
			assertTrue(impl + "list number label lost", isPresent(doc,
					"//fo:list-block//fo:list-item-label//fo:block[normalize-space(.)='1.']"));
			assertTrue(impl + "list item text lost", isPresent(doc,
					"//fo:list-block//fo:list-item-body//fo:block[contains(.,'item text')]"));
		}
	}

	/* ------------------------------------------------------------------
	 * Phase 5: traversal semantics (mc:AlternateContent, v:rect textboxes)
	 * ------------------------------------------------------------------ */

	private static final String MC = "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"";
	private static final String V = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
	private static final String O = "xmlns:o=\"urn:schemas-microsoft-com:office:office\"";

	private WordprocessingMLPackage phase5Pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " " + MC + " " + V + " " + O + "><w:body>"
				// only the Fallback should be rendered
				+ "<mc:AlternateContent>"
				+   "<mc:Choice Requires=\"wps\"><w:p><w:r><w:t>choice text</w:t></w:r></w:p></mc:Choice>"
				+   "<mc:Fallback><w:p><w:r><w:t>fallback text</w:t></w:r></w:p></mc:Fallback>"
				+ "</mc:AlternateContent>"
				// a textbox hosted in v:rect (as Word writes for some textboxes)
				+ "<w:p><w:r><w:pict>"
				+   "<v:rect style=\"width:100pt;height:50pt\">"
				+     "<v:textbox><w:txbxContent>"
				+       "<w:p><w:r><w:t>rect box</w:t></w:r></w:p>"
				+     "</w:txbxContent></v:textbox>"
				+   "</v:rect>"
				+ "</w:pict></w:r></w:p>"
				// a horizontal rule: no textbox, no image; must not break the conversion
				+ "<w:p><w:r><w:pict>"
				+   "<v:rect style=\"width:0;height:1.5pt\" o:hr=\"t\" o:hrstd=\"t\" fillcolor=\"#a0a0a0\" stroked=\"f\"/>"
				+ "</w:pict></w:r></w:p>"
				+ "<w:p><w:r><w:t>after hr</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testPhase5TraversalSemantics() throws Exception {

		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(phase5Pkg(), flag));
			String text = doc.getDocumentElement().getTextContent();
			String impl = flagName(flag) + ": ";

			assertTrue(impl + "mc:Fallback content lost", text.contains("fallback text"));
			assertTrue(impl + "mc:Choice content rendered (should be Fallback only)",
					!text.contains("choice text"));

			assertTrue(impl + "v:rect textbox content lost", text.contains("rect box"));

			// the o:hr rule paragraph must not have broken the conversion
			assertTrue(impl + "content after the horizontal rule lost", text.contains("after hr"));
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
