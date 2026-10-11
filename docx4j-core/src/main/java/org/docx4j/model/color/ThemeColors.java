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
package org.docx4j.model.color;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.dml.BaseStyles;
import org.docx4j.dml.CTBaseStylesOverride;
import org.docx4j.dml.CTColor;
import org.docx4j.dml.CTColorMapping;
import org.docx4j.dml.CTColorMappingOverride;
import org.docx4j.dml.CTColorScheme;
import org.docx4j.dml.CTStyleMatrix;
import org.docx4j.dml.Theme;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.ThemeOverridePart;
import org.docx4j.openpackaging.parts.PresentationML.HandoutMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A theme's colour scheme and the colour map that leads to it: all a colour reference needs.
 * Immutable; each {@code with...} returns a new one.  Which theme and which map apply depends on
 * the host (CR-034 §2.1): {@link #of(WordprocessingMLPackage)}, {@link #of(SpreadsheetMLPackage)},
 * {@link #of(Part)} for a slide, layout, master or notes part, and {@link #forChart(Chart)} for a
 * chart in any of them - drawn in its host's theme (charts plan, finding 7).
 *
 * <p>The scheme's slots are {@code dk1}, {@code lt1}, {@code dk2}, {@code lt2},
 * {@code accent1} to {@code accent6}, {@code hlink} and {@code folHlink}; the map takes
 * {@code bg1}, {@code tx1}, {@code bg2}, {@code tx2} (and, in principle, the accents and
 * hyperlinks) to a slot.  A slot holding a system colour is the machine's colour, not its
 * {@code lastClr} (CR-034 §11).  A package with no theme part gets the theme Office supplies:
 * the bundled one {@code docx4j.fonts.defaultTheme} names.</p>
 *
 * @since 17.3.3 (CR-034)
 */
public final class ThemeColors {

	private static final Logger log = LoggerFactory.getLogger(ThemeColors.class);

	static final String[] SLOTS = { "dk1", "lt1", "dk2", "lt2", "accent1", "accent2", "accent3", "accent4",
			"accent5", "accent6", "hlink", "folHlink" };
	private static final String[] MAPPED = { "bg1", "tx1", "bg2", "tx2", "accent1", "accent2", "accent3", "accent4",
			"accent5", "accent6", "hlink", "folHlink" };

	private final Map<String, ResolvedColor> slots;
	private final Map<String, String> map;
	private final CTStyleMatrix formatScheme;
	private final ResolvedColor placeholder;

	private ThemeColors(Map<String, ResolvedColor> slots, Map<String, String> map, CTStyleMatrix formatScheme,
			ResolvedColor placeholder) {
		this.slots = slots;
		this.map = map;
		this.formatScheme = formatScheme;
		this.placeholder = placeholder;
	}

	// ------------------------------------------------------------------ making one

	/** The default colour map: bg1 to lt1, tx1 to dk1, bg2 to lt2, tx2 to dk2, the rest to themselves */
	static Map<String, String> defaultMap() {
		Map<String, String> m = new HashMap<>();
		m.put("bg1", "lt1");
		m.put("tx1", "dk1");
		m.put("bg2", "lt2");
		m.put("tx2", "dk2");
		for (int i = 4; i < MAPPED.length; i++) {
			m.put(MAPPED[i], MAPPED[i]);
		}
		return Collections.unmodifiableMap(m);
	}

	/**
	 * A theme with the default colour map.
	 *
	 * @param theme null for the theme docx4j supplies where a package has none
	 */
	public static ThemeColors of(Theme theme) {
		if (theme == null) theme = defaultTheme();
		BaseStyles elements = theme == null ? null : theme.getThemeElements();
		return new ThemeColors(scheme(elements == null ? null : elements.getClrScheme()), defaultMap(),
				elements == null ? null : elements.getFmtScheme(), null);
	}

	private static Map<String, ResolvedColor> scheme(CTColorScheme cs) {
		Map<String, ResolvedColor> m = new HashMap<>();
		if (cs != null) {
			CTColor[] c = { cs.getDk1(), cs.getLt1(), cs.getDk2(), cs.getLt2(), cs.getAccent1(), cs.getAccent2(),
					cs.getAccent3(), cs.getAccent4(), cs.getAccent5(), cs.getAccent6(), cs.getHlink(), cs.getFolHlink() };
			for (int i = 0; i < SLOTS.length; i++) {
				ResolvedColor r = c[i] == null ? null : DrawingMLColors.resolve(c[i], null);
				if (r != null) m.put(SLOTS[i], r);
			}
		}
		return Collections.unmodifiableMap(m);
	}

	private static Theme defaultTheme;
	private static String defaultThemeResource;

	/** The bundled theme docx4j.fonts.defaultTheme names (2023 unless set otherwise) */
	static synchronized Theme defaultTheme() {
		String resource = Docx4jProperties.getDefaultTheme().resource();
		if (defaultTheme == null || !resource.equals(defaultThemeResource)) {
			try (InputStream is = ThemeColors.class.getClassLoader().getResourceAsStream(resource)) {
				defaultTheme = (Theme) XmlUtils.unwrap(XmlUtils.unmarshal(is, Context.jcThemePart));
				defaultThemeResource = resource;
			} catch (Exception e) {
				log.error("Couldn't read the default theme " + resource + ": " + e.getMessage(), e);
				return null;
			}
		}
		return defaultTheme;
	}

	private static Theme theme(ThemePart part) {
		if (part == null) return null;
		try {
			return part.getContents();
		} catch (Exception e) {
			log.warn("Couldn't read " + part.getPartName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * A docx: the main document part's theme (or the default theme) and {@code w:clrSchemeMapping}
	 * from the settings, which applies to WordprocessingML theme colours and DrawingML scheme colours
	 * alike (CR-034 §11, P2).
	 */
	public static ThemeColors of(WordprocessingMLPackage pkg) {
		ThemeColors tc = of(theme(pkg.getMainDocumentPart().getThemePart()));
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		org.docx4j.wml.CTColorSchemeMapping m = null;
		try {
			m = dsp == null ? null : dsp.getContents().getClrSchemeMapping();
		} catch (Exception e) {
			log.warn("Couldn't read the settings part: " + e.getMessage());
		}
		return m == null ? tc : tc.withWordMapping(m);
	}

	/** An xlsx: the workbook's theme (or the default theme), with the default map for its drawings */
	public static ThemeColors of(SpreadsheetMLPackage pkg) {
		return of(theme(pkg.getWorkbookPart().getThemePart()));
	}

	/**
	 * The theme and map for a part: a slide (its master's theme, its master's {@code p:clrMap}, then
	 * its layout's {@code p:clrMapOvr}, then its own), a layout, a master, a notes slide, a notes or
	 * handout master; for a part of a docx or xlsx, its package's.
	 */
	public static ThemeColors of(Part part) {
		try {
			if (part instanceof SlidePart) {
				SlidePart slide = (SlidePart) part;
				SlideLayoutPart layout = slide.getSlideLayoutPart();
				ThemeColors tc = layout == null ? of((Theme) null) : of(layout);
				return tc.withOverride(slide.getContents().getClrMapOvr());
			}
			if (part instanceof SlideLayoutPart) {
				SlideLayoutPart layout = (SlideLayoutPart) part;
				SlideMasterPart master = layout.getSlideMasterPart();
				ThemeColors tc = master == null ? of((Theme) null) : of(master);
				return tc.withOverride(layout.getContents().getClrMapOvr());
			}
			if (part instanceof SlideMasterPart) {
				return of(themeOf(part)).withMapping(((SlideMasterPart) part).getContents().getClrMap());
			}
			if (part instanceof NotesSlidePart) {
				NotesSlidePart notes = (NotesSlidePart) part;
				NotesMasterPart master = notes.getNotesMasterPart();
				ThemeColors tc = master == null ? of((Theme) null) : of(master);
				return tc.withOverride(notes.getContents().getClrMapOvr());
			}
			if (part instanceof NotesMasterPart) {
				return of(themeOf(part)).withMapping(((NotesMasterPart) part).getContents().getClrMap());
			}
			if (part instanceof HandoutMasterPart) {
				return of(themeOf(part)).withMapping(((HandoutMasterPart) part).getContents().getClrMap());
			}
		} catch (Exception e) {
			log.warn("Couldn't read the colour map of " + part.getPartName() + ": " + e.getMessage());
			return of((Theme) null);
		}
		OpcPackage pkg = part.getPackage();
		if (pkg instanceof WordprocessingMLPackage) return of((WordprocessingMLPackage) pkg);
		if (pkg instanceof SpreadsheetMLPackage) return of((SpreadsheetMLPackage) pkg);
		return of((Theme) null);
	}

	/** the theme a master relates to */
	private static Theme themeOf(Part part) {
		RelationshipsPart rp = part.getRelationshipsPart();
		Relationship rel = rp == null ? null : rp.getRelationshipByType(Namespaces.THEME);
		Part p = rel == null ? null : rp.getPart(rel);
		return p instanceof ThemePart ? theme((ThemePart) p) : null;
	}

	// ------------------------------------------------------------------ deriving one

	/** This theme with a DrawingML colour map ({@code p:clrMap}, {@code c:clrMapOvr}); null leaves it */
	public ThemeColors withMapping(CTColorMapping m) {
		if (m == null) return this;
		Map<String, String> mm = new HashMap<>(map);
		put(mm, "bg1", m.getBg1() == null ? null : m.getBg1().value());
		put(mm, "tx1", m.getTx1() == null ? null : m.getTx1().value());
		put(mm, "bg2", m.getBg2() == null ? null : m.getBg2().value());
		put(mm, "tx2", m.getTx2() == null ? null : m.getTx2().value());
		put(mm, "accent1", m.getAccent1() == null ? null : m.getAccent1().value());
		put(mm, "accent2", m.getAccent2() == null ? null : m.getAccent2().value());
		put(mm, "accent3", m.getAccent3() == null ? null : m.getAccent3().value());
		put(mm, "accent4", m.getAccent4() == null ? null : m.getAccent4().value());
		put(mm, "accent5", m.getAccent5() == null ? null : m.getAccent5().value());
		put(mm, "accent6", m.getAccent6() == null ? null : m.getAccent6().value());
		put(mm, "hlink", m.getHlink() == null ? null : m.getHlink().value());
		put(mm, "folHlink", m.getFolHlink() == null ? null : m.getFolHlink().value());
		return new ThemeColors(slots, Collections.unmodifiableMap(mm), formatScheme, placeholder);
	}

	/** {@code a:overrideClrMapping} replaces the map; {@code a:masterClrMapping} (or null) keeps it */
	public ThemeColors withOverride(CTColorMappingOverride o) {
		return o == null || o.getOverrideClrMapping() == null ? this : withMapping(o.getOverrideClrMapping());
	}

	/** This theme with WordprocessingML's {@code w:clrSchemeMapping} (whose names differ: dark1, t1, ...) */
	public ThemeColors withWordMapping(org.docx4j.wml.CTColorSchemeMapping m) {
		if (m == null) return this;
		Map<String, String> mm = new HashMap<>(map);
		put(mm, "bg1", wordSlot(m.getBg1()));
		put(mm, "tx1", wordSlot(m.getT1()));
		put(mm, "bg2", wordSlot(m.getBg2()));
		put(mm, "tx2", wordSlot(m.getT2()));
		put(mm, "accent1", wordSlot(m.getAccent1()));
		put(mm, "accent2", wordSlot(m.getAccent2()));
		put(mm, "accent3", wordSlot(m.getAccent3()));
		put(mm, "accent4", wordSlot(m.getAccent4()));
		put(mm, "accent5", wordSlot(m.getAccent5()));
		put(mm, "accent6", wordSlot(m.getAccent6()));
		put(mm, "hlink", wordSlot(m.getHyperlink()));
		put(mm, "folHlink", wordSlot(m.getFollowedHyperlink()));
		return new ThemeColors(slots, Collections.unmodifiableMap(mm), formatScheme, placeholder);
	}

	private static String wordSlot(org.docx4j.wml.STColorSchemeIndex i) {
		return i == null ? null : WORD_NAMES.get(i.value());
	}

	private static void put(Map<String, String> m, String key, String slot) {
		if (slot != null) m.put(key, slot);
	}

	/** WordprocessingML's names for the scheme's slots */
	private static final Map<String, String> WORD_NAMES;
	static {
		Map<String, String> m = new HashMap<>();
		m.put("dark1", "dk1");
		m.put("light1", "lt1");
		m.put("dark2", "dk2");
		m.put("light2", "lt2");
		for (int i = 1; i <= 6; i++) m.put("accent" + i, "accent" + i);
		m.put("hyperlink", "hlink");
		m.put("followedHyperlink", "folHlink");
		WORD_NAMES = Collections.unmodifiableMap(m);
	}

	/** This theme with another colour scheme and format scheme (a theme override part's) */
	public ThemeColors withScheme(CTColorScheme scheme, CTStyleMatrix fmt) {
		return new ThemeColors(scheme == null ? slots : scheme(scheme), map, fmt == null ? formatScheme : fmt, placeholder);
	}

	/**
	 * A chart's colours: this (its host's) theme, under the chart's theme override part if it has
	 * one, and the chart's {@code c:clrMapOvr} if it states one.
	 */
	public ThemeColors forChart(Chart chart) {
		ThemeColors tc = this;
		try {
			RelationshipsPart rp = chart.getRelationshipsPart();
			Relationship rel = rp == null ? null : rp.getRelationshipByType(Namespaces.THEME_OVERRIDE);
			Part p = rel == null ? null : rp.getPart(rel);
			if (p instanceof ThemeOverridePart) {
				Object o = XmlUtils.unwrap(((ThemeOverridePart) p).getContents());
				if (o instanceof CTBaseStylesOverride) {
					tc = tc.withScheme(((CTBaseStylesOverride) o).getClrScheme(), ((CTBaseStylesOverride) o).getFmtScheme());
				}
			}
			tc = tc.withMapping(chart.getContents().getClrMapOvr());
		} catch (Exception e) {
			log.warn("Couldn't read " + chart.getPartName() + "'s theme override or colour map: " + e.getMessage());
		}
		return tc;
	}

	/** This theme with {@code phClr} standing for the given colour (a style reference's) */
	public ThemeColors withPlaceholder(ResolvedColor color) {
		return new ThemeColors(slots, map, formatScheme, color);
	}

	// ------------------------------------------------------------------ reading it

	/** A scheme slot's colour (dk1, lt1, ..., folHlink), not through the map */
	public ResolvedColor slot(String slot) {
		return slots.get(slot);
	}

	/**
	 * A DrawingML {@code schemeClr} value's colour: bg1, tx1, bg2, tx2 (and the accents and
	 * hyperlinks) through the map, dk1 ... lt2 directly, {@code phClr} the placeholder.
	 */
	public ResolvedColor schemeColor(String val) {
		if (val == null) return null;
		if ("phClr".equals(val)) return placeholder;
		String slot = map.get(val);
		return slots.get(slot == null ? val : slot);
	}

	/**
	 * A WordprocessingML {@code w:themeColor} value's colour: text1, background1, text2,
	 * background2 through the map, dark1 ... followedHyperlink directly; null for "none".
	 */
	public ResolvedColor wordThemeColor(String themeColor) {
		if (themeColor == null) return null;
		switch (themeColor) {
		case "text1": return schemeColor("tx1");
		case "background1": return schemeColor("bg1");
		case "text2": return schemeColor("tx2");
		case "background2": return schemeColor("bg2");
		default:
			String slot = WORD_NAMES.get(themeColor);
			return slot == null ? null : slots.get(slot);
		}
	}

	/**
	 * A SpreadsheetML {@code theme} index's colour.  Excel swaps the first two pairs: 0 is lt1, 1
	 * dk1, 2 lt2, 3 dk2, then accent1 to accent6, hlink, folHlink (CR-034 §11, P4).
	 */
	public ResolvedColor sheetThemeColor(int index) {
		String[] order = { "lt1", "dk1", "lt2", "dk2", "accent1", "accent2", "accent3", "accent4", "accent5", "accent6",
				"hlink", "folHlink" };
		return index < 0 || index >= order.length ? null : slots.get(order[index]);
	}

	/** The theme's format scheme, which style references index into; null where the theme has none */
	public CTStyleMatrix getFormatScheme() {
		return formatScheme;
	}

	/** {@code phClr}'s colour, where a style reference supplies one */
	public ResolvedColor getPlaceholder() {
		return placeholder;
	}
}
