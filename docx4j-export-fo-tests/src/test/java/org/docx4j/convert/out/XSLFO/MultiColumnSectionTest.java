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
 * Multi-column sections, as Word writes them (CR-001, real documents):
 * <ul>
 * <li>the column gap comes from the columns' own w:col/@w:space where there are
 * w:col children; only equal columns with no children use w:cols/@w:space;</li>
 * <li>continuous sections merged into one page-sequence keep their own page
 * margins: the sequence uses the first section's, and the other parts carry the
 * difference as indents on a block-container.</li>
 * </ul>
 * XSL-FO's region-body columns are all the same width, so columns of different
 * widths (w:cols/@w:equalWidth="0") are still rendered equal.
 */
public class MultiColumnSectionTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 2cm margins. */
	private static final String PG_SZ = "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
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

	private static Element first(org.w3c.dom.Document doc, String localName) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, localName);
		return (nl.getLength()==0) ? null : (Element)nl.item(0);
	}

	// ------------------------------------------------------------------ the gap

	private void gapFromTheColumns(int flags) throws Exception {
		// two columns of different widths, the gap on the first column
		org.w3c.dom.Document doc = fo(pkg(p("in two columns")
				+ "<w:sectPr>" + PG_SZ
				+ "<w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\"/>"
				+ "<w:cols w:equalWidth=\"0\" w:num=\"2\" w:space=\"708\">"
				+ "<w:col w:w=\"2509\" w:space=\"1025\"/><w:col w:w=\"6130\"/></w:cols>"
				+ "</w:sectPr>"), flags);
		Element regionBody = first(doc, "region-body");
		assertNotNull(regionBody);
		assertEquals("2", regionBody.getAttribute("column-count"));
		assertEquals("1025 twips, not the container's 708", "51.25pt", regionBody.getAttribute("column-gap"));
	}

	@Test
	public void gapFromTheColumnsVisitor() throws Exception {
		gapFromTheColumns(Docx4J.FLAG_NONE);
	}

	@Test
	public void gapFromTheColumnsXslt() throws Exception {
		gapFromTheColumns(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void equalColumnsStillUseTheContainersSpace() throws Exception {
		org.w3c.dom.Document doc = fo(pkg(p("in two columns")
				+ "<w:sectPr>" + PG_SZ
				+ "<w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr>"), Docx4J.FLAG_NONE);
		assertEquals("35.4pt", first(doc, "region-body").getAttribute("column-gap"));
	}

	// -------------------------------------------------------------- the margins

	/** Two continuous sections, 2 columns then 1, with different w:pgMar. */
	private static WordprocessingMLPackage twoSections() throws Exception {
		return pkg(p("two columns a") + p("two columns b")
				+ "<w:p><w:pPr><w:sectPr>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"567\" w:bottom=\"1440\" w:left=\"567\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("one column")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"1134\" w:bottom=\"1440\" w:left=\"1134\"/>"
				+ "<w:cols w:space=\"708\"/></w:sectPr>");
	}

	private void marginsPerPart(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(twoSections(), flags);

		// one page master, with the first section's margins (567tw = 28.35pt)
		NodeList masters = doc.getElementsByTagNameNS(FO_NS, "simple-page-master");
		assertEquals(1, masters.getLength());
		Element master = (Element)masters.item(0);
		assertEquals("28.35pt", master.getAttribute("margin-left"));
		assertEquals("28.35pt", master.getAttribute("margin-right"));

		// and the one-column part's paragraph carries the difference (1134-567 = 567tw)
		Element para = null;
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			if ("one column".equals(el.getTextContent())) para = el;
		}
		assertNotNull("no block for the part with other margins", para);
		assertEquals("28.35pt", para.getAttribute("start-indent"));
		assertEquals("28.35pt", para.getAttribute("end-indent"));

		// it is inside the block that spans all the columns
		assertTrue("the indented part is not inside the spanning block",
				"all".equals(((Element)para.getParentNode()).getAttribute("span")));
	}

	@Test
	public void marginsPerPartVisitor() throws Exception {
		marginsPerPart(Docx4J.FLAG_NONE);
	}

	@Test
	public void marginsPerPartXslt() throws Exception {
		marginsPerPart(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * And it takes effect: FOP lays the indented part's line out 28.35pt in from the
	 * page-sequence's text edge, and 56.7pt narrower.
	 */
	@Test
	public void theIndentIsWhereTheLineStarts() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(twoSections(), Docx4J.FLAG_NONE);
		// 595.3pt page - 2 x 28.35pt margins = 538.6pt; the line is 56.7pt narrower
		boolean found = false;
		NodeList nl = areaTree.getElementsByTagName("block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element block = (Element)nl.item(i);
			if ("one column".equals(block.getTextContent())
					&& block.getAttribute("start-indent").length() > 0) {
				assertEquals("28350", block.getAttribute("start-indent"));
				assertEquals("28350", block.getAttribute("end-indent"));
				assertEquals("481900", block.getAttribute("ipd"));
				found = true;
			}
		}
		assertTrue("the indented part was not laid out at the narrower width", found);
	}
}
