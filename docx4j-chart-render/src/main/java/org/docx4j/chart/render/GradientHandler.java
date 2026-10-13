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
package org.docx4j.chart.render;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.util.Locale;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.w3c.dom.Element;

/**
 * Batik's SVG generator writes a two-stop {@code GradientPaint} only; this writes the chart's
 * gradients (DrawingML's, any number of stops, linear or radial: {@link Drawing.Gradient}) as SVG
 * {@code linearGradient} and {@code radialGradient} definitions, in user space, which FOP and
 * browsers draw.
 */
final class GradientHandler extends DefaultExtensionHandler {

	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	@Override
	public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext ctx) {
		if (!(paint instanceof MultipleGradientPaint)) return null;
		MultipleGradientPaint p = (MultipleGradientPaint) paint;
		Element e;
		String id;
		if (p instanceof LinearGradientPaint) {
			LinearGradientPaint l = (LinearGradientPaint) p;
			id = ctx.getIDGenerator().generateID("linearGradient");
			e = ctx.getDOMFactory().createElementNS(SVG_NS, "linearGradient");
			e.setAttributeNS(null, "x1", n(l.getStartPoint().getX()));
			e.setAttributeNS(null, "y1", n(l.getStartPoint().getY()));
			e.setAttributeNS(null, "x2", n(l.getEndPoint().getX()));
			e.setAttributeNS(null, "y2", n(l.getEndPoint().getY()));
		} else if (p instanceof RadialGradientPaint) {
			RadialGradientPaint r = (RadialGradientPaint) p;
			id = ctx.getIDGenerator().generateID("radialGradient");
			e = ctx.getDOMFactory().createElementNS(SVG_NS, "radialGradient");
			e.setAttributeNS(null, "cx", n(r.getCenterPoint().getX()));
			e.setAttributeNS(null, "cy", n(r.getCenterPoint().getY()));
			e.setAttributeNS(null, "r", n(r.getRadius()));
			e.setAttributeNS(null, "fx", n(r.getFocusPoint().getX()));
			e.setAttributeNS(null, "fy", n(r.getFocusPoint().getY()));
		} else {
			return null;
		}
		e.setAttributeNS(null, "id", id);
		e.setAttributeNS(null, "gradientUnits", "userSpaceOnUse");
		AffineTransform t = p.getTransform();
		if (t != null && !t.isIdentity()) {
			e.setAttributeNS(null, "gradientTransform", "matrix(" + n(t.getScaleX()) + " " + n(t.getShearY()) + " " + n(t.getShearX()) + " "
					+ n(t.getScaleY()) + " " + n(t.getTranslateX()) + " " + n(t.getTranslateY()) + ")");
		}
		float[] f = p.getFractions();
		Color[] c = p.getColors();
		for (int i = 0; i < f.length; i++) {
			Element stop = ctx.getDOMFactory().createElementNS(SVG_NS, "stop");
			stop.setAttributeNS(null, "offset", n(f[i]));
			stop.setAttributeNS(null, "stop-color", String.format(Locale.ROOT, "#%02x%02x%02x", c[i].getRed(), c[i].getGreen(), c[i].getBlue()));
			if (c[i].getAlpha() < 255) stop.setAttributeNS(null, "stop-opacity", n(c[i].getAlpha() / 255.0));
			e.appendChild(stop);
		}
		return new SVGPaintDescriptor("url(#" + id + ")", "1", e);
	}

	/** at most four decimals, no trailing zeros */
	private static String n(double v) {
		String s = String.format(Locale.ROOT, "%.4f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return s.equals("-0") ? "0" : s;
	}
}
