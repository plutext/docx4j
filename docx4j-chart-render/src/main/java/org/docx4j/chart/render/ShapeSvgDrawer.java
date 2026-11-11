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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.docx4j.chart.render.api.Effects;
import org.docx4j.chart.render.api.Scene3D;
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.chart.render.api.SvgWriter;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.images.PictureCrop;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.model.shapes.ShapeOutlines;
import org.w3c.dom.Document;

/**
 * docx4j-core's {@link ShapeDrawer}, by {@link ShapeRenderer}: a shape, group or canvas drawn to SVG for the HTML
 * output, as the PDF output draws it (CR-080 phase 5); and by {@link ShapeRenderer#pictureFrame} and {@link Effects}, a
 * picture's frame and effects for both (CR-081).
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
			drawn = ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, cx, cy, p, org.docx4j.model.shapes.ShapeText.hasText(wsp));
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
	public PictureDrawing drawPicture(Object spPr, ThemeColors theme, double w, double h, BufferedImage picture, PictureCrop crop,
			double maxPixelsPerPoint, List<String> problems) {
		if (!(w > 0) || !(h > 0) || w * 12700 > Long.MAX_VALUE / 2 || h * 12700 > Long.MAX_VALUE / 2) return null;
		CTShapeProperties sp = spPr instanceof CTShapeProperties ? (CTShapeProperties) spPr : null;
		final ShapeRenderer.PictureFrame frame = ShapeRenderer.pictureFrame(sp, theme, Math.round(w * 12700), Math.round(h * 12700));
		if (problems != null) {
			for (String code : frame.getProblems()) if (!problems.contains(code)) problems.add(code);
		}
		final List<EffectPicture> out = new ArrayList<EffectPicture>();
		CTEffectList fx = sp == null ? null : sp.getEffectLst();
		AffineTransform at = picture == null || picture.getWidth() <= 0 || picture.getHeight() <= 0 ? null
				: frame(picture.getWidth(), picture.getHeight(), w, h, crop);
		// a 3-D picture (17 of Word's 28 picture styles): the picture in its frame, its line and the fill behind it, are
		// the face of one picture, which takes its place, its effects cast by it (CR-081 phase 5a)
		Scene3D.Picture three = null;
		if (sp != null && ShapeRenderer.is3d(sp.getScene3D(), sp.getSp3D())) {
			if (at != null) {
				final AffineTransform fat = at;
				Rectangle2D.Double box = new Rectangle2D.Double(0, 0, w, h);
				Effects.Painter face = g -> {
					if (frame.getBehind() != null) frame.getBehind().paint(g);
					paintPicture(g, frame, picture, fat, box);
					if (frame.getLine() != null) frame.getLine().paint(g);
				};
				Area outline = new Area(frame.getOutline() != null ? frame.getOutline() : box);
				if (frame.getLine() != null) outline.add(new Area(ShapeRenderer.outline(frame.getLine())));
				double ppt = maxPixelsPerPoint > 0 ? Math.min(Effects.BASE_PIXELS_PER_POINT, maxPixelsPerPoint) : Effects.BASE_PIXELS_PER_POINT;
				three = Scene3D.draw(face, outline, w, h, sp.getScene3D(), sp.getSp3D(), theme, ppt, problems);
			}
			if (three == null && problems != null && !problems.contains(ShapeRenderer.FLAT_3D)) problems.add(ShapeRenderer.FLAT_3D);
		}
		// a transparent picture's preset shadow is its strokes', a picture (fxp3-02, -07); an opaque one's the frame's
		// vector fills (phase 4)
		boolean rasterPreset = three == null && fx != null && fx.getPrstShdw() != null && at != null && transparent(picture);
		Effects.Painter presetPainter = null;
		Rectangle2D presetBounds = null;
		final List<Effects.Picture> presetPictures = new ArrayList<Effects.Picture>();
		if (rasterPreset) {
			Rectangle2D.Double box = new Rectangle2D.Double(0, 0, w, h);
			Rectangle2D drawn = frame.getLineBounds() == null ? box : box.createUnion(frame.getLineBounds());
			Effects.Painter whole = g -> {
				paintPicture(g, frame, picture, at, box);
				if (frame.getLine() != null) frame.getLine().paint(g);
			};
			presetPictures.addAll(Effects.presetShadowPictures(fx.getPrstShdw(), theme, whole, drawn, drawn, maxPixelsPerPoint));
		} else if (frame.getShadow() != null && three == null) {
			presetPainter = g -> frame.getShadow().paint(g);
			presetBounds = frame.getShadowBounds();
		}
		if (!presetPictures.isEmpty()) {
			presetPainter = g -> {
				for (Effects.Picture p : presetPictures) {
					Rectangle2D.Double pl = p.getPlace();
					g.drawImage(p.getImage(), new AffineTransform(pl.width / p.getImage().getWidth(), 0, 0, pl.height / p.getImage().getHeight(), pl.x,
							pl.y), null);
				}
			};
			for (Effects.Picture p : presetPictures) presetBounds = presetBounds == null ? p.getPlace() : presetBounds.createUnion(p.getPlace());
		}
		List<Effects.Picture> all = new ArrayList<Effects.Picture>();
		if (fx != null && at != null) {
			Rectangle2D.Double box = new Rectangle2D.Double(0, 0, w, h);
			Effects.Painter line = frame.getLine() == null ? null : g -> frame.getLine().paint(g);
			Effects.Painter body = g -> paintPicture(g, frame, picture, at, box);
			Rectangle2D lineBounds = frame.getLineBounds();
			if (three != null) {      // cast by the 3-D picture, within its bounds, which may reach past the frame
				body = three.painter();
				line = g -> {
				};
				lineBounds = three.getPlace();
			}
			Effects.Result r = Effects.draw(fx, theme, w, h, body, line, lineBounds, presetPainter, presetBounds, maxPixelsPerPoint);
			if (problems != null) {
				for (String code : r.getProblems()) if (!problems.contains(code)) problems.add(code);
			}
			// Word's order under the picture: the reflection, the preset shadow, then the outer shadow and glow
			for (Effects.Picture p : r.getPictures()) if ("reflection".equals(p.getEffect())) all.add(p);
			all.addAll(presetPictures);
			for (Effects.Picture p : r.getPictures()) if (!"reflection".equals(p.getEffect())) all.add(p);
			for (final Effects.Picture p : all) {
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
		}
		if (three != null) {
			final Scene3D.Picture picture3d = three;
			out.add(new EffectPicture() {
				@Override
				public BufferedImage getImage() {
					return picture3d.getImage();
				}

				@Override
				public Rectangle2D getPlace() {
					return picture3d.getPlace();
				}

				@Override
				public EffectLayer getLayer() {
					return EffectLayer.REPLACE;
				}

				@Override
				public String getEffect() {
					return "scene3d";
				}
			});
		}
		final Document behind = frame.getBehind() == null ? null : SvgWriter.toSvg(frame.getBehind(), frame.getBehindBounds());
		final Document lineSvg = frame.getLine() == null ? null : SvgWriter.toSvg(frame.getLine(), frame.getLineBounds());
		final Document shadow = frame.getShadow() == null || rasterPreset ? null : SvgWriter.toSvg(frame.getShadow(), frame.getShadowBounds());
		final Shape clip = frame.isBox() ? null : frame.getOutline();
		return new PictureDrawing() {
			@Override
			public Shape getClip() {
				return clip;
			}

			@Override
			public Document getBehind() {
				return behind;
			}

			@Override
			public Document getLine() {
				return lineSvg;
			}

			@Override
			public List<EffectPicture> getEffects() {
				return out;
			}

			@Override
			public Document getShadow() {
				return shadow;
			}
		};
	}

	/** Whether the bitmap has a pixel that isn't opaque */
	static boolean transparent(BufferedImage picture) {
		if (picture == null || !picture.getColorModel().hasAlpha()) return false;
		int w = picture.getWidth(), h = picture.getHeight();
		int[] row = new int[w];
		for (int y = 0; y < h; y++) {
			picture.getRGB(0, y, w, 1, row, 0, w);
			for (int v : row) if ((v >>> 24) < 255) return true;
		}
		return false;
	}

	/**
	 * The picture as its frame shows it, for its effects' silhouette: the fill behind it, then the crop's part of the
	 * bitmap stretched to the frame, both within the outline. An outline that isn't the box is a mask drawn
	 * antialiased, as Word's effect pictures have its edge (fxr71-03's inner shadow is a picture, not clipped in the
	 * PDF); a Graphics2D clip is not antialiased.
	 */
	static void paintPicture(Graphics2D g, ShapeRenderer.PictureFrame frame, BufferedImage picture, AffineTransform at, Rectangle2D box) {
		if (frame.isBox()) {
			if (frame.getBehind() != null) frame.getBehind().paint(g);
			Shape clip = g.getClip();
			g.clip(box);
			g.drawImage(picture, at, null);
			g.setClip(clip);
			return;
		}
		// the outline's pixels, in the device's space (the transform can scale and skew: a shadow's)
		AffineTransform t = g.getTransform();
		Rectangle device = t.createTransformedShape(frame.getOutline().getBounds2D()).getBounds();
		if (device.width <= 0 || device.height <= 0) return;
		if ((long) device.width * device.height > 64_000_000L) {
			// past any effect picture's cap (D10): clipped as it comes
			Shape clip = g.getClip();
			g.clip(frame.getOutline());
			if (frame.getBehind() != null) frame.getBehind().paint(g);
			g.clip(box);
			g.drawImage(picture, at, null);
			g.setClip(clip);
			return;
		}
		AffineTransform toLayer = AffineTransform.getTranslateInstance(-device.x, -device.y);
		toLayer.concatenate(t);
		BufferedImage layer = new BufferedImage(device.width, device.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D lg = layer.createGraphics();
		lg.setRenderingHints(g.getRenderingHints());
		lg.setTransform(toLayer);
		if (frame.getBehind() != null) frame.getBehind().paint(lg);
		lg.clip(box);
		lg.drawImage(picture, at, null);
		lg.dispose();
		BufferedImage mask = new BufferedImage(device.width, device.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D mg = mask.createGraphics();
		mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		mg.setTransform(toLayer);
		mg.setColor(Color.BLACK);
		mg.fill(frame.getOutline());
		mg.dispose();
		lg = layer.createGraphics();
		lg.setComposite(AlphaComposite.DstIn);
		lg.drawImage(mask, 0, 0, null);
		lg.dispose();
		g.setTransform(new AffineTransform());
		g.drawImage(layer, device.x, device.y, null);
		g.setTransform(t);
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
