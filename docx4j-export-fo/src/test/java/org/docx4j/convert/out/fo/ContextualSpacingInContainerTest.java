package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * w:contextualSpacing pairs paragraphs through a borders/shading container: the seams
 * inside the container and the seam from its last paragraph to the paragraph after it.
 * See {@code WordLayoutFixups.paragraphBlocks} and word-layout-rules.md §3.
 *
 * @since 17.1.1
 */
public class ContextualSpacingInContainerTest {

	private static final String SHD = "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FFFFFF\"/>";

	private static String p(String pPr, String text) {
		return "<w:p><w:pPr>" + pPr + "</w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String convert(String body) throws Exception {
		String xml = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>" + body
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
		String styles = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:docDefaults>"
				+ "<w:pPrDefault><w:pPr><w:spacing w:after=\"200\" w:line=\"276\" w:lineRule=\"auto\"/></w:pPr></w:pPrDefault></w:docDefaults>"
				+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style></w:styles>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(xml));
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement((org.docx4j.wml.Styles) XmlUtils.unmarshalString(styles));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	/** space-after of each paragraph block holding the given text, in document order. */
	private static List<String> spaceAfter(String fo, String... texts) {
		List<String> out = new ArrayList<>();
		for (String t : texts) {
			int i = fo.indexOf(">" + t + "<");
			assertTrue(t, i > 0);
			int start = fo.lastIndexOf("<block", i);
			int start2 = fo.lastIndexOf("<fo:block", i);
			String tag = fo.substring(Math.max(start, start2), fo.indexOf(">", Math.max(start, start2)));
			Matcher m = Pattern.compile("space-after=\"([^\"]*)\"").matcher(tag);
			out.add(m.find() ? m.group(1) : "none");
		}
		return out;
	}

	@Test
	public void contextualParagraphsInsideAShadingContainerGetNoGap() throws Exception {
		String c = SHD + "<w:contextualSpacing/>";
		List<String> after = spaceAfter(convert(p(c, "one") + p(c, "two") + p(c, "three") + p("", "four")), "one", "two", "three");
		assertEquals("0pt", after.get(0));
		assertEquals("0pt", after.get(1));
		// the last one is followed by a plain Normal paragraph - same style, contextual on one side: no gap
		assertEquals("0pt", after.get(2));
	}

	@Test
	public void aNonContextualNeighbourOfADifferentStyleKeepsItsSpace() throws Exception {
		String c = SHD + "<w:contextualSpacing/>";
		List<String> after = spaceAfter(convert(p(c, "one") + p(c, "two") + p("<w:pStyle w:val=\"Heading1\"/>", "head")), "one", "two");
		assertEquals("0pt", after.get(0));
		assertEquals("10pt", after.get(1));
	}
}
