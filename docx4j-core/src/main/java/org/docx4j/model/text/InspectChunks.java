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
package org.docx4j.model.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An HTML, XHTML or MHT {@code altChunk} as the inspector reads it (CR-053 phase 3, WD REQ-902): through
 * the import the extraction runs (REQ-901, 0.26), REQ-907's limits checked before it, so that what
 * {@code display: none} hides is located at its element's path in the tree jsoup built, and addressed
 * where the extraction under {@code hidden: true} puts it.
 */
final class InspectChunks {

	private InspectChunks() {}

	/** What {@code display: none} hides under one outermost element with text. */
	static final class Hidden {
		/** The element's path in the parsed tree, before the rewrite (REQ-901). */
		final String path;
		/** Its text, as the import made it: a run's {@code w:t}, a block a line feed from the next. */
		final String text;
		/** The paragraph or table that holds its text first: the finding's block. */
		final Object block;

		Hidden(String path, String text, Object block) {
			this.path = path;
			this.text = text;
			this.block = block;
		}
	}

	/** A chunk read, or the reason it is skipped (REQ-907). */
	static final class Read {
		/** Whether jsoup parsed it: the {@code altchunk} finding's {@code parsed} is then {@code html}. */
		boolean parsed;
		/** The reason it is skipped, or null where it was read. */
		String skipped;
		/** The scratch package the import made: the extraction's reading of the chunk in this inspection. */
		WordprocessingMLPackage scratch;
		/** What it hides, in document order. */
		List<Hidden> hidden = Collections.emptyList();
	}

	/**
	 * The chunk read as the extraction reads it: decoded and parsed; its elements, as parsed, counted
	 * against the inspection's budget and against {@link HtmlChunkImport#MAX_ELEMENTS} (WD 0.27's REQ-513
	 * and REQ-907); rewritten; then imported, never fetching. Without an HTML reader, or with an importer that cannot say what it
	 * hid (before 17.3.4), it is {@code not_decoded}; a chunk jsoup or the import cannot read is
	 * {@code unreadable}; one over the import's limit {@code limit.elements}. A tree past the
	 * inspection's own budget stops the inspection ({@link InspectRun.Stop}).
	 */
	static Read read(byte[] data, boolean mht, OpcPackage host, String part, InspectRun run) {
		Read read = new Read();
		if (!HtmlChunkImport.readerPresent()) {
			read.skipped = Inspection.NOT_DECODED;
			return read;
		}
		String where = "the altChunk " + part;
		Map<Element, String> paths = new IdentityHashMap<Element, String>();
		Document dom = HtmlChunkImport.parsed(data, mht, where, paths);
		if (dom == null) {
			read.skipped = Inspection.UNREADABLE;
			return read;
		}
		read.parsed = true;
		run.elements(HtmlChunks.count(dom));
		if (HtmlChunkImport.over(dom, where)) {
			read.skipped = Inspection.LIMIT_ELEMENTS;
			return read;
		}
		if (!HtmlChunkImport.rewritten(dom, host, where)) {
			read.skipped = Inspection.UNREADABLE;
			return read;
		}
		HtmlChunkImport.Inspected imported = HtmlChunkImport.inspected(dom, where);
		if (imported == null) {
			read.skipped = Inspection.UNREADABLE;
			return read;
		}
		if (imported.hidden == null) {
			read.skipped = Inspection.NOT_DECODED;
			return read;
		}
		read.scratch = imported.scratch;
		read.hidden = hidden(imported, paths);
		return read;
	}

	/**
	 * Each outermost hidden element whose content has text that is not all whitespace, at its path; the
	 * importer reports the outermost alone (its dedupe by marked ancestor), and an element inside one only
	 * where an element between them shows its content again.
	 */
	private static List<Hidden> hidden(HtmlChunkImport.Inspected imported, Map<Element, String> paths) {
		Map<Object, P> paragraphs = new IdentityHashMap<Object, P>();
		for (Object o : imported.scratch.getMainDocumentPart().getContent()) paragraphs(o, null, paragraphs);
		List<Hidden> out = new ArrayList<Hidden>();
		for (Map.Entry<Element, List<Object>> e : imported.hidden.entrySet()) {
			StringBuilder text = new StringBuilder();
			Object block = null;
			Object last = null;
			for (Object o : e.getValue()) {
				Object u = XmlUtils.unwrap(o);
				Object holder = u instanceof R ? paragraphs.get(u) : u;
				String t = text(u);
				if (text.length() > 0 && (holder == null || holder != last)) text.append('\n');
				text.append(t);
				if (block == null && !t.strip().isEmpty()) block = holder;
				last = holder;
			}
			if (text.toString().strip().isEmpty()) continue;
			String path = paths.get(e.getKey());
			out.add(new Hidden(path == null ? "" : path, text.toString(), block));
		}
		return out;
	}

	/** Each run to the paragraph that holds it, through hyperlinks, fields and tables. */
	private static void paragraphs(Object o, P in, Map<Object, P> out) {
		Object u = XmlUtils.unwrap(o);
		if (u instanceof P) in = (P) u;
		else if (u instanceof R) {
			if (in != null) out.put(u, in);
			return;
		}
		if (u instanceof ContentAccessor) for (Object c : ((ContentAccessor) u).getContent()) paragraphs(c, in, out);
	}

	/** A run's {@code w:t}; a paragraph's runs'; a table's paragraphs', a line feed apart. */
	private static String text(Object o) {
		List<String> paras = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		text(o, sb, paras);
		if (paras.isEmpty()) return sb.toString();
		return String.join("\n", paras);
	}

	private static void text(Object o, StringBuilder sb, List<String> paras) {
		Object u = XmlUtils.unwrap(o);
		if (u instanceof Text) {
			sb.append(((Text) u).getValue() == null ? "" : ((Text) u).getValue());
		} else if (u instanceof P) {
			StringBuilder p = new StringBuilder();
			for (Object c : ((P) u).getContent()) text(c, p, paras);
			paras.add(p.toString());
		} else if (u instanceof ContentAccessor) {
			for (Object c : ((ContentAccessor) u).getContent()) text(c, sb, paras);
		}
	}
}
