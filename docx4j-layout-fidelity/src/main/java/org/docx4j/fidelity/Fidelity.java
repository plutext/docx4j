package org.docx4j.fidelity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.compare.PixelComparison;
import org.docx4j.fidelity.corpus.Corpus;
import org.docx4j.fidelity.extract.PdfLayout;
import org.docx4j.fidelity.extract.PdfLayoutExtractor;
import org.docx4j.fidelity.report.HtmlReport;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Command line entry point.
 *
 * <pre>
 *   generate &lt;corpusDir&gt;                              write the probe docx files
 *   render   &lt;corpusDir&gt; &lt;pdfDir&gt;                     docx4j+FOP: one PDF (and .fo) per docx
 *   compare  &lt;refPdfDir&gt; &lt;candPdfDir&gt; &lt;reportDir&gt; [dpi]  compare PDFs with the same basename, write report
 *   run      &lt;corpusDir&gt; &lt;refPdfDir&gt; &lt;reportDir&gt; [dpi]  render into reportDir/fop, then compare
 * </pre>
 */
public final class Fidelity {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			usage();
			return;
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
		default:
			usage();
		}
	}

	private static void usage() {
		System.out.println("usage: generate <corpusDir> | render <corpusDir> <pdfDir> | compare <refPdfDir> <candPdfDir> <reportDir> [dpi] | run <corpusDir> <refPdfDir> <reportDir> [dpi]");
		System.out.println("       -Dfidelity.only=id,id  restricts render/compare/run to those probes");
	}

	public static void render(File corpusDir, File pdfDir) throws Exception {
		pdfDir.mkdirs();
		for (File docx : docxFiles(corpusDir)) {
			String id = docx.getName().replaceAll("\\.docx$", "");
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
			System.out.println("rendered " + id);
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
