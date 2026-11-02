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
package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * Each block's address written before it ({@link MarkdownExportOptions#setAddressComments}): before a
 * list or quote once, before a code block of several paragraphs once, before a note's definition; and
 * the export without the option as before.
 */
public class MarkdownAddressCommentsTest {

	private static final String MD = "# Title\n\nFirst paragraph.[^1]\n\n1. one\n2. two\n3. three\n\n- red\n- blue\n\n"
			+ "| a | b |\n| --- | --- |\n| 1 | 2 |\n\n```\nx = 1\ny = 2\n```\n\n> quote one\n>\n> quote two\n\n"
			+ "Last.\n\n[^1]: The note.\n";

	private static WordprocessingMLPackage document() throws Exception {
		WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(MD);
		((P) pkg.getMainDocumentPart().getContent().get(0)).setParaId("5A2B1C3D");
		// a code block of two paragraphs (the importer writes a fence as one, its lines broken)
		pkg.getMainDocumentPart().addStyledParagraphOfText(ImportStyles.SOURCE_CODE, "a = 1");
		pkg.getMainDocumentPart().addStyledParagraphOfText(ImportStyles.SOURCE_CODE, "b = 2");
		return pkg;
	}

	@Test
	public void addressBeforeEachTopLevelBlock() throws Exception {
		String md = new MarkdownExporter(new MarkdownExportOptions().setAddressComments(true)).export(document());
		assertEquals("<!-- body/0 paraId=5A2B1C3D -->\n\n# Title\n\n"
				+ "<!-- body/1 -->\n\nFirst paragraph.[^1]\n\n"
				// a list's items are blocks 2 to 4, the comment before the list alone; the next list has its own
				+ "<!-- body/2 -->\n\n1. one\n2. two\n3. three\n\n"
				+ "<!-- body/5 -->\n\n- red\n- blue\n\n"
				+ "<!-- body/7 -->\n\n|a|b|\n|---|---|\n|1|2|\n\n"
				+ "<!-- body/8 -->\n\n```\nx = 1\ny = 2\n```\n\n"
				// a quote's paragraphs are blocks 9 and 10, the comment before the quote alone
				+ "<!-- body/9 -->\n\n> quote one\n> \n> quote two\n\n"
				+ "<!-- body/11 -->\n\nLast.\n\n"
				// a code block of two paragraphs: the first's
				+ "<!-- body/12 -->\n\n```\na = 1\nb = 2\n```\n\n"
				// a note's definition: its first block's
				+ "<!-- footnote:1/0 -->\n\n[^1]: The note.\n", md);
	}

	@Test
	public void withoutTheOptionAsBefore() throws Exception {
		String plain = new MarkdownExporter().export(document());
		String commented = new MarkdownExporter(new MarkdownExportOptions().setAddressComments(true)).export(document());
		assertEquals(plain, commented.replaceAll("<!-- [^\\n]* -->\n\n", ""));
		assertEquals(-1, plain.indexOf("<!--"));
	}
}
