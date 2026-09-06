package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.fop.fonts.WordGlyphWidths;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;

/**
 * The glyph advances FOP measures with - and writes into the PDF's <code>/Widths</code> -
 * are rounded to the nearest 1/1000 em, not truncated (CR-001 §10).
 *
 * <p>FOP's <code>OpenFont.convertTTFUnit2PDFUnit</code> divides where it should round, so
 * every advance is up to one unit short: on 12pt Liberation Serif a 30-character line
 * measures 139.164pt where the font's own metrics give 139.295. Word measures with the
 * font's exact advances, and its own PDFs carry rounded widths.</p>
 *
 * <p>Which font the document is set in depends on what is installed, so this compares the
 * same document rendered with the correction on and off rather than naming widths: no
 * width may be smaller with it on, and their sum must be larger (any TrueType font has
 * advances which are not whole thousandths of an em).</p>
 *
 * @since 17.0.6
 */
public class GlyphAdvanceRoundingPdfTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	@After
	public void restore() {
		Docx4jProperties.getProperties().remove(WordGlyphWidths.PROPERTY);
	}

	private static byte[] pdf() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r><w:t>"
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
				+ "tempor incididunt ut labore et dolore magna aliqua."
				+ "</w:t></w:r></w:p></w:body></w:document>"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg, baos);
		return baos.toByteArray();
	}

	/** the /Widths array of each simple font of the document, by its /BaseFont name */
	private static Map<String, List<Integer>> widths(byte[] pdf) throws Exception {
		Map<String, List<Integer>> out = new TreeMap<String, List<Integer>>();
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			for (PDPage page : doc.getPages()) {
				PDResources resources = page.getResources();
				if (resources == null) continue;
				COSBase fonts = resources.getCOSObject().getDictionaryObject(COSName.FONT);
				if (!(fonts instanceof COSDictionary)) continue;
				for (COSName name : ((COSDictionary) fonts).keySet()) {
					COSBase base = ((COSDictionary) fonts).getDictionaryObject(name);
					if (!(base instanceof COSDictionary)) continue;
					COSDictionary font = (COSDictionary) base;
					COSBase widths = font.getDictionaryObject(COSName.WIDTHS);
					if (!(widths instanceof COSArray)) continue;
					List<Integer> values = new ArrayList<Integer>();
					for (COSBase w : (COSArray) widths) {
						if (w instanceof COSNumber) values.add(((COSNumber) w).intValue());
					}
					out.put(font.getNameAsString(COSName.BASE_FONT), values);
				}
			}
		}
		return out;
	}

	@Test
	public void widthsAreRoundedNotTruncated() throws Exception {

		Docx4jProperties.setProperty(WordGlyphWidths.PROPERTY, false);
		Map<String, List<Integer>> truncated = widths(pdf());

		Docx4jProperties.setProperty(WordGlyphWidths.PROPERTY, true);
		Map<String, List<Integer>> rounded = widths(pdf());

		assertTrue("no font widths in the PDF", truncated.size() > 0);
		assertEquals("the same fonts", truncated.keySet(), rounded.keySet());

		int larger = 0;
		for (String font : truncated.keySet()) {
			List<Integer> t = truncated.get(font), r = rounded.get(font);
			assertEquals(font + ": the same number of widths", t.size(), r.size());
			for (int i = 0; i < r.size(); i++) {
				assertTrue(font + ": rounding never narrows an advance: " + r.get(i)
						+ " against " + t.get(i), r.get(i) >= t.get(i));
				assertTrue(font + ": and never by more than a unit at " + i + ": " + r.get(i)
						+ " against " + t.get(i), r.get(i) - t.get(i) <= 1);
				if (r.get(i) > t.get(i)) larger++;
			}
		}
		assertTrue("no advance was rounded up at all", larger > 0);
	}
}
