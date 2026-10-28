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
 * IF-field probe, round 8 (Jason, through the portfolio: "we'd better measure those possible triggers"): one file per
 * candidate field, each in round 7's shape (the candidate, three literal IFs with stored-wrong results, a last field),
 * so each file answers on its own whether the candidate triggers Word's open-time pass (rounds 6 and 7: DATE and TIME
 * do). As-is docx per file. Not a test.
 */
public class IfFieldProbeRound8Builder {

	/** file name, the candidate's instruction, its stored result, what it is. */
	static final String[][] CANDIDATES = {
			{ "date", " DATE \\@ \"yyyy\" ", "2030", "positive control (round 6)" },
			{ "none", null, null, "negative control: no candidate, the three IFs and the last field only" },
			{ "formula", " = 1 + 1 ", "3", "negative control: a formula" },
			{ "docproperty", " DOCPROPERTY Title ", "Nope", "a document property (Title is Probe)" },
			{ "dirty-docproperty", "DIRTY DOCPROPERTY Title ", "Nope", "the same with w:dirty on its begin (Word asks; answer No)" },
			{ "edittime", " EDITTIME ", "999", "" },
			{ "printdate", " PRINTDATE \\@ \"yyyy\" ", "2030", "" },
			{ "savedate", " SAVEDATE \\@ \"yyyy\" ", "2030", "" },
			{ "createdate", " CREATEDATE \\@ \"yyyy\" ", "2030", "" },
			{ "filename", " FILENAME ", "stale.docx", "" },
			{ "filesize", " FILESIZE ", "1", "" },
			{ "numpages", " NUMPAGES ", "9", "" },
			{ "page", " PAGE ", "9", "" },
			{ "sectionpages", " SECTIONPAGES ", "9", "" },
			{ "section", " SECTION ", "9", "" },
			{ "numwords", " NUMWORDS ", "9", "" },
			{ "numchars", " NUMCHARS ", "9", "" },
			{ "username", " USERNAME ", "Nobody", "" },
			{ "userinitials", " USERINITIALS ", "ZZ", "" },
			{ "author", " AUTHOR ", "Nobody", "" },
			{ "title", " TITLE ", "Nope", "" },
			{ "info", " INFO Title ", "Nope", "" },
			{ "revnum", " REVNUM ", "99", "" },
			{ "template", " TEMPLATE ", "stale.dotm", "" },
			{ "docvariable", " DOCVARIABLE x ", "x", "a variable that does not exist" },
			{ "listnum", " LISTNUM ", "OLD", "" },
			{ "autonum", " AUTONUM ", "OLD", "" },
			{ "symbol", " SYMBOL 169 ", "OLD", "" },
			{ "seq", " SEQ fig ", "9", "" },
			{ "ref", " REF Edited ", "OLD", "a REF to a bookmark holding NEW" },
			{ "compare", " COMPARE 1 = 1 ", "0", "" },
	};

	static String candidate(String instr, String stored) {
		if (instr.startsWith("DIRTY ")) {
			return "<w:r><w:fldChar w:fldCharType=\"begin\" w:dirty=\"true\"/></w:r>" + instrText(" " + instr.substring(6))
					+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + stored + "</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
		}
		return complex(instrText(instr), stored, false);
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		StringBuilder readme = new StringBuilder("IF-field probe, round 8 (one Save As per file, about 10 minutes): for each iffield-probe-r8-<candidate>.docx: open it (answer No\n"
				+ "to any prompt), touch nothing, Save As iffield-probe-r8-<candidate>-word-asis.docx (the docx alone is enough: the results are in it;\n"
				+ "a plain-text save beside it is welcome but not needed). Each file holds the candidate field (stored stale), then Y02, Y03, Y04,\n"
				+ "three literal IFs stored wrong, then Y05, a literal IF stored wrong that is the last field. The candidate triggers Word's\n"
				+ "open-time pass if Y02 to Y04 come out as their true branches (A2, A3, A4); Y05 is expected as stored (B5) either way.\n\nFiles:\n");
		StringBuilder tsv = new StringBuilder("file\tcandidate\tstored\tnote\n");
		for (String[] c : CANDIDATES) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			MainDocumentPart mdp = pkg.getMainDocumentPart();
			CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
			ObjectFactory dc = new ObjectFactory();
			SimpleLiteral title = dc.createSimpleLiteral();
			title.getContent().add("Probe");
			core.setTitle(dc.createTitle(title));
			add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe, round 8 (CR-067), candidate " + c[0] + ". Title property: Probe. Bookmark Edited: </w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"Edited\"/><w:r><w:t>NEW</w:t></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>");
			if (c[1] != null) add(mdp, p("Y01_" + c[0].toUpperCase().replace('-', '_'), candidate(c[1], c[2])));
			add(mdp, p("Y02_STORED", complex(instrText(" IF 2 = 2 \"A2\" \"B2\" "), "B2", false)));
			add(mdp, p("Y03_STORED", complex(instrText(" IF 3 = 3 \"A3\" \"B3\" "), "B3", false)));
			add(mdp, p("Y04_STORED", complex(instrText(" IF 4 = 4 \"A4\" \"B4\" "), "B4", false)));
			add(mdp, p("Y05_LAST", complex(instrText(" IF 5 = 5 \"A5\" \"B5\" "), "B5", false)));
			add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe (no field after this).</w:t></w:r></w:p>");
			pkg.save(new File(out, "iffield-probe-r8-" + c[0] + ".docx"));
			readme.append("iffield-probe-r8-").append(c[0]).append(".docx: ").append(c[1] == null ? "(no candidate)" : c[1].trim()).append(c[3].isEmpty() ? "" : "  -- " + c[3]).append('\n');
			tsv.append(c[0]).append('\t').append(c[1] == null ? "" : c[1].trim()).append('\t').append(c[2] == null ? "" : c[2]).append('\t').append(c[3]).append('\n');
		}
		Files.write(new File(out, "README-round8.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected-round8.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CANDIDATES.length + " files");
	}
}
