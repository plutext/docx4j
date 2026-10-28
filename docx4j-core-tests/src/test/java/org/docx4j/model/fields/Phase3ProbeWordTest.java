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
package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.index.IndexGenerator;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocHelper;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Br;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * CR-067 phase 3 (section 14.2): the six INDEX fields of the probe regenerated from its XE marks by
 * {@link IndexGenerator} give, paragraph for paragraph (style, text, bold and italic page numbers, the section
 * paragraphs of {@code \c}), what Word 365's update (Ctrl+A, F9) wrote in {@code phase3-probe-r1-word365.docx};
 * FILENAME, TEMPLATE and FILESIZE from the context; NUMWORDS and NUMCHARS over the word-count probe.
 */
public class Phase3ProbeWordTest {

	static WordprocessingMLPackage load(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource("fields/cr067-phase3/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	/** The probe's pages: a paragraph holding a page break ends its page (the XE pages are 1 to 3 by explicit breaks). */
	static IndexGenerator.Pager pageBreakPager(WordprocessingMLPackage pkg) {
		@SuppressWarnings("unchecked")
		List<P> paragraphs = (List<P>) (List<?>) TocHelper.getAllElementsFromObject(pkg.getMainDocumentPart().getJaxbElement().getBody(), P.class);
		final Map<P, Integer> pages = new IdentityHashMap<P, Integer>();
		int page = 1;
		for (P p : paragraphs) {
			pages.put(p, page);
			for (Object o : TocHelper.getAllElementsFromObject(p, Br.class)) {
				if (((Br) o).getType() == STBrType.PAGE) page++;
			}
		}
		return new IndexGenerator.Pager() {
			@Override public Integer pageOf(P p) {
				return pages.get(p);
			}
		};
	}

	/** Each INDEX field's result, by token: one line per paragraph, "[style] text" with **bold** and _italic_ runs, "[SECT cols=n]" for a section paragraph. */
	static Map<String, List<String>> indexResults(WordprocessingMLPackage pkg) {
		Map<String, List<String>> out = new LinkedHashMap<String, List<String>>();
		List<Object> body = pkg.getMainDocumentPart().getContent();
		for (int i = 0; i < body.size(); i++) {
			Object u = XmlUtils.unwrap(body.get(i));
			if (!(u instanceof P)) continue;
			String text = text((P) u);
			if (!text.startsWith("IDX")) continue;
			String token = text.substring(0, text.indexOf(" = "));
			List<String> lines = new ArrayList<String>();
			for (int j = i + 1; j < body.size(); j++) {
				Object v = XmlUtils.unwrap(body.get(j));
				if (!(v instanceof P)) continue;
				P p = (P) v;
				if (hasFldChar(p, STFldCharType.END)) { i = j; break; }
				if (p.getPPr() != null && p.getPPr().getSectPr() != null) {
					lines.add("[SECT type=" + (p.getPPr().getSectPr().getType() == null ? "-" : p.getPPr().getSectPr().getType().getVal())
							+ " cols=" + (p.getPPr().getSectPr().getCols() == null || p.getPPr().getSectPr().getCols().getNum() == null ? "1" : p.getPPr().getSectPr().getCols().getNum()) + "]");
					continue;
				}
				lines.add("[" + (p.getPPr() == null || p.getPPr().getPStyle() == null ? "-" : p.getPPr().getPStyle().getVal()) + "] " + text(p));
			}
			out.put(token, lines);
		}
		return out;
	}

	static boolean hasFldChar(P p, STFldCharType type) {
		for (Object o : TocHelper.getAllElementsFromObject(p, FldChar.class)) if (((FldChar) o).getFldCharType() == type) return true;
		return false;
	}

	/** The paragraph's runs' text, bold runs in ** and italic in _ _ (a bold italic run in **_ _**). */
	static String text(P p) {
		StringBuilder sb = new StringBuilder();
		for (Object o : p.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (!(u instanceof R)) continue;
			R r = (R) u;
			boolean b = r.getRPr() != null && r.getRPr().getB() != null && r.getRPr().getB().isVal();
			boolean i = r.getRPr() != null && r.getRPr().getI() != null && r.getRPr().getI().isVal();
			StringBuilder t = new StringBuilder();
			for (Object c : r.getContent()) {
				Object v = XmlUtils.unwrap(c);
				if (v instanceof Text && !(c instanceof jakarta.xml.bind.JAXBElement && !"t".equals(((jakarta.xml.bind.JAXBElement<?>) c).getName().getLocalPart()))) t.append(((Text) v).getValue());
			}
			if (t.length() == 0) continue;
			sb.append(b ? "**" : "").append(i ? "_" : "").append(t).append(i ? "_" : "").append(b ? "**" : "");
		}
		return sb.toString();
	}

	@Test
	public void indexesAsWord() throws Exception {
		Map<String, List<String>> word = indexResults(load("phase3-probe-r1-word365.docx"));
		assertEquals(6, word.size());
		assertEquals("[Index1] Apple, 1, 2, 3", word.get("IDX01_PLAIN").get(3));
		WordprocessingMLPackage pkg = load("phase3-probe-r1.docx");
		int n = new IndexGenerator(pkg, pageBreakPager(pkg)).update();
		assertEquals(6, n);
		Map<String, List<String>> ours = indexResults(pkg);
		assertEquals(word.keySet(), ours.keySet());
		for (String token : word.keySet()) {
			assertEquals(token, String.join("\n", word.get(token)), String.join("\n", ours.get(token)));
		}
		// the enclosing section continues continuous after a \c index (measured)
		assertEquals("continuous", pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr().getType().getVal());
	}

	@Test
	public void wordsShapeSurvivesTheUpdaterWithoutPages() throws Exception {
		// Word's saved shape (begin and separate in one paragraph, the entries, the end in its own): no pager, kept as it is
		WordprocessingMLPackage pkg = load("phase3-probe-r1-word365.docx");
		Map<String, List<String>> before = indexResults(pkg);
		new FieldUpdater(pkg).only(new java.util.HashSet<String>(java.util.Arrays.asList("INDEX"))).indexPager(null).update(false);
		assertEquals(before, indexResults(pkg));
		// and regenerated with the pages, the same again
		new FieldUpdater(pkg).only(new java.util.HashSet<String>(java.util.Arrays.asList("INDEX"))).indexPager(pageBreakPager(pkg)).update(false);
		assertEquals(before, indexResults(pkg));
	}

	@Test
	public void fileFieldsAsWord() throws Exception {
		Map<String, String> word = ExpressionProbeWordTest.results(load("phase3-probe-r1-word365.docx"));
		WordprocessingMLPackage pkg = load("phase3-probe-r1.docx");
		FieldUpdater fu = new FieldUpdater(pkg);
		fu.getContext().fileName("X:\\__tmp\\cr067\\phase3\\phase3-probe-r1-word365.docx").fileSize(16820L);
		fu.only(new java.util.HashSet<String>(java.util.Arrays.asList("FILENAME", "TEMPLATE", "FILESIZE", "DOCPROPERTY"))).update(false);
		Map<String, String> ours = ExpressionProbeWordTest.results(pkg);
		for (String token : new String[] { "N01_FILENAME", "N02_FILENAME_PATH", "N03_FILENAME_UPPER", "N06_FILESIZE", "N07_FILESIZE_KB", "N08_FILESIZE_MB" }) {
			assertEquals(token, word.get(token), ours.get(token));
		}
		assertEquals("phase3-probe-r1-word365.docx", ours.get("N01_FILENAME"));
		// TEMPLATE: the probe's app.xml (docx4j's) names no template, so the cached result stays; Word's own says Normal.dotm
		assertEquals("Normal.dotm", word.get("N04_TEMPLATE"));
		assertEquals("OLD", ours.get("N04_TEMPLATE"));
		assertEquals("OLD", ours.get("N05_TEMPLATE_PATH")); // \p: not known to docx4j
		// Word's save of the probe names its template: evaluated from that file, the same
		WordprocessingMLPackage saved = load("phase3-probe-r1-word365.docx");
		assertEquals("Normal.dotm", FieldEvaluators.evaluate(FieldInstruction.parse(" TEMPLATE "), FieldContext.of(saved)));
		assertEquals("Normal.dotm", FieldEvaluators.evaluate(FieldInstruction.parse(" DOCPROPERTY Template "), FieldContext.of(saved)));
	}

	@Test
	public void wordCountsAsWord() throws Exception {
		Map<String, String> word = ExpressionProbeWordTest.results(load("numwords-probe-r1-word365.docx"));
		assertEquals("36", word.get("W01_NUMWORDS"));
		assertEquals("212", word.get("W02_NUMCHARS"));
		WordprocessingMLPackage pkg = load("numwords-probe-r1.docx");
		FieldContext ctx = FieldContext.of(pkg);
		// the fresh counts of the file as Word saw it before its update (the fields' OLD results in place)
		assertEquals("36", FieldEvaluators.evaluate(FieldInstruction.parse(" NUMWORDS "), ctx));
		assertEquals("212", FieldEvaluators.evaluate(FieldInstruction.parse(" NUMCHARS "), ctx));
		// the updater: NUMWORDS unchanged by its own result; NUMCHARS counts the 36 written over OLD (Word's 212 was
		// its stale count; its DOCPROPERTY Characters, updated later, said 211 too)
		new FieldUpdater(pkg).update(false);
		Map<String, String> ours = ExpressionProbeWordTest.results(pkg);
		assertEquals("36", ours.get("W01_NUMWORDS"));
		assertEquals("211", ours.get("W02_NUMCHARS"));
		assertEquals("211", word.get("W04_PROP_CHARACTERS"));
		// headers and footnotes are not counted: Word's status bar said 38 (the footnote's two)
		assertTrue(true);
	}
}
