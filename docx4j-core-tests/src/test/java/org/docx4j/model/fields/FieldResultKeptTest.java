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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.model.fields.merge.MailMerger.OutputField;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Test;

/**
 * A field that FieldUpdater or MailMerger doesn't write keeps its result. Until 17.3.5 FieldsPreprocessor dropped
 * every unlocked MERGEFIELD's and DOCPROPERTY's result, whoever the caller, so FieldUpdater emptied a MERGEFIELD's
 * «name», MailMerger emptied a DOCPROPERTY's result, and FieldUpdater emptied a DOCPROPERTY whose property it couldn't
 * find. A merged MERGEFIELD still shows its value, or nothing where there is none; with \* MERGEFORMAT the value
 * takes the old result's formatting, as Word gives it.
 */
public class FieldResultKeptTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A paragraph: a marker, then a complex field whose result is bold and whose end run is italic */
	private static String complex(String marker, String... instr) {
		StringBuilder sb = new StringBuilder("<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>");
		for (String i : instr) sb.append("<w:r><w:instrText xml:space=\"preserve\">" + i + "</w:instrText></w:r>");
		return sb.append("<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:rPr><w:b/></w:rPr><w:t>OLD</w:t></w:r>"
				+ "<w:r><w:rPr><w:i/></w:rPr><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>").toString();
	}

	private static String simple(String marker, String instr) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" " + instr + " \"><w:r><w:t>OLD</w:t></w:r></w:fldSimple></w:p>";
	}

	private static WordprocessingMLPackage pkg(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		for (String p : paragraphs) pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(p));
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Client", "Acme");
		return pkg;
	}

	/** The paragraph holding the marker, as XML; null where it has gone */
	private static String paragraph(WordprocessingMLPackage pkg, String marker) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + marker + " <")) return m.group();
		return null;
	}

	/** The run holding the text, as XML */
	private static String run(String paragraph, String text) {
		Matcher m = Pattern.compile("<w:r>(?:(?!<w:r>).)*?<w:t[^>]*>" + text + "</w:t></w:r>", Pattern.DOTALL).matcher(paragraph);
		assertTrue(text + " in " + paragraph, m.find());
		return m.group();
	}

	private static Map<DataFieldName, String> name(String value) {
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		if (value != null) data.put(new DataFieldName("name"), value);
		return data;
	}

	@After
	public void defaultOutput() {
		MailMerger.setMERGEFIELDInOutput(OutputField.REMOVED);
	}

	@Test
	public void fieldUpdaterKeepsAMergefieldsResult() throws Exception {
		WordprocessingMLPackage pkg = pkg(complex("PLAIN", " MERGEFIELD name "),
				complex("MERGEFORMAT", " MERGEFIELD name \\* MERGEFORMAT "));
		new FieldUpdater(pkg).update(false);
		for (String m : new String[] { "PLAIN", "MERGEFORMAT" }) {
			assertTrue(m, run(paragraph(pkg, m), "OLD").contains("<w:b/>"));
		}
	}

	@Test
	public void fieldUpdaterKeepsADocpropertysResultWhereThePropertyIsMissing() throws Exception {
		WordprocessingMLPackage pkg = pkg(complex("MISSING", " DOCPROPERTY Nope "), complex("FOUND", " DOCPROPERTY Client "));
		new FieldUpdater(pkg).update(false);
		assertTrue(paragraph(pkg, "MISSING"), paragraph(pkg, "MISSING").contains(">OLD<"));
		String found = paragraph(pkg, "FOUND");
		assertTrue(found, found.contains(">Acme<") && !found.contains(">OLD<"));
		assertTrue("no MERGEFORMAT: the end run's formatting, as before", run(found, "Acme").contains("<w:i/>"));
	}

	@Test
	public void mailMergerKeepsADocpropertysResult() throws Exception {
		WordprocessingMLPackage pkg = pkg(complex("COMPLEX", " DOCPROPERTY Client "), simple("SIMPLE", "DOCPROPERTY Client"),
				complex("MERGED", " MERGEFIELD name "));
		new MailMerger(pkg).performMerge(name("Jo"), false);
		assertTrue(paragraph(pkg, "COMPLEX"), run(paragraph(pkg, "COMPLEX"), "OLD").contains("<w:b/>"));
		assertTrue(paragraph(pkg, "SIMPLE"), paragraph(pkg, "SIMPLE").contains(">OLD<"));
		assertTrue(paragraph(pkg, "MERGED"), paragraph(pkg, "MERGED").contains(">Jo<"));
	}

	/** A merged value: without \* MERGEFORMAT the end run's formatting, as before; with it, the old result's */
	@Test
	public void aMergedValuesFormatting() throws Exception {
		WordprocessingMLPackage pkg = pkg(complex("PLAIN", " MERGEFIELD name "),
				complex("MERGEFORMAT", " MERGEFIELD name \\* MERGEFORMAT "),
				complex("SPLIT", " MERGEFIELD name ", "\\* MERGEFORMAT "));
		new MailMerger(pkg).performMerge(name("Jo"), false);
		String plain = run(paragraph(pkg, "PLAIN"), "Jo");
		assertTrue(plain, plain.contains("<w:i/>") && !plain.contains("<w:b/>"));
		for (String m : new String[] { "MERGEFORMAT", "SPLIT" }) {
			String merged = paragraph(pkg, m);
			assertTrue(m + ": " + merged, run(merged, "Jo").contains("<w:b/>") && !merged.contains(">OLD<"));
		}
	}

	/** No value merges as nothing, whatever the output option: the old result isn't left in the output */
	@Test
	public void noValueMergesAsNothing() throws Exception {
		for (OutputField fate : new OutputField[] { OutputField.DEFAULT, OutputField.KEEP_MERGEFIELD, OutputField.REMOVED }) {
			MailMerger.setMERGEFIELDInOutput(fate);
			WordprocessingMLPackage pkg = pkg(complex("EMPTY", " MERGEFIELD name "));
			new MailMerger(pkg).performMerge(name(null), false);
			String p = paragraph(pkg, "EMPTY");
			assertFalse(fate + ": " + p, p != null && p.contains(">OLD<"));
		}
	}
}
