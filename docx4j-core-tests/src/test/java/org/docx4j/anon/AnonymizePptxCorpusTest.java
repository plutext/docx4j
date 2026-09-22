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

import org.docx4j.XmlUtils;
import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.AnonymizeResult.PartAction;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.junit.Test;

/**
 * The anonymiser on docx4j's own presentations (see src/test/resources/anon/README.md):
 * STRICT leaves every one clean and verified, the output reloads, and the
 * things each deck is here for are gone.
 */
public class AnonymizePptxCorpusTest {

	static final String[] CORPUS = {
			"loadAndSave.pptx",
			"anon/AutoShapes.pptx",
			"anon/pptx-chart.pptx",
			"anon/table.pptx",
			"anon/strict.pptx" };

	static PresentationMLPackage load(String name) throws Exception {
		InputStream is = AnonymizePptxCorpusTest.class.getResourceAsStream("/" + name);
		assertNotNull("resource " + name, is);
		return (PresentationMLPackage) OpcPackage.load(is);
	}

	static PresentationMLPackage reload(PresentationMLPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return (PresentationMLPackage) OpcPackage.load(new ByteArrayInputStream(baos.toByteArray()));
	}

	static Part part(OpcPackage pkg, String name) throws Exception {
		return pkg.getParts().get(new PartName(name));
	}

	static PartAction action(AnonymizeResult r, String partName) {
		return AnonymizeCorpusTest.action(r, partName);
	}

	@Test
	public void strictIsCleanVerifiedAndReloads() throws Exception {
		List<String> problems = new ArrayList<String>();
		for (String name : CORPUS) {
			PresentationMLPackage pkg = load(name);
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
			PresentationMLPackage again = reload(pkg);
			assertNotNull(again.getMainPresentationPart().getContents());
			assertFalse(name + ": no slides after reload", again.getMainPresentationPart().getContents().getSldIdLst().getSldId().isEmpty());
		}
		assertTrue(String.join("\n", problems), problems.isEmpty());
	}

	@Test
	public void modernCommentsAuthorsAndTheChartWorkbook() throws Exception {
		// loadAndSave.pptx: 2018 comments with an author (name, initials, email user id), a chart
		// with its embedded workbook, an SVG, a sensitivity label, a thumbnail, a hyperlink
		PresentationMLPackage pkg = load("loadAndSave.pptx");
		String originalAuthors = XmlUtils.w3CDomNodeToString(((XmlPart) part(pkg, "/ppt/authors.xml")).getDocument());
		assertTrue(originalAuthors, originalAuthors.contains("userId=\"S::"));

		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());

		String authors = XmlUtils.w3CDomNodeToString(((XmlPart) part(pkg, "/ppt/authors.xml")).getDocument());
		assertTrue(authors, authors.contains("name=\"Author 1\""));
		assertTrue(authors, authors.contains("userId=\"Author 1\""));
		assertTrue(authors, authors.contains("providerId=\"None\""));
		assertFalse(authors, authors.contains("@"));
		String comments = XmlUtils.w3CDomNodeToString(((XmlPart) part(pkg, "/ppt/comments/modernComment_101_A987BC77.xml")).getDocument());
		assertTrue(comments, comments.contains("created=\"" + Names.FIXED_DATE.toXMLFormat() + "\""));
		assertFalse(comments, comments.contains(">Comment<"));
		assertEquals(1, r.getAuthorsRenamed());

		assertNull(part(pkg, "/ppt/embeddings/Microsoft_Excel_Worksheet.xlsx"));
		assertEquals(Action.SCRUBBED, action(r, "/ppt/charts/chart1.xml").action);
		String chartXml = ((org.docx4j.openpackaging.parts.DrawingML.Chart) part(pkg, "/ppt/charts/chart1.xml")).getXML();
		assertFalse("the workbook link is gone", chartXml.contains("externalData"));
		assertTrue("the caches are still there", chartXml.contains("numCache"));

		assertNull(part(pkg, "/docMetadata/LabelInfo.xml"));
		assertNull(part(pkg, "/docProps/thumbnail.jpeg"));
		assertEquals(Action.REPLACED, action(r, "/ppt/media/image2.svg").action);
		assertEquals(1, r.getExternalTargetsReplaced());
		assertNull(pkg.getDocPropsCorePart().getContents().getCreator());
		assertNull(pkg.getDocPropsExtendedPart().getContents().getTitlesOfParts());
		assertNull(pkg.getDocPropsExtendedPart().getContents().getPresentationFormat());

		// the slide text is scrambled, the placeholders are still placeholders
		SlidePart slide2 = (SlidePart) part(pkg, "/ppt/slides/slide2.xml");
		String xml = slide2.getXML();
		assertFalse(xml, xml.contains("Slide Title"));
		assertTrue(xml, xml.contains("<p:ph type=\"title\""));
		assertTrue("the a14:m math branch is still there", xml.contains("a14:m"));
	}

	@Test
	public void printerSettingsGoWithTheirRelationship() throws Exception {
		PresentationMLPackage pkg = load("anon/pptx-chart.pptx");
		assertNotNull(part(pkg, "/ppt/printerSettings/printerSettings1.bin"));
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		assertNull(part(pkg, "/ppt/printerSettings/printerSettings1.bin"));
		String rels = pkg.getMainPresentationPart().getRelationshipsPart().getXML();
		assertFalse(rels, rels.contains("printerSettings"));
		reload(pkg);
	}

	@Test
	public void keepModeIsNeverCleanWhenSomethingIsKept() throws Exception {
		PresentationMLPackage pkg = load("anon/pptx-chart.pptx");
		AnonymizeResult r = new Anonymize(pkg, Anonymize.Mode.KEEP).go();
		assertFalse(r.isClean());
		boolean workbook = false;
		for (PartAction a : r.getKeptUnsafe()) {
			if (a.partName.startsWith("/ppt/embeddings/")) workbook = true;
		}
		assertTrue("the embedded workbook is what was kept: " + r.getKeptUnsafe(), workbook);
		assertNotNull(part(pkg, "/ppt/embeddings/Microsoft_Excel_Sheet1.xlsx"));
	}

}
