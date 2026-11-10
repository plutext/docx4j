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
package org.docx4j.chart.render;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.model.shapes.ShapeOutlines;
import org.w3c.dom.Document;

/**
 * docx4j-core's {@link ShapeDrawer}, by {@link ShapeRenderer}: a shape, group or canvas drawn to SVG for the HTML
 * output, as the PDF output draws it (CR-080 phase 5).
 *
 * @since 17.3.7
 */
public final class ShapeSvgDrawer implements ShapeDrawer {

	@Override
	public Drawing draw(Object graphic, ThemeColors theme, long cx, long cy, Function<String, BufferedImage> pictures) {
		ShapeRenderer.Pictures p = pictures == null ? relId -> null : pictures::apply;
		ShapeRenderer.Drawn drawn;
		if (graphic instanceof CTWordprocessingShape) {
			CTWordprocessingShape wsp = (CTWordprocessingShape) graphic;
			drawn = ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, cx, cy, p);
		} else if (graphic instanceof CTWordprocessingGroup) {
			drawn = ShapeRenderer.drawGroup((CTWordprocessingGroup) graphic, theme, cx, cy, p);
		} else if (graphic instanceof CTWordprocessingCanvas) {
			drawn = ShapeRenderer.drawCanvas((CTWordprocessingCanvas) graphic, theme, cx, cy, p);
		} else {
			return null;
		}
		return new Svg(drawn);
	}

	private static final class Svg implements Drawing {
		private final ShapeRenderer.Drawn drawn;

		Svg(ShapeRenderer.Drawn drawn) {
			this.drawn = drawn;
		}

		@Override
		public Document toSvg() {
			return drawn.toSvg();
		}

		@Override
		public Rectangle2D getBounds() {
			return drawn.getBounds();
		}

		@Override
		public boolean isEmpty() {
			return drawn.isEmpty();
		}

		@Override
		public List<String> getProblems() {
			return drawn.getProblems();
		}

		@Override
		public ShapeOutlines getOutlines() {
			return drawn.getOutlines();
		}

		@Override
		public List<Child> getChildren() {
			List<Child> out = new ArrayList<Child>();
			for (final ShapeRenderer.Placed p : drawn.getChildren()) {
				out.add(new Child() {
					@Override
					public Object getChild() {
						return p.getChild();
					}

					@Override
					public Rectangle2D getBox() {
						return p.getBox();
					}

					@Override
					public ShapeOutlines getOutlines() {
						return p.getOutlines();
					}
				});
			}
			return out;
		}
	}
}
