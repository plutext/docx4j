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
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A Word 2013 repeating section ({@code w15:repeatingSection}) whose node set is empty: the
 * section is left as it is, its template item and that item's bound controls untouched, as Word
 * shows a repeating section with no items (measured in Word 365 on the zero-item fixture, 2026-10-22:
 * the item stays with its stored text, its binding unchanged, nothing emptied, no placeholder, no item
 * added to the XML; OpenDoPE v3's REQ-053 note says the same). OpenDoPEHandler marks the section
 * {@code w15:resultRepeatZero}; until 17.3.4 OpenDoPEIntegrity.xslt unwrapped it with every other
 * repeating section before binding, so the marker was lost and the template's controls were bound
 * against nothing (docx4j-portfolio's reading, 2026-10-22). The fixtures are the portfolio's
 * bind-fun probes (Word's {@code ns0} prefix): one item in the document, none or three in the XML.
 * On the three-item fixture Word makes and fills three items, as docx4j does; with a prefix other
 * than {@code ns0}, Word rewrites the inner XPaths to {@code ns0} but not their prefix mappings, so
 * its own items come out empty, a Word quirk docx4j's own contextualising does not share.
 */
public class W15RepeatingSectionZeroItemsTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";
	private static final String DIR = System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/";

	private static String bind(String fixture, String implementation, int flags) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(DIR + fixture));
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, flags);
			return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	private static void zeroItemsLeaveTheSectionAsItIs(String implementation) throws Exception {
		String xml = bind("w15-repeating-section-zero-items.docx", implementation, Docx4J.FLAG_BIND_BIND_XML);
		assertTrue("the template item's text stays: " + xml, xml.contains("RS0_ITEM_STALE"));
		assertTrue("the section stays a Word repeating section: " + xml, xml.contains("<w15:repeatingSection/>") && xml.contains("<w15:repeatingSectionItem/>"));
		assertTrue("the template's bound control keeps its binding: " + xml, xml.contains("ns0:name[1]"));
		assertFalse("the marker is gone after binding: " + xml, xml.contains("resultRepeatZero"));
		assertTrue("the section's own tag is back: " + xml, xml.contains("<w:tag w:val=\"Items\"/>"));
		assertFalse("no placeholder was bound in: " + xml, xml.contains("w:showingPlcHdr"));
	}

	@Test
	public void zeroItemsLeaveTheSectionAsItIsXSLT() throws Exception {
		zeroItemsLeaveTheSectionAsItIs("BindingTraverserXSLT");
	}

	@Test
	public void zeroItemsLeaveTheSectionAsItIsNonXSLT() throws Exception {
		zeroItemsLeaveTheSectionAsItIs("BindingTraverserNonXSLT");
	}

	/** With the controls removed too: the template content stays on the page, the controls go (RemovalHandler.xslt's rule for the marker). */
	@Test
	public void zeroItemsWithTheControlsRemovedKeepTheTemplateContent() throws Exception {
		String xml = bind("w15-repeating-section-zero-items.docx", "BindingTraverserXSLT",
				Docx4J.FLAG_BIND_BIND_XML | Docx4J.FLAG_BIND_REMOVE_SDT);
		assertTrue(xml, xml.contains("RS0_ITEM_STALE") && xml.contains("Item: "));
		assertFalse(xml, xml.contains("<w:sdt>"));
	}

	/** The usual case is unchanged: three items in the XML give three items, the template's text gone. */
	@Test
	public void threeItemsStillBindThree() throws Exception {
		for (String implementation : new String[] { "BindingTraverserXSLT", "BindingTraverserNonXSLT" }) {
			String xml = bind("w15-repeating-section-three-items.docx", implementation, Docx4J.FLAG_BIND_BIND_XML);
			assertTrue(implementation + ": " + xml, xml.contains("RS_ITEM_A") && xml.contains("RS_ITEM_B") && xml.contains("RS_ITEM_C"));
			assertFalse(implementation + ": " + xml, xml.contains("RS_ITEM_STALE"));
			assertFalse(implementation + ": the sections are unwrapped before binding, as before: " + xml, xml.contains("w15:repeatingSection"));
			assertEquals(implementation, 3, xml.split("Item: ", -1).length - 1);
		}
	}
}
