package org.docx4j.model.properties.run;

import static org.junit.Assert.assertEquals;

import org.docx4j.XmlUtils;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.STVerticalAlignRun;
import org.junit.Test;
import org.w3c.dom.Element;

/** Word draws superscripts and subscripts at 65% size, raised by 0.36 of the size or lowered by 0.16 (CR-001 Phase 4). */
public class VerticalAlignmentFOTest {

	private static Element inline(String fontSize, STVerticalAlignRun val) {
		Element el = XmlUtils.getNewDocumentBuilder().newDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
		if (fontSize != null) el.setAttribute("font-size", fontSize);
		CTVerticalAlignRun va = new CTVerticalAlignRun();
		va.setVal(val);
		new VerticalAlignment(va).setXslFO(el);
		return el;
	}

	@Test
	public void superscriptOfAn11ptRun() {
		Element el = inline("11.0pt", STVerticalAlignRun.SUPERSCRIPT);
		assertEquals("7.15pt", el.getAttribute("font-size"));
		assertEquals("3.96pt", el.getAttribute("baseline-shift"));
	}

	@Test
	public void subscriptOfA12ptRun() {
		Element el = inline("12.0pt", STVerticalAlignRun.SUBSCRIPT);
		assertEquals("7.8pt", el.getAttribute("font-size"));
		assertEquals("-1.92pt", el.getAttribute("baseline-shift"));
	}

	@Test
	public void withoutAKnownSizeTheKeywordsAreUsed() {
		Element el = inline(null, STVerticalAlignRun.SUPERSCRIPT);
		assertEquals("65%", el.getAttribute("font-size"));
		assertEquals("super", el.getAttribute("baseline-shift"));
	}

	@Test
	public void baselineLeavesTheElementAlone() {
		Element el = inline("11.0pt", STVerticalAlignRun.BASELINE);
		assertEquals("11.0pt", el.getAttribute("font-size"));
		assertEquals("", el.getAttribute("baseline-shift"));
	}
}
