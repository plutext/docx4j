package org.docx4j.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * {@code w:beforeAutospacing="0"} in direct formatting must switch off the
 * {@code w:beforeAutospacing="1"} of the style the paragraph uses (CR-001 &#xa7;3, HTML
 * auto spacing).
 *
 * <p>XJC's {@code isBeforeAutospacing()} returns a primitive {@code boolean}, which
 * reports an absent attribute and an explicit {@code "0"} alike as false, so
 * {@code StyleUtil.apply(Spacing, Spacing)} carried only true and the style's value
 * survived; {@code PropertyFactory} then gave the paragraph
 * {@code HTML_AUTO_SPACING_TWIPS} = 280tw = 14pt.  Measured on a real document whose
 * NormalWeb style carries {@code w:beforeAutospacing="1"} and which overrides it on 20
 * paragraphs: 17 came out 14pt low (Word's first divergence at y=171.4, docx4j's at
 * 186.2) and the error compounded down the page.</p>
 *
 * @since 17.0.6
 */
public class AutospacingOverrideTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A style with HTML auto spacing on both sides. */
	private static final String STYLE =
			"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"NormalWeb\">"
			+ "<w:name w:val=\"Normal (Web)\"/>"
			+ "<w:pPr><w:spacing w:before=\"100\" w:beforeAutospacing=\"1\""
			+ " w:after=\"119\" w:afterAutospacing=\"1\"/></w:pPr></w:style>";

	private static PropertyResolver resolver() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Styles styles = (Styles) pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
		styles.getStyle().add((Style)XmlUtils.unmarshalString(STYLE, Context.jc, Style.class));
		return new PropertyResolver(pkg);
	}

	private static PPr pPr(String inner) throws Exception {
		return (PPr)XmlUtils.unmarshalString("<w:pPr " + W + ">" + inner + "</w:pPr>",
				Context.jc, PPr.class);
	}

	@Test
	public void theStylesAutospacingApplies() throws Exception {
		PPr effective = resolver().getEffectivePPr(
				pPr("<w:pStyle w:val=\"NormalWeb\"/>"));
		assertNotNull(effective.getSpacing());
		assertTrue("the style's w:beforeAutospacing", effective.getSpacing().isBeforeAutospacing());
		assertTrue("the style's w:afterAutospacing", effective.getSpacing().isAfterAutospacing());
	}

	@Test
	public void directFormattingSwitchesItOff() throws Exception {
		PPr effective = resolver().getEffectivePPr(
				pPr("<w:pStyle w:val=\"NormalWeb\"/>"
					+ "<w:spacing w:before=\"0\" w:beforeAutospacing=\"0\""
					+ " w:after=\"0\" w:afterAutospacing=\"0\"/>"));
		assertNotNull(effective.getSpacing());
		assertFalse("w:beforeAutospacing=\"0\" must beat the style's \"1\"",
				effective.getSpacing().isBeforeAutospacing());
		assertFalse("w:afterAutospacing=\"0\" must beat the style's \"1\"",
				effective.getSpacing().isAfterAutospacing());
	}

	/** Direct formatting that does not mention it leaves the style's value alone: the
	 *  attribute being absent is not the same as its being false. */
	@Test
	public void directFormattingWhichIsSilentLeavesItAlone() throws Exception {
		PPr effective = resolver().getEffectivePPr(
				pPr("<w:pStyle w:val=\"NormalWeb\"/><w:spacing w:before=\"240\"/>"));
		assertNotNull(effective.getSpacing());
		assertTrue("an absent w:beforeAutospacing must not clear the style's",
				effective.getSpacing().isBeforeAutospacing());
		assertTrue("an absent w:afterAutospacing must not clear the style's",
				effective.getSpacing().isAfterAutospacing());
	}
}
