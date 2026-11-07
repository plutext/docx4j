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

 */package org.docx4j.openpackaging.parts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.jaxb.McLoadMode;
import org.docx4j.jaxb.ResolvedAlternateContent;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.LoadOptions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * mc:AlternateContent as a part's root (CR-079 phase 1; WD REQ-317). The files are {@code mc-root/}, with Word's saves of them
 * as {@code -word365.docx}: a source reads as Word's save of it reads.
 */
public class McRootTest {

	private static final String[] AS_WORD = { "rw-20-mc-root-choice", "rw-21-mc-root-fallback", "rw-22-mc-root-fallback-only", "rw-23-mc-root-two-documents",
			"rw-24-mc-root-nothing-selected", "rw-26-mc-root-two-in-fallback", "rw-27-mc-root-two-in-choice-sizes", "c79-10-hdr-root", "c79-11-ftr-root",
			"c79-12-footnotes-root", "c79-13-endnotes-root", "c79-14-comments-root", "c79-15-styles-root", "c79-16-numbering-root", "c79-17-settings-root",
			"c79-18-hdr-root-unknown-choice", "c79-20-two-docs-background-first", "c79-21-two-docs-background-second", "c79-50-unknown-in-taken",
			"c79-51-unknown-in-not-taken", "c79-70-bare-body-in-branch", "c79-71-empty-branch-taken" };

	private static File file(String name) throws Exception {
		return new File(McRootTest.class.getClassLoader().getResource("mc-root/" + name + ".docx").toURI());
	}

	private static WordprocessingMLPackage load(String name) throws Exception {
		return WordprocessingMLPackage.load(file(name));
	}

	private static String text(WordprocessingMLPackage pkg) {
		return Texts.plainText(pkg, new TextOptions()).trim();
	}

	private static byte[] saved(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		return out.toByteArray();
	}

	private static String entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				if (e.getName().equals(name)) return new String(in.readAllBytes(), "UTF-8");
			}
		}
		return null;
	}

	@Test
	public void aSourceReadsAsWordsSaveOfIt() throws Exception {
		for (String name : AS_WORD) assertEquals(name, text(load(name + "-word365")), text(load(name)));
	}

	/** Every part Word was seen to read the branch of, and a save of each, which failed before. */
	@Test
	public void aPartsRootReadsTheBranch() throws Exception {
		WordprocessingMLPackage header = load("c79-10-hdr-root");
		Hdr hdr = header.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader().getContents();
		assertTrue(org.docx4j.XmlUtils.marshaltoString(hdr).contains("CHOICE HEADER"));
		assertTrue(entry(saved(header), "word/header1.xml").contains("CHOICE HEADER"));

		WordprocessingMLPackage unknownChoice = load("c79-18-hdr-root-unknown-choice");
		Hdr fallback = unknownChoice.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader().getContents();
		assertTrue(org.docx4j.XmlUtils.marshaltoString(fallback).contains("FALLBACK HEADER"));

		assertEquals("right", load("c79-15-styles-root").getMainDocumentPart().getStyleDefinitionsPart().getStyleById("Probe").getPPr().getJc().getVal().value().toLowerCase());
		assertEquals(BigInteger.valueOf(1000), load("c79-17-settings-root").getMainDocumentPart().getDocumentSettingsPart().getContents().getDefaultTabStop().getVal());
		assertTrue(org.docx4j.XmlUtils.marshaltoString(load("c79-16-numbering-root").getMainDocumentPart().getNumberingDefinitionsPart().getContents()).contains("CHOICE%1"));
		assertTrue(org.docx4j.XmlUtils.marshaltoString(load("c79-14-comments-root").getMainDocumentPart().getCommentsPart().getContents()).contains("CHOICE COMMENT"));
		for (String name : AS_WORD) saved(load(name));
	}

	/** D2: the element stays open to inspection, every branch of it. */
	@Test
	public void theElementIsRecordedWhole() throws Exception {
		WordprocessingMLPackage pkg = load("rw-20-mc-root-choice");
		MainDocumentPart main = pkg.getMainDocumentPart();
		assertTrue(main.getContents() instanceof Document);
		List<ResolvedAlternateContent> records = main.getResolvedAlternateContent();
		assertEquals(1, records.size());
		assertTrue(records.get(0).getBranch() instanceof AlternateContent.Choice);
		assertNotNull(records.get(0).getElement().getFallback());
		assertNull(main.getContents().getParent());
	}

	/** D2, REQ-305: an empty document, the words in the record, and the warning with no address, since a root has none. */
	@Test
	public void nothingSelectedIsAnEmptyDocument() throws Exception {
		WordprocessingMLPackage pkg = load("rw-24-mc-root-nothing-selected");
		assertTrue(pkg.getMainDocumentPart().getContent().isEmpty());
		assertNull(pkg.getMainDocumentPart().getResolvedAlternateContent().get(0).getBranch());
		TextResult result = Texts.of(pkg, new TextOptions());
		boolean warned = false;
		for (TextWarning w : result.warnings()) {
			if (TextWarning.MCE_NOTHING_SELECTED.equals(w.getKind())) {
				warned = true;
				assertEquals("/word/document.xml", w.getSelector());
				assertNull(w.getAddress());
			}
		}
		assertTrue(warned);
	}

	/** D4: a part that was read saves as an ordinary one, the branch not read gone; one that wasn't stays as it came. */
	@Test
	public void aSaveOfAPartReadWritesAnOrdinaryOne() throws Exception {
		WordprocessingMLPackage pkg = load("rw-20-mc-root-choice");
		pkg.getMainDocumentPart().getContents();
		String xml = entry(saved(pkg), "word/document.xml");
		assertTrue(xml, xml.contains("<w:document"));
		assertFalse(xml, xml.contains("AlternateContent"));
		assertFalse(xml, xml.contains("FALLBACK WORDS"));
		assertTrue(entry(saved(load("rw-21-mc-root-fallback")), "word/document.xml").contains("AlternateContent"));
	}

	/** D3: the part has to hold a document, in either mode. */
	@Test
	public void preserveResolvesTheRootToo() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file("rw-20-mc-root-choice"), new LoadOptions().mcLoadMode(McLoadMode.PRESERVE));
		assertEquals("CHOICE WORDS", text(pkg));
	}

	/** §2.1's disagreement: an unknown element sends the part through the stylesheet, which reads the same branch. */
	@Test
	public void theStylesheetRouteReadsTheSameBranch() throws Exception {
		assertEquals("CHOICE WORDS", text(load("c79-50-unknown-in-taken")));
		assertEquals("CHOICE WORDS", text(load("c79-51-unknown-in-not-taken")));
	}

	/** §7: the binder re-created, so that an XPath meets the document and not the element it came from. */
	@Test
	public void theBinderSeesTheDocument() throws Exception {
		String property = "docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart";
		String before = Docx4jProperties.getProperty(property);
		Docx4jProperties.setProperty(property, "true");
		try {
			MainDocumentPart main = load("rw-20-mc-root-choice").getMainDocumentPart();
			List<Object> ps = main.getJAXBNodesViaXPath("//w:p", false);
			assertEquals(1, ps.size());
			assertTrue(org.docx4j.XmlUtils.unwrap(ps.get(0)) instanceof P);
			assertTrue(main.getContent().contains(org.docx4j.XmlUtils.unwrap(ps.get(0))));
		} finally {
			Docx4jProperties.setProperty(property, before == null ? "false" : before);
		}
	}

	/** D9: a root Word reads as a document stands as one. */
	@Test
	public void aBodyOrAParagraphAtTheRootReads() throws Exception {
		for (String name : new String[] { "or-32-body", "mr-32-p-root" }) {
			assertTrue(name, text(load(name)).length() > 0);
			assertEquals(name, text(load(name + "-word365")), text(load(name)));
		}
	}

	/** D9: any other root is named once, in place of a ClassCastException at the first use, and a save keeps the bytes. */
	@Test
	public void aRootItCantHoldIsNamed() throws Exception {
		WordprocessingMLPackage pkg = load("or-10-hdr");
		try {
			pkg.getMainDocumentPart().getContents();
			fail("read");
		} catch (Docx4JException refused) {
			assertTrue(String.valueOf(refused.getCause()), String.valueOf(refused.getCause()).contains("has a root it can't hold"));
		}
		assertTrue(pkg.getMainDocumentPart().isContentUnreadable());
		assertTrue(entry(saved(pkg), "word/document.xml").contains("<w:hdr"));
	}
}
