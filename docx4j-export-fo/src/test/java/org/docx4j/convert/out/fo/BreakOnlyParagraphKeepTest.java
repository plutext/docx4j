package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;

/**
 * A paragraph of nothing but one or two {@code w:br} is kept together, as Word's widow
 * control keeps a two- or three-line paragraph whole; FOP would otherwise break between
 * the nested blocks the breaks are written as.  See
 * {@link WordLayoutFixups#keepBreakOnlyParagraphsTogether} and word-layout-rules.md §3.
 *
 * @since 17.1.1
 */
public class BreakOnlyParagraphKeepTest {

	private static final String PROPERTY = "docx4j.convert.out.fo.wordLayout.keepBreakOnlyParagraph";

	@After
	public void restore() {
		Docx4jProperties.setProperty(PROPERTY, "true");
	}

	private static String doc(String paragraphs) {
		return "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ "<w:p><w:r><w:t>before</w:t></w:r></w:p>" + paragraphs
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
	}

	private static String breaks(int n, String pPr, String textAfter) {
		StringBuilder sb = new StringBuilder("<w:p>");
		if (pPr != null) sb.append("<w:pPr>").append(pPr).append("</w:pPr>");
		for (int i = 0; i < n; i++) sb.append("<w:r><w:br/></w:r>");
		if (textAfter != null) sb.append("<w:r><w:t>").append(textAfter).append("</w:t></w:r>");
		return sb.append("</w:p>").toString();
	}

	private static String convert(String paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(doc(paragraphs)));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	private static int keptBlocks(String fo) {
		int n = 0;
		Matcher m = Pattern.compile("<fo:block[^>]*keep-together\\.within-page=\"always\"").matcher(fo);
		while (m.find()) n++;
		return n;
	}

	@Test
	public void oneOrTwoBreaksAloneAreKeptTogether() throws Exception {
		assertEquals(1, keptBlocks(convert(breaks(1, null, null))));
		assertEquals(1, keptBlocks(convert(breaks(2, null, null))));
	}

	/** Four lines: Word may split them two and two. */
	@Test
	public void threeBreaksAreNot() throws Exception {
		assertEquals(0, keptBlocks(convert(breaks(3, null, null))));
	}

	@Test
	public void aParagraphWithTextIsNot() throws Exception {
		assertEquals(0, keptBlocks(convert(breaks(1, null, "some text"))));
		assertEquals(0, keptBlocks(convert("<w:p><w:r><w:t>one</w:t></w:r><w:r><w:br/></w:r><w:r><w:t>two</w:t></w:r></w:p>")));
	}

	@Test
	public void widowControlOffIsNot() throws Exception {
		assertEquals(0, keptBlocks(convert(breaks(2, "<w:widowControl w:val=\"0\"/>", null))));
	}

	/** A paragraph of one w:br is two lines in Word: the line before the break gets the
	 *  no-break space the leading-break rule writes (as a break with text after it does),
	 *  and the line after it the one the trailing-break rule writes - two in all, as a
	 *  paragraph of two breaks has (before the first, after the last).  A break followed by
	 *  text gets the leading one only; an empty paragraph none. */
	@Test
	public void aLoneBreakOpensWithItsOwnLine() throws Exception {
		assertEquals(2, nbsp(convert(breaks(1, null, null))));
		assertEquals(2, nbsp(convert(breaks(2, null, null))));
		assertEquals(1, nbsp(convert(breaks(1, null, "text after"))));
		assertEquals(0, nbsp(convert("<w:p/>")));
	}

	private static int nbsp(String fo) {
		int n = 0;
		for (int i = 0; i < fo.length(); i++) if (fo.charAt(i) == '\u00a0') n++;
		return n;
	}

	@Test
	public void thePropertyTurnsItOff() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "false");
		assertEquals(0, keptBlocks(convert(breaks(2, null, null))));
	}
}
