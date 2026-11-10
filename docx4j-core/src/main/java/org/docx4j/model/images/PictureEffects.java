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
package org.docx4j.model.images;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.STShapeType;
import org.docx4j.dml.picture.Pic;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.model.shapes.SvgIds;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A DrawingML picture in its frame, with its effects, drawn as Word draws them (CR-081 phases 1 and 2), for the PDF and
 * HTML outputs alike: one SVG of the picture's frame, a user unit a point, its overflow visible, holding the effect
 * pictures under the picture ({@code pic:spPr/a:effectLst}: outer shadow, glow, reflection), the fill behind it, the
 * picture itself (an {@code image} of what the output names for it, so a JPEG passes through to the PDF), both clipped
 * to its outline ({@code a:prstGeom}, {@code a:custGeom}), the pictures over it (an inner shadow), then its line
 * ({@code a:ln}), wholly outside it; where there is a soft edge, the soft-edged picture in place of the fill, the
 * picture and the line. The frame and the effects are drawn by the {@link ShapeDrawer} on the classpath
 * (docx4j-chart-render's); without one the picture is output as it always was. Where the picture's pixels can't be
 * read (a metafile, an unreadable bitmap), its effects aren't drawn.
 *
 * <p>An inline picture's effects are cut at its line's top and bottom, which its effect extent sets, as Word cuts
 * them (phase 0 findings §3; Word's line also has the text's descent under the picture, which this leaves out); an
 * anchored picture's aren't cut.</p>
 *
 * @since 17.3.7
 */
final class PictureEffects {

	private static final Logger log = LoggerFactory.getLogger(PictureEffects.class);

	/** An upper bound on an effect picture's pixels per point (CR-081 D3); unset, Word's rule */
	static final String PIXELS_PER_POINT_PROPERTY = "docx4j.convert.out.effects.pixelsPerPoint";
	/** The most pixels a picture is read at to draw its effects (a bitmap claiming more is read reduced, or not) */
	static final long MAX_READ_PIXELS = 64_000_000L;

	static final String SVG_NS = "http://www.w3.org/2000/svg";
	static final String XLINK_NS = "http://www.w3.org/1999/xlink";

	private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

	private PictureEffects() {}

	/** The picture's effect list, where it has an effect Word draws; else null */
	static CTEffectList effects(Pic pic) {
		if (pic == null || pic.getSpPr() == null) return null;
		CTEffectList fx = pic.getSpPr().getEffectLst();
		if (fx == null) return null;
		boolean any = fx.getOuterShdw() != null || fx.getInnerShdw() != null || fx.getGlow() != null && fx.getGlow().getRad() > 0
				|| fx.getSoftEdge() != null && fx.getSoftEdge().getRad() > 0 || fx.getReflection() != null;
		return any ? fx : null;
	}

	/**
	 * Whether the picture is more than its bitmap in its box, as Word draws it: effects, an outline that isn't its box
	 * (a preset other than rect, a custom geometry), or a line (CR-081 phase 2). A fill behind it shows only through a
	 * transparent bitmap: see {@link #fillBehind(Pic)}.
	 */
	static boolean framed(Pic pic) {
		if (pic == null || pic.getSpPr() == null) return false;
		CTShapeProperties sp = pic.getSpPr();
		if (effects(pic) != null) return true;
		if (sp.getCustGeom() != null) return true;
		if (sp.getPrstGeom() != null && sp.getPrstGeom().getPrst() != null && sp.getPrstGeom().getPrst() != STShapeType.RECT) return true;
		CTLineProperties ln = sp.getLn();
		return ln != null && ln.getNoFill() == null && (ln.getSolidFill() != null || ln.getGradFill() != null || ln.getPattFill() != null);
	}

	/** Whether the picture has a fill behind it ({@code a:solidFill}, {@code a:gradFill}, {@code a:pattFill} in its spPr) */
	static boolean fillBehind(Pic pic) {
		if (pic == null || pic.getSpPr() == null) return false;
		CTShapeProperties sp = pic.getSpPr();
		return sp.getNoFill() == null && (sp.getSolidFill() != null || sp.getGradFill() != null || sp.getPattFill() != null);
	}

	/**
	 * The picture in its frame with its effects as an SVG element in document, or null where it is drawn as its bitmap
	 * alone (no drawer; nothing round it, clipped or drawn over it).
	 *
	 * @param href the picture as the output refers to it; null for a data: URI of the part's own bytes (the PDF:
	 *        the FOP fork admits only data: resources in an SVG)
	 * @param cropped href's bitmap is cut to the crop already (the FO image handler's), so it is drawn whole in the
	 *        crop's frame; false for the stored bitmap, which the crop cuts here
	 * @param clip how far beyond the frame's top and bottom the effects may reach, in points; null for no cut
	 * @param ids the prefix of the SVG's ids (unique in an HTML page)
	 */
	static Element svg(Document document, WordprocessingMLPackage pkg, Pic pic, double w, double h, BinaryPart part,
			PictureCrop crop, String href, boolean cropped, double[] clip, String ids) {
		ShapeDrawer drawer = ShapeDrawer.getDrawer();
		if (drawer == null || pic == null || pic.getSpPr() == null || part == null || !(w > 0) || !(h > 0)) return null;
		CTEffectList fx = effects(pic);
		boolean framed = framed(pic);
		if (!framed && !fillBehind(pic)) return null;
		byte[] bytes;
		try {
			bytes = part.getBytes();
		} catch (Exception e) {
			return null;
		}
		// a fill behind an opaque bitmap doesn't show
		if (!framed && !hasAlpha(bytes)) return null;
		// the pixels, where there are effects to draw from them
		BufferedImage picture = null;
		if (fx != null) {
			picture = read(bytes, w, h, crop);
			if (picture == null) report("render.effect.picture_unread", part.getPartName() == null ? "" : part.getPartName().getName());
		}
		ThemeColors theme = null;
		try {
			theme = pkg == null ? null : ThemeColors.of(pkg);
		} catch (Exception e) {
			log.debug("No theme colours: " + e);
		}
		double ppt = 0;
		String bound = Docx4jProperties.getProperty(PIXELS_PER_POINT_PROPERTY);
		if (bound != null && !bound.trim().isEmpty()) {
			try {
				ppt = Double.parseDouble(bound.trim());
			} catch (NumberFormatException e) {
				log.warn(PIXELS_PER_POINT_PROPERTY + " is not a number: " + bound);
			}
		}
		List<String> problems = new ArrayList<String>();
		ShapeDrawer.PictureDrawing drawing;
		try {
			drawing = drawer.drawPicture(pic.getSpPr(), theme, w, h, picture, crop, ppt, problems);
		} catch (RuntimeException e) {
			log.warn("A picture's frame and effects couldn't be drawn: " + e, e);
			return null;
		}
		for (String code : problems) report(code, "");
		if (drawing == null || drawing.isPlain()) return null;
		String prefix = ids == null ? "" : ids;

		Element svg = document.createElementNS(SVG_NS, "svg");
		svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", XLINK_NS);
		svg.setAttribute("viewBox", "0 0 " + num(w) + " " + num(h));
		svg.setAttribute("preserveAspectRatio", "none");
		svg.setAttribute("overflow", "visible");
		boolean replaced = false;
		for (ShapeDrawer.EffectPicture p : drawing.getEffects()) if (p.getLayer() == ShapeDrawer.EffectLayer.REPLACE) replaced = true;
		for (ShapeDrawer.EffectLayer layer : new ShapeDrawer.EffectLayer[] { ShapeDrawer.EffectLayer.UNDER, ShapeDrawer.EffectLayer.REPLACE,
				ShapeDrawer.EffectLayer.OVER }) {
			if (layer == ShapeDrawer.EffectLayer.REPLACE && !replaced) {
				// the fill behind, then the picture, in the outline
				Element behind = nested(document, drawing.getBehind(), prefix + "b-");
				if (behind != null) svg.appendChild(behind);
				Element image = picture(document, href != null ? href : dataUri(part, bytes), w, h, crop, cropped);
				Shape outline = drawing.getClip();
				if (outline != null) {
					String path = pathData(outline);
					if (path != null) {
						Element defs = document.createElementNS(SVG_NS, "defs");
						Element cp = document.createElementNS(SVG_NS, "clipPath");
						cp.setAttribute("id", prefix + "outline");
						Element pe = document.createElementNS(SVG_NS, "path");
						pe.setAttribute("d", path);
						if (outline.getPathIterator(null).getWindingRule() == PathIterator.WIND_EVEN_ODD) pe.setAttribute("clip-rule", "evenodd");
						cp.appendChild(pe);
						defs.appendChild(cp);
						svg.appendChild(defs);
						Element g = document.createElementNS(SVG_NS, "g");
						g.setAttribute("clip-path", "url(#" + prefix + "outline)");
						g.appendChild(image);
						image = g;
					}
				}
				svg.appendChild(image);
				continue;
			}
			for (ShapeDrawer.EffectPicture p : drawing.getEffects()) {
				if (p.getLayer() != layer) continue;
				Element e = effect(document, p, layer == ShapeDrawer.EffectLayer.REPLACE ? null : clip, h);
				if (e != null) svg.appendChild(e);
			}
		}
		if (!replaced) {
			Element line = nested(document, drawing.getLine(), prefix + "l-");
			if (line != null) svg.appendChild(line);
		}
		return svg;
	}

	/**
	 * A drawer's SVG (a user unit a point, its viewBox where it is in the frame) as an svg in this one, drawn where its
	 * viewBox puts it, its overflow visible, its ids prefixed (two drawers' SVGs number theirs alike)
	 */
	private static Element nested(Document document, Document drawn, String prefix) {
		if (drawn == null || drawn.getDocumentElement() == null) return null;
		Element e = (Element) document.importNode(drawn.getDocumentElement(), true);
		String[] vb = e.getAttribute("viewBox").trim().split("[\\s,]+");
		if (vb.length != 4) return null;
		e.setAttribute("x", vb[0]);
		e.setAttribute("y", vb[1]);
		e.setAttribute("width", vb[2]);
		e.setAttribute("height", vb[3]);
		e.setAttribute("overflow", "visible");
		SvgIds.prefix(e, prefix);
		return e;
	}

	/** The shape as SVG path data, in points; null where it has no segments */
	static String pathData(Shape shape) {
		StringBuilder b = new StringBuilder();
		double[] c = new double[6];
		for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
			switch (it.currentSegment(c)) {
				case PathIterator.SEG_MOVETO: b.append('M').append(num(c[0])).append(' ').append(num(c[1])); break;
				case PathIterator.SEG_LINETO: b.append('L').append(num(c[0])).append(' ').append(num(c[1])); break;
				case PathIterator.SEG_QUADTO: b.append('Q').append(num(c[0])).append(' ').append(num(c[1])).append(' ').append(num(c[2])).append(' ')
						.append(num(c[3])); break;
				case PathIterator.SEG_CUBICTO: b.append('C').append(num(c[0])).append(' ').append(num(c[1])).append(' ').append(num(c[2])).append(' ')
						.append(num(c[3])).append(' ').append(num(c[4])).append(' ').append(num(c[5])); break;
				case PathIterator.SEG_CLOSE: b.append('Z'); break;
				default: break;
			}
		}
		return b.length() == 0 ? null : b.toString();
	}

	/** Whether the bitmap's first image has alpha (its header's colour model); false where it can't be told */
	static boolean hasAlpha(byte[] bytes) {
		if (bytes == null || bytes.length == 0) return false;
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			if (in == null) return false;
			Iterator<ImageReader> it = ImageIO.getImageReaders(in);
			if (!it.hasNext()) return false;
			ImageReader reader = it.next();
			try {
				reader.setInput(in, true, true);
				Iterator<javax.imageio.ImageTypeSpecifier> types = reader.getImageTypes(0);
				return types.hasNext() && types.next().getColorModel().hasAlpha();
			} finally {
				reader.dispose();
			}
		} catch (Exception | OutOfMemoryError e) {
			log.debug("The picture's header wasn't read: " + e);
			return false;
		}
	}

	/** The picture in its frame: href's bitmap, the crop's part of it stretched to the frame */
	private static Element picture(Document document, String href, double w, double h, PictureCrop crop, boolean cropped) {
		PictureCrop c = crop == null ? PictureCrop.NONE : crop;
		if (cropped || c.isNone()) {
			// drawn whole: in the frame, or where a crop that moves an edge out leaves it (CR-029's padding)
			double[] pad = cropped && c.pads() ? c.framePadding() : new double[4];
			Element image = image(document, href, w * pad[0], h * pad[1], w * (1 - pad[0] - pad[2]), h * (1 - pad[1] - pad[3]));
			return image;
		}
		// the stored bitmap as a unit square, the crop's part of it in view, stretched to the frame
		Element view = document.createElementNS(SVG_NS, "svg");
		view.setAttribute("x", "0");
		view.setAttribute("y", "0");
		view.setAttribute("width", num(w));
		view.setAttribute("height", num(h));
		view.setAttribute("viewBox", num(c.left) + " " + num(c.top) + " " + num(1 - c.left - c.right) + " " + num(1 - c.top - c.bottom));
		view.setAttribute("preserveAspectRatio", "none");
		view.appendChild(image(document, href, 0, 0, 1, 1));
		return view;
	}

	/** An effect picture where it goes, cut at the clip's top and bottom; null where nothing of it is left */
	private static Element effect(Document document, ShapeDrawer.EffectPicture p, double[] clip, double h) {
		BufferedImage im = p.getImage();
		Rectangle2D place = p.getPlace();
		if (clip != null) {
			double top = -clip[0], bottom = h + clip[1];
			double pph = im.getHeight() / place.getHeight();
			int r0 = (int) Math.max(0, Math.floor((top - place.getY()) * pph));
			int r1 = (int) Math.min(im.getHeight(), Math.ceil((bottom - place.getY()) * pph));
			if (r1 <= r0) return null;
			if (r0 > 0 || r1 < im.getHeight()) {
				im = im.getSubimage(0, r0, im.getWidth(), r1 - r0);
				place = new Rectangle2D.Double(place.getX(), place.getY() + r0 / pph, place.getWidth(), (r1 - r0) / pph);
			}
		}
		String uri = png(im);
		if (uri == null) return null;
		return image(document, uri, place.getX(), place.getY(), place.getWidth(), place.getHeight());
	}

	private static Element image(Document document, String href, double x, double y, double w, double h) {
		Element image = document.createElementNS(SVG_NS, "image");
		image.setAttribute("x", num(x));
		image.setAttribute("y", num(y));
		image.setAttribute("width", num(w));
		image.setAttribute("height", num(h));
		image.setAttribute("preserveAspectRatio", "none");
		image.setAttributeNS(XLINK_NS, "xlink:href", href);
		return image;
	}

	/**
	 * The bitmap, read at no more pixels than the effects need: 200 dpi of the frame twice over, through the crop (a
	 * large photograph is read reduced); null where it can't be read
	 */
	static BufferedImage read(byte[] bytes, double w, double h, PictureCrop crop) {
		if (bytes == null || bytes.length == 0) return null;
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			if (in == null) return null;
			Iterator<ImageReader> it = ImageIO.getImageReaders(in);
			if (!it.hasNext()) return null;
			ImageReader reader = it.next();
			try {
				reader.setInput(in, true, true);
				int pw = reader.getWidth(0), ph = reader.getHeight(0);
				if (pw <= 0 || ph <= 0) return null;
				PictureCrop c = crop == null ? PictureCrop.NONE : crop;
				double fw = Math.max(0.01, 1 - Math.max(0, c.left) - Math.max(0, c.right));
				double fh = Math.max(0.01, 1 - Math.max(0, c.top) - Math.max(0, c.bottom));
				double needW = w * 400 / 72 / fw, needH = h * 400 / 72 / fh;
				int step = (int) Math.max(1, Math.floor(Math.min(pw / needW, ph / needH)));
				while ((long) (pw / step) * (ph / step) > MAX_READ_PIXELS) step++;
				ImageReadParam param = reader.getDefaultReadParam();
				if (step > 1) param.setSourceSubsampling(step, step, 0, 0);
				return reader.read(0, param);
			} finally {
				reader.dispose();
			}
		} catch (Exception | OutOfMemoryError e) {
			log.debug("Picture not read for its effects: " + e);
			return null;
		}
	}

	/**
	 * A picture the image handler wrote, as a data: URI: src itself where it is one, else the file it names (a file:
	 * URI or a path), its type from its bytes (PNG, JPEG, GIF, BMP); null where it can't be read
	 */
	static String dataUri(String src) {
		if (src == null || src.isEmpty()) return null;
		if (src.startsWith("data:")) return src;
		try {
			java.nio.file.Path path = src.startsWith("file:") ? java.nio.file.Paths.get(new java.net.URI(src)) : java.nio.file.Paths.get(src);
			if (!java.nio.file.Files.isRegularFile(path) || java.nio.file.Files.size(path) > 256L * 1024 * 1024) return null;
			byte[] b = java.nio.file.Files.readAllBytes(path);
			String type = b.length > 3 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G' ? "image/png"
					: b.length > 2 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 ? "image/jpeg"
					: b.length > 3 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' ? "image/gif"
					: b.length > 1 && b[0] == 'B' && b[1] == 'M' ? "image/bmp" : null;
			return type == null ? null : "data:" + type + ";base64," + Base64.getEncoder().encodeToString(b);
		} catch (Exception e) {
			log.debug("The picture's file wasn't read: " + e);
			return null;
		}
	}

	private static String dataUri(BinaryPart part, byte[] bytes) {
		String type = part.getContentType();
		if (type == null || type.isEmpty()) type = "application/octet-stream";
		return "data:" + type + ";base64," + Base64.getEncoder().encodeToString(bytes);
	}

	private static String png(BufferedImage im) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (!ImageIO.write(im, "png", out)) return null;
			return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (Exception e) {
			log.warn("An effect picture couldn't be written: " + e);
			return null;
		}
	}

	private static void report(String code, String detail) {
		if (REPORTED.add(code)) log.warn(code + (detail.isEmpty() ? "" : ": " + detail) + " (a picture's effects aren't all drawn; further ones not logged)");
	}

	static String num(double v) {
		String s = String.format(Locale.ROOT, "%.4f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
		return s.equals("-0") ? "0" : s;
	}
}
