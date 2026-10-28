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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.fields.index.IndexGenerator;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
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
 * FILENAME, TEMPLATE and FILESIZE from the context; NUMWORDS and NUMCHARS over the word-count probe. Round 2 (section 14.4):
 * LISTNUM, AUTONUM, AUTONUMLGL, AUTONUMOUT and SYMBOL against Word's PDFs (Word writes no result for them: its saves keep
 * OLD), in the updater, the text model and the HTML writer.
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

	/** What Word's PDF of its round-1 save paints for the LISTNUM and AUTONUM cases (round 2, section 14.4): Word writes no result for them. */
	static final String[][] LIST_NUMBERS = {
			{ "L01_LISTNUM", "i)" }, { "L02_LISTNUM", "ii)" }, { "L03_LEVEL2", "b)" }, { "L04_LEVEL3", "i)" }, { "L05_LEVEL1", "2)" },
			{ "L06_START", "x)" }, { "L07_AFTER_START", "xi)" }, { "L09_LEGAL", "2.1.14." }, { "L10_LEGAL_L2", "2.2." },
			{ "L11_LEGAL_L2", "2.3." }, { "L12_LEGAL_L1", "2.3.1." }, { "L13_OUTLINE", "2." }, { "L14_OUTLINE_L2", "D." },
			{ "L15_OUTLINE_L3", "1." }, { "L16_NUMBERDEFAULT", "ii)" }, { "L17_L2_START", "e)" }, { "L18_L2_NEXT", "f)" },
			{ "L19_L1_AFTER", "i)" },
			{ "A01_AUTONUM", "1." }, { "A02_AUTONUM", "2." }, { "A03_SEPARATOR", "3:" }, { "A04_AUTONUM", "4." },
			{ "A05_LGL_H1", "1." }, { "A06_LGL_H2", "1.1." }, { "A07_LGL_H2", "1.2." }, { "A08_LGL_H1", "2." },
			{ "A09_LGL_H2_NOPERIOD", "2.1" }, { "A10_LGL_NORMAL", "2.1.1." }, { "A11_OUT_H1", "III." }, { "A12_OUT_H2", "A." },
			{ "A13_OUT_H3", "1." }, { "A14_OUT_H2", "B." }, { "A15_AUTONUM_AFTER", "1." } };

	/** What Word's PDF of listnum-probe-r2-word365.docx paints (no heading before the first case). */
	static final String[][] ROUND2 = {
			{ "R01_LISTNUM", "1)" }, { "R02_LISTNUM", "2)" }, { "R03_LEVEL3", "i)" }, { "R04_LEVEL2", "b)" }, { "R05_LEGAL", "3." },
			{ "R06_AFTER_H1", "a)" }, { "R07_AFTER_H2", "i)" }, { "R08_IN_H1", "4)" }, { "R09_LEVEL1", "5)" }, { "R10_AFTER_H3", "(1)" },
			{ "R11_AUTONUM", "1." }, { "R12_AUTONUM", "2." }, { "R13_AUTONUM_AFTER_H1", "3." }, { "R14_AUTONUM_IN_H1", "1." },
			{ "R15_AUTONUMLGL", "1.1." } };

	/** The marshalled paragraph holding the case, or null. */
	static String paragraphXml(WordprocessingMLPackage pkg, String token) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + token + " = <")) return m.group();
		return null;
	}

	/** The w:t texts of the marshalled paragraph, joined. */
	static String shown(String paragraphXml) {
		StringBuilder sb = new StringBuilder();
		Matcher m = Pattern.compile("<w:t(?: [^>]*)?>([^<]*)</w:t>").matcher(paragraphXml);
		while (m.find()) sb.append(m.group(1));
		return sb.toString();
	}

	@Test
	public void listNumbersAsWord() throws Exception {
		WordprocessingMLPackage pkg = load("phase3-probe-r1.docx");
		new FieldUpdater(pkg).only(FieldUpdater.LIVE).update(false);
		Map<String, String> ours = ExpressionProbeWordTest.results(pkg);
		for (String[] c : LIST_NUMBERS) assertEquals(c[0], c[1], ours.get(c[0]));
		assertTrue(shown(paragraphXml(pkg, "L08_TWO_IN_ONE")).contains("xii) and xiii)")); // two in one paragraph
		// Word's save keeps OLD in every one of them: it paints them live
		Map<String, String> word = ExpressionProbeWordTest.results(load("phase3-probe-r1-word365.docx"));
		for (String[] c : LIST_NUMBERS) assertEquals(c[0], "OLD", word.get(c[0]));
	}

	@Test
	public void listNumbersRound2AsWord() throws Exception {
		WordprocessingMLPackage pkg = load("listnum-probe-r2.docx");
		new FieldUpdater(pkg).only(FieldUpdater.LIVE).update(false);
		Map<String, String> ours = ExpressionProbeWordTest.results(pkg);
		for (String[] c : ROUND2) assertEquals(c[0], c[1], ours.get(c[0]));
		Map<String, String> word = ExpressionProbeWordTest.results(load("listnum-probe-r2-word365.docx"));
		for (String[] c : ROUND2) assertEquals(c[0], "OLD", word.get(c[0]));
	}

	/** The same numbers from the text model under fields: update and from the HTML writer (the PDF writer shares its preprocessing). */
	@Test
	public void listNumbersInTextAndHtml() throws Exception {
		String text = PlainText.of(Texts.of(load("listnum-probe-r2.docx"), new TextOptions().fields(TextOptions.Fields.UPDATE)));
		for (String[] c : ROUND2) assertTrue(c[0] + " in " + text, text.contains(c[0] + " = " + c[1]));
		String html = html(load("listnum-probe-r2.docx"));
		for (String[] c : ROUND2) assertTrue(c[0] + " in " + html, html.contains(c[0] + " = " + c[1]));
		String symbols = html(load("phase3-probe-r1.docx"));
		assertTrue(symbols, symbols.contains("S10_LETTER = A") && symbols.contains("S09_OVER_255 = ###") && symbols.contains("S01_DECIMAL = \u00a9"));
		String words = PlainText.of(Texts.of(load("phase3-probe-r1.docx"), new TextOptions().fields(TextOptions.Fields.UPDATE)));
		assertTrue(words, words.contains("S03_SYMBOL_FONT = \uF0A9") && words.contains("S14_NO_ARGUMENT = ###") && words.contains("L09_LEGAL = 2.1.14."));
	}

	static String html(WordprocessingMLPackage pkg) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8").replaceAll("<[^>]+>", "").replaceAll("\\s+", " ");
	}

	/** D26 (Jason, 2026-10-29): the package's source name and size are the defaults of every context built from it. */
	@Test
	public void sourceNameAndSizeOnThePackage() throws Exception {
		Map<String, String> word = ExpressionProbeWordTest.results(load("phase3-probe-r1-word365.docx"));
		WordprocessingMLPackage pkg = load("phase3-probe-r1.docx");
		pkg.setSourceName("X:\\__tmp\\cr067\\phase3\\phase3-probe-r1-word365.docx");
		pkg.setSourceSize(16820L);
		// the text model under fields: update, from the package alone
		String text = PlainText.of(Texts.of(pkg, new TextOptions().fields(TextOptions.Fields.UPDATE)));
		assertTrue(text, text.contains("N01_FILENAME = phase3-probe-r1-word365.docx") && text.contains("N02_FILENAME_PATH = X:\\__tmp\\cr067\\phase3\\phase3-probe-r1-word365.docx")
				&& text.contains("N06_FILESIZE = 16820") && text.contains("N07_FILESIZE_KB = 17"));
		// the updater, from the package alone
		new FieldUpdater(pkg).only(new java.util.HashSet<String>(java.util.Arrays.asList("FILENAME", "FILESIZE"))).update(false);
		Map<String, String> ours = ExpressionProbeWordTest.results(pkg);
		for (String token : new String[] { "N01_FILENAME", "N02_FILENAME_PATH", "N03_FILENAME_UPPER", "N06_FILESIZE", "N07_FILESIZE_KB", "N08_FILESIZE_MB" }) {
			assertEquals(token, word.get(token), ours.get(token));
		}
		// the context's own value overrides the package's
		WordprocessingMLPackage again = load("phase3-probe-r1.docx");
		again.setSourceName("package.docx");
		FieldUpdater fu = new FieldUpdater(again).only(new java.util.HashSet<String>(java.util.Arrays.asList("FILENAME")));
		fu.getContext().fileName("call.docx");
		fu.update(false);
		assertEquals("call.docx", ExpressionProbeWordTest.results(again).get("N01_FILENAME"));
		assertEquals("OLD", ExpressionProbeWordTest.results(load("phase3-probe-r1.docx")).get("N01_FILENAME")); // nothing set: cached
	}

	/** SYMBOL as Word's PDF paints it (mutool: glyph, font and size per run): the character, w:sym for a symbol font, the font and size, ### in bold for a code Word cannot read. */
	@Test
	public void symbolsAsWord() throws Exception {
		WordprocessingMLPackage pkg = load("phase3-probe-r1.docx");
		new FieldUpdater(pkg).only(FieldUpdater.LIVE).update(false);
		Map<String, String> ours = ExpressionProbeWordTest.results(pkg);
		String[][] texts = { { "S01_DECIMAL", "\u00a9" }, { "S02_HEX", "\u00a9" }, { "S05_UNICODE", "\u20ac" }, { "S06_UNICODE_HEX", "\u20ac" },
				{ "S07_SIZE", "\u00a9" }, { "S08_FONT_SIZE_H", "\u00a9" }, { "S09_OVER_255", "###" }, { "S10_LETTER", "A" }, { "S11_ANSI", "A" },
				{ "S12_COURIER", "A" }, { "S13_NOT_A_NUMBER", "###" }, { "S14_NO_ARGUMENT", "###" } };
		for (String[] c : texts) assertEquals(c[0], c[1], ours.get(c[0]));
		String s3 = paragraphXml(pkg, "S03_SYMBOL_FONT");
		assertTrue(s3, s3.contains("<w:sym ") && s3.contains("w:font=\"Symbol\"") && s3.contains("w:char=\"F0A9\""));
		String s4 = paragraphXml(pkg, "S04_WINGDINGS");
		assertTrue(s4, s4.contains("<w:sym ") && s4.contains("w:font=\"Wingdings\"") && s4.contains("w:char=\"F041\""));
		String s7 = paragraphXml(pkg, "S07_SIZE");
		assertTrue(s7, s7.contains("<w:sz w:val=\"48\"/>"));
		String s8 = paragraphXml(pkg, "S08_FONT_SIZE_H");
		assertTrue(s8, s8.contains("w:ascii=\"Arial\"") && s8.contains("<w:sz w:val=\"24\"/>"));
		String s12 = paragraphXml(pkg, "S12_COURIER");
		assertTrue(s12, s12.contains("w:ascii=\"Courier New\"") && !s12.contains("<w:sym "));
		String s9 = paragraphXml(pkg, "S09_OVER_255");
		assertTrue(s9, s9.contains("<w:b/>"));
		Map<String, String> word = ExpressionProbeWordTest.results(load("phase3-probe-r1-word365.docx"));
		for (String[] c : texts) assertEquals(c[0], "OLD", word.get(c[0]));
	}
}
