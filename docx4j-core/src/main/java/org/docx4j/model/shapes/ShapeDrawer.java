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
 * Draws a DrawingML shape, group or canvas as SVG, for an output docx4j-core writes itself (HTML: CR-080 phase 5).
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
