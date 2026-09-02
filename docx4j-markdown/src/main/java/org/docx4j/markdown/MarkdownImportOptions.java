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

	/**
	 * How a code block becomes wml.
	 */
	public enum CodeBlockShape {
		/**
		 * One paragraph, lines separated by w:br (the default: keeps the
		 * block a single unit for styling and copy/paste).
		 */
		SINGLE_PARAGRAPH,
		/** One paragraph per line. */
		PARAGRAPH_PER_LINE
	}

	/**
	 * The supported markdown extensions (all enabled by default).
	 * MATH is TeX math: {@code $...$} / {@code $$...$$} (and the
	 * {@code \(...\)} / {@code \[...\]} forms); see CR-006-markdown-math.
	 */
	public enum Extension {
		TABLES, STRIKETHROUGH, TASK_LIST_ITEMS, FOOTNOTES, YAML_FRONT_MATTER, MATH
	}

	private static final org.slf4j.Logger log =
			org.slf4j.LoggerFactory.getLogger(MarkdownImportOptions.class);

	/**
	 * How TeX math is realised.
	 */
	public enum MathPolicy {
		/**
		 * Translate the supported LaTeX subset to native OMML equations
		 * (the default); an equation outside the subset falls back to its
		 * literal source and raises an issue.
		 */
		OMML,
		/** Never translate: all math as literal source text. */
		LITERAL
	}

	private HtmlPolicy htmlPolicy = HtmlPolicy.DROP;
	private CodeBlockShape codeBlockShape = CodeBlockShape.SINGLE_PARAGRAPH;
	private java.util.Set<Extension> extensions = java.util.EnumSet.allOf(Extension.class);
	private MarkdownImageHandler imageHandler = new DefaultMarkdownImageHandler();
	private MarkdownImportIssueListener issueListener = issue -> log.warn("{}", issue);
	private MathPolicy mathPolicy = MathPolicy.OMML;

	public HtmlPolicy getHtmlPolicy() {
		return htmlPolicy;
	}

	public MarkdownImportOptions setHtmlPolicy(HtmlPolicy htmlPolicy) {
		this.htmlPolicy = htmlPolicy;
		return this;
	}

	public CodeBlockShape getCodeBlockShape() {
		return codeBlockShape;
	}

	public MarkdownImportOptions setCodeBlockShape(CodeBlockShape codeBlockShape) {
		this.codeBlockShape = codeBlockShape;
		return this;
	}

	public java.util.Set<Extension> getExtensions() {
		return extensions;
	}

	/**
	 * Restrict which markdown extensions are enabled (the default is all).
	 * Pass an empty set for pure CommonMark.
	 */
	public MarkdownImportOptions setExtensions(java.util.Set<Extension> extensions) {
		this.extensions = extensions;
		return this;
	}

	public MarkdownImageHandler getImageHandler() {
		return imageHandler;
	}

	/**
	 * How images are realised.  The default embeds data URIs and local files
	 * but never fetches remote URLs (those degrade to hyperlinked alt text);
	 * supply a handler to change that — eg
	 * {@code new DefaultMarkdownImageHandler(baseDir)} to resolve relative
	 * paths, or your own implementation to fetch remote images.
	 */
	public MarkdownImportOptions setImageHandler(MarkdownImageHandler imageHandler) {
		this.imageHandler = imageHandler;
		return this;
	}

	public MarkdownImportIssueListener getIssueListener() {
		return issueListener;
	}

	/**
	 * Where degradations are reported (see {@link MarkdownImportIssue}).
	 * The default logs a warning per issue; supply eg {@code issues::add}
	 * to collect them for programmatic triage.
	 */
	public MarkdownImportOptions setIssueListener(MarkdownImportIssueListener issueListener) {
		this.issueListener = issueListener;
		return this;
	}

	public MathPolicy getMathPolicy() {
		return mathPolicy;
	}

	public MarkdownImportOptions setMathPolicy(MathPolicy mathPolicy) {
		this.mathPolicy = mathPolicy;
		return this;
	}

}
