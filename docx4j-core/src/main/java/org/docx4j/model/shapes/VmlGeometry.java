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

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.docx4j.dml.STPathFillMode;

/**
 * A VML shape's geometry, evaluated at the shape's size: its {@code path} (ISO/IEC 29500-4 §19.1.2.14), with its
 * {@code v:formulas} (§19.1.2.4), {@code adj}, {@code coordsize} and {@code coordorigin} (§19.1.2.19), as each set of
 * sub-paths an {@code e} ends, filled or not ({@code nf}) and stroked or not ({@code ns}), and the text box rectangle
 * ({@code v:path textboxrect}). The results are in points from the shape's top left, y down, as
 * {@link ShapeGeometry}'s are, so that a VML WordArt's text paths warp its text as a DrawingML warp's do (CR-083 phase
 * 4), and so that VML shapes can be drawn from it later (CR-080 D6).
 * <p>
 * The shape's own attributes are merged over its shapetype's before this is called ({@link VmlShapes}).
 * <p>
 * What Part 4 leaves open is settled here as follows (CR-083 phase 4 findings §1.6), each to be checked against
 * Word's PDFs where a probe can show it:
 * <ul>
 * <li>Formulas: each result a 32-bit integer; {@code prod} (Word's spelling; the standard's {@code product} too) rounds
 * half up, {@code mid} toward 0, the inexact operations down; a division by zero, the square root of a negative, an
 * ellipse past its axis give 0; a forward {@code @n}, an {@code #n} past the adj list, a missing argument give 0; a
 * result past 32 bits is clamped.</li>
 * <li>Paths: a value left out (between two commas, or before a command letter) is 0; a repeated parameter list repeats
 * its command; {@code @} and the command letters delimit values ({@code m@7,l@8,}, {@code @2@14}); further pairs after
 * {@code m} are lines; repeated {@code qx}/{@code qy} pairs alternate between the two; {@code qb} with no current point
 * starts at its end point and closes; {@code ae}/{@code al} take a centre, the radii and the start and sweep angles in
 * fd (degrees &times; 2<sup>16</sup>), a sweep past a turn drawn as a turn; {@code at}, {@code ar}, {@code wa},
 * {@code wr} take a bounding box and two radial points, {@code at}/{@code ar} counterclockwise and {@code wa}/{@code wr}
 * clockwise, equal points a whole ellipse, as GDI's {@code Arc}. Angles go counterclockwise as seen on the page, as
 * GDI's do ({@link #ANGLE_SIGN}).</li>
 * <li>A {@code coordsize} side that is absent or not positive is 1,000, VML's default; {@code coordorigin} defaults to
 * 0,0.</li>
 * <li>The named values: {@code width}, {@code height}, {@code xcenter}, {@code ycenter} from the coordinate space;
 * {@code xlimo}, {@code ylimo} from {@code limo}; {@code hasfill}, {@code hasstroke} and {@code lineDrawn} 1 or 0;
 * {@code pixelWidth}, {@code pixelHeight} and {@code pixelLineWidth} at 96 pixels an inch; {@code emuWidth},
 * {@code emuHeight} and their halves from the shape's size.</li>
 * </ul>
 * Bounded against hostile input as {@link ShapeGeometry} is (CR-080 D10): 128 formulas (the standard's limit), 1 MiB of
 * path and formula text, 8,192 path commands and 256 sub-paths. A geometry past a limit, or that can't be read, is the
 * shape's box with {@link ShapeOutlines#getProblem()} saying why; a WordArt draws nothing then.
 *
 * @since 17.3.7
 */
public final class VmlGeometry {

	/** The formulas a geometry may have: Part 4 §19.1.2.4's limit. */
	public static final int MAX_FORMULAS = 128;

	/** The default {@code coordsize} side. */
	static final int DEFAULT_COORDSIZE = 1000;

	/** An fd unit: a degree is 2<sup>16</sup> of them. */
	static final double FD = 65536;

	/**
	 * The direction of VML's angles: +1 counterclockwise as seen on the page (y down), as GDI's {@code AngleArc} and
	 * {@code Arc}. Provisional until Word's arch shapetypes (types 144 to 151) are measured (CR-083 phase 4 round).
	 */
	static final int ANGLE_SIGN = 1;

	private VmlGeometry() {}

	/** What a geometry is evaluated from: the shape's attributes merged over its shapetype's. */
	public static final class Definition {
		private String path;
		private List<String> formulas = Collections.emptyList();
		private Integer[] adj = new Integer[0];
		private String coordsize, coordorigin, textboxrect, limo;
		private boolean filled = true, stroked = true;
		private double strokeWeightPt = 0.75;

		public Definition path(String path) {
			this.path = path;
			return this;
		}

		/** Each {@code v:f}'s {@code eqn}, in order. */
		public Definition formulas(List<String> formulas) {
			this.formulas = formulas == null ? Collections.<String>emptyList() : formulas;
			return this;
		}

		/** The adjust values, null where none is given (as {@link #adj(String)} reads them). */
		public Definition adj(Integer[] adj) {
			this.adj = adj == null ? new Integer[0] : adj;
			return this;
		}

		public Definition coordsize(String coordsize) {
			this.coordsize = coordsize;
			return this;
		}

		public Definition coordorigin(String coordorigin) {
			this.coordorigin = coordorigin;
			return this;
		}

		public Definition textboxrect(String textboxrect) {
			this.textboxrect = textboxrect;
			return this;
		}

		public Definition limo(String limo) {
			this.limo = limo;
			return this;
		}

		/** Whether the shape is filled and stroked, and its stroke's weight: {@code hasfill}, {@code hasstroke}, {@code pixelLineWidth}. */
		public Definition paint(boolean filled, boolean stroked, double strokeWeightPt) {
			this.filled = filled;
			this.stroked = stroked;
			this.strokeWeightPt = strokeWeightPt;
			return this;
		}

		public String getPath() {
			return path;
		}
	}

	/** A geometry past a limit. */
	static final class OverLimit extends RuntimeException {
		private static final long serialVersionUID = 1L;

		OverLimit(String what) {
			super(what, null, false, false);
		}
	}

	/** A geometry that can't be read. */
	static final class Bad extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Bad(String what) {
			super(what, null, false, false);
		}
	}

	// ------------------------------------------------------------------ adj

	/**
	 * An {@code adj} attribute's values, null for each left out ({@code ",10800"}: the first left out), or null for no
	 * attribute. Values past 8 are ignored (Part 4: {@code #0} to {@code #7}).
	 */
	public static Integer[] adj(String adj) {
		if (adj == null) return null;
		String[] parts = adj.split(",", -1);
		Integer[] out = new Integer[Math.min(8, parts.length)];
		for (int i = 0; i < out.length; i++) {
			String s = parts[i].trim();
			if (s.isEmpty()) continue;
			try {
				out[i] = clamp(Long.parseLong(s));
			} catch (NumberFormatException e) {
				out[i] = null; // unreadable: as if left out
			}
		}
		return out;
	}

	/** A shape's adjust values over its shapetype's: each the shape's where it gives one, else the shapetype's. */
	public static Integer[] adj(Integer[] own, Integer[] type) {
		if (own == null) return type == null ? new Integer[0] : type;
		if (type == null) return own;
		Integer[] out = new Integer[Math.max(own.length, type.length)];
		for (int i = 0; i < out.length; i++) {
			Integer o = i < own.length ? own[i] : null;
			out[i] = o != null ? o : i < type.length ? type[i] : null;
		}
		return out;
	}

	// ------------------------------------------------------------------ evaluation

	/**
	 * The geometry at the shape's size, in points.
	 *
	 * @return each {@code e}-delimited set of sub-paths an outline ({@link ShapeOutline#getFill()} {@code NONE} after
	 *         {@code nf}, {@link ShapeOutline#isStroked()} false after {@code ns}), and the text box rectangle; no outline
	 *         if there is no path; the box with a problem if the geometry can't be evaluated
	 */
	public static ShapeOutlines outlines(Definition d, double widthPt, double heightPt) {
		double w = Math.max(0, widthPt), h = Math.max(0, heightPt);
		try {
			Space space = new Space(d, w, h);
			long[] f = formulas(d.formulas, d.adj, space);
			List<ShapeOutline> sets = d.path == null || d.path.trim().isEmpty()
					? Collections.<ShapeOutline>emptyList() : paths(d.path, f, d.adj, space);
			Rectangle2D.Double text = textRectangle(d.textboxrect, f, d.adj, space);
			return new ShapeOutlines(sets, text, Collections.<ShapeOutlines.ConnectionSite>emptyList());
		} catch (OverLimit e) {
			return ShapeOutlines.box(w, h, ShapeOutlines.OVER_LIMIT, "a VML geometry: " + e.getMessage());
		} catch (Bad e) {
			return ShapeOutlines.box(w, h, ShapeOutlines.BAD_GEOMETRY, "a VML geometry: " + e.getMessage());
		} catch (RuntimeException e) {
			return ShapeOutlines.box(w, h, ShapeOutlines.BAD_GEOMETRY, "a VML geometry: " + e);
		}
	}

	/** The coordinate space and the named values. */
	static final class Space {
		final double ox, oy, cw, ch, w, h;
		final long limoX, limoY;
		final boolean filled, stroked;
		final double strokeWeightPt;

		Space(Definition d, double w, double h) {
			long[] size = pair(d.coordsize);
			cw = size == null || size[0] <= 0 ? DEFAULT_COORDSIZE : size[0];
			ch = size == null || size[1] <= 0 ? DEFAULT_COORDSIZE : size[1];
			long[] origin = pair(d.coordorigin);
			ox = origin == null ? 0 : origin[0];
			oy = origin == null ? 0 : origin[1];
			long[] l = pair(d.limo);
			limoX = l == null ? 0 : l[0];
			limoY = l == null ? 0 : l[1];
			this.w = w;
			this.h = h;
			filled = d.filled;
			stroked = d.stroked;
			strokeWeightPt = d.strokeWeightPt;
		}

		/** The transform from the coordinate space to points. */
		AffineTransform toPoints() {
			AffineTransform at = AffineTransform.getScaleInstance(w / cw, h / ch);
			at.translate(-ox, -oy);
			return at;
		}

		long named(String name) {
			switch (name.toLowerCase(Locale.ROOT)) {
			case "width": return (long) cw;
			case "height": return (long) ch;
			case "xcenter": return (long) Math.floor(ox + cw / 2);
			case "ycenter": return (long) Math.floor(oy + ch / 2);
			case "xlimo": return limoX;
			case "ylimo": return limoY;
			case "hasfill": return filled ? 1 : 0;
			case "hasstroke":
			case "linedrawn": return stroked ? 1 : 0;
			case "pixellinewidth": return stroked ? Math.round(strokeWeightPt * 96 / 72) : 0;
			case "pixelwidth": return Math.round(w * 96 / 72);
			case "pixelheight": return Math.round(h * 96 / 72);
			case "emuwidth": return Math.round(w * 12700);
			case "emuheight": return Math.round(h * 12700);
			case "emuwidth2": return Math.round(w * 12700 / 2);
			case "emuheight2": return Math.round(h * 12700 / 2);
			default: throw new Bad("an unknown value " + name);
			}
		}
	}

	/** Two numbers, "a,b", or null. */
	private static long[] pair(String s) {
		if (s == null || s.trim().isEmpty()) return null;
		String[] p = s.split(",", -1);
		long[] out = new long[2];
		for (int i = 0; i < 2; i++) {
			String v = i < p.length ? p[i].trim() : "";
			if (v.isEmpty()) continue;
			try {
				out[i] = Long.parseLong(v);
			} catch (NumberFormatException e) {
				try {
					out[i] = (long) Double.parseDouble(v);
				} catch (NumberFormatException e2) {
					return null;
				}
			}
		}
		return out;
	}

	/** Each formula's value, in order. */
	static long[] formulas(List<String> eqns, Integer[] adj, Space space) {
		if (eqns.size() > MAX_FORMULAS) throw new OverLimit(eqns.size() + " formulas");
		long chars = 0;
		long[] out = new long[eqns.size()];
		for (int i = 0; i < out.length; i++) {
			String eqn = eqns.get(i);
			if (eqn == null) continue;
			chars += eqn.length();
			if (chars > GeometryReader.MAX_TEXT) throw new OverLimit("formulas of " + chars + " characters");
			String[] t = eqn.trim().split("[\\s,]+");
			if (t.length == 0 || t[0].isEmpty()) continue;
			long[] a = new long[3];
			for (int k = 0; k < 3 && k + 1 < t.length; k++) a[k] = operand(t[k + 1], out, i, adj, space);
			out[i] = clamp(operate(t[0], a[0], a[1], a[2]));
		}
		return out;
	}

	/** A formula's argument: a number, {@code @n}, {@code #n} or a named value. */
	private static long operand(String s, long[] done, int index, Integer[] adj, Space space) {
		char c = s.charAt(0);
		if (c == '@' || c == '#') {
			int n = index(s);
			if (c == '@') return n >= 0 && n < index && n < done.length ? done[n] : 0;
			return n >= 0 && n < adj.length && adj[n] != null ? adj[n] : 0;
		}
		if (c == '-' || c == '+' || Character.isDigit(c)) {
			try {
				return clamp(Long.parseLong(s));
			} catch (NumberFormatException e) {
				throw new Bad("a value " + s);
			}
		}
		return space.named(s);
	}

	private static int index(String ref) {
		try {
			return Integer.parseInt(ref.substring(1));
		} catch (NumberFormatException e) {
			throw new Bad("a reference " + ref);
		}
	}

	/** One operation, as Part 4 §19.1.2.4 defines it, rounded as it says. */
	static long operate(String op, long v, long p1, long p2) {
		switch (op.toLowerCase(Locale.ROOT)) {
		case "val": return v;
		case "sum": return v + p1 - p2;
		case "prod":
		case "product": return product(v, p1, p2);
		case "mid": return (v + p1) / 2; // toward 0
		case "abs": return Math.abs(v);
		case "min": return Math.min(v, p1);
		case "max": return Math.max(v, p1);
		case "if": return v > 0 ? p1 : p2;
		case "mod": return down(Math.sqrt((double) v * v + (double) p1 * p1 + (double) p2 * p2));
		case "atan2": return down(Math.toDegrees(Math.atan2(p1, v)) * FD);
		case "sin": return down(v * Math.sin(radians(p1)));
		case "cos": return down(v * Math.cos(radians(p1)));
		case "tan": return down(v * Math.tan(radians(p1)));
		case "cosatan2": return down(v * Math.cos(Math.atan2(p2, p1)));
		case "sinatan2": return down(v * Math.sin(Math.atan2(p2, p1)));
		case "sqrt": return v <= 0 ? 0 : down(Math.sqrt(v));
		case "sumangle": return v + p1 * (long) FD - p2 * (long) FD;
		case "ellipse": {
			if (p1 == 0) return 0;
			double r = 1 - ((double) v / p1) * ((double) v / p1);
			return r <= 0 ? 0 : down(p2 * Math.sqrt(r));
		}
		default: throw new Bad("an unknown operation " + op);
		}
	}

	/** v &times; p1 / p2 to the nearest integer, a half up; 0 for a division by zero. */
	private static long product(long v, long p1, long p2) {
		if (p2 == 0) return 0;
		java.math.BigInteger num = java.math.BigInteger.valueOf(v).multiply(java.math.BigInteger.valueOf(p1));
		java.math.BigInteger den = java.math.BigInteger.valueOf(p2);
		if (den.signum() < 0) {
			num = num.negate();
			den = den.negate();
		}
		// floor(num / den + 1/2) = floor((2 num + den) / 2 den)
		java.math.BigInteger[] qr = num.shiftLeft(1).add(den).divideAndRemainder(den.shiftLeft(1));
		java.math.BigInteger q = qr[0];
		if (qr[1].signum() < 0) q = q.subtract(java.math.BigInteger.ONE);
		return q.max(java.math.BigInteger.valueOf(Integer.MIN_VALUE)).min(java.math.BigInteger.valueOf(Integer.MAX_VALUE)).longValue();
	}

	private static double radians(long fd) {
		return Math.toRadians(fd / FD);
	}

	/** Down, toward -infinity, as the inexact operations round; a near-integer read as that integer. */
	private static long down(double x) {
		if (Double.isNaN(x)) return 0;
		return clamp((long) Math.floor(x + 1e-6));
	}

	private static int clamp(long v) {
		return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, v));
	}

	// ------------------------------------------------------------------ path

	/** A command and its values, each a number or a reference, resolved. */
	static final class Command {
		final String name;
		final long[] values;

		Command(String name, long[] values) {
			this.name = name;
			this.values = values;
		}
	}

	private static final String[] TWO_LETTER = { "nf", "ns", "ae", "al", "at", "ar", "wa", "wr", "qx", "qy", "qb" };

	/** The path's commands, their values resolved. */
	static List<Command> commands(String path, long[] f, Integer[] adj, Space space) {
		if (path.length() > GeometryReader.MAX_TEXT) throw new OverLimit("a path of " + path.length() + " characters");
		List<Command> out = new ArrayList<Command>();
		int i = 0, n = path.length();
		while (i < n) {
			char c = Character.toLowerCase(path.charAt(i));
			if (Character.isWhitespace(c) || c == ',') {
				if (c == ',' && out.isEmpty()) throw new Bad("a value before any command");
				i++;
				continue;
			}
			if (!Character.isLetter(c)) throw new Bad("a value before any command, at " + i);
			String name = null;
			if (i + 1 < n) {
				String two = path.substring(i, i + 2).toLowerCase(Locale.ROOT);
				for (String t : TWO_LETTER) if (t.equals(two)) name = two;
			}
			if (name == null) {
				if ("mlcxetrv".indexOf(c) < 0) throw new Bad("an unknown command at " + i + ": " + c);
				name = String.valueOf(c);
			}
			i += name.length();
			int end = i;
			while (end < n && !Character.isLetter(path.charAt(end))) end++;
			out.add(new Command(name, values(path.substring(i, end), f, adj, space)));
			if (out.size() > GeometryReader.MAX_COMMANDS) throw new OverLimit("more than " + GeometryReader.MAX_COMMANDS + " commands");
			i = end;
		}
		return out;
	}

	/**
	 * A command's values: comma-separated fields, each holding one or more values separated by spaces, or none (a value
	 * left out: 0); {@code @} and {@code #} start a value, as a sign after a digit does.
	 */
	private static long[] values(String s, long[] f, Integer[] adj, Space space) {
		if (s.trim().isEmpty() && s.indexOf(',') < 0) return new long[0];
		String[] fields = s.split(",", -1);
		List<Long> out = new ArrayList<Long>();
		for (String field : fields) {
			List<String> tokens = tokens(field);
			if (tokens.isEmpty()) {
				out.add(0L);
				continue;
			}
			for (String t : tokens) {
				char c = t.charAt(0);
				if (c == '@') {
					int k = index(t);
					out.add(k >= 0 && k < f.length ? f[k] : 0L);
				} else if (c == '#') {
					int k = index(t);
					out.add(k >= 0 && k < adj.length && adj[k] != null ? (long) adj[k] : 0L);
				} else {
					try {
						out.add((long) clamp(Long.parseLong(t)));
					} catch (NumberFormatException e) {
						throw new Bad("a value " + t);
					}
				}
			}
			if (out.size() > 8 * GeometryReader.MAX_COMMANDS) throw new OverLimit("too many values");
		}
		long[] v = new long[out.size()];
		for (int k = 0; k < v.length; k++) v[k] = out.get(k);
		return v;
	}

	private static List<String> tokens(String field) {
		List<String> out = new ArrayList<String>();
		int i = 0, n = field.length();
		while (i < n) {
			char c = field.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			int start = i;
			if (c == '@' || c == '#' || c == '-' || c == '+') i++;
			while (i < n && Character.isDigit(field.charAt(i))) i++;
			if (i == start || (i == start + 1 && !Character.isDigit(c))) throw new Bad("a value in \"" + field + "\"");
			out.add(field.substring(start, i));
		}
		return out;
	}

	/** The command's parameter count, repeated where Part 4 allows it. */
	private static int arity(String name) {
		switch (name) {
		case "m": case "l": case "t": case "r": case "qx": case "qy": case "qb": return 2;
		case "c": case "v": case "ae": case "al": return 6;
		case "at": case "ar": case "wa": case "wr": return 8;
		default: return 0;
		}
	}

	/** The sets of sub-paths, in points. */
	static List<ShapeOutline> paths(String path, long[] f, Integer[] adj, Space space) {
		List<Command> commands = commands(path, f, adj, space);
		List<ShapeOutline> sets = new ArrayList<ShapeOutline>();
		AffineTransform toPoints = space.toPoints();
		Builder b = new Builder();
		for (Command cmd : commands) {
			String name = cmd.name;
			long[] v = cmd.values;
			int k = arity(name);
			if (k == 0) {
				switch (name) {
				case "x": b.close(); break;
				case "e": b.end(sets, toPoints); break;
				case "nf": b.filled = false; break;
				case "ns": b.stroked = false; break;
				default: break;
				}
				continue;
			}
			if (name.equals("qb")) {
				b.quadratic(pad(v, 2));
				continue;
			}
			v = pad(v, k);
			for (int g = 0; g < v.length; g += k) {
				double[] p = new double[k];
				for (int j = 0; j < k; j++) p[j] = v[g + j];
				boolean first = g == 0;
				switch (name) {
				case "m": if (first) b.moveTo(p[0], p[1]); else b.lineTo(p[0], p[1]); break;
				case "l": b.lineTo(p[0], p[1]); break;
				case "c": b.curveTo(p[0], p[1], p[2], p[3], p[4], p[5]); break;
				case "t": b.moveTo(b.x + p[0], b.y + p[1]); break;
				case "r": b.lineTo(b.x + p[0], b.y + p[1]); break;
				case "v": b.curveTo(b.x + p[0], b.y + p[1], b.x + p[2], b.y + p[3], b.x + p[4], b.y + p[5]); break;
				case "ae": b.angleArc(p, false); break;
				case "al": b.angleArc(p, true); break;
				case "at": b.boxArc(p, false, false); break;
				case "ar": b.boxArc(p, true, false); break;
				case "wa": b.boxArc(p, false, true); break;
				case "wr": b.boxArc(p, true, true); break;
				case "qx": b.quadrant(p[0], p[1], (g / 2) % 2 == 0); break;
				case "qy": b.quadrant(p[0], p[1], (g / 2) % 2 == 1); break;
				default: break;
				}
				if (b.subPaths > GeometryReader.MAX_PATHS) throw new OverLimit("more than " + GeometryReader.MAX_PATHS + " sub-paths");
			}
		}
		b.end(sets, toPoints);
		return sets;
	}

	/** The values made up to a whole number of parameter lists, with 0. */
	private static long[] pad(long[] v, int k) {
		if (v.length == 0) return new long[k];
		if (v.length % k == 0) return v;
		long[] out = new long[(v.length / k + 1) * k];
		System.arraycopy(v, 0, out, 0, v.length);
		return out;
	}

	/** A set of sub-paths being built, in the coordinate space. */
	private static final class Builder {
		Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		boolean filled = true, stroked = true, open;
		double x, y, startX, startY;
		int subPaths;

		void moveTo(double px, double py) {
			path.moveTo(px, py);
			open = true;
			subPaths++;
			x = startX = px;
			y = startY = py;
		}

		void lineTo(double px, double py) {
			if (!open) moveTo(x, y);
			path.lineTo(px, py);
			x = px;
			y = py;
		}

		void curveTo(double x1, double y1, double x2, double y2, double px, double py) {
			if (!open) moveTo(x, y);
			path.curveTo(x1, y1, x2, y2, px, py);
			x = px;
			y = py;
		}

		void close() {
			if (!open) return;
			path.closePath();
			x = startX;
			y = startY;
		}

		void end(List<ShapeOutline> sets, AffineTransform toPoints) {
			if (path.getCurrentPoint() != null) {
				Path2D.Double p = new Path2D.Double(path, toPoints);
				sets.add(new ShapeOutline(p, filled ? STPathFillMode.NORM : STPathFillMode.NONE, stroked, true));
			}
			path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			filled = true;
			stroked = true;
			open = false;
		}

		/** A quarter ellipse to (px, py), starting out horizontal or vertical. */
		void quadrant(double px, double py, boolean horizontal) {
			if (!open) moveTo(x, y);
			double k = 0.5522847498307936;
			double x0 = x, y0 = y;
			if (horizontal) curveTo(x0 + k * (px - x0), y0, px, py - k * (py - y0), px, py);
			else curveTo(x0, y0 + k * (py - y0), px - k * (px - x0), py, px, py);
		}

		/** Quadratic curves through control points, OpenType's way, to the last point; from it, closed, if none is current. */
		void quadratic(long[] v) {
			int n = v.length / 2;
			double ex = v[2 * n - 2], ey = v[2 * n - 1];
			boolean closeAfter = !open;
			if (!open) moveTo(ex, ey);
			if (n == 1) {
				lineTo(ex, ey);
			} else {
				for (int i = 0; i < n - 1; i++) {
					double cx = v[2 * i], cy = v[2 * i + 1];
					double nx, ny;
					if (i == n - 2) {
						nx = ex;
						ny = ey;
					} else {
						nx = (cx + v[2 * i + 2]) / 2;
						ny = (cy + v[2 * i + 3]) / 2;
					}
					path.quadTo(cx, cy, nx, ny);
					x = nx;
					y = ny;
				}
			}
			if (closeAfter) close();
		}

		/** {@code ae}/{@code al}: centre, radii, start and sweep angles in fd. */
		void angleArc(double[] p, boolean move) {
			double start = Math.toRadians(p[4] / FD), sweep = Math.toRadians(p[5] / FD);
			arc(p[0], p[1], p[2], p[3], start, clampSweep(sweep), move);
		}

		/** {@code at}, {@code ar}, {@code wa}, {@code wr}: a bounding box and two radial points. */
		void boxArc(double[] p, boolean move, boolean clockwise) {
			double cx = (p[0] + p[2]) / 2, cy = (p[1] + p[3]) / 2;
			double rx = Math.abs(p[2] - p[0]) / 2, ry = Math.abs(p[3] - p[1]) / 2;
			double a0 = angle(p[4] - cx, p[5] - cy, rx, ry), a1 = angle(p[6] - cx, p[7] - cy, rx, ry);
			double sweep;
			if (p[4] == p[6] && p[5] == p[7]) {
				sweep = 2 * Math.PI;
			} else {
				sweep = a1 - a0;
				while (sweep <= 0) sweep += 2 * Math.PI;
				while (sweep > 2 * Math.PI) sweep -= 2 * Math.PI;
			}
			arc(cx, cy, rx, ry, a0, clockwise ? sweep - 2 * Math.PI == 0 ? -2 * Math.PI : sweep - 2 * Math.PI : sweep, move);
		}

		/** The parametric angle of a point seen from the centre, counterclockwise on the page. */
		private static double angle(double dx, double dy, double rx, double ry) {
			return Math.atan2(-ANGLE_SIGN * dy / (ry == 0 ? 1 : ry), dx / (rx == 0 ? 1 : rx));
		}

		private static double clampSweep(double s) {
			if (Double.isNaN(s)) return 0;
			return Math.max(-2 * Math.PI, Math.min(2 * Math.PI, s));
		}

		/** An elliptical arc, as cubic curves of at most a quarter turn; a line to its start, or a move. */
		private void arc(double cx, double cy, double rx, double ry, double start, double sweep, boolean move) {
			double sx = cx + rx * Math.cos(start), sy = cy - ANGLE_SIGN * ry * Math.sin(start);
			if (move || !open) moveTo(sx, sy);
			else lineTo(sx, sy);
			if (sweep == 0) return;
			int segments = (int) Math.ceil(Math.abs(sweep) / (Math.PI / 2) - 1e-9);
			double d = sweep / segments;
			double k = 4.0 / 3 * Math.tan(d / 4);
			double t0 = start;
			for (int i = 0; i < segments; i++) {
				double t1 = t0 + d;
				double x0 = cx + rx * Math.cos(t0), y0 = cy - ANGLE_SIGN * ry * Math.sin(t0);
				double x1 = cx + rx * Math.cos(t1), y1 = cy - ANGLE_SIGN * ry * Math.sin(t1);
				// the derivatives, d/dt of (cx + rx cos t, cy - s ry sin t)
				double dx0 = -rx * Math.sin(t0), dy0 = -ANGLE_SIGN * ry * Math.cos(t0);
				double dx1 = -rx * Math.sin(t1), dy1 = -ANGLE_SIGN * ry * Math.cos(t1);
				curveTo(x0 + k * dx0, y0 + k * dy0, x1 - k * dx1, y1 - k * dy1, x1, y1);
				t0 = t1;
			}
		}
	}

	// ------------------------------------------------------------------ text box

	/** The first of {@code textboxrect}'s rectangles, in points; the whole box if there is none. */
	static Rectangle2D.Double textRectangle(String textboxrect, long[] f, Integer[] adj, Space space) {
		Rectangle2D.Double whole = new Rectangle2D.Double(0, 0, space.w, space.h);
		if (textboxrect == null || textboxrect.trim().isEmpty()) return whole;
		String first = textboxrect.split(";")[0];
		long[] v = values(first, f, adj, space);
		if (v.length < 4) return whole;
		AffineTransform at = space.toPoints();
		double[] pts = { v[0], v[1], v[2], v[3] };
		at.transform(pts, 0, pts, 0, 2);
		return new Rectangle2D.Double(Math.min(pts[0], pts[2]), Math.min(pts[1], pts[3]), Math.abs(pts[2] - pts[0]),
				Math.abs(pts[3] - pts[1]));
	}

	// ------------------------------------------------------------------ sub-paths

	/** A path's sub-paths, each from its moveto, those with no segment left out: a VML text path's lines. */
	public static List<Path2D.Double> subPaths(Path2D path) {
		List<Path2D.Double> out = new ArrayList<Path2D.Double>();
		Path2D.Double current = null;
		boolean drawn = false;
		double[] c = new double[6];
		for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
			int type = it.currentSegment(c);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				if (current != null && drawn) out.add(current);
				current = new Path2D.Double();
				current.moveTo(c[0], c[1]);
				drawn = false;
				break;
			case PathIterator.SEG_LINETO:
				current.lineTo(c[0], c[1]);
				drawn = true;
				break;
			case PathIterator.SEG_QUADTO:
				current.quadTo(c[0], c[1], c[2], c[3]);
				drawn = true;
				break;
			case PathIterator.SEG_CUBICTO:
				current.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
				drawn = true;
				break;
			case PathIterator.SEG_CLOSE:
				current.closePath();
				break;
			default:
				break;
			}
		}
		if (current != null && drawn) out.add(current);
		return out;
	}
}
