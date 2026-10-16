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

import java.util.List;

import org.docx4j.model.revisions.TrackedChange.FieldT;
import org.docx4j.model.revisions.TrackedChange.GroupT;
import org.docx4j.model.revisions.TrackedChange.MathRunT;
import org.docx4j.model.revisions.TrackedChange.ParaPropsT;
import org.docx4j.model.revisions.TrackedChange.RowT;
import org.docx4j.model.revisions.TrackedChange.RunPropsT;
import org.docx4j.model.revisions.TrackedChange.RunT;
import org.docx4j.model.revisions.TrackedChange.SectionT;
import org.docx4j.model.revisions.TrackedChange.TableT;
import org.docx4j.model.revisions.TrackedChange.CellT;
import org.docx4j.model.revisions.TrackedChange.Target;
import org.docx4j.model.revisions.Trees.Revision;
import org.docx4j.model.revisions.Trees.View;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

/** A tracked change's text, as Office JS gives it (core-ts {@code TrackedChange.text}; checks 22, 27, 30, 31). */
final class Texts {

	private Texts() {}

	static String of(TrackedChange change) {
		Target t = change.target;
		if (t instanceof RunT) {
			Revision r = ((RunT) t).revision;
			return Trees.text(((RunT) t).item, r == Revision.INS || r == Revision.MOVE_TO ? View.ACCEPTED : View.ORIGINAL);
		}
		if (t instanceof TrackedChange.MarkT || t instanceof TrackedChange.MarkPropsT) return "\r";
		if (t instanceof GroupT) {
			StringBuilder out = new StringBuilder();
			for (TrackedChange piece : ((GroupT) t).pieces) out.append(piece.getText());
			return out.toString();
		}
		if (t instanceof RunPropsT) return Trees.text(((RunPropsT) t).run, View.ACCEPTED);
		if (t instanceof FieldT) {
			StringBuilder out = new StringBuilder();
			for (Object item : ((FieldT) t).span) Trees.appendText(item, View.ACCEPTED, out);
			return out.toString();
		}
		if (t instanceof ParaPropsT) return change.para == null ? "" : Trees.paragraphText(change.para.p, View.ACCEPTED);
		if (t instanceof RowT) return rowText(((RowT) t).tr, ((RowT) t).ins ? View.ACCEPTED : View.ORIGINAL);
		if (t instanceof MathRunT) {
			StringBuilder out = new StringBuilder();
			for (Object item : ((MathRunT) t).value.getRest()) {
				String s = Trees.itemText(item);
				if (s != null) out.append(s);
			}
			return out.toString();
		}
		if (t instanceof TableT) return Tables.changeText(((TableT) t).tbl);
		if (t instanceof CellT) return rowText(((CellT) t).tr, View.ACCEPTED);
		if (t instanceof SectionT) return ((SectionT) t).text;
		return "";
	}

	/**
	 * A row's text: its cells a tab apart, then {@code \r\n}; a cell's paragraphs {@code \r} apart; a
	 * nested table's rows each with their own {@code \r\n} (checks 30 and 31).
	 */
	static String rowText(Tr tr, View view) {
		StringBuilder out = new StringBuilder();
		boolean first = true;
		for (Object item : tr.getContent()) {
			Object v = Trees.unwrap(item);
			if (!(v instanceof Tc)) continue;
			if (!first) out.append('\t');
			first = false;
			cellText(((Tc) v).getContent(), view, out, new boolean[1]);
		}
		return out.append("\r\n").toString();
	}

	private static void cellText(List<Object> items, View view, StringBuilder out, boolean[] afterParagraph) {
		for (Object item : items) {
			Object v = Trees.unwrap(item);
			if (v instanceof P) {
				if (afterParagraph[0]) out.append('\r');
				out.append(Trees.paragraphText((P) v, view));
				afterParagraph[0] = true;
			} else if (v instanceof Tbl) {
				for (Object row : ((Tbl) v).getContent()) {
					Object rv = Trees.unwrap(row);
					if (rv instanceof Tr) out.append(rowText((Tr) rv, view));
				}
				afterParagraph[0] = false;
			} else {
				List<Object> children = Trees.blockItems(v);
				if (children != null) cellText(children, view, out, afterParagraph);
			}
		}
	}
}
