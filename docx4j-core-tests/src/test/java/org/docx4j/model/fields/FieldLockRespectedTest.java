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
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A locked field ({@code w:fldLock}) keeps its result: FieldUpdater doesn't update a locked DOCPROPERTY or
 * DOCVARIABLE, simple or complex, and MailMerger doesn't merge a locked MERGEFIELD, which keeps its field code and
 * result whatever the output option. A simple field keeps its lock when MailMerger makes it complex. Until 17.3.5
 * docx4j read the lock (#697) but updated and merged locked fields all the same.
 */
public class FieldLockRespectedTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A paragraph: a marker, then a complex field with the given begin attributes, instruction and result */
	private static String complex(String marker, String beginAttributes, String instr, String result) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"" + beginAttributes + "/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + result + "</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
	}

	/** A paragraph: a marker, then a simple field */
	private static String simple(String marker, String attributes, String instr, String result) {
		return "<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + marker + " </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" " + instr + " \"" + attributes + "><w:r><w:t>" + result + "</w:t></w:r></w:fldSimple></w:p>";
	}

	private static WordprocessingMLPackage pkg(String... paragraphs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		for (String p : paragraphs) pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(p));
		return pkg;
	}

	/** The paragraph holding the marker, as XML */
	private static String paragraph(WordprocessingMLPackage pkg, String marker) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		Matcher m = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL).matcher(xml);
		while (m.find()) if (m.group().contains(">" + marker + " <")) return m.group();
		throw new AssertionError(marker + " not in " + xml);
	}

	private static WordprocessingMLPackage withClient(WordprocessingMLPackage pkg) throws Exception {
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Client", "Acme");
		return pkg;
	}

	@Test
	public void fieldUpdaterLeavesALockedDocproperty() throws Exception {
		WordprocessingMLPackage pkg = withClient(pkg(
				complex("LOCKED", " w:fldLock=\"1\"", "DOCPROPERTY Client", "Old"),
				complex("UNLOCKED", "", "DOCPROPERTY Client", "Old")));
		new FieldUpdater(pkg).update(false);
		String locked = paragraph(pkg, "LOCKED");
		assertTrue(locked, locked.contains(">Old<") && !locked.contains("Acme"));
		String unlocked = paragraph(pkg, "UNLOCKED");
		assertTrue(unlocked, unlocked.contains(">Acme<") && !unlocked.contains(">Old<"));
	}

	@Test
	public void fieldUpdaterLeavesALockedSimpleDocproperty() throws Exception {
		WordprocessingMLPackage pkg = withClient(pkg(
				simple("LOCKED", " w:fldLock=\"1\"", "DOCPROPERTY Client", "Old"),
				simple("UNLOCKED", "", "DOCPROPERTY Client", "Old")));
		new FieldUpdater(pkg).update(false);
		String locked = paragraph(pkg, "LOCKED");
		assertTrue(locked, locked.contains(">Old<") && !locked.contains("Acme"));
		String unlocked = paragraph(pkg, "UNLOCKED");
		assertTrue(unlocked, unlocked.contains(">Acme<"));
	}

	@Test
	public void mailMergerLeavesALockedMergefield() throws Exception {
		WordprocessingMLPackage pkg = pkg(
				complex("LOCKED", " w:fldLock=\"1\"", "MERGEFIELD name", "«name»"),
				complex("UNLOCKED", "", "MERGEFIELD name", "«name»"));
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		data.put(new DataFieldName("name"), "Jo");
		new MailMerger(pkg).performMerge(data, false);
		String locked = paragraph(pkg, "LOCKED");
		assertTrue(locked, locked.contains("«name»") && !locked.contains(">Jo<"));
		assertTrue("the field kept: " + locked, locked.contains("MERGEFIELD") && locked.contains("fldLock"));
		String unlocked = paragraph(pkg, "UNLOCKED");
		assertTrue(unlocked, unlocked.contains(">Jo<") && !unlocked.contains("fldChar")); // merged to text (REMOVED)
	}

	/** With no value, an unlocked MERGEFIELD is removed (REMOVED, the default); a locked one stays */
	@Test
	public void mailMergerLeavesALockedMergefieldWithoutAValue() throws Exception {
		WordprocessingMLPackage pkg = pkg(
				complex("LOCKED", " w:fldLock=\"1\"", "MERGEFIELD name", "«name»"),
				complex("UNLOCKED", "", "MERGEFIELD name", "«name»"));
		new MailMerger(pkg).performMerge(new HashMap<DataFieldName, String>(), false);
		String locked = paragraph(pkg, "LOCKED");
		assertTrue(locked, locked.contains("«name»") && locked.contains("MERGEFIELD"));
		String unlocked = paragraph(pkg, "UNLOCKED");
		assertFalse(unlocked, unlocked.contains("MERGEFIELD"));
	}

	/** MailMerger makes simple fields complex first; the lock (and dirty flag) go with them */
	@Test
	public void aSimpleMergefieldKeepsItsLockWhenMadeComplex() throws Exception {
		WordprocessingMLPackage pkg = pkg(simple("LOCKED", " w:fldLock=\"1\" w:dirty=\"true\"", "MERGEFIELD name", "«name»"),
				simple("UNLOCKED", "", "MERGEFIELD name", "«name»"));
		FieldsPreprocessor.complexifyFields(pkg.getMainDocumentPart());
		String complexified = paragraph(pkg, "LOCKED");
		assertTrue(complexified, Pattern.compile("<w:fldChar [^>]*w:fldLock=\"(1|true)\"").matcher(complexified).find());
		assertTrue(complexified, Pattern.compile("<w:fldChar [^>]*w:dirty=\"true\"").matcher(complexified).find());
		assertFalse(paragraph(pkg, "UNLOCKED"), paragraph(pkg, "UNLOCKED").contains("fldLock"));

		WordprocessingMLPackage merged = pkg(simple("LOCKED", " w:fldLock=\"1\"", "MERGEFIELD name", "«name»"));
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		data.put(new DataFieldName("name"), "Jo");
		new MailMerger(merged).performMerge(data, false);
		String locked = paragraph(merged, "LOCKED");
		assertTrue(locked, locked.contains("«name»") && !locked.contains(">Jo<"));
	}
}
