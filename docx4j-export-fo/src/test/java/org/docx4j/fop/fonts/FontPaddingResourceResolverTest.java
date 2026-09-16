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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.junit.Test;

/**
 * FOP's TrueType subsetter must be able to subset the last glyph of every font docx4j
 * ships, which {@link FontPaddingResourceResolver} is what makes true.
 *
 * <p>Arimo and Carlito put an empty U+00A0 glyph last and end the file with the glyf
 * table, so FOP reads two bytes that are not there, throws, and writes the font
 * descriptor with no font file - the PDF then names a font it does not embed. This is
 * the defect at the level it happens, over the real font files, without needing a PDF.</p>
 *
 * @since 17.1.1
 */
public class FontPaddingResourceResolverTest {

	/** every TrueType face in the font jars docx4j-export-fo depends on */
	private static final String[] BUNDLED = {
		"/fonts/Arimo/static/Arimo-Regular.ttf",
		"/fonts/Arimo/static/Arimo-Bold.ttf",
		"/fonts/Arimo/static/Arimo-Italic.ttf",
		"/fonts/Arimo/static/Arimo-BoldItalic.ttf",
		"/fonts/Carlito/Carlito-Regular.ttf",
		"/fonts/Carlito/Carlito-Bold.ttf",
		"/fonts/Carlito/Carlito-Italic.ttf",
		"/fonts/Carlito/Carlito-BoldItalic.ttf",
	};

	/**
	 * With the padding, FOP can subset the last glyph of every bundled face.
	 */
	@Test
	public void fopCanSubsetTheLastGlyphOfEveryBundledFace() throws Exception {
		List<String> padded = new ArrayList<String>();
		for (String resource : BUNDLED) {
			byte[] raw = read(resource);
			int pad = FontPaddingResourceResolver.paddingNeeded(raw);
			assertTrue(resource + ": " + pad + " bytes of padding", pad >= 0 && pad <= 2);
			byte[] served = (pad == 0) ? raw : Arrays.copyOf(raw, raw.length + pad);
			try {
				subsetLastGlyph(served);
			} catch (Exception e) {
				fail(resource + " could not be subset even padded: " + e);
			}
			if (pad > 0) {
				padded.add(resource);
			}
		}
		assertFalse("no bundled face needed padding - has the defect gone, or the jars changed?",
				padded.isEmpty());
	}

	/**
	 * The padding is exactly what the last glyph's offset leaves short of two bytes - so
	 * the test above is not vacuous, and the padded font is no longer than it must be.
	 *
	 * <p>Deliberately not phrased as "FOP throws without it": that will stop being true
	 * when a FOP with the guard in {@code GlyfTable.isComposite} ships.</p>
	 */
	@Test
	public void paddingIsWhatTheLastGlyphLeavesShort() {
		int withTheShape = 0;
		for (String resource : BUNDLED) {
			byte[] raw = read(resource);
			long available = raw.length - lastGlyphOffset(raw);
			int expected = (available >= 2) ? 0 : (int) (2 - available);
			assertEquals(resource + ": " + available + " bytes after the last glyph's offset",
					expected, FontPaddingResourceResolver.paddingNeeded(raw));
			if (expected > 0) {
				withTheShape++;
			}
		}
		assertTrue("no bundled face ends the file at its last glyph", withTheShape > 0);
	}

	/** The resolver serves those bytes, and passes everything else through untouched. */
	@Test
	public void theResolverServesThePaddedFont() throws Exception {
		final byte[] raw = read("/fonts/Carlito/Carlito-Regular.ttf");
		final int pad = FontPaddingResourceResolver.paddingNeeded(raw);
		assertTrue("Carlito Regular needs padding", pad > 0);

		ResourceResolver delegate = new ResourceResolver() {
			public Resource getResource(URI uri) {
				return new Resource(new ByteArrayInputStream(raw));
			}
			public java.io.OutputStream getOutputStream(URI uri) {
				throw new UnsupportedOperationException();
			}
		};
		ResourceResolver resolver = new FontPaddingResourceResolver(delegate);

		byte[] font = IOUtils.toByteArray(
				resolver.getResource(new URI("file:/fonts/Carlito-Regular.ttf")));
		assertEquals(raw.length + pad, font.length);
		for (int i = raw.length; i < font.length; i++) {
			assertEquals("the appended bytes are zero", 0, font[i]);
		}

		byte[] other = IOUtils.toByteArray(
				resolver.getResource(new URI("file:/pictures/image1.png")));
		assertEquals("a resource that is not a font is untouched", raw.length, other.length);
	}

	@Test
	public void onlyFontUrisAreConsidered() throws Exception {
		assertTrue(FontPaddingResourceResolver.isSfntUri(new URI("file:/x/Arimo-Bold.TTF")));
		assertTrue(FontPaddingResourceResolver.isSfntUri(new URI("file:/x/y.otf")));
		assertTrue(FontPaddingResourceResolver.isSfntUri(new URI("file:/x/y.ttc")));
		assertTrue("a font inside a jar",
				FontPaddingResourceResolver.isSfntUri(new URI("jar:file:/x/f.jar!/fonts/y.ttf")));
		assertTrue("a query does not hide the extension",
				FontPaddingResourceResolver.isSfntUri(new URI("file:/x/y.ttf?a=b")));
		assertFalse(FontPaddingResourceResolver.isSfntUri(new URI("file:/x/y.png")));
		assertFalse(FontPaddingResourceResolver.isSfntUri(new URI("file:/x/y.pfb")));
	}

	/** Not a font at all, and a truncated one: neither may be padded, neither may throw. */
	@Test
	public void nonFontBytesAreLeftAlone() {
		assertEquals(0, FontPaddingResourceResolver.paddingNeeded(null));
		assertEquals(0, FontPaddingResourceResolver.paddingNeeded(new byte[0]));
		assertEquals(0, FontPaddingResourceResolver.paddingNeeded("not a font at all".getBytes()));
		byte[] truncated = Arrays.copyOf(read("/fonts/Carlito/Carlito-Regular.ttf"), 5000);
		assertEquals(0, FontPaddingResourceResolver.paddingNeeded(truncated));
	}

	// ------------------------------------------------------------------ helpers

	private static byte[] read(String resource) {
		InputStream is = FontPaddingResourceResolverTest.class.getResourceAsStream(resource);
		assertNotNull("not on the classpath: " + resource, is);
		try {
			return IOUtils.toByteArray(is);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/** FOP's own subsetter, over .notdef and the font's last glyph. */
	private static void subsetLastGlyph(byte[] font) throws Exception {
		Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();
		glyphs.put(0, 0);
		glyphs.put(numGlyphs(font) - 1, 1);
		TTFSubSetFile subset = new TTFSubSetFile();
		subset.readFont(new FontFileReader(new ByteArrayInputStream(font)), "probe", null, glyphs);
		assertTrue("the subset has some bytes", subset.getFontSubset().length > 0);
	}

	/** where the font's last glyph begins, read independently of the class under test. */
	private static long lastGlyphOffset(byte[] font) {
		long glyf = tableOffset(font, "glyf");
		long loca = tableOffset(font, "loca");
		long head = tableOffset(font, "head");
		int last = numGlyphs(font) - 1;
		int indexToLocFormat = (short) u16(font, (int) head + 50);
		long offset = (indexToLocFormat == 0)
				? u16(font, (int) loca + 2 * last) * 2L
				: u32(font, (int) loca + 4 * last);
		return glyf + offset;
	}

	private static long tableOffset(byte[] font, String tag) {
		int numTables = u16(font, 4);
		for (int i = 0; i < numTables; i++) {
			int entry = 12 + 16 * i;
			if (font[entry] == tag.charAt(0) && font[entry + 1] == tag.charAt(1)
					&& font[entry + 2] == tag.charAt(2) && font[entry + 3] == tag.charAt(3)) {
				return u32(font, entry + 8);
			}
		}
		throw new IllegalStateException("no " + tag + " table");
	}

	private static int u16(byte[] b, int at) {
		return ((b[at] & 0xFF) << 8) | (b[at + 1] & 0xFF);
	}

	private static long u32(byte[] b, int at) {
		return ((long) (b[at] & 0xFF) << 24) | ((b[at + 1] & 0xFF) << 16)
				| ((b[at + 2] & 0xFF) << 8) | (b[at + 3] & 0xFF);
	}

	/** numGlyphs, from the font's maxp table. */
	private static int numGlyphs(byte[] font) {
		int numTables = ((font[4] & 0xFF) << 8) | (font[5] & 0xFF);
		for (int i = 0; i < numTables; i++) {
			int entry = 12 + 16 * i;
			if (font[entry] == 'm' && font[entry + 1] == 'a'
					&& font[entry + 2] == 'x' && font[entry + 3] == 'p') {
				int offset = ((font[entry + 8] & 0xFF) << 24) | ((font[entry + 9] & 0xFF) << 16)
						| ((font[entry + 10] & 0xFF) << 8) | (font[entry + 11] & 0xFF);
				return ((font[offset + 4] & 0xFF) << 8) | (font[offset + 5] & 0xFF);
			}
		}
		throw new IllegalStateException("no maxp table");
	}
}
