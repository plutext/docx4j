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
package org.docx4j.model.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.LineSpacing;
import org.docx4j.model.properties.paragraph.SpaceAfter;
import org.docx4j.model.properties.paragraph.SpaceBefore;
import org.docx4j.model.properties.run.FontSize;
import org.docx4j.model.properties.run.Spacing;
import org.docx4j.model.properties.table.CellMarginLeft;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.TblWidth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * CR-066 D5: a CSS length that converts to a value outside Word's range for its attribute is written as computed
 * (the caller gets a document back; ImportXHTML's own CR decides whether to throw) and reported at WARN with the
 * CSS value, the converted value and the attribute's range. A length inside the range logs nothing.
 */
public class CssLengthRangeWarnTest {

	private final List<ILoggingEvent> events = new ArrayList<ILoggingEvent>();
	private ch.qos.logback.classic.Logger logger;
	private AppenderBase<ILoggingEvent> appender;
	private Level previousLevel;

	@Before
	public void captureTheLog() {
		logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Property.class);
		previousLevel = logger.getLevel();
		logger.setLevel(Level.WARN);
		appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(ILoggingEvent event) {
				events.add(event);
			}
		};
		appender.setContext(logger.getLoggerContext());
		appender.start();
		logger.addAppender(appender);
	}

	@After
	public void releaseTheLog() {
		logger.detachAppender(appender);
		appender.stop();
		logger.setLevel(previousLevel);
	}

	private List<String> warnings() {
		List<String> found = new ArrayList<String>();
		for (ILoggingEvent event : events) {
			if (event.getLevel().equals(Level.WARN) && event.getFormattedMessage().contains("CR-066")) found.add(event.getFormattedMessage());
		}
		return found;
	}

	/** A length as a CSS parser hands it over (cssparser ignores the unit asked of getFloatValue) */
	private static CSSValue length(final float value, final short unit, final String text) {
		return (CSSValue) Proxy.newProxyInstance(CSSPrimitiveValue.class.getClassLoader(), new Class<?>[] { CSSPrimitiveValue.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
						case "getCssText": return text;
						case "getCssValueType": return CSSValue.CSS_PRIMITIVE_VALUE;
						case "getPrimitiveType": return unit;
						case "getFloatValue": return value;
						case "toString": return text;
						default: throw new UnsupportedOperationException(method.getName());
					}
				});
	}

	@Test
	public void anIndentBeyondInt16IsWrittenAndWarned() {
		Indent indent = new Indent(length(5000f, CSSPrimitiveValue.CSS_IN, "5000in"));
		assertEquals("written as computed", 7200000, ((Ind) indent.getObject()).getLeft().intValue());
		assertEquals(warnings().toString(), 1, warnings().size());
		String line = warnings().get(0);
		assertTrue(line, line.startsWith("margin-left: 5000in converts to 7200000 twips, outside Word's range -32768..32767 for w:ind/@w:left"));
	}

	@Test
	public void spaceBeforeAndAfterAreUnsigned() {
		new SpaceBefore(length(45.5f, CSSPrimitiveValue.CSS_IN, "45.5in"));   // 65520: inside
		assertEquals(0, warnings().size());
		SpaceBefore before = new SpaceBefore(length(46f, CSSPrimitiveValue.CSS_IN, "46in"));  // 66240: outside
		assertEquals(66240, ((BigInteger) before.getObject()).intValue());
		assertEquals(1, warnings().size());
		assertTrue(warnings().get(0), warnings().get(0).contains("0..65535 for w:spacing/@w:before"));
		new SpaceAfter(length(-1f, CSSPrimitiveValue.CSS_PT, "-1pt"));
		assertEquals(2, warnings().size());
		assertTrue(warnings().get(1), warnings().get(1).contains("-20 twips, outside Word's range 0..65535 for w:spacing/@w:after"));
	}

	@Test
	public void lineSpacingRunSpacingCellMarginAndFontSize() {
		new LineSpacing(length(20000f, CSSPrimitiveValue.CSS_PERCENTAGE, "20000%"));  // 48000 twips
		assertTrue(warnings().toString(), warnings().get(0).contains("48000 twips, outside Word's range -32768..32767 for w:spacing/@w:line"));
		Spacing spacing = new Spacing(length(30f, CSSPrimitiveValue.CSS_IN, "30in"));  // 43200
		assertEquals(43200, ((CTSignedTwipsMeasure) spacing.getObject()).getVal().intValue());
		assertTrue(warnings().get(1), warnings().get(1).contains("w:rPr/w:spacing/@w:val"));
		CellMarginLeft margin = new CellMarginLeft(length(1000f, CSSPrimitiveValue.CSS_MM, "1000mm")); // 56736
		assertEquals(56736, ((TblWidth) margin.getObject()).getW().intValue());
		assertTrue(warnings().get(2), warnings().get(2).contains("a cell margin's w:w (dxa)"));
		FontSize size = new FontSize(length(40000f, CSSPrimitiveValue.CSS_PT, "40000pt"));  // 80000 half-points
		assertEquals(80000, ((HpsMeasure) size.getObject()).getVal().intValue());
		assertTrue(warnings().get(3), warnings().get(3).contains("80000 half-points, outside Word's range 0..65535 for w:sz/@w:val"));
		assertEquals(4, warnings().size());
		new FontSize(length(1638.5f, CSSPrimitiveValue.CSS_PT, "1638.5pt"));  // 3277: beyond the UI's limit, kept by Word
		assertEquals("inside the storage range, nothing logged", 4, warnings().size());
	}
}
