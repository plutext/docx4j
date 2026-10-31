/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * A raised or lowered run in {@code <sup>} or {@code <sub>} (WD REQ-710; CR-069 phase 3): CommonMark inline HTML,
 * never a Unicode superscript or subscript character; the tags outside the emphasis and outside a code span; none in
 * a code block, whose text is literal, nor around a note's reference, which is a reference. Each Markdown is also
 * rendered, as GitHub and the DPP read it, so that the tags are seen to be markup and the emphasis inside them to open.
 */
public class MarkdownVerticalAlignTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String r(String rPr, String text) {
		return "<w:r>" + (rPr.isEmpty() ? "" : "<w:rPr>" + rPr + "</w:rPr>") + "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static final String SUP = "<w:vertAlign w:val=\"superscript\"/>";
	private static final String SUB = "<w:vertAlign w:val=\"subscript\"/>";

	private static WordprocessingMLPackage document(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		for (String p : paragraphs) {
			pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString("<w:p " + W + ">" + p + "</w:p>", Context.jc, P.class));
		}
		return pkg;
	}

	private static String export(WordprocessingMLPackage pkg) throws Exception {
		return new MarkdownExporter().export(pkg);
	}

	private static String render(String markdown) {
		java.util.List<org.commonmark.Extension> gfm = Arrays.asList(StrikethroughExtension.create());
		return HtmlRenderer.builder().extensions(gfm).build().render(Parser.builder().extensions(gfm).build().parse(markdown));
	}

	/** The probe's cases (dpp C06, C07): the tags, the literal characters kept as stored (REQ-602). */
	@Test
	public void raisedAndLoweredRunsAreTagged() throws Exception {
		String md = export(document(
				r("", "H") + r(SUB, "2") + r("", "O and x") + r(SUP, "2") + r("", " on the 1") + r(SUP, "st") + r("", "; literal x² and H₂O."),
				r("", "Revenue 48,210") + r(SUP, "3") + r("", " in the period.")));
		assertEquals("H<sub>2</sub>O and x<sup>2</sup> on the 1<sup>st</sup>; literal x² and H₂O.\n\n"
				+ "Revenue 48,210<sup>3</sup> in the period.\n", md);
		assertEquals("<p>Revenue 48,210<sup>3</sup> in the period.</p>\n", render("Revenue 48,210<sup>3</sup> in the period.\n"));
	}

	/** The tags outside the emphasis: {@code **} after a digit and before a tag would not open, inside it does. */
	@Test
	public void theTagsAreOutsideTheEmphasis() throws Exception {
		String md = export(document(r("", "Revenue 48,210") + r("<w:b/>" + SUP, "3") + r("", " and C") + r("<w:i/><w:strike/>" + SUB, "x")));
		assertEquals("Revenue 48,210<sup>**3**</sup> and C<sub>~~*x*~~</sub>\n", md);
		assertEquals("<p>Revenue 48,210<sup><strong>3</strong></sup> and C<sub><del><em>x</em></del></sub></p>\n", render(md));
	}

	/** Raised runs of one formatting share their tags; the formatting changing inside them, each part has its own. */
	@Test
	public void runsShareTheirTagsAsTheyShareTheirEmphasis() throws Exception {
		String md = export(document(r("", "x") + r(SUP, "a") + r(SUP + "<w:color w:val=\"FF0000\"/>", "b") + r("<w:b/>" + SUP, "c") + r(SUB, "d")));
		assertEquals("x<sup>ab</sup><sup>**c**</sup><sub>d</sub>\n", md);
	}

	/** A code span inside the tags, where they are markup; a code block has none, its text being literal. */
	@Test
	public void codeSpanInsideTheTagsAndNoneInACodeBlock() throws Exception {
		String mono = "<w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/>";
		String md = export(document(r("", "call ") + r(mono + SUP, "f()") + r("", " here")));
		assertEquals("call <sup>`f()`</sup> here\n", md);
		assertEquals("<p>call <sup><code>f()</code></sup> here</p>\n", render(md));
		WordprocessingMLPackage code = document("<w:pPr><w:pStyle w:val=\"HTMLPreformatted\"/></w:pPr>" + r("", "x = 10") + r(SUP, "3"));
		assertEquals("```\nx = 103\n```\n", export(code));
	}

	/** Document text can't form the tags: its {@code <} is escaped. */
	@Test
	public void documentTextCannotFormTheTags() throws Exception {
		String md = export(document(r("", "a &lt;sup&gt;3&lt;/sup&gt; b")));
		assertEquals("<p>a &lt;sup&gt;3&lt;/sup&gt; b</p>\n", render(md));
	}

	/** A note's reference is a reference, not a superscript (REQ-710): its style raises it, Markdown writes {@code [^1]}. */
	@Test
	public void aNoteReferenceIsNotASuperscript() throws Exception {
		WordprocessingMLPackage pkg = document(r("", "Body") + "<w:r><w:rPr>" + SUP + "</w:rPr><w:footnoteReference w:id=\"1\"/></w:r>");
		FootnotesPart footnotes = new FootnotesPart();
		pkg.getMainDocumentPart().addTargetPart(footnotes);
		footnotes.setJaxbElement((org.docx4j.wml.CTFootnotes) XmlUtils.unmarshalString("<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p><w:r><w:rPr>" + SUP + "</w:rPr><w:footnoteRef/></w:r>" + r("", " The note.") + "</w:p></w:footnote>"
				+ "</w:footnotes>", Context.jc, org.docx4j.wml.CTFootnotes.class));
		assertEquals("Body[^1]\n\n[^1]: The note.\n", export(pkg));
	}
}
