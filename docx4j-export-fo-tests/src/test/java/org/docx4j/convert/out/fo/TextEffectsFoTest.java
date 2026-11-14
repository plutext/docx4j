/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.model.shapes.WordArtText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RPr;
import org.junit.Test;

/**
 * A body run's text effects for the FO renderer fork (CR-083 phase 5, step 2; fop/CR-043): the room a run's line takes
 * ({@code docx4j:effect-room}), the conversion's table and painter, and the hints WordLayoutFixups turns into the
 * {@code docx4j:} attributes. The painting is chart-render's TextEffectBodyTest; Word's numbers are phase 5 findings
 * section 6.2's.
 */
public class TextEffectsFoTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static RPr rPr(String content) throws Exception {
		return (RPr) XmlUtils.unmarshalString("<w:rPr " + NS + "><w:rFonts w:ascii=\"Aptos\" w:hAnsi=\"Aptos\"/>" + content + "</w:rPr>");
	}

	private static final String GLOW5 = "<w14:glow w14:rad=\"63500\"><w14:srgbClr w14:val=\"ED7D31\"><w14:alpha w14:val=\"60000\"/>"
			+ "</w14:srgbClr></w14:glow>";

	/** A glow of 5 pt at 14 pt: 0.75 of its radius at (14 / 72)^0.7 above and below (wb81-11: 1.20 and 1.20 on Word's grid) */
	@Test
	public void aGlowsRoomIsPhase3sRule() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		RPr r = rPr("<w:sz w:val=\"28\"/>" + GLOW5);
		String room = TextEffectsFo.room(WordArtText.bodyRun(pkg, r), r);
		double g = TextEffects.GLOW_ROOM * TextEffects.scale(14) * 5;
		assertEquals(String.format(java.util.Locale.ROOT, "%.3f %.3f", g, g), room);
	}

	/** A superscript's room is its run's at the size it states, unraised (wb81-21: 1.92 above and below at 14 pt) */
	@Test
	public void aSuperscriptsRoomIsItsRunsAtItsStatedSize() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		RPr plain = rPr("<w:sz w:val=\"28\"/>" + GLOW5);
		RPr sup = rPr("<w:sz w:val=\"28\"/><w:vertAlign w:val=\"superscript\"/>" + GLOW5);
		assertEquals(TextEffectsFo.room(WordArtText.bodyRun(pkg, plain), plain), TextEffectsFo.room(WordArtText.bodyRun(pkg, sup), sup));
	}

	/** An outline or a gradient takes no room; a run with no effect is not marked */
	@Test
	public void onlyShadowsGlowsAndReflectionsTakeRoom() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		RPr outline = rPr("<w:sz w:val=\"28\"/><w14:textOutline w14:w=\"12700\" w14:cap=\"flat\" w14:cmpd=\"sng\" w14:algn=\"ctr\">"
				+ "<w14:solidFill><w14:srgbClr w14:val=\"C00000\"/></w14:solidFill><w14:prstDash w14:val=\"solid\"/><w14:round/>"
				+ "</w14:textOutline>");
		assertTrue(WordArtText.hasBodyEffect(outline));
		assertNull(TextEffectsFo.room(WordArtText.bodyRun(pkg, outline), outline));
		assertFalse(WordArtText.hasBodyEffect(rPr("<w:sz w:val=\"28\"/>")));
		// a solid opaque fill is the run's colour, not an effect; with alpha it is
		assertFalse(WordArtText.hasBodyEffect(rPr("<w14:textFill><w14:solidFill><w14:srgbClr w14:val=\"C00000\"/></w14:solidFill></w14:textFill>")));
		assertTrue(WordArtText.hasBodyEffect(rPr("<w14:textFill><w14:solidFill><w14:srgbClr w14:val=\"C00000\">"
				+ "<w14:alpha w14:val=\"40000\"/></w14:srgbClr></w14:solidFill></w14:textFill>")));
	}

	/** The conversion keeps one table, which its painter reads; none where the renderer lacks the capability */
	@Test
	public void theTableIsTheConversions() {
		FOSettings settings = new FOSettings();
		assertSame(TextEffectsFo.table(settings), TextEffectsFo.table(settings));
		assertEquals(TextEffectsFo.enabled(), TextEffectsFo.painter(settings) != null);
		assertTrue(TextEffectsFo.table(new FOSettings()) != TextEffectsFo.table(settings));
	}

	/** The hints become the docx4j: attributes the renderer reads, on a run's inline and an empty paragraph's block */
	@Test
	public void theHintsBecomeAttributes() throws Exception {
		WordLayoutOn.assume();
		String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"><fo:layout-master-set><fo:simple-page-master "
				+ "master-name=\"p\"><fo:region-body/></fo:simple-page-master></fo:layout-master-set><fo:page-sequence "
				+ "master-reference=\"p\"><fo:flow flow-name=\"xsl-region-body\"><fo:block>a <fo:inline "
				+ TextEffectsFo.HINT_TEXT_EFFECT + "=\"fx1\" " + TextEffectsFo.HINT_EFFECT_ROOM + "=\"1.191 1.191\">b</fo:inline></fo:block>"
				+ "<fo:block " + TextEffectsFo.HINT_EFFECT_ROOM + "=\"1.907 1.907\"> </fo:block></fo:flow></fo:page-sequence></fo:root>";
		String out = WordLayoutFixups.apply(fo, 15);
		assertFalse(out, out.contains(TextEffectsFo.HINT_TEXT_EFFECT));
		assertFalse(out, out.contains(TextEffectsFo.HINT_EFFECT_ROOM));
		assertTrue(out, out.contains("docx4j:text-effect=\"fx1\""));
		assertTrue(out, out.contains("docx4j:effect-room=\"1.191 1.191\""));
		assertTrue(out, out.contains("docx4j:effect-room=\"1.907 1.907\""));
	}
	/** A run formatted as its previous sibling run takes its id, Word drawing the two as one (wb80-15); another doesn't */
	@Test
	public void identicalNeighboursShareAnId() throws Exception {
		WordLayoutOn.assume();
		String run = "<fo:inline " + TextEffectsFo.HINT_TEXT_EFFECT + "=\"%s\" " + TextEffectsFo.HINT_TEXT_EFFECT_KEY + "=\"%s\">%s</fo:inline>";
		String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"><fo:layout-master-set><fo:simple-page-master "
				+ "master-name=\"p\"><fo:region-body/></fo:simple-page-master></fo:layout-master-set><fo:page-sequence "
				+ "master-reference=\"p\"><fo:flow flow-name=\"xsl-region-body\"><fo:block>"
				+ String.format(run, "fx1", "k1", "Shadow") + String.format(run, "fx2", "k1", "Next")
				+ String.format(run, "fx3", "k2", "Other") + "</fo:block></fo:flow></fo:page-sequence></fo:root>";
		String out = WordLayoutFixups.apply(fo, 15);
		assertFalse(out, out.contains(TextEffectsFo.HINT_TEXT_EFFECT_KEY));
		assertFalse(out, out.contains("docx4j:text-effect=\"fx2\""));
		assertEquals(out, 2, out.split("docx4j:text-effect=\"fx1\"", -1).length - 1);
		assertTrue(out, out.contains("docx4j:text-effect=\"fx3\""));
	}
	/** A shadow that draws nothing (inner, no blur, no distance: numbering's "no shadow") and a glow of radius 0 are no
	 *  effect: the five corpus documents whose heading numbers carry them were painted for nothing */
	@Test
	public void effectsThatDrawNothingAreNone() throws Exception {
		assertFalse(WordArtText.hasBodyEffect(rPr("<w14:shadow w14:blurRad=\"0\" w14:dist=\"0\" w14:dir=\"0\" w14:sx=\"0\" "
				+ "w14:sy=\"0\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"none\"><w14:srgbClr w14:val=\"000000\"/></w14:shadow>"
				+ "<w14:textOutline w14:w=\"0\" w14:cap=\"flat\" w14:cmpd=\"sng\" w14:algn=\"ctr\"><w14:noFill/>"
				+ "<w14:prstDash w14:val=\"solid\"/><w14:bevel/></w14:textOutline>")));
		assertFalse(WordArtText.hasBodyEffect(rPr("<w14:glow w14:rad=\"0\"><w14:srgbClr w14:val=\"ED7D31\"/></w14:glow>")));
		assertFalse(WordArtText.hasBodyEffect(rPr("<w14:reflection w14:blurRad=\"0\" w14:stA=\"0\" w14:stPos=\"0\" w14:endA=\"0\" "
				+ "w14:endPos=\"0\" w14:dist=\"0\" w14:dir=\"0\" w14:fadeDir=\"0\" w14:sx=\"0\" w14:sy=\"0\" w14:kx=\"0\" "
				+ "w14:ky=\"0\" w14:algn=\"none\"/>")));
		// gallery style 15's inner shadow has blur and distance: drawn
		assertTrue(WordArtText.hasBodyEffect(rPr("<w14:shadow w14:blurRad=\"63500\" w14:dist=\"50800\" w14:dir=\"13500000\" "
				+ "w14:sx=\"0\" w14:sy=\"0\" w14:kx=\"0\" w14:ky=\"0\" w14:algn=\"none\"><w14:srgbClr w14:val=\"000000\"/>"
				+ "</w14:shadow>")));
	}
}
