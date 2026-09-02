import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Regenerates the derived formats of the Getting Started guide from the
 * canonical docx, using docx4j itself (the HTML via the visitor exporter —
 * the 17.0.4 default — and the Markdown via docx4j-markdown).  The PDF is
 * produced separately, from Word (update the TOC field there first: F9).
 *
 * Run per release, from the repo root, against the installed artifacts
 * (JEP 330 single-file launch; needs docx4j-core, docx4j-JAXB-ReferenceImpl,
 * docx4j-markdown and their deps on the classpath — or just the fat jar):
 *
 *   java -cp docx4j-bundle-&lt;version&gt;-shaded.jar etc/GenGettingStartedDocs.java \
 *        docs/Docx4j_GettingStarted.docx docs \
 *        docs/Docx4j_GettingStarted_files Docx4j_GettingStarted_files
 *
 * Do not hand-edit the generated .md / .html — they are derived artifacts;
 * the docx is canonical.
 */
public class GenGettingStartedDocs {
	public static void main(String[] args) throws Exception {
		String docx = args[0], outDir = args[1], filesDir = args[2], filesUri = args[3];

		// Markdown (images extracted, so the md is diffable)
		WordprocessingMLPackage pkg = Docx4J.load(new java.io.File(docx));
		String md = new org.docx4j.markdown.MarkdownExporter(
				new org.docx4j.markdown.MarkdownExportOptions()
					.setImageDirPath(filesDir)
					.setImageTargetUri(filesUri))
			.export(pkg);
		java.nio.file.Files.writeString(java.nio.file.Path.of(outDir, "Docx4j_GettingStarted.md"), md);
		System.out.println("MD OK " + md.length() + " chars");

		// HTML (visitor pathway = the 17.0.4 default); no UUID prefix on image
		// names so HTML and MD share the same extracted files
		pkg = Docx4J.load(new java.io.File(docx)); // fresh instance for the second conversion
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		settings.setImageDirPath(filesDir);
		settings.setImageTargetUri(filesUri);
		settings.setImageHandler(new org.docx4j.convert.out.html.HTMLConversionImageHandler(filesDir, filesUri, false));
		try (java.io.OutputStream os = new java.io.FileOutputStream(
				new java.io.File(outDir, "Docx4j_GettingStarted.html"))) {
			Docx4J.toHTML(settings, os, Docx4J.FLAG_NONE);
		}
		System.out.println("HTML OK");
	}
}
