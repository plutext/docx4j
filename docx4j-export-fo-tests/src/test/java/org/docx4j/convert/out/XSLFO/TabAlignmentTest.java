package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A line holding a tab is aligned by the paragraph's w:jc, the tab's own width
 * counted in.
 *
 * <p>Word sizes the tabs on the line as if it began at the left indent - so the stop a
 * trailing tab reaches does not move with the alignment - and then aligns the whole
 * line.  Measured on the {@code tab-jc} probe against Word 365 (A4, Times New Roman
 * 12pt, 1in margins: a 451.3pt line centred on 297.65 and ending at 523.35): a centred
 * paragraph whose 87.7pt of text is followed by a tab is drawn at 243.7..331.4 - the
 * text plus the 20.3pt the tab takes to the 180pt default stop, 108pt in all, centred -
 * and the same text right-aligned at 415.6..496.6; with a custom left stop at 6000
 * twips the line is 300pt whatever it holds and Word starts it at 147.7 centred, 223.5
 * right-aligned.  docx4j drew every one of them flush left (§4.4 used to say a line
 * holding a tab is laid out from the left whatever the paragraph's w:jc).</p>
 *
 * <p>A justified paragraph is the exception: the tab absorbs the slack and the line is
 * laid out from the start, which is where Word draws it.</p>
 *
 * <p>Both FO pathways; what the line manager does with the stops themselves is
 * org.docx4j.fop.wordlayout.TabStopTest.</p>
 *
 * @since 17.0.6
 */
public class TabAlignmentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FONT =
			"<w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/></w:rPr>";

	/** A4 portrait with 1in margins: a 451.35pt line. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "xslt";
	}

	/** One paragraph: some text, then a trailing tab where asked for. */
	private static WordprocessingMLPackage pkg(String jc, boolean tab) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:jc w:val=\"" + jc + "\"/></w:pPr>"
				+ "<w:r>" + FONT + "<w:t>trailing tab</w:t>" + (tab ? "<w:tab/>" : "") + "</w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/** How far in the only line starts, in millipoints. */
	private int startIndent(String jc, boolean tab, int flags) throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(jc, tab), flags);
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		assertEquals(flagName(flags) + ": one line expected", 1, lines.getLength());
		String v = ((Element) lines.item(0)).getAttribute("start-indent");
		return v.length() == 0 ? 0 : Integer.parseInt(v);
	}

	/**
	 * The slack a centred line takes is half the slack the same line takes when it is
	 * right-aligned - so the two alignments see the same line width, which is the text
	 * plus the tab, and neither is flush left.
	 */
	@Test
	public void aLineHoldingATabIsCentredAndRightAlignedWithIt() throws Exception {
		for (int flags : FLAGS) {
			int centre = startIndent("center", true, flags);
			int right = startIndent("right", true, flags);
			assertTrue(flagName(flags) + ": a centred line holding a tab started at " + centre,
					centre > 0);
			assertTrue(flagName(flags) + ": a right-aligned line holding a tab started at " + right,
					right > 0);
			assertEquals(flagName(flags) + ": centred is half of right-aligned",
					right / 2, centre, 1);
		}
	}

	/**
	 * And the tab's width is part of what is aligned: the same text without the tab
	 * has further to travel to reach the right margin.
	 */
	@Test
	public void theTabsWidthIsCountedInTheAlignment() throws Exception {
		for (int flags : FLAGS) {
			int withTab = startIndent("right", true, flags);
			int withoutTab = startIndent("right", false, flags);
			assertTrue(flagName(flags) + ": with the tab " + withTab + ", without " + withoutTab,
					withTab < withoutTab);
		}
	}

	/** A justified paragraph, and a left-aligned one, start at the margin. */
	@Test
	public void aJustifiedOrLeftAlignedLineStartsAtTheMargin() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(flagName(flags), 0, startIndent("both", true, flags));
			assertEquals(flagName(flags), 0, startIndent("left", true, flags));
		}
	}
}
