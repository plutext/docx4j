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
package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.Preprocess;
import org.docx4j.jaxb.McLoadMode;
import org.docx4j.jaxb.McProbeDocx;
import org.docx4j.jaxb.McSelection;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * CR-051 D6: the preprocessing step that resolves the working copy's block-level
 * {@code mc:AlternateContent} to the branch the exporter draws, and the HTML exporters drawing it
 * (section 4: in {@code word} mode the branch Word reads; in {@code preserve} mode the Fallback by
 * default and the Choice under {@code docx4j.jaxb.mc.preferChoice}).
 */
public class ResolveAlternateContentTest {

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String ac(String tag) {
		return "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p(tag + "_W14") + "</mc:Choice>"
				+ "<mc:Fallback>" + p(tag + "_FB") + "</mc:Fallback></mc:AlternateContent>";
	}

	/** An element in the body and one in the header the section references. */
	private static McProbeDocx probe() {
		return McProbeDocx.body(p("before") + ac("BODY") + p("after")
				+ "<w:sectPr><w:headerReference w:type=\"default\" r:id=\"rId7\"/><w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\"/></w:sectPr>")
				.part("header1.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header", "rId7", "w:hdr", p("head") + ac("HDR"));
	}

	private static HeaderPart header(WordprocessingMLPackage pkg) {
		for (Part part : pkg.getParts().getParts().values()) if (part instanceof HeaderPart) return (HeaderPart) part;
		throw new AssertionError("no header part");
	}

	private static String xml(Object o) {
		return XmlUtils.marshaltoString(o, true, false);
	}

	private String preferBefore;

	@Before
	public void remember() {
		preferBefore = Docx4jProperties.getProperty(McSelection.PROPERTY);
	}

	@After
	public void restore() {
		Docx4jProperties.setProperty(McSelection.PROPERTY, preferBefore == null ? "" : preferBefore);
	}

	/** A DOCUMENT-only feature set still gets the header copied and spliced, and the caller's package keeps every element. */
	@Test
	public void documentOnlyFeaturesStillCopyTheHeaderAndSplice() throws Exception {
		WordprocessingMLPackage pkg = probe().load(McLoadMode.PRESERVE);
		header(pkg).getJaxbElement();
		Set<String> features = new HashSet<String>();
		features.add(ConversionFeatures.PP_COMMON_DEEP_COPY);
		features.add(ConversionFeatures.PP_COMMON_MOVE_PAGEBREAK); // asks for the main part alone
		WordprocessingMLPackage copy = Preprocess.process(pkg, features);
		assertTrue("the step's constant is forced on", features.contains(ConversionFeatures.PP_COMMON_RESOLVE_ALTERNATE_CONTENT));
		assertNotSame(pkg, copy);
		assertNotSame("the header was copied, not shared", header(pkg), header(copy));
		String main = xml(copy.getMainDocumentPart().getJaxbElement());
		String hdr = xml(header(copy).getJaxbElement());
		assertFalse(main, main.contains("mc:AlternateContent"));
		assertFalse(hdr, hdr.contains("mc:AlternateContent"));
		assertTrue("the branch the exporter draws: the Fallback by default", main.contains("BODY_FB") && hdr.contains("HDR_FB"));
		assertFalse(main.contains("BODY_W14") || hdr.contains("HDR_W14"));
		// the caller's package keeps every element
		assertTrue(xml(pkg.getMainDocumentPart().getJaxbElement()).contains("<mc:AlternateContent"));
		assertTrue(xml(header(pkg).getJaxbElement()).contains("<mc:AlternateContent"));
		assertEquals(2, countOf(xml(pkg.getMainDocumentPart().getJaxbElement()), "<mc:Choice") + countOf(xml(header(pkg).getJaxbElement()), "<mc:Choice"));
	}

	/** Run again on a copy already spliced (the FO pre-pass runs Preprocess in place), the step changes nothing. */
	@Test
	public void runTwiceChangesNothing() throws Exception {
		WordprocessingMLPackage pkg = probe().load(McLoadMode.PRESERVE);
		Set<String> features = new HashSet<String>();
		features.add(ConversionFeatures.PP_COMMON_DEEP_COPY);
		WordprocessingMLPackage copy = Preprocess.process(pkg, features);
		String before = xml(copy.getMainDocumentPart().getJaxbElement()) + xml(header(copy).getJaxbElement());
		assertEquals(0, ResolveAlternateContent.process(copy, true));
		Set<String> again = new HashSet<String>(features);
		again.remove(ConversionFeatures.PP_COMMON_DEEP_COPY);
		assertTrue(Preprocess.process(copy, again) == copy);
		assertEquals(before, xml(copy.getMainDocumentPart().getJaxbElement()) + xml(header(copy).getJaxbElement()));
	}

	/** Without the deep copy, the caller's own package is resolved. */
	@Test
	public void withoutTheDeepCopyTheCallersPackageIsResolved() throws Exception {
		WordprocessingMLPackage pkg = probe().load(McLoadMode.PRESERVE);
		Set<String> features = new HashSet<String>();
		features.add(ConversionFeatures.PP_COMMON_MOVE_PAGEBREAK);
		assertTrue(Preprocess.process(pkg, features) == pkg);
		assertFalse(xml(pkg.getMainDocumentPart().getJaxbElement()).contains("mc:AlternateContent"));
	}

	/** In word mode the HTML exporters, visitor and XSLT, draw the branch Word reads. */
	@Test
	public void wordModeDrawsTheBranchWordReads() throws Exception {
		for (int flag : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String html = html(probe().load(McLoadMode.WORD), flag);
			assertTrue(flag + ": " + html, html.contains("BODY_W14") && html.contains("HDR_W14"));
			assertFalse(flag + ": " + html, html.contains("BODY_FB") || html.contains("HDR_FB"));
		}
	}

	/** In preserve mode they draw the Fallback by default, and the Choice under preferChoice; the caller's package keeps every element. */
	@Test
	public void preserveModeDrawsTheFallbackOrThePreferredChoice() throws Exception {
		for (int flag : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			Docx4jProperties.setProperty(McSelection.PROPERTY, "");
			WordprocessingMLPackage pkg = probe().load(McLoadMode.PRESERVE);
			String html = html(pkg, flag);
			assertTrue(flag + ": " + html, html.contains("BODY_FB") && html.contains("HDR_FB"));
			assertFalse(flag + ": " + html, html.contains("BODY_W14") || html.contains("HDR_W14"));
			assertTrue(xml(pkg.getMainDocumentPart().getJaxbElement()).contains("<mc:AlternateContent"));

			Docx4jProperties.setProperty(McSelection.PROPERTY, "w14");
			html = html(probe().load(McLoadMode.PRESERVE), flag);
			assertTrue(flag + ": " + html, html.contains("BODY_W14") && html.contains("HDR_W14"));
			assertFalse(flag + ": " + html, html.contains("BODY_FB") || html.contains("HDR_FB"));
		}
	}

	private static String html(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, out, flags);
		return out.toString("UTF-8");
	}

	private static int countOf(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}
}
