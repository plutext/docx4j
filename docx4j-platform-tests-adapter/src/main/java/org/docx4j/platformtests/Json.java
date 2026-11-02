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
package org.docx4j.platformtests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the runner's operation.json (RFC 8259) into maps, lists, strings, numbers (Long or Double), booleans and
 * null, so that the adapter needs nothing beyond docx4j.
 */
final class Json {

	private final String s;
	private int i;

	private Json(String s) { this.s = s; }

	static Object parse(String text) {
		Json j = new Json(text);
		j.space();
		Object v = j.value();
		j.space();
		if (j.i != j.s.length()) throw j.error("trailing characters");
		return v;
	}

	private Object value() {
		if (i >= s.length()) throw error("unexpected end");
		char c = s.charAt(i);
		switch (c) {
		case '{': return object();
		case '[': return array();
		case '"': return string();
		case 't': return word("true", Boolean.TRUE);
		case 'f': return word("false", Boolean.FALSE);
		case 'n': return word("null", null);
		default: return number();
		}
	}

	private Map<String, Object> object() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		i++;
		space();
		if (peek('}')) { i++; return m; }
		while (true) {
			space();
			if (!peek('"')) throw error("expected a key");
			String key = string();
			space();
			expect(':');
			space();
			m.put(key, value());
			space();
			if (peek(',')) { i++; continue; }
			expect('}');
			return m;
		}
	}

	private List<Object> array() {
		List<Object> l = new ArrayList<Object>();
		i++;
		space();
		if (peek(']')) { i++; return l; }
		while (true) {
			space();
			l.add(value());
			space();
			if (peek(',')) { i++; continue; }
			expect(']');
			return l;
		}
	}

	private String string() {
		StringBuilder sb = new StringBuilder();
		i++;
		while (i < s.length()) {
			char c = s.charAt(i++);
			if (c == '"') return sb.toString();
			if (c != '\\') { sb.append(c); continue; }
			if (i >= s.length()) break;
			char e = s.charAt(i++);
			switch (e) {
			case '"': sb.append('"'); break;
			case '\\': sb.append('\\'); break;
			case '/': sb.append('/'); break;
			case 'b': sb.append('\b'); break;
			case 'f': sb.append('\f'); break;
			case 'n': sb.append('\n'); break;
			case 'r': sb.append('\r'); break;
			case 't': sb.append('\t'); break;
			case 'u':
				if (i + 4 > s.length()) throw error("short \\u escape");
				sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
				i += 4;
				break;
			default: throw error("bad escape \\" + e);
			}
		}
		throw error("unterminated string");
	}

	private Object number() {
		int start = i;
		while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
		String n = s.substring(start, i);
		if (n.isEmpty()) throw error("unexpected character '" + s.charAt(start) + "'");
		if (n.indexOf('.') < 0 && n.indexOf('e') < 0 && n.indexOf('E') < 0) return Long.valueOf(n);
		return Double.valueOf(n);
	}

	private Object word(String w, Object v) {
		if (!s.startsWith(w, i)) throw error("unexpected token");
		i += w.length();
		return v;
	}

	private void space() {
		while (i < s.length() && " \t\r\n".indexOf(s.charAt(i)) >= 0) i++;
	}

	private boolean peek(char c) { return i < s.length() && s.charAt(i) == c; }

	private void expect(char c) {
		if (!peek(c)) throw error("expected '" + c + "'");
		i++;
	}

	private IllegalArgumentException error(String what) {
		return new IllegalArgumentException("operation.json: " + what + " at offset " + i);
	}
}
