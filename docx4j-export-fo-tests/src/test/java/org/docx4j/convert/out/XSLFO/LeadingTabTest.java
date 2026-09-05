package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A tab at the start of a paragraph becomes a leader to Word's next tab stop
 * (CR-001 §6.10: the Getting Started guide's code blocks begin with tabs, and
 * Word starts their text 0.5in per tab); a tab after text is a leader of no
 * length, which the line manager sizes during layout.  Both FO pathways.
 */
public class LeadingTabTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FONT = "<w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/></w:rPr>";

	private static org.w3c.dom.Document fo(String paragraphs, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + paragraphs + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** leader-length of each fo:leader, in document order */
	private static List<String> leaders(org.w3c.dom.Document doc) {
		List<String> out = new ArrayList<>();
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "leader");
		for (int i = 0; i < nl.getLength(); i++) {
			Element l = (Element) nl.item(i);
			assertEquals("space", l.getAttribute("leader-pattern"));
			out.add(l.getAttribute("leader-length"));
		}
		return out;
	}

	private static String p(String pPr, String runContent) {
		return "<w:p>" + (pPr == null ? "" : "<w:pPr>" + pPr + "</w:pPr>") + "<w:r>" + FONT + runContent + "</w:r></w:p>";
	}

	private void check(int flags) throws Exception {
		// two leading tabs, default stops every 720 twips
		assertEquals(java.util.Arrays.asList("36pt", "36pt"),
				leaders(fo(p(null, "<w:tab/><w:tab/><w:t>x</w:t>"), flags)));
		// a tab after text: not a leading tab, so it becomes a leader of no length which
		// the line manager sizes from the paragraph's stops (see TabStopHintsTest)
		assertEquals(java.util.Arrays.asList("0pt"),
				leaders(fo(p(null, "<w:t>a</w:t><w:tab/><w:t>b</w:t>"), flags)));
		// a custom stop at 1000 twips, then the default grid resumes (1440)
		assertEquals(java.util.Arrays.asList("50pt", "22pt"),
				leaders(fo(p("<w:tabs><w:tab w:val=\"left\" w:pos=\"1000\"/></w:tabs>", "<w:tab/><w:tab/><w:t>x</w:t>"), flags)));
		// a hanging indent: the first line starts at 360 and the left indent is a stop
		assertEquals(java.util.Arrays.asList("18pt"),
				leaders(fo(p("<w:ind w:left=\"720\" w:hanging=\"360\"/>", "<w:tab/><w:t>x</w:t>"), flags)));
		// a tab in the second run, the first being empty of text, is still leading
		assertEquals(java.util.Arrays.asList("36pt"),
				leaders(fo("<w:p><w:r>" + FONT + "</w:r><w:r>" + FONT + "<w:tab/><w:t>x</w:t></w:r></w:p>", flags)));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void lastLineIsNotJustifiedForIt() throws Exception {
		org.w3c.dom.Document doc = fo(p(null, "<w:tab/><w:t>x</w:t>"), Docx4J.FLAG_NONE);
		NodeList blocks = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			assertEquals("", ((Element) blocks.item(i)).getAttribute("text-align-last"));
		}
	}
}
