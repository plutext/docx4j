package org.docx4j.fidelity.golden;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGridCol;

/**
 * Reports what Word did to a table's declared geometry when it opened and saved the
 * document - the table half of what {@link WordGoldenRunner}'s resave is for.
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.GridDiff &lt;originalDir&gt; &lt;resavedDir&gt;</pre>
 *
 * <p>A {@code w:tblGrid} is Word's own cached layout, so the grid Word writes back is the
 * answer to "what width did Word give this column", stated in twips rather than read off a
 * page image - and where Word rewrites a grid it was given, that grid was not the one Word
 * would have used.  That is what makes a generated probe unusable as evidence about a grid
 * until Word has written one: the harness's grid is whatever the generator chose, and Word
 * recomputes an autofit table's on open.  A table Word leaves alone is one whose grid it
 * agrees with.
 *
 * <p>Prints one line per table whose grid changed, with the widths before and after and the
 * ratio of each column, then a count of the documents and tables affected.  A table whose
 * column count changed is reported as such rather than compared column by column.
 */
public final class GridDiff {

	public static void main(String[] args) throws Exception {
		File origDir = new File(args[0]), resavedDir = new File(args[1]);
		File[] files = origDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
		if (files == null || files.length == 0) throw new IllegalArgumentException("no docx in " + origDir);
		Arrays.sort(files);
		int docsSeen = 0, docsChanged = 0, tablesSeen = 0, tablesChanged = 0;
		for (File orig : files) {
			String id = orig.getName().replaceAll("\\.docx$", "");
			File resaved = new File(resavedDir, id + ".docx");
			if (resaved.length() == 0) continue;
			List<int[]> a, b;
			try {
				a = grids(orig);
				b = grids(resaved);
			} catch (Exception e) {
				System.out.println(id + ": cannot read - " + e);
				continue;
			}
			docsSeen++;
			boolean any = false;
			for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
				tablesSeen++;
				int[] x = a.get(i), y = b.get(i);
				if (Arrays.equals(x, y)) continue;
				tablesChanged++;
				any = true;
				System.out.println(id + " table " + (i + 1)
						+ (x.length != y.length
								? ": columns " + x.length + " -> " + y.length
								: ": " + Arrays.toString(x) + " -> " + Arrays.toString(y)
										+ "  x" + ratios(x, y)));
			}
			if (a.size() != b.size()) {
				System.out.println(id + ": tables " + a.size() + " -> " + b.size());
				any = true;
			}
			if (any) docsChanged++;
		}
		System.out.printf("%d of %d documents had a w:tblGrid rewritten by Word; %d of %d tables%n",
				docsChanged, docsSeen, tablesChanged, tablesSeen);
	}

	/** Each table's w:tblGrid, in document order; an empty array where it has none. */
	private static List<int[]> grids(File docx) throws Exception {
		WordprocessingMLPackage pkg = Docx4J.load(docx);
		ClassFinder finder = new ClassFinder(Tbl.class);
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		List<int[]> out = new ArrayList<int[]>();
		for (Object o : finder.results) {
			Tbl tbl = (Tbl) o;
			if (tbl.getTblGrid() == null || tbl.getTblGrid().getGridCol() == null) {
				out.add(new int[0]);
				continue;
			}
			List<TblGridCol> cols = tbl.getTblGrid().getGridCol();
			int[] w = new int[cols.size()];
			for (int i = 0; i < w.length; i++) {
				BigInteger v = cols.get(i).getW();
				w[i] = v == null ? 0 : v.intValue();
			}
			out.add(w);
		}
		return out;
	}

	/** Word's width over the declared one, per column, to three places. */
	private static String ratios(int[] before, int[] after) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < before.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(before[i] == 0 ? "-" : String.format("%.3f", after[i] / (double) before[i]));
		}
		return sb.append(']').toString();
	}
}
