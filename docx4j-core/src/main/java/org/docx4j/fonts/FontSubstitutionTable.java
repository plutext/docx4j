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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The substitutes docx4j draws a document font with when the machine lacks it, and what
 * a user can do about it: {@code font-substitutes.xml}, beside
 * {@code word-line-metrics.properties}.
 *
 * <p>Until 17.2.0 this knowledge was in the comments and the code of
 * {@link Mapper#addMetricallyCompatibleSubstitutes()},
 * {@link FontFallback} and {@link WidthFactors}, where nothing could read it: a report
 * could not cite it, and the TypeScript and Python ports, which treat this repository as
 * their oracle, could not either (CR-017 gap 4).  The passes now read the table, so
 * there is one place it lives.  The <em>measurements</em> stay in those classes'
 * comments, which is where the reader of the code will look for them; the table's
 * {@code error} attributes quote them.</p>
 *
 * <p>Three kinds of row:</p>
 * <ul>
 * <li><b>substitutes</b> - a document font and the open clones for it, best first.  The
 *     pass takes the first one this machine has
 *     ({@link Mapper#addFirstAvailableSubstitute}).</li>
 * <li><b>scriptSubstitutes</b> - what to try for one script (or the symbol and emoji
 *     blocks) before the class defaults, where the ordinary order picks a face whose
 *     widths are wrong; {@code name="*"} applies to every document font, otherwise the
 *     row applies to a document font whose name starts with the row's.  Read by
 *     {@link FontFallback#selectCovering}.</li>
 * <li><b>widthFactors</b> - what a substitute's advances are multiplied by so the run
 *     measures what the document font measures; read by {@link WidthFactors}.</li>
 * </ul>
 *
 * <p>And a catalogue of the substitutes themselves - licence, the docx4j font jar
 * carrying it where there is one, example distribution packages and the scripts it
 * covers - which is where a report's "what can I do about it" comes from.</p>
 *
 * <p>The file is parsed once, lazily, with a plain DOM parser: no JAXB context and no
 * generated classes, since nothing marshals it.</p>
 *
 * @since 17.2.0
 */
public final class FontSubstitutionTable {

	private static final Logger log = LoggerFactory.getLogger(FontSubstitutionTable.class);

	static final String RESOURCE = "font-substitutes.xml";

	private FontSubstitutionTable() {}

	/** One open substitute for a document font, with what was measured of it. */
	public static final class Substitute {

		private final String font;
		private final String quality;
		private final String error;

		Substitute(String font, String quality, String error) {
			this.font = font;
			this.quality = quality;
			this.error = error;
		}

		/** The physical font's name, as {@link PhysicalFonts} keys it. */
		public String getFont() { return font; }

		/** {@code metric} (a metric-compatible clone: Word's advances), {@code measured}
		 *  (no clone exists; this face was measured against Word's own PDF and is the
		 *  closest), or {@code class} (a face of the same class only). */
		public String getQuality() { return quality; }

		/** What was measured, in the words of the comment it was extracted from; null
		 *  where nothing was (a class substitute). */
		public String getError() { return error; }

		@Override
		public String toString() {
			return font + " (" + quality + (error==null ? "" : ": " + error) + ")";
		}
	}

	/** A document font and its substitutes, best first. */
	public static final class Row {

		private final String documentFont;
		private final String script;
		private final List<Substitute> substitutes;

		Row(String documentFont, String script, List<Substitute> substitutes) {
			this.documentFont = documentFont;
			this.script = script;
			this.substitutes = Collections.unmodifiableList(substitutes);
		}

		/** The document font, as a docx writes it; {@code *} on a script row which
		 *  applies to every font. */
		public String getDocumentFont() { return documentFont; }

		/** The coverage group this row is for - a {@link Character.UnicodeScript} name,
		 *  {@link FontFallback#SYMBOL_GROUP} or {@link FontFallback#EMOJI_GROUP} - or
		 *  null on a row of the substitution table proper. */
		public String getScript() { return script; }

		public List<Substitute> getSubstitutes() { return substitutes; }

		/** The substitutes' names, best first. */
		public String[] substituteNames() {
			String[] names = new String[substitutes.size()];
			for (int i=0; i<names.length; i++) names[i] = substitutes.get(i).getFont();
			return names;
		}

		/** This row's entry for that physical font, or null where the row does not name
		 *  it.  Matched on the name the table writes, with the FO layer's suffixes
		 *  stripped, and case-insensitively, since a face may be keyed under either its
		 *  full name or its family.  @since 17.2.0 */
		public Substitute substituteNamed(String physicalFontName) {
			if (physicalFontName==null) return null;
			String name = PhysicalFonts.stripSuffixes(physicalFontName).trim();
			for (Substitute s : substitutes) {
				if (s.getFont().equalsIgnoreCase(name)) return s;
			}
			return null;
		}

		/** Whether this row is for that document font: any font for {@code *}, else a
		 *  name starting with the row's (so "Cambria Math" takes Cambria's row, as the
		 *  code this was extracted from did). */
		public boolean matches(String documentFontName) {
			if ("*".equals(documentFont)) return true;
			if (documentFontName==null) return false;
			return documentFontName.trim().toLowerCase(Locale.ENGLISH)
					.startsWith(documentFont.toLowerCase(Locale.ENGLISH));
		}

		@Override
		public String toString() {
			return documentFont + (script==null ? "" : "/" + script) + " -> " + substitutes;
		}
	}

	/** Where to get a substitute, and what it carries. */
	public static final class Clone {

		private final String name;
		private final String alsoKnownAs;
		private final String licence;
		private final String jar;
		private final String packages;
		private final String scripts;

		Clone(String name, String alsoKnownAs, String licence, String jar, String packages, String scripts) {
			this.name = name;
			this.alsoKnownAs = alsoKnownAs;
			this.licence = licence;
			this.jar = jar;
			this.packages = packages;
			this.scripts = scripts;
		}

		public String getName() { return name; }

		/** The other name this face goes by where a document or a pass uses one
		 *  ("Carlito Regular" for Carlito); null where there is none. */
		public String getAlsoKnownAs() { return alsoKnownAs; }

		/** Read from the licence file the docx4j font jar ships, or from the packaging of
		 *  the copy installed on the machine the table was written on. */
		public String getLicence() { return licence; }

		/** The docx4j Maven artifact carrying it, or null: the answer docx4j controls. */
		public String getJar() { return jar; }

		/** Example distribution package names.  Data, not a promise: they go stale. */
		public String getPackages() { return packages; }

		/** The scripts this face carries. */
		public String getScripts() { return scripts; }

		@Override
		public String toString() { return name + " (" + licence + ")"; }
	}

	/** The substitution table, in order: a document font and its open clones, best
	 *  first.  {@link Mapper#addMetricallyCompatibleSubstitutes()} walks it. */
	public static List<Row> substitutes() {
		return table().substitutes;
	}

	/** The per-script rows, in the order they are tried. */
	public static List<Row> scriptSubstitutes() {
		return table().scriptSubstitutes;
	}

	/** The row for this document font, or null.  Case-insensitive, exact name (not the
	 *  prefix match the script rows use). */
	public static Row rowFor(String documentFont) {
		if (documentFont==null) return null;
		return table().byDocumentFont.get(documentFont.trim().toLowerCase(Locale.ENGLISH));
	}

	/**
	 * What the table records of this face as the substitute for that document font in
	 * that script, or null where it records nothing: the entry of the first script row
	 * which is for the font and the script and names the face.
	 *
	 * @since 17.2.0
	 */
	public static Substitute scriptSubstitute(String documentFont, String script, String physicalFontName) {
		if (script==null) return null;
		for (Row row : scriptSubstitutes()) {
			if (!script.equals(row.getScript()) || !row.matches(documentFont)) continue;
			Substitute s = row.substituteNamed(physicalFontName);
			if (s!=null) return s;
		}
		return null;
	}

	/** The catalogue entry for a physical font, by its name or the other name it goes
	 *  by; null where the table does not carry it. */
	public static Clone cloneNamed(String physicalFontName) {
		if (physicalFontName==null) return null;
		return table().clones.get(PhysicalFonts.stripSuffixes(physicalFontName)
				.trim().toLowerCase(Locale.ENGLISH));
	}

	/** Every catalogue entry, in the order the file lists them. */
	public static List<Clone> clones() {
		return table().cloneList;
	}

	/** A measured width factor: the document font, the substitute family it was measured
	 *  against, the face of the run it was measured on, and the factor.
	 *  {@link WidthFactors} applies them. */
	public static final class WidthFactor {

		private final String documentFont;
		private final String substituteFamily;
		private final boolean bold, italic;
		private final double factor;

		WidthFactor(String documentFont, String substituteFamily, boolean bold, boolean italic,
				double factor) {
			this.documentFont = documentFont;
			this.substituteFamily = substituteFamily;
			this.bold = bold;
			this.italic = italic;
			this.factor = factor;
		}

		public String getDocumentFont() { return documentFont; }

		/** Matched as a prefix of the physical font's name, the FO layer's suffixes
		 *  stripped: the table is keyed on the document font, and the factor holds only
		 *  where the substitute really is the family it was measured against. */
		public String getSubstituteFamily() { return substituteFamily; }

		/** Whether the row was measured on a <b>bold</b> run.  A family's weights are not
		 *  one another's width: Word's Tahoma Bold is 6 per cent wider than the Arimo Bold
		 *  which stands in for it where its regular is within 0.6 per cent of Arimo's.
		 *  @since 17.2.0 */
		public boolean isBold() { return bold; }

		/** Whether the row was measured on an <b>italic</b> run.  @since 17.2.0 */
		public boolean isItalic() { return italic; }

		public double getFactor() { return factor; }

		/** {@code regular}, {@code bold}, {@code italic} or {@code bolditalic}: the
		 *  {@code face} attribute's value.  @since 17.2.0 */
		public String getFace() {
			return bold ? (italic ? "bolditalic" : "bold") : (italic ? "italic" : "regular");
		}
	}

	/**
	 * The measured width factors, keyed by the lower-cased document font: <b>every</b> row
	 * for that font, in the order the file lists them, because one document font can want
	 * more than one.
	 *
	 * <p>A font can be drawn in two substitutes at once - Cambria's Latin goes to Caladea
	 * and its Greek to P052, and the two need opposite corrections - and a substitute's
	 * weights are not one another's width, so a family needs a row per face as well.  The
	 * map was one row per font until 17.2.0 (CR-001 batch 46 item 1), which could express
	 * neither.
	 *
	 * @since 17.2.0
	 */
	public static Map<String, List<WidthFactor>> widthFactors() {
		return table().widthFactors;
	}

	// ---- parsing

	private static final class Table {
		final List<Row> substitutes;
		final List<Row> scriptSubstitutes;
		final Map<String, Row> byDocumentFont;
		final Map<String, Clone> clones;
		final List<Clone> cloneList;
		final Map<String, List<WidthFactor>> widthFactors;
		Table(List<Row> substitutes, List<Row> scriptSubstitutes, Map<String, Row> byDocumentFont,
				Map<String, Clone> clones, List<Clone> cloneList, Map<String, List<WidthFactor>> widthFactors) {
			this.substitutes = Collections.unmodifiableList(substitutes);
			this.scriptSubstitutes = Collections.unmodifiableList(scriptSubstitutes);
			this.byDocumentFont = Collections.unmodifiableMap(byDocumentFont);
			this.clones = Collections.unmodifiableMap(clones);
			this.cloneList = Collections.unmodifiableList(cloneList);
			for (Map.Entry<String, List<WidthFactor>> e : widthFactors.entrySet()) {
				e.setValue(Collections.unmodifiableList(e.getValue()));
			}
			this.widthFactors = Collections.unmodifiableMap(widthFactors);
		}
	}

	private static volatile Table table;

	private static Table table() {
		Table t = table;
		if (t==null) {
			synchronized (FontSubstitutionTable.class) {
				t = table;
				if (t==null) {
					t = read();
					table = t;
				}
			}
		}
		return t;
	}

	private static Table read() {

		List<Row> substitutes = new ArrayList<Row>();
		List<Row> scriptSubstitutes = new ArrayList<Row>();
		Map<String, Row> byDocumentFont = new LinkedHashMap<String, Row>();
		Map<String, Clone> clones = new LinkedHashMap<String, Clone>();
		List<Clone> cloneList = new ArrayList<Clone>();
		Map<String, List<WidthFactor>> widthFactors = new LinkedHashMap<String, List<WidthFactor>>();

		try (InputStream is = FontSubstitutionTable.class.getResourceAsStream(RESOURCE)) {
			if (is==null) {
				/* Without it no font is substituted at all, so say so loudly rather than
				 * leaving a user to wonder why every missing font reaches FOP. */
				log.error(RESOURCE + " is not on the classpath; docx4j has no substitutes"
						+ " for the fonts this machine lacks");
				return new Table(substitutes, scriptSubstitutes, byDocumentFont, clones, cloneList, widthFactors);
			}
			org.w3c.dom.Document doc = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(is);
			Element root = doc.getDocumentElement();

			for (Element fonts : children(root, "substitutes")) {
				for (Element font : children(fonts, "font")) {
					Row row = row(font);
					if (row==null) continue;
					substitutes.add(row);
					byDocumentFont.put(row.getDocumentFont().toLowerCase(Locale.ENGLISH), row);
				}
			}
			for (Element fonts : children(root, "scriptSubstitutes")) {
				for (Element font : children(fonts, "font")) {
					Row row = row(font);
					if (row!=null && row.getScript()!=null) scriptSubstitutes.add(row);
				}
			}
			for (Element list : children(root, "widthFactors")) {
				for (Element wf : children(list, "widthFactor")) {
					String font = attr(wf, "font");
					String family = attr(wf, "substituteFamily");
					String factor = attr(wf, "factor");
					if (font==null || family==null || factor==null) continue;
					String face = attr(wf, "face");
					face = face==null ? "regular" : face.trim().toLowerCase(Locale.ENGLISH);
					boolean bold = face.startsWith("bold");
					boolean italic = face.endsWith("italic");
					if (!("regular".equals(face) || "bold".equals(face) || "italic".equals(face)
							|| "bolditalic".equals(face))) {
						log.error(RESOURCE + ": " + font + " has width factor face '" + face
								+ "'; expected regular, bold, italic or bolditalic");
						continue;
					}
					try {
						widthFactors
								.computeIfAbsent(font.trim().toLowerCase(Locale.ENGLISH),
										k -> new ArrayList<WidthFactor>())
								.add(new WidthFactor(font.trim(), family.trim(), bold, italic,
										Double.parseDouble(factor.trim())));
					} catch (NumberFormatException e) {
						log.error(RESOURCE + ": " + font + " has width factor '" + factor + "'");
					}
				}
			}
			for (Element list : children(root, "clones")) {
				for (Element c : children(list, "clone")) {
					String name = attr(c, "name");
					if (name==null || name.trim().length()==0) continue;
					Clone clone = new Clone(name.trim(), attr(c, "alsoKnownAs"), attr(c, "licence"),
							attr(c, "jar"), attr(c, "packages"), attr(c, "scripts"));
					cloneList.add(clone);
					clones.put(clone.getName().toLowerCase(Locale.ENGLISH), clone);
					if (clone.getAlsoKnownAs()!=null) {
						clones.put(clone.getAlsoKnownAs().trim().toLowerCase(Locale.ENGLISH), clone);
					}
				}
			}
		} catch (Exception e) {
			log.error("Couldn't read " + RESOURCE + ": " + e.getMessage(), e);
		}
		return new Table(substitutes, scriptSubstitutes, byDocumentFont, clones, cloneList, widthFactors);
	}

	private static Row row(Element font) {
		String name = attr(font, "name");
		if (name==null || name.trim().length()==0) return null;
		List<Substitute> substitutes = new ArrayList<Substitute>();
		for (Element s : children(font, "substitute")) {
			String f = attr(s, "font");
			if (f==null || f.trim().length()==0) continue;
			substitutes.add(new Substitute(f.trim(), attr(s, "quality"), attr(s, "error")));
		}
		String script = attr(font, "script");
		return new Row(name.trim(), script==null ? null : script.trim(), substitutes);
	}

	private static List<Element> children(Element parent, String name) {
		List<Element> result = new ArrayList<Element>();
		NodeList nodes = parent.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n instanceof Element && name.equals(n.getNodeName())) result.add((Element)n);
		}
		return result;
	}

	private static String attr(Element e, String name) {
		return e.hasAttribute(name) ? e.getAttribute(name) : null;
	}
}
