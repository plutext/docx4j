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

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;

/**
 * A DrawingML shape's properties ({@code a:spPr}: a classic chart's {@code c:spPr}, a chartex's
 * {@code cx:spPr}) as a {@link Drawing}'s: its solid fill, gradient (any number of stops, linear
 * or along a path) or pattern (Office's 8 by 8 tiles), its line and dashes, its outer shadow and
 * glow, each colour resolved through the theme as Office resolves it (CR-034).
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class Fills {

	private final ThemeColors theme;

	/** @param theme the colours a shape's scheme colours resolve through: the chart's (its own override and map applied) */
	public Fills(ThemeColors theme) {
		this.theme = theme;
	}

	/** A resolved DrawingML colour as AWT's, its alpha kept; null for none */
	public static Color awt(ResolvedColor c) {
		if (c == null) return null;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.round(255.0 * c.getAlpha() / ResolvedColor.OPAQUE));
	}

	/** A shape's fill: solid only (phases 1 and 2); null for none */
	public Color fill(CTShapeProperties sp) {
		if (sp == null || sp.getNoFill() != null) return null;
		if (sp.getSolidFill() != null) return awt(DrawingMLColors.resolve(sp.getSolidFill(), theme));
		return null;
	}

	/** A shape's gradient fill (a:gradFill), its stops resolved through the theme; null for none */
	public Drawing.Gradient gradient(CTShapeProperties sp) {
		return sp == null ? null : gradient(sp.getGradFill());
	}

	/** A gradient fill (a:gradFill, a line's too), its stops resolved through the theme; null for none */
	public Drawing.Gradient gradient(org.docx4j.dml.CTGradientFillProperties g) {
		if (g == null || g.getGsLst() == null) return null;
		List<org.docx4j.dml.CTGradientStop> gs = new ArrayList<>(g.getGsLst().getGs());
		if (gs.isEmpty()) return null;
		gs.sort((a, b) -> Integer.compare(a.getPos(), b.getPos()));
		float[] stops = new float[gs.size()];
		Color[] colours = new Color[gs.size()];
		for (int i = 0; i < gs.size(); i++) {
			org.docx4j.dml.CTGradientStop st = gs.get(i);
			Object c = st.getSchemeClr() != null ? st.getSchemeClr() : st.getSrgbClr() != null ? st.getSrgbClr()
					: st.getPrstClr() != null ? st.getPrstClr() : st.getSysClr() != null ? st.getSysClr()
							: st.getHslClr() != null ? st.getHslClr() : st.getScrgbClr();
			colours[i] = awt(DrawingMLColors.resolve(c, theme));
			if (colours[i] == null) colours[i] = Color.GRAY;
			// strictly increasing, as AWT's gradients need
			float f = (float) Math.max(0, Math.min(1, st.getPos() / 100000.0));
			stops[i] = i > 0 && f <= stops[i - 1] ? Math.min(1f, stops[i - 1] + 1e-5f) : f;
		}
		if (stops.length > 1 && stops[stops.length - 1] <= stops[stops.length - 2]) {
			// stops crowded at the end: as they came, spread a little below it
			for (int i = stops.length - 2; i >= 0 && stops[i] >= stops[i + 1]; i--) stops[i] = stops[i + 1] - 1e-5f;
		}
		if (g.getPath() != null) {
			org.docx4j.dml.CTRelativeRect r = g.getPath().getFillToRect();
			double[] focus = r == null ? new double[] { 0.5, 0.5, 0.5, 0.5 }
					: new double[] { r.getL() / 100000.0, r.getT() / 100000.0, r.getR() / 100000.0, r.getB() / 100000.0 };
			return new Drawing.Gradient(stops, colours, false, 0, false, focus);
		}
		double angle = g.getLin() != null && g.getLin().getAng() != null ? g.getLin().getAng() / 60000.0 : 0;
		boolean scaled = g.getLin() != null && Boolean.TRUE.equals(g.getLin().isScaled());
		return new Drawing.Gradient(stops, colours, true, angle, scaled, null);
	}

	/** A shape's outer shadow and glow (a:effectLst), or null for none */
	public List<Drawing.Shadow> shadows(CTShapeProperties sp) {
		if (sp == null || sp.getEffectLst() == null) return null;
		List<Drawing.Shadow> out = new ArrayList<>();
		org.docx4j.dml.CTGlowEffect gl = sp.getEffectLst().getGlow();
		if (gl != null) {
			Object c = gl.getSchemeClr() != null ? gl.getSchemeClr() : gl.getSrgbClr() != null ? gl.getSrgbClr() : gl.getPrstClr() != null ? gl.getPrstClr()
					: gl.getSysClr() != null ? gl.getSysClr() : gl.getHslClr() != null ? gl.getHslClr() : gl.getScrgbClr();
			Drawing.Shadow g = Drawing.Shadow.glow(awt(DrawingMLColors.resolve(c, theme)), gl.getRad() / 12700.0);
			if (g.getColour() != null && g.getGlow() > 0) out.add(g);
		}
		org.docx4j.dml.CTOuterShadowEffect os = sp.getEffectLst().getOuterShdw();
		if (os != null) {
			Object c = os.getSchemeClr() != null ? os.getSchemeClr() : os.getSrgbClr() != null ? os.getSrgbClr() : os.getPrstClr() != null ? os.getPrstClr()
					: os.getSysClr() != null ? os.getSysClr() : os.getHslClr() != null ? os.getHslClr() : os.getScrgbClr();
			String al = os.getAlgn() == null ? "b" : os.getAlgn().value();
			Drawing.Shadow sh = Drawing.Shadow.outer(awt(DrawingMLColors.resolve(c, theme)), os.getBlurRad() / 12700.0, os.getDist() / 12700.0,
					os.getDir() / 60000.0, os.getSx() / 100000.0, os.getSy() / 100000.0, os.getKx() / 60000.0, os.getKy() / 60000.0,
					al.endsWith("l") ? 0 : al.endsWith("r") ? 1 : 0.5, al.startsWith("t") ? 0 : al.startsWith("b") ? 1 : 0.5);
			if (sh.getColour() != null) out.add(sh);
		}
		return out.isEmpty() ? null : out;
	}

	/** A shape's pattern fill (a:pattFill), its colours resolved through the theme; null for none */
	public Drawing.Pattern pattern(CTShapeProperties sp) {
		if (sp == null || sp.getPattFill() == null) return null;
		org.docx4j.dml.CTPatternFillProperties pf = sp.getPattFill();
		Color fg = pf.getFgClr() == null ? Color.BLACK : awt(DrawingMLColors.resolve(pf.getFgClr(), theme));
		Color bg = pf.getBgClr() == null ? Color.WHITE : awt(DrawingMLColors.resolve(pf.getBgClr(), theme));
		String prst = pf.getPrst() == null ? "pct5" : pf.getPrst().value();
		return new Drawing.Pattern(Drawing.Pattern.preset(prst), fg == null ? Color.BLACK : fg, bg);
	}

	/** An area filled by a shape's properties: its solid fill, gradient or pattern, and its line */
	public Drawing.Area area(Shape shape, CTShapeProperties sp, Color fill, String role, Shape clip) {
		Drawing.Gradient gr = gradient(sp);
		Drawing.Pattern pt = gr == null ? pattern(sp) : null;
		Drawing.Area a = new Drawing.Area(shape, gr != null ? gr.getColours()[0] : pt != null ? pt.getForeground() : fill, lineColour(sp), lineWidth(sp),
				role, clip);
		a.setGradient(gr);
		a.setPattern(pt);
		a.setShadows(shadows(sp));
		a.setDash(dashes(sp, a.getLineWidth()));
		return a;
	}

	/** A shape's line's colour (solid only); null for none */
	public Color lineColour(CTShapeProperties sp) {
		CTLineProperties ln = sp == null ? null : sp.getLn();
		if (ln == null || ln.getNoFill() != null || ln.getSolidFill() == null) return null;
		return awt(DrawingMLColors.resolve(ln.getSolidFill(), theme));
	}

	/** A shape's line's width in points: 0.75 where it says none */
	public static double lineWidth(CTShapeProperties sp) {
		CTLineProperties ln = sp == null ? null : sp.getLn();
		return ln == null || ln.getW() == null ? 0.75 : ln.getW() / 12700.0;
	}

	/**
	 * A line's dashes (a:prstDash), in points: DrawingML's presets in multiples of the line's
	 * width (Office's: dash 4 3, sysDash 3 1, ...); null for a solid line
	 */
	public static float[] dashes(CTShapeProperties sp, double width) {
		if (sp == null || sp.getLn() == null || sp.getLn().getPrstDash() == null || sp.getLn().getPrstDash().getVal() == null) return null;
		float[] u;
		switch (sp.getLn().getPrstDash().getVal().value()) {
		case "dot": u = new float[] { 1, 3 }; break;
		case "dash": u = new float[] { 4, 3 }; break;
		case "lgDash": u = new float[] { 8, 3 }; break;
		case "dashDot": u = new float[] { 4, 3, 1, 3 }; break;
		case "lgDashDot": u = new float[] { 8, 3, 1, 3 }; break;
		case "lgDashDotDot": u = new float[] { 8, 3, 1, 3, 1, 3 }; break;
		case "sysDash": u = new float[] { 3, 1 }; break;
		case "sysDot": u = new float[] { 1, 1 }; break;
		case "sysDashDot": u = new float[] { 3, 1, 1, 1 }; break;
		case "sysDashDotDot": u = new float[] { 3, 1, 1, 1, 1, 1 }; break;
		default: return null;
		}
		for (int i = 0; i < u.length; i++) u[i] *= (float) Math.max(width, 0.1);
		return u;
	}
}
