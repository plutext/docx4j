package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A run of consecutive paragraphs whose {@code w:pBdr} definitions are identical is one
 * border box in Word (CR-001 &#xa7;3), and a shading change inside the run does not open
 * a second one.
 *
 * <p>The {@code Containerization} preprocess nests the shading containers inside the
 * border container, which is right; both are built from the same paragraph properties,
 * so the inner one repeated the outer's top and bottom borders and their {@code w:space}
 * padding - 2 x (0.51 + 1) = <b>3.02pt</b> per shading change with a 0.5pt border at
 * {@code w:space="1"}.  Measured on a planner whose cells hold three identically bordered
 * paragraphs in three different fills: Word's row pitch is 61.0 -&gt; 70.1 -&gt; 79.2 (9.1pt,
 * the bare Arial 8pt line box, so Word adds nothing at the change) where docx4j went
 * 61.3 -&gt; 70.5 -&gt; 82.7; 16 Word pages came out as 21, and are 16.</p>
 *
 * @since 17.1.0
 */
public class BorderContainerMergeTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String PBDR =
			"<w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/>"
			+ "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"auto\"/>"
			+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/>"
			+ "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"auto\"/></w:pBdr>";

	private static String bordered(String fill, String text) {
		return "<w:p><w:pPr>" + PBDR
				+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"" + fill + "\"/></w:pPr>"
				+ "<w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	/** @param fills one fill per paragraph; all the paragraphs share one w:pBdr */
	private static WordprocessingMLPackage pkg(String... fills) throws Exception {
		StringBuilder body = new StringBuilder();
		for (int i = 0; i < fills.length; i++) {
			body.append(bordered(fills[i], "line " + (i + 1)));
		}
		body.append("<w:p><w:r><w:t>after the box</w:t></w:r></w:p>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** Blocks carrying a top border, whatever their depth. */
	private static int blocksWithTopBorder(org.w3c.dom.Document doc) {
		int n = 0;
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			if (((Element) nl.item(i)).getAttribute("border-top-width").length() > 0) n++;
		}
		return n;
	}

	/** The outermost block carrying a top border. */
	private static Element boxBlock(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getAttribute("border-top-width").length() > 0) return el;
		}
		return null;
	}

	private void check(int flags) throws Exception {

		// three fills, one border: one box
		org.w3c.dom.Document three = fo(pkg("E5B8B7", "FFC000", "92D050"), flags);
		assertEquals("one border box for the run", 1, blocksWithTopBorder(three));
		Element box = boxBlock(three);
		assertNotNull(box);
		assertEquals("1pt", box.getAttribute("padding-top"));
		assertEquals("1pt", box.getAttribute("padding-bottom"));
		assertTrue("the box keeps its bottom border",
				box.getAttribute("border-bottom-width").length() > 0);

		// the shading of each paragraph is still applied inside it
		assertTrue(XmlUtils.w3CDomNodeToString(three).contains("#FFC000"));
		assertTrue(XmlUtils.w3CDomNodeToString(three).contains("#92D050"));

		// and the same for one paragraph carrying both a border and shading
		assertEquals("one border box for one paragraph", 1,
				blocksWithTopBorder(fo(pkg("E5B8B7"), flags)));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * And it is worth 3.02pt per shading change on the page: the box holding three
	 * paragraphs is exactly as tall whether they share a fill or not, which is what
	 * Word draws.
	 */
	@Test
	public void shadingChangeCostsNoHeight() throws Exception {
		double oneFill = boxHeight(pkg("E5B8B7", "E5B8B7", "E5B8B7"));
		double threeFills = boxHeight(pkg("E5B8B7", "FFC000", "92D050"));
		assertTrue("no box measured", oneFill > 0);
		assertEquals("a shading change added height", oneFill, threeFills, 0.5);
	}

	/** The block-progression-dimension of the outermost block area, in points. */
	private double boxHeight(WordprocessingMLPackage pkg) throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg, Docx4J.FLAG_NONE);
		NodeList nl = areaTree.getElementsByTagName("block");
		double best = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (!el.getTextContent().startsWith("line 1")) continue;
			String bpd = el.getAttribute("bpda").length() > 0
					? el.getAttribute("bpda") : el.getAttribute("bpd");
			if (bpd.length() == 0) continue;
			best = Math.max(best, Double.parseDouble(bpd) / 1000d);
		}
		return best;
	}
}
