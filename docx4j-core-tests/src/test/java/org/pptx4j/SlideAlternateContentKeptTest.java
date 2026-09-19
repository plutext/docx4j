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
package org.pptx4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.utils.ResourceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pptx4j.convert.out.svginhtml.SvgExporter;
import org.pptx4j.pml.Shape;

/**
 * CR-021 phase 3: PresentationML keeps mc:AlternateContent in a shape tree and reads it
 * through the one selection rule.  loadAndSave.pptx's slide 2 carries the usual
 * PowerPoint case: a Choice Requires="a14" holding the shape with the slide's text (an
 * equation among it) and a Fallback holding a picture of it with a single nbsp.
 */
public class SlideAlternateContentKeptTest {

	private String savedPreference;

	@Before
	public void savePreference() {
		savedPreference = Docx4jProperties.getProperty(McSelection.PROPERTY, "");
		Docx4jProperties.setProperty(McSelection.PROPERTY, "");
	}

	@After
	public void restorePreference() {
		Docx4jProperties.setProperty(McSelection.PROPERTY, savedPreference);
	}

	private static SlidePart slide2(PresentationMLPackage pkg) throws Exception {
		return (SlidePart) pkg.getParts().get(new PartName("/ppt/slides/slide2.xml"));
	}

	private static AlternateContent alternateContentIn(List<Object> content) {
		for (Object o : content) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof AlternateContent) return (AlternateContent) u;
		}
		return null;
	}

	private static int shapes(List<Object> content) {
		int n = 0;
		for (Object o : content) if (XmlUtils.unwrap(o) instanceof Shape) n++;
		return n;
	}

	@Test
	public void shapeTreeElementIsKeptAndRoundTripsWithItsPrefixDeclared() throws Exception {
		PresentationMLPackage pkg = PresentationMLPackage.load(ResourceUtils.getResource("loadAndSave.pptx"));
		SlidePart slide = slide2(pkg);
		List<Object> tree = slide.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame();
		AlternateContent ac = alternateContentIn(tree);
		assertNotNull("mc:AlternateContent kept in p:spTree", ac);
		assertEquals("a14", ac.getChoice().get(0).getRequires());
		assertNotNull(ac.getFallback());

		// the one-branch view: the element replaced by its selected branch's one shape
		List<Object> selected = McSelection.selectedContent(tree);
		assertEquals(tree.size(), selected.size());
		assertEquals("one shape more than the tree shows by instanceof", shapes(tree) + 1, shapes(selected));

		String xml = slide.getXML();
		assertTrue("both branches written back", xml.contains("<mc:Choice Requires=\"a14\">") && xml.contains("<mc:Fallback>"));
		assertTrue("the Choice's Requires prefix is declared on the slide root, or PowerPoint 2010 calls the file corrupt",
				xml.substring(0, xml.indexOf("<p:cSld")).contains("xmlns:a14=\"http://schemas.microsoft.com/office/drawing/2010/main\""));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		PresentationMLPackage again = PresentationMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
		assertNotNull(alternateContentIn(slide2(again).getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()));
	}

	@Test
	public void svgExportDrawsOneBranch() throws Exception {
		PresentationMLPackage pkg = PresentationMLPackage.load(ResourceUtils.getResource("loadAndSave.pptx"));
		SlidePart slide = slide2(pkg);

		String svg = SvgExporter.svg(pkg, slide);
		assertTrue("the shapes beside the element are drawn", svg.contains("Slide Title") && svg.contains("Text box"));
		assertFalse("the Choice's text is not drawn by default", svg.contains("Equation"));

		Docx4jProperties.setProperty(McSelection.PROPERTY, "a14");
		svg = SvgExporter.svg(pkg, slide);
		assertTrue("the Choice's text is drawn when a14 is preferred", svg.contains("Equation"));
		assertEquals("and drawn once", svg.indexOf("Equation"), svg.lastIndexOf("Equation"));
	}
}
