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
package org.docx4j.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.Style;

/**
 * Where a paragraph sits in a table, as far as its formatting is concerned: the table style
 * which applies to the table, and the conditional formats ({@code w:tblStylePr}) the paragraph
 * is under.  Handed to {@link org.docx4j.model.PropertyResolver}'s overloads which take one,
 * so that the resolver can put the table style's contribution where ECMA-376-1 &#xa7;17.7.2
 * puts it: above the document defaults, below the paragraph's own style.
 *
 * <p>Obtained from a {@link TableContext} ({@link TableContext#forParagraph}), from a
 * {@link TableContextTracker} during a walk, or from
 * {@link org.docx4j.model.PropertyResolver#cellContextOf(org.docx4j.wml.P)}.  Null stands for
 * "not in a table".  Immutable.</p>
 *
 * <p>See docs/developer/change-requests/CR-030-table-conditions-in-the-resolver.md.</p>
 *
 * @since 17.3.1
 */
public final class CellContext {

	private final String tableStyleId;
	private final Style tableStyle;
	private final Set<STTblStyleOverrideType> conditions;
	private final List<CTTblStylePr> textConditions;
	private final boolean formatsText;
	private final Key key;

	/**
	 * @param tableStyleId the id of the table style the table resolves to, or null
	 * @param tableStyle that style's w:basedOn chain merged (never null; shared, not to be changed)
	 * @param conditions the conditions the paragraph is under (TableStyleConditions.resolve)
	 */
	CellContext(String tableStyleId, Style tableStyle, Set<STTblStyleOverrideType> conditions) {
		this.tableStyleId = tableStyleId;
		this.tableStyle = tableStyle;
		EnumSet<STTblStyleOverrideType> c = EnumSet.noneOf(STTblStyleOverrideType.class);
		if (conditions != null) c.addAll(conditions);
		this.conditions = Collections.unmodifiableSet(c);

		List<CTTblStylePr> text = new ArrayList<CTTblStylePr>();
		EnumSet<STTblStyleOverrideType> named = EnumSet.noneOf(STTblStyleOverrideType.class);
		for (CTTblStylePr pr : TableStyleConditions.applicable(tableStyle, c)) {
			if (TableStyleConditions.formatsText(pr)) {
				text.add(pr);
				named.add(pr.getType());
			}
		}
		this.textConditions = Collections.unmodifiableList(text);
		this.formatsText = !text.isEmpty()
				|| (tableStyle != null && tableStyle.getPPr() != null && !StyleUtil.isEmpty(tableStyle.getPPr()))
				|| (tableStyle != null && tableStyle.getRPr() != null && !StyleUtil.isEmpty(tableStyle.getRPr()));
		this.key = new Key(tableStyleId, named);
	}

	/** The id of the table style the table resolves to, or null where none applies. */
	public String getTableStyleId() {
		return tableStyleId;
	}

	/** The table style's w:basedOn chain merged, as {@link org.docx4j.model.PropertyResolver#getTableStyleChain(String)}
	 *  gives it: shared, so read it and do not change it. */
	public Style getTableStyle() {
		return tableStyle;
	}

	/** The conditions the paragraph is under, gated by the table's w:tblLook. */
	public Set<STTblStyleOverrideType> getConditions() {
		return conditions;
	}

	/** The table style's conditional formats which apply here and give text a w:pPr or
	 *  w:rPr, in the order they are applied (ECMA-376-1 &#xa7;17.7.6). */
	public List<CTTblStylePr> getTextConditions() {
		return textConditions;
	}

	/** Whether the table style gives this paragraph anything at all: its own w:pPr or w:rPr,
	 *  or a conditional format with one.  Where it does not, resolving with this context
	 *  gives the same answer as resolving without one. */
	public boolean formatsText() {
		return formatsText;
	}

	/** What the table level's composition depends on: the table style and the conditional
	 *  formats which format text.  A value, for keying caches. */
	public Key getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "CellContext[" + tableStyleId + " " + conditions + "]";
	}

	/**
	 * The table level of a paragraph's formatting, as a value: two paragraphs whose keys are
	 * equal get the same contribution from their table.  A structured key, never a joined
	 * string (CR-030 D8: ids may contain the separator).
	 */
	public static final class Key {

		private final String tableStyleId;
		private final Set<STTblStyleOverrideType> conditions;

		Key(String tableStyleId, Set<STTblStyleOverrideType> conditions) {
			this.tableStyleId = tableStyleId;
			this.conditions = conditions;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Key)) return false;
			Key k = (Key) o;
			return (tableStyleId == null ? k.tableStyleId == null : tableStyleId.equals(k.tableStyleId))
					&& conditions.equals(k.conditions);
		}

		@Override
		public int hashCode() {
			return 31 * (tableStyleId == null ? 0 : tableStyleId.hashCode()) + conditions.hashCode();
		}

		@Override
		public String toString() {
			return tableStyleId + " " + conditions;
		}
	}
}
