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
	 *  them keeps it (@since 17.1.0) */
	private static final String DOT_TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"dots\"/>";

	/** what tabToFO writes for a paragraph whose leader is a rule (w:leader hyphen,
	 *  underscore or heavy) */
	private static final String RULE_TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"rule\"/>";

	/** the same as DOT_TAB, with dots of 6pt rather than 7.2pt, so that a tab starting on a
	 *  multiple of the text's own advance need not start on the dots' grid */
	private static final String DOT_TAB_10 =
			"<fo:leader docx4j:tab=\"1\" font-size=\"10pt\" leader-length=\"0pt\" leader-pattern=\"dots\"/>";

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

	/** The blank each dot leader opens with, in points: Word's dots sit on a grid fixed to
	 *  the reference area, so the leader's area is that blank followed by the dots
	 *  (LBP.PhasedLeaderArea, written by the area tree as an inlineparent of the two).
	 *  A leader already on the grid is not wrapped and contributes nothing here. */
	private static List<Double> leaderPhases(String fo) throws Exception {
		List<Double> out = new ArrayList<>();
		NodeList parents = area(fo).getElementsByTagName("inlineparent");
		for (int i = 0; i < parents.getLength(); i++) {
			List<Element> kids = childElements((Element) parents.item(i));
			if (kids.size() == 2 && "space".equals(kids.get(0).getLocalName())
					&& "inlineparent".equals(kids.get(1).getLocalName())) {
				out.add(round(ipd(kids.get(0), "ipd")));
			}
		}
		return out;
	}

	private static List<Element> childElements(Element el) {
		List<Element> out = new ArrayList<>();
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) out.add((Element) children.item(i));
		}
		return out;
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
		// the only stop is at 7600 twips = 380pt, past the 340pt right edge of a line with
		// a 60pt right indent but inside the 400pt reference area: Word puts the text on it
		// anyway and the line runs into the indent rather than wrapping (measured on a
		// w:ind right=360 footer whose right stop is the full text width).  Past the
		// reference area the stop is unreachable instead; see
		// aTabThatReachesNoStopBreaksTheLine.  @since 17.1.0 (the indent)
		assertEquals(at(0, 380),
				wordStarts(fo("7600:left:none", "0:0:.", "end-indent=\"60pt\"", "abc" + TAB + "x")));
	}

	/**
	 * A tab which can reach no stop before the end of the reference area breaks the line:
	 * the text after it starts the next one, with the tab measured again from there.
	 *
	 * <p>Measured on the {@code tab-clamp-right} probe, whose left stop at 9355 twips is
	 * 16.4pt past an A4 page's text column: Word writes "left stop 9355:" on one line and
	 * "SHORT" on the next at the left indent, with an empty line between them holding the
	 * tab (which reaches nothing from the line's start either).  On the
	 * {@code tab-leader-trailing} probe the second of two trailing tabs reaches nothing,
	 * and the "12" after it lands on the first stop the re-measured tab reaches on the
	 * next line - 360 twips, x=90.05 with a 72pt margin, not the left indent.</p>
	 *
	 * @since 17.1.0
	 */
	@Test
	public void aTabThatReachesNoStopBreaksTheLine() throws Exception {
		// "abcdefghij" is 72pt; the only stop is at 450pt, past the 400pt edge, so the tab
		// breaks the line and from its start reaches the 36pt one instead
		assertEquals(at(0, 36),
				wordStarts(fo("720:left:none;9000:left:none", "0:0:.", null,
						"abcdefghij" + TAB + "x")));
		// with no stop reachable from the line's start either, the tab has a line to
		// itself and the text starts the one after it
		assertEquals(at(0, 0),
				wordStarts(fo("9000:left:none", "0:0:.", null, "abc" + TAB + "x")));
	}

	/**
	 * Two tabs which reach no stop nonetheless keep their line.
	 *
	 * <p>A tab whose stop draws a <b>leader</b> fills to the end of the line: measured on a
	 * corpus prospectus whose table-of-contents entries sit in cells 443pt wide against a
	 * 12000-twip left stop with dots, 150pt past the cell, Word draws each entry's dots to
	 * the cell's edge on the entry's own line.  A tab with <b>nothing after it</b> has
	 * nothing to move to the next line: measured on a corpus header of a picture and seven
	 * tabs whose last two reach nothing, where Word's header is shorter than one line of
	 * that paragraph.  And where what precedes the tab does not itself fit, the ordinary
	 * greedy break wins - measured on a corpus form whose cell holds
	 * {@code 1.1<tab>Technische Freigabe erteilt<tab>o}, which Word breaks before
	 * "erteilt".</p>
	 *
	 * @since 17.1.0
	 */
	@Test
	public void aLeaderTabAndAnOverfullLineDoNotBreakAtTheTab() throws Exception {
		// the stop at 450pt is past the 400pt edge, but its leader runs to the edge rather
		// than taking the text to the next line (a rule leader, so that the leader's own
		// area is one and wordStarts does not count its dots as words)
		assertEquals(at(0, 450),
				wordStarts(fo("9000:left:hyphen", "0:0:.", null, "abcdefghij" + RULE_TAB + "x")));
		// 60 glyphs are 432pt, past the 400pt line before the tab is even reached: the tab
		// does not break a line which is over-full already, and runs on to its stop.
		// (Word's emergency break is off here: a 432pt word on a 400pt line is exactly
		// what it breaks, and this case is about the tab, not the word - see
		// EmergencyBreakTest.  @since 17.1.0)
		System.setProperty(WordLayoutCustomizer.EMERGENCY_BREAK, "false");
		try {
			assertEquals(at(0, 450),
					wordStarts(fo("9000:left:none", "0:0:.", null,
							"abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij"
							+ TAB + "x")));
		} finally {
			System.clearProperty(WordLayoutCustomizer.EMERGENCY_BREAK);
		}
		// and a trailing tab in a header has nothing to move: it stays on the line it is
		// on (one line); in the flow it takes a line of its own (two) - the
		// tab-trailing-cell probe, whose rows with the tab are two lines tall in Word
		// where the next default stop is past the cell, and one without it.  @since 17.1.1
		assertEquals(1, lineCount(foStatic("9000:left:none", "0:0:.", "abc" + TAB)));
		assertEquals(2, lineCount(fo("9000:left:none", "0:0:.", null, "abc" + TAB)));
		assertEquals(1, lineCount(fo("9000:left:none", "0:0:.", null, "abc")));
	}

	/** The same block set as a header (fo:static-content of a region-before). */
	private static String foStatic(String tabs, String ind, String content) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:docx4j=\"" + NS + "\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"400pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body margin-top=\"100pt\"/><fo:region-before extent=\"100pt\"/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\">"
				+ "<fo:static-content flow-name=\"xsl-region-before\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " docx4j:tabs=\"" + tabs + "\" docx4j:tab-default=\"720\" docx4j:tab-ind=\"" + ind + "\">" + content + "</fo:block>"
				+ "</fo:static-content>"
				+ "<fo:flow flow-name=\"xsl-region-body\"><fo:block>body</fo:block></fo:flow></fo:page-sequence></fo:root>";
	}

	/** The number of line areas of the first block laid out (the header's, or the flow's
	 *  first). */
	private static int lineCount(String fo) throws Exception {
		Document doc = area(fo);
		NodeList blocks = doc.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			int n = b.getElementsByTagName("lineArea").getLength();
			if (n > 0) return n;
		}
		return 0;
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
		assertEquals(at(0, 380), wordStarts(fo("7600:left:none", "0:0:.",
				"end-indent=\"60pt\" text-align=\"center\"", "abc" + TAB + "x")));
		assertEquals(at(0, 380), wordStarts(fo("7600:left:none", "0:0:.",
				"end-indent=\"60pt\" text-align=\"end\"", "abc" + TAB + "x")));
	}

	/**
	 * The leader a tab draws is the leader of the stop it <em>reaches</em>, which is
	 * known only here: the FO gives every tab of the paragraph the paragraph's own
	 * leader and the line manager keeps it, blanks it, or replaces it.
	 *
	 * <p>Measured on Word 365's PDF of a table of contents whose stops are
	 * 360/540/851 left with no leader and 9990 right with a dot leader: every entry's
	 * dots run to the right stop, whether the entry has one tab or two.  docx4j gave
	 * the n-th tab the n-th stop's leader until 17.1.0, so a one-tab entry took the
	 * first stop's (none) and painted nothing.</p>
	 *
	 * @since 17.1.0
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
	 * @since 17.1.0
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
	 * @since 17.1.0
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
	 * Word draws a tab's leader dots on a grid fixed to the reference area, not from the
	 * end of the text, so a dot leader opens with a blank of less than one dot.
	 *
	 * <p>Measured on the {@code tab-leader-trailing} and {@code tab-leader-resolved}
	 * goldens, whose dots step 3.121pt: all seven leader runs across the two documents
	 * begin and end on one grid anchored at the 72.02pt left margin (to within the 0.05pt
	 * the PDF's rounding allows), and each holds exactly the grid cells falling inside its
	 * tab.  {@code leader-alignment="reference-area"} is what XSL FO offers for it, but
	 * FOP 2.11 honours that in its RTF renderer alone.</p>
	 *
	 * @since 17.1.0
	 */
	@Test
	public void aDotLeaderStartsOnWordsGrid() throws Exception {
		// dots of 6pt (Courier 10pt) after "abc", which ends at 21.6pt: the first grid
		// point at or after that is 24pt, so the leader opens with 2.4pt of blank
		assertEquals(at(2.4),
				leaderPhases(fo("4000:right:dot", "0:0:.", null, "abc" + DOT_TAB_10 + "wxyz")));
		// "abcde" ends at 36pt, already on the grid: no blank, and the area is FOP's own
		assertEquals(at(),
				leaderPhases(fo("4000:right:dot", "0:0:.", null, "abcde" + DOT_TAB_10 + "wxyz")));
		// the dots of a leader whose own advance divides the text's need none either
		assertEquals(at(),
				leaderPhases(fo("4000:right:dot", "0:0:.", null, "abc" + DOT_TAB + "wxyz")));
	}

	/**
	 * Word clamps a centre, right or decimal stop so that the text it aligns ends on the
	 * right indent: unlike a left stop, such a stop does not take the line past the
	 * indent (measured on a centred footer whose centre stop would have taken its text
	 * 11.3pt past the content width - Word draws one line filling the width, where the
	 * unclamped tab overflowed and wrapped onto a second).
	 *
	 * @since 17.1.0
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
		// a left stop past the indent is still honoured (§4.4), and one that fits is
		// unaffected by the clamp
		assertEquals(at(0, 380),
				wordStarts(fo("7600:left:none", "0:0:.", "end-indent=\"60pt\"", "abc" + TAB + "x")));
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
	 * @since 17.1.0
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
