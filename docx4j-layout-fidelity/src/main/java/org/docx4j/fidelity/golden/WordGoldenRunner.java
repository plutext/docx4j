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
 * <pre>java -cp ... org.docx4j.fidelity.golden.WordGoldenRunner &lt;corpusDir&gt; &lt;goldenDir&gt;</pre>
 */
public final class WordGoldenRunner {

	public static void main(String[] args) throws Exception {
		File corpusDir = new File(args[0]);
		File goldenDir = new File(args[1]);
		goldenDir.mkdirs();
		File[] files = corpusDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
		if (files == null || files.length == 0) throw new IllegalArgumentException("no docx in " + corpusDir);
		Arrays.sort(files);

		Documents4jLocalServices word = new Documents4jLocalServices();
		try (PrintWriter m = new PrintWriter(new FileWriter(new File(goldenDir, "golden-manifest.properties")))) {
			m.println("source=documents4j-local (desktop Word)");
			m.println("generated=" + ZonedDateTime.now());
			m.println("os=" + System.getProperty("os.name") + " " + System.getProperty("os.version"));
			m.println("java=" + System.getProperty("java.version"));
			m.println("docx4j.version=" + Docx4J.class.getPackage().getImplementationVersion());
			for (File docx : files) {
				String id = docx.getName().replaceAll("\\.docx$", "");
				WordprocessingMLPackage pkg = Docx4J.load(docx);
				try (FileOutputStream os = new FileOutputStream(new File(goldenDir, id + ".pdf"))) {
					word.export(pkg, os);
				}
				m.println(id + ".compatibilityMode=" + compatMode(pkg));
				System.out.println("golden " + id);
			}
		} finally {
			// documents4j keeps worker threads; do not wait for them
			System.exit(0);
		}
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
