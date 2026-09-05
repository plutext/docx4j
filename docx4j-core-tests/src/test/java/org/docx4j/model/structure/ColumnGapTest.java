package org.docx4j.model.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.wml.SectPr;
import org.junit.Test;

/**
 * Where w:cols has w:col children, each column carries its own w:space and
 * w:cols/@w:space is only what Word's dialog was last given - commonly nothing
 * like the real gap.  Taking the container's value put the second column tens of
 * points from where Word has it (CR-001, real documents).
 */
public class ColumnGapTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static PageDimensions dimensions(String cols) throws Exception {
		return new PageDimensions((SectPr)XmlUtils.unmarshalString(
				"<w:sectPr " + W + "><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1134\" w:bottom=\"1440\" w:left=\"1134\"/>"
				+ cols + "</w:sectPr>"));
	}

	@Test
	public void equalColumnsUseTheContainersSpace() throws Exception {
		assertEquals(708, dimensions("<w:cols w:num=\"2\" w:space=\"708\"/>").getColsSpacing());
	}

	@Test
	public void columnChildrenCarryTheGap() throws Exception {
		PageDimensions pd = dimensions("<w:cols w:equalWidth=\"0\" w:num=\"2\" w:space=\"708\">"
				+ "<w:col w:w=\"2509\" w:space=\"1025\"/><w:col w:w=\"7216\"/></w:cols>");
		assertEquals("the first column's space, not the container's", 1025, pd.getColsSpacing());
		assertTrue(pd.hasUnequalCols());
	}

	@Test
	public void theLastColumnHasNoSpaceOfItsOwn() throws Exception {
		// three equal columns: the gap is on each column but the last
		PageDimensions pd = dimensions("<w:cols w:num=\"3\" w:space=\"708\">"
				+ "<w:col w:w=\"3000\" w:space=\"240\"/><w:col w:w=\"3000\" w:space=\"240\"/>"
				+ "<w:col w:w=\"3000\"/></w:cols>");
		assertEquals(240, pd.getColsSpacing());
		assertFalse("equal widths", pd.hasUnequalCols());
	}

	@Test
	public void noColumnsAtAllDefaultsToWordsHalfInch() throws Exception {
		assertEquals(720, dimensions("").getColsSpacing());
	}
}
