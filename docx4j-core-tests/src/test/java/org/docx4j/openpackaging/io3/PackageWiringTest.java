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
					assertTrue(refused.getMessage(), refused.getMessage().contains("2 main part relationships, to " + c[1]));
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

	private static List<String> entries(byte[] zip) throws Exception {
		List<String> names = new ArrayList<String>();
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) names.add(e.getName());
		}
		return names;
	}

	private static byte[] saved(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.save(pkg, out);
		return out.toByteArray();
	}

	/** D6: the package's relationships not well formed: those before the break stand, the main part's among them, as Word's repair keeps them. */
	@Test
	public void thePackagesRelationshipsAreReadUpToTheBreak() throws Exception {
		File broken = file("text-extraction/broken-part/broken-part-package-rels.docx");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(broken);
		assertEquals(Arrays.asList(new LoadFinding(LoadFinding.PART_NOT_READ, "/_rels/.rels", "1")), pkg.getLoadFindings());
		assertNotNull(pkg.getMainDocumentPart());
		assertNull(pkg.getParts().get(new PartName("/docProps/core.xml")));
		boolean warned = false;
		for (TextWarning w : Texts.of(pkg, new TextOptions()).warnings()) {
			warned |= TextWarning.PART_NOT_READ.equals(w.getKind()) && "/_rels/.rels".equals(w.getSelector()) && "1".equals(w.getDetail());
		}
		assertTrue(warned);
	}

	/** D6: where the break comes before the main part's relationship there is nothing to read: refused, naming the part and the parse error. */
	@Test
	public void aRefusalNamesThePackageRelationships() throws Exception {
		ByteArrayOutputStream cut = new ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream z = new java.util.zip.ZipOutputStream(cut);
				ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(bytes("control-one-main")))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				z.putNextEntry(new ZipEntry(e.getName()));
				z.write(e.getName().equals("_rels/.rels")
						? "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relat".getBytes("UTF-8") : in.readAllBytes());
				z.closeEntry();
			}
		}
		try {
			WordprocessingMLPackage.load(new ByteArrayInputStream(cut.toByteArray()));
			fail("loaded");
		} catch (Docx4JException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("_rels/.rels"));
			assertFalse(refused.getMessage(), refused.getMessage().contains("NullPointerException"));
			assertTrue(refused.getMessage(), refused.getMessage().length() > "The package's relationships part, _rels/.rels, could not be read: ".length());
		}
	}

	/** D5: a part's relationships not well formed: those before the break stand, and what only a lost one named reads as absent. */
	@Test
	public void aPartsRelationshipsAreReadUpToTheBreak() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file("text-extraction/broken-part/broken-part-document-rels.docx"));
		assertEquals(Arrays.asList(new LoadFinding(LoadFinding.PART_NOT_READ, "/word/_rels/document.xml.rels", "3")), pkg.getLoadFindings());
		assertEquals(3, pkg.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship().size());
		boolean warned = false;
		for (TextWarning w : Texts.of(pkg, new TextOptions()).warnings()) {
			warned |= TextWarning.PART_NOT_READ.equals(w.getKind()) && "/word/_rels/document.xml.rels".equals(w.getSelector()) && "3".equals(w.getDetail());
		}
		assertTrue(warned);
	}

	/** D8: a save that would make the repair permanent with loss is refused, naming the part; under the switch it writes the repaired form. */
	@Test
	public void theSaveGateRefusesALossySave() throws Exception {
		for (String name : new String[] { "broken-part-document-rels", "broken-part-package-rels" }) {
			File broken = file("text-extraction/broken-part/" + name + ".docx");
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(broken);
			try {
				saved(pkg);
				fail(name + " saved");
			} catch (Docx4JException refused) {
				assertTrue(refused.getMessage(), refused.getMessage().contains(".rels") && refused.getMessage().contains("relationships were kept"));
			}
			try {
				pkg.save(new ByteArrayOutputStream(), Docx4J.FLAG_SAVE_FLAT_XML);
				fail(name + " saved as Flat OPC");
			} catch (Docx4JException refused) {
				assertTrue(refused.getMessage(), refused.getMessage().contains("relationships were kept"));
			}
			// a read-only output's copy isn't a save
			WordprocessingMLPackage copy = (WordprocessingMLPackage) pkg.clone();
			assertEquals(pkg.getLoadFindings(), copy.getLoadFindings());
			assertEquals(text(pkg), text(copy));
			byte[] repaired = saved(WordprocessingMLPackage.load(broken, repair()));
			assertTrue(WordprocessingMLPackage.load(new ByteArrayInputStream(repaired)).getLoadFindings().isEmpty());
		}
	}

	/** D7: a store that isn't well formed reads as absent: a binding naming it resolves to the store whose root fits, as Word's repair does, and says so. */
	@Test
	public void anUnreadableStoreReadsAsAbsent() throws Exception {
		File broken = file("text-extraction/broken-part/store-not-read.docx");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(broken);
		String text = text(pkg);
		assertEquals(text, 3, text.split("STORE2_VALUE", -1).length - 1);
		assertFalse(text, text.contains("STORE1_VALUE"));
		int warned = 0;
		for (TextWarning w : Texts.of(pkg, new TextOptions()).warnings()) {
			if (TextWarning.PART_NOT_READ.equals(w.getKind()) && "/customXml/item1.xml".equals(w.getSelector())) warned++;
		}
		assertEquals(1, warned);
		assertTrue(pkg.getLoadFindings().isEmpty());
		boolean inspected = false;
		for (Inspection.Entry e : Inspect.of(pkg).skipped()) inspected |= Inspection.UNREADABLE.equals(e.getReason()) && e.getLocation().contains("/customXml/item1.xml");
		assertTrue(inspected);
	}

	/** D7: an untouched save writes the store's bytes; under the switch it goes, as Word's save drops it. */
	@Test
	public void anUnreadableStoreSavesAsBytesOrGoes() throws Exception {
		File broken = file("text-extraction/broken-part/store-not-read.docx");
		byte[] source = null;
		try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(broken)) {
			source = z.getInputStream(z.getEntry("customXml/item1.xml")).readAllBytes();
		}
		byte[] untouched = saved(WordprocessingMLPackage.load(broken));
		assertTrue(entries(untouched).contains("customXml/item1.xml"));
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(untouched))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) if (e.getName().equals("customXml/item1.xml")) assertTrue(Arrays.equals(source, z.readAllBytes()));
		}
		WordprocessingMLPackage repaired = WordprocessingMLPackage.load(broken, repair());
		assertFalse(entries(saved(repaired)).contains("customXml/item1.xml"));
		assertTrue(entries(saved(repaired)).contains("customXml/item2.xml"));
		assertTrue(text(repaired).contains("STORE2_VALUE"));
	}

	/** The document's relationships with their end cut off: all of them read, and a finding, a store to bind to among them. */
	private static byte[] storeWithAFinding() throws Exception {
		File intact = file("text-extraction/broken-part/store-not-read-intact.docx");
		String rels;
		try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(intact)) {
			rels = new String(z.getInputStream(z.getEntry("word/_rels/document.xml.rels")).readAllBytes(), "UTF-8");
		}
		return rewritten(intact, "word/_rels/document.xml.rels", rels.replace("</Relationships>", ""));
	}

	/** Bind copies the package back into itself: not a save, so the gate doesn't apply, and the findings and mode stand. */
	@Test
	public void bindIsNoSave() throws Exception {
		WordprocessingMLPackage word = WordprocessingMLPackage.load(new ByteArrayInputStream(storeWithAFinding()), repair());
		assertEquals(1, word.getLoadFindings().size());
		Docx4J.bind(word, "<root xmlns=\"urn:probe\"/>", Docx4J.FLAG_BIND_BIND_XML);
		assertEquals(RepairMode.WORD, word.getRepairMode());
		assertEquals(1, word.getLoadFindings().size());
		saved(word);
		WordprocessingMLPackage plain = WordprocessingMLPackage.load(new ByteArrayInputStream(storeWithAFinding()));
		Docx4J.bind(plain, "<root xmlns=\"urn:probe\"/>", Docx4J.FLAG_BIND_BIND_XML);
		assertEquals(1, plain.getLoadFindings().size());
		try {
			saved(plain);
			fail("saved");
		} catch (Docx4JException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("relationships were kept"));
		}
	}

	/** The older class's Flat OPC route is gated as the package's own is. */
	@Test
	public void flatOpcThroughTheOlderClassIsGated() throws Exception {
		WordprocessingMLPackage plain = WordprocessingMLPackage.load(new ByteArrayInputStream(storeWithAFinding()));
		try {
			new org.docx4j.openpackaging.io.SaveToZipFile(plain).saveFlatOPC(new ByteArrayOutputStream());
			fail("saved");
		} catch (Docx4JException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("relationships were kept"));
		}
	}

	/** A store that can't even start, its declared encoding unknown, is read as absent like one cut short. */
	@Test
	public void aStoreWithABadEncodingReadsAsAbsent() throws Exception {
		byte[] zip = rewritten(file("text-extraction/broken-part/store-not-read.docx"), "customXml/item1.xml",
				"<?xml version=\"1.0\" encoding=\"bogus-enc\"?><root/>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(zip));
		assertEquals("/customXml/item1.xml", pkg.getUnreadableStores().iterator().next());
	}

	/** Under the switch a store goes with every relationship to it, or a save can't find the part. */
	@Test
	public void aStoreReachedTwiceGoesWhole() throws Exception {
		String second = "<Relationship Id=\"rId77\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml\" Target=\"../customXml/item1.xml\"/>";
		File broken = file("text-extraction/broken-part/store-not-read.docx");
		String rels;
		try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(broken)) {
			rels = new String(z.getInputStream(z.getEntry("word/_rels/document.xml.rels")).readAllBytes(), "UTF-8");
		}
		byte[] zip = rewritten(broken, "word/_rels/document.xml.rels", rels.replace("</Relationships>", second + "</Relationships>"));
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(zip), repair());
		assertFalse(entries(saved(pkg)).contains("customXml/item1.xml"));
	}

	/** A relationships part that parses to its end has no break: its namespace wrong, nothing of it is kept, and the package is refused. */
	@Test
	public void noBreakNoPrefix() throws Exception {
		byte[] zip = rewritten(wiring("control-one-main"), "_rels/.rels",
				"<Relationships xmlns=\"urn:wrong\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
		try {
			WordprocessingMLPackage.load(new ByteArrayInputStream(zip));
			fail("loaded");
		} catch (Docx4JException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("_rels/.rels"));
		}
	}

	/**
	 * Word repairs two relationships to one target as it repairs two to two (round one-target, ot-01 to ot-03), so the count
	 * is of relationships, however the target is spelled; and an external target is no part.
	 */
	@Test
	public void relationshipsAreCountedNotTargets() throws Exception {
		String od = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
		String head = "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"" + od + "\" Target=\"word/document.xml\"/>";
		for (String spelling : new String[] { "word/document.xml", "/word/document.xml" }) {
			byte[] zip = rewritten(wiring("control-one-main"), "_rels/.rels", head + "<Relationship Id=\"rId2\" Type=\"" + od + "\" Target=\"" + spelling + "\"/></Relationships>");
			try {
				WordprocessingMLPackage.load(new ByteArrayInputStream(zip));
				fail("loaded");
			} catch (Docx4JException refused) {
				assertTrue(refused.getMessage(), refused.getMessage().contains("2 main part relationships"));
			}
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(zip), repair());
			assertEquals("CONTROL_WORDS", text(pkg));
			assertEquals(LoadFinding.MULTIPLE_MAIN_PARTS, pkg.getLoadFindings().get(0).getKind());
			assertEquals("2", pkg.getLoadFindings().get(0).getDetail());
			try (java.util.zip.ZipInputStream in = new java.util.zip.ZipInputStream(new ByteArrayInputStream(saved(pkg)))) {
				for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
					if (e.getName().equals("_rels/.rels")) assertEquals(2, new String(in.readAllBytes(), "UTF-8").split("officeDocument\"", -1).length);
				}
			}
		}
		byte[] zip = rewritten(wiring("control-one-main"), "_rels/.rels", head
				+ "<Relationship Id=\"rId2\" Type=\"" + od + "\" Target=\"http://example.com/x.docx\" TargetMode=\"External\"/></Relationships>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(zip));
		assertEquals("CONTROL_WORDS", text(pkg));
		assertTrue(pkg.getLoadFindings().isEmpty());
	}

	private static byte[] rewritten(File source, String name, String xml) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream z = new java.util.zip.ZipOutputStream(out);
				ZipInputStream in = new ZipInputStream(new java.io.FileInputStream(source))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				z.putNextEntry(new ZipEntry(e.getName()));
				z.write(e.getName().equals(name) ? xml.getBytes("UTF-8") : in.readAllBytes());
				z.closeEntry();
			}
		}
		return out.toByteArray();
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
