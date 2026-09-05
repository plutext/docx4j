package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A PAGEREF field - which is what every entry of a table of contents holds - whose
 * bookmark the document no longer contains keeps the result Word cached for it.
 *
 * <p>Word paints that cached result; an {@code fo:page-number-citation} whose
 * {@code ref-id} is never emitted is painted as nothing at all by FOP, which cost one
 * corpus document all 150 of its page numbers (its TOC hyperlinks point at headings
 * that have been deleted).  Both FO pathways.</p>
 *
 * @since 17.0.6
 */
public class PagerefMissingBookmarkTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static final String ENTRY =
			"<w:p><w:fldSimple w:instr=\" PAGEREF _Toc123 \\h \">"
			+ "<w:r><w:t>7</w:t></w:r></w:fldSimple></w:p>";

	private static final String HEADING =
			"<w:p><w:bookmarkStart w:id=\"1\" w:name=\"_Toc123\"/>"
			+ "<w:r><w:t>Heading</w:t></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>";

	private static String fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), "UTF-8");
	}

	@Test
	public void aPagerefToAMissingBookmarkKeepsTheCachedResult() throws Exception {
		for (int flag : FLAGS) {
			String fo = fo(ENTRY, flag);
			assertFalse("a citation of an id nothing emits, which FOP paints as nothing: " + fo,
					fo.contains("page-number-citation"));
			assertFalse("a link to an id nothing emits: " + fo,
					fo.contains("internal-destination=\"_Toc123\""));
			assertTrue("the cached page number is gone: " + fo, fo.contains(">7<"));
		}
	}

	@Test
	public void aPagerefToABookmarkTheDocumentHasIsStillAField() throws Exception {
		for (int flag : FLAGS) {
			String fo = fo(ENTRY + HEADING, flag);
			assertTrue("no page-number-citation: " + fo,
					fo.contains("page-number-citation"));
			assertTrue("no ref-id: " + fo, fo.contains("ref-id=\"_Toc123\""));
			assertTrue("no bookmark to point at: " + fo, fo.contains("id=\"_Toc123\""));
		}
	}
}
