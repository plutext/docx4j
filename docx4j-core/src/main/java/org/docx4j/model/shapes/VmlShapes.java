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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.vml.CTF;
import org.docx4j.vml.CTFormulas;
import org.docx4j.vml.CTGroup;
import org.docx4j.vml.CTPath;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTTextPath;
import org.jvnet.jaxb.lang.Child;

import jakarta.xml.bind.JAXBElement;

/**
 * A VML shape read with its shapetype (CR-083 phase 4): the shapetype found by the shape's {@code type} among those in
 * its part, and each attribute the shape doesn't give taken from the shapetype, one attribute at a time, as Word reads
 * its own watermark (whose text path has {@code on} and {@code fitshape} on the shapetype and {@code string} and
 * {@code style} on the shape: phase 4 findings §1.3). The placement properties of {@code style} are the shape's own
 * (Part 4 §19.1.2.23 lists those a shape doesn't take from its shapetype), so {@link Resolved#getStyle()} is the
 * shape's.
 * <p>
 * Word writes a shapetype once in a part, in the first {@code w:pict} that uses it, later ones carrying only the
 * {@code type}; a group holds its own. So a part's shapetypes are indexed whole, groups included ({@link Index}). A
 * {@code type} that resolves to nothing leaves the shape with no shapetype, which draws nothing where the shapetype held
 * the path, as in Word (phase 0's wv20-41). Word reads a part in order: a shape's shapetype is the last of its id
 * before it, and one only after it doesn't count (cr083-phase4b's wv49-05 and -06; two before it, the second:
 * cr083-phase4's wv43-07).
 *
 * @since 17.3.7
 */
public final class VmlShapes {

	private VmlShapes() {}

	/**
	 * A part's shapetypes by id and its shapes, each numbered in the part's order, and the ids looked for and not found
	 * since it was built.
	 */
	public static final class Index {
		private final Map<String, List<Object[]>> byId = new HashMap<String, List<Object[]>>();
		private final Map<CTShape, Integer> shapes = new java.util.IdentityHashMap<CTShape, Integer>();
		private final Map<String, Integer> shapesByKey = new HashMap<String, Integer>();
		private final java.util.Set<String> misses = new java.util.HashSet<String>();
		private int order;

		private Index() {}

		/** The shapetypes in a part's content (its JAXB root: a {@code w:document}, {@code w:hdr}, {@code w:ftr}, ...). */
		public static Index of(Object root) {
			Index index = new Index();
			new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
				@Override
				public List<Object> apply(Object o) {
					Object u = XmlUtils.unwrap(o);
					if (u instanceof CTShapetype) index.add((CTShapetype) u);
					else if (u instanceof CTShape) index.shape((CTShape) u);
					else if (u instanceof CTGroup) index.group((CTGroup) u, 0);
					return null;
				}
			});
			return index;
		}

		private void add(CTShapetype t) {
			int at = order++;
			String id = t.getVmlId();
			if (id != null) byId.computeIfAbsent(id, k -> new ArrayList<Object[]>()).add(new Object[] { at, t });
		}

		private void shape(CTShape s) {
			if (shapes.containsKey(s)) return;
			int at = order++;
			shapes.put(s, at);
			shapesByKey.putIfAbsent(key(s), at);
		}

		/** A group's shapetypes and shapes, and its groups' (TraversalUtil doesn't enter a group). */
		private void group(CTGroup g, int depth) {
			if (depth > 64) return;
			for (JAXBElement<?> j : g.getPathOrFormulasOrHandles()) {
				Object u = j.getValue();
				if (u instanceof CTShapetype) add((CTShapetype) u);
				else if (u instanceof CTShape) shape((CTShape) u);
				else if (u instanceof CTGroup) group((CTGroup) u, depth + 1);
			}
		}

		/** A shape copied out of the part (the XSLT's routes unmarshal their node) is found by these. */
		private static String key(CTShape s) {
			return s.getVmlId() + '\u0000' + s.getSpid() + '\u0000' + s.getType() + '\u0000' + s.getStyle();
		}

		/** The last shapetype a {@code type} names ({@code #_x0000_t136}) in the part, or null. */
		public CTShapetype get(String type) {
			return get(type, null);
		}

		/**
		 * The shapetype a shape's {@code type} names: the last of its id before the shape in the part, as Word reads it
		 * (one only after it doesn't count: cr083-phase4b's wv49-05, -06); for a shape not in the part, the last.
		 */
		public CTShapetype get(String type, CTShape shape) {
			if (type == null) return null;
			String id = type.startsWith("#") ? type.substring(1) : type;
			List<Object[]> all = byId.get(id);
			if (all == null) return null;
			Integer at = shape == null ? null : shapes.get(shape);
			if (at == null && shape != null) at = shapesByKey.get(key(shape));
			if (at == null) return (CTShapetype) all.get(all.size() - 1)[1];
			CTShapetype found = null;
			for (Object[] t : all) {
				if ((Integer) t[0] > at) break;
				found = (CTShapetype) t[1];
			}
			return found;
		}
	}

	/** The topmost object above the shape by its parents: its part's root, where its parents are set. */
	public static Object root(Object o) {
		Object at = o;
		for (int i = 0; i < 10000 && at instanceof Child; i++) {
			Object up = ((Child) at).getParent();
			if (up == null) break;
			at = up;
		}
		return at;
	}

	/**
	 * The shape with its shapetype, the part's shapetypes indexed afresh: for a single shape. A conversion passes its
	 * own cache ({@link #resolve(CTShape, Object, Map)}).
	 *
	 * @param root the part's content (its JAXB root), or null to find it from the shape's parents
	 */
	public static Resolved resolve(CTShape shape, Object root) {
		return resolve(shape, root, null);
	}

	/**
	 * The shape with its shapetype.
	 *
	 * @param root the part's content (its JAXB root), or null to find it from the shape's parents
	 * @param cache the conversion's indexes by root, which live as long as it does (a static cache held every converted
	 *        document: its index's shapetypes lead back to their root by their parents); null for none
	 */
	public static Resolved resolve(CTShape shape, Object root, Map<Object, Index> cache) {
		if (root == null) root = root(shape);
		CTShapetype type = null;
		String ref = shape.getType();
		if (ref != null && root != null) {
			Index index = cache == null ? null : cache.get(root);
			if (index == null) {
				index = Index.of(root);
				if (cache != null) cache.put(root, index);
			}
			type = index.get(ref, shape);
			if ((type == null && !index.misses.contains(ref)) || (type != null && root(type) != root)) {
				// a shapetype added since the part was indexed, or one no longer in it: indexed again, once a miss
				Index again = Index.of(root);
				if (cache != null) cache.put(root, again);
				type = again.get(ref, shape);
				if (type == null) again.misses.add(ref);
			}
		}
		return new Resolved(shape, type);
	}

	/** A shape and its shapetype (or none), read attribute by attribute. */
	public static final class Resolved {
		private final CTShape shape;
		private final CTShapetype type;

		Resolved(CTShape shape, CTShapetype type) {
			this.shape = shape;
			this.type = type;
		}

		public CTShape getShape() {
			return shape;
		}

		/** The shapetype, or null where the {@code type} is absent or resolves to nothing. */
		public CTShapetype getShapetype() {
			return type;
		}

		/** The shape's child of this kind ({@code v:textpath}, {@code v:fill}, ...), or null. */
		public <E> E own(Class<E> kind) {
			return first(shape.getPathOrFormulasOrHandles(), kind);
		}

		/** The shapetype's child of this kind, or null. */
		public <E> E inherited(Class<E> kind) {
			return type == null ? null : first(type.getEGShapeElements(), kind);
		}

		/** Whether the shape or its shapetype has a child of this kind. */
		public boolean has(Class<?> kind) {
			return own(kind) != null || inherited(kind) != null;
		}

		/** An attribute of a child element: the shape's child's if it gives it, else the shapetype's child's. */
		public <E, T> T child(Class<E> kind, Function<E, T> attribute) {
			E o = own(kind);
			T v = o == null ? null : attribute.apply(o);
			if (v != null) return v;
			E t = inherited(kind);
			return t == null ? null : attribute.apply(t);
		}

		/** An attribute of the shape, else its shapetype's. */
		public <T> T attribute(Function<CTShape, T> own, Function<CTShapetype, T> inherited) {
			T v = own.apply(shape);
			if (v != null) return v;
			return type == null ? null : inherited.apply(type);
		}

		/**
		 * The adjust values, the shape's over its shapetype's, each held to its handle's range ({@code xrange},
		 * {@code yrange}), as Word evaluates them (cr083-phase4's wv42-49: 136's adj of 23313 drawn as 14971)
		 */
		public Integer[] getAdj() {
			Integer[] adj = VmlGeometry.adj(VmlGeometry.adj(shape.getAdj()), type == null ? null : VmlGeometry.adj(type.getAdj()));
			org.docx4j.vml.CTHandles hs = own(org.docx4j.vml.CTHandles.class);
			if (hs == null) hs = inherited(org.docx4j.vml.CTHandles.class);
			if (hs == null) return adj;
			for (org.docx4j.vml.CTH h : hs.getH()) {
				String pos = h.getPosition();
				if (pos == null || h.getPolar() != null) continue;
				String[] xy = pos.split(",", -1);
				for (int axis = 0; axis < 2 && axis < xy.length; axis++) {
					String ref = xy[axis].trim();
					String range = axis == 0 ? h.getXrange() : h.getYrange();
					if (!ref.startsWith("#") || range == null) continue;
					try {
						int n = Integer.parseInt(ref.substring(1));
						String[] r = range.split(",");
						long lo = Math.round(Double.parseDouble(r[0].trim())), hi = Math.round(Double.parseDouble(r[1].trim()));
						if (n < adj.length && adj[n] != null && lo <= hi) adj[n] = (int) Math.max(lo, Math.min(hi, adj[n]));
					} catch (RuntimeException e) {
						// a range Word wrote otherwise: as given
					}
				}
			}
			return adj;
		}

		/** The path: the shape's {@code path}, else its {@code v:path}'s {@code v}, else the shapetype's likewise. */
		public String getPath() {
			if (shape.getPath() != null) return shape.getPath();
			CTPath p = own(CTPath.class);
			if (p != null && p.getV() != null) return p.getV();
			if (type == null) return null;
			if (type.getPath() != null) return type.getPath();
			CTPath tp = inherited(CTPath.class);
			return tp == null ? null : tp.getV();
		}

		/** The formulas: the shape's {@code v:formulas} if it has one, else the shapetype's. */
		public List<String> getFormulas() {
			CTFormulas f = own(CTFormulas.class);
			if (f == null) f = inherited(CTFormulas.class);
			if (f == null) return Collections.emptyList();
			List<String> out = new ArrayList<String>();
			for (CTF eqn : f.getF()) out.add(eqn.getEqn());
			return out;
		}

		public String getCoordsize() {
			return attribute(CTShape::getCoordsize, CTShapetype::getCoordsize);
		}

		public String getCoordorigin() {
			return attribute(CTShape::getCoordorigin, CTShapetype::getCoordorigin);
		}

		/** The shape's own style: placement isn't inherited. */
		public String getStyle() {
			return shape.getStyle();
		}

		/**
		 * Whether this is WordArt: it has a text path with text. Word draws it whatever {@code on} and {@code textpathok}
		 * say (cr083-phase4's wv43-11 and -12: on="f" on the shape, textpathok="f" on its shapetype, both drawn); what it
		 * draws is its geometry's, and a shape with no path draws nothing.
		 */
		public boolean isTextPath() {
			if (!has(CTTextPath.class)) return false;
			String s = child(CTTextPath.class, CTTextPath::getString);
			return s != null && !s.isEmpty();
		}

		/** The geometry's definition, for {@link VmlGeometry}. */
		public VmlGeometry.Definition definition() {
			return new VmlGeometry.Definition().path(getPath()).formulas(getFormulas()).adj(getAdj())
					.coordsize(getCoordsize()).coordorigin(getCoordorigin())
					.textboxrect(child(CTPath.class, CTPath::getTextboxrect)).limo(child(CTPath.class, CTPath::getLimo))
					.paint(isFilled(), isStroked(), strokeWeightPt());
		}

		/** {@code filled}, by default true. */
		public boolean isFilled() {
			org.docx4j.vml.STTrueFalse f = attribute(CTShape::getFilled, CTShapetype::getFilled);
			if (f != null && !isTrue(f)) return false;
			org.docx4j.vml.STTrueFalse on = child(org.docx4j.vml.CTFill.class, org.docx4j.vml.CTFill::getOn);
			return on == null || isTrue(on);
		}

		/** {@code stroked}, by default true. */
		public boolean isStroked() {
			org.docx4j.vml.STTrueFalse s = attribute(CTShape::getStroked, CTShapetype::getStroked);
			if (s != null && !isTrue(s)) return false;
			org.docx4j.vml.STTrueFalse on = child(org.docx4j.vml.CTStroke.class, org.docx4j.vml.CTStroke::getOn);
			return on == null || isTrue(on);
		}

		/** The stroke's weight in points: {@code v:stroke weight}, else {@code strokeweight}, else 0.75 pt (VML's 1 px). */
		public double strokeWeightPt() {
			String w = child(org.docx4j.vml.CTStroke.class, org.docx4j.vml.CTStroke::getWeight);
			if (w == null) w = attribute(CTShape::getStrokeweight, CTShapetype::getStrokeweight);
			double pt = VmlShapes.length(w, Double.NaN);
			return Double.isNaN(pt) || pt < 0 ? 0.75 : pt;
		}
	}

	private static <E> E first(List<JAXBElement<?>> children, Class<E> kind) {
		if (children == null) return null;
		for (JAXBElement<?> j : children) {
			Object u = j.getValue();
			if (kind.isInstance(u)) return kind.cast(u);
		}
		return null;
	}

	/** {@code t} or {@code true}. */
	public static boolean isTrue(org.docx4j.vml.STTrueFalse v) {
		return v == org.docx4j.vml.STTrueFalse.T || v == org.docx4j.vml.STTrueFalse.TRUE;
	}

	/**
	 * A VML length in points: a number with a unit ({@code pt}, {@code in}, {@code cm}, {@code mm}, {@code pc},
	 * {@code px} at 96 an inch, {@code emu}), or none (pixels, Part 4 says; Word writes {@code strokeweight} in points
	 * with the unit, and EMU as a plain number in some attributes, so a plain number is taken as pixels here). The
	 * default for null or what can't be read.
	 */
	public static double length(String s, double dflt) {
		if (s == null) return dflt;
		String t = s.trim().toLowerCase(java.util.Locale.ROOT);
		if (t.isEmpty()) return dflt;
		double scale;
		String n;
		if (t.endsWith("pt")) { scale = 1; n = t.substring(0, t.length() - 2); }
		else if (t.endsWith("in")) { scale = 72; n = t.substring(0, t.length() - 2); }
		else if (t.endsWith("cm")) { scale = 72 / 2.54; n = t.substring(0, t.length() - 2); }
		else if (t.endsWith("mm")) { scale = 72 / 25.4; n = t.substring(0, t.length() - 2); }
		else if (t.endsWith("pc")) { scale = 12; n = t.substring(0, t.length() - 2); }
		else if (t.endsWith("px")) { scale = 0.75; n = t.substring(0, t.length() - 2); }
		else if (t.endsWith("emu")) { scale = 1 / 12700.0; n = t.substring(0, t.length() - 3); }
		else { scale = 0.75; n = t; }
		try {
			double v = Double.parseDouble(n.trim()) * scale;
			return Double.isFinite(v) ? v : dflt;
		} catch (NumberFormatException e) {
			return dflt;
		}
	}
}
