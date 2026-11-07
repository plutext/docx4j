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
package org.docx4j.openpackaging.io3;

import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What a load does with a package Word opens only after its repair prompt (CR-075 section 4; the repair
 * policy's P4, the WD's {@code repair: word} when an engine builds it).
 *
 * <ul>
 * <li>{@link #NONE} (the default): such a package is refused, naming what is wrong.</li>
 * <li>{@link #WORD}: Word's prompt answered in advance. The package reads as Word's repair gives it, and
 * what was repaired is recorded ({@link org.docx4j.openpackaging.packages.OpcPackage#getLoadFindings()}).</li>
 * </ul>
 *
 * @since 17.3.6 (CR-075 phase 1)
 */
public enum RepairMode {

	NONE,

	WORD;

	private static final Logger log = LoggerFactory.getLogger(RepairMode.class);

	/** The property: {@code none} (the default) or {@code word}. */
	public static final String PROPERTY = "docx4j.openpackaging.load.repair";

	private static volatile String warnedAbout;

	/** The mode a value names ({@code none}, {@code word}; case-insensitive, trimmed), or null for any other value. */
	public static RepairMode parse(String value) {
		if (value == null) return null;
		String v = value.trim();
		if (v.equalsIgnoreCase("none")) return NONE;
		if (v.equalsIgnoreCase("word")) return WORD;
		return null;
	}

	/** The mode {@value #PROPERTY} names; {@link #NONE} where it is unset or names neither mode (logged once). */
	public static RepairMode fromProperty() {
		String value = Docx4jProperties.getProperty(PROPERTY);
		if (value == null || value.trim().isEmpty()) return NONE;
		RepairMode mode = parse(value);
		if (mode == null) {
			if (!value.equals(warnedAbout)) {
				warnedAbout = value;
				log.warn(PROPERTY + "=" + value + " names neither 'none' nor 'word'; using none");
			}
			return NONE;
		}
		return mode;
	}
}
