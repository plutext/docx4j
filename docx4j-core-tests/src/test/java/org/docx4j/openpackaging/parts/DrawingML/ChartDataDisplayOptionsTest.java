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
package org.docx4j.openpackaging.parts.DrawingML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart.CTDataDisplayOptions16;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

import jakarta.xml.bind.JAXBElement;

/**
 * Office writes {@code <c16r3:dispNaAsBlank val="1"/>} in every chart's c:extLst, val
 * unqualified; the schema text of [MS-ODRAWXML] 5.31 says attributeFormDefault="qualified",
 * and bound that way val was never read, so a typed round trip wrote the element bare and the
 * option (treat #N/A as blank) flipped to its default, false.  Found by the docx4j-python
 * session's schema refresh.  The prefix is Office's c16r3 too (it was a made-up "c173").
 */
public class ChartDataDisplayOptionsTest {

	@Test
	public void docx() throws Exception {
		check("loadAndSave.docx", "/word/charts/chart1.xml");
	}

	@Test
	public void pptx() throws Exception {
		check("loadAndSave.pptx", "/ppt/charts/chart1.xml");
	}

	@Test
	public void xlsx() throws Exception {
		check("loadAndSave.xlsx", "/xl/charts/chart1.xml");
	}

	private void check(String resource, String partName) throws Exception {
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource(resource));
		Part part = null;
		for (Part p : pkg.getParts().getParts().values()) {
			if (p.getPartName().getName().equals(partName)) {
				part = p;
			}
		}
		assertNotNull(partName, part);
		Chart chart = (Chart) part;
		CTChartSpace chartSpace = chart.getContents();

		// typed: the extension's val is read
		CTDataDisplayOptions16 options = null;
		for (CTExtension ext : chartSpace.getChart().getExtLst().getExt()) {
			for (Object o : ext.getAny()) {
				Object v = XmlUtils.unwrap(o);
				if (v instanceof CTDataDisplayOptions16) {
					options = (CTDataDisplayOptions16) v;
				}
			}
		}
		assertNotNull("c16r3:dataDisplayOptions16 typed in " + resource, options);
		assertTrue("val=\"1\" read", options.getDispNaAsBlank().isVal());

		// written back with the value and Office's prefix
		String xml = XmlUtils.marshaltoString(chartSpace, true, false, chart.getJAXBContext());
		assertTrue(xml, xml.contains("<c16r3:dispNaAsBlank val=\"true\"/>"));
		assertTrue(xml, xml.contains("xmlns:c16r3=\"http://schemas.microsoft.com/office/drawing/2017/03/chart\""));
		assertEquals(-1, xml.indexOf("c173"));
	}
}
