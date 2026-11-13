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
package org.docx4j.model.shapes;

import java.util.List;

import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTGradientStop;

/**
 * Word's curve for blending two colours along a gradient (CR-083 phase 4, D24).
 *
 * <p>Word blends a VML gradient (its default method, {@code sigma}) and a DrawingML gradient of exactly two stops, at
 * 0 and 100%, along an S-curve in gamma-2.2 light, not evenly in sRGB: red to blue is (238, 0, 106) a quarter of the
 * way and (187, 0, 186) in the middle, where an even blend gives (191, 0, 64) and (128, 0, 128) (cr083-phase4's
 * wv45-09, Word's 256 samples; a run's {@code w14:gradFill}, wb50-13, -14 and -16, linear and path alike). A DrawingML
 * gradient of three or more stops, or of two stops inside its frame, Word blends evenly in sRGB between its stops and
 * holds at its end colours outside (wb50-15, wg20-06 to -08): only the two-stop case at the ends takes the curve
 * (phase 5 findings section 1.2).</p>
 *
 * <p>The curve is sampled at sixteenths ({@link #weight(int)}), within 11 of 255 of each of Word's samples, so a
 * gradient drawn by it is a run of 17 stops blended evenly between them.</p>
 *
 * @since 17.3.7
 */
public final class GradientCurve {

	/** The second colour's weight at each sixteenth of the way from the first */
	private static final double[] SIGMA = { 0, .018, .0443, .0854, .1422, .2147, .2996, .3976, .5, .6024, .7004, .7853,
			.8578, .9146, .9557, .982, 1 };

	private static final double GAMMA = 2.2;

	/** The curve's steps: its samples are at 0, 1/16, ... 16/16 */
	public static final int STEPS = SIGMA.length - 1;

	private GradientCurve() {}

	/** The second colour's weight at {@code k} sixteenths of the way (0 to {@link #STEPS}) */
	public static double weight(int k) {
		return SIGMA[k];
	}

	/** Two colours (0xRRGGBB) blended in gamma-2.2 light, {@code w} of the second */
	public static int blend(int a, int b, double w) {
		int rgb = 0;
		for (int shift = 16; shift >= 0; shift -= 8) {
			double ca = ((a >> shift) & 255) / 255.0, cb = ((b >> shift) & 255) / 255.0;
			double v = Math.pow((1 - w) * Math.pow(ca, GAMMA) + w * Math.pow(cb, GAMMA), 1 / GAMMA);
			rgb |= (int) Math.round(Math.max(0, Math.min(1, v)) * 255) << shift;
		}
		return rgb;
	}

	/**
	 * The curve's 17 colours from {@code from} to {@code to}, each 0xAARRGGBB: the colours blended by
	 * {@link #blend(int, int, double)}, the alpha evenly by the same weight
	 */
	public static int[] argb(int from, int to) {
		int[] out = new int[SIGMA.length];
		int a0 = (from >>> 24) & 255, a1 = (to >>> 24) & 255;
		for (int k = 0; k < SIGMA.length; k++) {
			double w = SIGMA[k];
			int alpha = (int) Math.round(a0 + (a1 - a0) * w);
			out[k] = (alpha << 24) | blend(from & 0xFFFFFF, to & 0xFFFFFF, w);
		}
		return out;
	}

	/** Whether Word blends this gradient by the curve: exactly two stops, one at 0 and one at 100% (D24) */
	public static boolean applies(CTGradientFillProperties g) {
		if (g == null || g.getGsLst() == null) return false;
		List<CTGradientStop> gs = g.getGsLst().getGs();
		if (gs.size() != 2) return false;
		int p0 = gs.get(0).getPos(), p1 = gs.get(1).getPos();
		return Math.min(p0, p1) == 0 && Math.max(p0, p1) == 100000;
	}

	/** As {@link #applies(CTGradientFillProperties)}, for a run's {@code w14:gradFill} */
	public static boolean applies(org.docx4j.w14.CTGradientFillProperties g) {
		if (g == null || g.getGsLst() == null) return false;
		List<org.docx4j.w14.CTGradientStop> gs = g.getGsLst().getGs();
		if (gs.size() != 2) return false;
		int p0 = gs.get(0).getPos(), p1 = gs.get(1).getPos();
		return Math.min(p0, p1) == 0 && Math.max(p0, p1) == 100000;
	}
}
