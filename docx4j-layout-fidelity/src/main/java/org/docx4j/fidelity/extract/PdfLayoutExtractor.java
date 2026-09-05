package org.docx4j.fidelity.extract;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

/** Builds a {@link PdfLayout} from a PDF with PDFBox: text lines via PDFTextStripper, boxes via a graphics stream engine. */
public final class PdfLayoutExtractor {

	private PdfLayoutExtractor() {}

	public static PdfLayout extract(File pdf) throws IOException {
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PdfLayout out = new PdfLayout();
			for (PDPage page : doc.getPages()) {
				out.pageWidths.add((double) page.getMediaBox().getWidth());
				out.pageHeights.add((double) page.getMediaBox().getHeight());
			}
			int i = 0;
			for (PDPage page : doc.getPages()) {
				new BoxCollector(page, i, out).run();
				i++;
			}
			// boxes first: the text collector uses vertical rules (table borders) as split points
			TextCollector tc = new TextCollector(out);
			tc.setSortByPosition(true);
			tc.getText(doc);
			out.lines.sort((a, b) -> a.page != b.page ? Integer.compare(a.page, b.page)
					: (Math.abs(a.y - b.y) > 0.01 ? Double.compare(a.y, b.y) : Double.compare(a.x0, b.x0)));
			return out;
		}
	}

	/**
	 * Collects every glyph position per page, then forms lines itself: glyphs are
	 * clustered by baseline, and a cluster is split into separate lines where the
	 * horizontal gap exceeds 0.7 x the font size (table cells, tab stops, columns).
	 * PDFTextStripper's own line grouping merges table cells that share a baseline.
	 */
	private static final class TextCollector extends PDFTextStripper {
		private final PdfLayout out;
		private final List<TextPosition> pagePositions = new ArrayList<>();

		TextCollector(PdfLayout out) {
			this.out = out;
		}

		@Override
		protected void writeString(String s, List<TextPosition> positions) {
			pagePositions.addAll(positions);
		}

		@Override
		protected void endPage(PDPage page) throws IOException {
			formLines(getCurrentPageNo() - 1);
			pagePositions.clear();
			super.endPage(page);
		}

		private void formLines(int pageIndex) {
			List<TextPosition> ps = new ArrayList<>(pagePositions);
			ps.sort((a, b) -> Math.abs(a.getYDirAdj() - b.getYDirAdj()) > 0.01f
					? Float.compare(a.getYDirAdj(), b.getYDirAdj()) : Float.compare(a.getXDirAdj(), b.getXDirAdj()));
			List<TextPosition> cluster = new ArrayList<>();
			float clusterY = 0;
			for (TextPosition tp : ps) {
				float tol = Math.max(1f, 0.3f * tp.getFontSizeInPt());
				if (!cluster.isEmpty() && Math.abs(tp.getYDirAdj() - clusterY) > tol) {
					emit(pageIndex, cluster);
					cluster = new ArrayList<>();
				}
				if (cluster.isEmpty()) clusterY = tp.getYDirAdj();
				cluster.add(tp);
			}
			if (!cluster.isEmpty()) emit(pageIndex, cluster);
		}

		/**
		 * Split a baseline cluster into lines (a) at a gap wider than 0.7 em that is also
		 * more than three times the cluster's median word gap (tab stops, borderless
		 * cells), or (b) at any gap crossed by a vertical rule (table borders). Justified
		 * text has uniformly wide word gaps, so (a) keeps such lines together.
		 */
		private void emit(int pageIndex, List<TextPosition> cluster) {
			cluster.sort((a, b) -> Float.compare(a.getXDirAdj(), b.getXDirAdj()));
			List<Float> gaps = new ArrayList<>();
			for (int i = 1; i < cluster.size(); i++) {
				float g = cluster.get(i).getXDirAdj() - (cluster.get(i - 1).getXDirAdj() + cluster.get(i - 1).getWidthDirAdj());
				if (g > 0.15f * cluster.get(i - 1).getFontSizeInPt()) gaps.add(g);
			}
			Collections.sort(gaps);
			float medianWordGap = gaps.isEmpty() ? 0f : gaps.get(gaps.size() / 2);
			List<TextPosition> run = new ArrayList<>();
			TextPosition prev = null;
			for (TextPosition tp : cluster) {
				if (prev != null) {
					float from = prev.getXDirAdj() + prev.getWidthDirAdj();
					float gap = tp.getXDirAdj() - from;
					float em = Math.max(prev.getFontSizeInPt(), 1f);
					boolean wide = gap > 0.7f * em && gap > 3f * medianWordGap;
					if (wide || (gap > 0 && verticalRuleBetween(pageIndex, from, tp.getXDirAdj(), tp.getYDirAdj(), em))) {
						addLine(pageIndex, run);
						run = new ArrayList<>();
					}
				}
				run.add(tp);
				prev = tp;
			}
			addLine(pageIndex, run);
		}

		/** A thin vertical stroke/fill box lying horizontally inside [x0,x1] and vertically spanning the baseline. */
		private boolean verticalRuleBetween(int pageIndex, float x0, float x1, float baseline, float em) {
			for (PdfLayout.Box b : out.boxes) {
				if (b.page != pageIndex || b.w > 3 || b.h < 0.5 * em) continue;
				double cx = b.x + b.w / 2;
				if (cx < x0 || cx > x1) continue;
				if (b.y <= baseline && b.y + b.h >= baseline - 0.7 * em) return true;
			}
			return false;
		}

		private void addLine(int pageIndex, List<TextPosition> run) {
			if (run.isEmpty()) return;
			StringBuilder text = new StringBuilder();
			List<Double> ys = new ArrayList<>();
			/* x0/x1 span the ink, not the whitespace glyphs around it.  Word writes a
			 * tab as a space glyph at the position the tab started from, and ends a
			 * justified line with the space that carries the paragraph mark's size, so
			 * counting those made a line look up to 3pt wider at each end than the text
			 * on it - and made a correct indent look like a 60 twip error. */
			double x0 = Double.MAX_VALUE, x1 = -Double.MAX_VALUE;
			TextPosition prev = null;
			TextPosition firstInk = null;
			for (TextPosition tp : run) {
				if (prev != null) {
					float gap = tp.getXDirAdj() - (prev.getXDirAdj() + prev.getWidthDirAdj());
					if (gap > 0.15f * prev.getFontSizeInPt() && text.length() > 0 && text.charAt(text.length() - 1) != ' ') {
						text.append(' ');
					}
				}
				text.append(tp.getUnicode());
				ys.add((double) tp.getYDirAdj());
				if (!isBlank(tp)) {
					if (firstInk == null) firstInk = tp;
					x0 = Math.min(x0, tp.getXDirAdj());
					x1 = Math.max(x1, tp.getXDirAdj() + tp.getWidthDirAdj());
				}
				prev = tp;
			}
			String t = text.toString().trim().replaceAll("\\s+", " ");
			if (t.isEmpty()) return;
			Collections.sort(ys);
			PdfLayout.Line l = new PdfLayout.Line();
			l.page = pageIndex;
			l.y = ys.get(ys.size() / 2);
			l.x0 = x0;
			l.x1 = x1;
			TextPosition first = firstInk != null ? firstInk : run.get(0);
			l.size = first.getFontSizeInPt();
			l.font = first.getFont() == null ? "" : String.valueOf(first.getFont().getName());
			l.text = t;
			out.lines.add(l);
		}

		/** Whether this glyph puts no ink on the page (a space, or a tab written as one). */
		private static boolean isBlank(TextPosition tp) {
			String u = tp.getUnicode();
			if (u == null || u.isEmpty()) return true;
			for (int i = 0; i < u.length(); i++) {
				char c = u.charAt(i);
				if (!Character.isWhitespace(c) && c != '\u00a0' && c != '\u200b') return false;
			}
			return true;
		}
	}

	/** Records the bounding box of every stroked/filled path and every image, in top-left page coordinates. */
	private static final class BoxCollector extends PDFGraphicsStreamEngine {
		private final int pageIndex;
		private final PdfLayout out;
		private final double pageHeight;
		private GeneralPath path = new GeneralPath();

		BoxCollector(PDPage page, int pageIndex, PdfLayout out) {
			super(page);
			this.pageIndex = pageIndex;
			this.out = out;
			this.pageHeight = page.getMediaBox().getHeight();
		}

		void run() throws IOException {
			processPage(getPage());
		}

		private void record(String kind, Rectangle2D r) {
			if (r == null || (r.getWidth() == 0 && r.getHeight() == 0)) return;
			PdfLayout.Box b = new PdfLayout.Box();
			b.page = pageIndex;
			b.kind = kind;
			b.x = r.getX();
			b.y = pageHeight - (r.getY() + r.getHeight());
			b.w = r.getWidth();
			b.h = r.getHeight();
			out.boxes.add(b);
		}

		@Override
		public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
			path.moveTo((float) p0.getX(), (float) p0.getY());
			path.lineTo((float) p1.getX(), (float) p1.getY());
			path.lineTo((float) p2.getX(), (float) p2.getY());
			path.lineTo((float) p3.getX(), (float) p3.getY());
			path.closePath();
		}

		@Override
		public void drawImage(PDImage pdImage) {
			Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
			// unit square mapped through the CTM
			Point2D a = ctm.transformPoint(0, 0);
			Point2D b = ctm.transformPoint(1, 0);
			Point2D c = ctm.transformPoint(1, 1);
			Point2D d = ctm.transformPoint(0, 1);
			double minX = Math.min(Math.min(a.getX(), b.getX()), Math.min(c.getX(), d.getX()));
			double maxX = Math.max(Math.max(a.getX(), b.getX()), Math.max(c.getX(), d.getX()));
			double minY = Math.min(Math.min(a.getY(), b.getY()), Math.min(c.getY(), d.getY()));
			double maxY = Math.max(Math.max(a.getY(), b.getY()), Math.max(c.getY(), d.getY()));
			record("image", new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY));
		}

		@Override
		public void clip(int windingRule) {
			// clipping paths are not layout; drop the pending path
			path = new GeneralPath();
		}

		@Override
		public void moveTo(float x, float y) {
			path.moveTo(x, y);
		}

		@Override
		public void lineTo(float x, float y) {
			path.lineTo(x, y);
		}

		@Override
		public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
			path.curveTo(x1, y1, x2, y2, x3, y3);
		}

		@Override
		public Point2D getCurrentPoint() {
			return path.getCurrentPoint() == null ? new Point2D.Float(0, 0) : path.getCurrentPoint();
		}

		@Override
		public void closePath() {
			path.closePath();
		}

		@Override
		public void endPath() {
			path = new GeneralPath();
		}

		@Override
		public void strokePath() {
			record("stroke", path.getBounds2D());
			path = new GeneralPath();
		}

		@Override
		public void fillPath(int windingRule) {
			record("fill", path.getBounds2D());
			path = new GeneralPath();
		}

		@Override
		public void fillAndStrokePath(int windingRule) {
			record("fill", path.getBounds2D());
			path = new GeneralPath();
		}

		@Override
		public void shadingFill(COSName shadingName) {
			// ignore
		}
	}
}
