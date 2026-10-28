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
import static org.docx4j.model.fields.probe.ExpressionProbeBuilder.xmlEscape;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * IF-field probe, round 5: does Word, opening a document, share a stored result across fields with the identical
 * instruction, and which one wins? Round 1's rewritten fields all shared their instruction with a field holding the
 * right result (C14); rounds 2 to 4 (no such twin) changed nothing. Families of two or three fields with the same
 * instruction and different stored results, in both orders; a formula, a DOCPROPERTY, the wildcard IF; a locked twin;
 * an empty twin last; a simple-field twin; and round 1's hidden-branch field alone. As-is only. Not a test.
 */
public class IfFieldProbeRound5Builder {

	static String field(String instr, String cached, boolean locked, String resultRPr) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"" + (locked ? " w:fldLock=\"true\"" : "") + "/></w:r>" + instrText(instr)
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ (cached.isEmpty() ? "" : "<w:r>" + resultRPr + "<w:t>" + cached + "</w:t></w:r>")
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	static String p(String token, String xml) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>" + xml + "</w:p>";
	}

	static final String[][] CASES = {
			{ "V01_RIGHT_FIRST", field(" IF 6 = 6 \"A6\" \"B6\" ", "A6", false, ""), "A6", "the right result first; its twin V02 holds the wrong one" },
			{ "V02_WRONG_LAST", field(" IF 6 = 6 \"A6\" \"B6\" ", "B6", false, ""), "B6", "the wrong result last: if the last wins, both show B6; if the right one spreads, both show A6" },
			{ "V03_WRONG_FIRST", field(" IF 7 = 7 \"A7\" \"B7\" ", "B7", false, ""), "B7", "the wrong result first" },
			{ "V04_RIGHT_LAST", field(" IF 7 = 7 \"A7\" \"B7\" ", "A7", false, ""), "A7", "the right result last" },
			{ "V05_THREE_X", field(" IF 8 = 8 \"A8\" \"B8\" ", "X8", false, ""), "X8", "three twins with three results that are no branch" },
			{ "V06_THREE_Y", field(" IF 8 = 8 \"A8\" \"B8\" ", "Y8", false, ""), "Y8", "" },
			{ "V07_THREE_Z", field(" IF 8 = 8 \"A8\" \"B8\" ", "Z8", false, ""), "Z8", "" },
			{ "V08_FORMULA_WRONG", field(" = 4 + 4 ", "9", false, ""), "9", "a formula stored wrong; its twin V09 right" },
			{ "V09_FORMULA_RIGHT", field(" = 4 + 4 ", "8", false, ""), "8", "" },
			{ "V10_DOCPROPERTY_WRONG", field(" DOCPROPERTY Title ", "Nope", false, ""), "Nope", "a DOCPROPERTY stored wrong; its twin V11 right" },
			{ "V11_DOCPROPERTY_RIGHT", field(" DOCPROPERTY Title ", "Probe", false, ""), "Probe", "" },
			{ "V12_WILDCARD_WRONG", field(" IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" ", "WILD_MISS", false, ""), "WILD_MISS", "the wildcard IF stored wrong; its twin V13 right" },
			{ "V13_WILDCARD_RIGHT", field(" IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" ", "WILD_MATCH", false, ""), "WILD_MATCH", "" },
			{ "V14_WRONG_WITH_LOCKED_TWIN", field(" IF 1 = 1 \"A1\" \"B1\" ", "B1", false, ""), "B1", "stored wrong; its twin V15 is locked and right" },
			{ "V15_LOCKED_RIGHT", field(" IF 1 = 1 \"A1\" \"B1\" ", "A1", true, ""), "A1", "locked, right" },
			{ "V16_WRONG_THEN_EMPTY", field(" IF 2 = 2 \"A2\" \"B2\" ", "B2", false, ""), "B2", "stored wrong; its twin V17 has an empty result, last" },
			{ "V17_EMPTY_LAST", field(" IF 2 = 2 \"A2\" \"B2\" ", "", false, ""), "", "an empty result last" },
			{ "V18_HIDDEN_BRANCH_ALONE", "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>" + instrText(" IF 1 = 1 \"") + "<w:r><w:rPr><w:vanish/></w:rPr><w:instrText>HIDDEN_WHEN_UPDATED</w:instrText></w:r>" + instrText("\" \"VISIBLE_STALE\" ")
					+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>VISIBLE_STALE</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>", "VISIBLE_STALE", "round 1's C12 alone: its true branch hidden text in the code; Word's open removed its separate and result" },
			{ "V19_HIDDEN_RESULT", field(" IF 3 = 3 \"A3\" \"B3\" ", "B3", false, "<w:rPr><w:vanish/></w:rPr>"), "B3 (hidden)", "the stored result run hidden, the code plain" },
			{ "V20_WRONG_COMPLEX", field(" IF 5 = 5 \"A5\" \"B5\" ", "B5", false, ""), "B5", "stored wrong; its twin V21 is a w:fldSimple holding the right result" },
			{ "V21_RIGHT_SIMPLE", "<w:fldSimple w:instr=\"" + xmlEscape(" IF 5 = 5 \"A5\" \"B5\" ") + "\"><w:r><w:t>A5</w:t></w:r></w:fldSimple>", "A5", "the simple-field twin, right" },
			{ "V22_SPACED_RIGHT", field("  IF 6 = 6  \"A6\" \"B6\" ", "A6", false, ""), "A6", "V01's instruction with extra spaces, right: does whitespace separate families?" },
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
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 5 (CR-067): twins with the same instruction and different stored results. Title property: Probe.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder("IF-field probe, round 5 (about 2 minutes): open iffield-probe-r5.docx (answer No to any prompt), touch nothing,\n"
				+ "Save As Plain Text iffield-probe-r5-word-asis.txt and Save As iffield-probe-r5-word-asis.docx. No F9 needed.\n\nCases (token: stored -- what it asks):\n\n");
		StringBuilder tsv = new StringBuilder("token\tstored\tnote\n");
		for (String[] c : CASES) {
			add(mdp, p(c[0], c[1]));
			readme.append(c[0]).append(": ").append(c[2].isEmpty() ? "(separate, no result)" : c[2]).append(c[3].isEmpty() ? "" : "  -- " + c[3]).append('\n');
			tsv.append(c[0]).append('\t').append(c[2]).append('\t').append(c[3]).append('\n');
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		pkg.save(new File(out, "iffield-probe-r5.docx"));
		Files.write(new File(out, "README-round5.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round5.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CASES.length + " cases");
	}
}
