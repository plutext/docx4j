package org.docx4j.markdown;

import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Exports WordprocessingML to markdown (CommonMark + GFM extensions).
 *
 * <p>Since 17.3.4 (CR-046 phase 3) the text is the text model's ({@link Texts}, WD REQ-202):
 * its block model is written as a commonmark-java AST, which
 * org.commonmark.renderer.markdown.MarkdownRenderer then serializes —
 * so markdown escaping is handled by the reference implementation.
 * {@link #export(TextResult)} writes a result the caller already has, so that its plain text
 * and its Markdown are of one reading.</p>
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
	 * Export the given package to markdown: the text model's reading of it under
	 * {@link MarkdownExportOptions#getTextOptions()} (by default the main story, its text boxes
	 * and its notes, in the {@code accepted} view).  The package is not changed.
	 */
	public String export(WordprocessingMLPackage pkg) throws Docx4JException {
		return write(Texts.of(pkg, options.getTextOptions()), pkg);
	}

	/**
	 * Export a reading the text model has made ({@link Texts#of}), under the options it was made
	 * with ({@link TextResult#options()}): its view, and its metadata as YAML front matter where
	 * it was read with {@code metadata: true} (WD REQ-704).  A block quote's depth is then its
	 * indent alone, there being no styles to ask for the quote style's own
	 * ({@link #export(TextResult, WordprocessingMLPackage)}).
	 *
	 * @since 17.3.4 (CR-046 phase 3)
	 */
	public String export(TextResult result) throws Docx4JException {
		return write(result, null);
	}

	/**
	 * As {@link #export(TextResult)}, the package the result was read from asked for its styles
	 * (a block quote's depth is its indent beyond the quote style's own), as
	 * {@link #export(WordprocessingMLPackage)} does.  The package is not changed.
	 *
	 * @since 17.3.4 (CR-046 phase 3)
	 */
	public String export(TextResult result, WordprocessingMLPackage pkg) throws Docx4JException {
		return write(result, pkg);
	}

	private String write(TextResult result, WordprocessingMLPackage pkg) throws Docx4JException {

		org.commonmark.node.Document document = new WmlToMarkdown(result, pkg, options).convert();

		MarkdownRenderer renderer = MarkdownRenderer.builder()
				.extensions(java.util.List.of(
						TablesExtension.create(),
						StrikethroughExtension.create(),
						TaskListItemsExtension.create(),
						FootnotesExtension.create(),
						YamlFrontMatterExtension.create(),
						org.docx4j.markdown.math.MathExtension.create()))
				.build();
		String markdown = renderer.render(document);
		return result.options().isMetadata() ? frontMatter(result) + markdown : markdown;
	}

	/**
	 * The document properties as a YAML front-matter block (WD REQ-704, REQ-515): a mapping for
	 * each kind of property the package has, {@code core}, {@code app} and {@code custom}, in that
	 * order, each property by its element's local name (core, application) or its name (custom),
	 * its value a double-quoted string.  Empty where the package has none.
	 */
	static String frontMatter(TextResult result) {
		StringBuilder sb = new StringBuilder();
		for (String kind : new String[] { TextResult.CORE, TextResult.APP, TextResult.CUSTOM }) {
			Object properties = result.metadata().get(kind);
			if (!(properties instanceof java.util.Map) || ((java.util.Map<?, ?>) properties).isEmpty()) continue;
			sb.append(kind).append(":\n");
			for (java.util.Map.Entry<?, ?> e : ((java.util.Map<?, ?>) properties).entrySet()) {
				sb.append("  ").append(yamlKey(String.valueOf(e.getKey()))).append(": ")
						.append(yamlString(String.valueOf(e.getValue()))).append('\n');
			}
		}
		return sb.length() == 0 ? "" : "---\n" + sb + "---\n\n";
	}

	private static String yamlKey(String key) {
		return key.matches("[A-Za-z_][A-Za-z0-9_.-]*") ? key : yamlString(key);
	}

	/** A YAML double-quoted scalar: {@code \\}, {@code \"} and control characters escaped. */
	private static String yamlString(String value) {
		StringBuilder sb = new StringBuilder("\"");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"': sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			default:
				if (c < 0x20 || c == 0x7f || c == 0x85 || c == 0x2028 || c == 0x2029) sb.append(String.format("\\u%04x", (int) c));
				else sb.append(c);
			}
		}
		return sb.append('"').toString();
	}

	protected MarkdownExportOptions getOptions() {
		return options;
	}

}
