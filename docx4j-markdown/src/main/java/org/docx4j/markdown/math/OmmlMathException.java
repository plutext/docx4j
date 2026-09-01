package org.docx4j.markdown.math;

/**
 * An OMML equation uses constructs outside the supported subset; the
 * exporter catches this and flattens the equation to its text with a
 * warning (documented lossiness).
 */
public class OmmlMathException extends Exception {

	private static final long serialVersionUID = 1L;

	public OmmlMathException(String message) {
		super(message);
	}

}
