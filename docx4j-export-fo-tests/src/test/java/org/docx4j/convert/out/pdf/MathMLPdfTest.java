package org.docx4j.convert.out.pdf;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * PDF (XSL-FO) math: equations are emitted as MathML in
 * fo:instream-foreign-object, which the jeuclid-fop plugin renders. See
 * CR-008-math-pdf-fo. jeuclid is a dependency of docx4j-export-fo, so it is on the
 * test classpath here.
 */
public class MathMLPdfTest {

	private WordprocessingMLPackage load() throws Exception {
		String path = System.getProperty("user.dir")
				+ "/src/test/resources/math/equation-sample.docx";
		return WordprocessingMLPackage.load(new File(path));
	}

	private String toFO(int flag) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(load());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flag);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private void assertForeignObjectMathML(int flag) throws Exception {
		String fo = toFO(flag);
		assertTrue("FO should wrap the equation in fo:instream-foreign-object:\n" + fo,
				fo.contains("instream-foreign-object"));
		assertTrue("the equation should be MathML (<math>) inside the foreign object",
				fo.contains("http://www.w3.org/1998/Math/MathML"));
		assertTrue("the fraction should be an <mfrac>", fo.contains("<mfrac>"));
	}

	@Test
	public void visitorPathwayEmitsMathMLForeignObject() throws Exception {
		assertForeignObjectMathML(Docx4J.FLAG_EXPORT_PREFER_NONXSL);
	}

	@Test
	public void xsltPathwayEmitsMathMLForeignObject() throws Exception {
		assertForeignObjectMathML(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void rendersToPdfWithTheEquation() throws Exception {
		// jeuclid-fop is on the classpath, so the equation renders as vector paths;
		// the PDF is meaningfully larger than the text-only baseline (~12 KB).
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toPDF(load(), os);
		assertTrue("a PDF should be produced", os.size() > 3000);
		assertTrue("the PDF should carry the rendered equation (not just text): " + os.size()
				+ " bytes", os.size() > 13000);
	}
}
