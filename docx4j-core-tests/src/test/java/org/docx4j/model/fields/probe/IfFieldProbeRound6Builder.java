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

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * IF-field probe, round 6: the shapes of rounds 3 and 4 (resultless, separate-less, stored-wrong, with and without a
 * twin) beside a DATE field, the one thing round 1 alone contained and Word refreshes on open. If the refresh is a
 * field-update pass that also fills resultless fields (and their twins), round 1 is explained. As-is only. Not a test.
 */
public class IfFieldProbeRound6Builder {

	static final String[][] CASES = {
			{ "W01_DATE", complex(instrText(" DATE \\@ \"yyyy\" "), "2030", false), "2030", "the DATE: Word refreshes it on open (2026)" },
			{ "W02_STORED", complex(instrText(" IF 2 = 2 \"A2\" \"B2\" "), "B2", false), "B2", "stored wrong; an empty twin follows (W03)" },
			{ "W03_TWIN_EMPTY", complex(instrText(" IF 2 = 2 \"A2\" \"B2\" "), "", false), "", "a separate with no result, W02's twin" },
			{ "W04_ALONE", complex(instrText(" IF 1 = 1 \"A1\" \"B1\" "), "B1", false), "B1", "stored wrong, no twin" },
			{ "W05_NOSEP_ALONE", complex(instrText(" IF 9 = 9 \"A9\" \"B9\" "), null, false), "(no separate)", "no separate, no twin" },
			{ "W06_STORED3", complex(instrText(" IF 3 = 3 \"A3\" \"B3\" "), "B3", false), "B3", "stored wrong; a separate-less twin follows (W07)" },
			{ "W07_NOSEP_TWIN", complex(instrText(" IF 3 = 3 \"A3\" \"B3\" "), null, false), "(no separate)", "W06's twin" },
			{ "W08_FORMULA_STORED", complex(instrText(" = 3 + 3 "), "7", false), "7", "a formula stored wrong; an empty twin follows" },
			{ "W09_FORMULA_EMPTY", complex(instrText(" = 3 + 3 "), "", false), "", "" },
			{ "W10_DOCPROPERTY_STORED", complex(instrText(" DOCPROPERTY Title "), "Nope", false), "Nope", "a DOCPROPERTY stored wrong; an empty twin follows" },
			{ "W11_DOCPROPERTY_EMPTY", complex(instrText(" DOCPROPERTY Title "), "", false), "", "" },
			{ "W12_WILDCARD_EMPTY", complex(instrText(" IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\" "), "", false), "", "the wildcard IF with no result, alone" },
			{ "W13_HIDDEN_BRANCH", "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>" + instrText(" IF 1 = 1 \"") + "<w:r><w:rPr><w:vanish/></w:rPr><w:instrText>HIDDEN_WHEN_UPDATED</w:instrText></w:r>" + instrText("\" \"VISIBLE_STALE\" ")
					+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>VISIBLE_STALE</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>", "VISIBLE_STALE", "round 1's C12 beside a DATE" },
			{ "W14_COMPARE_EMPTY", complex(instrText(" COMPARE 2 > 1 "), "", false), "", "a COMPARE with no result" },
			{ "W15_IF_OVER_DATE", complex(instrText(" IF ") + complex(instrText(" DATE \\@ \"yyyy\" "), "2030", false) + instrText(" > 2026 \"EXPIRED\" \"VALID\" "), "STALE_OUTER", false), "STALE_OUTER", "round 1's C04" },
			{ "W16_EMPTY_ALONE", complex(instrText(" IF 8 = 8 \"A8\" \"B8\" "), "", false), "", "a separate with no result, no twin" },
			{ "W17_STRING_STORED", complex(instrText(" IF \"x\" = \"x\" \"A10\" \"B10\" "), "B10", false), "B10", "a string IF stored wrong; an empty twin follows" },
			{ "W18_STRING_EMPTY", complex(instrText(" IF \"x\" = \"x\" \"A10\" \"B10\" "), "", false), "", "" },
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
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 6 (CR-067): the shapes of rounds 3 and 4 beside a DATE field. Title property: Probe.</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder("IF-field probe, round 6 (about 2 minutes): open iffield-probe-r6.docx (answer No to any prompt), touch nothing,\n"
				+ "Save As Plain Text iffield-probe-r6-word-asis.txt and Save As iffield-probe-r6-word-asis.docx. No F9 needed.\n\nCases (token: stored -- what it asks):\n\n");
		StringBuilder tsv = new StringBuilder("token\tstored\tnote\n");
		for (String[] c : CASES) {
			add(mdp, p(c[0], c[1]));
			String stored = c[2].isEmpty() ? "(separate, no result)" : c[2];
			readme.append(c[0]).append(": ").append(stored).append(c[3].isEmpty() ? "" : "  -- " + c[3]).append('\n');
			tsv.append(c[0]).append('\t').append(stored).append('\t').append(c[3]).append('\n');
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		pkg.save(new File(out, "iffield-probe-r6.docx"));
		Files.write(new File(out, "README-round6.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round6.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CASES.length + " cases");
	}
}
