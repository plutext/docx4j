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
package org.docx4j.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TsvTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void cleanNeedsNoQuoting() {
		assertEquals("a b c 'd' e", Tsv.clean("a\tb\nc \"d\"\re"));
		assertEquals("", Tsv.clean(null));
		assertEquals("same", Tsv.clean("same"));
		assertEquals("1\t\tx", Tsv.row(1, null, "x"));
	}

	@Test
	public void repairTailCutsTheUnfinishedLine() throws Exception {
		Path p = tmp.getRoot().toPath().resolve("t.tsv");
		Files.write(p, "h\nrow1\nrow2-unfin".getBytes(StandardCharsets.UTF_8));
		assertEquals(10, Tsv.repairTail(p));
		assertEquals("h\nrow1\n", new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
		assertEquals(0, Tsv.repairTail(p));
	}

	@Test
	public void orphanMessagesDropped() throws Exception {
		Path main = tmp.getRoot().toPath().resolve("m.tsv");
		Path msgs = tmp.getRoot().toPath().resolve("msgs.tsv");
		String a = "a".repeat(64), b = "b".repeat(64);
		Files.write(main, ("sha256\tx\n" + a + "\t1\n").getBytes(StandardCharsets.UTF_8));
		Files.write(msgs, ("sha256\tm\n" + a + "\tkept\n" + b + "\torphan\n").getBytes(StandardCharsets.UTF_8));
		DoneSet done = DoneSet.read(main);
		assertTrue(done.contains(a));
		assertFalse(done.contains(b));
		assertEquals(1, done.size());
		assertEquals(1, Tsv.dropOrphanMessages(msgs, done));
		assertEquals("sha256\tm\n" + a + "\tkept\n", new String(Files.readAllBytes(msgs), StandardCharsets.UTF_8));
	}
}
