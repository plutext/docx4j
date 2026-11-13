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
			ShapeRenderer.Drawn drawn = ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, cx, cy, relId -> picture(host, relId), hasText(wsp),
					wsp.getBodyPr() != null && wsp.getBodyPr().getFlatTx() != null);
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

	/**
	 * The text box drawn as WordArt (CR-083 phases 1 and 2), at the box Word draws; null where it isn't WordArt or can't
	 * be drawn, and it stays text. Its gaps are logged, a code each (D9).
	 */
	static org.docx4j.chart.render.api.WordArtRenderer.Drawn wordArt(AbstractWmlConversionContext context, CTWordprocessingShape wsp,
			long cx, long cy) {
		if (wsp == null || context == null || context.getWmlPackage() == null) return null;
		try {
			org.docx4j.model.shapes.WordArtText art = org.docx4j.model.shapes.WordArtText.of(wsp, context.getWmlPackage());
			if (art == null) return null;
			ThemeColors theme = ThemeColors.of(context.getWmlPackage());
			final Part host = context.getCurrentPart();
			org.docx4j.chart.render.api.WordArtRenderer.Drawn drawn = org.docx4j.chart.render.api.WordArtRenderer.draw(art,
					org.docx4j.model.chart.ChartFonts.of(host), theme, cx, cy, relId -> picture(host, relId));
			if (drawn == null) return null;
			for (String code : drawn.getProblems()) log.warn(code + ": WordArt (CR-083 D9)");
			return drawn;
		} catch (RuntimeException | OutOfMemoryError e) {
			// out of memory too: the drawing is let go and the text box stays text, as a picture's adjustments are (CR-084)
			log.warn("render.wordart.content: WordArt couldn't be drawn, left as text: " + e, e);
			return null;
		}
	}

	/**
	 * The WordArt drawn as the container's first child, as {@link #addDrawing} adds a shape's, its text as the
	 * picture's alternative text for a tagged PDF (the SVG's own text, real or invisible, isn't tagged).
	 */
	static void addWordArt(Document doc, Element container, org.docx4j.chart.render.api.WordArtRenderer.Drawn drawn) {
		Rectangle2D.Double b = drawn.getBounds();
		Element abs = doc.createElementNS(XSL_FO, "fo:block-container");
		abs.setAttribute("absolute-position", "absolute");
		abs.setAttribute("top", pt(b.y));
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
		if (drawn.getText() != null && !drawn.getText().isEmpty()) {
			ifo.setAttributeNS("http://xmlgraphics.apache.org/fop/extensions", "fox:alt-text", drawn.getText());
		}
		ifo.appendChild(doc.importNode(drawn.toSvg().getDocumentElement(), true));
		block.appendChild(ifo);
		abs.appendChild(block);
		container.insertBefore(abs, container.getFirstChild());
	}

	// ------------------------------------------------------------------ VML WordArt (CR-083 phase 4)

	/**
	 * A {@code w:pict} or {@code w:object} holding VML WordArt (a {@code v:shape} whose text path is on, read with its
	 * shapetype: {@link org.docx4j.model.shapes.VmlShapes}), drawn: placed as a VML picture is
	 * ({@link FOTextBoxes#createVmlPictureContainer}: page, margin, centred, in a cell), the drawing at its bounds in a
	 * container of the unturned box, which Word places; one with no position in its line. In a header or footer its text
	 * isn't written again (D17).
	 *
	 * @param pict the w:pict or w:object, as JAXB
	 * @return the fragment, empty where Word draws nothing (its geometry has no text path); null where the shape isn't
	 *         VML WordArt, for the picture route to take
	 */
	static org.w3c.dom.DocumentFragment vmlWordArt(AbstractWmlConversionContext context, org.docx4j.wml.CTPictureBase pict) {
		if (pict == null || context == null || context.getWmlPackage() == null) return null;
		org.docx4j.vml.CTShape shape = null;
		for (Object o : pict.getAnyAndAny()) {
			Object u = org.docx4j.XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.vml.CTGroup) return vmlGroup(context, (org.docx4j.vml.CTGroup) u);
			if (u instanceof org.docx4j.vml.CTShape) {
				shape = (org.docx4j.vml.CTShape) u;
				break;
			}
		}
		if (shape == null) return null;
		try {
			Part part = context.getCurrentPart();
			Object root = root(part);
			org.docx4j.model.shapes.VmlShapes.Resolved r = org.docx4j.model.shapes.VmlShapes.resolve(shape, root);
			if (!r.isTextPath()) return null;
			org.docx4j.model.shapes.WordArtText art = org.docx4j.model.shapes.VmlWordArt.of(r, context.getWmlPackage());
			if (art == null) return null;
			java.util.Map<String, String> props = FOTextBoxes.parseStyle(r.getStyle());
			double w = FOTextBoxes.pts(props.get("width"), 0), h = FOTextBoxes.pts(props.get("height"), 0);
			String flip = props.getOrDefault("flip", "").toLowerCase(Locale.ROOT);
			boolean hf = part instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart
					|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
			ShapeOutlines geometry = org.docx4j.model.shapes.VmlGeometry.outlines(r.definition(), w, h);
			org.docx4j.chart.render.api.WordArtRenderer.Drawn drawn = org.docx4j.chart.render.api.WordArtRenderer.drawVml(art, geometry,
					org.docx4j.model.chart.ChartFonts.of(part), ThemeColors.of(context.getWmlPackage()), w, h,
					rotation(props.get("rotation")), flip.contains("x"), flip.contains("y"), !hf,
				"center".equals(props.get("mso-position-vertical")));
			Document doc = org.docx4j.XmlUtils.neww3cDomDocument();
			org.w3c.dom.DocumentFragment frag = doc.createDocumentFragment();
			if (drawn == null) return frag;
			for (String code : drawn.getProblems()) log.warn(code + ": VML WordArt (CR-083 D9)");
			if (drawn.isEmpty()) return frag; // Word draws nothing: no text path
			if (FOTextBoxes.isEnabled() && FOTextBoxes.isPositioned(props)) {
				org.docx4j.model.structure.PageDimensions pd = context.getSections().getCurrentSection().getPageDimensions();
				Element container = FOTextBoxes.createVmlPictureContainer(doc, props, wrapType(shape), pd);
				FOTextBoxes.stampCellPlacement(container, props, org.docx4j.openpackaging.parts.WordprocessingML
						.DocumentSettingsPart.getCompatibilityMode(context.getWmlPackage()) >= 15);
				addWordArt(doc, container, drawn);
				container.appendChild(doc.createElementNS(XSL_FO, "fo:block"));
				frag.appendChild(container);
			} else {
				// in its line, at its box
				Rectangle2D.Double b = drawn.getBounds();
				Element ifo = doc.createElementNS(XSL_FO, "fo:instream-foreign-object");
				ifo.setAttribute("content-width", pt(b.width));
				ifo.setAttribute("content-height", pt(b.height));
				ifo.setAttribute("scaling", "non-uniform");
				if (drawn.getText() != null && !drawn.getText().isEmpty()) {
					ifo.setAttributeNS("http://xmlgraphics.apache.org/fop/extensions", "fox:alt-text", drawn.getText());
				}
				ifo.appendChild(doc.importNode(drawn.toSvg().getDocumentElement(), true));
				frag.appendChild(ifo);
			}
			return frag;
		} catch (RuntimeException | OutOfMemoryError e) {
			log.warn("render.wordart.content: VML WordArt couldn't be drawn: " + e, e);
			return null;
		}
	}

	private static Object root(Part part) {
		return part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart
				? ((org.docx4j.openpackaging.parts.JaxbXmlPart<?>) part).getJaxbElement() : null;
	}

	/** A VML WordArt drawn at a size (its style's rotation and flip), or null where it can't be */
	private static org.docx4j.chart.render.api.WordArtRenderer.Drawn drawVml(AbstractWmlConversionContext context,
			org.docx4j.model.shapes.VmlShapes.Resolved r, double w, double h, java.util.Map<String, String> props) {
		org.docx4j.model.shapes.WordArtText art = org.docx4j.model.shapes.VmlWordArt.of(r, context.getWmlPackage());
		if (art == null) return null;
		Part part = context.getCurrentPart();
		String flip = props.getOrDefault("flip", "").toLowerCase(Locale.ROOT);
		boolean hf = part instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart
				|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
		ShapeOutlines geometry = org.docx4j.model.shapes.VmlGeometry.outlines(r.definition(), w, h);
		org.docx4j.chart.render.api.WordArtRenderer.Drawn drawn = org.docx4j.chart.render.api.WordArtRenderer.drawVml(art, geometry,
				org.docx4j.model.chart.ChartFonts.of(part), ThemeColors.of(context.getWmlPackage()), w, h,
				rotation(props.get("rotation")), flip.contains("x"), flip.contains("y"), !hf,
				"center".equals(props.get("mso-position-vertical")));
		if (drawn != null) for (String code : drawn.getProblems()) log.warn(code + ": VML WordArt (CR-083 D9)");
		return drawn;
	}

	/**
	 * A VML group holding WordArt (D18): placed by its own style as a VML picture is, each WordArt and picture in it in
	 * a container at its box, mapped through the group's {@code coordorigin} and {@code coordsize} from its
	 * {@code left}, {@code top}, {@code width} and {@code height} (a group in the group mapped in turn); the group's
	 * other shapes, and a turn or flip of the group, reported ({@code render.shape.vml}), the group drawn unturned. A
	 * group with no WordArt is left as it was (null), for CR-080 D6's CR.
	 */
	static org.w3c.dom.DocumentFragment vmlGroup(AbstractWmlConversionContext context, org.docx4j.vml.CTGroup group) {
		try {
			Part part = context.getCurrentPart();
			Object root = root(part);
			List<Object[]> members = new ArrayList<Object[]>();
			boolean[] art = { false };
			java.util.Map<String, String> gp = FOTextBoxes.parseStyle(group.getStyle());
			double gw = FOTextBoxes.pts(gp.get("width"), 0), gh = FOTextBoxes.pts(gp.get("height"), 0);
			List<String> problems = new ArrayList<String>();
			members(group, new double[] { 0, 0, gw, gh }, root, members, art, problems, 0);
			if (!art[0]) return null;
			if (rotation(gp.get("rotation")) != 0 || gp.containsKey("flip")) problems.add("render.shape.vml");
			for (String code : problems) log.warn(code + ": in a VML group with WordArt (CR-083 D18)");
			Document doc = org.docx4j.XmlUtils.neww3cDomDocument();
			org.w3c.dom.DocumentFragment frag = doc.createDocumentFragment();
			if (!(gw > 0) || !(gh > 0)) return frag;
			org.docx4j.model.structure.PageDimensions pd = context.getSections().getCurrentSection().getPageDimensions();
			Element container = FOTextBoxes.isEnabled() && FOTextBoxes.isPositioned(gp)
					? FOTextBoxes.createVmlPictureContainer(doc, gp, "none", pd)
					: FOTextBoxes.createContainer(doc, "inline", gw, gh, 0, "p:0", 0, 0, new double[] { 0, 0, 0, 0 });
			for (Object[] m : members) {
				double[] box = (double[]) m[1];
				Element at = doc.createElementNS(XSL_FO, "fo:block-container");
				at.setAttribute("absolute-position", "absolute");
				at.setAttribute("left", pt(box[0]));
				at.setAttribute("top", pt(box[1]));
				at.setAttribute("width", pt(box[2]));
				at.setAttribute("height", pt(box[3]));
				at.setAttribute("overflow", "visible");
				if (m[0] instanceof org.docx4j.model.shapes.VmlShapes.Resolved) {
					org.docx4j.model.shapes.VmlShapes.Resolved r = (org.docx4j.model.shapes.VmlShapes.Resolved) m[0];
					org.docx4j.chart.render.api.WordArtRenderer.Drawn drawn = drawVml(context, r, box[2], box[3],
							FOTextBoxes.parseStyle(r.getStyle()));
					if (drawn == null || drawn.isEmpty()) continue;
					addWordArt(doc, at, drawn);
					at.appendChild(doc.createElementNS(XSL_FO, "fo:block"));
				} else {
					// a picture: through the picture route, at its box
					org.docx4j.vml.CTShape pic = org.docx4j.XmlUtils.deepCopy((org.docx4j.vml.CTShape) m[0]);
					pic.setStyle("width:" + FOTextBoxes.fmt(box[2]) + "pt;height:" + FOTextBoxes.fmt(box[3]) + "pt");
					org.docx4j.wml.Pict tmp = new org.docx4j.wml.Pict();
					tmp.getAnyAndAny().add(new org.docx4j.vml.ObjectFactory().createShape(pic));
					org.w3c.dom.DocumentFragment f = org.docx4j.model.images.WordXmlPictureE10.createXslFoImgE10(context, tmp);
					if (f == null) continue;
					Element block = doc.createElementNS(XSL_FO, "fo:block");
					block.setAttribute("font-size", "0.1pt");
					block.setAttribute("line-height", "0pt");
					for (org.w3c.dom.Node n = f.getFirstChild(); n != null; n = n.getNextSibling()) {
						org.w3c.dom.Node copy = doc.importNode(n, true);
						if (copy instanceof Element) {
							((Element) copy).setAttribute("content-width", pt(box[2]));
							((Element) copy).setAttribute("content-height", pt(box[3]));
						}
						block.appendChild(copy);
					}
					at.appendChild(block);
				}
				container.appendChild(at);
			}
			container.appendChild(doc.createElementNS(XSL_FO, "fo:block"));
			frag.appendChild(container);
			return frag;
		} catch (RuntimeException | OutOfMemoryError e) {
			log.warn("render.wordart.content: a VML group's WordArt couldn't be drawn: " + e, e);
			return null;
		}
	}

	/**
	 * A group's WordArt and pictures, each with its box in the outer group's points, from its parent's coordinates:
	 * {resolved WordArt or picture shape, {x, y, w, h}}
	 *
	 * @param frame the group's box in the outer group's points: {x, y, w, h}
	 */
	private static void members(org.docx4j.vml.CTGroup g, double[] frame, Object root, List<Object[]> out, boolean[] art,
			List<String> problems, int depth) {
		if (depth > 16) return;
		long[] origin = pair(g.getCoordorigin(), 0, 0), size = pair(g.getCoordsize(), 1000, 1000);
		double sx = frame[2] / (size[0] > 0 ? size[0] : 1000), sy = frame[3] / (size[1] > 0 ? size[1] : 1000);
		for (jakarta.xml.bind.JAXBElement<?> j : g.getPathOrFormulasOrHandles()) {
			Object u = j.getValue();
			if (!(u instanceof org.docx4j.vml.CTShape || u instanceof org.docx4j.vml.CTGroup)) {
				if (u instanceof org.docx4j.vml.VmlShapeElements && !(u instanceof org.docx4j.vml.CTShapetype)
						&& !problems.contains("render.shape.vml")) problems.add("render.shape.vml");
				continue;
			}
			String style = u instanceof org.docx4j.vml.CTShape ? ((org.docx4j.vml.CTShape) u).getStyle() : ((org.docx4j.vml.CTGroup) u).getStyle();
			java.util.Map<String, String> p = FOTextBoxes.parseStyle(style);
			double x = frame[0] + (number(p.get("left")) - origin[0]) * sx, y = frame[1] + (number(p.get("top")) - origin[1]) * sy;
			double[] box = { x, y, number(p.get("width")) * sx, number(p.get("height")) * sy };
			if (u instanceof org.docx4j.vml.CTGroup) {
				members((org.docx4j.vml.CTGroup) u, box, root, out, art, problems, depth + 1);
				continue;
			}
			org.docx4j.vml.CTShape shape = (org.docx4j.vml.CTShape) u;
			org.docx4j.model.shapes.VmlShapes.Resolved r = org.docx4j.model.shapes.VmlShapes.resolve(shape, root);
			if (r.isTextPath()) {
				out.add(new Object[] { r, box });
				art[0] = true;
			} else if (r.has(org.docx4j.vml.CTImageData.class)) {
				out.add(new Object[] { shape, box });
			} else if (!problems.contains("render.shape.vml")) {
				problems.add("render.shape.vml");
			}
		}
	}

	/** A group child's coordinate: a plain number in the group's units (or with a unit, read as points) */
	private static double number(String v) {
		if (v == null) return 0;
		try {
			return Double.parseDouble(v.trim());
		} catch (NumberFormatException e) {
			return FOTextBoxes.pts(v, 0);
		}
	}

	private static long[] pair(String s, long a, long b) {
		long[] out = { a, b };
		if (s == null) return out;
		String[] p = s.split(",", -1);
		for (int i = 0; i < 2 && i < p.length; i++) {
			try {
				if (!p[i].trim().isEmpty()) out[i] = Long.parseLong(p[i].trim());
			} catch (NumberFormatException e) {
				// as given
			}
		}
		return out;
	}

	/** A VML style's {@code rotation}: degrees clockwise, or fd ({@code 20643840fd}) */
	static double rotation(String v) {
		if (v == null || v.trim().isEmpty()) return 0;
		String t = v.trim().toLowerCase(Locale.ROOT);
		try {
			if (t.endsWith("fd")) return Double.parseDouble(t.substring(0, t.length() - 2)) / 65536;
			return Double.parseDouble(t);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/** The shape's {@code w10:wrap} type, or "none" */
	private static String wrapType(org.docx4j.vml.CTShape shape) {
		for (jakarta.xml.bind.JAXBElement<?> j : shape.getPathOrFormulasOrHandles()) {
			if (j.getValue() instanceof org.docx4j.vml.wordprocessingDrawing.CTWrap) {
				org.docx4j.vml.wordprocessingDrawing.CTWrap wr = (org.docx4j.vml.wordprocessingDrawing.CTWrap) j.getValue();
				if (wr.getType() != null) return wr.getType().value();
			}
		}
		return "none";
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
