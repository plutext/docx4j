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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * JAXB RI stops reporting errors after ten (UnmarshallingContext.errorsCounter, a static field),
 * and an element it could skip is then skipped unreported. JaxbValidationEventHandler resets the
 * count when JAXB says the limit is reached (issue 164); until 17.3.6 it looked for the counter
 * under RI 2's class name only, so with RI 3 and 4 the reset failed and logged three ERRORs.
 */
public class JaxbErrorsCounterTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	@Test
	public void counterFoundInTheReferenceImplementation() throws Exception {
		Field f = JaxbValidationEventHandler.errorsCounterField();
		assertNotNull("docx4j-core-tests runs on docx4j-JAXB-ReferenceImpl", f);
		assertEquals("org.glassfish.jaxb.runtime.v2.runtime.unmarshaller.UnmarshallingContext", f.getDeclaringClass().getName());
	}

	/** Twelve w:br directly in a w:p: JAXB refuses the part, and the preprocessed pass reports each. */
	@Test
	public void counterResetPastTheLimit() throws Exception {
		StringBuilder p = new StringBuilder("<w:p><w:r><w:t>before</w:t></w:r>");
		for (int i = 0; i < 12; i++) p.append("<w:br/>");
		p.append("<w:r><w:t>after</w:t></w:r></w:p>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx(
				"<w:document xmlns:w=\"" + W + "\"><w:body>" + p + "</w:body></w:document>")));
		Document d = pkg.getMainDocumentPart().getContents();
		assertEquals("the paragraph's runs kept", "beforeafter", TextUtils.getText(d.getBody()));
		Field f = Class.forName("org.glassfish.jaxb.runtime.v2.runtime.unmarshaller.UnmarshallingContext").getDeclaredField("errorsCounter");
		f.setAccessible(true);
		int left = f.getInt(null);
		assertTrue("reset at the limit, so still counting (no reset leaves -1): " + left, left >= 0);
	}

	private static byte[] docx(String documentXml) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bos)) {
			entry(zip, "[Content_Types].xml", "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
					+ "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
					+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
					+ "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
			entry(zip, "_rels/.rels", "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
					+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
			entry(zip, "word/document.xml", documentXml);
		}
		return bos.toByteArray();
	}

	private static void entry(ZipOutputStream zip, String name, String content) throws Exception {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}
}
