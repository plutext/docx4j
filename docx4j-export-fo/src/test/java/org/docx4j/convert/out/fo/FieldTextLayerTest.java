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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Two things the text layer of the PDF gets wrong where nothing is misplaced:
 * a {@code PAGEREF} whose cached result is empty, and {@code w:noBreakHyphen}.
 *
 * <p>CR-001 batch 41, causes M60 and M44.
 *
 * @since 17.1.1
 */
public class FieldTextLayerTest {

	private static final String SECT =
			"<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/></w:sectPr>";

	private static String fo(String body) throws Exception {
		String xml = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body>" + body + SECT + "</w:body></w:document>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(xml));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	/** A complex PAGEREF field whose result is empty, pointing at a bookmark two pages on. */
	private static String pageref(String cachedResult) {
		return "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> PAGEREF target \\h </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ cachedResult
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>"
				+ "<w:bookmarkStart w:id=\"1\" w:name=\"target\"/>"
				+ "<w:p><w:r><w:t>the target</w:t></w:r></w:p>"
				+ "<w:bookmarkEnd w:id=\"1\"/>";
	}

	/**
	 * Word computes a PAGEREF whose cached result is empty; we dropped the whole field,
	 * so the line lost its page number (a table of contents lost every one of them).
	 */
	@Test
	public void anEmptyPagerefResultIsStillComputed() throws Exception {
		assertTrue("no fo:page-number-citation for a PAGEREF with an empty result",
				fo(pageref("")).contains("page-number-citation"));
	}

	/** And one with a cached result is unaffected. */
	@Test
	public void aPagerefWithACachedResultIsComputedToo() throws Exception {
		assertTrue(fo(pageref("<w:r><w:t>7</w:t></w:r>")).contains("page-number-citation"));
	}

	/**
	 * {@code w:noBreakHyphen} is a hyphen and nothing else: the zero-width no-break
	 * space that used to follow it is charged the font's notdef advance by FOP (0.8em,
	 * painting nothing), so the extracted text read a word space after every one.
	 */
	@Test
	public void aNonBreakingHyphenIsJustAHyphen() throws Exception {
		String out = fo("<w:p><w:r><w:t>Table 1</w:t><w:noBreakHyphen/><w:t>1: Terms</w:t></w:r></w:p>");
		assertFalse("the zero-width no-break space is back", out.indexOf('﻿') >= 0);
		assertTrue(out.contains("-"));
	}
}
