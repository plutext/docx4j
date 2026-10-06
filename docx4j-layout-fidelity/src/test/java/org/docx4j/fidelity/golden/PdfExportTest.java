/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** {@link PdfExport}: the export options as the PDF shows them. */
public class PdfExportTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void aPlainPdfReadsAsItsVersionAndProducer() throws Exception {
		File pdf = tmp.newFile("plain.pdf");
		try (PDDocument doc = new PDDocument()) {
			doc.setVersion(1.7f);
			doc.addPage(new PDPage());
			doc.getDocumentInformation().setProducer("Microsoft® Word for Microsoft 365");
			doc.save(pdf);
		}
		assertEquals("PDF 1.7, producer Microsoft® Word for Microsoft 365, not tagged, no bookmarks, not PDF/A,"
				+ " every font embedded", PdfExport.record(pdf));
	}

	/** Tagged, bookmarked, PDF/A, and a standard-14 font it does not embed. */
	@Test
	public void theOptionsWordCanBeAskedForShow() throws Exception {
		File pdf = tmp.newFile("options.pdf");
		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage();
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				cs.beginText();
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				cs.showText("x");
				cs.endText();
			}
			PDMarkInfo mark = new PDMarkInfo();
			mark.setMarked(true);
			doc.getDocumentCatalog().setMarkInfo(mark);
			PDDocumentOutline outline = new PDDocumentOutline();
			PDOutlineItem item = new PDOutlineItem();
			item.setTitle("Heading");
			outline.addLast(item);
			doc.getDocumentCatalog().setDocumentOutline(outline);
			String xmp = "<x:xmpmeta xmlns:x='adobe:ns:meta/'><rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
					+ "<rdf:Description rdf:about='' xmlns:pdfaid='http://www.aiim.org/pdfa/ns/id/'>"
					+ "<pdfaid:part>2</pdfaid:part><pdfaid:conformance>B</pdfaid:conformance>"
					+ "</rdf:Description></rdf:RDF></x:xmpmeta>";
			PDMetadata md = new PDMetadata(doc, new ByteArrayInputStream(xmp.getBytes(StandardCharsets.UTF_8)));
			doc.getDocumentCatalog().setMetadata(md);
			doc.save(pdf);
		}
		String record = PdfExport.record(pdf);
		assertTrue(record, record.contains(", tagged, bookmarks, PDF/A-2b, not embedded: Helvetica"));
	}

	@Test
	public void anUnreadablePdfIsARecordNotAFailure() throws Exception {
		File pdf = tmp.newFile("broken.pdf");
		java.nio.file.Files.write(pdf.toPath(), new byte[] { 1, 2, 3 });
		assertTrue(PdfExport.record(pdf).startsWith("unreadable: "));
	}

	@Test
	public void theRunsLineCountsOnlyWhereTheRecordsDiffer() {
		Map<String, Integer> one = new TreeMap<String, Integer>(Collections.singletonMap("PDF 1.7, a", 144));
		assertEquals("PDF 1.7, a", PdfExport.summary(one));
		Map<String, Integer> two = new TreeMap<String, Integer>(one);
		two.put("PDF 1.7, b", 2);
		assertEquals("PDF 1.7, a (144 PDFs) | PDF 1.7, b (2 PDFs)", PdfExport.summary(two));
		assertEquals("none cut this run", PdfExport.summary(new TreeMap<String, Integer>()));
	}
}
