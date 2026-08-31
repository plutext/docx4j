package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.junit.Test;

/**
 * Phase 0 spike findings as executable assertions: commonmark-java 0.30.0's
 * MarkdownRenderer covers ALL the extensions this module uses (tables,
 * strikethrough, task list items, footnotes, YAML front matter), and
 * parse+render is idempotent after the first trip.  If a commonmark upgrade
 * regresses either property, this test catches it.
 */
public class CommonmarkSpikeTest {

	static List<Extension> extensions() {
		return List.of(
				TablesExtension.create(),
				StrikethroughExtension.create(),
				TaskListItemsExtension.create(),
				FootnotesExtension.create(),
				YamlFrontMatterExtension.create());
	}

	@Test
	public void markdownRendererCoversExtensions() {

		String md = "---\n"
				+ "title: Spike\n"
				+ "---\n\n"
				+ "# Heading *one*\n\n"
				+ "Para with **bold**, `code`, ~~strike~~ and a [link](https://example.com).\n\n"
				+ "- bullet 1\n"
				+ "- [x] done task\n\n"
				+ "1. first\n"
				+ "2. second\n\n"
				+ "> quote\n\n"
				+ "```java\nint x = 1;\n```\n\n"
				+ "| a | b |\n|---|--:|\n| 1 | 2 |\n\n"
				+ "Footnote here.[^1]\n\n"
				+ "[^1]: the note\n\n"
				+ "---\n";

		List<Extension> extensions = extensions();
		Parser parser = Parser.builder().extensions(extensions).build();
		MarkdownRenderer renderer = MarkdownRenderer.builder().extensions(extensions).build();

		String out = renderer.render(parser.parse(md));

		// each extension construct survives a render (i.e. has a markdown renderer)
		assertTrue("front matter lost", out.contains("title: Spike"));
		assertTrue("strikethrough lost", out.contains("~~strike~~"));
		assertTrue("task list marker lost", out.contains("[x] done task"));
		assertTrue("table lost", out.contains("|1|2|"));
		assertTrue("table alignment lost", out.contains("---:"));
		assertTrue("footnote reference lost", out.contains("[^1]"));

		// idempotent after the first trip
		String out2 = renderer.render(parser.parse(out));
		assertEquals(out, out2);
	}

}
