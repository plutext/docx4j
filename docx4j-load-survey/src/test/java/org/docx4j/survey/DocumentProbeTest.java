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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** One document's row, through the worker's own path (a thread of its own, the timeout). */
public class DocumentProbeTest {

	@ClassRule
	public static TemporaryFolder tmp = new TemporaryFolder();

	private static Path dir;

	@BeforeClass
	public static void warmUp() throws Exception {
		dir = tmp.getRoot().toPath();
		survey(Fixtures.docx(dir, "warm.docx", Fixtures.GOOD)); // docx4j's one-off initialisation, as the worker does
	}

	private static WorkerMain.Outcome survey(Path p) throws Exception {
		return WorkerMain.survey("ab".repeat(32), p, 60_000, true);
	}

	private static Object v(WorkerMain.Outcome o, Column c) {
		return o.row[c.ordinal()];
	}

	@Test
	public void good() throws Exception {
		WorkerMain.Outcome o = survey(Fixtures.docx(dir, "good.docx", Fixtures.GOOD));
		assertFalse(o.exit);
		assertEquals("ok", v(o, Column.OUTCOME));
		assertEquals(1, v(o, Column.LOADED));
		assertEquals(1, v(o, Column.MAIN_OK));
		assertEquals("WordprocessingMLPackage", v(o, Column.PACKAGE));
		assertEquals(0, v(o, Column.STRICT));
		assertEquals(1, v(o, Column.PARTS));
		assertEquals(0, v(o, Column.PARTS_UNREADABLE));
		assertEquals(0, v(o, Column.WARNINGS));
		assertEquals(0, v(o, Column.MC_ELEMENTS));
		assertNull(v(o, Column.MC_CHOICES));
		assertEquals(0, v(o, Column.LENIENT_TRUNCATED));
		assertEquals(1, v(o, Column.SAVED));
		assertEquals(DocumentProbe.DOCX4J_VERSION, v(o, Column.DOCX4J_VERSION));
		assertTrue(o.messages.isEmpty());
	}

	@Test
	public void notAZip() throws Exception {
		Path p = dir.resolve("notzip.docx");
		Files.write(p, "<html>not found</html>".getBytes(StandardCharsets.UTF_8));
		WorkerMain.Outcome o = survey(p);
		assertFalse(o.exit);
		assertEquals("load_failed", v(o, Column.OUTCOME));
		assertEquals(0, v(o, Column.LOADED));
		assertTrue(String.valueOf(v(o, Column.EXCEPTION)), String.valueOf(v(o, Column.EXCEPTION)).startsWith("org.docx4j.openpackaging.exceptions."));
		assertNull("not loaded: nothing read", v(o, Column.MAIN_OK));
		assertNull(v(o, Column.SAVED));
	}

	@Test
	public void mainPartNotWellFormed() throws Exception {
		WorkerMain.Outcome o = survey(Fixtures.docxRaw(dir, "badmain.docx",
				"<w:document xmlns:w=\"" + Fixtures.W + "\"><w:body><w:p></w:body></w:document>"));
		assertEquals("main_unreadable", v(o, Column.OUTCOME));
		assertEquals("load leaves the part unread", 1, v(o, Column.LOADED));
		assertEquals(0, v(o, Column.MAIN_OK));
		assertEquals(1, v(o, Column.PARTS_UNREADABLE));
		assertTrue(String.valueOf(v(o, Column.UNREADABLE)), String.valueOf(v(o, Column.UNREADABLE)).startsWith("/word/document.xml: "));
		assertTrue("docx4j logs the failure", (Integer) v(o, Column.ERRORS) + (Integer) v(o, Column.WARNINGS) > 0);
		assertFalse(o.messages.isEmpty());
		assertEquals("read", o.messages.get(0)[Column.Message.PHASE.ordinal()]);
	}

	@Test
	public void lenientIntegersCountedForTheDocument() throws Exception {
		WorkerMain.Outcome o = survey(Fixtures.docx(dir, "lenient.docx", Fixtures.LENIENT));
		assertEquals("ok", v(o, Column.OUTCOME));
		assertEquals(1, v(o, Column.LENIENT_TRUNCATED));
		assertEquals(1, v(o, Column.LENIENT_EMPTY));
		assertEquals(1, v(o, Column.LENIENT_DROPPED));
		assertEquals(2, v(o, Column.OUT_OF_RANGE));
		assertEquals("the dropped value's WARN (CR-065 D3)", 1, v(o, Column.WARNINGS));
	}

	@Test
	public void alternateContentBranches() throws Exception {
		WorkerMain.Outcome o = survey(Fixtures.docx(dir, "mc.docx", Fixtures.MC_BODY));
		assertEquals("ok", v(o, Column.OUTCOME));
		assertEquals(2, v(o, Column.MC_ELEMENTS));
		assertEquals("the block-level one, in word mode", 1, v(o, Column.MC_RESOLVED));
		assertEquals("Fallback=1 w14=1", v(o, Column.MC_CHOICES));
	}

	@Test
	public void preprocessedPartCounted() throws Exception {
		// an element of an ignorable namespace where JAXB expects none: JAXB refuses the part, and
		// mc-preprocessor.xslt drops the element
		WorkerMain.Outcome o = survey(Fixtures.docx(dir, "preprocessed.docx", Fixtures.GOOD + "<w14:unknownToDocx4j/>"));
		assertEquals("ok", v(o, Column.OUTCOME));
		assertEquals(1, v(o, Column.MAIN_OK));
		assertEquals(1, v(o, Column.PARTS_PREPROCESSED));
	}

	@Test
	public void labels() {
		assertEquals("none", DocumentProbe.label(null));
	}
}
