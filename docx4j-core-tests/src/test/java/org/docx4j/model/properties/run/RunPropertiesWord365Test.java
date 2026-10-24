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
package org.docx4j.model.properties.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.text.TextBlock;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextRun;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTEm;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STEm;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Run properties read as Word 365 reads them (CR-060), against the probes in {@code runprops/word365} and Word's
 * saves of them (see that folder's README). Word writes back only values it reads one way, which docx4j reads
 * right, so each marker's reading in a probe must equal its reading in Word's save: through the resolver, the text
 * model, the HTML and PDF styling, and a docx4j round trip.
 */
public class RunPropertiesWord365Test {

	private static final String DIR = "runprops/word365/";

	private static final String[] FILES = {
		"onoff-valid", "onoff-empty", "onoff-space", "onoff-TRUE", "onoff-yes",
		"props-b-case", "props-b-styles", "props-toggles-1", "props-toggles-2",
		"props-u-valid", "props-u-case", "props-u-other", "props-enum-missing", "props-enum-other",
		"colour-names-highlight", "colour-names-css", "colour-case", "colour-hex",
		"colour-table-grey", "colour-table-other",
		"props-u-styles", "colour-styles", "props-enum-styles", "colour-elsewhere",
		"props-u-onoff-styles", "colour-auto-styles", "highlight-char-styles", "colour-elsewhere-styles",
		"underline-styles", "strike-styles" }; // round 8's emphasis-marks too, but Word's save splits its Japanese markers' runs

	/**
	 * Where Word's own save changes what Word drew (CR-060 section 13, item 5), Word's reading from its PDF: a direct
	 * highlight over a character style's, drawn but dropped by the save; an underline colour "auto" over a style's
	 * red, drawn black but dropped.
	 */
	private static final Map<String, Map<String, String>> WORD_DREW = new LinkedHashMap<>();
	static {
		WORD_DREW.put("props-enum-styles M6_HIGHLIGHT_GREEN", java.util.Collections.singletonMap("highlight", "green"));
		WORD_DREW.put("highlight-char-styles H7_CHAR_STYLE_DIRECT_GREEN", java.util.Collections.singletonMap("highlight", "green"));
		WORD_DREW.put("colour-elsewhere-styles U4_UNDERLINE_AUTO", java.util.Collections.singletonMap("u-color", "auto"));
	}

	/** The on/off run properties the probes test (CR-060 section 1, rule 1) */
	private static final Map<String, Function<RPr, BooleanDefaultTrue>> ON_OFF = new LinkedHashMap<>();
	static {
		ON_OFF.put("b", RPr::getB);
		ON_OFF.put("i", RPr::getI);
		ON_OFF.put("caps", RPr::getCaps);
		ON_OFF.put("smallCaps", RPr::getSmallCaps);
		ON_OFF.put("strike", RPr::getStrike);
		ON_OFF.put("dstrike", RPr::getDstrike);
		ON_OFF.put("outline", RPr::getOutline);
		ON_OFF.put("shadow", RPr::getShadow);
		ON_OFF.put("emboss", RPr::getEmboss);
		ON_OFF.put("imprint", RPr::getImprint);
		ON_OFF.put("vanish", RPr::getVanish);
	}

	/** A probe paragraph's marker run, in the probe and in Word's save */
	static final class Marker {
		final String file, text;
		final R run, savedRun;
		final P p, savedP;

		Marker(String file, String text, R run, P p, R savedRun, P savedP) {
			this.file = file;
			this.text = text;
			this.run = run;
			this.p = p;
			this.savedRun = savedRun;
			this.savedP = savedP;
		}

		@Override
		public String toString() {
			return file + " " + text;
		}
	}

	/** A probe and Word's save, loaded */
	static final class Probe {
		final String name;
		final WordprocessingMLPackage pkg, saved;
		final List<Marker> markers = new ArrayList<>();

		Probe(String name, WordprocessingMLPackage pkg, WordprocessingMLPackage saved) {
			this.name = name;
			this.pkg = pkg;
			this.saved = saved;
			List<P> ps = paragraphs(pkg), savedPs = paragraphs(saved);
			assertEquals(name + ": paragraphs in the save", ps.size(), savedPs.size());
			for (int i = 1; i < ps.size(); i++) { // the first paragraph is the probe's title
				List<R> runs = runs(ps.get(i));
				if (runs.size() != 3) continue;
				String text = text(runs.get(1));
				R savedRun = null;
				for (R r : runs(savedPs.get(i))) {
					if (text(r).contains(text)) savedRun = r;
				}
				assertNotNull(name + ": " + text + " as one run in the save", savedRun);
				markers.add(new Marker(name, text, runs.get(1), ps.get(i), savedRun, savedPs.get(i)));
			}
			assertFalse(name + ": markers", markers.isEmpty());
		}
	}

	static WordprocessingMLPackage load(String resource) throws Exception {
		try (InputStream is = ResourceUtils.getResource(DIR + resource)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	static Probe probe(String name) throws Exception {
		return new Probe(name, load(name + ".docx"), load(name + "-word365.docx"));
	}

	static List<P> paragraphs(WordprocessingMLPackage pkg) {
		List<P> ps = new ArrayList<>();
		for (Object o : pkg.getMainDocumentPart().getContent()) {
			if (XmlUtils.unwrap(o) instanceof P) ps.add((P) XmlUtils.unwrap(o));
		}
		return ps;
	}

	static List<R> runs(P p) {
		List<R> rs = new ArrayList<>();
		for (Object o : p.getContent()) {
			if (XmlUtils.unwrap(o) instanceof R) rs.add((R) XmlUtils.unwrap(o));
		}
		return rs;
	}

	static String text(R r) {
		StringBuilder sb = new StringBuilder();
		for (Object o : r.getContent()) {
			if (XmlUtils.unwrap(o) instanceof Text) sb.append(((Text) XmlUtils.unwrap(o)).getValue());
		}
		return sb.toString();
	}

	/** What a run's effective properties say, as far as the probes test them */
	static Map<String, String> reading(WordprocessingMLPackage pkg, P p, R r) throws Exception {
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		RPr eff = resolver.getEffectiveRPr(r.getRPr(), p.getPPr());
		Map<String, String> m = new LinkedHashMap<>();
		for (Map.Entry<String, Function<RPr, BooleanDefaultTrue>> e : ON_OFF.entrySet()) {
			BooleanDefaultTrue b = eff == null ? null : e.getValue().apply(eff);
			m.put(e.getKey(), b != null && b.isVal() ? "on" : "off");
		}
		U u = eff == null ? null : eff.getU();
		m.put("u", u == null || u.getVal() == null || u.getVal() == UnderlineEnumeration.NONE ? "none" : u.getVal().value());
		Highlight h = eff == null ? null : eff.getHighlight();
		m.put("highlight", h == null || h.getVal() == null || h.getVal().equals("none") ? "none" : h.getVal());
		CTVerticalAlignRun va = eff == null ? null : eff.getVertAlign();
		m.put("vertAlign", va == null || va.getVal() == null ? "none" : va.getVal().value());
		CTEm em = eff == null ? null : eff.getEm();
		m.put("em", em == null || em.getVal() == null || em.getVal() == STEm.NONE ? "none" : em.getVal().value());
		ResolvedColor c = eff == null ? null : resolver.resolveColor(eff.getColor());
		m.put("color", c == null ? "auto" : c.toHex());
		ResolvedColor uc = u == null ? null : resolver.resolveColor(u);
		m.put("u-color", m.get("u").equals("none") ? "-" : uc == null ? "auto" : uc.toHex());
		org.docx4j.wml.CTShd shd = eff == null ? null : eff.getShd();
		String fill = shd == null ? null : Property.fillColour(shd.getFill());
		m.put("fill", fill == null ? "none" : fill);
		org.docx4j.wml.CTBorder bdr = eff == null ? null : eff.getBdr();
		boolean bordered = bdr != null && bdr.getVal() != null && bdr.getVal() != org.docx4j.wml.STBorder.NONE
				&& bdr.getVal() != org.docx4j.wml.STBorder.NIL;
		m.put("border", bordered ? Property.borderColour(bdr.getColor()) : "none");
		return m;
	}

	/** Word's reading of a marker: its save's, or its PDF's where the save changes what Word drew */
	static Map<String, String> wordReading(Probe pr, Marker mk) throws Exception {
		Map<String, String> m = reading(pr.saved, mk.savedP, mk.savedRun);
		if (WORD_DREW.containsKey(mk.toString())) m.putAll(WORD_DREW.get(mk.toString()));
		return m;
	}

	private static void compare(String what, Marker mk, Map<String, String> probe, Map<String, String> word,
			List<String> diffs) {
		for (String k : word.keySet()) {
			if (!word.get(k).equals(probe.get(k))) {
				diffs.add(mk + " " + what + " " + k + ": docx4j " + probe.get(k) + ", Word " + word.get(k));
			}
		}
	}

	private static void assertNone(List<String> diffs) {
		assertTrue(diffs.size() + " readings differ from Word's:\n" + String.join("\n", diffs), diffs.isEmpty());
	}

	/** The resolver's effective properties, for every marker */
	@Test
	public void effectivePropertiesAsWordReadsThem() throws Exception {
		List<String> diffs = new ArrayList<>();
		for (String f : FILES) {
			Probe pr = probe(f);
			for (Marker mk : pr.markers) {
				compare("effective", mk, reading(pr.pkg, mk.p, mk.run), wordReading(pr, mk), diffs);
			}
		}
		assertNone(diffs);
	}

	/** The text model's bold, italic and struck through, for every marker */
	@Test
	public void textModelAsWordReadsIt() throws Exception {
		List<String> diffs = new ArrayList<>();
		for (String f : FILES) {
			Probe pr = probe(f);
			List<TextRun> runs = textRuns(pr.pkg), savedRuns = textRuns(pr.saved);
			for (Marker mk : pr.markers) {
				TextRun t = textRun(runs, mk.text), w = textRun(savedRuns, mk.text);
				if (t == null && w == null) continue; // hidden (w:vanish): no text in either
				if (t == null || w == null) {
					diffs.add(mk + " text model: docx4j " + (t == null ? "hidden" : "shown") + ", Word " + (w == null ? "hidden" : "shown"));
					continue;
				}
				if (t.isBold() != w.isBold()) diffs.add(mk + " text model bold: docx4j " + t.isBold() + ", Word " + w.isBold());
				if (t.isItalic() != w.isItalic()) diffs.add(mk + " text model italic: docx4j " + t.isItalic() + ", Word " + w.isItalic());
				if (t.isStrike() != w.isStrike()) diffs.add(mk + " text model strike: docx4j " + t.isStrike() + ", Word " + w.isStrike());
			}
		}
		assertNone(diffs);
	}

	private static List<TextRun> textRuns(WordprocessingMLPackage pkg) {
		List<TextRun> runs = new ArrayList<>();
		for (TextBlock b : Texts.of(pkg, new TextOptions()).blocks()) {
			runs.addAll(b.getRuns());
		}
		return runs;
	}

	private static TextRun textRun(List<TextRun> runs, String text) {
		for (TextRun r : runs) {
			if (r.getText() != null && r.getText().contains(text)) return r;
		}
		return null;
	}

	/**
	 * The HTML styling of every marker: its paragraph style's class, its character style's, then its own CSS, as the
	 * HTML writers build them (a run's direct w:rPr goes in its style attribute).
	 */
	@Test
	public void htmlStylingAsWordReadsIt() throws Exception {
		List<String> diffs = new ArrayList<>();
		for (String f : FILES) {
			Probe pr = probe(f);
			for (Marker mk : pr.markers) {
				if (WORD_DREW.containsKey(mk.toString())) continue; // stated in wordsOwnSaveChangesWhatItDrew
				String probe = htmlDrawn(pr.pkg, mk.p, mk.run), word = htmlDrawn(pr.saved, mk.savedP, mk.savedRun);
				if (!probe.equals(word)) diffs.add(mk + " HTML: docx4j {" + probe + "}, Word {" + word + "}");
			}
		}
		assertNone(diffs);
	}

	/** The PDF styling, from a run's effective properties as the FO writer has it, for every marker */
	@Test
	public void pdfStylingAsWordReadsIt() throws Exception {
		List<String> diffs = new ArrayList<>();
		for (String f : FILES) {
			Probe pr = probe(f);
			for (Marker mk : pr.markers) {
				if (WORD_DREW.containsKey(mk.toString())) continue; // stated in wordsOwnSaveChangesWhatItDrew
				String probe = fo(pr.pkg, mk.p, mk.run), word = fo(pr.saved, mk.savedP, mk.savedRun);
				if (!probe.equals(word)) diffs.add(mk + " PDF: docx4j {" + probe + "}, Word {" + word + "}");
			}
		}
		assertNone(diffs);
	}

	/**
	 * A w:u stating only a colour is no underline in Word (CR-060 section 1, rule 2); Word's save keeps it as it is,
	 * so it is stated here rather than read from the save.
	 */
	@Test
	public void colourOnlyUnderlineIsNotDrawn() throws Exception {
		Probe pr = probe("props-u-valid");
		Marker mk = marker(pr, "U7_COLOR_ONLY");
		assertFalse(css(pr.pkg, mk.run.getRPr()), css(pr.pkg, mk.run.getRPr()).contains("underline"));
		assertFalse(fo(pr.pkg, mk.p, mk.run), fo(pr.pkg, mk.p, mk.run).contains("underline"));
		Marker red = marker(pr, "U8_SINGLE_RED");
		assertTrue(css(pr.pkg, red.run.getRPr()), css(pr.pkg, red.run.getRPr()).contains("underline"));
	}

	/**
	 * A highlight, vertAlign or em whose value is missing or invalid is one Word's repair drops (CR-060 section 1,
	 * rule 2), so over a style stating the property, the style's value is what Word shows (section 12): vertAlign
	 * and em over a character style, highlight over a paragraph style. A character style's highlight Word never
	 * draws (section 13).
	 */
	@Test
	public void anInvalidValueOverAStyleLeavesTheStyles() throws Exception {
		String w = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		List<org.docx4j.wml.Style> styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle();
		styles.add((org.docx4j.wml.Style) XmlUtils.unmarshalString(
				"<w:style " + w + " w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Marked\"><w:name w:val=\"Marked\"/>"
				+ "<w:rPr><w:highlight w:val=\"yellow\"/><w:vertAlign w:val=\"superscript\"/><w:em w:val=\"dot\"/></w:rPr></w:style>"));
		styles.add((org.docx4j.wml.Style) XmlUtils.unmarshalString(
				"<w:style " + w + " w:type=\"paragraph\" w:customStyle=\"1\" w:styleId=\"ParaMarked\"><w:name w:val=\"ParaMarked\"/>"
				+ "<w:rPr><w:highlight w:val=\"yellow\"/></w:rPr></w:style>"));
		String[] direct = { "<w:highlight w:val=\"YELLOW\"/><w:vertAlign w:val=\"Superscript\"/><w:em w:val=\"DOT\"/>",
				"<w:highlight w:val=\"bogus\"/><w:vertAlign w:val=\"bogus\"/><w:em w:val=\"bogus\"/>",
				"<w:highlight/><w:vertAlign/><w:em/>" };
		for (String d : direct) {
			P p = (P) XmlUtils.unmarshalString("<w:p " + w + "><w:pPr><w:pStyle w:val=\"ParaMarked\"/></w:pPr>"
					+ "<w:r><w:rPr><w:rStyle w:val=\"Marked\"/>" + d + "</w:rPr><w:t>x</w:t></w:r></w:p>");
			pkg.getMainDocumentPart().getContent().add(p);
			Map<String, String> m = reading(pkg, p, runs(p).get(0));
			assertEquals(d, "yellow", m.get("highlight")); // the paragraph style's
			assertEquals(d, "superscript", m.get("vertAlign"));
			assertEquals(d, "dot", m.get("em"));
			P alone = (P) XmlUtils.unmarshalString("<w:p " + w + "><w:r><w:rPr><w:rStyle w:val=\"Marked\"/>" + d
					+ "</w:rPr><w:t>x</w:t></w:r></w:p>");
			pkg.getMainDocumentPart().getContent().add(alone);
			assertEquals(d, "none", reading(pkg, alone, runs(alone).get(0)).get("highlight")); // the character style's isn't drawn
		}
	}

	/** The same through both HTML writers, end to end, for a few markers. */
	@Test
	public void bothHtmlWritersAsWordReadsIt() throws Exception {
		for (int flag : new int[] { org.docx4j.Docx4J.FLAG_EXPORT_PREFER_XSL, org.docx4j.Docx4J.FLAG_EXPORT_PREFER_NONXSL }) {
			String u = html(load("props-u-valid.docx"), flag);
			assertFalse(flag + " U1_BARE", spanStyle(u, "U1_BARE").contains("underline"));
			assertFalse(flag + " U7_COLOR_ONLY", spanStyle(u, "U7_COLOR_ONLY").contains("underline"));
			assertTrue(flag + " U2_SINGLE", spanStyle(u, "U2_SINGLE").contains("underline"));
			String b = html(load("onoff-valid.docx"), flag);
			assertFalse(flag + " V5_OFF", spanStyle(b, "V5_OFF").contains("bold"));
			assertTrue(flag + " V4_ON", spanStyle(b, "V4_ON").contains("bold"));
			String c = html(load("colour-case.docx"), flag);
			assertTrue(flag + " K4_Red", spanStyle(c, "K4_Red").replace(" ", "").contains("color:#FF0000"));
			assertFalse(flag + " K1_auto", spanStyle(c, "K1_auto").contains("auto"));
			String h = html(load("props-enum-other.docx"), flag);
			assertFalse(flag + " N1_HIGHLIGHT_UPPER", spanStyle(h, "N1_HIGHLIGHT_UPPER").contains("null"));
		}
	}

	private static String html(WordprocessingMLPackage pkg, int flag) throws Exception {
		org.docx4j.convert.out.HTMLSettings settings = org.docx4j.Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		org.docx4j.Docx4J.toHTML(settings, out, flag);
		return out.toString("UTF-8");
	}

	/** The style attribute of the span holding the token; "" where it has none */
	private static String spanStyle(String html, String token) {
		Matcher m = Pattern.compile("<span([^>]*)>" + token + "<").matcher(html);
		assertTrue(token + " in " + html, m.find());
		Matcher style = Pattern.compile("style=\"([^\"]*)\"").matcher(m.group(1));
		return style.find() ? style.group(1) : "";
	}

	/**
	 * The markers whose save changes what Word drew (WORD_DREW): a direct highlight over a character style's is
	 * green in HTML and PDF, and an underline colour "auto" over a style's red underline is automatic.
	 */
	@Test
	public void wordsOwnSaveChangesWhatItDrew() throws Exception {
		for (String[] fm : new String[][] { { "props-enum-styles", "M6_HIGHLIGHT_GREEN" },
				{ "highlight-char-styles", "H7_CHAR_STYLE_DIRECT_GREEN" } }) {
			Probe pr = probe(fm[0]);
			Marker mk = marker(pr, fm[1]);
			assertTrue(mk + " HTML", css(pr.pkg, mk.run.getRPr()).contains("background-color:#008000"));
			assertTrue(mk + " PDF", fo(pr.pkg, mk.p, mk.run).contains("background-color=#008000"));
		}
		Probe pr = probe("colour-elsewhere-styles");
		Marker u4 = marker(pr, "U4_UNDERLINE_AUTO");
		assertEquals("auto", reading(pr.pkg, u4.p, u4.run).get("u-color"));
	}

	static Marker marker(Probe pr, String text) {
		for (Marker mk : pr.markers) {
			if (mk.text.equals(text)) return mk;
		}
		throw new AssertionError(pr.name + ": no marker " + text);
	}

	static String css(WordprocessingMLPackage pkg, RPr rPr) {
		StringBuilder sb = new StringBuilder();
		if (rPr != null) {
			for (Property p : PropertyFactory.createProperties(pkg, rPr)) {
				String c = p.getCssProperty();
				if (c != null) sb.append(c);
			}
		}
		return drawn(sb.toString(), ":", false);
	}

	/**
	 * What a browser applies to a run in the HTML output: the classes of its paragraph style's chain (Normal where it
	 * names none), then of its character style's chain (without a highlight, which HtmlCssHelper leaves out of a
	 * character style's class), then its style attribute; a later value of a property wins.
	 */
	static String htmlDrawn(WordprocessingMLPackage pkg, P p, R r) {
		Map<String, String> m = new LinkedHashMap<>();
		String pStyle = p.getPPr() != null && p.getPPr().getPStyle() != null ? p.getPPr().getPStyle().getVal() : "Normal";
		for (org.docx4j.wml.Style s : chain(pkg, pStyle)) cascade(m, cssOf(pkg, s.getRPr()));
		String rStyle = r.getRPr() != null && r.getRPr().getRStyle() != null ? r.getRPr().getRStyle().getVal() : null;
		for (org.docx4j.wml.Style s : chain(pkg, rStyle)) {
			RPr rPr = s.getRPr() == null ? null : (RPr) XmlUtils.deepCopy(s.getRPr());
			if (rPr != null) rPr.setHighlight(null);
			cascade(m, cssOf(pkg, rPr));
		}
		cascade(m, cssOf(pkg, r.getRPr()));
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : m.entrySet()) sb.append(e.getKey()).append(':').append(e.getValue()).append(';');
		return drawn(sb.toString(), ":", false);
	}

	/** A style and its w:basedOn ancestors, root first */
	private static List<org.docx4j.wml.Style> chain(WordprocessingMLPackage pkg, String styleId) {
		List<org.docx4j.wml.Style> out = new ArrayList<>();
		if (pkg.getMainDocumentPart().getStyleDefinitionsPart() == null) return out; // round 1's probes have none
		java.util.Set<String> seen = new java.util.HashSet<>();
		while (styleId != null && seen.add(styleId)) {
			org.docx4j.wml.Style s = pkg.getMainDocumentPart().getStyleDefinitionsPart().getStyleById(styleId);
			if (s == null) break;
			out.add(0, s);
			styleId = s.getBasedOn() == null ? null : s.getBasedOn().getVal();
		}
		return out;
	}

	private static String cssOf(WordprocessingMLPackage pkg, RPr rPr) {
		StringBuilder sb = new StringBuilder();
		if (rPr != null) {
			for (Property prop : PropertyFactory.createProperties(pkg, rPr)) {
				String c = prop.getCssProperty();
				if (c != null) sb.append(c);
			}
		}
		return sb.toString();
	}

	private static void cascade(Map<String, String> m, String css) {
		for (String d : css.split(";")) {
			int i = d.indexOf(':');
			if (i > 0) m.put(d.substring(0, i).trim(), d.substring(i + 1).trim());
		}
	}

	static String fo(WordprocessingMLPackage pkg, P p, R r) throws Exception {
		RPr eff = pkg.getMainDocumentPart().getPropertyResolver().getEffectiveRPr(r.getRPr(), p.getPPr());
		Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("inline");
		if (eff != null) {
			for (Property prop : PropertyFactory.createProperties(pkg, eff)) {
				prop.setXslFO(e);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < e.getAttributes().getLength(); i++) {
			sb.append(e.getAttributes().item(i).getNodeName()).append("=").append(e.getAttributes().item(i).getNodeValue()).append(";");
		}
		return drawn(sb.toString(), "=", true);
	}

	/** Properties whose value comes from the document defaults, which Word's save adds and a probe lacks */
	private static final List<String> DEFAULTS = java.util.Arrays.asList("font-size", "font-family", "language",
			"country", "lang");

	/**
	 * What CSS declarations or FO attributes draw, for comparing a probe with Word's save: an off value
	 * ({@code font-weight:normal}, {@code text-decoration:none}) is no different from none, and colours compare as
	 * upper-case hex. Anything else is compared as written, so an invalid value ({@code color:#red}) differs.
	 */
	static String drawn(String declarations, String sep, boolean fo) {
		TreeSet<String> s = new TreeSet<>();
		for (String d : declarations.split(";")) {
			int i = d.indexOf(sep);
			if (i < 0) continue;
			String k = d.substring(0, i).trim(), v = d.substring(i + 1).trim();
			if (DEFAULTS.contains(k)) continue;
			switch (k) {
			case "font-weight":
				if (v.equals("bold")) s.add(k + sep + v);
				break;
			case "font-style":
				if (!v.equals("normal")) s.add(k + sep + v);
				break;
			case "text-decoration":
			case "text-decoration-line":
				for (String t : v.split("\\s+")) {
					if (!t.equals("none") && !t.isEmpty()) s.add("text-decoration" + sep + t);
				}
				break;
			case "text-transform":
				if (!v.equals("none")) s.add(k + sep + v);
				break;
			case "font-variant":
				if (!v.equals("normal")) s.add(k + sep + v);
				break;
			case "baseline-shift": // in points, from the font size, which comes from the defaults
				s.add(k + sep + (v.startsWith("-") || v.equals("sub") ? "sub" : "super"));
				break;
			case "color":
			case "background-color":
				if (fo && k.equals("color") && v.equals("black")) break; // FO's automatic
				if (v.matches("#[0-9A-Fa-f]{6}")) s.add(k + sep + v.toUpperCase());
				else if (!v.equals("transparent")) s.add(k + sep + v);
				break;
			default:
				s.add(k + sep + v);
			}
		}
		return String.join(";", s);
	}

	/**
	 * A docx4j round trip keeps Word's reading of every marker (a save never changes meaning, CR-060 section 3.4), and
	 * writes on/off as true or false (D2), a colour name as Word writes it (CR-061, which replaced D4's "as read"), and
	 * drops the highlight, vertAlign and em elements whose value is missing or invalid, as Word's repair does (D3).
	 */
	@Test
	public void roundTripKeepsWordsReading() throws Exception {
		List<String> diffs = new ArrayList<>();
		for (String f : FILES) {
			Probe pr = probe(f);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pr.pkg.save(out);
			Probe again = new Probe(f, WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray())), pr.saved);
			for (Marker mk : again.markers) {
				compare("after a round trip", mk, reading(again.pkg, mk.p, mk.run), wordReading(pr, mk), diffs);
			}
			String xml = documentXml(out.toByteArray());
			Matcher m = Pattern.compile("<w:(" + String.join("|", ON_OFF.keySet()) + ")\\b[^>]*?w:val=\"([^\"]*)\"").matcher(xml);
			while (m.find()) {
				if (!m.group(2).equals("true") && !m.group(2).equals("false")) {
					diffs.add(f + " saved w:" + m.group(1) + " w:val=\"" + m.group(2) + "\" (D2: true or false)");
				}
			}
			Matcher bare = Pattern.compile("<w:(highlight|vertAlign|em)\\s*/>").matcher(xml);
			while (bare.find()) diffs.add(f + " saved a bare w:" + bare.group(1) + " (D3: dropped, as Word repairs)");
			Matcher hl = Pattern.compile("<w:highlight w:val=\"([^\"]*)\"").matcher(xml);
			while (hl.find()) {
				if (!HighlightValues.ALL.contains(hl.group(1))) diffs.add(f + " saved w:highlight w:val=\"" + hl.group(1) + "\" (D3)");
			}
		}
		Probe other = probe("props-enum-other");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		other.pkg.save(out);
		// CR-061: the colour name is written as Word writes it (CR-060's D4 kept it as read; the probe's label text holds
		// the words w:color w:val="red", so the run itself is looked at)
		String n6 = ColourNormalFormTest.run(documentXml(out.toByteArray()), "N6_COLOR_NAME");
		if (!n6.contains("w:color w:val=\"FF0000\"")) diffs.add("props-enum-other: N6's \"red\" not written FF0000 (CR-061): " + n6);
		assertNone(diffs);
	}

	/** ST_HighlightColor's values, for the round trip's check */
	static final class HighlightValues {
		static final List<String> ALL = java.util.Arrays.asList("black", "blue", "cyan", "green", "magenta", "red",
				"yellow", "white", "darkBlue", "darkCyan", "darkGreen", "darkMagenta", "darkRed", "darkYellow",
				"darkGray", "lightGray", "none");
	}

	static String documentXml(byte[] docx) throws Exception {
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docx))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().equals("word/document.xml")) {
					return new String(zis.readAllBytes(), "UTF-8");
				}
			}
		}
		throw new AssertionError("no word/document.xml");
	}
}
