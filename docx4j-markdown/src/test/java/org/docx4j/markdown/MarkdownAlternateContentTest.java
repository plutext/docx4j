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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McLoadMode;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/** CR-051 section 4: Markdown follows the text model, so a block-level {@code mc:AlternateContent} gives the branch Word reads, in both load modes. */
public class MarkdownAlternateContentTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static WordprocessingMLPackage built() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + p("before")
				+ "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p("CHOICEW14") + "</mc:Choice><mc:Fallback>" + p("FALLBACKTEXT") + "</mc:Fallback></mc:AlternateContent>"
				+ p("after") + "</w:body></w:document>"));
		return pkg;
	}

	private static WordprocessingMLPackage loaded(McLoadMode mode) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Docx4J.save(built(), bytes);
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bytes.toByteArray()), mode);
	}

	@Test
	public void markdownFollowsTheTextModel() throws Exception {
		for (WordprocessingMLPackage pkg : new WordprocessingMLPackage[] { built(), loaded(McLoadMode.WORD), loaded(McLoadMode.PRESERVE) }) {
			String md = new MarkdownExporter().export(pkg);
			assertTrue(md, md.contains("CHOICEW14"));
			assertFalse(md, md.contains("FALLBACKTEXT"));
		}
	}
}
