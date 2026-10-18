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
package org.docx4j.markdown;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A chart in markdown (CR-035 phase 3): its note (CR-047), its title as a paragraph and its data as
 * a table, after the paragraph the chart is in.  The note's brackets are escaped, so that it is not
 * read as a link.  The fixtures are Word's Insert Chart pie and waterfall (docx4j-core-tests'
 * charts/).
 */
public class MarkdownChartTest {

	private static String markdown(String fixture) throws Exception {
		try (InputStream is = MarkdownChartTest.class.getClassLoader().getResourceAsStream("charts/" + fixture)) {
			return Docx4J.toMarkdown(WordprocessingMLPackage.load(is));
		}
	}

	@Test
	public void pie() throws Exception {
		String md = markdown("pie-chart.docx");
		assertTrue(md, md.contains("\\[chart: pie\\]\n\nSales\n\n||Sales|\n|---|---:|\n|1st Qtr|8.2|\n|2nd Qtr|3.2|\n|3rd Qtr|1.4|\n|4th Qtr|1.2|\n"));
	}

	/** A chartex chart: Word's picture of it stays, and its data follows from the part's caches */
	@Test
	public void waterfall() throws Exception {
		String md = markdown("waterfall.docx");
		int picture = md.indexOf("![Chart 1](data:image/png;base64,");
		int table = md.indexOf("\\[chart: waterfall\\]\n\nChart Title\n\n||Series1|\n|---|---:|\n|Category 1|100|");
		assertTrue(md, picture >= 0 && table > picture);
		assertTrue(md, md.contains("|Category 4|-40|"));
	}
}
