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
package org.docx4j.model.text;

import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.junit.Test;

/**
 * ASK and FILLIN under {@code fields: update} (WD REQ-506; python-64's reading, 2026-10-29): an ASK shows nothing and
 * defines its bookmark with its {@code \d} default, so a REF after it reads that text; a FILLIN shows its default;
 * without one, nothing. Under the default, cached, mode the stored results are read.
 */
public class FieldValuesPromptsTest {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	static String field(String instr, String cached) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\">" + instr.replace("\"", "&quot;")
				+ "</w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + cached
				+ "</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	static void add(MainDocumentPart mdp, String label, String instr) throws Exception {
		mdp.addObject(XmlUtils.unmarshalString("<w:p " + W + "><w:r><w:t xml:space=\"preserve\">" + label + " = </w:t></w:r>" + field(instr, "OLD") + "</w:p>"));
	}

	@Test
	public void askAndFillinComputed() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		add(mdp, "A", " ASK Who \"Your name?\" \\d \"Jane\" ");
		add(mdp, "R", " REF Who ");
		add(mdp, "F", " FILLIN \"Colour?\" \\d \"blue\" ");
		add(mdp, "N", " FILLIN \"Colour?\" ");
		String updated = PlainText.of(Texts.of(pkg, new TextOptions().fields(TextOptions.Fields.UPDATE)));
		assertTrue(updated, updated.contains("A = \n") && updated.contains("R = Jane\n") && updated.contains("F = blue\n") && updated.contains("N = \n"));
		String cached = PlainText.of(Texts.of(pkg, new TextOptions()));
		assertTrue(cached, cached.contains("A = OLD\n") && cached.contains("R = OLD\n") && cached.contains("F = OLD\n"));
	}
}
