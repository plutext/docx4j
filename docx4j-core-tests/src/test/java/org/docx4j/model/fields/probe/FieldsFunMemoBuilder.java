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
 * "One file, many readings" (Jason, 2026-10-29, through the portfolio): a one-page purchase approval memo whose fields are
 * placed so that each reader class tells a different story, from the eight IF-probe rounds' measured behaviours. Prose a
 * person would read, one field per sentence, the stored results chosen so the surfaces differ. Not a test:
 * {@code java FieldsFunMemoBuilder <outDir>}.
 */
public class FieldsFunMemoBuilder {

	static String t(String text) {
		return "<w:r><w:t xml:space=\"preserve\">" + xmlEscape(text) + "</w:t></w:r>";
	}

	static String p(String... runs) {
		StringBuilder sb = new StringBuilder("<w:p " + W + ">");
		for (String r : runs) sb.append(r);
		return sb.append("</w:p>").toString();
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		ObjectFactory dc = new ObjectFactory();
		SimpleLiteral title = dc.createSimpleLiteral();
		title.getContent().add("Purchase approval");
		core.setTitle(dc.createTitle(title));
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Amount", "750");

		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>" + t("Purchase approval") + "</w:p>");
		// 1. the open-time trigger
		add(mdp, p(t("To: Finance. From: Procurement. Prepared in "), complex(instrText(" DATE \\@ \"yyyy\" "), "2020", false), t(".")));
		add(mdp, p(t("This memo seeks approval of the attached purchase order from ACME Supplies.")));
		// 2. a literal IF, stored wrong: Word's open recomputes it because of the DATE
		add(mdp, p(t("Payment terms: Net "), complex(instrText(" IF 1 = 1 \"30\" \"90\" "), "90", false), t(" days.")));
		// 3. a nested-branch IF: the nested field's stored result leaks in readers that flatten nesting
		add(mdp, p(t("Amount due: $"), complex(instrText(" IF 1 = 1 \"") + complex(instrText(" DOCPROPERTY Amount "), "1", false) + instrText("\" \"0\" "), "500", false), t(".")));
		// 4. a simple field: some readers skip w:fldSimple
		add(mdp, p(t("Discount: "), "<w:fldSimple w:instr=\" = 10 \"><w:r><w:t>10</w:t></w:r></w:fldSimple>", t("%.")));
		// 5. a wildcard IF, stored wrong
		add(mdp, p(t("Supplier code matches our approved list: "), complex(instrText(" IF \"ACME-2026\" = \"ACME-*\" \"yes\" \"no\" "), "no", false), t(".")));
		// 6. a formula, stored wrong
		add(mdp, p(t("Subtotal check: 2 + 2 = "), complex(instrText(" = 2 + 2 "), "5", false), t(".")));
		// 7. a locked IF, stored wrong
		add(mdp, p(t("Approval status: "), complex(instrText(" IF 1 = 1 \"approved\" \"rejected\" "), "rejected", true), t(".")));
		// 8. an IF over PAGE: Word's print-time pass
		add(mdp, p(t("Printed on page "), complex(instrText(" IF ") + complex(instrText(" PAGE "), "9", false) + instrText(" = 1 \"one\" \"two or later\" "), "two or later", false), t(".")));
		// 9. a hidden true branch
		add(mdp, p(t("Reviewer note: "), "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>" + instrText(" IF 1 = 1 \"") + "<w:r><w:rPr><w:vanish/></w:rPr><w:instrText>confidential</w:instrText></w:r>" + instrText("\" \"none\" ")
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>none</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>", t(".")));
		// the last field, correct, so that Word's open-time pass skips nothing that matters
		add(mdp, p(t("Reference: "), complex(instrText(" DOCPROPERTY Title "), "Purchase approval", false), t(".")));

		File docx = new File(out, "one-file-many-readings.docx");
		pkg.save(docx);
		String readme = "One file, many readings (CR-067; Jason, 2026-10-29): a one-page purchase approval memo whose ten fields make each reader\n"
				+ "class tell a different story. Title property: Purchase approval; custom property Amount: 750. The fields, one per sentence,\n"
				+ "with the stored result and what each surface is expected to show (measured in the IF-field probe's eight rounds):\n\n"
				+ "sentence | stored (libraries; text models cached) | docx4j fields: update | Word open (as-is txt/docx) | Word PDF export (as-is) | Word F9\n"
				+ "1 Prepared in {DATE} | 2020 | 2020 (the WD never computes DATE) | 2026 | 2026 | 2026\n"
				+ "2 Net {IF 1=1 30 90} days | 90 | 30 | 30 (recomputed on open: the DATE triggers the pass) | 30 | 30\n"
				+ "3 Amount due ${IF 1=1 \"{DOCPROPERTY Amount}\" 0} | 500 (a reader flattening nesting prints 1500) | 750 | 500 (a nested-branch IF is not recomputed on open) | 500 | 750\n"
				+ "4 Discount {=10 as w:fldSimple}% | 10 (a reader skipping w:fldSimple prints nothing) | 10 | 10 | 10 | 10\n"
				+ "5 Supplier code matches {IF \"ACME-2026\" = \"ACME-*\" yes no} | no | yes | yes | yes | yes\n"
				+ "6 Subtotal check {= 2 + 2} | 5 | 4 | 5 (formulas are not recomputed on open) | 5 | 4\n"
				+ "7 Approval status {IF 1=1 approved rejected, locked} | rejected | rejected | rejected | rejected | rejected\n"
				+ "8 Printed on page {IF {PAGE} = 1 one \"two or later\"} | two or later | two or later (PAGE not computed) | two or later | one (the print updates PAGE first) | one\n"
				+ "9 Reviewer note {IF 1=1 \"<hidden>confidential\" none} | none | confidential (visible: a deviation) | (nothing: the open removes the result) | (nothing) | confidential as hidden text\n"
				+ "10 Reference {DOCPROPERTY Title} | Purchase approval | Purchase approval | Purchase approval | Purchase approval | Purchase approval\n\n"
				+ "Jason's Word steps: open one-file-many-readings.docx, answer No to any prompt, touch nothing; Save As Plain Text\n"
				+ "one-file-many-readings-word365-asis.txt, Save As one-file-many-readings-word365-asis.docx, Save As PDF one-file-many-readings-word365-asis.pdf;\n"
				+ "then Ctrl+A, F9, Save As Plain Text one-file-many-readings-word365-f9.txt and Save As PDF one-file-many-readings-word365-f9.pdf.\n";
		Files.write(new File(out, "README.txt").toPath(), readme.getBytes(StandardCharsets.UTF_8));
		System.out.println("wrote " + docx);
	}
}
