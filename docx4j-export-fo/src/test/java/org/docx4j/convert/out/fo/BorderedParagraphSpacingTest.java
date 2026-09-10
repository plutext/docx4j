package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
 * A bordered paragraph's space-before and space-after lie outside its border: the
 * wrapper carries them, the paragraph's block inside the border does not.  See
 * {@link WordLayoutFixups#spacingOutsideBorders} and word-layout-rules.md §3.
 *
 * @since 17.1.1
 */
public class BorderedParagraphSpacingTest {

	private static final String PROPERTY = "docx4j.convert.out.fo.wordLayout.spacingOutsideBorders";

	@After
	public void restore() {
		Docx4jProperties.setProperty(PROPERTY, "true");
	}

	private static final String BDR_TOP = "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/>";
	private static final String BDR_BOTTOM = "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/>";

	private static String p(String pPr, String text) {
		return "<w:p><w:pPr>" + pPr + "</w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String convert(String body) throws Exception {
		String xml = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ p("<w:spacing w:after=\"120\"/>", "before") + body + p("", "after")
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(xml));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	/** The start tags, in order, of the wrapper block with a border and of every
	 *  block-level element inside it. */
	private static List<String> borderedRun(String fo) {
		Matcher w = Pattern.compile("<fo:block[^>]*border-(top|bottom)-width[^>]*>").matcher(fo);
		assertTrue("a bordered wrapper", w.find());
		int end = fo.indexOf("</fo:block>", w.end());
		List<String> tags = new ArrayList<>();
		tags.add(w.group());
		Matcher m = Pattern.compile("<(?:fo:)?(block|list-block)\\b[^>]*>").matcher(fo.substring(w.end(), end));
		while (m.find()) tags.add(m.group());
		return tags;
	}

	private static boolean has(String tag, String attr) {
		return tag.contains(" " + attr + "=\"");
	}

	@Test
	public void aBorderedParagraphKeepsItsSpacingOnTheWrapperOnly() throws Exception {
		List<String> run = borderedRun(convert(p("<w:pBdr>" + BDR_TOP + BDR_BOTTOM + "</w:pBdr><w:spacing w:before=\"120\" w:after=\"120\"/>", "bordered")));
		assertTrue(run.get(0), has(run.get(0), "space-before") && has(run.get(0), "space-after"));
		assertEquals("6pt", Pattern.compile("space-before=\"([^\"]*)\"").matcher(run.get(0)).results().findFirst().get().group(1));
		String inner = run.get(1);
		assertFalse(inner, has(inner, "space-before"));
		assertFalse(inner, has(inner, "space-after"));
	}

	@Test
	public void aNumberedBorderedParagraphsListBlockLosesThemToo() throws Exception {
		List<String> run = borderedRun(convert(p("<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr><w:pBdr>" + BDR_TOP + BDR_BOTTOM + "</w:pBdr><w:spacing w:before=\"120\" w:after=\"120\"/>", "numbered")));
		String listBlock = run.stream().filter(t -> t.contains("list-block")).findFirst().get();
		assertFalse(listBlock, has(listBlock, "space-before"));
		assertFalse(listBlock, has(listBlock, "space-after"));
	}

	@Test
	public void aTopBorderOnlyLeavesTheSpaceAfterInside() throws Exception {
		List<String> run = borderedRun(convert(p("<w:pBdr>" + BDR_TOP + "</w:pBdr><w:spacing w:before=\"120\" w:after=\"120\"/>", "top only")));
		String inner = run.get(1);
		assertFalse(inner, has(inner, "space-before"));
		assertTrue(inner, has(inner, "space-after"));
	}

	/** Two paragraphs sharing one box: the first loses its before, the last its after,
	 *  and the seam between them keeps both. */
	@Test
	public void aRunOfBorderedParagraphsKeepsTheSpacingBetweenThem() throws Exception {
		String pPr = "<w:pBdr>" + BDR_TOP + BDR_BOTTOM + "</w:pBdr><w:spacing w:before=\"120\" w:after=\"120\"/>";
		List<String> run = borderedRun(convert(p(pPr, "first") + p(pPr, "second")));
		List<String> paras = new ArrayList<>();
		for (String t : run.subList(1, run.size())) if (t.contains("docx4j:pstyle") || t.contains("docx4j-pstyle")) paras.add(t);
		if (paras.size() < 2) paras = run.subList(1, run.size());
		assertTrue(paras.size() >= 2);
		String first = paras.get(0), last = paras.get(paras.size() - 1);
		assertFalse(first, has(first, "space-before"));
		assertTrue(first, has(first, "space-after"));
		assertTrue(last, has(last, "space-before"));
		assertFalse(last, has(last, "space-after"));
	}

	@Test
	public void thePropertyTurnsItOff() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "false");
		List<String> run = borderedRun(convert(p("<w:pBdr>" + BDR_TOP + BDR_BOTTOM + "</w:pBdr><w:spacing w:before=\"120\" w:after=\"120\"/>", "bordered")));
		assertTrue(run.get(1), has(run.get(1), "space-before"));
	}
}
