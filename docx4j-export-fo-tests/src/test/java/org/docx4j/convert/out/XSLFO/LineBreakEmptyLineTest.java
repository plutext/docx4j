package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Word gives a {@code w:br} its line even where nothing follows it on that line.
 *
 * <p>The twin of the whole-paragraph rule: {@code BrWriter} writes a line break as a
 * nested {@code fo:block line-height="0pt" linefeed-treatment="preserve"}, so where what
 * follows the break is empty - an empty run, a field with no result, or another break -
 * nothing sizes the new line, the 0pt line-height stands, and the line vanishes.
 * Measured against Word 365 on a paragraph reading
 * {@code 555 test <br/> word <fld/> <br/> <fld/> <br/> <fld/> <br/> <fld/>} whose fields
 * have no result: Word runs from y=200.7 to 293.9, six line boxes, where docx4j ran
 * 200.5 to 247.3 - three lines, 46.5pt, lost.  748 such breaks in 50 documents of three
 * corpora.</p>
 *
 * @since 17.0.6
 */
public class LineBreakEmptyLineTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	/** The height of the body flow in millipoints: one 11pt Calibri line is 15442.
	 *  (Counting {@code lineArea}s does not answer the question, because the nested
	 *  block a line break is emitted as contributes one of its own whatever its
	 *  height - which is exactly the defect.) */
	private int flowHeight(org.w3c.dom.Document areaTree) {
		NodeList flows = areaTree.getElementsByTagName("flow");
		int n = 0;
		for (int i = 0; i < flows.getLength(); i++) {
			String bpd = ((Element) flows.item(i)).getAttribute("bpd");
			if (bpd.length() > 0) n += Integer.parseInt(bpd);
		}
		return n;
	}

	/** one line of the body's 11pt Calibri, in millipoints */
	private static final int LINE = 15442;

	private static final String ONE = "<w:p><w:r><w:t>a</w:t></w:r></w:p>";

	/** A break whose new line holds a run painting nothing still takes a line. */
	@Test
	public void aBreakBeforeAnEmptyRunTakesALine() throws Exception {
		String two = "<w:p><w:r><w:t>a</w:t></w:r><w:r><w:br/></w:r><w:r><w:t></w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			int base = flowHeight(areaTree(pkg(ONE), flag));
			int withBreak = flowHeight(areaTree(pkg(two), flag));
			assertEquals(flagName(flag) + ": a break before an empty run took no line box",
					base + LINE, withBreak);
		}
	}

	/** Two contiguous breaks are already right - the postprocessor gives the second its
	 *  line - and must not gain a third. */
	@Test
	public void contiguousBreaksAreUnchanged() throws Exception {
		String three = "<w:p><w:r><w:t>a</w:t></w:r><w:r><w:br/></w:r><w:r><w:br/></w:r></w:p>";
		for (int flag : FLAGS) {
			int base = flowHeight(areaTree(pkg(ONE), flag));
			assertEquals(flagName(flag) + ": two breaks are three line boxes, not four",
					base + 2 * LINE, flowHeight(areaTree(pkg(three), flag)));
		}
	}

	/** A break with text after it is one more line, as it always was. */
	@Test
	public void aBreakWithTextAfterItIsUnchanged() throws Exception {
		String body = "<w:p><w:r><w:t>a</w:t></w:r><w:r><w:br/></w:r><w:r><w:t>b</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			int base = flowHeight(areaTree(pkg(ONE), flag));
			assertEquals(flagName(flag) + ": a break with text after it is one more line",
					base + LINE, flowHeight(areaTree(pkg(body), flag)));
		}
	}
}
