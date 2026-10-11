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
package org.docx4j.openpackaging.parts.DrawingML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTChartStyle;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTColorStyle;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTChartSpace;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.junit.Test;

/**
 * The chart style, colour style and chartex parts hold their own types, and a chart style part
 * docx4j has read is written back without the empty {@code mods=""} it gained before 17.3.3
 * (CR-035 phase 0).
 *
 * <p>The style parts' {@code cs:lnRef}, {@code cs:fillRef}, {@code cs:effectRef},
 * {@code cs:fontRef} and the style entries take a {@code mods} attribute, an {@code xs:list};
 * the generated field was initialised to an empty list, so an absent attribute came back as
 * {@code mods=""} - 2,646 of them over the chart fixtures.  The generated objects now leave an
 * attribute list null until something is put in it (ModifyGeneratedSources, for all 48 such
 * fields), and a deep copy of an empty list stays null too.</p>
 */
public class ChartStylePartsTypedTest {

	private static final String[] FIXTURES = {
			"charts/bar-chart.docx", "charts/bar-chart-style-2.docx", "charts/clustered-column.docx",
			"charts/line-chart.docx", "charts/pie-chart.docx", "charts/stacked-column.docx",
			"charts/bar-chart.xlsx", "charts/bar-chart-style-2.xlsx", "charts/clustered-column.xlsx",
			"charts/line-chart.xlsx", "charts/pie-chart.xlsx", "charts/stacked-column.xlsx",
			"charts/waterfall.xlsx", "charts/waterfall.docx", "charts/waterfall.pptx", "loadAndSave.pptx" };

	private static final Pattern EMPTY_MODS = Pattern.compile("\\smods=\"\"");
	private static final Pattern MODS = Pattern.compile("\\smods=\"[^\"]+\"");

	private static byte[] bytes(String resource) throws Exception {
		try (InputStream is = ChartStylePartsTypedTest.class.getClassLoader().getResourceAsStream(resource)) {
			return is.readAllBytes();
		}
	}

	private static Map<String, String> xmlEntries(byte[] zip) throws Exception {
		Map<String, String> m = new HashMap<>();
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
				if (e.getName().endsWith(".xml")) m.put(e.getName(), new String(z.readAllBytes(), StandardCharsets.UTF_8));
			}
		}
		return m;
	}

	private static int count(Pattern p, String s) {
		int n = 0;
		for (Matcher m = p.matcher(s); m.find(); ) n++;
		return n;
	}

	@Test
	public void partsHoldTheirOwnTypes() throws Exception {

		int style = 0, colour = 0, chartex = 0;
		for (String f : FIXTURES) {
			OpcPackage pkg = OpcPackage.load(new ByteArrayInputStream(bytes(f)));
			for (Part p : pkg.getParts().getParts().values()) {
				if (p instanceof ChartStylePart) {
					assertTrue(f, ((ChartStylePart) p).getContents() instanceof CTChartStyle);
					style++;
				} else if (p instanceof ChartColorStylePart) {
					assertTrue(f, ((ChartColorStylePart) p).getContents() instanceof CTColorStyle);
					colour++;
				} else if (p instanceof ChartExSpacePart) {
					CTChartSpace cx = ((ChartExSpacePart) p).getContents();
					assertTrue(f, cx.getChart() != null && cx.getChartData() != null);
					chartex++;
				}
			}
		}
		// waterfall.xlsx holds a line chart and the waterfall, each with its style and colours;
		// waterfall.docx and .pptx a waterfall each
		assertEquals(17, style);
		assertEquals(17, colour);
		assertEquals(3, chartex);
	}

	@Test
	public void noEmptyModsAfterARoundTrip() throws Exception {
		roundTrip(false);
	}

	@Test
	public void noEmptyModsAfterADeepCopy() throws Exception {
		roundTrip(true);
	}

	/** Every JAXB part read (and, with copy, replaced by its deep copy), the package saved */
	private static void roundTrip(boolean copy) throws Exception {

		List<String> failures = new ArrayList<>();
		int styleParts = 0;
		for (String f : FIXTURES) {
			byte[] in = bytes(f);
			OpcPackage pkg = OpcPackage.load(new ByteArrayInputStream(in));
			for (Part p : pkg.getParts().getParts().values()) {
				if (p instanceof JaxbXmlPart) {
					@SuppressWarnings("unchecked")
					JaxbXmlPart<Object> jp = (JaxbXmlPart<Object>) p;
					Object o = jp.getContents();
					if (copy && o != null) jp.setContents(XmlUtils.deepCopy(o, jp.getJAXBContext()));
				}
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pkg.save(out);
			Map<String, String> before = xmlEntries(in), after = xmlEntries(out.toByteArray());
			for (Map.Entry<String, String> e : after.entrySet()) {
				if (!e.getKey().matches(".*/charts/style\\d+\\.xml")) continue;
				styleParts++;
				int empty = count(EMPTY_MODS, e.getValue());
				if (empty > 0) failures.add(f + " " + e.getKey() + ": " + empty + " mods=\"\"");
				// the mods Office wrote are kept
				assertEquals(f + " " + e.getKey(), count(MODS, before.get(e.getKey())), count(MODS, e.getValue()));
			}
		}
		assertEquals(17, styleParts);
		assertTrue(failures.toString(), failures.isEmpty());
	}
}
