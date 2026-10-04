package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.FontFallback;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A font the box hasn't got used to fall back to whatever the document's default font
 * mapped to, whether or not that could render the run: Georgian set in a font we lack
 * came out as a row of notdef boxes (FOP's "Glyph ... not available", rendered as '#')
 * even on a box with Georgian faces installed.  The fallback is glyph-aware and
 * class-aware since 17.0.5; CR-001 cause C3.
 *
 * <p>The area tree is checked as well as the FO, since a family the FOP configuration
 * doesn't declare would silently be rendered in FOP's default font instead.</p>
 *
 * @since 17.0.5
 */
public class GlyphFallbackTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** a serif with Georgian, and a sans, neither of which a Linux box has */
	private static final String GEORGIAN_FONT = "Sylfaen";
	private static final String SANS_FONT = "Tahoma";

	private static final String GEORGIAN = "გამარჯობა";
	private static final String LATIN = "The quick brown fox";

	private static WordprocessingMLPackage pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ para(GEORGIAN_FONT, GEORGIAN)
				+ para(SANS_FONT, LATIN)
				+ "</w:body></w:document>"));
		return pkg;
	}

	private static String para(String font, String text) {
		return "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\"/>"
				+ "<w:sz w:val=\"24\"/></w:rPr><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static void assumeFontsAbsent() {
		Assume.assumeTrue(GEORGIAN_FONT + " is unexpectedly installed", PhysicalFonts.get(GEORGIAN_FONT)==null);
		Assume.assumeTrue("nothing installed has Georgian",
				FontFallback.selectCovering(GEORGIAN_FONT, GEORGIAN.codePoints().toArray())!=null);
	}

	/** the fo:inline holding this text, and its font-family */
	private static String family(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i=0; i<nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			if (text.equals(el.getTextContent()) && el.getAttribute("font-family").length()>0) {
				return el.getAttribute("font-family");
			}
		}
		return null;
	}

	@Test
	public void georgianIsSetInAFontWhichHasIt() throws Exception {

		assumeFontsAbsent();
		WordprocessingMLPackage pkg = pkg();
		org.w3c.dom.Document fo = w3cDomDocumentFromByteArray(toFO(pkg));

		// the document default is Calibri, which a Linux box renders in Carlito: no Georgian
		PhysicalFont documentDefault = pkg.getFontMapper().get("Calibri");

		int seen = 0;
		NodeList nl = fo.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i=0; i<nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			String text = el.getTextContent();
			String family = el.getAttribute("font-family");
			if (family.length()==0 || !FontFallback.needsCoverage(text.codePoints().toArray())) continue;
			seen++;
			PhysicalFont chosen = PhysicalFonts.get(family);
			assertNotNull(family + " is not an installed font", chosen);
			assertTrue("'" + text + "' is set in " + family + ", which has no glyphs for it",
					FontFallback.covers(chosen, text.codePoints().toArray()));
			if (documentDefault!=null) {
				assertNotEquals("still the document default's font", documentDefault.getName(), family);
			}
		}
		assertTrue("no fo:inline carries the Georgian text", seen>0);
	}

	/** An unmapped sans is set in a sans, not in whatever the document default is. */
	@Test
	public void anUnmappedSansIsSetInASans() throws Exception {

		Assume.assumeTrue(SANS_FONT + " is unexpectedly installed", PhysicalFonts.get(SANS_FONT)==null);
		org.w3c.dom.Document fo = w3cDomDocumentFromByteArray(toFO(pkg()));

		String family = family(fo, LATIN);
		assertNotNull("no fo:inline for the Latin text", family);
		assertNotEquals(SANS_FONT + " was set in a serif", FontFallback.FontClass.SERIF,
				FontFallback.classOf(family));
	}

	/**
	 * What FOP actually rendered.  A font-family the configuration doesn't declare is
	 * not an error: FOP quietly uses its default font, and the glyphs come out as '#'.
	 */
	@Test
	public void fopRendersTheGeorgianInThatFont() throws Exception {

		assumeFontsAbsent();
		org.w3c.dom.Document areaTree = areaTree(pkg(), Docx4J.FLAG_NONE);

		boolean seen = false;
		NodeList nl = areaTree.getElementsByTagName("text");
		for (int i=0; i<nl.getLength(); i++) {
			Element text = (Element)nl.item(i);
			String content = text.getTextContent();
			if (content==null || !FontFallback.needsCoverage(content.codePoints().toArray())) continue;
			seen = true;
			String fontName = text.getAttribute("font-name");
			PhysicalFont pf = PhysicalFonts.get(fontName);
			assertNotNull("FOP rendered '" + content + "' in " + fontName
					+ ", which is not one of ours - the family was not declared to it", pf);
			assertTrue("FOP rendered '" + content + "' in " + fontName + ", which has no glyphs for it",
					FontFallback.covers(pf, content.codePoints().toArray()));
		}
		assertTrue("the Georgian text did not reach the area tree", seen);
	}

	/** U+1F44D (thumbs up) and then U+1F600 (grinning face): one coverage group, two lines. */
	private static final int THUMBS_UP = 0x1F44D;
	private static final int GRINNING = 0x1F600;

	/**
	 * A later character of a group gets a font which has it.  The fallback chosen for a
	 * group is cached per document font, so it was chosen for the group's first character
	 * in the document; until 17.3.1 a later character it lacked used it anyway, and came
	 * out as '#' (CR-020's probe surrogate-pairs-bidi: U+1F44D chose Noto Sans Symbols 2,
	 * which has no U+1F600).  Runs only where this machine's fonts set that trap.
	 */
	@Test
	public void aLaterCharacterOfTheGroupGetsAFontWhichHasIt() throws Exception {

		PhysicalFont forThumbs = FontFallback.selectCovering("Liberation Serif", new int[] { THUMBS_UP });
		Assume.assumeTrue("nothing installed has U+1F44D", forThumbs!=null);
		Assume.assumeTrue(forThumbs.getName() + " has U+1F600 too, so there is no trap here",
				!FontFallback.covers(forThumbs, new int[] { GRINNING }));
		Assume.assumeTrue("nothing installed has U+1F600",
				FontFallback.selectCovering("Liberation Serif", new int[] { GRINNING })!=null);

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ para("Liberation Serif", "first " + new String(Character.toChars(THUMBS_UP)))
				+ para("Liberation Serif", "then " + new String(Character.toChars(GRINNING)))
				+ "</w:body></w:document>"));

		org.w3c.dom.Document fo = w3cDomDocumentFromByteArray(toFO(pkg));
		int seen = 0;
		NodeList nl = fo.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i=0; i<nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			String text = el.getTextContent();
			String family = el.getAttribute("font-family");
			if (family.length()==0 || text.codePoints().noneMatch(cp -> cp==GRINNING || cp==THUMBS_UP)) continue;
			if (el.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline").getLength()>0) continue;
			seen++;
			PhysicalFont chosen = PhysicalFonts.get(family);
			assertNotNull(family + " is not an installed font", chosen);
			assertTrue("'" + text + "' is set in " + family + ", which has no glyph for it",
					FontFallback.covers(chosen, text.codePoints().filter(cp -> cp > 0xFFFF).toArray()));
		}
		assertTrue("no fo:inline carries the emoji", seen>=2);

		// and FOP draws them, rather than its '#'
		org.w3c.dom.Document areaTree = areaTree(pkg, Docx4J.FLAG_NONE);
		String drawn = areaTree.getDocumentElement().getTextContent();
		assertTrue("U+1F600 did not reach the area tree", drawn.contains(new String(Character.toChars(GRINNING))));
		assertTrue("U+1F44D did not reach the area tree", drawn.contains(new String(Character.toChars(THUMBS_UP))));
		assertTrue("FOP drew a '#'", !drawn.contains("#"));
	}

	private byte[] toFO(WordprocessingMLPackage pkg) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		return baos.toByteArray();
	}
}
