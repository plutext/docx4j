/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.model.properties.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.html.HtmlCssHelper;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.shapes.GradientCurve;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A body run's Word 2010 text effects as run properties (CR-083 phase 5, step 1): a solid {@code w14:textFill} as the
 * run's colour over {@code w:color}, with its alpha (wb50-01, -02); in HTML its outline, gradient, shadow and glow as
 * CSS (D23); and Word's curve for a gradient of two stops at 0 and 100% (D24). The FO routes are export-fo-tests'
 * TextFillFoTest.
 */
public class TextEffectPropertiesTest {

	static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	static final String RED = "<w14:solidFill><w14:srgbClr w14:val=\"C00000\"/></w14:solidFill>";

	static String fill(String inner) {
		return "<w14:textFill>" + inner + "</w14:textFill>";
	}

	static String alpha(String val, int alpha) {
		return "<w14:solidFill><w14:srgbClr w14:val=\"" + val + "\"><w14:alpha w14:val=\"" + alpha
				+ "\"/></w14:srgbClr></w14:solidFill>";
	}

	static String gradient(int ang, String... stops) {
		StringBuilder gs = new StringBuilder();
		for (int i = 0; i < stops.length; i += 2) {
			gs.append("<w14:gs w14:pos=\"" + stops[i] + "\"><w14:srgbClr w14:val=\"" + stops[i + 1] + "\"/></w14:gs>");
		}
		return "<w14:gradFill><w14:gsLst>" + gs + "</w14:gsLst><w14:lin w14:ang=\"" + ang * 60000
				+ "\" w14:scaled=\"0\"/></w14:gradFill>";
	}

	static final String OUTLINE = "<w14:textOutline w14:w=\"12700\" w14:cap=\"flat\" w14:cmpd=\"sng\" w14:algn=\"ctr\">"
			+ RED + "<w14:prstDash w14:val=\"solid\"/><w14:round/></w14:textOutline>";

	static final String SHADOW = "<w14:shadow w14:blurRad=\"50800\" w14:dist=\"38100\" w14:dir=\"2700000\" w14:sx=\"100000\" "
			+ "w14:sy=\"100000\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"tl\"><w14:srgbClr w14:val=\"000000\">"
			+ "<w14:alpha w14:val=\"60000\"/></w14:srgbClr></w14:shadow>";

	static final String GLOW = "<w14:glow w14:rad=\"63500\"><w14:srgbClr w14:val=\"ED7D31\"><w14:alpha w14:val=\"60000\"/>"
			+ "</w14:srgbClr></w14:glow>";

	static final String REFLECTION = "<w14:reflection w14:blurRad=\"6350\" w14:stA=\"55000\" w14:stPos=\"0\" w14:endA=\"300\" "
			+ "w14:endPos=\"45500\" w14:dist=\"0\" w14:dir=\"5400000\" w14:fadeDir=\"5400000\" w14:sx=\"100000\" "
			+ "w14:sy=\"-100000\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"bl\"/>";

	static RPr rPr(String content) throws Exception {
		return (RPr) XmlUtils.unmarshalString("<w:rPr " + NS + ">" + content + "</w:rPr>");
	}

	static final WordprocessingMLPackage PKG;
	static {
		try {
			PKG = WordprocessingMLPackage.createPackage();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/** The style attribute HTML's applyAttributes writes (the last property of each CSS name winning) */
	static String css(String rPr) throws Exception {
		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		Element span = d.createElement("span");
		HtmlCssHelper.applyAttributes(PropertyFactory.createProperties(PKG, rPr(rPr)), span);
		return span.getAttribute("style");
	}

	/** The FO colour the run's properties write, applied in order as XsltFOFunctions.createFoAttributes applies them */
	static String fo(String rPr) throws Exception {
		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		Element inline = d.createElement("inline");
		for (Property p : PropertyFactory.createProperties(PKG, rPr(rPr))) p.setXslFO(inline);
		return inline.hasAttribute("color") ? inline.getAttribute("color") : null;
	}

	// ------------------------------------------------------------------------------------------------ the curve

	@Test
	public void theCurveIsWordsSamples() {
		// wb50-13 and cr083-phase4's wv45-09, red to blue: (238, 0, 106) a quarter of the way, (187, 0, 186) halfway
		assertEquals(0xEE0069, GradientCurve.blend(0xFF0000, 0x0000FF, GradientCurve.weight(4)));
		assertEquals(0xBA00BA, GradientCurve.blend(0xFF0000, 0x0000FF, GradientCurve.weight(8)));
		assertEquals(16, GradientCurve.STEPS);
		int[] argb = GradientCurve.argb(0xFFFF0000, 0x800000FF);
		assertEquals(17, argb.length);
		assertEquals(0xFFFF0000, argb[0]);
		assertEquals(0x800000FF, argb[16]);
		assertEquals(0xC0, argb[8] >>> 24); // the alpha evenly by the curve's weight: 255 - 127 * 0.5
	}

	@Test
	public void theCurveIsForTwoStopsAtTheEndsOnly() throws Exception {
		assertTrue(GradientCurve.applies(dml(gradient(0, "0", "FF0000", "100000", "0000FF"))));
		assertTrue(GradientCurve.applies(dml(gradient(0, "100000", "FF0000", "0", "0000FF"))));
		// wg20-06: stops inside the run; wb50-15: three stops; blended evenly by Word
		assertFalse(GradientCurve.applies(dml(gradient(0, "21000", "FF0000", "88000", "0000FF"))));
		assertFalse(GradientCurve.applies(dml(gradient(0, "0", "FF0000", "50000", "FFFF00", "100000", "0000FF"))));
		assertFalse(GradientCurve.applies(dml(gradient(0, "0", "FF0000", "60000", "0000FF"))));
	}

	static CTGradientFillProperties dml(String gradFill) throws Exception {
		return (CTGradientFillProperties) TextEffects.fill(rPr(fill(gradFill)).getTextFill());
	}

	// ------------------------------------------------------------------------------------------------ the PDF's colour

	@Test
	public void aSolidFillIsTheRunsColourOverWColor() throws Exception {
		// wb50-01: w14:textFill wins over w:color
		assertEquals("#C00000", fo("<w:color w:val=\"000000\"/>" + fill(RED)));
	}

	@Test
	public void aFillsAlphaIsTheColoursAlpha() throws Exception {
		// wb50-02: w14:alpha 40000 is a transparency, drawn at 0.6 (153 of 255)
		assertEquals("#C0000099", fo("<w:color w:val=\"000000\"/>" + fill(alpha("C00000", 40000))));
		// FOP draws a colour of alpha 00 opaque: none left is written 01
		assertEquals("#C0000001", fo(fill(alpha("C00000", 100000))));
	}

	@Test
	public void aSchemeFillResolvesThroughTheTheme() throws Exception {
		assertEquals("#E97132", fo(fill("<w14:solidFill><w14:schemeClr w14:val=\"accent2\"/></w14:solidFill>")));
	}

	@Test
	public void aGradientOrNoFillKeepsWColorUntilTheForkDrawsThem() throws Exception {
		assertEquals("#2F5597", fo("<w:color w:val=\"2F5597\"/>" + fill(gradient(0, "0", "FF0000", "100000", "0000FF"))));
		assertEquals("#2F5597", fo("<w:color w:val=\"2F5597\"/>" + fill("<w14:noFill/>")));
		assertEquals("#2F5597", fo("<w:color w:val=\"2F5597\"/>" + OUTLINE + GLOW));
	}

	@Test
	public void aParagraphMarksFillColoursItsNumber() throws Exception {
		ParaRPr mark = (ParaRPr) XmlUtils.unmarshalString("<w:rPr " + NS + "><w:color w:val=\"000000\"/>" + fill(RED) + "</w:rPr>",
				org.docx4j.jaxb.Context.jc, ParaRPr.class);
		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		Element label = d.createElement("inline");
		for (Property p : PropertyFactory.createProperties(PKG, mark)) p.setXslFO(label);
		assertEquals("#C00000", label.getAttribute("color"));
	}

	// ------------------------------------------------------------------------------------------------ HTML (D23)

	@Test
	public void htmlSolidFill() throws Exception {
		String s = css("<w:color w:val=\"000000\"/>" + fill(RED));
		assertTrue(s, s.contains("color: #C00000;"));
		assertFalse(s, s.contains("#000000"));
		assertTrue(css(fill(alpha("C00000", 40000))).contains("color: rgba(192, 0, 0, 0.6);"));
	}

	@Test
	public void htmlGradientByWordsCurve() throws Exception {
		String s = css("<w:color w:val=\"000000\"/>" + fill(gradient(0, "0", "FF0000", "100000", "0000FF")));
		assertTrue(s, s.contains("-webkit-text-fill-color: transparent;"));
		assertTrue(s, s.contains("background-clip: text;"));
		// DrawingML's 0 degrees, left to right, is CSS's 90; 17 stops on Word's curve
		assertTrue(s, s.contains("linear-gradient(90deg, #FF0000 0%, "));
		assertTrue(s, s.contains("#EE0069 25%"));
		assertTrue(s, s.contains("#BA00BA 50%"));
		assertTrue(s, s.contains("#0000FF 100%)"));
		// the underline keeps w:color
		assertTrue(s, s.contains("color: #000000;"));
	}

	@Test
	public void htmlGradientOfThreeStopsEvenly() throws Exception {
		String s = css(fill(gradient(90, "0", "FF0000", "50000", "FFFF00", "100000", "0000FF")));
		assertTrue(s, s.contains("linear-gradient(180deg, #FF0000 0%, #FFFF00 50%, #0000FF 100%)"));
		s = css(fill(gradient(0, "21000", "FF0000", "88000", "0000FF")));
		assertTrue(s, s.contains("linear-gradient(90deg, #FF0000 21%, #0000FF 88%)"));
	}

	@Test
	public void htmlNoFillAndOutline() throws Exception {
		String hollow = css(fill("<w14:noFill/>") + OUTLINE);
		assertTrue(hollow, hollow.contains("-webkit-text-fill-color: transparent;"));
		assertTrue(hollow, hollow.contains("-webkit-text-stroke: 1pt #C00000;"));
		String none = css(fill("<w14:noFill/>"));
		assertTrue(none, none.contains("-webkit-text-fill-color: transparent;"));
		assertFalse(none, none.contains("-webkit-text-stroke"));
	}

	@Test
	public void htmlShadowAndGlowAtWordsDrawnLengths() throws Exception {
		// 36 pt: lengths at (36 / 72)^0.7; the shadow's 3 pt at 45 degrees, its blur 4 pt (sigma 4 / 3, CSS's radius 2
		// sigma); the glow's radius
		double s = TextEffects.scale(36);
		String d = TextEffectCss.number(3 * s * Math.cos(Math.PI / 4), 3);
		String blur = TextEffectCss.number(4 * s * 2 / 3, 3);
		String css = css("<w:sz w:val=\"72\"/>" + SHADOW + GLOW);
		assertTrue(css, css.contains("text-shadow: " + d + "pt " + d + "pt " + blur + "pt rgba(0, 0, 0, 0.4), 0 0 "
				+ TextEffectCss.number(5 * s, 3) + "pt rgba(237, 125, 49, 0.4);"));
		// no size stated: in em, at Word's scale for 11 pt
		css = css(GLOW);
		assertTrue(css, css.contains("text-shadow: 0 0 " + TextEffectCss.number(5 * TextEffects.scale(11) / 11, 4) + "em "));
	}

	@Test
	public void htmlReflectionIsReportedNotDrawn() throws Exception {
		String s = css(REFLECTION);
		assertFalse(s, s.contains("text-shadow"));
	}

	// ------------------------------------------------------------------------------------------------ both HTML routes

	static WordprocessingMLPackage doc() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Style> styles = mdp.getStyleDefinitionsPart().getContents().getStyle();
		styles.add((Style) XmlUtils.unmarshalString("<w:style " + NS + " w:type=\"character\" w:styleId=\"Effect\">"
				+ "<w:name w:val=\"Effect\"/><w:rPr>" + GLOW + fill(gradient(0, "0", "FF0000", "100000", "0000FF"))
				+ "</w:rPr></w:style>"));
		mdp.getContent().add(p("<w:r><w:rPr><w:color w:val=\"000000\"/>" + OUTLINE + fill(RED) + "</w:rPr><w:t>FILLED</w:t></w:r>"));
		mdp.getContent().add(p("<w:r><w:rPr><w:rStyle w:val=\"Effect\"/></w:rPr><w:t>STYLED</w:t></w:r>"));
		return pkg;
	}

	static P p(String content) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p " + NS + ">" + content + "</w:p>");
	}

	@Test
	public void bothHtmlRoutesWriteTheEffects() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(doc());
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, os, flags);
			String html = os.toString("UTF-8");
			int at = html.indexOf(">FILLED<");
			assertTrue(pathway + "FILLED", at > 0);
			String span = html.substring(html.lastIndexOf("<span", at), at);
			assertTrue(pathway + span, span.contains("color: #C00000;"));
			assertTrue(pathway + span, span.contains("-webkit-text-stroke: 1pt #C00000;"));
			// the character style's class
			int rule = html.indexOf("span.Effect");
			assertTrue(pathway + "the style's rule", rule >= 0);
			String effect = html.substring(rule, html.indexOf('}', rule));
			assertTrue(pathway + effect, effect.contains("text-shadow: 0 0 "));
			assertTrue(pathway + effect, effect.contains("linear-gradient(90deg, #FF0000 0%"));
			assertNotNull(effect);
		}
	}
}
