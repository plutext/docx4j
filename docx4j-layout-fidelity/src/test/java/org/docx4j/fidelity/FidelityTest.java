package org.docx4j.fidelity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.List;

import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.corpus.Corpus;
import org.junit.Test;

/**
 * End-to-end: generate the corpus, render it with docx4j+FOP, compare against a
 * golden directory and write the report to target/fidelity/report.
 *
 * The golden directory is given by the system property {@code docx4j.fidelity.golden}
 * (or the environment variable {@code DOCX4J_FIDELITY_GOLDEN}); without it the test
 * is skipped. Phase 0 asserts only that the pipeline ran; tolerances that fail the
 * build arrive with the phases that make them achievable.
 */
public class FidelityTest {

	@Test
	public void corpusGeneratesAndRenders() throws Exception {
		File corpus = new File("target/fidelity/corpus");
		Corpus.generate(corpus);
		assertTrue(new File(corpus, "corpus.txt").exists());
		Fidelity.render(corpus, new File("target/fidelity/fop"));
		for (org.docx4j.fidelity.corpus.Probe p : Corpus.all()) {
			assertTrue(p.id, new File("target/fidelity/fop", p.id + ".pdf").length() > 0);
		}
	}

	@Test
	public void compareAgainstGoldens() throws Exception {
		String golden = System.getProperty("docx4j.fidelity.golden", System.getenv("DOCX4J_FIDELITY_GOLDEN"));
		assumeTrue("no golden dir configured (-Ddocx4j.fidelity.golden=...)", golden != null && new File(golden).isDirectory());
		File corpus = new File("target/fidelity/corpus");
		Corpus.generate(corpus);
		File fop = new File("target/fidelity/fop");
		Fidelity.render(corpus, fop);
		List<LayoutComparison.Result> results = Fidelity.compare(new File(golden), fop, new File("target/fidelity/report"), 100);
		assertFalse(results.isEmpty());
		assertTrue(new File("target/fidelity/report/index.html").exists());
	}
}
