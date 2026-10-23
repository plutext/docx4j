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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSdtDropDownList;
import org.docx4j.wml.SdtPr;
import org.junit.Test;

/**
 * Typed controls bound to a custom XML part, as Word 365 shows them (measured 2026-10-22 on
 * docx4j-portfolio's bind-typed probe, the fixture here): a drop-down shows the item whose value
 * is bound, by its display text, and gets {@code w:lastValue}; a combo box bound to text in no
 * item shows the text, and gets it as {@code w:lastValue}; a date control shows the stored date's
 * nominal day in its format; a check box bound to true is checked. Until 17.3.4 the drop-down and
 * combo box showed the bound value itself.
 */
public class TypedControlBindingTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";

	private static String bind(String implementation) throws Exception {
		return bind(implementation, "typed-controls.docx");
	}

	private static String bind(String implementation, String fixture) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
					System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/" + fixture));
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, Docx4J.FLAG_BIND_BIND_XML);
			return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	/** The control with this alias, as marshalled. */
	private static String control(String xml, String alias) {
		Matcher m = Pattern.compile("<w:sdt>(?:(?!<w:sdt>).)*?<w:alias w:val=\"" + alias + "\"/>.*?</w:sdt>", Pattern.DOTALL).matcher(xml);
		assertTrue("control " + alias + " in " + xml, m.find());
		return m.group();
	}

	private static void wordsReading(String implementation) throws Exception {
		wordsReading(implementation, "typed-controls.docx");
	}

	private static void wordsReading(String implementation, String fixture) throws Exception {
		String xml = bind(implementation, fixture);

		String dd2 = control(xml, "DD2");
		assertTrue(implementation + " DD2: " + dd2, dd2.contains(">DD2_SECOND_ITEM</w:t>"));
		assertFalse(implementation + " DD2 shows the value: " + dd2, dd2.contains(">v2</w:t>"));
		assertTrue(implementation + " DD2 lastValue: " + dd2, dd2.contains("<w:dropDownList w:lastValue=\"v2\">"));

		String cb3 = control(xml, "CB3");
		assertTrue(implementation + " CB3: " + cb3, cb3.contains(">CB3_XML_FREE_TEXT</w:t>"));
		assertTrue(implementation + " CB3 lastValue: " + cb3, cb3.contains("<w:comboBox w:lastValue=\"CB3_XML_FREE_TEXT\">"));

		String dt1 = control(xml, "DT1");
		assertTrue(implementation + " DT1: " + dt1, dt1.contains(">22 October 2026</w:t>"));

		String ck4 = control(xml, "CK4");
		assertTrue(implementation + " CK4: " + ck4, ck4.contains("<w14:checked w14:val=\"1\"/>") || ck4.contains("<w14:checked w14:val=\"true\"/>"));
		assertTrue(implementation + " CK4 glyph: " + ck4, ck4.contains("☒"));
	}

	@Test
	public void boundTypedControlsShowWhatWordShowsXSLT() throws Exception {
		wordsReading("BindingTraverserXSLT");
	}

	@Test
	public void boundTypedControlsShowWhatWordShowsNonXSLT() throws Exception {
		wordsReading("BindingTraverserNonXSLT");
	}

	/**
	 * CR-054: the same probe as docx4j-portfolio's bind-typed.docx has it, prefix {@code p}, which docx4j's
	 * built-in table maps to PresentationML. Until 17.3.5 the table won and every control showed its
	 * placeholder; the binding's own mappings now win, so the two fixtures bind alike.
	 */
	@Test
	public void aBindingPrefixInDocx4jsTableBindsAlikeXSLT() throws Exception {
		wordsReading("BindingTraverserXSLT", "typed-controls-p-prefix.docx");
	}

	@Test
	public void aBindingPrefixInDocx4jsTableBindsAlikeNonXSLT() throws Exception {
		wordsReading("BindingTraverserNonXSLT", "typed-controls-p-prefix.docx");
	}

	/** The mapping on its own: a value in no item is shown as it is; the last value reverses the display text. */
	@Test
	public void listControlsMapValuesAndTexts() throws Exception {
		SdtPr sdtPr = (SdtPr) XmlUtils.unmarshalString("<w:sdtPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:dropDownList><w:listItem w:displayText=\"One\" w:value=\"1\"/><w:listItem w:displayText=\"Two\" w:value=\"2\"/></w:dropDownList></w:sdtPr>");
		assertEquals("Two", ListControls.displayTextFor(sdtPr, "2"));
		assertEquals("3", ListControls.displayTextFor(sdtPr, "3"));
		assertEquals("", ListControls.displayTextFor(sdtPr, ""));
		Object list = ListControls.listOf(sdtPr);
		assertEquals("2", ListControls.lastValueFor(list, "Two"));
		assertEquals("3", ListControls.lastValueFor(list, "3"));
		ListControls.setLastValue(sdtPr, "Two");
		assertEquals("2", ((CTSdtDropDownList) list).getLastValue());
		assertEquals("a plain text control is not a list", "x", ListControls.displayTextFor(
				(SdtPr) XmlUtils.unmarshalString("<w:sdtPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:text/></w:sdtPr>"), "x"));
	}
}
