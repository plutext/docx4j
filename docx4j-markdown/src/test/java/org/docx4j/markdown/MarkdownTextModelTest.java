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

	private static final String SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"40\" height=\"20\"><rect width=\"40\" height=\"20\" fill=\"red\"/></svg>";

	/** A picture as the ImageAddSVG sample writes it: an a:blip with only its svgBlip, on an image/svg+xml part. */
	private static String picture(String blip) {
		return "<w:p " + W + " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:drawing><wp:inline><wp:extent cx=\"381000\" cy=\"190500\"/><wp:docPr id=\"2\" name=\"Picture 2\" descr=\"A red box\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic>"
				+ "<pic:nvPicPr><pic:cNvPr id=\"1\" name=\"\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill>" + blip + "<a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic>"
				+ "</wp:inline></w:drawing></w:r></w:p>";
	}

	private static WordprocessingMLPackage svgPicture() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.DefaultXmlPart svg = new org.docx4j.openpackaging.parts.DefaultXmlPart(
				new org.docx4j.openpackaging.parts.PartName("/word/media/image1.svg"));
		svg.setRelationshipType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
		svg.setContentType(new org.docx4j.openpackaging.contenttype.ContentType("image/svg+xml"));
		svg.setDocument(new java.io.ByteArrayInputStream(SVG.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		String rId = pkg.getMainDocumentPart().addTargetPart(svg).getId();
		addP(pkg, picture("<a:blip><a:extLst><a:ext uri=\"{96DAC541-7B7A-43D3-8B79-37D633B846F1}\">"
				+ "<asvg:svgBlip xmlns:asvg=\"http://schemas.microsoft.com/office/drawing/2016/SVG/main\" r:embed=\"" + rId + "\"/>"
				+ "</a:ext></a:extLst></a:blip>"));
		// as loaded: the part is what docx4j makes of an image/svg+xml part, the svgBlip as unmarshalled
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		pkg.save(out);
		return WordprocessingMLPackage.load(new java.io.ByteArrayInputStream(out.toByteArray()));
	}

	/** An SVG picture with no PNG copy is written with its SVG, not dropped (issue 699). */
	@Test
	public void svgPictureWithNoPngCopy() throws Exception {
		WordprocessingMLPackage pkg = svgPicture();
		TextResult result = Texts.of(pkg, new TextOptions());
		org.docx4j.openpackaging.parts.Part part = null;
		for (org.docx4j.model.text.TextBlock b : result.blocks())
			for (org.docx4j.model.text.TextRun r : b.getRuns())
				if (r.getKind() == org.docx4j.model.text.TextRun.Kind.IMAGE) part = r.getImage().getPart();
		assertEquals("image/svg+xml", part.getContentType());
		String md = export(pkg);
		String prefix = "![A red box](data:image/svg+xml;base64,";
		assertTrue(md, md.startsWith(prefix));
		String svg = new String(java.util.Base64.getDecoder().decode(md.substring(prefix.length(), md.indexOf(')'))),
				java.nio.charset.StandardCharsets.UTF_8);
		assertTrue(svg, svg.contains("<rect") && svg.contains("fill=\"red\""));

		java.io.File dir = java.nio.file.Files.createTempDirectory("md-svg").toFile();
		md = new MarkdownExporter(new MarkdownExportOptions().setImageDirPath(dir.getAbsolutePath())).export(pkg);
		assertEquals("![A red box](image1.svg)\n", md);
		assertTrue(new java.io.File(dir, "image1.svg").length() > 0);
	}

	/** A picture whose image can't be had (a missing relationship) keeps its alt text. */
	@Test
	public void pictureWithNoImageKeepsItsAltText() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		addP(pkg, picture("<a:blip r:embed=\"rIdMissing\"/>"));
		assertEquals("![A red box]()\n", export(pkg));
	}

	/** A list's numbering is its num instance's, w:lvlOverride applied: a start override, an overriding level. */
	@Test
	public void numberingOverrides() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((org.docx4j.wml.Numbering) XmlUtils.unmarshalString("<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"1\"><w:multiLevelType w:val=\"singleLevel\"/>"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/></w:lvl></w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/><w:lvlOverride w:ilvl=\"0\"><w:startOverride w:val=\"5\"/></w:lvlOverride></w:num>"
				+ "<w:num w:numId=\"2\"><w:abstractNumId w:val=\"1\"/><w:lvlOverride w:ilvl=\"0\">"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"o\"/></w:lvl></w:lvlOverride></w:num>"
				+ "</w:numbering>", Context.jc, org.docx4j.wml.Numbering.class));
		addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr><w:r><w:t>Fifth</w:t></w:r></w:p>");
		addP(pkg, "<w:p " + W + "/>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"2\"/></w:numPr></w:pPr><w:r><w:t>Bulleted</w:t></w:r></w:p>");
		assertEquals("5. Fifth\n\n- Bulleted\n", export(pkg));
	}

	/**
	 * The numbering-style indirection (w:numStyleLink to a numbering style whose w:numPr names the w:num
	 * that holds the levels) is followed; a w:numPr from the paragraph's style counts; and a level linked
	 * to another paragraph style (w:lvl/w:pStyle) numbers no paragraph whose style merely carries its
	 * w:numPr, as Word gives it no number (the list labels' rule).
	 */
	@Test
	public void numberingThroughStyles() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((org.docx4j.wml.Numbering) XmlUtils.unmarshalString("<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"1\"><w:styleLink w:val=\"MyList\"/>"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"3\"/><w:numFmt w:val=\"upperRoman\"/><w:pStyle w:val=\"Numbered\"/><w:lvlText w:val=\"%1.\"/></w:lvl></w:abstractNum>"
				+ "<w:abstractNum w:abstractNumId=\"2\"><w:numStyleLink w:val=\"MyList\"/></w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/></w:num>"
				+ "<w:num w:numId=\"2\"><w:abstractNumId w:val=\"2\"/></w:num>"
				+ "</w:numbering>", Context.jc, org.docx4j.wml.Numbering.class));
		for (String style : new String[] {
				"<w:style " + W + " w:type=\"numbering\" w:styleId=\"MyList\"><w:name w:val=\"My List\"/><w:pPr><w:numPr><w:numId w:val=\"1\"/></w:numPr></w:pPr></w:style>",
				"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Numbered\"><w:name w:val=\"Numbered\"/><w:pPr><w:numPr><w:numId w:val=\"1\"/></w:numPr></w:pPr></w:style>",
				"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Other\"><w:name w:val=\"Other\"/><w:pPr><w:numPr><w:numId w:val=\"1\"/></w:numPr></w:pPr></w:style>" }) {
			pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
					(org.docx4j.wml.Style) XmlUtils.unmarshalString(style, Context.jc, org.docx4j.wml.Style.class));
		}
		addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"2\"/></w:numPr></w:pPr><w:r><w:t>Linked</w:t></w:r></w:p>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Numbered\"/></w:pPr><w:r><w:t>Styled</w:t></w:r></w:p>");
		addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Other\"/></w:pPr><w:r><w:t>Unnumbered</w:t></w:r></w:p>");
		// two lists, III. and III. (CR-014 P2): the second takes the other delimiter, or Markdown would run them together
		assertEquals("3. Linked\n\n3) Styled\n\nUnnumbered\n", export(pkg));
		// the list labels agree
		TextResult r = Texts.of(pkg, new TextOptions().listLabels(true));
		assertEquals("III.\t", r.blocks().get(0).getListLabel());
		assertEquals("III.\t", r.blocks().get(1).getListLabel());
		assertEquals(null, r.blocks().get(2).getListLabel());
		assertEquals(null, r.blocks().get(2).getNumbering());
	}

	/** A decimal list, numId 1, levels 0 and 1 at 720 and 1440 (hanging 360); and numId 2, another list. */
	private static WordprocessingMLPackage twoLists() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		StringBuilder x = new StringBuilder("<w:numbering " + W + ">");
		for (int a = 1; a <= 2; a++) {
			x.append("<w:abstractNum w:abstractNumId=\"" + a + "\">");
			for (int l = 0; l <= 1; l++) {
				x.append("<w:lvl w:ilvl=\"" + l + "\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%"
						+ (l + 1) + ".\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"" + (720 + 720 * l) + "\" w:hanging=\"360\"/></w:pPr></w:lvl>");
			}
			x.append("</w:abstractNum>");
		}
		x.append("<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/></w:num><w:num w:numId=\"2\"><w:abstractNumId w:val=\"2\"/></w:num></w:numbering>");
		ndp.setJaxbElement((org.docx4j.wml.Numbering) XmlUtils.unmarshalString(x.toString(), Context.jc, org.docx4j.wml.Numbering.class));
		return pkg;
	}

	private static void item(WordprocessingMLPackage pkg, int numId, int ilvl, String text) throws Exception {
		addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"" + ilvl + "\"/><w:numId w:val=\"" + numId
				+ "\"/></w:numPr></w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>");
	}

	private static void para(WordprocessingMLPackage pkg, Integer left, String text) throws Exception {
		addP(pkg, "<w:p " + W + ">" + (left == null ? "" : "<w:pPr><w:ind w:left=\"" + left + "\"/></w:pPr>")
				+ (text.isEmpty() ? "" : "<w:r><w:t>" + text + "</w:t></w:r>") + "</w:p>");
	}

	/**
	 * A paragraph at the margin between two items ends the list, and the list goes on at Word's count where
	 * it resumes (WD REQ-703): 1. note 2. 3., not 1. note 1. 2.
	 */
	@Test
	public void anInterruptedListGoesOnAtItsCount() throws Exception {
		WordprocessingMLPackage pkg = twoLists();
		item(pkg, 1, 0, "Apples");
		para(pkg, null, "A note");
		item(pkg, 1, 0, "Pears");
		item(pkg, 1, 0, "Plums");
		assertEquals("1. Apples\n\nA note\n\n2. Pears\n3. Plums\n", export(pkg));
		TextResult r = Texts.of(pkg);
		assertEquals(Integer.valueOf(1), r.blocks().get(0).getNumbering().getValue());
		assertEquals(Integer.valueOf(2), r.blocks().get(2).getNumbering().getValue());
	}

	/** An unlabelled paragraph indented to the item's text continues the item: one list, the count unbroken. */
	@Test
	public void anIndentedParagraphContinuesTheItem() throws Exception {
		WordprocessingMLPackage pkg = twoLists();
		item(pkg, 1, 0, "Apples");
		para(pkg, 720, "A note");
		item(pkg, 1, 0, "Pears");
		String md = export(pkg);
		assertEquals("1. Apples\n   \n   A note\n\n2. Pears\n", md);
	}

	/** Under a nested item, the paragraph continues the deepest item whose text it reaches. */
	@Test
	public void aParagraphContinuesTheDeepestItemItReaches() throws Exception {
		WordprocessingMLPackage pkg = twoLists();
		item(pkg, 1, 0, "A");
		item(pkg, 1, 1, "A.1");
		para(pkg, 720, "about A");
		item(pkg, 1, 1, "A.2");
		item(pkg, 1, 0, "B");
		assertEquals("1. A\n   \n   1. A.1\n   \n   about A\n   \n   2. A.2\n\n2. B\n", export(pkg));
	}

	/** Markdown has no empty paragraphs: an empty one between items neither ends the list nor joins it. */
	@Test
	public void anEmptyParagraphLeavesTheListAlone() throws Exception {
		WordprocessingMLPackage pkg = twoLists();
		item(pkg, 1, 0, "Apples");
		para(pkg, null, "");
		item(pkg, 1, 0, "Pears");
		assertEquals("1. Apples\n2. Pears\n", export(pkg));
	}

	/** Two lists one after the other stay two: the second takes the other delimiter. */
	@Test
	public void adjacentListsAreKeptApart() throws Exception {
		WordprocessingMLPackage pkg = twoLists();
		item(pkg, 1, 0, "X");
		item(pkg, 2, 0, "Y");
		item(pkg, 1, 0, "X again");
		assertEquals("1. X\n\n1) Y\n\n2. X again\n", export(pkg));
	}

	/**
	 * A margin paragraph inside a nested list ends it all; the nested list resumes under an empty item
	 * showing its parent's count (Markdown can't continue a nested list without its parent item).
	 */
	@Test
	public void aNestedListResumesUnderItsParentsCount() throws Exception {
		WordprocessingMLPackage pkg = twoLists();
		item(pkg, 1, 0, "A");
		item(pkg, 1, 0, "B");
		item(pkg, 1, 1, "B.1");
		para(pkg, null, "A note");
		item(pkg, 1, 1, "B.2");
		String md = export(pkg);
		assertEquals("1. A\n2. B\n   1. B.1\n\nA note\n\n2. 2. B.2\n", md);
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
