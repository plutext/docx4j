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
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.xmlEscape;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * Builds the CR-067 phase 2 merge probe (the merge conditionals, D6): a mail-merge template holding NEXTIF, SKIPIF,
 * MERGEREC, MERGESEQ, SET, ASK and FILLIN, with IF, {@code =} and the MERGEFIELD switches over merge data, and a data
 * source (a docx with a table of five records). Jason runs the merge in Word; the merged output is the oracle
 * ({@code MergeProbeWordTest} replays it through the mergers). Not a test: {@code java MergeProbeBuilder <outDir>}.
 */
public class MergeProbeBuilder {

	/** The data source: Name, Middle, Amount, City. */
	static final String[][] DATA = {
			{ "Name", "Middle", "Amount", "City" },
			{ "Ann", "", "150", "Sydney" },
			{ "Bob", "J", "50", "Melbourne" },
			{ "Cat", "", "200", "Perth" },
			{ "Dan", "K", "300", "Hobart" },
			{ "Eve", "", "120", "Darwin" },
	};

	/** token, instruction («…» a nested field), prediction per output letter (record 1; 3 with 4 pulled by NEXTIF; 5). */
	static final List<String[]> CASES = new ArrayList<String[]>();
	static void c(String token, String instr, String expect) { CASES.add(new String[] { token, instr, expect }); }
	static {
		c("M01_MERGEREC", "MERGEREC", "the record's number in the data source: 1; 3; 5 (record 2 is skipped by M12, record 4 is pulled in by M16)");
		c("M02_MERGESEQ", "MERGESEQ", "the letter's number among the merged ones: 1; 2; 3 (a skipped record does not count; unmeasured)");
		c("M03_NAME", "MERGEFIELD Name", "Ann; Cat; Eve");
		c("M04_NAME_UPPER", "MERGEFIELD Name \\* Upper", "ANN; CAT; EVE");
		c("M05_NAME_BEFORE_AFTER", "MERGEFIELD Name \\b \"Dear \" \\f \",\"", "Dear Ann,; Dear Cat,; Dear Eve,");
		c("M06_MIDDLE", "MERGEFIELD Middle", "empty for all three (Ann, Cat and Eve have no middle initial); whether Word removes the line (suppress blank lines) is unmeasured: compare the merged output's paragraphs");
		c("M07_MIDDLE_BEFORE_AFTER", "MERGEFIELD Middle \\b \"(\" \\f \")\"", "empty for all three: \\b and \\f print only around a non-empty value");
		c("M08_AMOUNT_FORMAT", "MERGEFIELD Amount \\# \"$#,##0.00\"", "$150.00; $200.00; $120.00");
		c("M09_DOUBLE", "=«MERGEFIELD Amount»*2", "300; 400; 240");
		c("M10_IF_AMOUNT", "IF «MERGEFIELD Amount» >= 100 \"big\" \"small\"", "big; big; big (small would be Bob's, skipped)");
		c("M11_COMPARE_CITY", "COMPARE «MERGEFIELD City» = \"S*\"", "1; 0; 0");
		c("M12_SKIPIF", "SKIPIF «MERGEFIELD Amount» < 100", "empty; the record whose Amount is below 100 (Bob, 50) produces no letter at all");
		c("M13_SET_TOTAL", "SET total «=«MERGEFIELD Amount»+1»", "empty on display (the value 151; 201; 121 stored under the bookmark)");
		c("M14_REF_TOTAL", "REF total", "151; 201; 121");
		c("M15_NOPE", "MERGEFIELD Nope", "Word's text for a field the data source has no column for (unmeasured; an error text, or «Nope»)");
		c("M16_ASK_ONCE", "ASK Greeting \"Greeting for every letter?\" \\d \"Hello\" \\o", "empty on display; Word asks ONCE (\\o) at the start of the merge: press OK for the default Hello");
		c("M17_REF_GREETING", "REF Greeting", "Hello in every letter");
		c("M18_ASK_EACH", "ASK Prompt2 \"Prompt for this record?\" \\d \"Hi\"", "empty on display; Word asks for each letter (three times, or four if it also asks for the skipped record: please count): press OK each time");
		c("M19_REF_PROMPT2", "REF Prompt2", "Hi in every letter");
		c("M20_FILLIN_ONCE", "FILLIN \"Your name?\" \\d \"World\" \\o", "World in every letter; asked once (press OK)");
		c("M21_FILLIN_EACH", "FILLIN \"Per letter?\" \\d \"Each\"", "Each in every letter; asked for each letter (press OK each time; count the prompts)");
		c("M22_BEFORE_NEXTIF", "MERGEFIELD Name", "Ann; Cat; Eve (the record before the NEXTIF)");
		c("M23_NEXTIF", "NEXTIF «MERGEFIELD City» = \"Perth\"", "empty; in Cat's letter (Perth) the rest of the letter takes the next record (Dan) without starting a new letter");
		c("M24_AFTER_NEXTIF", "MERGEFIELD Name", "Ann; Dan; Eve");
		c("M25_MERGEREC_AFTER", "MERGEREC", "1; 4; 5");
		c("M26_MERGESEQ_AFTER", "MERGESEQ", "1; 2 or 3 (whether the pulled record counts is unmeasured); 3 or 4");
		c("M27_CITY_AFTER", "MERGEFIELD City", "Sydney; Hobart; Darwin");
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();

		// the data source: a document holding one table, the header row first (Word reads it as a recipient list)
		WordprocessingMLPackage data = WordprocessingMLPackage.createPackage();
		StringBuilder tbl = new StringBuilder("<w:tbl " + W + "><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid>");
		for (int i = 0; i < DATA[0].length; i++) tbl.append("<w:gridCol w:w=\"2000\"/>");
		tbl.append("</w:tblGrid>");
		for (String[] row : DATA) {
			tbl.append("<w:tr>");
			for (String cell : row) {
				tbl.append("<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr><w:p>");
				if (!cell.isEmpty()) tbl.append("<w:r><w:t>" + xmlEscape(cell) + "</w:t></w:r>");
				tbl.append("</w:p></w:tc>");
			}
			tbl.append("</w:tr>");
		}
		tbl.append("</w:tbl>");
		add(data.getMainDocumentPart(), tbl.toString());
		add(data.getMainDocumentPart(), "<w:p " + W + "/>");
		File dataDocx = new File(out, "merge-data-r1.docx");
		data.save(dataDocx);

		// the template
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>CR-067 phase 2 merge probe</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">One letter per record of merge-data-r1.docx (Name, Middle, Amount, City). Each case: its token, then the field.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder();
		readme.append("CR-067 phase 2: Word 365 mail merge of the merge conditionals (one merge, about 10 minutes)\n\n");
		readme.append("Files: merge-probe-r1.docx (the template), merge-data-r1.docx (the data source: a table of five records).\n\n");
		readme.append("1. Open merge-probe-r1.docx normally. Note the Word build.\n");
		readme.append("2. Mailings > Select Recipients > Use an Existing List... > choose merge-data-r1.docx (in this folder). If Word asks\n");
		readme.append("   which table, take the only one. (The fields may now show their cached OLD text; that is fine.)\n");
		readme.append("3. Mailings > Finish & Merge > Edit Individual Documents... > All > OK. Word will prompt for the ASK and FILLIN fields:\n");
		readme.append("   press OK (Enter) to accept each default, and please COUNT the prompts (M16 and M20 should ask once, M18 and M21 once\n");
		readme.append("   per letter; whether the skipped record also prompts is one of the questions).\n");
		readme.append("4. Save the merged document (the new window, 'Letters1') As merge-probe-r1-merged.docx in this folder; keep the\n");
		readme.append("   .docx format. Also Save As PDF merge-probe-r1-merged.pdf if quick. Close the template without saving (or save it\n");
		readme.append("   as merge-probe-r1-attached.docx if Word insists: either way, not over merge-probe-r1.docx).\n");
		readme.append("5. In results.txt: the Word build, the prompt counts, anything Word asked or complained about (a missing column, say),\n");
		readme.append("   and the number of letters (sections) in the merged document (predicted: 3, since record 2 is skipped).\n\n");
		readme.append("Data: 1 Ann (no middle) 150 Sydney; 2 Bob J 50 Melbourne; 3 Cat (no middle) 200 Perth; 4 Dan K 300 Hobart; 5 Eve (no middle) 120 Darwin.\n");
		readme.append("Predicted letters: record 1; record 3 whose second half pulls record 4 (NEXTIF on City = Perth); record 5. Record 2 is\n");
		readme.append("skipped (SKIPIF Amount < 100).\n\n");
		readme.append("Cases (token: field -> predicted, letter by letter):\n");
		int n = 0;
		for (String[] c : CASES) {
			n++;
			add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + c[0] + " = </w:t></w:r>" + field(c[1], "OLD") + "</w:p>");
			readme.append(String.format("%3d. %s: { %s } -> %s%n", n, c[0], c[1].replace("«", "{ ").replace("»", " }"), c[2]));
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the letter.</w:t></w:r></w:p>");
		File docx = new File(out, "merge-probe-r1.docx");
		pkg.save(docx);
		try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(out, "README.txt")), StandardCharsets.UTF_8)) {
			w.write(readme.toString());
		}
		System.out.println("wrote " + docx + " (" + n + " cases), " + dataDocx + " and README.txt");
	}
}
