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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A comment (WD REQ-504): its author, date and blocks, the comment it replies to, and whether it is
 * resolved ({@code w15:done}). Plain text renders a comment at its reference, replies after it.
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextComment {

	final String id;
	String author;
	String initials;
	String date;
	String parentId;
	boolean resolved;
	final List<TextBlock> blocks = new ArrayList<TextBlock>();
	final List<TextComment> replies = new ArrayList<TextComment>();

	TextComment(String id) {
		this.id = id;
	}

	/** The comment's {@code w:id}. */
	public String getId() { return id; }
	public String getAuthor() { return author; }
	public String getInitials() { return initials; }
	/** The date as the package wrote it, or null. */
	public String getDate() { return date; }
	/** The {@code w:id} of the comment this one replies to ({@code w15:paraIdParent}), or null. */
	public String getParentId() { return parentId; }
	/** True when the comment is resolved ({@code w15:done}); a comment like any other otherwise. */
	public boolean isResolved() { return resolved; }
	public List<TextBlock> getBlocks() { return Collections.unmodifiableList(blocks); }
	/** The replies, in the comments part's order. */
	public List<TextComment> getReplies() { return Collections.unmodifiableList(replies); }

	/** The comment's text: its blocks a line apart. */
	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < blocks.size(); i++) {
			if (i > 0) sb.append('\n');
			sb.append(blocks.get(i).getText());
		}
		return sb.toString();
	}
}
