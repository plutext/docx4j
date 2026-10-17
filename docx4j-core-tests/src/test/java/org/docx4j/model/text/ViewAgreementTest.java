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

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * WD REQ-406 and REQ-1202 (CR-046 section 2.3): the view walk's {@code accepted} and
 * {@code original} text equal to the text of a copy resolved by Accept All or Reject All
 * ({@link TrackedChanges}) and read with no view logic ({@link Texts#ofResolved}), over CR-043's
 * fixtures ({@code revisions/}) and the probe ({@code text-extraction/}), in the default scope and
 * with every story and content class included. The copy is the reference: where they differ, the
 * walk is wrong.
 *
 * <p>The copy is the fixture loaded a second time, not {@code OpcPackage.clone()}: a save drops the
 * declaration of a {@code Requires} prefix bound to an unconventional namespace (probe case 6), so a
 * saved copy would select another branch.</p>
 */
public class ViewAgreementTest {

	static Path resources() {
		for (Path dir : List.of(Path.of("src/test/resources"), Path.of("docx4j-core-tests/src/test/resources")))
			if (Files.isDirectory(dir.resolve("revisions"))) return dir;
		throw new IllegalStateException("Run from the repository root or docx4j-core-tests");
	}

	static List<Path> fixtures() throws Exception {
		List<Path> files = new ArrayList<Path>();
		for (String root : List.of("revisions", "text-extraction")) {
			try (Stream<Path> s = Files.walk(resources().resolve(root))) {
				files.addAll(s.filter(p -> p.toString().endsWith(".docx")).sorted().collect(Collectors.toList()));
			}
		}
		return files;
	}

	@Test
	public void walkEqualsResolvedCopy() throws Exception {
		List<Path> files = fixtures();
		assertTrue(files.size() > 100);
		List<String> differences = new ArrayList<String>();
		for (Path f : files) {
			for (TextView view : new TextView[] { TextView.ACCEPTED, TextView.ORIGINAL }) {
				for (boolean all : new boolean[] { false, true }) {
					TextOptions options = TextOptions.of(view);
					if (all) options.include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS).comments(true).hidden(true).fieldCodes(true);
					String walk = PlainText.of(Texts.of(WordprocessingMLPackage.load(f.toFile()), options));
					WordprocessingMLPackage copy = WordprocessingMLPackage.load(f.toFile());
					if (view == TextView.ACCEPTED) TrackedChanges.acceptAll(copy);
					else TrackedChanges.rejectAll(copy);
					String reference = PlainText.of(Texts.ofResolved(copy, options));
					if (!walk.equals(reference)) differences.add(f.getFileName() + " " + view + (all ? " (all stories)" : "") + firstDifference(walk, reference));
				}
			}
		}
		assertTrue(differences.size() + " differences:\n" + String.join("\n", differences), differences.isEmpty());
	}

	private static String firstDifference(String walk, String reference) {
		String[] w = walk.split("\n", -1), r = reference.split("\n", -1);
		int i = 0;
		while (i < Math.min(w.length, r.length) && w[i].equals(r[i])) i++;
		return ": line " + (i + 1) + "\n  walk: " + (i < w.length ? w[i] : "<end>") + "\n  copy: " + (i < r.length ? r[i] : "<end>");
	}
}
