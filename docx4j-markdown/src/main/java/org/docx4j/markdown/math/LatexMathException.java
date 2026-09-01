package org.docx4j.markdown.math;

/**
 * A LaTeX expression is outside the supported subset (see the module README
 * / CR-markdown-math), or malformed.  The importer catches this and falls
 * back to the literal source — loudly, via the issue listener.
 */
public class LatexMathException extends Exception {

	private static final long serialVersionUID = 1L;

	public LatexMathException(String message) {
		super(message);
	}

}
