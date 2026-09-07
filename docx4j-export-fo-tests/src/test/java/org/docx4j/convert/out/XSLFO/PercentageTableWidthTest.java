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
 * A {@code w:tblW} in <b>pct</b> is a width Word gives the table exactly (CR-001
 * &#xa7;6.5): the percentage is of the text column, the {@code w:tblGrid} is scaled to
 * it, and a table wider than the column overhangs the right margin rather than being
 * refitted.
 *
 * <p>Measured on the {@code table-grid-pct} probe, whose text column is 9026 twips
 * (A4 less two 1-inch margins):</p>
 * <ul>
 * <li>{@code w:tblW 5000 pct} with a fixed layout and a grid of 4614+4614 = 9228 (2.2
 *     per cent over) gives Word two columns of 224.98 and 225.22pt - the grid scaled by
 *     9026/9228 - and the twin whose grid is 3000+3000 comes out at the same widths,
 *     the grid scaled up by 1.504.  docx4j used the grid as it stood.</li>
 * <li>{@code w:tblW 6000 pct} - 120 per cent - is drawn 541.2pt wide, from the left
 *     margin to x=613.2 on a 523.2pt column, where docx4j clamped it to the column.</li>
 * <li>{@code w:tblW 5000 pct} with {@code w:tblInd 720} is the full 9026 twips wide
 *     starting at the indent, so the percentage is of the column and not of what the
 *     indent leaves of it.</li>
 * </ul>
 *
 * <p>Both FO pathways.</p>
 *
 * @since 17.1.0
 */
public class PercentageTableWidthTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait with 1in margins: a 9026-twip (451.3pt) text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "xslt";
	}

	private static WordprocessingMLPackage pkg(int pct, int gridCol, boolean fixed, int tblInd)
			throws Exception {
		String cellW = fixed ? "<w:tcW w:w=\"" + gridCol + "\" w:type=\"dxa\"/>"
				: "<w:tcW w:w=\"0\" w:type=\"auto\"/>";
		String cell = "<w:tc><w:tcPr>" + cellW + "</w:tcPr>"
				+ "<w:p><w:r><w:t>Cell</w:t></w:r></w:p></w:tc>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr>"
				+ "<w:tblW w:w=\"" + pct + "\" w:type=\"pct\"/>"
				+ (fixed ? "<w:tblLayout w:type=\"fixed\"/>" : "")
				+ (tblInd > 0 ? "<w:tblInd w:w=\"" + tblInd + "\" w:type=\"dxa\"/>" : "")
				+ "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"" + gridCol + "\"/><w:gridCol w:w=\"" + gridCol + "\"/></w:tblGrid>"
				+ "<w:tr>" + cell + cell + "</w:tr></w:tbl>"
				+ "<w:p/>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The sum of the fo:table-column widths, in points. */
	private static double tableWidthPt(org.w3c.dom.Document doc) {
		NodeList cols = doc.getElementsByTagNameNS(FO_NS, "table-column");
		assertTrue("no columns", cols.getLength() > 0);
		double sum = 0;
		for (int i = 0; i < cols.getLength(); i++) {
			sum += pt(((Element) cols.item(i)).getAttribute("column-width"));
		}
		return sum;
	}

	private static double pt(String v) {
		v = v.trim();
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		throw new IllegalArgumentException(v);
	}

	@Test
	public void anOverWideGridIsScaledDownToThePercentage() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(flagName(flags) + ": 9228 twips of grid at 100 per cent of 9026",
					451.3, tableWidthPt(fo(pkg(5000, 4614, true, 0), flags)), 0.3);
		}
	}

	@Test
	public void aNarrowGridIsScaledUpToThePercentage() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(flagName(flags) + ": 6000 twips of grid at 100 per cent of 9026",
					451.3, tableWidthPt(fo(pkg(5000, 3000, true, 0), flags)), 0.3);
		}
	}

	@Test
	public void aPercentageOverAHundredOverhangsTheColumn() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(flagName(flags) + ": 120 per cent of a 451.3pt column",
					541.6, tableWidthPt(fo(pkg(6000, 4513, false, 0), flags)), 0.5);
		}
	}

	@Test
	public void theIndentDoesNotComeOffThePercentage() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(flagName(flags) + ": the percentage is of the whole text column",
					451.3, tableWidthPt(fo(pkg(5000, 4513, false, 720), flags)), 0.5);
		}
	}

	/** A grid which already sums to the percentage width is left exactly alone. */
	@Test
	public void aGridWhichAlreadyFitsIsUntouched() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(flagName(flags) + ": 4513+4513 is the column",
					451.3, tableWidthPt(fo(pkg(5000, 4513, true, 0), flags)), 0.15);
		}
	}
}
