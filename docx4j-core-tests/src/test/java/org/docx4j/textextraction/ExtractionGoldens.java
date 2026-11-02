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
package org.docx4j.textextraction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuts the published goldens of WD REQ-1203 (GOLDENS.md, "Published goldens"): for each fixture of
 * the suite, its plain text in the three views and its Markdown, as {@link TextsAdapter} writes them
 * under the defaults, and the manifest naming them. Run it, then review every file by hand against
 * WD sections 3 to 7 before committing it (CR-046 phase 3): a golden is what the ports are held to.
 *
 * <p>{@code ExtractionGoldens <fixture-directory>} writes into the directory, replacing the goldens
 * and the manifest's rows of scenario {@code default} (its other rows stay). A Markdown golden's image destinations are written {@code …}, as the comparison
 * reads them ({@link ExtractionConformance#imageDestinations}).</p>
 *
 * <p>{@code ExtractionGoldens <fixture-directory> html-reader-present}, run with an HTML reader on the
 * class path (jsoup and docx4j-ImportXHTML; {@link org.docx4j.model.text.HtmlChunkImport#readerPresent}),
 * cuts the second set, scenario {@value ExtractionConformance#HTML_READER_PRESENT}: a row for each golden of
 * the first, naming the default golden where the text is the same and a {@code <stem>.html-reader.*} file
 * where it is not (the chunks a reader expands), and keeps the manifest's other rows.  @since 17.3.6</p>
 */
public final class ExtractionGoldens {

	static final String[] FIXTURES = { "extraction-probe.docx", "extraction-probe-external.docm", ExtractionStructureCases.FIXTURE };
	static final String[] VIEWS = { "accepted", "original", "markup" };

	public static void main(String[] args) throws Exception {
		Path dir = Path.of(args[0]);
		if (args.length > 1 && args[1].equals("html-reader-present")) {
			readerPresent(dir);
			return;
		}
		TextsAdapter engine = new TextsAdapter();
		List<String> manifest = new ArrayList<String>();
		manifest.add("fixture\tcase\tview\tscenario\tformat\tgolden\tstatements");
		for (String fixture : FIXTURES) {
			String stem = fixture.substring(0, fixture.lastIndexOf('.'));
			for (String view : VIEWS) {
				String name = stem + "." + view + ".txt";
				write(dir.resolve(name), engine.extract(dir.resolve(fixture), view, "default", "txt").text);
				manifest.add(fixture + "\t0\t" + view + "\tdefault\ttxt\t" + name + "\tREQ-1203");
			}
			String name = stem + ".md";
			write(dir.resolve(name), ExtractionConformance.imageDestinations(
					engine.extract(dir.resolve(fixture), "accepted", "default", "md").text));
			manifest.add(fixture + "\t0\taccepted\tdefault\tmd\t" + name + "\tREQ-703, REQ-1203");
		}
		// the other scenarios' rows (the html reader present set) are cut by their own run
		Path existing = dir.resolve("goldens.tsv");
		if (Files.exists(existing)) for (String line : Files.readAllLines(existing, StandardCharsets.UTF_8)) {
			String[] cells = line.split("\t");
			if (cells.length > 3 && !cells[3].equals("scenario") && !cells[3].equals("default")) manifest.add(line);
		}
		Files.write(existing, manifest, StandardCharsets.UTF_8);
	}

	/** The second set: each golden again with an HTML reader present, a file of its own only where it differs. */
	static void readerPresent(Path dir) throws Exception {
		if (!org.docx4j.model.text.HtmlChunkImport.readerPresent())
			throw new IllegalStateException("no HTML reader on the class path (jsoup and docx4j-ImportXHTML)");
		TextsAdapter engine = new TextsAdapter();
		String scenario = ExtractionConformance.HTML_READER_PRESENT;
		List<String> manifest = new ArrayList<String>();
		for (String line : Files.readAllLines(dir.resolve("goldens.tsv"), StandardCharsets.UTF_8)) {
			if (!line.contains("\t" + scenario + "\t")) manifest.add(line);   // the default set stays as it is
		}
		for (String fixture : FIXTURES) {
			String stem = fixture.substring(0, fixture.lastIndexOf('.'));
			for (String view : VIEWS) {
				String text = engine.extract(dir.resolve(fixture), view, scenario, "txt").text;
				// a reader that is on the class path but cannot load (a missing dependency of the importer:
				// pdfbox, openhtmltopdf) expands nothing, and this set would silently be the first
				if (fixture.equals(FIXTURES[0]) && !text.contains("ALT36_CHUNK_TEXT"))
					throw new IllegalStateException("the HTML reader expanded no chunk (case 36's ALT36_CHUNK_TEXT is missing):"
							+ " is all of docx4j-ImportXHTML on the class path, its pdfbox and openhtmltopdf included?");
				String name = golden(dir, stem + "." + view + ".txt", stem + ".html-reader." + view + ".txt", text);
				manifest.add(fixture + "\t0\t" + view + "\t" + scenario + "\ttxt\t" + name + "\tREQ-1203");
			}
			String md = ExtractionConformance.imageDestinations(engine.extract(dir.resolve(fixture), "accepted", scenario, "md").text);
			String name = golden(dir, stem + ".md", stem + ".html-reader.md", md);
			manifest.add(fixture + "\t0\taccepted\t" + scenario + "\tmd\t" + name + "\tREQ-703, REQ-1203");
		}
		Files.write(dir.resolve("goldens.tsv"), manifest, StandardCharsets.UTF_8);
	}

	/** The default golden's name where this text is its text, else this text written under its own name. */
	private static String golden(Path dir, String defaultName, String ownName, String text) throws Exception {
		Path scratch = Files.createTempFile("golden", ".txt");
		try {
			write(scratch, text);
			if (java.util.Arrays.equals(Files.readAllBytes(scratch), Files.readAllBytes(dir.resolve(defaultName)))) {
				Files.deleteIfExists(dir.resolve(ownName));
				return defaultName;
			}
			Files.copy(scratch, dir.resolve(ownName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			return ownName;
		} finally {
			Files.deleteIfExists(scratch);
		}
	}

	/** UTF-8 without a BOM, LF, one terminal LF (GOLDENS.md). */
	private static void write(Path file, String text) throws Exception {
		String s = text.replace("\r\n", "\n");
		while (s.endsWith("\n")) s = s.substring(0, s.length() - 1);
		Files.writeString(file, s + "\n", StandardCharsets.UTF_8);
	}
}
