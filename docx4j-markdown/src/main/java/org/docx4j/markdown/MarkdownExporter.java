package org.docx4j.markdown;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Exports WordprocessingML to markdown (CommonMark + GFM extensions).
 *
 * <p>A TraversalUtil-based walker builds a commonmark-java AST, which
 * org.commonmark.renderer.markdown.MarkdownRenderer then serializes —
 * so markdown escaping is handled by the reference implementation.</p>
 */
public class MarkdownExporter {

	private final MarkdownExportOptions options;

	public MarkdownExporter() {
		this(new MarkdownExportOptions());
	}

	public MarkdownExporter(MarkdownExportOptions options) {
		this.options = options;
	}

	/**
	 * Export the main document part of the given package to markdown.
	 */
	public String export(WordprocessingMLPackage pkg) throws Docx4JException {
		throw new UnsupportedOperationException("Phase 3 (export core) not yet implemented");
	}

	protected MarkdownExportOptions getOptions() {
		return options;
	}

}
