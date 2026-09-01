package org.docx4j.markdown;

/**
 * A construct the importer could not (or chose not to) convert faithfully —
 * the machine-readable analogue of scanning converter warnings.  Nothing is
 * ever silently dropped: an issue is raised whenever content degrades (eg a
 * math expression falling back to its literal source).
 */
public class MarkdownImportIssue {

	private final String construct;
	private final String source;
	private final String reason;

	public MarkdownImportIssue(String construct, String source, String reason) {
		this.construct = construct;
		this.source = source;
		this.reason = reason;
	}

	/** What kind of construct, eg "inline math", "display math". */
	public String getConstruct() {
		return construct;
	}

	/** The markdown/LaTeX source of the construct. */
	public String getSource() {
		return source;
	}

	/** Why it degraded, and to what. */
	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return construct + " [" + source + "]: " + reason;
	}

}
