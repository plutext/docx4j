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
import org.docx4j.dml.CTPercentage;
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
 * ({@code meth="cycle"}).  Shared by {@link ChartDataEditor} (a new series in its automatic colour)
 * and the renderer (CR-037 §4.2).
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

	/** Office's colorful style (id 10, its charts' default): the six accents, nine variations of each */
	private static final int[][] COLORFUL = { {}, { 60000 }, { 80000, 20000 }, { 80000 }, { 60000, 40000 }, { 50000 }, { 70000, 30000 },
			{ 70000 }, { 50000, 50000 } };

	/**
	 * The colour for an index where a chart has no colour style: Office's colorful style's (the
	 * accents, then lumMod and lumOff variations); to be measured (CR-037 R14)
	 */
	public static Object automatic(long idx) {
		ObjectFactory f = new ObjectFactory();
		CTSchemeColor c = new CTSchemeColor();
		c.setVal(STSchemeColorVal.fromValue("accent" + (idx % 6 + 1)));
		int[] v = COLORFUL[(int) ((idx / 6) % COLORFUL.length)];
		if (v.length > 0) {
			CTPercentage mod = new CTPercentage();
			mod.setVal(v[0]);
			c.getEGColorTransform().add(f.createCTSchemeColorLumMod(mod));
		}
		if (v.length > 1) {
			CTPercentage off = new CTPercentage();
			off.setVal(v[1]);
			c.getEGColorTransform().add(f.createCTSchemeColorLumOff(off));
		}
		return c;
	}
}
