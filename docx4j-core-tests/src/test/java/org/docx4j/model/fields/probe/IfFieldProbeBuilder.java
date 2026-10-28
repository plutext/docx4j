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
import java.util.ArrayList;
import java.util.List;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;

/**
 * The IF-field probe (Jason, 2026-10-29, through the portfolio): one paragraph per case, {@code Cnn_LABEL = } then a
 * field whose stored result is deliberately the losing branch, so that a text extractor reading the cached result and
 * one computing the field disagree. Two files: the probe, and a copy with {@code w:updateFields} on; a README with
 * Word's steps and the expected text per mode; {@code expected.tsv} for scoring every extractor. Not a test:
 * {@code java IfFieldProbeBuilder <outDir>}.
 */
public class IfFieldProbeBuilder {

	/** token, field xml, stored result as read (cached), docx4j update, Word F9, note. */
	static final List<String[]> CASES = new ArrayList<String[]>();

	static void c(String token, String xml, String cached, String update, String f9, String note) {
		CASES.add(new String[] { token, xml, cached, update, f9, note });
	}

	static String simple(String instr, String cached) {
		return "<w:fldSimple w:instr=\"" + xmlEscape(instr) + "\">" + (cached == null ? "" : "<w:r><w:t>" + cached + "</w:t></w:r>") + "</w:fldSimple>";
	}

	/** A complex field from pieces: the instruction runs given, a separate where {@code cached} is not null, the result. */
	static String complex(String instrRuns, String cached, boolean locked) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"" + (locked ? " w:fldLock=\"true\"" : "") + "/></w:r>" + instrRuns
				+ (cached == null ? "" : "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + (cached.isEmpty() ? "" : "<w:r><w:t>" + cached + "</w:t></w:r>"))
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	static final String IF_APPROVED = " IF 1 = 1 \"APPROVED\" \"REJECTED\" ";

	static {
		c("C01_LITERAL_IF", ExpressionProbeBuilder.field("IF 1 = 1 \"APPROVED\" \"REJECTED\"", "REJECTED"), "REJECTED", "APPROVED", "APPROVED", "a literal condition, the stored result the losing branch");
		c("C02_SIMPLE_FIELD", simple(IF_APPROVED, "REJECTED"), "REJECTED", "APPROVED", "APPROVED", "the same as a w:fldSimple");
		c("C03_DOCPROPERTY_COND", complex(instrText(" IF ") + complex(instrText(" DOCPROPERTY Title "), "Nope", false) + instrText(" = \"Probe\" \"TITLE_MATCH\" \"TITLE_MISMATCH\" "), "TITLE_MISMATCH", false), "TITLE_MISMATCH", "TITLE_MATCH", "TITLE_MATCH", "the condition on the Title property (Probe); the nested field's stored result says Nope");
		c("C04_DATE_COND", complex(instrText(" IF ") + complex(instrText(" DATE \\@ \"yyyy\" "), "2030", false) + instrText(" > 2026 \"EXPIRED\" \"VALID\" "), "STALE_OUTER", false), "STALE_OUTER", "EXPIRED", "VALID", "the condition on a DATE whose stored result is 2030: docx4j's text model never computes DATE (WD), so it reads 2030 and says EXPIRED; Word rewrites DATE on open (2026) and says VALID");
		c("C05_FORMULA", ExpressionProbeBuilder.field("= 1 + 1", "3"), "3", "2", "2", "a formula stored wrong");
		c("C06_COMPARE", ExpressionProbeBuilder.field("COMPARE 2 > 1", "0"), "0", "1", "1", "a comparison stored wrong");
		c("C07_LOCKED", complex(instrText(IF_APPROVED), "REJECTED", true), "REJECTED", "REJECTED", "REJECTED", "w:fldLock: no one may update it");
		c("C08_NO_RESULT", complex(instrText(IF_APPROVED), "", false), "", "APPROVED", "APPROVED", "begin, separate, end: no stored result");
		c("C09_NO_SEPARATE", complex(instrText(IF_APPROVED), null, false), "", "APPROVED", "APPROVED", "begin, end, no separate (REQ-505: field.no_result)");
		c("C10_NESTED_BRANCH", complex(instrText(" IF 1 = 1 \"") + complex(instrText(" DOCPROPERTY Title "), "Nope", false) + instrText("\" \"LIT\" "), "LIT", false), "LIT", "Probe", "Probe", "the true branch is a nested DOCPROPERTY (stored Nope)");
		c("C11_REF_EDITED", complex(instrText(" IF ") + complex(instrText(" REF Edited "), "OLD", false) + instrText(" = \"OLD\" \"REF_OLD\" \"REF_NEW\" "), "REF_OLD", false), "REF_OLD", "REF_NEW", "REF_NEW", "the condition on a REF to the bookmark Edited, whose text was changed to NEW after the last update (the paragraph before)");
		c("C12_HIDDEN_TRUE", complex(instrText(" IF 1 = 1 \"") + "<w:r><w:rPr><w:vanish/></w:rPr><w:instrText>HIDDEN_WHEN_UPDATED</w:instrText></w:r>" + instrText("\" \"VISIBLE_STALE\" "), "VISIBLE_STALE", false),
				"VISIBLE_STALE", "HIDDEN_WHEN_UPDATED", "(hidden: HIDDEN_WHEN_UPDATED as hidden text, so Word's plain text omits it)", "the true branch is hidden text (w:vanish on its instruction run): Word gives the result the branch's formatting");
		c("C13_HAND_EDITED", ExpressionProbeBuilder.field("IF 1 = 1 \"APPROVED\" \"REJECTED\"", "NEITHER_BRANCH_TEXT"), "NEITHER_BRANCH_TEXT", "APPROVED", "APPROVED", "a stored result that is neither branch (hand-edited)");
		c("C14_CONTROL_TRUE", ExpressionProbeBuilder.field("IF 1 = 1 \"APPROVED\" \"REJECTED\"", "APPROVED"), "APPROVED", "APPROVED", "APPROVED", "control: the stored result is the right one");
		c("C15_SIMPLE_EMPTY", simple(IF_APPROVED, null), "", "APPROVED", "APPROVED", "a w:fldSimple with no result");
		c("C16_WILDCARD", ExpressionProbeBuilder.field("IF \"Probe\" = \"Pr*\" \"WILD_MATCH\" \"WILD_MISS\"", "WILD_MISS"), "WILD_MISS", "WILD_MATCH", "WILD_MATCH", "a wildcard comparison (measured in phase 2)");
	}

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

		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">IF-field probe (CR-067): each case is its token, then a field whose stored result is the losing branch. Title property: Probe.</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">Bookmark Edited: </w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"Edited\"/><w:r><w:t>NEW</w:t></w:r><w:bookmarkEnd w:id=\"1\"/><w:r><w:t xml:space=\"preserve\"> (its text was OLD when the fields were last updated).</w:t></w:r></w:p>");
		StringBuilder readme = new StringBuilder();
		readme.append("IF-field probe (CR-067; Jason's question 2026-10-29: can an IF field give different extracted text, updated or not?)\n\n");
		readme.append("Files: iffield-probe-r1.docx (the probe) and iffield-probe-r1-updatefields.docx (the same with w:updateFields on, so Word\n");
		readme.append("asks on open). Steps, each a Save As Plain Text (Windows encoding, CR/LF, no line-break insertion) and a Save As docx:\n");
		readme.append("(1) open iffield-probe-r1.docx (no prompt expected), touch nothing: Save As iffield-probe-r1-word365-asis.txt and -asis.docx;\n");
		readme.append("(2) open it again, Ctrl+A, F9: Save As iffield-probe-r1-word365-f9.txt and -f9.docx;\n");
		readme.append("(3) open iffield-probe-r1-updatefields.docx, answer Yes to the update prompt: Save As iffield-probe-r1-word365-updatefields-yes.txt;\n");
		readme.append("    open it again, answer No: Save As iffield-probe-r1-word365-updatefields-no.txt.\n\n");
		readme.append("Expected per case (token: stored/cached | docx4j fields: update | Word F9). The as-is and updatefields-no texts should equal the\n");
		readme.append("stored column (Word's converter writes the stored result, measured 2026-10-29); updatefields-yes should equal Word F9.\n\n");
		StringBuilder tsv = new StringBuilder("token\tcached\tupdate\tword_f9\tnote\n");
		for (String[] c : CASES) {
			add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + c[0] + " = </w:t></w:r>" + c[1] + "</w:p>");
			readme.append(c[0]).append(": ").append(quote(c[2])).append(" | ").append(quote(c[3])).append(" | ").append(quote(c[4])).append("  -- ").append(c[5]).append('\n');
			tsv.append(c[0]).append('\t').append(c[2]).append('\t').append(c[3]).append('\t').append(c[4]).append('\t').append(c[5]).append('\n');
		}
		add(mdp, "<w:p " + W + "><w:r><w:t>End of the probe.</w:t></w:r></w:p>");
		File docx = new File(out, "iffield-probe-r1.docx");
		pkg.save(docx);

		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart(true);
		BooleanDefaultTrue on = Context.getWmlObjectFactory().createBooleanDefaultTrue();
		dsp.getJaxbElement().setUpdateFields(on);
		pkg.save(new File(out, "iffield-probe-r1-updatefields.docx"));

		Files.write(new File(out, "README.txt").toPath(), readme.toString().getBytes(StandardCharsets.UTF_8));
		Files.write(new File(out, "expected.tsv").toPath(), tsv.toString().getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + out + ": " + CASES.size() + " cases");
	}

	static String quote(String s) {
		return s.isEmpty() ? "(nothing)" : s;
	}
}
