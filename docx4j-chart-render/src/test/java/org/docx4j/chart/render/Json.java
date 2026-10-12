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
package org.docx4j.chart.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just enough JSON to read the oracle's files (Office's layout dumps, chart_trace.py's readings):
 * objects as maps, arrays as lists, numbers as doubles.
 */
final class Json {

	private final String s;
	private int i;

	private Json(String s) {
		this.s = s;
	}

	static Object parse(String text) {
		Json j = new Json(text);
		j.space();
		Object v = j.value();
		j.space();
		if (j.i != j.s.length()) throw new IllegalArgumentException("trailing text at " + j.i);
		return v;
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> object(Object o) {
		return (Map<String, Object>) o;
	}

	@SuppressWarnings("unchecked")
	static List<Object> list(Object o) {
		return o == null ? new ArrayList<Object>() : (List<Object>) o;
	}

	static Double number(Object o) {
		return o instanceof Number ? ((Number) o).doubleValue() : null;
	}

	private void space() {
		while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
	}

	private Object value() {
		char c = s.charAt(i);
		switch (c) {
		case '{': {
			Map<String, Object> m = new LinkedHashMap<>();
			i++;
			space();
			if (s.charAt(i) == '}') {
				i++;
				return m;
			}
			while (true) {
				space();
				String k = string();
				space();
				expect(':');
				space();
				m.put(k, value());
				space();
				if (s.charAt(i) == ',') {
					i++;
					continue;
				}
				expect('}');
				return m;
			}
		}
		case '[': {
			List<Object> l = new ArrayList<>();
			i++;
			space();
			if (s.charAt(i) == ']') {
				i++;
				return l;
			}
			while (true) {
				space();
				l.add(value());
				space();
				if (s.charAt(i) == ',') {
					i++;
					continue;
				}
				expect(']');
				return l;
			}
		}
		case '"':
			return string();
		default:
			if (s.startsWith("true", i)) {
				i += 4;
				return Boolean.TRUE;
			}
			if (s.startsWith("false", i)) {
				i += 5;
				return Boolean.FALSE;
			}
			if (s.startsWith("null", i)) {
				i += 4;
				return null;
			}
			int start = i;
			while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
			return Double.parseDouble(s.substring(start, i));
		}
	}

	private void expect(char c) {
		if (s.charAt(i) != c) throw new IllegalArgumentException("expected " + c + " at " + i);
		i++;
	}

	private String string() {
		expect('"');
		StringBuilder b = new StringBuilder();
		while (true) {
			char c = s.charAt(i++);
			if (c == '"') return b.toString();
			if (c != '\\') {
				b.append(c);
				continue;
			}
			char e = s.charAt(i++);
			switch (e) {
			case 'n': b.append('\n'); break;
			case 't': b.append('\t'); break;
			case 'r': b.append('\r'); break;
			case 'b': b.append('\b'); break;
			case 'f': b.append('\f'); break;
			case 'u':
				b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
				i += 4;
				break;
			default: b.append(e);
			}
		}
	}
}
