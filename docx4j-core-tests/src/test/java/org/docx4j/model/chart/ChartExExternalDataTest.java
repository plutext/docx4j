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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTChartSpace;
import org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTExternalData;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * {@code cx:externalData}'s {@code cx:autoUpdate}, the link from a chartex part to its embedded
 * workbook, is kept when a document Word or PowerPoint wrote is loaded and saved (CR-040 phase 2).
 * Office writes the attribute qualified with the chartex namespace; [MS-ODRAWXML]'s schema
 * declares it unqualified, so it was bound without a namespace, passed by on load and lost on
 * every save.
 *
 * @since 17.3.3
 */
public class ChartExExternalDataTest {

	private static final String CX = "http://schemas.microsoft.com/office/drawing/2014/chartex";

	private static OpcPackage load(String name) throws Exception {
		try (InputStream is = ChartExExternalDataTest.class.getClassLoader().getResourceAsStream("charts/" + name)) {
			return OpcPackage.load(is);
		}
	}

	/** The first chartex part's cx:externalData. */
	private static CTExternalData externalData(OpcPackage pkg) throws Exception {
		for (ChartRef c : Charts.in(pkg)) {
			if (!c.isChartEx()) continue;
			CTChartSpace space = ((ChartExSpacePart) c.getChartPart()).getContents();
			return space.getChartData().getExternalData();
		}
		return null;
	}

	private static String part(byte[] zip, String name) throws Exception {
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null;) {
				if (e.getName().equals(name)) return new String(z.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		return null;
	}

	/** The cx:externalData element of a chartex part's XML. */
	private static Element externalDataElement(String xml) throws Exception {
		org.w3c.dom.Document d = XmlUtils.getNewDocumentBuilder().parse(
				new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		return (Element) d.getElementsByTagNameNS(CX, "externalData").item(0);
	}

	private void keptOnRoundTrip(String fixture, String chartPart) throws Exception {
		OpcPackage pkg = load(fixture);
		CTExternalData ed = externalData(pkg);
		assertNotNull(fixture + ": cx:externalData", ed);
		assertEquals(fixture + ": Office's cx:autoUpdate=\"0\" read", Boolean.FALSE, ed.isAutoUpdate());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		Element saved = externalDataElement(part(out.toByteArray(), chartPart));
		assertNotNull(saved);
		String value = saved.getAttributeNS(CX, "autoUpdate");
		assertTrue(fixture + ": written in the chartex namespace, as Office writes it: " + value,
				"0".equals(value) || "false".equals(value));
		assertEquals(fixture + ": not written unqualified", "", saved.getAttribute("autoUpdate"));
		assertEquals(Boolean.FALSE, externalData(OpcPackage.load(new ByteArrayInputStream(out.toByteArray()))).isAutoUpdate());
	}

	@Test
	public void keptInAPowerPointChart() throws Exception {
		keptOnRoundTrip("waterfall.pptx", "ppt/charts/chartEx1.xml");
	}

	@Test
	public void keptInAWordChart() throws Exception {
		keptOnRoundTrip("waterfall.docx", "word/charts/chartEx1.xml");
	}

	@Test
	public void madeInCodeMarshalsAsOfficeWritesIt() throws Exception {
		CTExternalData ed = new CTExternalData();
		ed.setId("rId1");
		ed.setAutoUpdate(false);
		String xml = XmlUtils.marshaltoString(
				new jakarta.xml.bind.JAXBElement<CTExternalData>(new javax.xml.namespace.QName(CX, "externalData"),
						CTExternalData.class, ed), true, false, Context.jc);
		Element e = externalDataElement(xml);
		assertEquals("false", e.getAttributeNS(CX, "autoUpdate"));
	}
}
