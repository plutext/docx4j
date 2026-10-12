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
package org.docx4j.model.chart;

import java.awt.Font;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.Docx4jProperties;
import org.docx4j.dml.BaseStyles;
import org.docx4j.dml.FontCollection;
import org.docx4j.dml.Theme;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.apache.poi.sl.draw.Docx4jDrawFontManager;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fonts a chart's text is drawn in (CR-037): a chart names its faces as theme references
 * ({@code +mn-lt}, the theme's minor Latin font; {@code +mj-lt}, its major) or by name, and is
 * drawn in its host's theme (CR-035); the face is then an AWT font made from the very file
 * docx4j's font mapper gives FOP ({@link Docx4jDrawFontManager}), so that the widths a chart is
 * laid out with are the widths its text has in the PDF.  Calibri, for instance, is Carlito
 * (docx4j-export-fo-fonts-crosextra), whose advances are Calibri's.
 *
 * @since 17.3.3 (CR-037)
 */
public final class ChartFonts {

	private static final Logger log = LoggerFactory.getLogger(ChartFonts.class);

	private final String majorLatin;
	private final String minorLatin;
	private final Mapper mapper;
	private final Docx4jDrawFontManager manager;
	/** the faces this object has mapped */
	private final Set<String> mapped = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private final Map<String, Font> fonts = new ConcurrentHashMap<String, Font>();
	/** the faces each AWT family drawn stands for (Carlito: Calibri) */
	private final Map<String, Set<String>> facesByFamily = new ConcurrentHashMap<String, Set<String>>();

	private ChartFonts(String majorLatin, String minorLatin, Mapper mapper) {
		Docx4jProperties.DefaultTheme t = Docx4jProperties.getDefaultTheme();
		this.majorLatin = majorLatin == null || majorLatin.isEmpty() ? t.majorLatin() : majorLatin;
		this.minorLatin = minorLatin == null || minorLatin.isEmpty() ? t.minorLatin() : minorLatin;
		this.mapper = mapper == null ? new IdentityPlusMapper() : mapper;
		this.manager = new Docx4jDrawFontManager(this.mapper);
	}

	/**
	 * The theme's Latin faces, the fonts mapped by this mapper (a docx's own, so that a chart's
	 * text is set as the document's is); a mapper of its own where null
	 */
	public static ChartFonts of(String majorLatin, String minorLatin, Mapper mapper) {
		return new ChartFonts(majorLatin, minorLatin, mapper);
	}

	/** A theme's Latin faces (the default theme's where null) */
	public static ChartFonts of(Theme theme, Mapper mapper) {
		String major = null, minor = null;
		BaseStyles.FontScheme fs = theme == null || theme.getThemeElements() == null ? null
				: theme.getThemeElements().getFontScheme();
		if (fs != null) {
			major = latin(fs.getMajorFont());
			minor = latin(fs.getMinorFont());
		}
		return new ChartFonts(major, minor, mapper);
	}

	/**
	 * A chart's host's theme (a docx's main document's, a workbook's, a slide's master's) and,
	 * for a docx, the document's font mapper
	 */
	public static ChartFonts of(Part host) {
		OpcPackage pkg = host == null ? null : host.getPackage();
		Mapper mapper = null;
		if (pkg instanceof WordprocessingMLPackage) {
			try {
				mapper = ((WordprocessingMLPackage) pkg).getFontMapper();
			} catch (Exception e) {
				log.debug("no font mapper: " + e.getMessage());
			}
		}
		return of(theme(host), mapper);
	}

	private static String latin(FontCollection fc) {
		return fc == null || fc.getLatin() == null ? null : fc.getLatin().getTypeface();
	}

	private static Theme theme(Part host) {
		try {
			ThemePart tp = null;
			OpcPackage pkg = host == null ? null : host.getPackage();
			if (host instanceof SlidePart) {
				SlideLayoutPart layout = ((SlidePart) host).getSlideLayoutPart();
				Part master = layout == null ? null : layout.getSlideMasterPart();
				tp = master == null ? null : related(master);
			} else if (pkg instanceof WordprocessingMLPackage) {
				tp = ((WordprocessingMLPackage) pkg).getMainDocumentPart().getThemePart();
			} else if (pkg instanceof SpreadsheetMLPackage) {
				tp = ((SpreadsheetMLPackage) pkg).getWorkbookPart().getThemePart();
			}
			return tp == null ? null : tp.getContents();
		} catch (Exception e) {
			log.warn("Couldn't read the chart's host's theme: " + e.getMessage());
			return null;
		}
	}

	private static ThemePart related(Part part) {
		RelationshipsPart rp = part.getRelationshipsPart();
		Relationship rel = rp == null ? null : rp.getRelationshipByType(Namespaces.THEME);
		Part p = rel == null ? null : rp.getPart(rel);
		return p instanceof ThemePart ? (ThemePart) p : null;
	}

	/** The theme's major (heading) Latin face */
	public String getMajorLatin() {
		return majorLatin;
	}

	/** The theme's minor (body) Latin face, a chart's text's where it names none */
	public String getMinorLatin() {
		return minorLatin;
	}

	/**
	 * The face a typeface names: {@code +mn-lt} and {@code +mj-lt} (and none) the theme's,
	 * anything else as written
	 */
	public String face(String typeface) {
		if (typeface == null || typeface.isEmpty() || "+mn-lt".equals(typeface)) return minorLatin;
		if ("+mj-lt".equals(typeface)) return majorLatin;
		return typeface;
	}

	/** The AWT font for a typeface (as {@link #face} resolves it) at a size in points */
	public Font font(String typeface, boolean bold, boolean italic, double size) {
		String face = face(typeface);
		map(face);
		String key = face + "|" + bold + "|" + italic;
		Font base = fonts.computeIfAbsent(key, k -> manager.createAWTFont(null, () -> face, 1, bold, italic));
		if (!face.equalsIgnoreCase(base.getFamily())) {
			facesByFamily.computeIfAbsent(base.getFamily(), k -> Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()))
					.add(face);
		}
		return base.deriveFont((float) size);
	}

	/**
	 * The faces the AWT families this object has made fonts of stand for: Carlito, Calibri
	 * (the family an SVG of the chart names, and the face the chart names)
	 */
	Map<String, Set<String>> facesByFamily() {
		return facesByFamily;
	}

	/**
	 * A face the mapper hasn't mapped yet, mapped as a docx's fonts are: a chart in a docx can
	 * name a face the document's text doesn't use (Calibri in an Aptos document), and its
	 * drawing, and FOP drawing its SVG, need it mapped too
	 */
	private void map(String face) {
		if (mapper.get(face) != null || !mapped.add(face)) return;
		Set<String> one = Collections.singleton(face);
		try {
			synchronized (mapper) {
				mapper.populateFontMappings(one, null);
				mapper.addMetricallyCompatibleSubstitutes();
				if (mapper.wantsClassBasedSubstitutes()) mapper.addClassBasedSubstitutes(one);
				mapper.addMapperSubstitutes(one, null);
				mapper.addWordDefaultSubstitutes(one, null);
				mapper.addNoBoldFaceAliases(one);
			}
		} catch (Exception e) {
			log.warn("Couldn't map the chart font " + face + ": " + e.getMessage());
		}
	}
}
