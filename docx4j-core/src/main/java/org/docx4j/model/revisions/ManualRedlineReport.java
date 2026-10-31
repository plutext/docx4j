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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What {@link ManualRedline} converted, and what it left with its reason (CR-068 section 3.1).
 * Each {@link Change} is one thing deleted: struck runs (a {@code w:del}), a separating space (in the
 * struck text's {@code w:del}, or one of its own), a paragraph mark. Addresses are the text model's ({@code body/12},
 * {@code header:rId8/2}; {@code org.docx4j.model.text.Texts#addresses}) of the paragraph, read
 * before the conversion; a text box's paragraph that the text model gives none takes the address of
 * the paragraph the box is in. A text box's other {@code mc:AlternateContent} branch is converted
 * alike and not reported again. {@link #toJson()} is the form docx4j-mcp reports.
 *
 * @since 17.3.5
 */
public final class ManualRedlineReport {

	/** What a change converted: struck runs, double-struck runs, a separating space (D13), a paragraph mark (D9). */
	public enum Kind {
		STRIKE("strike"), DOUBLE_STRIKE("double_strike"), SEPARATOR_SPACE("separator_space"), PARAGRAPH_MARK("paragraph_mark");

		private final String label;

		Kind(String label) {
			this.label = label;
		}

		/** The name in {@link #toJson()}. */
		public String label() {
			return label;
		}
	}

	/** Why a struck run, or a struck paragraph mark, was left as it is (CR-068 section 3.2, D10). */
	public enum Reason {
		/** Inside {@code w:ins}, {@code w:del}, {@code w:moveFrom} or {@code w:moveTo}, or its strike itself a tracked formatting change. */
		IN_TRACKED_CHANGE("in_tracked_change"),
		/** A run of a field, {@code begin} to {@code end}, or in a {@code w:fldSimple}. */
		FIELD("field"),
		/** A math run. */
		MATH("math"),
		/** Struck by a style only, not by its own properties (D5). */
		STYLE_STRIKE("style_strike"),
		/** A wholly converted paragraph's struck mark that Word cannot delete: a cell's last, one before a table or ending a section or its story. */
		PARAGRAPH_MARK("paragraph_mark"),
		/** In a comment: the comments story is not converted by default (D2). */
		COMMENT("comment");

		private final String label;

		Reason(String label) {
			this.label = label;
		}

		/** The name in {@link #toJson()}. */
		public String label() {
			return label;
		}
	}

	/** One tracked deletion written. */
	public static final class Change {
		private final Kind kind;
		private final String part;
		private final String address;
		private final String text;
		private final int runs;
		private final boolean ownDeletion;

		Change(Kind kind, String part, String address, String text, int runs, boolean ownDeletion) {
			this.kind = kind;
			this.part = part;
			this.address = address;
			this.text = text;
			this.runs = runs;
			this.ownDeletion = ownDeletion;
		}

		public Kind getKind() {
			return kind;
		}

		/** The part's name ({@code /word/document.xml}, ...). */
		public String getPart() {
			return part;
		}

		/** The paragraph's address in the text model; null where it has none. */
		public String getAddress() {
			return address;
		}

		/** The text deleted ({@code \t} for a tab, {@code \n} for a break; empty for a paragraph mark). */
		public String getText() {
			return text;
		}

		/** How many runs the deletion holds (0 for a paragraph mark). */
		public int getRuns() {
			return runs;
		}

		/**
		 * Whether it is a tracked deletion of its own: false for a separating space that went into the
		 * {@code w:del} of the struck text beside it, as Word's own deletion holds it (one revision).
		 */
		public boolean isOwnDeletion() {
			return ownDeletion;
		}

		@Override
		public String toString() {
			return kind.label() + " " + address + " '" + text + "'";
		}
	}

	/** Struck runs (consecutive ones of one paragraph and reason together), or a struck paragraph mark, left as they are. */
	public static final class Left {
		private final Reason reason;
		private final String part;
		private final String address;
		private final String text;

		Left(Reason reason, String part, String address, String text) {
			this.reason = reason;
			this.part = part;
			this.address = address;
			this.text = text;
		}

		public Reason getReason() {
			return reason;
		}

		public String getPart() {
			return part;
		}

		public String getAddress() {
			return address;
		}

		/** The run's text (empty for a paragraph mark). */
		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return reason.label() + " " + address + " '" + text + "'";
		}
	}

	private final boolean preview;
	private final List<Change> changes = new ArrayList<Change>();
	private final List<Left> left = new ArrayList<Left>();

	ManualRedlineReport(boolean preview) {
		this.preview = preview;
	}

	void add(Change change) {
		changes.add(change);
	}

	/** A left run; one following another of the same reason in the same paragraph joins it (a field's runs are one entry). */
	void add(Left l) {
		Left last = left.isEmpty() ? null : left.get(left.size() - 1);
		if (last != null && last.reason == l.reason && last.part.equals(l.part)
				&& (last.address == null ? l.address == null : last.address.equals(l.address))) {
			left.set(left.size() - 1, new Left(last.reason, last.part, last.address, last.text + l.text));
		} else {
			left.add(l);
		}
	}

	/** True for {@link ManualRedline#preview}: what a conversion would do; the package was not changed. */
	public boolean isPreview() {
		return preview;
	}

	/** Every tracked deletion written, in document order, story by story. */
	public List<Change> getChanges() {
		return Collections.unmodifiableList(changes);
	}

	/** Every struck run or mark left, with its reason. */
	public List<Left> getLeft() {
		return Collections.unmodifiableList(left);
	}

	/** How many tracked deletions were written: each {@code w:del}, each paragraph mark (Word may list a paragraph's text and mark as one). */
	public int getDeletions() {
		int n = 0;
		for (Change c : changes) if (c.ownDeletion) n++;
		return n;
	}

	/** How many tracked insertions were written: none until the opt-ins (CR-068 phase 2). */
	public int getInsertions() {
		return 0;
	}

	/** How many runs were converted. */
	public int getRuns() {
		int n = 0;
		for (Change c : changes) if (c.kind == Kind.STRIKE || c.kind == Kind.DOUBLE_STRIKE) n += c.runs;
		return n;
	}

	/** Changes by kind, by their {@link Kind#label()}. */
	public Map<String, Integer> getByKind() {
		Map<String, Integer> out = new LinkedHashMap<String, Integer>();
		for (Change c : changes) out.merge(c.kind.label(), 1, Integer::sum);
		return Collections.unmodifiableMap(out);
	}

	/** Changes by part name. */
	public Map<String, Integer> getByPart() {
		Map<String, Integer> out = new LinkedHashMap<String, Integer>();
		for (Change c : changes) out.merge(c.part, 1, Integer::sum);
		return Collections.unmodifiableMap(out);
	}

	/** What was left, by reason, by their {@link Reason#label()}. */
	public Map<String, Integer> getLeftByReason() {
		Map<String, Integer> out = new LinkedHashMap<String, Integer>();
		for (Left l : left) out.merge(l.reason.label(), 1, Integer::sum);
		return Collections.unmodifiableMap(out);
	}

	/**
	 * <pre>
	 * {"preview":false,"deletions":2,"insertions":0,"runs":1,"byKind":{"strike":1,"separator_space":1},
	 *  "byPart":{"/word/document.xml":2},
	 *  "changes":[{"kind":"strike","part":"/word/document.xml","address":"body/1","text":"1.316"}, ...],
	 *  "left":[{"reason":"field","part":"/word/document.xml","address":"body/7","text":"Version 2"}]}
	 * </pre>
	 */
	public String toJson() {
		StringBuilder out = new StringBuilder();
		out.append("{\"preview\":").append(preview).append(",\"deletions\":").append(getDeletions())
				.append(",\"insertions\":").append(getInsertions()).append(",\"runs\":").append(getRuns())
				.append(",\"byKind\":");
		object(getByKind(), out);
		out.append(",\"byPart\":");
		object(getByPart(), out);
		out.append(",\"changes\":[");
		for (int i = 0; i < changes.size(); i++) {
			Change c = changes.get(i);
			if (i > 0) out.append(',');
			out.append("{\"kind\":").append(quote(c.kind.label())).append(",\"part\":").append(quote(c.part))
					.append(",\"address\":").append(c.address == null ? "null" : quote(c.address))
					.append(",\"text\":").append(quote(c.text)).append('}');
		}
		out.append("],\"left\":[");
		for (int i = 0; i < left.size(); i++) {
			Left l = left.get(i);
			if (i > 0) out.append(',');
			out.append("{\"reason\":").append(quote(l.reason.label())).append(",\"part\":").append(quote(l.part))
					.append(",\"address\":").append(l.address == null ? "null" : quote(l.address))
					.append(",\"text\":").append(quote(l.text)).append('}');
		}
		return out.append("]}").toString();
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
		for (char c : (s == null ? "" : s).toCharArray()) {
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
