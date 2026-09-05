package org.docx4j.fidelity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.compare.PixelComparison;
import org.docx4j.fidelity.corpus.Corpus;
import org.docx4j.fidelity.extract.PdfLayout;
import org.docx4j.fidelity.extract.PdfLayoutExtractor;
import org.docx4j.fidelity.report.HtmlReport;
import org.docx4j.fidelity.score.Scoreboard;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Command line entry point.
 *
 * <pre>
 *   generate &lt;corpusDir&gt;                              write the probe docx files
 *   render   &lt;corpusDir&gt; &lt;pdfDir&gt;                     docx4j+FOP: one PDF (and .fo) per docx
 *   compare  &lt;refPdfDir&gt; &lt;candPdfDir&gt; &lt;reportDir&gt; [dpi]  compare PDFs with the same basename, write report
 *   run      &lt;corpusDir&gt; &lt;refPdfDir&gt; &lt;reportDir&gt; [dpi]  render into reportDir/fop, then compare
 *   score    &lt;corpusDir&gt; &lt;refPdfDir&gt; &lt;outDir&gt; [baseline.csv]  score a large corpus, CSV + delta
 * </pre>
 */
public final class Fidelity {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			usage();
			return;
		}
		// -Dfidelity.hyphenate=true|false overrides the documents' hyphenation setting
		// (docx4j.convert.out.fo.hyphenate): a golden made on a machine without the
		// language's proofing tools is unhyphenated whatever the document asks for
		String hyphenate = System.getProperty("fidelity.hyphenate");
		if (hyphenate != null) {
			org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.hyphenate", hyphenate.trim());
		}
		// real documents are not zip bombs: a 668KB docx whose EMF images unpack to
		// 95MB trips docx4j's 50MB default, so the guard is loosened for scoring
		// (must run before ZipPartStore is loaded, which reads it once)
		if (System.getProperty("fidelity.maxUncompressed") == null) {
			org.docx4j.Docx4jProperties.setProperty(
					"docx4j.openpackaging.package.MAX_UNCOMPRESSED_SIZE.unzip.error", "1073741824");
		}
		switch (args[0]) {
		case "generate":
			Corpus.generate(new File(args[1]));
			break;
		case "render":
			render(new File(args[1]), new File(args[2]));
			break;
		case "compare":
			compare(new File(args[1]), new File(args[2]), new File(args[3]), args.length > 4 ? Integer.parseInt(args[4]) : 100);
			break;
		case "run": {
			File report = new File(args[3]);
			File fop = new File(report, "fop");
			render(new File(args[1]), fop);
			compare(new File(args[2]), fop, report, args.length > 4 ? Integer.parseInt(args[4]) : 100);
			break;
		}
		case "score":
			score(new File(args[1]), new File(args[2]), new File(args[3]),
					args.length > 4 ? new File(args[4]) : null);
			break;
		default:
			usage();
		}
	}

	private static void usage() {
		System.out.println("usage: generate <corpusDir> | render <corpusDir> <pdfDir> | compare <refPdfDir> <candPdfDir> <reportDir> [dpi] | run <corpusDir> <refPdfDir> <reportDir> [dpi]");
		System.out.println("       score <corpusDir> <refPdfDir> <outDir> [baseline.csv]");
		System.out.println("             renders and compares every docx that has a <id>.pdf in refPdfDir,");
		System.out.println("             one row per document; survives failures, writes outDir/scoreboard.csv");
		System.out.println("             and outDir/scoreboard.txt, and diffs against a previous scoreboard.csv.");
		System.out.println("       -Dfidelity.only=id,id       restricts render/compare/run/score to those documents");
		System.out.println("       -Dfidelity.timeoutSeconds=N per-document conversion timeout in score (default 120)");
	}

	public static void render(File corpusDir, File pdfDir) throws Exception {
		pdfDir.mkdirs();
		for (File docx : docxFiles(corpusDir)) {
			String id = docx.getName().replaceAll("\\.docx$", "");
			renderOne(docx, pdfDir, id);
			System.out.println("rendered " + id);
		}
	}

	/** docx4j+FOP: writes pdfDir/id.fo and pdfDir/id.pdf. */
	static void renderOne(File docx, File pdfDir, String id) throws Exception {
		WordprocessingMLPackage pkg = Docx4J.load(docx);
		FOSettings fo = Docx4J.createFOSettings();
		fo.setOpcPackage(pkg);
		fo.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		try (FileOutputStream os = new FileOutputStream(new File(pdfDir, id + ".fo"))) {
			Docx4J.toFO(fo, os, Docx4J.FLAG_NONE);
		}
		// Explicitly the FO/FOP pathway: Docx4J.toPDF would silently prefer documents4j
		// (Word) when docx4j-documents4j-local is on the classpath, as it is here.
		pkg = Docx4J.load(docx);
		FOSettings pdf = Docx4J.createFOSettings();
		pdf.setOpcPackage(pkg);
		try (FileOutputStream os = new FileOutputStream(new File(pdfDir, id + ".pdf"))) {
			Docx4J.toFO(pdf, os, Docx4J.FLAG_NONE);
		}
	}

	public static List<LayoutComparison.Result> compare(File refDir, File candDir, File reportDir, int dpi) throws Exception {
		List<LayoutComparison.Result> results = new ArrayList<>();
		File[] refs = refDir.listFiles((d, n) -> n.endsWith(".pdf") && selected(n));
		if (refs == null) throw new IllegalArgumentException("no PDFs in " + refDir);
		Arrays.sort(refs);
		for (File ref : refs) {
			File cand = new File(candDir, ref.getName());
			if (!cand.exists()) {
				System.out.println("skip " + ref.getName() + ": no candidate");
				continue;
			}
			String id = ref.getName().replaceAll("\\.pdf$", "");
			PdfLayout a = PdfLayoutExtractor.extract(ref);
			PdfLayout b = PdfLayoutExtractor.extract(cand);
			LayoutComparison.Result r = LayoutComparison.compare(id, a, b);
			if (dpi > 0) {
				r.pixels = PixelComparison.compare(ref, cand, dpi);
			}
			results.add(r);
			System.out.printf("%-24s pages %d/%d  lines %d/%d  parity %.0f%%/%.0f%%  dy med %.2f max %.2f  %s%n", id,
					r.refPages, r.candPages, r.refLines, r.candLines, r.lineParity() * 100, r.pageParity() * 100,
					r.medianDy, r.maxDy, r.firstDivergence);
		}
		HtmlReport.write(reportDir, refDir.getPath(), candDir.getPath(), results);
		System.out.println("report: " + new File(reportDir, "index.html"));
		return results;
	}

	/**
	 * Scores a whole corpus of real documents: renders every docx that has a
	 * reference PDF of the same basename, compares the two, and writes
	 * outDir/scoreboard.csv and outDir/scoreboard.txt. Unlike {@link #render} and
	 * {@link #compare} this is built to survive a corpus that misbehaves: a
	 * document that throws is one error row, a document that hangs is one timeout
	 * row, and the run carries on.
	 *
	 * Documents are done smallest first, so a run that is cut short has still
	 * covered the most documents.
	 *
	 * @param baselineCsv a previous scoreboard.csv, or null; when given, a delta
	 *                    section says whether this run is better or worse
	 */
	public static List<Scoreboard.Row> score(File corpusDir, File refPdfDir, File outDir, File baselineCsv)
			throws Exception {
		File fopDir = new File(outDir, "fop");
		fopDir.mkdirs();
		int timeoutSeconds = Integer.getInteger("fidelity.timeoutSeconds", 120);

		File[] docs = docxFiles(corpusDir);
		Arrays.sort(docs, Comparator.comparingLong(File::length).thenComparing(File::getName));

		List<Scoreboard.Row> rows = new ArrayList<>();
		int n = 0;
		for (File docx : docs) {
			n++;
			String id = docx.getName().replaceAll("\\.docx$", "");
			File ref = new File(refPdfDir, id + ".pdf");
			if (!ref.exists()) {
				Scoreboard.Row row = new Scoreboard.Row(id, docx.length(), "noref");
				row.compatMode = compatMode(docx);
				row.error = "no reference PDF " + ref.getName();
				rows.add(row);
				System.out.printf(Locale.ROOT, "[%d/%d] %-44s noref%n", n, docs.length, id);
				continue;
			}
			Scoreboard.Row scored = scoreOne(docx, ref, fopDir, id, timeoutSeconds, n, docs.length);
			scored.compatMode = compatMode(docx);
			rows.add(scored);
		}

		List<String> deltaLines = null;
		if (baselineCsv != null) {
			List<Scoreboard.Row> before = Scoreboard.readCsv(baselineCsv);
			deltaLines = Scoreboard.delta(baselineCsv.getPath(), before, rows);
		}

		File csv = new File(outDir, "scoreboard.csv");
		Scoreboard.writeCsv(csv, rows);
		List<String> text = Scoreboard.textReport(rows, deltaLines);
		Scoreboard.writeLines(new File(outDir, "scoreboard.txt"), text);

		System.out.println();
		for (String l : Scoreboard.Aggregate.of(rows).lines()) System.out.println(l);
		if (deltaLines != null) {
			System.out.println();
			for (String l : deltaLines) System.out.println(l);
		}
		System.out.println();
		System.out.println("scoreboard: " + csv + " and " + new File(outDir, "scoreboard.txt"));
		return rows;
	}

	/**
	 * The document's own Word compatibility mode: w:compatSetting w:name="compatibilityMode"
	 * in word/settings.xml, or "" where the document declares none.
	 *
	 * <p>The corpus file names carry a leading number which was used for this, but it
	 * disagrees with the document often enough to be useless for segmenting results
	 * (and eight documents of the sample declare no mode at all), so the docx itself is
	 * read.  The zip entry is parsed directly: this runs before the conversion, on every
	 * document of a large corpus, and a full load would cost far more than it is worth.
	 */
	static String compatMode(File docx) {
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(docx)) {
			java.util.zip.ZipEntry entry = zip.getEntry("word/settings.xml");
			if (entry == null) return "";
			String xml;
			try (java.io.InputStream is = zip.getInputStream(entry)) {
				xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			}
			java.util.regex.Matcher m = COMPAT_SETTING.matcher(xml);
			while (m.find()) {
				String element = m.group();
				if (!element.contains("\"compatibilityMode\"")) continue;
				java.util.regex.Matcher v = COMPAT_VAL.matcher(element);
				if (v.find()) return v.group(1);
			}
		} catch (Exception e) {
			System.out.println("compatMode " + docx.getName() + ": " + e);
		}
		return "";
	}

	private static final java.util.regex.Pattern COMPAT_SETTING =
			java.util.regex.Pattern.compile("<w:compatSetting\\b[^>]*>");
	private static final java.util.regex.Pattern COMPAT_VAL =
			java.util.regex.Pattern.compile("w:val=\"([^\"]*)\"");

	/**
	 * One document, in its own thread so a conversion that never returns cannot
	 * stop the run. On timeout the thread is interrupted and abandoned: it may keep
	 * running (and holding memory) until the JVM exits, which is accepted here
	 * because the alternative is losing the whole scoring run to one bad document.
	 */
	private static Scoreboard.Row scoreOne(File docx, File ref, File fopDir, String id, int timeoutSeconds, int n,
			int total) {
		ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "fidelity-score-" + id);
			t.setDaemon(true); // so an abandoned conversion cannot keep the JVM alive
			return t;
		});
		Scoreboard.Row row;
		try {
			Future<LayoutComparison.Result> f = exec.submit(() -> {
				renderOne(docx, fopDir, id);
				PdfLayout a = PdfLayoutExtractor.extract(ref);
				PdfLayout b = PdfLayoutExtractor.extract(new File(fopDir, id + ".pdf"));
				return LayoutComparison.compare(id, a, b);
			});
			try {
				row = Scoreboard.Row.of(f.get(timeoutSeconds, TimeUnit.SECONDS), docx.length());
			} catch (TimeoutException e) {
				f.cancel(true);
				row = new Scoreboard.Row(id, docx.length(), "timeout");
				row.error = "no result within " + timeoutSeconds + "s";
			} catch (ExecutionException e) {
				row = errorRow(id, docx.length(), e.getCause() == null ? e : e.getCause());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				row = errorRow(id, docx.length(), e);
			}
		} catch (Throwable t) {
			row = errorRow(id, docx.length(), t);
		} finally {
			exec.shutdownNow();
		}
		if (row.scored()) {
			System.out.printf(Locale.ROOT, "[%d/%d] %-44s ok       parity %.0f%%  pages %d/%d%n", n, total, id,
					row.lineParity * 100, row.refPages, row.candPages);
		} else {
			System.out.printf(Locale.ROOT, "[%d/%d] %-44s %-8s %s%n", n, total, id, row.status, row.error);
		}
		return row;
	}

	private static Scoreboard.Row errorRow(String id, long size, Throwable t) {
		Scoreboard.Row row = new Scoreboard.Row(id, size, "error");
		String msg = t.getMessage();
		row.error = t.getClass().getSimpleName() + (msg == null ? "" : ": " + Scoreboard.firstLine(msg));
		return row;
	}

	/** -Dfidelity.only=id,id restricts render and compare to those probes. */
	static boolean selected(String fileName) {
		String only = System.getProperty("fidelity.only");
		if (only == null || only.trim().isEmpty()) return true;
		String id = fileName.replaceAll("\\.(docx|pdf)$", "");
		for (String s : only.split(",")) {
			if (s.trim().equals(id)) return true;
		}
		return false;
	}

	static File[] docxFiles(File dir) {
		File[] files = dir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~") && selected(n));
		if (files == null) throw new IllegalArgumentException("no docx in " + dir);
		Arrays.sort(files);
		return files;
	}
}
