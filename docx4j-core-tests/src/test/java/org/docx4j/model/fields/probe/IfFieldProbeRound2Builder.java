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
import static org.docx4j.model.fields.probe.IfFieldProbeBuilder.simple;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * IF-field probe, round 2: which fields Word recomputes when it merely opens a document (round 1: the literal IFs and
 * the IF over a DATE were rewritten on open, those over a DOCPROPERTY, a REF, a formula, a COMPARE and the wildcard IF
 * were not). One paragraph per case, the stored result the losing branch unless said. Not a test.
 */
public class IfFieldProbeRound2Builder {

	static String dirtyComplex(String instr, String cached) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\" w:dirty=\"true\"/></w:r>" + instrText(instr)
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + cached + "</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	static final String[][] CASES = {
			{ "R01_STRING_EQ", complex(instrText(" IF \"a\" = \"a\" \"STR_MATCH\" \"STR_MISS\" "), "STR_MISS", false), "STR_MISS", "a string equality, no wildcard: recomputed on open?" },
			{ "R02_WILDCARD_TRUE_STORED", complex(instrText(" IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" "), "WILD_MATCH", false), "WILD_MATCH", "the wildcard IF with the true branch stored: an open-time evaluation without wildcards would flip it to WILD_MISS" },
			{ "R03_NUMERIC_GT", complex(instrText(" IF 2 > 1 \"NUM_GT\" \"NUM_LE\" "), "NUM_LE", false), "NUM_LE", "a numeric comparison with another operator" },
			{ "R04_FALSE_COND", complex(instrText(" IF 1 = 2 \"WRONG\" \"RIGHT\" "), "WRONG", false), "WRONG", "a false condition with the true branch stored" },
			{ "R05_STRING_NE", complex(instrText(" IF \"b\" <> \"a\" \"NE_TRUE\" \"NE_FALSE\" "), "NE_FALSE", false), "NE_FALSE", "a string inequality" },
			{ "R06_CASE", complex(instrText(" IF \"a\" = \"A\" \"CASE_MATCH\" \"CASE_MISS\" "), "CASE_MATCH", false), "CASE_MATCH", "case: F9 said a string comparison is case-sensitive (phase 2); the true branch stored" },
			{ "R07_OVER_FORMULA", complex(instrText(" IF ") + complex(instrText(" = 1 + 1 "), "3", false) + instrText(" = 2 \"FORMULA_TRUE\" \"FORMULA_FALSE\" "), "FORMULA_FALSE", false), "FORMULA_FALSE", "an IF over a formula whose stored result is wrong" },
			{ "R08_OVER_PAGE", complex(instrText(" IF ") + complex(instrText(" PAGE "), "7", false) + instrText(" = 1 \"PAGE_ONE\" \"PAGE_OTHER\" "), "PAGE_OTHER", false), "PAGE_OTHER", "an IF over PAGE (a layout field)" },
			{ "R09_OVER_RIGHT_DOCPROPERTY", complex(instrText(" IF ") + complex(instrText(" DOCPROPERTY Title "), "Probe", false) + instrText(" = \"Probe\" \"TITLE_MATCH\" \"TITLE_MISMATCH\" "), "TITLE_MISMATCH", false), "TITLE_MISMATCH", "an IF over a DOCPROPERTY whose stored result is already right, the outer's wrong" },
			{ "R10_OVER_NUMPAGES", complex(instrText(" IF ") + complex(instrText(" NUMPAGES "), "9", false) + instrText(" = 1 \"ONE_PAGE\" \"MORE_PAGES\" "), "MORE_PAGES", false), "MORE_PAGES", "an IF over NUMPAGES" },
			{ "R11_DIRTY_WILDCARD", dirtyComplex(" IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" ", "WILD_MISS"), "WILD_MISS", "the wildcard IF with w:dirty on its begin" },
			{ "R12_DIRTY_FORMULA", dirtyComplex(" = 1 + 1 ", "3"), "3", "a formula with w:dirty on its begin" },
			{ "R13_DIRTY_DOCPROPERTY", dirtyComplex(" DOCPROPERTY Title ", "Nope"), "Nope", "a DOCPROPERTY with w:dirty on its begin" },
			{ "R14_SIMPLE_DIRTY", "<w:fldSimple w:instr=\" = 1 + 1 \" w:dirty=\"true\"><w:r><w:t>3</w:t></w:r></w:fldSimple>", "3", "a w:fldSimple formula with w:dirty" },
			{ "R15_COMPARE_EQ", complex(instrText(" COMPARE 1 = 1 "), "0", false), "0", "COMPARE with the literal operands of C01" },
			{ "R16_IF_NUMBERS_STRINGS", complex(instrText(" IF \"1\" = \"1\" \"QUOTED_MATCH\" \"QUOTED_MISS\" "), "QUOTED_MISS", false), "QUOTED_MISS", "the literal IF with its numbers quoted" },
			{ "R17_SIMPLE_WILDCARD", simple(" IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" ", "WILD_MISS"), "WILD_MISS", "the wildcard IF as a w:fldSimple" },
			{ "R18_DOCVARIABLE", complex(instrText(" IF ") + complex(instrText(" DOCVARIABLE Missing "), "x", false) + instrText(" = \"x\" \"VAR_X\" \"VAR_OTHER\" "), "VAR_X", false), "VAR_X", "an IF over a DOCVARIABLE that does not exist (stored x)" },
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
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 2 (CR-067): which fields Word recomputes on open. Title property: Probe.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder();
		readme.append("IF-field probe, round 2 (about 3 minutes): open iffield-probe-r2.docx, touch nothing, Save As Plain Text\n");
		readme.append("iffield-probe-r2-word365-asis.txt and Save As iffield-probe-r2-word365-asis.docx; then Ctrl+A, F9, Save As Plain Text\n");
		readme.append("iffield-probe-r2-word365-f9.txt. Cases (token: the stored result -- what the case asks):\n\n");
		StringBuilder tsv = new StringBuilder("token\tstored\tnote\n");
		for (String[] c : CASES) {
			add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + c[0] + " = </w:t></w:r>" + c[1] + "</w:p>");
			readme.append(c[0]).append(": ").append(c[2]).append("  -- ").append(c[3]).append('\n');
			tsv.append(c[0]).append('\t').append(c[2]).append('\t').append(c[3]).append('\n');
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		pkg.save(new File(out, "iffield-probe-r2.docx"));
		Files.write(new File(out, "README-round2.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round2.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CASES.length + " cases");
	}
}
