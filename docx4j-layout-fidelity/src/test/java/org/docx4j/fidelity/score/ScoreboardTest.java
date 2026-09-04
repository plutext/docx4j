package org.docx4j.fidelity.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.score.Scoreboard.Aggregate;
import org.docx4j.fidelity.score.Scoreboard.Row;
import org.junit.Test;

/**
 * The scoreboard arithmetic and formats, on synthetic {@link LayoutComparison.Result}
 * objects: no conversion, no PDFs, so this runs anywhere in milliseconds.
 */
public class ScoreboardTest {

	private static LayoutComparison.Result result(String id, int refPages, int candPages, int refLines, int matched) {
		LayoutComparison.Result r = new LayoutComparison.Result();
		r.id = id;
		r.refPages = refPages;
		r.candPages = candPages;
		r.refLines = refLines;
		r.candLines = refLines;
		r.matched = matched;
		r.matchedSamePage = matched;
		r.medianDy = 0.5;
		r.maxDy = 2.25;
		r.firstDivergence = "line break: ref p1 line has no match: \"a, \"quoted\" line\"";
		return r;
	}

	private static List<Row> scoreboard() {
		List<Row> rows = new ArrayList<>();
		rows.add(Row.of(result("12_en-AU_tbl_1", 2, 2, 100, 100), 1000)); // parity 1.00
		rows.add(Row.of(result("15_en-US_num_2", 3, 3, 100, 90), 2000)); // parity 0.90
		rows.add(Row.of(result("plain_3", 4, 5, 100, 50), 3000)); // parity 0.50, pages differ
		Row err = new Row("14_de-DE_sdt_4", 4000, "error");
		err.error = "NullPointerException: boom\nat some.Frame";
		rows.add(err);
		rows.add(new Row("15_fr-FR_fields_5", 5000, "noref"));
		return rows;
	}

	@Test
	public void compatModeIsTheLeadingNumber() {
		assertEquals("12", Scoreboard.compatMode("12_en-AU_fields7_num_tbl_11868"));
		assertEquals("", Scoreboard.compatMode("break-justified"));
		assertEquals("", Scoreboard.compatMode(""));
	}

	@Test
	public void aggregateCountsAndAverages() {
		Aggregate a = Aggregate.of(scoreboard());
		assertEquals(3, a.scored);
		assertEquals(1, a.errors);
		assertEquals(0, a.timeouts);
		assertEquals(1, a.noref);
		assertEquals(2, a.samePages); // the third document's page count differs
		assertEquals(240, a.linesMatched);
		assertEquals(300, a.linesTotal);
		assertEquals(0.80, a.lineRatio(), 1e-9);
		assertEquals(0.90, a.medianParity, 1e-9);
		assertEquals((1.0 + 0.9 + 0.5) / 3, a.meanParity, 1e-9);
		assertEquals(1, a.atLeastGood); // only the 1.00 document is >= 0.98
	}

	@Test
	public void csvRoundTripsIncludingQuotedFields() throws Exception {
		File csv = new File("target/fidelity/score-test/scoreboard.csv");
		List<Row> rows = scoreboard();
		Scoreboard.writeCsv(csv, rows);

		List<String> lines = java.nio.file.Files.readAllLines(csv.toPath());
		assertEquals(String.join(",", Scoreboard.HEADER), lines.get(0));
		assertEquals(rows.size() + 2, lines.size()); // header + rows + TOTAL
		assertTrue(lines.get(lines.size() - 1).startsWith("\"TOTAL\""));
		assertTrue("aggregate stashed in the TOTAL row", lines.get(lines.size() - 1).contains("scored=3"));
		for (String l : lines) assertFalse("no embedded newline", l.contains("\n"));

		List<Row> back = Scoreboard.readCsv(csv); // the TOTAL row is dropped
		assertEquals(rows.size(), back.size());
		assertEquals("12_en-AU_tbl_1", back.get(0).id);
		assertEquals("12", back.get(0).compatMode);
		assertEquals(1000, back.get(0).sizeBytes);
		assertEquals(1.0, back.get(0).lineParity, 1e-9);
		assertEquals(rows.get(0).firstDivergence, back.get(0).firstDivergence); // quotes survived
		assertEquals("error", back.get(3).status);
		assertEquals("NullPointerException: boom", back.get(3).error); // first line only
		assertEquals("noref", back.get(4).status);

		// and the aggregate recomputed from the re-read rows is the one we wrote
		assertEquals(Aggregate.of(rows).summary(), Aggregate.of(back).summary());
	}

	@Test
	public void deltaNamesRegressionsAndImprovements() throws Exception {
		File csv = new File("target/fidelity/score-test/baseline.csv");
		Scoreboard.writeCsv(csv, scoreboard());
		List<Row> before = Scoreboard.readCsv(csv);

		List<Row> after = new ArrayList<>();
		after.add(Row.of(result("12_en-AU_tbl_1", 2, 3, 100, 100), 1000)); // parity same, page count now differs
		after.add(Row.of(result("15_en-US_num_2", 3, 3, 100, 40), 2000)); // 0.90 -> 0.40 regression
		after.add(Row.of(result("plain_3", 4, 4, 100, 99), 3000)); // 0.50 -> 0.99 improvement
		after.add(Row.of(result("14_de-DE_sdt_4", 1, 1, 10, 10), 4000)); // error -> ok
		after.add(new Row("15_fr-FR_fields_5", 5000, "noref")); // unchanged, must not be listed
		after.add(Row.of(result("new_6", 1, 1, 10, 10), 6000)); // not in the baseline

		List<String> delta = Scoreboard.delta("baseline.csv", before, after);
		String text = String.join("\n", delta);

		assertTrue(text.startsWith("delta vs baseline.csv"));
		assertTrue("before/after columns", text.contains("before"));
		assertTrue("mean moved", text.contains("mean line parity"));
		assertTrue(text.contains("changed documents: 4 (2 regressions, 2 improvements)"));

		List<String> changes = new ArrayList<>();
		for (String l : delta) {
			if (l.startsWith("  REGRESSION") || l.startsWith("  improved")) changes.add(l);
		}
		assertEquals(4, changes.size());
		// worst drop first
		assertTrue(changes.get(0).startsWith("  REGRESSION"));
		assertTrue(changes.get(0).contains("15_en-US_num_2"));
		assertTrue(changes.get(0).contains("0.9000 -> 0.4000"));
		// a document that only changed page-count equality is still a regression
		assertTrue(changes.get(1).startsWith("  REGRESSION"));
		assertTrue(changes.get(1).contains("12_en-AU_tbl_1"));
		assertTrue(changes.get(1).contains("pages 2/2 -> 2/3"));
		assertTrue(changes.get(2).startsWith("  improved"));
		assertTrue(changes.get(2).contains("plain_3"));
		assertTrue(changes.get(3).startsWith("  improved"));
		assertTrue(changes.get(3).contains("14_de-DE_sdt_4"));

		assertTrue("status change flagged", text.contains("[error -> ok]"));
		assertTrue("new document reported", text.contains("not in the baseline: 1 [new_6]"));
		assertFalse("unchanged document not listed", text.contains("15_fr-FR_fields_5"));
	}

	@Test
	public void deltaAgainstItselfIsEmpty() {
		List<Row> rows = scoreboard();
		String text = String.join("\n", Scoreboard.delta("same.csv", rows, rows));
		assertTrue(text.contains("changed documents: 0 (0 regressions, 0 improvements)"));
		assertFalse(text.contains("not in the baseline"));
		assertFalse(text.contains("only in the baseline"));
	}

	@Test
	public void textReportPutsTheWorstDocumentsFirst() {
		List<String> report = Scoreboard.textReport(scoreboard(), null);
		assertEquals("documents scored     3", report.get(0));
		List<String> docLines = new ArrayList<>();
		for (String l : report) {
			if (l.contains("_") || l.startsWith("plain")) docLines.add(l);
		}
		assertEquals(5, docLines.size());
		assertTrue(docLines.get(0).startsWith("14_de-DE_sdt_4")); // failures first
		assertTrue(docLines.get(2).startsWith("plain_3")); // then 0.50
		assertTrue(docLines.get(4).startsWith("12_en-AU_tbl_1")); // best last
	}
}
