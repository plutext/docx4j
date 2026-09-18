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
package org.docx4j.fonts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fonts read for their <b>metrics only</b>: docx4j may measure a document font from one of
 * these files, and never draws in it.
 *
 * <p>Some layout decisions turn on how wide the document's own face is, not on how wide the
 * substitute is - the width of a bullet label being the case this was built for. Word puts a
 * Symbol {@code arrowdblright} bullet 0.987 em wide at 11pt, 218 twips, and its numbering tab
 * therefore passes a stop at 1548 twips; the face this machine substitutes is 0.838 em, 184
 * twips, ends 11 twips short of that stop, and the tab takes it, putting the line's text
 * 8.25pt to the left of Word's (measured on a corpus CV, CR-001 batch 48 item 6 §6). Neither
 * a wider bound nor a narrower one closes that: Symbol's widest glyph is 1.042 em and
 * Wingdings' 1.443, so nothing but the font itself answers.</p>
 *
 * <p><b>Why metrics only.</b> Registering Symbol with {@link PhysicalFonts} would answer the
 * question and change a great deal else: every Symbol run in every document would be drawn in
 * it, and each such document's class (RULE-CLASSES) would change with it. A deployment may
 * hold a font file it may measure but not embed, or simply want its layout decisions to match
 * the authoring machine's without changing a glyph. So these faces are kept out of
 * {@code PhysicalFonts} entirely: nothing discovers them, nothing draws in them, and the only
 * thing that reads them is a caller which asks for a width.</p>
 *
 * <p>Configured by {@code docx4j.fonts.metricsOnly.dirs}: directories, separated by the
 * platform's path separator, each scanned (not recursively) for {@code .ttf}, {@code .otf}
 * and {@code .ttc} files. Off where the property is absent, which is the default.</p>
 *
 * @since 17.1.1 (CR-001 batch 49 item 2)
 */
public final class MetricsOnlyFonts {

	private static final Logger log = LoggerFactory.getLogger(MetricsOnlyFonts.class);

	/** Directories whose fonts are read for metrics and never drawn in; separated by
	 *  {@link File#pathSeparator}.  Absent by default. */
	public static final String DIRS = "docx4j.fonts.metricsOnly.dirs";

	private MetricsOnlyFonts() {}

	/** family name, lower-cased -> the plainest face found for it; null until scanned */
	private static Map<String, PhysicalFont> byFamily;

	/** What {@link #DIRS} said when the scan was made, so a test can change it. */
	private static String scanned;

	/**
	 * The face to measure this document font with, or null where no directory offers one.
	 *
	 * <p>Matched on the family name as a docx writes it ("Symbol", "Wingdings"),
	 * case-insensitively.</p>
	 */
	public static synchronized PhysicalFont get(String documentFont) {

		if (documentFont == null || documentFont.trim().length() == 0) return null;
		String dirs = Docx4jProperties.getProperty(DIRS, "").trim();
		if (dirs.length() == 0) {
			byFamily = null;
			scanned = null;
			return null;
		}
		if (byFamily == null || !dirs.equals(scanned)) {
			byFamily = scan(dirs);
			scanned = dirs;
		}
		return byFamily.get(documentFont.trim().toLowerCase());
	}

	/** Width in points of {@code text} in this document font at this size, or -1 where the
	 *  font is not one of these. */
	public static double widthPt(String text, String documentFont, double sizePt) {
		PhysicalFont pf = get(documentFont);
		if (pf == null) return -1;
		double w = TextMeasurer.widthPt(text, pf, sizePt);
		return w > 0 ? w : -1;
	}

	private static Map<String, PhysicalFont> scan(String dirs) {

		Map<String, PhysicalFont> found = new HashMap<String, PhysicalFont>();
		for (String dir : dirs.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
			File d = new File(dir.trim());
			if (dir.trim().length() == 0) continue;
			if (!d.isDirectory()) {
				log.warn(DIRS + ": not a directory, skipped: " + d);
				continue;
			}
			File[] files = d.listFiles();
			if (files == null) continue;
			for (File f : files) {
				String lower = f.getName().toLowerCase();
				if (!(lower.endsWith(".ttf") || lower.endsWith(".otf") || lower.endsWith(".ttc"))) continue;
				add(found, f);
			}
		}
		log.info(DIRS + ": " + found.size() + " families read for metrics only, from " + dirs);
		return found;
	}

	private static void add(Map<String, PhysicalFont> found, File f) {

		List<PhysicalFont> faces;
		try {
			// builds the PhysicalFont objects WITHOUT putting them in PhysicalFonts' map
			faces = PhysicalFonts.getPhysicalFont(null, f.toURI(), PhysicalFonts.fontResolver());
		} catch (Exception e) {
			log.warn("Couldn't read " + f + " for metrics: " + e.getMessage());
			return;
		}
		if (faces == null) return;
		for (PhysicalFont pf : faces) {
			if (pf == null || pf.getName() == null) continue;
			for (String key : keys(pf.getName())) {
				// the first file wins, so a directory listed earlier takes precedence
				if (!found.containsKey(key)) found.put(key, pf);
			}
		}
	}

	/** The names a document might call this face: its own ("Symbol"), and the family where
	 *  the face name carries a style ("Wingdings Regular" -> "wingdings"). */
	private static List<String> keys(String name) {
		List<String> out = new ArrayList<String>(2);
		String n = name.trim();
		out.add(n.toLowerCase());
		for (String style : new String[] { " Regular", " Book", " Roman", " Normal" }) {
			if (n.length() > style.length() && n.endsWith(style)) {
				out.add(n.substring(0, n.length() - style.length()).trim().toLowerCase());
				break;
			}
		}
		return out;
	}

	/** Forget the scan, so a test can change the property.  @since 17.1.1 */
	public static synchronized void reset() {
		byFamily = null;
		scanned = null;
	}
}
