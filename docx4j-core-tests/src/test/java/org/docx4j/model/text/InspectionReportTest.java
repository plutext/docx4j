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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * CR-050 phase 5: the Markdown report of an inspection carries every finding under its group, with the
 * counts the inspection gives, the skipped entries and the limitations; and nothing taken from the
 * document can form Markdown of its own.
 */
public class InspectionReportTest {

	private static final Pattern HEADING = Pattern.compile("^## (Active|Unseen|Omitted|Info) \\((\\d+)\\)", Pattern.MULTILINE);

	private static int count(String report, String group) {
		Matcher m = HEADING.matcher(report);
		while (m.find()) if (m.group(1).equals(group)) return Integer.parseInt(m.group(2));
		throw new AssertionError("No " + group + " heading in\n" + report);
	}

	/** The section of a group: from its heading to the next. */
	private static String section(String report, String group) {
		int start = report.indexOf("\n## " + group + " (");
		assertTrue(group, start >= 0);
		int end = report.indexOf("\n## ", start + 1);
		return report.substring(start, end < 0 ? report.length() : end);
	}

	@Test
	public void everyFixtureReports() throws Exception {
		Set<Inspection.Capability> caps = Inspection.capabilitiesOnClassPath();
		for (String name : InspectTest.FIXTURES) {
			Inspection i = Inspect.of(InspectTest.fixture(name));
			String report = InspectionReport.markdown(i, name);
			assertTrue(name, report.startsWith("# Inspection: " + name + "\n"));
			assertTrue(name, report.contains(i.status() == Inspection.Status.COMPLETE ? "**Complete**" : "**Partial**"));
			Inspection.Counts counts = i.counts(new TextOptions(), Inspection.Format.PLAIN, caps);
			assertEquals(name, counts.getActive(), count(report, "Active"));
			assertEquals(name, counts.getUnseen(), count(report, "Unseen"));
			assertEquals(name, counts.getOmitted(), count(report, "Omitted"));
			assertEquals(name, counts.getInfo(), count(report, "Info"));
			assertEquals(name, i.findings().size(), counts.getActive() + counts.getUnseen() + counts.getOmitted() + counts.getInfo());
			for (InspectFinding f : i.findings()) {
				String g = f.group(new TextOptions(), Inspection.Format.PLAIN, caps).name();
				String section = section(report, g.charAt(0) + g.substring(1).toLowerCase());
				assertTrue(name + ": " + f + " under " + g, section.contains("- `" + f.getKind() + "`"));
			}
			assertTrue(name, report.contains("\n## Skipped and undecided\n"));
			for (Inspection.Entry e : i.skipped()) assertTrue(name + ": " + e, report.contains("- skipped: `" + e.getReason() + "` at `" + e.getLocation() + "`"));
			assertTrue(name, report.contains("\n## Not detected\n"));
			for (String l : Inspection.LIMITATIONS) assertTrue(name + ": " + l, report.contains("- `" + l.substring(0, l.indexOf(": ")) + "`: "));
			assertFalse(name, report.contains("\r"));
		}
	}

	/** Hidden text is omitted under the defaults, with its reveal; under {@code hidden: true} it is unseen. */
	@Test
	public void hiddenTextUnderItsGroup() throws Exception {
		InspectTest.Doc d = new InspectTest.Doc();
		d.p(InspectTest.run("Shown. ") + InspectTest.hidden("SECRET_TEXT"));
		WordprocessingMLPackage pkg = d.done();
		Inspection i = Inspect.of(pkg);
		String report = InspectionReport.markdown(i);
		assertTrue(report, report.startsWith("# Inspection\n"));
		String omitted = section(report, "Omitted");
		assertTrue(omitted, omitted.contains("- `text.hidden` (hidden text), in `/word/document.xml` at `"));
		assertTrue(omitted, omitted.contains("`SECRET_TEXT`; visible: no; reveal: extract with `hidden: true`"));
		assertFalse(section(report, "Unseen").contains("SECRET_TEXT"));

		String revealed = InspectionReport.markdown(i, new TextOptions().hidden(true), Inspection.Format.PLAIN, Inspection.capabilitiesOnClassPath(), "built");
		assertTrue(revealed, section(revealed, "Unseen").contains("`SECRET_TEXT`"));
		assertFalse(section(revealed, "Omitted").contains("SECRET_TEXT"));
		assertTrue(revealed, revealed.contains("under the default options with `hidden=true`:"));
		assertTrue(report, report.contains("under the default options:"));
	}

	/** An excerpt holding backticks, asterisks or a newline stays one code span. */
	@Test
	public void excerptsCannotFormMarkdown() throws Exception {
		InspectTest.Doc d = new InspectTest.Doc();
		d.p(InspectTest.hidden("a ``double`` and *stars* and `one`"));
		String report = InspectionReport.markdown(Inspect.of(d.done()));
		assertTrue(report, report.contains("``` a ``double`` and *stars* and `one` ```"));
		assertEquals("``` `` ```", InspectionReport.code("``"));
		assertEquals("`a b`", InspectionReport.code("a\nb"));
		assertEquals("` `", InspectionReport.code(""));
		assertEquals("\\*x\\* \\<y\\>", InspectionReport.plain("*x* <y>"));
		assertEquals("U+200B U+0041", InspectionReport.codePoints("\u200bA"));
		assertEquals(null, InspectionReport.codePoints("plain\nlines"));
	}

	/** A report a limit stopped says so, and its counts read as lower bounds. */
	@Test
	public void partialReportSaysSo() throws Exception {
		WordprocessingMLPackage pkg = InspectTest.fixture("extraction-probe-inspect.docx");
		Inspection stopped = Inspect.of(pkg, new InspectRun(3, Inspect.MAX_PAYLOAD_BYTES, Inspect.MAX_TOTAL_BYTES, Inspect.MAX_ELEMENTS));
		assertEquals(Inspection.Status.PARTIAL, stopped.status());
		String report = InspectionReport.markdown(stopped);
		assertTrue(report, report.contains("**Partial**"));
		assertTrue(report, report.contains("(lower bounds)"));
		assertTrue(report, report.contains("- skipped: `" + Inspection.LIMIT_FINDINGS + "`"));
	}
}
