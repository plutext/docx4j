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
package org.docx4j.model.fields.docproperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.xml.datatype.DatatypeFactory;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.model.fields.FieldValueException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * DOCPROPERTY's built-in core properties: each name reads its own property, and an absent
 * property (or an absent core or extended part) is "no value", not a NullPointerException.
 */
public class DocPropertyResolverTest {

	private static final ObjectFactory DC = new ObjectFactory();

	private static SimpleLiteral literal(String text) {
		SimpleLiteral literal = DC.createSimpleLiteral();
		literal.getContent().add(text);
		return literal;
	}

	/** LastSavedBy is cp:lastModifiedBy and LastSavedTime is dcterms:modified, not the other way round. */
	@Test
	public void lastSavedByAndLastSavedTime() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		core.setLastModifiedBy("Jo Bloggs");
		core.setModified(literal("2026-10-17T09:00:00Z"));

		DocPropertyResolver resolver = new DocPropertyResolver(pkg);
		assertEquals("Jo Bloggs", resolver.getValue("LastSavedBy"));
		assertEquals("2026-10-17T09:00:00Z", resolver.getValue("LastSavedTime"));
	}

	@Test
	public void titleAndComments() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		core.setTitle(DC.createTitle(literal("The title")));
		core.setDescription(DC.createDescription(literal("The comments")));

		DocPropertyResolver resolver = new DocPropertyResolver(pkg);
		assertEquals("The title", resolver.getValue("Title"));
		assertEquals("The comments", resolver.getValue("Comments"));
	}

	/** A custom property whose name is part of a built-in one's (Page in Pages, Time in LastSavedTime). */
	@Test
	public void customPropertyNamedLikePartOfABuiltIn() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Page", "custom page");
		pkg.getDocPropsCustomPart().setProperty("Time", "custom time");

		DocPropertyResolver resolver = new DocPropertyResolver(pkg);
		assertEquals("custom page", resolver.getValue("Page"));
		assertEquals("custom time", resolver.getValue("Time"));
	}

	@Test
	public void lastPrintedAndHyperlinkBase() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getDocPropsCorePart().getJaxbElement().setLastPrinted(
				DatatypeFactory.newInstance().newXMLGregorianCalendar("2026-10-17T09:00:00Z"));
		pkg.getDocPropsExtendedPart().getJaxbElement().setHyperlinkBase("http://example.com/base/");

		DocPropertyResolver resolver = new DocPropertyResolver(pkg);
		assertEquals("2026-10-17T09:00:00Z", resolver.getValue("LastPrinted"));
		assertEquals("http://example.com/base/", resolver.getValue("HyperlinkBase"));
	}

	/** createPackage's core properties are empty: every one of these is absent. */
	@Test
	public void absentCorePropertyHasNoValue() throws Exception {
		DocPropertyResolver resolver = new DocPropertyResolver(WordprocessingMLPackage.createPackage());
		for (String key : new String[] { "Title", "Comments", "Author", "Subject", "CreateTime",
				"LastPrinted", "LastSavedBy", "LastSavedTime", "RevisionNumber" }) {
			assertNoValue(resolver, key);
		}
	}

	@Test
	public void absentPartsHaveNoValue() throws Exception {
		DocPropertyResolver resolver = new DocPropertyResolver(new WordprocessingMLPackage());
		assertNoValue(resolver, "Title");
		assertNoValue(resolver, "Company");
	}

	private static void assertNoValue(DocPropertyResolver resolver, String key) throws Exception {
		try {
			resolver.getValue(key);
			fail(key + " should have no value");
		} catch (FieldValueException expected) {
			// the caller's "no value" path
		}
	}
}
