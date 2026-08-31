package org.docx4j.convert.out.html;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * Feature parity between the two HTML exporters: the same assertions are run
 * against the output of HTMLExporterXslt (the reference) and HTMLExporterVisitor.
 *
 * See docs/developer/change-requests/CR-html-exporter-parity.md; test methods are
 * grouped by that CR's phases.  Assertions are on the serialized output (regex),
 * as in NoteFontTest, so no DTD resolution is involved.
 */
public class HtmlVisitorParityTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final int[] FLAGS = new int[] {
			Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static String flagName(int flag) {
		return (flag==Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "NONXSL");
	}

	/** the text appears within a span with this class (possibly with spans between) */
	private static boolean inSpanWithClass(String html, String cls, String text) {
		return Pattern.compile(
				"<span class=\"" + cls + "\"[^>]*>(<span[^>]*>)*" + Pattern.quote(text))
				.matcher(html).find();
	}

	/* ------------------------------------------------------------------
	 * Phase 1: previously dropped content (and the run-span placement fix)
	 * ------------------------------------------------------------------ */

	private WordprocessingMLPackage phase1Pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r>"
				+   "<w:t>be</w:t><w:softHyphen/><w:t>fore</w:t>"
				+   "<w:t xml:space=\"preserve\"> non</w:t><w:noBreakHyphen/><w:t>breaking</w:t>"
				+   "<w:t xml:space=\"preserve\"> line one</w:t><w:cr/><w:t>line two</w:t>"
				+ "</w:r></w:p>"
				+ "<w:p>"
				+   "<w:ins w:id=\"1\" w:author=\"a\" w:date=\"2026-09-01T00:00:00Z\">"
				+     "<w:r><w:t>inserted</w:t></w:r>"
				+   "</w:ins>"
				+   "<w:del w:id=\"2\" w:author=\"a\" w:date=\"2026-09-01T00:00:00Z\">"
				+     "<w:r><w:delText>deleted</w:delText></w:r>"
				+   "</w:del>"
				+ "</w:p>"
				+ "<w:p>"
				+   "<w:moveFromRangeStart w:id=\"3\" w:name=\"m1\" w:author=\"a\" w:date=\"2026-09-01T00:00:00Z\"/>"
				+   "<w:moveFrom w:id=\"4\" w:author=\"a\" w:date=\"2026-09-01T00:00:00Z\">"
				+     "<w:r><w:t>movedout</w:t></w:r>"
				+   "</w:moveFrom>"
				+   "<w:moveFromRangeEnd w:id=\"3\"/>"
				+   "<w:moveToRangeStart w:id=\"5\" w:name=\"m1\" w:author=\"a\" w:date=\"2026-09-01T00:00:00Z\"/>"
				+   "<w:moveTo w:id=\"6\" w:author=\"a\" w:date=\"2026-09-01T00:00:00Z\">"
				+     "<w:r><w:t>movedin</w:t></w:r>"
				+   "</w:moveTo>"
				+   "<w:moveToRangeEnd w:id=\"5\"/>"
				+ "</w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testPhase1DroppedContent() throws Exception {

		for (int flag : FLAGS) {
			String html = toHTML(phase1Pkg(), flag);
			String impl = flagName(flag) + ": ";

			// run content belongs inside the p element (regression: the 17.0.1
			// rtlAwareAppendChildToCurrentP extraction put visitor spans NEXT TO it)
			assertTrue(impl + "run content not inside its p", Pattern.compile(
					"<p[^>]*>[^<]*(<span[^>]*>)+be").matcher(html).find());

			// the characters may sit between spans, so compare the tag-stripped text
			String text = html.replaceAll("<[^>]*>", "");
			assertTrue(impl + "soft hyphen lost", text.contains("be\u00ADfore"));
			assertTrue(impl + "no-break hyphen lost", text.contains("non\u2011breaking"));
			assertTrue(impl + "w:cr line break lost", html.contains("<br clear=\"all\""));

			assertTrue(impl + "inserted text not marked",
					inSpanWithClass(html, "ins", "inserted"));
			assertTrue(impl + "deleted text lost or not marked",
					inSpanWithClass(html, "del", "deleted"));
			assertTrue(impl + "moved-to text not marked",
					inSpanWithClass(html, "ins", "movedin"));
			assertTrue(impl + "moved-from text not marked",
					inSpanWithClass(html, "del", "movedout"));

			// the move range markers are skipped (in particular, no anchor is
			// emitted for the CTMoveBookmark range starts)
			assertTrue(impl + "move range marker leaked an anchor",
					!html.contains("name=\"m1\""));
		}
	}

	/* ------------------------------------------------------------------
	 * Phase 4: paragraph/run fidelity (classes, numbering, empty paragraphs,
	 * span composition/merging)
	 * ------------------------------------------------------------------ */

	private WordprocessingMLPackage phase4Pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>plaintext</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr><w:b/></w:rPr><w:t>boldtext</w:t></w:r></w:p>"
				+ "<w:p/>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+   "<w:r><w:t>item text</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"MyList\"/></w:pPr><w:r><w:t>styled item</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>heading text</w:t></w:r></w:p>"
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

		// a style carrying the numbering (style-based numbering case)
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style)XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"MyList\">"
				+ "<w:name w:val=\"MyList\"/><w:basedOn w:val=\"Normal\"/>"
				+ "<w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+ "</w:style>", Context.jc, Style.class));

		return pkg;
	}

	@Test
	public void testPhase4ParagraphRunFidelity() throws Exception {

		for (int flag : FLAGS) {
			String html = toHTML(phase4Pkg(), flag);
			String impl = flagName(flag) + ": ";

			// every paragraph gets a class from the style tree, incl. default-styled
			assertTrue(impl + "no class on a default-styled paragraph", Pattern.compile(
					"<p class=\"Normal[^\"]*\"[^>]*>(<span[^>]*>)*plaintext").matcher(html).find());
			assertTrue(impl + "no class from the paragraph's own style", Pattern.compile(
					"<p class=\"Heading1[^\"]*\"").matcher(html).find());

			// an empty paragraph is preserved with an nbsp
			assertTrue(impl + "empty paragraph not preserved", Pattern.compile(
					"<p[^>]*>\u00A0</p>").matcher(html).find());

			// run span composition: the rPr css and the w:t font selection are
			// merged into ONE span (no nested span), with the default character
			// style class
			assertTrue(impl + "run span not composed (nested spans, or missing css)",
					Pattern.compile("<span class=\"DefaultParagraphFont[^\"]*\" "
							+ "style=\"[^\"]*font-weight: bold;[^\"]*font-family[^\"]*\">boldtext</span>")
							.matcher(html).find());

			// numbered paragraphs (direct and style-based) both become li
			// (via the HTML_ELEMENT sdts the ListsToContentControls preprocess adds)
			assertTrue(impl + "direct-numbered paragraph is not an li", Pattern.compile(
					"<li[^>]*style=\"display: list-item;\"[^>]*>(<span[^>]*>)*item text").matcher(html).find());
			assertTrue(impl + "style-numbered paragraph is not an li", Pattern.compile(
					"<li class=\"MyList[^\"]*\"[^>]*>(<span[^>]*>)*styled item").matcher(html).find());
		}
	}

	/* ------------------------------------------------------------------ */

	private String toHTML(WordprocessingMLPackage wordMLPackage, int flag) throws Exception {

		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		htmlSettings.setOpcPackage(wordMLPackage);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(htmlSettings, baos, flag);
		return baos.toString("UTF-8");
	}
}
