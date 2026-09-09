package org.docx4j.fidelity.golden;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.common.writer.AbstractTableWriter;
import org.docx4j.fidelity.golden.ColumnError.FoTable;
import org.docx4j.fidelity.golden.ColumnError.Row;
import org.docx4j.finders.ClassFinder;
import org.docx4j.model.table.AutofitLayout;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblWidth;

/**
 * How does Word share a shortfall among the columns of an autofit table whose content minima
 * do not fit?  This joins what docx4j's column sizer <em>saw</em> - the per-column content
 * minima and maxima it measured, written by
 * {@code docx4j.convert.out.fo.wordLayout.dumpAutofit} - to the {@code w:tblGrid} Word wrote
 * when it re-saved the same document, selects the tables Word had to squeeze, and scores
 * candidate distribution rules against Word's grid over that population.
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.ShortfallFit out.csv &lt;originalDir&gt; &lt;resavedDir&gt; [&lt;originalDir&gt; &lt;resavedDir&gt; ...]</pre>
 *
 * <p>Pairing is {@link ColumnError}'s: the resaved document's tables against the FO tables of
 * an in-process render of the original, by index.  A dump record is joined to a pair by the
 * widths docx4j finally gave the table, which are the {@code fo:table-column} widths in twips
 * exactly, so the join is exact and needs no ordering assumption (the writer meets nested tables
 * before their enclosing one, the FO lists them in pre-order).
 *
 * <p>Two populations are reported, because a shortfall reaches Word by two routes:
 * <ul>
 * <li><b>content</b>: tables docx4j's content pass sized, whose minima sum to more than the
 *     width Word gave the table - the pass's output is then scaled to the page
 *     ({@code AbstractTableWriter.fitToAvailableWidth});
 * <li><b>grid</b>: autofit tables ({@code w:tblW} absent or {@code auto}, layout not fixed)
 *     whose cached {@code w:tblGrid} Word rewrote narrower on re-save - Word re-ran its own
 *     content autofit into the page, and where docx4j's measured minima do not fit that width
 *     either, Word's new grid is a shortfall distribution too.  docx4j lays such a table out on
 *     its grid scaled to the page.
 * </ul>
 * Single-column tables are counted but not scored (there is nothing to distribute).
 *
 * <p>Every rule is scored on <em>shares</em>: the rule's widths are scaled to Word's grid
 * total, so what is measured is the distribution and not the total, which the page fit
 * decides separately.  The error is per column, |ours / Word's - 1|; a table is "within x%"
 * when its worst column is.  The population is also reported per document, because one
 * author's template can supply most of it.
 *
 * <p>The CSV holds both populations, one row per table, so a rule this class does not know
 * about can be tried without re-rendering anything.
 */
public final class ShortfallFit {

	/** One dumped table, as {@code AbstractTableWriter.dumpAutofit} writes it. */
	static final class Dump {
		String document;
		int index, columns;
		boolean contentSized, fitted;
		int available = -1;
		int[] min, max, pref, floor, content, fin;
		boolean used;
	}

	/** The table properties the selection needs, read from the original document. */
	static final class Props {
		boolean fixed;
		String tblWType;   // null where there is no w:tblW
		int tblW;
	}

	/** A joined table. */
	static final class Sample {
		String id;
		int tableIndex;
		String population;   // "content" or "grid"
		int[] declared, word, ours, min, max, pref, floor, content;
		int available;
		int wordTotal, declaredTotal;
		long minTotal;
		boolean contentSized, fixed;
		String tblWType;
	}

	/** A candidate distribution rule. */
	interface Rule {
		String name();
		/** Widths whose total is {@code total}. */
		double[] apply(Sample s, int total);
	}

	public static void main(String[] args) throws Exception {
		File csv = new File(args[0]);
		// -Dfidelity.allTablesCsv=<file>: every table paired with a Word grid and joined to a
		// dump record, shortfall or not, with the sizer's inputs and Word's grid side by side
		String allPath = System.getProperty("fidelity.allTablesCsv");
		PrintWriter allCsv = allPath == null ? null : new PrintWriter(new File(allPath), "UTF-8");
		if (allCsv != null) {
			allCsv.println("id,tableIndex,columns,fixed,tblWType,contentSized,wordRewroteGrid,available,"
					+ "declaredTotal,wordTotal,min,max,pref,floor,content,declared,docx4j,word");
		}
		List<Sample> all = new ArrayList<Sample>();
		int docs = 0, docsWithDump = 0, tablesPaired = 0, tablesContentSized = 0, tablesUnjoined = 0,
				tablesMeasured = 0;
		for (int a = 1; a + 1 < args.length; a += 2) {
			File origDir = new File(args[a]), resavedDir = new File(args[a + 1]);
			File[] files = origDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
			if (files == null) throw new IllegalArgumentException("no docx in " + origDir);
			Arrays.sort(files);
			for (File orig : files) {
				String id = orig.getName().replaceAll("\\.docx$", "");
				File resaved = new File(resavedDir, id + ".docx");
				if (resaved.length() == 0) continue;
				docs++;
				File dumpFile = File.createTempFile("autofit-", ".csv");
				dumpFile.delete();
				Docx4jProperties.setProperty(AbstractTableWriter.DUMP_AUTOFIT, dumpFile.getPath());
				List<int[]> declared, word;
				List<Props> props;
				List<FoTable> ours;
				List<Dump> dumps;
				try {
					declared = ColumnError.grids(orig);
					props = props(orig);
					word = ColumnError.grids(resaved);
					ours = ColumnError.render(orig);
					dumps = readDump(dumpFile);
				} catch (Exception e) {
					System.out.printf(Locale.ROOT, "%-44s failed - %s%n", id, ColumnError.firstLine(e));
					continue;
				} finally {
					dumpFile.delete();
				}
				if (!dumps.isEmpty()) docsWithDump++;
				if (word.size() != ours.size()) {
					System.out.printf(Locale.ROOT, "%-44s tables %d in Word's docx, %d in our FO - skipped%n",
							id, word.size(), ours.size());
					continue;
				}
				List<String> skips = new ArrayList<String>();
				int content = 0, grid = 0;
				for (int i = 0; i < word.size(); i++) {
					Row r = ColumnError.compare(id, i + 1, declared, word.get(i), ours.get(i), skips);
					if (r == null) continue;
					tablesPaired++;
					Dump d = join(dumps, r.docx4j);
					if (d == null) {
						tablesUnjoined++;
						continue;
					}
					if (d.min == null) continue;
					tablesMeasured++;
					if (d.contentSized) tablesContentSized++;
					Sample s = new Sample();
					s.id = id;
					s.tableIndex = r.tableIndex;
					s.declared = i < declared.size() ? declared.get(i) : new int[0];
					s.word = r.word;
					s.ours = r.docx4j;
					s.min = d.min;
					s.max = d.max;
					s.pref = d.pref;
					s.floor = d.floor;
					s.content = d.content;
					s.available = d.available;
					s.wordTotal = r.wordTotal;
					for (int w : s.declared) s.declaredTotal += w;
					for (int m : d.min) s.minTotal += m;
					s.contentSized = d.contentSized;
					Props p = i < props.size() ? props.get(i) : new Props();
					s.fixed = p.fixed;
					s.tblWType = p.tblWType;
					boolean autofit = !p.fixed && (p.tblWType == null || "auto".equals(p.tblWType));
					if (allCsv != null) {
						// every joined table, whether or not it is a shortfall: what the sizer saw
						// beside what Word kept or wrote (RowGridDiff's question, with the minima)
						allCsv.printf(Locale.ROOT, "%s,%d,%d,%s,%s,%s,%s,%d,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s%n",
								ColumnError.csvCell(id), s.tableIndex, s.word.length, s.fixed,
								s.tblWType == null ? "" : s.tblWType, s.contentSized, r.wordRewroteGrid,
								s.available, s.declaredTotal, s.wordTotal, j(s.min), j(s.max), j(s.pref),
								j(s.floor), j(s.content), j(s.declared), j(s.ours), j(s.word));
					}
					if (d.contentSized && s.minTotal > r.wordTotal) {
						s.population = "content";
						content++;
					} else if (!d.contentSized && autofit && r.wordRewroteGrid
							&& s.declared.length == r.word.length
							&& s.declaredTotal > r.wordTotal * 1.01 && s.minTotal > r.wordTotal) {
						s.population = "grid";
						grid++;
					} else {
						continue;
					}
					all.add(s);
				}
				System.out.printf(Locale.ROOT, "%-44s %d tables, shortfall: %d content, %d grid%n",
						id, word.size(), content, grid);
			}
		}

		if (allCsv != null) {
			allCsv.close();
			System.out.println("all joined tables: " + allPath);
		}
		System.out.println();
		System.out.printf(Locale.ROOT, "%d documents, %d produced a dump; %d tables paired with Word's grid,"
				+ " %d measured by the content pass (%d sized by it), %d could not be joined to a dump record%n",
				docs, docsWithDump, tablesPaired, tablesMeasured, tablesContentSized, tablesUnjoined);
		writeCsv(csv, all);
		System.out.println("csv: " + csv);
		for (String pop : new String[] { "content", "grid" }) {
			List<Sample> samples = new ArrayList<Sample>();
			int single = 0;
			Map<String, Integer> perDocument = new TreeMap<String, Integer>();
			for (Sample s : all) {
				if (!pop.equals(s.population)) continue;
				if (s.word.length < 2) { single++; continue; }
				samples.add(s);
				perDocument.merge(s.id, 1, Integer::sum);
			}
			System.out.println();
			System.out.printf(Locale.ROOT, "== %s shortfall: %d tables with 2+ columns in %d documents"
					+ " (+ %d single-column tables, not scored)%n", pop, samples.size(), perDocument.size(), single);
			List<Map.Entry<String, Integer>> byCount = new ArrayList<Map.Entry<String, Integer>>(perDocument.entrySet());
			byCount.sort((x, y) -> y.getValue() - x.getValue());
			for (int i = 0; i < Math.min(12, byCount.size()); i++) {
				System.out.printf(Locale.ROOT, "  %4d  %s%n", byCount.get(i).getValue(), byCount.get(i).getKey());
			}
			int withPref = 0;
			for (Sample s : samples) {
				boolean any = false;
				for (int p : s.pref) any |= p > 0;
				if (any) withPref++;
			}
			System.out.printf(Locale.ROOT, "  tables with a w:tcW on some column: %d of %d%n", withPref, samples.size());
			score(samples, rules());
		}
	}

	// ---- rules -------------------------------------------------------------------------

	static List<Rule> rules() {
		List<Rule> out = new ArrayList<Rule>();
		out.add(power("proportional to min", 1.0));
		out.add(new Rule() {
			public String name() { return "proportional to the declared grid"; }
			public double[] apply(Sample s, int total) { return weights(toDouble(s.declared), total); }
		});
		out.add(new Rule() {
			public String name() { return "proportional to docx4j's widths (now)"; }
			public double[] apply(Sample s, int total) { return weights(toDouble(s.ours), total); }
		});
		out.add(new Rule() {
			public String name() { return "AutofitLayout.distribute into Word's total"; }
			public double[] apply(Sample s, int total) {
				return weights(toDouble(AutofitLayout.distribute(s.min, s.max, s.pref, total)), total);
			}
		});
		out.add(new Rule() {
			public String name() { return "AutofitLayout.squeeze: floor + prop (min-floor)"; }
			public double[] apply(Sample s, int total) {
				return toDouble(AutofitLayout.squeeze(s.min, s.floor, total));
			}
		});
		out.add(power("equal", 0.0));
		for (double k : new double[] { 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 }) out.add(power("min^" + k, k));
		out.add(new Rule() {
			public String name() { return "equal, max-capped"; }
			public double[] apply(Sample s, int total) { return equalCapped(s.max, total); }
		});
		out.add(new Rule() {
			public String name() { return "equal, min-capped (level fill)"; }
			public double[] apply(Sample s, int total) { return equalCapped(s.min, total); }
		});
		for (double f : new double[] { 0.25, 0.5, 0.75 }) {
			out.add(floorPlus("floor " + f + " of equal + prop min", f, false));
			out.add(floorPlus("floor " + f + " of equal + prop (min-floor)", f, true));
		}
		out.add(new Rule() {
			public String name() { return "proportional to max"; }
			public double[] apply(Sample s, int total) { return weights(toDouble(s.max), total); }
		});
		out.add(new Rule() {
			public String name() { return "proportional to sqrt(max)"; }
			public double[] apply(Sample s, int total) {
				double[] w = new double[s.max.length];
				for (int i = 0; i < w.length; i++) w[i] = Math.sqrt(Math.max(1, s.max[i]));
				return weights(w, total);
			}
		});
		out.add(new Rule() {
			public String name() { return "proportional to (min+max)/2"; }
			public double[] apply(Sample s, int total) {
				double[] w = new double[s.max.length];
				for (int i = 0; i < w.length; i++) w[i] = (s.min[i] + s.max[i]) / 2.0;
				return weights(w, total);
			}
		});
		out.add(new Rule() {
			public String name() { return "proportional to log(min)"; }
			public double[] apply(Sample s, int total) {
				double[] w = new double[s.min.length];
				for (int i = 0; i < w.length; i++) w[i] = Math.log(Math.max(2, s.min[i]));
				return weights(w, total);
			}
		});
		return out;
	}

	static Rule power(final String name, final double k) {
		return new Rule() {
			public String name() { return name; }
			public double[] apply(Sample s, int total) {
				double[] w = new double[s.min.length];
				for (int i = 0; i < w.length; i++) w[i] = Math.pow(Math.max(1, s.min[i]), k);
				return weights(w, total);
			}
		};
	}

	static Rule floorPlus(final String name, final double f, final boolean aboveFloor) {
		return new Rule() {
			public String name() { return name; }
			public double[] apply(Sample s, int total) {
				int n = s.min.length;
				double floor = f * total / n;
				double[] w = new double[n];
				double sumW = 0;
				for (int i = 0; i < n; i++) {
					w[i] = aboveFloor ? Math.max(0, s.min[i] - floor) : s.min[i];
					sumW += w[i];
				}
				double remainder = total - floor * n;
				double[] out = new double[n];
				for (int i = 0; i < n; i++) {
					out[i] = floor + (sumW > 0 ? remainder * w[i] / sumW : remainder / n);
				}
				return out;
			}
		};
	}

	/** Equal shares, except that a column whose cap is below its share takes the cap and the
	 *  rest share the remainder; repeated until stable. */
	static double[] equalCapped(int[] cap, int total) {
		int n = cap.length;
		double[] out = new double[n];
		boolean[] capped = new boolean[n];
		while (true) {
			double remaining = total;
			int free = 0;
			for (int i = 0; i < n; i++) {
				if (capped[i]) remaining -= cap[i];
				else free++;
			}
			if (free == 0) break;
			double share = remaining / free;
			boolean changed = false;
			for (int i = 0; i < n; i++) {
				if (!capped[i] && cap[i] < share) {
					capped[i] = true;
					changed = true;
				}
			}
			if (!changed) {
				for (int i = 0; i < n; i++) out[i] = capped[i] ? cap[i] : share;
				return out;
			}
		}
		for (int i = 0; i < n; i++) out[i] = cap[i];
		return out;
	}

	static double[] weights(double[] w, int total) {
		double sum = 0;
		for (double v : w) sum += v;
		double[] out = new double[w.length];
		for (int i = 0; i < w.length; i++) out[i] = sum > 0 ? total * w[i] / sum : (double) total / w.length;
		return out;
	}

	static double[] toDouble(int[] a) {
		double[] d = new double[a.length];
		for (int i = 0; i < a.length; i++) d[i] = a[i];
		return d;
	}

	// ---- scoring -----------------------------------------------------------------------

	static void score(List<Sample> samples, List<Rule> rules) {
		if (samples.isEmpty()) {
			System.out.println("  no tables: nothing to fit");
			return;
		}
		System.out.printf(Locale.ROOT, "  %-44s %8s %8s %8s %6s %6s %10s %8s%n", "rule (scored on " + samples.size()
				+ " tables)", "colMean", "colMed", "tblMed", "<=1%", "<=5%", "twips", "docMean");
		for (Rule rule : rules) {
			List<Double> colErrs = new ArrayList<Double>();
			List<Double> tblErrs = new ArrayList<Double>();
			Map<String, double[]> perDoc = new LinkedHashMap<String, double[]>();
			int within1 = 0, within5 = 0;
			long twips = 0;
			for (Sample s : samples) {
				double[] w;
				try {
					w = rule.apply(s, s.wordTotal);
				} catch (RuntimeException e) {
					continue;
				}
				if (w.length != s.word.length) continue;
				double worst = 0, sum = 0;
				int counted = 0;
				for (int i = 0; i < w.length; i++) {
					if (s.word[i] <= 0) continue;
					double e = Math.abs(w[i] / s.word[i] - 1);
					colErrs.add(e);
					worst = Math.max(worst, e);
					sum += e;
					counted++;
					twips += Math.round(Math.abs(w[i] - s.word[i]));
				}
				if (counted == 0) continue;
				double mean = sum / counted;
				tblErrs.add(mean);
				if (worst <= 0.01) within1++;
				if (worst <= 0.05) within5++;
				double[] d = perDoc.get(s.id);
				if (d == null) perDoc.put(s.id, d = new double[2]);
				d[0] += mean;
				d[1]++;
			}
			double docMean = 0;
			for (double[] d : perDoc.values()) docMean += d[0] / d[1];
			docMean /= Math.max(1, perDoc.size());
			System.out.printf(Locale.ROOT, "  %-44s %8.4f %8.4f %8.4f %6d %6d %10d %8.4f%n", rule.name(),
					mean(colErrs), median(colErrs), median(tblErrs), within1, within5, twips, docMean);
		}
	}

	static double mean(List<Double> v) {
		double s = 0;
		for (double d : v) s += d;
		return v.isEmpty() ? Double.NaN : s / v.size();
	}

	static double median(List<Double> v) {
		if (v.isEmpty()) return Double.NaN;
		double[] a = new double[v.size()];
		for (int i = 0; i < a.length; i++) a[i] = v.get(i);
		Arrays.sort(a);
		return a.length % 2 == 1 ? a[a.length / 2] : (a[a.length / 2 - 1] + a[a.length / 2]) / 2;
	}

	// ---- the documents, the dump and the join ------------------------------------------

	/** Each table's own w:tblPr, in the same order as {@link ColumnError#grids}. */
	static List<Props> props(File docx) throws Exception {
		WordprocessingMLPackage pkg = Docx4J.load(docx);
		ClassFinder finder = new ClassFinder(Tbl.class);
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		List<Props> out = new ArrayList<Props>();
		for (Object o : finder.results) {
			Tbl tbl = (Tbl) o;
			Props p = new Props();
			if (tbl.getTblPr() != null) {
				p.fixed = tbl.getTblPr().getTblLayout() != null
						&& tbl.getTblPr().getTblLayout().getType() == STTblLayoutType.FIXED;
				TblWidth w = tbl.getTblPr().getTblW();
				if (w != null) {
					p.tblWType = w.getType() == null ? "dxa" : w.getType();
					p.tblW = w.getW() == null ? 0 : w.getW().intValue();
					if (p.tblW <= 0 && !"auto".equals(p.tblWType)) p.tblWType = "auto";
				}
			}
			out.add(p);
		}
		return out;
	}

	static List<Dump> readDump(File f) throws Exception {
		List<Dump> out = new ArrayList<Dump>();
		if (!f.exists()) return out;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f),
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = r.readLine()) != null) {
				String[] c = splitCsv(line);
				if (c.length < 12) continue;
				Dump d = new Dump();
				d.document = c[0];
				d.index = Integer.parseInt(c[1]);
				d.columns = Integer.parseInt(c[2]);
				d.contentSized = Boolean.parseBoolean(c[3]);
				d.fitted = Boolean.parseBoolean(c[4]);
				d.available = c[5].isEmpty() ? -1 : Integer.parseInt(c[5]);
				d.min = ints(c[6]);
				d.max = ints(c[7]);
				d.pref = ints(c[8]);
				d.floor = ints(c[9]);
				d.content = ints(c[10]);
				d.fin = ints(c[11]);
				out.add(d);
			}
		}
		return out;
	}

	/** The first unused record whose final widths are exactly the FO's columns. */
	static Dump join(List<Dump> dumps, int[] ours) {
		for (Dump d : dumps) {
			if (!d.used && d.fin != null && Arrays.equals(d.fin, ours)) {
				d.used = true;
				return d;
			}
		}
		return null;
	}

	static int[] ints(String s) {
		s = s.trim();
		if (s.isEmpty()) return null;
		String[] p = s.split(" ");
		int[] out = new int[p.length];
		for (int i = 0; i < p.length; i++) out[i] = Integer.parseInt(p[i]);
		return out;
	}

	static String[] splitCsv(String line) {
		List<String> out = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean q = false;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (q) {
				if (ch == '"') {
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') { sb.append('"'); i++; }
					else q = false;
				} else sb.append(ch);
			} else if (ch == '"') q = true;
			else if (ch == ',') { out.add(sb.toString()); sb.setLength(0); }
			else sb.append(ch);
		}
		out.add(sb.toString());
		return out.toArray(new String[0]);
	}

	static String j(int[] a) {
		return a == null ? "" : ColumnError.join(a);
	}

	static void writeCsv(File csv, List<Sample> samples) throws Exception {
		try (PrintWriter w = new PrintWriter(csv, "UTF-8")) {
			w.println("id,tableIndex,population,columns,fixed,tblWType,available,declaredTotal,wordTotal,"
					+ "min,max,pref,floor,content,declared,docx4j,word");
			for (Sample s : samples) {
				w.printf(Locale.ROOT, "%s,%d,%s,%d,%s,%s,%d,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s%n",
						ColumnError.csvCell(s.id), s.tableIndex, s.population, s.word.length, s.fixed,
						s.tblWType == null ? "" : s.tblWType, s.available, s.declaredTotal, s.wordTotal,
						j(s.min), j(s.max), j(s.pref), j(s.floor), j(s.content), j(s.declared), j(s.ours),
						j(s.word));
			}
		}
	}
}
