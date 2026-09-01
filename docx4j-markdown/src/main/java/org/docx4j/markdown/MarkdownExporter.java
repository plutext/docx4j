package org.docx4j.markdown;

import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.renderer.markdown.MarkdownRenderer;
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

		org.commonmark.node.Document document = new WmlToMarkdown(pkg, options).convert();

		MarkdownRenderer renderer = MarkdownRenderer.builder()
				.extensions(java.util.List.of(
						TablesExtension.create(),
						StrikethroughExtension.create(),
						TaskListItemsExtension.create(),
						FootnotesExtension.create(),
						YamlFrontMatterExtension.create(),
						org.docx4j.markdown.math.MathExtension.create()))
				.build();
		return renderer.render(document);
	}

	protected MarkdownExportOptions getOptions() {
		return options;
	}

}
