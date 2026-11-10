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
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.docx4j.model.color.ThemeColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Draws a DrawingML shape, group or canvas as SVG, for an output docx4j-core writes itself (HTML: CR-080 phase 5), and
 * a picture's frame and effects, for the PDF and HTML outputs alike (CR-081).
 * docx4j-chart-render provides one (its ShapeRenderer, as it provides the {@link org.docx4j.model.chart.ChartRenderer});
 * without it a shape's text is still laid out where Word sets it, and nothing is drawn under it.
 *
 * @since 17.3.7
 */
public interface ShapeDrawer {

	/**
	 * The shape drawn at its extent, cx by cy EMU.
	 *
	 * @param graphic a wps:wsp, wpg:wgp or wpc:wpc (CTWordprocessingShape, CTWordprocessingGroup, CTWordprocessingCanvas)
	 * @param pictures a picture fill's picture by its relationship id, or null where it can't be had
	 * @return null where the graphic isn't one it draws
	 */
	Drawing draw(Object graphic, ThemeColors theme, long cx, long cy, Function<String, BufferedImage> pictures);

	/**
	 * A picture drawn as Word draws it in its frame, w by h points (CR-081 phases 1 and 2): its outline ({@code
	 * pic:spPr/a:prstGeom} or {@code a:custGeom}), which clips it; the fill behind it; its line ({@code a:ln}), wholly
	 * outside it; and its effects ({@code a:effectLst}: outer and inner shadow, glow, soft edge, reflection), pictures
	 * with alpha under it, over it, or in its place (the picture with its soft edge, its line in it).
	 *
	 * @param spPr the picture's pic:spPr (a CTShapeProperties)
	 * @param picture the stored bitmap, all of it (it may be a reduced copy); null where it can't be read: its effects
	 *        are then not drawn, its frame is
	 * @param crop what of the bitmap the frame shows ({@code a:srcRect}), or null for all of it
	 * @param maxPixelsPerPoint an upper bound on an effect picture's resolution, 0 for none (Word's: 200 dpi, coarser
	 *        for a wide blur)
	 * @param problems the gaps are added here, stable codes ({@code render.effect.*}, {@code render.shape.*})
	 * @return null where this drawer doesn't draw pictures
	 * @since 17.3.7
	 */
	default PictureDrawing drawPicture(Object spPr, ThemeColors theme, double w, double h, BufferedImage picture,
			org.docx4j.model.images.PictureCrop crop, double maxPixelsPerPoint, List<String> problems) {
		return null;
	}

	/**
	 * A picture as Word draws it, in its frame: points from its box's top left, y down. Drawn in this order: the effect
	 * pictures under it; its preset shadow; the fill behind it, then the picture, both clipped to the outline; the effect pictures over it;
	 * the line. Where an effect picture takes its place (a soft edge), that picture is drawn for the fill, the picture
	 * and the line.
	 *
	 * @since 17.3.7
	 */
	interface PictureDrawing {

		/** The outline the picture and the fill behind it are clipped to; null where it is the picture's box */
		java.awt.Shape getClip();

		/** The fill behind the picture as SVG, a user unit a point, its viewBox where it is in the frame; null for none */
		Document getBehind();

		/** The line round the picture as SVG, likewise; null for none */
		Document getLine();

		/** The effect pictures, in the order they are drawn */
		List<EffectPicture> getEffects();

		/**
		 * The preset shadow ({@code a:prstShdw}: vector fills, CR-081 phase 4) as SVG, a user unit a point, its viewBox
		 * where it is in the frame, drawn after the effect pictures under the picture; null for none
		 */
		default Document getShadow() {
			return null;
		}

		/** Nothing is drawn round the picture or clipped from it, and it has no effects: it is drawn as a picture alone */
		default boolean isPlain() {
			return getClip() == null && getBehind() == null && getLine() == null && getShadow() == null && getEffects().isEmpty();
		}
	}

	/** Where an effect picture is drawn: under the picture, over it, or in its place */
	enum EffectLayer {
		UNDER, OVER, REPLACE
	}

	/** An effect picture, in the frame of the picture it is drawn for: points from its box's top left, y down */
	interface EffectPicture {

		/** Its pixels, not premultiplied */
		BufferedImage getImage();

		/** Where it goes */
		Rectangle2D getPlace();

		EffectLayer getLayer();

		/** The effect's element name: outerShdw, glow, reflection, innerShdw, softEdge */
		String getEffect();
	}

	/** A shape, group or canvas drawn, in its frame: points from its box's top left, y down */
	interface Drawing {

		/** As SVG, a user unit a point, its canvas {@link #getBounds()}: what it paints, which can reach past the box */
		Document toSvg();

		/** All it paints, its line and arrowheads included; it can reach past the box */
		Rectangle2D getBounds();

		/** Nothing is painted */
		boolean isEmpty();

		/** The gaps, stable codes (render.shape.*, CR-080 D9) */
		List<String> getProblems();

		/** A shape's geometry (its text rectangle); a group's or canvas's is its box */
		ShapeOutlines getOutlines();

		/** A group's or canvas's shapes and pictures where they are drawn, in document order; none for a shape */
		List<Child> getChildren();
	}

	/** A shape or picture in a group or canvas, where it is drawn */
	interface Child {

		/** The child: a CTWordprocessingShape, a picture, a nested group's shape */
		Object getChild();

		/** Its box in the drawing's frame, before its turn and flips */
		Rectangle2D getBox();

		/** Its geometry at its box (its text rectangle) */
		ShapeOutlines getOutlines();
	}

	// ------------------------------------------------------------------ lookup

	/** cache: looked up once */
	final class Holder {
		private Holder() {}
		static final Logger log = LoggerFactory.getLogger(ShapeDrawer.class);
		static volatile ShapeDrawer drawer;
		static volatile boolean resolved;
	}

	/** The drawer on the classpath (docx4j-chart-render's), or null; {@link #setDrawer} overrides it */
	static ShapeDrawer getDrawer() {
		if (!Holder.resolved) {
			synchronized (Holder.class) {
				if (!Holder.resolved) {
					ShapeDrawer found = null;
					try {
						Iterator<ShapeDrawer> it = ServiceLoader.load(ShapeDrawer.class).iterator();
						if (!it.hasNext()) it = ServiceLoader.load(ShapeDrawer.class, ShapeDrawer.class.getClassLoader()).iterator();
						if (it.hasNext()) found = it.next();
						if (it.hasNext()) Holder.log.warn("More than one ShapeDrawer on the classpath; using " + found.getClass().getName());
					} catch (Throwable t) {
						Holder.log.warn("No ShapeDrawer: " + t);
					}
					if (found == null) {
						Holder.log.debug("No ShapeDrawer on the classpath; shapes aren't drawn in HTML (add docx4j-chart-render).");
					}
					Holder.drawer = found;
					Holder.resolved = true;
				}
			}
		}
		return Holder.drawer;
	}

	/** Overrides the discovered drawer; null turns shape drawing off */
	static void setDrawer(ShapeDrawer d) {
		synchronized (Holder.class) {
			Holder.drawer = d;
			Holder.resolved = true;
		}
	}
}
