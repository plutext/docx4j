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
package org.docx4j.model.pagination;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.DelText;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Text;

/**
 * The text model pagination offsets are counted in (CR-012 phase 2): for each run item,
 * how many characters it stands for.  The FO exporter counts runs off with it on the
 * document it lays out, and the writer resolves offsets with it on the document the
 * markers go into, so the two must agree; it is chosen to match what FOP's line areas
 * hold, so that the characters FOP reports before a page boundary are an offset in it.
 *
 * <ul>
 * <li>{@code w:t}: its characters (a hidden run's too: the exporter skips the run, but
 *     the offsets of the runs after it are counted the same on both sides).</li>
 * <li>{@code w:sym}, {@code w:noBreakHyphen}: one character, as FOP renders them.</li>
 * <li>{@code w:tab}, and a {@code w:br} or {@code w:cr} that wraps text: one character,
 *     as Word counts them.  FOP renders a tab as an empty space area, which the area
 *     tree reader counts as one, and ends a line at a break, where it drops a space's
 *     worth as it does at every line end.</li>
 * <li>Nothing for a page or column {@code w:br} (the preprocessing takes it out of the
 *     paragraph), {@code w:ptab} (a leader), {@code w:softHyphen} (rendered only where
 *     FOP breaks there, which the reader and resolver correct for), field characters
 *     and field codes ({@code w:instrText}), deleted text, pictures, drawings, objects,
 *     note and comment references, and {@code w:lastRenderedPageBreak} itself.</li>
 * </ul>
 *
 * @since 17.1.1
 */
public final class RunText {

	private RunText() {}

	/** The characters this run item stands for. */
	public static int length(Object item) {

		String name = null;
		if (item instanceof JAXBElement) {
			name = ((JAXBElement<?>) item).getName().getLocalPart();
			item = ((JAXBElement<?>) item).getValue();
		}
		if (item instanceof DelText) return 0;
		if (item instanceof Text) {
			if ("instrText".equals(name) || "delInstrText".equals(name)) return 0;
			String value = ((Text) item).getValue();
			return value == null ? 0 : value.length();
		}
		if (item instanceof R.Sym || item instanceof R.NoBreakHyphen) return 1;
		if (item instanceof R.Tab || item instanceof R.Cr) return 1;
		if (item instanceof Br) {
			STBrType type = ((Br) item).getType();
			return (type == null || type == STBrType.TEXT_WRAPPING) ? 1 : 0;
		}
		return 0;
	}

	/** The characters this run stands for. */
	public static int length(R r) {
		int n = 0;
		for (Object item : r.getContent()) n += length(item);
		return n;
	}

	/** The characters these run items stand for. */
	public static int length(List<Object> items) {
		int n = 0;
		for (Object item : items) n += length(item);
		return n;
	}

	/** The run's text in this model: the {@code w:t} characters, {@code -} for a
	 *  non-breaking hyphen, a space for a tab or a wrapping break, U+FFFD for a symbol. */
	public static void appendText(R r, StringBuilder sb) {
		for (Object item : r.getContent()) {
			int n = length(item);
			if (n == 0) continue;
			Object v = XmlUtils.unwrap(item);
			if (v instanceof Text) sb.append(((Text) v).getValue());
			else if (v instanceof R.NoBreakHyphen) sb.append('-');
			else if (v instanceof R.Tab || v instanceof R.Cr || v instanceof Br) sb.append(' ');
			else sb.append('\uFFFD');
		}
	}

	/** Whether this run item takes no characters but ends a line's text: a tab, a
	 *  break, a soft hyphen; the marker goes after it, as after a space. */
	public static boolean isSkippable(Object item) {
		Object v = XmlUtils.unwrap(item);
		return v instanceof R.Tab || v instanceof R.Ptab || v instanceof Br
				|| v instanceof R.Cr || v instanceof R.SoftHyphen
				|| v instanceof R.LastRenderedPageBreak;
	}

	/**
	 * The runs of this content in document order, the way the FO exporter meets them:
	 * descending into hyperlinks, fields, content controls, smart tags, insertions and
	 * moved-to text, not into deleted or moved-from text (which the accepted view has
	 * not got) and not into runs (a text box's paragraphs are laid out where the box is).
	 */
	public static List<R> runs(List<Object> content) {
		List<R> runs = new ArrayList<R>();
		collectRuns(content, runs);
		return runs;
	}

	/** The runs of the paragraph, as {@link #runs(List)}. */
	public static List<R> runs(P p) {
		return runs(p.getContent());
	}

	private static void collectRuns(List<Object> content, List<R> out) {

		if (content == null) return;
		for (Object o : content) {
			String name = (o instanceof JAXBElement ? ((JAXBElement<?>) o).getName().getLocalPart() : null);
			Object v = XmlUtils.unwrap(o);
			if (v instanceof R) {
				out.add((R) v);
			} else if (v instanceof RunDel) {
				// deleted: not rendered
			} else if (v instanceof RunIns) {
				collectRuns(((RunIns) v).getCustomXmlOrSmartTagOrSdt(), out);
			} else if (v instanceof RunTrackChange) {
				if (!"moveFrom".equals(name)) collectRuns(((RunTrackChange) v).getAccOrBarOrBox(), out);
			} else if (v instanceof SdtElement) {
				if (((SdtElement) v).getSdtContent() != null) {
					collectRuns(((SdtElement) v).getSdtContent().getContent(), out);
				}
			} else if (v instanceof ContentAccessor) {
				collectRuns(((ContentAccessor) v).getContent(), out);
			}
		}
	}
}
