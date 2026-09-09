package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.documents4j.local.Documents4jLocalServices;
import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.extract.PdfLayout;
import org.docx4j.fidelity.extract.PdfLayoutExtractor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Runs on the Windows VM that has Word installed. Has Word render a handful of the
 * <b>re-saved</b> documents to PDF and diffs each against the golden Word cut from the
 * original, then counts the field-error text on each side. Two questions, one run.
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.ResaveInvariance &lt;docxDir&gt; &lt;goldensDir&gt; &lt;outDir&gt; [count | --only=&lt;id&gt;,&lt;id&gt;]</pre>
 *
 * <p>{@code docxDir} is the re-saved directory for the question and the corpus directory for
 * the control: run the same document from both against the one golden, and if the corpus
 * rendering is identical while the re-saved one differs, the re-save is proven to be the
 * variable. That comparison needs {@code --only}, because the size sampling ranks by file
 * size and Word's re-save changes it - the first control run picked five different documents
 * from the run it was meant to control.</p>
 *
 * <p><b>Question one: is Word's rendering invariant under its own re-save?</b> Word normalises
 * a document as it loads it - styles, numbering, the autofit grid, {@code w:compat} - and it
 * writes both the PDF and the re-saved docx out of that same in-memory model, so
 * {@code PDF(original)} and {@code PDF(resaved)} ought to be the same pages. If they are, the
 * goldens already cut stay valid now that the harness scores docx4j's rendering of the
 * <em>re-saved</em> file (see the README, "What to score"), and nothing has to be re-cut.
 * If they are not, then whatever differs is something Word computes at render time and does
 * not persist into the docx - which is worth knowing precisely, because docx4j can only work
 * from what was persisted.</p>
 *
 * <p><b>Question two: what does Word do with fields here?</b> The goldens were cut through a
 * conversion script that updates fields, so Word's PDF carries recomputed field results while
 * both the original and the re-saved docx carry the stored ones. That gap is not hypothetical:
 * one corpus document has lost its bookmarks altogether, so its golden is full of
 * {@code Error! Reference source not found.} text which appears nowhere in the docx - about
 * 1,160 lines of it in that one document, all of them unmatchable. So each side's field-error
 * lines are counted separately and the two counts compared.</p>
 *
 * <p>The two outcomes of that count mean opposite things, and are reported apart. Where both
 * sides carry the <em>same</em> errors, they are the document's own: an error string the
 * author saved as the stored result, which every renderer prints because it is what the docx
 * says - one control document shows fifteen of them on both sides with no field update
 * configured at all. Where the golden carries <em>more</em>, a field update recomputed them
 * when the golden was cut, and the reference is then showing text the docx does not contain,
 * which docx4j can never match. "The document is broken" and "the reference is wrong" are not
 * the same finding.</p>
 *
 * <p>Which makes the field update itself the measurement, so this tool can turn it on and off:
 * {@code -Dfidelity.updateFields=true|false} (see {@link ConversionScript}, which
 * {@link WordGoldenRunner} shares). Cut the same documents twice into two output directories,
 * once each way, and the difference is exactly what Word's field update does to the page - the
 * measurement which established that the goldens were cut with the update on.</p>
 *
 * <p>The documents are picked by size - smallest, largest and an even spread between - rather
 * than alphabetically, because the first five of anything sorted by name are as likely as not
 * all short, and a one-page document answers neither question. {@code count} overrides the
 * default of five. Each PDF is cut by the same route {@link WordGoldenRunner} uses (docx4j
 * loads the package and documents4j hands Word a temp file; handing Word the corpus file
 * itself fails on most of the corpus), each document is named before Word is given it so a
 * stall says which one, and a document Word refuses is stepped over rather than ending the
 * run. A PDF already in {@code outDir} is kept unless {@code --force} is given, and
 * {@code --no-word} skips the conversion entirely and just re-reads an output directory that
 * is already full - which is how a finished run is read again on a machine without Word.</p>
 *
 * <p>Two values in a PDF depend on the run rather than on the document, and both are masked
 * before the comparison, on both sides, and said out loud rather than silently dropped: the
 * temp file name a {@code FILENAME} field prints (see {@link #TEMP_NAME}) and the day a
 * {@code DATE} field prints, which the extractor's own normalisation collapses.</p>
 */
public final class ResaveInvariance {

	private static final String PPT_BRIDGE = "pptx4j.documents4j.MicrosoftPowerpointBridge.enabled";

	/** How many times a conversion is attempted before it is called a failure of the document.
	 *  {@code -Dfidelity.wordAttempts=} overrides it, as in {@link WordGoldenRunner}. */
	private static final int ATTEMPTS = Integer.parseInt(System.getProperty("fidelity.wordAttempts", "3"));

	/** How many documents are picked when the command line does not say. */
	private static final int DEFAULT_COUNT = 5;

	private static Documents4jLocalServices word;

	private static synchronized Documents4jLocalServices converter() {
		if (word == null) word = new Documents4jLocalServices();
		return word;
	}

	/** Drop the converter so the next call builds a new one, with a new Word behind it. */
	private static synchronized void resetConverter() {
		word = null;
	}

	/** Whether documents4j refused the work because its pool had already been shut down,
	 *  which says nothing about this document. */
	private static boolean isRejected(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof java.util.concurrent.RejectedExecutionException) return true;
			if (c.getCause() == c) break;
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("usage: ResaveInvariance <docxDir> <goldensDir> <outDir>"
					+ " [count | --only=<id>[,<id>...] | <id> ...] [--force] [--no-word]");
			System.out.println("  <docxDir> is the resaved directory for the question, or the corpus"
					+ " directory for the control");
			System.exit(2);
		}
		File docxDir = new File(args[0]);
		File goldensDir = new File(args[1]);
		File outDir = new File(args[2]);
		int count = DEFAULT_COUNT;
		List<String> only = new ArrayList<>();
		for (int i = 3; i < args.length; i++) {
			String a = args[i];
			if (a.startsWith("--only=")) {
				for (String s : a.substring("--only=".length()).split(",")) {
					if (!s.trim().isEmpty()) only.add(s.trim().replaceAll("\\.docx$", ""));
				}
			} else if (a.startsWith("--")) {
				continue;
			} else if (a.matches("\\d+")) {
				count = Integer.parseInt(a);
			} else {
				only.add(a.replaceAll("\\.docx$", ""));
			}
		}
		boolean force = Arrays.asList(args).contains("--force");
		boolean noWord = Arrays.asList(args).contains("--no-word");
		outDir.mkdirs();

		ConversionScript.warnAboutProperties();
		if (!noWord) ConversionScript.configure(outDir);
		System.out.println("ignored as run-dependent: documents4j's temp file name (a FILENAME field"
				+ " prints it, and it is random per conversion), and dates and times, through the"
				+ " extractor's own normalisation" + (DATES_NORMALISED ? "" : " - WHICH IS TURNED OFF"
				+ " by -Dfidelity.dateNormalise=false, so a DATE field will read as a difference"));

		/* documents4j starts PowerPoint as well as Word unless it is told not to, and
		 * PowerPoint has to run in the foreground, so a first-run or activation dialog
		 * stops the run before Word has been asked anything and looks exactly like a hang.
		 * Nothing here is a pptx. */
		if (System.getProperty(PPT_BRIDGE) == null) {
			org.docx4j.Docx4jProperties.setProperty(PPT_BRIDGE, "false");
		}

		List<File> picked = pick(docxDir, goldensDir, count, only);
		if (picked.isEmpty()) {
			throw new IllegalArgumentException("no <id>.docx in " + docxDir
					+ " with a matching <id>.pdf in " + goldensDir);
		}

		int identical = 0, differing = 0, failed = 0;
		int totalPagesDiffering = 0, totalLinesDiffering = 0;
		int fieldErrGolden = 0, fieldErrNew = 0, fieldDisagreements = 0;
		int storedErrors = 0, updatedErrors = 0;
		int n = 0;
		for (File docx : picked) {
			n++;
			String id = docx.getName().replaceAll("\\.docx$", "");
			File golden = new File(goldensDir, id + ".pdf");
			File fresh = new File(outDir, id + ".pdf");
			if (!noWord && (fresh.length() == 0 || force)) {
				progress("converting", id, n, picked.size(), docx);
				if (!convert(docx, fresh)) {
					failed++;
					continue;
				}
			} else if (fresh.length() == 0) {
				System.out.println("[" + n + "/" + picked.size() + "] " + id + ": no PDF in outDir, skipped");
				failed++;
				continue;
			} else {
				System.out.println("[" + n + "/" + picked.size() + "] " + id + ": PDF already present");
			}

			PdfLayout a, b;
			try {
				a = PdfLayoutExtractor.extract(golden);
				b = PdfLayoutExtractor.extract(fresh);
			} catch (IOException e) {
				System.out.println("  cannot read a PDF: " + e);
				failed++;
				continue;
			}
			Scrubbed sa = scrub(a), sb = scrub(b);
			if (sa.lines + sb.lines > 0) {
				System.out.printf("  %d line(s) golden / %d new carry documents4j's temp file name and"
						+ " are compared with it masked; first: \"%s\"%n",
						sa.lines, sb.lines, abbreviate(sa.first != null ? sa.first : sb.first));
			}
			Diff d = diff(id, a, b);
			report(id, d);
			if (d.identical()) {
				identical++;
			} else {
				differing++;
				totalPagesDiffering += d.pagesDiffering;
				totalLinesDiffering += d.linesDiffering;
			}

			Errors ea = fieldErrors(a), eb = fieldErrors(b);
			fieldErrGolden += ea.lines;
			fieldErrNew += eb.lines;
			if (ea.lines != eb.lines) fieldDisagreements++;
			System.out.println("  field errors: golden " + ea + "; new " + eb
					+ (ea.lines == eb.lines ? "  (agree)" : "  DISAGREE by " + (eb.lines - ea.lines) + " lines"));
			/* Which of the two kinds of field error this is - and they mean opposite things.
			 * Equal counts on both sides are the document's own: an error string the author
			 * saved as the stored result, which every renderer prints because it is what the
			 * docx says. A golden which carries more of them than the new render printed them
			 * because a field update recomputed a REF whose bookmark is gone - the reference
			 * is then showing something the docx does not contain, and docx4j can never match
			 * it. "The document is broken" and "the reference is wrong" are not the same
			 * finding, so the tool does not report them as one number. */
			if (ea.lines > 0 && ea.lines == eb.lines) {
				storedErrors++;
				System.out.println("      both sides agree, so these are stored results the document"
						+ " itself carries - the document, not the reference");
			} else if (ea.lines > eb.lines) {
				updatedErrors++;
				System.out.println("      the golden carries " + (ea.lines - eb.lines) + " more, so a"
						+ " field update recomputed them when it was cut - the reference shows what"
						+ " the docx does not contain");
			} else if (eb.lines > ea.lines) {
				updatedErrors++;
				System.out.println("      this run carries " + (eb.lines - ea.lines) + " more than the"
						+ " golden, so this run updated fields the golden's did not");
			}
			System.out.flush();
		}

		System.out.println();
		System.out.println("== question one: is Word's rendering invariant under its own re-save? ==");
		System.out.printf("%d of %d documents rendered identically; %d differed (%d pages, %d lines in total); %d failed%n",
				identical, identical + differing, differing, totalPagesDiffering, totalLinesDiffering, failed);
		if (differing == 0 && identical > 0) {
			System.out.println("So the goldens cut from the original are Word's rendering of the re-saved file too,");
			System.out.println("and scoring the re-saved docx against them compares like with like.");
		} else if (differing > 0) {
			System.out.println("So Word computes something at render time that it does not write into the docx.");
			System.out.println("The differing lines above are that thing; the goldens are not valid for those documents");
			System.out.println("as a reference for the re-saved file, and should be re-cut from it.");
		}
		System.out.println();
		System.out.println("== question two: field errors ==");
		System.out.printf("golden %d lines, new %d lines; the two sides disagree on %d of %d documents%n",
				fieldErrGolden, fieldErrNew, fieldDisagreements, identical + differing);
		System.out.printf("%d document(s) carry the same errors on both sides (stored results - the"
				+ " document is broken)%n", storedErrors);
		System.out.printf("%d document(s) differ (a field update recomputed them - the reference holds"
				+ " what the docx does not)%n", updatedErrors);
		System.out.println("field update this run: " + ConversionScript.description());
		System.out.println("review markup this run: " + ConversionScript.markup());
		System.out.println(Errors.LIMITATION);
		// documents4j keeps worker threads; do not wait for them
		System.exit(failed == 0 ? 0 : 1);
	}

	// ------------------------------------------------------------------ picking

	/**
	 * The documents which have both a docx in {@code docxDir} and a golden PDF, sampled by
	 * size: the smallest, the largest, and an even spread of ranks between them. Alphabetical
	 * order carries no information about a document, so the first five of it can easily be
	 * five short ones - and a document of one page cannot show a page break moving. Size
	 * ranking is deterministic, so two runs pick the same documents and can be compared.
	 *
	 * <p>Deterministic is not the same as <em>comparable across bases</em>, though, and the
	 * first pair of runs proved it: the control run over the corpus files picked five
	 * different documents from the run over the re-saved ones, because Word's re-save changes
	 * a document's size and so its rank. The control was then a control over nothing - the
	 * document that differed was never run on the other basis. So {@code only} names the
	 * documents outright ({@code --only=<id>,<id>} or the ids as positional arguments), which
	 * is how the same document is run from {@code corpus} and from {@code resaved} against the
	 * one golden: if the corpus rendering is identical and the re-saved one differs, the
	 * re-save is proven to be the variable.</p>
	 */
	private static List<File> pick(File docxDir, File goldensDir, int count, List<String> only) {
		if (!only.isEmpty()) {
			List<File> named = new ArrayList<>();
			for (String id : only) {
				File docx = new File(docxDir, id + ".docx");
				File pdf = new File(goldensDir, id + ".pdf");
				if (docx.length() == 0) {
					System.out.println("  asked for " + id + ", but there is no such docx in " + docxDir);
				} else if (pdf.length() == 0) {
					System.out.println("  asked for " + id + ", but there is no golden for it in " + goldensDir);
				} else {
					named.add(docx);
					System.out.printf("  picked %s (%d KB) - named on the command line%n", id, docx.length() / 1024);
				}
			}
			return named;
		}
		File[] all = docxDir.listFiles((d, name) -> name.endsWith(".docx") && !name.startsWith("~"));
		if (all == null) throw new IllegalArgumentException("cannot list " + docxDir);
		List<File> eligible = new ArrayList<>();
		for (File f : all) {
			String id = f.getName().replaceAll("\\.docx$", "");
			if (new File(goldensDir, id + ".pdf").length() > 0) eligible.add(f);
		}
		// by size, then by name so that two documents of identical size still order the same way
		eligible.sort((x, y) -> {
			int c = Long.compare(x.length(), y.length());
			return c != 0 ? c : x.getName().compareTo(y.getName());
		});
		int n = eligible.size();
		System.out.println(n + " documents have both a docx here and a golden PDF");
		System.out.println("sampling by size; a run over corpus and a run over resaved will NOT pick the"
				+ " same documents, because Word's re-save changes a document's size and so its rank."
				+ " Use --only=<id> to compare the two bases.");
		LinkedHashSet<File> out = new LinkedHashSet<>();
		if (count >= n) {
			out.addAll(eligible);
		} else if (count == 1) {
			out.add(eligible.get(n / 2));
		} else {
			for (int k = 0; k < count; k++) {
				out.add(eligible.get((int) Math.round(k * (n - 1) / (double) (count - 1))));
			}
		}
		List<File> picked = new ArrayList<>(out);
		for (File f : picked) {
			int rank = eligible.indexOf(f);
			String why = rank == 0 ? "smallest"
					: rank == n - 1 ? "largest"
					: String.format("%d%% of the way up by size", Math.round(100.0 * rank / (n - 1)));
			System.out.printf("  picked %s (%d KB, rank %d of %d) - %s%n",
					f.getName().replaceAll("\\.docx$", ""), f.length() / 1024, rank + 1, n, why);
		}
		return picked;
	}

	// ------------------------------------------------------------------ conversion

	/**
	 * Has Word render the document to {@code pdf}, by {@link WordGoldenRunner}'s route:
	 * docx4j loads the package and documents4j saves it to a temp file for Word, because
	 * handing Word the file itself fails on most of the corpus. Returns false, having said
	 * why, for a document Word refuses - the run steps over it rather than ending.
	 */
	private static boolean convert(File docx, File pdf) {
		Throwable last = null;
		for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
			try {
				WordprocessingMLPackage pkg = Docx4J.load(docx);
				try (OutputStream os = new FileOutputStream(pdf)) {
					converter().export(pkg, os);
				}
				if (pdf.length() == 0) throw new IllegalStateException("Word produced an empty PDF");
				return true;
			} catch (Throwable t) {
				last = t;
				pdf.delete();
				/* documents4j shuts its worker pool down once it decides Word is unusable,
				 * and rejects everything afterwards out of hand. A terminated pool is not a
				 * fact about the document, so the converter is rebuilt and this one retried. */
				if (isRejected(t)) resetConverter();
				if (attempt < ATTEMPTS) {
					System.out.println("    attempt " + attempt + " failed (" + rootCause(t) + "), retrying");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}
		System.out.println("  FAILED " + pdf.getName() + ": " + rootCause(last));
		return false;
	}

	/** documents4j nests the reason several deep - a ConversionInputException says only that
	 *  Word refused the input - so the whole chain is printed. */
	private static String rootCause(Throwable t) {
		StringBuilder sb = new StringBuilder();
		for (Throwable c = t; c != null && sb.length() < 500; c = c.getCause()) {
			if (sb.length() > 0) sb.append(" <- ");
			sb.append(c.getClass().getSimpleName()).append(": ").append(c.getMessage());
			if (c.getCause() == c) break;
		}
		return sb.toString().replace('\n', ' ');
	}

	/** Names the document <em>before</em> Word is handed it, so a run which stops - or which
	 *  is simply slow on a long document - says which one it is on. */
	private static void progress(String what, String id, int n, int total, File docx) {
		System.out.printf("[%d/%d] %s %s (%d KB)%n", n, total, what, id, docx.length() / 1024);
		System.out.flush();
	}

	// ------------------------------------------------------------------ run-dependent text

	/** What one side's masking came to: how many lines carried a temp file name, and the first
	 *  of them as it was written, so the output says what was masked rather than hiding it. */
	private static final class Scrubbed {
		int lines;
		String first;
	}

	/**
	 * documents4j's temp file name, which is random per conversion:
	 * {@code Documents4jLocalServices} saves the package to {@code docx_<random>.docx} before
	 * Word is given it, so a {@code FILENAME} field prints a different name in every PDF ever
	 * cut of the same document. That is a fact about the conversion, not about the layout, and
	 * it cost the first run a false positive - a document whose one "difference" was
	 * {@code docx_12277429448431206055.docx} against {@code docx_3404776806619476754.docx}.
	 */
	private static final Pattern TEMP_NAME =
			Pattern.compile("(?:docx|xlsx|pptx|resave)_\\d+\\.(?:docx|xlsx|pptx)");

	/** What a masked temp name is compared as. Readable rather than a sentinel, because it is
	 *  printed back in the first-difference report. */
	private static final String TEMP_MASK = "docx_<temp>.docx";

	/** Whether the extractor is normalising dates, which is the other run-dependent value: a
	 *  {@code DATE} field prints the day the PDF was cut, so a golden and a render made on
	 *  different days differ on it. {@link PdfLayout.Line#key()} - what {@code score} pairs on,
	 *  and what this tool compares on - collapses it, unless it has been turned off. */
	private static final boolean DATES_NORMALISED =
			!"false".equalsIgnoreCase(System.getProperty("fidelity.dateNormalise", "true"));

	/**
	 * Masks the temp file name in every line, <b>before</b> anything asks a line for its
	 * {@link PdfLayout.Line#key()} - the key is computed once and cached, so this has to
	 * happen first, and doing it here means the positional diff and {@link LayoutComparison}
	 * both see the same masked text. Dates need no equivalent: the key normalises them
	 * already, on both sides, which is exactly what {@code score} does.
	 */
	private static Scrubbed scrub(PdfLayout layout) {
		Scrubbed s = new Scrubbed();
		for (PdfLayout.Line l : layout.lines) {
			if (l.text == null) continue;
			Matcher m = TEMP_NAME.matcher(l.text);
			if (!m.find()) continue;
			s.lines++;
			if (s.first == null) s.first = l.text;
			l.text = m.reset().replaceAll(TEMP_MASK);
		}
		return s;
	}

	// ------------------------------------------------------------------ the diff

	/** What one document's two renderings came to. */
	private static final class Diff {
		int goldenPages, newPages;
		int pagesDiffering;
		int linesDiffering;
		int goldenLines, newLines;
		String pageCounts = "";
		String firstDiff = "";
		LayoutComparison.Result lcs;

		boolean identical() {
			return goldenPages == newPages && pagesDiffering == 0 && linesDiffering == 0;
		}
	}

	/**
	 * The same comparison {@code score} makes, on the same extraction: both PDFs go through
	 * {@link PdfLayoutExtractor}, so whatever it normalises is normalised on both sides, and
	 * the lines are then walked in order. Two lines are the same when their
	 * {@link PdfLayout.Line#key()}s are - the key being what the scoring LCS pairs on, so
	 * that a date field which prints the day the PDF was cut does not read as a difference
	 * between two renderings made on different days.
	 *
	 * <p>The positional walk is what answers question one (are these the same pages?), and
	 * {@link LayoutComparison} is run alongside it so that a document which is <em>not</em>
	 * identical is described in the same numbers the scoreboard uses.</p>
	 */
	private static Diff diff(String id, PdfLayout golden, PdfLayout fresh) {
		Diff d = new Diff();
		d.goldenPages = golden.pageCount();
		d.newPages = fresh.pageCount();
		d.goldenLines = golden.lines.size();
		d.newLines = fresh.lines.size();

		int[] a = perPage(golden), b = perPage(fresh);
		StringBuilder counts = new StringBuilder();
		for (int p = 0; p < Math.max(a.length, b.length); p++) {
			int x = p < a.length ? a[p] : -1, y = p < b.length ? b[p] : -1;
			if (x == y) continue;
			d.pagesDiffering++;
			if (counts.length() < 200) {
				if (counts.length() > 0) counts.append(", ");
				counts.append("p").append(p + 1).append(' ')
						.append(x < 0 ? "-" : String.valueOf(x)).append('/')
						.append(y < 0 ? "-" : String.valueOf(y));
			}
		}
		d.pageCounts = counts.toString();

		List<PdfLayout.Line> ga = golden.lines, fb = fresh.lines;
		int common = Math.min(ga.size(), fb.size());
		for (int i = 0; i < common; i++) {
			PdfLayout.Line x = ga.get(i), y = fb.get(i);
			if (x.key().equals(y.key())) continue;
			d.linesDiffering++;
			if (d.firstDiff.isEmpty()) {
				d.firstDiff = String.format("golden p%d y=%.2f \"%s\"%n            new    p%d y=%.2f \"%s\"",
						x.page + 1, x.y, abbreviate(x.text), y.page + 1, y.y, abbreviate(y.text));
			}
		}
		int surplus = Math.abs(ga.size() - fb.size());
		if (surplus > 0) {
			d.linesDiffering += surplus;
			if (d.firstDiff.isEmpty()) {
				PdfLayout.Line x = ga.size() > fb.size() ? ga.get(common) : fb.get(common);
				d.firstDiff = String.format("%s has %d line(s) the other does not, from p%d y=%.2f \"%s\"",
						ga.size() > fb.size() ? "golden" : "new", surplus, x.page + 1, x.y, abbreviate(x.text));
			}
		}
		d.lcs = LayoutComparison.compare(id, golden, fresh);
		return d;
	}

	private static int[] perPage(PdfLayout layout) {
		int[] counts = new int[layout.pageCount()];
		for (PdfLayout.Line l : layout.lines) {
			if (l.page >= 0 && l.page < counts.length) counts[l.page]++;
		}
		return counts;
	}

	private static String abbreviate(String s) {
		return s.length() > 60 ? s.substring(0, 57) + "..." : s;
	}

	private static void report(String id, Diff d) {
		if (d.identical()) {
			System.out.printf("  %s: identical - %d pages, %d lines%n", id, d.goldenPages, d.goldenLines);
			return;
		}
		System.out.printf("  %s: pages %d golden / %d new; lines %d / %d%n",
				id, d.goldenPages, d.newPages, d.goldenLines, d.newLines);
		if (d.pagesDiffering > 0) {
			System.out.printf("      %d page(s) differ in line count: %s%n", d.pagesDiffering, d.pageCounts);
		}
		if (d.linesDiffering > 0) {
			System.out.printf("      %d line(s) differ; first: %s%n", d.linesDiffering, d.firstDiff);
		}
		System.out.printf("      as score pairs them: matched %d of %d, line parity %.4f, page parity %.4f%n",
				d.lcs.matched, d.lcs.refLines, d.lcs.lineParity(), d.lcs.pageParity());
		if (!d.lcs.firstDivergence.isEmpty()) {
			System.out.println("      first divergence: " + d.lcs.firstDivergence);
		}
	}

	// ------------------------------------------------------------------ field errors

	/** One side's field-error census. */
	private static final class Errors {
		int lines, hits, markerOnly;

		static final String LIMITATION =
				"(a line carrying a field error is counted once; an error string which wrapped onto a"
				+ " second line counts once, not twice. Only the English and French wordings are known"
				+ " exactly - a line in another language is counted separately as \"marker\", on the"
				+ " Error!/Erreur ! it opens with, and may be an ordinary sentence.)";

		@Override
		public String toString() {
			return lines + " lines / " + hits + " hits"
					+ (markerOnly > 0 ? " (+" + markerOnly + " marker)" : "");
		}
	}

	/**
	 * How many lines carry a field-error string. Word writes one where a {@code REF},
	 * {@code PAGEREF} or {@code NOTEREF} field can no longer find its bookmark, so it is text
	 * the PDF holds and the docx does not - and a document whose bookmarks are gone can carry
	 * over a thousand of them, all of which score as unmatched lines against a docx4j render
	 * that (correctly) shows the stored result instead. Counting them on both sides says
	 * whether the two renderings agree about the document's fields, which is the half of
	 * question two a PDF can answer.
	 *
	 * <p>The English and French wordings are matched exactly; anything else is caught only if
	 * it opens with one of the error markers, which is stated in the output rather than
	 * guessed at.</p>
	 */
	private static Errors fieldErrors(PdfLayout layout) {
		Errors e = new Errors();
		for (PdfLayout.Line l : layout.lines) {
			int hits = 0;
			for (Pattern p : FIELD_ERROR) {
				Matcher m = p.matcher(l.text);
				while (m.find()) hits++;
			}
			if (hits > 0) {
				e.lines++;
				e.hits += hits;
			} else if (MARKER.matcher(l.text).find()) {
				e.markerOnly++;
			}
		}
		return e;
	}

	/**
	 * A space, in any of the forms one can arrive in. The extractor folds U+00A0, U+2007 and
	 * U+202F to an ordinary space and collapses runs of whitespace, so an ordinary
	 * {@code \s} would do - but only while {@code -Dfidelity.nbspNormalise} is left on, and
	 * French puts a no-break space before its "!", which Java's {@code \s} does not match by
	 * definition. Spelling the no-break spaces out costs nothing and keeps the census right
	 * whatever the extractor is told to do.
	 */
	private static final String SP = "[\\s\\u00a0\\u2007\\u202f]";

	/** Word's own wordings, English and French. */
	private static final Pattern[] FIELD_ERROR = {
			Pattern.compile("Error" + SP + "*!" + SP + "*Reference" + SP + "+source" + SP + "+not" + SP + "+found\\."),
			Pattern.compile("Error" + SP + "*!" + SP + "*Bookmark" + SP + "+not" + SP + "+defined\\."),
			Pattern.compile("Erreur" + SP + "*!" + SP + "*Source" + SP + "+du" + SP + "+renvoi" + SP + "+introuvable\\."),
			Pattern.compile("Erreur" + SP + "*!" + SP + "*Signet" + SP + "+non" + SP + "+d\\u00e9fini\\."),
	};

	/** What a localised field error opens with, for the languages the corpora are written in.
	 *  Counted apart from the exact wordings, and reported apart, because on its own it is
	 *  weak evidence: an ordinary sentence can begin "Error!" too. */
	private static final Pattern MARKER =
			Pattern.compile("(?:Error|Erreur|Fehler|Errore|Fout)" + SP + "*!");

	private ResaveInvariance() {}
}
