package org.docx4j.fonts.fop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.docx4j.convert.out.fopconf.Fonts;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.RunFontSelector;
import org.junit.Assume;
import org.junit.Test;

/** Word does not kern by default; FOP does, moving line breaks (CR-001 Phase 4). */
public class FopConfigKerningTest {

	@Test
	public void kerningIsOffForEveryConfiguredFont() throws Exception {
		Mapper mapper = new IdentityPlusMapper(); // discovers the physical fonts
		Assume.assumeTrue("no physical fonts on this machine", !PhysicalFonts.getPhysicalFonts().isEmpty());
		Set<String> inUse = new HashSet<>();
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			// any two fonts, mapped under their own names
			mapper.put(pf.getName(), pf);
			inUse.add(pf.getName());
			if (inUse.size() == 2) break;
		}
		Fop fop = FopConfigUtil.createConfigurationObject(mapper, inUse);
		int fonts = 0, twins = 0;
		java.util.Set<String> names = new HashSet<>();
		for (Fonts.Font f : fop.getRenderers().getRenderer().get(0).getFonts().getFont()) {
			// the no-ligature twin has no OpenType feature at all, kerning included,
			// so it is neither a font needing a kerned twin nor a kerned twin itself
			if (f.getEncodingMode()!=null) continue;
			for (Fonts.Font.FontTriplet t : f.getFontTriplet()) names.add(t.getName());
			if (Boolean.TRUE.equals(f.isKerning())) {
				twins++;
				for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
					assertTrue("kerned twin under the plain name: " + t.getName(),
							t.getName().endsWith(RunFontSelector.KERNED_SUFFIX));
				}
			} else {
				fonts++;
				assertEquals("kerning not switched off for " + f.getEmbedUrl(), Boolean.FALSE, f.isKerning());
				for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
					assertTrue("no kerned twin for " + t.getName(), names.contains(t.getName() + RunFontSelector.KERNED_SUFFIX)
							|| twinsPending(fop, t.getName()));
				}
			}
		}
		Assume.assumeTrue("no physical font found to configure", fonts > 0);
		assertEquals("one kerned twin per font", fonts, twins);
	}

	/** the twin follows its font in the list, so it may not have been seen yet */
	private static boolean twinsPending(Fop fop, String name) {
		for (Fonts.Font f : fop.getRenderers().getRenderer().get(0).getFonts().getFont()) {
			for (Fonts.Font.FontTriplet t : f.getFontTriplet()) {
				if (t.getName().equals(name + RunFontSelector.KERNED_SUFFIX)) return true;
			}
		}
		return false;
	}

	@Test
	public void wordKernsARunWhenItsSizeReachesTheThreshold() {
		org.docx4j.wml.ObjectFactory f = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.RPr rPr = f.createRPr();
		assertFalse("no w:kern", RunFontSelector.isKerned(rPr));
		org.docx4j.wml.HpsMeasure kern = f.createHpsMeasure();
		kern.setVal(java.math.BigInteger.valueOf(28)); // 14pt and above
		rPr.setKern(kern);
		org.docx4j.wml.HpsMeasure sz = f.createHpsMeasure();
		sz.setVal(java.math.BigInteger.valueOf(24)); // 12pt
		rPr.setSz(sz);
		assertFalse("12pt is below a 14pt threshold", RunFontSelector.isKerned(rPr));
		sz.setVal(java.math.BigInteger.valueOf(28));
		assertTrue("14pt meets a 14pt threshold", RunFontSelector.isKerned(rPr));
		sz.setVal(java.math.BigInteger.valueOf(56));
		assertTrue(RunFontSelector.isKerned(rPr));
		kern.setVal(java.math.BigInteger.ZERO);
		assertFalse("w:kern 0 is off", RunFontSelector.isKerned(rPr));
	}

	@Test
	public void kernedTwinResolvesToTheSameFont() {
		Assume.assumeTrue(!PhysicalFonts.getPhysicalFonts().isEmpty());
		PhysicalFont pf = PhysicalFonts.getPhysicalFonts().values().iterator().next();
		assertEquals(pf, PhysicalFonts.get(pf.getName() + RunFontSelector.KERNED_SUFFIX));
	}
}
