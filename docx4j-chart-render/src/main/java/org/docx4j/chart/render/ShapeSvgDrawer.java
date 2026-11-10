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

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.docx4j.chart.render.api.Effects;
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTEffectList;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.images.PictureCrop;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.model.shapes.ShapeOutlines;
import org.w3c.dom.Document;

/**
 * docx4j-core's {@link ShapeDrawer}, by {@link ShapeRenderer}: a shape, group or canvas drawn to SVG for the HTML
 * output, as the PDF output draws it (CR-080 phase 5); and by {@link Effects}, a picture's effects for both (CR-081).
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

	@Override
	public List<EffectPicture> drawEffects(Object effects, ThemeColors theme, double w, double h, BufferedImage picture, PictureCrop crop,
			double maxPixelsPerPoint, List<String> problems) {
		List<EffectPicture> out = new ArrayList<EffectPicture>();
		if (!(effects instanceof CTEffectList) || picture == null || picture.getWidth() <= 0 || picture.getHeight() <= 0) return out;
		AffineTransform at = frame(picture.getWidth(), picture.getHeight(), w, h, crop);
		if (at == null) return out;
		Rectangle2D.Double box = new Rectangle2D.Double(0, 0, w, h);
		Effects.Result r = Effects.draw((CTEffectList) effects, theme, w, h, g -> {
			// the picture as the frame shows it: the crop's part of the bitmap stretched to the frame, nothing past it
			java.awt.Shape clip = g.getClip();
			g.clip(box);
			g.drawImage(picture, at, null);
			g.setClip(clip);
		}, maxPixelsPerPoint);
		if (problems != null) {
			for (String code : r.getProblems()) if (!problems.contains(code)) problems.add(code);
		}
		for (final Effects.Picture p : r.getPictures()) {
			final EffectLayer layer = p.getLayer() == Effects.Layer.UNDER ? EffectLayer.UNDER
					: p.getLayer() == Effects.Layer.OVER ? EffectLayer.OVER : EffectLayer.REPLACE;
			out.add(new EffectPicture() {
				@Override
				public BufferedImage getImage() {
					return p.getImage();
				}

				@Override
				public Rectangle2D getPlace() {
					return p.getPlace();
				}

				@Override
				public EffectLayer getLayer() {
					return layer;
				}

				@Override
				public String getEffect() {
					return p.getEffect();
				}
			});
		}
		return out;
	}

	/**
	 * The bitmap's pixels to the frame's points: the crop's part (fractions of the bitmap from each edge, negative
	 * moving an edge out) stretched to w by h; null where nothing of it is shown
	 */
	static AffineTransform frame(int pw, int ph, double w, double h, PictureCrop crop) {
		double l = crop == null ? 0 : crop.left, t = crop == null ? 0 : crop.top, r = crop == null ? 0 : crop.right,
				b = crop == null ? 0 : crop.bottom;
		double fw = 1 - l - r, fh = 1 - t - b;
		if (!(fw > 0) || !(fh > 0) || !(w > 0) || !(h > 0)) return null;
		double sx = w / (fw * pw), sy = h / (fh * ph);
		return new AffineTransform(sx, 0, 0, sy, -l * pw * sx, -t * ph * sy);
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
