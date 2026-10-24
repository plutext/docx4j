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

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * #277: FieldUpdater updates the document information fields Word inserts for a document property (AUTHOR,
 * COMMENTS, KEYWORDS, LASTSAVEDBY, SUBJECT, TITLE), as it does DOCPROPERTY; one with an argument, with which Word
 * sets the property, is left as it is. Until 17.3.5 only DOCPROPERTY and DOCVARIABLE were updated, and Keywords
 * wasn't one of the properties DOCPROPERTY read.
 */
public class DocInfoFieldsTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String complex(String marker, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>OLD</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
	}

	private static String simple(String marker, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" " + instr.replace("\"", "&quot;") + " \"><w:r><w:t>OLD</w:t></w:r></w:fldSimple></w:p>";
	}

	private static SimpleLiteral literal(String value) {
		SimpleLiteral l = new SimpleLiteral();
		l.getContent().add(value);
		return l;
	}

	private static WordprocessingMLPackage pkg(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		for (String p : paragraphs) pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(p));
		CoreProperties core = pkg.getDocPropsCorePart().getJaxbElement();
		ObjectFactory dc = new ObjectFactory();
		core.setTitle(dc.createTitle(literal("The Title")));
		core.setCreator(literal("Ann Author"));
		core.setDescription(dc.createDescription(literal("A comment")));
		core.setSubject(literal("The Subject"));
		core.setKeywords("alpha, beta");
		core.setLastModifiedBy("Sam Saver");
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Client", "Acme");
		return pkg;
	}

	private static String paragraph(WordprocessingMLPackage pkg, String marker) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + marker + " <")) return m.group();
		throw new AssertionError(marker + " not in " + xml);
	}

	@Test
	public void documentInformationFieldsAreUpdated() throws Exception {
		String[][] cases = { { "TITLE", "The Title" }, { "AUTHOR", "Ann Author" }, { "COMMENTS", "A comment" },
				{ "SUBJECT", "The Subject" }, { "KEYWORDS", "alpha, beta" }, { "LASTSAVEDBY", "Sam Saver" },
				{ "AUTHOR \\* Upper", "ANN AUTHOR" }, { "DOCPROPERTY Keywords", "alpha, beta" } };
		String[] paragraphs = new String[cases.length * 2];
		for (int i = 0; i < cases.length; i++) {
			paragraphs[2 * i] = complex("C" + i, cases[i][0]);
			paragraphs[2 * i + 1] = simple("S" + i, cases[i][0]);
		}
		WordprocessingMLPackage pkg = pkg(paragraphs);
		new FieldUpdater(pkg).update(false);
		for (int i = 0; i < cases.length; i++) {
			for (String m : new String[] { "C" + i, "S" + i }) {
				String p = paragraph(pkg, m);
				assertTrue(cases[i][0] + ": " + p, p.contains(">" + cases[i][1] + "<") && !p.contains(">OLD<"));
			}
		}
	}

	/** With an argument Word sets the property (AUTHOR "Jo"); docx4j leaves the field as it is */
	@Test
	public void oneWithAnArgumentIsLeft() throws Exception {
		WordprocessingMLPackage pkg = pkg(complex("C", "AUTHOR \"Jo Other\""), simple("S", "TITLE \"Other\""));
		new FieldUpdater(pkg).update(false);
		assertTrue(paragraph(pkg, "C"), paragraph(pkg, "C").contains(">OLD<"));
		assertTrue(paragraph(pkg, "S"), paragraph(pkg, "S").contains(">OLD<"));
	}

	/**
	 * #244: a property's name in any case, as Word 365 reads it (the share's docprop-case probe, 2026-10-25): a custom
	 * property named Client for client, CLIENT and "cLiEnT"; the title for title. Until 17.3.5 only an exact match.
	 */
	@Test
	public void aPropertyNameInAnyCase() throws Exception {
		String[][] cases = { { "DOCPROPERTY client", "Acme" }, { "DOCPROPERTY CLIENT", "Acme" }, { "DOCPROPERTY \"cLiEnT\"", "Acme" },
				{ "DOCPROPERTY title", "The Title" }, { "DOCPROPERTY keywords", "alpha, beta" } };
		String[] paragraphs = new String[cases.length];
		for (int i = 0; i < cases.length; i++) paragraphs[i] = complex("C" + i, cases[i][0]);
		WordprocessingMLPackage pkg = pkg(paragraphs);
		new FieldUpdater(pkg).update(false);
		for (int i = 0; i < cases.length; i++) {
			String p = paragraph(pkg, "C" + i);
			assertTrue(cases[i][0] + ": " + p, p.contains(">" + cases[i][1] + "<"));
		}
	}

	/**
	 * The probe itself: FieldUpdater gives each field the result Word 365's update gave it (fields/docprop-case, Word's
	 * save beside it), but for a missing property, where Word writes its error text and docx4j keeps the old result.
	 */
	@Test
	public void asWordUpdatedTheProbe() throws Exception {
		WordprocessingMLPackage probe = WordprocessingMLPackage.load(
				org.docx4j.utils.ResourceUtils.getResource("fields/docprop-case/docprop-case.docx"));
		WordprocessingMLPackage word = WordprocessingMLPackage.load(
				org.docx4j.utils.ResourceUtils.getResource("fields/docprop-case/docprop-case-word365.docx"));
		new FieldUpdater(probe).update(false);
		Pattern result = Pattern.compile("fldCharType=\"separate\".*?<w:t[^>]*>([^<]*)</w:t>.*?fldCharType=\"end\"", Pattern.DOTALL);
		for (int k = 1; k <= 7; k++) {
			Matcher ours = result.matcher(probeParagraph(probe, "K" + k));
			Matcher words = result.matcher(probeParagraph(word, "K" + k));
			assertTrue(ours.find() && words.find());
			String expected = k == 7 ? "OLD" : words.group(1);
			assertTrue("K" + k + ": docx4j " + ours.group(1) + ", Word " + words.group(1), expected.equals(ours.group(1)));
		}
	}

	private static String probeParagraph(WordprocessingMLPackage pkg, String marker) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + marker + " DOCPROPERTY")) return m.group();
		throw new AssertionError(marker);
	}

	/** A simple DOCPROPERTY's quoted name is read as a complex one's is */
	@Test
	public void aSimpleFieldsQuotedPropertyName() throws Exception {
		WordprocessingMLPackage pkg = pkg(simple("S", "DOCPROPERTY \"Client\""));
		new FieldUpdater(pkg).update(false);
		assertTrue(paragraph(pkg, "S"), paragraph(pkg, "S").contains(">Acme<"));
	}
}
