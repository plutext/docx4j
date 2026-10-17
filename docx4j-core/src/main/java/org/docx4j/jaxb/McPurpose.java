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

/**
 * What a reader selects a branch of {@code mc:AlternateContent} for (CR-046 section 2.2): the rule
 * differs, since a branch docx4j can read is not always one it can draw.
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public enum McPurpose {

	/**
	 * Text: the ISO rule (ISO/IEC 29500-3 section 9.3; WD REQ-302). The first {@code mc:Choice}
	 * each of whose {@code Requires} prefixes resolves, by namespace URI, to a namespace in the
	 * published understood set ({@link McSelection#understoodNamespaces()}), else the
	 * {@code mc:Fallback}, else, for an element with Choices only, the first Choice (WD REQ-305).
	 */
	TEXT,

	/**
	 * Drawing: {@code docx4j.jaxb.mc.preferChoice}, empty by default, so that the Fallback is
	 * drawn ({@link McSelection#selectedBranch(org.docx4j.mce.AlternateContent)}); the FO and
	 * HTML exporters' layout, and {@link McMode#READ} walks until CR-046 phase 3.
	 */
	DRAW,

	/** Every branch, for a mutator ({@link McMode#ALL}). */
	ALL
}
