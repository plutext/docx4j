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
package org.docx4j.model.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Two defects the Enterprise ChartEx work found (2026-10-28): a chartex chart in an xlsx had no size,
 * because its reference's anchor was the Choice's frame and not the cell anchor round it; and reading a
 * document with a chartex chart through Charts.in warned once per chart about the chart element's parent,
 * which is its GraphicData by design.
 */
public class ChartExDefectsTest {

	private final List<ILoggingEvent> events = new ArrayList<ILoggingEvent>();
	private ch.qos.logback.classic.Logger logger;
	private Level previous;
	private AppenderBase<ILoggingEvent> appender;

	@Before
	public void captureTraversalLog() {
		appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(ILoggingEvent event) {
				events.add(event);
			}
		};
		logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TraversalUtil.class);
		previous = logger.getLevel();
		logger.setLevel(Level.DEBUG);
		appender.setContext(logger.getLoggerContext());
		appender.start();
		logger.addAppender(appender);
		ChartExRenderer.setRenderer(null);
	}

	@After
	public void releaseTheLog() {
		logger.detachAppender(appender);
		logger.setLevel(previous);
		appender.stop();
		ChartExRenderer.setRenderer(null);
	}

	private static OpcPackage load(String name) throws Exception {
		try (InputStream is = ChartExDefectsTest.class.getClassLoader().getResourceAsStream("charts/" + name)) {
			return OpcPackage.load(is);
		}
	}

	private static ChartRef chartEx(OpcPackage pkg) throws Exception {
		for (ChartRef c : Charts.in(pkg)) if (c.isChartEx()) return c;
		fail("no chartex chart");
		return null;
	}

	@Test
	public void anXlsxChartexChartHasItsCellAnchorsSize() throws Exception {
		ChartRef c = chartEx(load("waterfall.xlsx"));
		assertTrue("the anchor is the cell anchor round the Choice", XmlUtils.unwrap(c.getAnchor()) instanceof CTTwoCellAnchor);
		assertNotNull("ChartAnchorSize reads it", ChartImages.getSize(c));
		assertTrue(ChartImages.getSize(c).getWidth() > 0 && ChartImages.getSize(c).getHeight() > 0);
		ChartExRenderer.setRenderer(new ChartExRendererTest.TestRenderer(true));
		assertNotNull("drawn at the anchor's size, none given", ChartImages.toSvg(c));
	}

	@Test
	public void readingAChartexDocumentWarnsNothingAboutTheChartElementsParent() throws Exception {
		for (String name : new String[] { "waterfall.docx", "waterfall.pptx", "waterfall.xlsx" }) {
			events.clear();
			assertNotNull(chartEx(load(name)));
			for (ILoggingEvent e : events) {
				if (e.getLevel().isGreaterOrEqual(Level.WARN)) fail(name + ": " + e.getFormattedMessage());
			}
		}
		// the document walked too, as an output walks it (the WARN came once per chart from any walk)
		OpcPackage pkg = load("waterfall.docx");
		events.clear();
		TraversalUtil.visit((org.docx4j.openpackaging.packages.WordprocessingMLPackage) pkg, false, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				return null;
			}
		});
		int warns = 0;
		for (ILoggingEvent e : events) if (e.getLevel().isGreaterOrEqual(Level.WARN)) warns++;
		assertEquals("no WARN about a parent", 0, warns);
	}
}
