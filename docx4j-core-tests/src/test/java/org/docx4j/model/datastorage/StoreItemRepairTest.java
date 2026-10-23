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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Ignore;
import org.junit.Test;

/**
 * CR-058 (2): a {@code w:storeItemID} that names no custom XML part is resolved to the part the XPath
 * fits, as Word repairs it (measured in Word 365 on docx4j-portfolio's bind-plain probe, 2026-10-22: B7
 * showed its XML value and the save carried the part's ID), by the generator (both traversers, which
 * write the resolved ID back) and by the text model (which writes nothing). Several parts fit: the first,
 * in part-name order, whose XPath yields a node, else the first that fits. None fits: the control is left
 * as it is, without an exception; the property
 * {@code docx4j.model.datastorage.BindingHandler.strictStoreItemIds=true} makes a stale ID fail the bind
 * with an {@code InputIntegrityException}.
 *
 * <p>Until 17.3.5 (measured in phase 0, 2026-10-23): the NonXSLT traverser threw that exception for the
 * whole document; the XSLT traverser logged it from its error listener and went on, leaving the
 * control's content EMPTY ({@code <w:sdtContent/>}), so a stale ID silently lost the control's text.</p>
 */
public class StoreItemRepairTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";
	private static final String STRICT_PROPERTY = "docx4j.model.datastorage.BindingHandler.strictStoreItemIds";
	private static final String PART1 = "{B1D0F00D-0000-4000-8000-000000000001}";
	private static final String PART2 = "{B1D0F00D-0000-4000-8000-000000000002}";
	private static final String STALE = "{B1D0F00D-0000-4000-8000-0000000000FF}";

	private static WordprocessingMLPackage load(String fixture) throws Exception {
		return WordprocessingMLPackage.load(new java.io.File(
				System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/" + fixture));
	}

	private static String bind(String implementation, String fixture) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			WordprocessingMLPackage pkg = load(fixture);
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, Docx4J.FLAG_BIND_BIND_XML);
			return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	private static String text(String fixture) throws Exception {
		return Texts.plainText(load(fixture), TextOptions.of(TextView.ACCEPTED));
	}

	/** The control with this alias, as marshalled. */
	private static String control(String xml, String alias) {
		Matcher m = Pattern.compile("<w:sdt>(?:(?!<w:sdt>).)*?<w:alias w:val=\"" + alias + "\"/>.*?</w:sdt>", Pattern.DOTALL).matcher(xml);
		assertTrue("control " + alias + " in " + xml, m.find());
		return m.group();
	}

	private static void shows(String implementation, String xml, String alias, String value, String storeItemId) {
		String control = control(xml, alias);
		assertTrue(implementation + " " + alias + ": " + control, control.contains(">" + value + "</w:t>"));
		assertTrue(implementation + " " + alias + " store item: " + control, control.contains("w:storeItemID=\"" + storeItemId + "\""));
	}

	private static final String[] IMPLEMENTATIONS = { "BindingTraverserXSLT", "BindingTraverserNonXSLT" };

	// --- bind-plain B7: one part, which the XPath fits (Word's measured case) ---

	@Ignore("CR-058 phase 2")
	@Test
	public void theGeneratorResolvesAStaleStoreItemIdAsWordDoes() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			String xml = bind(implementation, "bind-plain.docx"); // until 17.3.5: XSLT emptied B7, NonXSLT threw
			shows(implementation, xml, "B1", "B1_XML_VALUE", PART1);
			shows(implementation, xml, "B7", "B7_XML_VALUE", PART1); // the ID written back, as Word's save has it
		}
	}

	@Ignore("CR-058 phase 2")
	@Test
	public void theTextModelShowsTheValueWordShows() throws Exception {
		String text = text("bind-plain.docx");
		assertTrue(text, text.contains("B7_XML_VALUE") && text.contains("B1_XML_VALUE"));
		assertFalse(text, text.contains("B7_STALE_SURFACE"));
	}

	@Ignore("CR-058 phase 2")
	@Test
	public void theStrictPropertyFailsTheBind() throws Exception {
		Docx4jProperties.setProperty(STRICT_PROPERTY, "true");
		try {
			for (String implementation : IMPLEMENTATIONS) {
				try {
					bind(implementation, "bind-plain.docx");
					fail(implementation + ": a stale store item ID should fail the bind when strict");
				} catch (Exception expected) {
					assertTrue(implementation + ": " + expected, String.valueOf(expected.getMessage()).contains("storeItemId")
							|| (expected.getCause() != null && String.valueOf(expected.getCause().getMessage()).contains("storeItemId")));
				}
			}
		} finally {
			Docx4jProperties.setProperty(STRICT_PROPERTY, "false");
		}
	}

	// --- two parts fit (which part Word picks is the phase 0 probe; the rule here is D4's) ---

	@Ignore("CR-058 phase 2")
	@Test
	public void whereTwoPartsFitTheFirstThatYieldsANodeIsBound() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			String xml = bind(implementation, "bind-plain-two-parts.docx");
			shows(implementation, xml, "B1", "B1_XML_VALUE", PART1);
			shows(implementation, xml, "B7", "B7_XML_VALUE_PART1", PART1); // both yield: the first in part-name order
			shows(implementation, xml, "B11", "B11_XML_VALUE_PART2", PART2); // only part 2 yields
		}
		String text = text("bind-plain-two-parts.docx");
		assertTrue(text, text.contains("B7_XML_VALUE_PART1") && text.contains("B11_XML_VALUE_PART2"));
	}

	// --- no part fits: the control is left as it is, and the document still binds ---

	@Ignore("CR-058 phase 2")
	@Test
	public void whereNoPartFitsTheControlIsLeftAndTheRestBinds() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			String xml = bind(implementation, "bind-plain-no-part.docx"); // until 17.3.5: XSLT emptied both, NonXSLT threw
			shows(implementation, xml, "B1", "B1_XML_VALUE", PART1);
			shows(implementation, xml, "B7", "B7_STALE_SURFACE", STALE);
			shows(implementation, xml, "B12", "B12_STALE_SURFACE", STALE);
		}
	}

	/** True today and after: the text model reads the surface of a binding no part resolves. */
	@Test
	public void theTextModelShowsTheSurfaceWhereNoPartFits() throws Exception {
		String text = text("bind-plain-no-part.docx");
		assertTrue(text, text.contains("B1_XML_VALUE") && text.contains("B7_STALE_SURFACE") && text.contains("B12_STALE_SURFACE"));
		assertFalse(text, text.contains("B7_XML_VALUE"));
	}
}
