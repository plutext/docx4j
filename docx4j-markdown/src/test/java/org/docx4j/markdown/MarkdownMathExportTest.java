package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.markdown.math.LatexToOmml;
import org.docx4j.markdown.math.OmmlToLatex;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-006-markdown-math phase d: OMML → LaTeX (the reverse translator), and the
 * exporter's flatten-with-warning fallback for equations outside the subset.
 */
public class MarkdownMathExportTest {

	/** Normalizing round trip at the translator level. */
	private static String latexTrip(String latex) throws Exception {
		return new OmmlToLatex().convertOMath(new LatexToOmml().convertInline(latex));
	}

	@Test
	public void translatorRoundTripsNormalizedForms() throws Exception {
		String[] fixedPoints = {
			"x+y=2",
			"P=\\frac{1}{2}\\rho AU^{3}",
			"U_{i}^{3}",
			"\\sqrt{2}",
			"\\sqrt[3]{x+y}",
			"\\sum_{i}A_{i}",
			"\\sum_{i}A_{i}U_{i}^{3}", // A_i binds into the operator's base, U_i^3 flows after
			"\\sum_{i}P(U_{i})",
			"\\int_{0}^{1}x",
			"(x+y)",
			"P(U_{i})",
			"\\left[x+y\\right]",
			"\\text{site wind}",
			"U_{\\mathrm{REWS}}",
			"\\sin x",
			"\\hat{x}",
			"\\overline{AB}",
			"\\boxed{\\text{a}\\to\\text{b}}",
			"\\begin{aligned}x&=1 \\\\ y&=2\\end{aligned}",
			"\\begin{matrix}a&b \\\\ c&d\\end{matrix}",
			"\\begin{pmatrix}1&2 \\\\ 3&4\\end{pmatrix}",
			"\\begin{bmatrix}x \\\\ y\\end{bmatrix}",
			"\\begin{pmatrix}\\frac{1}{2}&0 \\\\ 0&U^{3}\\end{pmatrix}",
			"\\underbrace{a+b}",
			"\\underbrace{a+b}_{n}",
			"\\overbrace{x+y}^{n}",
			"\\underset{n}{X}",
			"\\underset{n\\to\\infty}{\\max}",
			"(\\frac{\\sum_{i}A_{i}U_{i}^{3}}{\\sum_{i}A_{i}})^{1/3}",
		};
		for (String latex : fixedPoints) {
			assertEquals(latex, latexTrip(latex));
			// and stable (idempotent) too
			assertEquals(latex, latexTrip(latexTrip(latex)));
		}
	}

	@Test
	public void nonCanonicalInputNormalizes() throws Exception {
		assertEquals("P=\\frac{1}{2}\\rho AU^{3}",
				latexTrip("P = \\frac12 \\rho A U^3"));
		assertEquals("x\\le y", latexTrip("x \\leq y"));
		assertEquals("a\\to b", latexTrip("a \\rightarrow b"));
		// plain \left( \right) normalizes to bare parens (both are m:d)
		assertEquals("(x+y)", latexTrip("\\left(x+y\\right)"));
		// parens around a bare matrix normalize to pmatrix (both are m:d + m:m)
		assertEquals("\\begin{pmatrix}1&2 \\\\ 3&4\\end{pmatrix}",
				latexTrip("(\\begin{matrix}1&2 \\\\ 3&4\\end{matrix})"));
		assertEquals("\\begin{pmatrix}1&2 \\\\ 3&4\\end{pmatrix}",
				latexTrip("\\left(\\begin{matrix}1&2 \\\\ 3&4\\end{matrix}\\right)"));
	}

	@Test
	public void docxRoundTripViaExporter() throws Exception {
		String md = "Power $P=\\frac{1}{2}\\rho AU^{3}$ and:\n\n"
				+ "$$\n\\begin{aligned}P_{\\mathrm{gross}}&=P(U), \\\\ "
				+ "P_{\\mathrm{net}}&=P_{\\mathrm{gross}}(1-L).\\end{aligned}\n$$\n";
		WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(md);
		assertEquals(md, new MarkdownExporter().export(pkg));
	}

	@Test
	public void unsupportedOmmlFlattensWithWarning() throws Exception {
		// m:func is not in the subset: build one directly
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String pXml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\">"
				+ "<m:oMath>"
				+ "<m:func><m:fName><m:r><m:t>cosech</m:t></m:r></m:fName>"
				+ "<m:e><m:r><m:t>x</m:t></m:r></m:e></m:func>"
				+ "</m:oMath>"
				+ "</w:p>";
		pkg.getMainDocumentPart().getContent().add(
				(P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));

		String md = new MarkdownExporter().export(pkg);
		// flattened to its text, not emitted as math
		assertTrue(md.contains("cosech"));
		assertTrue(md.contains("x"));
		assertFalse(md.contains("$"));
	}

	@Test
	public void trackedChangesAcceptedRevisionsView() throws Exception {
		// CR-010 / issue 348: w:ins content is included, w:del excluded
		String xml = "<m:oMath xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
				+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<m:r><m:t>a+</m:t></m:r>"
				+ "<m:r><w:ins w:id=\"1\" w:author=\"a\" w:date=\"2026-09-01T12:43:00Z\">"
				+ "<w:rPr><w:rFonts w:ascii=\"Cambria Math\"/></w:rPr><m:t>b</m:t></w:ins></m:r>"
				+ "<m:r><w:del w:id=\"2\" w:author=\"a\" w:date=\"2026-09-01T12:43:00Z\">"
				+ "<w:rPr><w:rFonts w:ascii=\"Cambria Math\"/></w:rPr><m:t>c</m:t></w:del></m:r>"
				+ "</m:oMath>";
		org.docx4j.math.CTOMath oMath = (org.docx4j.math.CTOMath) XmlUtils.unwrap(
				XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.math.CTOMath.class));
		assertEquals("a+b", new OmmlToLatex().convertOMath(oMath));
	}

	@Test
	public void mathInHeadingExports() throws Exception {
		WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(
				"# The $U^{3}$ law\n");
		assertEquals("# The $U^{3}$ law\n", new MarkdownExporter().export(pkg));
	}

}
