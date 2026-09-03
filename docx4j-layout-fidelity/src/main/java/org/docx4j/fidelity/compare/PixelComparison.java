package org.docx4j.fidelity.compare;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Level-2 comparison: rasterise both PDFs and count differing ink pixels.
 * The overlay paints reference-only ink red, candidate-only ink blue, shared
 * ink black, so a vertical shift reads as a red/blue "ghost" pair.
 */
public final class PixelComparison {

	public static final class PageDiff {
		public int page;
		public BufferedImage ref, cand, overlay;
		public int inkRef, inkCand, differing;
		/** differing pixels over the union of ink pixels (0 = identical, 1 = disjoint). */
		public double ratio;
	}

	private PixelComparison() {}

	public static List<PageDiff> compare(File refPdf, File candPdf, int dpi) throws IOException {
		List<PageDiff> out = new ArrayList<>();
		try (PDDocument ref = Loader.loadPDF(refPdf); PDDocument cand = Loader.loadPDF(candPdf)) {
			PDFRenderer rr = new PDFRenderer(ref);
			PDFRenderer cr = new PDFRenderer(cand);
			int pages = Math.max(ref.getNumberOfPages(), cand.getNumberOfPages());
			for (int p = 0; p < pages; p++) {
				BufferedImage a = p < ref.getNumberOfPages() ? rr.renderImageWithDPI(p, dpi, ImageType.GRAY) : null;
				BufferedImage b = p < cand.getNumberOfPages() ? cr.renderImageWithDPI(p, dpi, ImageType.GRAY) : null;
				int w = Math.max(a == null ? 0 : a.getWidth(), b == null ? 0 : b.getWidth());
				int h = Math.max(a == null ? 0 : a.getHeight(), b == null ? 0 : b.getHeight());
				PageDiff d = new PageDiff();
				d.page = p;
				d.ref = pad(a, w, h);
				d.cand = pad(b, w, h);
				d.overlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				final int inkThreshold = 200;
				int union = 0;
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						int ga = d.ref.getRaster().getSample(x, y, 0);
						int gb = d.cand.getRaster().getSample(x, y, 0);
						boolean ia = ga < inkThreshold, ib = gb < inkThreshold;
						int rgb;
						if (ia && ib) {
							int g = Math.min(ga, gb);
							rgb = (g << 16) | (g << 8) | g;
						} else if (ia) {
							rgb = 0xD02020;
							d.differing++;
						} else if (ib) {
							rgb = 0x2050D0;
							d.differing++;
						} else {
							rgb = 0xFFFFFF;
						}
						if (ia) d.inkRef++;
						if (ib) d.inkCand++;
						if (ia || ib) union++;
						d.overlay.setRGB(x, y, rgb);
					}
				}
				d.ratio = union == 0 ? 0 : (double) d.differing / union;
				out.add(d);
			}
		}
		return out;
	}

	private static BufferedImage pad(BufferedImage src, int w, int h) {
		BufferedImage dst = new BufferedImage(Math.max(w, 1), Math.max(h, 1), BufferedImage.TYPE_BYTE_GRAY);
		java.awt.Graphics2D g = dst.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
		if (src != null) g.drawImage(src, 0, 0, null);
		g.dispose();
		return dst;
	}
}
