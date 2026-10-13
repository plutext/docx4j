/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.chart.render.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A value axis's bounds and major unit as Excel computes them where the file leaves them
 * automatic (CR-037 §11, findings 5 and 9; probes R2 and R11):
 * <ul>
 * <li>the minimum is zero where the data's range is more than a sixth of its maximum (all
 *     positive), else the data's minimum less half its range, down to the unit; the maximum is
 *     the data's plus a twentieth of the range from that minimum, up to the unit (KB 214075, which
 *     has the data's range); data of either sign is padded a twentieth at both ends; all
 *     negative is mirrored; all zeros is 0 to 1 (on 137 column probes, R1 to R12, none apart);</li>
 * <li>the unit is the finest of 1, 2 and 5 times a power of ten that gives at most ten
 *     intervals, each a label's line, within the axis's length less {@link #RESERVE}
 *     (n lines &lt;= length - reserve); where none does, the coarsest that gives one interval;</li>
 * <li>a log axis runs from the power of its base below the data to the power above, by the base;</li>
 * <li>a 100% axis runs from the data's least share to its greatest, each out to the unit and no
 *     further than -100% and 100%, unpadded (r14-stacked-100-negative: -36% to 77% is -40% to 80%;
 *     all positive, 0 to 100%);</li>
 * <li>a scatter chart's x axis, its data not from zero, is padded a twentieth of their range at
 *     both ends, not the half range below (p6-scatter-dates: 46023 to 46026 is 46022.5 to 46026.5
 *     by 0.5).</li>
 * </ul>
 * A bound or unit the file sets is kept.  The length is the plot's height with one line of
 * category labels (Excel chooses the unit before it wraps or turns them).  A horizontal value axis
 * (a bar chart's, a scatter chart's x axis) has its own test of whether its labels fit
 * ({@link Fits}).
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class AxisScale {

	/**
	 * What a vertical axis's length keeps back from its intervals' lines: 8.93 to 9.89pt, the same at
	 * 7 to 16pt (463 tests: R1 to R13 and r14-sweep-column-h's 182 heights).  Phase 1's
	 * (n + 1) lines within the length and 4.87pt fitted R11 alone, whose 14pt probes set it
	 */
	public static final double RESERVE = 9.41;

	/** At most this many intervals (R2, R11) */
	public static final int MAX_INTERVALS = 10;

	private final double min;
	private final double max;
	private final double unit;
	private final boolean log;

	private AxisScale(double min, double max, double unit, boolean log) {
		this.min = min;
		this.max = max;
		this.unit = unit;
		this.log = log;
	}

	/** The axis's least value */
	public double getMin() {
		return min;
	}

	/** The axis's greatest value */
	public double getMax() {
		return max;
	}

	/** The major unit: a log axis's base */
	public double getUnit() {
		return unit;
	}

	/** Whether the axis is logarithmic */
	public boolean isLog() {
		return log;
	}

	/** The values labelled, min to max */
	public List<Double> values() {
		List<Double> v = new ArrayList<>();
		if (log) {
			for (double x = min; x <= max * (1 + 1e-9); x *= unit) v.add(x);
			return v;
		}
		BigDecimal lo = BigDecimal.valueOf(min), u = BigDecimal.valueOf(unit);
		// (a doubled unit can stop short of the maximum: 0 to 60000 by 40000)
		int n = (int) Math.floor((max - min) / unit + 1e-9);
		for (int k = 0; k <= n; k++) v.add(lo.add(u.multiply(BigDecimal.valueOf(k))).doubleValue());
		return v;
	}

	/** Where a value falls between min (0) and max (1) */
	public double fraction(double v) {
		if (log) return (Math.log(v) - Math.log(min)) / (Math.log(max) - Math.log(min));
		return (v - min) / (max - min);
	}

	/**
	 * The unit doubled, the bounds kept: a horizontal axis's labels that overlap at the unit its
	 * length gives (r14-sweep-bar-x10000: 0 to 60000 by 40000)
	 */
	public AxisScale doubled() {
		return new AxisScale(min, max, unit * 2, log);
	}

	public static int intervals(double lo, double hi, double u) {
		return (int) Math.round((hi - lo) / u);
	}

	/** Whether an axis's labels fit at a unit: its bounds, the unit, the intervals */
	public interface Fits {
		boolean fits(double min, double max, double unit, int intervals);
	}

	/**
	 * @param dataMin, dataMax the values plotted
	 * @param setMin, setMax, setUnit what the file states (null: automatic)
	 * @param logBase a log axis's base, or null
	 * @param length the axis's length in points (see the class's note)
	 * @param line the height of a label's line
	 */
	public static AxisScale of(double dataMin, double dataMax, Double setMin, Double setMax, Double setUnit, Double logBase,
			double length, double line) {
		return of(dataMin, dataMax, setMin, setMax, setUnit, logBase, (lo, hi, u, n) -> n * line <= length - RESERVE, Bounds.VALUE);
	}

	/** How an axis's automatic bounds are found (see the class's note) */
	public enum Bounds {
		/** a value axis's */
		VALUE,
		/** a 100% chart's */
		PERCENT,
		/** a scatter chart's x axis's */
		X
	}

	/** An axis's scale, its bounds found so, its length and line as {@link #of(double, double, Double, Double, Double, Double, double, double)}'s */
	public static AxisScale of(double dataMin, double dataMax, Double setMin, Double setMax, Double setUnit, Double logBase,
			double length, double line, Bounds kind) {
		return of(dataMin, dataMax, setMin, setMax, setUnit, logBase, (lo, hi, u, n) -> n * line <= length - RESERVE, kind);
	}

	/**
	 * @param fits whether the labels fit at a unit (a vertical axis: n lines within its length less
	 *     {@link #RESERVE})
	 */
	public static AxisScale of(double dataMin, double dataMax, Double setMin, Double setMax, Double setUnit, Double logBase, Fits fits) {
		return of(dataMin, dataMax, setMin, setMax, setUnit, logBase, fits, Bounds.VALUE);
	}

	/** @param kind how the bounds are found (see the class's note) */
	public static AxisScale of(double dataMin, double dataMax, Double setMin, Double setMax, Double setUnit, Double logBase, Fits fits,
			Bounds kind) {
		if (logBase != null && logBase > 1 && dataMin > 0) {
			double b = logBase;
			double lo = setMin != null ? setMin : Math.pow(b, Math.floor(Math.log(dataMin) / Math.log(b) + 1e-9));
			double hi = setMax != null ? setMax : Math.pow(b, Math.ceil(Math.log(dataMax) / Math.log(b) - 1e-9));
			if (hi <= lo) hi = lo * b;
			return new AxisScale(lo, hi, b, true);
		}
		boolean zeros = dataMin == 0 && dataMax == 0;
		if (setUnit != null && setUnit > 0) {
			double[] b = bounds(dataMin, dataMax, setUnit, setMin, setMax, zeros, kind);
			return new AxisScale(b[0], b[1], setUnit, false);
		}
		double[] oneInterval = null;
		double oneUnit = 0;
		for (double u : units()) {
			double[] b = bounds(dataMin, dataMax, u, setMin, setMax, zeros, kind);
			int n = intervals(b[0], b[1], u);
			if (n < 1) continue;
			if (n <= MAX_INTERVALS && fits.fits(b[0], b[1], u, n)) return new AxisScale(b[0], b[1], u, false);
			if (n == 1 && oneInterval == null) {
				oneInterval = b;
				oneUnit = u;
			}
		}
		if (oneInterval != null) return new AxisScale(oneInterval[0], oneInterval[1], oneUnit, false);
		double[] b = bounds(dataMin, dataMax, 1, setMin, setMax, zeros, kind);
		return new AxisScale(b[0], b[1], Math.max(1e-12, b[1] - b[0]), false);
	}

	/** 1, 2 and 5 times the powers of ten, ascending */
	private static List<Double> units() {
		List<Double> l = new ArrayList<>();
		for (int e = -9; e <= 15; e++) {
			for (int m : new int[] { 1, 2, 5 }) l.add(BigDecimal.valueOf(m).scaleByPowerOfTen(e).doubleValue());
		}
		return l;
	}

	private static double down(double v, double u) {
		return Math.floor(v / u + 1e-9) * u;
	}

	private static double up(double v, double u) {
		return Math.ceil(v / u - 1e-9) * u;
	}

	private static double[] bounds(double dmin, double dmax, double u, Double setMin, Double setMax, boolean zeros, Bounds kind) {
		double lo, hi;
		if (kind == Bounds.PERCENT) {
			lo = dmin >= 0 ? 0 : Math.max(-1, down(dmin, u));
			hi = dmax <= 0 ? 0 : Math.min(1, up(dmax, u));
		} else if (zeros) {
			lo = 0;
			hi = 1;
		} else if (dmin >= 0) {
			double range = dmax - dmin;
			boolean fromZero = dmax == dmin || range > dmax / 6;
			lo = fromZero ? 0 : down(dmin - (kind == Bounds.X ? range / 20 : range / 2), u);
			// padded a twentieth of the range from the axis's minimum (R12: 1.1 to 4.8 is 0 to 6
			// by 1, not 0 to 5 by 0.5; a constant series so pads from zero)
			hi = up(dmax + (dmax - lo) / 20, u);
		} else if (dmax <= 0) {
			double[] m = bounds(-dmax, -dmin, u, null, null, false, kind);
			lo = -m[1];
			hi = -m[0];
		} else {
			double pad = (dmax - dmin) / 20;
			lo = down(dmin - pad, u);
			hi = up(dmax + pad, u);
		}
		// a bound set anchors the ticks: the other runs from it by the unit (R2: max 150 by 20 gives -10)
		if (setMin != null && setMax == null) {
			hi = setMin + Math.ceil((hi - setMin) / u - 1e-9) * u;
			lo = setMin;
		} else if (setMax != null && setMin == null) {
			lo = setMax - Math.ceil((setMax - lo) / u - 1e-9) * u;
			hi = setMax;
		} else if (setMin != null) {
			lo = setMin;
			hi = setMax;
		}
		if (hi <= lo) hi = lo + u;
		return new double[] { lo, hi };
	}
}
