/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.model.images;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.Theme;
import org.docx4j.model.color.ThemeColors;
import org.junit.Test;

/**
 * {@link PictureRecolour} against Word: CR-084 phase 0's probes (129 of them, the 12 VML ones since phase 3, on four
 * calibration pictures of 256
 * patches each), the colours Word's PDF drew for each patch (read by {@code CR-084-notes/read_recolour.py}; the table
 * {@code images/recolour/word-patches.tsv}, written by {@code CR-084-notes/build_test_table.py}).
 *
 * <p>The tolerance is phase 1's gate (CR-084 §6): 2 levels of 255; 7 for {@code tint} and for a semi-transparent
 * pixel's colour, before or after (Word stores pictures premultiplied: {@code alphaCeiling} makes a pixel at alpha 17
 * opaque in a colour 7 levels off); 5 where Word re-encoded the picture as a JPEG. A colour is
 * compared where Word's alpha is at least a quarter. Not compared, as departures from Word decided in phase 0: {@code hsl},
 * which docx4j draws as the standard has it and Word draws black (D2); and {@code clrChange} on a semi-transparent
 * pixel, which Word's premultiplied storage makes miss for some alphas.</p>
 */
public class PictureRecolourWordTest {

	private static final String NS = " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";

	static CTBlip blip(String children) throws Exception {
		String xml = "<pic:pic" + NS + "><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"p\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"rId1\">" + children + "</a:blip><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"914400\" cy=\"914400\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>";
		return ((Pic) XmlUtils.unwrap(XmlUtils.unmarshalString(xml))).getBlipFill().getBlip();
	}

	/** A v:imagedata with these attributes */
	static org.docx4j.vml.CTImageData imageData(String attributes) throws Exception {
		String xml = "<v:imagedata xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"rId1\" o:title=\"\" "
				+ attributes + "/>";
		return (org.docx4j.vml.CTImageData) XmlUtils.unwrap(XmlUtils.unmarshalString(xml));
	}

	private static List<String[]> table(String name) throws Exception {
		List<String[]> rows = new ArrayList<String[]>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(
				PictureRecolourWordTest.class.getClassLoader().getResourceAsStream("images/recolour/" + name), StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null;) {
				if (line.startsWith("#") || line.isEmpty()) continue;
				rows.add(line.split("\t", -1));
			}
		}
		return rows;
	}

	/** The patches as ARGB; a patch not in the picture (outside a crop) as {@link #ABSENT} */
	private static long[] argb(String patches) {
		String[] s = patches.trim().split(" ");
		long[] out = new long[s.length];
		for (int i = 0; i < s.length; i++) {
			if (s[i].equals("-")) {
				out[i] = ABSENT;
				continue;
			}
			long v = Long.parseLong(s[i], 16); // RRGGBBAA
			out[i] = (v & 0xFF) << 24 | v >>> 8;
		}
		return out;
	}

	/** Not a colour: opaque white is -1 as an int */
	private static final long ABSENT = -1L;

	/** The 256 patches as a 16 by 16 picture, a pixel a patch */
	static BufferedImage picture(long[] patches) {
		BufferedImage im = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < 256; i++) im.setRGB(i % 16, i / 16, (int) patches[i]);
		return im;
	}

	@Test
	public void wordsPhase0ProbesWithinTheGatesTolerance() throws Exception {
		Map<String, long[]> sources = new HashMap<String, long[]>();
		for (String[] r : table("sources.tsv")) sources.put(r[0], argb(r[1]));
		// the probes' theme is Office 2023's, docx4j's default
		ThemeColors theme = ThemeColors.of((Theme) null);
		List<String> failures = new ArrayList<String>();
		StringBuilder summary = new StringBuilder();
		int probes = 0, compared = 0;
		for (String[] r : table("word-patches.tsv")) {
			if (r[0].equals("probe")) continue;
			String probe = r[0], note = r[1], blipXml = r[4];
			if (blipXml.contains("<a:hsl ")) continue; // D2: the standard's, not Word's (black)
			boolean jpeg = r[3].equals("DCTDecode");
			boolean tint = blipXml.contains("<a:tint ");
			boolean clrChange = blipXml.contains("<a:clrChange");
			long[] src = sources.get(r[2]);
			long[] word = argb(r[5]);
			// a VML probe's v:imagedata attributes (phase 3), else a DrawingML probe's a:blip children
			PictureRecolour rc = blipXml.startsWith("vml:") ? PictureRecolour.ofVml(imageData(blipXml.substring(4)))
					: PictureRecolour.of(blip(blipXml), theme);
			BufferedImage out = rc.apply(picture(src), 0);
			double worst = 0, worstAlpha = 0;
			probes++;
			for (int i = 0; i < 256; i++) {
				if (word[i] == ABSENT) continue;
				int ours = out.getRGB(i % 16, i / 16);
				int wa = (int) (word[i] >>> 24), oa = ours >>> 24, sa = (int) (src[i] >>> 24);
				worstAlpha = Math.max(worstAlpha, Math.abs(wa - oa));
				if (Math.abs(wa - oa) > 2) failures.add(probe + " patch " + i + " alpha " + oa + ", Word " + wa);
				if (wa < 64) continue;
				if (clrChange && sa != 0 && sa != 255) continue; // Word's premultiplied storage misses some alphas
				// semi-transparent in the source or in Word's picture: Word's premultiplied storage
				double tol = tint || wa < 255 || sa < 255 ? 7 : (jpeg ? 5 : 2);
				for (int shift = 16; shift >= 0; shift -= 8) {
					int d = (int) Math.abs(((ours >> shift) & 0xFF) - ((word[i] >> shift) & 0xFF));
					worst = Math.max(worst, d);
					if (d > tol) {
						failures.add(String.format(Locale.ROOT, "%s (%s) patch %d: ours %08X, Word %08X, source %08X", probe, note, i, ours,
								word[i], src[i]));
						break;
					}
				}
				compared++;
			}
			summary.append(String.format(Locale.ROOT, "%s %-50s colour %3.0f alpha %3.0f%n", probe, note, worst, worstAlpha));
		}
		assertTrue("too few probes: " + probes, probes > 100);
		assertTrue(failures.size() + " patches off Word (of " + compared + "):\n" + String.join("\n", failures.subList(0, Math.min(40, failures.size())))
				+ "\n" + summary, failures.isEmpty());
	}

	/**
	 * {@code clrChange}'s tolerance against Word (phase 1b, round cr084-clrchange-tol, 19 probes): whether Word took each
	 * patch (cleared it, or turned it blue), against {@link PictureRecolour}'s rule for the picture's format. The only
	 * patches let differ are a JPEG's within 2 levels of the boundary, where Word's decoder and the one that read the
	 * source (the table's) part by up to 2 levels.
	 */
	@Test
	public void clrChangeTakesWhatWordTakes() throws Exception {
		List<String> failures = new ArrayList<String>();
		int probes = 0, patches = 0, excused = 0;
		for (String[] r : table("clrchange-word.tsv")) {
			if (r[0].equals("probe")) continue;
			String probe = r[0], format = r[2];
			long[] src = argb(r[4]), word = argb(r[5]);
			CTBlip blip = blip(r[3]);
			PictureRecolour rc = PictureRecolour.of(blip, (ThemeColors) null);
			org.docx4j.model.color.ResolvedColor from = org.docx4j.model.color.DrawingMLColors.resolve(
					((org.docx4j.dml.CTColorChangeEffect) blip.getAlphaBiLevelOrAlphaCeilingOrAlphaFloor().get(0)).getClrFrom(), null);
			BufferedImage out = rc.apply(picture(src), 0, format);
			probes++;
			for (int i = 0; i < 256; i++) {
				if (word[i] == ABSENT) continue;
				int ours = out.getRGB(i % 16, i / 16);
				boolean wordTook = (word[i] >>> 24) < 128 || isBlue(word[i]) && !isBlue(src[i]);
				boolean weTook = (ours >>> 24) < 128 || isBlue(ours) && !isBlue(src[i]);
				patches++;
				if (wordTook == weTook) continue;
				int d = Math.max(Math.abs((int) ((src[i] >> 16) & 0xFF) - from.getRed()),
						Math.max(Math.abs((int) ((src[i] >> 8) & 0xFF) - from.getGreen()), Math.abs((int) (src[i] & 0xFF) - from.getBlue())));
				if (format.equals("jpeg") && Math.abs(d - 16.5) <= 2) {
					excused++;
					continue;
				}
				failures.add(String.format(Locale.ROOT, "%s (%s) patch %d, %d levels off: Word %s, docx4j %s", probe, r[1], i, d,
						wordTook ? "took it" : "left it", weTook ? "took it" : "left it"));
			}
		}
		assertTrue("probes " + probes, probes == 19);
		assertTrue(failures.size() + " of " + patches + " patches (" + excused + " excused at a JPEG's boundary):\n"
				+ String.join("\n", failures.subList(0, Math.min(30, failures.size()))), failures.isEmpty());
	}

	private static boolean isBlue(long argb) {
		return (argb & 0xFF) > 200 && ((argb >> 16) & 0xFF) < 60 && ((argb >> 8) & 0xFF) < 60;
	}
}
