package org.docx4j.convert.out.html;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
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

	/* ------------------------------------------------------------------ */

	private String toHTML(WordprocessingMLPackage wordMLPackage, int flag) throws Exception {

		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		htmlSettings.setOpcPackage(wordMLPackage);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(htmlSettings, baos, flag);
		return baos.toString("UTF-8");
	}
}
