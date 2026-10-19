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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextField;
import org.docx4j.dml.CTTextLineBreak;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.diagram.CTCxn;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.STCxnType;
import org.docx4j.dml.diagram.STPtType;

/**
 * DrawingML's text for the text model: a paragraph's ({@code a:p}), and SmartArt's (WD REQ-511) in
 * reading order.
 */
final class Drawings {

	private Drawings() {}

	/** A DrawingML paragraph's text: its runs' and fields' {@code a:t}, a {@code a:br} a line break. */
	static String text(CTTextParagraph p) {
		StringBuilder sb = new StringBuilder();
		for (Object o : p.getEGTextRun()) {
			Object v = Extractor.unwrap(o);
			if (v instanceof CTRegularTextRun) {
				if (((CTRegularTextRun) v).getT() != null) sb.append(((CTRegularTextRun) v).getT());
			} else if (v instanceof CTTextField) {
				if (((CTTextField) v).getT() != null) sb.append(((CTTextField) v).getT());
			} else if (v instanceof CTTextLineBreak) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	/** A text body's paragraphs, a line each; none when every one is empty. */
	static List<String> lines(CTTextBody body) {
		List<String> lines = new ArrayList<String>();
		if (body == null) return lines;
		boolean any = false;
		for (CTTextParagraph p : body.getP()) {
			String s = text(p);
			if (!s.isEmpty()) any = true;
			lines.add(s);
		}
		return any ? lines : new ArrayList<String>();
	}

	/**
	 * SmartArt's text from its data part (REQ-511): from the {@code doc} point depth-first along the
	 * {@code parOf} connections, each point's children by {@code @srcOrd}, the text of the points of
	 * type {@code node} and {@code asst}, each {@code a:p} a line. A point whose paragraphs are all
	 * empty gives nothing. Storage order (of {@code dgm:ptLst} or {@code dgm:cxnLst}) is not reading
	 * order, and the drawing part's cached shapes are not read.
	 */
	static List<String> smartArt(CTDataModel model) {
		return smartArt(model, null);
	}

	/** As {@link #smartArt(CTDataModel)}, with each point that gives lines mapped to the index of its first (where {@code starts} is not null). */
	static List<String> smartArt(CTDataModel model, Map<Object, Integer> starts) {
		List<String> lines = new ArrayList<String>();
		if (model == null || model.getPtLst() == null) return lines;
		Map<String, CTPt> points = new HashMap<String, CTPt>();
		CTPt doc = null;
		for (CTPt pt : model.getPtLst().getPt()) {
			if (pt.getModelId() == null) continue;
			points.put(pt.getModelId(), pt);
			if (doc == null && pt.getType() == STPtType.DOC) doc = pt;
		}
		if (doc == null) return lines;
		Map<String, List<CTCxn>> children = new HashMap<String, List<CTCxn>>();
		if (model.getCxnLst() != null) {
			for (CTCxn c : model.getCxnLst().getCxn()) {
				if (c.getType() != STCxnType.PAR_OF || c.getSrcId() == null) continue;
				children.computeIfAbsent(c.getSrcId(), k -> new ArrayList<CTCxn>()).add(c);
			}
		}
		for (List<CTCxn> list : children.values()) Collections.sort(list, Comparator.comparingLong(CTCxn::getSrcOrd));
		visit(doc, points, children, new LinkedHashSet<String>(), lines, starts);
		return lines;
	}

	private static void visit(CTPt pt, Map<String, CTPt> points, Map<String, List<CTCxn>> children, Set<String> seen,
			List<String> lines, Map<Object, Integer> starts) {
		if (!seen.add(pt.getModelId())) return; // a cycle
		if (pt.getType() == STPtType.NODE || pt.getType() == STPtType.ASST) {
			List<String> own = lines(pt.getT());
			if (starts != null && !own.isEmpty()) starts.put(pt, lines.size());
			lines.addAll(own);
		}
		List<CTCxn> list = children.get(pt.getModelId());
		if (list == null) return;
		for (CTCxn c : list) {
			CTPt child = points.get(c.getDestId());
			if (child != null) visit(child, points, children, seen, lines, starts);
		}
	}
}
