import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Regenerates the derived formats of the Getting Started guide from the
 * canonical docx, using docx4j itself (the HTML via the visitor exporter —
 * the 17.0.4 default — the Markdown via docx4j-markdown, and, from 17.0.5,
 * the PDF via docx4j-export-fo with the Word layout managers).  The TOC is
 * updated first with docx4j's own pagination (TocGenerator.updateToc), so
 * the contents page numbers match the PDF docx4j produces; the canonical
 * docx is not modified.
 *
 * Run per release, from the repo root, against the installed artifacts
 * (JEP 330 single-file launch; needs docx4j-core, docx4j-JAXB-ReferenceImpl,
 * docx4j-markdown, docx4j-export-fo and the croscore/crosextra font jars on
 * the classpath — or just the fat jar):
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

		// Update the TOC (entries and page numbers) with docx4j's own pagination
		// (TocGenerator paginates via docx4j-export-fo), so the PDF's contents page
		// matches the PDF; the updated package is saved to a temp file that the
		// three conversions below load, and the canonical docx is left alone.
		WordprocessingMLPackage tocPkg = Docx4J.load(new java.io.File(docx));
		new org.docx4j.toc.TocGenerator(tocPkg).updateToc(false);
		java.io.File updated = java.io.File.createTempFile("GettingStarted-toc", ".docx");
		updated.deleteOnExit();
		Docx4J.save(tocPkg, updated);
		System.out.println("TOC OK");
		docx = updated.getPath();

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
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
		java.nio.file.Files.writeString(
				java.nio.file.Path.of(outDir, "Docx4j_GettingStarted.html"),
				breakAtBlockBoundaries(baos.toString(java.nio.charset.StandardCharsets.UTF_8)));
		System.out.println("HTML OK");

		// PDF via XSL FO / FOP with the Word layout managers (17.0.5); fonts: the
		// docx names Calibri/Cambria/Consolas, mapped to Carlito/Caladea/Cousine
		pkg = Docx4J.load(new java.io.File(docx));
		try (java.io.FileOutputStream pdf = new java.io.FileOutputStream(
				java.nio.file.Path.of(outDir, "Docx4j_GettingStarted.pdf").toFile())) {
			org.docx4j.convert.out.FOSettings fo = Docx4J.createFOSettings();
			fo.setOpcPackage(pkg);
			Docx4J.toFO(fo, pdf, Docx4J.FLAG_NONE); // explicitly the FO pathway
		}
		System.out.println("PDF OK");
	}

	/**
	 * One line per block element instead of one 100,000+ char line.  NOT
	 * OutputKeys.INDENT: the serializers deliberately set INDENT=no because
	 * indentation inserts whitespace before inline elements (a line break
	 * before a subscript renders as a space).  A newline immediately before
	 * a block-level tag sits between blocks, where HTML ignores it.
	 */
	static String breakAtBlockBoundaries(String html) {
		return html
			.replaceAll("<(p|div|table|tbody|tr|td|th|li|ul|ol|h[1-6]|head|body|style|meta|title|link)([ >/])", "\n<$1$2")
			.replaceAll("</(div|table|tbody|tr|ul|ol|head|body|html)>", "\n</$1>");
	}
}
