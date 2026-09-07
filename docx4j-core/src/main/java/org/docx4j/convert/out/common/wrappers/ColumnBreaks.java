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
package org.docx4j.convert.out.common.wrappers;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code w:br w:type="column"} in a section whose columns are <b>equal</b>, which the
 * region body lays out (columns of different widths are a one-row table instead, and
 * {@link UnequalColumns} divides those at the same breaks).
 *
 * <p>Word divides the paragraph the break is <em>in</em>: what precedes the break ends
 * the column and what follows it opens the next (&#xa7;7.3).  Until 17.1.0 the break was
 * emitted as an ordinary line break, so the column was never taken.  The paragraph is
 * therefore split in two here, the same shape of change as the hard page break's
 * ({@code convert/out/common/preprocess/PageBreak}); the break itself opens the second
 * half, where the FO exporter turns it into {@code break-before="column"} on that half's
 * block.</p>
 *
 * <p>The two halves are one paragraph, so nothing is doubled between them.  The
 * paragraph's <b>space-after</b> goes with the half that ends it - the second - and its
 * space-before stays on both, which is where Word puts it (both measured on
 * {@code columns-unequal}; taking the space-before off the second half cost four corpus
 * documents 2 to 6 points of line parity each).  The second half takes neither the
 * numbering label - Word numbers the paragraph once - nor the first-line indent, and the
 * {@code w:sectPr}, which belongs to the paragraph's end, goes with it.</p>
 *
 * @since 17.1.0
 */
class ColumnBreaks {

	private static final Logger log = LoggerFactory.getLogger(ColumnBreaks.class);

	private ColumnBreaks() {}

	/**
	 * Split each paragraph of this section (or merged part) which holds a column break
	 * with content before it.  The list is the conversion's own, but the paragraphs in
	 * it are the document's - which is converted more than once - so a paragraph which
	 * is split is replaced by copies rather than modified.
	 *
	 * @return whether anything was split
	 */
	static boolean split(List<Object> content) {

		if (content == null) return false;
		boolean changed = false;
		for (int i = 0; i < content.size(); i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (!(o instanceof P)) continue;
			P p = (P) o;
			int[] at = firstColumnBreak(p);
			if (at == null) continue;
			if (!contentPrecedes(p, at)) continue; // the break already opens the paragraph

			P before = copyOf(p);
			P after = copyOf(p);
			UnequalColumns.truncateAtColumnBreak(before, true, false);
			UnequalColumns.truncateAtColumnBreak(after, false, true); // the break opens it
			UnequalColumns.dropSpacing(before, false); // the space-after ends the paragraph
			if (before.getPPr() != null) before.getPPr().setSectPr(null);
			continuationProperties(after.getPPr());

			content.set(i, before);
			content.add(i + 1, after);
			changed = true;
			// the continuation is now at i+1 and is visited by the loop in its turn, so a
			// paragraph with several column breaks is split at each of them
		}
		if (changed && log.isDebugEnabled()) {
			log.debug("paragraph(s) split at a w:br w:type=\"column\"");
		}
		return changed;
	}

	/** A copy which can be cut about, keeping the original's place in the document so
	 *  that anything which walks its ancestors still can. */
	private static P copyOf(P p) {
		P copy = XmlUtils.deepCopy(p);
		copy.setParent(p.getParent());
		return copy;
	}

	/** One paragraph gets one number and one first-line indent, and both were used by
	 *  the half which began it. */
	private static void continuationProperties(PPr pPr) {
		if (pPr == null) return;
		pPr.setNumPr(null);
		PPrBase.Ind ind = pPr.getInd();
		if (ind != null) {
			ind.setFirstLine(null);
			ind.setFirstLineChars(null);
			if (ind.getHanging() != null) {
				ind.setHanging(null);
				ind.setHangingChars(null);
			}
		}
	}

	/** Where the paragraph's first column break is: {@code {i,j}} in the i-th item's run
	 *  content, or null. */
	private static int[] firstColumnBreak(P p) {
		List<Object> content = p.getContent();
		if (content == null) return null;
		for (int i = 0; i < content.size(); i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (!(o instanceof R)) continue;
			List<Object> rc = ((R) o).getContent();
			for (int j = 0; rc != null && j < rc.size(); j++) {
				if (isColumnBreak(XmlUtils.unwrap(rc.get(j)))) return new int[] { i, j };
			}
		}
		return null;
	}

	private static boolean isColumnBreak(Object o) {
		return (o instanceof org.docx4j.wml.Br) && STBrType.COLUMN.equals(((org.docx4j.wml.Br) o).getType());
	}

	/** Whether anything which draws precedes the break. */
	private static boolean contentPrecedes(P p, int[] at) {
		List<Object> content = p.getContent();
		for (int i = 0; i < at[0]; i++) {
			if (draws(XmlUtils.unwrap(content.get(i)))) return true;
		}
		List<Object> rc = ((R) XmlUtils.unwrap(content.get(at[0]))).getContent();
		for (int j = 0; j < at[1]; j++) {
			if (drawsInRun(XmlUtils.unwrap(rc.get(j)))) return true;
		}
		return false;
	}

	private static boolean draws(Object o) {
		if (o instanceof PPr || o instanceof org.docx4j.wml.ProofErr
				|| o instanceof org.docx4j.wml.CommentRangeStart
				|| o instanceof org.docx4j.wml.CommentRangeEnd
				|| o instanceof org.docx4j.wml.CTMarkupRange) return false;
		if (o instanceof R) {
			List<Object> rc = ((R) o).getContent();
			for (int j = 0; rc != null && j < rc.size(); j++) {
				if (drawsInRun(XmlUtils.unwrap(rc.get(j)))) return true;
			}
			return false;
		}
		return true; // hyperlink, sdt, smartTag, ins, ...
	}

	private static boolean drawsInRun(Object o) {
		if (o instanceof org.docx4j.wml.RPr) return false;
		if (o instanceof org.docx4j.wml.Br) return false; // a break draws nothing of its own
		if (o instanceof org.docx4j.wml.Text) {
			String v = ((org.docx4j.wml.Text) o).getValue();
			return v != null && v.length() > 0;
		}
		if (o instanceof org.docx4j.wml.R.LastRenderedPageBreak) return false;
		return true;
	}

}
