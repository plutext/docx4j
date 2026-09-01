package org.docx4j.markdown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports markdown (CommonMark + GFM extensions) into WordprocessingML,
 * by walking the commonmark-java AST directly into wml (no HTML detour).
 *
 * <p>The importer builds into a caller-supplied WordprocessingMLPackage, so a
 * styles template docx can supply house styles; it ensures the styles it
 * references exist, adding minimal definitions where absent.</p>
 */
public class MarkdownImporter {

	private static final Logger log = LoggerFactory.getLogger(MarkdownImporter.class);

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

		Parser parser = Parser.builder().extensions(commonmarkExtensions()).build();
		Node document = parser.parse(markdown);

		MarkdownToWmlVisitor visitor = new MarkdownToWmlVisitor(pkg, options);

		if (options.getExtensions().contains(MarkdownImportOptions.Extension.FOOTNOTES)) {
			visitor.setFootnoteDefinitions(collectFootnoteDefinitions(document));
		}
		if (options.getExtensions().contains(MarkdownImportOptions.Extension.YAML_FRONT_MATTER)) {
			applyFrontMatter(document, pkg);
		}

		document.accept(visitor);

		List<Object> content = visitor.getResults();
		pkg.getMainDocumentPart().getContent().addAll(content);
		return content;
	}

	private List<Extension> commonmarkExtensions() {
		List<Extension> extensions = new ArrayList<>();
		for (MarkdownImportOptions.Extension e : options.getExtensions()) {
			switch (e) {
			case TABLES:
				extensions.add(TablesExtension.create());
				break;
			case STRIKETHROUGH:
				extensions.add(StrikethroughExtension.create());
				break;
			case TASK_LIST_ITEMS:
				extensions.add(TaskListItemsExtension.create());
				break;
			case FOOTNOTES:
				extensions.add(FootnotesExtension.create());
				break;
			case YAML_FRONT_MATTER:
				extensions.add(YamlFrontMatterExtension.create());
				break;
			}
		}
		return extensions;
	}

	private static Map<String, FootnoteDefinition> collectFootnoteDefinitions(Node document) {
		Map<String, FootnoteDefinition> definitions = new HashMap<>();
		document.accept(new AbstractVisitor() {
			@Override
			public void visit(CustomBlock customBlock) {
				if (customBlock instanceof FootnoteDefinition) {
					FootnoteDefinition definition = (FootnoteDefinition) customBlock;
					definitions.putIfAbsent(definition.getLabel(), definition);
				} else {
					visitChildren(customBlock);
				}
			}
		});
		return definitions;
	}

	/**
	 * YAML front matter keys title/author/keywords become the corresponding
	 * core document properties; other keys are ignored.
	 */
	private void applyFrontMatter(Node document, WordprocessingMLPackage pkg) {

		YamlFrontMatterVisitor yamlVisitor = new YamlFrontMatterVisitor();
		document.accept(yamlVisitor);
		Map<String, List<String>> data = yamlVisitor.getData();
		if (data.isEmpty()) {
			return;
		}

		DocPropsCorePart corePart = pkg.getDocPropsCorePart();
		if (corePart == null) {
			pkg.addDocPropsCorePart();
			corePart = pkg.getDocPropsCorePart();
		}
		CoreProperties coreProps = corePart.getJaxbElement();
		org.docx4j.docProps.core.dc.elements.ObjectFactory dcFactory =
				new org.docx4j.docProps.core.dc.elements.ObjectFactory();

		for (Map.Entry<String, List<String>> entry : data.entrySet()) {
			String value = String.join(", ", entry.getValue());
			switch (entry.getKey().toLowerCase(java.util.Locale.ROOT)) {
			case "title":
				org.docx4j.docProps.core.dc.elements.SimpleLiteral title =
						dcFactory.createSimpleLiteral();
				title.getContent().add(value);
				coreProps.setTitle(dcFactory.createTitle(title));
				break;
			case "author":
				org.docx4j.docProps.core.dc.elements.SimpleLiteral creator =
						dcFactory.createSimpleLiteral();
				creator.getContent().add(value);
				coreProps.setCreator(creator);
				break;
			case "keywords":
				coreProps.setKeywords(value);
				break;
			default:
				log.debug("Ignoring front matter key {}", entry.getKey());
			}
		}
	}

	protected MarkdownImportOptions getOptions() {
		return options;
	}

}
