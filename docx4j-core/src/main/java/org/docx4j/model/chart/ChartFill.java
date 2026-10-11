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

import org.docx4j.dml.CTShapeProperties;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.StyleFill;
import org.docx4j.model.color.ThemeColors;

/**
 * A series', point's or other chart element's fill or line, as its {@code c:spPr} states it.
 * {@link Kind#AUTOMATIC} where it states none: the colour Office gives it then comes from the
 * chart's style and colour style parts, by the series' index and the chart's style number
 * ({@link ChartModel#getStyle()}), which is drawing's business (the charts plan's phase 7), so it
 * is reported as automatic rather than guessed.  A solid fill's colour is resolved through the
 * chart's theme (CR-034).
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartFill {

	public enum Kind {
		/** nothing stated: the chart's style and colour style decide */
		AUTOMATIC,
		/** {@code a:noFill} */
		NONE,
		/** {@code a:solidFill}: {@link #getColor()} */
		SOLID,
		/** {@code a:gradFill} */
		GRADIENT,
		/** {@code a:pattFill} */
		PATTERN,
		/** {@code a:blipFill} */
		PICTURE,
		/** {@code a:grpFill} */
		GROUP
	}

	static final ChartFill AUTO = new ChartFill(Kind.AUTOMATIC, null, null);

	private final Kind kind;
	private final ResolvedColor color;
	private final Object source;

	private ChartFill(Kind kind, ResolvedColor color, Object source) {
		this.kind = kind;
		this.color = color;
		this.source = source;
	}

	/** The fill of a {@code c:spPr} */
	static ChartFill fill(CTShapeProperties spPr, ThemeColors theme) {
		if (spPr == null) return AUTO;
		Object f = spPr.getNoFill() != null ? spPr.getNoFill()
				: spPr.getSolidFill() != null ? spPr.getSolidFill()
				: spPr.getGradFill() != null ? spPr.getGradFill()
				: spPr.getPattFill() != null ? spPr.getPattFill()
				: spPr.getBlipFill() != null ? spPr.getBlipFill()
				: spPr.getGrpFill();
		return f == null ? AUTO : of(DrawingMLColors.fill(f, theme), f);
	}

	/** The line of a {@code c:spPr} ({@code a:ln}'s fill) */
	static ChartFill line(CTShapeProperties spPr, ThemeColors theme) {
		if (spPr == null || spPr.getLn() == null) return AUTO;
		StyleFill sf = DrawingMLColors.lineFill(spPr.getLn(), theme);
		return of(sf, spPr.getLn());
	}

	private static ChartFill of(StyleFill sf, Object source) {
		switch (sf.getKind()) {
			case NONE: return new ChartFill(Kind.NONE, null, source);
			case SOLID: return new ChartFill(Kind.SOLID, sf.getColor(), source);
			case GRADIENT: return new ChartFill(Kind.GRADIENT, null, source);
			case PATTERN: return new ChartFill(Kind.PATTERN, null, source);
			case PICTURE: return new ChartFill(Kind.PICTURE, null, source);
			case GROUP: return new ChartFill(Kind.GROUP, null, source);
			default: return AUTO;
		}
	}

	public Kind getKind() {
		return kind;
	}

	public boolean isAutomatic() {
		return kind == Kind.AUTOMATIC;
	}

	/** A solid fill's colour, as Office draws it; null for any other kind */
	public ResolvedColor getColor() {
		return color;
	}

	/** The JAXB fill (or {@code a:ln}) it was read from; null for automatic */
	public Object getSource() {
		return source;
	}
}
