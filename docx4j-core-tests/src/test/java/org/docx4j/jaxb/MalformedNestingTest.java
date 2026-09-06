package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Document;
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
 * @since 17.0.6
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
	 * <p>17.0.6 first flattened the nested runs' content into the outer run, which lost
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
