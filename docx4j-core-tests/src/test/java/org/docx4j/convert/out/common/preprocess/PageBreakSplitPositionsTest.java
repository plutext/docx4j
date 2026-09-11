package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.wml.Br;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * PageBreak.splitPositions replays PageBreak.updateParagraph without splitting: where
 * a paragraph with these runs would be cut (CR-012).
 */
public class PageBreakSplitPositionsTest {

	private static final ObjectFactory factory = new ObjectFactory();

	private static R text(String s) {
		R r = factory.createR();
		Text t = factory.createText();
		t.setValue(s);
		r.getContent().add(t);
		return r;
	}

	private static Object pageBreak() {
		Br br = factory.createBr();
		br.setType(STBrType.PAGE);
		return br;
	}

	private static R breakRun() {
		R r = factory.createR();
		r.getContent().add(pageBreak());
		return r;
	}

	private static List<Object> content(Object... items) {
		List<Object> l = new ArrayList<Object>();
		for (Object o : items) l.add(o);
		return l;
	}

	private static String positions(List<int[]> splits) {
		StringBuilder sb = new StringBuilder();
		for (int[] at : splits) {
			sb.append(sb.length() == 0 ? "" : " ").append(at[0]);
			if (at.length > 1) sb.append(".").append(at[1]);
		}
		return sb.toString();
	}

	@Test
	public void textThenBreakIsSplitAtTheBreak() {
		assertEquals("1.0", positions(PageBreak.splitPositions(content(text("a"), breakRun(), text("b")), false, true, false)));
	}

	@Test
	public void aLeadingBreakBecomesBreakBefore_noSplit() {
		assertEquals("", positions(PageBreak.splitPositions(content(breakRun(), text("b")), false, true, false)));
	}

	@Test
	public void everyLaterBreakSplitsToo() {
		assertEquals("1.0 3.0", positions(PageBreak.splitPositions(
				content(text("a"), breakRun(), text("b"), breakRun(), text("c")), false, true, false)));
		// the leading break is the paragraph's break-before; the next one has content before it
		assertEquals("2.0", positions(PageBreak.splitPositions(
				content(breakRun(), text("b"), breakRun(), text("c")), false, true, false)));
		// two leading breaks: the second cannot be absorbed
		assertEquals("1.0", positions(PageBreak.splitPositions(
				content(breakRun(), breakRun(), text("c")), false, true, false)));
	}

	@Test
	public void insideARun() {
		R r = factory.createR();
		Text a = factory.createText(); a.setValue("a");
		Text b = factory.createText(); b.setValue("b");
		r.getContent().add(a);
		r.getContent().add(pageBreak());
		r.getContent().add(b);
		assertEquals("0.1", positions(PageBreak.splitPositions(content(r), false, true, false)));
	}

	@Test
	public void withoutTheCompatFlagNothingIsSplit() {
		assertEquals("", positions(PageBreak.splitPositions(content(text("a"), breakRun(), text("b")), false, false, false)));
		assertEquals("", positions(PageBreak.splitPositions(content(text("a"), breakRun(), text("b")), true, false, false)));
	}

	@Test
	public void keepBreakLineSplitsABreakOnlyParagraph() {
		assertEquals("", positions(PageBreak.splitPositions(content(breakRun()), false, true, false)));
		assertEquals("0.0", positions(PageBreak.splitPositions(content(breakRun()), false, true, true)));
	}
}
