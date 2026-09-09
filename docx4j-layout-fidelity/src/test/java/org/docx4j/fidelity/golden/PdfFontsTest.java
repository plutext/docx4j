package org.docx4j.fidelity.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.SortedSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.Test;

/**
 * What a golden set records about the fonts that cut it: the faces a PDF embeds, subset
 * prefixes stripped, and the previous cut's record read back from a manifest that is
 * appended to run after run.
 */
public class PdfFontsTest {

	@Test
	public void theFacesAPdfUsesAreListedSortedAndOnce() throws Exception {
		File pdf = File.createTempFile("fonts", ".pdf");
		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage();
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				cs.beginText();
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
				cs.showText("one");
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				cs.showText("two");
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
				cs.showText("two again");
				cs.endText();
			}
			doc.save(pdf);
		}
		SortedSet<String> faces = PdfFonts.faces(pdf);
		assertEquals("[Helvetica, Times-Roman]", faces.toString());
		assertEquals("Helvetica,Times-Roman", PdfFonts.record(pdf));
		pdf.delete();
	}

	@Test
	public void theSubsetPrefixIsStripped() {
		assertEquals("Lato-Bold", PdfFonts.strip("BCDFEE+Lato-Bold"));
		assertEquals("Calibri", PdfFonts.strip("Calibri"));
		assertEquals("ABCDEFG+NotASubsetTag", PdfFonts.strip("ABCDEFG+NotASubsetTag"));
		assertEquals("abcdef+lower", PdfFonts.strip("abcdef+lower"));
	}

	@Test
	public void anUnreadablePdfIsARecordNotAFailure() throws Exception {
		File notAPdf = File.createTempFile("fonts", ".pdf");
		Files.write(notAPdf.toPath(), "not a pdf".getBytes(StandardCharsets.US_ASCII));
		assertTrue(PdfFonts.record(notAPdf).startsWith("unreadable: "));
		notAPdf.delete();
	}

	/** The last record per document wins, since a manifest is appended to run after run. */
	@Test
	public void thePreviousCutsRecordIsTheLatestLinePerDocument() throws Exception {
		File manifest = File.createTempFile("golden-manifest", ".properties");
		Files.write(manifest.toPath(), String.join("\n",
				"# run 1",
				"fieldUpdate=on",
				"a.compatibilityMode=15",
				"a.fonts=ArialMT,Calibri",
				"b.fonts=Calibri",
				"fonts=ArialMT,Calibri",
				"# run 2",
				"a.fonts=ArialMT,Lato",
				"").getBytes(StandardCharsets.UTF_8));
		Map<String, String> previous = PdfFonts.previous(manifest);
		assertEquals("ArialMT,Lato", previous.get("a"));
		assertEquals("Calibri", previous.get("b"));
		assertEquals(2, previous.size());
		assertTrue(PdfFonts.previous(new File(manifest.getParentFile(), "no-such-manifest")).isEmpty());
		manifest.delete();
	}
}
