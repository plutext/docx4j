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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A text in the block model (WD sections 7.4 and 8): the blocks in reading order (the main story with
 * each text box after its anchor, the notes after it, headers and footers by section where included),
 * the comments, the warnings and the metadata. Plain text ({@link PlainText}) and the other
 * serialisations are written from it (REQ-202).
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextResult {

	/** {@link #metadata()}'s key for the settings' {@code w:updateFields} (REQ-506, REQ-801): a {@link Boolean}. */
	public static final String FIELDS_UPDATE_ON_OPEN = "fields_update_on_open";
	/** {@link #metadata()}'s key for the core properties (REQ-515): a map from each property's element name ({@code title}, {@code creator}, ...) to its value. */
	public static final String CORE = "core";
	/** {@link #metadata()}'s key for the application properties (REQ-515): a map from element name ({@code Company}, {@code Template}, ...) to value; the statistics left out. */
	public static final String APP = "app";
	/** {@link #metadata()}'s key for the custom properties (REQ-515): a map from each property's name to its typed value. */
	public static final String CUSTOM = "custom";

	private final List<TextBlock> blocks;
	private final Map<String, TextComment> comments;
	private final List<TextWarning> warnings;
	private final Map<String, Object> metadata;
	private final TextOptions options;

	TextResult(List<TextBlock> blocks, Map<String, TextComment> comments, List<TextWarning> warnings,
			Map<String, Object> metadata, TextOptions options) {
		this.blocks = Collections.unmodifiableList(blocks);
		this.comments = Collections.unmodifiableMap(new LinkedHashMap<String, TextComment>(comments));
		this.warnings = Collections.unmodifiableList(warnings);
		this.metadata = Collections.unmodifiableMap(metadata);
		this.options = options;
	}

	/** The blocks in reading order (REQ-501, REQ-502, REQ-518, REQ-606). Comments are not among them. */
	public List<TextBlock> blocks() { return blocks; }

	/** The comments the view includes, by {@code w:id}, in the comments part's order (REQ-504). */
	public Map<String, TextComment> comments() { return comments; }

	/** The warnings raised (REQ-801), never with the document's text (REQ-802). */
	public List<TextWarning> warnings() { return warnings; }

	/**
	 * The result's metadata (REQ-801, REQ-515), whatever the options: {@link #FIELDS_UPDATE_ON_OPEN},
	 * and the document properties under {@link #CORE}, {@link #APP} and {@link #CUSTOM}, each a map,
	 * present when the package has that part (the core and application properties in their schema's
	 * order, the custom ones in the part's). The statistics (pages, words,
	 * characters, lines, paragraphs) are left out: they are as of the last save, and depend on layout.
	 * Property values are document text, and are in the text stream only under {@code metadata: true}.
	 */
	public Map<String, Object> metadata() { return metadata; }

	/** The options the result was made with. */
	public TextOptions options() { return options; }
}
