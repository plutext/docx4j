package org.docx4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * A style added to the styles part after the PropertyResolver has been constructed
 * must still be visible to style resolution (the resolver is cached on the
 * MainDocumentPart for the life of the package, so callers can't avoid this by
 * ordering their calls).  A style *modified* after construction is only visible
 * after refresh(), since resolved properties are cached.
 *
 * @since 17.0.4
 */
public class PropertyResolverLiveStylesTest {

	private static final org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

	private WordprocessingMLPackage createPkg() throws Exception {

		java.io.InputStream is = ResourceUtils.getResource("org/docx4j/model/styles-simple.xml");
		Styles styles = (Styles)XmlUtils.unmarshal(is);
		return PropertyResolverTestUtils.createdPkgWithStyles(styles);
	}

	private Style paragraphStyle(String styleId, String basedOn, Integer spacingAfter) {

		Style s = factory.createStyle();
		s.setType("paragraph");
		s.setStyleId(styleId);
		Style.Name name = factory.createStyleName();
		name.setVal(styleId);
		s.setName(name);
		if (basedOn!=null) {
			Style.BasedOn basedOnEl = factory.createStyleBasedOn();
			basedOnEl.setVal(basedOn);
			s.setBasedOn(basedOnEl);
		}
		if (spacingAfter!=null) {
			PPr pPr = factory.createPPr();
			PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
			spacing.setAfter(BigInteger.valueOf(spacingAfter));
			pPr.setSpacing(spacing);
			s.setPPr(pPr);
		}
		return s;
	}

	@Test
	public void testStyleAddedAfterResolverConstructed() throws Exception {

		WordprocessingMLPackage pkg = createPkg();

		// Construct the resolver first ...
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();

		// ... then add a style directly to the styles part
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().add(
				paragraphStyle("LateStyle", "Normal", 123));

		assertNotNull("added style not found", resolver.getStyle("LateStyle"));

		PPr effective = resolver.getEffectivePPr("LateStyle");
		assertNotNull("added style not resolved", effective);
		// 123 from the style itself; falling back to docDefaults would give 200
		assertEquals(123, effective.getSpacing().getAfter().intValue());
	}

	@Test
	public void testStyleBasedOnAnotherLateStyle() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();

		java.util.List<Style> styles
			= pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle();
		styles.add(paragraphStyle("LateBase", "Normal", 300));
		styles.add(paragraphStyle("LateDerived", "LateBase", null));

		PPr effective = resolver.getEffectivePPr("LateDerived");
		assertNotNull(effective);
		// inherited from LateBase, through the basedOn chain
		assertEquals(300, effective.getSpacing().getAfter().intValue());
	}

	@Test
	public void testLateCharacterStyleRPr() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();

		Style s = factory.createStyle();
		s.setType("character");
		s.setStyleId("LateChar");
		Style.Name name = factory.createStyleName();
		name.setVal("LateChar");
		s.setName(name);
		RPr rPr = factory.createRPr();
		HpsMeasure sz = factory.createHpsMeasure();
		sz.setVal(BigInteger.valueOf(48));
		rPr.setSz(sz);
		s.setRPr(rPr);
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().add(s);

		RPr effective = resolver.getEffectiveRPr("LateChar");
		assertNotNull("added character style not resolved", effective);
		assertEquals(48, effective.getSz().getVal().intValue());
	}

	@Test
	public void testModifiedStyleVisibleAfterRefresh() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();

		// Resolve it once, so it is cached
		assertEquals(0, resolver.getEffectivePPr("Heading1").getSpacing().getAfter().intValue());

		// Modify the style
		Style heading1 = resolver.getStyle("Heading1");
		heading1.getPPr().getSpacing().setAfter(BigInteger.valueOf(555));

		// The cached resolution is stale; that is the documented contract ...
		assertEquals(0, resolver.getEffectivePPr("Heading1").getSpacing().getAfter().intValue());

		// ... and refresh() is the way to pick the change up
		resolver.refresh();
		assertEquals(555, resolver.getEffectivePPr("Heading1").getSpacing().getAfter().intValue());
	}

}
