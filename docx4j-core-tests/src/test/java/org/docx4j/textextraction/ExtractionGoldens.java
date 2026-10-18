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
 * and the manifest. A Markdown golden's image destinations are written {@code …}, as the comparison
 * reads them ({@link ExtractionConformance#imageDestinations}).</p>
 */
public final class ExtractionGoldens {

	static final String[] FIXTURES = { "extraction-probe.docx", "extraction-probe-external.docm" };
	static final String[] VIEWS = { "accepted", "original", "markup" };

	public static void main(String[] args) throws Exception {
		Path dir = Path.of(args[0]);
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
		Files.write(dir.resolve("goldens.tsv"), manifest, StandardCharsets.UTF_8);
	}

	/** UTF-8 without a BOM, LF, one terminal LF (GOLDENS.md). */
	private static void write(Path file, String text) throws Exception {
		String s = text.replace("\r\n", "\n");
		while (s.endsWith("\n")) s = s.substring(0, s.length() - 1);
		Files.writeString(file, s + "\n", StandardCharsets.UTF_8);
	}
}
