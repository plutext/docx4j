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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.finders.ClassFinder;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtRun;
import org.junit.Test;

/**
 * An element JAXB does not expect inside {@code w:sdtPr} (an {@code mc:Ignorable} extension, or an unknown one) is
 * content for the preprocessor, as anywhere else. JAXB's message for it lists the expected elements, among them
 * {@code w14:entityPicker}, and the load took that for a parser's entity error and left the main document part
 * unread ({@link XmlUtils#isEntityError}; found by open-agreements/docx-platform-tests' content-control scenarios,
 * 2026-11-03). An undeclared entity is still refused.
 */
public class UnexpectedElementInSdtPrTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String EXTENSION = "<ext:opaqueExtension ext:sentinel=\"extension-child-sentinel\">"
			+ "<ext:payload><ext:nested>opaque payload sentinel</ext:nested></ext:payload></ext:opaqueExtension>";

	@Test
	public void ignorableExtensionInInlineSdtPr() throws Exception {
		WordprocessingMLPackage pkg = load("<w:document " + W
				+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:ext=\"urn:example:ext\""
				+ " mc:Ignorable=\"ext\"><w:body><w:p><w:r><w:t xml:space=\"preserve\">Edit target: thirty days. </w:t></w:r>"
				+ "<w:sdt><w:sdtPr><w:alias w:val=\"Opaque inline control\"/><w:tag w:val=\"dpt-inline-sdt\"/>" + EXTENSION
				+ "<w:id w:val=\"54\"/></w:sdtPr><w:sdtContent><w:r><w:t>Controlled text sentinel</w:t></w:r></w:sdtContent></w:sdt>"
				+ "<w:r><w:t xml:space=\"preserve\"> Paragraph tail.</w:t></w:r></w:p></w:body></w:document>");
		assertEquals("Edit target: thirty days. Controlled text sentinel Paragraph tail.", text(pkg));
		SdtElement sdt = onlySdt(pkg, SdtRun.class);
		assertEquals("dpt-inline-sdt", sdt.getSdtPr().getTag().getVal());
	}

	@Test
	public void ignorableExtensionInBlockSdtPr() throws Exception {
		WordprocessingMLPackage pkg = load("<w:document " + W
				+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:ext=\"urn:example:ext\""
				+ " mc:Ignorable=\"ext\"><w:body><w:sdt><w:sdtPr>" + EXTENSION + "<w:tag w:val=\"dpt-block-sdt\"/></w:sdtPr>"
				+ "<w:sdtContent><w:p><w:r><w:t>Controlled paragraph</w:t></w:r></w:p></w:sdtContent></w:sdt>"
				+ "<w:p><w:r><w:t>Edit target: thirty days.</w:t></w:r></w:p></w:body></w:document>");
		assertEquals("Controlled paragraphEdit target: thirty days.", text(pkg));
		assertEquals("dpt-block-sdt", onlySdt(pkg, SdtBlock.class).getSdtPr().getTag().getVal());
	}

	@Test
	public void unknownElementInSdtPr() throws Exception {
		// not declared ignorable: Word would refuse the file, docx4j drops the element and reads the rest, as elsewhere
		WordprocessingMLPackage pkg = load("<w:document " + W + " xmlns:ext=\"urn:example:ext\"><w:body><w:sdt><w:sdtPr>"
				+ "<ext:unknown/><w:tag w:val=\"t\"/></w:sdtPr><w:sdtContent><w:p><w:r><w:t>kept</w:t></w:r></w:p>"
				+ "</w:sdtContent></w:sdt></w:body></w:document>");
		assertEquals("kept", text(pkg));
		assertEquals("t", onlySdt(pkg, SdtBlock.class).getSdtPr().getTag().getVal());
	}

	@Test
	public void undeclaredEntityStillRefused() throws Exception {
		WordprocessingMLPackage pkg = load("<w:document " + W + "><w:body><w:p><w:r><w:t>a &xxe; b</w:t></w:r></w:p>"
				+ "</w:body></w:document>");
		try {
			pkg.getMainDocumentPart().getContent();
			fail("an undeclared entity was read");
		} catch (RuntimeException e) {
			StringBuilder causes = new StringBuilder();
			for (Throwable t = e; t != null; t = t.getCause()) causes.append(t).append('\n');
			assertTrue(causes.toString(), causes.toString().contains("xxe"));
		}
	}

	private static WordprocessingMLPackage load(String documentXml) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			put(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
					+ "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
					+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
					+ "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
					+ "</Types>");
			put(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
					+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
					+ "</Relationships>");
			put(zip, "word/document.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + documentXml);
		}
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bytes.toByteArray()));
	}

	private static void put(ZipOutputStream zip, String name, String content) throws Exception {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	private static String text(WordprocessingMLPackage pkg) throws Exception {
		java.io.StringWriter out = new java.io.StringWriter();
		TextUtils.extractText(pkg.getMainDocumentPart().getJaxbElement(), out);
		return out.toString();
	}

	private static SdtElement onlySdt(WordprocessingMLPackage pkg, Class<?> type) {
		ClassFinder finder = new ClassFinder(type);
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		List<Object> found = finder.results;
		assertEquals(1, found.size());
		return (SdtElement) found.get(0);
	}
}
