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
package org.docx4j.jaxb;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What a load does with an {@code mc:AlternateContent} at block, row or cell level in
 * WordprocessingML (CR-051 section 3.2): in the body, a header or footer, a cell, a note, a
 * comment, a text box, a block-level content control or custom XML element, a table (among its
 * rows) or a row (among its cells).  Run- and paragraph-level elements are not governed: they
 * are kept whole in both modes, as since 3.3.8 and 17.2.0.
 *
 * <ul>
 * <li>{@link #WORD} (the default): right after a part is unmarshalled, each such element is
 * replaced by the content of the branch Word reads ({@link McSelection#selectedBranch(McPurpose,
 * AlternateContent) McPurpose.TEXT}: the first Choice docx4j understands, else the Fallback), so
 * the tree holds what Word's holds and a save writes what Word's save writes.  An element with no
 * understood Choice and no Fallback stays whole (WD REQ-305); one with no branch at all is
 * removed.  Each element taken out is kept on the part, with where it stood
 * ({@link org.docx4j.openpackaging.parts.JaxbXmlPart#getResolvedAlternateContent}).</li>
 * <li>{@link #PRESERVE}: the element stays whole in the tree, consumers choose a branch at use
 * (CR-021's rule) and a save writes every branch.  For the tools that must reproduce their input:
 * the anonymiser, diffx, an editor that saves after a small edit.  Until CR-051 phase 2, row- and
 * cell-level elements are resolved as in {@link #WORD} in this mode too.</li>
 * </ul>
 *
 * <p>The mode is the property {@value #PROPERTY} ({@code word} or {@code preserve}), which a
 * package's own setting overrides ({@link OpcPackage#setMcBlockLevel}); it is read when each part
 * is unmarshalled, so give it at load ({@code WordprocessingMLPackage.load(file, mode)},
 * {@code Docx4J.load(file, mode)}, {@code Load3.setMcBlockLevel}) rather than after.</p>
 *
 * @since 17.3.4 (CR-051 phase 1)
 */
public enum McBlockLevel {

	/** Each element resolved to the branch Word reads, on load. */
	WORD,

	/** Each element kept whole. */
	PRESERVE;

	private static final Logger log = LoggerFactory.getLogger(McBlockLevel.class);

	/** The property: {@code word} (the default) or {@code preserve}. */
	public static final String PROPERTY = "docx4j.jaxb.mc.blockLevel";

	private static volatile String warnedAbout;

	/**
	 * The mode a value names ({@code word}, {@code preserve}; case-insensitive, trimmed), or null for
	 * any other value.
	 */
	public static McBlockLevel parse(String value) {
		if (value == null) return null;
		String v = value.trim();
		if (v.equalsIgnoreCase("word")) return WORD;
		if (v.equalsIgnoreCase("preserve")) return PRESERVE;
		return null;
	}

	/** The mode {@value #PROPERTY} names; {@link #WORD} where it is unset or names neither mode (logged once). */
	public static McBlockLevel fromProperty() {
		String value = Docx4jProperties.getProperty(PROPERTY);
		if (value == null || value.trim().isEmpty()) return WORD;
		McBlockLevel mode = parse(value);
		if (mode == null) {
			if (!value.equals(warnedAbout)) {
				warnedAbout = value;
				log.warn(PROPERTY + "=" + value + " names neither 'word' nor 'preserve'; using word");
			}
			return WORD;
		}
		return mode;
	}

	/**
	 * The mode in force for a package: its own setting where one was given, else the property's.
	 * A null package (a part outside any package) answers with the property's.
	 */
	public static McBlockLevel of(OpcPackage pkg) {
		if (pkg != null && pkg.getMcBlockLevel() != null) return pkg.getMcBlockLevel();
		return fromProperty();
	}
}
