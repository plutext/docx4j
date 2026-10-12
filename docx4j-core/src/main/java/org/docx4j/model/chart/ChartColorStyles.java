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
package org.docx4j.model.chart;

import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTColorStyle;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTColorStyleVariation;
import org.docx4j.dml.CTPositiveFixedPercentage;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.ObjectFactory;
import org.docx4j.dml.STSchemeColorVal;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.ChartColorStylePart;
import org.docx4j.copy.Copyable;

/**
 * The colours Office gives a chart's series and points that have none of their own: its colour
 * style's ({@code cs:colorStyle}, the chart's colors part, Office 2013's), the colour for an index
 * its colour index modulo their number, varied by the variation for the index divided by it
 * ({@code meth="cycle"}), what {@link ChartDataEditor} writes into a new series; and the colour
 * Office shows for one that has none, the renderer's ({@link #automatic(long, int)}; CR-037 §4.2).
 *
 * @since 17.3.3 (CR-037)
 */
public final class ChartColorStyles {

	private ChartColorStyles() {
	}

	/** The colour style of a chart part (the colors part it relates to), or null where it has none */
	public static CTColorStyle of(Chart chart) {
		if (chart == null || chart.getRelationshipsPart() == null) return null;
		for (org.docx4j.relationships.Relationship r : chart.getRelationshipsPart().getRelationships().getRelationship()) {
			Part p = chart.getRelationshipsPart().getPart(r);
			if (p instanceof ChartColorStylePart) {
				try {
					return ((ChartColorStylePart) p).getContents();
				} catch (Docx4JException e) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * The colour style's colour for an index: colour idx mod n, varied by variation (idx / n) mod
	 * v; a new object (an {@code a:schemeClr} ..., its transforms the variation's after its own)
	 */
	@SuppressWarnings("unchecked")
	public static Object automatic(CTColorStyle cs, long idx) {
		List<Object> colours = cs.getEGColorChoice();
		int n = colours.size();
		Object c = ((Copyable) XmlUtils.unwrap(colours.get((int) (idx % n)))).copy();
		List<CTColorStyleVariation> vs = cs.getVariation();
		if (!vs.isEmpty()) {
			CTColorStyleVariation v = vs.get((int) ((idx / n) % vs.size()));
			List<JAXBElement<?>> to = (List<JAXBElement<?>>) ChartSeries.get(c, "getEGColorTransform");
			if (to != null) {
				for (JAXBElement<?> t : v.getEGColorTransform()) {
					Object val = t.getValue() instanceof Copyable ? ((Copyable) t.getValue()).copy() : t.getValue();
					to.add(new JAXBElement<Object>(t.getName(), (Class<Object>) t.getDeclaredType(), val));
				}
			}
		}
		return c;
	}

	/**
	 * The colour Office shows for a series that has no fill of its own (or, where the colours vary
	 * by point, a point), whatever the chart's colour style: accent (idx mod 6) + 1, shaded or tinted
	 * by its cycle, idx / 6, of the count's, count / 6: t = floor(140 (cycle + 1) / (cycles + 2)) - 70
	 * percent, a shade of 100 + t below zero, a tint of 100 - t above (CR-037 R14, every channel
	 * exact: eight series, shaded 76% then tinted 77%, with the colour style part and without it;
	 * twelve slices, shaded 65% then plain).  The colour style is what Office writes into a series
	 * it adds ({@link #automatic(CTColorStyle, long)}), not what it shows for one that has none.
	 * @param idx the series' index (c:idx), or the point's
	 * @param count the series in the chart, or the points
	 */
	public static Object automatic(long idx, int count) {
		ObjectFactory f = new ObjectFactory();
		CTSchemeColor c = new CTSchemeColor();
		c.setVal(STSchemeColorVal.fromValue("accent" + (idx % 6 + 1)));
		long cycle = idx / 6, cycles = Math.max(0, count) / 6;
		long t = Math.min(70, 140 * (cycle + 1) / (cycles + 2) - 70);
		if (t != 0) {
			CTPositiveFixedPercentage p = new CTPositiveFixedPercentage();
			p.setVal((int) (100 - Math.abs(t)) * 1000);
			c.getEGColorTransform().add(t < 0 ? f.createCTSchemeColorShade(p) : f.createCTSchemeColorTint(p));
		}
		return c;
	}
}
