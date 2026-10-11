/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.fidelity.corpus;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Style;

/**
 * Where Word draws the Hyperlink character style and where it suppresses it.  docx4j's
 * FieldHyperlinkStyle drops the style from a hyperlinked TOC's entries, measured on the corpus
 * goldens (docx4j 455463622); a Word MVP says hyperlinked cross-references suppress it too, and
 * no corpus document has one.  This probe asks Word about both, and about the edges of the TOC
 * rule: a run outside a w:hyperlink, another character style, direct formatting, a style based
 * on Hyperlink, FollowedHyperlink, the older nested HYPERLINK fields, a TOC without {@code \h}.
 *
 * <p>Each case is a run whose text begins with its label (H01, T03 ...).  The Hyperlink style is
 * made unmistakable: colour 467886 (the 2023 theme's hyperlink), single underline, bold, 14pt,
 * against body text of black, regular, 11pt; so Word's PDF, read with {@code tools/office_colours.py} (text colour, rules,
 * font, size), shows whether the style was drawn and in which respects.  Every field is locked
 * ({@code w:fldLock}) so that Word draws the result as written rather than regenerating it.</p>
 *
 * <p>Writes {@code hl-fields.docx} and {@code cases.tsv} into the directory given.</p>
 */
public final class HyperlinkStyleProbes {

	private static final String NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String PROBE = "hl-fields.docx";
	private static final List<String> CASES = new ArrayList<>();

	private HyperlinkStyleProbes() {
	}

	private static void kase(String label, String context, String rPr, String expect) {
		CASES.add(PROBE + "\t" + label + "\t" + context + "\t" + rPr + "\t" + expect);
	}

	private static String r(String rPr, String text) {
		return "<w:r><w:rPr>" + rPr + "</w:rPr><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static String plain(String text) {
		return "<w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static String begin() {
		return "<w:r><w:fldChar w:fldCharType=\"begin\" w:fldLock=\"true\"/></w:r>";
	}

	private static String instr(String instr) {
		return "<w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r>";
	}

	private static String separate() {
		return "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>";
	}

	private static String end() {
		return "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	/** a complex field in one paragraph, its result one run */
	private static String field(String instr, String resultRPr, String resultText) {
		return begin() + instr(instr) + separate() + r(resultRPr, resultText) + end();
	}

	private static String p(String content) {
		return "<w:p><w:pPr><w:spacing w:after=\"160\"/></w:pPr>" + content + "</w:p>";
	}

	private static String toc(String content) {
		return "<w:p><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>" + content + "</w:p>";
	}

	private static String link(String anchor, String rPr, String text) {
		return "<w:hyperlink w:anchor=\"" + anchor + "\" w:history=\"1\">" + r(rPr, text) + "</w:hyperlink>";
	}

	private static final String HL = "<w:rStyle w:val=\"Hyperlink\"/>";

	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		dir.mkdirs();

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		List<Style> styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle();
		for (String s : new String[] {
				"<w:style" + NS + " w:type=\"character\" w:styleId=\"Hyperlink\"><w:name w:val=\"Hyperlink\"/>"
						+ "<w:rPr><w:b/><w:color w:val=\"467886\" w:themeColor=\"hyperlink\"/><w:sz w:val=\"28\"/><w:u w:val=\"single\"/></w:rPr></w:style>",
				"<w:style" + NS + " w:type=\"character\" w:styleId=\"FollowedHyperlink\"><w:name w:val=\"FollowedHyperlink\"/>"
						+ "<w:rPr><w:b/><w:color w:val=\"96607D\" w:themeColor=\"followedHyperlink\"/><w:sz w:val=\"28\"/><w:u w:val=\"single\"/></w:rPr></w:style>",
				"<w:style" + NS + " w:type=\"character\" w:customStyle=\"1\" w:styleId=\"HyperlinkChild\"><w:name w:val=\"Hyperlink Child\"/>"
						+ "<w:basedOn w:val=\"Hyperlink\"/><w:rPr><w:i/></w:rPr></w:style>",
				"<w:style" + NS + " w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Marked\"><w:name w:val=\"Marked\"/>"
						+ "<w:rPr><w:b/><w:color w:val=\"C00000\"/><w:sz w:val=\"28\"/><w:u w:val=\"double\"/></w:rPr></w:style>",
				"<w:style" + NS + " w:type=\"paragraph\" w:styleId=\"TOC1\"><w:name w:val=\"toc 1\"/><w:basedOn w:val=\"Normal\"/>"
						+ "<w:next w:val=\"Normal\"/><w:uiPriority w:val=\"39\"/><w:pPr><w:spacing w:after=\"160\"/></w:pPr></w:style>",
		}) {
			Style style = (Style) XmlUtils.unmarshalString(s);
			styles.removeIf(existing -> style.getStyleId().equals(existing.getStyleId()));
			styles.add(style);
		}

		StringBuilder b = new StringBuilder();
		b.append(p(plain("Hyperlink style probe (docx4j FieldHyperlinkStyle follow-up, 2026-10-12).  The Hyperlink style is"
				+ " 467886, underlined, bold, 14pt; body text is black, regular, 11pt; Marked is C00000, double"
				+ " underline, bold, 14pt.  Every field is locked.")));
		b.append("<w:p><w:pPr><w:spacing w:after=\"160\"/></w:pPr><w:bookmarkStart w:id=\"1\" w:name=\"_Ref_probe\"/>"
				+ plain("BOOKMARKED target paragraph") + "<w:bookmarkEnd w:id=\"1\"/></w:p>");

		// controls, outside any field
		b.append(p(link("_Ref_probe", HL, "H01 w:hyperlink, Hyperlink style, no field")));
		kase("H01", "w:hyperlink, no field", "rStyle Hyperlink", "drawn (control)");
		b.append(p(r(HL, "H02 Hyperlink style, no hyperlink, no field")));
		kase("H02", "plain run, no field", "rStyle Hyperlink", "drawn (control)");
		b.append(p(r("<w:rStyle w:val=\"Marked\"/>", "M01 Marked style, no field")));
		kase("M01", "plain run, no field", "rStyle Marked", "drawn (control)");

		// cross-references and other fields, each result a run with the style
		String[][] xrefs = {
				{ "H03", "REF _Ref_probe \\h", HL, "REF \\h" },
				{ "H04", "REF _Ref_probe", HL, "REF, no \\h" },
				{ "H05", "PAGEREF _Ref_probe \\h", HL, "PAGEREF \\h" },
				{ "H06", "PAGEREF _Ref_probe", HL, "PAGEREF, no \\h" },
				{ "H07", "HYPERLINK \\l \"_Ref_probe\"", HL, "HYPERLINK \\l field" },
				{ "H08", "REF _Ref_probe \\h", "<w:rStyle w:val=\"Marked\"/>", "REF \\h, Marked style" },
				{ "H09", "REF _Ref_probe \\h", "<w:color w:val=\"C00000\"/><w:u w:val=\"double\"/>", "REF \\h, direct colour and underline" },
				{ "H10", "REF _Ref_probe \\h \\* MERGEFORMAT", HL, "REF \\h \\* MERGEFORMAT" },
		};
		for (String[] x : xrefs) {
			b.append(p(plain("Field: ") + field(x[1], x[2], x[0] + " " + x[3] + " result")));
			kase(x[0], x[1], x[2].replace("<w:rStyle w:val=\"", "rStyle ").replace("\"/>", ""), "the question");
		}

		// a hyperlinked TOC over several paragraphs
		b.append(p(plain("A TOC with \\h follows.")));
		b.append("<w:p><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>" + begin() + instr("TOC \\o \"1-3\" \\h \\z \\u") + separate()
				+ link("_Ref_probe", HL, "T01 TOC \\h entry, w:hyperlink, Hyperlink style") + "</w:p>");
		kase("T01", "TOC \\h, w:hyperlink", "rStyle Hyperlink", "suppressed (corpus)");
		b.append(toc(r(HL, "T02 TOC \\h entry, no w:hyperlink, Hyperlink style")));
		kase("T02", "TOC \\h, no w:hyperlink", "rStyle Hyperlink", "the question");
		b.append(toc(link("_Ref_probe", "<w:rStyle w:val=\"Marked\"/>", "T03 TOC \\h entry, w:hyperlink, Marked style")));
		kase("T03", "TOC \\h, w:hyperlink", "rStyle Marked", "the question");
		b.append(toc(link("_Ref_probe", "<w:color w:val=\"C00000\"/><w:u w:val=\"double\"/>",
				"T04 TOC \\h entry, w:hyperlink, direct colour and underline")));
		kase("T04", "TOC \\h, w:hyperlink", "direct color C00000, u double", "the question");
		b.append(toc(link("_Ref_probe", "<w:rStyle w:val=\"HyperlinkChild\"/>", "T05 TOC \\h entry, w:hyperlink, style based on Hyperlink, italic")));
		kase("T05", "TOC \\h, w:hyperlink", "rStyle HyperlinkChild (basedOn Hyperlink, i)", "the question");
		b.append(toc(link("_Ref_probe", "<w:rStyle w:val=\"FollowedHyperlink\"/>", "T06 TOC \\h entry, w:hyperlink, FollowedHyperlink style")));
		kase("T06", "TOC \\h, w:hyperlink", "rStyle FollowedHyperlink", "the question");
		b.append(toc(begin() + instr("HYPERLINK \\l \"_Ref_probe\"") + separate()
				+ r(HL, "T07 TOC \\h entry, nested HYPERLINK field, Hyperlink style") + end()));
		kase("T07", "TOC \\h, nested HYPERLINK \\l field", "rStyle Hyperlink", "the question");
		b.append(toc(link("_Ref_probe", "", "T08 TOC \\h entry, page number follows: ")
				+ "<w:hyperlink w:anchor=\"_Ref_probe\" w:history=\"1\">" + begin() + instr("PAGEREF _Ref_probe \\h") + separate()
				+ r(HL, "T08P") + end() + "</w:hyperlink>" + end()));
		kase("T08P", "TOC \\h, PAGEREF \\h in w:hyperlink (the page number)", "rStyle Hyperlink", "the question");

		// a TOC without \h
		b.append(p(plain("A TOC without \\h follows.")));
		b.append("<w:p><w:pPr><w:pStyle w:val=\"TOC1\"/></w:pPr>" + begin() + instr("TOC \\o \"1-3\"") + separate()
				+ r(HL, "U01 TOC without \\h, no w:hyperlink, Hyperlink style") + "</w:p>");
		kase("U01", "TOC without \\h", "rStyle Hyperlink", "the question (MVP: an unlinked TOC keeps the style)");
		b.append(toc(link("_Ref_probe", HL, "U02 TOC without \\h, w:hyperlink, Hyperlink style") + end()));
		kase("U02", "TOC without \\h, w:hyperlink", "rStyle Hyperlink", "the question");

		b.append(p(plain("End.")));

		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document" + NS + "><w:body>" + b + "</w:body></w:document>"));
		pkg.save(new File(dir, PROBE));
		try (PrintWriter out = new PrintWriter(new File(dir, "cases.tsv"), StandardCharsets.UTF_8)) {
			out.println("probe\tlabel\tcontext\trPr\texpect");
			for (String c : CASES) out.println(c);
		}
		System.out.println("wrote " + CASES.size() + " cases into " + dir);
	}
}
