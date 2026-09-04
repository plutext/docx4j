package org.docx4j.fonts.fop.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.docx4j.convert.out.fopconf.Fonts;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
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
		int fonts = 0;
		for (Fonts.Font f : fop.getRenderers().getRenderer().get(0).getFonts().getFont()) {
			fonts++;
			assertEquals("kerning not switched off for " + f.getEmbedUrl(), Boolean.FALSE, f.isKerning());
		}
		Assume.assumeTrue("no physical font found to configure", fonts > 0);
	}
}
