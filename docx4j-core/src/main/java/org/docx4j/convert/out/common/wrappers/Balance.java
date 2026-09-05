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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Where a stretch of <b>unequal columns</b> ({@link UnequalColumns}) does not say where its
 * columns divide - it carries no {@code w:br w:type="column"} - Word balances them itself,
 * and divides whatever paragraph the balance point falls in.
 *
 * <p>Measured on the columns-unequal probe, whose third section is one paragraph in columns
 * of 157 and 318pt: Word gives each column eight lines, breaking inside the paragraph ("...
 * quis nostrud" ends column 1, "exercitation ..." opens column 2).  A one-row table cannot
 * flow its content from one cell into the next, so until 17.0.6 such a section fell back to
 * FOP's equal columns, whose widths are nothing like Word's (measured, 237.5pt each where
 * Word's are 157 and 318).</p>
 *
 * <p>The division is estimated rather than laid out: each word's advance is taken as half
 * its font size per character, the words are filled greedily into each column's measure,
 * and the split is the word at which the two line counts come closest.  What the estimate
 * has to get right is the <em>ratio</em> of the columns' capacities, which is the ratio of
 * their measures whatever the per-character estimate is.  A paragraph is divided only where
 * every one of its runs is plain text; otherwise the nearest paragraph boundary is taken. A
 * stretch which would not fit the page at that estimate is left alone, since a one-row
 * table cannot break into columns across a page.</p>
 *
 * @since 17.0.6
 */
final class Balance {

	private static final Logger log = LoggerFactory.getLogger(Balance.class);

	/** A character's advance and a space's, as a share of the font size. */
	private static final double PER_CHARACTER = 0.5, SPACE = 0.25;

	/** Line height as a share of the font size, for the does-it-fit check. */
	private static final double LINE_HEIGHT = 1.2;

	private static final double DEFAULT_SIZE_PT = 11;

	/** Beyond this many words the O(n<sup>2</sup>) search is not worth making. */
	private static final int MAX_WORDS = 20000;

	private Balance() {}

	/** One word: which paragraph it is in, its advance, and the space before it. */
	private static final class Word {
		final int para;
		final double width;
		final double space;
		Word(int para, double width, double space) {
			this.para = para;
			this.width = width;
			this.space = space;
		}
	}

	/**
	 * @param content the stretch of content the section's columns hold
	 * @param widths the column widths in twips
	 * @return the content divided into one group per column, or null to leave the section
	 *         to the region body
	 */
	static List<List<Object>> split(List<Object> content, int[] widths, SectPr sectPr) {

		if (widths.length != 2) return null; // one boundary to find

		List<P> paras = new ArrayList<P>();
		for (Object o : content) {
			Object unwrapped = XmlUtils.unwrap(o);
			if (!(unwrapped instanceof P)) return null; // a table cannot be divided
			P p = (P) unwrapped;
			if (hasBreak(p)) return null; // the document does say where the flow divides
			paras.add(p);
		}
		if (paras.isEmpty()) return null;

		List<Word> words = new ArrayList<Word>();
		int[] wordsPerPara = new int[paras.size()];
		for (int i = 0; i < paras.size(); i++) {
			int before = words.size();
			words(paras.get(i), i, words);
			wordsPerPara[i] = words.size() - before;
		}
		if (words.size() < 2 || words.size() > MAX_WORDS) return null;

		// the first word at which column one is no shorter than column two: as the split
		// moves along, column one grows and column two shrinks, and Word gives the odd
		// line to the first column (measured on the columns-unequal probe, whose sixteen
		// lines it divides eight and eight)
		double w1 = widths[0] / 20.0, w2 = widths[1] / 20.0;
		int best = -1, bestLines = 0;
		for (int k = 1; k < words.size(); k++) {
			int l1 = lines(words, 0, k, w1);
			int l2 = lines(words, k, words.size(), w2);
			best = k;
			bestLines = Math.max(l1, l2);
			if (l1 >= l2) break;
		}
		if (best < 1) return null;

		// Word balances to shorten the columns; where the content is short enough to sit
		// in the first column at the same height, it does not divide it at all (measured
		// on a two-column section holding one line: Word leaves column two empty), and
		// neither do we - the region body's own columns do that as well as a table can.
		if (bestLines >= lines(words, 0, words.size(), w1)) return null;

		if (!fits(bestLines, paras, sectPr)) {
			log.debug("unequal columns left to the region body: " + bestLines
					+ " estimated lines do not fit the page");
			return null;
		}

		// which paragraph the split falls in, and how many of its words end column one
		int para = 0, offset = best;
		while (para < wordsPerPara.length && offset > wordsPerPara[para]) {
			offset -= wordsPerPara[para];
			para++;
		}
		if (para >= paras.size()) return null;

		List<Object> first = new ArrayList<Object>(content.subList(0, para));
		List<Object> second = new ArrayList<Object>();
		P divided = paras.get(para);
		if (offset >= wordsPerPara[para]) {              // the boundary is where it ends
			first.add(content.get(para));
		} else if (offset == 0 || !divisible(divided)) { // keep the paragraph whole
			second.add(content.get(para));
		} else {
			P before = XmlUtils.deepCopy(divided);
			P after = XmlUtils.deepCopy(divided);
			divide(before, offset, true);
			divide(after, offset, false);
			UnequalColumns.dropSpacing(before, false); // its space-after ends the paragraph
			first.add(before);
			second.add(after);
		}
		second.addAll(content.subList(para + 1, content.size()));
		if (first.isEmpty() || second.isEmpty()) return null;

		if (log.isDebugEnabled()) {
			log.debug("unequal columns balanced at word " + best + " of " + words.size()
					+ " (paragraph " + para + " word " + offset + "), " + bestLines + " lines");
		}
		List<List<Object>> groups = new ArrayList<List<Object>>(2);
		groups.add(first);
		groups.add(second);
		return groups;
	}

	/** Whether the paragraph carries a break of its own, which divides the flow in a way a
	 *  one-row table cannot follow. */
	private static boolean hasBreak(P p) {
		for (Object child : p.getContent()) {
			Object o = XmlUtils.unwrap(child);
			if (!(o instanceof R)) continue;
			for (Object rChild : ((R) o).getContent()) {
				Object c = XmlUtils.unwrap(rChild);
				if (c instanceof org.docx4j.wml.Br) {
					STBrType type = ((org.docx4j.wml.Br) c).getType();
					if (STBrType.COLUMN.equals(type) || STBrType.PAGE.equals(type)) return true;
				}
			}
		}
		return p.getPPr() != null && p.getPPr().getPageBreakBefore() != null
				&& p.getPPr().getPageBreakBefore().isVal();
	}

	/** Every run is plain text, so the paragraph can be divided at a word. */
	private static boolean divisible(P p) {
		for (Object child : p.getContent()) {
			Object o = XmlUtils.unwrap(child);
			if (o instanceof PPr) continue;
			if (!(o instanceof R)) return false;
			for (Object rChild : ((R) o).getContent()) {
				Object c = XmlUtils.unwrap(rChild);
				if (!(c instanceof Text) && !(c instanceof RPr)) return false;
			}
		}
		return true;
	}

	/** The paragraph's words, at their estimated advance. */
	private static void words(P p, int index, List<Word> words) {
		for (Object child : p.getContent()) {
			Object o = XmlUtils.unwrap(child);
			if (!(o instanceof R)) continue;
			R r = (R) o;
			double size = sizePt(r, p);
			for (Object rChild : r.getContent()) {
				Object c = XmlUtils.unwrap(rChild);
				if (!(c instanceof Text)) continue;
				String value = ((Text) c).getValue();
				if (value == null) continue;
				for (String word : value.split("\\s+")) {
					if (word.length() == 0) continue;
					words.add(new Word(index, word.length() * PER_CHARACTER * size, SPACE * size));
				}
			}
		}
	}

	private static double sizePt(R r, P p) {
		BigInteger sz = null;
		if (r.getRPr() != null && r.getRPr().getSz() != null) sz = r.getRPr().getSz().getVal();
		if (sz == null && p.getPPr() != null && p.getPPr().getRPr() != null
				&& p.getPPr().getRPr().getSz() != null) {
			sz = p.getPPr().getRPr().getSz().getVal();
		}
		return sz == null ? DEFAULT_SIZE_PT : sz.doubleValue() / 2;
	}

	/** Greedily fills the words into a column of this measure, a new line per paragraph. */
	private static int lines(List<Word> words, int from, int to, double measure) {
		int count = 0, para = Integer.MIN_VALUE;
		double used = 0;
		for (int i = from; i < to; i++) {
			Word word = words.get(i);
			if (word.para != para) {
				para = word.para;
				count++;
				used = word.width;
			} else if (used + word.space + word.width > measure) {
				count++;
				used = word.width;
			} else {
				used += word.space + word.width;
			}
		}
		return count;
	}

	/** Whether the taller column fits the page at this estimate. */
	private static boolean fits(int lines, List<P> paras, SectPr sectPr) {
		if (sectPr == null || sectPr.getPgSz() == null || sectPr.getPgSz().getH() == null) {
			return false;
		}
		double height = sectPr.getPgSz().getH().doubleValue();
		if (sectPr.getPgMar() != null) {
			if (sectPr.getPgMar().getTop() != null) height -= sectPr.getPgMar().getTop().doubleValue();
			if (sectPr.getPgMar().getBottom() != null) {
				height -= sectPr.getPgMar().getBottom().doubleValue();
			}
		}
		double size = DEFAULT_SIZE_PT;
		for (P p : paras) {
			for (Object child : p.getContent()) {
				Object o = XmlUtils.unwrap(child);
				if (o instanceof R) size = Math.max(size, sizePt((R) o, p));
			}
		}
		return lines * size * LINE_HEIGHT * 20 <= height;
	}

	/** Where in the paragraph the given word begins: the index of the content item, of the
	 *  run's child, and the character offset in its text. */
	private static int[] locate(P p, int words) {
		int seen = 0;
		List<Object> content = p.getContent();
		for (int i = 0; i < content.size(); i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (!(o instanceof R)) continue;
			List<Object> runContent = ((R) o).getContent();
			for (int j = 0; j < runContent.size(); j++) {
				Object c = XmlUtils.unwrap(runContent.get(j));
				if (!(c instanceof Text)) continue;
				String value = ((Text) c).getValue();
				if (value == null) value = "";
				int here = count(value);
				if (seen + here < words) {
					seen += here;
					continue;
				}
				return new int[] { i, j, offsetOfWord(value, words - seen) };
			}
		}
		return null;
	}

	/** Keeps the first {@code words} words of the paragraph, or everything after them. */
	private static void divide(P p, int words, boolean keepBefore) {
		int[] at = locate(p, words);
		if (at == null) return;
		List<Object> content = p.getContent();
		R run = (R) XmlUtils.unwrap(content.get(at[0]));
		List<Object> runContent = run.getContent();
		Text text = (Text) XmlUtils.unwrap(runContent.get(at[1]));
		String value = text.getValue() == null ? "" : text.getValue();

		if (keepBefore) {
			text.setValue(value.substring(0, Math.min(at[2], value.length())));
			while (runContent.size() > at[1] + 1) runContent.remove(runContent.size() - 1);
			while (content.size() > at[0] + 1) content.remove(content.size() - 1);
		} else {
			text.setValue(value.substring(Math.min(at[2], value.length())));
			text.setSpace("preserve");
			for (int j = at[1] - 1; j >= 0; j--) {
				if (!(XmlUtils.unwrap(runContent.get(j)) instanceof RPr)) runContent.remove(j);
			}
			for (int i = at[0] - 1; i >= 0; i--) {
				if (!(XmlUtils.unwrap(content.get(i)) instanceof PPr)) content.remove(i);
			}
		}
	}

	private static int count(String value) {
		int n = 0;
		for (String word : value.split("\\s+")) if (word.length() > 0) n++;
		return n;
	}

	/** The character offset at which the given number of words has been passed. */
	private static int offsetOfWord(String value, int words) {
		int seen = 0, i = 0;
		while (i < value.length()) {
			while (i < value.length() && Character.isWhitespace(value.charAt(i))) i++;
			if (seen == words) return i;
			while (i < value.length() && !Character.isWhitespace(value.charAt(i))) i++;
			seen++;
		}
		return value.length();
	}

}
