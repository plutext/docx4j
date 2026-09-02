package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.markdown.math.LatexMathException;
import org.docx4j.markdown.math.LatexToOmml;
import org.docx4j.math.CTOMath;
import org.docx4j.math.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-markdown-math phase b: the restricted LaTeX subset → OMML, per
 * construct; placement (m:oMath inline, m:oMathPara display); loud lossless
 * fallback for anything outside the subset.
 */
public class MarkdownMathOmmlTest {

	private final ObjectFactory mathFactory = new ObjectFactory();

	private String omml(String latex) throws Exception {
		CTOMath oMath = new LatexToOmml().convertInline(latex);
		return XmlUtils.marshaltoString(mathFactory.createOMath(oMath), true, Context.jc);
	}

	// ------------------------------------------------------------ constructs

	@Test
	public void runsMerge() throws Exception {
		String xml = omml("x+y=2");
		assertTrue(xml.contains(">x+y=2<"));
		// one merged run, not five
		assertEquals(1, count(xml, "<m:r>"));
	}

	@Test
	public void fraction() throws Exception {
		String xml = omml("\\frac{a}{b}");
		assertTrue(xml.contains("<m:f>"));
		assertTrue(xml.indexOf("<m:num>") < xml.indexOf(">a<"));
		assertTrue(xml.indexOf("<m:den>") < xml.indexOf(">b<"));
	}

	@Test
	public void fracWithSingleTokenArgs() throws Exception {
		String xml = omml("\\frac12\\rho A U^3");
		assertTrue(xml.contains("<m:f>"));
		assertTrue(xml.contains(">1<"));
		assertTrue(xml.contains(">2<"));
		assertTrue(xml.contains("ρ"));
		assertTrue(xml.contains("<m:sSup>"));
	}

	@Test
	public void scripts() throws Exception {
		assertTrue(omml("x^2").contains("<m:sSup>"));
		assertTrue(omml("A_i").contains("<m:sSub>"));
		String both = omml("U_i^3");
		assertTrue(both.contains("<m:sSubSup>"));
	}

	@Test
	public void sqrt() throws Exception {
		String xml = omml("\\sqrt{x}");
		assertTrue(xml.contains("<m:rad>"));
		assertTrue(xml.contains("<m:degHide"));
		String cube = omml("\\sqrt[3]{x}");
		assertTrue(cube.contains("<m:deg>"));
		assertTrue(cube.contains(">3<"));
	}

	@Test
	public void naryWithLimits() throws Exception {
		String xml = omml("\\sum_i A_i");
		assertTrue(xml.contains("<m:nary>"));
		assertTrue(xml.contains("m:val=\"∑\""));
		assertTrue(xml.contains("m:val=\"undOvr\""));
		assertTrue(xml.contains("<m:supHide")); // no upper limit given
		// the operand flows after the operator (empty nary base, like texmath)
		assertTrue(xml.contains("<m:sSub>"));

		String integral = omml("\\int_0^1 x");
		assertTrue(integral.contains("m:val=\"∫\""));
		assertTrue(integral.contains("m:val=\"subSup\""));
	}

	@Test
	public void leftRightDelimiters() throws Exception {
		String xml = omml("\\left( \\frac{a}{b} \\right)");
		assertTrue(xml.contains("<m:d>"));
		assertTrue(xml.contains("<m:f>"));
		// invisible delimiter
		String invisible = omml("\\left. x \\right|");
		assertTrue(invisible.contains("m:val=\"\""));
	}

	@Test
	public void textAndMathrm() throws Exception {
		// \text -> normal (prose) text, spaces kept
		String text = omml("\\text{site wind}");
		assertTrue(text.contains("<m:nor"));
		assertTrue(text.contains(">site wind<"));
		// \mathrm and {\rm ..} -> upright math style
		String rm = omml("U_{\\rm REWS}");
		assertTrue(rm.contains("m:val=\"p\""));
		assertTrue(rm.contains(">REWS<"));
		String mathrm = omml("\\mathrm{REWS}");
		assertTrue(mathrm.contains("m:val=\"p\""));
	}

	@Test
	public void symbolsMergeIntoOneRun() throws Exception {
		String xml = omml("\\rho\\rightarrow\\infty");
		assertTrue(xml.contains(">ρ→∞<"));
	}

	@Test
	public void functionNamesAreUpright() throws Exception {
		String xml = omml("\\sin x");
		assertTrue(xml.contains(">sin<"));
		assertTrue(xml.contains("m:val=\"p\""));
	}

	@Test
	public void escapesAndSpacing() throws Exception {
		assertTrue(omml("\\{a\\}").contains(">{a}<"));
		// \, is a THIN SPACE (U+2009), \quad an EM SPACE (U+2003)
		assertTrue(omml("a\\,b").contains(">a b<"));
		assertTrue(omml("a\\quad b").contains(">a b<"));
	}

	@Test
	public void matrix() throws Exception {
		String xml = omml("\\begin{matrix}a&b \\\\ c&d\\end{matrix}");
		assertTrue(xml.contains("<m:m>"));
		assertEquals(2, count(xml, "<m:mr>"));
		assertEquals(4, count(xml, "<m:e>"));
		// pmatrix/bmatrix add the surrounding delimiter
		String p = omml("\\begin{pmatrix}1&2 \\\\ 3&4\\end{pmatrix}");
		assertTrue(p.contains("<m:d>"));
		assertTrue(p.contains("m:val=\"(\""));
		String b = omml("\\begin{bmatrix}x \\\\ y\\end{bmatrix}");
		assertTrue(b.contains("m:val=\"[\""));
		assertEquals(2, count(b, "<m:mr>"));
	}

	@Test
	public void underAndOverbrace() throws Exception {
		String under = omml("\\underbrace{a+b}_{n}");
		assertTrue(under.contains("<m:groupChr>"));
		assertTrue(under.contains("m:val=\"⏟\""));
		assertTrue(under.contains("m:val=\"bot\""));
		assertTrue(under.contains("<m:sSub>")); // the label is the brace's subscript
		String over = omml("\\overbrace{x+y}");
		assertTrue(over.contains("m:val=\"⏞\""));
		assertTrue(over.contains("m:val=\"top\""));
	}

	@Test
	public void underset() throws Exception {
		String xml = omml("\\underset{0}{x}");
		assertTrue(xml.contains("<m:limLow>"));
		// m:e (the base) precedes m:lim (the content below)
		assertTrue(xml.indexOf(">x<") < xml.indexOf("<m:lim>"));
		assertTrue(xml.contains(">0<"));
	}

	@Test
	public void rewsGoldenEquation() throws Exception {
		// the diagnostic equation from the motivating course material
		String xml = omml("U_{\\rm REWS}\n=\n\\left(\n\\frac{\\sum_i A_i U_i^3}\n"
				+ "     {\\sum_i A_i}\n\\right)^{1/3}");
		assertTrue(xml.contains("<m:d>"));
		assertTrue(xml.contains("<m:f>"));
		assertTrue(xml.contains("<m:nary>"));
		assertTrue(xml.contains("<m:sSup>")); // the ^{1/3} on the closing paren
		assertTrue(xml.contains(">REWS<"));
	}

	@Test
	public void unsupportedCommandFailsWhole() throws Exception {
		try {
			omml("\\xcancel{x}"); // cancel package: outside the subset
			fail("expected LatexMathException");
		} catch (LatexMathException e) {
			assertTrue(e.getMessage().contains("\\xcancel"));
		}
		try {
			omml("\\frac{a}"); // malformed
			fail("expected LatexMathException");
		} catch (LatexMathException e) {
			// expected
		}
	}

	// ------------------------------------------------------------ placement

	private final List<MarkdownImportIssue> issues = new ArrayList<>();

	private WordprocessingMLPackage convert(String md) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart(md, pkg);
		return pkg;
	}

	private static Object unwrapped(P p, int i) {
		return XmlUtils.unwrap(p.getContent().get(i));
	}

	@Test
	public void inlineMathBecomesOMath() throws Exception {
		WordprocessingMLPackage pkg = convert("Power $P=\\frac12\\rho A U^3$ here\n");
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		assertTrue(unwrapped(p, 1) instanceof CTOMath);
		assertTrue(issues.isEmpty());
	}

	@Test
	public void displayMathBecomesOMathPara() throws Exception {
		WordprocessingMLPackage pkg = convert("$$\nP_{\\rm wind} = \\frac12 \\rho A U^3\n$$\n");
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		assertTrue(unwrapped(p, 0) instanceof org.docx4j.math.CTOMathPara);
		assertTrue(issues.isEmpty());
	}

	@Test
	public void singleLineDisplayAlsoBecomesOMathPara() throws Exception {
		WordprocessingMLPackage pkg = convert("$$E=mc^2$$\n");
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		assertTrue(unwrapped(p, 0) instanceof org.docx4j.math.CTOMathPara);
	}

	@Test
	public void mathInsideTableCellAndListItem() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"| eq |\n|----|\n| $x^2$ |\n\n- item with $\\rho$\n");
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertEquals(2, count(xml, "<m:oMath>"));
		assertTrue(issues.isEmpty());
	}

	@Test
	public void failedEquationFallsBackLoudly() throws Exception {
		WordprocessingMLPackage pkg = convert("ok $x^2$ bad $\\unknowncmd{y}$ end\n");
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		// the good equation converted...
		assertTrue(unwrapped(p, 1) instanceof CTOMath);
		// ...the bad one fell back to its literal source, delimiters intact
		StringBuilder sb = new StringBuilder();
		org.docx4j.TraversalUtil.visit(p, new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object child) {
				child = XmlUtils.unwrap(child);
				if (child instanceof org.docx4j.wml.Text) {
					sb.append(((org.docx4j.wml.Text) child).getValue());
				}
				return null;
			}
		});
		assertTrue(sb.toString().contains("$\\unknowncmd{y}$"));
		assertEquals(1, issues.size());
		assertEquals("inline math", issues.get(0).getConstruct());
		assertTrue(issues.get(0).getReason().contains("\\unknowncmd"));
	}

	@Test
	public void mathDocumentSaves() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"# Wind\n\nInline $P=\\frac12\\rho A U^3$.\n\n"
				+ "$$\nU_{\\rm REWS}=\\left(\\frac{\\sum_i A_i U_i^3}{\\sum_i A_i}\\right)^{1/3}\n$$\n");
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		pkg.save(baos);
		assertTrue(baos.size() > 0);
		assertTrue(issues.isEmpty());
	}

	@Test
	public void literalPolicySkipsTranslation() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions()
				.setMathPolicy(MarkdownImportOptions.MathPolicy.LITERAL)
				.setIssueListener(issues::add))
				.importToMainDocumentPart("$x^2$\n", pkg);
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue(!xml.contains("<m:oMath>"));
		assertTrue(issues.isEmpty());
	}

	private static int count(String haystack, String needle) {
		int count = 0;
		int from = 0;
		while ((from = haystack.indexOf(needle, from)) >= 0) {
			count++;
			from += needle.length();
		}
		return count;
	}

}
