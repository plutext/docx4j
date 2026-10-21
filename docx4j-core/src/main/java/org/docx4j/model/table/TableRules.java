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

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSplice;
import org.docx4j.mce.AlternateContent;
import org.docx4j.wml.CTCustomXmlBlock;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;

/**
 * Word's rules for what a table cell holds, as measured on Word 365 (the record is
 * {@code docs/developer/word-table-rules.md}): a cell holds at least one block, and its last block,
 * <b>as Word reads it</b>, is a paragraph. Word looks through block-level content controls and
 * custom XML elements, and reads one branch of an {@code mc:AlternateContent}; the branches it
 * does not read are not checked. A cell that breaks the rule opens as "unreadable content", and
 * Word's repair appends an empty paragraph to the cell, after any wrapper.
 *
 * <p>Writers of cells use {@link #endWithParagraph(Tc)} before saving, which does what Word's
 * repair does and nothing when the rule already holds, so a cell ending in a wrapped paragraph
 * gets no spare paragraph.
 *
 * @since 17.3.4
 */
public final class TableRules {

	private TableRules() {}

	/**
	 * Whether the last block among these cell contents, as Word reads it, is a paragraph.
	 * Range markup (bookmarks, comment and move ranges, permissions) after the last block does
	 * not count; a list with no block at all is false.
	 */
	public static boolean endsWithParagraph(List<Object> contents) {
		Object last = lastBlock(contents);
		if (last instanceof P) return true;
		if (last instanceof SdtBlock) return endsWithParagraph(((SdtBlock) last).getSdtContent().getContent());
		if (last instanceof CTCustomXmlBlock) return endsWithParagraph(((CTCustomXmlBlock) last).getContent());
		if (last instanceof AlternateContent) {
			// the branch Word reads, as the text model reads it (CR-051 section 10.3); the others are not checked
			List<Object> read = McSelection.branches((AlternateContent) last, McMode.READ);
			if (read.isEmpty()) return false;
			List<Object> items = McSplice.contentOf(read.get(0));
			return items != null && endsWithParagraph(items);
		}
		return false;
	}

	/** Whether the cell's last block, as Word reads it, is a paragraph ({@link #endsWithParagraph(List)}). */
	public static boolean endsWithParagraph(Tc tc) {
		return endsWithParagraph(tc.getContent());
	}

	/**
	 * Appends an empty paragraph to the cell when its last block, as Word reads it, is not one
	 * (Word's own repair: the paragraph goes after any wrapper, never inside it).
	 *
	 * @return true when a paragraph was appended
	 */
	public static boolean endWithParagraph(Tc tc) {
		if (endsWithParagraph(tc)) return false;
		P p = Context.getWmlObjectFactory().createP();
		p.setParent(tc);
		tc.getContent().add(p);
		return true;
	}

	/** The last block-level item (a paragraph, table, block control, custom XML block or {@code mc:AlternateContent}), unwrapped; null when there is none. */
	private static Object lastBlock(List<Object> contents) {
		for (int i = contents.size() - 1; i >= 0; i--) {
			Object v = XmlUtils.unwrap(contents.get(i));
			if (v instanceof P || v instanceof Tbl || v instanceof SdtBlock || v instanceof CTCustomXmlBlock
					|| v instanceof AlternateContent) {
				return v;
			}
		}
		return null;
	}
}
