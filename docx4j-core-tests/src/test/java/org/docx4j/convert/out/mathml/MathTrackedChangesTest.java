package org.docx4j.convert.out.mathml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.math.CTOMath;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-010 / issue 348: tracked changes inside equations.  Word wraps math run
 * content in w:ins/w:del INSIDE m:r; before the schema fix, the wrapper and
 * everything in it (including the m:t) was dropped at unmarshal time —
 * silent data loss on a load/save round trip.
 */
public class MathTrackedChangesTest {

	private static final String NS =
			"xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" "
			+ "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	// the shapes Word actually writes (from the CR-010 sample docx):
	// the whole run content - m:rPr, w:rPr, m:t - moves inside the wrapper
	private static final String OMATH_XML =
			"<m:oMath " + NS + ">"
			+ "<m:r><m:t>kept</m:t></m:r>"
			+ "<m:r><w:ins w:id=\"1\" w:author=\"a\" w:date=\"2026-09-01T12:43:00Z\">"
			+   "<m:rPr><m:sty m:val=\"p\"/></m:rPr>"
			+   "<w:rPr><w:rFonts w:ascii=\"Cambria Math\"/></w:rPr>"
			+   "<m:t>inserted</m:t></w:ins></m:r>"
			+ "<m:r><w:del w:id=\"2\" w:author=\"a\" w:date=\"2026-09-01T12:43:00Z\">"
			+   "<w:rPr><w:rFonts w:ascii=\"Cambria Math\"/></w:rPr>"
			+   "<m:t>deleted</m:t></w:del></m:r>"
			+ "</m:oMath>";

	private CTOMath unmarshal() throws Exception {
		return (CTOMath) XmlUtils.unwrap(
				XmlUtils.unmarshalString(OMATH_XML, Context.jc, CTOMath.class));
	}

	/** The model holds the wrappers and their content: nothing is lost. */
	@Test
	public void modelRoundTrips() throws Exception {
		CTOMath oMath = unmarshal();
		String xml = XmlUtils.marshaltoString(
				new org.docx4j.math.ObjectFactory().createOMath(oMath), true, Context.jc);
		assertTrue("w:ins wrapper lost", xml.contains("<w:ins "));
		assertTrue("w:del wrapper lost", xml.contains("<w:del "));
		assertTrue("inserted text lost", xml.contains(">inserted<"));
		assertTrue("deleted text lost", xml.contains(">deleted<"));
		assertTrue("author attribute lost", xml.contains("w:author=\"a\""));
		assertTrue("m:rPr inside the wrapper lost", xml.contains("m:val=\"p\""));
	}

	/** MathML output is the accepted-revisions view: ins in, del out. */
	@Test
	public void mathMLAcceptedRevisionsView() throws Exception {
		String mathML = new OmmlToMathML().toMathMLString(unmarshal());
		// letters are tokenised into per-character mi elements: compare text
		String text = mathML.replaceAll("<[^>]*>", "").replaceAll("\\s", "");
		assertTrue(text, text.contains("kept"));
		assertTrue("w:ins content missing from MathML: " + text, text.contains("inserted"));
		assertFalse("w:del content rendered in MathML: " + text, text.contains("deleted"));
	}

	/** The Word-produced sample survives a full load -> save -> reload. */
	@Test
	public void sampleDocxRoundTrips() throws Exception {
		WordprocessingMLPackage pkg;
		try (InputStream is = ResourceUtils.getResource("tracked-changes-equations.docx")) {
			pkg = WordprocessingMLPackage.load(is);
		}
		String before = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue("tracked equation content missing after load",
				before.contains("<w:del ") && before.contains("<w:ins "));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		WordprocessingMLPackage reloaded = WordprocessingMLPackage.load(
				new ByteArrayInputStream(baos.toByteArray()));
		String after = XmlUtils.marshaltoString(
				reloaded.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue("w:ins in equation lost on round trip", after.contains("<w:ins "));
		assertTrue("w:del in equation lost on round trip", after.contains("<w:del "));
		// specific deleted content from the sample survives
		assertTrue("deleted m:t content lost on round trip", after.contains("s^{-1}"));
	}

}
