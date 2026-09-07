package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A complex field with no {@code w:fldChar w:fldCharType="separate"} has no result
 * (ECMA-376 17.16.18), so Word paints nothing for it: everything between its begin and
 * its end is field instruction.  For {@code IF} that is how Word writes a conditional
 * block of a footer - {@code IF { PAGE } = { NUMPAGES } "…" ""} - with the true branch,
 * table and all, sitting in {@code w:instrText}.
 *
 * <p>Measured against Word 365 on an 8-page document whose footer is exactly that:
 * Word's footer is the page number alone ("1/8" at y=811.2) and ours had a stray "19"
 * at y=724.4 and a {@code region-after extent="112.251pt"} against Word's ~47 - 65pt of
 * body lost on every page.</p>
 *
 * @since 17.1.0
 */
public class ResultlessIfFieldTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	private static String process(String body) throws Exception {
		WordprocessingMLPackage p = pkg(body);
		FieldsCombiner.process(p);
		return XmlUtils.marshaltoString(p.getMainDocumentPart().getJaxbElement(), true);
	}

	private static String begin() {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>";
	}
	private static String instr(String s) {
		return "<w:r><w:instrText xml:space=\"preserve\">" + s + "</w:instrText></w:r>";
	}
	private static String sep() {
		return "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>";
	}
	private static String end() {
		return "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	/** The whole span goes, including a table that lies between the begin and the end. */
	@Test
	public void aResultlessIfPaintsNothing() throws Exception {
		String body =
				"<w:p><w:r><w:t>before</w:t></w:r>"
				+ begin() + instr(" IF ")
				+ begin() + instr("PAGE ") + sep() + "<w:r><w:t>2</w:t></w:r>" + end()
				+ instr(" = ")
				+ begin() + instr("NUMPAGES") + sep() + "<w:r><w:t>8</w:t></w:r>" + end()
				+ instr("\"") + "</w:p>"
				+ "<w:tbl><w:tr><w:tc><w:tcPr><w:tcW w:w=\"5000\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p>" + instr("true branch") + "</w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p>" + instr("\" \"\" ") + end() + "<w:r><w:t>after</w:t></w:r></w:p>";

		String out = process(body);
		assertTrue("the text before the field must stay", out.contains("before"));
		assertTrue("the text after the field must stay", out.contains("after"));
		assertTrue("the branch's table must go", !out.contains("<w:tbl>"));
		assertTrue("the nested PAGE must go", !out.contains("PAGE"));
		assertTrue("the nested NUMPAGES must go", !out.contains("NUMPAGES"));
		assertTrue("the branch's text must go", !out.contains("true branch"));
	}

	/** An IF that does have a separate keeps its cached result, which is what Word shows. */
	@Test
	public void anIfWithAResultKeepsIt() throws Exception {
		String body = "<w:p>"
				+ begin() + instr(" IF 1 = 1 \"yes\" \"no\" ") + sep()
				+ "<w:r><w:t>yes</w:t></w:r>" + end() + "</w:p>";
		String out = process(body);
		assertTrue("the cached result must stay", out.contains("yes"));
	}

	/** Only IF: a PAGE field with no separate is one we render ourselves. */
	@Test
	public void aResultlessPageFieldIsLeftAlone() throws Exception {
		String body = "<w:p>" + begin() + instr(" PAGE ") + end() + "</w:p>";
		String out = process(body);
		assertTrue("PAGE must survive", out.contains("PAGE"));
	}

	/** Text sharing the run that carries the begin or the end is not lost: such a span
	 *  is left alone rather than deleted with the field. */
	@Test
	public void aBeginRunCarryingTextIsLeftAlone() throws Exception {
		String body = "<w:p>"
				+ "<w:r><w:t>keep</w:t><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ instr(" IF 1 = 1 \"a\" \"b\" ")
				+ end() + "</w:p>";
		String out = process(body);
		assertTrue("text in the begin run must survive", out.contains("keep"));
	}

	/** The paragraphs themselves stay (Word keeps the paragraph marks), so the block
	 *  count is unchanged where the begin and the end are in different paragraphs. */
	@Test
	public void theParagraphMarksStay() throws Exception {
		String body = "<w:p>" + begin() + instr(" IF 1 = 1 \"a\" \"b\" ") + "</w:p>"
				+ "<w:p>" + end() + "</w:p>";
		WordprocessingMLPackage p = pkg(body);
		FieldsCombiner.process(p);
		List<Object> content = p.getMainDocumentPart().getJaxbElement().getBody().getContent();
		assertEquals("both paragraph marks stay", 2, content.size());
	}
}
