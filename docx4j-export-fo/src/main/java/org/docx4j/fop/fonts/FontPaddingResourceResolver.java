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
package org.docx4j.fop.fonts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves FOP a TrueType font whose last glyph is empty with a zero byte or two appended,
 * so that FOP's subsetter does not read past the end of the file.
 *
 * <h3>The defect</h3>
 * A glyph whose {@code loca} entry gives it a length of zero has no glyph description at
 * all. FOP's {@code GlyfTable.isComposite} reads a 2-byte {@code numberOfContours} at the
 * glyph's offset without checking its length; where the empty glyph is the <b>last</b> in
 * the font and the {@code glyf} table is the <b>last</b> table in the file, that read is
 * past the end of the file and throws:
 * <pre>
 * ERROR org.apache.fop.pdf.PDFFactory - Failed to embed font [...] Carlito-Regular
 * java.io.EOFException: Reached EOF, file size=593908
 *     at org.apache.fop.fonts.truetype.FontFileReader.read(FontFileReader.java:100)
 *     at org.apache.fop.fonts.truetype.GlyfTable.isComposite(GlyfTable.java:206)
 *     at org.apache.fop.fonts.truetype.TTFSubSetFile.scanGlyphs(TTFSubSetFile.java:572)
 *     at org.apache.fop.pdf.PDFFactory.getFontSubsetBytes(PDFFactory.java:1482)
 * </pre>
 * {@code PDFFactory} catches it, logs it, and writes the font descriptor with no
 * {@code /FontFile2}: the PDF then names a font it does not embed, which fails PDF/A and
 * leaves the reader to substitute. Nothing else in the document is affected, so the defect
 * is easy to miss - the text is still drawn, in whatever the reader has.
 *
 * <p>Several of the fonts docx4j itself ships are of exactly this shape, because their last
 * glyph is U+00A0 NO-BREAK SPACE: measured over the 31 TrueType files in docx4j's font jars,
 * <b>Arimo Bold, Arimo Italic, Arimo BoldItalic, Carlito Regular and Carlito Italic</b> have
 * fewer than two bytes after that glyph's offset. So any document with a non-breaking space
 * in an Arial or Calibri run - which is to say a great many real documents - loses its body
 * font from the PDF.</p>
 *
 * <h3>The fix here</h3>
 * FOP reads a font from its {@code embed-url} itself, through the
 * {@link ResourceResolver} the {@code FopFactory} was built with, so wrapping that resolver
 * is enough: this one hands back the same bytes with the one or two zero bytes the read
 * needs. That is harmless - FOP finds every table through the table directory, and a
 * subset FOP writes is built from the glyph data it copies, not from the tail - and it is
 * done <b>in memory</b>: the font files themselves are upstream binaries, and trailing
 * bytes would invalidate {@code head.checkSumAdjustment}.
 *
 * <p>Only a font that needs it is touched. The table directory is parsed to find where the
 * last glyph begins, and the stream is passed through unchanged unless that offset leaves
 * fewer than two bytes - so for every other font, and for every resource that is not a
 * font, the bytes FOP sees are exactly the bytes it sees today.</p>
 *
 * <p>The real fix belongs in FOP (an empty glyph is never composite and must not be read);
 * once that has shipped this class can go. Set
 * {@value #PROPERTY} to false to turn it off.</p>
 *
 * @since 17.2.0
 */
public class FontPaddingResourceResolver implements ResourceResolver {

	private static Logger log = LoggerFactory.getLogger(FontPaddingResourceResolver.class);

	/** Set false to serve font files to FOP unchanged. */
	public static final String PROPERTY = "docx4j.fonts.fop.padEmptyLastGlyph";

	/** At most this many bytes are ever appended: a well formed font needs one or two. */
	private static final int MAX_PAD = 2;

	private final ResourceResolver delegate;

	public FontPaddingResourceResolver(ResourceResolver delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("delegate");
		}
		this.delegate = delegate;
	}

	/**
	 * This resolver over {@code delegate}, or over FOP's default resolver where that is
	 * null; {@code delegate} itself where {@value #PROPERTY} is false.
	 */
	public static ResourceResolver wrap(ResourceResolver delegate) {
		ResourceResolver actual = (delegate != null) ? delegate
				: ResourceResolverFactory.createDefaultResourceResolver();
		if (!Docx4jProperties.getProperty(PROPERTY, true)) {
			return actual;
		}
		/* The docx4j FO renderer carries the GlyfTable fix itself (hook glyf-empty-glyph), so
		 * the padding is not needed there and the bytes go through untouched.  CR-020 phase 1. */
		if (org.docx4j.convert.out.fo.FopCapabilities.has(
				org.docx4j.convert.out.fo.FopCapabilities.Capability.GLYF_EMPTY_GLYPH)) {
			return actual;
		}
		return new FontPaddingResourceResolver(actual);
	}

	public Resource getResource(URI uri) throws IOException {

		Resource resource = delegate.getResource(uri);
		if (!isSfntUri(uri)) {
			return resource;
		}

		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(resource);
		} finally {
			resource.close();
		}

		int pad = paddingNeeded(bytes);
		if (pad > 0) {
			log.debug("{}: appending {} byte(s); its last glyph is empty and ends the file", uri, pad);
			bytes = Arrays.copyOf(bytes, bytes.length + pad);
		}
		return new Resource(resource.getType(), new ByteArrayInputStream(bytes));
	}

	public OutputStream getOutputStream(URI uri) throws IOException {
		return delegate.getOutputStream(uri);
	}

	/** Whether this URI names a TrueType/OpenType file (so, one with a glyf table to read). */
	static boolean isSfntUri(URI uri) {
		if (uri == null) {
			return false;
		}
		String s = uri.toString();
		int cut = s.indexOf('#');
		if (cut >= 0) {
			s = s.substring(0, cut);
		}
		cut = s.indexOf('?');
		if (cut >= 0) {
			s = s.substring(0, cut);
		}
		s = s.toLowerCase(java.util.Locale.ROOT);
		return s.endsWith(".ttf") || s.endsWith(".ttc") || s.endsWith(".otf");
	}

	/**
	 * How many bytes this font needs appended for the 2-byte read at its last glyph's
	 * offset to stay inside the file: 0 for a font that does not have the defect's shape,
	 * for a font with no glyf outlines (a CFF/OpenType one), and for anything that cannot
	 * be parsed as a font at all.
	 */
	static int paddingNeeded(byte[] font) {
		if (font == null || font.length < 12) {
			return 0;
		}
		try {
			if (font[0] == 't' && font[1] == 't' && font[2] == 'c' && font[3] == 'f') {
				// a collection: every font in it has its own table directory
				long numFonts = uint32(font, 8);
				int pad = 0;
				for (int i = 0; i < numFonts; i++) {
					int offset = (int) uint32(font, 12 + 4 * i);
					pad = Math.max(pad, paddingForDirectory(font, offset));
				}
				return pad;
			}
			return paddingForDirectory(font, 0);
		} catch (RuntimeException e) {
			// a font this cannot read is a font FOP will reject anyway; leave it alone
			log.debug("not padded: {}", e.toString());
			return 0;
		}
	}

	private static int paddingForDirectory(byte[] font, int dirOffset) {

		int numTables = uint16(font, dirOffset + 4);
		int glyfOffset = -1;
		int locaOffset = -1;
		int maxpOffset = -1;
		int headOffset = -1;
		for (int i = 0; i < numTables; i++) {
			int entry = dirOffset + 12 + 16 * i;
			String tag = new String(font, entry, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
			int offset = (int) uint32(font, entry + 8);
			if ("glyf".equals(tag)) {
				glyfOffset = offset;
			} else if ("loca".equals(tag)) {
				locaOffset = offset;
			} else if ("maxp".equals(tag)) {
				maxpOffset = offset;
			} else if ("head".equals(tag)) {
				headOffset = offset;
			}
		}
		if (glyfOffset < 0 || locaOffset < 0 || maxpOffset < 0 || headOffset < 0) {
			return 0; // no TrueType outlines (a CFF font, say)
		}

		int numGlyphs = uint16(font, maxpOffset + 4);
		if (numGlyphs < 1) {
			return 0;
		}
		int indexToLocFormat = (short) uint16(font, headOffset + 50);
		long lastGlyph = (indexToLocFormat == 0)
				? uint16(font, locaOffset + 2 * (numGlyphs - 1)) * 2L
				: uint32(font, locaOffset + 4 * (numGlyphs - 1));

		long end = glyfOffset + lastGlyph + 2;
		if (end <= font.length) {
			return 0;
		}
		long needed = end - font.length;
		// more than MAX_PAD means the loca table points well beyond the file: the font is
		// broken in a way padding cannot help
		return (needed > MAX_PAD) ? 0 : (int) needed;
	}

	private static int uint16(byte[] b, int at) {
		if (at < 0 || at + 2 > b.length) {
			throw new IllegalArgumentException("read of 2 at " + at + " in " + b.length);
		}
		return ((b[at] & 0xFF) << 8) | (b[at + 1] & 0xFF);
	}

	private static long uint32(byte[] b, int at) {
		if (at < 0 || at + 4 > b.length) {
			throw new IllegalArgumentException("read of 4 at " + at + " in " + b.length);
		}
		return ((long) (b[at] & 0xFF) << 24) | ((b[at + 1] & 0xFF) << 16)
				| ((b[at + 2] & 0xFF) << 8) | (b[at + 3] & 0xFF);
	}
}
