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
package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTDocVar;
import org.docx4j.wml.CTDocVars;
import org.docx4j.wml.CTSettings;
import org.junit.Test;

/**
 * FieldEvaluators (CR-067 phase 0): one evaluator per field name, consumed through one method; the
 * context's clock, language, bookmarks and sequence counters; a caller's own evaluator; and
 * FieldUpdater's name filter over it.
 */
public class FieldEvaluatorsTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static SimpleLiteral literal(String value) {
		SimpleLiteral l = new SimpleLiteral();
		l.getContent().add(value);
		return l;
	}

	static WordprocessingMLPackage pkg(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		for (String p : paragraphs) pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(p));
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		ObjectFactory dc = new ObjectFactory();
		core.setTitle(dc.createTitle(literal("The Title")));
		core.setCreator(literal("Ann Author"));
		core.setKeywords("alpha, beta");
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Client", "Acme");
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		CTSettings settings = Context.getWmlObjectFactory().createCTSettings();
		CTDocVars vars = Context.getWmlObjectFactory().createCTDocVars();
		CTDocVar v = Context.getWmlObjectFactory().createCTDocVar();
		v.setName("v1");
		v.setVal("one");
		vars.getDocVar().add(v);
		settings.setDocVars(vars);
		dsp.setJaxbElement(settings);
		pkg.getMainDocumentPart().addTargetPart(dsp);
		return pkg;
	}

	private static String simple(String marker, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" " + instr.replace("\"", "&quot;") + " \"><w:r><w:t>OLD</w:t></w:r></w:fldSimple></w:p>";
	}

	private static String paragraph(WordprocessingMLPackage pkg, String marker) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + marker + " <")) return m.group();
		throw new AssertionError(marker + " not in " + xml);
	}

	@Test
	public void thePropertyFields() throws Exception {
		FieldContext ctx = FieldContext.of(pkg());
		assertEquals("Acme", FieldEvaluators.evaluate(" DOCPROPERTY Client ", ctx));
		assertEquals("the switches are applied", "ACME", FieldEvaluators.evaluate(" DOCPROPERTY \"Client\" \\* Upper ", ctx));
		assertEquals("The Title", FieldEvaluators.evaluate(" TITLE ", ctx));
		assertEquals("Ann Author", FieldEvaluators.evaluate(" AUTHOR ", ctx));
		assertEquals("an argument is what Word sets the property from, and shows", "Jo", FieldEvaluators.evaluate(" AUTHOR \"Jo\" ", ctx));
		assertEquals("alpha, beta", FieldEvaluators.evaluate(" KEYWORDS ", ctx));
		assertEquals("one", FieldEvaluators.evaluate(" DOCVARIABLE v1 ", ctx));
		assertEquals("hi there", FieldEvaluators.evaluate(" QUOTE \"hi there\" ", ctx));
		assertNull("a missing property keeps the cached result", FieldEvaluators.evaluate(" DOCPROPERTY nope ", ctx));
		assertNull("no argument", FieldEvaluators.evaluate(" DOCPROPERTY ", ctx));
		assertNull("not evaluated here", FieldEvaluators.evaluate(" FILENAME ", ctx));
		assertNull("not evaluated here", FieldEvaluators.evaluate(" MERGEFIELD x ", ctx));
		assertNull(FieldEvaluators.evaluate("", ctx));
		assertNull(FieldEvaluators.evaluate((String) null, ctx));
	}

	@Test
	public void sequencesAndReferencesAreThePasss() throws Exception {
		FieldContext ctx = FieldContext.of(pkg());
		assertEquals("1", FieldEvaluators.evaluate(" SEQ Figure ", ctx));
		assertEquals("2", FieldEvaluators.evaluate(" SEQ Figure \\* ARABIC ", ctx));
		assertEquals("\\c repeats", "2", FieldEvaluators.evaluate(" SEQ Figure \\c ", ctx));
		assertEquals("\\r resets", "5", FieldEvaluators.evaluate(" SEQ Table \\r 5 ", ctx));
		assertEquals("6", FieldEvaluators.evaluate(" SEQ Table ", ctx));
		assertEquals("\\h hides, and counts", "", FieldEvaluators.evaluate(" SEQ Figure \\h ", ctx));
		assertEquals("4", FieldEvaluators.evaluate(" SEQ Figure ", ctx));
		assertEquals("the identifier's case does not matter", "5", FieldEvaluators.evaluate(" SEQ figure ", ctx));
		assertNull(FieldEvaluators.evaluate(" SEQ ", ctx));
		assertNull("a reset that is not a number", FieldEvaluators.evaluate(" SEQ X \\r abc ", ctx));

		ctx.bookmarks().put("bm1", "Bookmarked text");
		assertEquals("Bookmarked text", FieldEvaluators.evaluate(" REF bm1 ", ctx));
		assertEquals("BOOKMARKED TEXT", FieldEvaluators.evaluate(" REF BM1 \\* Upper ", ctx));
		assertNull("a REF needing numbering or layout keeps its cached result", FieldEvaluators.evaluate(" REF bm1 \\n ", ctx));
		assertNull(FieldEvaluators.evaluate(" REF nope ", ctx));
		FieldContext another = FieldContext.of(ctx.getPackage());
		assertEquals("a new context is a new pass", "1", FieldEvaluators.evaluate(" SEQ Figure ", another));
	}

	@Test
	public void theDateFieldsReadTheClock() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2026, Calendar.MARCH, 4, 5, 6, 7);
		final Date fixed = c.getTime();
		FieldContext ctx = FieldContext.of(pkg()).clock(() -> fixed);
		assertEquals("2026-03-04", FieldEvaluators.evaluate(" DATE \\@ \"yyyy-MM-dd\" ", ctx));
		assertEquals("05:06", FieldEvaluators.evaluate(" TIME \\@ \"HH:mm\" ", ctx));
		assertNull("PRINTDATE is cp:lastPrinted, not the clock (D14); none here keeps the cached result",
				FieldEvaluators.evaluate(" PRINTDATE \\@ \"d MMMM yyyy\" ", ctx.language("en-US")));
		assertEquals("4 March 2026", FieldEvaluators.evaluate(" DATE \\@ \"d MMMM yyyy\" ", ctx.language("en-US")));
		assertEquals("4 mars 2026", FieldEvaluators.evaluate(" DATE \\@ \"d MMMM yyyy\" ", ctx.language("fr-FR")));
		assertTrue(FieldEvaluators.get("DATE").formatsItself());
		assertFalse(FieldEvaluators.get("DOCPROPERTY").formatsItself());
		assertEquals("createPackage's defaults state one", "en-US", FieldContext.documentLanguage(ctx.getPackage()));
	}

	@Test
	public void aCallersOwnEvaluator() throws Exception {
		assertTrue(FieldEvaluators.names().contains("DATE"));
		assertTrue(FieldEvaluators.has("docproperty"));
		assertFalse(FieldEvaluators.has("TESTUSER"));
		FieldEvaluators.register("TESTUSER", (instruction, context) -> context.getUserName());
		assertTrue(FieldEvaluators.has("testuser"));
		FieldContext ctx = FieldContext.of(pkg());
		assertNull("the user is absent by default: the field keeps its cached result", FieldEvaluators.evaluate(" TESTUSER ", ctx));
		assertEquals("JO", FieldEvaluators.evaluate(" TESTUSER \\* Upper ", ctx.user("Jo", "J", null)));
		assertNull("absent by default (D8)", ctx.getExternal());
		assertNull(ctx.getFileName());
	}

	// ---- tier 1 (CR-067 phase 1)

	private static WordprocessingMLPackage datedPkg(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = pkg(paragraphs);
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		core.setRevision("7");
		core.setCreated(w3cdtf("2024-02-03T04:05:06Z"));
		core.setModified(w3cdtf("2025-06-07T08:09:10Z"));
		return pkg;
	}

	private static SimpleLiteral w3cdtf(String value) {
		org.docx4j.docProps.core.dc.terms.W3CDTF w = new org.docx4j.docProps.core.dc.terms.W3CDTF();
		w.getContent().add(value);
		return w;
	}

	@Test
	public void theCorePropertyDatesRevisionAndInfo() throws Exception {
		FieldContext ctx = FieldContext.of(datedPkg()).language("en-US");
		// the probe's times are UTC; the field prints them in the machine's zone, so compare in UTC terms
		java.util.Date created = FieldSources.parseW3CDTF("2024-02-03T04:05:06Z");
		assertEquals(1706933106000L, created.getTime());
		String ymd = new java.text.SimpleDateFormat("yyyy-MM-dd").format(created);
		assertEquals(ymd, FieldEvaluators.evaluate(" CREATEDATE \\@ \"yyyy-MM-dd\" ", ctx));
		assertEquals(new java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.US).format(FieldSources.parseW3CDTF("2025-06-07T08:09:10Z")),
				FieldEvaluators.evaluate(" SAVEDATE \\@ \"d MMMM yyyy\" ", ctx));
		assertTrue("a default format, until the probe says which", FieldEvaluators.evaluate(" CREATEDATE ", ctx).length() > 0);
		assertEquals("7", FieldEvaluators.evaluate(" REVNUM ", ctx));
		assertEquals("Acme", FieldEvaluators.evaluate(" INFO DOCPROPERTY Client ", ctx));
		assertEquals("The Title", FieldEvaluators.evaluate(" INFO Title ", ctx));
		assertEquals("THE TITLE", FieldEvaluators.evaluate(" INFO Title \\* Upper ", ctx));
		assertEquals("7", FieldEvaluators.evaluate(" INFO RevNum ", ctx));
		assertNull("no name", FieldEvaluators.evaluate(" INFO ", ctx));
		assertNull("not a field's name", FieldEvaluators.evaluate(" INFO Nope ", ctx));
		assertNull("INFO INFO is not a loop", FieldEvaluators.evaluate(" INFO INFO Title ", ctx));
		assertNull(FieldSources.parseW3CDTF("not a date"));
		assertEquals(created, FieldSources.parseW3CDTF("2024-02-03T04:05:06+00:00"));
	}

	@Test
	public void sectionNoteReferenceAndTheUser() throws Exception {
		WordprocessingMLPackage pkg = pkg(
				"<w:p " + W + "><w:r><w:t xml:space=\"preserve\">A </w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"bm1\"/><w:r><w:t>Bookmarked</w:t></w:r><w:r><w:t xml:space=\"preserve\"> text</w:t></w:r><w:bookmarkEnd w:id=\"1\"/>"
				+ "<w:bookmarkStart w:id=\"2\" w:name=\"bmNote\"/><w:r><w:footnoteReference w:id=\"1\"/></w:r><w:bookmarkEnd w:id=\"2\"/>"
				+ "<w:bookmarkStart w:id=\"3\" w:name=\"bmNote2\"/><w:r><w:footnoteReference w:id=\"2\"/></w:r><w:bookmarkEnd w:id=\"3\"/>"
				+ "<w:bookmarkStart w:id=\"4\" w:name=\"bmEnd\"/><w:r><w:endnoteReference w:id=\"1\"/></w:r><w:bookmarkEnd w:id=\"4\"/></w:p>",
				"<w:p " + W + "><w:pPr><w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/></w:sectPr></w:pPr></w:p>",
				"<w:p " + W + "><w:r><w:t>in section 2</w:t></w:r></w:p>");
		FieldContext ctx = FieldContext.of(pkg).readBookmarks();
		assertEquals("Bookmarked text", FieldEvaluators.evaluate(" REF bm1 ", ctx));
		assertEquals("1", FieldEvaluators.evaluate(" NOTEREF bmNote ", ctx));
		assertEquals("2", FieldEvaluators.evaluate(" NOTEREF bmNote2 \\f ", ctx));
		assertEquals("endnotes count on their own", "1", FieldEvaluators.evaluate(" NOTEREF bmEnd ", ctx));
		assertNull(FieldEvaluators.evaluate(" NOTEREF nope ", ctx));
		assertNull("the consumer has not said which section", FieldEvaluators.evaluate(" SECTION ", ctx));
		assertEquals("2", FieldEvaluators.evaluate(" SECTION ", ctx.section(2)));
		java.util.List<Object> body = pkg.getMainDocumentPart().getContent();
		assertEquals(Integer.valueOf(1), FieldContext.sectionOf(pkg.getMainDocumentPart(), body.get(0)));
		assertEquals(Integer.valueOf(2), FieldContext.sectionOf(pkg.getMainDocumentPart(), body.get(2)));
		assertNull(FieldContext.sectionOf(null, body.get(0)));
		assertNull(FieldEvaluators.evaluate(" USERNAME ", ctx));
		assertEquals("Jo", FieldEvaluators.evaluate(" USERNAME ", ctx.user("Jo", "JB", "1 Street")));
		assertEquals("JB", FieldEvaluators.evaluate(" USERINITIALS ", ctx));
		assertEquals("1 Street", FieldEvaluators.evaluate(" USERADDRESS ", ctx));
	}

	@Test
	public void fieldUpdaterUpdatesTheTierOneFieldsInPlace() throws Exception {
		WordprocessingMLPackage pkg = datedPkg(
				"<w:p " + W + "><w:bookmarkStart w:id=\"1\" w:name=\"bm1\"/><w:r><w:t>Bookmarked text</w:t></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>",
				simple("R", "REF bm1"), simple("S1", "SECTION"), simple("C", "CREATEDATE \\@ \"yyyy\""), simple("Q", "QUOTE \"hi\""),
				simple("U", "USERNAME"), simple("I", "INFO Title"),
				"<w:p " + W + "><w:pPr><w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/></w:sectPr></w:pPr></w:p>",
				simple("S2", "SECTION"));
		new FieldUpdater(pkg).update(false);
		assertTrue(paragraph(pkg, "R").contains("Bookmarked text"));
		assertTrue(paragraph(pkg, "S1").contains(">1<"));
		assertTrue(paragraph(pkg, "S2").contains(">2<"));
		assertTrue(paragraph(pkg, "C").contains(new java.text.SimpleDateFormat("yyyy").format(FieldSources.parseW3CDTF("2024-02-03T04:05:06Z"))));
		assertTrue(paragraph(pkg, "Q").contains(">hi<"));
		assertTrue(paragraph(pkg, "I").contains("The Title"));
		assertTrue("no user supplied: the cached result stays", paragraph(pkg, "U").contains("OLD"));
		FieldUpdater withUser = new FieldUpdater(pkg);
		withUser.getContext().user("Jo", null, null);
		withUser.update(false);
		assertTrue(paragraph(pkg, "U").contains(">Jo<"));
	}

	@Test
	public void fieldUpdaterUpdatesItsNamesOnly() throws Exception {
		WordprocessingMLPackage pkg = pkg(simple("T", "TITLE"), simple("P", "DOCPROPERTY Client"), simple("D", "DATE \\@ \"yyyy\""));
		FieldUpdater updater = new FieldUpdater(pkg).only(Collections.singleton("title"));
		assertEquals(Collections.singleton("TITLE"), updater.getFieldNames());
		updater.update(false);
		assertTrue(paragraph(pkg, "T").contains("The Title"));
		assertTrue("not in the set", paragraph(pkg, "P").contains("OLD"));
		assertTrue("not in the set", paragraph(pkg, "D").contains("OLD"));

		new FieldUpdater(pkg).update(false);
		assertTrue(paragraph(pkg, "P").contains("Acme"));
		assertTrue("every evaluated field by default, DATE included (D10, phase 1)", paragraph(pkg, "D").contains(new java.text.SimpleDateFormat("yyyy").format(new Date())));
		assertTrue(new FieldUpdater(pkg).getFieldNames().contains("DOCPROPERTY"));
		assertTrue(new FieldUpdater(pkg).getFieldNames().contains("DATE"));
	}
}
