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
package org.docx4j.model.color;

/**
 * The colour arithmetic Office uses, as CR-034's probes measured it (CR-034 §11): sRGB's transfer
 * function, HSL with an unclamped saturation, and Windows' integer HLS for SpreadsheetML tints.
 *
 * @since 17.3.3 (CR-034)
 */
final class ColorMath {

	private ColorMath() {
	}

	// ------------------------------------------------------------------ sRGB and linear RGB

	/** sRGB (0 to 1) to linear RGB (IEC 61966-2-1) */
	static double toLinear(double c) {
		return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

	/** linear RGB to sRGB, clamped to 0 to 1 */
	static double toSrgb(double l) {
		double c = clamp01(l);
		return c <= 0.0031308 ? c * 12.92 : 1.055 * Math.pow(c, 1 / 2.4) - 0.055;
	}

	static double clamp01(double v) {
		return v < 0 ? 0 : v > 1 ? 1 : v;
	}

	/** a channel, 0 to 1, as 0 to 255, rounded half up */
	static int channel(double v) {
		return (int) Math.floor(clamp01(v) * 255 + 0.5);
	}

	// ------------------------------------------------------------------ HSL

	/** {h, s, l}, each 0 to 1, from sRGB channels 0 to 1; a grey's hue is 0 */
	static double[] rgbToHsl(double r, double g, double b) {
		double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
		double l = (max + min) / 2;
		if (max == min) return new double[] { 0, 0, l };
		double d = max - min;
		double s = l <= 0.5 ? d / (max + min) : d / (2 - max - min);
		double h;
		if (max == r) {
			h = ((g - b) / d) % 6;
			if (h < 0) h += 6;
		} else if (max == g) {
			h = (b - r) / d + 2;
		} else {
			h = (r - g) / d + 4;
		}
		return new double[] { h / 6, s, l };
	}

	/**
	 * sRGB channels (0 to 1, clamped) from HSL; the saturation is not clamped, as Office does not
	 * clamp it (a saturation above 1 pushes a channel past its range, which is then clamped).
	 */
	static double[] hslToRgb(double h, double s, double l) {
		return new double[] { hslChannel(0, h, s, l), hslChannel(8, h, s, l), hslChannel(4, h, s, l) };
	}

	private static double hslChannel(int n, double h, double s, double l) {
		double k = (n + h * 12) % 12;
		if (k < 0) k += 12;
		double a = s * Math.min(l, 1 - l);
		return clamp01(l - a * Math.max(-1, Math.min(Math.min(k - 3, 9 - k), 1)));
	}

	// ------------------------------------------------------------------ Windows' integer HLS (Excel)

	static final int HLSMAX = 240;
	private static final int RGBMAX = 255;

	/** {h, l, s} on 0 to 240, as Windows' ColorRGBToHLS (KB29240) computes them */
	static int[] rgbToHls240(int r, int g, int b) {
		int max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
		int l = ((max + min) * HLSMAX + RGBMAX) / (2 * RGBMAX);
		if (max == min) return new int[] { HLSMAX * 2 / 3, l, 0 };
		int s = l <= HLSMAX / 2
				? ((max - min) * HLSMAX + (max + min) / 2) / (max + min)
				: ((max - min) * HLSMAX + (2 * RGBMAX - max - min) / 2) / (2 * RGBMAX - max - min);
		int d = max - min;
		int rd = ((max - r) * (HLSMAX / 6) + d / 2) / d;
		int gd = ((max - g) * (HLSMAX / 6) + d / 2) / d;
		int bd = ((max - b) * (HLSMAX / 6) + d / 2) / d;
		int h;
		if (r == max) h = bd - gd;
		else if (g == max) h = HLSMAX / 3 + rd - bd;
		else h = 2 * HLSMAX / 3 + gd - rd;
		if (h < 0) h += HLSMAX;
		if (h > HLSMAX) h -= HLSMAX;
		return new int[] { h, l, s };
	}

	/**
	 * RGB from {@link #rgbToHls240}'s values, as Windows' ColorHLSToRGB, except that a grey is
	 * rounded rather than truncated, which is what Excel's fills show (CR-034 §11, P4).
	 */
	static int[] hls240ToRgb(int h, int l, int s) {
		if (s == 0) {
			int v = (l * RGBMAX + HLSMAX / 2) / HLSMAX;
			return new int[] { v, v, v };
		}
		int m2 = l <= HLSMAX / 2 ? (l * (HLSMAX + s) + HLSMAX / 2) / HLSMAX : l + s - (l * s + HLSMAX / 2) / HLSMAX;
		int m1 = 2 * l - m2;
		return new int[] {
				(hueToRgb(m1, m2, h + HLSMAX / 3) * RGBMAX + HLSMAX / 2) / HLSMAX,
				(hueToRgb(m1, m2, h) * RGBMAX + HLSMAX / 2) / HLSMAX,
				(hueToRgb(m1, m2, h - HLSMAX / 3) * RGBMAX + HLSMAX / 2) / HLSMAX };
	}

	private static int hueToRgb(int n1, int n2, int hue) {
		if (hue < 0) hue += HLSMAX;
		if (hue > HLSMAX) hue -= HLSMAX;
		if (hue < HLSMAX / 6) return n1 + ((n2 - n1) * hue + HLSMAX / 12) / (HLSMAX / 6);
		if (hue < HLSMAX / 2) return n2;
		if (hue < HLSMAX * 2 / 3) return n1 + ((n2 - n1) * (HLSMAX * 2 / 3 - hue) + HLSMAX / 12) / (HLSMAX / 6);
		return n1;
	}
}
