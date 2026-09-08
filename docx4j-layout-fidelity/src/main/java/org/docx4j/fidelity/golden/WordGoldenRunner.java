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
 */
public final class WordGoldenRunner {

	private static final String PPT_BRIDGE = "pptx4j.documents4j.MicrosoftPowerpointBridge.enabled";

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
		Documents4jLocalServices word = new Documents4jLocalServices();
		int failed = 0;
		int done = 0;
		int skipped = 0;
		try (PrintWriter m = new PrintWriter(new FileWriter(new File(goldenDir, "golden-manifest.properties"), true))) {
			m.println("# run " + ZonedDateTime.now());
			m.println("source=documents4j-local (desktop Word)");
			m.println("os=" + System.getProperty("os.name") + " " + System.getProperty("os.version"));
			m.println("java=" + System.getProperty("java.version"));
			int n = 0;
			for (File docx : files) {
				n++;
				String id = docx.getName().replaceAll("\\.docx$", "");
				File pdf = new File(goldenDir, id + ".pdf");
				/* The resave is checked before the PDF's own skip, so that a resaved
				 * directory can be added to a golden set that is already cut without
				 * --force re-cutting every PDF.  It has its own try: a document Word
				 * refuses (a ConversionInputException) must be named and stepped over,
				 * not left to end the run several hundred documents in. */
				if (resavedDir != null) {
					File resaved = new File(resavedDir, id + ".docx");
					if (resaved.length() == 0 || force) {
						progress("resaving", id, n, files.length, docx);
						try {
							try (FileOutputStream os = new FileOutputStream(resaved)) {
								word.updateDocx(docx, os);
							}
							if (resaved.length() == 0) throw new IllegalStateException("Word produced an empty docx");
							m.println(id + ".resaved=" + ZonedDateTime.now());
							System.out.println("  resaved " + id);
						} catch (Throwable t) {
							failed++;
							resaved.delete();
							m.println(id + ".RESAVE_FAILED=" + rootCause(t));
							System.out.println("  RESAVE FAILED " + id + ": " + rootCause(t));
						}
						m.flush();
					}
				}
				if (pdf.length() > 0 && !force) {
					skipped++;
					continue;
				}
				progress("converting", id, n, files.length, docx);
				try {
					WordprocessingMLPackage pkg = Docx4J.load(docx);
					try (FileOutputStream os = new FileOutputStream(pdf)) {
						word.export(pkg, os);
					}
					if (pdf.length() == 0) throw new IllegalStateException("Word produced an empty PDF");
					m.println(id + ".compatibilityMode=" + compatMode(pkg));
					m.println(id + ".generated=" + ZonedDateTime.now());
					done++;
					System.out.println("  golden " + id);
				} catch (Throwable t) {
					failed++;
					pdf.delete();
					m.println(id + ".FAILED=" + t);
					System.out.println("  FAILED " + id + ": " + t);
					t.printStackTrace(System.out);
				}
				m.flush();
			}
		}
		System.out.printf("done %d, skipped (already present) %d, failed %d%n", done, skipped, failed);
		// documents4j keeps worker threads; do not wait for them
		System.exit(failed == 0 ? 0 : 1);
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
