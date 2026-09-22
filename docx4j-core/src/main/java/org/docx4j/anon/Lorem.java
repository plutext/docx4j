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
package org.docx4j.anon;

import java.util.Random;

/**
 * The replacement vocabulary: the words of the classic <i>lorem ipsum</i> passage
 * (Cicero, <i>De finibus bonorum et malorum</i>, as the printers' filler has carried it
 * since the 1500s - public domain), from which {@link ScrambleText} draws the Latin
 * text it writes in place of a document's, and against which {@link Verify} tests
 * whether a surviving token could be the scrambler's own.
 * <p>
 * Until 17.2.1 this was com.thedeanda:lorem, the one dependency that kept the
 * anonymiser in a module of its own (and a filename-based automatic module under
 * JPMS). The scrambler only ever needed a run of pseudo-words with a natural
 * length distribution, and the check only ever needed the same list; one array
 * serves both, so the list cannot drift from the check.
 *
 * @since 17.2.1
 */
public final class Lorem {

	private Lorem() {}

	/** The distinct words of the passage (the filler and its Cicero source), lower case; the same list {@link Verify} allows. */
	public static final String[] WORDS = {
			"lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit", "sed", "do",
			"eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore", "magna", "aliqua", "enim",
			"ad", "minim", "veniam", "quis", "nostrud", "exercitation", "ullamco", "laboris", "nisi",
			"aliquip", "ex", "ea", "commodo", "consequat", "duis", "aute", "irure", "in", "reprehenderit",
			"voluptate", "velit", "esse", "cillum", "eu", "fugiat", "nulla", "pariatur", "excepteur",
			"sint", "occaecat", "cupidatat", "non", "proident", "sunt", "culpa", "qui", "officia",
			"deserunt", "mollit", "anim", "id", "est", "laborum", "neque", "porro", "quisquam", "quia",
			"dolorem", "quaerat", "voluptatem", "aspernatur", "aut", "odit", "fugit", "consequuntur",
			"magni", "dolores", "eos", "ratione", "sequi", "nesciunt", "ipsam", "ipsa", "voluptas",
			"aliquam", "numquam", "eius", "modi", "tempora", "incidunt", "magnam", "quam", "nostrum",
			"exercitationem", "ullam", "corporis", "suscipit", "laboriosam", "aliquid", "commodi",
			"consequatur", "autem", "vel", "eum", "iure", "nihil", "molestiae", "illum", "quo", "at",
			"vero", "accusamus", "iusto", "odio", "dignissimos", "ducimus", "blanditiis", "praesentium",
			"voluptatum", "deleniti", "atque", "corrupti", "quos", "quas", "molestias", "excepturi",
			"obcaecati", "cupiditate", "provident", "similique", "mollitia", "animi", "dolorum", "fuga",
			"harum", "quidem", "rerum", "facilis", "expedita", "distinctio", "nam", "libero", "tempore",
			"cum", "soluta", "nobis", "eligendi", "optio", "cumque", "impedit", "minus", "quod", "maxime",
			"placeat", "facere", "possimus", "omnis", "assumenda", "temporibus", "quibusdam", "officiis",
			"debitis", "necessitatibus", "saepe", "eveniet", "voluptates", "repudiandae", "recusandae",
			"itaque", "earum", "hic", "tenetur", "sapiente", "delectus", "reiciendis", "maiores", "alias",
			"perferendis", "doloribus", "asperiores", "repellat" };

	/**
	 * @param count how many words
	 * @return that many words drawn at random, separated by single spaces
	 */
	public static String words(int count, Random random) {
		StringBuilder sb = new StringBuilder(count * 7);
		for (int i = 0; i < count; i++) {
			if (i > 0) sb.append(' ');
			sb.append(WORDS[random.nextInt(WORDS.length)]);
		}
		return sb.toString();
	}

	private static String joined;

	/** true if the token (lower case) is a substring of some word of the list */
	public static synchronized boolean isFragment(String token) {
		if (joined == null) {
			StringBuilder sb = new StringBuilder(" ");
			for (String w : WORDS) sb.append(w).append(' ');
			joined = sb.toString();
		}
		return joined.contains(token);
	}

}
