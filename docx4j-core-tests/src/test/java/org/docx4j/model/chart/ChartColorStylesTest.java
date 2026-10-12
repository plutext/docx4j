/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.chart;

import static org.junit.Assert.assertEquals;

import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ThemeColors;
import org.junit.Test;

/**
 * The colours Office shows for series and points with none of their own
 * ({@link ChartColorStyles#automatic(long, int)}), as Excel and Word drew CR-037's R14 probes in
 * the Office 2013 to 2022 theme: every channel exact.
 */
public class ChartColorStylesTest {

	private static String hex(ThemeColors theme, long idx, int count) {
		return DrawingMLColors.resolve(ChartColorStyles.automatic(idx, count), theme).toHex();
	}

	private static ThemeColors theme() throws Exception {
		return ChartsTest.refs("clustered-column.docx").get(0).getHostTheme();
	}

	@Test
	public void sixOrFewerArePlainAccents() throws Exception {
		ThemeColors t = theme();
		String[] accents = { "4472C4", "ED7D31", "A5A5A5", "FFC000", "5B9BD5" };
		for (int i = 0; i < 5; i++) assertEquals(accents[i], hex(t, i, 5));
	}

	/** r14-auto-colours: eight series, the first six shaded 76%, the next tinted 77% */
	@Test
	public void eightSeries() throws Exception {
		ThemeColors t = theme();
		String[] office = { "3B64AD", "D26E2A", "929292", "E2AA00", "5089BC", "62993E", "8FA2D4", "F1A78A" };
		for (int i = 0; i < office.length; i++) assertEquals("series " + i, office[i], hex(t, i, office.length));
	}

	/** r14-auto-pie: twelve slices, the first six shaded 65%, the next plain */
	@Test
	public void twelvePoints() throws Exception {
		ThemeColors t = theme();
		String[] office = { "375DA1", "C46627", "888888", "D39E00", "4A7FB0", "5B8E39", "4472C4", "ED7D31", "A5A5A5", "FFC000", "5B9BD5",
				"70AD47" };
		for (int i = 0; i < office.length; i++) assertEquals("point " + i, office[i], hex(t, i, office.length));
	}
}
