/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.utils.XPathFactoryUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendope.conditions.Xpathref;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * How an OpenDoPE condition's XPath becomes a boolean, pinned so the ports
 * (docx4j-core-ts CR-005 §7, the OpenDoPE v3 specification's table 7) have an
 * oracle rather than a reading of the code. Measured on Saxon-HE, 2026-09-24.
 *
 * With opendope.conditions.Xpathref.XPathBoolean=cast2, the whole expression
 * is wrapped as xs:boolean(...) before evaluation: a CAST of the atomized
 * node, not the effective boolean value of a node sequence. So an element
 * holding "false" is false, one holding "yes" is an error, and a path
 * selecting nothing is false. cast1 wraps in XPath 1.0 boolean(), the
 * effective boolean value, under which any non-empty text is true. The
 * default (false) is Java's Boolean.parseBoolean of the string value.
 *
 * docx4j.openpackaging.parts.XmlPart.xpath2.typechecking is orthogonal: it
 * fires only when Saxon refuses to compare xs:boolean to xs:string, and never
 * on a bare path.
 *
 * A cast error is not a false: Xpathref.xpathEval rethrows it as the unchecked
 * InputIntegrityException, which fails the whole bind.
 */
public class ConditionBooleanSemanticsTest {

	static final String STORE_ITEM_ID = "{8b049945-9dfe-4726-9de9-cf5691e53858}";

	static final String DATA = "<invoice><misc>"
			+ "<t>true</t><f>false</f><one>1</one><zero>0</zero><ws> false </ws>"
			+ "<yes>yes</yes><cap>True</cap><empty></empty><two>2</two>"
			+ "<multi>true</multi><multi>false</multi>"
			+ "</misc></invoice>";

	static CustomXmlDataStoragePart part;
	static Map<String, CustomXmlPart> parts;

	@BeforeClass
	public static void setUp() throws Exception {
		XPathFactoryUtil.setxPathFactory(new net.sf.saxon.xpath.XPathFactoryImpl());

		CustomXmlDataStorage dataStorage = new CustomXmlDataStorageImpl();
		dataStorage.setDocument(load(DATA));
		part = new CustomXmlDataStoragePart();
		part.setData(dataStorage);

		parts = new HashMap<String, CustomXmlPart>();
		parts.put(STORE_ITEM_ID, part);
	}

	static void mode(String xpathBoolean, String typechecking) {
		Docx4jProperties.setProperty("opendope.conditions.Xpathref.XPathBoolean", xpathBoolean);
		Docx4jProperties.setProperty("docx4j.openpackaging.parts.XmlPart.xpath2.typechecking", typechecking);
	}

	/** null when the evaluation throws; the caller asserts on which. */
	static Boolean cast2(String xpath) {
		try {
			return part.cachedXPathGetBoolean(xpath, null); // no prefixMappings: docx4j's own table has xs
		} catch (Docx4JException e) {
			return null;
		}
	}

	static String path(String element) {
		return "/invoice[1]/misc/" + element;
	}

	// ---- cast2 (the recommended setting): the eleven cases ---------------

	@Test
	public void cast2_lexicalSpace() {
		mode("cast2", "strict");
		assertEquals(Boolean.TRUE, cast2(path("t")));
		assertEquals(Boolean.FALSE, cast2(path("f")));
		assertEquals(Boolean.TRUE, cast2(path("one")));
		assertEquals(Boolean.FALSE, cast2(path("zero")));
		assertEquals("whitespace collapses before the cast", Boolean.FALSE, cast2(path("ws")));
	}

	@Test
	public void cast2_nothingSelectedIsFalse() {
		mode("cast2", "strict");
		assertEquals(Boolean.FALSE, cast2(path("missing")));
	}

	@Test
	public void cast2_outsideTheLexicalSpaceIsAnError() {
		mode("cast2", "strict");
		assertEquals("yes", null, cast2(path("yes")));
		assertEquals("True (case-sensitive)", null, cast2(path("cap")));
		assertEquals("empty element", null, cast2(path("empty")));
		assertEquals("2", null, cast2(path("two")));
	}

	@Test
	public void cast2_moreThanOneNodeIsAnError() {
		mode("cast2", "strict");
		assertEquals(null, cast2(path("multi")));
	}

	@Test
	public void cast2_typecheckingSettingMakesNoDifferenceOnABarePath() {
		mode("cast2", "cast2");
		assertEquals(Boolean.FALSE, cast2(path("f")));
		assertEquals(Boolean.FALSE, cast2(path("missing")));
		assertEquals(null, cast2(path("yes")));
		assertEquals(null, cast2(path("empty")));
	}

	// ---- the contrasts ----------------------------------------------------

	@Test
	public void cast1_isTheEffectiveBooleanValue() {
		mode("cast1", "strict");
		assertEquals("\"false\" is a non-empty string", Boolean.TRUE, cast2(path("f")));
		assertEquals(Boolean.TRUE, cast2(path("yes")));
		assertEquals(Boolean.TRUE, cast2(path("empty")));
		assertEquals(Boolean.TRUE, cast2(path("multi")));
		assertEquals(Boolean.FALSE, cast2(path("missing")));
	}

	@Test
	public void javaDefault_isParseBoolean() throws Exception {
		mode("false", "strict");
		assertTrue(Boolean.parseBoolean(part.xpathGetString(path("t"), null)));
		assertTrue("case-insensitive", Boolean.parseBoolean(part.xpathGetString(path("cap"), null)));
		assertFalse(Boolean.parseBoolean(part.xpathGetString(path("f"), null)));
		assertFalse(Boolean.parseBoolean(part.xpathGetString(path("yes"), null)));
		assertFalse("\"1\" is not Java-true", Boolean.parseBoolean(part.xpathGetString(path("one"), null)));
	}

	// ---- the typechecking property: where it does fire --------------------

	@Test
	public void typechecking_firesOnlyOnBooleanEqualsString() {
		mode("cast2", "strict");
		assertEquals("strict: Saxon refuses to compare xs:boolean to xs:string", null, cast2("true() = 'true'"));
		mode("cast2", "cast2");
		assertEquals("cast2 rewrites the right-hand side as xs:boolean('true')", Boolean.TRUE, cast2("true() = 'true'"));
		assertEquals("string on the left: the message is the other way round, not caught", null,
				cast2("string(/invoice[1]/misc/f) = true()"));
		mode("cast2", "cast1");
		assertEquals("cast1: boolean('true') = true()", Boolean.TRUE, cast2("true() = 'true'"));
		assertEquals("cast1: boolean('false') is true, so false() = true is false", Boolean.FALSE, cast2("false() = 'false'"));
	}

	// ---- a cast error fails the bind, it is not a false -------------------

	@Test
	public void castErrorAbortsTheBind() {
		mode("cast2", "strict");
		assertFalse(Xpathref.xpathEval(null, parts, STORE_ITEM_ID, path("f"), null));
		try {
			Xpathref.xpathEval(null, parts, STORE_ITEM_ID, path("yes"), null);
			fail("expected InputIntegrityException");
		} catch (InputIntegrityException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("cannot be cast to a boolean"));
		}
	}

	static Document load(String xml) throws Exception {
		return XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(xml)));
	}
}
