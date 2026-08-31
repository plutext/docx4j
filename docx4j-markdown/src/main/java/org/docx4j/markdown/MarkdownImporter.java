package org.docx4j.markdown;

import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Imports markdown (CommonMark + GFM extensions) into WordprocessingML,
 * by walking the commonmark-java AST directly into wml (no HTML detour).
 *
 * <p>The importer builds into a caller-supplied WordprocessingMLPackage, so a
 * styles template docx can supply house styles; it ensures the styles it
 * references exist, adding minimal definitions where absent.</p>
 */
public class MarkdownImporter {

	private final MarkdownImportOptions options;

	public MarkdownImporter() {
		this(new MarkdownImportOptions());
	}

	public MarkdownImporter(MarkdownImportOptions options) {
		this.options = options;
	}

	/**
	 * Convert markdown into a fresh WordprocessingMLPackage.
	 */
	public WordprocessingMLPackage createPackage(String markdown) throws Docx4JException {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		importToMainDocumentPart(markdown, pkg);
		return pkg;
	}

	/**
	 * Convert markdown and append the resulting block-level content to the
	 * main document part of the given package (which may be a styles template).
	 *
	 * @return the block-level content which was added
	 */
	public List<Object> importToMainDocumentPart(String markdown, WordprocessingMLPackage pkg)
			throws Docx4JException {

		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdown);

		MarkdownToWmlVisitor visitor = new MarkdownToWmlVisitor(pkg, options);
		document.accept(visitor);

		List<Object> content = visitor.getResults();
		pkg.getMainDocumentPart().getContent().addAll(content);
		return content;
	}

	protected MarkdownImportOptions getOptions() {
		return options;
	}

}
