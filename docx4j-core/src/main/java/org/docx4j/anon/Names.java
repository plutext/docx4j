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
package org.docx4j.anon;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;

/**
 * The consistent renamings the anonymiser applies, shared by its visitors:
 * <ul>
 * <li>authors become {@code Author 1}, {@code Author 2}... in order of first
 * appearance, the same name wherever it occurs (comments, tracked changes,
 * people.xml), so who-replied-to-whom and which changes are one reviewer's
 * survive (CR-019 decision 2); initials become {@code A1}, {@code A2}...;</li>
 * <li>a bookmark, form-field or sequence name becomes {@code bm} plus a hash
 * of the name: no pre-pass is needed for a REF in a header to still point at
 * a bookmark in the body;</li>
 * <li>a custom style id becomes {@code s} plus a hash; built-in styles (a name
 * Word knows: KnownStyles.xml plus the document's own latent-style list) keep
 * their names and ids, because Word identifies them by name;</li>
 * <li>every date becomes {@link #FIXED_DATE};</li>
 * <li>in a workbook (CR-019 phase 3), sheets become {@code Sheet1}, {@code Sheet2}...
 * in workbook order (read up front, so a formula in any part maps the same way)
 * and a defined name becomes {@code n_} plus a hash - an underscore keeps it from
 * ever reading as a cell reference; the {@code _xlnm.} built-ins keep their names.</li>
 * </ul>
 *
 * @since 17.2.0
 */
public class Names {

	/** what every w:date, dateUtc and docProps date becomes */
	public static final XMLGregorianCalendar FIXED_DATE;
	static {
		try {
			FIXED_DATE = DatatypeFactory.newInstance().newXMLGregorianCalendar("2000-01-01T00:00:00Z");
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	public static XMLGregorianCalendar fixedDate() {
		return (XMLGregorianCalendar) FIXED_DATE.clone();
	}

	private final Map<String, String> authors = new LinkedHashMap<String, String>();
	private final Map<String, String> initials = new LinkedHashMap<String, String>();
	private final Set<String> builtInStyleNames = new HashSet<String>();
	private final Set<String> builtInStyleIds = new HashSet<String>();
	/** sheet name (lower-cased: Excel matches them case-insensitively) to Sheet<n> */
	private final Map<String, String> sheets = new LinkedHashMap<String, String>();

	/**
	 * @param pkg a docx (its own latent-style names join the built-in list) or, since
	 *            17.2.1, a pptx (no styles part: KnownStyles.xml alone)
	 */
	public Names(OpcPackage pkg) {
		for (Style s : StyleDefinitionsPart.getKnownStyles().values()) {
			if (s.getName() != null) builtInStyleNames.add(key(s.getName().getVal()));
			builtInStyleIds.add(s.getStyleId());
		}
		if (pkg instanceof WordprocessingMLPackage
				&& ((WordprocessingMLPackage) pkg).getMainDocumentPart() != null
				&& ((WordprocessingMLPackage) pkg).getMainDocumentPart().getStyleDefinitionsPart() != null) {
			try {
				Styles styles = ((WordprocessingMLPackage) pkg).getMainDocumentPart().getStyleDefinitionsPart().getContents();
				if (styles != null && styles.getLatentStyles() != null) {
					for (Styles.LatentStyles.LsdException e : styles.getLatentStyles().getLsdException()) {
						builtInStyleNames.add(key(e.getName()));
					}
				}
				if (styles != null) {
					for (Style s : styles.getStyle()) {
						if (s.getName() != null && isBuiltInStyleName(s.getName().getVal())) {
							builtInStyleIds.add(s.getStyleId());
						}
					}
				}
			} catch (Exception e) {
				// no styles part contents: only KnownStyles then
			}
		}
		if (pkg instanceof SpreadsheetMLPackage && ((SpreadsheetMLPackage) pkg).getWorkbookPart() != null) {
			try {
				org.xlsx4j.sml.Workbook wb = ((SpreadsheetMLPackage) pkg).getWorkbookPart().getContents();
				if (wb != null && wb.getSheets() != null) {
					int n = 0;
					for (org.xlsx4j.sml.Sheet sheet : wb.getSheets().getSheet()) {
						n++;
						if (sheet.getName() != null) sheets.put(key(sheet.getName()), "Sheet" + n);
					}
				}
			} catch (Exception e) {
				// no workbook contents: sheet names are then scrambled like any other text
			}
		}
	}

	/** Sheet<n> for a sheet of the workbook (by name, case-insensitively); null for an unknown name. */
	public String sheet(String name) {
		if (name == null) return null;
		return sheets.get(key(name));
	}

	/** true if the workbook has a sheet of this name */
	public boolean isSheet(String name) {
		return name != null && sheets.containsKey(key(name));
	}

	/** The anonymised defined name: the _xlnm. built-ins as they are, else n_ + hash. */
	public static String definedName(String name) {
		if (name == null) return null;
		if (name.startsWith("_xlnm.") || name.startsWith("_xlfn.")) return name;
		return "n_" + hash(name);
	}

	private static String key(String name) {
		return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
	}

	/** A style name Word ships (heading 1, Table Grid, ...): kept, since Word identifies built-in styles by name. */
	public boolean isBuiltInStyleName(String name) {
		return name != null && builtInStyleNames.contains(key(name));
	}

	/** The id of a built-in style in this document: kept. */
	public boolean isBuiltInStyleId(String styleId) {
		return styleId != null && builtInStyleIds.contains(styleId);
	}

	/** The anonymised id of a style: itself for a built-in, else s + hash. */
	public String styleId(String styleId) {
		if (styleId == null || isBuiltInStyleId(styleId)) return styleId;
		return "s" + hash(styleId);
	}

	/** The anonymised name of a bookmark, form field or sequence: bm + hash, stable across parts. */
	public static String identifier(String name) {
		if (name == null) return null;
		return "bm" + hash(name);
	}

	/** Author n, allotted in order of first appearance. */
	public String author(String author) {
		if (author == null) return null;
		String mapped = authors.get(author);
		if (mapped == null) {
			mapped = "Author " + (authors.size() + 1);
			authors.put(author, mapped);
		}
		return mapped;
	}

	/** An, allotted in order of first appearance. */
	public String initials(String value) {
		if (value == null) return null;
		String mapped = initials.get(value);
		if (mapped == null) {
			mapped = "A" + (initials.size() + 1);
			initials.put(value, mapped);
		}
		return mapped;
	}

	/** How many distinct authors were renamed. */
	public int authorCount() {
		return authors.size();
	}

	static String hash(String s) {
		return Integer.toUnsignedString(s.hashCode());
	}

}
