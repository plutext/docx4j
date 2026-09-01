package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * CR-markdown-math phase a: $-math recognition, currency guards, lossless
 * literal fallback (LaTeX→OMML is phase b), and issue reporting.
 */
public class MarkdownMathRecognitionTest {

	private final List<MarkdownImportIssue> issues = new ArrayList<>();

	private WordprocessingMLPackage convert(String md) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions().setIssueListener(issues::add))
				.importToMainDocumentPart(md, pkg);
		return pkg;
	}

	private static List<Object> content(WordprocessingMLPackage pkg) {
		return pkg.getMainDocumentPart().getContent();
	}

	private static String text(Object o) {
		StringBuilder sb = new StringBuilder();
		org.docx4j.TraversalUtil.visit(o, new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object child) {
				child = XmlUtils.unwrap(child);
				if (child instanceof Text) {
					sb.append(((Text) child).getValue());
				}
				return null;
			}
		});
		return sb.toString();
	}

	private static String codeCharText(P p) {
		StringBuilder sb = new StringBuilder();
		for (Object o : p.getContent()) {
			if (o instanceof R && ((R) o).getRPr() != null
					&& ((R) o).getRPr().getRStyle() != null
					&& "CodeChar".equals(((R) o).getRPr().getRStyle().getVal())) {
				sb.append(text(o));
			}
		}
		return sb.toString();
	}

	// -------------------------------------------------------- recognition

	@Test
	public void inlineDollarMath() throws Exception {
		WordprocessingMLPackage pkg = convert("Power: $P=\\frac12\\rho A U^3$ done\n");
		P p = (P) content(pkg).get(0);
		assertEquals("Power: $P=\\frac12\\rho A U^3$ done", text(p));
		// the math is one CodeChar-styled run, delimiters preserved
		assertEquals("$P=\\frac12\\rho A U^3$", codeCharText(p));
		assertEquals(1, issues.size());
		assertEquals("inline math", issues.get(0).getConstruct());
		assertEquals("P=\\frac12\\rho A U^3", issues.get(0).getSource());
	}

	@Test
	public void backslashParenInline() throws Exception {
		WordprocessingMLPackage pkg = convert("Euler \\(e^{i\\pi}=-1\\) qed\n");
		P p = (P) content(pkg).get(0);
		assertEquals("$e^{i\\pi}=-1$", codeCharText(p)); // normalized to $ form
	}

	@Test
	public void displayBlock() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"Before\n\n$$\nP_{\\rm wind}\n=\n\\frac12 \\rho A U^3\n$$\n\nAfter\n");
		assertEquals(3, content(pkg).size());
		P math = (P) content(pkg).get(1);
		// literal fallback paragraph: fenced, lines separated by w:br
		assertEquals("$$P_{\\rm wind}=\\frac12 \\rho A U^3$$", codeCharText(math));
		assertEquals(1, issues.size());
		assertEquals("display math", issues.get(0).getConstruct());
		assertEquals("P_{\\rm wind}\n=\n\\frac12 \\rho A U^3", issues.get(0).getSource());
	}

	@Test
	public void displayBracketBlockNormalizes() throws Exception {
		WordprocessingMLPackage pkg = convert("\\[\nE=mc^2\n\\]\n");
		P math = (P) content(pkg).get(0);
		assertEquals("$$E=mc^2$$", codeCharText(math));
	}

	@Test
	public void singleLineDoubleDollarIsDisplayHint() throws Exception {
		WordprocessingMLPackage pkg = convert("$$E=mc^2$$\n");
		P p = (P) content(pkg).get(0);
		assertEquals("$$E=mc^2$$", codeCharText(p));
	}

	@Test
	public void displayMathInsideListItem() throws Exception {
		WordprocessingMLPackage pkg = convert("- item text\n\n  $$\n  E=mc^2\n  $$\n");
		// equations in list contexts must not be a problem (unlike the Pandoc route)
		assertTrue(text(content(pkg).get(1)).contains("E=mc^2"));
	}

	// -------------------------------------------------------- guards

	@Test
	public void currencyIsNotMath() throws Exception {
		WordprocessingMLPackage pkg = convert("It costs $5 now and $6 later.\n");
		P p = (P) content(pkg).get(0);
		assertEquals("It costs $5 now and $6 later.", text(p));
		assertEquals("", codeCharText(p));
		assertTrue(issues.isEmpty());
	}

	@Test
	public void spacedDollarsAreNotMath() throws Exception {
		WordprocessingMLPackage pkg = convert("a $ b $ c\n");
		assertEquals("a $ b $ c", text(content(pkg).get(0)));
		assertTrue(issues.isEmpty());
	}

	@Test
	public void escapedDollarIsLiteral() throws Exception {
		WordprocessingMLPackage pkg = convert("pay \\$5x\\$ now\n");
		P p = (P) content(pkg).get(0);
		assertEquals("pay $5x$ now", text(p));
		assertEquals("", codeCharText(p));
	}

	@Test
	public void mathToggleOff() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		EnumSet<MarkdownImportOptions.Extension> noMath =
				EnumSet.complementOf(EnumSet.of(MarkdownImportOptions.Extension.MATH));
		new MarkdownImporter(new MarkdownImportOptions().setExtensions(noMath)
				.setIssueListener(issues::add))
				.importToMainDocumentPart("Power $U^3$ here\n", pkg);
		P p = (P) content(pkg).get(0);
		assertEquals("Power $U^3$ here", text(p));
		assertEquals("", codeCharText(p));
		assertTrue(issues.isEmpty());
	}

	// -------------------------------------------------------- rendering

	@Test
	public void extensionRendersMathBack() throws Exception {
		// parse + render (the extension's MarkdownRenderer side): canonical form kept
		java.util.List<org.commonmark.Extension> extensions =
				java.util.List.of(org.docx4j.markdown.math.MathExtension.create());
		org.commonmark.parser.Parser parser =
				org.commonmark.parser.Parser.builder().extensions(extensions).build();
		org.commonmark.renderer.markdown.MarkdownRenderer renderer =
				org.commonmark.renderer.markdown.MarkdownRenderer.builder().extensions(extensions).build();

		String md = "Inline $P=\\frac12\\rho A U^3$ and:\n\n$$\nU_{\\rm REWS}\n$$\n";
		String out = renderer.render(parser.parse(md));
		assertEquals(md, out);
		// stable on the second trip too
		assertEquals(out, renderer.render(parser.parse(out)));
	}

	@Test
	public void literalDollarSurvivesRenderTrip() throws Exception {
		java.util.List<org.commonmark.Extension> extensions =
				java.util.List.of(org.docx4j.markdown.math.MathExtension.create());
		org.commonmark.parser.Parser parser =
				org.commonmark.parser.Parser.builder().extensions(extensions).build();
		org.commonmark.renderer.markdown.MarkdownRenderer renderer =
				org.commonmark.renderer.markdown.MarkdownRenderer.builder().extensions(extensions).build();

		String out = renderer.render(parser.parse("costs $5 now\n"));
		// '$' is escaped in text so it can't become math on re-parse
		assertEquals("costs \\$5 now\n", out);
		assertEquals(out, renderer.render(parser.parse(out)));
	}

	@Test
	public void outputSavesAndMarshals() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"# Math\n\nInline $x^2$ and\n\n$$\n\\frac{a}{b}\n$$\n");
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		pkg.save(baos);
		assertTrue(baos.size() > 0);
	}

}
