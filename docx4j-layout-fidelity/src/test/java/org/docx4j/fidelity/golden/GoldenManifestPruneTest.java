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
package org.docx4j.fidelity.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * A re-cut document's manifest lines replace its previous ones; everything else - the run
 * headers, the machine state, the run-level font summary, and the documents this run did
 * not touch - keeps its place.
 *
 * @since 17.2.0 (CR-001 batch 48)
 */
public class GoldenManifestPruneTest {

	private static File write(String... lines) throws Exception {
		File f = File.createTempFile("golden-manifest", ".properties");
		Files.write(f.toPath(), Arrays.asList(lines), StandardCharsets.UTF_8);
		return f;
	}

	private static List<String> read(File f) throws Exception {
		return Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
	}

	@Test
	public void aReCutDocumentsLinesReplaceItsPreviousOnes() throws Exception {
		File f = write(
				"# run one",
				"os=Linux",
				"connectedExperiences=unknown",
				"alpha.compatibilityMode=15",
				"alpha.generated=12:46",
				"alpha.fonts=Calibri",
				"alpha.resaved=12:46",
				"beta.generated=12:47",
				"beta.fonts=Cambria",
				"fonts=Calibri,Cambria",
				"# run two",
				"os=Linux",
				"connectedExperiences=on",
				"alpha.compatibilityMode=15",
				"alpha.generated=17:46",
				"alpha.fonts=Aptos",
				"fonts=Aptos");

		assertEquals("the three superseded lines of alpha", 3, WordGoldenRunner.pruneSupersededLines(f));

		List<String> kept = read(f);
		assertEquals("one generated line for alpha, the new one",
				Arrays.asList("alpha.generated=17:46"),
				kept.stream().filter(l -> l.startsWith("alpha.generated")).collect(java.util.stream.Collectors.toList()));
		assertEquals("and one fonts line", Arrays.asList("alpha.fonts=Aptos"),
				kept.stream().filter(l -> l.startsWith("alpha.fonts")).collect(java.util.stream.Collectors.toList()));
		assertTrue("the resaved line stays where the re-save was skipped",
				kept.contains("alpha.resaved=12:46"));
		assertTrue("a document this run did not touch is untouched", kept.contains("beta.generated=12:47"));
		assertTrue(kept.contains("beta.fonts=Cambria"));
		assertEquals("both run headers stay", 2, kept.stream().filter(l -> l.startsWith("# run")).count());
		assertEquals("as does every run-level line, one per run", 2,
				kept.stream().filter(l -> l.startsWith("connectedExperiences=")).count());
		assertEquals("including the run-level font summary", 2,
				kept.stream().filter(l -> l.equals("fonts=Calibri,Cambria") || l.equals("fonts=Aptos")).count());
	}

	/** A re-cut which failed deletes the PDF, so the lines describing the one it replaced go. */
	@Test
	public void aFailedReCutTakesTheLinesOfThePdfItDeleted() throws Exception {
		File f = write(
				"alpha.generated=12:46",
				"alpha.fonts=Calibri",
				"alpha.resaved=12:46",
				"alpha.FAILED=Word produced an empty PDF");

		assertEquals(2, WordGoldenRunner.pruneSupersededLines(f));
		List<String> kept = read(f);
		assertEquals(Arrays.asList("alpha.resaved=12:46", "alpha.FAILED=Word produced an empty PDF"), kept);
	}

	@Test
	public void aManifestWithNothingSupersededIsLeftAlone() throws Exception {
		File f = write("# run one", "os=Linux", "alpha.generated=12:46", "fonts=Calibri");
		long before = f.lastModified();
		assertEquals(0, WordGoldenRunner.pruneSupersededLines(f));
		assertEquals("not rewritten", before, f.lastModified());
		assertEquals(4, read(f).size());
	}

	@Test
	public void aMissingManifestIsNotAnError() throws Exception {
		assertEquals(0, WordGoldenRunner.pruneSupersededLines(new File("no-such-manifest.properties")));
		assertEquals(0, WordGoldenRunner.pruneSupersededLines(null));
	}
}
