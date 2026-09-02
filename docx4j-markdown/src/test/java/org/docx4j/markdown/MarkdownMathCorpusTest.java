package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.markdown.math.LatexToOmml;
import org.docx4j.markdown.math.OmmlToLatex;
import org.docx4j.math.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * Subset growth round 1 (CR-006-markdown-math): the constructs a real math-laden
 * course import surfaced through the issue listener — cases, xrightarrow,
 * mathcal/mathbb, \not, sizing prefixes, \middle, overset/stackrel, widehat,
 * textbf, and backslash-space before a line break.
 */
public class MarkdownMathCorpusTest {

	private final ObjectFactory mathFactory = new ObjectFactory();

	private String omml(String latex) throws Exception {
		return XmlUtils.marshaltoString(
				mathFactory.createOMath(new LatexToOmml().convertInline(latex)),
				true, Context.jc);
	}

	private static String latexTrip(String latex) throws Exception {
		return new OmmlToLatex().convertOMath(new LatexToOmml().convertInline(latex));
	}

	// ------------------------------------------------------------- mappings

	@Test
	public void casesBecomesBracedEqArr() throws Exception {
		String xml = omml("\\begin{cases}P_r,&U_r\\le U<U_{co},\\\\0,&U\\ge U_{co}.\\end{cases}");
		assertTrue(xml.contains("<m:d>"));
		assertTrue(xml.contains("m:val=\"{\""));
		assertTrue(xml.contains("<m:eqArr>"));
	}

	@Test
	public void xrightarrowBecomesLimUpp() throws Exception {
		String xml = omml("U_\\mathrm{model}\\xrightarrow{g_1}U_\\mathrm{site}");
		assertTrue(xml.contains("<m:limUpp>"));
		assertTrue(xml.contains("⟶"));
		assertTrue(xml.contains(">model<"));
	}

	@Test
	public void mathcalBecomesScriptRun() throws Exception {
		String xml = omml("\\mathcal I_t");
		assertTrue(xml.contains("m:val=\"script\""));
		assertTrue(xml.contains(">I<"));
		assertTrue(omml("\\mathbb R").contains("m:val=\"double-struck\""));
	}

	@Test
	public void notCombinesSolidus() throws Exception {
		String xml = omml("a\\not\\Rightarrow b");
		assertTrue(xml.contains("⇏"));
	}

	@Test
	public void sizingPrefixesKeepTheDelimiter() throws Exception {
		String xml = omml("\\frac{dP}{dU}\\bigg|_{U_t}");
		assertTrue(xml.contains("<m:sSub>"));
		assertTrue(xml.contains(">|<"));
		assertTrue(!xml.contains("bigg"));
	}

	@Test
	public void middleBecomesSepChr() throws Exception {
		org.docx4j.math.CTOMath oMath = new LatexToOmml()
				.convertInline("\\left[\\frac{P}{P_r}\\middle|X\\right]");
		org.docx4j.math.CTD d = (org.docx4j.math.CTD) XmlUtils.unwrap(
				oMath.getEGOMathElements().get(0));
		assertEquals("|", d.getDPr().getSepChr().getVal());
		assertEquals(2, d.getE().size());
	}

	@Test
	public void oversetAndStackrel() throws Exception {
		String xml = omml("a_\\mathrm{obs}\\stackrel{?}{\\approx}a_\\mathrm{GWA}");
		assertTrue(xml.contains("<m:limUpp>"));
		assertTrue(xml.contains(">?<"));
		assertTrue(xml.contains("≈"));
	}

	@Test
	public void widehatAndTextbf() throws Exception {
		assertTrue(omml("\\widehat{\\operatorname{Var}}(\\bar d)").contains("<m:acc>"));
		String bold = omml("\\textbf{Predict before measuring.}");
		assertTrue(bold.contains("<m:nor"));
		assertTrue(bold.contains("<w:b/>"));
	}

	@Test
	public void backslashSpaceBeforeNewline() throws Exception {
		// "\ " where the escaped character is a NEWLINE (the corpus wraps there)
		assertTrue(omml("U_{\\min}^C\n\\ \\text{or}\\ \nU_{\\max}^C").contains(">or<"));
	}

	// ------------------------------------------------- \mathrm is math mode

	@Test
	public void mathrmParsesItsArgument() throws Exception {
		// \mathrm{m\,s^{-1}} used to become ONE literal-text run
		// "m\,s^{-1}" (readRawGroup); it is math mode — parse it, upright
		String xml = omml("\\mathrm{m\\,s^{-1}}.");
		assertTrue(xml.contains("<m:sSup>"));
		assertTrue(xml.contains(">-1<"));
		assertTrue(xml.contains("m:val=\"p\""));
		assertTrue(!xml.contains("s^{-1}"));
		// and the same via {\rm ...}
		String rm = omml("{\\rm m\\,s^{-1}}.");
		assertTrue(rm.contains("<m:sSup>"));
	}

	// ------------------------------------------------- single-line \[ .. \]

	@Test
	public void singleLineBracketDisplayIsMath() throws Exception {
		// \[{\rm m\,s^{-1}}.\] on ONE line has no inline fallback ($$ does),
		// so the block parser must claim it; it used to degrade silently to
		// escaped-bracket text
		List<MarkdownImportIssue> issues = new ArrayList<>();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart("\\[{\\rm m\\,s^{-1}}.\\]\n", pkg);
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue(xml.contains("<m:oMathPara>"));
		assertTrue(xml.contains("<m:sSup>")); // the ^{-1} exponent
		assertTrue(xml.contains(">-1<"));
		assertTrue(issues.isEmpty());
	}

	@Test
	public void closerMidLineDoesNotSwallowFollowingLines() throws Exception {
		// "$$x^2$$ and more" must not open a block that eats the next lines
		List<MarkdownImportIssue> issues = new ArrayList<>();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart("$$x^2$$ and more\n\nnext paragraph\n", pkg);
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue(xml.contains("<m:oMath")); // the inline $$ parser took it
		assertTrue(xml.contains("and more"));
		assertTrue(xml.contains("next paragraph"));
	}

	// ------------------------------------------------------- pandoc parity

	@Test
	public void naryBindsItsOperand() throws Exception {
		// the following atom (with its scripts) goes INSIDE the operator's
		// m:e — Word then treats operator+operand as a unit (as pandoc does);
		// further content flows after
		String xml = omml("\\sum_i A_i U_i^3");
		assertTrue(xml.contains("<m:e><m:sSub>"));
		assertTrue(xml.indexOf("<m:sSubSup>") > xml.indexOf("</m:nary>"));

		// but an operator/relation after the sum is not swallowed
		String bare = omml("\\sum_i = 1");
		assertTrue(bare.contains("<m:e/>"));
	}

	@Test
	public void bareParensBecomeRealDelimiters() throws Exception {
		String xml = omml("P(U_i)");
		assertTrue(xml.contains("<m:d>"));
		assertTrue(!xml.contains(">(<"));
		// and scripts bind to the whole group
		assertTrue(omml("(x+y)^2").contains("<m:sSup><m:sSupPr>")
				|| omml("(x+y)^2").contains("<m:sSup>"));
	}

	@Test
	public void unbalancedParenStaysLiteral() throws Exception {
		String xml = omml("f(x");
		assertTrue(!xml.contains("<m:d>"));
		assertTrue(xml.contains("f(x"));
		// a bare closing paren too
		assertTrue(omml("[0,1)").contains("[0,1)"));
	}

	// ------------------------------------------------------------- round trips

	@Test
	public void newConstructsRoundTrip() throws Exception {
		String[] fixedPoints = {
			"\\begin{cases}0,&U<U_{ci} \\\\ P_{r},&U\\ge U_{co}\\end{cases}",
			"U_{\\mathrm{model}}\\xrightarrow{g}U_{\\mathrm{site}}",
			"{\\mathcal{I}}_{t}",
			"\\mathbb{R}",
			"a\\not\\Rightarrow b",
			"\\left[\\frac{P}{P_{r}}\\middle|X\\right]",
			"\\overset{?}{\\approx}",
			"\\textbf{Predict before measuring.}",
			"\\frac{dP}{dU}|_{U_{t}}",
			"a\\iff b", // \Longleftrightarrow normalizes to the shortest command
		};
		for (String latex : fixedPoints) {
			assertEquals(latex, latexTrip(latex));
			assertEquals(latex, latexTrip(latexTrip(latex)));
		}
	}

	// ------------------------------------------------------------- the corpus

	/** Verbatim equations from the failing course import. */
	private static final String[] COURSE_EQUATIONS = {
		"U_\\mathrm{model}\n\\xrightarrow{g_1}\nU_\\mathrm{site}\n\\xrightarrow{g_2}\n"
			+ "U_\\mathrm{rotor}\n\\xrightarrow{f}\nP_\\mathrm{gross}\n\\xrightarrow{h}\nP_\\mathrm{observed}.",
		"P(U)\n=\n\\begin{cases}\nP_r,&U_r\\le U<U_{co},\\\\\n0,&U\\ge U_{co}.\n\\end{cases}",
		"P(U)=\n\\begin{cases}\n0,\n& U<U_{ci},\\\\\nP_r\n\\left(\n\\frac{U}{U_r}\n\\right)^3,\n"
			+ "& U_{ci}\\le U<U_r,\\\\\nP_r,\n& U_r\\le U<U_{co},\\\\\n0,\n& U\\ge U_{co}.\n\\end{cases}",
		"U(z)\n=\n\\begin{cases}\nU_h(z/z_h)^{\\alpha_1},\n& z<z_h,\\\\\nU_h(z/z_h)^{\\alpha_2},\n& z\\ge z_h.\n\\end{cases}",
		"\\Delta AEP\n\\approx\n\\sum_t\n\\frac{dP}{dU}\\bigg|_{U_t}\n\\Delta U_t\n\\Delta t.",
		"A_t\n=\n\\begin{cases}\n1,&\\text{available},\\\\\n0,&\\text{unavailable}.\n\\end{cases}",
		"e_{U,E}(t)\n=\ne_U(t)\n\\frac{dP}{dU}\\bigg|_{U_t}.",
		"\\text{correct AEP}\n\\not\\Rightarrow\n\\text{correct model}.",
		"a_\\mathrm{obs}\n\\stackrel{?}{\\approx}\na_\\mathrm{GWA}.",
		"\\hat P(t+h\\mid\\mathcal I_t)",
		"f(P_{t+h}\\mid\\mathcal I_t).",
		"Y_t=\n\\begin{cases}\n1,&|\\Delta P_t|>100\\ \\mathrm{MW},\\\\\n0,&\\text{otherwise}.\n\\end{cases}",
		"DM\n=\n\\frac{\n\\bar d\n}{\n\\sqrt{\\widehat{\\operatorname{Var}}(\\bar d)}\n}.",
		"R(p)\n=\nE\n\\left[\n\\frac{\nP_\\mathrm{actual}\n}{\nP_\\mathrm{potential}\n}\n\\middle|\nRRP=p\n\\right].",
		"E_\\mathrm{econ}\n=\n\\sum_{t\\in\\mathcal E}\n(\nP_\\mathrm{potential,t}\n-\nP_\\mathrm{actual,t}\n)\n\\Delta t.",
		"\\mathcal E,\\mathcal N",
		"SemiDispatchCap\n\\not\\Rightarrow\nNetworkCurtailment.",
		"\\text{high resource}\n\\Longleftrightarrow\n\\text{potentially large downscaling need}.",
		"CF(X)\n=\nE\n\\left[\n\\frac{P}{P_r}\n\\middle|\nX\n\\right].",
		"El\\ Niño\n\\not\\Rightarrow\n\\text{one universal wind anomaly}.",
		"\\mathbf X_t\n\\xrightarrow{g}\nU_{\\mathrm{rotor},t}\n\\xrightarrow{f}\nP_{\\mathrm{turbine},t}.",
		"\\textbf{Model the causal chain separately, predict before measuring, "
			+ "and let independent evidence decide which corrections survive.}",
		"q_\\mathrm{extra}\n=\nP[\nU_R^{LT}\n<\nU_{qmin}^C\n\\ \\text{or}\\ \nU_R^{LT}\n>\nU_{qmax}^C\n].",
		"L_\\tau(y,q_\\tau)\n=\n\\begin{cases}\n\\tau(y-q_\\tau),\n& y\\ge q_\\tau,\\\\\n"
			+ "(1-\\tau)(q_\\tau-y),\n& y<q_\\tau.\n\\end{cases}",
		"D_i(t)\n=\n\\begin{cases}\n1,&\\text{REZ }i\\text{ in drought},\\\\\n0,&\\text{otherwise}.\n\\end{cases}",
		"\\mathrm{m\\,s^{-1}}.",
		"{\\rm m\\,s^{-1}}.",
	};

	@Test
	public void courseCorpusConvertsWithoutIssues() throws Exception {
		List<MarkdownImportIssue> issues = new ArrayList<>();
		StringBuilder md = new StringBuilder();
		for (String equation : COURSE_EQUATIONS) {
			md.append("$$\n").append(equation).append("\n$$\n\n");
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart(md.toString(), pkg);

		assertEquals("unconverted: " + issues, 0, issues.size());
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertEquals(COURSE_EQUATIONS.length, count(xml, "<m:oMathPara>"));

		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		pkg.save(baos);
		assertTrue(baos.size() > 0);
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
