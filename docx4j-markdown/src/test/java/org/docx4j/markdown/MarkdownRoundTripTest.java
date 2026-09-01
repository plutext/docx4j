package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * Phase 3 golden round-trip suite: for documents within the supported
 * subset, md → docx → md must equal commonmark's own canonical rendering of
 * the input (render(parse(md))) — i.e. the docx trip loses nothing that the
 * markdown formalism itself keeps.  That also implies idempotence after the
 * first trip.
 */
public class MarkdownRoundTripTest {

	private static List<Extension> extensions() {
		return List.of(
				TablesExtension.create(),
				StrikethroughExtension.create(),
				TaskListItemsExtension.create(),
				FootnotesExtension.create(),
				YamlFrontMatterExtension.create());
	}

	/** What commonmark itself would emit for this markdown. */
	private static String canonical(String md) {
		List<Extension> extensions = extensions();
		Parser parser = Parser.builder().extensions(extensions).build();
		MarkdownRenderer renderer = MarkdownRenderer.builder().extensions(extensions).build();
		return renderer.render(parser.parse(md));
	}

	private static String roundTrip(String md) throws Exception {
		WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(md);
		return new MarkdownExporter().export(pkg);
	}

	private static void assertRoundTrip(String md) throws Exception {
		String canonical = canonical(md);
		String out = roundTrip(md);
		assertEquals(canonical, out);
		// and stable on a second trip
		assertEquals(canonical, roundTrip(out));
	}

	@Test
	public void headings() throws Exception {
		assertRoundTrip("# One\n\n## Two\n\n### Three\n\n#### Four\n\n##### Five\n\n###### Six\n");
	}

	@Test
	public void emphasisAndCode() throws Exception {
		assertRoundTrip("Plain with **bold**, *italic*, ***both*** and `code` here.\n");
	}

	@Test
	public void links() throws Exception {
		assertRoundTrip("See [docx4j](https://www.docx4java.org/) and [b *i* text](https://example.org/x).\n");
	}

	@Test
	public void bulletList() throws Exception {
		assertRoundTrip("- one\n- two\n  - nested\n  - deeper follows\n    - third\n- three\n");
	}

	@Test
	public void orderedList() throws Exception {
		assertRoundTrip("1. first\n2. second\n");
	}

	@Test
	public void orderedListStartAndRestart() throws Exception {
		assertRoundTrip("1. a\n2. b\n\nbetween\n\n3. c\n4. d\n");
	}

	@Test
	public void mixedNestedLists() throws Exception {
		assertRoundTrip("1. first\n   - bullet under ordered\n2. second\n");
	}

	@Test
	public void looseList() throws Exception {
		assertRoundTrip("- alpha\n\n- beta\n");
	}

	@Test
	public void blockQuotes() throws Exception {
		assertRoundTrip("> first line\n>\n> second paragraph\n");
	}

	@Test
	public void codeBlock() throws Exception {
		assertRoundTrip("```\nint x = 1;\n  indented();\n```\n");
	}

	@Test
	public void thematicBreakAndHardBreak() throws Exception {
		assertRoundTrip("above\n\n---\n\nhard\\\nbreak\n");
	}

	@Test
	public void escapedCharactersSurvive() throws Exception {
		// markdown-significant characters in plain text must be re-escaped on export
		assertRoundTrip("Literal \\*stars\\* and \\_underscores\\_ and \\# hash.\n");
	}

	// ------------------------------------------- phase 4: extension round trips

	@Test
	public void table() throws Exception {
		assertRoundTrip("| Name | Qty |\n|------|----:|\n| ant | 1 |\n| bee | 22 |\n");
	}

	@Test
	public void strikethrough() throws Exception {
		assertRoundTrip("keep ~~gone~~ end\n");
	}

	@Test
	public void footnote() throws Exception {
		// numeric labels: import normalizes labels to footnote ids
		assertRoundTrip("Body text.[^1]\n\nMore.[^2]\n\n[^1]: First note\n\n[^2]: Second *note*\n");
	}

	@Test
	public void composite() throws Exception {
		assertRoundTrip("# Title\n\n"
				+ "Intro with **bold** and a [link](https://example.org/).\n\n"
				+ "- point one\n- point two\n  1. sub a\n  2. sub b\n\n"
				+ "> a quote\n\n"
				+ "```\ncode();\n```\n\n"
				+ "---\n\n"
				+ "The end.\n");
	}

}
