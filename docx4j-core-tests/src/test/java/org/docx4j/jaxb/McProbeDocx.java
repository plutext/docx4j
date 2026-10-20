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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * A docx made from raw XML for the mc:AlternateContent load tests (CR-051), loaded through
 * docx4j's own load path in a given {@link McBlockLevel} mode, so that every part's unmarshal
 * meets the listener and the splice as a real load does.
 */
public final class McProbeDocx {

	public static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	public static final String MC = "http://schemas.openxmlformats.org/markup-compatibility/2006";

	/** The declarations every part's root carries: the conventional prefixes, and w99 for a namespace nobody understands. */
	public static final String NS = " xmlns:w=\"" + W + "\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:mc=\"" + MC + "\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\""
			+ " xmlns:wpi=\"http://schemas.microsoft.com/office/word/2010/wordprocessingInk\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:w99=\"urn:example:not-understood\"";

	private final Map<String, String> parts = new LinkedHashMap<String, String>();
	private final StringBuilder types = new StringBuilder();
	private final StringBuilder rels = new StringBuilder();

	private McProbeDocx(String rootAttributes, String body) {
		parts.put("word/document.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:document" + NS
				+ rootAttributes + "><w:body>" + body + "</w:body></w:document>");
	}

	/** A document whose body is this XML. */
	public static McProbeDocx body(String body) {
		return new McProbeDocx("", body);
	}

	/** A document whose root carries these further attributes (an mc:Ignorable) and whose body is this XML. */
	public static McProbeDocx body(String rootAttributes, String body) {
		return new McProbeDocx(rootAttributes, body);
	}

	/** Adds a part related from the main document part, its XML given without the root (which gets {@link #NS}). */
	public McProbeDocx part(String name, String contentType, String relType, String rId, String rootName, String content) {
		parts.put("word/" + name, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><" + rootName + NS + ">"
				+ content + "</" + rootName + ">");
		types.append("<Override PartName=\"/word/").append(name).append("\" ContentType=\"").append(contentType).append("\"/>");
		rels.append("<Relationship Id=\"").append(rId).append("\" Type=\"").append(relType).append("\" Target=\"").append(name).append("\"/>");
		return this;
	}

	public byte[] bytes() throws Exception {
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
					+ "</Relationships>");
			put(zip, "word/_rels/document.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" + rels + "</Relationships>");
			for (Map.Entry<String, String> e : parts.entrySet()) put(zip, e.getKey(), e.getValue());
		}
		return bytes.toByteArray();
	}

	/** Loaded with the property's mode. */
	public WordprocessingMLPackage load() throws Exception {
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bytes()));
	}

	/** Loaded with this mode given as the load option. */
	public WordprocessingMLPackage load(McBlockLevel mode) throws Exception {
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bytes()), mode);
	}

	private static void put(ZipOutputStream zip, String name, String xml) throws Exception {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(xml.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	/** A package saved to bytes. */
	public static byte[] save(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.save(pkg, out);
		return out.toByteArray();
	}

	/** A package saved and loaded again in this mode. */
	public static WordprocessingMLPackage roundTrip(WordprocessingMLPackage pkg, McBlockLevel mode) throws Exception {
		return WordprocessingMLPackage.load(new ByteArrayInputStream(save(pkg)), mode);
	}

	/** A zip entry's text. */
	public static String entry(byte[] docx, String name) throws Exception {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ) {
				if (e.getName().equals(name)) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[8192];
					for (int n; (n = zip.read(buf)) > 0; ) out.write(buf, 0, n);
					return new String(out.toByteArray(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new AssertionError("no " + name);
	}

	/** A part's root start tag, as saved. */
	static String rootTag(String xml, String rootName) {
		int start = xml.indexOf("<" + rootName);
		return xml.substring(start, xml.indexOf('>', start) + 1);
	}

	/** A part's root start tag, as saved: the first element after the XML declaration. */
	static String rootTag(String xml) {
		int start = xml.indexOf("?>");
		start = xml.indexOf('<', start < 0 ? 0 : start + 2);
		return xml.substring(start, xml.indexOf('>', start) + 1);
	}
}
