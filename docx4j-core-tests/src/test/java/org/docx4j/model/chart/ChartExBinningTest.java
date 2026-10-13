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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTBinCount;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTBinSize;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTBinning;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTSeries;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.STIntervalClosedSide;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

import jakarta.xml.bind.JAXBElement;

/**
 * CR-040: a histogram's bins as Excel writes them, {@code <cx:binSize val="10"/>} and
 * {@code <cx:binCount val="6"/>}, which [MS-ODRAWXML]'s schema has as text elements: read, and
 * kept through a save.  Until 17.3.3 docx4j logged an UnmarshalException on Excel's file and saved
 * {@code <cx:binning intervalClosed="r"/>}, the bins lost.  The fixtures are two histograms Excel
 * saved, their bins set by hand (enterprise CR-004 phase 0's probes, synthetic data).  The saved
 * files are written to {@code target/cr040/} for the Office check.
 */
public class ChartExBinningTest {

	private static final String CX = "http://schemas.microsoft.com/office/drawing/2014/chartex";

	private static OpcPackage load(String name) throws Exception {
		try (InputStream is = ChartExBinningTest.class.getClassLoader().getResourceAsStream("charts/chartex/" + name)) {
			return OpcPackage.load(is);
		}
	}

	/** The histogram's (its first series') binning */
	private static CTBinning binning(OpcPackage pkg) throws Exception {
		for (ChartRef c : Charts.in(pkg)) {
			if (!c.isChartEx()) continue;
			CTSeries s = c.readChartEx().getSeries().get(0).getSource() instanceof CTSeries
					? (CTSeries) c.readChartEx().getSeries().get(0).getSource() : null;
			assertNotNull(s);
			assertEquals("clusteredColumn", s.getLayoutId().value());
			return s.getLayoutPr().getBinning();
		}
		return null;
	}

	private static byte[] save(OpcPackage pkg) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		return out.toByteArray();
	}

	private static String part(byte[] zip, String name) throws Exception {
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null;) {
				if (e.getName().equals(name)) return new String(z.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		return null;
	}

	private static void office(String name, byte[] bytes) throws Exception {
		File dir = new File("target/cr040");
		dir.mkdirs();
		java.nio.file.Files.write(new File(dir, name).toPath(), bytes);
	}

	@Test
	public void excelsBinWidthReadAndKept() throws Exception {
		OpcPackage pkg = load("manual-binwidth-10.xlsx");
		CTBinning b = binning(pkg);
		assertEquals(10, b.getBinSize().getVal(), 0);
		assertNull(b.getBinCount());
		assertEquals(STIntervalClosedSide.R, b.getIntervalClosed());

		byte[] saved = save(pkg);
		String xml = part(saved, "xl/charts/chartEx1.xml");
		assertTrue(xml, xml.matches("(?s).*<cx:binning intervalClosed=\"r\">\\s*<cx:binSize val=\"10(\\.0)?\"/>\\s*</cx:binning>.*"));
		assertEquals(10, binning(OpcPackage.load(new ByteArrayInputStream(saved))).getBinSize().getVal(), 0);
		office("manual-binwidth-10-docx4j.xlsx", saved);
	}

	@Test
	public void excelsBinCountReadAndKept() throws Exception {
		OpcPackage pkg = load("manual-bincount-6.xlsx");
		CTBinning b = binning(pkg);
		assertEquals(6, b.getBinCount().getVal());
		assertNull(b.getBinSize());

		byte[] saved = save(pkg);
		String xml = part(saved, "xl/charts/chartEx1.xml");
		assertTrue(xml, xml.matches("(?s).*<cx:binning intervalClosed=\"r\">\\s*<cx:binCount val=\"6\"/>\\s*</cx:binning>.*"));
		assertEquals(6, binning(OpcPackage.load(new ByteArrayInputStream(saved))).getBinCount().getVal());
		office("manual-bincount-6-docx4j.xlsx", saved);
	}

	@Test
	public void binsMadeInCodeAsExcelWritesThem() throws Exception {
		CTBinning b = new CTBinning();
		CTBinSize size = new CTBinSize();
		size.setVal(2.5);
		b.setBinSize(size);
		String xml = XmlUtils.marshaltoString(new JAXBElement<CTBinning>(new QName(CX, "binning"), CTBinning.class, b), true, false, Context.jc);
		assertTrue(xml, xml.contains("<cx:binSize val=\"2.5\"/>"));

		b.setBinSize(null);
		CTBinCount count = new CTBinCount();
		count.setVal(12);
		b.setBinCount(count);
		xml = XmlUtils.marshaltoString(new JAXBElement<CTBinning>(new QName(CX, "binning"), CTBinning.class, b), true, false, Context.jc);
		assertTrue(xml, xml.contains("<cx:binCount val=\"12\"/>"));
	}
}
