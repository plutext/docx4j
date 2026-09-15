package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * Malformed nesting from broken producers: a {@code w:r} directly inside a {@code w:r},
 * and a {@code w:p} directly inside a {@code w:r} or a {@code w:hyperlink}.  None is in
 * the content model, so JAXB reports "unexpected element" and - once the preprocessor
 * has let unmarshalling continue - the whole subtree was discarded silently.
 *
 * <p>Word renders them, so {@code mc-preprocessor.xslt} hoists the content into the
 * legal position around it.  Measured on a corpus document holding 76 runs nested in
 * runs: the text it extracts went from 119,726 characters to 145,483, one word going
 * from 302 occurrences to 466 and another from 11 to 21, and the FO had held none of
 * the first at all.  10 documents of three corpora hold the shape.</p>
 *
 * @since 17.1.0
 */
public class MalformedNestingTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String text(String body) throws Exception {
		String xml = "<w:document " + W + "><w:body>" + body + "</w:body></w:document>";
		Object o = XmlUtils.unmarshal(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		StringWriter sw = new StringWriter();
		TextUtils.extractText(((Document) XmlUtils.unwrap(o)), sw);
		return sw.toString();
	}

	@Test
	public void runNestedInRunKeepsItsText() throws Exception {
		assertEquals("outer inner tail", text(
				"<w:p><w:r><w:t xml:space=\"preserve\">outer </w:t>"
				+ "<w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">inner </w:t></w:r>"
				+ "<w:t>tail</w:t></w:r></w:p>"));
	}

	@Test
	public void runNestedTwoDeepKeepsItsText() throws Exception {
		assertEquals("a b c", text(
				"<w:p><w:r><w:t xml:space=\"preserve\">a </w:t>"
				+ "<w:r><w:t xml:space=\"preserve\">b </w:t>"
				+ "<w:r><w:t>c</w:t></w:r></w:r></w:r></w:p>"));
	}

	@Test
	public void paragraphNestedInRunKeepsItsText() throws Exception {
		assertEquals("outer inner", text(
				"<w:p><w:r><w:t xml:space=\"preserve\">outer </w:t>"
				+ "<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t>inner</w:t></w:r></w:p>"
				+ "</w:r></w:p>"));
	}

	@Test
	public void paragraphNestedInHyperlinkKeepsItsText() throws Exception {
		assertEquals("before inner", text(
				"<w:p><w:hyperlink w:anchor=\"x\"><w:r><w:t xml:space=\"preserve\">before </w:t></w:r>"
				+ "<w:p><w:r><w:t>inner</w:t></w:r></w:p>"
				+ "</w:hyperlink></w:p>"));
	}

	/**
	 * The nested runs keep their own w:rPr - and their own whitespace.
	 *
	 * <p>17.1.0 first flattened the nested runs' content into the outer run, which lost
	 * both.  Measured against Word 365 on a document whose hyperlinks hold
	 * {@code w:hyperlink/w:r/(w:rPr, w:r, w:r, ...)} with the words in
	 * {@code w:rStyle="Highlight"} runs and the spaces between them in runs of their own:
	 * Word paints "Orb&aacute;n Viktor &raquo; Mondatok" in the highlighted style and
	 * docx4j painted "Orb&aacute;nViktor&raquo; Mondatok", the lone-space runs gone, on
	 * 466 lines.  The outer run is split instead, so each nested run stands as a run of
	 * its own.</p>
	 */
	@Test
	public void nestedRunsKeepTheirOwnFormattingAndSpaces() throws Exception {
		String body = "<w:p><w:hyperlink w:anchor=\"x\"><w:r><w:rPr><w:i/></w:rPr>"
				+ "<w:r><w:rPr><w:b/></w:rPr><w:t>Orban</w:t></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> </w:t></w:r>"
				+ "<w:r><w:rPr><w:b/></w:rPr><w:t>Viktor</w:t></w:r>"
				+ "</w:r></w:hyperlink></w:p>";
		assertEquals("Orban Viktor", text(body));

		String xml = "<w:document " + W + " xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">"
				+ "<w:body>" + body + "</w:body></w:document>";
		Object o = XmlUtils.unmarshal(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		String marshalled = XmlUtils.marshaltoString(o, true, true);
		assertTrue("the nested runs' own w:rPr was lost: " + marshalled,
				marshalled.contains("<w:b/>"));
	}

	// ------------------------------------------------ a nested paragraph is a paragraph

	private static List<P> paragraphs(String body) throws Exception {
		String xml = "<w:document " + W + "><w:body>" + body + "</w:body></w:document>";
		Object o = XmlUtils.unmarshal(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		List<P> out = new ArrayList<P>();
		for (Object c : ((Document) XmlUtils.unwrap(o)).getBody().getContent()) {
			if (XmlUtils.unwrap(c) instanceof P) out.add((P) XmlUtils.unwrap(c));
		}
		return out;
	}

	private static String textOf(P p) throws Exception {
		StringWriter sw = new StringWriter();
		TextUtils.extractText(p, sw);
		return sw.toString();
	}

	private static String pStyle(P p) {
		return p.getPPr()==null || p.getPPr().getPStyle()==null ? null : p.getPPr().getPStyle().getVal();
	}

	private static final String INNER = "<w:p><w:pPr><w:pStyle w:val=\"Inner\"/><w:jc w:val=\"center\"/></w:pPr>"
			+ "<w:r><w:t>inner</w:t></w:r></w:p>";

	/**
	 * A paragraph nested in a hyperlink is a paragraph of its own, with its own w:pPr
	 * (since 17.1.1).  Hoisting its runs into the paragraph around it kept the text but
	 * lost the style: a corpus document's 76 article summaries, each a w:p inside the
	 * w:hyperlink of the paragraph before it, in a style whose w:i is their only italic,
	 * came out roman where Word draws 45 italic lines.
	 */
	@Test
	public void paragraphNestedInHyperlinkIsAParagraphOfItsOwn() throws Exception {
		List<P> ps = paragraphs("<w:p><w:pPr><w:pStyle w:val=\"Outer\"/></w:pPr>"
				+ "<w:hyperlink w:anchor=\"x\"><w:r><w:t xml:space=\"preserve\">before </w:t></w:r>"
				+ INNER + "</w:hyperlink></w:p>");
		assertEquals("two paragraphs, the nested one after the outer", 2, ps.size());
		assertEquals("Outer", pStyle(ps.get(0)));
		assertEquals("before ", textOf(ps.get(0)));
		assertTrue("the hyperlink stays in the outer paragraph",
				XmlUtils.marshaltoString(ps.get(0), true, false).contains("w:hyperlink"));
		assertEquals("Inner", pStyle(ps.get(1)));
		assertEquals("center", ps.get(1).getPPr().getJc().getVal().value());
		assertEquals("inner", textOf(ps.get(1)));
	}

	/** What follows the nested paragraph in the hyperlink goes into a further paragraph
	 *  carrying the outer w:pPr, in its own copy of the hyperlink. */
	@Test
	public void contentAfterTheNestedParagraphGoesIntoAThirdParagraph() throws Exception {
		List<P> ps = paragraphs("<w:p><w:pPr><w:pStyle w:val=\"Outer\"/></w:pPr>"
				+ "<w:hyperlink w:anchor=\"x\"><w:r><w:t xml:space=\"preserve\">before </w:t></w:r>"
				+ INNER + "<w:r><w:t>after</w:t></w:r></w:hyperlink></w:p>");
		assertEquals(3, ps.size());
		assertEquals("before ", textOf(ps.get(0)));
		assertEquals("inner", textOf(ps.get(1)));
		assertEquals("Outer", pStyle(ps.get(2)));
		assertEquals("after", textOf(ps.get(2)));
		assertTrue(XmlUtils.marshaltoString(ps.get(2), true, false).contains("w:anchor=\"x\""));
	}

	/** A nested paragraph that ends its hyperlink adds no empty paragraph after itself. */
	@Test
	public void nothingAfterTheNestedParagraphMeansNoEmptyParagraph() throws Exception {
		List<P> ps = paragraphs("<w:p><w:hyperlink w:anchor=\"x\"><w:r><w:t>before</w:t></w:r>"
				+ INNER + "</w:hyperlink><w:bookmarkEnd w:id=\"0\"/></w:p>");
		assertEquals("the trailing bookmarkEnd is not content", 2, ps.size());
	}

	/** A paragraph nested in a run: the run is split around it, each half keeping the
	 *  run's own w:rPr, and the nested paragraph stands between the halves. */
	@Test
	public void paragraphNestedInRunSplitsTheRunAroundIt() throws Exception {
		List<P> ps = paragraphs("<w:p><w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">outer </w:t>"
				+ INNER + "<w:t>tail</w:t></w:r></w:p>");
		assertEquals(3, ps.size());
		assertEquals("outer ", textOf(ps.get(0)));
		assertEquals("inner", textOf(ps.get(1)));
		assertEquals("Inner", pStyle(ps.get(1)));
		assertEquals("tail", textOf(ps.get(2)));
		R tail = (R) XmlUtils.unwrap(ps.get(2).getContent().get(0));
		assertTrue("the outer run's w:rPr carries to the tail", tail.getRPr()!=null && tail.getRPr().getB()!=null);
		assertTrue(XmlUtils.unwrap(tail.getContent().get(0)) instanceof Text);
	}

	/** Two nested paragraphs in one hyperlink, in order, with the outer content between. */
	@Test
	public void twoNestedParagraphsKeepTheirOrder() throws Exception {
		List<P> ps = paragraphs("<w:p><w:hyperlink w:anchor=\"x\"><w:r><w:t>a</w:t></w:r>"
				+ INNER.replace("inner", "b") + "<w:r><w:t>c</w:t></w:r>"
				+ INNER.replace("inner", "d") + "</w:hyperlink></w:p>");
		assertEquals(4, ps.size());
		assertEquals("a", textOf(ps.get(0)));
		assertEquals("b", textOf(ps.get(1)));
		assertEquals("c", textOf(ps.get(2)));
		assertEquals("d", textOf(ps.get(3)));
	}

	/** A run deeper inside a run is the ordinary shape of a text box, and must not be
	 *  touched: w:r/w:pict/v:textbox/w:txbxContent/w:p/w:r is perfectly legal. */
	@Test
	public void runInsideATextBoxIsUntouched() throws Exception {
		String vml = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
		String body = "<w:p><w:r><w:t xml:space=\"preserve\">outer </w:t>"
				+ "<w:pict " + vml + "><v:shape><v:textbox><w:txbxContent>"
				+ "<w:p><w:r><w:t>boxed</w:t></w:r></w:p>"
				+ "</w:txbxContent></v:textbox></v:shape></w:pict>"
				+ "<w:r><w:t>hoisted</w:t></w:r></w:r></w:p>";
		String t = text(body);
		assertEquals("outer boxedhoisted", t);
	}
}
