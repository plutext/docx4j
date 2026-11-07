/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.openpackaging.parts;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.io3.LoadOptions;
import org.docx4j.openpackaging.io3.RepairMode;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A prefix in mc:Ignorable with no declaration in scope (WD REQ-323): read as though the list didn't hold it, told once a part, and
 * absent from a part a save writes anew. The files are {@code mc-ignorable/}, the portfolio's round undeclared-ignorable, with Word's
 * saves of those it opens as {@code -word365.docx}.
 */
public class UndeclaredIgnorableTest {

	private static final String MC = "http://schemas.openxmlformats.org/markup-compatibility/2006";

	/** Each file's mce.undeclared_prefix warnings, part and prefixes: the source's own, as UiSaves read them. */
	private static final Map<String, String> WARNED = new LinkedHashMap<String, String>();
	static {
		WARNED.put("ui-00-control", "");
		WARNED.put("ui-01-main-unknown", "/word/document.xml=zz9");
		WARNED.put("ui-02-main-known", "/word/document.xml=w14");
		WARNED.put("ui-03-main-several", "/word/document.xml=w15 wp14");
		WARNED.put("ui-04-main-lower-known", "/word/document.xml=w14");
		WARNED.put("ui-05-main-lower-unknown", "/word/document.xml=zz9");
		WARNED.put("ui-06-main-declared-unknown", "");
		WARNED.put("ui-07-main-leading-space", "");
		WARNED.put("ui-10-header", "/word/header1.xml=zz9");
		WARNED.put("ui-11-footnotes", "/word/footnotes.xml=zz9");
		WARNED.put("ui-12-styles", "/word/styles.xml=zz9");
		WARNED.put("ui-13-settings", "/word/settings.xml=zz9");
		WARNED.put("ui-14-all-parts-known", "/word/document.xml=w14 /word/footnotes.xml=w14 /word/header1.xml=w14 /word/settings.xml=w14 /word/styles.xml=w14");
	}

	private static File file(String name) throws Exception {
		return new File(UndeclaredIgnorableTest.class.getClassLoader().getResource("mc-ignorable/" + name + ".docx").toURI());
	}

	private static String text(WordprocessingMLPackage pkg) {
		return Texts.plainText(pkg, new TextOptions()).trim();
	}

	private static String warned(WordprocessingMLPackage pkg) {
		List<String> out = new ArrayList<String>();
		for (TextWarning w : Texts.of(pkg, new TextOptions()).warnings()) {
			if (TextWarning.MCE_UNDECLARED_PREFIX.equals(w.getKind())) out.add(w.getSelector() + "=" + w.getDetail());
		}
		java.util.Collections.sort(out);
		return String.join(" ", out);
	}

	/** Every part's mc:Ignorable prefix that names nothing in scope, as part:prefix. */
	private static List<String> undeclared(byte[] zip) throws Exception {
		List<String> out = new ArrayList<String>();
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				if (!e.getName().endsWith(".xml") || e.getName().startsWith("[")) continue;
				org.w3c.dom.Element root = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(in.readAllBytes())).getDocumentElement();
				for (String p : root.getAttributeNS(MC, "Ignorable").trim().split("\\s+")) {
					if (!p.isEmpty() && root.lookupNamespaceURI(p) == null) out.add(e.getName() + ":" + p);
				}
			}
		}
		return out;
	}

	private static byte[] entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) if (e.getName().equals(name)) return in.readAllBytes();
		}
		throw new AssertionError(name);
	}

	/** Each part read, so a save writes each anew. */
	private static byte[] readAndSaved(WordprocessingMLPackage pkg) throws Exception {
		text(pkg);
		for (Part p : pkg.getParts().getParts().values()) if (p instanceof JaxbXmlPart) ((JaxbXmlPart<?>) p).getJaxbElement();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		return out.toByteArray();
	}

	@Test
	public void readsAsWordsSaveAndTellsOnceAPart() throws Exception {
		for (Map.Entry<String, String> e : WARNED.entrySet()) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file(e.getKey()));
			assertEquals(e.getKey(), e.getValue(), warned(pkg));
			if (!e.getKey().startsWith("ui-05")) assertEquals(e.getKey(), text(WordprocessingMLPackage.load(file(e.getKey() + "-word365"))), text(pkg));
		}
	}

	/** The repair policy's section 5: a part written anew holds none; one written from its source bytes keeps them. */
	@Test
	public void aSaveWritesNoneAnewAndKeepsTheSourcesBytes() throws Exception {
		for (String name : WARNED.keySet()) {
			assertTrue(name, undeclared(readAndSaved(WordprocessingMLPackage.load(file(name)))).isEmpty());
			ByteArrayOutputStream untouched = new ByteArrayOutputStream();
			WordprocessingMLPackage.load(file(name)).save(untouched);
			byte[] source = java.nio.file.Files.readAllBytes(file(name).toPath());
			assertArrayEquals(name, entry(source, "word/document.xml"), entry(untouched.toByteArray(), "word/document.xml"));
		}
	}

	/** A prefix docx4j knows is declared, and kept; one it doesn't is dropped; one the source declared stays declared (ui-06). */
	@Test
	public void knownIsDeclaredUnknownDroppedDeclaredKept() throws Exception {
		String known = new String(entry(readAndSaved(WordprocessingMLPackage.load(file("ui-02-main-known"))), "word/document.xml"), "UTF-8");
		assertTrue(known, known.matches("(?s).*mc:Ignorable=\"[^\"]*\\bw14\\b.*"));
		String unknown = new String(entry(readAndSaved(WordprocessingMLPackage.load(file("ui-01-main-unknown"))), "word/document.xml"), "UTF-8");
		assertFalse(unknown, unknown.contains("zz9"));
		String declared = new String(entry(readAndSaved(WordprocessingMLPackage.load(file("ui-06-main-declared-unknown"))), "word/document.xml"), "UTF-8");
		assertTrue(declared, declared.contains("xmlns:zz9=\"urn:ui:zz9\""));
	}

	/** At every value of repair alike. */
	@Test
	public void everyRepairValueReadsItAlike() throws Exception {
		for (RepairMode mode : RepairMode.values()) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file("ui-14-all-parts-known"), new LoadOptions().repairMode(mode));
			assertEquals(mode.name(), WARNED.get("ui-14-all-parts-known"), warned(pkg));
		}
	}

	/** A caller's replacement content ends it. */
	@Test
	public void replacingTheContentEndsIt() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file("ui-01-main-unknown"));
		text(pkg);
		assertEquals(java.util.Collections.singletonList("zz9"), pkg.getMainDocumentPart().getUndeclaredIgnorable());
		pkg.getMainDocumentPart().setJaxbElement(WordprocessingMLPackage.load(file("ui-00-control")).getMainDocumentPart().getContents());
		assertTrue(pkg.getMainDocumentPart().getUndeclaredIgnorable().isEmpty());
	}
}
