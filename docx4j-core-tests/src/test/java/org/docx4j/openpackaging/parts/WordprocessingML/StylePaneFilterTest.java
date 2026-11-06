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
package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.sharedtypes.STOnOff;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.CTStylePaneFilter;
import org.junit.Test;

/**
 * {@code w:stylePaneFormatFilter}'s fifteen attributes, kept on a save (CR-078), in the spelling
 * the file gave them.  Nothing keeps them in step with {@code w:val}.
 */
public class StylePaneFilterTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** Word's files: all sixteen stand in the saved part as they stood in the source. */
	@Test public void keptOnASave() throws Exception {
		assertKept(bytes("NumberingImplicitNumId.docx"));
		assertKept(bytes("headers_footers/same_as_previous_numbering.docx"));
		File toc = new File("../docx4j-samples-docx4j/sample-docs/toc.docx");
		if (!toc.exists()) toc = new File("docx4j-samples-docx4j/sample-docs/toc.docx");
		assertKept(Files.readAllBytes(toc.toPath()));
	}

	private static void assertKept(byte[] docx) throws Exception {
		Map<String, String> source = attributes(settings(docx));
		assertEquals(source.toString(), 16, source.size());
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
		assertNotNull(pkg.getMainDocumentPart().getDocumentSettingsPart().getContents().getStylePaneFormatFilter());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		assertEquals(source, attributes(settings(out.toByteArray())));
	}

	/** Every spelling of ST_OnOff stands as it was given. */
	@Test public void theSpellings() throws Exception {
		String xml = marshalled("w:allStyles=\"on\" w:customStyles=\"off\" w:latentStyles=\"true\" w:stylesInUse=\"false\""
				+ " w:headingStyles=\"1\" w:numberingStyles=\"0\"");
		for (String kept : new String[] { "w:allStyles=\"on\"", "w:customStyles=\"off\"", "w:latentStyles=\"true\"",
				"w:stylesInUse=\"false\"", "w:headingStyles=\"1\"", "w:numberingStyles=\"0\"" }) {
			assertTrue(kept + " in " + xml, xml.contains(kept));
		}
	}

	@Test public void valAlone() throws Exception {
		CTStylePaneFilter filter = filter("w:val=\"3F01\"");
		assertEquals("3F01", filter.getVal());
		assertNull(filter.getAllStyles());
	}

	@Test public void attributesAlone() throws Exception {
		CTStylePaneFilter filter = filter("w:allStyles=\"1\"");
		assertNull(filter.getVal());
		assertEquals(STOnOff.ONE, filter.getAllStyles());
	}

	/** Nothing keeps the two in step: a save writes what it was given. */
	@Test public void theyMayDisagree() throws Exception {
		String xml = marshalled("w:val=\"0001\" w:allStyles=\"0\"");
		assertTrue(xml, xml.contains("w:val=\"0001\"") && xml.contains("w:allStyles=\"0\""));
	}

	/** A value ST_OnOff doesn't hold goes, and the part still reads. */
	@Test public void aValueOutsideTheType() throws Exception {
		CTStylePaneFilter filter = filter("w:val=\"3F01\" w:allStyles=\"maybe\" w:customStyles=\"1\"");
		assertEquals("3F01", filter.getVal());
		assertNull(filter.getAllStyles());
		assertEquals(STOnOff.ONE, filter.getCustomStyles());
	}

	private static CTStylePaneFilter filter(String attributes) throws Exception {
		return part(attributes).getContents().getStylePaneFormatFilter();
	}

	private static String marshalled(String attributes) throws Exception {
		return XmlUtils.marshaltoString(part(attributes).getJaxbElement(), true, false);
	}

	private static DocumentSettingsPart part(String attributes) throws Exception {
		DocumentSettingsPart part = new DocumentSettingsPart();
		part.unmarshal(new ByteArrayInputStream(("<w:settings " + W + "><w:stylePaneFormatFilter " + attributes + "/></w:settings>")
				.getBytes(StandardCharsets.UTF_8)));
		return part;
	}

	private static byte[] bytes(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource(name)) {
			return is.readAllBytes();
		}
	}

	private static String settings(byte[] docx) throws Exception {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ) {
				if (e.getName().equals("word/settings.xml")) return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		throw new IllegalStateException("no settings part");
	}

	private static Map<String, String> attributes(String settings) {
		Matcher element = Pattern.compile("<w:stylePaneFormatFilter([^>]*)/>").matcher(settings);
		assertTrue(settings, element.find());
		Map<String, String> attributes = new TreeMap<String, String>();
		Matcher a = Pattern.compile("(w:\\w+)=\"([^\"]*)\"").matcher(element.group(1));
		while (a.find()) attributes.put(a.group(1), a.group(2));
		return attributes;
	}
}
