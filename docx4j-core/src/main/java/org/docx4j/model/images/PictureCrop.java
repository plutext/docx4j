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
package org.docx4j.model.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The part of a picture's bitmap Word draws in the picture's frame: {@code a:srcRect}
 * on a DrawingML blip fill, or the crop attributes of a VML {@code v:imagedata}.
 *
 * <p>Each edge is a fraction of the bitmap's width (left, right) or height (top,
 * bottom), measured in from that edge. A positive value crops the edge in; a negative
 * one moves it out, so the picture sits inset in its frame with the extra area empty.
 * Word then stretches what remains to fill the frame.</p>
 *
 * @since 17.3.0 (CR-029)
 */
public final class PictureCrop {

	private static final Logger log = LoggerFactory.getLogger(PictureCrop.class);

	/** No crop: the whole bitmap. */
	public static final PictureCrop NONE = new PictureCrop(0, 0, 0, 0);

	public final double left, top, right, bottom;

	public PictureCrop(double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	/** Whether this is the whole bitmap, unchanged. */
	public boolean isNone() {
		return left == 0 && top == 0 && right == 0 && bottom == 0;
	}

	/** Whether an edge moves out, leaving empty area in the frame. */
	public boolean pads() {
		return left < 0 || top < 0 || right < 0 || bottom < 0;
	}

	/**
	 * What is cut from the bitmap: the edges which move in. Word crops the stored
	 * picture by these alone (measured: a 1377 x 1837 JPEG with r 2.5% and b 7.1%, and
	 * l and t negative, is 1342 x 1707 in Word's PDF); an edge which moves out is not
	 * in the bitmap but in where the picture sits in its frame ({@link #framePadding}).
	 */
	public PictureCrop positive() {
		if (!pads()) return this;
		return checked(Math.max(0, left), Math.max(0, top), Math.max(0, right), Math.max(0, bottom));
	}

	/**
	 * The empty area an edge which moves out leaves inside the frame, as fractions of
	 * the frame's width (left, right) or height (top, bottom): {left, top, right, bottom}.
	 * All zero where no edge moves out.
	 */
	public double[] framePadding() {
		double sx = 1 - left - right, sy = 1 - top - bottom;
		return new double[] {
				Math.max(0, -left) / sx, Math.max(0, -top) / sy,
				Math.max(0, -right) / sx, Math.max(0, -bottom) / sy };
	}

	/**
	 * From {@code a:srcRect}, whose l, t, r and b are in thousandths of a percent
	 * (so 25000 is a quarter). Null, and a rectangle which leaves nothing, give
	 * {@link #NONE}.
	 */
	public static PictureCrop of(org.docx4j.dml.CTRelativeRect srcRect) {
		if (srcRect == null) return NONE;
		return checked(srcRect.getL() / 100000d, srcRect.getT() / 100000d,
				srcRect.getR() / 100000d, srcRect.getB() / 100000d);
	}

	/**
	 * From {@code v:imagedata}'s {@code cropleft}, {@code croptop}, {@code cropright}
	 * and {@code cropbottom}. Each is a fraction ("0.25"), a fixed-point fraction in
	 * 65536ths ("16384f", which is how Word writes it), or a percentage ("25%").
	 */
	public static PictureCrop ofVml(String left, String top, String right, String bottom) {
		return checked(vmlFraction(left), vmlFraction(top), vmlFraction(right), vmlFraction(bottom));
	}

	static double vmlFraction(String s) {
		if (s == null) return 0;
		String v = s.trim();
		if (v.isEmpty()) return 0;
		try {
			if (v.endsWith("f")) return Double.parseDouble(v.substring(0, v.length() - 1)) / 65536d;
			if (v.endsWith("%")) return Double.parseDouble(v.substring(0, v.length() - 1)) / 100d;
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			log.warn("Unreadable VML crop value '" + s + "'; treated as 0");
			return 0;
		}
	}

	private static PictureCrop checked(double l, double t, double r, double b) {
		if (l == 0 && t == 0 && r == 0 && b == 0) return NONE;
		if (l + r >= 1 || t + b >= 1) {
			log.warn("A crop which leaves nothing of the picture (l " + l + ", t " + t + ", r " + r
					+ ", b " + b + "); the picture is drawn uncropped");
			return NONE;
		}
		return new PictureCrop(l, t, r, b);
	}

	/** A stable text for cache keys and file names, in {@code a:srcRect}'s units. */
	public String signature() {
		return Math.round(left * 100000) + "_" + Math.round(top * 100000) + "_"
				+ Math.round(right * 100000) + "_" + Math.round(bottom * 100000);
	}

	@Override
	public String toString() {
		return "PictureCrop[l " + left + ", t " + top + ", r " + right + ", b " + bottom + "]";
	}
}
