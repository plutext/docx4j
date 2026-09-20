/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.fonts;

import static org.docx4j.fonts.FontsTestSupport.families;
import static org.docx4j.fonts.FontsTestSupport.packageWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.fonts.fop.fonts.Typeface;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assume;
import org.junit.Test;

/**
 * A no-break space in a face which maps only the ordinary space (CR-017 phase 5).
 *
 * <p>U+00A0 and U+0020 differ in where a line may break, not in what is drawn, and
 * several faces map only the latter: Selawik and Selawik Light, Microsoft's own open
 * Segoe UI, have no U+00A0 at all.  Reading that as "no glyph" sent a paragraph whose
 * only content is a no-break space - which real documents have, as a spacer line - down
 * the no-coverage path, and the FO then carried <em>no</em> {@code font-family} for it at
 * all, so FOP drew it in its base-14 default: Helvetica, a face not embedded in the PDF.
 * Found on a corpus document set in Segoe UI Light, whose render gained an unembedded
 * Helvetica when Segoe UI Light moved from Source Sans 3 to Selawik Light.</p>
 *
 * @since 17.2.0
 */
public class NoBreakSpaceCoverageTest {

	private static final int NBSP = 0x00A0;
	private static final String DOC_FONT = "Docx4j Probe NoBreakSpace";

	@org.junit.BeforeClass
	public static void discover() throws Exception {
		PhysicalFonts.discoverPhysicalFonts(); // the map is empty until something asks
	}

	/** A face with the ordinary space and no U+00A0, or null where this machine has none. */
	private static PhysicalFont faceWithoutNoBreakSpace() throws Exception {
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (pf == null || pf.getEmbeddedURI() == null) continue;
			Typeface tf = GlyphCheck.getTypeface(pf);
			if (tf != null && raw(tf, ' ') && !raw(tf, NBSP)) return pf;
		}
		return null;
	}

	/** What the typeface itself maps, which is what GlyphCheck asked before the
	 *  no-break space was read as a space. */
	private static boolean raw(Typeface tf, int cp) {
		if (tf instanceof org.docx4j.fonts.fop.fonts.MultiByteFont) {
			return ((org.docx4j.fonts.fop.fonts.MultiByteFont) tf).hasCodePoint(cp);
		}
		return tf.hasChar((char) cp);
	}

	/** The coverage answer: a no-break space is a space. */
	@Test
	public void aFaceWithOnlyTheOrdinarySpaceCoversTheNoBreakSpace() throws Exception {

		PhysicalFont pf = faceWithoutNoBreakSpace();
		Assume.assumeTrue("every installed face maps U+00A0 on this machine", pf != null);

		assertTrue(pf.getName() + " maps U+0020 but not U+00A0, and a no-break space is a space",
				GlyphCheck.hasCodepoint(pf, NBSP));
		assertTrue(FontFallback.covers(pf, new int[] { NBSP }));
	}

	/** And so a run of only a no-break space keeps its own face in the FO, rather than
	 *  reaching FOP with no font-family at all and being drawn in its base-14. */
	@Test
	public void aRunOfOnlyANoBreakSpaceKeepsItsFont() throws Exception {

		PhysicalFont pf = faceWithoutNoBreakSpace();
		Assume.assumeTrue("every installed face maps U+00A0 on this machine", pf != null);

		WordprocessingMLPackage pkg = packageWith(null, null, null,
				"<w:p><w:r><w:rPr><w:rFonts w:ascii=\"" + DOC_FONT + "\" w:hAnsi=\"" + DOC_FONT + "\"/>"
				+ "<w:sz w:val=\"16\"/></w:rPr><w:t xml:space=\"preserve\">"
				+ String.valueOf((char) NBSP) + "</w:t></w:r></w:p>");
		pkg.getFontMapper().put(DOC_FONT, pf);

		List<String> families = families(pkg, 0);
		assertEquals("one span for the no-break space: " + families, 1, families.size());
		assertEquals("the no-break space keeps the run's own face", pf.getName(), families.get(0));
	}
}
