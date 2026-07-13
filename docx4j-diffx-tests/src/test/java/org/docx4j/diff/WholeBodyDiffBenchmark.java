package org.docx4j.diff;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Body;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.pageseeder.diffx.api.DiffHandler;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.core.DefaultXMLProcessor;
import org.pageseeder.diffx.core.OptimisticXMLProcessor;
import org.pageseeder.diffx.load.DOMLoader;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.xml.Sequence;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Phase 5 of the pso-diffx migration (see docx4j-diffx/PAGESEEDER-MIGRATION.md):
 * benchmark whole-body diffs without Docx4jDriver's divide-and-conquer
 * splitter, to find out whether the splitter (and the bundled
 * org.eclipse.compare LCS it depends on) is still needed now that Diff-X
 * has Myers greedy algorithms.
 *
 * Not a JUnit test (surefire only picks up *Test); run manually:
 *
 *   mvn compile -pl docx4j-diffx -DskipTests -Dgpg.skip=true install
 *   mvn test-compile exec:java -pl docx4j-diffx-tests \
 *     -Dexec.mainClass=org.docx4j.diff.WholeBodyDiffBenchmark \
 *     -Dexec.classpathScope=test -Dgpg.skip=true
 *
 * Contenders, each timed from a marshalled w:body DOM element pair:
 *  - splitter:  Docx4jDriver.diff (production path: top-level LCS via
 *               org.eclipse.compare, MatrixXMLAlgorithm per changed range)
 *  - optimistic: whole-body OptimisticXMLProcessor (Myers greedy, falls
 *               back to the matrix if the fast result is not well-formed)
 *  - matrix:    whole-body DefaultXMLProcessor (MatrixXMLAlgorithm); expected
 *               to refuse large inputs (comparison-count threshold)
 *
 * The document generator is seeded, so runs are reproducible.
 */
public class WholeBodyDiffBenchmark {

	static final int PARAGRAPHS = 500;
	static final int WARMUP = 2;
	static final int REPS = 5;

	static final String[] VOCAB = ("the quick brown fox jumped over lazy dog while carefully considering "
			+ "whether document differencing algorithms scale gracefully under realistic word processing "
			+ "workloads including tracked changes tables numbering fields hyperlinks images and styles "
			+ "because performance matters when comparing large contracts reports theses manuals and "
			+ "specifications produced by collaborating authors across many revisions").split(" ");

	static final ObjectFactory WML = new ObjectFactory();

	// ---- document model: a paragraph is a list of runs; a run is (text, bold) ----

	static class Run {
		String text; boolean bold;
		Run(String text, boolean bold) { this.text = text; this.bold = bold; }
		Run copy() { return new Run(text, bold); }
	}

	static List<List<Run>> generateBase() {
		Random r = new Random(42);
		List<List<Run>> doc = new ArrayList<>();
		for (int i = 0; i < PARAGRAPHS; i++) {
			int words = 8 + r.nextInt(18);
			StringBuilder sb = new StringBuilder();
			for (int w = 0; w < words; w++) {
				if (w > 0) sb.append(' ');
				sb.append(VOCAB[r.nextInt(VOCAB.length)]);
			}
			String text = sb.toString();
			// fragment into 1-5 runs, as Word tends to
			int runs = 1 + r.nextInt(5);
			List<Run> para = new ArrayList<>();
			int from = 0;
			for (int k = 0; k < runs; k++) {
				int to = (k == runs - 1) ? text.length()
						: Math.min(text.length(), from + 1 + r.nextInt(Math.max(1, (text.length() - from) / (runs - k))));
				if (to > from) {
					para.add(new Run(text.substring(from, to), r.nextInt(6) == 0));
					from = to;
				}
			}
			doc.add(para);
		}
		return doc;
	}

	static List<List<Run>> copy(List<List<Run>> doc) {
		List<List<Run>> out = new ArrayList<>();
		for (List<Run> p : doc) {
			List<Run> q = new ArrayList<>();
			for (Run run : p) q.add(run.copy());
			out.add(q);
		}
		return out;
	}

	/** Word-level edit inside ~pct% of paragraphs. */
	static List<List<Run>> editWords(List<List<Run>> base, int pct, long seed) {
		Random r = new Random(seed);
		List<List<Run>> doc = copy(base);
		for (List<Run> para : doc) {
			if (r.nextInt(100) >= pct) continue;
			Run run = para.get(r.nextInt(para.size()));
			String[] words = run.text.split(" ");
			int at = r.nextInt(words.length);
			switch (r.nextInt(3)) {
			case 0: words[at] = VOCAB[r.nextInt(VOCAB.length)]; break;          // replace
			case 1: words[at] = words[at] + " " + VOCAB[r.nextInt(VOCAB.length)]; break; // insert
			default: words[at] = ""; break;                                     // delete
			}
			run.text = String.join(" ", words).replaceAll("  +", " ").trim();
		}
		return doc;
	}

	/** Delete 10 paragraphs, insert 10 new ones, move a block of 20. */
	static List<List<Run>> structural(List<List<Run>> base, long seed) {
		Random r = new Random(seed);
		List<List<Run>> doc = copy(base);
		for (int i = 0; i < 10; i++) doc.remove(r.nextInt(doc.size()));
		for (int i = 0; i < 10; i++) {
			List<Run> p = new ArrayList<>();
			p.add(new Run("Newly inserted paragraph number " + i + " with some fresh content.", false));
			doc.add(r.nextInt(doc.size()), p);
		}
		int from = r.nextInt(doc.size() - 25);
		List<List<Run>> block = new ArrayList<>(doc.subList(from, from + 20));
		doc.subList(from, from + 20).clear();
		doc.addAll(r.nextInt(doc.size()), block);
		return doc;
	}

	static Body toBody(List<List<Run>> doc) {
		Body body = WML.createBody();
		for (List<Run> para : doc) {
			P p = WML.createP();
			for (Run run : para) {
				R r = WML.createR();
				if (run.bold) {
					RPr rPr = WML.createRPr();
					BooleanDefaultTrue b = WML.createBooleanDefaultTrue();
					rPr.setB(b);
					r.setRPr(rPr);
				}
				Text t = WML.createText();
				t.setValue(run.text);
				t.setSpace("preserve");
				r.getContent().add(t);
				p.getContent().add(r);
			}
			body.getContent().add(p);
		}
		return body;
	}

	static Element toElement(Body body) {
		return XmlUtils.marshaltoW3CDomDocument(body).getDocumentElement();
	}

	// ---- contenders ----

	interface Contender {
		String run(Element newer, Element older) throws Exception;
	}

	static final Contender SPLITTER = (newer, older) -> {
		StringWriter sw = new StringWriter();
		Docx4jDriver.diff(newer, older, sw);
		return sw.toString();
	};

	static String wholeBody(Element newer, Element older, boolean optimistic) throws Exception {
		DOMLoader loader = new DOMLoader();
		loader.setConfig(Docx4jDriver.legacyConfig());
		Sequence seq1 = loader.load(newer);
		Sequence seq2 = loader.load(older);
		StringWriter sw = new StringWriter();
		LegacyDiffOutput output = new LegacyDiffOutput(sw, true);
		output.addNamespaces(seq1.getNamespaces());
		output.addNamespaces(seq2.getNamespaces());
		DiffHandler<XMLToken> flipped = new DiffHandler<XMLToken>() {
			public void start() { output.start(); }
			public void handle(Operator op, XMLToken t) { output.handle(op.flip(), t); }
			public void end() { output.end(); }
		};
		if (optimistic) {
			new OptimisticXMLProcessor().diff(seq1.tokens(), seq2.tokens(), flipped);
		} else {
			new DefaultXMLProcessor().diff(seq1.tokens(), seq2.tokens(), flipped);
		}
		return sw.toString();
	}

	static final Contender OPTIMISTIC = (n, o) -> wholeBody(n, o, true);
	static final Contender MATRIX = (n, o) -> wholeBody(n, o, false);

	// ---- measurement ----

	static void bench(String scenario, List<List<Run>> newerModel, List<List<Run>> olderModel) throws Exception {

		Element newerForSplitter = toElement(toBody(newerModel));
		Element olderForSplitter = toElement(toBody(olderModel));
		Element newerForWhole = toElement(toBody(newerModel));
		Element olderForWhole = toElement(toBody(olderModel));

		DOMLoader loader = new DOMLoader();
		loader.setConfig(Docx4jDriver.legacyConfig());
		int tokens1 = loader.load(newerForWhole).size();
		int tokens2 = loader.load(olderForWhole).size();
		System.out.printf("%n=== %s  (%d vs %d tokens) ===%n", scenario, tokens1, tokens2);

		run("splitter  ", SPLITTER, newerForSplitter, olderForSplitter);
		run("optimistic", OPTIMISTIC, newerForWhole, olderForWhole);
		run("matrix    ", MATRIX, newerForWhole, olderForWhole);
	}

	static void run(String name, Contender contender, Element newer, Element older) {
		try {
			String out = null;
			for (int i = 0; i < WARMUP; i++) out = contender.run(newer, older);
			long[] times = new long[REPS];
			for (int i = 0; i < REPS; i++) {
				long t0 = System.nanoTime();
				out = contender.run(newer, older);
				times[i] = (System.nanoTime() - t0) / 1_000_000;
			}
			Arrays.sort(times);
			boolean wellFormed = true;
			try {
				XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(out)));
			} catch (Exception e) {
				wellFormed = false;
			}
			System.out.printf("  %s median %6d ms  (min %6d, max %6d)  markers: %5d  size: %8d  well-formed: %s%n",
					name, times[REPS / 2], times[0], times[REPS - 1], countMarkers(out), out.length(), wellFormed);
		} catch (Throwable t) {
			System.out.printf("  %s FAILED: %s%n", name, t);
		}
	}

	/** Crude diff-quality metric: how much of the output is marked as changed. */
	static int countMarkers(String out) {
		return count(out, "dfx:insert=") + count(out, "dfx:delete=")
				+ count(out, "<dfx:ins>") + count(out, "<dfx:del>")
				+ count(out, "<ins>") + count(out, "<del>");
	}

	static int count(String s, String needle) {
		int n = 0;
		for (int i = s.indexOf(needle); i >= 0; i = s.indexOf(needle, i + needle.length())) n++;
		return n;
	}

	public static void main(String[] args) throws Exception {
		// initialise JAXB context before timing anything
		@SuppressWarnings("unused")
		Object jc = Context.jc;

		List<List<Run>> base = generateBase();

		bench("identical            ", copy(base), base);
		bench("5% paragraphs edited ", editWords(base, 5, 1), base);
		bench("20% paragraphs edited", editWords(base, 20, 2), base);
		bench("structural (del/ins/move)", structural(base, 3), base);
		bench("50% paragraphs edited", editWords(base, 50, 4), base);
	}

}
