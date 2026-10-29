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
package org.docx4j.model.fields.probe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTFootnotes;

/**
 * Builds the CR-067 phase 1 probe: one docx holding every tier-1 field with a stale cached result,
 * for Word to update (Ctrl+A, F9) and save, and the README that says what to do and what each case
 * should show. Not a test: {@code java FieldProbeBuilder <outDir>}.
 *
 * <p>Every field is in the complex form Word writes, with the cached result {@code OLD} (or, for the
 * user fields, {@code OLDUSER}), so a case Word updates shows a value and a case it leaves alone keeps
 * OLD. The predictions are in CR-067 §11 before the run.</p>
 */
public class FieldProbeBuilder {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	static final List<String[]> CASES = new ArrayList<String[]>();
	static void c(String token, String instr, String expect) { CASES.add(new String[] { token, instr, expect }); }
	static {
		c("P01_CREATEDATE", "CREATEDATE", "the creation date and time, 3 February 2024 04:05:06 UTC, in Word's default date-time format for the locale (and the time shifted to the machine's zone)");
		c("P02_CREATEDATE_SW", "CREATEDATE \\@ \"yyyy-MM-dd HH:mm\"", "2024-02-03 04:05 (UTC; the hour shifts with the machine's zone)");
		c("P03_SAVEDATE", "SAVEDATE", "the last-saved date, 7 June 2025 08:09:10 UTC, in the default format; after Word's own save it may become the save's moment");
		c("P04_SAVEDATE_SW", "SAVEDATE \\@ \"d MMMM yyyy\"", "7 June 2025 (or the day of Word's save)");
		c("P05_REVNUM", "REVNUM", "7 (Word may add one on its save: 8)");
		c("P06_INFO_TITLE", "INFO Title", "Probe Title");
		c("P07_INFO_AUTHOR", "INFO Author", "Probe Author");
		c("P08_INFO_NOARG", "INFO", "Word's error text for an INFO with no name (unmeasured)");
		c("P09_INFO_NOPE", "INFO Nope", "Word's error text for an unknown name (unmeasured)");
		c("P10_SECTION", "SECTION", "1");
		c("P11_QUOTE", "QUOTE \"hello world\"", "hello world");
		c("P12_QUOTE_NOARG", "QUOTE", "empty, or Word's error text (unmeasured)");
		c("P13_REF", "REF bm1", "Bookmarked text (the bookmark's text, in its formatting)");
		c("P14_REF_UPPER", "REF bm1 \\* Upper", "BOOKMARKED TEXT");
		c("P15_REF_NOPE", "REF nope", "Error! Reference source not found.");
		c("P16_REF_H", "REF bm1 \\h", "Bookmarked text, as a hyperlink to the bookmark");
		c("P17_NOTEREF", "NOTEREF bmNote", "1 (the footnote's number; superscript only with \\f)");
		c("P18_NOTEREF_F", "NOTEREF bmNote \\f", "1 in the footnote reference style (superscript)");
		c("P19_NOTEREF_NOPE", "NOTEREF nope", "Error! Bookmark not defined.");
		c("P20_USERNAME", "USERNAME", "the Word user's name (File > Options > General); what it shows when that is blank is unmeasured");
		c("P21_USERINITIALS", "USERINITIALS", "the user's initials");
		c("P22_USERADDRESS", "USERADDRESS", "the user's mailing address (Options > Advanced > General), often empty");
		c("P23_KEYWORDS", "KEYWORDS", "alpha, beta (measured for #277; here as a control)");
		// section 2
		c("P24_SECTION2", "SECTION", "2 (this case is after the section break)");
		c("P25_CREATEDATE_SIMPLE", "CREATEDATE \\@ \"yyyy-MM-dd\"", "2024-02-03, as a simple field (w:fldSimple): Word updates it as it does the complex form");
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		ObjectFactory dc = new ObjectFactory();
		core.setTitle(dc.createTitle(literal("Probe Title")));
		core.setCreator(literal("Probe Author"));
		core.setSubject(literal("Probe Subject"));
		core.setDescription(dc.createDescription(literal("Probe Comments")));
		core.setKeywords("alpha, beta");
		core.setLastModifiedBy("Probe Saver");
		core.setRevision("7");
		core.setCreated(dated("2024-02-03T04:05:06Z"));
		core.setModified(dated("2025-06-07T08:09:10Z"));

		// the footnote NOTEREF points at
		FootnotesPart fnp = new FootnotesPart();
		fnp.setJaxbElement((CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p><w:pPr><w:pStyle w:val=\"FootnoteText\"/></w:pPr><w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteRef/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> The probe's footnote.</w:t></w:r></w:p></w:footnote></w:footnotes>")));
		mdp.addTargetPart(fnp);

		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>CR-067 phase 1 field probe</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">Select all, press F9, then save as described in README.txt. Each case: its token, then the field. A field Word leaves alone still says OLD.</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">A bookmark: </w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"bm1\"/><w:r><w:rPr><w:b/></w:rPr><w:t>Bookmarked text</w:t></w:r><w:bookmarkEnd w:id=\"1\"/>"
				+ "<w:r><w:t xml:space=\"preserve\">. A footnote reference: </w:t></w:r><w:bookmarkStart w:id=\"2\" w:name=\"bmNote\"/><w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteReference w:id=\"1\"/></w:r><w:bookmarkEnd w:id=\"2\"/><w:r><w:t>.</w:t></w:r></w:p>");

		StringBuilder readme = new StringBuilder();
		readme.append("CR-067 phase 1: Word 365 update of the tier-1 fields (one file, about 5 minutes)\n\n");
		readme.append("1. Open field-probe-r1.docx normally (not Open and Repair). Note the Word build (File > Account > About Word).\n");
		readme.append("2. Before updating, note what cases P20 to P22 show (they may already show the user, since Word evaluates them on open).\n");
		readme.append("3. Ctrl+A, then F9 to update every field. If Word asks about updating, say yes.\n");
		readme.append("4. For each case below, compare what the field shows with the expectation; note any difference (a word is enough).\n");
		readme.append("5. Save As field-probe-r1-word365.docx (keep the .docx format) beside this README, then also Save As PDF field-probe-r1-word365.pdf.\n");
		readme.append("6. Also useful: File > Options > General, the user name and initials Word has, so the USER* cases can be read.\n\n");
		readme.append("Cases (token: field -> expected after F9):\n");
		int n = 0;
		for (String[] c : CASES) {
			n++;
			boolean simple = c[0].contains("SIMPLE");
			if (c[0].equals("P24_SECTION2")) {
				// the section break: the first section's sectPr on a paragraph of its own
				add(mdp, "<w:p " + W + "><w:pPr><w:sectPr><w:type w:val=\"nextPage\"/><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr></w:pPr></w:p>");
			}
			add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t xml:space=\"preserve\">Case " + n + ": " + c[0] + " { " + c[1].replace("\\", "\\") + " }</w:t></w:r></w:p>");
			add(mdp, simple ? simpleField(c[0], c[1]) : complexField(c[0], c[1], c[0].startsWith("P2") && c[0].contains("USER") ? "OLDUSER" : "OLD"));
			readme.append(String.format("%2d. %s: { %s } -> %s%n", n, c[0], c[1], c[2]));
		}
		readme.append("\nBookmark bm1 holds the bold text 'Bookmarked text'; bookmark bmNote holds the footnote reference (footnote 1).\n");
		readme.append("Core properties: title Probe Title, author Probe Author, subject Probe Subject, comments Probe Comments, keywords alpha, beta,\n");
		readme.append("last saved by Probe Saver, revision 7, created 2024-02-03T04:05:06Z, modified 2025-06-07T08:09:10Z.\n");

		File docx = new File(out, "field-probe-r1.docx");
		pkg.save(docx);
		try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(out, "README.txt")), StandardCharsets.UTF_8)) {
			w.write(readme.toString());
		}
		System.out.println("wrote " + docx + " (" + CASES.size() + " cases) and README.txt");
	}

	/** A dcterms date, typed W3CDTF as Word writes it. */
	static SimpleLiteral dated(String w3cdtf) {
		org.docx4j.docProps.core.dc.terms.W3CDTF w = new org.docx4j.docProps.core.dc.terms.W3CDTF();
		w.getContent().add(w3cdtf);
		return w;
	}

	static SimpleLiteral literal(String value) {
		SimpleLiteral l = new SimpleLiteral();
		l.getContent().add(value);
		return l;
	}

	static void add(MainDocumentPart mdp, String xml) throws Exception {
		mdp.getContent().add(XmlUtils.unmarshalString(xml));
	}

	static String complexField(String token, String instr, String cached) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + xmlEscape(instr) + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>" + cached + "</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
	}

	static String simpleField(String token, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" " + xmlEscape(instr) + " \"><w:r><w:t>OLD</w:t></w:r></w:fldSimple></w:p>";
	}

	static String xmlEscape(String s) {
		return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
	}
}
