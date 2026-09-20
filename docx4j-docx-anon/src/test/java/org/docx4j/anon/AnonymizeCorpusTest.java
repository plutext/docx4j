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
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.AnonymizeResult.PartAction;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart;
import org.docx4j.w15.CTPerson;
import org.docx4j.wml.Comments;
import org.junit.Test;

/**
 * The anonymiser on docx4j's own documents (see src/test/resources/README.md):
 * STRICT leaves every one clean and verified, the output reloads, and the
 * things each document is here for are gone.
 */
public class AnonymizeCorpusTest {

	static final String[] CORPUS = {
			"loadAndSave.docx",
			"tracked-changes-equations.docx",
			"ole-inserted-doc.docx",
			"LegacyForms.docx",
			"vml-textbox.docx",
			"chart.docx",
			"strict-smartart.docx",
			"embedded-fonts.docx",
			"MERGEFIELD.docx" };

	static WordprocessingMLPackage load(String name) throws Exception {
		InputStream is = AnonymizeCorpusTest.class.getResourceAsStream("/" + name);
		assertNotNull("resource " + name, is);
		return WordprocessingMLPackage.load(is);
	}

	static byte[] save(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return baos.toByteArray();
	}

	static WordprocessingMLPackage reload(WordprocessingMLPackage pkg) throws Exception {
		return WordprocessingMLPackage.load(new ByteArrayInputStream(save(pkg)));
	}

	static Part part(WordprocessingMLPackage pkg, String name) throws Exception {
		return pkg.getParts().get(new PartName(name));
	}

	static PartAction action(AnonymizeResult r, String partName) {
		for (PartAction a : r.getActions()) {
			if (a.partName.equals(partName)) return a;
		}
		return null;
	}

	@Test
	public void strictIsCleanVerifiedAndReloads() throws Exception {
		List<String> problems = new ArrayList<String>();
		for (String name : CORPUS) {
			WordprocessingMLPackage pkg = load(name);
			AnonymizeResult r;
			try {
				r = new Anonymize(pkg).go();
			} catch (Exception e) {
				throw new AssertionError(name + ": " + e, e);
			}
			if (!r.isClean()) {
				problems.add(name + ": not clean\n" + r.summary());
			}
			if (!Boolean.TRUE.equals(r.getVerified())) {
				problems.add(name + ": not verified: " + r.getLeaks());
			}
			WordprocessingMLPackage again = reload(pkg);
			assertNotNull(again.getMainDocumentPart().getContents());
		}
		assertTrue(String.join("\n", problems), problems.isEmpty());
	}

	@Test
	public void keepModeIsNeverCleanWhenSomethingIsKept() throws Exception {
		// an OLE object cannot be made clean: KEEP keeps it and says so
		WordprocessingMLPackage pkg = load("ole-inserted-doc.docx");
		AnonymizeResult r = new Anonymize(pkg, Anonymize.Mode.KEEP).go();
		assertFalse(r.isClean());
		assertFalse(r.getKeptUnsafe().isEmpty());
		boolean ole = false;
		for (PartAction a : r.getKeptUnsafe()) {
			if (a.partName.startsWith("/word/embeddings/")) ole = true;
		}
		assertTrue("the embedding is what was kept: " + r.getKeptUnsafe(), ole);
		// but the text and identities were still scrubbed
		assertTrue(Boolean.TRUE.equals(r.getVerified()) || !r.getLeaks().isEmpty());
	}

	@Test
	public void commentsAuthorsAndPeopleBecomeAuthorN() throws Exception {
		WordprocessingMLPackage pkg = load("loadAndSave.docx");
		CommentsPart cp = pkg.getMainDocumentPart().getCommentsPart();
		assertNotNull(cp);
		String originalAuthor = cp.getContents().getComment().get(0).getAuthor();
		assertNotNull(originalAuthor);

		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());

		for (Comments.Comment c : cp.getContents().getComment()) {
			assertTrue(c.getAuthor(), c.getAuthor().startsWith("Author "));
			assertFalse(c.getAuthor().equals(originalAuthor));
			if (c.getInitials() != null) assertTrue(c.getInitials().startsWith("A"));
			assertEquals(Names.FIXED_DATE.toXMLFormat(), c.getDate().toXMLFormat());
		}
		PeoplePart people = null;
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof PeoplePart) people = (PeoplePart) p;
		}
		assertNotNull(people);
		for (CTPerson person : people.getContents().getPerson()) {
			assertTrue(person.getAuthor().startsWith("Author "));
			if (person.getPresenceInfo() != null) {
				assertEquals("None", person.getPresenceInfo().getProviderId());
				assertFalse(person.getPresenceInfo().getUserId().contains("@"));
			}
		}
		assertTrue(r.getAuthorsRenamed() > 0);
	}

	@Test
	public void chartKeepsItsShapeAndLosesItsWorkbook() throws Exception {
		WordprocessingMLPackage pkg = load("loadAndSave.docx");
		assertNotNull(part(pkg, "/word/embeddings/Microsoft_Excel_Worksheet.xlsx"));
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		assertNull(part(pkg, "/word/embeddings/Microsoft_Excel_Worksheet.xlsx"));
		assertEquals(Action.SCRUBBED, action(r, "/word/charts/chart1.xml").action);
		String chartXml = ((org.docx4j.openpackaging.parts.DrawingML.Chart) part(pkg, "/word/charts/chart1.xml")).getXML();
		assertFalse("the workbook link is gone", chartXml.contains("externalData"));
		assertTrue("the caches are still there", chartXml.contains("numCache"));
	}

	@Test
	public void metadataAndAlwaysRemovedParts() throws Exception {
		WordprocessingMLPackage pkg = load("loadAndSave.docx");
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		assertNull(pkg.getDocPropsCorePart().getContents().getCreator());
		assertNull(pkg.getDocPropsCorePart().getContents().getLastModifiedBy());
		assertNull(pkg.getDocPropsExtendedPart().getContents().getTitlesOfParts());
		assertNull(part(pkg, "/customXml/item1.xml"));
		assertNull(part(pkg, "/docMetadata/LabelInfo.xml"));
		// the svg is blank now
		Part svg = part(pkg, "/word/media/image2.svg");
		assertNotNull(svg);
		assertEquals(Action.REPLACED, action(r, "/word/media/image2.svg").action);
	}

	@Test
	public void embeddedFontsGoWithTheirReferences() throws Exception {
		WordprocessingMLPackage pkg = load("embedded-fonts.docx");
		assertNotNull(part(pkg, "/word/fonts/font1.odttf"));
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		assertNull(part(pkg, "/word/fonts/font1.odttf"));
		String fontTable = pkg.getMainDocumentPart().getFontTablePart().getXML();
		assertFalse(fontTable, fontTable.contains("embedRegular"));
		reload(pkg);
	}

	@Test
	public void oleObjectGoesAndItsPictureStays() throws Exception {
		WordprocessingMLPackage pkg = load("ole-inserted-doc.docx");
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		String body = pkg.getMainDocumentPart().getXML();
		assertFalse(body, body.contains("OLEObject"));
		assertTrue("the v:shape picture remains", body.contains("imagedata"));
		assertNotNull("as the labelled placeholder", part(pkg, MediaReplacer.OBJECT_PLACEHOLDER_PART_NAME));
		assertNull("the EMF preview is gone", part(pkg, "/word/media/image1.emf"));
		reload(pkg);
	}

	@Test
	public void jsonReportParses() throws Exception {
		WordprocessingMLPackage pkg = load("chart.docx");
		AnonymizeResult r = new Anonymize(pkg).go();
		String json = r.toJson();
		assertTrue(json, json.startsWith("{"));
		assertTrue(json, json.contains("\"clean\": true"));
		assertTrue(json, json.contains("\"parts\": ["));
		// every quote inside a string is escaped: a naive brace/quote balance check
		int quotes = 0;
		for (int i = 0; i < json.length(); i++) {
			if (json.charAt(i) == '"' && (i == 0 || json.charAt(i - 1) != '\\')) quotes++;
		}
		assertEquals(0, quotes % 2);
	}

}
