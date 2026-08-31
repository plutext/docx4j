package org.docx4j.markdown;

/**
 * Options for {@link MarkdownImporter}.  Per-call behavior is configured here,
 * not via Docx4jProperties globals.
 */
public class MarkdownImportOptions {

	/**
	 * What to do with HTML blocks and inline HTML encountered in the markdown.
	 */
	public enum HtmlPolicy {
		/** Silently drop HTML blocks and inline HTML (the default). */
		DROP,
		/** Insert the HTML source as literal text. */
		LITERAL,
		/** Route HTML through docx4j-ImportXHTML, if it is on the classpath. */
		IMPORT_XHTML
	}

	private HtmlPolicy htmlPolicy = HtmlPolicy.DROP;

	public HtmlPolicy getHtmlPolicy() {
		return htmlPolicy;
	}

	public MarkdownImportOptions setHtmlPolicy(HtmlPolicy htmlPolicy) {
		this.htmlPolicy = htmlPolicy;
		return this;
	}

}
