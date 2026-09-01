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
import org.docx4j.math.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * CR-markdown-math phase c: aligned → m:eqArr, boxed → m:borderBox,
 * accents → m:acc, overline/underline → m:bar.  (No backslashes in this
 * comment: {@code \}+u in a Java comment is a unicode escape.)
 */
public class MarkdownMathStructuresTest {

	private final ObjectFactory mathFactory = new ObjectFactory();

	private String omml(String latex) throws Exception {
		return XmlUtils.marshaltoString(
				mathFactory.createOMath(new LatexToOmml().convertInline(latex)),
				true, Context.jc);
	}

	/** The (single) m:eqArr produced by the given LaTeX. */
	private org.docx4j.math.CTEqArr eqArr(String latex) throws Exception {
		for (Object o : new LatexToOmml().convertInline(latex).getEGOMathElements()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.math.CTEqArr) {
				return (org.docx4j.math.CTEqArr) u;
			}
		}
		throw new AssertionError("no m:eqArr produced");
	}

	@Test
	public void alignedBecomesEqArr() throws Exception {
		String latex = "\\begin{aligned}\n"
				+ "P_{\\rm gross} &= P(U), \\\\\n"
				+ "P_{\\rm net} &= P_{\\rm gross}(1-L).\n"
				+ "\\end{aligned}";
		assertEquals(2, eqArr(latex).getE().size());
		String xml = omml(latex);
		assertTrue(xml.contains("<m:eqArr>"));
		// alignment marks survive as & (Word's linear-format convention)
		assertEquals(2, count(xml, "&amp;"));
		assertTrue(xml.contains(">gross<"));
		assertTrue(xml.contains(">net<"));
	}

	@Test
	public void alignStarAccepted() throws Exception {
		assertEquals(2, eqArr("\\begin{align*}x &= 1 \\\\ y &= 2\\end{align*}").getE().size());
	}

	@Test
	public void trailingRowSeparatorDoesNotAddEmptyRow() throws Exception {
		assertEquals(1, eqArr("\\begin{aligned}x &= 1 \\\\\n\\end{aligned}").getE().size());
	}

	@Test
	public void boxedBecomesBorderBox() throws Exception {
		String xml = omml("\\boxed{\\text{atmosphere}\\rightarrow\\text{site wind}}");
		assertTrue(xml.contains("<m:borderBox>"));
		assertTrue(xml.contains(">atmosphere<"));
		assertTrue(xml.contains("→"));
	}

	@Test
	public void accents() throws Exception {
		assertTrue(omml("\\hat{x}").contains("m:val=\"\u0302\""));
		assertTrue(omml("\\bar U").contains("m:val=\"\u0305\""));
		assertTrue(omml("\\vec{v}").contains("m:val=\"\u20D7\""));
		assertTrue(omml("\\dot q").contains("m:val=\"\u0307\""));
		assertTrue(omml("\\hat{x}").contains("<m:acc>"));
	}

	@Test
	public void overlineAndUnderline() throws Exception {
		String over = omml("\\overline{AB}");
		assertTrue(over.contains("<m:bar>"));
		assertTrue(over.contains("m:val=\"top\""));
		String under = omml("\\underline{x+y}");
		assertTrue(under.contains("m:val=\"bot\""));
	}

	@Test
	public void unsupportedEnvironmentStillFails() throws Exception {
		try {
			omml("\\begin{matrix}a & b\\end{matrix}");
			fail("expected LatexMathException");
		} catch (LatexMathException e) {
			assertTrue(e.getMessage().contains("matrix"));
		}
		try {
			omml("\\begin{aligned}x\\end{align}");
			fail("expected LatexMathException");
		} catch (LatexMathException e) {
			assertTrue(e.getMessage().contains("closed by"));
		}
	}

	@Test
	public void mismatchedRowSeparatorOutsideEnvironmentFails() throws Exception {
		try {
			omml("a \\\\ b");
			fail("expected LatexMathException");
		} catch (LatexMathException e) {
			// \\ only means something inside aligned
		}
	}

	@Test
	public void displayAlignedImports() throws Exception {
		List<MarkdownImportIssue> issues = new ArrayList<>();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart(
						"$$\n\\begin{aligned}\nP_{\\rm gross} &= P(U), \\\\\n"
						+ "P_{\\rm net} &= P_{\\rm gross}(1-L).\n\\end{aligned}\n$$\n",
						pkg);
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue(xml.contains("<m:oMathPara>"));
		assertTrue(xml.contains("<m:eqArr>"));
		assertTrue(issues.isEmpty());

		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		pkg.save(baos);
		assertTrue(baos.size() > 0);
	}

	@Test
	public void boxedCourseExampleImports() throws Exception {
		// the motivating example: \boxed no longer needs normalising away
		List<MarkdownImportIssue> issues = new ArrayList<>();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart(
						"$$\n\\boxed{\n\\text{atmosphere}\n\\rightarrow\n"
						+ "\\text{site wind}\n\\rightarrow\n\\text{rotor wind}\n}\n$$\n",
						pkg);
		String xml = XmlUtils.marshaltoString(
				pkg.getMainDocumentPart().getJaxbElement(), true, Context.jc);
		assertTrue(xml.contains("<m:borderBox>"));
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
