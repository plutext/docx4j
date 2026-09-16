package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The three-field running head Word's own header gallery writes -
 * <code>text &lt;w:ptab center&gt; text &lt;w:ptab right&gt; text</code> - centres its
 * middle field on the middle of the line and ends its last on the right margin.
 *
 * <p>docx4j wrote the right <code>w:ptab</code> and <b>nothing at all</b> for the centre
 * one, so the middle field ran straight on from the first. Measured (CR-001 batch 43, M43)
 * on a corpus document whose head is exactly this shape: Word sets its three fields at
 * 72.0..158.1, <b>253.8..341.7</b> (centred on x=297.75, the exact centre of the page) and
 * 394.0..523.5, where docx4j ran the first two together and started the middle field at
 * ~158 - 95.7pt short - on every one of its 35 pages, while the right ptab beside it landed
 * to 0.1pt.</p>
 *
 * @since 17.1.1
 */
public class HeaderCentrePtabTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1 inch margins: the line runs x=72..523.3, its centre 297.65. */
	private static final String SECT_PG =
			"<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"708\" w:footer=\"708\"/>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>body</w:t></w:r></w:p>"
				+ "<w:sectPr>" + SECT_PG + "</w:sectPr></w:body></w:document>"));

		HeaderPart hp = new HeaderPart(new PartName("/word/header1.xml"));
		hp.setPackage(pkg);
		hp.setJaxbElement((Hdr) XmlUtils.unmarshalString(
				"<w:hdr " + W + "><w:p>"
				+ "<w:r><w:t>LEFT</w:t></w:r>"
				+ "<w:r><w:ptab w:relativeTo=\"margin\" w:alignment=\"center\" w:leader=\"none\"/></w:r>"
				+ "<w:r><w:t>MIDDLE</w:t></w:r>"
				+ "<w:r><w:ptab w:relativeTo=\"margin\" w:alignment=\"right\" w:leader=\"none\"/></w:r>"
				+ "<w:r><w:t>RIGHT</w:t></w:r>"
				+ "</w:p></w:hdr>", org.docx4j.jaxb.Context.jc, Hdr.class));
		Relationship hr = pkg.getMainDocumentPart().addTargetPart(hp);

		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		HeaderReference headerReference = org.docx4j.jaxb.Context.getWmlObjectFactory()
				.createHeaderReference();
		headerReference.setId(hr.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);
		return pkg;
	}

	/** Walks a line's inline areas in order, accumulating x: the area tree gives each its
	 *  ipd, not an absolute position. */
	private static void walk(Element el, double[] x, java.util.Map<String, double[]> spans) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			org.w3c.dom.Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			String name = c.getLocalName();
			if ("text".equals(name)) {
				double w = mpt(c, "ipd");
				String t = c.getTextContent().trim();
				if (spans.containsKey(t)) spans.put(t, new double[] { x[0], x[0] + w });
				x[0] += w;
			} else if ("space".equals(name) || "leader".equals(name) || "char".equals(name)) {
				x[0] += mpt(c, "ipd");
			} else {
				walk(c, x, spans);
			}
		}
	}

	private static double mpt(Element el, String name) {
		String v = el.getAttribute(name);
		return v == null || v.length() == 0 ? 0 : Double.parseDouble(v) / 1000.0;
	}

	/**
	 * The area tree says where each field actually landed: LEFT at the margin, MIDDLE
	 * centred on the line, RIGHT ending at the right margin.
	 */
	private void check(int flags) throws Exception {
		org.w3c.dom.Document tree = areaTree(pkg(), flags);
		java.util.Map<String, double[]> spans = new java.util.LinkedHashMap<String, double[]>();
		for (String k : new String[] { "LEFT", "MIDDLE", "RIGHT" }) spans.put(k, null);
		NodeList lines = tree.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			double[] x = new double[] { 0 };
			walk((Element) lines.item(i), x, spans);
		}
		for (java.util.Map.Entry<String, double[]> e : spans.entrySet()) {
			assertTrue("field " + e.getKey() + " is not in the area tree", e.getValue() != null);
		}
		double[] left = spans.get("LEFT"), middle = spans.get("MIDDLE"), right = spans.get("RIGHT");

		// the line is 451.3pt wide; the middle field's own centre must be its middle
		double lineWidth = 451.3;
		assertEquals("the middle field is centred on the line, not run on from the first",
				lineWidth / 2, (middle[0] + middle[1]) / 2, 3.0);
		assertTrue("and it does not touch the first field: it starts at " + middle[0]
				+ " where the first ends at " + left[1], middle[0] > left[1] + 60);
		assertEquals("the right field still ends on the right margin", lineWidth, right[1], 3.0);
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
