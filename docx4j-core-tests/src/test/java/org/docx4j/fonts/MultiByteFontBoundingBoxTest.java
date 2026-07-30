package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;

import org.docx4j.fonts.fop.fonts.EmbeddingMode;
import org.docx4j.fonts.fop.fonts.MultiByteFont;
import org.junit.Test;

/**
 * MultiByteFont stores its glyph bounding boxes packed as ints, rather than as a
 * Rectangle per glyph (which cost us 36 bytes a glyph, retained for as long as the
 * font was loaded).  Nothing in docx4j reads them - they are for FOP's SVG and
 * Java2D output - so these tests are what stands between the packing and a silent
 * regression.
 */
public class MultiByteFontBoundingBoxTest {

	private MultiByteFont font() {
		// not embeddable (no embed URI), so getBoundingBox indexes by glyph index
		return new MultiByteFont(null, EmbeddingMode.AUTO);
	}

	@Test
	public void packedArrayIsReadBack() {

		MultiByteFont font = font();
		font.setBBoxArray(new int[] {1, 2, 3, 4, 5, 6, 7, 8});

		assertEquals(new Rectangle(1, 2, 3, 4), font.getBoundingBox(0, 1));
		assertEquals(new Rectangle(5, 6, 7, 8), font.getBoundingBox(1, 1));
	}

	@Test
	public void rectangleArrayIsReadBack() {

		MultiByteFont font = font();
		font.setBBoxArray(new Rectangle[] {
				new Rectangle(1, 2, 3, 4),
				new Rectangle(5, 6, 7, 8)});

		assertEquals(new Rectangle(1, 2, 3, 4), font.getBoundingBox(0, 1));
		assertEquals(new Rectangle(5, 6, 7, 8), font.getBoundingBox(1, 1));
	}

	@Test
	public void boundingBoxIsScaledBySize() {

		MultiByteFont font = font();
		font.setBBoxArray(new int[] {-1, 2, 3, 4});

		assertEquals(new Rectangle(-10, 20, 30, 40), font.getBoundingBox(0, 10));
	}
}
