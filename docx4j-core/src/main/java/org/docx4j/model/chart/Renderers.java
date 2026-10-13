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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;

/**
 * What {@link ChartRenderer} and {@link ChartExRenderer} share (CR-039): finding a provider, and
 * drawing a picture.
 */
final class Renderers {

	private Renderers() {
	}

	/**
	 * The first provider of the service: the thread's context class loader's, else docx4j-core's;
	 * null where there is none.  Where there are more, the first is kept and a warning names them
	 * all: two providers of one service is two jars that each expect to draw.
	 */
	static <T> T first(Class<T> service, Logger log) {
		try {
			T found = first(ServiceLoader.load(service), service, log);
			if (found == null) {
				// the thread's context class loader didn't have it: docx4j-core's own
				found = first(ServiceLoader.load(service, service.getClassLoader()), service, log);
			}
			return found;
		} catch (Throwable t) {
			log.warn("No " + service.getSimpleName() + ": " + t);
			return null;
		}
	}

	private static <T> T first(ServiceLoader<T> loader, Class<T> service, Logger log) {
		List<ServiceLoader.Provider<T>> providers = loader.stream().collect(Collectors.toList());
		if (providers.isEmpty()) return null;
		if (providers.size() > 1) {
			log.warn("More than one " + service.getSimpleName() + " on the classpath: "
					+ providers.stream().map(p -> p.type().getName()).collect(Collectors.joining(", ")) + "; using the first");
		}
		return providers.get(0).get();
	}

	/** A picture of width by height points, dpi pixels an inch, on white, drawn by draw (in points) */
	static BufferedImage picture(double width, double height, double dpi, Consumer<Graphics2D> draw) {
		double scale = dpi / 72.0;
		BufferedImage image = new BufferedImage((int) Math.max(1, Math.ceil(width * scale)),
				(int) Math.max(1, Math.ceil(height * scale)), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.scale(scale, scale);
			draw.accept(g);
		} finally {
			g.dispose();
		}
		return image;
	}
}
