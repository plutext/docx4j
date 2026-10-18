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
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * The Markdown export on the text model (CR-046 phase 3, WD REQ-202, REQ-703, REQ-704): what the
 * text model reads is what is written.
 */
public class MarkdownTextModelTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static void addP(WordprocessingMLPackage pkg, String pXml) throws Exception {
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));
	}

	private static String export(WordprocessingMLPackage pkg) throws Exception {
		return new MarkdownExporter().export(pkg);
	}

	/** A HYPERLINK field's result is a link to its instruction's address, whatever field_codes says (REQ-703). */
	@Test
	public void hyperlinkFieldIsALink() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addP(pkg, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">See </w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> HYPERLINK \"https://example.org/a\" \\l \"part2\" \\o \"tip\" </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>the example</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>"
				+ "<w:r><w:t>.</w:t></w:r></w:p>");
		assertEquals("See [the example](https://example.org/a#part2).\n", export(pkg));
		MarkdownExportOptions options = new MarkdownExportOptions();
		options.getTextOptions().fieldCodes(true);
		assertEquals("See  HYPERLINK \"https://example.org/a\" \\\\l \"part2\" \\\\o \"tip\" [the example](https://example.org/a#part2).\n",
				new MarkdownExporter(options).export(pkg));
	}

	/** A simple field's HYPERLINK too; and a w:hyperlink's anchor. */
	@Test
	public void simpleFieldAndAnchorLinks() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addP(pkg, "<w:p " + W + "><w:fldSimple w:instr=\" HYPERLINK \\l &quot;intro&quot; \"><w:r><w:t>back</w:t></w:r></w:fldSimple>"
				+ "<w:r><w:t xml:space=\"preserve\"> and </w:t></w:r>"
				+ "<w:hyperlink w:anchor=\"end\"><w:r><w:t>on</w:t></w:r></w:hyperlink></w:p>");
		assertEquals("[back](#intro) and [on](#end)\n", export(pkg));
	}

	/** Hidden text is the text model's: left out by default (WD REQ-507). */
	@Test
	public void hiddenTextIsLeftOut() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addP(pkg, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">shown </w:t></w:r>"
				+ "<w:r><w:rPr><w:vanish/></w:rPr><w:t>hidden</w:t></w:r></w:p>");
		assertEquals("shown \n", export(pkg)); // nothing trimmed (WD REQ-602)
		MarkdownExportOptions options = new MarkdownExportOptions();
		options.getTextOptions().hidden(true);
		assertEquals("shown hidden\n", new MarkdownExporter(options).export(pkg));
	}

	/** Footnotes [^n], endnotes [^en]: numbered apart, so labelled apart (WD REQ-503). */
	@Test
	public void endnotesAreNotes() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		EndnotesPart endnotes = new EndnotesPart();
		pkg.getMainDocumentPart().addTargetPart(endnotes);
		endnotes.setJaxbElement((org.docx4j.wml.CTEndnotes) XmlUtils.unmarshalString("<w:endnotes " + W + ">"
				+ "<w:endnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:id=\"1\"><w:p><w:r><w:endnoteRef/></w:r><w:r><w:t xml:space=\"preserve\"> The end note.</w:t></w:r></w:p></w:endnote>"
				+ "</w:endnotes>", Context.jc, org.docx4j.wml.CTEndnotes.class));
		addP(pkg, "<w:p " + W + "><w:r><w:t>Body</w:t></w:r><w:r><w:endnoteReference w:id=\"1\"/></w:r></w:p>");
		assertEquals("Body[^e1]\n\n[^e1]: The end note.\n", export(pkg));
	}

	/** Under metadata: true, a YAML front-matter block of the document properties (WD REQ-704). */
	@Test
	public void frontMatter() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.docProps.core.dc.elements.ObjectFactory dc = new org.docx4j.docProps.core.dc.elements.ObjectFactory();
		org.docx4j.docProps.core.dc.elements.SimpleLiteral title = dc.createSimpleLiteral();
		title.getContent().add("A \"quoted\" title");
		pkg.getDocPropsCorePart().getJaxbElement().setTitle(dc.createTitle(title));
		pkg.getMainDocumentPart().addParagraphOfText("Body");
		MarkdownExportOptions options = new MarkdownExportOptions();
		options.getTextOptions().metadata(true);
		String md = new MarkdownExporter(options).export(pkg);
		assertTrue(md, md.startsWith("---\ncore:\n  title: \"A \\\"quoted\\\" title\"\n"));
		assertTrue(md, md.endsWith("---\n\nBody\n"));
		assertEquals("Body\n", export(pkg));
	}

	/** In markup, a comment at its reference: {>>text<<} (WD REQ-504, REQ-703). */
	@Test
	public void commentInMarkup() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		CommentsPart comments = new CommentsPart();
		pkg.getMainDocumentPart().addTargetPart(comments);
		comments.setJaxbElement((org.docx4j.wml.Comments) XmlUtils.unmarshalString("<w:comments " + W + ">"
				+ "<w:comment w:id=\"3\" w:author=\"Ann\"><w:p><w:r><w:t>Check this</w:t></w:r></w:p></w:comment>"
				+ "</w:comments>", Context.jc, org.docx4j.wml.Comments.class));
		addP(pkg, "<w:p " + W + "><w:commentRangeStart w:id=\"3\"/><w:r><w:t>Claim</w:t></w:r><w:commentRangeEnd w:id=\"3\"/>"
				+ "<w:r><w:commentReference w:id=\"3\"/></w:r></w:p>");
		assertEquals("Claim\n", export(pkg));
		assertEquals("Claim{>>Check this<<}\n", new MarkdownExporter(new MarkdownExportOptions().setView(TextView.MARKUP)).export(pkg));
	}

	/** A text box's paragraphs once, after its anchoring paragraph, from the branch read for text (WD REQ-518). */
	@Test
	public void textBoxOnceAfterItsAnchor() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addP(pkg, "<w:p " + W + " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
				+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\">"
				+ "<w:r><w:t>Anchor</w:t></w:r><w:r><mc:AlternateContent>"
				+ "<mc:Choice Requires=\"wps\"><w:drawing><wp:inline><wp:extent cx=\"1\" cy=\"1\"/><wp:docPr id=\"1\" name=\"Box\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:txbx><w:txbxContent><w:p><w:r><w:t>In the box</w:t></w:r></w:p></w:txbxContent></wps:txbx><wps:bodyPr/></wps:wsp>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></mc:Choice>"
				+ "<mc:Fallback><w:pict><v:shape><v:textbox><w:txbxContent><w:p><w:r><w:t>In the box</w:t></w:r></w:p></w:txbxContent></v:textbox></v:shape></w:pict></mc:Fallback>"
				+ "</mc:AlternateContent></w:r></w:p>");
		pkg.getMainDocumentPart().addParagraphOfText("After");
		assertEquals("Anchor\n\nIn the box\n\nAfter\n", export(pkg));
	}

	private static void addTable(WordprocessingMLPackage pkg, String tblXml) throws Exception {
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(tblXml, Context.jc, org.docx4j.wml.Tbl.class));
	}

	private static String tc(String jc, String text) {
		return "<w:tc><w:p>" + (jc == null ? "" : "<w:pPr><w:jc w:val=\"" + jc + "\"/></w:pPr>")
				+ (text == null ? "" : "<w:r><w:t>" + text + "</w:t></w:r>") + "</w:p></w:tc>";
	}

	/**
	 * The GFM header is the w:tblHeader rows (WD REQ-703), several of them one row, their cells' texts
	 * " / " apart; a column's alignment is its topmost header cell's first paragraph's, empty or not,
	 * since GFM aligns a column from its header cell.
	 */
	@Test
	public void headerRowsAndAlignment() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String header = "<w:trPr><w:tblHeader/></w:trPr>";
		addTable(pkg, "<w:tbl " + W + ">"
				+ "<w:tr>" + header + tc("right", "A") + tc(null, "B") + "</w:tr>"
				+ "<w:tr>" + header + tc(null, "a2") + tc("center", "b2") + "</w:tr>"
				+ "<w:tr>" + tc(null, "1") + tc("center", "2") + "</w:tr></w:tbl>");
		assertEquals("|A / a2|B / b2|\n|---:|---|\n|1|2|\n", export(pkg));

		pkg = WordprocessingMLPackage.createPackage();
		addTable(pkg, "<w:tbl " + W + ">"
				+ "<w:tr>" + tc("right", null) + tc("center", "H") + "</w:tr>"
				+ "<w:tr>" + tc(null, "x") + tc(null, "1") + "</w:tr></w:tbl>");
		assertEquals("||H|\n|---:|:---:|\n|x|1|\n", export(pkg));
	}

	/** export(TextResult) writes the reading it is given: the Markdown and the plain text of one reading. */
	@Test
	public void exportOfAResult() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addStyledParagraphOfText("Heading1", "Title");
		pkg.getMainDocumentPart().addParagraphOfText("Body");
		TextResult result = Texts.of(pkg, TextOptions.of(TextView.ACCEPTED));
		assertEquals(export(pkg), new MarkdownExporter().export(result));
		assertEquals("# Title\n\nBody\n", new MarkdownExporter().export(result));
	}
}
