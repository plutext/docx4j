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
package org.docx4j.survey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

/**
 * What docx4j logs while one document is surveyed. Events count for the document only when they
 * come from the thread surveying it; anything else (a warm-up, another thread) goes to standard
 * error, which the coordinator keeps in the worker's log.
 */
final class LogCapture {

	/** At most this many distinct messages a document; the rest are counted. */
	static final int MAX_DISTINCT = 50;

	/**
	 * The INFO line of 17.3.5 when JAXB refused a part and it goes through mc-preprocessor.xslt:
	 * "encountered unexpected content; pre-processing" (JaxbXmlPart), "encountered unexpected
	 * content in /word/document.xml; pre-processing" (JaxbXmlPartXPathAware, most WordprocessingML
	 * parts).
	 */
	static boolean isPreprocessLine(String pattern) {
		return pattern != null && pattern.startsWith("encountered unexpected content") && pattern.endsWith("; pre-processing");
	}

	private static volatile Session current;

	private LogCapture() {
	}

	/** One document's events. */
	static final class Session {

		final Thread thread;
		private String phase = "load";
		private final Map<List<String>, int[]> messages = new LinkedHashMap<>();
		private int overflow;
		private int warnings;
		private int errors;
		private int preprocessed;

		Session(Thread thread) {
			this.thread = thread;
		}

		synchronized void phase(String p) {
			phase = p;
		}

		synchronized void preprocessed() {
			if (counts()) preprocessed++;
		}

		/** Whether an event now counts toward the row's totals: those of loading and reading. */
		private boolean counts() {
			return "load".equals(phase) || "read".equals(phase);
		}

		synchronized void record(Level level, String logger, String message, String throwable) {
			if (counts()) {
				if (level == Level.ERROR) errors++;
				else warnings++;
			}
			List<String> key = List.of(phase, level.name(), String.valueOf(logger),
					String.valueOf(message), throwable == null ? "" : throwable);
			int[] n = messages.get(key);
			if (n != null) {
				n[0]++;
			} else if (messages.size() < MAX_DISTINCT) {
				messages.put(key, new int[] { 1 });
			} else {
				overflow++;
			}
		}

		synchronized int warnings() {
			return warnings;
		}

		synchronized int errors() {
			return errors;
		}

		synchronized int preprocessedParts() {
			return preprocessed;
		}

		synchronized int overflow() {
			return overflow;
		}

		/** The message rows for this document, each {@link Column.Message} in order. */
		synchronized List<Object[]> rows(String sha) {
			List<Object[]> rows = new ArrayList<>(messages.size());
			for (Map.Entry<List<String>, int[]> e : messages.entrySet()) {
				List<String> k = e.getKey();
				rows.add(new Object[] { sha, k.get(0), k.get(1), k.get(2), k.get(3),
						k.get(4).isEmpty() ? null : k.get(4), e.getValue()[0] });
			}
			return rows;
		}
	}

	static void begin(Session s) {
		current = s;
	}

	static void end(Session s) {
		if (current == s) current = null;
	}

	static void log(Level level, String logger, String pattern, Object[] args, Throwable t) {
		Session s = current;
		boolean ours = s != null && Thread.currentThread() == s.thread;
		if (level == Level.INFO) {
			if (ours && isPreprocessLine(pattern)) s.preprocessed();
			return;
		}
		if (level != Level.WARN && level != Level.ERROR) return;
		String message = args == null || args.length == 0 ? pattern : MessageFormatter.basicArrayFormat(pattern, args);
		if (ours) {
			s.record(level, logger, Describe.line(message), Describe.of(t));
		} else {
			System.err.println(level + " " + logger + " [" + Thread.currentThread().getName() + "] "
					+ Describe.line(message) + (t == null ? "" : " " + Describe.withRoot(t)));
		}
	}
}
