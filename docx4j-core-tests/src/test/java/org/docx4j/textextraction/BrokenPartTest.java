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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * The broken-part probe family (text-extraction/broken-part, docx-tool-identifier CR-001 section 3c): its token table
 * is Word 365's reading of each file, every row docx4j reads as Word does passes, and each other row pends on the open
 * item or defect its statements name.
 */
public class BrokenPartTest {

	static Path dir() {
		return CompactProbeTest.resources().resolve("text-extraction/broken-part");
	}

	/** build_broken_part.py's labels, the rows docx4j reads otherwise today (README.md) */
	static final List<String> LABELS = List.of("WD 0.72 (2)", "WD 0.72 (3)");

	/** Every with_option is the token's count in Word's own save of the file, as ExtractionConformance counts it. */
	@Test public void withOptionIsWordsCount() throws Exception {
		List<Map<String, String>> rows = ExtractionConformance.table(dir().resolve("expected-tokens.tsv"));
		Set<String> suite = new LinkedHashSet<>();
		for (Map<String, String> row : rows) suite.add(row.get("token"));
		int checked = 0;
		for (Map<String, String> row : rows) {
			if (row.get("option").isEmpty()) continue;
			assertEquals("profile: word-text; list_labels: true; fields: update", row.get("option"));
			byte[] save = Files.readAllBytes(dir().resolve(row.get("fixture").replace(".docx", "-word365-utf8.txt")));
			assertTrue("UTF-8 with a byte order mark", (save[0] & 0xff) == 0xEF && (save[1] & 0xff) == 0xBB && (save[2] & 0xff) == 0xBF);
			String word = new String(save, 3, save.length - 3, StandardCharsets.UTF_8);
			assertEquals(row.get("fixture") + " " + row.get("token"), Integer.parseInt(row.get("with_option")),
					ExtractionConformance.count(word, row.get("token"), suite));
			checked++;
		}
		assertEquals(213, checked);
	}

	/** docx4j passes every row it reads as Word does; the rest pend on their label, none on a missing statement. */
	@Test public void rowsPassOrPendOnTheirLabel() throws Exception {
		Path report = Path.of("target/text-extraction/broken-part.tsv");
		ExtractionConformance.run(dir(), report, ExtractionConformance.engine());
		if (ExtractionConformance.engine() == null) return;
		List<String> rows = Files.readAllLines(report, StandardCharsets.UTF_8);
		int pass = 0, pending = 0;
		for (String r : rows.subList(1, rows.size())) {
			if (r.contains("\tPASS\t")) { pass++; continue; }
			assertTrue(r, r.contains("\tPENDING\tphase 2; ") && LABELS.stream().anyMatch(r::contains));
			pending++;
		}
		assertEquals(1185, pass);
		assertEquals(126, pending);
		String word = "\tprofile: word-text; list_labels: true; fields: update\t";
		assertTrue(rows.contains("broken-part-styles-late.docx\t110\twarning:part.not_read:/word/styles.xml\tmarkup\tdefault\tPASS\t"));
		assertTrue(rows.contains("broken-part-numbering-order.docx\t108\twarning:part.not_read:/word/numbering.xml\taccepted\tdefault\tPASS\t"));
		assertTrue(rows.contains("broken-part-styles.docx\t101\tDIRECT_LIST_1. first item\taccepted" + word + "PASS\t"));
		assertTrue(rows.contains("store-not-read-intact.docx\t122\tSTORE1_VALUE\taccepted" + word + "PASS\t"));
		assertTrue(rows.contains("broken-part-numbering-order.docx\t108\t3. heading two\taccepted" + word + "PENDING\tphase 2; WD 0.72 (2)"));
	}

	/** Word will not open a main part that is not well formed; docx4j loads the package and refuses to read it (REQ-801). */
	@Test public void mainPartBreaksAreRefused() throws Exception {
		for (String fixture : List.of("broken-part-document-entity.docx", "broken-part-document-encoding.docx",
				"broken-part-document-truncated.docx")) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(dir().resolve(fixture).toFile());
			try {
				Texts.of(pkg, TextOptions.of(TextView.ACCEPTED));
				fail(fixture + " read");
			} catch (Docx4JRuntimeException e) {
				assertTrue(fixture + ": " + e.getMessage(), e.getMessage().contains("/word/document.xml"));
			}
		}
	}
}
