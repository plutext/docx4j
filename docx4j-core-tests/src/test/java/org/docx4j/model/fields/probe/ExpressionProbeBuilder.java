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
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * Builds the CR-067 phase 2 probe (D6: the expression semantics measured before they are coded): one docx holding
 * {@code =} formulas, COMPARE and IF fields, SET/REF bookmarks and a table of cell formulas, every field with the
 * stale cached result {@code OLD}, for Word to update (Ctrl+A, F9) and save; and the README with the predictions.
 * Word's saved file is the oracle ({@code ExpressionProbeWordTest} replays it). Not a test:
 * {@code java ExpressionProbeBuilder <outDir>}.
 *
 * <p>A nested field in an instruction is written between {@code «} and {@code »}: {@code IF «=2+3» = 5 yes no}.</p>
 */
public class ExpressionProbeBuilder {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** token, instruction, prediction. */
	static final List<String[]> CASES = new ArrayList<String[]>();
	static void c(String token, String instr, String expect) { CASES.add(new String[] { token, instr, expect }); }
	static void group(String title) { CASES.add(new String[] { title }); }
	static {
		group("Formulas: operators and the default number format");
		c("F01_PRECEDENCE", "=2+3*4", "14");
		c("F02_PARENS", "=(2+3)*4", "20");
		c("F03_DIV", "=10/4", "2.5");
		c("F04_THIRD", "=1/3", "0.33 (Word's default rounds to two decimals; unmeasured)");
		c("F05_TWO_THIRDS", "=2/3", "0.67");
		c("F06_POWER", "=2^10", "1024");
		c("F07_ROOT", "=2^0.5", "1.41");
		c("F08_UNARY", "=-5+2", "-3");
		c("F09_PERCENT", "=50%", "0.5");
		c("F10_TIMES_NEG", "=2*-3", "-6");
		c("F11_SPACES", "= 5 + 1", "6");
		c("F12_BIG", "=1000000*1000000", "1000000000000 (or an overflow error text; unmeasured)");
		c("F13_SW_2DP", "=10 \\# \"0.00\"", "10.00");
		c("F14_SW_3DP", "=1/3 \\# \"0.000\"", "0.333");
		c("F15_COMPARE_IN_FORMULA", "=3>2", "1");
		c("F16_EQUAL_IN_FORMULA", "=3=2", "0");
		group("Formulas: error texts");
		c("F17_ZERO_DIVIDE", "=1/0", "!Zero Divide");
		c("F18_UNEXPECTED_END", "=1+", "!Unexpected End of Formula");
		c("F19_SYNTAX", "=1 2", "!Syntax Error, 2");
		c("F20_UNDEFINED", "=nope", "!Undefined Bookmark, nope");
		c("F21_BAD_FUNCTION", "=NOPE(1)", "!Undefined Bookmark, NOPE (or a syntax error; unmeasured)");
		group("Formulas: functions");
		c("F22_ABS", "=ABS(-3)", "3");
		c("F23_INT_POS", "=INT(3.7)", "3");
		c("F24_INT_NEG", "=INT(-3.7)", "-4 (rounds down; -3 if it truncates; unmeasured)");
		c("F25_ROUND_2", "=ROUND(2.345,2)", "2.35");
		c("F26_ROUND_HALF", "=ROUND(2.5,0)", "3");
		c("F27_ROUND_NEG_HALF", "=ROUND(-2.5,0)", "-3 (half away from zero; -2 if half to even; unmeasured)");
		c("F28_ROUND_HUNDREDS", "=ROUND(1234.5,-2)", "1200");
		c("F29_MOD", "=MOD(7,3)", "1");
		c("F30_MOD_NEG", "=MOD(-7,3)", "2 (floored, as Excel); -1 if truncated; unmeasured");
		c("F31_SIGN", "=SIGN(-5)", "-1");
		c("F32_SUM", "=SUM(1,2,3)", "6");
		c("F33_AVERAGE", "=AVERAGE(1,2,4)", "2.33");
		c("F34_MIN", "=MIN(4,2,8)", "2");
		c("F35_MAX", "=MAX(4,2,8)", "8");
		c("F36_COUNT", "=COUNT(1,2,3)", "3");
		c("F37_PRODUCT", "=PRODUCT(2,3,4)", "24");
		c("F38_AND", "=AND(1,0)", "0");
		c("F39_OR", "=OR(1,0)", "1");
		c("F40_NOT", "=NOT(0)", "1");
		c("F41_TRUE", "=TRUE", "1");
		c("F42_FALSE", "=FALSE", "0");
		c("F43_IF_FUNCTION", "=IF(3>2,10,20)", "10");
		c("F44_NESTED_FUNCTIONS", "=ROUND(AVERAGE(1,2),0)", "2 (1.5 rounds half away from zero; 2 either way if half to even)");
		group("Formulas: number forms (the VM's regional format decides; please note it in results.txt)");
		c("F45_DECIMAL_POINT", "=1.5+1", "2.5");
		c("F46_DECIMAL_COMMA", "=1,5+1", "an error (the comma is the list separator; unmeasured)");
		c("F47_THOUSANDS", "=1,000+1", "1001 if a thousands separator is read; else an error (unmeasured)");
		c("F48_LEADING_ZEROS", "=007+1", "8");
		group("Formulas: bookmarks (bookmark x holds the text 5)");
		c("F49_BOOKMARK", "=x*2", "10");
		c("F50_BOOKMARK_SPACED", "=3 + x", "8");
		c("F51_BOOKMARK_IN_FUNCTION", "=SUM(x,1)", "6");
		c("F52_DEFINED", "=DEFINED(x)", "1");
		c("F53_DEFINED_NOPE", "=DEFINED(nope)", "0");
		c("F54_NESTED_REF", "=«REF x»+1", "6 (the nested field's result is the operand)");
		group("COMPARE: numbers, strings, wildcards, case");
		c("C01_NUM_GT", "COMPARE 5 > 3", "1");
		c("C02_NUM_TWO_DIGITS", "COMPARE 10 > 9", "1 (numeric: a string compare would give 0)");
		c("C03_QUOTED_NUMBERS", "COMPARE \"10\" > \"9\"", "1 if quoted numbers still compare as numbers; 0 if as strings (unmeasured)");
		c("C04_STRING_ORDER", "COMPARE abc < abd", "1");
		c("C05_CASE", "COMPARE \"abc\" = \"ABC\"", "1 if the string compare ignores case; 0 if not (unmeasured)");
		c("C06_WILDCARD_STAR", "COMPARE \"abc\" = \"a*\"", "1");
		c("C07_WILDCARD_Q", "COMPARE \"abc\" = \"a?c\"", "1");
		c("C08_WILDCARD_LEFT", "COMPARE \"a?c\" = \"abc\"", "0 (wildcards only in the right operand)");
		c("C09_MIXED", "COMPARE 5 > \"abc\"", "0 (a string compare: \"5\" sorts before \"abc\")");
		c("C10_BOOKMARK_NAME", "COMPARE x = 5", "0 (x is the string x, not the bookmark; REF x would be 5)");
		c("C11_DECIMAL_EQ", "COMPARE 5 = 5.0", "1");
		c("C12_LEADING_ZEROS", "COMPARE 007 = 7", "1");
		c("C13_THOUSANDS", "COMPARE 1,000 = 1000", "1 if the thousands separator is read; else 0 (unmeasured)");
		c("C14_NEGATIVE", "COMPARE -1 < 0", "1");
		c("C15_EMPTY", "COMPARE \"\" = \"\"", "1");
		c("C16_NE", "COMPARE 5 <> 4", "1");
		c("C17_GE", "COMPARE 5 >= 5", "1");
		c("C18_LE", "COMPARE 5 <= 4", "0");
		group("IF: the forms");
		c("I01_QUOTED", "IF 5 > 3 \"yes\" \"no\"", "yes");
		c("I02_BARE", "IF 5 > 3 yes no", "yes");
		c("I03_NO_FALSE_TEXT", "IF 5 = 4 yes", "empty");
		c("I04_MULTI_WORD", "IF 5 = 5 \"multi word true\" \"multi word false\"", "multi word true");
		c("I05_NE", "IF 5 <> 4 yes no", "yes");
		c("I06_GE", "IF 5 >= 5 yes no", "yes");
		c("I07_LE", "IF 5 <= 4 yes no", "no");
		c("I08_NO_SPACES", "IF 5=5 yes no", "an error text, or yes if Word tokenises the operator (unmeasured)");
		c("I09_BAD_OPERATOR", "IF 5 ! 5 yes no", "Error! Unknown op code for conditional.");
		c("I10_EXTRA_TEXT", "IF 5 > 3 yes no extra", "yes");
		c("I11_BARE_EXTRA", "IF 5 = 5 a b c", "a");
		c("I12_EMPTY_STRINGS", "IF \"\" = \"\" yes no", "yes");
		c("I13_SPACE_IN_STRING", "IF \"a b\" = \"a b\" yes no", "yes");
		c("I14_EMPTY_RIGHT", "IF 5 = \"\" yes no", "no");
		c("I15_FORMAT_SWITCH", "IF 5 > 3 \"yes\" \"no\" \\* Upper", "YES");
		group("IF: numbers against strings");
		c("I16_NUMERIC", "IF 10 > 9 yes no", "yes (numeric)");
		c("I17_QUOTED_NUMBERS", "IF \"10\" > \"9\" yes no", "yes if quoted numbers compare as numbers; no if as strings (unmeasured)");
		c("I18_STRING_ORDER", "IF abc < abd yes no", "yes");
		c("I19_MIXED", "IF 5 > \"abc\" yes no", "no (a string compare)");
		c("I20_DECIMAL", "IF 5.0 = 5 yes no", "yes");
		c("I21_NEGATIVE", "IF -1 < 0 yes no", "yes");
		c("I22_THOUSANDS", "IF 1,000 > 999 yes no", "yes if the thousands separator is read; else no (unmeasured)");
		group("IF: case and wildcards");
		c("I23_CASE", "IF \"abc\" = \"ABC\" yes no", "yes if case is ignored; no if not (unmeasured)");
		c("I24_WILDCARD_STAR", "IF \"abc\" = \"a*\" yes no", "yes");
		c("I25_WILDCARD_Q", "IF \"abc\" = \"a?c\" yes no", "yes");
		c("I26_WILDCARD_NE", "IF \"abc\" <> \"a*\" yes no", "no");
		c("I27_WILDCARD_LT", "IF \"abc\" < \"a*\" yes no", "no (no wildcard for <: a plain string compare, and * sorts before b)");
		c("I28_WILDCARD_LEFT", "IF \"a?c\" = \"abc\" yes no", "no");
		group("IF: nested fields");
		c("I29_NESTED_FORMULA", "IF «=2+3» = 5 yes no", "yes");
		c("I30_NESTED_DATE", "IF «DATE \\@ yyyy» > 2000 yes no", "yes");
		c("I31_NESTED_IN_RESULT", "IF 5 = 5 «=1+1» no", "2");
		c("I32_NESTED_REF", "IF «REF x» = 5 yes no", "yes");
		c("I33_NESTED_BOTH", "IF «=1+1» < «=1+2» yes no", "yes");
		group("SET and REF (bookmarks a SET defines)");
		c("S01_SET", "SET total 42", "empty (SET shows nothing)");
		c("S02_REF_SET", "REF total", "42");
		c("S03_FORMULA_SET", "=total+1", "43");
		c("S04_IF_REF_SET", "IF «REF total» > 40 yes no", "yes");
		c("S05_DEFINED_SET", "=DEFINED(total)", "1");
		c("S06_SET_STRING", "SET greeting \"Hello World\"", "empty");
		c("S07_REF_STRING", "REF greeting", "Hello World");
		c("S08_IF_STRING_WILDCARD", "IF «REF greeting» = \"Hello*\" yes no", "yes");
		c("S09_SET_NESTED", "SET n «=6*7»", "empty");
		c("S10_REF_NESTED", "REF n", "42");
	}

	/** The table: rows of cells; a cell is a number, text, empty, or a formula (starts with =). */
	static final String[][] TABLE = {
			{ "1", "10", "x", "=SUM(LEFT)" },
			{ "2", "20", "", "=SUM(LEFT)" },
			{ "3", "30", "5", "=SUM(LEFT)" },
			{ "=SUM(ABOVE)", "=AVERAGE(ABOVE)", "=SUM(ABOVE)", "=SUM(A1:B2)" },
			{ "=A1+B2", "=MAX(A1:A3)", "=COUNT(C1:C3)", "=SUM(ABOVE)" },
	};
	static final String[][] TABLE_EXPECT = {
			{ null, null, null, "T_D1 11 (the text x is not a number)" },
			{ null, null, null, "T_D2 22 (the empty cell is nothing)" },
			{ null, null, null, "T_D3 38" },
			{ "T_A4 6", "T_B4 20", "T_C4 5 (x and the empty cell skipped)", "T_D4 33" },
			{ "T_A5 21", "T_B5 3", "T_C5 1 (COUNT counts the numbers only; 2 or 3 if it counts cells; unmeasured)", "T_D5 104 (the sum of the four results above it: 11+22+38+33)" },
	};

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>CR-067 phase 2 expression probe</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">Select all, press F9, then save as described in README.txt. Each case: its token, then the field. A field Word leaves alone still says OLD.</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">Bookmark x holds the number </w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"x\"/><w:r><w:t>5</w:t></w:r><w:bookmarkEnd w:id=\"1\"/><w:r><w:t>.</w:t></w:r></w:p>");

		StringBuilder readme = new StringBuilder();
		readme.append("CR-067 phase 2: Word 365 evaluation of = formulas, COMPARE, IF, SET/REF and table formulas (one file, about 10 minutes)\n\n");
		readme.append("1. Open expr-probe-r1.docx normally (not Open and Repair). Note the Word build (File > Account > About Word).\n");
		readme.append("2. Ctrl+A, then F9 to update every field. If Word asks anything, note what and answer to continue.\n");
		readme.append("3. Save As expr-probe-r1-word365.docx (keep the .docx format) beside this README. Word's saved file is the oracle;\n");
		readme.append("   there is no need to transcribe every case. In results.txt please note: the Word build; anything Word asked;\n");
		readme.append("   the VM's Windows regional format (Settings > Time & Language > Region > Regional format, and its decimal symbol\n");
		readme.append("   and list separator if you have them handy); and any case whose display surprised you.\n");
		readme.append("4. Optional: Save As PDF expr-probe-r1-word365.pdf (the formatting of the results, if Word changes any).\n\n");
		readme.append("Cases (token: field -> predicted after F9). 'unmeasured' marks a prediction the probe exists to settle.\n");
		int n = 0;
		for (String[] c : CASES) {
			if (c.length == 1) {
				add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t xml:space=\"preserve\">" + xmlEscape(c[0]) + "</w:t></w:r></w:p>");
				readme.append("\n").append(c[0]).append("\n");
				continue;
			}
			n++;
			add(mdp, complexField(c[0], c[1], "OLD"));
			readme.append(String.format("%3d. %s: { %s } -> %s%n", n, c[0], c[1].replace("«", "{ ").replace("»", " }"), c[2]));
		}

		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t>Table formulas (the cells below the numbers and to their right)</w:t></w:r></w:p>");
		add(mdp, table());
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>"); // Word wants a paragraph after a table
		readme.append("\nTable (columns A to D, rows 1 to 5): A1..A3 = 1, 2, 3; B1..B3 = 10, 20, 30; C1 = x (text), C2 empty, C3 = 5.\n");
		readme.append("Formula cells (address: field -> predicted):\n");
		char[] cols = { 'A', 'B', 'C', 'D' };
		for (int r = 0; r < TABLE.length; r++) {
			for (int col = 0; col < TABLE[r].length; col++) {
				if (TABLE[r][col].startsWith("=")) {
					readme.append(String.format("  %c%d: { %s } -> %s%n", cols[col], r + 1, TABLE[r][col], TABLE_EXPECT[r][col]));
				}
			}
		}
		readme.append("\nBookmark x holds the text 5 (a plain bookmark in the second paragraph). The SET cases define the bookmarks total, greeting and n.\n");
		readme.append("The document's language is en-US (createPackage's default); the IF and = number forms follow the VM's regional format.\n");

		File docx = new File(out, "expr-probe-r1.docx");
		pkg.save(docx);
		try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(out, "README.txt")), StandardCharsets.UTF_8)) {
			w.write(readme.toString());
		}
		System.out.println("wrote " + docx + " (" + n + " cases + " + TABLE.length + " table rows) and README.txt");
	}

	static void add(MainDocumentPart mdp, String xml) throws Exception {
		mdp.getContent().add(XmlUtils.unmarshalString(xml));
	}

	/** A paragraph "token = " then the complex field, nested fields («…») written as fields inside the instruction. */
	static String complexField(String token, String instr, String cached) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + field(instr, cached) + "</w:p>";
	}

	static String field(String instr, String cached) {
		StringBuilder sb = new StringBuilder();
		sb.append("<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>");
		int i = 0;
		while (i < instr.length()) {
			int open = instr.indexOf('«', i);
			if (open < 0) {
				sb.append(instrText((i == 0 ? " " : "") + instr.substring(i) + " "));
				break;
			}
			int close = matching(instr, open);
			String before = instr.substring(i, open);
			sb.append(instrText((i == 0 ? " " : "") + before));
			sb.append(field(instr.substring(open + 1, close), cached));
			i = close + 1;
			if (i >= instr.length()) sb.append(instrText(" "));
		}
		sb.append("<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>");
		sb.append("<w:r><w:t>" + cached + "</w:t></w:r>");
		sb.append("<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>");
		return sb.toString();
	}

	/** The index of the » matching the « at open (nested pairs inside counted). */
	static int matching(String instr, int open) {
		int depth = 0;
		for (int i = open; i < instr.length(); i++) {
			if (instr.charAt(i) == '«') depth++;
			else if (instr.charAt(i) == '»' && --depth == 0) return i;
		}
		throw new IllegalArgumentException("no » for « at " + open + " in " + instr);
	}

	static String instrText(String text) {
		return "<w:r><w:instrText xml:space=\"preserve\">" + xmlEscape(text) + "</w:instrText></w:r>";
	}

	static String table() {
		StringBuilder sb = new StringBuilder();
		sb.append("<w:tbl " + W + "><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/>"
				+ "<w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
				+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
				+ "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>");
		for (String[] row : TABLE) {
			sb.append("<w:tr>");
			for (String cell : row) {
				sb.append("<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr><w:p>");
				if (cell.startsWith("=")) {
					sb.append(field(cell, "OLD"));
				} else if (!cell.isEmpty()) {
					sb.append("<w:r><w:t>" + xmlEscape(cell) + "</w:t></w:r>");
				}
				sb.append("</w:p></w:tc>");
			}
			sb.append("</w:tr>");
		}
		sb.append("</w:tbl>");
		return sb.toString();
	}

	static String xmlEscape(String s) {
		return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
	}
}
