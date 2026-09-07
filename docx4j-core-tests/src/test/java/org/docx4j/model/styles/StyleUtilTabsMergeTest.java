package org.docx4j.model.styles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.Tabs;
import org.junit.Test;

/**
 * ECMA-376 &#xa7;17.3.1.38: a paragraph's custom tab stops are the union of the ones it
 * declares and the ones it inherits, and <code>w:val="clear"</code> removes the
 * inherited stop at that position.  Until 17.1.0 StyleUtil replaced the set outright.
 *
 * @since 17.1.0
 */
public class StyleUtilTabsMergeTest {

	private static Tabs tabs(Object... posThenVal) {
		Tabs t = Context.getWmlObjectFactory().createTabs();
		for (int i = 0; i < posThenVal.length; i += 2) {
			CTTabStop stop = Context.getWmlObjectFactory().createCTTabStop();
			stop.setPos(BigInteger.valueOf(((Number) posThenVal[i]).longValue()));
			stop.setVal((STTabJc) posThenVal[i + 1]);
			t.getTab().add(stop);
		}
		return t;
	}

	private static String describe(Tabs t) {
		StringBuilder sb = new StringBuilder();
		for (CTTabStop stop : t.getTab()) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(stop.getPos()).append(':').append(stop.getVal().value());
		}
		return sb.toString();
	}

	/** The style's stops survive; the paragraph's are added, in position order. */
	@Test
	public void directTabsAreAddedToTheStyles() {
		Tabs style = tabs(2880, STTabJc.LEFT, 5760, STTabJc.LEFT);
		Tabs merged = StyleUtil.apply(tabs(1440, STTabJc.LEFT), style);
		assertEquals("1440:left 2880:left 5760:left", describe(merged));
	}

	/** w:val="clear" removes the inherited stop at that position and adds nothing. */
	@Test
	public void clearRemovesTheInheritedStopAtThatPosition() {
		Tabs style = tabs(284, STTabJc.LEFT, 6804, STTabJc.LEFT, 7938, STTabJc.LEFT,
				9072, STTabJc.LEFT, 10348, STTabJc.RIGHT);
		Tabs direct = tabs(6804, STTabJc.CLEAR, 6379, STTabJc.LEFT);
		Tabs merged = StyleUtil.apply(direct, style);
		assertEquals("284:left 6379:left 7938:left 9072:left 10348:right", describe(merged));
	}

	/** A stop at a position the style already declares replaces it. */
	@Test
	public void aStopAtTheSamePositionReplacesTheInheritedOne() {
		Tabs merged = StyleUtil.apply(tabs(2880, STTabJc.RIGHT), tabs(2880, STTabJc.LEFT));
		assertEquals("2880:right", describe(merged));
	}

	/** An empty source leaves the destination alone. */
	@Test
	public void anEmptySourceChangesNothing() {
		Tabs style = tabs(2880, STTabJc.LEFT);
		assertEquals("2880:left", describe(StyleUtil.apply(
				Context.getWmlObjectFactory().createTabs(), style)));
		assertNull(StyleUtil.apply((Tabs) null, (Tabs) null));
	}
}
