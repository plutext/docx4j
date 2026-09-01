package org.docx4j.markdown.math;

import org.commonmark.node.CustomNode;

/**
 * Inline math: {@code $...$} or {@code \(...\)} (or {@code $$...$$} appearing
 * inline in a mixed line, in which case {@link #isDisplayHint()} is true).
 * The literal is the raw LaTeX source between the delimiters.
 */
public class InlineMath extends CustomNode {

	private final String literal;
	private final boolean displayHint;

	public InlineMath(String literal, boolean displayHint) {
		this.literal = literal;
		this.displayHint = displayHint;
	}

	public String getLiteral() {
		return literal;
	}

	/** True for {@code $$...$$} / {@code \[...\]} written inline. */
	public boolean isDisplayHint() {
		return displayHint;
	}

}
