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
package org.docx4j.fidelity.corpus;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;

/**
 * CR-034 phase 0: the probes that measure how Office resolves colour references, so that the
 * resolver can be written to match (docs/developer/change-requests/CR-034-theme-colours-resolved.md,
 * section 5).  Unlike {@link Corpus}'s probes these are not layout probes and are not scored;
 * each case is a labelled shape, run, paragraph or cell, and Office's PDF is read for the colour
 * it drew there (tools/office_colours.py, which matches each label to the fill it sits in).
 *
 * <p>Writes into the directory given:</p>
 * <ul>
 * <li>{@code tc-p1-precedence.docx}: WordprocessingML colours whose written value disagrees with
 *     their theme reference, in runs, shading, borders and an underline;</li>
 * <li>{@code tc-p2-colour-map.docx}: {@code w:clrSchemeMapping} with the light and dark slots
 *     swapped, runs and DrawingML shapes in the mapped colours;</li>
 * <li>{@code tc-p3-transforms.docx} and {@code .pptx}: the same grid of rectangles, each base
 *     colour with one DrawingML transform at a few values, Office's picker pairs, transforms in
 *     both orders, the other colour kinds, and shapes coloured only by a style reference;</li>
 * <li>{@code tc-p4-spreadsheet.xlsx}: cells filled with theme 0 to 9 at nine tints, and indexed
 *     0 to 65; {@code tc-p4-spreadsheet-palette.xlsx}: the indexed cells against a custom
 *     {@code x:indexedColors};</li>
 * <li>{@code tc-p5-map-override.pptx}: a slide overriding the master's colour map, and a slide
 *     keeping it;</li>
 * <li>{@code cases.tsv}: every case's label and what it says.</li>
 * </ul>
 *
 * <p>Every package carries the Office 2023 theme (docx4j's bundled theme-2023.xml), so Word,
 * PowerPoint and Excel resolve against the same scheme.</p>
 *
 * <p>usage: {@code java -cp ... org.docx4j.fidelity.corpus.ThemeColourProbes <dir>}</p>
 *
 * @since 17.3.3 (CR-034 phase 0)
 */
public final class ThemeColourProbes {

	private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String NS = " xmlns:w=\"" + W_NS + "\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String PML_NS = " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"";

	/** A4 at half-inch margins */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"720\" w:right=\"720\" w:bottom=\"720\" w:left=\"720\" w:header=\"360\" w:footer=\"360\" w:gutter=\"0\"/></w:sectPr>";

	/** probe, label, kind, what is written (the colour or the attributes), note */
	private static final List<String> CASES = new ArrayList<>();

	private static void kase(String probe, String label, String kind, String written, String note) {
		CASES.add(probe + "\t" + label + "\t" + kind + "\t" + written.replace("\t", " ") + "\t" + note);
	}

	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		dir.mkdirs();
		p1(dir);
		p2(dir);
		List<String[]> grid = p3Cases();
		p3Word(dir, grid);
		p3PowerPoint(dir, grid);
		p4(dir, false);
		p4(dir, true);
		p5(dir);
		try (PrintWriter out = new PrintWriter(new File(dir, "cases.tsv"), StandardCharsets.UTF_8)) {
			out.println("probe\tlabel\tkind\twritten\tnote");
			for (String c : CASES) out.println(c);
		}
		System.out.println("wrote " + CASES.size() + " cases into " + dir);
	}

	// ------------------------------------------------------------------ the themes

	private static String theme2023() throws Exception {
		try (InputStream is = Docx4jProperties.class.getClassLoader()
				.getResourceAsStream(Docx4jProperties.DefaultTheme.THEME_2023.resource())) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static WordprocessingMLPackage docx(String body) throws Exception {
		// createPackage gives a new package the 2023 theme by default (docx4j.fonts.defaultTheme)
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getThemePart().setContents(
				(org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshalString(theme2023(), Context.jcThemePart)));
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document" + NS + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	// ------------------------------------------------------------------ P1: precedence (Word)

	/** a paragraph labelled, its label run carrying rPr; the rest of the line in black */
	private static String runCase(String label, String rPr, String text) {
		return "<w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr><w:r><w:rPr>" + rPr + "<w:sz w:val=\"24\"/></w:rPr><w:t xml:space=\"preserve\">"
				+ label + " " + text + "</w:t></w:r></w:p>";
	}

	private static void p1(File dir) throws Exception {
		String probe = "tc-p1-precedence.docx";
		StringBuilder b = new StringBuilder();
		b.append("<w:p><w:r><w:t>CR-034 P1: does Word draw the written value or the theme reference where they disagree?"
				+ " Theme: Office 2023 (accent1 156082, accent2 E97132, accent3 196B24, accent4 0F9ED5, accent5 A02B93,"
				+ " accent6 4EA72E, hlink 467886).</w:t></w:r></w:p>");
		String[][] runs = {
				{ "P1-01", "<w:color w:val=\"FF0000\" w:themeColor=\"accent1\"/>", "val FF0000, theme accent1" },
				{ "P1-02", "<w:color w:val=\"FF0000\" w:themeColor=\"accent1\" w:themeTint=\"99\"/>", "val FF0000, theme accent1, tint 99" },
				{ "P1-03", "<w:color w:val=\"FF0000\" w:themeColor=\"accent1\" w:themeShade=\"BF\"/>", "val FF0000, theme accent1, shade BF" },
				{ "P1-04", "<w:color w:val=\"auto\" w:themeColor=\"accent1\"/>", "val auto, theme accent1" },
				{ "P1-05", "<w:color w:val=\"156082\" w:themeColor=\"accent1\"/>", "val 156082, theme accent1 (they agree)" },
				{ "P1-06", "<w:color w:val=\"FF0000\" w:themeColor=\"text1\"/>", "val FF0000, theme text1" },
				{ "P1-07", "<w:color w:val=\"00FF00\" w:themeColor=\"hyperlink\"/>", "val 00FF00, theme hyperlink" },
				{ "P1-08", "<w:color w:val=\"4F81BD\" w:themeColor=\"accent1\" w:themeShade=\"BF\"/>",
						"val 4F81BD (the 2007 accent1), theme accent1, shade BF" },
		};
		for (String[] r : runs) {
			b.append(runCase(r[0], r[1], r[2]));
			kase(probe, r[0], "run text", r[1], r[2]);
		}
		String[][] shading = {
				{ "P1-09", "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FF0000\" w:themeFill=\"accent2\"/>", "fill FF0000, themeFill accent2" },
				{ "P1-10", "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FF0000\" w:themeFill=\"accent2\" w:themeFillTint=\"66\"/>",
						"fill FF0000, themeFill accent2, tint 66" },
				{ "P1-11", "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"auto\" w:themeFill=\"accent2\"/>", "fill auto, themeFill accent2" },
				{ "P1-12", "<w:shd w:val=\"pct25\" w:color=\"FF0000\" w:themeColor=\"accent5\" w:fill=\"FFFFFF\"/>",
						"25% pattern, color FF0000 themeColor accent5, fill FFFFFF" },
		};
		for (String[] s : shading) {
			b.append("<w:p><w:pPr>" + s[1] + "<w:spacing w:after=\"120\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"24\"/></w:rPr><w:t xml:space=\"preserve\">"
					+ s[0] + " " + s[2] + "</w:t></w:r></w:p>");
			kase(probe, s[0], "paragraph shading", s[1], s[2]);
		}
		String[][] borders = {
				{ "P1-13", "<w:top w:val=\"single\" w:sz=\"24\" w:space=\"1\" w:color=\"FF0000\" w:themeColor=\"accent6\"/>", "top border, color FF0000, theme accent6" },
				{ "P1-14", "<w:top w:val=\"single\" w:sz=\"24\" w:space=\"1\" w:color=\"auto\" w:themeColor=\"accent6\"/>", "top border, color auto, theme accent6" },
				{ "P1-15", "<w:top w:val=\"single\" w:sz=\"24\" w:space=\"1\" w:color=\"FF0000\" w:themeColor=\"accent6\" w:themeShade=\"80\"/>",
						"top border, color FF0000, theme accent6, shade 80" },
		};
		for (String[] s : borders) {
			// a plain paragraph first, so that no two bordered paragraphs are adjacent (Word joins
			// adjacent paragraphs with the same borders into one box, with one top border)
			b.append("<w:p><w:r><w:t>(no border)</w:t></w:r></w:p>");
			b.append("<w:p><w:pPr><w:pBdr>" + s[1] + "</w:pBdr><w:spacing w:before=\"240\" w:after=\"120\"/></w:pPr><w:r><w:rPr><w:sz w:val=\"24\"/></w:rPr>"
					+ "<w:t xml:space=\"preserve\">" + s[0] + " " + s[2] + "</w:t></w:r></w:p>");
			kase(probe, s[0], "paragraph top border", s[1], s[2]);
		}
		String u = "<w:u w:val=\"thick\" w:color=\"FF0000\" w:themeColor=\"accent4\"/>";
		b.append(runCase("P1-16", u, "thick underline, color FF0000, theme accent4"));
		kase(probe, "P1-16", "underline", u, "thick underline, color FF0000, theme accent4");
		String rshd = "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FF0000\" w:themeFill=\"accent3\"/>";
		b.append(runCase("P1-17", rshd, "run shading, fill FF0000, themeFill accent3"));
		kase(probe, "P1-17", "run shading", rshd, "run shading, fill FF0000, themeFill accent3");
		docx(b.toString()).save(new File(dir, probe));
	}

	// ------------------------------------------------------------------ DrawingML shapes in Word

	private static int shapeId = 0;

	/**
	 * A run holding an inline wps rectangle, w x h EMU, whose spPr is {@code fillAndLine} (a fill
	 * and a line, or nothing where {@code styleXml} colours it) and whose text box holds the label
	 * in 7pt, black unless {@code labelColour} is false.  A VML Fallback for Word 2007.
	 */
	private static String wordShape(String label, long cx, long cy, String fillAndLine, String styleXml, boolean labelColour) {
		int n = ++shapeId;
		String labelRun = "<w:r><w:rPr>" + (labelColour ? "<w:color w:val=\"000000\"/>" : "") + "<w:sz w:val=\"14\"/></w:rPr><w:t>" + label + "</w:t></w:r>";
		String txbx = "<w:txbxContent><w:p><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>" + labelRun + "</w:p></w:txbxContent>";
		return "<w:r><mc:AlternateContent><mc:Choice Requires=\"wps\"><w:drawing>"
				+ "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
				+ "<wp:extent cx=\"" + cx + "\" cy=\"" + cy + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ "<wp:docPr id=\"" + (100 + n) + "\" name=\"" + label + "\"/><wp:cNvGraphicFramePr/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr/>"
				+ "<wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + fillAndLine + "</wps:spPr>"
				+ (styleXml == null ? "" : styleXml)
				+ "<wps:txbx>" + txbx + "</wps:txbx>"
				+ "<wps:bodyPr rot=\"0\" vert=\"horz\" wrap=\"square\" lIns=\"45720\" tIns=\"18288\" rIns=\"45720\" bIns=\"18288\" anchor=\"ctr\" anchorCtr=\"0\"><a:noAutofit/></wps:bodyPr>"
				+ "</wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></mc:Choice>"
				+ "<mc:Fallback><w:pict><v:rect style=\"width:" + (cx / 12700.0) + "pt;height:" + (cy / 12700.0) + "pt\" fillcolor=\"#808080\" stroked=\"f\">"
				+ "<v:textbox>" + txbx + "</v:textbox></v:rect></w:pict></mc:Fallback></mc:AlternateContent></w:r>";
	}

	private static String solidFill(String colourXml) {
		return "<a:solidFill>" + colourXml + "</a:solidFill><a:ln><a:noFill/></a:ln>";
	}

	// ------------------------------------------------------------------ P2: colour map (Word)

	private static void p2(File dir) throws Exception {
		String probe = "tc-p2-colour-map.docx";
		StringBuilder b = new StringBuilder();
		b.append("<w:p><w:r><w:t>CR-034 P2: w:clrSchemeMapping swaps the light and dark slots (bg1=dark1, t1=light1,"
				+ " bg2=dark2, t2=light2).  Does it apply to WordprocessingML theme colours and DrawingML scheme colours alike?</w:t></w:r></w:p>");
		String[][] runs = {
				{ "P2-01", "<w:color w:val=\"000000\" w:themeColor=\"text1\"/>", "text1 (val 000000)" },
				{ "P2-02", "<w:color w:val=\"FFFFFF\" w:themeColor=\"background1\"/>", "background1 (val FFFFFF)" },
				{ "P2-03", "<w:color w:val=\"0E2841\" w:themeColor=\"text2\"/>", "text2 (val 0E2841)" },
				{ "P2-04", "<w:color w:val=\"E8E8E8\" w:themeColor=\"background2\"/>", "background2 (val E8E8E8)" },
				{ "P2-05", "<w:color w:val=\"000000\" w:themeColor=\"dark1\"/>", "dark1, direct" },
				{ "P2-06", "", "no colour at all (automatic)" },
		};
		for (String[] r : runs) {
			b.append(runCase(r[0], r[1], r[2]));
			kase(probe, r[0], "run text", r[1].isEmpty() ? "(none)" : r[1], r[2]);
		}
		String[][] shapes = { { "P2-07", "tx1" }, { "P2-08", "bg1" }, { "P2-09", "tx2" }, { "P2-10", "bg2" },
				{ "P2-11", "dk1" }, { "P2-12", "lt1" }, { "P2-13", "accent1" } };
		b.append("<w:p>");
		for (String[] s : shapes) {
			String c = "<a:schemeClr val=\"" + s[1] + "\"/>";
			b.append(wordShape(s[0], 1097280L, 365760L, solidFill(c) .replace("<a:ln><a:noFill/></a:ln>",
					"<a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"808080\"/></a:solidFill></a:ln>"), null, true));
			b.append("<w:r><w:t xml:space=\"preserve\"> </w:t></w:r>");
			kase(probe, s[0], "shape fill", c, "schemeClr " + s[1] + " under the swapped mapping");
		}
		b.append("</w:p>");
		WordprocessingMLPackage pkg = docx(b.toString());
		pkg.getMainDocumentPart().getDocumentSettingsPart().getContents().setClrSchemeMapping(
				(org.docx4j.wml.CTColorSchemeMapping) XmlUtils.unwrap(XmlUtils.unmarshalString(
						"<w:clrSchemeMapping xmlns:w=\"" + W_NS + "\" w:bg1=\"dark1\" w:t1=\"light1\" w:bg2=\"dark2\" w:t2=\"light2\""
						+ " w:accent1=\"accent1\" w:accent2=\"accent2\" w:accent3=\"accent3\" w:accent4=\"accent4\""
						+ " w:accent5=\"accent5\" w:accent6=\"accent6\" w:hyperlink=\"hyperlink\" w:followedHyperlink=\"followedHyperlink\"/>",
						Context.jc, org.docx4j.wml.CTColorSchemeMapping.class)));
		pkg.save(new File(dir, probe));
	}

	// ------------------------------------------------------------------ P3: transforms (Word and PowerPoint)

	/** {label, spPr fill (and line) or null, wps/p style xml or null, what is written, note, labelColour} */
	private static List<String[]> p3Cases() {
		List<String[]> c = new ArrayList<>();
		int[] n = { 0 };
		java.util.function.BiConsumer<String, String> add = (colour, note) ->
				c.add(new String[] { String.format("T%03d", ++n[0]), solidFill(colour), null, colour, note, "1" });

		String[] bases = { "accent1", "accent2", "accent4", "tx2", "bg2" };
		String[][] transforms = {
				{ "tint", "25000", "50000", "75000" }, { "shade", "25000", "50000", "75000" },
				{ "comp" }, { "inv" }, { "gray" },
				{ "alpha", "25000", "50000", "75000" }, { "alphaOff", "-25000", "-50000" }, { "alphaMod", "50000", "25000" },
				{ "hue", "0", "7200000", "14400000" }, { "hueOff", "1800000", "-1800000", "5400000" }, { "hueMod", "50000", "150000", "200000" },
				{ "sat", "0", "50000", "100000" }, { "satOff", "-50000", "-25000", "25000" }, { "satMod", "50000", "150000", "200000" },
				{ "lum", "25000", "50000", "75000" }, { "lumOff", "-25000", "20000", "40000" }, { "lumMod", "25000", "50000", "75000" },
				{ "red", "0", "50000", "100000" }, { "redOff", "-25000", "25000" }, { "redMod", "50000", "150000" },
				{ "green", "0", "50000", "100000" }, { "greenOff", "-25000", "25000" }, { "greenMod", "50000", "150000" },
				{ "blue", "0", "50000", "100000" }, { "blueOff", "-25000", "25000" }, { "blueMod", "50000", "150000" },
				{ "gamma" }, { "invGamma" } };
		for (String base : bases) {
			for (String[] t : transforms) {
				if (t.length == 1) {
					add.accept("<a:schemeClr val=\"" + base + "\"><a:" + t[0] + "/></a:schemeClr>", base + " " + t[0]);
				} else {
					for (int i = 1; i < t.length; i++) {
						add.accept("<a:schemeClr val=\"" + base + "\"><a:" + t[0] + " val=\"" + t[i] + "\"/></a:schemeClr>",
								base + " " + t[0] + " " + t[i]);
					}
				}
			}
		}
		// Office's colour picker: lighter 80%, 60%, 40%, darker 25%, 50% of each scheme slot
		String[] slots = { "bg1", "tx1", "bg2", "tx2", "accent1", "accent2", "accent3", "accent4", "accent5", "accent6" };
		String[][] picker = { { "20000", "80000" }, { "40000", "60000" }, { "60000", "40000" }, { "75000", null }, { "50000", null } };
		for (String s : slots) {
			for (String[] p : picker) {
				add.accept("<a:schemeClr val=\"" + s + "\"><a:lumMod val=\"" + p[0] + "\"/>"
						+ (p[1] == null ? "" : "<a:lumOff val=\"" + p[1] + "\"/>") + "</a:schemeClr>",
						s + " picker lumMod " + p[0] + (p[1] == null ? "" : " lumOff " + p[1]));
			}
		}
		// order: the same transforms, reversed
		String[][] orders = {
				{ "<a:tint val=\"50000\"/><a:lumMod val=\"75000\"/>", "tint 50000 then lumMod 75000" },
				{ "<a:lumMod val=\"75000\"/><a:tint val=\"50000\"/>", "lumMod 75000 then tint 50000" },
				{ "<a:shade val=\"50000\"/><a:satMod val=\"150000\"/>", "shade 50000 then satMod 150000" },
				{ "<a:satMod val=\"150000\"/><a:shade val=\"50000\"/>", "satMod 150000 then shade 50000" },
				{ "<a:lumMod val=\"50000\"/><a:lumOff val=\"25000\"/>", "lumMod 50000 then lumOff 25000" },
				{ "<a:lumOff val=\"25000\"/><a:lumMod val=\"50000\"/>", "lumOff 25000 then lumMod 50000" },
				{ "<a:tint val=\"50000\"/><a:tint val=\"50000\"/>", "tint 50000 twice" },
				{ "<a:satMod val=\"103000\"/><a:lumMod val=\"102000\"/><a:tint val=\"94000\"/>", "the 2013 theme's gradient stop: satMod 103000 lumMod 102000 tint 94000" },
				{ "<a:shade val=\"76000\"/>", "chart style: shade 76000" },
				{ "<a:tint val=\"77000\"/>", "chart style: tint 77000" },
		};
		for (String[] o : orders) {
			add.accept("<a:schemeClr val=\"accent1\">" + o[0] + "</a:schemeClr>", "accent1 " + o[1]);
		}
		// the other colour kinds
		add.accept("<a:srgbClr val=\"336699\"/>", "srgbClr 336699 (control)");
		add.accept("<a:srgbClr val=\"336699\"><a:tint val=\"50000\"/></a:srgbClr>", "srgbClr 336699 tint 50000");
		add.accept("<a:scrgbClr r=\"50000\" g=\"25000\" b=\"75000\"/>", "scrgbClr 50/25/75%");
		add.accept("<a:hslClr hue=\"14400000\" sat=\"50000\" lum=\"40000\"/>", "hslClr 240deg 50% 40%");
		add.accept("<a:prstClr val=\"coral\"/>", "prstClr coral");
		add.accept("<a:sysClr val=\"windowText\" lastClr=\"FF0000\"/>", "sysClr windowText, lastClr FF0000");
		add.accept("<a:sysClr val=\"window\" lastClr=\"00FF00\"/>", "sysClr window, lastClr 00FF00");
		add.accept("<a:schemeClr val=\"hlink\"/>", "schemeClr hlink");
		add.accept("<a:schemeClr val=\"folHlink\"/>", "schemeClr folHlink");
		// shapes coloured only by a style reference (phClr), the label coloured by fontRef
		String[][] styles = {
				{ "<a:lnRef idx=\"2\"><a:schemeClr val=\"accent1\"><a:shade val=\"50000\"/></a:schemeClr></a:lnRef>"
						+ "<a:fillRef idx=\"1\"><a:schemeClr val=\"accent1\"/></a:fillRef>"
						+ "<a:effectRef idx=\"0\"><a:schemeClr val=\"accent1\"/></a:effectRef>"
						+ "<a:fontRef idx=\"minor\"><a:schemeClr val=\"lt1\"/></a:fontRef>",
						"Word's default shape style: line accent1 shade 50000 (lnRef 2), fill accent1 (fillRef 1), text lt1" },
				{ "<a:lnRef idx=\"1\"><a:schemeClr val=\"accent6\"/></a:lnRef>"
						+ "<a:fillRef idx=\"1\"><a:schemeClr val=\"accent2\"><a:lumMod val=\"60000\"/><a:lumOff val=\"40000\"/></a:schemeClr></a:fillRef>"
						+ "<a:effectRef idx=\"0\"><a:schemeClr val=\"accent2\"/></a:effectRef>"
						+ "<a:fontRef idx=\"minor\"><a:schemeClr val=\"dk1\"/></a:fontRef>",
						"line accent6 (lnRef 1), fill accent2 lumMod 60000 lumOff 40000 (fillRef 1), text dk1" },
				{ "<a:lnRef idx=\"3\"><a:schemeClr val=\"accent4\"/></a:lnRef>"
						+ "<a:fillRef idx=\"2\"><a:schemeClr val=\"accent4\"/></a:fillRef>"
						+ "<a:effectRef idx=\"0\"><a:schemeClr val=\"accent4\"/></a:effectRef>"
						+ "<a:fontRef idx=\"minor\"><a:schemeClr val=\"lt1\"/></a:fontRef>",
						"line accent4 (lnRef 3), fill accent4 through fillRef 2 (a gradient in the 2023 theme), text lt1" },
				{ "<a:lnRef idx=\"0\"><a:schemeClr val=\"accent5\"/></a:lnRef>"
						+ "<a:fillRef idx=\"1001\"><a:schemeClr val=\"accent5\"/></a:fillRef>"
						+ "<a:effectRef idx=\"0\"><a:schemeClr val=\"accent5\"/></a:effectRef>"
						+ "<a:fontRef idx=\"minor\"><a:schemeClr val=\"lt1\"/></a:fontRef>",
						"no line (lnRef 0), fill accent5 through fillRef 1001 (bgFillStyleLst), text lt1" },
		};
		for (String[] s : styles) {
			c.add(new String[] { String.format("T%03d", ++n[0]), "", s[0], s[0], s[1], "0" });
		}
		return c;
	}

	private static void p3Word(File dir, List<String[]> grid) throws Exception {
		String probe = "tc-p3-transforms.docx";
		StringBuilder b = new StringBuilder();
		b.append("<w:p><w:r><w:t>CR-034 P3: DrawingML colours in Word, five rectangles a line.  Theme: Office 2023.</w:t></w:r></w:p>");
		for (int i = 0; i < grid.size(); i++) {
			if (i % 5 == 0) b.append(i == 0 ? "<w:p><w:pPr><w:spacing w:after=\"60\"/></w:pPr>" : "</w:p><w:p><w:pPr><w:spacing w:after=\"60\"/></w:pPr>");
			String[] c = grid.get(i);
			String style = c[2] == null ? null : "<wps:style>" + c[2] + "</wps:style>";
			b.append(wordShape(c[0], 1280160L, 320040L, c[1], style, "1".equals(c[5])));
			b.append("<w:r><w:t xml:space=\"preserve\">  </w:t></w:r>");
			kase(probe, c[0], c[2] == null ? "shape fill" : "shape style", c[3], c[4]);
		}
		b.append("</w:p>");
		docx(b.toString()).save(new File(dir, probe));
	}

	private static void p3PowerPoint(File dir, List<String[]> grid) throws Exception {
		String probe = "tc-p3-transforms.pptx";
		PresentationMLPackage pml = pptx();
		int perSlide = 150, cols = 10;
		for (int s = 0; s * perSlide < grid.size(); s++) {
			SlidePart slide = slide(pml, s + 1, null);
			for (int i = s * perSlide; i < Math.min(grid.size(), (s + 1) * perSlide); i++) {
				String[] c = grid.get(i);
				int k = i - s * perSlide;
				long x = 228600L + (k % cols) * 868680L, y = 228600L + (k / cols) * 438912L;
				slide.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(
						pptShape(i + 2, c[0], x, y, 822960L, 384048L, c[1], c[2], "1".equals(c[5])));
				kase(probe, c[0], c[2] == null ? "shape fill" : "shape style", c[3], c[4]);
			}
		}
		pml.save(new File(dir, probe));
	}

	// ------------------------------------------------------------------ PowerPoint

	private static PresentationMLPackage pptx() throws Exception {
		PresentationMLPackage pml = PresentationMLPackage.createPackage();
		ThemePart theme = pml.getMainPresentationPart().getThemePart();
		theme.setContents((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshalString(theme2023(), Context.jcThemePart)));
		return pml;
	}

	private static SlidePart slide(PresentationMLPackage pml, int n, String clrMapOvr) throws Exception {
		MainPresentationPart pp = pml.getMainPresentationPart();
		SlideLayoutPart layout = (SlideLayoutPart) pml.getParts().getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		SlidePart slide = new SlidePart(new PartName("/ppt/slides/slide" + n + ".xml"));
		slide.setContents(SlidePart.createSld());
		if (clrMapOvr != null) {
			slide.getContents().setClrMapOvr((org.docx4j.dml.CTColorMappingOverride) XmlUtils.unwrap(XmlUtils.unmarshalString(
					clrMapOvr, org.pptx4j.jaxb.Context.jcPML, org.docx4j.dml.CTColorMappingOverride.class)));
		}
		pp.addSlide(n - 1, slide);
		slide.addTargetPart(layout);
		return slide;
	}

	/** A p:sp rectangle: spPr {@code fillAndLine} (empty where {@code styleXml} colours it), the label in 7pt */
	private static Object pptShape(int id, String label, long x, long y, long cx, long cy, String fillAndLine, String styleXml,
			boolean labelColour) throws Exception {
		String xml = "<p:sp" + PML_NS + "><p:nvSpPr><p:cNvPr id=\"" + id + "\" name=\"" + label + "\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>"
				+ "<p:spPr><a:xfrm><a:off x=\"" + x + "\" y=\"" + y + "\"/><a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + fillAndLine + "</p:spPr>"
				+ (styleXml == null ? "" : "<p:style>" + styleXml + "</p:style>")
				+ "<p:txBody><a:bodyPr lIns=\"45720\" tIns=\"18288\" rIns=\"45720\" bIns=\"18288\" anchor=\"ctr\"/><a:lstStyle/>"
				+ "<a:p><a:r><a:rPr lang=\"en-US\" sz=\"700\">"
				+ (labelColour ? "<a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill>" : "")
				+ "</a:rPr><a:t>" + label + "</a:t></a:r></a:p></p:txBody></p:sp>";
		return XmlUtils.unmarshalString(xml, org.pptx4j.jaxb.Context.jcPML);
	}

	// ------------------------------------------------------------------ P5: colour map override (PowerPoint)

	private static void p5(File dir) throws Exception {
		String probe = "tc-p5-map-override.pptx";
		PresentationMLPackage pml = pptx();
		String override = "<p:clrMapOvr" + PML_NS + "><a:overrideClrMapping bg1=\"dk1\" tx1=\"lt1\" bg2=\"dk2\" tx2=\"lt2\""
				+ " accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\""
				+ " accent6=\"accent6\" hlink=\"hlink\" folHlink=\"folHlink\"/></p:clrMapOvr>";
		String keep = "<p:clrMapOvr" + PML_NS + "><a:masterClrMapping/></p:clrMapOvr>";
		String[] fills = { "tx1", "bg1", "tx2", "bg2", "accent1" };
		for (int s = 1; s <= 2; s++) {
			SlidePart slide = slide(pml, s, s == 1 ? override : keep);
			for (int i = 0; i < fills.length; i++) {
				String label = String.format("P5-%d%d", s, i + 1);
				String c = "<a:schemeClr val=\"" + fills[i] + "\"/>";
				slide.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(
						pptShape(i + 2, label, 457200L + i * 1645920L, 914400L, 1554480L, 685800L,
								solidFill(c).replace("<a:ln><a:noFill/></a:ln>", "<a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"808080\"/></a:solidFill></a:ln>"),
								null, false));
				kase(probe, label, "shape fill, label in the slide's own text colour",
						c, (s == 1 ? "slide overrides the map (bg1=dk1, tx1=lt1, bg2=dk2, tx2=lt2)" : "slide keeps the master's map")
						+ "; schemeClr " + fills[i]);
			}
		}
		pml.save(new File(dir, probe));
	}

	// ------------------------------------------------------------------ P4: SpreadsheetML (Excel)

	private static final double[] TINTS = { -0.5, -0.25, -0.1, 0, 0.2, 0.3333, 0.4, 0.6, 0.8 };

	private static String col(int i) {
		return String.valueOf((char) ('A' + i));
	}

	private static void p4(File dir, boolean palette) throws Exception {
		String probe = palette ? "tc-p4-spreadsheet-palette.xlsx" : "tc-p4-spreadsheet.xlsx";
		List<String> fills = new ArrayList<>();
		StringBuilder rows1 = new StringBuilder(), rows2 = new StringBuilder();
		int label = 0;
		if (!palette) {
			for (int theme = 0; theme <= 9; theme++) {
				rows1.append("<row r=\"" + (theme + 1) + "\">");
				for (int t = 0; t < TINTS.length; t++) {
					String fg = "<fgColor theme=\"" + theme + "\"" + (TINTS[t] == 0 ? "" : " tint=\"" + TINTS[t] + "\"") + "/>";
					fills.add(fg);
					String l = String.format("X%03d", ++label);
					rows1.append(cell(col(t) + (theme + 1), fills.size(), l));
					kase(probe, l, "cell fill", fg, "theme " + theme + " tint " + TINTS[t]);
				}
				rows1.append("</row>");
			}
		}
		for (int i = 0; i <= 65; i++) {
			int r = i / 6 + 1, c = i % 6;
			if (c == 0) rows2.append(i == 0 ? "<row r=\"1\">" : "</row><row r=\"" + r + "\">");
			String fg = "<fgColor indexed=\"" + i + "\"/>";
			fills.add(fg);
			String l = String.format("%s%03d", palette ? "Z" : "Y", i);
			rows2.append(cell(col(c) + r, fills.size(), l));
			kase(probe, l, "cell fill", fg, "indexed " + i + (palette ? " against the workbook's own x:indexedColors" : ""));
		}
		rows2.append("</row>");

		SpreadsheetMLPackage xlsx = SpreadsheetMLPackage.createPackage();
		ThemePart theme = new ThemePart(new PartName("/xl/theme/theme1.xml"));
		xlsx.getWorkbookPart().addTargetPart(theme);
		theme.setContents((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshalString(theme2023(), Context.jcThemePart)));
		Styles styles = new Styles(new PartName("/xl/styles.xml"));
		xlsx.getWorkbookPart().addTargetPart(styles);
		styles.setContents((org.xlsx4j.sml.CTStylesheet) XmlUtils.unwrap(XmlUtils.unmarshalString(stylesXml(fills, palette), org.xlsx4j.jaxb.Context.jcSML)));
		int sheetId = 1;
		if (!palette) sheet(xlsx, sheetId++, "theme and tint", rows1.toString(), 9);
		sheet(xlsx, sheetId, "indexed", rows2.toString(), 6);
		xlsx.save(new File(dir, probe));
	}

	private static String cell(String ref, int style, String label) {
		return "<c r=\"" + ref + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t>" + label + "</t></is></c>";
	}

	private static void sheet(SpreadsheetMLPackage xlsx, int id, String name, String rows, int cols) throws Exception {
		WorksheetPart ws = xlsx.createWorksheetPart(new PartName("/xl/worksheets/sheet" + id + ".xml"), name, id);
		ws.setContents((org.xlsx4j.sml.Worksheet) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
				+ "<cols><col min=\"1\" max=\"" + cols + "\" width=\"12\" customWidth=\"1\"/></cols>"
				+ "<sheetData>" + rows + "</sheetData>"
				+ "<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>"
				+ "<pageSetup orientation=\"landscape\"/></worksheet>", org.xlsx4j.jaxb.Context.jcSML)));
	}

	/** One font (11pt, black), and a solid fill and cell format per case; style 0 is Normal */
	private static String stylesXml(List<String> fills, boolean palette) {
		StringBuilder s = new StringBuilder("<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		s.append("<fonts count=\"1\"><font><sz val=\"11\"/><color rgb=\"FF000000\"/><name val=\"Aptos Narrow\"/><family val=\"2\"/></font></fonts>");
		s.append("<fills count=\"" + (fills.size() + 2) + "\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>");
		for (String fg : fills) {
			s.append("<fill><patternFill patternType=\"solid\">" + fg + "<bgColor indexed=\"64\"/></patternFill></fill>");
		}
		s.append("</fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>");
		s.append("<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>");
		s.append("<cellXfs count=\"" + (fills.size() + 1) + "\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>");
		for (int i = 0; i < fills.size(); i++) {
			s.append("<xf numFmtId=\"0\" fontId=\"0\" fillId=\"" + (i + 2) + "\" borderId=\"0\" xfId=\"0\" applyFill=\"1\"/>");
		}
		s.append("</cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>");
		if (palette) {
			// 64 entries replacing the legacy palette: a hue wheel, so no entry equals a default one
			s.append("<colors><indexedColors>");
			for (int i = 0; i < 64; i++) {
				java.awt.Color c = java.awt.Color.getHSBColor(i / 64f, 0.6f, 0.85f);
				s.append(String.format("<rgbColor rgb=\"FF%02X%02X%02X\"/>", c.getRed(), c.getGreen(), c.getBlue()));
			}
			s.append("</indexedColors></colors>");
		}
		return s.append("</styleSheet>").toString();
	}
}
