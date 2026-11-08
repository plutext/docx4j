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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.junit.Test;

/**
 * WD REQ-204: an extractor doesn't modify the package it reads. A text reading, with list labels and without, and an inspection
 * leave a package's parts and its main part's relationships as they were, so a later save writes nothing the reading made.
 * Until 17.3.6 the list numbering asked for a property resolver that added docx4j's default styles part to a package with none.
 */
public class ReadLeavesThePackageTest {

	private static final String[] DIRECTORIES = { "", "mc-root", "mc-ignorable", "package-wiring", "text-extraction/broken-part",
			"fields/cr067-absent-properties" };

	private static File resource(String path) throws Exception {
		return new File(ReadLeavesThePackageTest.class.getClassLoader().getResource(path.isEmpty() ? "loadAndSave.docx" : path).toURI());
	}

	private static String shape(WordprocessingMLPackage pkg) {
		TreeSet<String> names = new TreeSet<String>();
		for (PartName n : pkg.getParts().getParts().keySet()) names.add(n.getName());
		int rels = pkg.getMainDocumentPart() == null || pkg.getMainDocumentPart().getRelationshipsPart() == null ? 0
				: pkg.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship().size();
		return names + " rels=" + rels;
	}

	private static void read(WordprocessingMLPackage pkg) {
		Texts.of(pkg, new TextOptions().listLabels(true));
		Texts.of(pkg, new TextOptions());
		Inspect.of(pkg);
	}

	@Test
	public void aReadingLeavesEveryFixtureAsItWas() throws Exception {
		List<String> changed = new ArrayList<String>();
		int read = 0;
		for (String directory : DIRECTORIES) {
			File dir = directory.isEmpty() ? resource("").getParentFile() : resource(directory);
			for (File f : dir.listFiles((d, name) -> name.endsWith(".docx"))) {
				WordprocessingMLPackage pkg;
				try {
					pkg = WordprocessingMLPackage.load(f);
				} catch (Exception refused) {
					continue; // a package the load refuses has nothing to read
				}
				String before = shape(pkg);
				try {
					read(pkg);
				} catch (RuntimeException refused) {
					continue; // a read refused, as REQ-318 and REQ-321 have some
				}
				read++;
				if (!before.equals(shape(pkg))) changed.add(directory + "/" + f.getName() + ": " + before + " -> " + shape(pkg));
			}
		}
		assertTrue(changed.toString(), changed.isEmpty());
		assertTrue(String.valueOf(read), read > 200);
	}

	/** The two that changed: a numbered paragraph and no styles part. The numbers are read all the same, and a save writes no styles part. */
	@Test
	public void numbersAreReadWithNoStylesPartAdded() throws Exception {
		for (String[] c : new String[][] { { "mc-root/c79-16-numbering-root.docx", "CHOICE1\tLISTED WORDS" },
				{ "text-extraction/broken-part/broken-part-styles-absent.docx", "DIRECT_LIST_1.\tfirst item" } }) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(resource(c[0]));
			assertTrue(c[0], Texts.plainText(pkg, new TextOptions().listLabels(true)).contains(c[1]));
			assertNull(c[0], pkg.getMainDocumentPart().getStyleDefinitionsPart());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pkg.save(out);
			try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
				for (ZipEntry e; (e = in.getNextEntry()) != null; ) assertFalse(c[0], e.getName().equals("word/styles.xml"));
			}
		}
	}

	/** Someone who asks for the package's resolver still gets the styles part it has always added: only a reader's changes. */
	@Test
	public void theResolverAsBeforeStillAddsIt() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(resource("mc-root/c79-16-numbering-root.docx"));
		assertTrue(pkg.getMainDocumentPart().getPropertyResolverAddingNothing() != null);
		assertNull(pkg.getMainDocumentPart().getStyleDefinitionsPart());
		pkg.getMainDocumentPart().getPropertyResolver();
		assertTrue(pkg.getMainDocumentPart().getStyleDefinitionsPart() != null);
		assertEquals(pkg.getMainDocumentPart().getPropertyResolver(), pkg.getMainDocumentPart().getPropertyResolverAddingNothing());
	}
}
