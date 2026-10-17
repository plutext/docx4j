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
package org.docx4j.model.text;

import java.util.Locale;

/**
 * The three revision views of WD section 4 (REQ-401): the document as Word's Accept All leaves it,
 * as Reject All leaves it, and every run as the package holds it with its insertions and deletions
 * marked.
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public enum TextView {

	/** After Accept All (Word's No Markup; Office JS's {@code current}). The default. */
	ACCEPTED("accepted"),

	/** After Reject All (Word's Original). */
	ORIGINAL("original"),

	/** Every run as stored, insertions and deletions marked; no structural change resolved (All Markup). */
	MARKUP("markup");

	private final String wdName;

	TextView(String wdName) {
		this.wdName = wdName;
	}

	/** The view's name in the WD and in {@link TextOptions#of(java.util.Map)}. */
	public String wdName() {
		return wdName;
	}

	/**
	 * The view of a WD name: {@code accepted}, {@code original} or {@code markup}, and
	 * {@code current} as a synonym of {@code accepted} (WD section 4.1's note).
	 *
	 * @throws IllegalArgumentException for any other name
	 */
	public static TextView of(String name) {
		String n = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
		if (n.equals("current")) return ACCEPTED;
		for (TextView v : values()) if (v.wdName.equals(n)) return v;
		throw new IllegalArgumentException("view: '" + name + "' is not accepted, original, markup or current (WD REQ-401)");
	}
}
