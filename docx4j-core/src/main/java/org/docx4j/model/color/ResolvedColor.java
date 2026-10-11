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
 * A colour as Office draws it: red, green and blue in sRGB (0 to 255) and an alpha in OOXML's unit
 * (0 to 100000, where 100000 is opaque).  Immutable.
 *
 * @since 17.3.3 (CR-034)
 */
public final class ResolvedColor {

	/** OOXML's opaque alpha, 100% in thousandths of a percent */
	public static final int OPAQUE = 100000;

	public static final ResolvedColor BLACK = new ResolvedColor(0, 0, 0, OPAQUE);
	public static final ResolvedColor WHITE = new ResolvedColor(255, 255, 255, OPAQUE);

	private final int red;
	private final int green;
	private final int blue;
	private final int alpha;

	private ResolvedColor(int red, int green, int blue, int alpha) {
		this.red = clamp(red, 255);
		this.green = clamp(green, 255);
		this.blue = clamp(blue, 255);
		this.alpha = clamp(alpha, OPAQUE);
	}

	private static int clamp(int v, int max) {
		return v < 0 ? 0 : v > max ? max : v;
	}

	/** An opaque colour; each channel is clamped to 0 to 255 */
	public static ResolvedColor rgb(int red, int green, int blue) {
		return new ResolvedColor(red, green, blue, OPAQUE);
	}

	/**
	 * An opaque colour from six hex digits (RRGGBB, either case; a leading '#' is allowed).
	 *
	 * @return null where the string is not six hex digits ("auto", for instance)
	 */
	public static ResolvedColor ofHex(String hex) {
		if (hex == null) return null;
		String h = hex.startsWith("#") ? hex.substring(1) : hex;
		if (h.length() != 6) return null;
		try {
			int v = Integer.parseInt(h, 16);
			return rgb((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** This colour with another alpha (0 to 100000) */
	public ResolvedColor withAlpha(int alpha) {
		return new ResolvedColor(red, green, blue, alpha);
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	/** @return 0 (transparent) to 100000 (opaque) */
	public int getAlpha() {
		return alpha;
	}

	public boolean isOpaque() {
		return alpha == OPAQUE;
	}

	/** @return RRGGBB, upper case, without the alpha */
	public String toHex() {
		return String.format("%02X%02X%02X", red, green, blue);
	}

	/** Whether each channel is within {@code units} of the other colour's (alpha not compared) */
	public boolean within(ResolvedColor other, int units) {
		return other != null && Math.abs(red - other.red) <= units && Math.abs(green - other.green) <= units
				&& Math.abs(blue - other.blue) <= units;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ResolvedColor)) return false;
		ResolvedColor c = (ResolvedColor) o;
		return red == c.red && green == c.green && blue == c.blue && alpha == c.alpha;
	}

	@Override
	public int hashCode() {
		return ((red * 31 + green) * 31 + blue) * 31 + alpha;
	}

	/** RRGGBB, or RRGGBB/alpha where not opaque */
	@Override
	public String toString() {
		return isOpaque() ? toHex() : toHex() + "/" + alpha;
	}
}
