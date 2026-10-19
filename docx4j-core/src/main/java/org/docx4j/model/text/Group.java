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

/**
 * An inspection finding's group for one output (WD REQ-905, CR-050 D14): what the finding means to
 * an agent holding that output's text. It is not a field of the finding: it follows from the finding
 * and the output's options, format and capabilities ({@link InspectFinding#group}).
 *
 * @since 17.3.4 (CR-050 phase 1)
 */
public enum Group {

	/** In the output, and not seen by a person (or seen differently, or possibly not). */
	UNSEEN,
	/** In the file, with text, and not in the output. */
	OMITTED,
	/** What Office would fetch or run: macros, external fields and relationships, scripts. */
	ACTIVE,
	/** The rest: an inventory item, or content in the output that a person sees. */
	INFO;

	/** The WD's identifier: {@code unseen}, {@code omitted}, {@code active} or {@code info}. */
	public String id() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}

	@Override
	public String toString() {
		return id();
	}
}
