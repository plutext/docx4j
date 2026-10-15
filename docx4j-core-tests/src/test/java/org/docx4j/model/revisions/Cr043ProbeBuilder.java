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
package org.docx4j.model.revisions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;

import org.docx4j.XmlUtils;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Document;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

/**
 * The base of CR-043 phase 0's Word probe: one document holding every case the Word-made fixtures
 * lack (CR-043 &#xa7;4.2), each a paragraph or table whose text starts with its case's tag (C01,
 * C02, ...). The PowerShell script {@code cr043-probe.ps1} beside the output makes each case's
 * tracked edit through Word's object model, saves the tracked document, and has Word accept all
 * and reject all of it; those three files are the goldens. Cases C10 and C10b are tracked already:
 * a paragraph mark inserted and one deleted between paragraphs of different properties, written
 * here, which Word then resolves (the rule the editor and core-ts disagree on, CR-043 &#xa7;3.2).
 *
 * <p>Usage: {@code Cr043ProbeBuilder <outDir>} writes {@code cr043-base.docx} and
 * {@code cr043-picture.png} (the picture case C04 inserts).</p>
 *
 * @since 17.3.3
 */
public class Cr043ProbeBuilder {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String NS = "xmlns:w=\"" + W + "\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";

	/** The author and date of the two handwritten revisions (C10, C10b). */
	static final String REV = "w:author=\"CR-043 base\" w:date=\"2026-10-16T00:00:00Z\"";

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		byte[] png = png(64, 32, 0x3366cc);
		Files.write(new File(out, "cr043-picture.png").toPath(), png(48, 24, 0xcc6633));
		WordprocessingMLPackage pkg = build(png);
		pkg.save(new File(out, "cr043-base.docx"));
		System.out.println("wrote " + new File(out, "cr043-base.docx"));
	}

	static WordprocessingMLPackage build(byte[] png) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage(PageSizePaper.A4, false);
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		HeaderPart header = new HeaderPart();
		header.setJaxbElement((Hdr) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:hdr " + NS + ">"
				+ p("C19 header text, a word to delete: DELETEME. Inserted text goes at the end.") + "</w:hdr>")));
		String headerId = mdp.addTargetPart(header).getId();
		FooterPart footer = new FooterPart();
		footer.setJaxbElement((Ftr) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:ftr " + NS + ">"
				+ p("C20 footer text, a word to delete: DELETEME. Inserted text goes at the end.") + "</w:ftr>")));
		String footerId = mdp.addTargetPart(footer).getId();

		EndnotesPart endnotes = new EndnotesPart();
		endnotes.setJaxbElement((CTEndnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:endnotes " + NS + ">"
				+ "<w:endnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:id=\"1\"><w:p><w:r><w:endnoteRef/></w:r><w:r><w:t xml:space=\"preserve\"> C12 endnote text (its reference is deleted).</w:t></w:r></w:p></w:endnote>"
				+ "<w:endnote w:id=\"2\"><w:p><w:r><w:endnoteRef/></w:r><w:r><w:t xml:space=\"preserve\"> C21 endnote text, a word to delete: DELETEME. Inserted text goes at the end.</w:t></w:r></w:p></w:endnote>"
				+ "</w:endnotes>")));
		mdp.addTargetPart(endnotes);

		CommentsPart comments = new CommentsPart();
		comments.setJaxbElement((Comments) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:comments " + NS + ">"
				+ "<w:comment w:id=\"0\" w:author=\"CR-043 base\" w:date=\"2026-10-16T00:00:00Z\" w:initials=\"CB\">"
				+ p("C18 comment text, a word to delete: DELETEME.") + "</w:comment></w:comments>")));
		mdp.addTargetPart(comments);

		StringBuilder b = new StringBuilder("<w:document " + NS + "><w:body>");
		b.append(p("CR-043 phase 0 probe, built by docx4j (Cr043ProbeBuilder). Each case's text starts with its tag; "
				+ "cr043-probe.ps1 makes the tracked edit the case names, through Word's object model."));

		// fields
		b.append(p("C01 a PAGE field is inserted at the end of this paragraph: "));
		b.append("<w:p>" + r("C02 the PAGE field here is deleted: ") + field(" PAGE ", "1") + r(" (after the field).") + "</w:p>");
		// pictures
		b.append(p("C04 a picture is inserted at the end of this paragraph: "));
		b.append("<w:p>" + r("C05 the picture here is deleted: ") + r(" (after the picture).") + "</w:p>");
		// paragraph marks next to tables and in a cell
		b.append(p("C08 this paragraph's mark, right before a table, is deleted."));
		b.append(table("C08", 1, 2));
		b.append("<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid><w:tr><w:tc>"
				+ "<w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr>"
				+ p("C09 first paragraph in the cell: its mark is deleted.")
				+ p("C09 last paragraph in the cell: deleting its mark (the cell's end) is tried.")
				+ "</w:tc></w:tr></w:tbl>");
		b.append(p("C09 after the table."));
		// handwritten: a mark inserted, a mark deleted, between paragraphs of different properties
		b.append("<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/><w:rPr><w:ins w:id=\"501\" " + REV + "/></w:rPr></w:pPr>"
				+ r("C10 heading half (Heading 1; its mark inserted, written by docx4j)") + "</w:p>");
		b.append("<w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr>" + r("C10 body half (Normal, right aligned)") + "</w:p>");
		b.append("<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/><w:rPr><w:del w:id=\"502\" " + REV + "/></w:rPr></w:pPr>"
				+ r("C10b heading half (Heading 1; its mark deleted, written by docx4j)") + "</w:p>");
		b.append("<w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr>" + r("C10b body half (Normal, right aligned)") + "</w:p>");
		// bookmarks in removed content
		b.append("<w:p>" + r("C11 before ") + "<w:bookmarkStart w:id=\"601\" w:name=\"c11_deleted\"/>" + r("BOOKMARKED")
				+ "<w:bookmarkEnd w:id=\"601\"/>" + r(" after: the bookmarked word is deleted.") + "</w:p>");
		b.append(p("C11b text is inserted at the end of this paragraph, and a bookmark put on it: "));
		// endnotes
		b.append("<w:p>" + r("C12 the endnote reference here is deleted: ") + endnoteRef(1) + r(" (after the reference).") + "</w:p>");
		b.append(p("C12b an endnote is inserted at the end of this paragraph: "));
		// tables
		b.append(p("C13 the table after this paragraph is deleted whole."));
		b.append(table("C13", 2, 2));
		b.append(p("C14 a column is added to the table after this paragraph."));
		b.append(table("C14", 2, 2));
		b.append(p("C15 move one."));
		b.append(p("C15 move two."));
		b.append(p("C15 move three: these three paragraphs are moved into the cell below."));
		b.append(table("C15", 1, 1));
		// run holders
		b.append(p("C16 a hyperlink is inserted at the end of this paragraph: "));
		b.append(p("C16b a content control is inserted at the end of this paragraph: "));
		// formatting
		b.append(p("C17 this whole paragraph, its mark included, is made bold."));
		// a comment
		b.append("<w:p><w:commentRangeStart w:id=\"0\"/>" + r("C18 commented words") + "<w:commentRangeEnd w:id=\"0\"/>"
				+ "<w:r><w:commentReference w:id=\"0\"/></w:r>" + r(": the comment's own text is edited.") + "</w:p>");
		b.append("<w:p>" + r("C21 the endnote of this reference is edited: ") + endnoteRef(2) + "</w:p>");
		b.append(p("C22 a text box anchored here is made untracked, then its text edited tracked."));
		b.append(p("C23 the second row of the table after this paragraph is indented (row property exceptions)."));
		b.append(table("C23", 2, 2));
		b.append(p("C25 a table is inserted in place of the next paragraph."));
		b.append(p("C25 placeholder."));
		// section A ends here; C06 deletes this break
		b.append("<w:p><w:pPr>" + sectPr(2880, headerId, footerId) + "</w:pPr>"
				+ r("C06 last paragraph of section A (left margin 2in): its mark, the section break, is deleted.") + "</w:p>");
		b.append(p("C06 first paragraph of section B (left margin 1.5in)."));
		// section B ends here; C07 changes its margin
		b.append("<w:p><w:pPr>" + sectPr(2160, null, null) + "</w:pPr>"
				+ r("C07 last paragraph of section B: section B's left margin is changed to 0.5in.") + "</w:p>");
		b.append(p("C07 first paragraph of section C (left margin 1in)."));
		b.append(p("End of the probe."));
		b.append(sectPr(1440, null, null));
		b.append("</w:body></w:document>");
		mdp.setJaxbElement((Document) XmlUtils.unwrap(XmlUtils.unmarshalString(b.toString())));

		// C05's picture, between its two runs
		BinaryPartAbstractImage image = BinaryPartAbstractImage.createImagePart(pkg, png);
		org.docx4j.dml.wordprocessingDrawing.Inline inline = image.createImageInline("c05", "C05 picture", 1, 2, false);
		Drawing drawing = new Drawing();
		drawing.getAnchorOrInline().add(inline);
		R picture = new R();
		picture.getContent().add(drawing);
		for (Object o : mdp.getContent()) {
			if (o instanceof P && textOf((P) o).startsWith("C05")) {
				((P) o).getContent().add(1, picture);
				picture.setParent(o);
			}
		}
		return pkg;
	}

	private static String textOf(P p) {
		StringBuilder sb = new StringBuilder();
		for (Object o : p.getContent()) {
			if (o instanceof R) {
				for (Object c : ((R) o).getContent()) {
					Object v = XmlUtils.unwrap(c);
					if (v instanceof org.docx4j.wml.Text) sb.append(((org.docx4j.wml.Text) v).getValue());
				}
			}
		}
		return sb.toString();
	}

	static String r(String text) {
		return "<w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	static String p(String text) {
		return "<w:p>" + r(text) + "</w:p>";
	}

	static String field(String instr, String result) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\">" + instr + "</w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + r(result)
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	static String endnoteRef(int id) {
		return "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:endnoteReference w:id=\"" + id + "\"/></w:r>";
	}

	static String tblPr() {
		String border = "w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"";
		return "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>"
				+ "<w:top " + border + "/><w:left " + border + "/><w:bottom " + border + "/><w:right " + border + "/>"
				+ "<w:insideH " + border + "/><w:insideV " + border + "/></w:tblBorders>"
				+ "<w:tblLayout w:type=\"fixed\"/></w:tblPr>";
	}

	/** A table of the given shape, each cell's text "tag rNcM". */
	static String table(String tag, int rows, int cols) {
		StringBuilder t = new StringBuilder("<w:tbl>" + tblPr() + "<w:tblGrid>");
		for (int c = 0; c < cols; c++) t.append("<w:gridCol w:w=\"2000\"/>");
		t.append("</w:tblGrid>");
		for (int r = 1; r <= rows; r++) {
			t.append("<w:tr>");
			for (int c = 1; c <= cols; c++) {
				t.append("<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>" + p(tag + " r" + r + "c" + c) + "</w:tc>");
			}
			t.append("</w:tr>");
		}
		return t.append("</w:tbl>").toString();
	}

	/** A4 portrait, 1in margins but the left one, with the header and footer where given. */
	static String sectPr(int left, String headerId, String footerId) {
		return "<w:sectPr>"
				+ (headerId == null ? "" : "<w:headerReference w:type=\"default\" r:id=\"" + headerId + "\"/>")
				+ (footerId == null ? "" : "<w:footerReference w:type=\"default\" r:id=\"" + footerId + "\"/>")
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"" + left + "\""
				+ " w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>";
	}

	static byte[] png(int w, int h, int rgb) throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) img.setRGB(x, y, rgb);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", bytes);
		return bytes.toByteArray();
	}
}
