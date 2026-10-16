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
package org.docx4j.model.revisions;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.model.revisions.TrackedChange.Kind;

/**
 * A document's tracked changes counted (CR-043 section 3.1): by kind, by author and by part, with
 * the changes this release lists but does not resolve (table, section and cell records, CR-043
 * phase 2) counted apart, so that a document that still shows them in Word after
 * {@code acceptAll} is not misread. {@link #toJson()} is the form docx4j-mcp reports.
 *
 * @since 17.3.3
 */
public final class TrackedChangesReport {

	private final int total;
	private final int unresolved;
	private final Map<Kind, Integer> byKind = new EnumMap<Kind, Integer>(Kind.class);
	private final Map<String, Integer> byAuthor = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> byPart = new LinkedHashMap<String, Integer>();

	TrackedChangesReport(List<TrackedChange> changes) {
		int unresolved = 0;
		for (TrackedChange change : changes) {
			Kind kind = change.getKind();
			byKind.merge(kind, 1, Integer::sum);
			byAuthor.merge(String.valueOf(change.getAuthor()), 1, Integer::sum);
			String part = change.getPart() == null ? "" : change.getPart().getPartName().getName();
			byPart.merge(part, 1, Integer::sum);
			if (!kind.isResolvable()) unresolved++;
		}
		this.total = changes.size();
		this.unresolved = unresolved;
	}

	/** How many changes are listed. */
	public int getTotal() {
		return total;
	}

	/** How many of them this release does not resolve (their kinds' {@link Kind#isResolvable()} is false). */
	public int getUnresolved() {
		return unresolved;
	}

	public Map<Kind, Integer> getByKind() {
		return Collections.unmodifiableMap(byKind);
	}

	public Map<String, Integer> getByAuthor() {
		return Collections.unmodifiableMap(byAuthor);
	}

	/** By part name ({@code /word/document.xml}, {@code /word/footnotes.xml}, ...). */
	public Map<String, Integer> getByPart() {
		return Collections.unmodifiableMap(byPart);
	}

	/**
	 * <pre>
	 * {"total":3,"unresolved":1,
	 *  "byKind":{"RUN_INSERTION":2,"TABLE_PROPERTIES":1},
	 *  "unresolvedKinds":["TABLE_PROPERTIES"],
	 *  "byAuthor":{"Jason":3},"byPart":{"/word/document.xml":3}}
	 * </pre>
	 */
	public String toJson() {
		StringBuilder out = new StringBuilder();
		out.append("{\"total\":").append(total).append(",\"unresolved\":").append(unresolved).append(",\"byKind\":{");
		boolean first = true;
		for (Map.Entry<Kind, Integer> e : byKind.entrySet()) {
			if (!first) out.append(',');
			first = false;
			out.append(quote(e.getKey().name())).append(':').append(e.getValue());
		}
		out.append("},\"unresolvedKinds\":[");
		first = true;
		for (Kind kind : byKind.keySet()) {
			if (kind.isResolvable()) continue;
			if (!first) out.append(',');
			first = false;
			out.append(quote(kind.name()));
		}
		out.append("],\"byAuthor\":");
		object(byAuthor, out);
		out.append(",\"byPart\":");
		object(byPart, out);
		return out.append('}').toString();
	}

	private static void object(Map<String, Integer> map, StringBuilder out) {
		out.append('{');
		boolean first = true;
		for (Map.Entry<String, Integer> e : map.entrySet()) {
			if (!first) out.append(',');
			first = false;
			out.append(quote(e.getKey())).append(':').append(e.getValue());
		}
		out.append('}');
	}

	private static String quote(String s) {
		StringBuilder out = new StringBuilder("\"");
		for (char c : s.toCharArray()) {
			if (c == '"' || c == '\\') out.append('\\').append(c);
			else if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
			else out.append(c);
		}
		return out.append('"').toString();
	}

	@Override
	public String toString() {
		return toJson();
	}
}
