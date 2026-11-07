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
package org.docx4j.openpackaging.io3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.jaxb.McLoadMode;
import org.docx4j.model.text.Inspect;
import org.docx4j.model.text.Inspection;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.packages.LoadFinding;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.junit.Test;

/**
 * A package with two readings (CR-075 phase 1; WD REQ-206): refused by default, naming the entry; read as Word's repair gives
 * it under {@link RepairMode#WORD}, from a file and from a stream alike.
 */
public class PackageWiringTest {

	private static final String[] DUPLICATED = { "dup-entry-document", "dup-entry-same-bytes", "dup-entry-content-types", "dup-entry-part-rels",
			"two-rels-entries-document-first", "two-rels-entries-other-first", "case-variant-exact-first", "case-variant-exact-last", "dup-central-reversed" };

	private static File file(String path) throws Exception {
		return new File(PackageWiringTest.class.getClassLoader().getResource(path).toURI());
	}

	private static File wiring(String name) throws Exception {
		return file("package-wiring/" + name + ".docx");
	}

	private static byte[] bytes(String name) throws Exception {
		return Files.readAllBytes(wiring(name).toPath());
	}

	private static LoadOptions repair() {
		return new LoadOptions().repairMode(RepairMode.WORD);
	}

	/** The same load from a file and from a stream. */
	private static List<WordprocessingMLPackage> both(String name, LoadOptions options) throws Exception {
		return Arrays.asList(WordprocessingMLPackage.load(wiring(name), options),
				WordprocessingMLPackage.load(new ByteArrayInputStream(bytes(name)), options));
	}

	private static String text(WordprocessingMLPackage pkg) {
		return Texts.plainText(pkg, new TextOptions()).trim();
	}

	/** The bytes up to the central directory: a zip with local headers alone. */
	private static byte[] noCentralDirectory(byte[] zip) {
		for (int i = 0; i + 3 < zip.length; i++) {
			if (zip[i] == 'P' && zip[i + 1] == 'K' && zip[i + 2] == 1 && zip[i + 3] == 2) return Arrays.copyOf(zip, i);
		}
		throw new AssertionError("no central directory");
	}

	/** REQ-206: refused from a file and from a stream, the refusal naming the entry and how many there are. */
	@Test
	public void entriesOfOneNameAreRefusedByDefault() throws Exception {
		Map<String, String> named = new LinkedHashMap<String, String>();
		named.put("dup-entry-document", "word/document.xml (2 entries)");
		named.put("dup-entry-same-bytes", "word/document.xml (2 entries)");
		named.put("dup-entry-content-types", "[Content_Types].xml (2 entries)");
		named.put("dup-entry-part-rels", "word/_rels/document.xml.rels (2 entries)");
		named.put("two-rels-entries-document-first", "_rels/.rels (2 entries)");
		named.put("case-variant-exact-first", "word/document.xml (2 entries)");
		named.put("case-variant-exact-last", "Word/Document.xml (2 entries)");
		named.put("dup-central-reversed", "word/document.xml (2 entries)");
		for (Map.Entry<String, String> e : named.entrySet()) {
			for (int route = 0; route < 2; route++) {
				try {
					if (route == 0) WordprocessingMLPackage.load(wiring(e.getKey()));
					else WordprocessingMLPackage.load(new ByteArrayInputStream(bytes(e.getKey())));
					fail(e.getKey() + " loaded");
				} catch (Docx4JException refused) {
					assertTrue(refused.getMessage(), refused.getMessage().contains(e.getValue()));
				}
			}
		}
	}

	/** Under the switch, the first of each name in the central directory, from a file and a stream alike, recorded once. */
	@Test
	public void theSwitchReadsTheFirstInTheCentralDirectory() throws Exception {
		String[][] cases = {
				{ "dup-entry-document", "FIRST_ENTRY", "word/document.xml" },
				{ "dup-entry-same-bytes", "SAME_WORDS", "word/document.xml" },
				{ "two-rels-entries-document-first", "MAIN_DOCUMENT_XML", "_rels/.rels" },
				{ "two-rels-entries-other-first", "MAIN_OTHER_XML", "_rels/.rels" },
				{ "case-variant-exact-first", "CASE_EXACT", "word/document.xml" },
				{ "case-variant-exact-last", "CASE_VARIANT", "Word/Document.xml" }, // Word check 75a: the first, not the name the relationship spells
				{ "dup-central-reversed", "SECOND_LOCAL", "word/document.xml" }, // the central directory's first, not the local headers'
		};
		for (String[] c : cases) {
			for (WordprocessingMLPackage pkg : both(c[0], repair())) {
				assertEquals(c[0], c[1], text(pkg));
				assertEquals(c[0], Arrays.asList(new LoadFinding(LoadFinding.DUPLICATE_ENTRY, c[2], "2")), pkg.getLoadFindings());
			}
		}
	}

	/** REQ-206's NOTE: more than one officeDocument relationship is refused, from a file and a stream, naming the targets. */
	@Test
	public void twoMainPartsAreRefusedNamingTheTargets() throws Exception {
		String[][] cases = {
				{ "two-main-rels-document-first", "word/document.xml, word/other.xml" },
				{ "two-main-rels-other-first", "word/other.xml, word/document.xml" },
				{ "two-main-rels-zip-other-first", "word/document.xml, word/other.xml" },
				{ "two-main-rels-types-other-first", "word/document.xml, word/other.xml" },
				{ "two-main-rels-names-swapped", "word/zeta.xml, word/alpha.xml" } };
		for (String[] c : cases) {
			for (int route = 0; route < 2; route++) {
				try {
					if (route == 0) WordprocessingMLPackage.load(wiring(c[0]));
					else WordprocessingMLPackage.load(new ByteArrayInputStream(bytes(c[0])));
					fail(c[0] + " loaded");
				} catch (Docx4JException refused) {
					assertTrue(refused.getMessage(), refused.getMessage().contains("2 main parts: " + c[1]));
				}
			}
		}
	}

	/** Under the switch, the last relationship's target, as a fresh Word reads it, whatever the Overrides, entries or names; a save holds one. */
	@Test
	public void theSwitchReadsTheLastRelationship() throws Exception {
		String[][] cases = {
				{ "two-main-rels-document-first", "MAIN_OTHER_XML", "/word/other.xml" },
				{ "two-main-rels-other-first", "MAIN_DOCUMENT_XML", "/word/document.xml" },
				{ "two-main-rels-zip-other-first", "MAIN_OTHER_XML", "/word/other.xml" },
				{ "two-main-rels-types-other-first", "MAIN_OTHER_XML", "/word/other.xml" },
				{ "two-main-rels-names-swapped", "MAIN_ALPHA_XML", "/word/alpha.xml" } };
		for (String[] c : cases) {
			String word = text(WordprocessingMLPackage.load(wiring(c[0] + "-word365"))); // Word's save, a first open
			for (WordprocessingMLPackage pkg : both(c[0], repair())) {
				assertEquals(c[0], c[1], text(pkg));
				assertEquals(c[0], word, text(pkg));
				assertEquals(c[0], Arrays.asList(new LoadFinding(LoadFinding.MULTIPLE_MAIN_PARTS, c[2], "2")), pkg.getLoadFindings());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Docx4J.save(pkg, out);
				int mains = 0;
				try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
					for (ZipEntry e; (e = z.getNextEntry()) != null; ) if (e.getName().startsWith("word/") && e.getName().endsWith(".xml")) mains++;
				}
				assertEquals(c[0], 1, mains);
				assertTrue(WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray())).getLoadFindings().isEmpty());
			}
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(wiring("two-main-rels-document-first"), repair());
		TextWarning w = Texts.of(pkg, new TextOptions()).warnings().get(0);
		assertEquals(TextWarning.PACKAGE_MULTIPLE_MAIN_PARTS, w.getKind());
		assertEquals("/word/other.xml", w.getSelector());
		assertEquals("2", w.getDetail());
	}

	/** A part no relationship names is no case: read the related part, with nothing found or refused, the override last or not. */
	@Test
	public void anOrphanMainPartIsNoCase() throws Exception {
		for (LoadOptions options : new LoadOptions[] { null, repair() }) {
			for (WordprocessingMLPackage pkg : both("one-main-rel-other-override-last", options)) {
				assertEquals("MAIN_DOCUMENT_XML", text(pkg));
				assertTrue(pkg.getLoadFindings().isEmpty());
			}
			for (WordprocessingMLPackage pkg : both("one-main-rel-document-override-last", options)) {
				assertEquals("MAIN_OTHER_XML", text(pkg));
				assertTrue(pkg.getLoadFindings().isEmpty());
			}
		}
	}

	/** The oracle: under the switch each reads as Word's own repaired save does (the round package-wiring, Word 16.0.20326.20232). */
	@Test
	public void theSwitchReadsAsWordsSaveDoes() throws Exception {
		for (String name : DUPLICATED) {
			String word = text(WordprocessingMLPackage.load(wiring(name + "-word365")));
			for (WordprocessingMLPackage pkg : both(name, repair())) assertEquals(name, word, text(pkg));
		}
	}

	/** Every entry is covered: the content types' first copy stands, and a part's relationships' first copy. */
	@Test
	public void theSwitchCoversEveryEntry() throws Exception {
		for (WordprocessingMLPackage pkg : both("dup-entry-content-types", repair())) {
			assertEquals("MAIN_DOCUMENT_XML", text(pkg));
		}
		for (WordprocessingMLPackage pkg : both("dup-entry-part-rels", repair())) {
			assertNotNull(pkg.getParts().get(new PartName("/word/header1.xml")));
			assertNull(pkg.getParts().get(new PartName("/word/header2.xml")));
		}
	}

	/** The finding reaches the text model as its warning, and the inspector. */
	@Test
	public void theFindingIsWarnedOfAndInspected() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(wiring("dup-entry-document"), repair());
		List<TextWarning> warnings = Texts.of(pkg, new TextOptions()).warnings();
		assertEquals(warnings.toString(), 1, warnings.size());
		TextWarning w = warnings.get(0);
		assertEquals(TextWarning.PACKAGE_DUPLICATE_ENTRY, w.getKind());
		assertEquals("word/document.xml", w.getSelector());
		assertEquals("2", w.getDetail());
		assertTrue(Texts.of(pkg, new TextOptions().warnings(false)).warnings().isEmpty());
		boolean reported = false;
		for (Inspection.Entry e : Inspect.of(pkg).skipped()) reported |= LoadFinding.DUPLICATE_ENTRY.equals(e.getReason());
		assertTrue(reported);
	}

	/** REQ-206: a save writes only the copy it read, whether the part was read or left as bytes. */
	@Test
	public void aSaveWritesOnlyTheCopyRead() throws Exception {
		for (boolean read : new boolean[] { true, false }) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(wiring("dup-entry-document"), repair());
			if (read) pkg.getMainDocumentPart().getContent();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Docx4J.save(pkg, out);
			List<String> names = new ArrayList<String>();
			try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
				for (ZipEntry e; (e = z.getNextEntry()) != null; ) names.add(e.getName());
			}
			assertEquals(names.toString(), names.indexOf("word/document.xml"), names.lastIndexOf("word/document.xml"));
			WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
			assertEquals("FIRST_ENTRY", text(again));
			assertTrue(again.getLoadFindings().isEmpty());
		}
	}

	/** The property gives the default, and the options win. */
	@Test
	public void thePropertyIsTheDefaultAndTheOptionsWin() throws Exception {
		Docx4jProperties.setProperty(RepairMode.PROPERTY, "word");
		try {
			assertEquals("FIRST_ENTRY", text(WordprocessingMLPackage.load(wiring("dup-entry-document"))));
			assertEquals("FIRST_ENTRY", text(WordprocessingMLPackage.load(new ByteArrayInputStream(bytes("dup-entry-document")))));
			try {
				WordprocessingMLPackage.load(wiring("dup-entry-document"), new LoadOptions().repairMode(RepairMode.NONE));
				fail("the options lost to the property");
			} catch (Docx4JException refused) {
				assertTrue(refused.getMessage().contains("word/document.xml"));
			}
		} finally {
			Docx4jProperties.setProperty(RepairMode.PROPERTY, "none");
		}
		assertEquals(RepairMode.NONE, RepairMode.fromProperty());
		assertEquals(RepairMode.WORD, RepairMode.parse(" Word "));
		assertNull(RepairMode.parse("maybe"));
	}

	/** An ordinary package: nothing found, in either mode; and the mc mode travels in the options as in its own overload. */
	@Test
	public void anOrdinaryPackageFindsNothing() throws Exception {
		for (LoadOptions options : new LoadOptions[] { null, new LoadOptions(), repair() }) {
			for (WordprocessingMLPackage pkg : both("control-one-main", options)) {
				assertEquals("CONTROL_WORDS", text(pkg));
				assertTrue(pkg.getLoadFindings().isEmpty());
			}
		}
		assertEquals(McLoadMode.PRESERVE, WordprocessingMLPackage.load(wiring("control-one-main"), new LoadOptions().mcLoadMode(McLoadMode.PRESERVE)).getMcLoadMode());
		assertEquals(McLoadMode.PRESERVE, WordprocessingMLPackage.load(wiring("control-one-main"), McLoadMode.PRESERVE).getMcLoadMode());
		assertNull(WordprocessingMLPackage.load(wiring("control-one-main"), new LoadOptions()).getMcLoadMode());
	}

	/** A stream with no central directory still loads through its local headers, and a duplicate there is still caught. */
	@Test
	public void aStreamWithNoCentralDirectoryIsReadByItsLocalHeaders() throws Exception {
		assertEquals("CONTROL_WORDS", text(WordprocessingMLPackage.load(new ByteArrayInputStream(noCentralDirectory(bytes("control-one-main"))))));
		byte[] duplicated = noCentralDirectory(bytes("dup-entry-document"));
		try {
			new ZipPartStore(new ByteArrayInputStream(duplicated), RepairMode.NONE);
			fail("loaded");
		} catch (Docx4JException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("word/document.xml (2 entries)"));
		}
		ZipPartStore store = new ZipPartStore(new ByteArrayInputStream(duplicated), RepairMode.WORD);
		assertEquals(Arrays.asList(new LoadFinding(LoadFinding.DUPLICATE_ENTRY, "word/document.xml", "2")), store.getLoadFindings());
		assertTrue(new String(store.getByteArray("word/document.xml").getBytes(), "UTF-8").contains("FIRST_ENTRY"));
	}

	/** The central directory is the authority: a local header it doesn't list is passed over, by a stream as by a file. */
	@Test
	public void aFileAndAStreamReadALocalOnlyDuplicateAlike() throws Exception {
		for (LoadOptions options : new LoadOptions[] { null, repair() }) {
			for (WordprocessingMLPackage pkg : both("dup-local-only", options)) {
				assertEquals("SECOND_LOCAL", text(pkg));
				assertTrue(pkg.getLoadFindings().isEmpty());
			}
		}
	}

	/** A directory entry is no part, so one given twice isn't a duplicate. */
	@Test
	public void aDirectoryEntryGivenTwiceIsNoDuplicate() throws Exception {
		for (WordprocessingMLPackage pkg : both("dup-directory-entry", null)) {
			assertEquals("DIRECTORY_WORDS", text(pkg));
			assertTrue(pkg.getLoadFindings().isEmpty());
		}
	}

	/** What the load found stands on a clone. */
	@Test
	public void aCloneKeepsTheFindings() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(wiring("dup-entry-document"), repair());
		assertEquals(pkg.getLoadFindings(), ((WordprocessingMLPackage) pkg.clone()).getLoadFindings());
	}

	/** A failure is the last read's, not any earlier one's. */
	@Test
	public void aLaterReadClearsAFailure() throws Exception {
		org.docx4j.openpackaging.parts.relationships.RelationshipsPart rels = WordprocessingMLPackage.createPackage().getRelationshipsPart();
		rels.unmarshal(new ByteArrayInputStream("<Relationships".getBytes("UTF-8")));
		assertNotNull(rels.getReadFailure());
		rels.unmarshal(new ByteArrayInputStream("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>".getBytes("UTF-8")));
		assertNull(rels.getReadFailure());
	}

	/** D6: the package's relationships not well formed: the refusal names the part and the parse error, not what it then broke. */
	@Test
	public void aRefusalNamesThePackageRelationships() throws Exception {
		try {
			WordprocessingMLPackage.load(file("text-extraction/broken-part/broken-part-package-rels.docx"));
			fail("loaded");
		} catch (Docx4JException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("_rels/.rels"));
			assertFalse(refused.getMessage(), refused.getMessage().contains("NullPointerException"));
			assertTrue(refused.getMessage(), refused.getMessage().length() > "The package's relationships part, _rels/.rels, could not be read: ".length());
		}
	}

	/** A part's relationships not well formed: read as it was, and said (its reading is phase 4's). */
	@Test
	public void aPartsRelationshipsFailureIsRecorded() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file("text-extraction/broken-part/broken-part-document-rels.docx"));
		assertEquals(Arrays.asList(new LoadFinding(LoadFinding.PART_NOT_READ, "/word/_rels/document.xml.rels", "0")), pkg.getLoadFindings());
		boolean warned = false;
		for (TextWarning w : Texts.of(pkg, new TextOptions()).warnings()) {
			warned |= TextWarning.PART_NOT_READ.equals(w.getKind()) && "/word/_rels/document.xml.rels".equals(w.getSelector()) && "0".equals(w.getDetail());
		}
		assertTrue(warned);
	}

	/** A load into an existing package starts its findings afresh. */
	@Test
	public void aLoadIntoAPackageReplacesItsFindings() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(wiring("dup-entry-document"), repair());
		assertEquals(1, pkg.getLoadFindings().size());
		Load3 loader = new Load3(new ZipPartStore(wiring("control-one-main")));
		loader.reuseExistingOpcPackage(pkg);
		loader.get();
		assertTrue(pkg.getLoadFindings().isEmpty());
	}
}
