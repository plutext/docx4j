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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * IF-field probe, round 4: a field with no separate at all (round 1's C09, the one shape round 3 lacked). Round 1's
 * open rewrote every field sharing C09's instruction; round 3 showed a resultless field WITH a separate changes
 * nothing. Here: a no-separate field alone; stored-wrong twins of a no-separate field (after, before, other branches,
 * a formula, a DOCPROPERTY); a no-separate field whose twin has a separate and no result. Not a test.
 */
public class IfFieldProbeRound4Builder {

	/** {@code cached} null: no separate; "": a separate with no result. */
	static String f(String token, String instr, String cached) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + complex(instrText(instr), cached, false) + "</w:p>";
	}

	static final String[][] CASES = {
			{ "U01_NOSEP_ALONE", " IF 9 = 9 \"A9\" \"B9\" ", null, "a field with no separate, an instruction of its own: computed on open, and written into the file?" },
			{ "U02_STORED", " IF 2 = 2 \"A2\" \"B2\" ", "B2", "stored wrong; a no-separate twin follows (U03)" },
			{ "U03_NOSEP_TWIN", " IF 2 = 2 \"A2\" \"B2\" ", null, "the no-separate twin of U02" },
			{ "U04_NOSEP_FIRST", " IF 3 = 3 \"A3\" \"B3\" ", null, "a no-separate field before its stored-wrong twin (U05)" },
			{ "U05_STORED_AFTER", " IF 3 = 3 \"A3\" \"B3\" ", "B3", "stored wrong, its no-separate twin before it" },
			{ "U06_OTHER_BRANCHES", " IF 2 = 2 \"OTHER_A\" \"OTHER_B\" ", "OTHER_B", "U02's condition with other branches" },
			{ "U07_FORMULA_STORED", " = 3 + 3 ", "7", "a formula stored wrong; a no-separate twin follows" },
			{ "U08_FORMULA_NOSEP", " = 3 + 3 ", null, "the no-separate twin of U07" },
			{ "U09_DOCPROPERTY_STORED", " DOCPROPERTY Title ", "Nope", "a DOCPROPERTY stored wrong; a no-separate twin follows" },
			{ "U10_DOCPROPERTY_NOSEP", " DOCPROPERTY Title ", null, "the no-separate twin of U09" },
			{ "U11_EMPTY_STORED", " IF 5 = 5 \"A5\" \"B5\" ", "B5", "stored wrong; a separate-but-empty twin follows (U12): round 3 says both stay" },
			{ "U12_EMPTY_TWIN", " IF 5 = 5 \"A5\" \"B5\" ", "", "the separate-but-empty twin of U11" },
			{ "U13_WILDCARD_STORED", " IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" ", "WILD_MISS", "the wildcard IF stored wrong; a no-separate twin follows" },
			{ "U14_WILDCARD_NOSEP", " IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" ", null, "the no-separate twin of U13" },
			{ "U15_CONTROL", " IF 1 = 1 \"A1\" \"B1\" ", "B1", "a stored-wrong IF with no twin of any kind" },
	};

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		ObjectFactory dc = new ObjectFactory();
		SimpleLiteral title = dc.createSimpleLiteral();
		title.getContent().add("Probe");
		core.setTitle(dc.createTitle(title));
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 4 (CR-067): fields with no separate and their twins. Title property: Probe.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder("IF-field probe, round 4 (about 2 minutes): open iffield-probe-r4.docx (answer No to any prompt), touch nothing,\n"
				+ "Save As Plain Text iffield-probe-r4-word-asis.txt and Save As iffield-probe-r4-word-asis.docx. No F9 needed.\n\nCases (token: stored -- what it asks):\n\n");
		StringBuilder tsv = new StringBuilder("token\tstored\tnote\n");
		for (String[] c : CASES) {
			add(mdp, f(c[0], c[1], c[2]));
			String stored = c[2] == null ? "(no separate)" : c[2].isEmpty() ? "(separate, no result)" : c[2];
			readme.append(c[0]).append(": ").append(stored).append("  -- ").append(c[3]).append('\n');
			tsv.append(c[0]).append('\t').append(stored).append('\t').append(c[3]).append('\n');
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		pkg.save(new File(out, "iffield-probe-r4.docx"));
		Files.write(new File(out, "README-round4.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round4.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CASES.length + " cases");
	}
}
