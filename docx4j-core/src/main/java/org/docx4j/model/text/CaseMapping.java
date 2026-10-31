/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.text;

import java.util.List;
import java.util.Locale;

/**
 * The capitals Word shows for {@code w:caps}, written by the plain-text and Markdown writers (WD REQ-607; CR-069): each
 * code point mapped to its single uppercase code point ({@link Character#toUpperCase(int)}, Unicode's simple mapping), by
 * the run's language, so that Turkish and Azeri {@code i} gives {@code İ} (U+0130); nothing expands, so {@code ß} stays
 * {@code ß}, the ligature {@code ﬁ} stays {@code ﬁ}, and {@code ǆ} gives {@code Ǆ}, not the titlecase {@code ǅ}. Measured
 * in Word for Microsoft 365 on 31 October 2026 (the {@code caps-lang} probes, docx4j-core-tests
 * {@code text-extraction/dpp/caps}). Not {@link String#toUpperCase(Locale)}, whose special casing expands {@code ß} to
 * {@code SS} and {@code ﬁ} to {@code FI}, which Word does not.
 *
 * <p>A small-caps run is written as its stored characters, unless {@code small_caps: upper} is set, when it is mapped as a
 * caps run is (Jason's ruling (d): small caps is a glyph variant that keeps the case in the letter heights). The block
 * model keeps the stored characters ({@link TextRun#getText()}), so that addresses and excerpts refer to them; the
 * mapping is the writers', and it keeps the length, in code points and in UTF-16 units.</p>
 *
 * <p>The Unicode version is the running JDK's (WD 0.52's REQ-607 asks for it documented): Unicode 10.0 on Java 11, 15.0
 * on Java 21. A case pair added after it is left unmapped there, U+1C8A (Unicode 16.0) for one.</p>
 *
 * @since 17.3.5
 */
public final class CaseMapping {

	/**
	 * The language where a run, its styles, the document defaults and {@code w:themeFontLang} name none: docx4j's
	 * documented default (REQ-607 leaves it to the implementation).
	 */
	public static final String DEFAULT_LANGUAGE = "en-US";

	private CaseMapping() {}

	/**
	 * A text in capitals by a language ({@code w:lang/@w:val}, a BCP 47 tag such as {@code tr-TR}; null for
	 * {@link #DEFAULT_LANGUAGE}): each code point to its single uppercase code point, {@code i} to {@code İ} where the
	 * language is Turkish or Azeri. A code point whose uppercase would change its length in UTF-16 is kept.
	 */
	public static String upper(String text, String language) {
		if (text == null || text.isEmpty()) return text;
		boolean dottedI = dottedI(language);
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length();) {
			int cp = text.codePointAt(i);
			i += Character.charCount(cp);
			int upper = dottedI && cp == 'i' ? 'İ' : Character.toUpperCase(cp);
			sb.appendCodePoint(Character.charCount(upper) == Character.charCount(cp) ? upper : cp);
		}
		return sb.toString();
	}

	/** Whether a language's capital of {@code i} is the dotted {@code İ}: Turkish and Azeri, as Java's own casing has it. */
	static boolean dottedI(String language) {
		if (language == null) return false;
		String primary = language.trim().split("[-_]", 2)[0].toLowerCase(Locale.ROOT);
		return primary.equals("tr") || primary.equals("az");
	}

	/** Whether a writer writes a run in capitals: a caps run, or a small-caps one under {@code small_caps: upper}. */
	public static boolean isUpper(TextRun run, TextOptions options) {
		return run.isCaps() || run.isSmallCaps() && options != null && options.getSmallCaps() == TextOptions.SmallCaps.UPPER;
	}

	/** A run's text as the writers write it: its stored text, in capitals where {@link #isUpper(TextRun, TextOptions)}. */
	public static String written(TextRun run, TextOptions options) {
		return isUpper(run, options) ? upper(run.getText(), run.getLanguage()) : run.getText();
	}

	/** A block's text ({@link TextBlock#getText()}) with each run's as the writers write it. */
	public static String written(TextBlock block, TextOptions options) {
		return block.text(r -> written(r, options));
	}

	/** A comment's text ({@link TextComment#getText()}): its blocks a line apart, as the writers write them. */
	public static String written(TextComment comment, TextOptions options) {
		StringBuilder sb = new StringBuilder();
		List<TextBlock> blocks = comment.getBlocks();
		for (int i = 0; i < blocks.size(); i++) {
			if (i > 0) sb.append('\n');
			sb.append(written(blocks.get(i), options));
		}
		return sb.toString();
	}
}
