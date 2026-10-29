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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * ChartExNotice: with no chartex renderer, the first chartex chart an output draws as its Fallback is
 * one WARN naming Docx4j Enterprise ChartEx, then nothing more is read; an image asked of such a chart
 * is refused with a reason that names it; with a renderer present, whatever its check says, nothing.
 */
public class ChartExNoticeTest {

	@Before
	public void noRenderer() {
		ChartExRenderer.setRenderer(null);
		ChartExNotice.reset();
		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "auto");
	}

	@After
	public void reset() {
		ChartExRenderer.setRenderer(null);
		ChartExNotice.reset();
		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "auto");
	}

	private static OpcPackage load(String name) throws Exception {
		try (InputStream is = ChartExNoticeTest.class.getClassLoader().getResourceAsStream("charts/" + name)) {
			return OpcPackage.load(is);
		}
	}

	private static ChartRef chartEx(OpcPackage pkg) throws Exception {
		for (ChartRef c : Charts.in(pkg)) if (c.isChartEx()) return c;
		fail("no chartex chart");
		return null;
	}

	private static AlternateContent alternateContent(WordprocessingMLPackage pkg) {
		List<AlternateContent> found = new ArrayList<AlternateContent>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent) found.add((AlternateContent) o);
				return null;
			}
		}, McMode.ALL);
		assertEquals(1, found.size());
		return found.get(0);
	}

	@Test
	public void theFallbackIsNoticedOnceAndNothingIsReadAfterIt() throws Exception {
		WordprocessingMLPackage pkg = (WordprocessingMLPackage) load("waterfall.docx");
		AlternateContent ac = alternateContent(pkg);
		assertSame(ac.getFallback(), McSelection.selectedBranch(ac, pkg.getMainDocumentPart()));
		assertEquals("the first chart met: the WARN", 1, ChartExNotice.count(ChartExNotice.Situation.FALLBACK_DRAWN));
		assertSame(ac.getFallback(), McSelection.selectedBranch(ac, pkg.getMainDocumentPart()));
		assertSame(ac.getFallback(), McSelection.selectedBranch(ac, pkg.getMainDocumentPart()));
		assertEquals("after it the Choice is not even read", 1, ChartExNotice.count(ChartExNotice.Situation.FALLBACK_DRAWN));
		assertEquals(0, ChartExNotice.count(ChartExNotice.Situation.NOT_DRAWN));
	}

	@Test
	public void theHtmlOutputNoticesItToo() throws Exception {
		WordprocessingMLPackage pkg = (WordprocessingMLPackage) load("waterfall.docx");
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		Docx4J.toHTML(settings, new ByteArrayOutputStream(), Docx4J.FLAG_EXPORT_PREFER_XSL);
		assertEquals(1, ChartExNotice.count(ChartExNotice.Situation.FALLBACK_DRAWN));
	}

	@Test
	public void silentWithARendererWhateverItsCheckSays() throws Exception {
		WordprocessingMLPackage pkg = (WordprocessingMLPackage) load("waterfall.docx");
		AlternateContent ac = alternateContent(pkg);
		ChartExRenderer.setRenderer(new ChartExRendererTest.TestRenderer(false));
		assertSame("refused: the Fallback, and no notice", ac.getFallback(), McSelection.selectedBranch(ac, pkg.getMainDocumentPart()));
		ChartExRenderer.setRenderer(new ChartExRendererTest.TestRenderer(true));
		assertSame(ac.getChoice().get(0), McSelection.selectedBranch(ac, pkg.getMainDocumentPart()));
		assertEquals(0, ChartExNotice.count(ChartExNotice.Situation.FALLBACK_DRAWN));
		// a user who chose the Fallback has decided: no notice either
		ChartExRenderer.setRenderer(null);
		Docx4jProperties.setProperty(ChartExBranch.PROPERTY, "fallback");
		assertSame(ac.getFallback(), McSelection.selectedBranch(ac, pkg.getMainDocumentPart()));
		assertEquals(0, ChartExNotice.count(ChartExNotice.Situation.FALLBACK_DRAWN));
	}

	@Test
	public void anImageIsRefusedWithTheReasonNamingEnterprise() throws Exception {
		ChartRef c = chartEx(load("waterfall.xlsx"));
		try {
			ChartImages.toSvg(c, 432, 252);
			fail();
		} catch (Docx4JException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(ChartExNotice.PRODUCT));
			assertTrue(e.getMessage(), e.getMessage().contains("waterfall chart"));
			assertTrue(e.getMessage(), e.getMessage().contains(ChartExNotice.URL));
		}
		assertEquals(1, ChartExNotice.count(ChartExNotice.Situation.NOT_DRAWN));
		try {
			ChartImages.toSvg(c, 432, 252);
			fail();
		} catch (Docx4JException e) {
			assertTrue(e.getMessage().contains(ChartExNotice.PRODUCT));
		}
		assertEquals("the second is a DEBUG", 2, ChartExNotice.count(ChartExNotice.Situation.NOT_DRAWN));
		assertEquals(0, ChartExNotice.count(ChartExNotice.Situation.FALLBACK_DRAWN));
	}

	@Test
	public void theKindIsExcelsName() throws Exception {
		assertEquals("waterfall chart", ChartExNotice.kind(chartEx(load("waterfall.docx")).readChartEx()));
		assertEquals("chartex chart", ChartExNotice.kind(null));
		assertFalse(ChartExNotice.isRegionMap(chartEx(load("waterfall.docx")).readChartEx()));
	}
}
