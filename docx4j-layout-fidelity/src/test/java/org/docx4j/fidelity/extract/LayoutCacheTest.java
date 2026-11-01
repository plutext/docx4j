/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.After;
import org.junit.Test;

/**
 * Word's PDFs' layouts kept between runs: what the cache hands back is what extracting the
 * PDF gives, an entry is made once and then read, and anything the extraction depends on
 * (here a {@code fidelity.*} property) leads to another directory rather than a stale entry.
 */
public class LayoutCacheTest {

	@After
	public void clear() {
		System.clearProperty("fidelity.layoutCache");
		System.clearProperty("fidelity.wordGapEm");
		System.clearProperty("fidelity.threads");
		LayoutCache.reset();
	}

	@Test
	public void theCachedLayoutIsTheExtractedOne() throws Exception {
		Path root = Files.createTempDirectory("layout-cache");
		File pdf = pdf(root, "Printed 10/30/26 05:21 PM", "second line");
		System.setProperty("fidelity.layoutCache", root.resolve("cache").toString());
		LayoutCache.reset();

		String fresh = describe(PdfLayoutExtractor.extract(pdf));
		String cold = describe(LayoutCache.reference(pdf));
		assertEquals(1, entries(LayoutCache.cacheDir()));
		String warm = describe(LayoutCache.reference(pdf));
		assertEquals(fresh, cold);
		assertEquals(fresh, warm);
		assertEquals(1, entries(LayoutCache.cacheDir()));
		assertTrue(fresh, fresh.contains("second line"));
	}

	@Test
	public void anExtractionPropertyIsAnotherDirectoryARunPropertyIsNot() throws Exception {
		Path root = Files.createTempDirectory("layout-cache");
		System.setProperty("fidelity.layoutCache", root.toString());
		LayoutCache.reset();
		Path plain = LayoutCache.cacheDir();

		System.setProperty("fidelity.threads", "12");
		LayoutCache.reset();
		assertEquals(plain, LayoutCache.cacheDir());

		System.setProperty("fidelity.wordGapEm", "0.3");
		LayoutCache.reset();
		assertNotEquals(plain, LayoutCache.cacheDir());
	}

	@Test
	public void offExtractsAndKeepsNothing() throws Exception {
		Path root = Files.createTempDirectory("layout-cache");
		File pdf = pdf(root, "only line");
		System.setProperty("fidelity.layoutCache", "off");
		LayoutCache.reset();
		assertNull(LayoutCache.cacheDir());
		assertEquals(describe(PdfLayoutExtractor.extract(pdf)), describe(LayoutCache.reference(pdf)));
		try (Stream<Path> s = Files.list(root)) {
			assertFalse(s.anyMatch(Files::isDirectory));
		}
	}

	private static File pdf(Path dir, String... lines) throws Exception {
		File pdf = dir.resolve("golden.pdf").toFile();
		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage();
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				cs.beginText();
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				cs.newLineAtOffset(72, 700);
				for (String line : lines) {
					cs.showText(line);
					cs.newLineAtOffset(0, -20);
				}
				cs.endText();
				cs.addRect(72, 600, 200, 1);
				cs.fill();
			}
			doc.save(pdf);
		}
		return pdf;
	}

	/** Everything a layout holds, as text. */
	private static String describe(PdfLayout layout) {
		StringBuilder sb = new StringBuilder();
		sb.append(layout.pageWidths).append(layout.pageHeights).append('\n');
		for (PdfLayout.Line l : layout.lines) {
			sb.append(l).append(' ').append(l.size).append(' ').append(l.font).append(' ').append(l.x1).append(' ')
					.append(l.leadNumberEnd).append(' ').append(l.restX0).append(' ').append(l.key()).append('\n');
		}
		for (PdfLayout.Box b : layout.boxes) sb.append(b).append('\n');
		return sb.toString();
	}

	private static long entries(Path dir) throws Exception {
		try (Stream<Path> s = Files.list(dir)) {
			return s.filter(p -> p.getFileName().toString().endsWith(".layout")).count();
		}
	}
}
