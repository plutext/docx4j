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
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@code altChunk} expanded at its position, as Word expands it on open (WD REQ-513), into blocks
 * apart from the package (REQ-204): {@code JaxbXmlPartAltChunkHost.convertAltChunks}' kinds, read
 * rather than converted in place. A plain-text chunk is its lines; an HTML chunk tidied by jsoup
 * where it is present and read as blocks, else its text with the tags stripped; an XHTML chunk
 * through docx4j-ImportXHTML where it is on the class path (into a scratch package), else read as
 * HTML; an MHT chunk through docx4j-ImportXHTML only; a WordprocessingML or Flat OPC chunk as a
 * package read one level down. An RTF chunk, a Word 2003 XML chunk and anything else are not
 * expanded.
 */
final class AltChunks {

	private static final Logger log = LoggerFactory.getLogger(AltChunks.class);

	private AltChunks() {}

	/** The chunk's content type as the package declares it, without parameters; the warning's selector. */
	static String contentType(AlternativeFormatInputPart part) {
		String ct = part.getContentType() == null ? "" : part.getContentType();
		int semi = ct.indexOf(';');
		return (semi >= 0 ? ct.substring(0, semi) : ct).trim();
	}

	/** The chunk's blocks; null when it is not expanded. */
	static List<TextBlock> expand(Extractor x, AlternativeFormatInputPart part, TextBlock.Story story, String prefix) {
		byte[] bytes;
		try {
			bytes = part.getBytes();
		} catch (RuntimeException e) {
			return null;
		}
		if (bytes == null) return null;
		String ct = contentType(part).toLowerCase(Locale.ROOT);
		String ext = part.getPartName() == null ? "" : part.getPartName().getExtension().toLowerCase(Locale.ROOT);
		boolean zip = bytes.length > 3 && bytes[0] == 'P' && bytes[1] == 'K';
		if (ct.equals("text/plain") || ct.isEmpty() && ext.equals("txt")) return lines(Embedded.decode(bytes), story, prefix);
		if (ct.equals("application/rtf") || ct.equals("text/rtf") || ext.equals("rtf")) return null;
		if (ct.equals("text/html") || ct.isEmpty() && (ext.equals("htm") || ext.equals("html"))) {
			return HtmlText.blocks(Embedded.decode(bytes), story, prefix);
		}
		if (ct.equals("application/xhtml+xml") || ct.isEmpty() && ext.equals("xhtml")) {
			List<TextBlock> imported = imported(x, bytes, false, story, prefix);
			return imported != null ? imported : HtmlText.blocks(Embedded.decode(bytes), story, prefix);
		}
		if (ct.equals("message/rfc822") || ext.equals("mht") || ext.equals("mhtml")) return imported(x, bytes, true, story, prefix);
		if (zip || isFlatOpc(bytes)) return document(x, bytes, story, prefix);
		return null;
	}

	/** A plain-text chunk's lines, a paragraph each. */
	private static List<TextBlock> lines(String text, TextBlock.Story story, String prefix) {
		List<TextBlock> out = new ArrayList<TextBlock>();
		String[] lines = text.split("\r\n|\r|\n", -1);
		int count = lines.length;
		if (count > 0 && lines[count - 1].isEmpty()) count--;
		for (int i = 0; i < count; i++) out.add(Embedded.paragraph(story, prefix + "/" + i, lines[i]));
		return out;
	}

	private static boolean isFlatOpc(byte[] bytes) {
		String head = new String(bytes, 0, Math.min(bytes.length, 2048), StandardCharsets.UTF_8);
		return head.contains("http://schemas.microsoft.com/office/2006/xmlPackage");
	}

	/** A WordprocessingML or Flat OPC chunk, read one level down; null for a package of another kind. */
	private static List<TextBlock> document(Extractor x, byte[] bytes, TextBlock.Story story, String prefix) {
		OpcPackage opc;
		try {
			opc = OpcPackage.load(new ByteArrayInputStream(bytes));
		} catch (Exception e) {
			log.warn("Couldn't load the altChunk at " + prefix + ": " + e.getMessage());
			return null;
		}
		if (!(opc instanceof WordprocessingMLPackage)) return null;
		return x.nested((WordprocessingMLPackage) opc, prefix, story);
	}

	/**
	 * An XHTML or MHT chunk through docx4j-ImportXHTML, reached reflectively as
	 * {@code convertAltChunks} reaches it (it is LGPL), into a scratch package so that nothing is
	 * added to the document's; null where it is not on the class path or fails.
	 */
	@SuppressWarnings("unchecked")
	private static List<TextBlock> imported(Extractor x, byte[] bytes, boolean mht, TextBlock.Story story, String prefix) {
		Class<?> importer;
		try {
			importer = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
		} catch (ClassNotFoundException | LinkageError e) {
			return null;
		}
		try {
			WordprocessingMLPackage scratch = WordprocessingMLPackage.createPackage();
			Constructor<?> ctor = importer.getConstructor(WordprocessingMLPackage.class);
			Object instance = ctor.newInstance(scratch);
			Method convert = importer.getMethod(mht ? "convertMHT" : "convert", InputStream.class, String.class);
			List<Object> content = (List<Object>) convert.invoke(instance, new ByteArrayInputStream(bytes), null);
			scratch.getMainDocumentPart().getContent().addAll(content);
			return x.nested(scratch, prefix, story);
		} catch (Exception | LinkageError e) {
			Throwable cause = e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null ? e.getCause() : e;
			log.warn("docx4j-ImportXHTML couldn't read the altChunk at " + prefix + ": " + cause);
			return null;
		}
	}
}
