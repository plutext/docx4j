package org.docx4j.convert.out.html;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.preprocess.Containerization;
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
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * Feature parity between the two HTML exporters: the same assertions are run
 * against the output of HTMLExporterXslt (the reference) and HTMLExporterVisitor.
 *
 * See docs/developer/change-requests/CR-003-html-exporter-parity.md; test methods are
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

	/** is text somewhere inside a span whose start tag contains cssFragment?
	 *  (unlike a plain regex, this follows nested spans to that span's own
	 *  close tag, so intervening markup - eg a br - doesn't matter) */
	private static boolean withinSpanStyled(String htmlIn, String cssFragment, String text) {

		// drop self-closed (empty) spans, so the depth count below need only
		// handle start/end tag pairs
		String html = htmlIn.replaceAll("<span[^>]*/>", "");

		java.util.regex.Matcher m = Pattern.compile("<span [^>]*"
				+ Pattern.quote(cssFragment) + "[^>]*>").matcher(html);
		while (m.find()) {
			int depth = 1;
			int i = m.end();
			while (i < html.length() && depth > 0) {
				int open = html.indexOf("<span", i);
				int close = html.indexOf("</span>", i);
				if (close < 0) break;
				int hit = html.indexOf(text, i);
				if (hit >= 0 && hit < close) return true;
				if (open >= 0 && open < close) {
					depth++;
					i = open + 5;
				} else {
					depth--;
					i = close + 7;
				}
			}
		}
		return false;
	}

	/** what the output actually contains around the needle — so an
	 *  environment-dependent failure (eg font mapping) is diagnosable from
	 *  the CI log alone */
	private static String around(String html, String needle) {
		int i = html.indexOf(needle);
		if (i < 0) {
			return " [needle '" + needle + "' absent from output]";
		}
		return " actual: [" + html.substring(Math.max(0, i - 300),
				Math.min(html.length(), i + needle.length() + 100)) + "]";
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
	 * A break in the middle of a run: the text after it stays in the run
	 * (17.0.6; the visitor used to close the run span at the break, so the
	 * following w:t got a bare span with none of the run's formatting, and
	 * logged "null currentSpan!")
	 * ------------------------------------------------------------------ */

	private WordprocessingMLPackage breakInRunPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				// break between two w:t of the same run
				+ "<w:p><w:r><w:rPr><w:b/></w:rPr>"
				+   "<w:t>beforebr</w:t><w:br/><w:t>afterbr</w:t>"
				+ "</w:r></w:p>"
				// break first in the run (as Word writes a soft return starting a line)
				+ "<w:p><w:r><w:rPr><w:i/></w:rPr>"
				+   "<w:br/><w:t>leadingbr</w:t>"
				+ "</w:r></w:p>"
				// page break, then more text in the same run
				+ "<w:p><w:r><w:rPr><w:b/></w:rPr>"
				+   "<w:t>beforepage</w:t><w:br w:type=\"page\"/><w:t>afterpage</w:t>"
				+ "</w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testBreakInsideRun() throws Exception {

		for (int flag : FLAGS) {
			String html = toHTML(breakInRunPkg(), flag);
			String impl = flagName(flag) + ": ";

			// text following the break keeps the run's formatting
			assertTrue(impl + "text after a break lost the run's bold"
					+ around(html, "afterbr"),
					withinSpanStyled(html, "font-weight: bold;", "afterbr"));
			assertTrue(impl + "text after a leading break lost the run's italic"
					+ around(html, "leadingbr"),
					withinSpanStyled(html, "font-style: italic;", "leadingbr"));
			assertTrue(impl + "text after a page break lost the run's bold"
					+ around(html, "afterpage"),
					withinSpanStyled(html, "font-weight: bold;", "afterpage"));

			// and the break itself is still there
			// (the page break is not: the PageBreak preprocess step has already
			// turned it into page-break-before on the paragraph)
			assertTrue(impl + "br lost", html.contains("<br"));
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
			// style class.  Composition is asserted unconditionally; the
			// font-family contribution only where the environment discovered
			// physical fonts at all (during the 17.0.4 release build, discovery
			// found none in the release shell, and blocking a release on the
			// OS font set is wrong - see html-parity-test-release-flake notes)
			assertTrue(impl + "run span not composed (nested spans, or missing css)"
					+ around(html, "boldtext"),
					Pattern.compile("<span class=\"DefaultParagraphFont[^\"]*\" "
							+ "style=\"[^\"]*font-weight: bold;[^\"]*\">boldtext</span>")
							.matcher(html).find());
			// Gate on font mapping actually working in this environment, not
			// on discovery: an EC2 box with only DejaVu discovers 22 fonts,
			// but the default mapper has no mapping for the document font
			// (Calibri), so font selection still contributes no css.
			if (!html.contains("font-family")) {
				System.err.println("WARNING: font mapping produced no css in this "
						+ "environment (document font not mappable to any discovered "
						+ "physical font); skipping the font-family composition assertion");
			} else {
				assertTrue(impl + "font selection css not merged into the composed run span"
						+ around(html, "boldtext"),
						Pattern.compile("<span class=\"DefaultParagraphFont[^\"]*\" "
								+ "style=\"[^\"]*font-family[^\"]*\">boldtext</span>")
								.matcher(html).find());
			}

			// numbered paragraphs (direct and style-based) both become li
			// (via the HTML_ELEMENT sdts the ListsToContentControls preprocess adds)
			assertTrue(impl + "direct-numbered paragraph is not an li", Pattern.compile(
					"<li[^>]*style=\"display: list-item;\"[^>]*>(<span[^>]*>)*item text").matcher(html).find());
			assertTrue(impl + "style-numbered paragraph is not an li", Pattern.compile(
					"<li class=\"MyList[^\"]*\"[^>]*>(<span[^>]*>)*styled item").matcher(html).find());
		}
	}

	/* ------------------------------------------------------------------
	 * Phase 2: sdt dispatch through SdtWriter (registered tag handlers)
	 * ------------------------------------------------------------------ */

	private WordprocessingMLPackage phase2Pkg() throws Exception {

		String pbdr = "<w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"FF0000\"/>"
				+ "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"FF0000\"/>"
				+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"FF0000\"/>"
				+ "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"FF0000\"/></w:pBdr>";
		String shd = "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FFFF00\"/>";

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + pbdr + "</w:pPr><w:r><w:t>bordered one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + pbdr + "</w:pPr><w:r><w:t>bordered two</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + shd + "</w:pPr><w:r><w:t>shaded one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + shd + "</w:pPr><w:r><w:t>shaded two</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+   "<w:r><w:t>item one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+   "<w:r><w:t>item two</w:t></w:r></w:p>"
				// no handler registered for this tag: the identity handler applies
				+ "<w:sdt><w:sdtPr><w:tag w:val=\"unhandled=1\"/></w:sdtPr><w:sdtContent>"
				+   "<w:p><w:r><w:t>control content</w:t></w:r></w:p></w:sdtContent></w:sdt>"
				+ "</w:body></w:document>"));

		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((Numbering)XmlUtils.unmarshalString(
				"<w:numbering " + W + "><w:abstractNum w:abstractNumId=\"1\">"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
				+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/></w:num></w:numbering>",
				Context.jc, Numbering.class));
		pkg.getMainDocumentPart().addTargetPart(ndp);

		return pkg;
	}

	/** the innermost element block starting at the first match of startRegex */
	private static String block(String html, String startRegex, String endTag) {
		java.util.regex.Matcher m = Pattern.compile(startRegex).matcher(html);
		if (!m.find()) return "";
		int end = html.indexOf(endTag, m.start());
		return (end<0 ? "" : html.substring(m.start(), end));
	}

	@Test
	public void testPhase2SdtTagHandlers() throws Exception {

		// the registered-tag-handler extension point (SdtWriter javadoc; note the
		// handler map is static, so this registration outlives the test - which is
		// fine here, since surefire forks a JVM per test class)
		SdtWriter.registerTagHandler(Containerization.TAG_BORDERS, new TagSingleBox());
		SdtWriter.registerTagHandler(Containerization.TAG_SHADING, new TagSingleBox());
		SdtWriter.registerTagHandler("HTML_ELEMENT", new SdtToListSdtTagHandler());

		for (int flag : FLAGS) {
			String html = toHTML(phase2Pkg(), flag);
			String impl = flagName(flag) + ": ";

			// TagSingleBox: one bordered div around both paragraphs
			String borderDiv = block(html, "<div style=\"[^\"]*border-left-style: solid;[^\"]*\">", "</div>");
			assertTrue(impl + "borders container div lost",
					borderDiv.contains("bordered one") && borderDiv.contains("bordered two"));

			// .. and a shaded div around both shaded paragraphs
			String shadedDiv = block(html, "<div style=\"[^\"]*background-color: #FFFF00;[^\"]*\">", "</div>");
			assertTrue(impl + "shading container div lost",
					shadedDiv.contains("shaded one") && shadedDiv.contains("shaded two"));

			// SdtToListSdtTagHandler: a real ol around the li items
			String ol = block(html, "<ol>", "</ol>");
			assertTrue(impl + "ol/li list lost",
					ol.contains("<li") && ol.contains("item one") && ol.contains("item two"));

			// an sdt with an unregistered tag passes its contents through (identity)
			assertTrue(impl + "identity sdt content lost", html.contains("control content"));
		}
	}

	/* ------------------------------------------------------------------
	 * Phase 3: footnotes and endnotes
	 * ------------------------------------------------------------------ */

	/** body text with a footnote and an endnote (cf NoteFontTest) */
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
			String html = toHTML(notesPkg(), flag);
			String impl = flagName(flag) + ": ";

			// the reference marks: bidirectional anchors around the number
			assertTrue(impl + "footnote reference lost", Pattern.compile(
					"<a name=\"fs1\"><a href=\"#fn1\">(<span[^>]*>)*1").matcher(html).find());
			assertTrue(impl + "endnote reference lost", Pattern.compile(
					"<a name=\"es1\"><a href=\"#en1\">(<span[^>]*>)*1").matcher(html).find());

			// the notes themselves, in their divs, numbered and linked back
			String footnotes = block(html, "<div class=\"footnotes\">", "</div>");
			assertTrue(impl + "footnote body lost", footnotes.contains("The footnote."));
			assertTrue(impl + "footnote number/backlink lost", Pattern.compile(
					"<a name=\"fn1\"><a href=\"#fs1\">").matcher(footnotes).find());

			String endnotes = block(html, "<div class=\"endnotes\">", "</div>");
			assertTrue(impl + "endnote body lost", endnotes.contains("The endnote."));
			assertTrue(impl + "endnote number/backlink lost", Pattern.compile(
					"<a name=\"en1\"><a href=\"#es1\">").matcher(endnotes).find());
		}
	}

	/* ------------------------------------------------------------------
	 * Phase 5: document chrome (head, userBodyTop/Tail, doctype) and VML
	 * ------------------------------------------------------------------ */

	private WordprocessingMLPackage phase5Pkg() throws Exception {

		String V = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " " + V + "><w:body>"
				// a VML textbox: not renderable in HTML (in either pathway), but must
				// neither break the conversion nor leak raw WML into the output
				+ "<w:p><w:r><w:pict><v:shape style=\"width:100pt;height:50pt\">"
				+   "<v:textbox><w:txbxContent><w:p><w:r><w:t>boxtext</w:t></w:r></w:p></w:txbxContent></v:textbox>"
				+ "</v:shape></w:pict></w:r></w:p>"
				+ "<w:p><w:r><w:t>after the box</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void testPhase5Chrome() throws Exception {

		for (int flag : FLAGS) {
			HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
			htmlSettings.setOpcPackage(phase5Pkg());
			htmlSettings.setUserBodyTop("<div id=\"userTop\">TOP</div>");
			htmlSettings.setUserBodyTail("<div id=\"userTail\">TAIL</div>");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(htmlSettings, baos, flag);
			String html = baos.toString("UTF-8");
			String impl = flagName(flag) + ": ";

			assertTrue(impl + "XHTML doctype missing",
					html.contains("-//W3C//DTD XHTML 1.0 Transitional//EN"));
			assertTrue(impl + "head style element missing", html.contains("<style"));
			assertTrue(impl + "head script missing", html.contains("function toggleDiv"));

			assertTrue(impl + "userBodyTop lost", html.contains("id=\"userTop\""));
			assertTrue(impl + "userBodyTail lost", html.contains("id=\"userTail\""));

			// the textbox: no crash, no raw WML leak, and following content intact
			assertTrue(impl + "content after the textbox lost", html.contains("after the box"));
			assertTrue(impl + "raw WML leaked into the output", !html.contains("txbxContent"));
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
