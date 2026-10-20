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
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McBlockLevel;
import org.docx4j.jaxb.McProbeDocx;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/** The anonymiser's CLI loads in {@code preserve} mode (CR-051 section 3.2, D5): its output keeps every branch of a block-level {@code mc:AlternateContent}. */
public class AnonymizeCliPreserveTest {

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	@Test
	public void outputKeepsEveryBranch() throws Exception {
		File in = File.createTempFile("mc-anon-in", ".docx");
		File out = File.createTempFile("mc-anon-out", ".docx");
		try {
			String element = "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p("Choice text") + "</mc:Choice>"
					+ "<mc:Fallback>" + p("Fallback text") + "</mc:Fallback></mc:AlternateContent>";
			Files.write(in.toPath(), McProbeDocx.body(p("before") + element + p("after")).bytes());
			assertEquals(0, AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--no-fonts" }));
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(out, McBlockLevel.PRESERVE);
			List<Object> content = pkg.getMainDocumentPart().getContent();
			assertEquals(3, content.size());
			Object element2 = XmlUtils.unwrap(content.get(1));
			assertTrue(element2.getClass().getName(), element2 instanceof AlternateContent);
			assertEquals(1, ((AlternateContent) element2).getChoice().size());
			assertNotNull(((AlternateContent) element2).getFallback());
		} finally {
			in.delete();
			out.delete();
		}
	}
}
