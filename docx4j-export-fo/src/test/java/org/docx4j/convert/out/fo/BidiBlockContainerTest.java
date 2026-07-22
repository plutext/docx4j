package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Issue 660: a w:bidi paragraph must establish an RTL paragraph embedding
 * level in the FO output, done by wrapping its fo:block in
 * fo:block-container writing-mode="rl-tb"; without that, FOP lays out the
 * runs of a mixed RTL/LTR paragraph in the wrong order.
 *
 * And a w:rtl run must NOT be wrapped in fo:bidi-override: FOP handles that
 * by reversing the characters itself, without applying the font's GSUB
 * rules, so Arabic came out unshaped (and in mixed paragraphs, mis-ordered).
 * The text is stored in logical order, so FOP's own Unicode bidi algorithm
 * implementation handles it, given the correct paragraph embedding level.
 */
public class BidiBlockContainerTest {

	@Test
	public void testXsl() throws Exception {
		check(convert(Docx4J.FLAG_EXPORT_PREFER_XSL));
	}

	@Test
	public void testNonXsl() throws Exception {
		check(convert(Docx4J.FLAG_EXPORT_PREFER_NONXSL));
	}

	private void check(String fo) {
		assertEquals("fo:bidi-override present", 0, count(fo, "bidi-override"));
		// the bidi paragraph, and only it, is wrapped
		// (start and end tag, and the element may or may not carry an fo: prefix)
		assertEquals("block-container count", 2, count(fo, "block-container"));
		assertEquals("writing-mode count", 1, count(fo, "writing-mode=\"rl-tb\""));
	}

	private String convert(int flags) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement(
				(Document)XmlUtils.unmarshalString(documentXML));

		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	private int count(String s, String token) {
		int count = 0;
		int i = s.indexOf(token);
		while (i >= 0) {
			count++;
			i = s.indexOf(token, i + token.length());
		}
		return count;
	}

	// A bidi paragraph with RTL and LTR runs (as in issue 660), then an
	// ordinary LTR paragraph
	String documentXML = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
            + "<w:body>"

                        + "<w:p>"
                            + "<w:pPr><w:bidi/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rtl/></w:rPr>"
                                + "<w:t xml:space=\"preserve\">ذهبتُ إلى الـ </w:t>"
                            +"</w:r>"
                            + "<w:r>"
                                + "<w:t>Mall</w:t>"
                            +"</w:r>"
                            + "<w:r>"
                                + "<w:rPr><w:rtl/></w:rPr>"
                                + "<w:t xml:space=\"preserve\"> لشراءِ حاسوبٍ جديدٍ.</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        + "<w:p>"
                            + "<w:r>"
                                + "<w:t>An ordinary paragraph.</w:t>"
                            +"</w:r>"
                        +"</w:p>"

        +"</w:body>"
    +"</w:document>";
}
