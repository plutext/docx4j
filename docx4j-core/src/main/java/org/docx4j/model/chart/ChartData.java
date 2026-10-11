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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart.CTFormulaRef;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart.CTFullRef;
import org.docx4j.dml.chart.CTAxDataSource;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.dml.chart.CTExtensionList;
import org.docx4j.dml.chart.CTLvl;
import org.docx4j.dml.chart.CTMultiLvlStrData;
import org.docx4j.dml.chart.CTMultiLvlStrRef;
import org.docx4j.dml.chart.CTNumData;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTNumRef;
import org.docx4j.dml.chart.CTNumVal;
import org.docx4j.dml.chart.CTSerTx;
import org.docx4j.dml.chart.CTStrData;
import org.docx4j.dml.chart.CTStrRef;
import org.docx4j.dml.chart.CTStrVal;

/**
 * One of a series' data: its name, categories, values, x or y values or bubble sizes.  The
 * reference ({@code c:f}, the cells it comes from) where there is one, and the points of its
 * cache or literal, <b>keyed by {@code c:pt/@idx}</b>: a cache with a gap has no point at that
 * index, and {@link #getCount()} ({@code c:ptCount}) says how many there are in all.  Each point
 * keeps the text the cache holds, so a value reads as Office wrote it ({@code 4.4000000000000004}).
 *
 * <p>Multi-level categories ({@code c:multiLvlStrRef}) have a {@link #getLevels() level} per row
 * of labels, the innermost (the one next to the axis) first, as the file has them.</p>
 *
 * <p>Excel's chart filter (measured, the p2-filtered probe, 2026-10-12): a category filtered out
 * narrows every series' {@code c:f} to the cells still shown, a union such as
 * {@code (Sheet1!$A$2:$A$3,Sheet1!$A$5)}, and its cache to their points, the whole range kept as
 * {@code c15:fullRef} ({@link #getFullReference()}); a series filtered out (a
 * {@code c15:filtered...Series}) has no {@code c:f} but a {@code c15:formulaRef}, which
 * {@link #getReference()} returns.</p>
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartData {

	/** What the data is in the file */
	public enum Kind {
		/** {@code c:numRef}: cells, with a number cache */
		NUMBER_REFERENCE,
		/** {@code c:numLit}: numbers written into the chart */
		NUMBER_LITERAL,
		/** {@code c:strRef}: cells, with a text cache */
		STRING_REFERENCE,
		/** {@code c:strLit}: text written into the chart */
		STRING_LITERAL,
		/** {@code c:multiLvlStrRef}: cells, several rows of labels */
		MULTI_LEVEL_REFERENCE,
		/** {@code c:tx/c:v}: a series name written as text */
		TEXT
	}

	private final Kind kind;
	private final String reference;
	private String fullReference;
	private final Long count;
	private final String formatCode;
	private final List<Map<Integer, String>> levels;
	private final Map<Integer, String> pointFormats;
	private final Object source;

	private ChartData(Kind kind, String reference, Long count, String formatCode, List<Map<Integer, String>> levels,
			Map<Integer, String> pointFormats, Object source) {
		this.kind = kind;
		this.reference = reference;
		this.count = count;
		this.formatCode = formatCode;
		this.levels = levels;
		this.pointFormats = pointFormats;
		this.source = source;
	}

	// ------------------------------------------------------------------ reading

	/** Categories or x values; null for none */
	static ChartData of(CTAxDataSource src) {
		if (src == null) return null;
		if (src.getMultiLvlStrRef() != null) return of(src.getMultiLvlStrRef());
		if (src.getNumRef() != null) return of(src.getNumRef());
		if (src.getNumLit() != null) return numbers(Kind.NUMBER_LITERAL, null, src.getNumLit(), src.getNumLit());
		if (src.getStrRef() != null) return of(src.getStrRef());
		if (src.getStrLit() != null) return strings(Kind.STRING_LITERAL, null, src.getStrLit(), src.getStrLit());
		return null;
	}

	/** Values, y values or bubble sizes; null for none */
	static ChartData of(CTNumDataSource src) {
		if (src == null) return null;
		if (src.getNumRef() != null) return of(src.getNumRef());
		if (src.getNumLit() != null) return numbers(Kind.NUMBER_LITERAL, null, src.getNumLit(), src.getNumLit());
		return null;
	}

	/** A series' name ({@code c:tx}); null for none */
	static ChartData of(CTSerTx tx) {
		if (tx == null) return null;
		if (tx.getStrRef() != null) return of(tx.getStrRef());
		if (tx.getV() != null) {
			Map<Integer, String> one = new TreeMap<Integer, String>();
			one.put(0, tx.getV());
			return new ChartData(Kind.TEXT, null, 1L, null, Collections.singletonList(Collections.unmodifiableMap(one)),
					Collections.<Integer, String>emptyMap(), tx);
		}
		return null;
	}

	static ChartData of(CTStrRef ref) {
		return withExtensions(strings(Kind.STRING_REFERENCE, ref.getF(), ref.getStrCache(), ref), ref.getF(), ref.getExtLst());
	}

	static ChartData of(CTNumRef ref) {
		return withExtensions(numbers(Kind.NUMBER_REFERENCE, ref.getF(), ref.getNumCache(), ref), ref.getF(), ref.getExtLst());
	}

	/** c15:fullRef, and c15:formulaRef where there is no c:f */
	private static ChartData withExtensions(ChartData d, String f, CTExtensionList extLst) {
		if (extLst == null) return d;
		String formulaRef = null;
		for (CTExtension e : extLst.getExt()) {
			for (Object o : e.getAny()) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof CTFullRef) d.fullReference = ((CTFullRef) u).getSqref();
				else if (u instanceof CTFormulaRef) formulaRef = ((CTFormulaRef) u).getSqref();
			}
		}
		if (f == null && formulaRef != null) {
			ChartData r = new ChartData(d.kind, formulaRef, d.count, d.formatCode, d.levels, d.pointFormats, d.source);
			r.fullReference = d.fullReference;
			return r;
		}
		return d;
	}

	static ChartData of(CTMultiLvlStrRef ref) {
		return withExtensions(multiLevel(ref), ref.getF(), ref.getExtLst());
	}

	private static ChartData multiLevel(CTMultiLvlStrRef ref) {
		CTMultiLvlStrData cache = ref.getMultiLvlStrCache();
		List<Map<Integer, String>> levels = new ArrayList<Map<Integer, String>>();
		Long count = null;
		if (cache != null) {
			count = cache.getPtCount() == null ? null : cache.getPtCount().getVal();
			for (CTLvl lvl : cache.getLvl()) levels.add(points(lvl.getPt()));
		}
		return new ChartData(Kind.MULTI_LEVEL_REFERENCE, ref.getF(), count, null, Collections.unmodifiableList(levels),
				Collections.<Integer, String>emptyMap(), ref);
	}

	private static ChartData strings(Kind kind, String f, CTStrData data, Object source) {
		Long count = data == null || data.getPtCount() == null ? null : data.getPtCount().getVal();
		Map<Integer, String> pts = data == null ? Collections.<Integer, String>emptyMap() : points(data.getPt());
		return new ChartData(kind, f, count, null, Collections.singletonList(pts),
				Collections.<Integer, String>emptyMap(), source);
	}

	private static ChartData numbers(Kind kind, String f, CTNumData data, Object source) {
		Long count = null;
		String formatCode = null;
		Map<Integer, String> pts = new TreeMap<Integer, String>();
		Map<Integer, String> formats = new TreeMap<Integer, String>();
		if (data != null) {
			count = data.getPtCount() == null ? null : data.getPtCount().getVal();
			formatCode = data.getFormatCode();
			for (CTNumVal v : data.getPt()) {
				pts.put((int) v.getIdx(), v.getV());
				if (v.getFormatCode() != null) formats.put((int) v.getIdx(), v.getFormatCode());
			}
		}
		return new ChartData(kind, f, count, formatCode, Collections.singletonList(Collections.unmodifiableMap(pts)),
				Collections.unmodifiableMap(formats), source);
	}

	private static Map<Integer, String> points(List<CTStrVal> pts) {
		Map<Integer, String> m = new TreeMap<Integer, String>();
		for (CTStrVal v : pts) m.put((int) v.getIdx(), v.getV());
		return Collections.unmodifiableMap(m);
	}

	// ------------------------------------------------------------------ what it holds

	public Kind getKind() {
		return kind;
	}

	/** Whether its points are numbers */
	public boolean isNumeric() {
		return kind == Kind.NUMBER_REFERENCE || kind == Kind.NUMBER_LITERAL;
	}

	/**
	 * {@code c:f}: the cells, as written ({@code Sheet1!$B$2:$B$5}), or a filtered series'
	 * {@code c15:formulaRef}; null for a literal
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * {@code c15:fullRef}: where Excel's chart filter hides categories, the whole range, of which
	 * {@link #getReference()} is the part shown; null where nothing is filtered
	 */
	public String getFullReference() {
		return fullReference;
	}

	/** {@code c:ptCount}: how many points there are, gaps included; null where the file has none */
	public Long getCount() {
		return count;
	}

	/** The points' number format ({@code c:formatCode}); null for text */
	public String getFormatCode() {
		return formatCode;
	}

	/** A point's own number format, where its {@code c:pt} gives one; else the data's */
	public String getFormatCode(int idx) {
		String f = pointFormats.get(idx);
		return f != null ? f : formatCode;
	}

	/** The points of the innermost level, by index; an index without a point is a gap */
	public Map<Integer, String> getPoints() {
		return levels.isEmpty() ? Collections.<Integer, String>emptyMap() : levels.get(0);
	}

	/** A point's text; null for a gap */
	public String getText(int idx) {
		return getPoints().get(idx);
	}

	/** A point's number; null for a gap or text that is not a number */
	public Double getNumber(int idx) {
		String v = getText(idx);
		if (v == null) return null;
		try {
			return Double.valueOf(v.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Every level of labels, the innermost first: one for anything but multi-level categories */
	public List<Map<Integer, String>> getLevels() {
		return levels;
	}

	/**
	 * How many points to show: {@link #getCount()}, or, where the file states none, one past the
	 * highest index.
	 */
	public int size() {
		if (count != null) return (int) Math.min(Integer.MAX_VALUE, count.longValue());
		int n = 0;
		for (Map<Integer, String> level : levels) {
			for (Integer i : level.keySet()) n = Math.max(n, i + 1);
		}
		return n;
	}

	/** The JAXB object it was read from: a CTNumRef, CTStrRef, CTMultiLvlStrRef, CTNumData, CTStrData or CTSerTx */
	public Object getSource() {
		return source;
	}
}
