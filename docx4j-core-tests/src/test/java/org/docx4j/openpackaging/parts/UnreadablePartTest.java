/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  docx4j is licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.openpackaging.parts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * CR-065 phase 1 (D1): a part whose source could not be read (its unmarshal failed) is never
 * replaced by an empty one. Before, MainDocumentPart.getContent() created a fresh Document when
 * getJaxbElement() returned null, so an edit after a failed read saved the new content alone.
 *
 * The fixtures are not well-formed XML (a closing tag cut short, an element left open), the one
 * failure no lenient reading mends: phase 1 used "abc" in an integer attribute, which phase 2
 * (LenientInteger, D3) now reads by dropping the attribute, so the part reads.
 */
public class UnreadablePartTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();
	private static final String ORIGINAL = "ORIGINAL CONTENT stays";

	@Test
	public void aMainPartThatCannotBeReadIsNotReplacedByAnEditAndSavesItsBytes() throws Exception {
		byte[] docx = fixture("word/document.xml", "</w:body>", "</w:body");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		assertNull("the failed read returns null", mdp.getJaxbElement());
		assertTrue(mdp.isContentUnreadable());
		assertNull("and again, without a second attempt", mdp.getJaxbElement());

		assertRefused(() -> mdp.getContent());
		assertRefused(() -> mdp.addParagraphOfText("NEW"));
		assertRefused(() -> mdp.addObject(F.createP()));

		String saved = partAfterSave(pkg, "word/document.xml");
		assertTrue("an untouched save keeps the source bytes", saved.contains(ORIGINAL));
		assertTrue(saved, saved.contains("</w:body") && !saved.contains("</w:body>"));
		assertFalse(saved.contains("NEW"));
	}

	@Test
	public void aHeaderThatCannotBeReadIsNotReplaced() throws Exception {
		byte[] docx = fixture("word/header.xml", "</w:hdr>", "</w:hdr");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
		HeaderPart hp = null;
		for (Part p : pkg.getParts().getParts().values()) if (p instanceof HeaderPart) hp = (HeaderPart) p;
		assertNotNull(hp);
		final HeaderPart header = hp;

		assertNull(header.getJaxbElement());
		assertTrue(header.isContentUnreadable());
		assertRefused(() -> header.getContent());
		assertTrue("the body still reads", pkg.getMainDocumentPart().getContent().size() > 0);
		String savedHeader = partAfterSave(pkg, "word/header.xml");
		assertTrue(savedHeader, savedHeader.contains("</w:hdr") && !savedHeader.contains("</w:hdr>"));
	}

	@Test
	public void settingsThatCannotBeReadAreNotReplaced() throws Exception {
		byte[] docx = fixture("word/settings.xml", "<w:defaultTabStop w:val=\"720\"/>", "<w:defaultTabStop w:val=\"720\">");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
		DocumentSettingsPart settings = pkg.getMainDocumentPart().getDocumentSettingsPart();

		assertNull(settings.getJaxbElement());
		assertTrue(settings.isContentUnreadable());
		assertRefused(() -> settings.removeEnforcement());
		assertRefused(() -> settings.attachTemplate("template.dotx"));
		String savedSettings = partAfterSave(pkg, "word/settings.xml");
		assertTrue(savedSettings, savedSettings.contains("<w:defaultTabStop w:val=\"720\">") && !savedSettings.contains("<w:defaultTabStop w:val=\"720\"/>"));
	}

	@Test
	public void aReplacementIsTheCallersDecision() throws Exception {
		byte[] docx = fixture("word/document.xml", "</w:body>", "</w:body");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		assertNull(mdp.getJaxbElement());

		org.docx4j.wml.Document replacement = F.createDocument();
		mdp.setJaxbElement(replacement);
		assertFalse(mdp.isContentUnreadable());
		assertSame(replacement, mdp.getJaxbElement());
		mdp.addParagraphOfText("NEW");
		assertTrue(partAfterSave(pkg, "word/document.xml").contains("NEW"));
	}

	@Test
	public void aCreatedPackageStillGetsNewContentFromTheConveniences() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		HeaderPart hp = new HeaderPart();
		pkg.getMainDocumentPart().addTargetPart(hp);
		assertNull("a part added to a created package has no content yet", hp.getJaxbElement());
		assertFalse(hp.isContentUnreadable());
		hp.getContent().add(F.createP());
		assertEquals(1, hp.getJaxbElement().getContent().size());
		pkg.getMainDocumentPart().getDocumentSettingsPart().removeEnforcement();
	}

	private static void assertRefused(Runnable edit) {
		try {
			edit.run();
			fail("the edit was not refused");
		} catch (Docx4JRuntimeException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("could not be read"));
			assertNotNull("the read failure is the cause", expected.getCause());
		}
	}

	/** A docx4j-made document, saved, with one replacement made in the named part's XML. */
	private static byte[] fixture(String partName, String from, String to) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		P p = F.createP(); PPr pPr = F.createPPr(); PPrBase.Spacing sp = F.createPPrBaseSpacing();
		sp.setBefore(BigInteger.valueOf(120)); pPr.setSpacing(sp); p.setPPr(pPr);
		R r = F.createR(); Text t = F.createText(); t.setValue(ORIGINAL); r.getContent().add(t); p.getContent().add(r);
		mdp.getContent().add(p);
		// a header with the same paragraph shape
		HeaderPart hp = new HeaderPart(); Relationship rel = mdp.addTargetPart(hp);
		Hdr hdr = F.createHdr(); hdr.getContent().add(org.docx4j.XmlUtils.deepCopy(p)); hp.setJaxbElement(hdr);
		HeaderReference href = F.createHeaderReference(); href.setId(rel.getId()); href.setType(HdrFtrRef.DEFAULT);
		SectPr sectPr = F.createSectPr(); sectPr.getEGHdrFtrReferences().add(href);
		mdp.getJaxbElement().getBody().setSectPr(sectPr);
		// a default tab stop in settings
		org.docx4j.wml.CTTwipsMeasure dts = F.createCTTwipsMeasure(); dts.setVal(BigInteger.valueOf(720));
		mdp.getDocumentSettingsPart().getJaxbElement().setDefaultTabStop(dts);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); pkg.save(bos);
		Map<String, byte[]> entries = unzip(bos.toByteArray());
		String xml = new String(entries.get(partName), StandardCharsets.UTF_8);
		assertTrue(partName + " holds " + from, xml.contains(from));
		entries.put(partName, xml.replace(from, to).getBytes(StandardCharsets.UTF_8));
		return zip(entries);
	}

	private static String partAfterSave(WordprocessingMLPackage pkg, String partName) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); pkg.save(bos);
		return new String(unzip(bos.toByteArray()).get(partName), StandardCharsets.UTF_8);
	}

	private static Map<String, byte[]> unzip(byte[] zip) throws IOException {
		Map<String, byte[]> m = new java.util.LinkedHashMap<>();
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = zis.getNextEntry()) != null;) m.put(e.getName(), zis.readAllBytes());
		}
		return m;
	}

	private static byte[] zip(Map<String, byte[]> entries) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(bos)) {
			for (Map.Entry<String, byte[]> e : entries.entrySet()) { zos.putNextEntry(new ZipEntry(e.getKey())); zos.write(e.getValue()); zos.closeEntry(); }
		}
		return bos.toByteArray();
	}
}
