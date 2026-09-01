package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * Phase 5: the Docx4J facade hooks (reflection into this module) work.
 */
public class Docx4JFacadeTest {

	@Test
	public void facadeRoundTrip() throws Exception {
		WordprocessingMLPackage pkg = Docx4J.fromMarkdown("# Title\n\nSome **bold** text.\n");
		assertEquals("# Title\n\nSome **bold** text.\n", Docx4J.toMarkdown(pkg));
	}

}
