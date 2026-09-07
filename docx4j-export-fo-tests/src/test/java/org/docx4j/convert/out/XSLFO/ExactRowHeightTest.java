package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A row of {@code w:trHeight} with {@code w:hRule="exact"} is exactly that tall in Word,
 * borders included (CR-001 &#xa7;6.7).
 *
 * <p>FOP treats the height as a minimum and grows the row to its content, so docx4j
 * clips the content to the height with an {@code fo:block-container}.  It also reads
 * {@code height} on an {@code fo:table-row} as the cell's <em>content</em> height and
 * then advances to the next row by that plus the border it charges the cell - half of
 * each collapsed border - so the rows came out half a point too tall.  Measured on the
 * page-blank probe (32 rows of {@code w:val="400" w:hRule="exact"}, 20pt, with 0.5pt
 * collapsed borders): Word's row pitch is 20.0 (baselines 617.5 / 637.6 / 657.5 / 677.5)
 * and docx4j's was 20.5 (630.3 / 650.8 / 671.3), 16pt over a page; the same 0.5pt was the
 * residual of the table-rowheight probe's two exact rows.</p>
 *
 * <p>Both FO pathways, measured in FOP's own area tree.</p>
 *
 * @since 17.1.0
 */
public class ExactRowHeightTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait with 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** Four rows, each w:trHeight 400 twips = 20pt, with 0.5pt (w:sz 4) single borders. */
	private static WordprocessingMLPackage pkg(String hRule) throws Exception {
		StringBuilder rows = new StringBuilder();
		for (int i = 1; i <= 4; i++) {
			rows.append("<w:tr><w:trPr><w:trHeight w:val=\"400\" w:hRule=\"").append(hRule)
					.append("\"/></w:trPr>")
					.append("<w:tc><w:p><w:r><w:t>row ").append(i).append("</w:t></w:r></w:p></w:tc>")
					.append("<w:tc><w:p><w:r><w:t>value ").append(i).append("</w:t></w:r></w:p></w:tc></w:tr>");
		}
		String tbl = "<w:tbl><w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/>"
				+ "<w:tblLayout w:type=\"fixed\"/>"
				+ "<w:tblBorders><w:top w:val=\"single\" w:sz=\"4\"/><w:left w:val=\"single\" w:sz=\"4\"/>"
				+ "<w:bottom w:val=\"single\" w:sz=\"4\"/><w:right w:val=\"single\" w:sz=\"4\"/>"
				+ "<w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/>"
				+ "</w:tblBorders></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4500\"/><w:gridCol w:w=\"4500\"/></w:tblGrid>"
				+ rows + "</w:tbl>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + tbl + "<w:p/>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/**
	 * The distinct block-progression offsets of the table's cell areas, in millipoints:
	 * FOP positions each cell absolutely within the table's reference area, so the
	 * difference between consecutive offsets is the row pitch.
	 */
	private static List<Double> rowOffsets(org.w3c.dom.Document areaTree) {
		List<Double> offsets = new ArrayList<>();
		NodeList blocks = areaTree.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element block = (Element) blocks.item(i);
			if (!"absolute".equals(block.getAttribute("positioning"))) continue;
			if (block.getAttribute("top-offset").length() == 0) continue;
			double top = Double.parseDouble(block.getAttribute("top-offset"));
			if (!offsets.isEmpty() && Math.abs(offsets.get(offsets.size() - 1) - top) < 0.5) continue;
			offsets.add(top);
		}
		return offsets;
	}

	private void exactRowsAreExactlyThatTall(int flags) throws Exception {
		List<Double> offsets = rowOffsets(areaTree(pkg("exact"), flags));
		assertTrue("expected one offset per row, got " + offsets, offsets.size() >= 4);
		for (int i = 1; i < 4; i++) {
			assertEquals("row " + i + " pitch (millipoints), offsets " + offsets,
					20000.0, offsets.get(i) - offsets.get(i - 1), 1.0);
		}
	}

	@Test
	public void exactRowsAreExactlyThatTallVisitor() throws Exception {
		exactRowsAreExactlyThatTall(Docx4J.FLAG_NONE);
	}

	@Test
	public void exactRowsAreExactlyThatTallXslt() throws Exception {
		exactRowsAreExactlyThatTall(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** w:hRule="atLeast" is untouched: FOP's minimum is what Word's is. */
	@Test
	public void atLeastRowsAreNotClipped() throws Exception {
		org.docx4j.convert.out.FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg("atLeast"));
		foSettings.setApacheFopMime(org.docx4j.convert.out.FOSettings.INTERNAL_FO_MIME);
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		String fo = new String(baos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		assertTrue("atLeast rows must keep their height", fo.contains("height=\"20pt\""));
		assertTrue("atLeast rows must not be clipped", !fo.contains("overflow=\"hidden\""));
	}
}
