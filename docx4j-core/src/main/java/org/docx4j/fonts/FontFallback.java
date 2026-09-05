/*
 *  Copyright 2007-2026, Plutext Pty Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Choosing a physical font for a document font we have no mapping for.
 *
 * <p>Until 17.0.5 an unmapped font fell back to whatever the document's default
 * font mapped to, which consults neither the font's class (a sans standing in for
 * a serif, or the other way about) nor whether the substitute can render the
 * characters at all: Georgian text in a font the box lacks came out as a row of
 * notdef boxes even where the box had Noto Sans Georgian installed.</p>
 *
 * <p>Two users:</p>
 * <ul>
 * <li>{@link Mapper#addClassBasedSubstitutes} maps what is left unmapped after the
 *     metrically-compatible table, so that the substitute is declared to FOP with
 *     everything else;</li>
 * <li>{@link RunFontSelector} as a last resort during conversion, per script segment.</li>
 * </ul>
 *
 * <p>Class information comes from FontSubstitutions.xml (the file
 * {@link BestMatchingMapper} uses for its explicit substitutions), falling back to
 * a heuristic on the font's name.</p>
 *
 * @since 17.0.5
 */
public class FontFallback {

	protected static Logger log = LoggerFactory.getLogger(FontFallback.class);

	/** The broad classes a stand-in can preserve. */
	public enum FontClass { SERIF, SANS, MONO, UNKNOWN }

	/** Scripts which do not identify a font of their own: no installed face is named
	 *  after them, so {@link #scriptWord} has nothing to look for and
	 *  {@link #warnNoCoverage} nothing to name. */
	private static final Set<Character.UnicodeScript> LATINISH = new LinkedHashSet<Character.UnicodeScript>(
			Arrays.asList(Character.UnicodeScript.LATIN, Character.UnicodeScript.COMMON,
					Character.UnicodeScript.INHERITED, Character.UnicodeScript.GREEK,
					Character.UnicodeScript.CYRILLIC, Character.UnicodeScript.UNKNOWN));

	/**
	 * Scripts every conventional stand-in covers, so that text using only these needs no
	 * coverage check at all.
	 *
	 * <p>Greek and Cyrillic are <b>not</b> among them, although they were until 17.0.6.
	 * A metric-compatible clone is chosen for its advance widths, and several of them
	 * carry the Latin alphabet alone: Caladea, which stands in for Cambria, has no Greek
	 * and no Cyrillic at all.  Measured on a Greek document set in Cambria, 48% of the
	 * glyphs painted were notdef, on 68 Word pages, while Carlito - already loaded for
	 * the same document's Calibri - covers both scripts.  Skipping the check for them
	 * meant the per-script pass ({@link RunFontSelector} glyph fallback) never ran on the
	 * text that most needed it.</p>
	 */
	private static final Set<Character.UnicodeScript> ALWAYS_COVERED = new LinkedHashSet<Character.UnicodeScript>(
			Arrays.asList(Character.UnicodeScript.LATIN, Character.UnicodeScript.COMMON,
					Character.UnicodeScript.INHERITED, Character.UnicodeScript.UNKNOWN));

	/** Whether a font used only with these code points needs a coverage-driven substitute
	 *  (as opposed to one chosen on class alone). */
	public static boolean needsCoverage(int[] codePoints) {
		if (codePoints==null) return false;
		for (int cp : codePoints) {
			if (!ALWAYS_COVERED.contains(scriptOf(cp))) return true;
		}
		return false;
	}

	public static Character.UnicodeScript scriptOf(int cp) {
		try {
			return Character.UnicodeScript.of(cp);
		} catch (IllegalArgumentException e) {
			return Character.UnicodeScript.UNKNOWN;
		}
	}

	// ---- FontSubstitutions.xml

	private static Map<String, org.docx4j.fonts.substitutions.FontSubstitutions.Replace> substitutions;

	/** The FontSubstitutions.xml entry for this font name, or null.  Keys there are the
	 *  font's name lower-cased with spaces and punctuation removed. */
	private static synchronized org.docx4j.fonts.substitutions.FontSubstitutions.Replace entryFor(String documentFontName) {

		if (documentFontName==null) return null;
		if (substitutions==null) {
			substitutions = new HashMap<String, org.docx4j.fonts.substitutions.FontSubstitutions.Replace>();
			try {
				jakarta.xml.bind.JAXBContext jc = jakarta.xml.bind.JAXBContext.newInstance(
						"org.docx4j.fonts.substitutions", FontFallback.class.getClassLoader());
				jakarta.xml.bind.Unmarshaller u = jc.createUnmarshaller();
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
				java.io.InputStream is = org.docx4j.utils.ResourceUtils.getResource(
						"org/docx4j/fonts/substitutions/FontSubstitutions.xml");
				org.docx4j.fonts.substitutions.FontSubstitutions fs
						= (org.docx4j.fonts.substitutions.FontSubstitutions)u.unmarshal(is);
				for (org.docx4j.fonts.substitutions.FontSubstitutions.Replace r : fs.getReplace()) {
					if (r.getName()!=null) substitutions.put(key(r.getName()), r);
				}
			} catch (Exception e) {
				log.warn("Couldn't read FontSubstitutions.xml: " + e.getMessage());
			}
		}
		org.docx4j.fonts.substitutions.FontSubstitutions.Replace r = substitutions.get(key(documentFontName));
		if (r!=null) return r;

		/* FontSubstitutions.xml knows font families, not the foundry and weight suffixes a
		 * document adds to them ("Calisto MT", "Segoe UI Light"), so try the family. */
		String[] words = documentFontName.trim().split("\\s+");
		for (int drop=1; drop<words.length; drop++) {
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<words.length-drop; i++) sb.append(words[i]);
			r = substitutions.get(key(sb.toString()));
			if (r!=null) return r;
		}
		return null;
	}

	/** The short key convention FontSubstitutions.xml uses. */
	private static String key(String fontName) {
		StringBuilder sb = new StringBuilder(fontName.length());
		for (int i=0; i<fontName.length(); i++) {
			char c = fontName.charAt(i);
			if (Character.isLetterOrDigit(c)) sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}

	/** Whether FontSubstitutions.xml calls this font condensed (Arial Narrow and the like):
	 *  no ordinary stand-in has its widths, and the corpus showed the document default is
	 *  the lesser evil, so these are deliberately left unmapped. */
	public static boolean isCondensed(String documentFontName) {
		org.docx4j.fonts.substitutions.FontSubstitutions.Replace r = entryFor(documentFontName);
		if (r!=null && r.getFontWidth()!=null && r.getFontWidth().toLowerCase().contains("condensed")) return true;
		String n = documentFontName==null ? "" : documentFontName.toLowerCase();
		return n.contains("narrow") || n.contains("condensed");
	}

	/**
	 * Families measured to be better off falling back to the document default than to a
	 * stand-in of their own class, because the stand-in's widths are further from theirs
	 * than the default's are.  (Condensed faces are the other such case; see
	 * {@link #isCondensed}.)  Kept short and evidence-based: each entry cost line parity
	 * on a real document when it was substituted.
	 */
	private static boolean leaveUnmapped(String documentFontName) {
		if (documentFontName==null) return false;
		String n = documentFontName.toLowerCase();
		// Lato is a narrow humanist sans; Arimo/Liberation Sans (Arial's widths) set a
		// Lato document a page longer than the document default did
		if (n.startsWith("lato")) return true;
		/* A PostScript name (TimesNewRomanPS-BoldMT, MyriadPro-Regular): no system has a
		 * family by that name, so Word doesn't resolve it either and falls back to the
		 * document default - and matching Word here means doing the same.  Measured: the
		 * class-correct substitute was further from Word's line breaks than the default. */
		return POSTSCRIPT_NAME.matcher(n).matches();
	}

	private static final java.util.regex.Pattern POSTSCRIPT_NAME = java.util.regex.Pattern.compile(
			".+-(bold|italic|bolditalic|regular|roman|light|medium|semibold|demibold|black|oblique)(mt|ps)?");

	/** The font's class, from FontSubstitutions.xml where it knows the font, else from its name. */
	public static FontClass classOf(String documentFontName) {

		FontClass fromTable = classFromSubstitutionsTable(documentFontName);
		return fromTable!=null ? fromTable : classFromName(documentFontName);
	}

	/** The class FontSubstitutions.xml gives this font, or null where it doesn't say. */
	private static FontClass classFromSubstitutionsTable(String documentFontName) {

		org.docx4j.fonts.substitutions.FontSubstitutions.Replace r = entryFor(documentFontName);
		if (r==null || r.getFontType()==null) return null;
		String type = r.getFontType().toLowerCase();
		// CJK, Symbol, Special, Decorative and Script faces have no stand-in of "their class"
		if (type.contains("cjk") || type.contains("symbol") || type.contains("special")
				|| type.contains("decorative") || type.contains("script") || type.contains("ctl")) {
			return FontClass.UNKNOWN;
		}
		if (type.contains("fixed") || type.contains("typewriter")) return FontClass.MONO;
		if (type.contains("sansserif")) return FontClass.SANS;
		if (type.contains("serif")) return FontClass.SERIF;
		return null;
	}

	/** The class to stand in for: the table's word, else what the name says for certain. */
	private static FontClass substitutionClass(String documentFontName) {

		FontClass fromTable = classFromSubstitutionsTable(documentFontName);
		return fromTable!=null ? fromTable : classFromName(documentFontName, false);
	}

	static FontClass classFromName(String documentFontName) {
		return classFromName(documentFontName, true);
	}

	/**
	 * Sans is tested before serif, so that "HelveticaNeue LT 55 Roman" is a sans.
	 *
	 * @param generic whether a name which only ends in "Sans" or "Serif" counts.  It does
	 *                when the question is what class an installed font is (Liberation
	 *                Sans, DejaVu Serif), but not when it is whether to stand in for a
	 *                document font: measured, that guess was worth less than the document
	 *                default (Gill Sans, a narrow humanist face, in Arimo's widths).
	 */
	static FontClass classFromName(String documentFontName, boolean generic) {

		if (documentFontName==null) return FontClass.UNKNOWN;
		String n = documentFontName.toLowerCase();
		// the open substitutes first, since a stand-in's own class is asked for as often
		// as a document font's (Cousine is Courier New's, Tinos Times New Roman's)
		for (String mono : new String[] { "cousine", "monospace", "courier", "consol", "typewriter",
				"terminal", "fixedsys", "inconsolata", "menlo" }) {
			if (n.contains(mono)) return FontClass.MONO;
		}
		// "mono" only as a word: Monotype Corsiva is a script face, not a monospace one
		for (String word : n.split("[^a-z0-9]+")) {
			if (word.equals("mono")) return FontClass.MONO;
		}
		// NB no bare "gothic": in a Microsoft font name it is as often Japanese (MS
		// Gothic, Yu Gothic) as it is a sans (Century Gothic, which FontSubstitutions.xml
		// knows).  "urw gothic" is the whole family name of the Avant Garde clone in the
		// URW base 35, which Century Gothic substitutes to, so it is not ambiguous.
		for (String sans : new String[] { "arimo", "carlito", "cantarell", "helvetica", "arial",
				"urw gothic",
				"verdana", "tahoma", "segoe", "gadugi", "trebuchet", "calibri", "grotesk",
				"grotesque", "futura", "frutiger", "myriad", "univers", "avenir", "lato", "roboto",
				"franklin", "open sans" }) {
			if (n.contains(sans)) return FontClass.SANS;
		}
		// "p052" and "c059" are the whole family names of the Palatino and Century
		// Schoolbook clones in the URW base 35 (Georgia, Book Antiqua and Palatino
		// Linotype substitute to P052), so nothing in their names says "serif".
		for (String serif : new String[] { "tinos", "caladea", "charter", "times", "georgia",
				"garamond", "palatino", "p052", "c059", "bookman", "book antiqua", "cambria",
				"constantia", "century", "baskerville", "caslon", "utopia", "minion", "sylfaen",
				"roman" }) {
			if (n.contains(serif)) return FontClass.SERIF;
		}
		if (generic) {
			if (n.contains("sans")) return FontClass.SANS;
			if (n.contains("serif")) return FontClass.SERIF;
		}
		return FontClass.UNKNOWN;
	}

	/** The families FontSubstitutions.xml offers for this font, in its order. */
	private static List<String> substituteNames(String documentFontName) {
		org.docx4j.fonts.substitutions.FontSubstitutions.Replace r = entryFor(documentFontName);
		List<String> result = new ArrayList<String>();
		if (r==null || r.getSubstFonts()==null) return result;
		for (String token : r.getSubstFonts().split(";")) {
			String t = key(token);
			if (t.length()>0) result.add(t);
		}
		return result;
	}

	/** Physical fonts under the short key convention, rebuilt when the discovered set grows. */
	private static Map<String, PhysicalFont> byKey = new HashMap<String, PhysicalFont>();
	private static int byKeySize = -1;

	private static synchronized PhysicalFont physicalFontByKey(String shortKey) {
		if (byKeySize != PhysicalFonts.getPhysicalFonts().size()) {
			byKey = new HashMap<String, PhysicalFont>();
			for (Map.Entry<String, PhysicalFont> e : PhysicalFonts.getPhysicalFonts().entrySet()) {
				byKey.put(key(e.getKey()), e.getValue());
			}
			byKeySize = PhysicalFonts.getPhysicalFonts().size();
		}
		return byKey.get(shortKey);
	}

	// ---- class defaults

	private static final String[] SANS_DEFAULTS = { "Arimo Regular", "Arimo", "Liberation Sans",
			"Nimbus Sans", "DejaVu Sans", "Noto Sans", "FreeSans" };
	private static final String[] SERIF_DEFAULTS = { "Tinos Regular", "Tinos", "Liberation Serif",
			"Nimbus Roman", "DejaVu Serif", "Noto Serif", "FreeSerif" };
	private static final String[] MONO_DEFAULTS = { "Cousine Regular", "Cousine", "Liberation Mono",
			"Nimbus Mono PS", "DejaVu Sans Mono", "Noto Mono", "FreeMono" };

	private static String[] defaultsFor(FontClass fontClass) {
		switch (fontClass) {
			case SANS: return SANS_DEFAULTS;
			case SERIF: return SERIF_DEFAULTS;
			case MONO: return MONO_DEFAULTS;
			default: return new String[0];
		}
	}

	/**
	 * A physical font of this document font's class, without regard to what it can
	 * render.  Used where the characters in question are Latin (or Greek/Cyrillic),
	 * which any of these covers.
	 *
	 * @return null where the font's class is unknown, or is condensed (see
	 *         {@link #isCondensed}), or nothing of that class is installed
	 */
	public static PhysicalFont selectByClass(String documentFontName) {

		if (isCondensed(documentFontName) || leaveUnmapped(documentFontName)) return null;
		FontClass fontClass = substitutionClass(documentFontName);
		if (fontClass==FontClass.UNKNOWN) return null;

		// FontSubstitutions.xml's own list first: it is ordered by closeness
		for (String candidate : substituteNames(documentFontName)) {
			PhysicalFont pf = physicalFontByKey(candidate);
			if (pf!=null && classOf(pf.getName())==fontClass) return pf;
		}
		for (String candidate : defaultsFor(fontClass)) {
			PhysicalFont pf = PhysicalFonts.get(candidate);
			if (pf!=null) return pf;
		}
		return null;
	}

	// ---- coverage

	/** Whether this font has a glyph for every one of these code points. */
	public static boolean covers(PhysicalFont pf, int[] codePoints) {
		if (pf==null || codePoints==null) return false;
		try {
			for (int cp : codePoints) {
				if (!GlyphCheck.hasCodepoint(pf, cp)) return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * A physical font which can render these code points, preferring one of the
	 * document font's own class.
	 *
	 * <p>Order: the substitutes FontSubstitutions.xml lists for the font, then the
	 * class defaults, then the Noto/DejaVu face for the script, then any installed
	 * font which covers them.</p>
	 *
	 * @return null where nothing installed covers them (the caller keeps whatever it
	 *         would have done; {@link #warnNoCoverage} says so once)
	 */
	public static PhysicalFont selectCovering(String documentFontName, int[] codePoints) {

		if (codePoints==null || codePoints.length==0) return null;

		FontClass fontClass = classOf(documentFontName);

		List<PhysicalFont> candidates = new ArrayList<PhysicalFont>();
		for (String name : measuredForScript(documentFontName, codePoints)) {
			PhysicalFont pf = PhysicalFonts.get(name);
			if (pf!=null) candidates.add(pf);
		}
		for (String name : substituteNames(documentFontName)) {
			PhysicalFont pf = physicalFontByKey(name);
			if (pf!=null) candidates.add(pf);
		}
		for (String name : defaultsFor(fontClass)) {
			PhysicalFont pf = PhysicalFonts.get(name);
			if (pf!=null) candidates.add(pf);
		}
		for (PhysicalFont pf : candidates) {
			if (covers(pf, codePoints)) return pf;
		}

		// Noto/DejaVu for the script, eg Noto Sans Georgian.  The script's own name is a
		// good filter on a Linux box, where the per-script Noto faces carry it.
		String scriptWord = scriptWord(codePoints);
		if (scriptWord!=null) {
			List<PhysicalFont> scriptFonts = new ArrayList<PhysicalFont>();
			for (Map.Entry<String, PhysicalFont> e : PhysicalFonts.getPhysicalFonts().entrySet()) {
				if (e.getKey().contains(scriptWord)) scriptFonts.add(e.getValue());
			}
			for (PhysicalFont pf : preferred(scriptFonts, fontClass)) {
				if (covers(pf, codePoints)) return pf;
			}
		}
		for (String name : new String[] { "Noto Sans", "Noto Serif", "DejaVu Sans", "DejaVu Serif",
				"FreeSerif", "FreeSans", "Arial Unicode MS" }) {
			PhysicalFont pf = PhysicalFonts.get(name);
			if (pf!=null && covers(pf, codePoints)) return pf;
		}

		// Last resort: anything installed which covers them.
		for (PhysicalFont pf : preferred(new ArrayList<PhysicalFont>(PhysicalFonts.getPhysicalFonts().values()), fontClass)) {
			if (covers(pf, codePoints)) return pf;
		}
		return null;
	}

	/**
	 * Substitutes measured against Word's own PDFs for one document font in one script,
	 * where the ordinary class-based order picks a face whose widths are wrong.
	 *
	 * <p>A document font may need a different stand-in per script - the class defaults
	 * are chosen for Latin - and a script's first covering face is not necessarily its
	 * closest.  Measured: Sylfaen's Georgian in DejaVu Serif comes out 8.6% wide (a line
	 * Word ends at x=525.5 ran to 546.6, 21pt past its right edge; over 82 exact-match
	 * lines of another document the ratio is Word/ours 0.876), where DejaVu Serif
	 * Condensed measures 0.900 - the only Georgian-covering face within 3%.  Noto Serif
	 * Georgian is 0.999 of DejaVu Serif, i.e. no better.</p>
	 *
	 * <p>Sylfaen's <em>Cyrillic</em> has no such answer here: Caladea measures closest to
	 * it (1.0288 against Tinos's 1.041) but has no Cyrillic at all, so Tinos stands.</p>
	 *
	 * @since 17.0.6
	 */
	private static List<String> measuredForScript(String documentFontName, int[] codePoints) {

		List<String> result = new ArrayList<String>();
		if (documentFontName==null) return result;
		String name = documentFontName.trim().toLowerCase();
		boolean georgian = false;
		for (int cp : codePoints) {
			if (scriptOf(cp)==Character.UnicodeScript.GEORGIAN) { georgian = true; break; }
		}
		if (georgian && name.startsWith("sylfaen")) {
			result.add("DejaVu Serif Condensed");
			result.add("DejaVu Serif Condensed Book");
		}
		return result;
	}

	/** Regular weights before bold/italic/light variants, and the document font's class first. */
	private static List<PhysicalFont> preferred(List<PhysicalFont> fonts, final FontClass fontClass) {
		List<PhysicalFont> sorted = new ArrayList<PhysicalFont>(fonts);
		java.util.Collections.sort(sorted, new java.util.Comparator<PhysicalFont>() {
			public int compare(PhysicalFont a, PhysicalFont b) {
				int d = rank(a, fontClass) - rank(b, fontClass);
				if (d!=0) return d;
				return String.valueOf(a.getName()).compareTo(String.valueOf(b.getName()));
			}
		});
		return sorted;
	}

	private static int rank(PhysicalFont pf, FontClass fontClass) {
		String n = String.valueOf(pf.getName()).toLowerCase();
		int rank = 0;
		if (n.contains("bold") || n.contains("italic") || n.contains("oblique")
				|| n.contains("light") || n.contains("thin") || n.contains("black")
				|| n.contains("medium") || n.contains("semi") || n.contains("extra")) {
			rank += 4;
		}
		if (fontClass!=FontClass.UNKNOWN && classFromName(n)!=fontClass) rank += 2;
		if (!n.startsWith("noto") && !n.startsWith("dejavu") && !n.startsWith("free")) rank += 1;
		return rank;
	}

	/** The lower-case name a per-script font would carry, eg "georgian" - from the first
	 *  code point whose script is not shared with Latin. */
	private static String scriptWord(int[] codePoints) {
		for (int cp : codePoints) {
			Character.UnicodeScript script = scriptOf(cp);
			if (LATINISH.contains(script)) continue;
			return script.name().toLowerCase().replace('_', ' ');
		}
		return null;
	}

	// ---- warning once per font and script

	private static final Set<String> warned = ConcurrentHashMap.newKeySet();

	/**
	 * Say once, per document font and script, that nothing installed can render it -
	 * rather than once per glyph, which is what FOP would otherwise do.
	 */
	public static void warnNoCoverage(String documentFontName, int[] codePoints) {

		Character.UnicodeScript script = Character.UnicodeScript.UNKNOWN;
		int lowest = Integer.MAX_VALUE, highest = 0;
		for (int cp : codePoints) {
			Character.UnicodeScript s = scriptOf(cp);
			if (!LATINISH.contains(s)) script = s;
			if (cp<lowest) lowest = cp;
			if (cp>highest) highest = cp;
		}
		if (!warned.add(documentFontName + "/" + script)) return;
		log.warn("No installed font can render the " + script + " text this document sets in "
				+ documentFontName + " (U+" + Integer.toHexString(lowest).toUpperCase()
				+ "..U+" + Integer.toHexString(highest).toUpperCase()
				+ "); it will render as notdef.  Install a font for that script.");
	}

	private FontFallback() {}
}
