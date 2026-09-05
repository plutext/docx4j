package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
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
 * {@code w:w}, Word's character scaling (CR-001 &#xa7;4.6).
 *
 * <p>Word multiplies the run's glyph advances by a percentage.  Neither XSL-FO nor FOP
 * can scale text horizontally, so the effect that matters for layout - the run's total
 * advance - is reproduced with {@code letter-spacing}, measured from the font FOP will
 * use.  Measured on a document of 186 scaled runs (w:w 102/103/105) whose font mapping is
 * exact: the median width ratio of our lines to Word's over twenty long matched lines was
 * 0.9516, e.g. a line Word draws 72.5..520.3 (447.8pt) was 72.4..497.2 (424.8pt).</p>
 *
 * @since 17.0.6
 */
public class CharacterScalingTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String TEXT = "The quick brown fox jumps over the lazy dog";

	private static String paragraph(String w) {
		String rPr = "<w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:sz w:val=\"22\"/>"
				+ (w == null ? "" : "<w:w w:val=\"" + w + "\"/>") + "</w:rPr>";
		return "<w:p><w:r>" + rPr + "<w:t>" + TEXT + "</w:t></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
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

	/** The letter-spacing of the span holding the text, in points. */
	private static double letterSpacing(org.w3c.dom.Document doc) {
		NodeList inlines = doc.getElementsByTagNameNS(FO_NS, "inline");
		for (int i = 0; i < inlines.getLength(); i++) {
			Element el = (Element) inlines.item(i);
			if (!TEXT.equals(el.getTextContent())) continue;
			String v = el.getAttribute("letter-spacing");
			if (v.length() == 0) continue;
			return Double.parseDouble(v.endsWith("pt") ? v.substring(0, v.length() - 2) : v);
		}
		return 0;
	}

	private void condensedTextIsNarrowed(int flags) throws Exception {
		double none = letterSpacing(fo(pkg(paragraph(null)), flags));
		double at94 = letterSpacing(fo(pkg(paragraph("94")), flags));
		assertEquals("an unscaled run carries no letter-spacing", 0.0, none, 0.0001);
		assertTrue("w:w=94 must condense the run: " + at94, at94 < -0.05);
	}

	@Test
	public void condensedTextIsNarrowedVisitor() throws Exception {
		condensedTextIsNarrowed(Docx4J.FLAG_NONE);
	}

	@Test
	public void condensedTextIsNarrowedXslt() throws Exception {
		condensedTextIsNarrowed(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void expandedTextIsWidened() throws Exception {
		assertTrue(letterSpacing(fo(pkg(paragraph("105")), Docx4J.FLAG_NONE)) > 0.05);
	}

	/**
	 * The point of it: the painted line is the fraction of its natural width that Word
	 * makes it.  Measured in the area tree, which is what PDF gets.
	 */
	@Test
	public void theLineIsScaledToWordsWidth() throws Exception {
		int natural = lineWidth(areaTree(pkg(paragraph(null)), Docx4J.FLAG_NONE));
		int scaled = lineWidth(areaTree(pkg(paragraph("94")), Docx4J.FLAG_NONE));
		assertTrue("no line", natural > 0);
		double ratio = (double) scaled / natural;
		assertEquals("the line must come out at 94% of its natural width", 0.94, ratio, 0.02);
	}

	private static int lineWidth(org.w3c.dom.Document areaTree) {
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		assertTrue("no lineArea", lines.getLength() > 0);
		NodeList texts = ((Element) lines.item(0)).getElementsByTagName("text");
		int total = 0;
		for (int i = 0; i < texts.getLength(); i++) {
			String ipd = ((Element) texts.item(i)).getAttribute("ipd");
			if (ipd.length() > 0) total += Integer.parseInt(ipd);
		}
		return total;
	}
}
