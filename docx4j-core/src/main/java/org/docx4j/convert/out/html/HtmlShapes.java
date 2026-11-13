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
package org.docx4j.convert.out.html;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.model.shapes.ShapeGeometry;
import org.docx4j.model.shapes.ShapeOutlines;
import org.docx4j.model.shapes.ShapeText;
import org.docx4j.model.shapes.SvgIds;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A DrawingML shape (wps:wsp), group (wpg:wgp) or canvas (wpc:wpc) in the HTML output, by the visitor and the XSLT
 * alike (CR-080 phase 5, D5): a box of its extent, holding its drawing, the SVG docx4j-chart-render draws for the PDF
 * (through {@link ShapeDrawer}), and over it each shape's text where Word lays it out, in the geometry's text
 * rectangle, inset, anchored top, middle or foot. Until 17.3.7 the visitor wrote a shape's text as paragraphs in its
 * anchor's paragraph and drew nothing; the XSLT wrote nothing at all.
 *
 * <p>The box is a {@code span} with {@code display:inline-block}, in its line as a picture's {@code img} is, its
 * lengths in points, its size against the text (as a picture's {@code img} is since 17.3.7, its points as 4/3 as many
 * CSS pixels: CR-080 D14). The drawing is an {@code svg} of the box, a user unit a point, its overflow visible, so that what
 * it paints past the box (half its line, arrowheads, a callout's leader) shows. A shape's paragraphs are written as
 * {@code span}s with {@code display:block} ({@link #phrasing}): the box is inside its anchor's {@code p}, which an
 * HTML parser would end at a nested {@code p}, breaking the box. Its paragraph styles are class selectors, which a
 * {@code span} takes as a {@code p} does.</p>
 *
 * @since 17.3.7
 */
public final class HtmlShapes {

	private static final Logger log = LoggerFactory.getLogger(HtmlShapes.class);

	static final String SVG_NS = "http://www.w3.org/2000/svg";

	/** The class of a shape's box, and of the container of its text */
	public static final String BOX_CLASS = "docx4j-shape", TEXT_CLASS = "docx4j-shape-text";

	/** The least extent a drawing is given, in pt: a line has no height (or width), and an SVG of none paints nothing */
	private static final double LEAST_EXTENT_PT = 0.01;

	private HtmlShapes() {}

	/** A shape in HTML: its box, and each of its shapes with a text box with the element its text goes in */
	public static final class Box {
		public final Element element;
		/** Every shape with a text box, in document order ({@link ShapeText#textBoxes}) */
		public final List<CTWordprocessingShape> shapes;
		/** Each one's container, or null where Word shows no text (a line, a connector) or it has no box */
		public final List<Element> texts;

		Box(Element element, List<CTWordprocessingShape> shapes, List<Element> texts) {
			this.element = element;
			this.shapes = shapes;
			this.texts = texts;
		}
	}

	/** The shape, group or canvas a wp:anchor or wp:inline holds, or null */
	public static Object graphic(Object anchorOrInline) {
		org.docx4j.dml.Graphic g = anchorOrInline instanceof Anchor ? ((Anchor) anchorOrInline).getGraphic()
				: anchorOrInline instanceof Inline ? ((Inline) anchorOrInline).getGraphic() : null;
		if (g == null || g.getGraphicData() == null) return null;
		if (g.getGraphicData().getWordprocessingShape() != null) return g.getGraphicData().getWordprocessingShape();
		for (Object o : g.getGraphicData().getAny()) {
			Object v = o instanceof jakarta.xml.bind.JAXBElement ? ((jakarta.xml.bind.JAXBElement<?>) o).getValue() : o;
			if (v instanceof CTWordprocessingShape || v instanceof CTWordprocessingGroup || v instanceof CTWordprocessingCanvas) return v;
		}
		return null;
	}

	/**
	 * The shape's box, its drawing in it where a {@link ShapeDrawer} draws it, and an empty container for each shape's
	 * text, which the caller fills (and then passes to {@link #phrasing}). Null where it has no extent, or neither paints
	 * nor shows text nor takes space (anchored with no wrap).
	 */
	public static Box create(AbstractWmlConversionContext context, Object anchorOrInline, Document document) {
		Object graphic = graphic(anchorOrInline);
		org.docx4j.dml.CTPositiveSize2D extent = anchorOrInline instanceof Anchor ? ((Anchor) anchorOrInline).getExtent()
				: anchorOrInline instanceof Inline ? ((Inline) anchorOrInline).getExtent() : null;
		if (graphic == null || extent == null || extent.getCx() <= 0 && extent.getCy() <= 0) return null;
		double w = Math.max(LEAST_EXTENT_PT, extent.getCx() / 12700d);
		double h = Math.max(LEAST_EXTENT_PT, extent.getCy() / 12700d);

		// a text box drawn as WordArt: its box Word's, its text drawn (CR-083)
		if (graphic instanceof CTWordprocessingShape) {
			Box art = wordArt(context, (CTWordprocessingShape) graphic, extent.getCx(), extent.getCy(), document, anchorOrInline);
			if (art != null) return art;
		}

		ShapeDrawer.Drawing drawn = draw(context, graphic, extent.getCx(), extent.getCy());
		boolean paints = drawn != null && !drawn.isEmpty();

		// each shape with a text box, where Word shows its text: its box in the drawing's frame and its geometry
		List<CTWordprocessingShape> shapes = ShapeText.textBoxes(graphic);
		Map<Object, Rectangle2D> boxes = new IdentityHashMap<Object, Rectangle2D>();
		Map<Object, ShapeOutlines> outlines = new IdentityHashMap<Object, ShapeOutlines>();
		if (graphic instanceof CTWordprocessingShape) {
			boxes.put(graphic, new Rectangle2D.Double(0, 0, extent.getCx() / 12700d, extent.getCy() / 12700d));
			outlines.put(graphic, drawn != null ? drawn.getOutlines() : outlines((CTWordprocessingShape) graphic, extent.getCx(), extent.getCy()));
		} else if (drawn != null) {
			for (ShapeDrawer.Child c : drawn.getChildren()) {
				boxes.put(c.getChild(), c.getBox());
				outlines.put(c.getChild(), c.getOutlines());
			}
		}
		boolean text = false;
		for (CTWordprocessingShape wsp : shapes) {
			if (ShapeText.hasText(wsp) && boxes.containsKey(wsp)) text = true;
		}
		if (!paints && !text && anchorOrInline instanceof Anchor && takesNoSpace((Anchor) anchorOrInline)) return null;

		Element box = document.createElement("span");
		box.setAttribute("class", BOX_CLASS);
		box.setAttribute("style", "display:inline-block;position:relative;vertical-align:bottom;width:" + pt(w) + ";height:" + pt(h));
		if (!text) {
			box.setAttribute("role", "img");
			box.setAttribute("aria-label", label(anchorOrInline));
		}
		if (paints) {
			Element svg = (Element) document.importNode(drawn.toSvg().getDocumentElement(), true);
			svg.setAttribute("width", pt(w));
			svg.setAttribute("height", pt(h));
			// a user unit a point from the box's top left, as the drawing's frame is: what it paints past the box shows
			svg.setAttribute("viewBox", "0 0 " + num(w) + " " + num(h));
			svg.setAttribute("preserveAspectRatio", "none");
			svg.setAttribute("overflow", "visible");
			svg.setAttribute("style", "position:absolute;left:0;top:0;overflow:visible");
			svg.setAttribute("aria-hidden", "true");
			SvgIds.prefix(svg, idPrefix(context, anchorOrInline));
			box.appendChild(svg);
		}
		List<Element> texts = new ArrayList<Element>();
		for (CTWordprocessingShape wsp : shapes) {
			Rectangle2D b = boxes.get(wsp);
			if (b == null || !ShapeText.hasText(wsp)) {
				texts.add(null);
				continue;
			}
			Element t = textContainer(document, wsp, b, outlines.get(wsp));
			box.appendChild(t);
			texts.add(t);
		}
		return new Box(box, shapes, texts);
	}

	/**
	 * A w:pict or w:object holding VML WordArt (CR-083 phase 4), drawn: the SVG the PDF has, in its line at its box,
	 * where HTML writes a VML picture (it positions none). Not written from a header or footer (D17).
	 *
	 * @return the fragment, empty where nothing is written (a header's or footer's, or one Word draws nothing of); null
	 *         where it isn't VML WordArt, for the picture route
	 * @since 17.3.7
	 */
	public static org.w3c.dom.DocumentFragment vmlWordArt(AbstractWmlConversionContext context, Object pictOrIterator) {
		org.docx4j.wml.CTPictureBase pict = null;
		if (pictOrIterator instanceof org.docx4j.wml.CTPictureBase) {
			pict = (org.docx4j.wml.CTPictureBase) pictOrIterator;
		} else if (pictOrIterator instanceof org.w3c.dom.traversal.NodeIterator) {
			org.w3c.dom.Node n = ((org.w3c.dom.traversal.NodeIterator) pictOrIterator).nextNode();
			if (n == null) return null;
			try {
				Object jaxb = org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshal(n, org.docx4j.jaxb.Context.jc,
						"object".equals(n.getLocalName()) ? org.docx4j.wml.CTObject.class : org.docx4j.wml.Pict.class));
				if (jaxb instanceof org.docx4j.wml.CTPictureBase) pict = (org.docx4j.wml.CTPictureBase) jaxb;
			} catch (Exception e) {
				log.warn("VML WordArt couldn't be read: " + e.getMessage());
				return null;
			}
		}
		if (pict == null || context == null || context.getWmlPackage() == null) return null;
		org.docx4j.vml.CTShape shape = null;
		for (Object o : pict.getAnyAndAny()) {
			Object u = org.docx4j.XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.vml.CTShape) {
				shape = (org.docx4j.vml.CTShape) u;
				break;
			}
		}
		if (shape == null) return null;
		Document document = org.docx4j.XmlUtils.neww3cDomDocument();
		org.w3c.dom.DocumentFragment frag = document.createDocumentFragment();
		try {
			Part part = context.getCurrentPart();
			Object root = part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart
					? ((org.docx4j.openpackaging.parts.JaxbXmlPart<?>) part).getJaxbElement() : null;
			org.docx4j.model.shapes.VmlShapes.Resolved r = org.docx4j.model.shapes.VmlShapes.resolve(shape, root, context.getVmlShapetypes());
			if (!r.isTextPath()) return null;
			// D17: VML WordArt in a header or footer isn't written in HTML
			if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart
					|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FooterPart) return frag;
			ShapeDrawer drawer = ShapeDrawer.getDrawer();
			org.docx4j.model.shapes.WordArtText art = org.docx4j.model.shapes.VmlWordArt.of(r, context.getWmlPackage());
			if (drawer == null || art == null) return frag;
			java.util.Map<String, String> props = style(r.getStyle());
			double w = org.docx4j.model.shapes.VmlShapes.length(props.get("width"), 0);
			double h = org.docx4j.model.shapes.VmlShapes.length(props.get("height"), 0);
			String flip = props.getOrDefault("flip", "").toLowerCase(java.util.Locale.ROOT);
			double rotation = 0;
			try {
				String rot = props.getOrDefault("rotation", "0").trim().toLowerCase(java.util.Locale.ROOT);
				rotation = rot.endsWith("fd") ? Double.parseDouble(rot.substring(0, rot.length() - 2)) / 65536 : Double.parseDouble(rot);
			} catch (NumberFormatException e) {
				rotation = 0;
			}
			ShapeOutlines geometry = org.docx4j.model.shapes.VmlGeometry.outlines(r.definition(), w, h);
			ShapeDrawer.WordArtDrawing d = drawer.drawVmlWordArt(art, geometry, part, ThemeColors.of(context.getWmlPackage()), w, h,
					rotation, flip.contains("x"), flip.contains("y"), true);
			if (d == null) return frag;
			for (String code : d.getProblems()) log.warn(code + ": VML WordArt in HTML (CR-083 D9)");
			Element box = document.createElement("span");
			box.setAttribute("class", BOX_CLASS);
			box.setAttribute("style", "display:inline-block;position:relative;vertical-align:bottom;width:" + pt(w) + ";height:" + pt(h));
			box.setAttribute("role", "img");
			box.setAttribute("aria-label", d.getText() == null ? "" : d.getText());
			Element svg = (Element) document.importNode(d.toSvg().getDocumentElement(), true);
			svg.setAttribute("width", pt(w));
			svg.setAttribute("height", pt(h));
			svg.setAttribute("viewBox", "0 0 " + num(w) + " " + num(h));
			svg.setAttribute("preserveAspectRatio", "none");
			svg.setAttribute("overflow", "visible");
			svg.setAttribute("style", "position:absolute;left:0;top:0;overflow:visible");
			svg.setAttribute("aria-hidden", "true");
			SvgIds.prefix(svg, "vml-" + Integer.toHexString(System.identityHashCode(shape)) + "-");
			box.appendChild(svg);
			frag.appendChild(box);
			return frag;
		} catch (RuntimeException | OutOfMemoryError e) {
			log.warn("render.wordart.content: VML WordArt couldn't be drawn in HTML: " + e, e);
			return frag;
		}
	}

	/** For the XSLT's w:pict template, which has found a text path: the drawing, or nothing */
	public static org.w3c.dom.DocumentFragment vmlWordArtXslt(org.docx4j.convert.out.html.HTMLConversionContext context,
			org.w3c.dom.traversal.NodeIterator pict) {
		org.w3c.dom.DocumentFragment frag = vmlWordArt(context, pict);
		return frag != null ? frag : org.docx4j.XmlUtils.neww3cDomDocument().createDocumentFragment();
	}

	/** A style attribute's properties */
	private static java.util.Map<String, String> style(String css) {
		java.util.Map<String, String> out = new java.util.LinkedHashMap<String, String>();
		if (css == null) return out;
		for (String decl : css.split(";")) {
			int colon = decl.indexOf(':');
			if (colon > 0) out.put(decl.substring(0, colon).trim().toLowerCase(java.util.Locale.ROOT), decl.substring(colon + 1).trim());
		}
		return out;
	}

	/**
	 * A text box drawn as WordArt (CR-083 phase 1): a box of the size Word draws ({@code wrap="none"}: as wide as its
	 * text; {@code a:spAutoFit}: as tall), holding the SVG the PDF has, its text in it real or invisible as Word's PDF has
	 * it; no text container, the text being in the drawing. Null where it isn't WordArt or can't be drawn: then it is a
	 * text box as any other.
	 */
	private static Box wordArt(AbstractWmlConversionContext context, CTWordprocessingShape wsp, long cx, long cy, Document document,
			Object anchorOrInline) {
		ShapeDrawer drawer = ShapeDrawer.getDrawer();
		if (drawer == null || context == null || context.getWmlPackage() == null) return null;
		try {
			org.docx4j.model.shapes.WordArtText art = org.docx4j.model.shapes.WordArtText.of(wsp, context.getWmlPackage());
			if (art == null) return null;
			ThemeColors theme = ThemeColors.of(context.getWmlPackage());
			final Part host = context.getCurrentPart();
			ShapeDrawer.WordArtDrawing d = drawer.drawWordArt(art, host, theme, cx, cy, relId -> picture(host, relId));
			if (d == null) return null;
			for (String code : d.getProblems()) log.warn(code + ": WordArt in HTML (CR-083 D9)");
			double w = Math.max(LEAST_EXTENT_PT, d.getWidth()), h = Math.max(LEAST_EXTENT_PT, d.getHeight());
			Element box = document.createElement("span");
			box.setAttribute("class", BOX_CLASS);
			box.setAttribute("style", "display:inline-block;position:relative;vertical-align:bottom;width:" + pt(w) + ";height:" + pt(h));
			box.setAttribute("role", "img");
			box.setAttribute("aria-label", d.getText() == null || d.getText().isEmpty() ? label(anchorOrInline) : d.getText());
			Element svg = (Element) document.importNode(d.toSvg().getDocumentElement(), true);
			svg.setAttribute("width", pt(w));
			svg.setAttribute("height", pt(h));
			svg.setAttribute("viewBox", "0 0 " + num(w) + " " + num(h));
			svg.setAttribute("preserveAspectRatio", "none");
			svg.setAttribute("overflow", "visible");
			svg.setAttribute("style", "position:absolute;left:0;top:0;overflow:visible");
			svg.setAttribute("aria-hidden", "true");
			SvgIds.prefix(svg, idPrefix(context, anchorOrInline));
			box.appendChild(svg);
			List<CTWordprocessingShape> shapes = new ArrayList<CTWordprocessingShape>();
			shapes.add(wsp);
			List<Element> texts = new ArrayList<Element>();
			texts.add(null);
			return new Box(box, shapes, texts);
		} catch (RuntimeException | OutOfMemoryError e) {
			// out of memory too: the drawing is let go and the text box stays text, as a picture's adjustments are (CR-084)
			log.warn("render.wordart.content: WordArt couldn't be drawn in HTML, left as text: " + e, e);
			return null;
		}
	}

	/**
	 * A shape's text: positioned at its box in the shape's box, set in by its text rectangle and its insets with half
	 * its line, anchored as its bodyPr says.  Turned shapes' text stays level, as in the PDF (CR-080 D3).
	 */
	private static Element textContainer(Document document, CTWordprocessingShape wsp, Rectangle2D b, ShapeOutlines o) {
		double[] in = ShapeText.textInsets(ShapeText.withLine(ShapeText.bodyInsets(wsp), ShapeText.linePt(wsp)), o, wsp,
				b.getWidth(), b.getHeight());
		if (in == null) in = ShapeText.DEFAULT_INSETS;
		String anchor = ShapeText.anchor(wsp);
		Element t = document.createElement("span");
		t.setAttribute("class", TEXT_CLASS);
		t.setAttribute("style", "position:absolute;box-sizing:border-box;display:flex;flex-direction:column;overflow:visible"
				+ ";left:" + pt(b.getX()) + ";top:" + pt(b.getY()) + ";width:" + pt(b.getWidth()) + ";height:" + pt(b.getHeight())
				+ ";padding:" + pt(in[1]) + " " + pt(in[2]) + " " + pt(in[3]) + " " + pt(in[0])
				+ ";justify-content:" + ("ctr".equals(anchor) ? "center" : "b".equals(anchor) ? "flex-end" : "flex-start"));
		return t;
	}

	/**
	 * A shape's converted text as phrasing content: each {@code p} (and {@code div}) under the container becomes a
	 * {@code span} with {@code display:block}, its attributes kept, so that the box stays whole inside its anchor's
	 * {@code p} when an HTML parser reads it.
	 */
	public static void phrasing(Element container) {
		List<Element> blocks = new ArrayList<Element>();
		collectBlocks(container, blocks);
		for (Element e : blocks) {
			Element span = e.getOwnerDocument().createElementNS(e.getNamespaceURI(), e.getPrefix() == null ? "span" : e.getPrefix() + ":span");
			for (int i = 0; i < e.getAttributes().getLength(); i++) {
				org.w3c.dom.Attr a = (org.w3c.dom.Attr) e.getAttributes().item(i);
				span.setAttributeNS(a.getNamespaceURI(), a.getName(), a.getValue());
			}
			String style = span.getAttribute("style");
			span.setAttribute("style", "display:block;" + style);
			while (e.getFirstChild() != null) span.appendChild(e.getFirstChild());
			e.getParentNode().replaceChild(span, e);
		}
	}

	private static void collectBlocks(Node n, List<Element> out) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (!(c instanceof Element)) continue;
			String name = c.getLocalName() == null ? c.getNodeName() : c.getLocalName();
			if ("table".equals(name)) continue; // a table in a text box stays one (rare)
			if ("p".equals(name) || "div".equals(name)) out.add((Element) c);
			collectBlocks(c, out);
		}
	}

	private static ShapeDrawer.Drawing draw(AbstractWmlConversionContext context, Object graphic, long cx, long cy) {
		ShapeDrawer drawer = ShapeDrawer.getDrawer();
		if (drawer == null) return null;
		try {
			ThemeColors theme = ThemeColors.of(context.getWmlPackage());
			final Part host = context.getCurrentPart();
			ShapeDrawer.Drawing d = drawer.draw(graphic, theme, cx, cy, relId -> picture(host, relId));
			if (d != null) {
				for (String code : d.getProblems()) log.warn(code + ": a DrawingML shape in HTML (CR-080 D9)");
			}
			return d;
		} catch (RuntimeException e) {
			log.warn("render.shape.bad_geometry: a DrawingML shape couldn't be drawn in HTML: " + e, e);
			return null;
		}
	}

	/** A shape's geometry with no drawer to hand, for its text rectangle; null where it can't be had */
	private static ShapeOutlines outlines(CTWordprocessingShape wsp, long cx, long cy) {
		try {
			return wsp.getSpPr() == null ? null : ShapeGeometry.outlines(wsp.getSpPr(), cx, cy);
		} catch (RuntimeException e) {
			return null;
		}
	}

	/** A picture fill's picture, by its relationship from the part being converted; null where ImageIO can't read it */
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

	private static boolean takesNoSpace(Anchor a) {
		return a.getWrapSquare() == null && a.getWrapTight() == null && a.getWrapThrough() == null && a.getWrapTopAndBottom() == null;
	}

	/** The shape's alternative text: its docPr's description, else its name, else "Shape" */
	private static String label(Object anchorOrInline) {
		org.docx4j.dml.CTNonVisualDrawingProps pr = anchorOrInline instanceof Anchor ? ((Anchor) anchorOrInline).getDocPr()
				: anchorOrInline instanceof Inline ? ((Inline) anchorOrInline).getDocPr() : null;
		if (pr != null && pr.getDescr() != null && !pr.getDescr().trim().isEmpty()) return pr.getDescr().trim();
		if (pr != null && pr.getName() != null && !pr.getName().trim().isEmpty()) return pr.getName().trim();
		return "Shape";
	}

	/** A prefix unique to this drawing in the document: its part's name and its docPr id */
	private static String idPrefix(AbstractWmlConversionContext context, Object anchorOrInline) {
		org.docx4j.dml.CTNonVisualDrawingProps pr = anchorOrInline instanceof Anchor ? ((Anchor) anchorOrInline).getDocPr()
				: anchorOrInline instanceof Inline ? ((Inline) anchorOrInline).getDocPr() : null;
		String part = "";
		if (context != null && context.getCurrentPart() != null) {
			String n = context.getCurrentPart().getPartName().getName();
			part = n.substring(n.lastIndexOf('/') + 1).replace(".xml", "");
		}
		return ("docx4j-shape-" + part + "-" + (pr == null ? "0" : Long.toString(pr.getId())) + "-").replaceAll("[^A-Za-z0-9_-]", "_");
	}

	private static String pt(double v) {
		return num(v) + "pt";
	}

	private static String num(double v) {
		String s = String.format(Locale.ROOT, "%.2f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return "-0".equals(s) ? "0" : s;
	}
}
