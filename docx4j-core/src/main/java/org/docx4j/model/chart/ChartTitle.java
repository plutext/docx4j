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
package org.docx4j.model.chart;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextField;
import org.docx4j.dml.CTTextLineBreak;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.chart.CTTitle;

/**
 * A chart's or an axis' title ({@code c:title}): its text as the file has it, rich text or a
 * cell's ({@link #getText()}, {@link #getReference()}), and the text Office shows
 * ({@link #getShownText()}), which differs where the title states none: Office then shows a
 * default ("Chart Title", "Axis Title", or a single series' name).
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartTitle {

	private final String text;
	private final ChartData reference;
	private final boolean overlay;
	private final String shownText;
	private final CTTitle source;

	private ChartTitle(String text, ChartData reference, boolean overlay, String shownText, CTTitle source) {
		this.text = text;
		this.reference = reference;
		this.overlay = overlay;
		this.shownText = shownText;
		this.source = source;
	}

	/**
	 * @param fallback the text Office shows where the title states none
	 */
	static ChartTitle of(CTTitle t, String fallback) {
		if (t == null) return null;
		String text = null;
		ChartData ref = null;
		if (t.getTx() != null) {
			if (t.getTx().getRich() != null) {
				text = text(t.getTx().getRich());
			} else if (t.getTx().getStrRef() != null) {
				ref = ChartData.of(t.getTx().getStrRef());
				text = ref.getText(0);
			}
		}
		boolean overlay = t.getOverlay() != null && t.getOverlay().isVal();
		return new ChartTitle(text, ref, overlay, text != null ? text : fallback, t);
	}

	/** Rich text's paragraphs, joined by line feeds; a run's line breaks are line feeds too */
	static String text(CTTextBody body) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (CTTextParagraph p : body.getP()) {
			if (!first) sb.append('\n');
			first = false;
			for (Object o : p.getEGTextRun()) {
				Object r = XmlUtils.unwrap(o);
				if (r instanceof CTRegularTextRun) {
					if (((CTRegularTextRun) r).getT() != null) sb.append(((CTRegularTextRun) r).getT());
				} else if (r instanceof CTTextField) {
					if (((CTTextField) r).getT() != null) sb.append(((CTTextField) r).getT());
				} else if (r instanceof CTTextLineBreak) {
					sb.append('\n');
				}
			}
		}
		return sb.toString();
	}

	/** The title's text as the file states it ({@code c:tx}); null where it states none */
	public String getText() {
		return text;
	}

	/** {@code c:tx/c:strRef}: the cell the title comes from; null for rich text or none */
	public ChartData getReference() {
		return reference;
	}

	/** {@code c:overlay}: whether the title is laid over the plot area rather than beside it */
	public boolean isOverlay() {
		return overlay;
	}

	/** The text Office shows: the title's own, or its default where it states none */
	public String getShownText() {
		return shownText;
	}

	public CTTitle getSource() {
		return source;
	}
}
