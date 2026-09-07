package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The <code>w:settings/w:compat</code> flags the layout rules read, each measured by
 * toggling it on a document that is otherwise identical.
 *
 * <p>A rule keys on the flag and the compatibility mode supplies the flag's default
 * (<code>org.docx4j.model.CompatibilityOptions</code>, and word-layout-settings.md §2), so
 * these tests state the flag explicitly: what they check is that stating it changes the FO
 * the way the rule says, in <b>both</b> exporter pathways.</p>
 *
 * @since 17.1.0
 */
public class CompatFlagsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/**
	 * A package holding this body at <b>compatibility mode 15</b>, with each named
	 * w:compat flag set to that value.  The mode is stated so that the flags which are not
	 * stated take Word 365's defaults, which is what these tests measure against.
	 */
	private static WordprocessingMLPackage pkg(String body, String... flagsOnOff) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart(true);
		CTSettings settings = dsp.getContents();
		if (flagsOnOff.length > 0) {
			StringBuilder compat = new StringBuilder("<w:compat " + W + ">");
			for (int i = 0; i + 1 < flagsOnOff.length; i += 2) {
				compat.append("<w:").append(flagsOnOff[i])
						.append(" w:val=\"").append(flagsOnOff[i + 1]).append("\"/>");
			}
			compat.append("</w:compat>");
			settings.setCompat((org.docx4j.wml.CTCompat) XmlUtils.unwrap(
					XmlUtils.unmarshalString(compat.toString(), org.docx4j.jaxb.Context.jc,
							org.docx4j.wml.CTCompat.class)));
		}
		dsp.setWordCompatSetting("compatibilityMode", "15");
		return pkg;
	}

	// -------------------------------------------------------------- line geometry

	/** Where each line's last glyph ends, in points from the region's start edge. */
	private static List<Double> lineEnds(org.w3c.dom.Document areaTree) {
		List<Double> ends = new ArrayList<Double>();
		NodeList las = areaTree.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			Element line = (Element) las.item(i);
			double[] x = { mpt(line, "start-indent") };
			double[] end = { x[0] };
			walk(line, x, end);
			ends.add(Math.round(end[0] * 10) / 10.0);
		}
		return ends;
	}

	private static void walk(Element el, double[] x, double[] end) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			String name = c.getLocalName();
			if ("text".equals(name)) {
				x[0] += mpt(c, "ipd");
				end[0] = x[0];
			} else if ("space".equals(name) || "leader".equals(name)) {
				x[0] += mpt(c, "ipd");
			} else {
				walk(c, x, end);
			}
		}
	}

	private static double mpt(Element el, String name) {
		String v = el.getAttribute(name);
		return v == null || v.length() == 0 ? 0 : Integer.parseInt(v) / 1000.0;
	}

	private static int pageCount(org.w3c.dom.Document areaTree) {
		return areaTree.getElementsByTagName("pageViewport").getLength();
	}

	// ------------------------------------------------- w:doNotExpandShiftReturn (§4.2)

	/** Enough justified prose to fill two lines, then a soft return, then more. */
	private static final String JUSTIFIED_WITH_BREAK =
			"<w:p><w:pPr><w:jc w:val=\"both\"/></w:pPr>"
			+ "<w:r><w:t xml:space=\"preserve\">"
			+ "alpha bravo charlie delta echo foxtrot golf hotel india juliet kilo lima "
			+ "mike november oscar papa quebec romeo sierra tango uniform victor</w:t></w:r>"
			+ "<w:r><w:br/></w:r>"
			+ "<w:r><w:t xml:space=\"preserve\">"
			+ "whiskey xray yankee zulu alpha bravo charlie delta echo foxtrot golf hotel "
			+ "india juliet kilo lima mike november oscar papa quebec</w:t></w:r></w:p>";

	/**
	 * Word justifies the line that ends at a soft return, in a justified paragraph, unless
	 * w:doNotExpandShiftReturn is set - and docx4j had the flag-on behaviour hard-wired
	 * for every document, because BrWriter makes the break a nested fo:block and FOP then
	 * treats the line as a last line.
	 */
	@Test
	public void aLineEndingInASoftReturnIsJustified() throws Exception {
		for (int flag : FLAGS) {
			List<Double> free = lineEnds(areaTree(pkg(JUSTIFIED_WITH_BREAK), flag));
			List<Double> set = lineEnds(areaTree(
					pkg(JUSTIFIED_WITH_BREAK, "doNotExpandShiftReturn", "1"), flag));
			assertTrue(flagName(flag) + ": expected the paragraph to wrap: " + free,
					free.size() >= 4);
			assertEquals(flagName(flag) + ": the two documents broke differently: "
					+ free + " / " + set, set.size(), free.size());

			// the line before the break is the one whose two documents disagree; every
			// full line of a justified paragraph reaches the same measure
			double measure = free.get(0).doubleValue();
			int breakLine = -1;
			for (int i = 0; i < set.size(); i++) {
				if (set.get(i).doubleValue() < measure - 1) { breakLine = i; break; }
			}
			assertTrue(flagName(flag) + ": no short line before the break: " + set,
					breakLine > 0 && breakLine < set.size() - 1);
			assertEquals(flagName(flag) + ": the line before the soft return was not"
					+ " justified: " + free, measure, free.get(breakLine).doubleValue(), 0.5);
			assertTrue(flagName(flag) + ": w:doNotExpandShiftReturn did not stop it: " + set,
					set.get(breakLine).doubleValue() < measure - 1);
			// and the paragraph's own last line is never justified, either way
			assertTrue(flagName(flag) + ": the paragraph's last line was justified: " + free,
					free.get(free.size() - 1).doubleValue() < measure - 1);
		}
	}

	/** An unjustified paragraph is untouched by the rule. */
	@Test
	public void aSoftReturnInARaggedParagraphIsUnchanged() throws Exception {
		String ragged = JUSTIFIED_WITH_BREAK.replace("<w:jc w:val=\"both\"/>", "");
		for (int flag : FLAGS) {
			assertEquals(flagName(flag) + ": a ragged paragraph must not change",
					lineEnds(areaTree(pkg(ragged, "doNotExpandShiftReturn", "1"), flag)),
					lineEnds(areaTree(pkg(ragged), flag)));
		}
	}

	// -------------------------------------------- w:splitPgBreakAndParaMark (§3.3)

	private static final String BREAK_MID_PARAGRAPH =
			"<w:p><w:r><w:t>before</w:t></w:r>"
			+ "<w:r><w:br w:type=\"page\"/></w:r>"
			+ "<w:r><w:t>after</w:t></w:r></w:p>";

	/**
	 * Word breaks the page at the break: what precedes it stays on the page it is on.
	 * With w:splitPgBreakAndParaMark off the whole paragraph goes to the next page, which
	 * is what docx4j did to 17.0.5.
	 */
	@Test
	public void splitPgBreakAndParaMarkKeepsTheTextBeforeTheBreak() throws Exception {
		for (int flag : FLAGS) {
			org.w3c.dom.Document split = areaTree(pkg(BREAK_MID_PARAGRAPH), flag);
			org.w3c.dom.Document whole = areaTree(
					pkg(BREAK_MID_PARAGRAPH, "splitPgBreakAndParaMark", "0"), flag);
			assertEquals(flagName(flag) + ": the split leaves a line on each page",
					2, lineCount(split));
			assertEquals(flagName(flag) + ": two pages, one line each", 2, pageCount(split));
			/* With the flag off the break becomes the paragraph's own w:pageBreakBefore,
			 * so "before" and "after" are one line - and, this being the document's first
			 * paragraph, a break-before at the start of the flow costs no page at all. */
			assertEquals(flagName(flag) + ": with the flag off both halves are one line",
					1, lineCount(whole));
			assertEquals(flagName(flag) + ": and one page", 1, pageCount(whole));
		}
	}

	// --------------------------------------------- w:suppressSpBfAfterPgBrk (§3.3)

	private static final String SPACE_AFTER_BREAK =
			"<w:p><w:r><w:t>page one</w:t></w:r></w:p>"
			+ "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>"
			+ "<w:p><w:pPr><w:spacing w:before=\"480\"/></w:pPr>"
			+ "<w:r><w:t>page two</w:t></w:r></w:p>";

	/**
	 * "Do Not Use Space Before On First Line After a Page Break": the 24pt space-before of
	 * the paragraph the break moves onto is dropped where the flag is on and kept where it
	 * is off.  The flag's default is the compatibility mode's (on from 15), which is what
	 * the mode-keyed rule did before 17.1.0.
	 */
	@Test
	public void suppressSpBfAfterPgBrkDropsTheSpace() throws Exception {
		for (int flag : FLAGS) {
			double kept = spaceBeforeOnLastPage(areaTree(
					pkg(SPACE_AFTER_BREAK, "suppressSpBfAfterPgBrk", "0"), flag));
			double dropped = spaceBeforeOnLastPage(areaTree(
					pkg(SPACE_AFTER_BREAK, "suppressSpBfAfterPgBrk", "1"), flag));
			assertEquals(flagName(flag) + ": the flag off keeps the whole 24pt space-before",
					24.0, kept, 0.5);
			assertEquals(flagName(flag) + ": the flag on drops it", 0.0, dropped, 0.5);
		}
	}

	/** The space-before FOP kept on the first block of the last page, in points. */
	private double spaceBeforeOnLastPage(org.w3c.dom.Document areaTree) {
		NodeList pages = areaTree.getElementsByTagName("pageViewport");
		Element last = (Element) pages.item(pages.getLength() - 1);
		NodeList blocks = last.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (b.getElementsByTagName("lineArea").getLength() == 0) continue;
			return mpt(b, "space-before");
		}
		return 0;
	}

	// --------------------------------- w:allowSpaceOfSameStyleInTable (§3.5)

	private static final String CONTEXTUAL_CELL =
			"<w:tbl><w:tblPr><w:tblW w:w=\"5000\" w:type=\"dxa\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"5000\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"5000\" w:type=\"dxa\"/></w:tcPr>"
			+ "<w:p><w:pPr><w:contextualSpacing/><w:spacing w:after=\"400\"/></w:pPr>"
			+ "<w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
			+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>";

	/**
	 * docx4j cancels a lone contextual paragraph's space at a cell's edges, which is what
	 * Word 365 was measured doing - and Word 365 does the same with "Allow Contextual
	 * Spacing of Paragraphs in Tables" stated: the compat-breaks probe pair renders
	 * identically (row pitch 13.7pt either way), so the flag changes nothing here.
	 */
	@Test
	public void allowSpaceOfSameStyleInTableChangesNothing() throws Exception {
		for (int flag : FLAGS) {
			int cancelled = tableHeight(areaTree(pkg(CONTEXTUAL_CELL), flag));
			int stated = tableHeight(areaTree(
					pkg(CONTEXTUAL_CELL, "allowSpaceOfSameStyleInTable", "1"), flag));
			assertEquals(flagName(flag) + ": Word 365 ignores the flag", cancelled, stated);
		}
	}

	// ------------------------------------------------------ w:noTabHangInd (§4.4)

	/** left indent 50pt, first line at 25pt, then a tab: the hanging indent's implicit stop
	 *  is at 50pt and the default grid's next stop past 25pt is 36pt. */
	private static final String HANGING_TAB =
			"<w:p><w:pPr><w:ind w:left=\"1000\" w:hanging=\"500\"/></w:pPr>"
			+ "<w:r><w:t>A</w:t></w:r><w:r><w:tab/></w:r><w:r><w:t>B</w:t></w:r></w:p>";

	/**
	 * "Do Not Create Custom Tab Stop for Hanging Indent": without it the tab reaches the
	 * implicit stop the hanging indent makes at the left indent, with it the default grid.
	 */
	@Test
	public void noTabHangIndDropsTheImplicitStop() throws Exception {
		for (int flag : FLAGS) {
			double implicit = lineEnds(areaTree(pkg(HANGING_TAB), flag)).get(0).doubleValue();
			double grid = lineEnds(areaTree(
					pkg(HANGING_TAB, "noTabHangInd", "1"), flag)).get(0).doubleValue();
			assertEquals(flagName(flag) + ": the hanging indent's stop is at 50pt and the"
					+ " default grid's at 36pt (" + implicit + " / " + grid + ")",
					14.0, implicit - grid, 0.5);
		}
	}

	// ------------------------------------------------------- w:growAutofit (§6.5)

	/** A table which states no w:tblW - so its own w:tblGrid is the layout Word cached -
	 *  whose grid is twice the text column, holding one long line. */
	private static final String WIDE_AUTOFIT_TABLE =
			"<w:tbl><w:tblPr/><w:tblGrid><w:gridCol w:w=\"18000\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"18000\" w:type=\"dxa\"/></w:tcPr>"
			+ "<w:p><w:r><w:t>alpha bravo charlie delta echo foxtrot golf hotel india juliet"
			+ " kilo lima mike november oscar papa quebec romeo sierra tango</w:t></w:r>"
			+ "</w:p></w:tc></w:tr></w:tbl>";

	/**
	 * "Allow Tables to AutoFit Into Page Margins" would leave such a grid alone where §6.5
	 * scales it into the text column, and <b>docx4j deliberately does not read it</b>:
	 * measured over the three corpora, the two documents which state the flag have Word 365
	 * fitting their grids to the column anyway, and honouring it cost them 0.948 -&gt; 0.248
	 * and 0.930 -&gt; 0.842 of Word's lines (word-layout-settings.md §4(e)).
	 */
	@Test
	public void growAutofitDoesNotStopTheGridBeingFitted() throws Exception {
		for (int flag : FLAGS) {
			int fitted = lineCount(areaTree(pkg(WIDE_AUTOFIT_TABLE), flag));
			assertTrue(flagName(flag) + ": the fitted table wraps (" + fitted + " lines)",
					fitted > 1);
			assertEquals(flagName(flag) + ": w:growAutofit must change nothing",
					fitted, lineCount(areaTree(pkg(WIDE_AUTOFIT_TABLE, "growAutofit", "1"), flag)));
		}
	}

	/** The block-progression-dimension of the first fo:table's area, in millipoints. */
	private int tableHeight(org.w3c.dom.Document areaTree) {
		NodeList blocks = areaTree.getElementsByTagName("block");
		int max = 0;
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (b.getElementsByTagName("lineArea").getLength() == 0) continue;
			String bpd = b.getAttribute("bpd");
			if (bpd.length() > 0) max = Math.max(max, Integer.parseInt(bpd));
		}
		return max;
	}
}
