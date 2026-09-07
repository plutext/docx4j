package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The page masters a section produces: the parity the {@code odd-or-even} alternatives
 * select on ({@code w:pgNumType/@w:start}) and the margins a mirrored even page takes
 * ({@code w:mirrorMargins}).  Both FO pathways build them in LayoutMasterSetBuilder, so
 * one assertion covers both.
 *
 * @since 17.1.0
 */
public class PageMastersParityTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String R = "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** @param settingsXml the w:settings children, or null */
	private static org.w3c.dom.Document fo(String sectPr, String settingsXml, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>"
				+ "<w:p><w:r><w:t>body</w:t></w:r></w:p>"
				+ sectPr
				+ "</w:body></w:document>"));
		if (settingsXml != null) {
			DocumentSettingsPart settings = new DocumentSettingsPart();
			settings.setJaxbElement((CTSettings)XmlUtils.unmarshalString(
					"<w:settings " + W + ">" + settingsXml + "</w:settings>",
					org.docx4j.jaxb.Context.jc, CTSettings.class));
			pkg.getMainDocumentPart().addTargetPart(settings);
		}
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element masterNamed(org.w3c.dom.Document doc, String name) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "simple-page-master");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (name.equals(el.getAttribute("master-name"))) return el;
		}
		return null;
	}

	/** The odd-or-even of the conditional naming this master, or null. */
	private static String parityFor(org.w3c.dom.Document doc, String masterName) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "conditional-page-master-reference");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (masterName.equals(el.getAttribute("master-reference"))) {
				String v = el.getAttribute("odd-or-even");
				return v.length() == 0 ? null : v;
			}
		}
		return null;
	}

	private static final String PG_MAR =
			"<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1418\" w:right=\"1418\" w:bottom=\"1418\" w:left=\"2268\""
			+ " w:header=\"709\" w:footer=\"709\" w:gutter=\"0\"/>";

	/**
	 * XSL 1.1 &#xa7;6.4.5 requires {@code initial-page-number} to be a positive integer and
	 * FOP clamps a smaller one, so a section whose {@code w:pgNumType w:start="0"} lays
	 * out as folio 1 where Word calls it page 0: the ODD and EVEN alternatives must swap
	 * for the headers to land on the side Word puts them.  Measured: with an even header
	 * of no {@code w:jc} and a right-aligned default one, Word's page 2 header is
	 * right-aligned at x=430.3..524.7 and ours was the even one at 70.9..162.9.
	 */
	@Test
	public void pgNumTypeStartZeroSwapsTheParity() throws Exception {
		String sectPr = "<w:p><w:pPr><w:sectPr>" + PG_MAR
				+ "<w:pgNumType w:start=\"0\"/>"
				+ "</w:sectPr></w:pPr></w:p>";
		for (int flag : FLAGS) {
			// mirrored margins give this section a parity-selected pair to read the
			// swap off; the same flag drives the odd/even header alternatives
			org.w3c.dom.Document doc = fo(sectPr, "<w:mirrorMargins/>", flag);
			assertEquals(flagName(flag) + ": Word's even page is FOP's odd folio",
					"odd", parityFor(doc, "s1-simple-mirrored"));
			assertEquals(flagName(flag) + ": Word's odd page is FOP's even folio",
					"even", parityFor(doc, "s1-simple"));
		}
	}

	/** An even start FOP does not have to clamp leaves the parity as it is. */
	@Test
	public void pgNumTypeStartTwoKeepsTheParity() throws Exception {
		String sectPr = "<w:p><w:pPr><w:sectPr>" + PG_MAR
				+ "<w:pgNumType w:start=\"2\"/>"
				+ "</w:sectPr></w:pPr></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(sectPr, "<w:mirrorMargins/>", flag);
			assertEquals(flagName(flag), "even", parityFor(doc, "s1-simple-mirrored"));
			assertEquals(flagName(flag), "odd", parityFor(doc, "s1-simple"));
		}
	}

	/** With no w:pgNumType the parity is Word's, so nothing swaps. */
	@Test
	public void noPgNumTypeLeavesTheParityAlone() throws Exception {
		String sectPr = "<w:p><w:pPr><w:sectPr>" + PG_MAR + "</w:sectPr></w:pPr></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(sectPr, null, flag);
			assertNotNull(flagName(flag) + ": no page master at all", masterNamed(doc, "s1-simple"));
			assertEquals(flagName(flag) + ": margin-left", "113.4pt",
					masterNamed(doc, "s1-simple").getAttribute("margin-left"));
		}
	}

	/**
	 * {@code w:mirrorMargins}: {@code w:pgMar/@w:left} is the inside margin, so an even
	 * (left-hand) page takes it on the right.  Measured on a 42-page document whose
	 * three sectPr all say {@code w:left="2268" w:right="1418"}: Word's even pages start
	 * at x=70.8 and ours at 113.4.
	 */
	@Test
	public void mirrorMarginsSwapsTheEvenPage() throws Exception {
		String sectPr = "<w:p><w:pPr><w:sectPr>" + PG_MAR + "</w:sectPr></w:pPr></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(sectPr, "<w:mirrorMargins/>", flag);
			Element odd = masterNamed(doc, "s1-simple");
			Element even = masterNamed(doc, "s1-simple-mirrored");
			assertNotNull(flagName(flag) + ": no odd master", odd);
			assertNotNull(flagName(flag) + ": no mirrored even master", even);
			assertEquals(flagName(flag) + ": odd margin-left", "113.4pt", odd.getAttribute("margin-left"));
			assertEquals(flagName(flag) + ": odd margin-right", "70.9pt", odd.getAttribute("margin-right"));
			assertEquals(flagName(flag) + ": even margin-left", "70.9pt", even.getAttribute("margin-left"));
			assertEquals(flagName(flag) + ": even margin-right", "113.4pt", even.getAttribute("margin-right"));
			// and the alternatives select on parity
			assertEquals(flagName(flag) + ": the mirrored master is the even one",
					"even", parityFor(doc, "s1-simple-mirrored"));
			assertEquals(flagName(flag) + ": the plain master is the odd one",
					"odd", parityFor(doc, "s1-simple"));
			// the two masters serve the same regions, so one static-content covers both
			assertEquals(flagName(flag) + ": region-before name",
					regionName(odd, "region-before"), regionName(even, "region-before"));
			assertEquals(flagName(flag) + ": region-after name",
					regionName(odd, "region-after"), regionName(even, "region-after"));
		}
	}

	/** Without the setting there is no mirrored twin. */
	@Test
	public void noMirrorMarginsNoTwin() throws Exception {
		String sectPr = "<w:p><w:pPr><w:sectPr>" + PG_MAR + "</w:sectPr></w:pPr></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(sectPr, null, flag);
			assertNull(flagName(flag), masterNamed(doc, "s1-simple-mirrored"));
		}
	}

	private static String regionName(Element master, String localName) {
		NodeList nl = master.getElementsByTagNameNS(FO, localName);
		return nl.getLength() == 0 ? null : ((Element) nl.item(0)).getAttribute("region-name");
	}
}
