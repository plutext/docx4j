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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TreeSet;

import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.text.Inspect;
import org.docx4j.model.text.InspectFinding;
import org.docx4j.model.text.Inspection;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-051 phase 1, stage B: the two readers of {@code word} mode's record (the text model's
 * {@code mce.branches_differ} and the inspector's {@code mce.branch}, each at the address
 * {@code preserve} mode gives), an element directly inside another's branch, and the
 * anonymiser's CLI loading in {@code preserve} mode.
 */
public class McBlockLevelReadersTest {

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	/** A w14 Choice (understood) and a Fallback, each one paragraph of different text. */
	private static String ac(String tag) {
		return "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p(tag + "_W14") + "</mc:Choice>"
				+ "<mc:Fallback>" + p(tag + "_FB") + "</mc:Fallback></mc:AlternateContent>";
	}

	private static final String TBL_PROPS = "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>";

	private static final String TBL = "<w:tbl>" + TBL_PROPS + "<w:tr><w:tc>" + p("cell") + ac("CELL") + "</w:tc></w:tr></w:tbl>";

	/** A row-level element (each branch a row) among a table's rows, and a cell-level one (each branch a cell) among a row's cells (CR-051 phase 2). */
	private static String acOf(String tag, String open, String close) {
		return "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + open + p(tag + "_W14") + close + "</mc:Choice>"
				+ "<mc:Fallback>" + open + p(tag + "_FB") + close + "</mc:Fallback></mc:AlternateContent>";
	}

	private static final String TBL_ROWS_AND_CELLS = "<w:tbl>" + TBL_PROPS
			+ "<w:tr><w:tc>" + p("r1") + "</w:tc></w:tr>" + acOf("ROW", "<w:tr><w:tc>", "</w:tc></w:tr>")
			+ "<w:tr><w:tc>" + p("c1") + "</w:tc>" + acOf("TRC", "<w:tc>", "</w:tc>") + "</w:tr></w:tbl>";

	private static String textBox(String blocks) {
		return "<w:p><w:r><w:drawing><wp:inline><wp:extent cx=\"1270000\" cy=\"635000\"/><wp:docPr id=\"1\" name=\"Text Box 1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr txBox=\"1\"/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1270000\" cy=\"635000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></wps:spPr>"
				+ "<wps:txbx><w:txbxContent>" + blocks + "</w:txbxContent></wps:txbx>"
				+ "<wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
	}

	/** Elements in the body, a cell, a block content control, a text box, a header, a footnote and a comment, each referenced from the body. */
	private static McProbeDocx probe() {
		String body = p("before") + ac("BODY") + TBL + TBL_ROWS_AND_CELLS
				+ "<w:sdt><w:sdtPr><w:id w:val=\"1\"/></w:sdtPr><w:sdtContent>" + p("sdt") + ac("SDT") + "</w:sdtContent></w:sdt>"
				+ textBox(p("box") + ac("TXBX"))
				+ "<w:p><w:r><w:t>note</w:t></w:r><w:r><w:footnoteReference w:id=\"1\"/></w:r></w:p>"
				+ "<w:p><w:commentRangeStart w:id=\"1\"/><w:r><w:t>commented</w:t></w:r><w:commentRangeEnd w:id=\"1\"/><w:r><w:commentReference w:id=\"1\"/></w:r></w:p>"
				+ "<w:sectPr><w:headerReference w:type=\"default\" r:id=\"rId7\"/><w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\"/></w:sectPr>";
		return McProbeDocx.body(body)
				.part("header1.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header", "rId7", "w:hdr", p("head") + ac("HDR"))
				.part("footnotes.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes", "rId9", "w:footnotes",
						"<w:footnote w:id=\"1\">" + p("n1") + ac("FN") + "</w:footnote>")
				.part("comments.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments", "rId8", "w:comments",
						"<w:comment w:id=\"1\" w:author=\"a\">" + ac("CM") + "</w:comment>");
	}

	private static TreeSet<String> differWarnings(WordprocessingMLPackage pkg) {
		TreeSet<String> out = new TreeSet<String>();
		TextOptions options = new TextOptions().include(TextOptions.Include.HEADERS).include(TextOptions.Include.COMMENTS);
		for (TextWarning w : Texts.of(pkg, options).warnings()) {
			if (TextWarning.MCE_BRANCHES_DIFFER.equals(w.getKind())) out.add(w.getAddress() + " " + w.getBranch());
		}
		return out;
	}

	/** The text model raises mce.branches_differ from the record in word mode, at the address preserve mode gives. */
	@Test
	public void textModelWarnsFromTheRecord() throws Exception {
		TreeSet<String> word = differWarnings(probe().load(McBlockLevel.WORD));
		TreeSet<String> preserve = differWarnings(probe().load(McBlockLevel.PRESERVE));
		assertTrue(word.toString(), word.contains("body/1 mc:Choice Requires=\"w14\""));
		assertTrue("the cell's, the control's, the text box's, the header's, the note's and the comment's too: " + word, word.size() >= 9);
		assertTrue("the row-level and the cell-level elements' (CR-051 phase 2): " + word, word.contains("body/3/1 mc:Choice Requires=\"w14\"") && word.contains("body/3/2/1 mc:Choice Requires=\"w14\""));
		assertEquals("the same warnings in both modes", preserve, word);
	}

	private static TreeSet<String> branchFindings(WordprocessingMLPackage pkg) {
		TreeSet<String> out = new TreeSet<String>();
		Inspection i = Inspect.of(pkg);
		for (InspectFinding f : i.findings()) {
			if (InspectFinding.MCE_BRANCH.equals(f.getKind())) {
				out.add(f.getAddress() + " | " + f.getLocation().getPart() + " " + f.getLocation().getPath() + " | " + f.getExcerpt());
			}
		}
		return out;
	}

	/**
	 * The inspector makes an mce.branch finding for each unselected branch, with the same location and
	 * excerpt in both modes.  Such a finding carries no extraction address in either mode (its content is
	 * never extracted: CR-050's NEVER), so its location, the part and the path, is what the two modes
	 * must agree on.
	 */
	@Test
	public void inspectorFindsTheRecordedBranchesWherePreserveModeHasThem() throws Exception {
		TreeSet<String> preserve = branchFindings(probe().load(McBlockLevel.PRESERVE));
		TreeSet<String> word = branchFindings(probe().load(McBlockLevel.WORD));
		assertTrue(preserve.toString(), preserve.size() >= 9);
		for (String tag : new String[] { "BODY_FB", "CELL_FB", "ROW_FB", "TRC_FB", "SDT_FB", "TXBX_FB", "HDR_FB", "FN_FB", "CM_FB" }) {
			boolean found = false;
			for (String s : preserve) if (s.endsWith(tag)) found = true;
			assertTrue(tag + " in " + preserve, found);
		}
		assertEquals(preserve, word);
		// and at a known location: the body's element, its Fallback
		boolean body = false;
		for (String s : word) if (s.contains("/word/document.xml /w:document[1]/w:body[1]/mc:AlternateContent[1]/mc:Fallback[1] | BODY_FB")) body = true;
		assertTrue(word.toString(), body);
	}

	/** An element directly inside another's branch is resolved once the outer one is: both spliced, nothing left whole. */
	@Test
	public void elementDirectlyInABranchIsResolvedToo() throws Exception {
		String inner = ac("INNER");
		String outer = "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p("OUTER_W14") + inner + "</mc:Choice>"
				+ "<mc:Fallback>" + p("OUTER_FB") + "</mc:Fallback></mc:AlternateContent>";
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + outer + p("after")).load(McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Object> content = mdp.getContent();
		assertEquals(content.toString(), 4, content.size());
		assertTrue(XmlUtils.unwrap(content.get(1)) instanceof P);
		assertTrue(XmlUtils.unwrap(content.get(2)) instanceof P);
		String xml = XmlUtils.marshaltoString(mdp.getJaxbElement(), true, false);
		assertTrue(xml, xml.contains("OUTER_W14") && xml.contains("INNER_W14"));
		assertFalse(xml, xml.contains("mc:AlternateContent"));
		assertEquals(2, mdp.getResolvedAlternateContent().size());
		assertEquals("w:body", mdp.getResolvedAlternateContent().get(1).getContainerName());
		assertEquals(2, mdp.getResolvedAlternateContent().get(1).getIndex());
		// in a run, no: a paragraph-level element stays whole, and so does one directly in its branch
		String para = "<w:p><mc:AlternateContent><mc:Choice Requires=\"w14\"><w:r><w:t>R_W14</w:t></w:r>"
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:r><w:t>RI_W14</w:t></w:r></mc:Choice><mc:Fallback><w:r><w:t>RI_FB</w:t></w:r></mc:Fallback></mc:AlternateContent>"
				+ "</mc:Choice><mc:Fallback><w:r><w:t>R_FB</w:t></w:r></mc:Fallback></mc:AlternateContent></w:p>";
		WordprocessingMLPackage runs = McProbeDocx.body(para).load(McBlockLevel.WORD);
		assertEquals(0, runs.getMainDocumentPart().getResolvedAlternateContent().size());
		P first = (P) XmlUtils.unwrap(runs.getMainDocumentPart().getContent().get(0));
		assertTrue(XmlUtils.unwrap(first.getContent().get(0)) instanceof AlternateContent);
	}
}
