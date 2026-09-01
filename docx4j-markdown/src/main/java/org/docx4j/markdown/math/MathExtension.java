package org.docx4j.markdown.math;

import java.util.Collections;
import java.util.Set;

import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.InlineParserState;
import org.commonmark.parser.beta.ParsedInline;
import org.commonmark.parser.beta.Position;
import org.commonmark.parser.beta.Scanner;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockParserFactory;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.renderer.markdown.MarkdownWriter;

/**
 * TeX-math markdown extension: inline {@code $...$} / {@code \(...\)} and
 * display {@code $$ ... $$} / {@code \[ ... \]} become {@link InlineMath} /
 * {@link DisplayMath} nodes carrying the raw LaTeX.
 *
 * <p>Inline {@code $} follows GitHub-style guards so currency doesn't
 * false-positive: the opening delimiter must not be followed by whitespace,
 * the closing one must not be preceded by whitespace nor followed by a
 * digit; {@code \$} escapes (handled by core markdown escaping).</p>
 *
 * <p>Registered for the MarkdownRenderer too, so the nodes serialize back to
 * {@code $...$} / {@code $$...$$} (with {@code \(..\)}/{@code \[..\]} input
 * normalized to the dollar forms), and literal {@code $} in plain text is
 * escaped.</p>
 */
public class MathExtension implements Parser.ParserExtension, MarkdownRenderer.MarkdownRendererExtension {

	private MathExtension() {
	}

	public static MathExtension create() {
		return new MathExtension();
	}

	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customInlineContentParserFactory(new DollarInlineFactory());
		parserBuilder.customInlineContentParserFactory(new BackslashInlineFactory());
		parserBuilder.customBlockParserFactory(new DisplayBlockFactory("$$", "$$"));
		parserBuilder.customBlockParserFactory(new DisplayBlockFactory("\\[", "\\]"));
	}

	@Override
	public void extend(MarkdownRenderer.Builder rendererBuilder) {
		rendererBuilder.nodeRendererFactory(new MarkdownNodeRendererFactory() {
			@Override
			public NodeRenderer create(MarkdownNodeRendererContext context) {
				return new MathMarkdownNodeRenderer(context);
			}
			@Override
			public Set<Character> getSpecialCharacters() {
				return Collections.singleton('$'); // literal $ in text gets escaped
			}
		});
	}

	// ------------------------------------------------------------ inline $

	private static final class DollarInlineFactory implements InlineContentParserFactory {
		@Override
		public Set<Character> getTriggerCharacters() {
			return Collections.singleton('$');
		}
		@Override
		public InlineContentParser create() {
			return MathExtension::tryParseDollar;
		}
	}

	private static ParsedInline tryParseDollar(InlineParserState state) {

		Scanner scanner = state.scanner();
		int open = scanner.matchMultiple('$');
		if (open < 1 || open > 2) {
			return ParsedInline.none();
		}
		if (isSpace(scanner.peek()) || scanner.peek() == Scanner.END) {
			return ParsedInline.none(); // opening $ must hug its content
		}

		Position contentStart = scanner.position();
		Position contentEnd = null;
		char prev = 0;
		boolean escaped = false;
		while (scanner.hasNext()) {
			char c = scanner.peek();
			if (c == '$' && !escaped && !isSpace(prev)) {
				Position closeStart = scanner.position();
				int close = scanner.matchMultiple('$');
				if (close == open && !Character.isDigit(scanner.peek())) {
					contentEnd = closeStart;
					break;
				}
				prev = '$'; // not a valid close; the dollars join the content
				escaped = false;
				continue;
			}
			escaped = (c == '\\') && !escaped;
			prev = c;
			scanner.next();
		}
		if (contentEnd == null) {
			return ParsedInline.none();
		}
		String literal = scanner.getSource(contentStart, contentEnd).getContent();
		if (literal.trim().isEmpty()) {
			return ParsedInline.none();
		}
		return ParsedInline.of(new InlineMath(literal, open == 2), scanner.position());
	}

	// ------------------------------------------------------------ inline \( \[

	private static final class BackslashInlineFactory implements InlineContentParserFactory {
		@Override
		public Set<Character> getTriggerCharacters() {
			return Collections.singleton('\\');
		}
		@Override
		public InlineContentParser create() {
			return MathExtension::tryParseBackslash;
		}
	}

	/**
	 * Only {@code \(...\)} — NOT {@code \[...\]}: markdown's own escaping
	 * writes literal brackets as {@code \[x\]}, so an inline bracket form
	 * would turn escaped brackets into math.  {@code \[ ... \]} is
	 * supported as a display BLOCK only.
	 */
	private static ParsedInline tryParseBackslash(InlineParserState state) {

		Scanner scanner = state.scanner();
		if (!scanner.next("\\(")) {
			return ParsedInline.none(); // ordinary escape; core handles it
		}

		Position contentStart = scanner.position();
		while (scanner.hasNext()) {
			Position candidate = scanner.position();
			if (scanner.next("\\)")) {
				String literal = scanner.getSource(contentStart, candidate).getContent();
				if (literal.trim().isEmpty()) {
					return ParsedInline.none();
				}
				return ParsedInline.of(new InlineMath(literal, false), scanner.position());
			}
			scanner.next();
		}
		return ParsedInline.none();
	}

	private static boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\n';
	}

	// ------------------------------------------------------------ display blocks

	private static final class DisplayBlockFactory implements BlockParserFactory {

		private final String opener;
		private final String closer;

		DisplayBlockFactory(String opener, String closer) {
			this.opener = opener;
			this.closer = closer;
		}

		@Override
		public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
			if (state.getIndent() >= 4) {
				return BlockStart.none();
			}
			CharSequence line = state.getLine().getContent();
			int i = state.getNextNonSpaceIndex();
			if (!startsWith(line, i, opener)) {
				return BlockStart.none();
			}
			String trimmed = line.subSequence(i + opener.length(), line.length())
					.toString().trim();
			boolean singleLine = false;
			int closerIndex = trimmed.indexOf(closer);
			if (closerIndex >= 0) {
				if (closerIndex == trimmed.length() - closer.length()) {
					// \[ x \] (or $$ x $$) complete on this line: a display
					// block too — crucial for \[..\], which has NO inline form
					singleLine = true;
				} else {
					// the closer mid-line, with trailing content: not a
					// display block (else it would swallow following lines)
					return BlockStart.none();
				}
			}
			return BlockStart.of(new DisplayBlockParser(closer, singleLine))
					.atIndex(i + opener.length());
		}

		private static boolean startsWith(CharSequence line, int index, String prefix) {
			if (index + prefix.length() > line.length()) {
				return false;
			}
			for (int j = 0; j < prefix.length(); j++) {
				if (line.charAt(index + j) != prefix.charAt(j)) {
					return false;
				}
			}
			return true;
		}
	}

	private static final class DisplayBlockParser extends AbstractBlockParser {

		private final DisplayMath block = new DisplayMath();
		private final String closer;
		private final boolean singleLine;
		private final StringBuilder content = new StringBuilder();
		private boolean seenOpeningRemainder;

		DisplayBlockParser(String closer, boolean singleLine) {
			this.closer = closer;
			this.singleLine = singleLine;
		}

		@Override
		public Block getBlock() {
			return block;
		}

		@Override
		public BlockContinue tryContinue(ParserState state) {
			if (singleLine) {
				return BlockContinue.none(); // complete: the block closes here
			}
			String line = state.getLine().getContent().toString();
			String trimmed = line.trim();
			if (trimmed.endsWith(closer)) {
				String beforeCloser = trimmed.substring(0, trimmed.length() - closer.length()).trim();
				if (!beforeCloser.isEmpty()) {
					appendContentLine(beforeCloser);
				}
				return BlockContinue.finished(); // consumes the closing line
			}
			return BlockContinue.atIndex(state.getIndex());
		}

		@Override
		public void addLine(SourceLine line) {
			if (!seenOpeningRemainder) {
				seenOpeningRemainder = true; // remainder of the opening line
				String rest = line.getContent().toString().trim();
				if (singleLine) {
					rest = rest.substring(0, rest.length() - closer.length()).trim();
				}
				if (!rest.isEmpty()) {
					appendContentLine(rest);
				}
				return;
			}
			appendContentLine(line.getContent().toString());
		}

		private void appendContentLine(String s) {
			if (content.length() > 0) {
				content.append('\n');
			}
			content.append(s);
		}

		@Override
		public void closeBlock() {
			block.setLiteral(content.toString());
		}
	}

	// ------------------------------------------------------------ rendering

	private static final class MathMarkdownNodeRenderer implements NodeRenderer {

		private final MarkdownNodeRendererContext context;

		MathMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
			this.context = context;
		}

		@Override
		public Set<Class<? extends Node>> getNodeTypes() {
			return Set.of(InlineMath.class, DisplayMath.class);
		}

		@Override
		public void render(Node node) {
			MarkdownWriter writer = context.getWriter();
			if (node instanceof InlineMath) {
				InlineMath math = (InlineMath) node;
				String delimiter = math.isDisplayHint() ? "$$" : "$";
				writer.raw(delimiter);
				writer.raw(math.getLiteral().replace('\n', ' '));
				writer.raw(delimiter);
			} else if (node instanceof DisplayMath) {
				writer.raw("$$");
				writer.line();
				for (String line : ((DisplayMath) node).getLiteral().split("\n", -1)) {
					writer.raw(line);
					writer.line();
				}
				writer.raw("$$");
				writer.block();
			}
		}
	}

}
