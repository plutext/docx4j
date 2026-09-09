/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.table;

/**
 * Column widths for a table Word lays out with "autofit" (its default table
 * layout), from the columns' content widths.  Word's algorithm is not
 * documented; measured against Word 365 (CR-001 harness, table-autofit probe)
 * it behaves like the classic automatic table layout of HTML/CSS:
 *
 * <ul>
 * <li>each column has a minimum width (its widest unbreakable content, i.e. the
 *   longest word, plus cell margins) and a maximum (its content unwrapped);
 * <li>a column with a preferred width (w:tcW in twips) is that wide;
 * <li>if the maxima fit in the available width the table takes its maxima
 *   (auto-width tables shrink to their content); if even the minima do not fit
 *   the minima are used; otherwise each auto column gets its minimum plus a
 *   share of the slack proportional to (max - min).
 * </ul>
 *
 * For a 3-column auto table whose cells held "short", "medium length cell" and a
 * paragraph of prose, Word gave 34.3 / 65.5 / 349.6 pt of 451.3 available; this
 * gives 34.7 / 61.6 / 354.7 with docx4j's measurements.
 *
 * All values in twips.
 *
 * @since 17.0.5
 */
public final class AutofitLayout {

	private AutofitLayout() {}

	/**
	 * @param min per-column minimum content widths (including cell margins)
	 * @param max per-column maximum content widths (including cell margins)
	 * @param preferred per-column preferred width, or -1 for an auto column
	 * @param available width the table may occupy
	 * @return column widths
	 */
	public static int[] distribute(int[] min, int[] max, int[] preferred, int available) {
		int n = min.length;
		int[] lo = new int[n], hi = new int[n];
		boolean[] fixed = new boolean[n];
		long sumLo = 0, sumHi = 0, flex = 0;
		for (int i = 0; i < n; i++) {
			if (preferred != null && preferred[i] > 0) {
				lo[i] = hi[i] = preferred[i];
				fixed[i] = true;
			} else {
				lo[i] = Math.max(0, min[i]);
				hi[i] = Math.max(lo[i], max[i]);
				flex += hi[i] - lo[i];
			}
			sumLo += lo[i];
			sumHi += hi[i];
		}
		int[] out = new int[n];
		if (sumHi <= available || available <= 0) {
			System.arraycopy(hi, 0, out, 0, n);
			return out;
		}
		if (sumLo >= available) {
			System.arraycopy(lo, 0, out, 0, n);
			return out;
		}
		long slack = available - sumLo;
		long given = 0;
		int lastFlex = -1;
		for (int i = 0; i < n; i++) {
			if (fixed[i] || flex == 0) {
				out[i] = lo[i];
			} else {
				long share = slack * (hi[i] - lo[i]) / flex;
				out[i] = (int) (lo[i] + share);
				given += share;
				lastFlex = i;
			}
		}
		if (lastFlex >= 0) {
			out[lastFlex] += (int) (slack - given); // rounding remainder
		}
		return out;
	}

	/**
	 * Column widths for a table every one of whose cells states a preferred width
	 * ({@code w:tcW} in twips), laid out as Word lays such a table out when it does not
	 * trust the cached {@code w:tblGrid}: each column's <b>maximum</b> is its preference
	 * (or its content minimum, where that is wider) and its minimum is its content minimum,
	 * and the classic auto layout of {@link #distribute} then applies - the preferences
	 * where they fit, and otherwise each column's minimum plus a share of the slack in
	 * proportion to what its preference is above its minimum.
	 *
	 * <p>Fitted against the {@code w:tblGrid} Word wrote when it re-saved a corpus certificate
	 * (CR-001 harness, {@code RowGridDiff}): seven columns preferring 2116 / 1125 / 1126 / 2236
	 * / 1951 / 948 / 948 twips (10450, on a 9020-twip text column) with content minima of 877 /
	 * 320 / 766 / 1363 / 443 / 320 / 987, laid out by Word as <b>1778 / 905 / 1027 / 1998 /
	 * 1540 / 776 / 986</b>; this gives 1777 / 905 / 1028 / 1997 / 1539 / 776 / 987 - within a
	 * twip on every column, the last of them widened past its 948 to the 987 its {@code M/TONS}
	 * needs.  Treating the preferences as <em>fixed</em> instead ({@link #distribute}) leaves
	 * that column at 948 and shares the rest among the others, 0.5% out.  Over the three
	 * real-document corpora and the probes, on every autofit table whose columns all carry a
	 * preference (1699 tables), this reproduces the grid Word kept or wrote to within 1% on
	 * 1644 and within 5% on 1662, with docx4j's own content minima as input.</p>
	 *
	 * <p>{@link #distribute} is still the pass for a table with any auto column: there the
	 * two readings were measured against Word's grids and neither fits the 22 such tables
	 * well (5 against 1 within 1%), so the fixed reading, which the probes were measured
	 * on, stands.</p>
	 *
	 * @param min per-column minimum content widths (including cell margins)
	 * @param preferred per-column preferred width, or -1 for none (such a column's maximum
	 *        is then its minimum)
	 * @param available width the table may occupy
	 * @return column widths
	 * @since 17.1.1
	 */
	public static int[] distributePreferredAsMaximum(int[] min, int[] preferred, int available) {
		int n = min.length;
		int[] lo = new int[n], hi = new int[n];
		long sumLo = 0, sumHi = 0, flex = 0;
		for (int i = 0; i < n; i++) {
			lo[i] = Math.max(0, min[i]);
			int p = preferred == null || i >= preferred.length ? -1 : preferred[i];
			hi[i] = Math.max(lo[i], p);
			flex += hi[i] - lo[i];
			sumLo += lo[i];
			sumHi += hi[i];
		}
		int[] out = new int[n];
		if (sumHi <= available || available <= 0) {
			System.arraycopy(hi, 0, out, 0, n);
			return out;
		}
		if (sumLo >= available || flex == 0) {
			System.arraycopy(lo, 0, out, 0, n);
			return out;
		}
		long slack = available - sumLo;
		double[] exact = new double[n];
		for (int i = 0; i < n; i++) exact[i] = lo[i] + (double) slack * (hi[i] - lo[i]) / flex;
		// largest-remainder rounding, which is the rounding Word's grids show: on the
		// certificate a share of 1538.97 comes out 1540 in Word and 1538 truncated
		roundToTotal(exact, out, available);
		return out;
	}

	/**
	 * Column widths for a table whose columns, as sized, do not fit the width it has:
	 * how Word shares the shortfall.
	 *
	 * <p>Measured against the {@code w:tblGrid} Word itself wrote when it re-saved the
	 * real-document corpora (CR-001 harness, {@code ShortfallFit}): <b>a column's cell
	 * margins are a fixed cost, and only the text is squeezed</b> - each column keeps its
	 * floor (its margins, plus any picture, which Word does not shrink) and what is left of
	 * the table's width is shared in proportion to what each column's width is <em>above</em>
	 * its floor.  On a 36-column table whose content minima are 2672 twips for the first
	 * column, 664 for the next 34 and 881 for the last, with 216 twips of cell margins each
	 * and 9350 to share, this gives 427 / 254.4 / 273 against Word's 431 / 254.35 / 271;
	 * scaling every column in proportion (the rule this replaces) gives 987 / 237 / 315,
	 * and an equal division 260 each.  On a table of a text column beside a picture Word
	 * keeps the picture's 403 twips and breaks the text into what is left.</p>
	 *
	 * <p>Where the floors alone do not fit, or nothing stands above them, every column is
	 * scaled in proportion instead.  Widths which already fit are returned as they are.</p>
	 *
	 * @param widths the columns as sized (their content minima, or the widths they were given)
	 * @param floor the incompressible part of each column - its cell margins plus the
	 *        widest picture it holds - or null for none
	 * @param available the width the table has
	 * @return column widths summing to {@code available} (or the widths, where they fit)
	 * @since 17.1.1
	 */
	public static int[] squeeze(int[] widths, int[] floor, int available) {
		int n = widths.length;
		int[] out = new int[n];
		long sum = 0, sumFloor = 0, sumText = 0;
		long[] f = new long[n], text = new long[n];
		for (int i = 0; i < n; i++) {
			f[i] = floor == null || i >= floor.length ? 0 : Math.max(0, floor[i]);
			text[i] = Math.max(0, widths[i] - f[i]);
			sum += widths[i];
			sumFloor += f[i];
			sumText += text[i];
		}
		if (available <= 0 || sum <= available || n == 0) {
			System.arraycopy(widths, 0, out, 0, n);
			return out;
		}
		double[] exact = new double[n];
		if (sumFloor >= available || sumText <= 0) {
			// not even the margins fit: every column in proportion
			for (int i = 0; i < n; i++) exact[i] = (double) Math.max(0, widths[i]) * available / Math.max(1, sum);
		} else {
			long room = available - sumFloor;
			for (int i = 0; i < n; i++) exact[i] = f[i] + (double) room * text[i] / sumText;
		}
		roundToTotal(exact, out, available);
		return out;
	}

	/**
	 * Whole twips from exact shares, summing to {@code total}: each column takes the
	 * whole part of its share (at least 1), and the twips that leaves over go one each to
	 * the columns with the largest fractional parts, earlier columns first - which is
	 * also the shape of a grid Word writes (twelve columns of 255 and then 22 of 254 for
	 * 34 equal shares of 8648).
	 */
	private static void roundToTotal(double[] exact, int[] out, int total) {
		int n = exact.length;
		long given = 0;
		for (int i = 0; i < n; i++) {
			out[i] = Math.max(1, (int) Math.floor(exact[i]));
			given += out[i];
		}
		long left = total - given;
		if (left > 0) {
			Integer[] order = new Integer[n];
			for (int i = 0; i < n; i++) order[i] = i;
			java.util.Arrays.sort(order, (a, b) -> {
				double fa = exact[a] - Math.floor(exact[a]), fb = exact[b] - Math.floor(exact[b]);
				return fa == fb ? Integer.compare(a, b) : Double.compare(fb, fa);
			});
			for (int k = 0; left > 0; k = (k + 1) % n, left--) out[order[k]]++;
		} else if (left < 0) {
			// only where every share rounded up to the 1-twip minimum: take from the widest
			int widest = 0;
			for (int i = 1; i < n; i++) if (out[i] > out[widest]) widest = i;
			out[widest] = (int) Math.max(1, out[widest] + left);
		}
	}
}
