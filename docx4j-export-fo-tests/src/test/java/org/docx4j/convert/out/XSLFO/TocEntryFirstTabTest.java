package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A table-of-contents entry for a numbered heading is <code>number &lt;tab&gt; title
 * &lt;tab&gt; page</code>, and its style declares one stop: the right dot-leader stop at
 * the margin.  Word sends the <b>first</b> tab to whatever stop lies before that one - in
 * the corpus, the implicit stop a hanging indent makes at the left indent - and an
 * implicit stop draws no leader; only the second tab reaches the dot stop.
 *
 * <p>docx4j gave the stretching dot leader to <i>every</i> tab of such a paragraph, so the
 * first one drew dots from the number to the title and pushed the title onto a second
 * line.  Measured (CR-001 batch 43, M21) on a corpus document whose 280 <code>toc 2</code>
 * entries carry <code>w:ind w:left="624" w:hanging="624"</code> and a single
 * <code>&lt;w:tab w:val="right" w:leader="dot" w:pos="9627"/&gt;</code>: Word sets the
 * whole entry on one baseline, pitch 15.9pt, where ours took two lines at 34.2 - two pages
 * of that document.</p>
 *
 * <p>So the stretching leader now belongs to the paragraph's <b>last</b> tab only; an
 * earlier one takes the ordinary zero-length <code>docx4j-tab</code> leader, whose pattern
 * the line manager keeps, blanks or replaces from the stop the tab actually reaches.</p>
 *
 * @since 17.1.1
 */
public class TocEntryFirstTabTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** {@code FopFactoryCustomizer.WORD_LAYOUT_NAMESPACE}, where WordLayoutFixups moves
	 *  the {@code docx4j-tab} hint the line manager reads */
	private static final String WORD_LAYOUT_NS = "http://docx4j.org/fop/word-layout";

	/** the corpus shape: a hanging indent and one right dot-leader stop at the margin */
	private static final String TOC_PPR =
			"<w:pPr>"
			+ "<w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9627\"/></w:tabs>"
			+ "<w:ind w:left=\"624\" w:hanging=\"624\"/>"
			+ "</w:pPr>";

	/** the other corpus shape: the same stop, and <b>no</b> w:ind - so nothing stands
	 *  between the line's start and the dot stop, and Word does paint the leading dots */
	private static final String TOC_PPR_NO_IND =
			"<w:pPr>"
			+ "<w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9627\"/></w:tabs>"
			+ "</w:pPr>";

	private static String run(String text) {
		return "<w:r><w:t>" + text + "</w:t></w:r>";
	}

	private static final String TAB = "<w:r><w:tab/></w:r>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	/** every fo:leader of the document, in order */
	private static List<Element> leaders(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));
		List<Element> found = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "leader");
		for (int i = 0; i < nl.getLength(); i++) found.add((Element) nl.item(i));
		return found;
	}

	/** the stretching dot leader: leader-length.maximum="100%" and a dots pattern */
	private static boolean isStretchingDotLeader(Element leader) {
		return "dots".equals(leader.getAttribute("leader-pattern"))
				&& "100%".equals(leader.getAttribute("leader-length.maximum"));
	}

	/** the ordinary tab leader the line manager sizes and whose pattern it settles */
	private static boolean isLaidOutTab(Element leader) {
		return "0pt".equals(leader.getAttribute("leader-length"))
				&& !leader.hasAttribute("leader-length.maximum")
				&& (leader.hasAttribute("docx4j-tab")
					|| leader.getAttributeNS(WORD_LAYOUT_NS, "tab").length() > 0);
	}

	/**
	 * number &lt;tab&gt; title &lt;tab&gt; page: the first tab must not take the entry's
	 * dot leader, the second must.
	 */
	private void checkTwoTabEntry(int flags) throws Exception {
		List<Element> leaders = leaders(pkg(
				"<w:p>" + TOC_PPR + run("4.1") + TAB + run("The heading's title") + TAB
				+ run("56") + "</w:p><w:p/>"), flags);
		assertEquals("two tabs, two leaders", 2, leaders.size());

		Element first = leaders.get(0), second = leaders.get(1);
		assertTrue("the first tab must not take the entry's stretching dot leader, it got "
				+ XmlUtils.w3CDomNodeToString(first), isLaidOutTab(first));
		assertTrue("the second tab, which reaches the dot stop, keeps it; it got "
				+ XmlUtils.w3CDomNodeToString(second), isStretchingDotLeader(second));
	}

	/**
	 * title &lt;tab&gt; page, the unnumbered entry: its one tab is its last, so it keeps
	 * the stretching dot leader it has always had.
	 */
	private void checkOneTabEntry(int flags) throws Exception {
		List<Element> leaders = leaders(pkg(
				"<w:p>" + TOC_PPR + run("The heading's title") + TAB + run("56")
				+ "</w:p><w:p/>"), flags);
		assertEquals("one tab, one leader", 1, leaders.size());
		assertTrue("the only tab reaches the dot stop and keeps the stretching leader; it got "
				+ XmlUtils.w3CDomNodeToString(leaders.get(0)),
				isStretchingDotLeader(leaders.get(0)));
	}

	/**
	 * The same entry with no <code>w:ind</code>: nothing stands between the line's start
	 * and the dot stop, so the first tab reaches the dot stop too and keeps its leader -
	 * which is what Word does, measured on a second corpus document whose
	 * <code>toc 1</code> style carries no indent and whose golden reads
	 * <code>................2.4.2 Output...</code>.
	 */
	private void checkTwoTabEntryNoIndent(int flags) throws Exception {
		List<Element> leaders = leaders(pkg(
				"<w:p>" + TOC_PPR_NO_IND + run("2.4.2") + TAB + run("The heading's title") + TAB
				+ run("18") + "</w:p><w:p/>"), flags);
		assertEquals("two tabs, two leaders", 2, leaders.size());
		assertTrue("with no stop before the dot stop the first tab reaches it and keeps the "
				+ "leader; it got " + XmlUtils.w3CDomNodeToString(leaders.get(0)),
				isStretchingDotLeader(leaders.get(0)));
		assertTrue("and so does the second; it got "
				+ XmlUtils.w3CDomNodeToString(leaders.get(1)),
				isStretchingDotLeader(leaders.get(1)));
	}

	@Test
	public void twoTabEntryNoIndentVisitorPathway() throws Exception {
		checkTwoTabEntryNoIndent(Docx4J.FLAG_NONE);
	}

	@Test
	public void twoTabEntryNoIndentXsltPathway() throws Exception {
		checkTwoTabEntryNoIndent(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void twoTabEntryVisitorPathway() throws Exception {
		checkTwoTabEntry(Docx4J.FLAG_NONE);
	}

	@Test
	public void twoTabEntryXsltPathway() throws Exception {
		checkTwoTabEntry(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void oneTabEntryVisitorPathway() throws Exception {
		checkOneTabEntry(Docx4J.FLAG_NONE);
	}

	@Test
	public void oneTabEntryXsltPathway() throws Exception {
		checkOneTabEntry(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
