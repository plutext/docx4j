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
package org.docx4j.model.revisions;

import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;

/**
 * One story's content: a part's body, a header, a footer, a note, a comment, or a text box (CR-043
 * section 3.1). A paragraph join never crosses its boundary, and a move's halves pair within it.
 */
final class Story {

	final WordprocessingMLPackage pkg;
	final JaxbXmlPart<?> part;
	/** The live list of the story's block-level content. */
	final List<Object> root;
	/** What holds {@link #root}: a body, a header, a note, a {@code w:txbxContent}. */
	final Object rootOwner;
	/**
	 * True for a text box in a second branch of {@code mc:AlternateContent} (the {@code mc:Fallback}
	 * of the {@code mc:Choice} listed): its changes are twins, not listed on their own.
	 */
	final boolean twin;

	Story(WordprocessingMLPackage pkg, JaxbXmlPart<?> part, List<Object> root, Object rootOwner, boolean twin) {
		this.pkg = pkg;
		this.part = part;
		this.root = root;
		this.rootOwner = rootOwner;
		this.twin = twin;
	}
}
