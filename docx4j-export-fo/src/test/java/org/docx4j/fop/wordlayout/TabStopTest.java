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

	/** what tabToFO writes for a paragraph whose leader is a rule (w:leader heavy;
	 *  hyphen and underscore are drawn as their own characters since 17.2.0, see
	 *  WordLayoutCustomizer.leaderCharacters) */
	private static final String RULE_TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"rule\"/>";

	/** the same as DOT_TAB, with dots of 6pt rather than 7.2pt, so that a tab starting on a
	 *  multiple of the text's own advance need not start on the dots' grid */
	private static final String DOT_TAB_10 =
			"<fo:leader docx4j:tab=\"1\" font-size=\"10pt\" leader-length=\"0pt\" leader-pattern=\"dots\"/>";

	/** the same again with dots of 6.6pt (Courier 11pt), an advance which is <b>not</b> a
	 *  whole number of the 1/300 inch cells Word counts its leader grid in */
	private static final String DOT_TAB_11 =
			"<fo:leader docx4j:tab=\"1\" font-size=\"11pt\" leader-length=\"0pt\" leader-pattern=\"dots\"/>";

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
				if (!isTabSpace(c)) starts.add(round(x[0]));
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
			if ("text".equals(name)) {
				// a tab of no pattern is the space character Word writes for it, which
				// reaches the area tree as a text area (@since 17.2.0)
				if (isTabSpace(c)) found.add("space");
				continue;                                   // otherwise words, not leaders
			}
			if ("space".equals(name)) found.add("space");
			else if ("leader".equals(name)) found.add("rule");
			else if ("inlineparent".equals(name)) found.add("dots");
			else collectLeaders(c, found);
		}
	}

	/** A text area holding nothing but whitespace is a tab's own space and not a word:
	 *  since 17.2.0 a tab writes the space character Word writes for its advance rather
	 *  than jumping the pen ({@code WordLayoutCustomizer.tabSpaces}), and the blank at
	 *  each end of a leader run is written the same way. */
	private static boolean isTabSpace(Element text) {
		String t = text.getTextContent();
		return t != null && t.length() > 0 && t.trim().isEmpty();
	}

	/** The baseline each dot of each dot leader is drawn on, in points from the line's
	 *  top: the leader's area carries the offset, the dot its baseline within it. */
	private static List<Double> dotBaselines(String fo) throws Exception {
		List<Double> found = new ArrayList<>();
		NodeList parents = area(fo).getElementsByTagName("inlineparent");
		for (int i = 0; i < parents.getLength(); i++) {
			Element parent = (Element) parents.item(i);
			// the innermost only: the repeating area sits inside a wrapper carrying the
			// grid blank and the space at each end of the run, and both are inlineparents.
			// The offset is then the chain's, not one area's.
			if (parent.getElementsByTagName("inlineparent").getLength() > 0) continue;
			double offset = 0;
			for (Node up = parent; up instanceof Element; up = up.getParentNode()) {
				if (!"inlineparent".equals(up.getLocalName())) break;
				offset += ipd((Element) up, "offset");
			}
			NodeList texts = parent.getElementsByTagName("text");
			for (int j = 0; j < texts.getLength(); j++) {
				Element text = (Element) texts.item(j);
				if (!".".equals(text.getTextContent())) continue;
				found.add(round(offset + ipd(text, "baseline")));
			}
		}
		return found;
	}

	/** The step each dot leader repeats on, in points, once per leader: Word's grid pitch,
	 *  which is the character's advance rounded to the 1/300 inch its layout works in and
	 *  carried by the repeating unit itself. */
	private static List<Double> dotUnitWidths(String fo) throws Exception {
		List<Double> out = new ArrayList<>();
		NodeList parents = area(fo).getElementsByTagName("inlineparent");
		for (int i = 0; i < parents.getLength(); i++) {
			Element parent = (Element) parents.item(i);
			if (parent.getElementsByTagName("inlineparent").getLength() > 0) continue;
			NodeList texts = parent.getElementsByTagName("text");
			for (int j = 0; j < texts.getLength(); j++) {
				Element text = (Element) texts.item(j);
				if (!".".equals(text.getTextContent())) continue;
				Double w = round(ipd(text, "ipd"));
				if (!out.contains(w)) out.add(w);
			}
		}
		return out;
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
			if (kids.isEmpty()) continue;
			Element first = kids.get(0);
			// the blank is a space area, or (17.2.0, tabSpaces) the space character Word
			// writes there; a blank of no width is Word's grid already met, not a phase
			boolean blank = "space".equals(first.getLocalName())
					|| ("text".equals(first.getLocalName()) && isTabSpace(first));
			if (!blank || kids.size() < 2 || !"inlineparent".equals(kids.get(1).getLocalName())) continue;
			double phase = round(ipd(first, "ipd"));
			if (phase > 0) out.add(phase);
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

	/**
	 * A rule leader is drawn <b>on its own line</b>, not at the top of it.
	 *
	 * <p>{@code w:leader="heavy"} is the one kind Word draws as a line rather than as
	 * characters (CR-001 batch 45 made underscore and hyphen the characters they are), so
	 * it is the one kind that still asks FOP for {@code leader-pattern="rule"} and gets a
	 * drawn path with nothing in the text layer.  Where that path lands is worth a guard:
	 * FOP hangs the area on the leader's own alignment context, and its centre comes half
	 * its thickness above the text's baseline.  Measured on this FO: the rule's offset is
	 * 6.548pt and its thickness 1.000pt against the line's baseline of 7.548pt, so its
	 * centre is 7.048; in a rendered PDF of the same shape the stroke is at y=158.300 where
	 * the entry's baseline is 158.800.  A leader hung at the line's top would come out at
	 * half its thickness instead, seven points away.</p>
	 *
	 * @since 17.2.0
	 */
	@Test
	public void aRuleLeaderIsDrawnOnItsLine() throws Exception {
		System.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, "false");
		Element line;
		try {
			line = (Element) area(fo("9000:left:heavy", "0:0:.", null,
					"abcdefghij" + RULE_TAB + "x")).getElementsByTagName("lineArea").item(0);
		} finally {
			System.clearProperty(WordLayoutCustomizer.LEADER_CHARACTERS);
		}
		Element rule = (Element) line.getElementsByTagName("leader").item(0);
		Element text = (Element) line.getElementsByTagName("text").item(0);
		double centre = ipd(rule, "offset") + ipd(rule, "ruleThickness") / 2;
		double baseline = ipd(text, "offset") + ipd(text, "baseline");
		org.junit.Assert.assertTrue("the rule is at " + centre + "pt and the baseline at "
				+ baseline + "pt", Math.abs(centre - baseline) <= 1.0);
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
		// than taking the text to the next line.  Word draws every leader as characters
		// (CR-001 batch 45), so this case asks for the rule fallback instead
		// (leaderCharacters=false), where the leader's own area is one and wordStarts does
		// not count its glyphs as words
		System.setProperty(WordLayoutCustomizer.LEADER_CHARACTERS, "false");
		try {
			assertEquals(at(0, 450),
					wordStarts(fo("9000:left:heavy", "0:0:.", null, "abcdefghij" + RULE_TAB + "x")));
		} finally {
			System.clearProperty(WordLayoutCustomizer.LEADER_CHARACTERS);
		}
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
		// where the next default stop is past the cell, and one without it.  @since 17.2.0
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
	 * A leader character steps by its own advance <b>rounded to 1/300 inch</b>, the grid
	 * Word's layout works in, and not by the advance itself.
	 *
	 * <p>Read out of the {@code tab-leader-kinds} golden's content stream, where every
	 * leader is drawn in the paragraph mark's font at 11.04pt and the rounding is written
	 * as a character spacing: a full stop and a middle dot advance 2.782pt and are drawn
	 * {@code 0.0979 Tc} apart, twelve cells; a hyphen advances 3.380 and is drawn
	 * {@code -0.0182 Tc} apart, fourteen - the spacing is <em>negative</em>, so the advance
	 * is rounded and not rounded up; an underscore advances 5.500 and is drawn
	 * {@code 0.0221 Tc} apart, twenty-three.</p>
	 *
	 * @since 17.2.0
	 */
	@Test
	public void aLeaderCharacterStepsByItsAdvanceRoundedToWordsGrid() throws Exception {
		assertEquals(2880, LBP.roundToGrid(2782));      // a full stop or a middle dot
		assertEquals(3360, LBP.roundToGrid(3380));      // a hyphen: down, not up
		assertEquals(5520, LBP.roundToGrid(5500));      // an underscore
		// and it reaches the repeating unit: Courier 11pt's dot advances 6.6pt, 27.5
		// cells, which Word would step 6.72
		assertEquals(at(6.72),
				dotUnitWidths(fo("4000:right:dot", "0:0:.", null, "abc" + DOT_TAB_11 + "wxyz")));
	}

	/**
	 * A leader run opens on a whole multiple of its own step measured from the <b>page's</b>
	 * left edge, the tab's start having been taken down to the 1/300 inch cell first.
	 *
	 * <p>Every run on the {@code tab-leader-kinds} golden does: its dot and middle-dot runs
	 * open at 25, 45, 47, 57 and 60 steps of 2.881pt, its hyphen runs at 22, 45 and 46 of
	 * 3.3612, its underscore runs at 14, 23, 26, 27, 30, 32 and 59 of 5.522.  The cases
	 * below are that golden's own numbers, in millipoints, each taken from the space Word
	 * writes at the tab's start.  Taking the start down to the cell first is what puts the
	 * P09 run one step back, at 46 rather than 47, where the text before it ends a fraction
	 * of a cell past the 46th: Word opens the run a touch behind the text there and writes
	 * no space at all, which is why the phase is 0.</p>
	 *
	 * @since 17.2.0
	 */
	@Test
	public void aLeaderRunOpensOnAWholeMultipleOfItsStepFromThePageEdge() throws Exception {
		assertEquals(129600 - 127970, LBP.gridPhase(127970, 2880));   // P04, 45 steps
		assertEquals(135360 - 134690, LBP.gridPhase(134690, 2880));   // P05, 47
		assertEquals(151200 - 148150, LBP.gridPhase(148150, 3360));   // P08, 45
		assertEquals(176640 - 172150, LBP.gridPhase(172150, 5520));   // P11, 32
		assertEquals(77280 - 72024, LBP.gridPhase(72024, 5520));      // a leading tab, 14
		assertEquals(0, LBP.gridPhase(154776, 3360));                 // P09, 46 - behind
		// and it reaches the line: "abc" ends at 21.6pt, inside the fourth step of 6.72,
		// so the run opens at 26.88 - four steps from the page edge, this page having no
		// margin - and the blank before it is 5.28
		assertEquals(at(5.28),
				leaderPhases(fo("4000:right:dot", "0:0:.", null, "abc" + DOT_TAB_11 + "wxyz")));
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

	// ---- the stretching leader of a table-of-contents entry (CR-001 batch 46 item 2) ----

	/** What XsltFOFunctions.tabToFO writes for a table-of-contents entry: a leader which
	 *  stretches to the end of the line rather than one the line manager lays out against
	 *  a stop, marked docx4j:toc-leader so the grid pass knows whose it is.  Courier 11pt,
	 *  whose dot advances 6.6pt - 27.5 of the 1/300 inch cells Word counts in, so it is
	 *  not already on the grid. */
	private static final String TOC_LEADER_11 =
			"<fo:leader docx4j:toc-leader=\"dot\" font-size=\"11pt\" leader-pattern=\"dots\""
			+ " leader-length.minimum=\"12pt\" leader-length.optimum=\"40pt\""
			+ " leader-length.maximum=\"100%\"/>";

	/** The same leader as a <b>rule</b>, which draws no characters and has no grid. */
	private static final String TOC_RULE_11 =
			"<fo:leader docx4j:toc-leader=\"dot\" font-size=\"11pt\" leader-pattern=\"rule\""
			+ " leader-length.minimum=\"12pt\" leader-length.optimum=\"40pt\""
			+ " leader-length.maximum=\"100%\"/>";

	/** And an unmarked stretching leader: one the document did not ask for. */
	private static final String PLAIN_LEADER_11 =
			"<fo:leader font-size=\"11pt\" leader-pattern=\"dots\""
			+ " leader-length.minimum=\"12pt\" leader-length.optimum=\"40pt\""
			+ " leader-length.maximum=\"100%\"/>";

	private static String tocFo(String leader) {
		return fo("", "0:0:.", "text-align=\"justify\" text-align-last=\"justify\"",
				"abc" + leader + "9");
	}

	/**
	 * A table-of-contents entry's stretching leader goes on Word's grid too: its dots step
	 * on the advance rounded to 1/300 inch, and the run opens on a whole multiple of that
	 * step measured from the page's left edge.
	 *
	 * <p>Measured on the {@code tab-leader-kinds} golden (batch 45 &#xa7;C.3): Word's
	 * hyphens sit at 151.270 / 154.630 / 157.990, a step of 3.36 = fourteen cells, where
	 * ours sat at 149.418 / 152.784 on the raw 3.366 anchored on the line.  The anchor is
	 * what a reader sees - Word's run opens 3.214pt after the text and ours opened 1.422pt,
	 * under the 0.25 em a space needs to be read as one.</p>
	 *
	 * <p>Here the page has no margin, so the page edge is the region edge and the numbers
	 * are the test's own: Courier 11pt's dot advances 6.6pt and steps 6.72 (28 cells);
	 * "abc" at 12pt ends at 21.6pt, inside the fourth step, so the run opens at 26.88 -
	 * four steps from the page edge - and the blank before it is 5.28.</p>
	 *
	 * @since 17.2.0
	 */
	@Test
	public void aTocLeaderStepsOnWordsGridAndOpensOnIt() throws Exception {
		assertEquals(at(6.72), dotUnitWidths(tocFo(TOC_LEADER_11)));
		assertEquals(at(26.88 - 21.6), leaderPhases(tocFo(TOC_LEADER_11)));
	}

	/**
	 * And the line is exactly as wide as it was: the dots give up the phase and the blank
	 * in front of them takes it.  The line is already justified when the pass runs and this
	 * leader is what absorbed its slack, so anything else would move the page number.
	 *
	 * @since 17.2.0
	 */
	@Test
	public void gridingATocLeaderDoesNotChangeTheLinesWidth() throws Exception {
		String fo = tocFo(TOC_LEADER_11);
		String off = System.getProperty(WordLayoutCustomizer.LEADER_GRID);
		List<Double> ungridded;
		try {
			System.setProperty(WordLayoutCustomizer.LEADER_GRID, "false");
			ungridded = lineWidths(fo);
		} finally {
			if (off == null) System.clearProperty(WordLayoutCustomizer.LEADER_GRID);
			else System.setProperty(WordLayoutCustomizer.LEADER_GRID, off);
		}
		assertEquals("the line fills the 400pt page either way", at(400), ungridded);
		assertEquals(ungridded, lineWidths(fo));
		// and the wrapper measures what the leader measured: blank + dots, to the point
		assertEquals(at(400), phasedLeaderWidths(fo, 400 - 3 * CHAR - CHAR));
	}

	/**
	 * The same, with the leader where a real entry puts it: inside the {@code fo:inline}s
	 * which carry the entry's size and colour and inside the {@code fo:basic-link} of its
	 * bookmark.  The element the line's sequence holds then names the <b>outermost</b> of
	 * those managers, so the leader has to be reached through the element's position chain
	 * - which is what {@code tabLeader} does for a tab, and what this covers: written
	 * without it, the pass fired on a leader written bare and on no real entry at all.
	 *
	 * @since 17.2.0
	 */
	@Test
	public void aTocLeaderIsFoundInsideTheEntrysInlines() throws Exception {
		String nested = fo("", "0:0:.", "text-align=\"justify\" text-align-last=\"justify\"",
				"<fo:inline font-size=\"12pt\"><fo:basic-link internal-destination=\"t\">"
				+ "<fo:inline color=\"#0563C1\">abc</fo:inline>"
				+ "<fo:inline>" + TOC_LEADER_11 + "9</fo:inline>"
				+ "</fo:basic-link></fo:inline>")
				.replace("</fo:flow>", "<fo:block break-before=\"page\" id=\"t\">x</fo:block></fo:flow>");
		assertEquals(at(6.72), dotUnitWidths(nested));
		assertEquals(at(26.88 - 21.6), leaderPhases(nested));
	}

	/** A rule leader has no characters and so no grid, and an unmarked leader is not ours:
	 *  both are left exactly as FOP built them. */
	@Test
	public void aRuleLeaderAndAnUnmarkedLeaderAreLeftAlone() throws Exception {
		assertEquals(at(), leaderPhases(tocFo(TOC_RULE_11)));
		assertEquals(at(), leaderPhases(tocFo(PLAIN_LEADER_11)));
		assertEquals("an unmarked dot leader keeps FOP's own step, the raw advance",
				at(6.6), dotUnitWidths(tocFo(PLAIN_LEADER_11)));
	}

	/** Each line's ipd, in points. */
	private static List<Double> lineWidths(String fo) throws Exception {
		List<Double> out = new ArrayList<>();
		NodeList las = area(fo).getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			out.add(round(ipd((Element) las.item(i), "ipd")));
		}
		return out;
	}

	/** Each line's ipd again, having first checked that the phased leader on it measures
	 *  {@code leaderWidth}: the blank and the dots together are what the leader was. */
	private static List<Double> phasedLeaderWidths(String fo, double leaderWidth) throws Exception {
		Document doc = area(fo);
		NodeList parents = doc.getElementsByTagName("inlineparent");
		for (int i = 0; i < parents.getLength(); i++) {
			List<Element> kids = childElements((Element) parents.item(i));
			if (kids.size() != 2 || !"space".equals(kids.get(0).getLocalName())) continue;
			if (!"inlineparent".equals(kids.get(1).getLocalName())) continue;
			assertEquals("the blank and the dots are the leader's own width",
					round(leaderWidth),
					round(ipd(kids.get(0), "ipd") + ipd(kids.get(1), "ipd")), 0.001);
		}
		List<Double> out = new ArrayList<>();
		NodeList las = doc.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			out.add(round(ipd((Element) las.item(i), "ipd")));
		}
		return out;
	}
}
