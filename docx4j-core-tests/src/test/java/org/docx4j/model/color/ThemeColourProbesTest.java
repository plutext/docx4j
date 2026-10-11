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
package org.docx4j.model.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.junit.Test;
import org.pptx4j.pml.Shape;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

/**
 * The resolver against Office (CR-034 phase 1): every labelled case of the phase 0 probes
 * (theme-colors/, ThemeColourProbes in the fidelity harness), each probe file loaded and each case
 * resolved through its host's {@link ThemeColors}, compared with what Word, PowerPoint or Excel drew
 * (readings.tsv, read from their PDFs by tools/office_colours.py).  Exact, except the cases CR-034
 * §11 records as one unit off and the saturation set on a grey ({@link #known}).
 */
public class ThemeColourProbesTest {

	/** Office's reading per probe and label: page, fill, stroke, text, rules */
	private static final Map<String, Map<String, String[]>> READINGS = new HashMap<>();

	static {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(resource("readings.tsv"), StandardCharsets.UTF_8))) {
			r.readLine();
			for (String line; (line = r.readLine()) != null; ) {
				String[] f = (line + "\t\t\t\t\t\t").split("\t", -1);
				READINGS.computeIfAbsent(f[0], k -> new HashMap<>()).put(f[1], new String[] { f[2], f[3], f[4], f[5], f[6] });
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saturation set on the grey bg2 (E8E8E8): Office gives a grey a hue of about 40 degrees, and a
	 * negative saturation flips it; docx4j's grey has hue 0 (CR-034 §11).
	 */
	private static boolean known(String label) {
		String note = NOTES.get("tc-p3-transforms.docx/" + label);
		return note != null && note.startsWith("bg2 sat");
	}

	private static InputStream resource(String name) {
		return ThemeColourProbesTest.class.getClassLoader().getResourceAsStream("theme-colors/" + name);
	}

	private static String expected(String probe, String label, int column) {
		String[] r = READINGS.get(probe).get(label);
		return r == null ? null : r[column];
	}

	/** exact, within one unit, further: counted per family, failures listed */
	private static final class Tally {
		final Map<String, int[]> families = new TreeMap<>();
		final List<String> off = new ArrayList<>();

		void add(String family, String label, ResolvedColor got, String expected) {
			int[] f = families.computeIfAbsent(family, k -> new int[3]);
			String want = expected.contains("/") ? expected.substring(0, expected.indexOf('/')) : expected;
			ResolvedColor w = ResolvedColor.ofHex(want);
			if (got != null && got.toHex().equals(want)) f[0]++;
			else if (got != null && got.within(w, 1)) f[1]++;
			else {
				f[2]++;
				off.add(label + " " + family + ": docx4j " + got + ", Office " + expected);
			}
			if (expected.contains("/") && got != null) {
				double a = Double.parseDouble(expected.substring(expected.indexOf('/') + 1));
				if (Math.abs(got.getAlpha() / 100000.0 - a) > 0.004) off.add(label + " alpha: docx4j " + got + ", Office " + expected);
			}
		}

		String report() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, int[]> e : families.entrySet()) {
				sb.append(String.format("  %-34s exact %3d, one unit %2d, more %2d%n", e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]));
			}
			return sb.toString();
		}

		int count(int column) {
			int n = 0;
			for (int[] f : families.values()) n += f[column];
			return n;
		}
	}

	// ------------------------------------------------------------------ the docx probes, read as XML

	private static org.w3c.dom.Document xml(String probe, String part) throws Exception {
		try (ZipInputStream z = new ZipInputStream(resource(probe))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
				if (e.getName().equals(part)) {
					DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
					f.setNamespaceAware(true);
					return f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(z.readAllBytes()));
				}
			}
		}
		throw new IllegalArgumentException(part);
	}

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
	private static final String WPS = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";

	private static Element child(Element e, String ns, String name) {
		for (Node n = e == null ? null : e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && ns.equals(n.getNamespaceURI()) && name.equals(n.getLocalName())) return (Element) n;
		}
		return null;
	}

	private static String text(Element e) {
		StringBuilder sb = new StringBuilder();
		NodeList ts = e.getElementsByTagNameNS(W, "t");
		for (int i = 0; i < ts.getLength(); i++) sb.append(ts.item(i).getTextContent());
		return sb.toString();
	}

	private static String attr(Element e, String name) {
		if (e == null) return null;
		String v = e.getAttributeNS(W, name);
		return v.isEmpty() ? null : v;
	}

	private static <T> T jaxb(Element e, Class<T> type) throws Exception {
		return type.cast(XmlUtils.unwrap(XmlUtils.unmarshalString(XmlUtils.w3CDomNodeToString(e), Context.jc, type)));
	}

	/** The wps shapes of a docx probe: label, fill or style, against Office's fill, stroke and text */
	private static void wordShapes(String probe, Tally tally) throws Exception {
		WordprocessingMLPackage pkg;
		try (InputStream is = resource(probe)) {
			pkg = WordprocessingMLPackage.load(is);
		}
		ThemeColors theme = ThemeColors.of(pkg);
		NodeList shapes = xml(probe, "word/document.xml").getElementsByTagNameNS(WPS, "wsp");
		for (int i = 0; i < shapes.getLength(); i++) {
			Element wsp = (Element) shapes.item(i);
			String label = text(wsp).trim();
			if (expected(probe, label, 1) == null) continue;
			shape(probe, label, child(child(wsp, WPS, "spPr"), A, "solidFill"), child(wsp, WPS, "style"), theme, tally);
		}
	}

	private static void shape(String probe, String label, Element solidFill, Element style, ThemeColors theme, Tally tally)
			throws Exception {
		if (solidFill != null) {
			ResolvedColor c = DrawingMLColors.resolve(jaxb(solidFill, CTSolidColorFillProperties.class), theme);
			tally.add(family(label), label, c, expected(probe, label, 1));
		} else if (style != null) {
			styled(probe, label, jaxb(style, CTShapeStyle.class), theme, tally);
		}
	}

	private static void styled(String probe, String label, CTShapeStyle s, ThemeColors theme, Tally tally) {
		StyleFill fill = DrawingMLColors.fillReference(s.getFillRef(), theme);
		if (fill.getKind() == StyleFill.Kind.SOLID) {
			tally.add("style: fill", label, fill.getColor(), expected(probe, label, 1));
		} else {
			// not solid: Office drew no single-colour fill for it either
			assertTrue(label + " " + fill, expected(probe, label, 1).isEmpty() || fill.getKind() != StyleFill.Kind.UNKNOWN);
		}
		StyleFill line = DrawingMLColors.lineReference(s.getLnRef(), theme);
		String stroke = expected(probe, label, 2);
		if (line.getKind() == StyleFill.Kind.SOLID && !stroke.isEmpty()) {
			tally.add("style: line", label, line.getColor(), stroke);
		} else if (line.getKind() == StyleFill.Kind.NONE) {
			assertEquals(label + " has no line", "", stroke);
		}
		tally.add("style: font", label, DrawingMLColors.fontReference(s.getFontRef(), theme), expected(probe, label, 3));
	}

	/** the family of a P3 label, from cases.tsv's note */
	private static final Map<String, String> NOTES = new HashMap<>();
	static {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(resource("cases.tsv"), StandardCharsets.UTF_8))) {
			r.readLine();
			for (String line; (line = r.readLine()) != null; ) {
				String[] f = line.split("\t", -1);
				NOTES.put(f[0] + "/" + f[1], f[4]);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String family(String label) {
		String note = NOTES.get("tc-p3-transforms.docx/" + label);
		if (note == null) return label.substring(0, 2);
		if (note.contains("picker")) return "picker";
		String[] w = note.split(" ");
		return w.length > 1 && w[0].matches("accent\\d|tx2|bg2") ? w[1] : w[0];
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void p3TransformsInWord() throws Exception {
		Tally t = new Tally();
		wordShapes("tc-p3-transforms.docx", t);
		check("P3, Word", t, 403);
	}

	@Test
	public void p3TransformsInPowerPoint() throws Exception {
		String probe = "tc-p3-transforms.pptx";
		PresentationMLPackage pml;
		try (InputStream is = resource(probe)) {
			pml = (PresentationMLPackage) OpcPackage.load(is);
		}
		Tally t = new Tally();
		for (Part p : pml.getParts().getParts().values()) {
			if (!(p instanceof SlidePart)) continue;
			ThemeColors theme = ThemeColors.of(p);
			for (Object o : ((SlidePart) p).getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()) {
				if (!(o instanceof Shape)) continue;
				Shape sp = (Shape) o;
				String label = label(sp);
				if (expected(probe, label, 1) == null) continue;
				if (sp.getSpPr() != null && sp.getSpPr().getSolidFill() != null) {
					t.add(family(label), label, DrawingMLColors.resolve(sp.getSpPr().getSolidFill(), theme), expected(probe, label, 1));
				} else if (sp.getStyle() != null) {
					styled(probe, label, sp.getStyle(), theme, t);
				}
			}
		}
		check("P3, PowerPoint", t, 403);
	}

	private static String label(Shape sp) {
		StringBuilder sb = new StringBuilder();
		if (sp.getTxBody() != null) {
			for (CTTextParagraph tp : sp.getTxBody().getP()) {
				for (Object r : tp.getEGTextRun()) {
					if (r instanceof CTRegularTextRun) sb.append(((CTRegularTextRun) r).getT());
				}
			}
		}
		return sb.toString().trim();
	}

	/** every case reproduced exactly or within one unit, except {@link #KNOWN} */
	private static void check(String what, Tally t, int cases) {
		System.out.println(what + ":\n" + t.report());
		List<String> unexpected = new ArrayList<>();
		for (String o : t.off) {
			if (!known(o.substring(0, o.indexOf(' ')))) unexpected.add(o);
		}
		assertTrue(what + ": " + unexpected, unexpected.isEmpty());
		assertTrue(what + " cases read: " + (t.count(0) + t.count(1) + t.count(2)), t.count(0) + t.count(1) + t.count(2) >= cases);
	}

	@Test
	public void p1Precedence() throws Exception {
		String probe = "tc-p1-precedence.docx";
		WordprocessingMLPackage pkg;
		try (InputStream is = resource(probe)) {
			pkg = WordprocessingMLPackage.load(is);
		}
		ThemeColors theme = ThemeColors.of(pkg);
		Tally t = new Tally();
		NodeList ps = xml(probe, "word/document.xml").getElementsByTagNameNS(W, "p");
		for (int i = 0; i < ps.getLength(); i++) {
			Element p = (Element) ps.item(i);
			String txt = text(p);
			if (!txt.startsWith("P1-")) continue;
			String label = txt.substring(0, 5);
			Element pPr = child(p, W, "pPr");
			Element rPr = child(child(p, W, "r"), W, "rPr");
			Element color = child(rPr, W, "color"), u = child(rPr, W, "u"), rShd = child(rPr, W, "shd"), pShd = child(pPr, W, "shd");
			Element top = child(child(pPr, W, "pBdr"), W, "top");
			if (color != null) {
				t.add("run text", label, WordColors.resolve(attr(color, "val"), attr(color, "themeColor"), attr(color, "themeTint"),
						attr(color, "themeShade"), theme), expected(probe, label, 3));
			}
			if (pShd != null && "clear".equals(attr(pShd, "val"))) {
				t.add("paragraph shading", label, WordColors.resolve(attr(pShd, "fill"), attr(pShd, "themeFill"),
						attr(pShd, "themeFillTint"), attr(pShd, "themeFillShade"), theme), expected(probe, label, 1));
			}
			if (pShd != null && "pct25".equals(attr(pShd, "val"))) {
				// the pattern's colour, then Word's 25% blend of it over the fill
				ResolvedColor pattern = WordColors.resolve(attr(pShd, "color"), attr(pShd, "themeColor"), null, null, theme);
				ResolvedColor fill = ResolvedColor.ofHex(attr(pShd, "fill"));
				ResolvedColor blend = ResolvedColor.rgb(
						(int) Math.round(0.25 * pattern.getRed() + 0.75 * fill.getRed()),
						(int) Math.round(0.25 * pattern.getGreen() + 0.75 * fill.getGreen()),
						(int) Math.round(0.25 * pattern.getBlue() + 0.75 * fill.getBlue()));
				t.add("pattern blend", label, blend, expected(probe, label, 1));
			}
			if (rShd != null) {
				t.add("run shading", label, WordColors.resolve(attr(rShd, "fill"), attr(rShd, "themeFill"), null, null, theme),
						expected(probe, label, 1));
			}
			if (top != null) {
				t.add("top border", label, WordColors.resolve(attr(top, "color"), attr(top, "themeColor"), attr(top, "themeTint"),
						attr(top, "themeShade"), theme), expected(probe, label, 4).split(" ")[0]);
			}
			if (u != null) {
				t.add("underline", label, WordColors.resolve(attr(u, "color"), attr(u, "themeColor"), null, null, theme),
						expected(probe, label, 4).split(" ")[0]);
			}
		}
		check("P1", t, 17);
		// exact but for P1-10, a paragraph shading's themeFillTint whose written value is unusable
		// (FF0000): Word drew F6C5AC, one unit below the computation that gives its three run
		// cases exactly (CR-034 §11)
		assertTrue("P1: " + t.report(), t.count(1) <= 1 && t.count(2) == 0);
	}

	@Test
	public void p2ColourMap() throws Exception {
		String probe = "tc-p2-colour-map.docx";
		WordprocessingMLPackage pkg;
		try (InputStream is = resource(probe)) {
			pkg = WordprocessingMLPackage.load(is);
		}
		ThemeColors theme = ThemeColors.of(pkg);
		Tally t = new Tally();
		NodeList ps = xml(probe, "word/document.xml").getElementsByTagNameNS(W, "p");
		for (int i = 0; i < ps.getLength(); i++) {
			Element p = (Element) ps.item(i);
			String txt = text(p);
			if (!txt.startsWith("P2-")) continue;
			String label = txt.substring(0, 5);
			Element color = child(child(child(p, W, "r"), W, "rPr"), W, "color");
			ResolvedColor c = color == null ? null : WordColors.resolve(attr(color, "val"), attr(color, "themeColor"), null, null, theme);
			t.add("run text", label, c == null ? ResolvedColor.BLACK : c, expected(probe, label, 3));   // automatic text is black
		}
		wordShapes(probe, t);
		check("P2", t, 13);
		assertEquals("every P2 case exact", 0, t.count(1) + t.count(2));
	}

	@Test
	public void p5ColourMapOverride() throws Exception {
		String probe = "tc-p5-map-override.pptx";
		PresentationMLPackage pml;
		try (InputStream is = resource(probe)) {
			pml = (PresentationMLPackage) OpcPackage.load(is);
		}
		Tally t = new Tally();
		for (Part p : pml.getParts().getParts().values()) {
			if (!(p instanceof SlidePart)) continue;
			ThemeColors theme = ThemeColors.of(p);
			for (Object o : ((SlidePart) p).getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()) {
				if (!(o instanceof Shape)) continue;
				Shape sp = (Shape) o;
				String label = label(sp);
				if (expected(probe, label, 1) == null) continue;
				t.add("fill", label, DrawingMLColors.resolve(sp.getSpPr().getSolidFill(), theme), expected(probe, label, 1));
				// the label states no colour: the master's text styles give tx1, through the slide's map
				t.add("text (tx1)", label, theme.schemeColor("tx1"), expected(probe, label, 3));
			}
		}
		check("P5", t, 20);
		assertEquals("every P5 case exact", 0, t.count(1) + t.count(2));
	}

	@Test
	public void p4Spreadsheet() throws Exception {
		Tally t = new Tally();
		for (String probe : new String[] { "tc-p4-spreadsheet.xlsx", "tc-p4-spreadsheet-palette.xlsx" }) {
			SpreadsheetMLPackage xlsx;
			try (InputStream is = resource(probe)) {
				xlsx = (SpreadsheetMLPackage) OpcPackage.load(is);
			}
			ThemeColors theme = ThemeColors.of(xlsx);
			CTStylesheet ss = xlsx.getWorkbookPart().getStylesPart().getContents();
			org.xlsx4j.sml.CTIndexedColors indexed = ss.getColors() == null ? null : ss.getColors().getIndexedColors();
			for (Part p : xlsx.getParts().getParts().values()) {
				if (!(p instanceof WorksheetPart)) continue;
				for (Row row : ((WorksheetPart) p).getContents().getSheetData().getRow()) {
					for (Cell c : row.getC()) {
						String label = c.getIs().getT().getValue();
						long fillId = ss.getCellXfs().getXf().get((int) c.getS()).getFillId();
						org.xlsx4j.sml.CTColor fg = ss.getFills().getFill().get((int) fillId).getPatternFill().getFgColor();
						String family = label.startsWith("X") ? (fg.getTint() == 0 ? "theme" : "theme + tint")
								: label.startsWith("Y") ? "indexed" : "indexed, custom palette";
						t.add(family, label, SpreadsheetColors.resolve(fg, theme, indexed), expected(probe, label, 1));
					}
				}
			}
		}
		check("P4", t, 222);
		assertEquals("every P4 case exact", 0, t.count(1) + t.count(2));
	}

	/** Word's themeTint/themeShade: the computation within one unit of Word's every distinct value in four corpora */
	@Test
	public void wordTintAndShadeAgainstTheCorpora() throws Exception {
		int exact = 0, near = 0, rows = 0;
		List<String> far = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(resource("word-tint-shade.tsv"), StandardCharsets.UTF_8))) {
			r.readLine();
			for (String line; (line = r.readLine()) != null; ) {
				String[] f = line.split("\t");
				ResolvedColor base = ResolvedColor.ofHex(f[0]);
				double v = Integer.parseInt(f[2], 16) / 255.0;
				ResolvedColor got = WordColors.luminance(base, v, "tint".equals(f[1]));
				rows++;
				if (got.toHex().equals(f[3])) exact++;
				else if (got.within(ResolvedColor.ofHex(f[3]), 1)) near++;
				else far.add(line + " -> " + got);
			}
		}
		System.out.println("Word tint/shade: " + rows + " distinct, exact " + exact + ", one unit " + near);
		assertTrue(far.toString(), far.isEmpty());
		// with the written value beside it (a current cache), Word's own value comes back exactly
		ResolvedColor accent1 = ResolvedColor.ofHex("5B9BD5");
		assertEquals("2E74B5", WordColors.resolve("2E74B5", "accent1", null, "BF",
				ThemeColors.of((org.docx4j.dml.Theme) null).withScheme(scheme("accent1", accent1), null)).toHex());
	}

	private static org.docx4j.dml.CTColorScheme scheme(String slot, ResolvedColor c) throws Exception {
		return (org.docx4j.dml.CTColorScheme) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<a:clrScheme xmlns:a=\"" + A + "\" name=\"t\"><a:dk1><a:srgbClr val=\"000000\"/></a:dk1><a:lt1><a:srgbClr val=\"FFFFFF\"/></a:lt1>"
				+ "<a:dk2><a:srgbClr val=\"44546A\"/></a:dk2><a:lt2><a:srgbClr val=\"E7E6E6\"/></a:lt2>"
				+ "<a:accent1><a:srgbClr val=\"" + c.toHex() + "\"/></a:accent1><a:accent2><a:srgbClr val=\"ED7D31\"/></a:accent2>"
				+ "<a:accent3><a:srgbClr val=\"A5A5A5\"/></a:accent3><a:accent4><a:srgbClr val=\"FFC000\"/></a:accent4>"
				+ "<a:accent5><a:srgbClr val=\"4472C4\"/></a:accent5><a:accent6><a:srgbClr val=\"70AD47\"/></a:accent6>"
				+ "<a:hlink><a:srgbClr val=\"0563C1\"/></a:hlink><a:folHlink><a:srgbClr val=\"954F72\"/></a:folHlink></a:clrScheme>",
				Context.jc, org.docx4j.dml.CTColorScheme.class));
	}
}
