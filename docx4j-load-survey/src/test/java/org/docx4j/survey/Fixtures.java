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
package org.docx4j.survey;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Small packages written byte for byte, for the cases docx4j's API won't make. */
final class Fixtures {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String MC = "http://schemas.openxmlformats.org/markup-compatibility/2006";
	static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";

	private static final String DECL = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	private static final String CONTENT_TYPES = DECL
			+ "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
			+ "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
			+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
			+ "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
			+ "</Types>";

	private static final String RELS = DECL
			+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
			+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
			+ "</Relationships>";

	private Fixtures() {
	}

	/** A docx whose main part is this body. */
	static Path docx(Path dir, String name, String body) throws IOException {
		return docxRaw(dir, name, DECL + "<w:document xmlns:w=\"" + W + "\" xmlns:mc=\"" + MC + "\" xmlns:w14=\"" + W14
				+ "\" mc:Ignorable=\"w14\"><w:body>" + body + "</w:body></w:document>");
	}

	/** A docx whose main part is these characters, well-formed or not. */
	static Path docxRaw(Path dir, String name, String documentXml) throws IOException {
		Path p = dir.resolve(name);
		try (OutputStream os = Files.newOutputStream(p); ZipOutputStream zip = new ZipOutputStream(os)) {
			entry(zip, "[Content_Types].xml", CONTENT_TYPES);
			entry(zip, "_rels/.rels", RELS);
			entry(zip, "word/document.xml", documentXml);
		}
		return p;
	}

	private static void entry(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	static final String GOOD = "<w:p><w:r><w:t>hello</w:t></w:r></w:p>";

	/** CR-065: one truncated, one empty, one dropped; CR-066: two out of range. */
	static final String LENIENT = "<w:p><w:pPr><w:spacing w:before=\"120.5\"/>"
			+ "<w:ind w:left=\"7205001\" w:right=\"-32769\" w:firstLine=\"\" w:hanging=\"abc\"/></w:pPr></w:p>";

	/** One block-level element whose w14 Choice is read, one in a run whose unknown Choice is not. */
	static final String MC_BODY = "<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:p><w:r><w:t>choice</w:t></w:r></w:p></mc:Choice>"
			+ "<mc:Fallback><w:p><w:r><w:t>fallback</w:t></w:r></w:p></mc:Fallback></mc:AlternateContent>"
			+ "<w:p><w:r><mc:AlternateContent><mc:Choice xmlns:zz=\"urn:unknown\" Requires=\"zz\"><w:t>zz</w:t></mc:Choice>"
			+ "<mc:Fallback><w:t>fb</w:t></mc:Fallback></mc:AlternateContent></w:r></w:p>";
}
