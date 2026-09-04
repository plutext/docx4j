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
}
