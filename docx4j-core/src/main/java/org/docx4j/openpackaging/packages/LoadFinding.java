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
package org.docx4j.openpackaging.packages;

import java.util.Objects;

/**
 * What a load read around in a package (CR-075 D2): a kind under the WD's code, a selector and a detail.
 * The text model raises each as a warning, and the inspector reports it.
 *
 * @since 17.3.6 (CR-075 phase 1)
 */
public final class LoadFinding {

	/** Two zip entries of one name, read under {@link org.docx4j.openpackaging.io3.RepairMode#WORD}: selector the name as the first spells it, detail the number of entries (WD REQ-206). */
	public static final String DUPLICATE_ENTRY = "package.duplicate_entry";
	/** Two officeDocument relationships, read under {@link org.docx4j.openpackaging.io3.RepairMode#WORD}: selector the part read, the last relationship's target; detail the number of relationships (WD REQ-206's NOTE). */
	public static final String MULTIPLE_MAIN_PARTS = "package.multiple_main_parts";

	/** A relationships part in another namespace, read as Word's repair reads it, every relationship by its local name; selector the part, detail how many. Nothing is lost. */
	public static final String RELATIONSHIPS_NAMESPACE = "package.relationships_namespace";
	/** A relationships part that could not be read: selector its name, detail the number of relationships kept (WD REQ-801). */
	public static final String PART_NOT_READ = "part.not_read";
	/**
	 * An External officeDocument relationship beside an internal one, dropped by docx4j's own repair under
	 * {@link org.docx4j.openpackaging.io3.RepairMode#ALL}, the URL never fetched: selector the part read, detail the URL as written.
	 * @since 17.3.6 (CR-075 phase 5)
	 */
	public static final String EXTERNAL_MAIN_PART = "package.external_main_part";

	/** WD REQ-207's {@code repaired}: none, or a repair of Word's followed, or one of docx4j's own. */
	public static final String REPAIRED_NONE = "none", REPAIRED_WORD = "word", REPAIRED_OWN = "own";

	/** What these findings say was repaired at the load, as REQ-207's {@code repaired} has it: {@code own} over {@code word} over {@code none}. @since 17.3.6 */
	public static String repaired(java.util.List<LoadFinding> findings) {
		String repaired = REPAIRED_NONE;
		for (LoadFinding f : findings) {
			if (EXTERNAL_MAIN_PART.equals(f.kind)) return REPAIRED_OWN;
			if (DUPLICATE_ENTRY.equals(f.kind) || MULTIPLE_MAIN_PARTS.equals(f.kind) || RELATIONSHIPS_NAMESPACE.equals(f.kind)) repaired = REPAIRED_WORD;
		}
		return repaired;
	}

	private final String kind;
	private final String selector;
	private final String detail;

	public LoadFinding(String kind, String selector, String detail) {
		this.kind = Objects.requireNonNull(kind);
		this.selector = selector;
		this.detail = detail;
	}

	public String getKind() { return kind; }
	public String getSelector() { return selector; }
	public String getDetail() { return detail; }

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LoadFinding)) return false;
		LoadFinding f = (LoadFinding) o;
		return kind.equals(f.kind) && Objects.equals(selector, f.selector) && Objects.equals(detail, f.detail);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, selector, detail);
	}

	@Override
	public String toString() {
		return kind + (selector == null ? "" : " (" + selector + ")") + (detail == null ? "" : " [" + detail + "]");
	}
}
