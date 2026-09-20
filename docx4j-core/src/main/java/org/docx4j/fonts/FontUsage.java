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
package org.docx4j.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * What a document uses each of its fonts for: how much text, in which scripts, in which
 * faces.
 *
 * <p>{@code MainDocumentPart.fontsInUse()} (CR-016 phase 4) is a walk for <em>names</em>:
 * it says which fonts a document mentions, which is what the {@link Mapper} needs.  A
 * report has to grade the substitution, and for that it needs to know what each font is
 * used for - a metric clone which covers every script the font is set in is exact, and
 * the same clone is only near where the document sets Greek in it (CR-017 gap 5).</p>
 *
 * <p>Built by {@link FontsAnalysis#usage}, which asks
 * {@link RunFontSelector#documentFontFor} for every character.</p>
 *
 * @since 17.2.0
 */
public final class FontUsage {

	/** The four faces a run can ask for, from {@code w:b} and {@code w:i}. */
	public enum Face { REGULAR, BOLD, ITALIC, BOLD_ITALIC }

	/** One document font's use. */
	public static final class Use {

		private final String documentFont;
		private long characters;
		private long runs;
		private final Map<String, long[]> byScript = new TreeMap<String, long[]>();
		private final Map<Face, long[]> byFace = new LinkedHashMap<Face, long[]>();
		private final Map<String, Integer> sampleCodePoints = new TreeMap<String, Integer>();

		Use(String documentFont) {
			this.documentFont = documentFont;
		}

		/** The font's name as the document has it. */
		public String getDocumentFont() { return documentFont; }

		/** Characters of text this document sets in it (a {@code w:sym} counts as one). */
		public long getCharacters() { return characters; }

		/** Runs of text, counted once each however many scripts they carry. */
		public long getRuns() { return runs; }

		/** Characters and runs by coverage group - a {@link Character.UnicodeScript}
		 *  name, {@link FontFallback#SYMBOL_GROUP} or {@link FontFallback#EMOJI_GROUP} -
		 *  each value {@code {characters, runs}}. */
		public Map<String, long[]> getByScript() { return Collections.unmodifiableMap(byScript); }

		/** Characters and runs by face, each value {@code {characters, runs}}. */
		public Map<Face, long[]> getByFace() { return Collections.unmodifiableMap(byFace); }

		/** The scripts this font is used for, in name order. */
		public java.util.Set<String> getScripts() { return getByScript().keySet(); }

		/** The faces this font is used in. */
		public java.util.Set<Face> getFaces() { return getByFace().keySet(); }

		/** A code point of that script the document actually sets in this font, so a
		 *  report can ask whether the face that draws it has the glyph. */
		public Integer sampleCodePoint(String script) {
			return sampleCodePoints.get(script);
		}

		void add(String script, Face face, int characters, int codePoint) {
			this.characters += characters;
			count(byScript, script, characters);
			count(byFace, face, characters);
			if (!sampleCodePoints.containsKey(script)) sampleCodePoints.put(script, codePoint);
		}

		private static <K> void count(Map<K, long[]> map, K key, int characters) {
			long[] counts = map.get(key);
			if (counts==null) {
				counts = new long[2];
				map.put(key, counts);
			}
			counts[0] += characters;
		}

		/** Called once per run, after its characters, with the keys that run touched. */
		void endRun(java.util.Set<String> scripts, java.util.Set<Face> faces) {
			runs++;
			for (String script : scripts) {
				long[] counts = byScript.get(script);
				if (counts!=null) counts[1]++;
			}
			for (Face face : faces) {
				long[] counts = byFace.get(face);
				if (counts!=null) counts[1]++;
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(documentFont);
			sb.append(": ").append(characters).append(" characters in ").append(runs).append(" runs");
			if (!byScript.isEmpty()) sb.append("; scripts ").append(byScript.keySet());
			if (!byFace.isEmpty()) sb.append("; faces ").append(byFace.keySet());
			return sb.toString();
		}
	}

	private final Map<String, Use> uses = new LinkedHashMap<String, Use>();
	private long characters;
	private long runs;

	FontUsage() {}

	/** Every font the document sets text in, most text first, ties broken by name. */
	public List<Use> byTextCarried() {
		List<Use> all = new ArrayList<Use>(uses.values());
		Collections.sort(all, new java.util.Comparator<Use>() {
			public int compare(Use a, Use b) {
				if (a.getCharacters()!=b.getCharacters()) {
					return a.getCharacters() > b.getCharacters() ? -1 : 1;
				}
				return String.CASE_INSENSITIVE_ORDER.compare(a.getDocumentFont(), b.getDocumentFont());
			}
		});
		return Collections.unmodifiableList(all);
	}

	/** The use of one font, or null where the document sets no text in it. */
	public Use get(String documentFont) {
		return documentFont==null ? null : uses.get(key(documentFont));
	}

	/** Characters of text walked, over every font. */
	public long getCharacters() { return characters; }

	/** Runs walked. */
	public long getRuns() { return runs; }

	Use use(String documentFont) {
		String key = key(documentFont);
		Use use = uses.get(key);
		if (use==null) {
			use = new Use(documentFont);
			uses.put(key, use);
		}
		return use;
	}

	void countRun() { runs++; }

	void countCharacters(int n) { characters += n; }

	private static String key(String name) {
		return name.trim().toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		return characters + " characters in " + runs + " runs, " + uses.size() + " fonts";
	}
}
