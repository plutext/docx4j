package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A DOCPROPERTY field naming a property the document doesn't have used to abort the
 * whole export with FieldValueException "No value found for DOCPROPERTY ...".  Word
 * displays the field's cached result in that case, and so do we now - for w:fldSimple
 * and for the complex field (w:fldChar) form, which FieldsCombiner turns into one.
 *
 * @since 17.0.5
 */
public class DocPropertyMissingTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** a w:fldSimple DOCPROPERTY with a cached result */
	private static final String SIMPLE =
			"<w:p><w:fldSimple w:instr=\" DOCPROPERTY  NoSuchProperty  \\* MERGEFORMAT \">"
			+ "<w:r><w:t>cached simple result</w:t></w:r></w:fldSimple></w:p>";

	/** the same field as a complex field, with its result runs */
	private static final String COMPLEX =
			"<w:p>"
			+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
			+ "<w:r><w:instrText xml:space=\"preserve\"> DOCPROPERTY  AlsoMissing  \\* MERGEFORMAT </w:instrText></w:r>"
			+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
			+ "<w:r><w:t>cached complex result</w:t></w:r>"
			+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"
			+ "</w:p>";

	/** a DOCPROPERTY with no cached result at all: the field just disappears */
	private static final String NO_RESULT =
			"<w:p><w:fldSimple w:instr=\" DOCPROPERTY  Empty \"/></w:p>";

	private static String fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private void check(int flags) throws Exception {

		String fo = fo(SIMPLE, flags);
		assertTrue("the cached result should be used:\n" + fo, fo.contains("cached simple result"));
		assertTrue("the rest of the document should still be there", fo.contains("after"));
		assertFalse("the field instruction is not text", fo.contains("DOCPROPERTY"));

		fo = fo(COMPLEX, flags);
		assertTrue("the complex field's cached result should be used:\n" + fo,
				fo.contains("cached complex result"));
		assertTrue(fo.contains("after"));

		// no cached result: nothing to show, but the export must still complete
		fo = fo(NO_RESULT, flags);
		assertTrue(fo.contains("after"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
