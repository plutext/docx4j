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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * #634: a field's instruction is read in one place, FieldRef.getInstr(), and its name from the whole instruction,
 * which Word may split across runs. Until 17.3.5 getFldName read only the first run ("MERGE" of "MERGE", "FIELD x"),
 * and gave a nested field's name where the instruction began with one.
 */
public class FieldRefInstructionTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String instr(String text) {
		return "<w:r><w:instrText xml:space=\"preserve\">" + text + "</w:instrText></w:r>";
	}

	private static String field(String instructions, String result) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>" + instructions + "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>" + result + "</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static FieldRef fieldRef(String content) throws Exception {
		List<FieldRef> refs = new ArrayList<FieldRef>();
		FieldsPreprocessor.canonicalise((P) XmlUtils.unmarshalString("<w:p " + W + ">" + content + "</w:p>"), refs);
		assertEquals(1, refs.size());
		return refs.get(0);
	}

	@Test
	public void theInstructionIsReadWhole() throws Exception {
		FieldRef fr = fieldRef(field(instr(" MERGE") + instr("FIELD  name ") + instr("\\* MERGEFORMAT "), "«name»"));
		assertEquals(" MERGEFIELD  name \\* MERGEFORMAT ", fr.getInstr());
		assertEquals("MERGEFIELD", fr.getFldName());
		assertTrue(fr.isMergeFormat());
	}

	@Test
	public void aNestedFieldsNameIsNotThisFieldsName() throws Exception {
		FieldRef iff = fieldRef(field(instr(" IF ") + field(instr(" MERGEFIELD a "), "x") + instr(" = \"1\" \"yes\" \"no\" "), "no"));
		assertEquals("IF", iff.getFldName());
		assertNull("holds a nested field", iff.getInstr());
		FieldRef computed = fieldRef(field(field(instr(" MERGEFIELD type "), "PAGE"), "1"));
		assertNull("named by the nested field's result, when Word updates it", computed.getFldName());
	}

	@Test
	public void aMergefieldWhoseNameIsSplitIsMerged() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + ">"
				+ field(instr(" MERGE") + instr("FIELD name "), "«name»") + "</w:p>"));
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		data.put(new DataFieldName("name"), "Jo");
		new MailMerger(pkg).performMerge(data, false);
		String p = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getContent().get(0), true, false);
		assertTrue(p, p.contains(">Jo<") && !p.contains("«name»"));
	}
}
