package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.docx4j.Docx4J;
import org.docx4j.documents4j.local.Documents4jLocalServices;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTCompatSetting;

/**
 * Runs on the Windows VM that has Word installed. Converts every docx in the
 * corpus directory to PDF through Word (documents4j-local) and writes a
 * manifest recording the environment, so goldens are reproducible.
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.WordGoldenRunner &lt;corpusDir&gt; &lt;goldenDir&gt; [resavedDir] [--force]</pre>
 *
 * <p>Given a third directory, each document is also <b>opened and saved by Word</b> into it,
 * as a docx.  The PDF says where Word put the text; the resaved docx says what Word
 * <em>computed</em>, and some questions can only be answered by the second.  A
 * {@code w:tblGrid} is the case that motivated it: in a Word-authored autofit table the grid
 * is Word's own cached layout, so where the grid and the cells disagree the grid is what Word
 * draws - but in a document this harness generated the grid is whatever the generator wrote,
 * and Word recomputes it on open.  A probe's grid therefore proves nothing until Word has
 * written one, and the resave is how to get it.  Word recomputes more than tables (the
 * section's own {@code w:sectPr}, style and numbering normalisation, field results), so the
 * resaved corpus is worth having for any "what does Word actually compute here" question;
 * {@link org.docx4j.fidelity.Fidelity} {@code griddiff} reads the table half of it.</p>
 *
 * <p>The golden PDF is always made from the <em>original</em> docx, never from the resaved
 * one: rendering Word's own output would be measuring Word against Word.</p>
 *
 * Probes whose PDF already exists (non-empty) are skipped unless --force is given, so a
 * failed run can be resumed. Failures are reported per file and the exit code is 1 if any.
 *
 * <p><b>Whether Word updates the document's fields on the way through is chosen here</b>, with
 * {@code -Dfidelity.updateFields=true|false} (and {@code -Dfidelity.fieldUpdateScript=<path>}
 * to name a script), resolved by {@link ConversionScript} - the same helper
 * {@link ResaveInvariance} uses, so the two cannot drift. It is not a detail of the run but a
 * property of the golden set: a document rendered with the update reproduces the goldens
 * already cut <em>exactly</em>, and the same document rendered without it differs by hundreds
 * of lines, so those goldens hold Word's recomputed field text, which docx4j - rendering the
 * stored result - never produces. Cut with the update off, both sides show the stored result
 * and the comparison is layout alone. The mode and the script path are printed at startup and
 * written into {@code golden-manifest.properties} (and into
 * {@code resaved-manifest.properties} beside a resaved directory), so a set says how it was
 * cut instead of leaving the next person to measure it.</p>
 */
public final class WordGoldenRunner {

	private static final String PPT_BRIDGE = "pptx4j.documents4j.MicrosoftPowerpointBridge.enabled";

	/** How many times a resave is attempted before it is called a failure of the document.
	 *  {@code -Dfidelity.wordAttempts=} overrides it. */
	private static final int ATTEMPTS = Integer.parseInt(System.getProperty("fidelity.wordAttempts", "3"));

	/** Hand Word the corpus file itself rather than docx4j's re-save of it.  Truer, but
	 *  most of the corpus fails that way; see the resave block. */
	private static final boolean RESAVE_ORIGINAL_BYTES =
			Boolean.parseBoolean(System.getProperty("fidelity.resaveOriginalBytes", "false"));

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
		File corpusDir = new File(args[0]);
		File goldenDir = new File(args[1]);
		File resavedDir = args.length > 2 && !args[2].startsWith("--") ? new File(args[2]) : null;
		boolean force = Arrays.asList(args).contains("--force");
		goldenDir.mkdirs();
		if (resavedDir != null) resavedDir.mkdirs();
		File[] files = corpusDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
		if (files == null || files.length == 0) throw new IllegalArgumentException("no docx in " + corpusDir);
		Arrays.sort(files);

		/* documents4j starts PowerPoint as well as Word unless it is told not to, and
		 * PowerPoint has to run in the foreground - so a first-run, activation or
		 * "presentation was not saved" dialog stops the run before Word has been asked
		 * anything, and looks exactly like a hang.  This harness converts nothing but
		 * docx.  -Dpptx4j.documents4j.MicrosoftPowerpointBridge.enabled=true puts it
		 * back. */
		if (System.getProperty(PPT_BRIDGE) == null) {
			org.docx4j.Docx4jProperties.setProperty(PPT_BRIDGE, "false");
		}

		/* Which script Word runs, and therefore whether it updates the document's fields on the
		 * way through, decides what a golden set *is*: measured, a document rendered with the
		 * update reproduces its golden exactly and the same document rendered without it differs
		 * by hundreds of lines, so the set already cut was cut with the update on, and every
		 * field-bearing document is scored against Word's recomputed field text. Since that is a
		 * property of the set rather than of a run, it is resolved here - before the converter is
		 * built, which is when documents4j materialises the script - and recorded in the manifest
		 * beside the PDFs. -Dfidelity.updateFields=true|false decides it; see ConversionScript. */
		ConversionScript.warnAboutProperties();
		ConversionScript.configure(goldenDir);

		int failed = 0;
		int done = 0;
		int skipped = 0;
		try (PrintWriter m = new PrintWriter(new FileWriter(new File(goldenDir, "golden-manifest.properties"), true))) {
			m.println("# run " + ZonedDateTime.now());
			m.println("source=documents4j-local (desktop Word)");
			m.println("os=" + System.getProperty("os.name") + " " + System.getProperty("os.version"));
			m.println("java=" + System.getProperty("java.version"));
			/* So the set says how it was cut. Nobody could tell, of the set before this line
			 * existed, whether Word had updated its fields; it took a pair of runs of
			 * ResaveInvariance to find out, and that is not a thing to rediscover. */
			m.println("fieldUpdate=" + ConversionScript.mode());
			m.println("wordConvertScript=" + ConversionScript.path());
			if (resavedDir != null) writeResavedManifest(resavedDir);
			int n = 0;
			for (File docx : files) {
				n++;
				String id = docx.getName().replaceAll("\\.docx$", "");
				File pdf = new File(goldenDir, id + ".pdf");
				/* The PDF is the primary artefact and goes first, so that a resave which
				 * puts Word in a bad state can never cost a golden.  The resave is still
				 * attempted when the PDF was skipped, so a resaved directory can be added
				 * to a golden set that is already cut without --force re-cutting every
				 * PDF. */
				if (pdf.length() > 0 && !force) {
					skipped++;
				} else {
					progress("converting", id, n, files.length, docx);
					try {
						WordprocessingMLPackage pkg = Docx4J.load(docx);
						try (FileOutputStream os = new FileOutputStream(pdf)) {
							converter().export(pkg, os);
						}
						if (pdf.length() == 0) throw new IllegalStateException("Word produced an empty PDF");
						m.println(id + ".compatibilityMode=" + compatMode(pkg));
						m.println(id + ".generated=" + ZonedDateTime.now());
						done++;
						System.out.println("  golden " + id);
					} catch (Throwable t) {
						failed++;
						pdf.delete();
						m.println(id + ".FAILED=" + rootCause(t));
						System.out.println("  FAILED " + id + ": " + rootCause(t));
					}
					m.flush();
				}
				if (resavedDir != null) {
					File resaved = new File(resavedDir, id + ".docx");
					if (resaved.length() == 0 || force) {
						progress("resaving", id, n, files.length, docx);
						Throwable last = null;
						for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
							File local = null;
							try {
								/* The resave goes through the same mechanism as the golden
								 * PDF, which works on every document in the corpus: the
								 * package is loaded by docx4j and handed to documents4j,
								 * which saves it to a local temp file for Word to open
								 * (Documents4jLocalServices.export(pkg, ...)).  Handing
								 * Word the corpus file instead - which is what
								 * updateDocx(File, ...) does - fails on most of the corpus
								 * with documents4j's "the input file seems to be corrupt",
								 * identically on every retry and whether the file is on the
								 * share or copied to a local disk first, while the same
								 * document converts to PDF without complaint.  The two
								 * paths differ in nothing else that we have been able to
								 * find: the content parts are byte-identical between the
								 * corpus file and docx4j's re-save of it, and the three
								 * package-metadata parts differ only in attribute order.
								 *
								 * It does mean Word is shown docx4j's re-save rather than
								 * the corpus bytes - but so is the golden PDF, so the two
								 * are at least consistent, and docx4j's save marshals the
								 * w:tblGrid it read without recomputing it, which is what
								 * GridDiff asks about.  -Dfidelity.resaveOriginalBytes=true
								 * hands Word a local copy of the file itself instead. */
								try (FileOutputStream os = new FileOutputStream(resaved)) {
									if (RESAVE_ORIGINAL_BYTES) {
										local = File.createTempFile("resave_", ".docx");
										java.nio.file.Files.copy(docx.toPath(), local.toPath(),
												java.nio.file.StandardCopyOption.REPLACE_EXISTING);
										converter().updateDocx(local, os);
									} else {
										converter().updateDocx(Docx4J.load(docx), os);
									}
								}
								if (resaved.length() == 0) throw new IllegalStateException("Word produced an empty docx");
								last = null;
								break;
							} catch (Throwable t) {
								last = t;
								resaved.delete();
								/* documents4j shuts its worker pool down once it decides
								 * Word is unusable, and every conversion after that is
								 * rejected out of hand - 22 of them in the run which found
								 * this.  A terminated pool is not a fact about the
								 * document, so the converter is rebuilt and the document
								 * tried again. */
								if (isRejected(t)) resetConverter();
								if (attempt < ATTEMPTS) {
									System.out.println("    attempt " + attempt + " failed ("
											+ rootCause(t) + "), retrying");
									Thread.sleep(2000);
								}
							} finally {
								if (local != null) local.delete();
							}
						}
						if (last == null) {
							m.println(id + ".resaved=" + ZonedDateTime.now());
							System.out.println("  resaved " + id);
						} else {
							failed++;
							m.println(id + ".RESAVE_FAILED=" + rootCause(last));
							System.out.println("  RESAVE FAILED " + id + ": " + rootCause(last));
						}
						m.flush();
					}
				}
			}
		}
		System.out.printf("done %d, skipped (already present) %d, failed %d%n", done, skipped, failed);
		// documents4j keeps worker threads; do not wait for them
		System.exit(failed == 0 ? 0 : 1);
	}

	/**
	 * The same environment record beside the re-saved documents, because a resaved directory
	 * travels separately from the goldens - it is scored on its own - and the field-update mode
	 * matters to it for the same reason: Word runs one script for both, so a resave cut with the
	 * update on has had Word's recomputed results written through it wherever a field result is
	 * stored. A reader of that directory should not have to find the golden manifest to learn it.
	 */
	private static void writeResavedManifest(File resavedDir) {
		try (PrintWriter r = new PrintWriter(new FileWriter(new File(resavedDir, "resaved-manifest.properties"), true))) {
			r.println("# run " + ZonedDateTime.now());
			r.println("source=documents4j-local (desktop Word), opened and saved back as docx");
			r.println("os=" + System.getProperty("os.name") + " " + System.getProperty("os.version"));
			r.println("java=" + System.getProperty("java.version"));
			r.println("fieldUpdate=" + ConversionScript.mode());
			r.println("wordConvertScript=" + ConversionScript.path());
		} catch (Exception e) {
			// a manifest is a record, not the work; never let it cost a run
			System.out.println("  could not write resaved-manifest.properties: " + e);
		}
	}

	/** documents4j nests the reason several deep - a ConversionInputException says only
	 *  that Word refused the input - so the manifest records the whole chain: after a run,
	 *  what the failures have in common is the diagnosis. */
	private static String rootCause(Throwable t) {
		StringBuilder sb = new StringBuilder();
		for (Throwable c = t; c != null && sb.length() < 500; c = c.getCause()) {
			if (sb.length() > 0) sb.append(" <- ");
			sb.append(c.getClass().getSimpleName()).append(": ").append(c.getMessage());
			if (c.getCause() == c) break;
		}
		return sb.toString().replace('\n', ' ');
	}

	/** Names the document <em>before</em> Word is handed it, so that a run which stops -
	 *  or which is simply slow on a long document - says which one it is on. */
	private static void progress(String what, String id, int n, int total, File docx) {
		System.out.printf("[%d/%d] %s %s (%d KB)%n", n, total, what, id, docx.length() / 1024);
		System.out.flush();
	}

	static String compatMode(WordprocessingMLPackage pkg) {
		try {
			DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
			if (dsp == null || dsp.getContents().getCompat() == null) return "absent";
			for (CTCompatSetting cs : dsp.getContents().getCompat().getCompatSetting()) {
				if ("compatibilityMode".equals(cs.getName())) return cs.getVal();
			}
		} catch (Exception e) {
			return "error: " + e.getMessage();
		}
		return "absent";
	}
}
