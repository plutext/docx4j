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
 * rather than converted in place. A plain-text chunk is its lines; an HTML, XHTML or MHT chunk as
 * Word reads it ({@link HtmlChunkImport}: parsed by jsoup and read through docx4j-ImportXHTML, into a
 * scratch package, never fetching (WD section 11), what {@code display: none} hides as hidden text),
 * and not expanded without both (CR-053 D7); a WordprocessingML or Flat OPC chunk as a package read
 * one level down. An RTF chunk, a Word 2003 XML chunk and anything else are not expanded.
 */
final class AltChunks {

	private static final Logger log = LoggerFactory.getLogger(AltChunks.class);

	private AltChunks() {}

	/** Whether docx4j-ImportXHTML is on the class path. */
	static boolean importerPresent() {
		return HtmlChunkImport.importerPresent();
	}

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
		if (ct.equals("text/html") || ct.equals("application/xhtml+xml") || ct.equals("message/rfc822")
				|| ct.isEmpty() && (ext.equals("htm") || ext.equals("html") || ext.equals("xhtml"))
				|| ext.equals("mht") || ext.equals("mhtml")) {
			return html(x, part, story, prefix);
		}
		if (zip || isFlatOpc(bytes)) return document(x, part, bytes, story, prefix);
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
	private static List<TextBlock> document(Extractor x, AlternativeFormatInputPart part, byte[] bytes, TextBlock.Story story, String prefix) {
		OpcPackage opc;
		try {
			// an inspection's own reading of the chunk, where it made one (CR-050)
			opc = x.packages != null && x.packages.containsKey(part) ? x.packages.get(part) : OpcPackage.load(new ByteArrayInputStream(bytes));
		} catch (Exception e) {
			log.warn("Couldn't load the altChunk at " + prefix + ": " + e.getMessage());
			return null;
		}
		if (!(opc instanceof WordprocessingMLPackage)) return null;
		return x.nested((WordprocessingMLPackage) opc, prefix, story);
	}

	/**
	 * An HTML, XHTML or MHT chunk as Word reads it, into a scratch package so that nothing is added to
	 * the document's; null where it is not expanded: no HTML reader (jsoup and docx4j-ImportXHTML), or a
	 * chunk the reader refuses. An inspection's reading takes the inspector's import of the chunk.
	 */
	private static List<TextBlock> html(Extractor x, AlternativeFormatInputPart part, TextBlock.Story story, String prefix) {
		// an inspection's reading calls no importer of its own (CR-053 phase 3)
		if (x.chunks != null) {
			WordprocessingMLPackage read = x.chunks.get(part);
			return read == null ? null : x.nested(read, prefix, story);
		}
		if (!HtmlChunkImport.readerPresent()) return null;
		WordprocessingMLPackage scratch;
		try {
			scratch = WordprocessingMLPackage.createPackage();
		} catch (Exception e) {
			log.warn("Couldn't make a package to read the altChunk at " + prefix + ": " + e);
			return null;
		}
		List<Object> content = HtmlChunkImport.read(scratch, part, "the altChunk at " + prefix);
		if (content == null) return null;
		scratch.getMainDocumentPart().getContent().addAll(content);
		return x.nested(scratch, prefix, story);
	}
}
