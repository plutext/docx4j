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
package org.docx4j.convert.out.fo;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeOutlines;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A DrawingML shape ({@code wps:wsp}) drawn in the FO (CR-080 phase 2): its drawing, docx4j-chart-render's
 * {@link ShapeRenderer} as SVG, goes first in the shape's container, under its text, which is laid out in the
 * geometry's text rectangle.
 */
final class FOShapes {

	private static final Logger log = LoggerFactory.getLogger(FOShapes.class);

	private static final String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	/**
	 * The presets Word draws no text in, though a text box be there (CR-080 phase 0): the line, the connectors and
	 * three of the chart symbols.
	 */
	static final Set<String> NO_TEXT = org.docx4j.model.shapes.ShapeText.NO_TEXT;

	private FOShapes() {}

	/** The shape's preset's name, or null for a custom geometry or none */
	static String preset(CTWordprocessingShape wsp) {
		return org.docx4j.model.shapes.ShapeText.preset(wsp);
	}

	/** Whether Word shows the shape's text: it has a text box, and its preset isn't one of {@link #NO_TEXT} */
	static boolean hasText(CTWordprocessingShape wsp) {
		return org.docx4j.model.shapes.ShapeText.hasText(wsp);
	}

	/**
	 * The shape drawn at its extent (Word draws it at its anchor's, whatever its a:xfrm says: phase 0, fp78, fp80); null
	 * where it can't be. Its gaps are logged, a code each.
	 */
	static ShapeRenderer.Drawn draw(AbstractWmlConversionContext context, CTWordprocessingShape wsp, long cx, long cy) {
		if (wsp == null || cx <= 0 && cy <= 0) return null;
		try {
			ThemeColors theme = ThemeColors.of(context.getWmlPackage());
			final Part host = context.getCurrentPart();
			ShapeRenderer.Drawn drawn = ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, cx, cy, relId -> picture(host, relId), hasText(wsp));
			for (String code : drawn.getProblems()) {
				log.warn(code + ": a DrawingML shape" + (drawn.getOutlines().getDetail() == null ? "" : ", " + drawn.getOutlines().getDetail())
						+ " (CR-080 D9)");
			}
			return drawn;
		} catch (RuntimeException e) {
			log.warn("render.shape.bad_geometry: a DrawingML shape couldn't be drawn: " + e, e);
			return null;
		}
	}

	/** A picture fill's picture, by its relationship from the part being converted; null where it isn't one ImageIO reads */
	private static BufferedImage picture(Part host, String relId) {
		try {
			if (host == null || host.getRelationshipsPart() == null) return null;
			Part p = host.getRelationshipsPart().getPart(relId);
			if (!(p instanceof BinaryPart)) return null;
			java.nio.ByteBuffer b = ((BinaryPart) p).getBuffer();
			byte[] bytes = new byte[b.remaining()];
			b.duplicate().get(bytes);
			return javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (Exception e) {
			log.debug("A picture fill's picture couldn't be read: " + e);
			return null;
		}
	}

	/**
	 * The text's insets with the geometry's text rectangle added, as Word lays out a shape's text in it: each edge's
	 * distance in from the box. A shape whose a:xfrm is omitted has no text rectangle ([MS-OI29500] 2.1.1315), its
	 * text in the box.
	 *
	 * @param insets the bodyPr's, or null for Word's defaults
	 */
	static double[] textInsets(double[] insets, ShapeRenderer.Drawn drawn, CTWordprocessingShape wsp, double w, double h) {
		return drawn == null ? insets : textInsets(insets, drawn.getOutlines(), wsp, w, h);
	}

	/** As {@link #textInsets(double[], ShapeRenderer.Drawn, CTWordprocessingShape, double, double)}, from the geometry */
	static double[] textInsets(double[] insets, ShapeOutlines outlines, CTWordprocessingShape wsp, double w, double h) {
		return org.docx4j.model.shapes.ShapeText.textInsets(insets, outlines, wsp, w, h);
	}

	/**
	 * The text where the shape's bodyPr anchors it in the text rectangle ({@code anchor}: t, ctr, b), as the
	 * container's display-align. {@code just} and {@code dist} are read as top. Without it docx4j set every shape's text
	 * at its top, where Word sets an inserted shape's, whose anchor is ctr, in its middle.
	 */
	static void anchorText(Element container, CTWordprocessingShape wsp) {
		if (wsp == null || wsp.getBodyPr() == null || wsp.getBodyPr().getAnchor() == null) return;
		switch (wsp.getBodyPr().getAnchor().value()) {
			case "ctr": container.setAttribute("display-align", "center"); break;
			case "b": container.setAttribute("display-align", "after"); break;
			default: container.setAttribute("display-align", "before");
		}
	}

	/**
	 * The drawing as the container's first child, under its text: an absolutely positioned block-container at the
	 * drawing's bounds, measured from the container's content rectangle, which starts below its padding-top. FOP
	 * paints it before the text that follows it.
	 */
	static void addDrawing(Document doc, Element container, ShapeRenderer.Drawn drawn) {
		Rectangle2D.Double b = drawn.getBounds();
		double paddingTop = WordLayoutFixups.lengthPt(container.getAttribute("padding-top"));
		Element abs = doc.createElementNS(XSL_FO, "fo:block-container");
		abs.setAttribute("absolute-position", "absolute");
		abs.setAttribute("top", pt(b.y - paddingTop));
		abs.setAttribute("left", pt(b.x));
		abs.setAttribute("width", pt(b.width));
		abs.setAttribute("height", pt(b.height));
		abs.setAttribute("overflow", "visible");
		abs.setAttribute("start-indent", "0pt");
		abs.setAttribute("end-indent", "0pt");
		Element block = doc.createElementNS(XSL_FO, "fo:block");
		block.setAttribute("font-size", "0pt");
		block.setAttribute("line-height", "0pt");
		block.setAttribute("start-indent", "0pt");
		block.setAttribute("end-indent", "0pt");
		Element ifo = doc.createElementNS(XSL_FO, "fo:instream-foreign-object");
		ifo.setAttribute("content-width", pt(b.width));
		ifo.setAttribute("content-height", pt(b.height));
		ifo.setAttribute("scaling", "non-uniform");
		ifo.appendChild(doc.importNode(drawn.toSvg().getDocumentElement(), true));
		block.appendChild(ifo);
		abs.appendChild(block);
		container.insertBefore(abs, container.getFirstChild());
	}

	// ------------------------------------------------------------------ groups and canvases (CR-080 phase 3)

	/** The group (wpg:wgp) or canvas (wpc:wpc) a drawing's graphicData holds, or null */
	static Object group(org.docx4j.dml.GraphicData gd) {
		if (gd == null) return null;
		for (Object o : gd.getAny()) {
			Object v = o instanceof jakarta.xml.bind.JAXBElement ? ((jakarta.xml.bind.JAXBElement<?>) o).getValue() : o;
			if (v instanceof CTWordprocessingGroup || v instanceof CTWordprocessingCanvas) return v;
		}
		return null;
	}

	/** The group or canvas drawn at its extent; null where it can't be. Its gaps are logged, a code each. */
	static ShapeRenderer.Drawn drawGroup(AbstractWmlConversionContext context, Object group, long cx, long cy) {
		if (group == null || cx <= 0 && cy <= 0) return null;
		try {
			ThemeColors theme = ThemeColors.of(context.getWmlPackage());
			final Part host = context.getCurrentPart();
			ShapeRenderer.Drawn drawn = group instanceof CTWordprocessingGroup
					? ShapeRenderer.drawGroup((CTWordprocessingGroup) group, theme, cx, cy, relId -> picture(host, relId))
					: ShapeRenderer.drawCanvas((CTWordprocessingCanvas) group, theme, cx, cy, relId -> picture(host, relId));
			for (String code : drawn.getProblems()) {
				log.warn(code + ": a DrawingML " + (group instanceof CTWordprocessingGroup ? "group" : "canvas") + " (CR-080 D9)");
			}
			return drawn;
		} catch (RuntimeException e) {
			log.warn("render.shape.bad_geometry: a DrawingML group or canvas couldn't be drawn: " + e, e);
			return null;
		}
	}

	/**
	 * The shapes in a group or canvas with a text box (wps:txbx/w:txbxContent), nested groups' included, in document
	 * order: the order the XSLT pathway converts their text in
	 */
	static List<CTWordprocessingShape> textBoxes(Object group) {
		if (!(group instanceof CTWordprocessingGroup || group instanceof CTWordprocessingCanvas)) return new ArrayList<CTWordprocessingShape>();
		return org.docx4j.model.shapes.ShapeText.textBoxes(group);
	}

	/**
	 * A group's or canvas's shape's text: a container at the shape's box in the group's container (which has no
	 * padding), absolutely positioned, set in by its text rectangle and its insets, anchored as its bodyPr says. A
	 * turned shape's text stays level, as a single shape's does (phase 2).
	 *
	 * @param insets the shape's bodyPr insets with its line's half, or null for Word's defaults
	 */
	static Element childText(Document doc, ShapeRenderer.Placed placed, CTWordprocessingShape wsp, double[] insets) {
		Rectangle2D.Double b = placed.getBox();
		double[] in = textInsets(insets, placed.getOutlines(), wsp, b.width, b.height);
		Element container = FOTextBoxes.createContainer(doc, "inline", b.width, b.height, 0, "p:0", 0, 0, in);
		container.setAttribute("absolute-position", "absolute");
		container.setAttribute("left", pt(b.x));
		double paddingTop = WordLayoutFixups.lengthPt(container.getAttribute("padding-top"));
		container.setAttribute("top", pt(b.y + paddingTop));
		anchorText(container, wsp);
		return container;
	}

	private static String pt(double v) {
		String s = String.format(Locale.ROOT, "%.4f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return s + "pt";
	}
}
