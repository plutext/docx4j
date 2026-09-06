package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Styles;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A document whose <code>w:docDefaults/w:rPrDefault/w:rPr</code> carries
 * <code>&lt;w:vanish/&gt;</code> hides every run in it - the filler paragraphs the
 * header/footer extent pre-pass builds included - so the trimmed package it lays out
 * produced an empty <code>&lt;fo:flow/&gt;</code>, which is invalid FO.  The pre-pass
 * threw, and the half-page extents it starts from survived: measured (CR-001 &#xa7;7), one
 * corpus document rendered <b>35 pages for Word's 3</b>, its FO saying
 * <code>region-before extent="396.0pt"</code> and <code>region-after extent="396.0pt"</code>
 * on a 792pt page with no header or footer part at all.
 *
 * <p>Both halves are covered here: the filler runs override the default, and a flow (or
 * static-content) with nothing in it gets one empty <code>fo:block</code>.</p>
 *
 * @since 17.0.6
 */
public class HiddenDocDefaultsExtentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String STYLES =
			"<w:styles " + W + "><w:docDefaults><w:rPrDefault><w:rPr>"
			+ "<w:vanish/>"
			+ "</w:rPr></w:rPrDefault></w:docDefaults></w:styles>";

	private static WordprocessingMLPackage pkg(boolean runOverridesVanish) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement(
				(Styles) XmlUtils.unmarshalString(STYLES));
		String rPr = runOverridesVanish ? "<w:rPr><w:vanish w:val=\"false\"/></w:rPr>" : "";
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r>" + rPr + "<w:t>visible</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"720\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(boolean runOverridesVanish, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(runOverridesVanish));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the largest region extent written on any page master, in points */
	private static double maxExtent(org.w3c.dom.Document doc) {
		double max = 0;
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "*");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (!el.getLocalName().startsWith("region-")) continue;
			String extent = el.getAttribute("extent");
			if (extent.endsWith("pt")) {
				max = Math.max(max, Double.parseDouble(extent.substring(0, extent.length() - 2)));
			}
		}
		return max;
	}

	private void check(int flags) throws Exception {
		// the document itself renders: no header or footer part, so nothing is reserved
		assertTrue("half-page extents survived the pre-pass",
				maxExtent(fo(true, flags)) < 40);

		// and an all-hidden flow is still valid FO
		org.w3c.dom.Document allHidden = fo(false, flags);
		NodeList flows = allHidden.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "flow");
		assertEquals(1, flows.getLength());
		assertTrue("fo:flow must hold at least one block",
				((Element) flows.item(0)).getElementsByTagNameNS(
						"http://www.w3.org/1999/XSL/Format", "block").getLength() > 0);
		assertTrue("and the extents are still not half the page",
				maxExtent(allHidden) < 40);
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
