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
package org.docx4j.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Calendar;

import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * CR-051 section 10.3 item 11: a deleted block-level {@code mc:AlternateContent} keeps its wrappers
 * in the difference, so each branch's paragraphs stay in their branch and the text shows once.
 */
public class AlternateContentDeletedTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static Body body(String content) throws Exception {
		return ((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + content + "</w:body></w:document>")).getBody();
	}

	@Test
	public void deletedElementKeepsItsWrappers() throws Exception {
		String element = "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p("GONE_W14") + "</mc:Choice><mc:Fallback>" + p("GONE_FB") + "</mc:Fallback></mc:AlternateContent>";
		Body older = body(p("same") + element);
		Body newer = body(p("same"));
		StringWriter sw = new StringWriter();
		new Differencer().diff(newer, older, new StreamResult(sw), "A", Calendar.getInstance(), null, null);
		String out = sw.toString();
		assertEquals(out, 1, count(out, "<mc:AlternateContent"));
		assertEquals(out, 1, count(out, "<mc:Choice"));
		assertEquals(out, 1, count(out, "<mc:Fallback"));
		assertEquals(out, 1, count(out, "GONE_W14"));
		assertEquals(out, 1, count(out, "GONE_FB"));
		assertTrue(out, out.contains("w:del"));
	}

	private static int count(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}
}
