package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

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
 * The wrapper block which <code>Containerization</code> puts round a run of shaded
 * paragraphs is built from the <b>first</b> paragraph's properties, and two things it
 * used to carry were wrong.
 *
 * <ul>
 * <li><b>The <code>margin</code> shorthand.</b> It is written as 0 so that no white strip
 * appears between shaded paragraphs, but FOP 2.11 lets the shorthand win over the
 * <code>space-before</code>/<code>space-after</code> on the same element.  Proved by
 * running two blocks through FOP: one with <code>margin-top="0in" space-before="30pt"
 * space-before.conditionality="retain"</code> renders at y=80.6, level with its sibling;
 * the identical block without <code>margin-top</code> at 122.6, exactly 30pt down.
 * Measured on two corpus documents whose row-1 cells Word puts on baseline 145.3 and we
 * put at 142.0, a -3.3pt which grew to -14.0pt by y=695.</li>
 * <li><b>The indents.</b> <code>start-indent</code>, <code>end-indent</code> and
 * <code>text-indent</code> are inherited, so every later paragraph of the group which
 * states none was displaced by the first one's: measured, a shaded group opening with a
 * <code>w:ind w:left="1440" w:hanging="360"</code> paragraph put the body paragraphs
 * after it at x=126.0 where Word draws them at 72.0.</li>
 * </ul>
 *
 * @since 17.0.6
 */
public class ShadingContainerTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String SHD = "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"D9D9D9\"/>";

	private static String para(String text, String extraPPr) {
		return "<w:p><w:pPr>" + extraPPr + SHD + "</w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>before</w:t></w:r></w:p>"
				// the first paragraph of the shaded run carries both the spacing and the indent
				+ para("indented first", "<w:spacing w:before=\"600\" w:after=\"0\"/>"
						+ "<w:ind w:left=\"1440\" w:hanging=\"360\"/>")
				+ para("plain second", "")
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the wrapper: the block carrying the shading */
	private static Element container(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getAttribute("background-color").length() > 0
					&& el.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block").getLength() > 0) {
				return el;
			}
		}
		return null;
	}

	private void check(int flags) throws Exception {
		Element container = container(fo(flags));
		assertNotNull("no shading container", container);

		assertTrue("the container keeps its space-before",
				container.getAttribute("space-before").startsWith("30"));
		assertEquals("margin-top would cancel it in FOP", "", container.getAttribute("margin-top"));
		// the after side has nothing to lose, so the shorthand is still written there
		assertEquals("0in", container.getAttribute("margin-bottom"));

		assertEquals("the wrapper must not pass its first paragraph's indent on",
				"0pt", container.getAttribute("start-indent"));
		assertEquals("0pt", container.getAttribute("text-indent"));
		assertEquals("0pt", container.getAttribute("end-indent"));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
