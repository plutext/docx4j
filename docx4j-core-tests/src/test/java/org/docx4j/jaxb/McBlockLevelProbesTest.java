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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-051 section 4, "word mode, against Word": Jason's probes ({@code mc-fun/} on the share,
 * copied to the test resources) and their Word 365 saves ({@code <name>-word365.docx}, 20 October
 * 2026).  A load and save in {@link McBlockLevel#WORD} mode gives the text Word's save gave; for
 * the {@code mc-txbx-*} probes the Choice paragraph with a bare wps box where Word wrote a
 * regenerated Fallback (section 3.5), which reads the same.  And in {@link McBlockLevel#PRESERVE}
 * mode every branch survives a load and a save.
 */
public class McBlockLevelProbesTest {

	private static final String[] PROBES = {
			"mc-text-newest-first", "mc-run-newest-first", "mc-sdt-run-newest-first",
			"mc-block-newest-first", "mc-sdt-block-newest-first", "mc-tc-block-newest-first",
			"mc-row-newest-first", "mc-sdt-row-newest-first", "mc-cell-newest-first", "mc-sdt-cell-newest-first",
			"mc-block-nofallback", "mc-mixed",
			"mc-txbx-block", "mc-txbx-sdt-block", "mc-txbx-tc-block", "mc-txbx-run" };

	/**
	 * Probes whose Word 365 save is on the share.  mc-text-, mc-run- and mc-block-newest-first have
	 * none (mc-run-word365 is of the original-order probe): paper section 7.8's measurement, that
	 * Word 365 shows w16sdtfl for them, is checked by {@link #blockLevelProbe}.
	 */
	private static final String[] WITH_WORD_SAVE = {
			"mc-sdt-block-newest-first", "mc-tc-block-newest-first",
			"mc-row-newest-first", "mc-sdt-row-newest-first", "mc-cell-newest-first", "mc-sdt-cell-newest-first",
			"mc-block-nofallback", "mc-mixed", "mc-sdt-run-newest-first",
			"mc-txbx-block", "mc-txbx-sdt-block", "mc-txbx-tc-block", "mc-txbx-run" };

	/** Loaded, with the main document part unmarshalled (a save streams a part not yet unmarshalled as it was stored). */
	private static WordprocessingMLPackage load(String name, McBlockLevel mode) throws Exception {
		InputStream is = ResourceUtils.getResource("mc-fun/" + name + ".docx");
		assertNotNull(name, is);
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(is, mode);
		pkg.getMainDocumentPart().getContents();
		return pkg;
	}

	private static String text(WordprocessingMLPackage pkg) {
		return Texts.normalize(Texts.plainText(pkg, new TextOptions()));
	}

	private static int alternateContentCount(String xml) {
		int n = 0;
		for (int i = xml.indexOf("<mc:AlternateContent"); i >= 0; i = xml.indexOf("<mc:AlternateContent", i + 1)) n++;
		return n;
	}

	/** Each probe, loaded and saved in word mode, reads as Word 365's save of it reads. */
	@Test
	public void wordModeSaveReadsAsWordsSave() throws Exception {
		for (String name : WITH_WORD_SAVE) {
			WordprocessingMLPackage pkg = load(name, McBlockLevel.WORD);
			WordprocessingMLPackage saved = McProbeDocx.roundTrip(pkg, McBlockLevel.WORD);
			WordprocessingMLPackage words = load(name + "-word365", McBlockLevel.WORD);
			assertEquals(name, text(words), text(saved));
			assertEquals(name + " (before the save)", text(words), text(pkg));
		}
	}

	/**
	 * Paper section 7.8's docx4j row (CR-051 phase 3, 21 October 2026): Jason's three original-order probes and their
	 * newest-first copies, read as Word 365 reads them. Text and run level are the text model's selection
	 * ({@code McPurpose.TEXT}, the published understood set); block level the load-time splice. The original order
	 * gives the first Choice, w14, at every level; the newest-first order the newest namespace docx4j understands,
	 * w16sdtfl, as Word 365 shows.
	 */
	@Test
	public void paperSection78() throws Exception {
		String[][] cells = { { "mc-text", "w14" }, { "mc-run", "w14" }, { "mc-block", "w14" },
				{ "mc-text-newest-first", "w16sdtfl" }, { "mc-run-newest-first", "w16sdtfl" }, { "mc-block-newest-first", "w16sdtfl" } };
		for (String[] cell : cells) {
			String text = text(load(cell[0], McBlockLevel.WORD));
			assertTrue(cell[0] + ": " + text, text.endsWith(cell[1]));
			for (String other : new String[] { "w15", "w16se", "w16cid", "w16cex", "w16sdtdh", "w16du", "wp14", "fallback", "TheEarthisround" }) {
				assertFalse(cell[0] + " shows " + other + ": " + text, text.contains(other));
			}
		}
	}

	/** The block-level probe: Word's branch in the body at the element's index, the record, and the root's mc:Ignorable. */
	@Test
	public void blockLevelProbe() throws Exception {
		WordprocessingMLPackage pkg = load("mc-block-newest-first", McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Object> content = mdp.getContent();
		assertEquals("two paragraphs, the promoted one, the sectPr apart", 3, countParagraphs(content));
		P promoted = (P) XmlUtils.unwrap(content.get(2));
		assertTrue(XmlUtils.marshaltoString(promoted).contains(">w16sdtfl<"));
		List<ResolvedAlternateContent> records = mdp.getResolvedAlternateContent();
		assertEquals(1, records.size());
		assertEquals("w:body", records.get(0).getContainerName());
		assertEquals(2, records.get(0).getIndex());
		assertEquals("w16sdtfl", ((AlternateContent.Choice) records.get(0).getBranch()).getRequires());
		assertEquals("every Choice is still in the record", 10, records.get(0).getElement().getChoice().size());
		assertEquals("the probe's root had no mc:Ignorable; the selected Choice's prefix is listed now", "w16sdtfl",
				((Document) mdp.getJaxbElement()).getIgnorable());

		String saved = McProbeDocx.entry(McProbeDocx.save(pkg), "word/document.xml");
		assertEquals("the save writes no element", 0, alternateContentCount(saved));
		String root = McProbeDocx.rootTag(saved);
		assertTrue(root, root.contains("mc:Ignorable=\"w16sdtfl\"") && root.contains("xmlns:w16sdtfl="));
	}

	/** With ten Choices and no Fallback, the first understood Choice, as Word 365 kept it. */
	@Test
	public void noFallbackProbe() throws Exception {
		WordprocessingMLPackage pkg = load("mc-block-nofallback", McBlockLevel.WORD);
		List<Object> content = pkg.getMainDocumentPart().getContent();
		assertTrue(XmlUtils.marshaltoString(XmlUtils.unwrap(content.get(2))).contains(">w16sdtfl<"));
		assertEquals(1, pkg.getMainDocumentPart().getResolvedAlternateContent().size());
	}

	/** mc-mixed: the body's element is resolved; the run-level content control's, the hyperlink's and the paragraph's stay whole (section 1's collateral resolution, fixed). */
	@Test
	public void mixedProbeResolvesTheBodysElementOnly() throws Exception {
		WordprocessingMLPackage pkg = load("mc-mixed", McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		assertEquals(1, mdp.getResolvedAlternateContent().size());
		String saved = McProbeDocx.entry(McProbeDocx.save(pkg), "word/document.xml");
		assertEquals("the three run-level elements are written whole", 3, alternateContentCount(saved));
		String text = Texts.plainText(pkg, new TextOptions());
		assertTrue(text, text.contains("SDTRUN_W14") && text.contains("HLINK_W14") && text.contains("INP_W14") && text.contains("BODY_W14"));
		assertFalse(text, text.contains("FALLBACK"));
	}

	/** In preserve mode every probe's elements survive a load and a save, with every branch and the Choices' prefixes declared. */
	@Test
	public void preserveModeKeepsEveryBranch() throws Exception {
		for (String name : PROBES) {
			WordprocessingMLPackage pkg = load(name, McBlockLevel.PRESERVE);
			String original = McProbeDocx.entry(bytesOf(name), "word/document.xml");
			int elements = alternateContentCount(original);
			boolean rowOrCell = name.contains("-row-") || name.contains("-cell-");
			String saved = McProbeDocx.entry(McProbeDocx.save(pkg), "word/document.xml");
			if (rowOrCell) {
				// CR-051 section 5, phase 1: row- and cell-level elements are resolved until phase 2
				assertEquals(name + ": resolved until phase 2", 0, alternateContentCount(saved));
				continue;
			}
			assertEquals(name + ": every element written", elements, alternateContentCount(saved));
			assertEquals(name + ": every Choice written", countOf(original, "<mc:Choice "), countOf(saved, "<mc:Choice "));
			assertEquals(name + ": the Fallbacks written", countOf(original, "<mc:Fallback"), countOf(saved, "<mc:Fallback"));
			assertTrue(name + ": nothing recorded", pkg.getMainDocumentPart().getResolvedAlternateContent().isEmpty());
			String root = McProbeDocx.rootTag(saved);
			if (name.contains("newest-first") || name.equals("mc-block-nofallback")) {
				for (String prefix : new String[] { "w14", "w15", "w16se", "w16cid", "w16", "w16cex", "w16sdtdh", "w16sdtfl", "w16du", "wp14" }) {
					assertTrue(name + ": xmlns:" + prefix + " declared: " + root, root.contains("xmlns:" + prefix + "="));
				}
			}
			WordprocessingMLPackage again = McProbeDocx.roundTrip(pkg, McBlockLevel.PRESERVE);
			assertEquals(name + ": and reads the same", text(pkg), text(again));
		}
	}

	private static byte[] bytesOf(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource("mc-fun/" + name + ".docx")) {
			return is.readAllBytes();
		}
	}

	private static int countOf(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}

	private static int countParagraphs(List<Object> content) {
		int n = 0;
		for (Object o : content) if (XmlUtils.unwrap(o) instanceof P) n++;
		return n;
	}

	/** The load option through a stream and the package override are the same mode for a probe. */
	@Test
	public void streamLoadOptionApplies() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(bytesOf("mc-block-newest-first")), McBlockLevel.PRESERVE);
		assertTrue(XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(2)) instanceof AlternateContent);
	}
}
