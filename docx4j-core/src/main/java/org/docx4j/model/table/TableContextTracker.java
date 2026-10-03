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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

/**
 * A {@code TraversalUtil} callback which knows, for each paragraph it reaches, the table
 * context the paragraph is in: extend it, and call {@link #cellContext(P)} from
 * {@link #apply(Object)} for each {@code P}.  The way to resolve every paragraph of a document
 * in context; {@link PropertyResolver#cellContextOf(P)} builds a {@link TableContext} per call
 * and is for one paragraph at a time.
 *
 * <p>The table on top of the stack is the paragraph's own (a nested table pushes its own
 * context, and a nested table's formatting owes nothing to its parent's).  A text box is a
 * story of its own: Word gives the paragraphs of one anchored in a cell none of the table's
 * formatting (CR-030 probe T3), so the walk starts an empty stack inside every text box
 * and restores the cell's on the way out.</p>
 *
 * @since 17.3.1
 */
public abstract class TableContextTracker extends CallbackImpl {

	private final PropertyResolver resolver;

	/** One entry per table being walked, innermost first; a barrier (null table) inside a text box. */
	private final Deque<Frame> stack = new ArrayDeque<Frame>();

	private static final class Frame {
		final TableContext table;
		Tr tr;
		Tc tc;
		Frame(TableContext table) {
			this.table = table;
		}
	}

	protected TableContextTracker(PropertyResolver resolver) {
		this.resolver = resolver;
	}

	protected PropertyResolver getPropertyResolver() {
		return resolver;
	}

	/**
	 * The context of a paragraph reached by this walk, or null where it is in no table (or in
	 * a text box, which is a story of its own).
	 */
	protected CellContext cellContext(P p) {
		Frame f = stack.peek();
		if (f == null || f.table == null) return null;
		if (p.getParent() instanceof CTTxbxContent) return null;
		return f.table.forParagraph(f.tr, f.tc, p.getPPr());
	}

	/** The table the walk is in, or null (outside any table, or in a text box). */
	protected TableContext tableContext() {
		Frame f = stack.peek();
		return f == null ? null : f.table;
	}

	@Override
	public void walkJAXBElements(Object parent) {

		List<Object> children = getChildren(parent);
		if (children == null) return;

		for (Object o : children) {

			o = XmlUtils.unwrap(o);

			this.apply(o);

			Frame enclosing = stack.peek();
			boolean pushed = false;
			Tr trBefore = null;
			Tc tcBefore = null;
			if (o instanceof Tbl) {
				stack.push(new Frame(resolver.tableContext((Tbl) o)));
				pushed = true;
			} else if (isStoryBoundary(o)) {
				stack.push(new Frame(null));
				pushed = true;
			} else if (enclosing != null && enclosing.table != null && o instanceof Tr) {
				trBefore = enclosing.tr;
				tcBefore = enclosing.tc;
				enclosing.tr = (Tr) o;
				enclosing.tc = null;
			} else if (enclosing != null && enclosing.table != null && o instanceof Tc) {
				tcBefore = enclosing.tc;
				enclosing.tc = (Tc) o;
			}

			if (this.shouldTraverse(o)) {
				walkJAXBElements(o);
			}

			if (pushed) {
				stack.pop();
			} else if (enclosing != null && enclosing.table != null && o instanceof Tr) {
				enclosing.tr = trBefore;
				enclosing.tc = tcBefore;
			} else if (enclosing != null && enclosing.table != null && o instanceof Tc) {
				enclosing.tc = tcBefore;
			}
		}
	}

	/**
	 * Whether a walk entering this object enters another story: the objects whose children
	 * TraversalUtil gives as a text box's content (a DrawingML shape's, through its anchor or
	 * inline, and a VML text box's), and w:txbxContent itself where it is reached.
	 */
	protected boolean isStoryBoundary(Object o) {
		return o instanceof CTTxbxContent
				|| o instanceof org.docx4j.vml.CTTextbox
				|| o instanceof org.docx4j.dml.wordprocessingDrawing.Anchor
				|| o instanceof org.docx4j.dml.wordprocessingDrawing.Inline;
	}
}
