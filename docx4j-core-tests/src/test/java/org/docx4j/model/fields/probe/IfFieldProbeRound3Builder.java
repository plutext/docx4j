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
 * IF-field probe, round 3: the reading of rounds 1 and 2 that Word, opening a document, computes a field that has no
 * result and gives every field with the same instruction that result. Families of fields: a stored-wrong field alone, a
 * stored-wrong field with a resultless twin (after it, before it, with different whitespace), for an IF, a formula, a
 * DOCPROPERTY and a string IF; a resultless field with an instruction of its own. Not a test.
 */
public class IfFieldProbeRound3Builder {

	static String f(String token, String instr, String cached) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + complex(instrText(instr), cached, false) + "</w:p>";
	}

	static final String[][] CASES = {
			{ "T01_ALONE", " IF 1 = 1 \"A1\" \"B1\" ", "B1", "a stored-wrong literal IF with no twin: round 2 says it stays B1" },
			{ "T02_STORED", " IF 2 = 2 \"A2\" \"B2\" ", "B2", "stored wrong, a resultless twin follows (T03): the reading says both show A2" },
			{ "T03_TWIN_RESULTLESS", " IF 2 = 2 \"A2\" \"B2\" ", "", "the resultless twin of T02" },
			{ "T04_TWIN_FIRST_RESULTLESS", " IF 3 = 3 \"A3\" \"B3\" ", "", "a resultless field before its stored-wrong twin (T05)" },
			{ "T05_STORED_AFTER", " IF 3 = 3 \"A3\" \"B3\" ", "B3", "stored wrong, its resultless twin before it" },
			{ "T06_FORMULA_STORED", " = 3 + 3 ", "7", "a formula stored wrong, a resultless twin follows" },
			{ "T07_FORMULA_TWIN", " = 3 + 3 ", "", "the resultless twin of T06" },
			{ "T08_DOCPROPERTY_STORED", " DOCPROPERTY Title ", "Nope", "a DOCPROPERTY stored wrong, a resultless twin follows" },
			{ "T09_DOCPROPERTY_TWIN", " DOCPROPERTY Title ", "", "the resultless twin of T08" },
			{ "T10_STRING_STORED", " IF \"x\" = \"x\" \"A10\" \"B10\" ", "B10", "a string IF stored wrong, a resultless twin follows" },
			{ "T11_STRING_TWIN", " IF \"x\" = \"x\" \"A10\" \"B10\" ", "", "the resultless twin of T10" },
			{ "T12_WHITESPACE_STORED", " IF 4 = 4 \"A4\" \"B4\" ", "B4", "stored wrong; its twin (T13) differs only in whitespace" },
			{ "T13_WHITESPACE_TWIN", "  IF 4 = 4  \"A4\" \"B4\" ", "", "the resultless twin with two spaces" },
			{ "T14_RESULTLESS_ALONE", " IF 9 = 9 \"A9\" \"B9\" ", "", "a resultless field with an instruction of its own: computed to show, and written into the file?" },
			{ "T15_DIFFERENT_BRANCHES", " IF 2 = 2 \"OTHER_A\" \"OTHER_B\" ", "OTHER_B", "the condition of T02 and T03 with other branches: untouched if the key is the whole instruction" },
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
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 3 (CR-067): a resultless field and its twins. Title property: Probe.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder("IF-field probe, round 3 (about 2 minutes): open iffield-probe-r3.docx (no prompt expected; if one, answer No), touch nothing,\n"
				+ "Save As Plain Text iffield-probe-r3-word-asis.txt and Save As iffield-probe-r3-word-asis.docx. No F9 needed.\n\nCases (token: stored -- what it asks):\n\n");
		StringBuilder tsv = new StringBuilder("token\tstored\tnote\n");
		for (String[] c : CASES) {
			add(mdp, f(c[0], c[1], c[2]));
			readme.append(c[0]).append(": ").append(c[2].isEmpty() ? "(no result)" : c[2]).append("  -- ").append(c[3]).append('\n');
			tsv.append(c[0]).append('\t').append(c[2]).append('\t').append(c[3]).append('\n');
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		pkg.save(new File(out, "iffield-probe-r3.docx"));
		Files.write(new File(out, "README-round3.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round3.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CASES.length + " cases");
	}
}
