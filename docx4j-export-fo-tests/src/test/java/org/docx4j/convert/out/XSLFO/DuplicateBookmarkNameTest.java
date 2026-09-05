package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Word does not require a bookmark's name to be unique in a document - copy/paste and
 * merges routinely leave two of a name, and a table of contents built twice leaves a
 * whole set of them - but FOP refuses to render a document with a repeated id at all:
 * "Property ID ... previously used; ID values must be unique within a document".  The
 * export of a document in this state failed completely, so only the first bookmark of
 * a name is now given an id; a reference resolves to it, which is what Word does too.
 *
 * @since 17.0.6
 */
public class DuplicateBookmarkNameTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String NAME = "heading_anchor";

	private static String bookmarked(int id, String text) {
		return "<w:p><w:bookmarkStart w:id=\"" + id + "\" w:name=\"" + NAME + "\"/>"
				+ "<w:r><w:t>" + text + "</w:t></w:r>"
				+ "<w:bookmarkEnd w:id=\"" + id + "\"/></w:p>";
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ bookmarked(1, "first occurrence")
				+ "<w:p><w:r><w:t>between them</w:t></w:r></w:p>"
				+ bookmarked(2, "second occurrence of the same bookmark name")
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private void idIsEmittedOnce(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		int seen = 0;
		NodeList inlines = doc.getElementsByTagNameNS(FO, "inline");
		for (int i = 0; i < inlines.getLength(); i++) {
			if (NAME.equals(((Element)inlines.item(i)).getAttribute("id"))) seen++;
		}
		assertEquals("FOP refuses a document with a repeated id", 1, seen);
		// the text of both paragraphs is still there
		assertTrue(doc.getDocumentElement().getTextContent().contains("first occurrence"));
		assertTrue(doc.getDocumentElement().getTextContent().contains("second occurrence"));
	}

	@Test
	public void idIsEmittedOnceVisitor() throws Exception {
		idIsEmittedOnce(Docx4J.FLAG_NONE);
	}

	@Test
	public void idIsEmittedOnceXslt() throws Exception {
		idIsEmittedOnce(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** The document lays out: before the fix FOP threw ValidationException here. */
	@Test
	public void theDocumentLaysOut() throws Exception {
		assertTrue("nothing was laid out", lineCount(areaTree(pkg(), Docx4J.FLAG_NONE)) >= 3);
	}
}
