/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;

/**
 * What Word's PDF export did, read off the PDF it wrote: the export options a golden set was
 * cut with.
 *
 * <p>The conversion script calls {@code SaveAs <pdf>, 17} (wdFormatPDF), as documents4j's own
 * script does, and that call takes no export options: Word applies its own. Which ones were in
 * force - PDF/A or not, tagged or not, bookmarks, and fonts it could not embed (which Word draws
 * as bitmaps, or leaves to the reader, according to "Bitmap text when fonts may not be
 * embedded") - shows in the PDF, so the record is taken from there rather than claimed: the PDF
 * version, the producer, whether it is tagged, whether it has an outline, its PDF/A part, and
 * the fonts it did not embed (Type 3 fonts, which is how bitmapped text comes, are named as
 * such).</p>
 *
 * @since 17.3.1
 */
public final class PdfExport {

	private static final Pattern PDFA_PART = Pattern.compile("pdfaid:part\\s*(?:=\\s*[\"']|>)\\s*(\\d)");
	private static final Pattern PDFA_CONFORMANCE = Pattern.compile("pdfaid:conformance\\s*(?:=\\s*[\"']|>)\\s*([A-Za-z])");

	private PdfExport() {}

	/** The record of one PDF, or {@code unreadable: ...} - a record, never a failure. */
	public static String record(File pdf) {
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PDDocumentCatalog cat = doc.getDocumentCatalog();
			StringBuilder sb = new StringBuilder();
			sb.append("PDF ").append(doc.getVersion());
			String producer = doc.getDocumentInformation() == null ? null : doc.getDocumentInformation().getProducer();
			sb.append(", producer ").append(producer == null || producer.trim().isEmpty() ? "none" : producer.trim());
			boolean tagged = cat.getStructureTreeRoot() != null
					|| (cat.getMarkInfo() != null && cat.getMarkInfo().isMarked());
			sb.append(tagged ? ", tagged" : ", not tagged");
			sb.append(cat.getDocumentOutline() != null && cat.getDocumentOutline().getFirstChild() != null
					? ", bookmarks" : ", no bookmarks");
			sb.append(", ").append(pdfa(cat.getMetadata()));
			SortedSet<String> notEmbedded = new TreeSet<String>();
			for (PDPage page : doc.getPages()) collect(page.getResources(), notEmbedded);
			sb.append(notEmbedded.isEmpty() ? ", every font embedded" : ", not embedded: " + PdfFonts.join(notEmbedded));
			return MachineState.oneLine(sb.toString());
		} catch (Exception e) {
			return "unreadable: " + e.getClass().getSimpleName();
		}
	}

	/** {@code PDF/A-2b} from the XMP metadata, or {@code not PDF/A}. */
	static String pdfa(PDMetadata metadata) {
		if (metadata == null) return "not PDF/A";
		try (InputStream in = metadata.exportXMPMetadata()) {
			String xmp = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			Matcher part = PDFA_PART.matcher(xmp);
			if (!part.find()) return "not PDF/A";
			Matcher conf = PDFA_CONFORMANCE.matcher(xmp);
			return "PDF/A-" + part.group(1) + (conf.find() ? conf.group(1).toLowerCase(java.util.Locale.ROOT) : "");
		} catch (IOException e) {
			return "PDF/A unknown";
		}
	}

	private static void collect(PDResources resources, SortedSet<String> notEmbedded) throws IOException {
		if (resources == null) return;
		for (COSName name : resources.getFontNames()) {
			PDFont font = resources.getFont(name);
			if (font == null) continue;
			if (font instanceof PDType3Font) {
				notEmbedded.add("Type3:" + (font.getName() == null ? name.getName() : font.getName()));
			} else if (!font.isEmbedded()) {
				notEmbedded.add(PdfFonts.strip(font.getName() == null ? name.getName() : font.getName()));
			}
		}
	}

	/** The run's line: each distinct record, with how many PDFs had it where there is more
	 *  than one.  {@code none cut} where the run cut nothing. */
	public static String summary(Map<String, Integer> records) {
		if (records.isEmpty()) return "none cut this run";
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> e : records.entrySet()) {
			if (sb.length() > 0) sb.append(" | ");
			sb.append(e.getKey());
			if (records.size() > 1) sb.append(" (").append(e.getValue()).append(" PDFs)");
		}
		return sb.toString();
	}
}
