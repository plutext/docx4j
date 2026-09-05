package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Word applies no standard ligature unless the run asks for one (w14:ligatures); FOP
 * applies the GSUB feature "liga" to every font that has one, unconditionally.  In
 * Calibri and its metric twin Carlito that turns "ti" and "tt" into ligature glyphs
 * which have no cmap entry of their own, so FOP mints a private-use code point for
 * them and the PDF's ToUnicode maps the ligature to U+E000: the text is painted in
 * the right place but cannot be extracted, searched or read by a screen reader.
 * Measured over a real-document corpus, 82 of 232 lines of a French document were
 * affected.
 *
 * <p>FOP has no per-run or per-feature switch, so docx4j declares a second copy of
 * each TrueType font with encoding-mode="single-byte" - which FOP loads as a simple
 * TrueType font, applying no OpenType feature to it - and sends runs of Latin text
 * which ask for neither ligatures nor kerning to that declaration, under the
 * font-family name plus {@link RunFontSelector#NOLIGA_SUFFIX}.  A run which does ask
 * for ligatures keeps the ordinary declaration, and so does anything but Latin: a
 * single-byte font chains 256-glyph encodings for whatever its primary one does not
 * hold, and measured over a real-document corpus a whole non-Latin alphabet then
 * loses characters from the PDF's text layer.</p>
 *
 * @since 17.0.5
 */
public class LigatureSuppressionTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String W14 = "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg(String runs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " " + W14 + "><w:body><w:p>" + runs + "</w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static String run(String rPr, String text) {
		return "<w:r><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>" + rPr + "</w:rPr>"
				+ "<w:t>" + text + "</w:t></w:r>";
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** the font-family of the first fo:inline which has one */
	private static String family(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "inline");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getAttribute("font-family").length() > 0) return el.getAttribute("font-family");
		}
		return null;
	}

	/** the name PhysicalFonts knows the Calibri metric twin by */
	private static final String CARLITO = "Carlito Regular";

	/** the test needs a TrueType stand-in for Calibri (Carlito) on the box */
	private static void assumeTrueTypeCalibri() throws Exception {
		PhysicalFonts.discoverPhysicalFonts();
		org.docx4j.fonts.PhysicalFont pf = PhysicalFonts.get(CARLITO);
		Assume.assumeTrue("no Carlito installed", pf != null && pf.getEmbeddedURI() != null);
		Assume.assumeTrue("Carlito is not TrueType-flavoured here",
				org.docx4j.fonts.fop.util.FopConfigUtil.isTrueTypeFlavoured(pf.getEmbeddedURI().toString()));
	}

	// --------------------------------------------------------------- the default

	private void plainLatinRunUsesTheNoLigatureTwin(int flags) throws Exception {
		assumeTrueTypeCalibri();
		String family = family(fo(pkg(run("", "attention to notification")), flags));
		assertTrue("expected the no-ligature twin, got " + family,
				family != null && family.endsWith(RunFontSelector.NOLIGA_SUFFIX));
		// and it still resolves to the physical font
		assertEquals(PhysicalFonts.get(CARLITO), PhysicalFonts.get(family));
	}

	@Test
	public void visitor() throws Exception {
		plainLatinRunUsesTheNoLigatureTwin(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		plainLatinRunUsesTheNoLigatureTwin(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ------------------------------------------------------- when Word does apply them

	@Test
	public void aRunWhichAsksForLigaturesKeepsThem() throws Exception {
		assumeTrueTypeCalibri();
		String family = family(fo(pkg(
				run("<w14:ligatures w14:val=\"standard\"/>", "attention to notification")), Docx4J.FLAG_NONE));
		assertFalse("a run with w14:ligatures must keep the ordinary declaration: " + family,
				family != null && family.endsWith(RunFontSelector.NOLIGA_SUFFIX));
	}

	/** Cyrillic keeps the ordinary declaration: a single-byte font would break its
	 *  text layer, and only Latin has the ligature problem in the first place. */
	@Test
	public void nonLatinKeepsTheOrdinaryDeclaration() throws Exception {
		assumeTrueTypeCalibri();
		String family = family(fo(pkg(run("", "\u0414\u043e\u0433\u043e\u0432\u043e\u0440 \u043e\u0431 \u043e\u043a\u0430\u0437\u0430\u043d\u0438\u0438")), Docx4J.FLAG_NONE));
		assertFalse("Cyrillic must not go to the single-byte twin: " + family,
				family != null && family.endsWith(RunFontSelector.NOLIGA_SUFFIX));
	}

	@Test
	public void aKernedRunKeepsItsKernedTwin() throws Exception {
		assumeTrueTypeCalibri();
		String family = family(fo(pkg(
				run("<w:kern w:val=\"16\"/><w:sz w:val=\"22\"/>", "attention")), Docx4J.FLAG_NONE));
		assertTrue("expected the kerned twin, got " + family,
				family != null && family.endsWith(RunFontSelector.KERNED_SUFFIX));
	}

	// -------------------------------------------------------------- and it renders

	/**
	 * The whole point: the PDF's text is the text of the document.  With the "liga"
	 * feature applied, "ti" came out as a private-use code point.
	 */
	@Test
	public void theTextSurvivesIntoThePdf() throws Exception {
		assumeTrueTypeCalibri();
		WordprocessingMLPackage pkg = pkg(run("", "attention notification institution"));
		ByteArrayOutputStream pdf = new ByteArrayOutputStream();
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		Docx4J.toPDF(pkg, pdf);
		assertTrue("no PDF", pdf.size() > 0);

		// the area tree carries the characters FOP actually painted
		org.w3c.dom.Document areaTree = areaTree(pkg, Docx4J.FLAG_NONE);
		String painted = areaTree.getDocumentElement().getTextContent();
		for (int i = 0; i < painted.length(); i++) {
			char c = painted.charAt(i);
			assertFalse("a private-use code point was painted (a ligature with no Unicode): U+"
					+ Integer.toHexString(c), c >= 0xE000 && c <= 0xF8FF);
		}
		assertTrue(painted.contains("attention"));
	}

	/** Every declared font-family the FO uses must be declared to FOP. */
	@Test
	public void theTwinIsDeclared() throws Exception {
		assumeTrueTypeCalibri();
		WordprocessingMLPackage pkg = pkg(run("", "attention"));
		org.docx4j.convert.out.fopconf.Fop config =
				org.docx4j.fonts.fop.util.FopConfigUtil.createConfigurationObject(
						pkg.getFontMapper(), pkg.getMainDocumentPart().fontsInUse());
		boolean found = false;
		for (org.docx4j.convert.out.fopconf.Fonts.Font f
				: org.docx4j.fonts.fop.util.FopConfigUtil.get(config.getRenderers(), "application/pdf")
						.getFonts().getFont()) {
			for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : f.getFontTriplet()) {
				if (t.getName().endsWith(RunFontSelector.NOLIGA_SUFFIX)) {
					assertEquals("single-byte", f.getEncodingMode());
					found = true;
				}
			}
		}
		assertTrue("no +noliga declaration in the FOP configuration", found);
	}
}
