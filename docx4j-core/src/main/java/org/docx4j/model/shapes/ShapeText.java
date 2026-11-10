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

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;

/**
 * Where Word lays out a DrawingML shape's text (wps:wsp/wps:txbx), as the PDF and HTML outputs both set it (CR-080
 * phases 2 and 5): which shapes show any, its insets with half the shape's line, the geometry's text rectangle, and
 * where its bodyPr anchors it.
 *
 * @since 17.3.7
 */
public final class ShapeText {

	private ShapeText() {}

	/**
	 * The presets Word draws no text in, though a text box be there (CR-080 phase 0): the line, the connectors and
	 * three of the chart symbols.
	 */
	public static final Set<String> NO_TEXT = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("line", "lineInv",
			"straightConnector1", "bentConnector2", "bentConnector3", "bentConnector4", "bentConnector5", "curvedConnector2",
			"curvedConnector3", "curvedConnector4", "curvedConnector5", "chartPlus", "chartStar", "chartX")));

	/** Word's default text insets, DrawingML's: 0.1in left and right, 0.05in top and bottom (l, t, r, b, in pt) */
	public static final double[] DEFAULT_INSETS = { 7.2, 3.6, 7.2, 3.6 };

	/** The shape's preset's name, or null for a custom geometry or none */
	public static String preset(CTWordprocessingShape wsp) {
		if (wsp == null || wsp.getSpPr() == null || wsp.getSpPr().getPrstGeom() == null || wsp.getSpPr().getPrstGeom().getPrst() == null) return null;
		return wsp.getSpPr().getPrstGeom().getPrst().value();
	}

	/** Whether Word shows the shape's text: it has a text box, and its preset isn't one of {@link #NO_TEXT} */
	public static boolean hasText(CTWordprocessingShape wsp) {
		return wsp != null && wsp.getTxbx() != null && wsp.getTxbx().getTxbxContent() != null && !NO_TEXT.contains(preset(wsp));
	}

	/** The shape's text insets (wps:bodyPr lIns, tIns, rIns, bIns) in pt, l t r b, Word's default for any unstated;
	 *  null where none is stated */
	public static double[] bodyInsets(CTWordprocessingShape wsp) {
		if (wsp == null || wsp.getBodyPr() == null) return null;
		org.docx4j.dml.CTTextBodyProperties b = wsp.getBodyPr();
		if (b.getLIns() == null && b.getTIns() == null && b.getRIns() == null && b.getBIns() == null) return null;
		return new double[] { b.getLIns() == null ? 7.2 : b.getLIns() / 12700d, b.getTIns() == null ? 3.6 : b.getTIns() / 12700d,
				b.getRIns() == null ? 7.2 : b.getRIns() / 12700d, b.getBIns() == null ? 3.6 : b.getBIns() / 12700d };
	}

	/**
	 * The width in pt of the line round the shape (wps:spPr/a:ln): 0 where there is none or it is a:noFill, its w where
	 * it gives one, DrawingML's default of 0.75pt where it doesn't. With no a:ln the shape is drawn by its wps:style's
	 * a:lnRef, which Word's text boxes give idx 0 (no line) unless styled: 0.75pt only where the lnRef names a line.
	 */
	public static double linePt(CTWordprocessingShape wsp) {
		if (wsp == null) return 0;
		org.docx4j.dml.CTLineProperties ln = wsp.getSpPr() == null ? null : wsp.getSpPr().getLn();
		if (ln == null) {
			org.docx4j.dml.CTShapeStyle style = wsp.getStyle();
			return (style != null && style.getLnRef() != null && style.getLnRef().getIdx() > 0) ? 0.75 : 0;
		}
		if (ln.getNoFill() != null) return 0;
		return ln.getW() == null ? 0.75 : ln.getW() / 12700d;
	}

	/** The insets, Word's defaults where null, each with half the line added; null where both are absent */
	public static double[] withLine(double[] insets, double linePt) {
		if (linePt <= 0) return insets;
		double[] in = (insets == null ? DEFAULT_INSETS : insets).clone();
		for (int i = 0; i < in.length; i++) in[i] += linePt / 2;
		return in;
	}

	/**
	 * The insets with the geometry's text rectangle added, as Word lays out a shape's text in it: each edge's distance
	 * in from the box (l, t, r, b, pt). A shape whose a:xfrm is omitted has no text rectangle ([MS-OI29500] 2.1.1315),
	 * its text in the box.
	 *
	 * @param insets the bodyPr's (with the line), or null for Word's defaults
	 * @param outlines the shape's geometry at its box w by h, or null
	 */
	public static double[] textInsets(double[] insets, ShapeOutlines outlines, CTWordprocessingShape wsp, double w, double h) {
		if (outlines == null || wsp == null || wsp.getSpPr() == null || wsp.getSpPr().getXfrm() == null) return insets;
		Rectangle2D r = outlines.getTextRectangle();
		if (r == null) return insets;
		double[] in = insets == null ? DEFAULT_INSETS.clone() : insets.clone();
		double[] add = { r.getMinX(), r.getMinY(), w - r.getMaxX(), h - r.getMaxY() };
		boolean any = false;
		for (int i = 0; i < 4; i++) {
			double a = Math.max(0, add[i]);
			if (a > 1e-6) any = true;
			in[i] += a;
		}
		return any ? in : insets;
	}

	/**
	 * The shapes with a text box (wps:txbx/w:txbxContent) in a graphic, in document order: a shape itself, or a group's
	 * or canvas's, nested groups' included. The order the XSLT pathways convert their text in.
	 */
	public static java.util.List<CTWordprocessingShape> textBoxes(Object graphic) {
		java.util.List<CTWordprocessingShape> out = new java.util.ArrayList<CTWordprocessingShape>();
		if (graphic instanceof CTWordprocessingShape) {
			CTWordprocessingShape wsp = (CTWordprocessingShape) graphic;
			if (wsp.getTxbx() != null && wsp.getTxbx().getTxbxContent() != null) out.add(wsp);
			return out;
		}
		java.util.List<Object> children
				= graphic instanceof org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup
				? ((org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup) graphic).getWspOrGrpSpOrGraphicFrame()
				: graphic instanceof org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas
				? ((org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas) graphic).getWspOrPicOrContentPart()
				: null;
		if (children == null) return out;
		for (Object o : children) {
			Object c = o instanceof jakarta.xml.bind.JAXBElement ? ((jakarta.xml.bind.JAXBElement<?>) o).getValue() : o;
			if (c instanceof CTWordprocessingShape
					|| c instanceof org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup) {
				out.addAll(textBoxes(c));
			}
		}
		return out;
	}

	/** Where the shape's bodyPr anchors its text in the text rectangle: "t", "ctr" or "b" ({@code just} and
	 *  {@code dist} read as "t"); null where it says nothing */
	public static String anchor(CTWordprocessingShape wsp) {
		if (wsp == null || wsp.getBodyPr() == null || wsp.getBodyPr().getAnchor() == null) return null;
		String a = wsp.getBodyPr().getAnchor().value();
		return "ctr".equals(a) || "b".equals(a) ? a : "t";
	}
}
