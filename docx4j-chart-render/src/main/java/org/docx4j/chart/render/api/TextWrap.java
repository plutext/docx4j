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
package org.docx4j.chart.render.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.model.color.ThemeColors;

/**
 * Text wrapped as Office wraps a chart's: rich text (a title's runs) to lines of pieces in their
 * styles, and plain text (a label's, a legend entry's) to lines at spaces and after hyphens.
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class TextWrap {

	private TextWrap() {
	}

	/** A piece of rich text: its text in a style */
	public static final class Seg {
		private String text;
		private final TextStyle style;

		public Seg(String text, TextStyle style) {
			this.text = text;
			this.style = style;
		}

		public String getText() {
			return text;
		}

		public TextStyle getStyle() {
			return style;
		}
	}

	/** A line of rich text: its pieces, its width, its height and ascent (its tallest piece's) */
	public static final class Line {
		private final List<Seg> segs = new ArrayList<>();
		private double width, line, ascent;

		/** Its pieces, in order */
		public List<Seg> getSegs() {
			return Collections.unmodifiableList(segs);
		}

		/** Its pieces' widths */
		public double getWidth() {
			return width;
		}

		/** Its height: its tallest piece's line */
		public double getLineHeight() {
			return line;
		}

		/** Its tallest piece's ascent */
		public double getAscent() {
			return ascent;
		}
	}

	/**
	 * A text body's paragraphs, each its runs in their styles: the paragraph's defRPr over the
	 * parent, a run's rPr over that; a line break starts a new paragraph
	 */
	public static List<List<Seg>> paragraphs(CTTextBody rich, TextStyle parent, ThemeColors theme) {
		List<List<Seg>> out = new ArrayList<>();
		for (CTTextParagraph p : rich.getP()) {
			TextStyle ps = parent.with(p.getPPr() == null ? null : p.getPPr().getDefRPr(), theme);
			List<Seg> para = new ArrayList<>();
			for (Object o : p.getEGTextRun()) {
				Object r = org.docx4j.XmlUtils.unwrap(o);
				if (r instanceof org.docx4j.dml.CTRegularTextRun) {
					org.docx4j.dml.CTRegularTextRun run = (org.docx4j.dml.CTRegularTextRun) r;
					if (run.getT() != null) para.add(new Seg(run.getT(), ps.with(run.getRPr(), theme)));
				} else if (r instanceof org.docx4j.dml.CTTextField) {
					org.docx4j.dml.CTTextField fld = (org.docx4j.dml.CTTextField) r;
					if (fld.getT() != null) para.add(new Seg(fld.getT(), ps.with(fld.getRPr(), theme)));
				} else if (r instanceof org.docx4j.dml.CTTextLineBreak) {
					out.add(para);
					para = new ArrayList<>();
				}
			}
			if (para.isEmpty()) para.add(new Seg("", ps));
			out.add(para);
		}
		return out;
	}

	/**
	 * Paragraphs of runs wrapped to lines no wider than max, at spaces (a word wider than max on a
	 * line of its own; a run's word that goes on into the next run kept whole); each line's pieces
	 * of one style joined; text of spaces alone a line with nothing on it
	 */
	public static List<Line> wrap(List<List<Seg>> paragraphs, double max) {
		List<Line> out = new ArrayList<>();
		for (List<Seg> para : paragraphs) {
			// the paragraph's words, each its pieces
			List<List<Seg>> words = new ArrayList<>();
			List<Seg> cur = new ArrayList<>();
			TextStyle last = para.isEmpty() ? null : para.get(0).style;
			for (Seg sg : para) {
				last = sg.style;
				String t = sg.text;
				int start = 0;
				for (int k = 0; k <= t.length(); k++) {
					if (k < t.length() && t.charAt(k) != ' ') continue;
					if (k > start) cur.add(new Seg(t.substring(start, k), sg.style));
					if (k < t.length()) {
						words.add(cur);
						cur = new ArrayList<>();
					}
					start = k + 1;
				}
			}
			words.add(cur);
			Line line = null;
			for (List<Seg> word : words) {
				// spaces at either end, or more than one, are dropped (p6-pie-numbers' title of a space: no width)
				if (word.isEmpty()) continue;
				double ww = 0;
				for (Seg sg : word) ww += sg.style.width(sg.text);
				TextStyle ws = word.isEmpty() ? last : word.get(0).style;
				if (line != null && line.width + ws.width(" ") + ww > max && !line.segs.isEmpty()) {
					out.add(line);
					line = null;
				}
				if (line == null) {
					line = new Line();
					line.line = ws.getLineHeight();
					line.ascent = ws.getAscent();
				} else {
					// the space before the word, on the line's last piece
					Seg prev = line.segs.isEmpty() ? null : line.segs.get(line.segs.size() - 1);
					if (prev != null) prev.text += " ";
					else line.segs.add(new Seg(" ", ws));
					line.width += ws.width(" ");
				}
				for (Seg sg : word) {
					Seg prev = line.segs.isEmpty() ? null : line.segs.get(line.segs.size() - 1);
					if (prev != null && prev.style == sg.style) prev.text += sg.text;
					else line.segs.add(new Seg(sg.text, sg.style));
					line.line = Math.max(line.line, sg.style.getLineHeight());
					line.ascent = Math.max(line.ascent, sg.style.getAscent());
				}
				line.width = 0;
				for (Seg sg : line.segs) line.width += sg.style.width(sg.text);
			}
			if (line == null) {
				line = new Line();
				line.line = last == null ? 0 : last.getLineHeight();
				line.ascent = last == null ? 0 : last.getAscent();
			}
			out.add(line);
		}
		return out;
	}

	/** The pieces a line can break between: at spaces, and after a hyphen ("22-Jan", r14-sweep-dates) */
	public static List<String> words(String text) {
		List<String> out = new ArrayList<>();
		for (String word : text.split(" ")) {
			int from = 0;
			for (int i = word.indexOf('-'); i >= 0 && i < word.length() - 1; i = word.indexOf('-', i + 1)) {
				if (i == 0) continue;
				out.add(word.substring(from, i + 1));
				from = i + 1;
			}
			out.add(word.substring(from));
		}
		return out;
	}

	/**
	 * Words wrapped to lines no wider than max (a word wider than max on a line of its own); a
	 * hyphen's word breaks after it; text of spaces alone is a line with nothing on it (p6-pie-numbers'
	 * title, " ")
	 */
	public static List<String> wrap(String text, TextStyle s, double max) {
		List<String> out = new ArrayList<>();
		String line = null;
		for (String word : text.split(" ")) {
			List<String> parts = words(word);
			for (int p = 0; p < parts.size(); p++) {
				String part = parts.get(p);
				String next = line == null ? part : line + (p == 0 ? " " : "") + part;
				if (line != null && s.width(next) > max) {
					out.add(line);
					line = part;
				} else {
					line = next;
				}
			}
		}
		if (line != null) out.add(line);
		if (out.isEmpty()) out.add("");
		return out;
	}

	/** Words wrapped to lines no wider than max, a word wider than max broken where it reaches it */
	public static List<String> wrapBroken(String text, TextStyle s, double max) {
		List<String> out = new ArrayList<>();
		String line = null;
		for (String word : text.split(" ")) {
			String rest = word;
			while (!rest.isEmpty()) {
				String next = line == null ? rest : line + " " + rest;
				if (s.width(next) <= max) {
					line = next;
					rest = "";
				} else if (line != null) {
					out.add(line);
					line = null;
				} else {
					int k = rest.length();
					while (k > 1 && s.width(rest.substring(0, k)) > max) k--;
					out.add(rest.substring(0, k));
					rest = rest.substring(k);
				}
			}
		}
		if (line != null) out.add(line);
		if (out.isEmpty()) out.add("");
		return out;
	}
}
