package org.docx4j.fidelity.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.List;

import javax.imageio.ImageIO;

import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.compare.LayoutComparison.Pair;
import org.docx4j.fidelity.compare.PixelComparison;
import org.docx4j.fidelity.extract.PdfLayout.Line;

/** Static HTML: an index of all probes, and a page per probe with side-by-side pages, overlay and line deltas. */
public final class HtmlReport {

	private HtmlReport() {}

	private static final String CSS = "body{font-family:sans-serif;font-size:13px;margin:16px}"
			+ "table{border-collapse:collapse}td,th{border:1px solid #ccc;padding:2px 6px;text-align:right}"
			+ "th{background:#eee}td.t{text-align:left;font-family:monospace}"
			+ ".bad{background:#fdd}.warn{background:#ffd}.ok{background:#dfd}"
			+ ".pages img{max-width:31%;border:1px solid #999;margin:2px}"
			+ "h2{margin-top:28px}";

	public static void write(File dir, String refLabel, String candLabel, List<LayoutComparison.Result> results) throws IOException {
		dir.mkdirs();
		try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, "index.html")))) {
			w.println("<!doctype html><html><head><meta charset='utf-8'><title>Layout fidelity report</title><style>" + CSS + "</style></head><body>");
			w.println("<h1>Layout fidelity report</h1><p>reference: " + esc(refLabel) + " &nbsp; candidate: " + esc(candLabel)
					+ " &nbsp; generated " + ZonedDateTime.now() + "</p>");
			w.println("<table><tr><th>probe</th><th>pages ref/cand</th><th>lines ref/cand</th><th>line parity</th><th>page parity</th>"
					+ "<th>median dy</th><th>max dy</th><th>median dx</th><th>max dx</th><th>worst pixel diff</th><th>first divergence</th></tr>");
			for (LayoutComparison.Result r : results) {
				w.printf("<tr><td class='t'><a href='%s/index.html'>%s</a></td><td%s>%d / %d</td><td>%d / %d</td><td%s>%.0f%%</td><td%s>%.0f%%</td>"
						+ "<td%s>%.2f</td><td>%.2f</td><td>%.2f</td><td>%.2f</td><td%s>%.0f%%</td><td class='t'>%s</td></tr>%n",
						r.id, r.id, cls(r.refPages != r.candPages ? 2 : 0), r.refPages, r.candPages, r.refLines, r.candLines,
						cls(r.lineParity() < 0.9 ? 2 : r.lineParity() < 1 ? 1 : 0), r.lineParity() * 100,
						cls(r.pageParity() < 0.9 ? 2 : r.pageParity() < 1 ? 1 : 0), r.pageParity() * 100,
						cls(Math.abs(r.medianDy) > 2 ? 2 : Math.abs(r.medianDy) > 0.5 ? 1 : 0), r.medianDy, r.maxDy, r.medianDx, r.maxDx,
						cls(r.worstPixelRatio() > 0.5 ? 2 : r.worstPixelRatio() > 0.1 ? 1 : 0), r.worstPixelRatio() * 100,
						esc(r.firstDivergence));
			}
			w.println("</table><p>dy/dx: candidate minus reference, points, over lines matched on the same page. "
					+ "Overlay colours: red = reference only, blue = candidate only, black = both.</p></body></html>");
		}
		for (LayoutComparison.Result r : results) {
			writeProbe(new File(dir, r.id), r);
		}
	}

	private static void writeProbe(File dir, LayoutComparison.Result r) throws IOException {
		dir.mkdirs();
		try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, "index.html")))) {
			w.println("<!doctype html><html><head><meta charset='utf-8'><title>" + esc(r.id) + "</title><style>" + CSS + "</style></head><body>");
			w.println("<h1>" + esc(r.id) + "</h1><p><a href='../index.html'>back</a></p>");
			w.printf("<p>pages %d / %d, lines %d / %d, matched %d (same page %d). First divergence: %s</p>%n",
					r.refPages, r.candPages, r.refLines, r.candLines, r.matched, r.matchedSamePage, esc(r.firstDivergence));
			for (PixelComparison.PageDiff pd : r.pixels) {
				ImageIO.write(pd.ref, "png", new File(dir, "ref-" + (pd.page + 1) + ".png"));
				ImageIO.write(pd.cand, "png", new File(dir, "cand-" + (pd.page + 1) + ".png"));
				ImageIO.write(pd.overlay, "png", new File(dir, "overlay-" + (pd.page + 1) + ".png"));
				w.printf("<h2>page %d &mdash; pixel diff %.0f%%</h2><div class='pages'>"
						+ "<img src='ref-%d.png' title='reference'><img src='cand-%d.png' title='candidate'><img src='overlay-%d.png' title='overlay'></div>%n",
						pd.page + 1, pd.ratio * 100, pd.page + 1, pd.page + 1, pd.page + 1);
			}
			w.println("<h2>matched lines</h2><table><tr><th>ref page</th><th>ref y</th><th>ref x</th><th>cand page</th><th>cand y</th><th>cand x</th><th>dy</th><th>dx</th><th>font/size (ref)</th><th>text</th></tr>");
			int shown = 0;
			for (Pair p : r.pairs) {
				if (shown++ > 400) {
					w.println("<tr><td colspan='10'>...</td></tr>");
					break;
				}
				w.printf("<tr%s><td>%d</td><td>%.2f</td><td>%.2f</td><td>%d</td><td>%.2f</td><td>%.2f</td><td>%.2f</td><td>%.2f</td><td class='t'>%s %.1f</td><td class='t'>%s</td></tr>%n",
						cls(!p.samePage ? 2 : Math.abs(p.dy) > 2 ? 1 : 0), p.ref.page + 1, p.ref.y, p.ref.x0, p.cand.page + 1, p.cand.y, p.cand.x0,
						p.dy, p.dx, esc(p.ref.font), p.ref.size, esc(p.ref.text));
			}
			w.println("</table>");
			writeOnly(w, "reference-only lines (broken differently in candidate)", r.refOnly);
			writeOnly(w, "candidate-only lines", r.candOnly);
			w.println("</body></html>");
		}
	}

	private static void writeOnly(PrintWriter w, String title, List<Line> lines) {
		if (lines.isEmpty()) return;
		w.println("<h2>" + title + " (" + lines.size() + ")</h2><table><tr><th>page</th><th>y</th><th>x</th><th>text</th></tr>");
		int shown = 0;
		for (Line l : lines) {
			if (shown++ > 200) break;
			w.printf("<tr><td>%d</td><td>%.2f</td><td>%.2f</td><td class='t'>%s</td></tr>%n", l.page + 1, l.y, l.x0, esc(l.text));
		}
		w.println("</table>");
	}

	private static String cls(int level) {
		return level == 2 ? " class='bad'" : level == 1 ? " class='warn'" : " class='ok'";
	}

	private static String esc(String s) {
		return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
