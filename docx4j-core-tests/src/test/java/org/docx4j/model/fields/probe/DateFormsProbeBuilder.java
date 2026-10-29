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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * The date-forms probe (the portfolio's date rule, sub-item 1; CR-067 §12): how Word reads a date-only value
 * ({@code 2024-02-03}), a local time with no offset ({@code 2025-06-07T08:09:10}) and a UTC value
 * ({@code 2025-01-02T03:04:05Z}) in the three places a document holds a date: a core property (created,
 * modified, lastPrinted, read by CREATEDATE, SAVEDATE, PRINTDATE), a revision's {@code w:date}, and a bound
 * date control's stored value. Not a test: {@code java DateFormsProbeBuilder <outDir>}.
 */
public class DateFormsProbeBuilder {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	static final String DATE_ONLY = "2024-02-03", LOCAL = "2025-06-07T08:09:10", UTC = "2025-01-02T03:04:05Z";

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		core.setCreated(w3cdtf(DATE_ONLY));
		core.setModified(w3cdtf(LOCAL));
		core.setLastPrinted(javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(UTC));

		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>Date forms probe (CR-067 D12)</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">Three stored forms: a date only (" + DATE_ONLY + "), a local time with no offset (" + LOCAL + "), a UTC time (" + UTC + "). Each in a core property, a revision and a bound date control.</w:t></w:r></w:p>");

		// 1. core properties through fields, each with a \@ switch that shows date and time
		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t>Part 1: core properties (Ctrl+A, F9 first)</w:t></w:r></w:p>");
		add(mdp, field("D01_CREATEDATE_DATEONLY", "CREATEDATE \\@ \"yyyy-MM-dd HH:mm:ss\""));
		add(mdp, field("D02_SAVEDATE_LOCAL", "SAVEDATE \\@ \"yyyy-MM-dd HH:mm:ss\""));
		add(mdp, field("D03_PRINTDATE_UTC", "PRINTDATE \\@ \"yyyy-MM-dd HH:mm:ss\""));
		add(mdp, field("D04_CREATEDATE_DEFAULT", "CREATEDATE"));
		add(mdp, field("D05_SAVEDATE_DEFAULT", "SAVEDATE"));
		add(mdp, field("D06_PRINTDATE_DEFAULT", "PRINTDATE"));

		// 2. revisions: an insertion per form; Jason reads the date Word shows in the balloon or the Reviewing Pane
		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t>Part 2: revisions (hover each insertion, or open the Reviewing Pane; note the date and time Word shows)</w:t></w:r></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">R01 date only: </w:t></w:r><w:ins w:id=\"101\" w:author=\"Probe\" w:date=\"" + DATE_ONLY + "\"><w:r><w:t>INSERTED_R01</w:t></w:r></w:ins></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">R02 local, no offset: </w:t></w:r><w:ins w:id=\"102\" w:author=\"Probe\" w:date=\"" + LOCAL + "\"><w:r><w:t>INSERTED_R02</w:t></w:r></w:ins></w:p>");
		add(mdp, "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">R03 UTC: </w:t></w:r><w:ins w:id=\"103\" w:author=\"Probe\" w:date=\"" + UTC + "\"><w:r><w:t>INSERTED_R03</w:t></w:r></w:ins></w:p>");

		// 3. bound date controls: a store item per form is heavier than it needs to be; the stored value is the control's own w:date fullDate and its text
		add(mdp, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t>Part 3: date controls (click each; note the date and time it shows and what the picker has selected)</w:t></w:r></w:p>");
		add(mdp, dateControl("B01_DATEONLY", DATE_ONLY, 201));
		add(mdp, dateControl("B02_LOCAL", LOCAL, 202));
		add(mdp, dateControl("B03_UTC", UTC, 203));

		File docx = new File(out, "date-forms-r1.docx");
		pkg.save(docx);
		String readme = "CR-067 D12 date forms probe: how Word reads a date-only, an offset-free and a UTC date (one file, about 5 minutes)\n\n"
				+ "1. Open date-forms-r1.docx normally. Note the Word build.\n"
				+ "2. Part 1: Ctrl+A, F9. For D01 to D06 write down the text each field shows.\n"
				+ "   Predicted: D01 2024-02-03 00:00:00 (a date only is midnight); D02 2025-06-07 08:09:10 as written (no offset: local);\n"
				+ "   D03 the UTC time shifted to your zone (03:04:05Z -> 14:04:05 in Sydney); D04 to D06 the same values in Word's default format.\n"
				+ "   Unknown: whether Word drops the seconds here too (it read a file's :06 as :00 in the phase 1 probe).\n"
				+ "3. Part 2: hover each inserted text (or Review > Reviewing Pane) and write down the date and time Word shows for R01, R02, R03.\n"
				+ "   Predicted: R01 2024-02-03 (midnight or no time); R02 2025-06-07 8:09 AM as written; R03 the Z read as local clock, 2025-01-02 3:04 AM\n"
				+ "   (Word means w:date as local wall-clock whatever its Z says, per CR-043), or shifted to your zone if Word converts it.\n"
				+ "4. Part 3: click each date control; write down the text shown and the date the picker highlights for B01, B02, B03.\n"
				+ "   Predicted (CR-058 D6): B01 2024-02-03 00:00; B02 2025-06-07 08:09 nominal; B03 the Z converted to your zone, 2025-01-02 14:04 in Sydney.\n"
				+ "5. Save As date-forms-r1-word365.docx beside this README; put your notes in results.txt here.\n"
				+ "Core properties: created " + DATE_ONLY + " (date only), modified " + LOCAL + " (no offset), lastPrinted " + UTC + ".\n";
		try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(out, "README.txt")), StandardCharsets.UTF_8)) {
			w.write(readme);
		}
		System.out.println("wrote " + docx + " and README.txt");
	}

	static SimpleLiteral w3cdtf(String value) {
		org.docx4j.docProps.core.dc.terms.W3CDTF w = new org.docx4j.docProps.core.dc.terms.W3CDTF();
		w.getContent().add(value);
		return w;
	}

	static void add(MainDocumentPart mdp, String xml) throws Exception {
		mdp.getContent().add(XmlUtils.unmarshalString(xml));
	}

	static String field(String token, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> " + instr.replace("\"", "&quot;") + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>OLD</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
	}

	/** A date control whose w:date fullDate holds the form; its text is the form as written, for Word to re-show. */
	static String dateControl(String token, String value, int id) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + token + " = </w:t></w:r>"
				+ "<w:sdt><w:sdtPr><w:id w:val=\"" + id + "\"/><w:date w:fullDate=\"" + value + "\"><w:dateFormat w:val=\"yyyy-MM-dd HH:mm\"/><w:lid w:val=\"en-US\"/><w:storeMappedDataAs w:val=\"dateTime\"/><w:calendar w:val=\"gregorian\"/></w:date></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t>" + value + "</w:t></w:r></w:sdtContent></w:sdt></w:p>";
	}
}
