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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.chart.render.api.Effects;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTEffectList;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A picture's effects against Word's (CR-081 phase 1): each probe's effect pictures, read from Word 16.0.20326's PDFs
 * of phase 0 (CR-081-notes/export_effects.py), against {@link Effects}'s, drawn for the same picture: which pictures,
 * under or over the drawing or in its place, where (in points from the drawing's box), at what resolution, in what
 * colour, and the alpha along three rows and three columns of each, sampled at Word's own pixels.
 */
public class EffectsTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"";
	private static final double W = 144, H = 108;

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static final Map<String, BufferedImage> PICTURES = new HashMap<String, BufferedImage>();
	private static ThemeColors theme;

	@BeforeClass
	public static void read() throws Exception {
		ClassLoader cl = EffectsTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-effects.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		for (String[] p : new String[][] { { "photo.png", "shapes/photo.png" }, { "lineart.png", "effects/lineart.png" },
				{ "lineart-alpha.png", "effects/lineart-alpha.png" } }) {
			try (InputStream in = cl.getResourceAsStream(p[1])) {
				PICTURES.put(p[0], javax.imageio.ImageIO.read(in));
			}
		}
		// the probes' documents: Word's default (2023) theme
		try (InputStream in = cl.getResourceAsStream("org/docx4j/openpackaging/parts/WordprocessingML/theme-2023.xml")) {
			theme = ThemeColors.of((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshal(in)));
		}
	}

	// ------------------------------------------------------------------ drawing a probe

	private static final Pattern EFFECTS = Pattern.compile("<a:effectLst>.*</a:effectLst>", Pattern.DOTALL);
	private static final Pattern CROP = Pattern.compile("<a:srcRect([^/]*)/>");

	static Effects.Result draw(Map<String, Object> p) throws Exception {
		String xml = (String) p.get("xml");
		Matcher m = EFFECTS.matcher(xml);
		assertTrue(m.find());
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS + "><wps:spPr>" + m.group()
				+ "</wps:spPr><wps:bodyPr/></wps:wsp>", Context.jc, CTWordprocessingShape.class);
		CTEffectList list = wsp.getSpPr().getEffectLst();
		BufferedImage img = PICTURES.get(p.get("object"));
		double[] crop = crop(xml);
		int sx0 = (int) Math.round(crop[0] * img.getWidth()), sy0 = (int) Math.round(crop[1] * img.getHeight());
		int sx1 = (int) Math.round((1 - crop[2]) * img.getWidth()), sy1 = (int) Math.round((1 - crop[3]) * img.getHeight());
		return Effects.draw(list, theme, W, H, g -> g.drawImage(img, 0, 0, (int) W, (int) H, sx0, sy0, sx1, sy1, null), 0);
	}

	private static double[] crop(String xml) {
		double[] c = new double[4];
		Matcher m = CROP.matcher(xml);
		if (m.find()) {
			String[] names = { "l", "t", "r", "b" };
			for (int i = 0; i < 4; i++) {
				Matcher a = Pattern.compile("\\b" + names[i] + "=\"(\\d+)\"").matcher(m.group(1));
				if (a.find()) c[i] = Integer.parseInt(a.group(1)) / 100000.0;
			}
		}
		return c;
	}

	// ------------------------------------------------------------------ comparing

	/** The alpha (0 to 255) of a picture at a point of the drawing's frame, bilinear, 0 outside it */
	static double alphaAt(Effects.Picture pic, double x, double y) {
		BufferedImage im = pic.getImage();
		double sx = im.getWidth() / pic.getPlace().width, sy = im.getHeight() / pic.getPlace().height;
		double fx = (x - pic.getPlace().x) * sx - 0.5, fy = (y - pic.getPlace().y) * sy - 0.5;
		int x0 = (int) Math.floor(fx), y0 = (int) Math.floor(fy);
		double tx = fx - x0, ty = fy - y0;
		return (1 - ty) * ((1 - tx) * a(im, x0, y0) + tx * a(im, x0 + 1, y0)) + ty * ((1 - tx) * a(im, x0, y0 + 1) + tx * a(im, x0 + 1, y0 + 1));
	}

	private static int a(BufferedImage im, int x, int y) {
		return x < 0 || y < 0 || x >= im.getWidth() || y >= im.getHeight() ? 0 : im.getRGB(x, y) >>> 24;
	}

	/** How one picture compares with Word's */
	static final class Compared {
		String probe, effect;
		double dx, dy, dw, dh, pptRatio, mean, max, wordMass;
		String colour, wordColour;
		double amax, wordAmax;

		@Override
		public String toString() {
			return String.format(Locale.ROOT, "%-9s %-10s place %+6.2f %+6.2f %+6.2f %+6.2f ppt x%.3f alpha mean %5.1f max %5.1f  amax %.3f/%.3f  %s/%s",
					probe, effect, dx, dy, dw, dh, pptRatio, mean, max, amax, wordAmax, colour, wordColour);
		}
	}

	static Compared compare(String probe, Effects.Picture pic, Map<String, Object> word) {
		Compared c = new Compared();
		c.probe = probe;
		c.effect = pic.getEffect();
		double wx = Json.number(word.get("x")), wy = Json.number(word.get("y")), ww = Json.number(word.get("w")), wh = Json.number(word.get("h"));
		c.dx = pic.getPlace().x - wx;
		c.dy = pic.getPlace().y - wy;
		c.dw = pic.getPlace().width - ww;
		c.dh = pic.getPlace().height - wh;
		Double wpw = Json.number(word.get("pw")), wph = Json.number(word.get("ph"));
		if (wpw == null) return c;
		c.pptRatio = pic.getPixelsPerPoint() / (wpw / ww);
		c.wordAmax = Json.number(word.get("amax")) == null ? 0 : Json.number(word.get("amax"));
		c.wordColour = (String) word.get("colour");
		c.wordMass = Json.number(word.get("mass")) == null ? 0 : Json.number(word.get("mass"));
		int peak = 0;
		long r = 0, g = 0, b = 0, n = 0;
		BufferedImage im = pic.getImage();
		for (int y = 0; y < im.getHeight(); y++) for (int x = 0; x < im.getWidth(); x++) peak = Math.max(peak, im.getRGB(x, y) >>> 24);
		for (int y = 0; y < im.getHeight(); y++) {
			for (int x = 0; x < im.getWidth(); x++) {
				int v = im.getRGB(x, y);
				if ((v >>> 24) > peak / 2) {
					r += (v >> 16) & 0xFF;
					g += (v >> 8) & 0xFF;
					b += v & 0xFF;
					n++;
				}
			}
		}
		c.amax = peak / 255.0;
		c.colour = n == 0 ? null : String.format("%02X%02X%02X", r / n, g / n, b / n);
		// each profile at its best of five phases, a Word pixel either way: a hard edge a pixel apart is not a departure
		double sum = 0, max = 0;
		int count = 0;
		for (String axis : new String[] { "rows", "cols" }) {
			Map<String, Object> profiles = Json.object(word.get(axis));
			if (profiles == null) continue;
			for (Map.Entry<String, Object> e : profiles.entrySet()) {
				double f = Double.parseDouble(e.getKey());
				List<Object> values = Json.list(e.getValue());
				double bestSum = Double.MAX_VALUE, bestMax = 0;
				for (double shift : new double[] { 0, -0.5, 0.5, -1, 1 }) {
					double s = 0, m = 0;
					for (int i = 0; i < values.size(); i++) {
						double x, y;
						if (axis.equals("rows")) {
							x = wx + (i + 0.5 + shift) * ww / wpw;
							y = wy + (Math.min(wph - 1, Math.floor(f * wph)) + 0.5) * wh / wph;
						} else {
							x = wx + (Math.min(wpw - 1, Math.floor(f * wpw)) + 0.5) * ww / wpw;
							y = wy + (i + 0.5 + shift) * wh / wph;
						}
						double d = Math.abs(alphaAt(pic, x, y) - Json.number(values.get(i)));
						s += d;
						m = Math.max(m, d);
					}
					if (s < bestSum) {
						bestSum = s;
						bestMax = m;
					}
				}
				sum += bestSum;
				max = Math.max(max, bestMax);
				count += values.size();
			}
		}
		c.mean = count == 0 ? 0 : sum / count;
		c.max = max;
		return c;
	}

	/** Word's pictures to compare: its images, less the drawing redrawn with its own transparency (no soft edge) */
	static List<Map<String, Object>> wordPictures(Map<String, Object> p, Effects.Layer layer) {
		List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		boolean softEdge = ((String) p.get("xml")).contains("softEdge");
		for (Object o : Json.list(p.get("effects"))) {
			Map<String, Object> e = Json.object(o);
			String role = (String) e.get("role");
			if (!role.equals(layer.name().toLowerCase(Locale.ROOT))) continue;
			if (layer == Effects.Layer.REPLACE && !softEdge) continue;
			out.add(e);
		}
		return out;
	}

	/** Probes phase 1 leaves out: a turned picture (docx4j draws a picture unturned; CR-081 phases 2 and 3) */
	static boolean inScope(Map<String, Object> p) {
		return !((String) p.get("note")).contains("turned");
	}

	/** Word's picture cut at the line, the reflection going up past the 60pt effect extent (the host's clip, findings §3) */
	static final java.util.Set<String> CUT_AT_THE_LINE = new java.util.HashSet<String>(java.util.Arrays.asList("fxr50-24", "fxr50-30"));

	static List<Compared> compareAll() throws Exception {
		List<Compared> out = new ArrayList<Compared>();
		for (Map<String, Object> p : PROBES) {
			if (!inScope(p)) continue;
			Effects.Result r = draw(p);
			for (Effects.Layer layer : Effects.Layer.values()) {
				List<Map<String, Object>> word = wordPictures(p, layer);
				List<Effects.Picture> mine = new ArrayList<Effects.Picture>();
				for (Effects.Picture pic : r.getPictures()) if (pic.getLayer() == layer) mine.add(pic);
				assertEquals(p.get("probe") + " " + layer + ": " + p.get("note"), word.size(), mine.size());
				for (int i = 0; i < word.size(); i++) out.add(compare((String) p.get("probe"), mine.get(i), word.get(i)));
			}
		}
		return out;
	}

	/**
	 * Alpha errors (mean, 0 to 255) past the rule, each understood: fxr50-11's reflection cuts off at stPos, 0.4pt from
	 * Word's cut, and a row of the profile lies in between; fx40-05's soft edge is inset 0.92 of its radius where the
	 * fit across the radii (0.82 to 0.92) is 0.88
	 */
	private static final Map<String, Double> KNOWN = new HashMap<String, Double>();
	static {
		KNOWN.put("fxr50-11", 10.0);
		KNOWN.put("fx40-05", 6.5);
	}

	// ------------------------------------------------------------------ D10: the cap

	private static CTEffectList list(String effects) throws Exception {
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS + "><wps:spPr><a:effectLst>" + effects
				+ "</a:effectLst></wps:spPr><wps:bodyPr/></wps:wsp>", Context.jc, CTWordprocessingShape.class);
		return wsp.getSpPr().getEffectLst();
	}

	private static final String BLACK = "<a:srgbClr val=\"000000\"><a:alpha val=\"40000\"/></a:srgbClr>";
	private static final Effects.Painter BOX = g -> {
		g.setColor(java.awt.Color.BLUE);
		g.fill(new java.awt.geom.Rectangle2D.Double(0, 0, 144, 108));
	};

	/** A drawing near 2^31 EMU a side with a shadow: past the cap, nothing drawn, reported, at once */
	@Test(timeout = 10000)
	public void aHugeExtentIsPastTheCap() throws Exception {
		double side = Integer.MAX_VALUE / 12700.0;
		for (String fx : new String[] { "<a:outerShdw blurRad=\"50800\" dist=\"38100\" dir=\"2700000\">" + BLACK + "</a:outerShdw>",
				"<a:glow rad=\"63500\">" + BLACK + "</a:glow>", "<a:innerShdw blurRad=\"63500\" dist=\"50800\">" + BLACK + "</a:innerShdw>",
				"<a:softEdge rad=\"63500\"/>", "<a:reflection blurRad=\"6350\" stA=\"52000\" endA=\"300\" endPos=\"35000\" dir=\"5400000\" sy=\"-100000\" algn=\"bl\"/>" }) {
			Effects.Result r = Effects.draw(list(fx), theme, side, side, g -> g.fillRect(0, 0, (int) side, (int) side), 0);
			assertTrue(fx, r.isEmpty());
			assertEquals(fx, java.util.Collections.singletonList(Effects.OVER_LIMIT), r.getProblems());
		}
	}

	/**
	 * Every radius, distance and scale at the schema's largest (and a scale of 0): each picture within the cap, at once;
	 * a picture past it is not made, and reported. A blur that wide Word makes coarse itself (k), so a shadow or a
	 * reflection is a few pixels; an inner shadow moved or blurred past the drawing covers it; a soft edge erodes all of
	 * it; a shadow scaled 21,474 times is past the cap.
	 */
	@Test(timeout = 20000)
	public void theSchemasLargestValuesStayWithinTheCap() throws Exception {
		String max = "27273042316900";
		for (String fx : new String[] { "<a:outerShdw blurRad=\"" + max + "\">" + BLACK + "</a:outerShdw>", "<a:glow rad=\"" + max + "\">" + BLACK + "</a:glow>",
				"<a:reflection blurRad=\"" + max + "\" dist=\"" + max + "\"/>", "<a:outerShdw dist=\"" + max + "\" sx=\"2147483647\">" + BLACK + "</a:outerShdw>",
				"<a:outerShdw sx=\"0\" sy=\"0\">" + BLACK + "</a:outerShdw>", "<a:innerShdw blurRad=\"" + max + "\" dist=\"" + max + "\">" + BLACK + "</a:innerShdw>",
				"<a:innerShdw blurRad=\"" + max + "\">" + BLACK + "</a:innerShdw>", "<a:softEdge rad=\"" + max + "\"/>",
				"<a:softEdge rad=\"" + max + "\"/><a:innerShdw dist=\"" + max + "\">" + BLACK + "</a:innerShdw>" }) {
			Effects.Result r = Effects.draw(list(fx), theme, 144, 108, BOX, 0);
			for (Effects.Picture p : r.getPictures()) {
				assertTrue(fx, p.getImage().getWidth() <= Effects.MAX_SIDE && p.getImage().getHeight() <= Effects.MAX_SIDE);
				assertTrue(fx, (long) p.getImage().getWidth() * p.getImage().getHeight() <= Effects.MAX_PIXELS);
			}
			for (String code : r.getProblems()) assertEquals(fx, Effects.OVER_LIMIT, code);
		}
		// a shadow scaled 21,474 times: past the cap
		Effects.Result r = Effects.draw(list("<a:outerShdw sx=\"2147483647\" sy=\"2147483647\">" + BLACK + "</a:outerShdw>"), theme, 144, 108, BOX, 0);
		assertTrue(r.isEmpty());
		assertEquals(java.util.Collections.singletonList(Effects.OVER_LIMIT), r.getProblems());
	}

	/** A large drawing is made coarser to stay within the cap, not dropped: 3000pt a side, at most 4096 pixels a side */
	@Test
	public void theCapMakesALargePictureCoarser() throws Exception {
		Effects.Result r = Effects.draw(list("<a:outerShdw blurRad=\"50800\" dist=\"38100\" dir=\"2700000\">" + BLACK + "</a:outerShdw>"), theme, 3000, 3000,
				g -> g.fillRect(0, 0, 3000, 3000), 0);
		assertEquals(1, r.getPictures().size());
		Effects.Picture p = r.getPictures().get(0);
		assertTrue(p.getImage().getWidth() <= Effects.MAX_SIDE && p.getImage().getHeight() <= Effects.MAX_SIDE);
		assertTrue((long) p.getImage().getWidth() * p.getImage().getHeight() <= Effects.MAX_PIXELS);
		assertTrue(p.getPixelsPerPoint() < Effects.BASE_PIXELS_PER_POINT && p.getPixelsPerPoint() >= Effects.MIN_PIXELS_PER_POINT);
	}

	/** The property bounds the resolution (D3); unset, Word's 200 dpi */
	@Test
	public void thePropertyBoundsTheResolution() throws Exception {
		CTEffectList fx = list("<a:outerShdw blurRad=\"50800\" dist=\"38100\" dir=\"2700000\">" + BLACK + "</a:outerShdw>");
		assertEquals(Effects.BASE_PIXELS_PER_POINT, Effects.draw(fx, theme, 144, 108, BOX, 0).getPictures().get(0).getPixelsPerPoint(), 0.02);
		assertEquals(1, Effects.draw(fx, theme, 144, 108, BOX, 1).getPictures().get(0).getPixelsPerPoint(), 0.02);
	}

	/** Nothing to draw it with, or nothing to draw: no pictures, no problems */
	@Test
	public void nothingDrawnWithoutADrawing() throws Exception {
		CTEffectList fx = list("<a:outerShdw blurRad=\"50800\">" + BLACK + "</a:outerShdw>");
		assertTrue(Effects.draw(null, theme, 144, 108, BOX, 0).isEmpty());
		assertTrue(Effects.draw(fx, theme, 0, 108, BOX, 0).isEmpty());
		assertTrue(Effects.draw(fx, theme, 144, 108, null, 0).isEmpty());
		assertTrue(Effects.draw(fx, theme, Double.NaN, 108, BOX, 0).getProblems().isEmpty());
	}

	@Test
	public void theEffectsAreWords() throws Exception {
		List<Compared> all = compareAll();
		if (Boolean.getBoolean("effects.report")) for (Compared c : all) System.out.println(c);
		assertEquals("effect pictures compared", 171, all.size());
		double sum = 0;
		for (Compared c : all) {
			String what = c.toString();
			boolean cut = CUT_AT_THE_LINE.contains(c.probe);
			// where: within a coarse pixel of Word's frame (2.16pt at a 32pt blur's k = 6), else a point
			assertTrue(what, Math.abs(c.dx) <= 1 && Math.abs(c.dw) <= 2.2);
			if (!cut) assertTrue(what, Math.abs(c.dy) <= 1 && Math.abs(c.dh) <= 2.2);
			if (c.pptRatio == 0) continue; // Word drew it as a vector fill (fxr11-11, an opaque shadow with no blur)
			assertEquals(what, 1, c.pptRatio, 0.01);
			if (!cut) assertEquals(what, c.wordAmax, c.amax, 0.025);
			if (!cut && c.colour != null && c.wordColour != null) {
				for (int i = 0; i < 6; i += 2) {
					assertEquals(what, Integer.parseInt(c.wordColour.substring(i, i + 2), 16), Integer.parseInt(c.colour.substring(i, i + 2), 16), 6);
				}
			}
			Double known = KNOWN.get(c.probe);
			assertTrue(what, c.mean <= (known != null ? known : 3.5));
			sum += c.mean;
		}
		assertTrue("mean alpha error " + sum / all.size(), sum / all.size() <= 1.0);
	}
}
