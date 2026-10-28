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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * CR-067 phase 3, round 2: LISTNUM's default level (round 1 put the first LISTNUM of a paragraph after a Heading 2 at
 * level 3, "i)", and showed one set of counters shared by the three named lists) and AUTONUM in a document without
 * headings, then after one. Not a test: {@code java ListNumProbeBuilder <outDir>}.
 */
public class ListNumProbeBuilder {

	/** style (null = Normal), token, instruction, prediction; a row with only a style and text is a plain paragraph. */
	static final String[][] CASES = {
			{ null, "R01_LISTNUM", "LISTNUM", "1) if the default level in a heading-free document is 1; i) if it is always 3 (unmeasured)" },
			{ null, "R02_LISTNUM", "LISTNUM", "2) (or ii))" },
			{ null, "R03_LEVEL3", "LISTNUM \\l 3", "i) (a level started under level 2 = 1, the implicit parent)" },
			{ null, "R04_LEVEL2", "LISTNUM \\l 2", "b) if R03 started an implicit level-2 item (round 1's reading); a) if not" },
			{ null, "R05_LEGAL", "LISTNUM LegalDefault", "the level-1 counter continued in the legal format: 3. (the counters are shared, round 1)" },
			{ "Heading1", "H1", null, "a Heading 1 paragraph with no field" },
			{ null, "R06_AFTER_H1", "LISTNUM", "a) if the default level is the preceding heading's level + 1; else the level-1 count continues" },
			{ "Heading2", "H2", null, "a Heading 2 paragraph with no field" },
			{ null, "R07_AFTER_H2", "LISTNUM", "i) if the default level is the preceding heading's level + 1" },
			{ "Heading1", "R08_IN_H1", "LISTNUM", "the field inside a Heading 1 paragraph: 1) at level 1, or a) (unmeasured)" },
			{ null, "R09_LEVEL1", "LISTNUM \\l 1", "the level-1 counter + 1" },
			{ "Heading3", "H3", null, "a Heading 3 paragraph with no field" },
			{ null, "R10_AFTER_H3", "LISTNUM", "(1) if the default level is the heading's level + 1 = 4" },
			{ null, "R11_AUTONUM", "AUTONUM", "1." },
			{ null, "R12_AUTONUM", "AUTONUM", "2." },
			{ "Heading1", "H1b", null, "a Heading 1 paragraph with no field" },
			{ null, "R13_AUTONUM_AFTER_H1", "AUTONUM", "3. if AUTONUM counts on across a heading; 1. if a heading restarts it (unmeasured)" },
			{ "Heading1", "R14_AUTONUM_IN_H1", "AUTONUM", "a Heading 1 paragraph holding the field: 4., or 1. (unmeasured)" },
			{ null, "R15_AUTONUMLGL", "AUTONUMLGL", "in Normal after a Heading 1: 1.1. or 1. (unmeasured)" },
	};

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">CR-067 phase 3 round 2: LISTNUM and AUTONUM (no heading before the first cases). Select all, F9, Save As listnum-probe-r2-word365.docx and also Save As PDF.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder();
		readme.append("CR-067 phase 3, round 2 (about 10 minutes):\n\n");
		readme.append("A. phase3-probe-r1-word365.docx (round 1): please open it and Save As PDF phase3-probe-r1-word365.pdf. Word writes no\n");
		readme.append("   result for LISTNUM, AUTONUM and SYMBOL into the file (they still say OLD in it), so the PDF is the only record of\n");
		readme.append("   L10 to L19, A01 to A15 and S01 to S14. If Word asks to update fields on open, say no (or yes: F9 gives the same).\n\n");
		readme.append("B. listnum-probe-r2.docx: open, Ctrl+A, F9, Save As listnum-probe-r2-word365.docx and Save As PDF listnum-probe-r2-word365.pdf.\n");
		readme.append("   Cases (token: field -> predicted):\n");
		int n = 0;
		for (String[] c : CASES) {
			String style = c[0] == null ? "" : "<w:pPr><w:pStyle w:val=\"" + c[0] + "\"/></w:pPr>";
			if (c[2] == null) {
				add(mdp, "<w:p " + W + ">" + style + "<w:r><w:t xml:space=\"preserve\">" + c[1] + " heading text</w:t></w:r></w:p>");
				readme.append("       (" + c[1] + ": " + c[3] + ")\n");
				continue;
			}
			n++;
			add(mdp, "<w:p " + W + ">" + style + "<w:r><w:t xml:space=\"preserve\">" + c[1] + " = </w:t></w:r>" + field(c[2], "OLD")
					+ (c[0] == null ? "" : "<w:r><w:t xml:space=\"preserve\"> heading text</w:t></w:r>") + "</w:p>");
			readme.append(String.format("   %3d. %s%s: { %s } -> %s%n", n, c[1], c[0] == null ? "" : " (" + c[0] + ")", c[2], xmlEscape(c[3]).replace("&quot;", "\"")));
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		readme.append("\nC. address-probe-r1.docx: open it, attach address-data-r1.docx again (Select Recipients > Use an Existing List), then\n");
		readme.append("   at the end of the document insert Mailings > Address Block (OK with the defaults) and Mailings > Greeting Line (OK with\n");
		readme.append("   the defaults), and Save As address-probe-r1-attached.docx (keep .docx). This records Word's own \\f format strings and\n");
		readme.append("   its field mapping (settings.xml), which round 1's merged output cannot show: in it the \\n inside a token came out as\n");
		readme.append("   the letter n (A01 to A04), and G03 fell back to its \\e text for every record.\n");
		File docx = new File(out, "listnum-probe-r2.docx");
		pkg.save(docx);
		try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(out, "README-round2.txt")), StandardCharsets.UTF_8)) {
			w.write(readme.toString());
		}
		System.out.println("wrote " + docx + " (" + n + " cases) and README-round2.txt");
	}
}
