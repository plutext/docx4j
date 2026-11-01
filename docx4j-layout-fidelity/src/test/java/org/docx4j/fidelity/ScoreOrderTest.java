/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docx4j.fidelity.score.Scoreboard;
import org.junit.Test;

/**
 * The order documents are started in on more than one thread: longest first by the baseline
 * run's timings.csv, documents it does not know before all of them, largest first, across
 * corpora; and timings.csv itself, which a run writes for the next one to read.
 */
public class ScoreOrderTest {

	@Test
	public void longestFirstUnknownFirstAcrossCorpora() throws Exception {
		Path a = corpus("a", "small", 10, "big", 30, "new", 20);
		Path b = corpus("b", "slow", 5);
		Files.write(a.resolve("base/timings.csv"), Arrays.asList("id,status,ms", "small,ok,900", "big,ok,100"),
				StandardCharsets.UTF_8);
		Files.write(b.resolve("base/timings.csv"), Arrays.asList("id,status,ms", "slow,ok,5000"), StandardCharsets.UTF_8);
		List<Fidelity.Job> jobs = Arrays.asList(job(a), job(b));
		assertEquals("[small, new, big]", ids(jobs.get(0))); // the scoreboard's order: smallest first

		List<int[]> tasks = new ArrayList<>();
		for (int j = 0; j < jobs.size(); j++)
			for (int i = 0; i < jobs.get(j).docs.length; i++) tasks.add(new int[] { j, i });
		Fidelity.startOrder(jobs, tasks);
		List<String> started = new ArrayList<>();
		for (int[] t : tasks) started.add(jobs.get(t[0]).docs[t[1]].getName().replace(".docx", ""));
		assertEquals("[new, slow, small, big]", started.toString());
	}

	@Test
	public void timingsAreWrittenInTheScoreboardsOrderAndReadBack() throws Exception {
		Path dir = Files.createTempDirectory("timings");
		Scoreboard.Row ok = new Scoreboard.Row("one", 10, "ok");
		Scoreboard.Row noref = new Scoreboard.Row("two", 20, "noref");
		File csv = dir.resolve("timings.csv").toFile();
		Fidelity.writeTimings(csv, new Scoreboard.Row[] { ok, noref },
				new long[][] { { 1500, 1000, 200, 150, 50, 30 }, null });
		assertEquals(Arrays.asList("id,status,ms,renderMs,refExtractMs,candExtractMs,compareMs,classMs",
				"one,ok,1500,1000,200,150,50,30", "two,noref,,,,,,"),
				Files.readAllLines(csv.toPath(), StandardCharsets.UTF_8));
		assertEquals("{one=1500}", Fidelity.readTimings(csv).toString());
		assertEquals("{}", Fidelity.readTimings(dir.resolve("absent.csv").toFile()).toString());
	}

	/** A corpus directory of docx files of the given sizes (name, bytes, ...), and a base/ beside them. */
	private static Path corpus(String name, Object... docs) throws Exception {
		Path dir = Files.createTempDirectory("corpus-" + name);
		Files.createDirectories(dir.resolve("base"));
		for (int k = 0; k < docs.length; k += 2) {
			Files.write(dir.resolve(docs[k] + ".docx"), new byte[(Integer) docs[k + 1]]);
		}
		return dir;
	}

	private static Fidelity.Job job(Path corpus) {
		File dir = corpus.toFile();
		return new Fidelity.Job(dir, dir, new File(dir, "out"), new File(dir, "base/scoreboard.csv"), null);
	}

	private static String ids(Fidelity.Job job) {
		List<String> ids = new ArrayList<>();
		for (File f : job.docs) ids.add(f.getName().replace(".docx", ""));
		return ids.toString();
	}
}
