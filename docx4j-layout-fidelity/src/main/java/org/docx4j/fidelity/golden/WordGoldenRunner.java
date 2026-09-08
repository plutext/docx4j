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

		Documents4jLocalServices word = new Documents4jLocalServices();
		int failed = 0;
		int done = 0;
		int skipped = 0;
		try (PrintWriter m = new PrintWriter(new FileWriter(new File(goldenDir, "golden-manifest.properties"), true))) {
			m.println("# run " + ZonedDateTime.now());
			m.println("source=documents4j-local (desktop Word)");
			m.println("os=" + System.getProperty("os.name") + " " + System.getProperty("os.version"));
			m.println("java=" + System.getProperty("java.version"));
			for (File docx : files) {
				String id = docx.getName().replaceAll("\\.docx$", "");
				File pdf = new File(goldenDir, id + ".pdf");
				/* The resave is checked before the PDF's own skip, so that a resaved
				 * directory can be added to a golden set that is already cut without
				 * --force re-cutting every PDF. */
				if (resavedDir != null) {
					File resaved = new File(resavedDir, id + ".docx");
					if (resaved.length() == 0 || force) {
						try (FileOutputStream os = new FileOutputStream(resaved)) {
							word.updateDocx(docx, os);
						}
						if (resaved.length() == 0) {
							resaved.delete();
							m.println(id + ".RESAVE_FAILED=Word produced an empty docx");
							System.out.println("RESAVE FAILED " + id);
						} else {
							resaved(id, m);
							System.out.println("resaved " + id);
						}
						m.flush();
					}
				}
				if (pdf.length() > 0 && !force) {
					skipped++;
					continue;
				}
				try {
					WordprocessingMLPackage pkg = Docx4J.load(docx);
					try (FileOutputStream os = new FileOutputStream(pdf)) {
						word.export(pkg, os);
					}
					if (pdf.length() == 0) throw new IllegalStateException("Word produced an empty PDF");
					m.println(id + ".compatibilityMode=" + compatMode(pkg));
					m.println(id + ".generated=" + ZonedDateTime.now());
					done++;
					System.out.println("golden " + id);
				} catch (Throwable t) {
					failed++;
					pdf.delete();
					m.println(id + ".FAILED=" + t);
					System.out.println("FAILED " + id + ": " + t);
					t.printStackTrace(System.out);
				}
				m.flush();
			}
		}
		System.out.printf("done %d, skipped (already present) %d, failed %d%n", done, skipped, failed);
		// documents4j keeps worker threads; do not wait for them
		System.exit(failed == 0 ? 0 : 1);
	}

	private static void resaved(String id, PrintWriter m) {
		m.println(id + ".resaved=" + ZonedDateTime.now());
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
