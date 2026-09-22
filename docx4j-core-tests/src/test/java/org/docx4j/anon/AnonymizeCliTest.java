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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The command line: exit 0 and a JSON report when clean, 1 in KEEP mode with
 * something kept, 2 on a usage error.
 */
public class AnonymizeCliTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File copy(String resource) throws Exception {
		File f = tmp.newFile(resource.substring(resource.lastIndexOf('/') + 1));
		Files.write(f.toPath(), AnonymizeProbesTest.resource(resource));
		return f;
	}

	@Test
	public void strictCleanExitsZero() throws Exception {
		File in = copy("anon/chart.docx");
		File out = new File(tmp.getRoot(), "out.docx");
		File json = new File(tmp.getRoot(), "report.json");
		int exit = AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--json", json.getPath(), "--no-fonts" });
		assertEquals(0, exit);
		assertTrue(out.length() > 0);
		String report = new String(Files.readAllBytes(json.toPath()), StandardCharsets.UTF_8);
		assertTrue(report, report.contains("\"clean\": true"));
		assertTrue(report, report.contains("\"verified\": true"));
		WordprocessingMLPackage.load(out); // reloads
	}

	@Test
	public void keepWithAnEmbeddingExitsOne() throws Exception {
		File in = copy("OLE/inserted doc.docx");
		File out = new File(tmp.getRoot(), "out.docx");
		int exit = AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--keep", "--no-fonts" });
		assertEquals(1, exit);
		assertTrue("the output is still written", out.length() > 0);
	}

	@Test
	public void usage() {
		assertEquals(2, AnonymizeCli.run(new String[] {}));
		assertEquals(2, AnonymizeCli.run(new String[] { "a.docx" }));
		assertEquals(2, AnonymizeCli.run(new String[] { "a.docx", "b.docx", "--bogus" }));
	}

}
