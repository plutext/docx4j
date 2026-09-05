package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Tabs in the middle of a line, through FOP itself, on Courier so every position
 * can be computed: 12pt Courier is 7.2pt per glyph.  A tab is an fo:leader of no
 * length carrying docx4j:tab, and the block carries the paragraph's stops
 * (docx4j:tabs, docx4j:tab-default, docx4j:tab-ind), as
 * XsltFOFunctions.tabToFO / applyTabStopHints emit them.
 */
public class TabStopTest {

	private static final double CHAR = 7.2;    // Courier 12pt advance
	private static final String NS = WordLayoutElementMapping.URI;

	private static final String TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"space\"/>";

	/** what XsltFOFunctions.tabToFO writes for a paragraph one of whose stops has a dot
	 *  leader: every tab of the paragraph gets it, and the line manager decides which of
	 *  them keeps it (@since 17.0.6) */
	private static final String DOT_TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"dots\"/>";

	/** @param tabs docx4j:tabs, "pos:align:leader;..." in twips; "" for none
	 *  @param ind  docx4j:tab-ind, "left:firstLine:separator" in twips */
	private static String fo(String tabs, String ind, String blockAttrs, String content) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:docx4j=\"" + NS + "\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"400pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " docx4j:tabs=\"" + tabs + "\" docx4j:tab-default=\"720\" docx4j:tab-ind=\"" + ind + "\""
				+ (blockAttrs == null ? "" : " " + blockAttrs) + ">" + content + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** FOP's area tree for this FO, with the Word layout managers in place. */
	private static Document area(String fo) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))), new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
	}

	/** The x at which each word of each line starts, in points from the region edge. */
	private static List<Double> wordStarts(String fo) throws Exception {
		Document doc = area(fo);
		List<Double> starts = new ArrayList<>();
		NodeList las = doc.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			Element line = (Element) las.item(i);
			double[] x = { ipd(line, "start-indent") };
			walk(line, x, starts);
		}
		return starts;
	}

	private static void walk(Element el, double[] x, List<Double> starts) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			String name = c.getLocalName();
			if ("text".equals(name)) {
				starts.add(round(x[0]));
				x[0] += ipd(c, "ipd");
			} else if ("space".equals(name) || "leader".equals(name)) {
				x[0] += ipd(c, "ipd");
			} else {
				walk(c, x, starts);
			}
		}
	}

	/** What each tab of each line drew, in order: "dots", "rule" or "space" (a leader of
	 *  no pattern, which is also what a blanked one becomes).  A dot leader is a
	 *  FilledArea, which the area tree writes as an inlineparent of repeated dots. */
	private static List<String> leaders(String fo) throws Exception {
		List<String> found = new ArrayList<>();
		NodeList las = area(fo).getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			collectLeaders((Element) las.item(i), found);
		}
		return found;
	}

	private static void collectLeaders(Element el, List<String> found) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			String name = c.getLocalName();
			if ("text".equals(name)) continue;              // words, not leaders
			if ("space".equals(name)) found.add("space");
			else if ("leader".equals(name)) found.add("rule");
			else if ("inlineparent".equals(name)) found.add("dots");
			else collectLeaders(c, found);
		}
	}

	/** The baseline each dot of each dot leader is drawn on, in points from the line's
	 *  top: the leader's area carries the offset, the dot its baseline within it. */
	private static List<Double> dotBaselines(String fo) throws Exception {
		List<Double> found = new ArrayList<>();
		NodeList parents = area(fo).getElementsByTagName("inlineparent");
		for (int i = 0; i < parents.getLength(); i++) {
			Element parent = (Element) parents.item(i);
			double offset = ipd(parent, "offset");
			NodeList texts = parent.getElementsByTagName("text");
			for (int j = 0; j < texts.getLength(); j++) {
				Element text = (Element) texts.item(j);
				if (!".".equals(text.getTextContent())) continue;
				found.add(round(offset + ipd(text, "baseline")));
			}
		}
		return found;
	}

	private static List<String> list(String... names) {
		List<String> out = new ArrayList<>();
		for (String n : names) out.add(n);
		return out;
	}

	private static double ipd(Element el, String attr) {
		String v = el.getAttribute(attr);
		if (v.length() == 0) return 0;
		return Double.parseDouble(v) / 1000.0;
	}

	private static double round(double v) {
		return Math.round(v * 100) / 100.0;
	}

	private static List<Double> at(double... xs) {
		List<Double> out = new ArrayList<>();
		for (double x : xs) out.add(round(x));
		return out;
	}

	@Test
	public void defaultStopsAreEvery36pt() throws Exception {
		// "abc" is 21.6pt, so the tab runs to the second default stop, 36pt
		assertEquals(at(0, 36), wordStarts(fo("", "0:0:.", null, "abc" + TAB + "x")));
		// a longer word passes 36pt: the next stop is 72pt.  A stop exactly at x is not
		// the "next" one, so 10 characters (72pt) would reach 108pt.
		assertEquals(at(0, 72), wordStarts(fo("", "0:0:.", null, "abcdefghi" + TAB + "x")));
		assertEquals(at(0, 108), wordStarts(fo("", "0:0:.", null, "abcdefghij" + TAB + "x")));
	}

	@Test
	public void twoTabsInARowGoToConsecutiveStops() throws Exception {
		assertEquals(at(0, 72), wordStarts(fo("", "0:0:.", null, "abc" + TAB + TAB + "x")));
	}

	@Test
	public void aCustomStopClearsTheDefaultStopsBeforeIt() throws Exception {
		// one left stop at 2000 twips = 100pt: the first tab reaches it although the
		// default grid would have offered 36pt, and the grid resumes beyond it
		assertEquals(at(0, 100, 108),
				wordStarts(fo("2000:left:none", "0:0:.", null, "abc" + TAB + "x" + TAB + "y")));
	}

	@Test
	public void aRightStopPutsTheEndOfTheFollowingTextOnIt() throws Exception {
		// stop at 4000 twips = 200pt, "wxyz" is 28.8pt wide -> it starts at 171.2pt
		assertEquals(at(0, 200 - 4 * CHAR),
				wordStarts(fo("4000:right:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	@Test
	public void aCentreStopPutsTheMiddleOfTheFollowingTextOnIt() throws Exception {
		assertEquals(at(0, 200 - 2 * CHAR),
				wordStarts(fo("4000:center:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	@Test
	public void aDecimalStopAlignsTheDecimalSeparator() throws Exception {
		// "12.34": the separator is the third character, so the text starts 2 chars left
		assertEquals(at(0, 200 - 2 * CHAR),
				wordStarts(fo("4000:decimal:none", "0:0:.", null, "abc" + TAB + "12.34")));
		// no separator: Word right-aligns it
		assertEquals(at(0, 200 - 5 * CHAR),
				wordStarts(fo("4000:decimal:none", "0:0:.", null, "abc" + TAB + "12345")));
	}

	@Test
	public void aRightStopTheTextCannotReachCollapsesTheTab() throws Exception {
		// the stop is at 36pt but "wxyz" ends at 21.6 + 28.8 = 50.4: Word cannot move
		// backwards, so the tab adds nothing
		assertEquals(at(0, 3 * CHAR),
				wordStarts(fo("720:right:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	@Test
	public void aStopPastTheRightIndentIsStillHonoured() throws Exception {
		// the only stop is at 9000 twips = 450pt, past the 400pt right edge: Word puts the
		// text on it anyway and the line runs into the indent rather than wrapping
		// (measured on a w:ind right=360 footer whose right stop is the full text width)
		assertEquals(at(0, 450),
				wordStarts(fo("9000:left:none", "0:0:.", null, "abc" + TAB + "x")));
	}

	/**
	 * Word sizes the tabs on a line as if the line began at the left indent, and then
	 * aligns the whole line - the tabs' widths counted in - by the paragraph's w:jc.
	 *
	 * <p>Measured on the {@code tab-jc} probe (A4, Times New Roman 12pt, 1in margins,
	 * so a 451.3pt line centred on 297.65 and ending at 523.35): a centred paragraph of
	 * text 87.7pt wide followed by a tab is drawn at 243.7..331.4 - the text plus the
	 * 20.3pt the tab takes to the 180pt default stop, 108pt in all, centred - and the
	 * same text right-aligned at 415.6..496.6.  With a custom stop at 6000 twips the
	 * line is 300pt whatever it holds, and Word draws it at 147.7 centred and 223.5
	 * right-aligned.  docx4j drew all of them flush left.</p>
	 */
	@Test
	public void aLineHoldingATabIsAlignedByTheParagraphsJc() throws Exception {
		// "abc" (21.6pt) + a tab to the 36pt default stop + "x" (7.2pt) = 43.2pt of line
		String content = "abc" + TAB + "x";
		double line = 43.2;
		assertEquals("flush left by default", at(0, 36),
				wordStarts(fo("", "0:0:.", null, content)));
		double centred = (400 - line) / 2;
		assertEquals(at(centred, centred + 36),
				wordStarts(fo("", "0:0:.", "text-align=\"center\"", content)));
		double right = 400 - line;
		assertEquals(at(right, right + 36),
				wordStarts(fo("", "0:0:.", "text-align=\"end\"", content)));
	}

	/**
	 * A justified line holding a tab is not stretched: the tab absorbs the slack, so
	 * the line is laid out from the start (which is also what its last line does).
	 */
	@Test
	public void aJustifiedLineHoldingATabIsLaidOutFromTheStart() throws Exception {
		assertEquals(at(0, 36),
				wordStarts(fo("", "0:0:.", "text-align=\"justify\"", "abc" + TAB + "x")));
	}

	/**
	 * A tab stop past the available width fills the line, so w:jc cannot move it any
	 * further: the stop at 9000 twips (450pt) takes the line past the 400pt edge, and
	 * a centred paragraph still starts at 0 rather than being pushed left of the margin.
	 */
	@Test
	public void aLineRunningPastTheEdgeIsNotMovedByJc() throws Exception {
		assertEquals(at(0, 450),
				wordStarts(fo("9000:left:none", "0:0:.", "text-align=\"center\"", "abc" + TAB + "x")));
		assertEquals(at(0, 450),
				wordStarts(fo("9000:left:none", "0:0:.", "text-align=\"end\"", "abc" + TAB + "x")));
	}

	/**
	 * The leader a tab draws is the leader of the stop it <em>reaches</em>, which is
	 * known only here: the FO gives every tab of the paragraph the paragraph's own
	 * leader and the line manager keeps it, blanks it, or replaces it.
	 *
	 * <p>Measured on Word 365's PDF of a table of contents whose stops are
	 * 360/540/851 left with no leader and 9990 right with a dot leader: every entry's
	 * dots run to the right stop, whether the entry has one tab or two.  docx4j gave
	 * the n-th tab the n-th stop's leader until 17.0.6, so a one-tab entry took the
	 * first stop's (none) and painted nothing.</p>
	 *
	 * @since 17.0.6
	 */
	@Test
	public void theLeaderIsTheOneOfTheStopTheTabReaches() throws Exception {
		String stops = "720:left:none;4000:right:dot";     // 36pt left, 200pt right + dots
		// "abcdefghij" is 72pt, past the left stop: the tab reaches the dot stop
		assertEquals(list("dots"), leaders(fo(stops, "0:0:.", null, "abcdefghij" + DOT_TAB + "9")));
		// "a" is 7.2pt: the same paragraph's tab reaches the left stop, which has none
		assertEquals(list("space"), leaders(fo(stops, "0:0:.", null, "a" + DOT_TAB + "9")));
	}

	/**
	 * A trailing tab: the first tab reaches the dot stop and draws its dots, the second
	 * runs on to the next default stop, which has no leader.  (Measured on a Word TOC
	 * whose entries end {@code <w:tab/><w:t/><w:tab/>}: the dots stop at the right stop.)
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aTrailingTabDrawsTheLeaderOfTheStopItReaches() throws Exception {
		assertEquals(list("dots", "space"),
				leaders(fo("720:left:none;4000:right:dot", "0:0:.", null, "abcdefghij" + DOT_TAB + DOT_TAB)));
	}

	/**
	 * A leader the FO asked FOP for as a space, whose resolved stop turns out to draw
	 * dots - a paragraph mixing dot and rule stops - is given a dot leader built here.
	 * It hangs on the alignment context FOP made for the pattern the FO asked for, so
	 * its dots take that context's height (the leader's rule thickness) as their
	 * baseline rather than their own; measured against the leader FOP builds itself,
	 * both land on the line's baseline, 7.548pt.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aLeaderReplacedWithDotsSitsWhereFopsOwnDotsSit() throws Exception {
		String content = "abc" + TAB + "wxyz";
		String dots = "abc" + DOT_TAB + "wxyz";
		List<Double> replaced = dotBaselines(fo("4000:right:dot", "0:0:.", null, content));
		List<Double> native_ = dotBaselines(fo("4000:right:dot", "0:0:.", null, dots));
		assertEquals("no dots were drawn", 20, native_.size());
		assertEquals(native_, replaced);
	}

	/**
	 * Word clamps a centre, right or decimal stop so that the text it aligns ends on the
	 * right indent: unlike a left stop, such a stop does not take the line past the
	 * indent (measured on a centred footer whose centre stop would have taken its text
	 * 11.3pt past the content width - Word draws one line filling the width, where the
	 * unclamped tab overflowed and wrapped onto a second).
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aCentreOrRightStopIsClampedAtTheRightIndent() throws Exception {
		// centre stop at 9000 twips = 450pt, past the 400pt edge: "abc" (21.6) + the tab
		// + "wxyzwxyz" (57.6) would centre the text on 450, so the tab is cut to end the
		// text on 400
		assertEquals(at(0, 400 - 8 * CHAR),
				wordStarts(fo("9000:center:none", "0:0:.", null, "abc" + TAB + "wxyzwxyz")));
		assertEquals(at(0, 400 - 4 * CHAR),
				wordStarts(fo("9000:right:none", "0:0:.", null, "abc" + TAB + "wxyz")));
		assertEquals(at(0, 400 - 5 * CHAR),
				wordStarts(fo("9000:decimal:none", "0:0:.", null, "abc" + TAB + "12345")));
		// a left stop past the edge is still honoured (§4.4), and one that fits is
		// unaffected by the clamp
		assertEquals(at(0, 450), wordStarts(fo("9000:left:none", "0:0:.", null, "abc" + TAB + "x")));
		assertEquals(at(0, 200 - 4 * CHAR),
				wordStarts(fo("4000:right:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	/**
	 * A right stop whose text is a page number FOP has not resolved yet still puts the
	 * number on the stop: FOP measures an unresolved fo:page-number-citation as the
	 * placeholder "MMM", and the width it gives up when it resolves is the tab's.
	 *
	 * <p>Measured on Word's PDF of a 311-page document (stops 1320 left, 9350 right with
	 * dots, margin 72pt): the page number's right edge is on the stop, 539.74pt, on
	 * every line, where docx4j's line ended 10.7pt short - "MMM" 20.71pt against "61"
	 * 10.18pt in DejaVu Sans 8pt.</p>
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aRightStopPutsAnUnresolvedPageNumberOnTheStop() throws Exception {
		// "abc" + a tab to the 200pt right stop + the citation, which resolves to "2"
		// (7.2pt): without the fix the tab keeps the room "MMM" (21.6pt) needed and the
		// number lands at 178.4
		String fo = fo("4000:right:none", "0:0:.", null,
				"abc" + TAB + "<fo:page-number-citation ref-id=\"target\"/>")
				.replace("</fo:flow>", "<fo:block break-before=\"page\" id=\"target\">x</fo:block></fo:flow>");
		assertEquals(at(0, 200 - CHAR, 0), wordStarts(fo));
	}

	@Test
	public void aHangingIndentMakesAStopAtTheLeftIndent() throws Exception {
		// left 1440 twips (72pt), hanging 720 (36pt): the first line starts 36pt in and
		// a tab there reaches the left indent, 72pt.  The area tree gives x within the
		// block, whose own start-indent is the 72pt left indent, so those are -36 and 0.
		assertEquals(at(-36, 0),
				wordStarts(fo("", "1440:-720:.", "start-indent=\"72pt\" text-indent=\"-36pt\"",
						"ab" + TAB + "x")));
	}
}
