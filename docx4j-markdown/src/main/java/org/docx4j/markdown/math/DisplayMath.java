package org.docx4j.markdown.math;

import org.commonmark.node.CustomBlock;

/**
 * Display math: a {@code $$ ... $$} (or {@code \[ ... \]}) block.  The
 * literal is the raw LaTeX source between the delimiters (lines joined
 * with \n).  Always rendered back as {@code $$ ... $$} — the canonical
 * form; {@code \[...\]} input normalizes to it.
 */
public class DisplayMath extends CustomBlock {

	private String literal;

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}

}
