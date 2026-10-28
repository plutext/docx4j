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
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.instrText;
import static org.docx4j.model.fields.probe.IfFieldProbeBuilder.complex;
import static org.docx4j.model.fields.probe.IfFieldProbeRound5Builder.p;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * IF-field probe, round 7: round 6 showed that a DATE in the document makes Word's open update every literal IF but the
 * document's last field (round 1's C16 and round 6's W18, both last, both left alone). Here round 6's W18 is followed by
 * two more literal IFs, and a TIME stands in for the DATE in a second copy. As-is only. Not a test.
 */
public class IfFieldProbeRound7Builder {

	static String[][] cases(String trigger) {
		return new String[][] {
				{ "X01_TRIGGER", complex(instrText(trigger), "2030", false), "2030", "the auto-updating field: " + trigger.trim() },
				{ "X02_STORED", complex(instrText(" IF 2 = 2 \"A2\" \"B2\" "), "B2", false), "B2", "stored wrong" },
				{ "X03_STRING_EMPTY", complex(instrText(" IF \"x\" = \"x\" \"A10\" \"B10\" "), "", false), "", "round 6's W18, no longer last" },
				{ "X04_STORED4", complex(instrText(" IF 4 = 4 \"A4\" \"B4\" "), "B4", false), "B4", "stored wrong, second to last field" },
				{ "X05_LAST", complex(instrText(" IF 7 = 7 \"A7\" \"B7\" "), "B7", false), "B7", "stored wrong, the LAST field of the document" },
		};
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		StringBuilder readme = new StringBuilder("IF-field probe, round 7 (about 3 minutes): for each of iffield-probe-r7-date.docx and iffield-probe-r7-time.docx: open\n"
				+ "(answer No to any prompt), touch nothing, Save As Plain Text iffield-probe-r7-date-word-asis.txt / iffield-probe-r7-time-word-asis.txt\n"
				+ "and Save As ...-word-asis.docx. No F9 needed. The last field of each document is X05.\n\n");
		StringBuilder tsv = new StringBuilder("file\ttoken\tstored\tnote\n");
		for (String[] variant : new String[][] { { "date", " DATE \\@ \"yyyy\" " }, { "time", " TIME \\@ \"yyyy\" " } }) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			MainDocumentPart mdp = pkg.getMainDocumentPart();
			add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 7 (CR-067), " + variant[0] + ": the last field of the document beside an auto-updating field.</w:t></w:r></w:p>");
			readme.append(variant[0]).append(":\n");
			for (String[] c : cases(variant[1])) {
				add(mdp, p(c[0], c[1]));
				readme.append(c[0]).append(": ").append(c[2].isEmpty() ? "(separate, no result)" : c[2]).append("  -- ").append(c[3]).append('\n');
				tsv.append(variant[0]).append('\t').append(c[0]).append('\t').append(c[2]).append('\t').append(c[3]).append('\n');
			}
			readme.append('\n');
			add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe (no field after this).</w:t></w:r></w:p>");
			pkg.save(new File(out, "iffield-probe-r7-" + variant[0] + ".docx"));
		}
		Files.write(new File(out, "README-round7.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round7.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out);
	}
}
