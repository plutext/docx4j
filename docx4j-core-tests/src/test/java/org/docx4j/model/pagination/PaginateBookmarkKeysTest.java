package org.docx4j.model.pagination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/** Paginate.bookmarkKeys: the paragraph a bookmark belongs to, for the TOC (CR-012 phase 3). */
public class PaginateBookmarkKeysTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static Map<String, String> keys(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body>" + body + "</w:body></w:document>"));
		return Paginate.bookmarkKeys(pkg.getMainDocumentPart());
	}

	@Test
	public void aBookmarkInsideAParagraphBelongsToIt() throws Exception {
		Map<String, String> keys = keys(
				"<w:p w14:paraId=\"AAAA0001\"><w:r><w:t>plain</w:t></w:r></w:p>"
				+ "<w:p w14:paraId=\"BBBB0002\"><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>"
				+ "<w:bookmarkStart w:id=\"0\" w:name=\"_Toc1\"/><w:r><w:t>Heading</w:t></w:r><w:bookmarkEnd w:id=\"0\"/></w:p>");
		assertEquals("BBBB0002", keys.get("_Toc1"));
	}

	@Test
	public void aBookmarkBetweenParagraphsBelongsToTheNext_andKeysAreTransientWithoutIds() throws Exception {
		Map<String, String> keys = keys(
				"<w:p><w:r><w:t>one</w:t></w:r></w:p>"
				+ "<w:bookmarkStart w:id=\"0\" w:name=\"whole\"/>"
				+ "<w:p><w:r><w:t>two</w:t></w:r></w:p>"
				+ "<w:bookmarkEnd w:id=\"0\"/>"
				+ "<w:tbl><w:tr><w:tc><w:p><w:bookmarkStart w:id=\"1\" w:name=\"cell\"/><w:r><w:t>three</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ "<w:bookmarkStart w:id=\"2\" w:name=\"trailing\"/><w:bookmarkEnd w:id=\"2\"/>");
		assertEquals("P2", keys.get("whole"));
		assertEquals("P3", keys.get("cell"));
		assertFalse("nothing after it to belong to", keys.containsKey("trailing"));
	}
}
