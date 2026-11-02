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
package org.docx4j.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** A run with worker JVMs over a small corpus, then the run again: nothing is surveyed twice. */
public class CoordinatorTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void runThenResume() throws Exception {
		Path corpus = tmp.newFolder("corpus").toPath();
		Path shard = Files.createDirectories(corpus.resolve("ab"));
		Fixtures.docx(shard, "good.docx", Fixtures.GOOD);
		Fixtures.docx(shard, "lenient.docx", Fixtures.LENIENT);
		Fixtures.docx(corpus, "mc.docx", Fixtures.MC_BODY);
		Files.write(corpus.resolve("notzip.docx"), "<html/>".getBytes(StandardCharsets.UTF_8));
		Files.write(corpus.resolve("ignored.txt"), "not a docx name".getBytes(StandardCharsets.UTF_8));
		Path out = tmp.getRoot().toPath().resolve("out");

		LoadSurvey.Options o = LoadSurvey.parse(new String[] { "run", "--corpus", corpus.toString(), "--out", out.toString(),
				"--workers", "2", "--heap", "512m", "--progress", "3600" });
		assertEquals(0, new Coordinator(o).run());

		Map<String, String> outcomes = outcomes(out);
		assertEquals(outcomes.toString(), 4, outcomes.size());
		assertEquals(outcomes.toString(), 3, outcomes.values().stream().filter("ok"::equals).count());
		assertEquals(outcomes.toString(), 1, outcomes.values().stream().filter("load_failed"::equals).count());
		assertEquals(Coordinator.shaOf(corpus.resolve("notzip.docx")) + " is the content's SHA-256",
				"load_failed", outcomes.get(Coordinator.shaOf(corpus.resolve("notzip.docx"))));
		assertTrue(Files.exists(out.resolve("summary.txt")));
		String sql = new String(Files.readAllBytes(out.resolve("import.sql")), StandardCharsets.UTF_8);
		assertTrue(sql, sql.contains(".import --skip 1 '" + out.toRealPath().resolve(Coordinator.MAIN) + "' docx4j_load_import"));
		List<String> messages = Files.readAllLines(out.resolve(Coordinator.MESSAGES));
		assertTrue("the lenient document's WARN", messages.size() > 1);

		// an unfinished line, as a run killed mid-write leaves: cut, and nothing surveyed twice
		Files.write(out.resolve(Coordinator.MAIN), "0123".getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);
		assertEquals(0, new Coordinator(o).run());
		assertEquals(5, Files.readAllLines(out.resolve(Coordinator.MAIN)).size());
		assertEquals(messages, Files.readAllLines(out.resolve(Coordinator.MESSAGES)));
	}

	@Test
	public void outInsideTheCorpusRefused() throws Exception {
		Path corpus = tmp.newFolder("corpus2").toPath();
		LoadSurvey.Options o = LoadSurvey.parse(new String[] { "run", "--corpus", corpus.toString(), "--out", corpus.resolve("out").toString() });
		try {
			new Coordinator(o).run();
			throw new AssertionError("ran");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("inside the corpus"));
		}
	}

	private static Map<String, String> outcomes(Path out) throws Exception {
		List<String> lines = Files.readAllLines(out.resolve(Coordinator.MAIN));
		assertEquals(String.join("\t", Column.names()), lines.get(0));
		Map<String, String> m = new HashMap<>();
		for (String l : lines.subList(1, lines.size())) {
			String[] f = Tsv.split(l);
			assertEquals(l, Column.values().length, f.length);
			m.put(f[0], f[Column.OUTCOME.ordinal()]);
		}
		return m;
	}
}
