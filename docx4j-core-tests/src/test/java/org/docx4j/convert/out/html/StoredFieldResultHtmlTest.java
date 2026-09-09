package org.docx4j.convert.out.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * {@code docx4j.convert.out.fields.dateCachedResult=true} paints a {@code DATE} or
 * {@code TIME} as the result the document stores rather than from the clock, in HTML
 * as in FO (the gate is in the writer both share); a {@code PAGEREF} is evaluated
 * either way, as Word evaluates it either way.  Both HTML pathways.  The FO side, and
 * the Word measurement behind the property, are in docx4j-export-fo-tests'
 * {@code StoredFieldResultTest}.
 *
 * @since 17.1.1
 */
public class StoredFieldResultHtmlTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final int[] PATHWAYS = {
			Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static final String PAGEREF =
			"<w:p><w:fldSimple w:instr=\" PAGEREF _Toc123 \">"
			+ "<w:r><w:t>7</w:t></w:r></w:fldSimple></w:p>";
	private static final String HEADING =
			"<w:p><w:bookmarkStart w:id=\"1\" w:name=\"_Toc123\"/>"
			+ "<w:r><w:t>Heading</w:t></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>";
	private static final String DATE =
			"<w:p><w:fldSimple w:instr=\" DATE \\@ &quot;d MMMM yyyy&quot; \">"
			+ "<w:r><w:t>31 December 1999</w:t></w:r></w:fldSimple></w:p>";
	private static final String TIME_NO_RESULT =
			"<w:p><w:fldSimple w:instr=\" TIME \\@ &quot;yyyy&quot; \"/></w:p>";

	private static String html(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setWmlPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private static String cached(String body, int flags) throws Exception {
		String was = Docx4jProperties.getProperty(AbstractFldSimpleWriter.PROPERTY_DATE_CACHED_RESULT);
		try {
			Docx4jProperties.setProperty(AbstractFldSimpleWriter.PROPERTY_DATE_CACHED_RESULT, "true");
			return html(body, flags);
		} finally {
			Docx4jProperties.setProperty(AbstractFldSimpleWriter.PROPERTY_DATE_CACHED_RESULT,
					was == null ? "false" : was);
		}
	}

	/** The default: a DATE is the clock's. */
	@Test
	public void byDefaultTheDateIsEvaluated() throws Exception {
		for (int flags : PATHWAYS) {
			String html = html(DATE, flags);
			assertFalse("the stored date was painted: " + html, html.contains("31 December 1999"));
		}
	}

	@Test
	public void withThePropertyOnTheStoredDateIsPainted() throws Exception {
		for (int flags : PATHWAYS) {
			String html = cached(DATE, flags);
			assertTrue("the stored date is gone: " + html, html.contains("31 December 1999"));
		}
	}

	/** HTML has one page, so an evaluated PAGEREF is "1"; it is evaluated either way. */
	@Test
	public void aPagerefIsEvaluatedEitherWay() throws Exception {
		for (int flags : PATHWAYS) {
			for (String html : new String[] { html(PAGEREF + HEADING, flags), cached(PAGEREF + HEADING, flags) }) {
				assertFalse("the stored PAGEREF result was painted: " + html, html.contains(">7<"));
			}
		}
	}

	/** A field with nothing stored is still evaluated. */
	@Test
	public void aFieldWithNoStoredResultIsStillEvaluated() throws Exception {
		String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		for (int flags : PATHWAYS) {
			String html = cached(TIME_NO_RESULT, flags);
			assertTrue("the year was not painted: " + html, html.contains(">" + year + "<"));
		}
	}
}
