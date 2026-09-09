package org.docx4j.fidelity.golden;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

/**
 * The font faces a PDF embeds - what a golden set records about the environment that cut it.
 *
 * <p>Word fetches fonts on its own: Office's cloud fonts, and Windows' on-demand supplemental
 * fonts, arrive when a document that asks for them is opened on a machine that is online. So
 * the reference environment changes itself, and nothing in the docx, the script or the
 * manifest said so - until two cuts of the same corpus four days apart embedded Calibri and
 * Ebrima in one and Lato and Nyala in the other, both documents having asked for Lato and Nyala
 * all along, and neither embedding a font. The first cut, by opening those documents, is most
 * likely what fetched the faces the second cut used. A set therefore records, per document,
 * the faces its PDF embeds, and a run records the distinct faces over the whole set, so two
 * manifests diff at a glance and {@link ResaveInvariance} can say "the fonts moved" instead of
 * leaving it to be worked out from line counts.</p>
 */
final class PdfFonts {

	/**
	 * The distinct base font names the PDF's pages use, sorted, with the subset prefix
	 * ({@code ABCDEF+}) stripped so that two subsets of one face read as one face. Fonts
	 * reached through form XObjects are included (Word draws nothing that way, but FOP
	 * does); annotations are not.
	 */
	static SortedSet<String> faces(File pdf) throws IOException {
		SortedSet<String> faces = new TreeSet<String>();
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			for (PDPage page : doc.getPages()) {
				collect(page.getResources(), faces, 0);
			}
		}
		return faces;
	}

	private static void collect(PDResources resources, SortedSet<String> faces, int depth) throws IOException {
		if (resources == null) return;
		for (COSName name : resources.getFontNames()) {
			PDFont font = resources.getFont(name);
			if (font == null) continue;
			String base = font.getName();
			if (base != null && base.length() > 0) faces.add(strip(base));
		}
		if (depth > 4) return;
		for (COSName name : resources.getXObjectNames()) {
			PDXObject x;
			try {
				x = resources.getXObject(name);
			} catch (IOException e) {
				continue; // an image that does not decode is not a font
			}
			if (x instanceof PDFormXObject) collect(((PDFormXObject)x).getResources(), faces, depth + 1);
		}
	}

	/** {@code ABCDEF+Lato-Bold} to {@code Lato-Bold}: the six-letter subset tag PDF producers put in front of an embedded subset. */
	static String strip(String baseFont) {
		return baseFont.matches("^[A-Z]{6}\\+.*") ? baseFont.substring(7) : baseFont;
	}

	/** The manifest value: comma-separated, sorted, no spaces (a face name has none). */
	static String join(Collection<String> faces) {
		StringBuilder sb = new StringBuilder();
		for (String f : faces) {
			if (sb.length() > 0) sb.append(',');
			sb.append(f);
		}
		return sb.toString();
	}

	/** The faces the PDF embeds as a manifest value, or {@code unreadable: ...} where PDFBox cannot open it - a record, never a failure. */
	static String record(File pdf) {
		try {
			return join(faces(pdf));
		} catch (Exception e) {
			return "unreadable: " + e.getClass().getSimpleName();
		}
	}

	/**
	 * The most recent {@code <id>.fonts=} line per document in a manifest as it stands before
	 * this run appends to it - the previous cut's record, for saying how many documents' faces
	 * moved. A manifest is appended to run after run, so the last line for an id is the latest.
	 * An unreadable or absent manifest is an empty map.
	 */
	static Map<String, String> previous(File manifest) {
		Map<String, String> previous = new LinkedHashMap<String, String>();
		if (!manifest.isFile()) return previous;
		try (BufferedReader r = Files.newBufferedReader(manifest.toPath(), StandardCharsets.UTF_8)) {
			String line;
			while ((line = r.readLine()) != null) {
				int eq = line.indexOf(".fonts=");
				if (eq > 0 && !line.startsWith("#")) {
					previous.put(line.substring(0, eq), line.substring(eq + ".fonts=".length()));
				}
			}
		} catch (IOException e) {
			// a record, not the work
		}
		return previous;
	}

	private PdfFonts() {}
}
