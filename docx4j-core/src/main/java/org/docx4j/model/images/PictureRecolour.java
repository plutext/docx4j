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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.docx4j.dml.CTAlphaBiLevelEffect;
import org.docx4j.dml.CTAlphaCeilingEffect;
import org.docx4j.dml.CTAlphaFloorEffect;
import org.docx4j.dml.CTAlphaInverseEffect;
import org.docx4j.dml.CTAlphaModulateEffect;
import org.docx4j.dml.CTAlphaModulateFixedEffect;
import org.docx4j.dml.CTAlphaReplaceEffect;
import org.docx4j.dml.CTBiLevelEffect;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlurEffect;
import org.docx4j.dml.CTColorChangeEffect;
import org.docx4j.dml.CTColorReplaceEffect;
import org.docx4j.dml.CTDuotoneEffect;
import org.docx4j.dml.CTEffectContainer;
import org.docx4j.dml.CTFillOverlayEffect;
import org.docx4j.dml.CTGrayscaleEffect;
import org.docx4j.dml.CTHSLEffect;
import org.docx4j.dml.CTLuminanceEffect;
import org.docx4j.dml.CTTintEffect;
import org.docx4j.dml.STBlendMode;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A picture's colour adjustments, as Word draws them (CR-084): the effect children of a DrawingML {@code a:blip}
 * (ISO/IEC 29500-1 §20.1.8: recolour, brightness and contrast, transparency, a transparent colour), which Word's Picture
 * Format writes for Recolor, Washout, Black and White, Transparency and Set Transparent Color.
 *
 * <p>{@link #of(CTBlip, ThemeColors)} reads them, in the order Word applies them, with their colours resolved
 * ({@link DrawingMLColors}); {@link #apply(BufferedImage, double)} applies them to a picture's pixels. Each is a function
 * of the pixel, measured against Word's PDFs (CR-084 phase 0, {@code CR-084-notes/phase0-findings.md} §3, its reference
 * implementation {@code fit_recolour.py}), on the sRGB values as stored, not in linear light:</p>
 * <ul>
 * <li>{@code grayscl}: Rec. 709 luma, {@code 0.2126 r + 0.7152 g + 0.0722 b};</li>
 * <li>{@code lum}: {@code s (x - 1/2) + 1/2 + b (1 + s) / 2} a channel, clipped, where {@code s = 1 + c} for a contrast
 *     c at or below 0 and {@code 1 / (1 - c)} above it, and b is the brightness;</li>
 * <li>{@code biLevel}: white where the luma, in whole levels of 255 rounded down, reaches the threshold in whole levels
 *     rounded, else black;</li>
 * <li>{@code duotone}: {@code clr1 + (clr2 - clr1) luma};</li>
 * <li>{@code alphaModFix}, {@code alphaBiLevel}, {@code alphaCeiling}, {@code alphaFloor}, {@code alphaInv},
 *     {@code alphaRepl}: as their names say; {@code alphaMod}: the alpha times the alpha of its container's effects
 *     applied to the picture itself;</li>
 * <li>{@code clrChange}: the exact colour only (and its alpha, with {@code useA}, the default), to {@code clrTo}'s
 *     colour, and its alpha with {@code useA};</li>
 * <li>{@code clrRepl}: every pixel the colour, its alpha kept; {@code fillOverlay}: a solid fill blended as its
 *     {@code blend} says;</li>
 * <li>{@code tint}: each hue turned {@code amt} of the way to {@code hue}, the shorter way round, its lightness and
 *     saturation kept.</li>
 * </ul>
 * <p>The effects apply in the order written, but {@code biLevel} comes before a {@code duotone} written ahead of it, as
 * Word has it in either order. A fully transparent pixel has no colour (black): Word stores a picture premultiplied.</p>
 *
 * <p>Two effects are drawn as the standard has them, not as Word draws them, and reported: {@code hsl} (offsets to the
 * hue, saturation and luminance; Word draws the picture black), {@code render.picture.hsl}; and {@code blur} (a
 * Gaussian, its sigma a third of the radius, as CR-081 found Word's shadows' blur; Word draws the picture not at all),
 * {@code render.picture.blur}. A fill overlay other than a solid fill isn't drawn
 * ({@code render.picture.fill_overlay}), nor an effect whose colour can't be resolved
 * ({@code render.picture.colour}).</p>
 *
 * @since 17.3.7
 */
public final class PictureRecolour {

	private static final Logger log = LoggerFactory.getLogger(PictureRecolour.class);

	/** Drawn as the standard has it, not as Word draws it (the picture black) */
	static final String HSL = "render.picture.hsl";
	/** Drawn as the standard has it, not as Word draws it (no picture at all) */
	static final String BLUR = "render.picture.blur";
	/** A fill overlay other than a solid fill: not drawn */
	static final String FILL_OVERLAY = "render.picture.fill_overlay";
	/** An effect's colour that couldn't be resolved (a scheme colour without a theme): the effect isn't drawn */
	static final String COLOUR = "render.picture.colour";
	/** A picture with adjustments that couldn't be read (a CMYK JPEG, an animated GIF): drawn as stored */
	static final String UNREAD = "render.picture.unread";
	/** A vector picture (a metafile) with adjustments that couldn't be rasterised: drawn as stored */
	static final String RECOLOUR_VECTOR = "render.picture.recolour_vector";

	/** Rec. 709's weights, on the stored sRGB values (CR-084 phase 0 findings §3) */
	static final double KR = 0.2126, KG = 0.7152, KB = 0.0722;

	/** No adjustment */
	public static final PictureRecolour NONE = new PictureRecolour(Collections.<Op>emptyList(), Collections.<String[]>emptyList());

	private final List<Op> ops;
	/** Codes and details reported when the adjustments are drawn: what isn't drawn as Word draws it */
	private final List<String[]> departures;
	private final String signature;

	private PictureRecolour(List<Op> ops, List<String[]> departures) {
		this.ops = ops;
		this.departures = departures;
		StringBuilder b = new StringBuilder();
		for (Op op : ops) {
			if (b.length() > 0) b.append(';');
			b.append(op.key);
		}
		this.signature = b.toString();
	}

	/** Whether there is nothing to apply */
	public boolean isNone() {
		return ops.isEmpty();
	}

	/** A stable text for cache keys, naming each operation with its parameters; empty for {@link #NONE} */
	public String signature() {
		return signature;
	}

	/** Whether the adjustments can make an opaque pixel transparent (an alpha effect, a colour made transparent) */
	public boolean mayAddAlpha() {
		for (Op op : ops) if (op.addsAlpha) return true;
		return false;
	}

	@Override
	public String toString() {
		return "PictureRecolour[" + signature + "]";
	}

	// ------------------------------------------------------------------------------------------------------ reading

	/**
	 * The adjustments of a blip, its theme colours resolved for the package's theme where an effect has a scheme colour
	 * (the theme is read only then).
	 */
	public static PictureRecolour of(CTBlip blip, WordprocessingMLPackage pkg) {
		if (blip == null || blip.getAlphaBiLevelOrAlphaCeilingOrAlphaFloor().isEmpty()) return NONE;
		ThemeColors theme = null;
		try {
			if (pkg != null && pkg.getMainDocumentPart() != null) theme = ThemeColors.of(pkg);
		} catch (Exception e) {
			log.debug("No theme colours: " + e);
		}
		return of(blip, theme);
	}

	/**
	 * The adjustments of a blip ({@code a:blip}'s effect children), in the order Word applies them.
	 *
	 * @param theme the theme colours for a {@code schemeClr}; null where there are none (such an effect is then not drawn)
	 */
	public static PictureRecolour of(CTBlip blip, ThemeColors theme) {
		if (blip == null || blip.getAlphaBiLevelOrAlphaCeilingOrAlphaFloor().isEmpty()) return NONE;
		List<String[]> departures = new ArrayList<String[]>();
		List<Op> ops = read(blip.getAlphaBiLevelOrAlphaCeilingOrAlphaFloor(), theme, departures);
		// biLevel before a duotone written ahead of it: Word draws the two alike in either order (phase 0 findings §3)
		for (int i = 0; i < ops.size(); i++) {
			if (!(ops.get(i) instanceof BiLevel)) continue;
			for (int j = 0; j < i; j++) {
				if (ops.get(j) instanceof Duotone) {
					ops.add(j, ops.remove(i));
					break;
				}
			}
		}
		if (ops.isEmpty() && departures.isEmpty()) return NONE;
		return new PictureRecolour(ops, departures);
	}

	private static List<Op> read(List<Object> effects, ThemeColors theme, List<String[]> departures) {
		List<Op> ops = new ArrayList<Op>();
		for (Object o : effects) {
			Op op = op(o, theme, departures);
			if (op != null) ops.add(op);
		}
		return ops;
	}

	private static Op op(Object o, ThemeColors theme, List<String[]> departures) {
		if (o instanceof CTGrayscaleEffect) return new Grayscale();
		if (o instanceof CTLuminanceEffect) {
			CTLuminanceEffect e = (CTLuminanceEffect) o;
			if (e.getBright() == 0 && e.getContrast() == 0) return null;
			return new Lum(e.getBright(), e.getContrast());
		}
		if (o instanceof CTBiLevelEffect) return new BiLevel(((CTBiLevelEffect) o).getThresh());
		if (o instanceof CTDuotoneEffect) {
			List<Object> cs = ((CTDuotoneEffect) o).getEGColorChoice();
			ResolvedColor c1 = cs.size() > 0 ? DrawingMLColors.resolve(cs.get(0), theme) : null;
			ResolvedColor c2 = cs.size() > 1 ? DrawingMLColors.resolve(cs.get(1), theme) : null;
			if (c1 == null || c2 == null) {
				departures.add(new String[] { COLOUR, "a:duotone" });
				return null;
			}
			return new Duotone(c1, c2);
		}
		if (o instanceof CTAlphaModulateFixedEffect) {
			int amt = ((CTAlphaModulateFixedEffect) o).getAmt();
			return amt == 100000 ? null : new AlphaModFix(amt);
		}
		if (o instanceof CTAlphaBiLevelEffect) return new AlphaBiLevel(((CTAlphaBiLevelEffect) o).getThresh());
		if (o instanceof CTAlphaCeilingEffect) return new AlphaCeiling();
		if (o instanceof CTAlphaFloorEffect) return new AlphaFloor();
		if (o instanceof CTAlphaInverseEffect) {
			CTAlphaInverseEffect e = (CTAlphaInverseEffect) o;
			Object c = first(e.getSrgbClr(), e.getSchemeClr(), e.getScrgbClr(), e.getHslClr(), e.getSysClr(), e.getPrstClr());
			ResolvedColor rc = c == null ? ResolvedColor.BLACK : DrawingMLColors.resolve(c, theme);
			if (rc == null) {
				departures.add(new String[] { COLOUR, "a:alphaInv" });
				return null;
			}
			return new AlphaInv(rc);
		}
		if (o instanceof CTAlphaModulateEffect) {
			CTEffectContainer cont = ((CTAlphaModulateEffect) o).getCont();
			List<Op> inner = cont == null ? Collections.<Op>emptyList() : read(cont.getEGEffect(), theme, departures);
			List<Op> pixel = new ArrayList<Op>();
			for (Op op : inner) {
				if (op instanceof Blur) departures.add(new String[] { BLUR, "a:alphaMod's a:blur" });
				else pixel.add(op);
			}
			return new AlphaMod(pixel);
		}
		if (o instanceof CTAlphaReplaceEffect) return new AlphaRepl(((CTAlphaReplaceEffect) o).getA());
		if (o instanceof CTColorChangeEffect) {
			CTColorChangeEffect e = (CTColorChangeEffect) o;
			ResolvedColor from = e.getClrFrom() == null ? null : DrawingMLColors.resolve(e.getClrFrom(), theme);
			ResolvedColor to = e.getClrTo() == null ? null : DrawingMLColors.resolve(e.getClrTo(), theme);
			if (from == null || to == null) {
				departures.add(new String[] { COLOUR, "a:clrChange" });
				return null;
			}
			return new ClrChange(from, to, e.isUseA());
		}
		if (o instanceof CTColorReplaceEffect) {
			CTColorReplaceEffect e = (CTColorReplaceEffect) o;
			Object c = first(e.getSrgbClr(), e.getSchemeClr(), e.getScrgbClr(), e.getHslClr(), e.getSysClr(), e.getPrstClr());
			ResolvedColor rc = c == null ? null : DrawingMLColors.resolve(c, theme);
			if (rc == null) {
				departures.add(new String[] { COLOUR, "a:clrRepl" });
				return null;
			}
			return new ClrRepl(rc);
		}
		if (o instanceof CTFillOverlayEffect) {
			CTFillOverlayEffect e = (CTFillOverlayEffect) o;
			if (e.getNoFill() != null) return null;
			if (e.getSolidFill() == null) {
				departures.add(new String[] { FILL_OVERLAY, "a:fillOverlay" });
				return null;
			}
			ResolvedColor rc = DrawingMLColors.resolve(e.getSolidFill(), theme);
			if (rc == null) {
				departures.add(new String[] { COLOUR, "a:fillOverlay" });
				return null;
			}
			return new FillOverlay(rc, e.getBlend());
		}
		if (o instanceof CTTintEffect) {
			CTTintEffect e = (CTTintEffect) o;
			return e.getAmt() == 0 ? null : new Tint(e.getHue(), e.getAmt());
		}
		if (o instanceof CTHSLEffect) {
			CTHSLEffect e = (CTHSLEffect) o;
			departures.add(new String[] { HSL, "a:hsl" });
			if (e.getHue() == 0 && e.getSat() == 0 && e.getLum() == 0) return null;
			return new Hsl(e.getHue(), e.getSat(), e.getLum());
		}
		if (o instanceof CTBlurEffect) {
			CTBlurEffect e = (CTBlurEffect) o;
			departures.add(new String[] { BLUR, "a:blur" });
			return e.getRad() <= 0 ? null : new Blur(e.getRad());
		}
		// an effect container's spatial effects (shadows, glow and so on) inside alphaMod: not a blip's
		if (o != null) log.debug("Not a picture colour adjustment: " + o.getClass().getSimpleName());
		return null;
	}

	private static Object first(Object... os) {
		for (Object o : os) if (o != null) return o;
		return null;
	}

	// ----------------------------------------------------------------------------------------------------- applying

	/**
	 * The picture with the adjustments applied: ARGB where any pixel is transparent, else RGB.
	 *
	 * @param src the picture (cropped already: the adjustments apply after the crop, as Word applies them)
	 * @param pixelsPerPoint the picture's pixels per point where it is shown, for a blur's radius; 0 where not known
	 *        (96 dpi then)
	 * @return src itself where there is nothing to apply
	 */
	public BufferedImage apply(BufferedImage src, double pixelsPerPoint) {
		if (src == null || isNone()) return src;
		report();
		int w = src.getWidth(), h = src.getHeight();
		// drawn, not read with getRGB: getRGB treats a grey picture's levels as linear and lightens them
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(src, 0, 0, null);
		} finally {
			g.dispose();
		}
		int[] px = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		// a fully transparent pixel has no colour: Word stores pictures premultiplied (alphaRepl, clrChange's useA)
		for (int i = 0; i < px.length; i++) if ((px[i] >>> 24) == 0) px[i] = 0;
		double ppp = pixelsPerPoint > 0 ? pixelsPerPoint : 96 / 72d;
		List<Op> run = new ArrayList<Op>();
		for (Op op : ops) {
			if (op instanceof Blur) {
				pixels(px, run);
				run.clear();
				((Blur) op).apply(px, w, h, ppp);
			} else {
				run.add(op);
			}
		}
		pixels(px, run);
		boolean opaque = true;
		for (int i = 0; i < px.length && opaque; i++) if ((px[i] >>> 24) != 0xFF) opaque = false;
		if (!opaque) return img;
		BufferedImage rgb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int[] out = ((DataBufferInt) rgb.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < px.length; i++) out[i] = px[i] & 0xFFFFFF;
		return rgb;
	}

	/** Logs, once a code, what isn't drawn as Word draws it */
	void report() {
		for (String[] d : departures) PictureEffects.report(d[0], d[1]);
	}

	/** The per-pixel operations over every pixel (ARGB, not premultiplied) */
	private static void pixels(int[] px, List<Op> run) {
		if (run.isEmpty()) return;
		Op[] ops = run.toArray(new Op[0]);
		double[] p = new double[4];
		int last = 0, lastOut = 0;
		boolean any = false;
		for (int i = 0; i < px.length; i++) {
			int v = px[i];
			if (any && v == last) {
				px[i] = lastOut;
				continue;
			}
			p[0] = ((v >> 16) & 0xFF) / 255d;
			p[1] = ((v >> 8) & 0xFF) / 255d;
			p[2] = (v & 0xFF) / 255d;
			p[3] = (v >>> 24) / 255d;
			for (Op op : ops) op.pixel(p);
			int out = level(p[3]) << 24 | level(p[0]) << 16 | level(p[1]) << 8 | level(p[2]);
			last = v;
			lastOut = out;
			any = true;
			px[i] = out;
		}
	}

	private static int level(double v) {
		if (!(v > 0)) return 0;
		if (v >= 1) return 255;
		return (int) Math.round(v * 255);
	}

	private static double clamp(double v) {
		return v < 0 ? 0 : (v > 1 ? 1 : v);
	}

	static double luma(double[] p) {
		return KR * p[0] + KG * p[1] + KB * p[2];
	}

	private static String hex(ResolvedColor c) {
		return c.toHex() + String.format(Locale.ROOT, "%05d", c.getAlpha());
	}

	// --------------------------------------------------------------------------------------------------- operations

	/** One adjustment: a function of the pixel (r, g, b, a, each 0 to 1, not premultiplied), changed in place */
	abstract static class Op {
		final String key;
		final boolean addsAlpha;

		Op(String key, boolean addsAlpha) {
			this.key = key;
			this.addsAlpha = addsAlpha;
		}

		abstract void pixel(double[] p);
	}

	static final class Grayscale extends Op {
		Grayscale() {
			super("grayscl", false);
		}

		@Override
		void pixel(double[] p) {
			double y = luma(p);
			p[0] = p[1] = p[2] = y;
		}
	}

	/** {@code s (x - 1/2) + 1/2 + b (1 + s) / 2}, s from the contrast; within 1.7 levels of Word on 36 probes */
	static final class Lum extends Op {
		final double slope, shift;

		Lum(int bright, int contrast) {
			super("lum" + bright + "," + contrast, false);
			double c = contrast / 100000d, b = bright / 100000d;
			slope = c <= 0 ? 1 + c : (c < 1 ? 1 / (1 - c) : 1e9);
			shift = 0.5 + b * (1 + slope) / 2;
		}

		@Override
		void pixel(double[] p) {
			for (int i = 0; i < 3; i++) p[i] = clamp(slope * (p[i] - 0.5) + shift);
		}
	}

	/**
	 * White where the luma, in whole levels rounded down, reaches the threshold in whole levels rounded: at 50% (128
	 * levels) a luma of 127.84 is black, at 75% (191) a luma of 191 white; every patch of phase 0's seven biLevel probes
	 */
	static final class BiLevel extends Op {
		final long thresh;

		BiLevel(int thresh) {
			super("biLevel" + thresh, false);
			this.thresh = Math.round(thresh / 100000d * 255);
		}

		@Override
		void pixel(double[] p) {
			double y = Math.floor(luma(p) * 255 + 1e-6);
			p[0] = p[1] = p[2] = y >= thresh ? 1 : 0;
		}
	}

	/** The luma's line from clr1 to clr2 */
	static final class Duotone extends Op {
		final double[] c1, c2;

		Duotone(ResolvedColor c1, ResolvedColor c2) {
			super("duotone" + hex(c1) + "," + hex(c2), false);
			this.c1 = rgb(c1);
			this.c2 = rgb(c2);
		}

		@Override
		void pixel(double[] p) {
			double y = luma(p);
			for (int i = 0; i < 3; i++) p[i] = clamp(c1[i] + (c2[i] - c1[i]) * y);
		}
	}

	static final class AlphaModFix extends Op {
		final double amt;

		AlphaModFix(int amt) {
			super("alphaModFix" + amt, amt < 100000);
			this.amt = amt / 100000d;
		}

		@Override
		void pixel(double[] p) {
			p[3] = clamp(p[3] * amt);
		}
	}

	static final class AlphaBiLevel extends Op {
		final double thresh;

		AlphaBiLevel(int thresh) {
			super("alphaBiLevel" + thresh, true);
			this.thresh = thresh / 100000d;
		}

		@Override
		void pixel(double[] p) {
			p[3] = p[3] >= thresh - 1e-9 ? 1 : 0;
		}
	}

	static final class AlphaCeiling extends Op {
		AlphaCeiling() {
			super("alphaCeiling", false);
		}

		@Override
		void pixel(double[] p) {
			if (p[3] > 0) p[3] = 1;
		}
	}

	static final class AlphaFloor extends Op {
		AlphaFloor() {
			super("alphaFloor", true);
		}

		@Override
		void pixel(double[] p) {
			if (p[3] < 1) p[3] = 0;
		}
	}

	/** The colour (black where none is given), its alpha 1 - alpha */
	static final class AlphaInv extends Op {
		final double[] c;

		AlphaInv(ResolvedColor c) {
			super("alphaInv" + hex(c), true);
			this.c = rgb(c);
		}

		@Override
		void pixel(double[] p) {
			p[0] = c[0];
			p[1] = c[1];
			p[2] = c[2];
			p[3] = 1 - p[3];
		}
	}

	/** The alpha times the alpha of the container's effects applied to the pixel (with alphaModFix 50%, 0.5 a^2) */
	static final class AlphaMod extends Op {
		final Op[] cont;

		AlphaMod(List<Op> cont) {
			super("alphaMod(" + keys(cont) + ")", true);
			this.cont = cont.toArray(new Op[0]);
		}

		@Override
		void pixel(double[] p) {
			double[] q = p.clone();
			for (Op op : cont) op.pixel(q);
			p[3] = clamp(p[3] * q[3]);
		}
	}

	static final class AlphaRepl extends Op {
		final double a;

		AlphaRepl(int a) {
			super("alphaRepl" + a, a < 100000);
			this.a = clamp(a / 100000d);
		}

		@Override
		void pixel(double[] p) {
			p[3] = a;
		}
	}

	/** The exact colour, and its alpha with useA, to clrTo's colour, and its alpha with useA */
	static final class ClrChange extends Op {
		final int fr, fg, fb, fa;
		final double[] to;
		final double toA;
		final boolean useA;

		ClrChange(ResolvedColor from, ResolvedColor to, boolean useA) {
			super("clrChange" + hex(from) + "," + hex(to) + (useA ? "" : ",noA"), useA && to.getAlpha() < ResolvedColor.OPAQUE);
			fr = from.getRed();
			fg = from.getGreen();
			fb = from.getBlue();
			fa = level(from.getAlpha() / (double) ResolvedColor.OPAQUE);
			this.to = rgb(to);
			this.toA = clamp(to.getAlpha() / (double) ResolvedColor.OPAQUE);
			this.useA = useA;
		}

		@Override
		void pixel(double[] p) {
			if (level(p[0]) != fr || level(p[1]) != fg || level(p[2]) != fb) return;
			if (useA && level(p[3]) != fa) return;
			p[0] = to[0];
			p[1] = to[1];
			p[2] = to[2];
			if (useA) p[3] = toA;
		}
	}

	static final class ClrRepl extends Op {
		final double[] c;

		ClrRepl(ResolvedColor c) {
			super("clrRepl" + hex(c), false);
			this.c = rgb(c);
		}

		@Override
		void pixel(double[] p) {
			p[0] = c[0];
			p[1] = c[1];
			p[2] = c[2];
		}
	}

	/** A solid fill blended with the picture's colour: over, mult, screen, darken, lighten; by the fill's alpha */
	static final class FillOverlay extends Op {
		final double[] c;
		final double a;
		final STBlendMode blend;

		FillOverlay(ResolvedColor c, STBlendMode blend) {
			super("fillOverlay" + (blend == null ? "over" : blend.value()) + hex(c), false);
			this.c = rgb(c);
			this.a = clamp(c.getAlpha() / (double) ResolvedColor.OPAQUE);
			this.blend = blend == null ? STBlendMode.OVER : blend;
		}

		@Override
		void pixel(double[] p) {
			for (int i = 0; i < 3; i++) {
				double x = p[i], f;
				switch (blend) {
					case MULT: f = x * c[i]; break;
					case SCREEN: f = 1 - (1 - x) * (1 - c[i]); break;
					case DARKEN: f = Math.min(x, c[i]); break;
					case LIGHTEN: f = Math.max(x, c[i]); break;
					default: f = c[i]; break;
				}
				p[i] = clamp(x + (f - x) * a);
			}
		}
	}

	/** Each hue turned amt of the way to the hue, the shorter way round (an exactly opposite hue by the plain
	 *  difference's sign); lightness and saturation kept */
	static final class Tint extends Op {
		final double hue, amt;

		Tint(int hue, int amt) {
			super("tint" + hue + "," + amt, false);
			this.hue = hue / 21600000d;
			this.amt = amt / 100000d;
		}

		@Override
		void pixel(double[] p) {
			double[] hsl = rgbToHsl(p[0], p[1], p[2]);
			double d = hue - hsl[0];
			if (d > 0.5 + 1e-9) d -= 1;
			else if (d < -0.5 - 1e-9) d += 1;
			double h = (hsl[0] + amt * d) % 1.0;
			if (h < 0) h += 1;
			hslToRgb(h, hsl[1], hsl[2], p);
		}
	}

	/** As the standard has it: offsets to the hue, saturation and luminance (Word draws the picture black) */
	static final class Hsl extends Op {
		final double hue, sat, lum;

		Hsl(int hue, int sat, int lum) {
			super("hsl" + hue + "," + sat + "," + lum, false);
			this.hue = hue / 21600000d;
			this.sat = sat / 100000d;
			this.lum = lum / 100000d;
		}

		@Override
		void pixel(double[] p) {
			double[] hsl = rgbToHsl(p[0], p[1], p[2]);
			double h = (hsl[0] + hue) % 1.0;
			if (h < 0) h += 1;
			hslToRgb(h, clamp(hsl[1] + sat), clamp(hsl[2] + lum), p);
		}
	}

	/** A Gaussian blur of every channel (premultiplied), its sigma a third of the radius, the edges extended (Word
	 *  draws a blurred picture not at all) */
	static final class Blur extends Op {
		final long rad;

		Blur(long rad) {
			super("blur" + rad, true);
			this.rad = rad;
		}

		@Override
		void pixel(double[] p) {
			// a whole-picture operation: see apply
		}

		void apply(int[] px, int w, int h, double pixelsPerPoint) {
			double sigma = rad / 12700d / 3 * pixelsPerPoint;
			if (!(sigma >= 0.3)) return;
			sigma = Math.min(sigma, Math.max(w, h));
			int n = w * h;
			float[][] ch = new float[4][n];
			for (int i = 0; i < n; i++) {
				int v = px[i];
				float a = (v >>> 24) / 255f;
				ch[3][i] = a;
				ch[0][i] = ((v >> 16) & 0xFF) / 255f * a;
				ch[1][i] = ((v >> 8) & 0xFF) / 255f * a;
				ch[2][i] = (v & 0xFF) / 255f * a;
			}
			float[] k = kernel(sigma);
			for (float[] c : ch) {
				gaussian(c, w, h, k, true);
				gaussian(c, w, h, k, false);
			}
			for (int i = 0; i < n; i++) {
				float a = ch[3][i];
				int al = level(a);
				if (al == 0) {
					px[i] = 0;
					continue;
				}
				px[i] = al << 24 | level(ch[0][i] / a) << 16 | level(ch[1][i] / a) << 8 | level(ch[2][i] / a);
			}
		}

		private static float[] kernel(double sigma) {
			int r = (int) Math.ceil(3 * sigma);
			float[] k = new float[2 * r + 1];
			double sum = 0;
			for (int i = -r; i <= r; i++) {
				double v = Math.exp(-i * i / (2 * sigma * sigma));
				k[i + r] = (float) v;
				sum += v;
			}
			for (int i = 0; i < k.length; i++) k[i] /= sum;
			return k;
		}

		private static void gaussian(float[] c, int w, int h, float[] k, boolean horizontal) {
			int r = k.length / 2;
			int len = horizontal ? w : h, lines = horizontal ? h : w;
			float[] line = new float[len];
			for (int l = 0; l < lines; l++) {
				for (int i = 0; i < len; i++) line[i] = c[horizontal ? l * w + i : i * w + l];
				for (int i = 0; i < len; i++) {
					float s = 0;
					for (int j = -r; j <= r; j++) {
						int x = Math.min(len - 1, Math.max(0, i + j));
						s += line[x] * k[j + r];
					}
					c[horizontal ? l * w + i : i * w + l] = s;
				}
			}
		}
	}

	private static String keys(List<Op> ops) {
		StringBuilder b = new StringBuilder();
		for (Op op : ops) {
			if (b.length() > 0) b.append(';');
			b.append(op.key);
		}
		return b.toString();
	}

	private static double[] rgb(ResolvedColor c) {
		return new double[] { c.getRed() / 255d, c.getGreen() / 255d, c.getBlue() / 255d };
	}

	/** Hue (0 to 1), saturation and lightness, as HSL has them */
	static double[] rgbToHsl(double r, double g, double b) {
		double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
		double l = (max + min) / 2;
		if (max == min) return new double[] { 0, 0, l };
		double d = max - min;
		double s = l <= 0.5 ? d / (max + min) : d / (2 - max - min);
		double h;
		if (max == r) h = (g - b) / d;
		else if (max == g) h = 2 + (b - r) / d;
		else h = 4 + (r - g) / d;
		h /= 6;
		if (h < 0) h += 1;
		return new double[] { h, s, l };
	}

	static void hslToRgb(double h, double s, double l, double[] out) {
		if (s == 0) {
			out[0] = out[1] = out[2] = l;
			return;
		}
		double m2 = l <= 0.5 ? l * (1 + s) : l + s - l * s;
		double m1 = 2 * l - m2;
		out[0] = clamp(hue(m1, m2, h + 1 / 3d));
		out[1] = clamp(hue(m1, m2, h));
		out[2] = clamp(hue(m1, m2, h - 1 / 3d));
	}

	private static double hue(double m1, double m2, double h) {
		h = h % 1.0;
		if (h < 0) h += 1;
		if (h < 1 / 6d) return m1 + (m2 - m1) * h * 6;
		if (h < 0.5) return m2;
		if (h < 2 / 3d) return m1 + (m2 - m1) * (2 / 3d - h) * 6;
		return m1;
	}
}
