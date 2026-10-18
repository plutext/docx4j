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
package org.docx4j.model.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * A docx made from raw XML for the text model's unit tests, loaded through docx4j's own load path
 * (so that, among other things, each {@code mc:Choice}'s {@code Requires} is resolved by URI as the
 * part is unmarshalled).
 */
final class MiniDocx {

	static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
			+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"";

	private final Map<String, String> parts = new LinkedHashMap<String, String>();
	private final Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
	private final StringBuilder types = new StringBuilder();
	private final StringBuilder rels = new StringBuilder();
	private final StringBuilder packageRels = new StringBuilder();

	private MiniDocx(String body) {
		parts.put("word/document.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:document " + NS + "><w:body>" + body + "</w:body></w:document>");
	}

	/** A document whose body is this XML (the prefixes of {@link #NS} are declared). */
	static MiniDocx body(String body) {
		return new MiniDocx(body);
	}

	/** Adds a part related from the main document part. */
	MiniDocx part(String name, String contentType, String relType, String rId, String xml) {
		parts.put("word/" + name, xml);
		types.append("<Override PartName=\"/word/").append(name).append("\" ContentType=\"").append(contentType).append("\"/>");
		rels.append("<Relationship Id=\"").append(rId).append("\" Type=\"").append(relType).append("\" Target=\"").append(name).append("\"/>");
		return this;
	}

	/** Adds a binary part related from the main document part. */
	MiniDocx part(String name, String contentType, String relType, String rId, byte[] bytes) {
		entries.put("word/" + name, bytes);
		types.append("<Override PartName=\"/word/").append(name).append("\" ContentType=\"").append(contentType).append("\"/>");
		rels.append("<Relationship Id=\"").append(rId).append("\" Type=\"").append(relType).append("\" Target=\"").append(name).append("\"/>");
		return this;
	}

	/** Adds a part related from the package (document properties). */
	MiniDocx packagePart(String path, String contentType, String relType, String rId, String xml) {
		parts.put(path, xml);
		types.append("<Override PartName=\"/").append(path).append("\" ContentType=\"").append(contentType).append("\"/>");
		packageRels.append("<Relationship Id=\"").append(rId).append("\" Type=\"").append(relType).append("\" Target=\"").append(path).append("\"/>");
		return this;
	}

	/** Adds a zip entry as it is, with its content type (a part's relationships, a part another part relates). */
	MiniDocx entry(String path, String contentType, String xml) {
		parts.put(path, xml);
		if (contentType != null) types.append("<Override PartName=\"/").append(path).append("\" ContentType=\"").append(contentType).append("\"/>");
		return this;
	}

	/** Adds a relationship from the main document part to a target outside {@code word/}, given relative to it. */
	MiniDocx relationship(String rId, String relType, String target) {
		rels.append("<Relationship Id=\"").append(rId).append("\" Type=\"").append(relType).append("\" Target=\"").append(target).append("\"/>");
		return this;
	}

	/** Adds a footnotes part holding these notes (the prefixes of {@link #NS} are declared). */
	MiniDocx footnotes(String notes) {
		return part("footnotes.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml",
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes", "rIdFn",
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:footnotes " + NS + ">" + notes + "</w:footnotes>");
	}

	WordprocessingMLPackage load() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			put(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
					+ "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
					+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
					+ "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
					+ types + "</Types>");
			put(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
					+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
					+ packageRels + "</Relationships>");
			put(zip, "word/_rels/document.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" + rels + "</Relationships>");
			for (Map.Entry<String, String> e : parts.entrySet()) put(zip, e.getKey(), e.getValue());
			for (Map.Entry<String, byte[]> e : entries.entrySet()) {
				zip.putNextEntry(new ZipEntry(e.getKey()));
				zip.write(e.getValue());
				zip.closeEntry();
			}
		}
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bytes.toByteArray()));
	}

	private static void put(ZipOutputStream zip, String name, String xml) throws Exception {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(xml.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}
}
