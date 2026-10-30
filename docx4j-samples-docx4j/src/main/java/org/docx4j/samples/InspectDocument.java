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
package org.docx4j.samples;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.Docx4J;
import org.docx4j.model.text.Group;
import org.docx4j.model.text.Inspect;
import org.docx4j.model.text.InspectFinding;
import org.docx4j.model.text.Inspection;
import org.docx4j.model.text.InspectionReport;
import org.docx4j.model.text.TextOptions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Inspect a document for content a reader of its text would not see on the page, and the other way
 * about (CR-050): hidden and white text, deleted text, unused headers, field codes, macros, external
 * links, embedded fonts drawing one letter as another, and so on. The inspection is read-only and
 * fetches nothing.
 *
 * <p>Three things are shown: the counts an agent would check before trusting an extraction; the
 * findings as objects, for a program; and the report for a person, written as Markdown beside the
 * input file.</p>
 *
 * <p>Usage: {@code InspectDocument [input.docx [report.md]]}. With no arguments it inspects
 * {@code sample-docs/sample-docxv2.docx} under the working directory and writes
 * {@code OUT_Inspection.md} there.</p>
 */
public class InspectDocument {

	public static void main(String[] args) throws Exception {

		String inputfilepath = args.length > 0 ? args[0] : System.getProperty("user.dir") + "/sample-docs/sample-docxv2.docx";
		String outputfilepath = args.length > 1 ? args[1] : System.getProperty("user.dir") + "/OUT_Inspection.md";

		WordprocessingMLPackage pkg = Docx4J.load(new File(inputfilepath));

		// 1. Inspect: every story (used or not), every part, every payload docx4j decodes.
		Inspection inspection = Inspect.of(pkg);

		// 2. The counts for the text an extraction gives under these options: the four numbers an
		// agent should read before acting on the text. Here, the plain-text extraction under the defaults.
		TextOptions options = new TextOptions();
		Inspection.Counts counts = inspection.counts(options, Inspection.Format.PLAIN);
		System.out.println(inspection.status() + ": " + inspection.findings().size() + " findings; for plain text under the defaults "
				+ counts + (counts.isLowerBound() ? " (lower bounds: something was skipped)" : ""));

		// 3. The findings as objects: here, only what Office would fetch or run, and what is in the
		// text but not seen by a person.
		for (InspectFinding f : inspection.findings()) {
			Group group = f.group(options, Inspection.Format.PLAIN, Inspection.capabilitiesOnClassPath());
			if (group == Group.ACTIVE || group == Group.UNSEEN) {
				System.out.println("  " + group + " " + f.getKind() + " at " + f.getLocation() + ": " + f.getExcerpt());
			}
		}
		for (Inspection.Entry skipped : inspection.skipped()) {
			System.out.println("  skipped " + skipped);
		}

		// 4. The report for a person, as Markdown. The one-argument form is for the plain-text
		// extraction under the default options; the long form takes the options, the format
		// (PLAIN, MARKDOWN or BLOCKS) and the capabilities the extraction had, so that its groups say
		// what that extraction's reader did and did not get.
		String report = InspectionReport.markdown(inspection, new File(inputfilepath).getName());
		Files.write(new File(outputfilepath).toPath(), report.getBytes(StandardCharsets.UTF_8));
		System.out.println("Report written to " + outputfilepath);

		// For an extraction that was run with hidden text included, in Markdown, with no HTML reader:
		// InspectionReport.markdown(inspection, new TextOptions().hidden(true), Inspection.Format.MARKDOWN,
		//         java.util.Collections.emptySet(), new File(inputfilepath).getName());
	}
}
