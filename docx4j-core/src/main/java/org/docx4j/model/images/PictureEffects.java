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
import org.docx4j.dml.picture.Pic;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A DrawingML picture with its effects ({@code pic:spPr/a:effectLst}: outer and inner shadow, glow, soft edge,
 * reflection), drawn as Word draws them (CR-081 phase 1), for the PDF and HTML outputs alike: one SVG of the picture's
 * frame, a user unit a point, its overflow visible, holding the effect pictures under the picture, the picture itself
 * (an {@code image} of what the output names for it, so a JPEG passes through to the PDF; the soft-edged picture in its
 * place where there is a soft edge) and the pictures over it. The effects are drawn by the {@link ShapeDrawer} on the classpath
 * (docx4j-chart-render's); without one, or where the picture's pixels can't be read (a metafile, an unreadable
 * bitmap), the picture is output as it always was, with no effects.
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
	 * The picture with its effects as an SVG element in document, or null where they aren't drawn (no drawer, no
	 * pixels, nothing to draw).
	 *
	 * @param href the picture as the output refers to it; null for a data: URI of the part's own bytes (the PDF:
	 *        the FOP fork admits only data: resources in an SVG)
	 * @param cropped href's bitmap is cut to the crop already (the FO image handler's), so it is drawn whole in the
	 *        crop's frame; false for the stored bitmap, which the crop cuts here
	 * @param clip how far beyond the frame's top and bottom the effects may reach, in points; null for no cut
	 */
	static Element svg(Document document, WordprocessingMLPackage pkg, CTEffectList fx, double w, double h, BinaryPart part,
			PictureCrop crop, String href, boolean cropped, double[] clip) {
		ShapeDrawer drawer = ShapeDrawer.getDrawer();
		if (drawer == null || fx == null || part == null || !(w > 0) || !(h > 0)) return null;
		byte[] bytes;
		try {
			bytes = part.getBytes();
		} catch (Exception e) {
			return null;
		}
		BufferedImage picture = read(bytes, w, h, crop);
		if (picture == null) {
			report("render.effect.picture_unread", part.getPartName() == null ? "" : part.getPartName().getName());
			return null;
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
		List<ShapeDrawer.EffectPicture> pictures;
		try {
			pictures = drawer.drawEffects(fx, theme, w, h, picture, crop, ppt, problems);
		} catch (RuntimeException e) {
			log.warn("A picture's effects couldn't be drawn: " + e, e);
			return null;
		}
		for (String code : problems) report(code, "");
		if (pictures == null || pictures.isEmpty()) return null;

		Element svg = document.createElementNS(SVG_NS, "svg");
		svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", XLINK_NS);
		svg.setAttribute("viewBox", "0 0 " + num(w) + " " + num(h));
		svg.setAttribute("preserveAspectRatio", "none");
		svg.setAttribute("overflow", "visible");
		boolean replaced = false;
		for (ShapeDrawer.EffectPicture p : pictures) if (p.getLayer() == ShapeDrawer.EffectLayer.REPLACE) replaced = true;
		for (ShapeDrawer.EffectLayer layer : new ShapeDrawer.EffectLayer[] { ShapeDrawer.EffectLayer.UNDER, ShapeDrawer.EffectLayer.REPLACE,
				ShapeDrawer.EffectLayer.OVER }) {
			if (layer == ShapeDrawer.EffectLayer.REPLACE && !replaced) {
				svg.appendChild(picture(document, href != null ? href : dataUri(part, bytes), w, h, crop, cropped));
				continue;
			}
			for (ShapeDrawer.EffectPicture p : pictures) {
				if (p.getLayer() != layer) continue;
				Element e = effect(document, p, layer == ShapeDrawer.EffectLayer.REPLACE ? null : clip, h);
				if (e != null) svg.appendChild(e);
			}
		}
		return svg;
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
