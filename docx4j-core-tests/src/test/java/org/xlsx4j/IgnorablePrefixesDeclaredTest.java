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
package org.xlsx4j;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * CR-024 phase 1's Excel check: a forced re-save of cr022-checkbox.xlsx,
 * cr022-slicers-timelines.xlsx and loadAndSave.xlsx was repaired by Excel 365, because
 * styles.xml, every worksheet, the tables, the slicer caches, the slicers and the
 * timelines named prefixes in mc:Ignorable (x16r2, xr3, x) that nothing declared.
 * Only the parts overriding {@code JaxbXmlPart.setMceIgnorable} (every WordprocessingML
 * root since CR-023, the workbook) handed their Ignorable list to the prefix declarator;
 * any other part declared a prefix only if its content happened to use the namespace,
 * which is why CR-022's fixtures passed.  The base class now reads the root object's
 * {@code getIgnorable()} itself.
 *
 * <p>The check is general: every XML part of each re-saved package whose root carries
 * mc:Ignorable declares each prefix it names, on the root.</p>
 */
public class IgnorablePrefixesDeclaredTest {

	@Test
	public void everyIgnorablePrefixIsDeclaredOnTheRoot() throws Exception {
		for (String file : new String[] { "cr022-checkbox.xlsx", "cr022-slicers-timelines.xlsx",
				"cr022-conditional-formatting.xlsx", "cr022-data-validation.xlsx", "cr022-sparklines.xlsx",
				"loadAndSave.xlsx", "loadAndSave.docx", "loadAndSave.pptx",
				// strict workbooks through the preprocessor: styles.xml names xr9 (CR-019 phase 3 finding)
				"anon/strict-invoice.xlsx", "anon/strict-chart.xlsx", "anon/strict-simple.xlsx" }) {
			byte[] saved = forcedResave(OpcPackage.load(ResourceUtils.getResource(file)));
			List<String> problems = new ArrayList<String>();
			int checked = 0;
			try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(saved))) {
				ZipEntry e;
				while ((e = zis.getNextEntry()) != null) {
					if (!e.getName().endsWith(".xml")) continue;
					byte[] bytes = readAll(zis);
					Element root = parse(bytes);
					String ignorable = root.getAttributeNS("http://schemas.openxmlformats.org/markup-compatibility/2006", "Ignorable");
					if (ignorable == null || ignorable.isEmpty()) continue;
					checked++;
					for (String prefix : ignorable.trim().split("\\s+")) {
						if (root.lookupNamespaceURI(prefix) == null) {
							problems.add(e.getName() + " names " + prefix + " in mc:Ignorable=\"" + ignorable + "\" and does not declare it");
						}
					}
				}
			}
			assertTrue(file + ": " + problems, problems.isEmpty());
			assertTrue(file + ": no part with mc:Ignorable was checked", checked > 0 || file.endsWith(".pptx"));
		}
	}

	/** Every JAXB part unmarshalled, then saved: each part re-marshalled from its objects. */
	static byte[] forcedResave(OpcPackage pkg) throws Exception {
		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			if (p instanceof JaxbXmlPart) {
				try {
					((JaxbXmlPart<?>) p).getContents();
				} catch (Exception e) {
					// the legacy VML drawing of a form control (xlsx4j backlog) is written back from its bytes
					assertTrue("only a VML part is expected to fail: " + p.getPartName() + ": " + e,
							p.getPartName().getName().endsWith(".vml"));
				}
			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return bos.toByteArray();
	}

	private static Element parse(byte[] xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml)).getDocumentElement();
	}

	private static byte[] readAll(java.io.InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
		return bos.toByteArray();
	}
}
