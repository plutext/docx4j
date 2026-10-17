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
	 * The result's metadata (REQ-801): {@code fields_update_on_open} (a {@link Boolean}, the
	 * settings' {@code w:updateFields}). The document properties join it in CR-046 phase 2 (REQ-515).
	 */
	public Map<String, Object> metadata() { return metadata; }

	/** The options the result was made with. */
	public TextOptions options() { return options; }
}
