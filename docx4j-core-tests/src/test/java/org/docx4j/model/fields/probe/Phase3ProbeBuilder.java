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

import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.W;
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.add;
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.field;
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.instrText;
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.xmlEscape;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.SectPr;

/**
 * Builds the CR-067 phase 3 probes (D6: measured in Word before coded), four files for one Word session:
 * <ul>
 * <li>{@code phase3-probe-r1.docx} (Ctrl+A, F9, save): XE marks over three pages and six INDEX fields; LISTNUM,
 * AUTONUM, AUTONUMLGL and AUTONUMOUT; FILENAME, TEMPLATE, FILESIZE; SYMBOL;</li>
 * <li>{@code numwords-probe-r1.docx} (F9, save): NUMWORDS and NUMCHARS over a controlled text, with a header and a
 * footnote, against the DOCPROPERTY statistics and the saved app.xml;</li>
 * <li>{@code address-probe-r1.docx} + {@code address-data-r1.docx} (a merge): ADDRESSBLOCK and GREETINGLINE over
 * four records with gaps.</li>
 * </ul>
 * Word's saved files are the oracle; the README carries every prediction. Not a test:
 * {@code java Phase3ProbeBuilder <outDir>}. Nested fields in an instruction are written between {@code «} and {@code »}.
 */
public class Phase3ProbeBuilder {

	/** token, instruction, prediction; a one-element row is a group heading. */
	static final List<String[]> CASES = new ArrayList<String[]>();
	static void c(String token, String instr, String expect) { CASES.add(new String[] { token, instr, expect }); }
	static void group(String title) { CASES.add(new String[] { title }); }

	static String heading(String text) {
		return "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t xml:space=\"preserve\">" + xmlEscape(text) + "</w:t></w:r></w:p>";
	}
	static String para(String text) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + xmlEscape(text) + "</w:t></w:r></w:p>";
	}
	static String pageBreak() {
		return "<w:p " + W + "><w:r><w:br w:type=\"page\"/></w:r></w:p>";
	}
	/** A field with no separate and no result (XE is written so by Word). */
	static String markField(String instr) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>" + instrText(" " + instr + " ") + "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}
	/** "text {XE ...}" in one paragraph: the mark follows its text, as Word places it. */
	static String marked(String text, String xe) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + xmlEscape(text) + "</w:t></w:r>" + markField(xe) + "</w:p>";
	}
	static String tokenField(String token, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + field(instr, "OLD") + "</w:p>";
	}
	static String styledTokenField(String style, String token, String instr, String after) {
		return "<w:p " + W + "><w:pPr><w:pStyle w:val=\"" + style + "\"/></w:pPr><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + field(instr, "OLD")
				+ "<w:r><w:t xml:space=\"preserve\">" + xmlEscape(after) + "</w:t></w:r></w:p>";
	}

	// ---------------------------------------------------------------- the main probe

	/** page, text, XE instruction, what it settles. */
	static final String[][] XE = {
			{ "1", "Apple is on page 1.", "XE \"Apple\"", "Apple, 1 (with pages 2 and 3 below: Apple, 1, 2, 3)" },
			{ "1", "A yellow banana.", "XE \"Banana:Yellow\"", "a subentry: Banana / Yellow, 1 (Index 2 style)" },
			{ "1", "A cherry in lower case.", "XE \"cherry\"", "cherry, 1 sorted with the C entries (case-insensitive order; unmeasured whether shown as typed)" },
			{ "1", "A dog that points to the cat.", "XE \"Dog\" \\t \"See Cat\"", "Dog. See Cat (the \\k separator defaults to '. '; whether the \\t text is italic: unmeasured)" },
			{ "1", "An egg with a bold page number.", "XE \"Egg\" \\b", "Egg, 1 with the 1 bold" },
			{ "1", "A fig with an italic page number.", "XE \"Fig\" \\i", "Fig, 1 with the 1 italic" },
			{ "1", "Ten items.", "XE \"10 items\"", "10 items, 1 (sorted before 2 items if the sort is by character, after it if numeric; unmeasured)" },
			{ "1", "Two items.", "XE \"2 items\"", "2 items, 1" },
			{ "1", "An eclair with an accent.", "XE \"éclair\"", "éclair, 1 sorted among the E entries (unmeasured)" },
			{ "1", "Apple again on page 1.", "XE \"Apple\"", "no second 1 in Apple's page list" },
			{ "2", "Apple on page 2.", "XE \"Apple\"", "Apple, 1, 2, 3" },
			{ "2", "A green banana.", "XE \"Banana:Green\"", "Banana / Green, 2 before Yellow, 1 (subentries sorted)" },
			{ "2", "Grapes start here and run to page 3 (bookmark GrapeRange).", "XE \"Grape\" \\r GrapeRange", "Grape, 2–3 (the \\g range separator defaults to an en dash)" },
			{ "2", "apple in lower case.", "XE \"apple\"", "a separate entry apple, 2, or merged into Apple (unmeasured: Word's index is case-sensitive, I predict separate)" },
			{ "2", "Banana itself.", "XE \"Banana\"", "Banana, 2 on the parent line, then Green, 2 and Yellow, 1 indented" },
			{ "2", "A kiwi of type b.", "XE \"Kiwi\" \\f \"b\"", "only in IDX04 (INDEX \\f \"b\"); absent from the others (unmeasured)" },
			{ "2", "A honeydew water melon (three levels).", "XE \"Melon:Water:Honeydew\"", "Melon / Water / Honeydew, 2 (Index 3 style for the third level)" },
			{ "3", "Apple on page 3.", "XE \"Apple\"", "Apple, 1, 2, 3 (three marks, three pages, listed not ranged)" },
			{ "3", "A nut with a bold italic page number.", "XE \"Nut\" \\b \\i", "Nut, 3 with the 3 bold italic" },
			{ "3", "An orange that points to the apple.", "XE \"Orange\" \\t \"See also Apple\"", "Orange. See also Apple" },
			{ "3", "A dog on page 3, with a page as well as the cross-reference.", "XE \"Dog\"", "Dog, 3. See Cat, or the cross-reference wins (unmeasured)" },
	};

	static {
		group("LISTNUM (the default list NumberDefault, then LegalDefault and OutlineDefault)");
		c("L01_LISTNUM", "LISTNUM", "1)");
		c("L02_LISTNUM", "LISTNUM", "2)");
		c("L03_LEVEL2", "LISTNUM \\l 2", "a)");
		c("L04_LEVEL3", "LISTNUM \\l 3", "i)");
		c("L05_LEVEL1", "LISTNUM \\l 1", "3)");
		c("L06_START", "LISTNUM \\s 10", "10)");
		c("L07_AFTER_START", "LISTNUM", "11)");
		c("L08_TWO_IN_ONE", "LISTNUM", "12) then 13) (two fields in the one paragraph; the second is appended by the builder)");
		c("L09_LEGAL", "LISTNUM LegalDefault", "1.");
		c("L10_LEGAL_L2", "LISTNUM LegalDefault \\l 2", "1.1.");
		c("L11_LEGAL_L2", "LISTNUM LegalDefault \\l 2", "1.2.");
		c("L12_LEGAL_L1", "LISTNUM LegalDefault", "2.");
		c("L13_OUTLINE", "LISTNUM OutlineDefault", "I.");
		c("L14_OUTLINE_L2", "LISTNUM OutlineDefault \\l 2", "A.");
		c("L15_OUTLINE_L3", "LISTNUM OutlineDefault \\l 3", "1.");
		c("L16_NUMBERDEFAULT", "LISTNUM NumberDefault", "14) (the same list as the plain LISTNUM; unmeasured)");
		c("L17_L2_START", "LISTNUM \\l 2 \\s 5", "e)");
		c("L18_L2_NEXT", "LISTNUM \\l 2", "f)");
		c("L19_L1_AFTER", "LISTNUM", "15)");
		group("AUTONUM, AUTONUMLGL, AUTONUMOUT (the paragraph style is named with each case)");
		c("A01_AUTONUM", "AUTONUM", "1.");
		c("A02_AUTONUM", "AUTONUM", "2.");
		c("A03_SEPARATOR", "AUTONUM \\s :", "3:");
		c("A04_AUTONUM", "AUTONUM", "4.");
		c("A05_LGL_H1", "AUTONUMLGL", "1. (Heading 1)");
		c("A06_LGL_H2", "AUTONUMLGL", "1.1. (Heading 2)");
		c("A07_LGL_H2", "AUTONUMLGL", "1.2. (Heading 2)");
		c("A08_LGL_H1", "AUTONUMLGL", "2. (Heading 1)");
		c("A09_LGL_H2_NOPERIOD", "AUTONUMLGL \\e", "2.1 (Heading 2; \\e drops the trailing period)");
		c("A10_LGL_NORMAL", "AUTONUMLGL", "a Normal paragraph: 2.1.1. or 3. or 1. (unmeasured)");
		c("A11_OUT_H1", "AUTONUMOUT", "I. (Heading 1; III. if the AUTONUMLGL headings count; unmeasured)");
		c("A12_OUT_H2", "AUTONUMOUT", "A. (Heading 2)");
		c("A13_OUT_H3", "AUTONUMOUT", "1. (Heading 3)");
		c("A14_OUT_H2", "AUTONUMOUT", "B. (Heading 2)");
		c("A15_AUTONUM_AFTER", "AUTONUM", "5. (continues A04) or 1. (restarted by the headings between); unmeasured");
		group("FILENAME, TEMPLATE, FILESIZE");
		c("N01_FILENAME", "FILENAME", "phase3-probe-r1.docx (the name at F9; the Save As name is not applied until the next update)");
		c("N02_FILENAME_PATH", "FILENAME \\p", "the full path on the VM (a drive letter or a UNC; please leave as Word wrote it)");
		c("N03_FILENAME_UPPER", "FILENAME \\* Upper", "PHASE3-PROBE-R1.DOCX");
		c("N04_TEMPLATE", "TEMPLATE", "Normal.dotm");
		c("N05_TEMPLATE_PATH", "TEMPLATE \\p", "C:\\Users\\<user>\\AppData\\Roaming\\Microsoft\\Templates\\Normal.dotm");
		c("N06_FILESIZE", "FILESIZE", "the file's size in bytes as opened (unmeasured: before or after the save)");
		c("N07_FILESIZE_KB", "FILESIZE \\k", "the size in kilobytes");
		c("N08_FILESIZE_MB", "FILESIZE \\m", "0 (the size in megabytes, rounded)");
		c("N09_DOCPROPERTY_TEMPLATE", "DOCPROPERTY Template", "Normal.dotm (app.xml's Template after the save)");
		c("N10_NUMPAGES", "NUMPAGES", "the page count (a layout field: a cross-check of the XE pages)");
		group("SYMBOL");
		c("S01_DECIMAL", "SYMBOL 169", "© (how Word stores the result: a w:t, or a w:sym; unmeasured)");
		c("S02_HEX", "SYMBOL 0xA9", "©");
		c("S03_SYMBOL_FONT", "SYMBOL 169 \\f \"Symbol\"", "♥ (code 169 in the Symbol font is the heart suit), stored as w:sym w:font=\"Symbol\" w:char=\"F0A9\" (unmeasured)");
		c("S04_WINGDINGS", "SYMBOL 65 \\f Wingdings", "the Wingdings glyph at 65 (a hand), w:sym w:font=\"Wingdings\" w:char=\"F041\" (unmeasured)");
		c("S05_UNICODE", "SYMBOL 8364 \\u", "€");
		c("S06_UNICODE_HEX", "SYMBOL 0x20AC \\u", "€");
		c("S07_SIZE", "SYMBOL 169 \\s 24", "© at 24 points (w:sz 48 on the result run)");
		c("S08_FONT_SIZE_H", "SYMBOL 169 \\f \"Arial\" \\s 12 \\h", "© in Arial 12 (\\h: no line-height change)");
		c("S09_OVER_255", "SYMBOL 300", "unmeasured: the low byte (44, a comma), the Unicode character 300 (Ĭ), or an error");
		c("S10_LETTER", "SYMBOL 65", "A");
		c("S11_ANSI", "SYMBOL 65 \\a", "A");
		c("S12_COURIER", "SYMBOL 0x41 \\u \\f \"Courier New\"", "A in Courier New");
		c("S13_NOT_A_NUMBER", "SYMBOL abc", "an error text (unmeasured), or nothing");
		c("S14_NO_ARGUMENT", "SYMBOL", "an error text (unmeasured), or nothing");
		group("INDEX fields (each regenerates the whole index from the XE marks on pages 1 to 3)");
		c("IDX01_PLAIN", "INDEX", "the entries, one paragraph each in the Index 1/2/3 styles, 'entry, pages' with ', ' separators, no headings, one column (unmeasured: the default column count)");
		c("IDX02_WORD_DEFAULT", "INDEX \\c \"2\" \\z \"1033\"", "the same in two columns (Word's own insertion defaults: continuous section breaks around the index)");
		c("IDX03_SWITCHES", "INDEX \\e \": \" \\h \"A\" \\l \"; \" \\g \" to \" \\c \"1\"", "a letter heading (Index Heading style) before each initial; 'Apple: 1; 2; 3'; 'Grape: 2 to 3'");
		c("IDX04_TYPE_B", "INDEX \\f \"b\"", "Kiwi, 2 only");
		c("IDX05_RUN_IN", "INDEX \\r", "subentries run in: 'Banana, 2: Green, 2; Yellow, 1' (unmeasured: the exact separators)");
		c("IDX06_LETTER_RANGE", "INDEX \\p \"a-c\"", "only the entries starting A to C (Apple, apple, Banana..., cherry); whether 10 items and 2 items appear: unmeasured");
	}

	static void mainProbe(File out, StringBuilder readme) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>CR-067 phase 3 probe</w:t></w:r></w:p>");
		add(mdp, para("Select all, press F9, then save as described in README.txt. Pages 1 to 3 hold index entries (XE marks, hidden); the cases follow; the INDEX fields are at the end. Each case: its token, then the field. A field Word leaves alone still says OLD."));

		readme.append("=== phase3-probe-r1.docx (Ctrl+A, F9, Save As phase3-probe-r1-word365.docx) ===\n\n");
		readme.append("Index entries (XE marks). Each page starts with a PAGE field so the page can be checked. The entries and what each settles:\n");
		String page = null;
		int x = 0;
		for (String[] e : XE) {
			if (!e[0].equals(page)) {
				if (page != null) add(mdp, pageBreak());
				page = e[0];
				add(mdp, heading("Index entries on page " + page));
				add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">PAGE_" + page + " = </w:t></w:r>" + field("PAGE", "OLD") + "</w:p>");
				readme.append("\nPage ").append(page).append(" (PAGE_" + page + " should show " + page + "):\n");
			}
			x++;
			String text = e[1];
			if (e[2].contains("GrapeRange")) {
				add(mdp, "<w:p " + W + "><w:bookmarkStart w:id=\"7\" w:name=\"GrapeRange\"/><w:r><w:t xml:space=\"preserve\">" + xmlEscape(text) + "</w:t></w:r>" + markField(e[2]) + "</w:p>");
			} else {
				add(mdp, marked(text, e[2]));
			}
			readme.append(String.format("  X%02d: { %s } -> %s%n", x, e[2], e[3]));
		}
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">The grape range ends here.</w:t></w:r><w:bookmarkEnd w:id=\"7\"/></w:p>");
		readme.append("  The bookmark GrapeRange runs from X13's paragraph (page 2) to a paragraph on page 3.\n");
		readme.append("\nThe cases (token: field -> predicted). 'unmeasured' marks what the probe exists to settle.\n");

		int n = 0;
		for (String[] c : CASES) {
			if (c.length == 1) {
				add(mdp, heading(c[0]));
				readme.append("\n").append(c[0]).append("\n");
				continue;
			}
			n++;
			String token = c[0];
			if (token.equals("L08_TWO_IN_ONE")) {
				add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + field(c[1], "OLD")
						+ "<w:r><w:t xml:space=\"preserve\"> and </w:t></w:r>" + field(c[1], "OLD") + "</w:p>");
			} else if (token.startsWith("A") && c[2].contains("(Heading ")) {
				String level = c[2].substring(c[2].indexOf("(Heading ") + 9, c[2].indexOf("(Heading ") + 10);
				add(mdp, styledTokenField("Heading" + level, token, c[1], " heading text"));
			} else {
				add(mdp, tokenField(token, c[1]));
			}
			readme.append(String.format("%3d. %s: { %s } -> %s%n", n, token, c[1], c[2]));
		}
		add(mdp, para("End of the probe."));
		File docx = new File(out, "phase3-probe-r1.docx");
		pkg.save(docx);
		System.out.println("wrote " + docx + " (" + XE.length + " XE marks, " + n + " cases)");
	}

	// ---------------------------------------------------------------- the word-count probe

	static final String[] BODY = {
			"The quick brown fox jumps over the lazy dog.",
			"Well-known e-mail a@b.com 1,000 don't café",
	};
	static final String[][] COUNT_CASES = {
			{ "W01_NUMWORDS", "NUMWORDS" },
			{ "W02_NUMCHARS", "NUMCHARS" },
			{ "W03_PROP_WORDS", "DOCPROPERTY Words" },
			{ "W04_PROP_CHARACTERS", "DOCPROPERTY Characters" },
			{ "W05_PROP_CHARS_SPACES", "DOCPROPERTY CharactersWithSpaces" },
			{ "W06_PROP_PARAGRAPHS", "DOCPROPERTY Paragraphs" },
			{ "W07_PROP_LINES", "DOCPROPERTY Lines" },
	};

	static void countProbe(File out, StringBuilder readme) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		FootnotesPart fnp = new FootnotesPart();
		fnp.setJaxbElement((CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p><w:pPr><w:pStyle w:val=\"FootnoteText\"/></w:pPr><w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteRef/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> Footnote text.</w:t></w:r></w:p></w:footnote></w:footnotes>")));
		mdp.addTargetPart(fnp);

		HeaderPart hp = new HeaderPart();
		Relationship rel = mdp.addTargetPart(hp);
		hp.setJaxbElement((Hdr) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:hdr " + W + "><w:p><w:pPr><w:pStyle w:val=\"Header\"/></w:pPr><w:r><w:t>Header words here</w:t></w:r></w:p></w:hdr>")));

		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + xmlEscape(BODY[0]) + "</w:t></w:r><w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteReference w:id=\"1\"/></w:r></w:p>");
		add(mdp, para(BODY[1]));
		for (String[] c : COUNT_CASES) add(mdp, tokenField(c[0], c[1]));
		// the header reference on the body's section properties
		SectPr sectPr = mdp.getJaxbElement().getBody().getSectPr();
		if (sectPr == null) {
			sectPr = new SectPr();
			mdp.getJaxbElement().getBody().setSectPr(sectPr);
		}
		HeaderReference hr = new HeaderReference();
		hr.setId(rel.getId());
		hr.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(hr);

		// the predictions: body words and characters, the tokens and the OLD results included
		int words = 0, chars = 0, charsSp = 0;
		List<String> paras = new ArrayList<String>();
		for (String b : BODY) paras.add(b);
		for (String[] c : COUNT_CASES) paras.add(c[0] + " = OLD");
		for (String p : paras) {
			for (String w : p.trim().split("\\s+")) if (!w.isEmpty()) words++;
			chars += p.replace(" ", "").length();
			charsSp += p.length();
		}
		readme.append("\n\n=== numwords-probe-r1.docx (Ctrl+A, F9, Save As numwords-probe-r1-word365.docx) ===\n\n");
		readme.append("Two text paragraphs, a footnote (2 words, 13 characters), a header (3 words), then the cases; the case lines\n");
		readme.append("count too (token, '=', result: 3 words each). Please also note the status bar's word count and the Word Count\n");
		readme.append("dialog's numbers (with and without 'Include textboxes, footnotes and endnotes') in results.txt.\n");
		readme.append(String.format("Body (the two texts + the %d case lines with OLD): %d words, %d characters without spaces, %d with spaces, %d paragraphs.%n",
				COUNT_CASES.length, words, chars, charsSp, paras.size()));
		readme.append("The number a case shows replaces OLD (3 characters) so NUMCHARS may differ by a few; a word is any run of non-space\n");
		readme.append("characters ('=' counts; 'Well-known', 'e-mail', 'a@b.com', '1,000', \"don't\" count one each): unmeasured.\n\n");
		readme.append(String.format("  W01_NUMWORDS: { NUMWORDS } -> %d (footnote and header excluded), %d if the footnote counts, %d if the header counts too (unmeasured)%n", words, words + 2, words + 5));
		readme.append(String.format("  W02_NUMCHARS: { NUMCHARS } -> %d (characters without spaces; unmeasured: with spaces would be %d), +13 if the footnote counts%n", chars, charsSp));
		readme.append("  W03_PROP_WORDS: { DOCPROPERTY Words } -> the same as W01 (app.xml Words after the save)\n");
		readme.append("  W04_PROP_CHARACTERS: { DOCPROPERTY Characters } -> the same as W02\n");
		readme.append(String.format("  W05_PROP_CHARS_SPACES: { DOCPROPERTY CharactersWithSpaces } -> %d (unmeasured)%n", charsSp));
		readme.append(String.format("  W06_PROP_PARAGRAPHS: { DOCPROPERTY Paragraphs } -> %d%n", paras.size()));
		readme.append("  W07_PROP_LINES: { DOCPROPERTY Lines } -> the layout's line count (unmeasured)\n");
		readme.append("After the save, docProps/app.xml's Words, Characters, CharactersWithSpaces, Paragraphs and Lines are read as well.\n");

		File docx = new File(out, "numwords-probe-r1.docx");
		pkg.save(docx);
		System.out.println("wrote " + docx + " (" + words + " words predicted)");
	}

	// ---------------------------------------------------------------- the address merge probe

	/** The data source: Word's standard address field names, so the ADDRESSBLOCK placeholders map without a Match Fields step. */
	static final String[][] DATA = {
			{ "Title", "First Name", "Last Name", "Suffix", "Company", "Address 1", "Address 2", "City", "State", "Postal Code", "Country" },
			{ "Mr", "John", "Smith", "Jr", "Acme Pty Ltd", "1 Main St", "Suite 2", "Sydney", "NSW", "2000", "Australia" },
			{ "Ms", "Jane", "Doe", "", "", "2 High St", "", "Auckland", "", "1010", "New Zealand" },
			{ "", "Pat", "Lee", "", "Beta Inc", "3 Low Rd", "", "Perth", "WA", "6000", "Australia" },
			{ "", "Sam", "", "", "", "4 Side Ln", "", "Hobart", "TAS", "7000", "Australia" },
	};

	static final String ADDRESS_FORMAT = "<<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><< _SUFFIX0_>>\\n<<_COMPANY_\\n>><<_STREET1_\\n>><<_STREET2_\\n>><<_CITY_>><<, _STATE_>><< _POSTAL_>><<\\n_COUNTRY_>>";

	static final List<String[]> ADDRESS_CASES = new ArrayList<String[]>();
	static void a(String token, String instr, String expect) { ADDRESS_CASES.add(new String[] { token, instr, expect }); }
	static {
		a("M01_FIRST_NAME", "MERGEFIELD \"First Name\"", "John; Jane; Pat; Sam (the control)");
		a("M02_MERGEREC", "MERGEREC", "1; 2; 3; 4");
		a("A01_ADDRESSBLOCK", "ADDRESSBLOCK \\f \"" + ADDRESS_FORMAT + "\" \\l 1033 \\c 2 \\e \"Australia\" \\d",
				"Word's own en-US insertion. Lines (line breaks within one paragraph, unmeasured): "
				+ "1: Mr John Smith Jr / Acme Pty Ltd / 1 Main St / Suite 2 / Sydney, NSW 2000 (no country: it equals \\e); "
				+ "2: Ms Jane Doe / 2 High St / Auckland 1010 / New Zealand (no state, so no ', '; the country differs from \\e); "
				+ "3: Pat Lee / Beta Inc / 3 Low Rd / Perth, WA 6000; "
				+ "4: Sam / 4 Side Ln / Hobart, TAS 7000. \\d (the destination country's conventions) may reorder record 2 (unmeasured)");
		a("A02_COUNTRY_ALWAYS", "ADDRESSBLOCK \\f \"" + ADDRESS_FORMAT + "\" \\l 1033 \\c 1 \\e \"Australia\"", "as A01 with the country on every record (Australia on 1, 3, 4); no \\d");
		a("A03_COUNTRY_NEVER", "ADDRESSBLOCK \\f \"" + ADDRESS_FORMAT + "\" \\l 1033 \\c 0 \\e \"Australia\"", "as A01 with no country on any record (New Zealand dropped from 2)");
		a("A04_SHORT_FORMAT", "ADDRESSBLOCK \\f \"<<_FIRST0_>><< _LAST0_>>\\n<<_STREET1_\\n>><<_CITY_>>\" \\l 1033", "John Smith / 1 Main St / Sydney; Jane Doe / 2 High St / Auckland; Pat Lee / 3 Low Rd / Perth; Sam / 4 Side Ln / Hobart");
		a("A05_NO_SWITCHES", "ADDRESSBLOCK", "Word's default format (unmeasured; the same as A01's, I predict, with its own country rule)");
		a("G01_GREETING", "GREETINGLINE \\f \"<<_BEFORE_ Dear >><<_TITLE0_ >><<_LAST0_>><<_AFTER_ ,>>\" \\l 1033 \\e \"Dear Sir or Madam,\"",
				"Dear Mr Smith,; Dear Ms Doe,; record 3 has no title: Dear Pat Lee, or Dear Lee, (unmeasured); record 4 has no last name: Dear Sir or Madam, (the \\e text) or Dear Sam, (unmeasured)");
		a("G02_FIRST_NAME", "GREETINGLINE \\f \"<<_BEFORE_ Dear >><<_FIRST0_>><<_AFTER_ ,>>\" \\l 1033 \\e \"Dear friend,\"", "Dear John,; Dear Jane,; Dear Pat,; Dear Sam,");
		a("G03_FULL", "GREETINGLINE \\f \"<<_BEFORE_ To >><<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><<_AFTER_ :>>\" \\l 1033 \\e \"To whom it may concern:\"",
				"To Mr John Smith:; To Ms Jane Doe:; To Pat Lee:; To Sam: or the \\e text (unmeasured)");
		a("G04_NO_SWITCHES", "GREETINGLINE", "Word's default (unmeasured: 'Dear John,'-style or 'Dear Mr Smith,'-style, and the fallback)");
	}

	static void addressProbe(File out, StringBuilder readme) throws Exception {
		WordprocessingMLPackage data = WordprocessingMLPackage.createPackage();
		StringBuilder tbl = new StringBuilder("<w:tbl " + W + "><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid>");
		for (int i = 0; i < DATA[0].length; i++) tbl.append("<w:gridCol w:w=\"900\"/>");
		tbl.append("</w:tblGrid>");
		for (String[] row : DATA) {
			tbl.append("<w:tr>");
			for (String cell : row) {
				tbl.append("<w:tc><w:tcPr><w:tcW w:w=\"900\" w:type=\"dxa\"/></w:tcPr><w:p>");
				if (!cell.isEmpty()) tbl.append("<w:r><w:t>" + xmlEscape(cell) + "</w:t></w:r>");
				tbl.append("</w:p></w:tc>");
			}
			tbl.append("</w:tr>");
		}
		tbl.append("</w:tbl>");
		add(data.getMainDocumentPart(), tbl.toString());
		add(data.getMainDocumentPart(), "<w:p " + W + "/>");
		File dataDocx = new File(out, "address-data-r1.docx");
		data.save(dataDocx);

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>CR-067 phase 3 address probe</w:t></w:r></w:p>");
		add(mdp, para("One letter per record of address-data-r1.docx. Each case: its token, then the field."));
		readme.append("\n\n=== address-probe-r1.docx + address-data-r1.docx (a mail merge, about 5 minutes) ===\n\n");
		readme.append("1. Open address-probe-r1.docx normally. Mailings > Select Recipients > Use an Existing List... > address-data-r1.docx.\n");
		readme.append("2. Mailings > Match Fields: the columns are named as Word's address fields, so they should map by themselves; if any\n");
		readme.append("   shows '(not matched)', please note which and match it. Then Finish & Merge > Edit Individual Documents... > All.\n");
		readme.append("3. Save the merged document As address-probe-r1-merged.docx in this folder (keep .docx); close the template without\n");
		readme.append("   saving over it (address-probe-r1-attached.docx if Word insists). Note in results.txt the Word build, anything Word\n");
		readme.append("   asked, and the number of letters (predicted 4).\n\n");
		readme.append("Data: 1 Mr John Smith Jr, Acme Pty Ltd, 1 Main St, Suite 2, Sydney NSW 2000, Australia; 2 Ms Jane Doe, 2 High St, Auckland 1010,\n");
		readme.append("New Zealand (no suffix, company, address 2 or state); 3 Pat Lee (no title), Beta Inc, 3 Low Rd, Perth WA 6000, Australia;\n");
		readme.append("4 Sam (no title or last name), 4 Side Ln, Hobart TAS 7000, Australia.\n\n");
		readme.append("Cases (token: field -> predicted, letter by letter; '/' marks a line break inside the block):\n");
		int n = 0;
		for (String[] c : ADDRESS_CASES) {
			n++;
			add(mdp, tokenField(c[0], c[1]));
			readme.append(String.format("%3d. %s: { %s } -> %s%n", n, c[0], c[1], c[2]));
		}
		add(mdp, para("End of the letter."));
		File docx = new File(out, "address-probe-r1.docx");
		pkg.save(docx);
		System.out.println("wrote " + docx + " (" + n + " cases) and " + dataDocx);
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		StringBuilder readme = new StringBuilder();
		readme.append("CR-067 phase 3: Word 365 evaluation of INDEX/XE, LISTNUM/AUTONUM, FILENAME/TEMPLATE/FILESIZE, SYMBOL, NUMWORDS/NUMCHARS,\n");
		readme.append("ADDRESSBLOCK/GREETINGLINE (three Word sessions in this folder, about 20 minutes in all)\n\n");
		readme.append("For each docx: open normally (not Open and Repair), note the Word build (File > Account > About Word) in results.txt,\n");
		readme.append("Ctrl+A, F9 (answer and note anything Word asks), Save As <name>-word365.docx beside this README (keep .docx).\n");
		readme.append("Word's saved files are the oracle; there is no need to transcribe the cases. A PDF of each (Save As PDF) is welcome\n");
		readme.append("but optional. Please note any case whose display surprised you, and the N02/N05 paths if Word shows them differently\n");
		readme.append("from what the file holds.\n\n");
		mainProbe(out, readme);
		countProbe(out, readme);
		addressProbe(out, readme);
		try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(out, "README.txt")), StandardCharsets.UTF_8)) {
			w.write(readme.toString());
		}
		System.out.println("wrote README.txt");
	}
}
