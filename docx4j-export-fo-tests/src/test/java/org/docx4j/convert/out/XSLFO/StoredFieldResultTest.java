package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * {@code docx4j.convert.out.fields.dateCachedResult=true} paints a {@code DATE} or
 * {@code TIME} as the result the document stores rather than from the clock; the fields
 * Word evaluates whatever its update setting - {@code PAGE} and {@code PAGEREF} here -
 * are evaluated either way.  Both FO pathways.
 *
 * <p>Measured against Word 365 with the field update off: the re-saved files carry the
 * day's date in every DATE and TIME, body and footer, so what a file stores is the date
 * Word last touched it and what Word's PDF paints is the date of the conversion; a
 * comparison against a PDF cut on another day differs on every such line for no reason
 * of layout.  And a table of contents whose 17 entries all store "3" prints 3, 4, 4,
 * 4, 5, 5, ... with the update off, which is why PAGEREF is not covered: painting its
 * stored result cost a TOC probe 0.98 -> 0.93 of line parity against Word.</p>
 *
 * @since 17.1.1
 */
public class StoredFieldResultTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static final String PAGEREF =
			"<w:p><w:fldSimple w:instr=\" PAGEREF _Toc123 \\h \">"
			+ "<w:r><w:t>7</w:t></w:r></w:fldSimple></w:p>";
	private static final String HEADING =
			"<w:p><w:bookmarkStart w:id=\"1\" w:name=\"_Toc123\"/>"
			+ "<w:r><w:t>Heading</w:t></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>";
	private static final String DATE =
			"<w:p><w:fldSimple w:instr=\" DATE \\@ &quot;d MMMM yyyy&quot; \">"
			+ "<w:r><w:t>31 December 1999</w:t></w:r></w:fldSimple></w:p>";
	private static final String TIME =
			"<w:p><w:fldSimple w:instr=\" TIME \\@ &quot;HH:mm&quot; \">"
			+ "<w:r><w:t>23:59</w:t></w:r></w:fldSimple></w:p>";
	private static final String DATE_NO_RESULT =
			"<w:p><w:fldSimple w:instr=\" DATE \\@ &quot;yyyy&quot; \"/></w:p>";
	private static final String PAGE =
			"<w:p><w:fldSimple w:instr=\" PAGE \"><w:r><w:t>42</w:t></w:r></w:fldSimple></w:p>";

	private static String fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private static String cached(String body, int flags) throws Exception {
		String was = Docx4jProperties.getProperty(AbstractFldSimpleWriter.PROPERTY_DATE_CACHED_RESULT);
		try {
			Docx4jProperties.setProperty(AbstractFldSimpleWriter.PROPERTY_DATE_CACHED_RESULT, "true");
			return fo(body, flags);
		} finally {
			Docx4jProperties.setProperty(AbstractFldSimpleWriter.PROPERTY_DATE_CACHED_RESULT,
					was == null ? "false" : was);
		}
	}

	/** The default: a DATE and a TIME are the clock's. */
	@Test
	public void byDefaultTheDateIsEvaluated() throws Exception {
		for (int flag : FLAGS) {
			String fo = fo(DATE + TIME, flag);
			assertFalse("the stored date was painted: " + fo, fo.contains("31 December 1999"));
			assertFalse("the stored time was painted: " + fo, fo.contains("23:59"));
		}
	}

	@Test
	public void withThePropertyOnTheStoredDateIsPainted() throws Exception {
		for (int flag : FLAGS) {
			String fo = cached(DATE + TIME, flag);
			assertTrue("the stored date is gone: " + fo, fo.contains("31 December 1999"));
			assertTrue("the stored time is gone: " + fo, fo.contains("23:59"));
		}
	}

	/** Word paints a PAGE and a PAGEREF from its pagination whether or not it updates fields, and so do we. */
	@Test
	public void pageAndPagerefAreEvaluatedEitherWay() throws Exception {
		for (int flag : FLAGS) {
			for (String fo : new String[] { fo(PAGE + PAGEREF + HEADING, flag), cached(PAGE + PAGEREF + HEADING, flag) }) {
				assertTrue("no page-number: " + fo, fo.contains("<page-number "));
				assertFalse("the stored page number was painted: " + fo, fo.contains(">42<"));
				assertTrue("no page-number-citation: " + fo, fo.contains("page-number-citation"));
				assertFalse("the stored PAGEREF result was painted: " + fo, fo.contains(">7<"));
			}
		}
	}

	/** A field with nothing stored is still evaluated, as a DOCPROPERTY is. */
	@Test
	public void aFieldWithNoStoredResultIsStillEvaluated() throws Exception {
		String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		for (int flag : FLAGS) {
			String fo = cached(DATE_NO_RESULT, flag);
			assertTrue("the year was not painted: " + fo, fo.contains(">" + year + "<"));
		}
	}
}
