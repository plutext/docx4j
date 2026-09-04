package org.docx4j.fonts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * Consolas and Lucida Console have no metric-compatible open clone, so
 * IdentityPlusMapper left them unmapped and code came out in the proportional
 * fallback font.  They now map to a monospace stand-in (Cousine, or Liberation
 * Mono, whichever the classpath has; CR-001 §6.10).
 */
public class MonospaceSubstituteTest {

	@Test
	public void consolasAndLucidaConsoleMapToAMonospaceFont() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Mapper mapper = new IdentityPlusMapper();
		pkg.setFontMapper(mapper);
		for (String docFont : new String[] { "Consolas", "Lucida Console" }) {
			if (PhysicalFonts.get(docFont) != null) continue; // installed: identity mapping, nothing to substitute
			PhysicalFont pf = mapper.get(docFont);
			assertNotNull(docFont + " left unmapped", pf);
			String name = pf.getName().toLowerCase();
			assertTrue(docFont + " mapped to " + pf.getName(), name.contains("cousine") || name.contains("liberation mono"));
		}
	}
}
