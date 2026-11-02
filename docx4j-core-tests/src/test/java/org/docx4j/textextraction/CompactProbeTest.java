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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

/**
 * The compact identification probe (text-extraction/compact, docx-tool-identifier CR-001 phase 2): the committed
 * files are what CompactProbeBuilder makes, every copied row reads in its compact file as in its source, and the
 * placements Word needs hold.
 */
public class CompactProbeTest {

	static Path resources() {
		return ExtractionConformanceTest.fixtures().getParent().getParent();
	}

	static Path compact() {
		return resources().resolve(CompactProbeBuilder.DIR);
	}

	/** The identifier copies these bytes: a rebuild that differs is a new probe, for Word's check again. */
	@Test public void committedFilesAreTheBuildersBytes() throws Exception {
		CompactProbeBuilder b = new CompactProbeBuilder(resources());
		assertArrayEquals(Files.readAllBytes(compact().resolve(CompactProbeBuilder.CONTENT)), b.content());
		assertArrayEquals(Files.readAllBytes(compact().resolve(CompactProbeBuilder.UPDATE)), b.update());
		assertEquals(Files.readString(compact().resolve("expected-tokens.tsv"), StandardCharsets.UTF_8), b.tokens());
		assertTrue(Files.isRegularFile(compact().resolve(CompactProbeBuilder.PRINTDATE)));
	}

	/** Every row passes as on its source fixture; case 36's HTML-reader rows pend there too. */
	@Test public void rowsReadAsInTheirSources() throws Exception {
		Path report = Path.of("target/text-extraction/compact.tsv");
		ExtractionConformance.run(compact(), report, ExtractionConformance.engine());
		if (ExtractionConformance.engine() == null) return;
		List<String> rows = Files.readAllLines(report, StandardCharsets.UTF_8);
		long pass = rows.stream().filter(r -> r.contains("\tPASS")).count();
		assertTrue("rows passed: " + pass, pass >= 200);
		for (String r : rows.subList(1, rows.size()))
			assertTrue(r, r.contains("\tPASS") || r.startsWith(CompactProbeBuilder.CONTENT + "\t36\tALT36_CHUNK_TEXT\t") && r.contains("html reader present"));
		assertTrue(rows.stream().anyMatch(r -> r.startsWith(CompactProbeBuilder.UPDATE + "\t6\tH₂O\t")));
		assertTrue(rows.stream().anyMatch(r -> r.startsWith(CompactProbeBuilder.CONTENT + "\t5\tROBERTS\t")));
	}

	/** WD-001: a run holding a character outside Windows-1252 starts before 246; R08, an IF over PAGE, on page 1. */
	@Test public void wordsPlacementsHold() throws Exception {
		byte[] content = Files.readAllBytes(compact().resolve(CompactProbeBuilder.CONTENT));
		CompactProbeBuilder.check(content);
		CompactProbeBuilder.check(Files.readAllBytes(compact().resolve(CompactProbeBuilder.UPDATE)));
		int r08 = -1, pageBreak = -1;
		for (Object[] run : CompactProbeBuilder.runStarts(content)) {
			if (r08 < 0 && ((String) run[1]).startsWith("R08_OVER_PAGE")) r08 = (Integer) run[0];
			if (pageBreak < 0 && ((String) run[1]).equals("BRK30_PAGE")) pageBreak = (Integer) run[0];
		}
		assertTrue(r08 + " " + pageBreak, r08 >= 0 && r08 < 400 && r08 < pageBreak);
		String xml = new String(CompactProbeBuilder.entry(content, "word/document.xml"), StandardCharsets.UTF_8);
		assertTrue("not before the first page break", xml.indexOf("R08_OVER_PAGE") < xml.indexOf("w:type=\"page\""));
		assertEquals("caps-lang5's literal H₂O alone in its file", xml.indexOf("H₂O"), xml.lastIndexOf("H₂O"));
	}
}
