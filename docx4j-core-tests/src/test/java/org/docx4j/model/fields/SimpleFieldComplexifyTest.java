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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * #78: a simple field made complex (FieldsPreprocessor.complexifyFields, which the mergers run first) keeps its
 * result as it was: each run with its formatting and content, and the bookmarks between them. Until 17.3.5 the
 * result's text went into one run, in the first run's formatting, and the bookmarks were dropped.
 */
public class SimpleFieldComplexifyTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** #78's sample, with a second run formatted differently */
	private static final String SAMPLE = "<w:p " + W + "><w:fldSimple w:instr=\" MERGEFIELD  num  \\* MERGEFORMAT \">"
			+ "<w:r><w:rPr><w:noProof/></w:rPr><w:t>«</w:t></w:r>"
			+ "<w:bookmarkStart w:name=\"num\" w:id=\"0\"/><w:bookmarkStart w:name=\"_GoBack\" w:id=\"1\"/>"
			+ "<w:r><w:rPr><w:b/></w:rPr><w:t>num</w:t><w:tab/></w:r>"
			+ "<w:bookmarkEnd w:id=\"0\"/><w:bookmarkEnd w:id=\"1\"/>"
			+ "<w:r><w:rPr><w:noProof/></w:rPr><w:t>»</w:t></w:r></w:fldSimple></w:p>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(SAMPLE));
		return pkg;
	}

	private static String body(WordprocessingMLPackage pkg) {
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getContent().get(0), true, false);
	}

	@Test
	public void theResultIsKeptAsItWas() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		FieldsPreprocessor.complexifyFields(pkg.getMainDocumentPart());
		String p = body(pkg);
		assertFalse(p, p.contains("fldSimple"));
		assertTrue(p, p.contains("<w:bookmarkStart w:name=\"num\"") && p.contains("<w:bookmarkStart w:name=\"_GoBack\"")
				&& p.contains("<w:bookmarkEnd w:id=\"0\"/>") && p.contains("<w:bookmarkEnd w:id=\"1\"/>"));
		assertTrue(p, p.contains("<w:r><w:rPr><w:b/></w:rPr><w:t>num</w:t><w:tab/></w:r>"));
		// in order: begin and separate, «, the bookmarks, num, », end
		String[] order = { "fldCharType=\"begin\"", "fldCharType=\"separate\"", ">«<", "w:name=\"num\"", ">num<",
				"<w:bookmarkEnd w:id=\"1\"/>", ">»<", "fldCharType=\"end\"" };
		int at = -1;
		for (String o : order) {
			int next = p.indexOf(o, at + 1);
			assertTrue(o + " after " + at + " in " + p, next > at);
			at = next;
		}
		assertEquals(p, 1, p.split("fldCharType=\"begin\"", -1).length - 1);
	}

	/** Merged, the value replaces the result's text, and the bookmarks stay */
	@Test
	public void mergedTheBookmarksStay() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		data.put(new DataFieldName("num"), "42");
		new MailMerger(pkg).performMerge(data, false);
		String p = body(pkg);
		assertTrue(p, p.contains(">42<") && p.contains("_GoBack") && p.contains("<w:bookmarkEnd w:id=\"0\"/>"));
		assertFalse(p, p.contains(">«<") || p.contains(">num<") || p.contains(">»<"));
	}
}
