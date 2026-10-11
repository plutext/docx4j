/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.chart;

import java.util.regex.Pattern;

/**
 * A minimal JSON writer for the chart model's JSON form: objects and arrays opened and closed in
 * order, two-space indentation, one member or element per line, so that the goldens diff well.
 * Numbers from a chart's caches are written as the file has them ({@link #rawNumber}).
 */
final class Json {

	private static final Pattern NUMBER = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");

	private final StringBuilder sb = new StringBuilder();
	private int depth;
	private boolean first = true;

	private void separate() {
		if (!first) sb.append(',');
		if (depth > 0) sb.append('\n');
		for (int i = 0; i < depth; i++) sb.append("  ");
		first = false;
	}

	private void name(String name) {
		separate();
		if (name != null) {
			string(name);
			sb.append(": ");
		}
	}

	Json open(String name) {
		name(name);
		sb.append('{');
		depth++;
		first = true;
		return this;
	}

	Json openArray(String name) {
		name(name);
		sb.append('[');
		depth++;
		first = true;
		return this;
	}

	Json close() {
		return end('}');
	}

	Json closeArray() {
		return end(']');
	}

	private Json end(char c) {
		depth--;
		if (!first) {
			sb.append('\n');
			for (int i = 0; i < depth; i++) sb.append("  ");
		}
		sb.append(c);
		first = false;
		return this;
	}

	Json put(String name, String value) {
		name(name);
		if (value == null) sb.append("null");
		else string(value);
		return this;
	}

	Json put(String name, Number value) {
		name(name);
		sb.append(value == null ? "null" : number(value));
		return this;
	}

	Json put(String name, Boolean value) {
		name(name);
		sb.append(value == null ? "null" : value.toString());
		return this;
	}

	/** A cache's point: its text as a JSON number where it is one, else as a string; null for a gap */
	Json rawNumber(String name, String text) {
		name(name);
		if (text == null) sb.append("null");
		else if (NUMBER.matcher(text.trim()).matches()) sb.append(text.trim());
		else string(text);
		return this;
	}

	Json nul(String name) {
		name(name);
		sb.append("null");
		return this;
	}

	private static String number(Number n) {
		if (n instanceof Double || n instanceof Float) {
			double d = n.doubleValue();
			if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) return String.valueOf((long) d);
			return String.valueOf(d);
		}
		return n.toString();
	}

	private void string(String s) {
		sb.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				default:
					if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
					else sb.append(c);
			}
		}
		sb.append('"');
	}

	@Override
	public String toString() {
		return sb.toString() + "\n";
	}
}
