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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtensiblePart;
import org.docx4j.utils.ResourceUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * word/commentsExtensible.xml, which before 17.2.0 had no schema in {@code xsd/}
 * and so loaded as a generic XML part: its bytes were preserved, but nothing
 * could read a comment's {@code durableId} or {@code dateUtc} through the model,
 * and the comment reactions part ([MS-OREACTXML]) anchors on those.
 *
 * <p>{@code loadAndSave.docx} (Word 365, 2026) carries the part, with one
 * {@code commentExtensible}.</p>
 *
 * @since 17.2.0
 */
public class CommentsExtensiblePartTest {

	private static final PartName PART_NAME;
	static {
		try {
			PART_NAME = new PartName("/word/commentsExtensible.xml");
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static WordprocessingMLPackage pkg;
	private static byte[] saved;

	@BeforeClass
	public static void loadAndSave() throws Exception {

		pkg = WordprocessingMLPackage.load(ResourceUtils.getResource("loadAndSave.docx"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		saved = baos.toByteArray();
	}

	private static String savedPart(String entryName) throws Exception {

		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(saved));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.getName().equals(entryName)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				int n;
				while ((n = zis.read(buf)) > 0) {
					baos.write(buf, 0, n);
				}
				return new String(baos.toByteArray(), StandardCharsets.UTF_8);
			}
		}
		throw new AssertionError(entryName + " not in the saved docx");
	}

	/** The content type now maps to a typed part, not the generic DefaultPart. */
	@Test
	public void partIsTyped() throws Exception {

		Part part = pkg.getParts().get(PART_NAME);
		assertNotNull("commentsExtensible.xml missing from the test document", part);
		assertTrue("loaded as " + part.getClass().getName(),
				part instanceof CommentsExtensiblePart);
	}

	/** And it is reachable from the main document part, by relationship type. */
	@Test
	public void reachableFromTheMainDocumentPart() throws Exception {

		CommentsExtensiblePart cep = pkg.getMainDocumentPart().getCommentsExtensiblePart();
		assertNotNull("not wired up by relationship type", cep);
		assertEquals(PART_NAME, cep.getPartName());
	}

	/** durableId and dateUtc read through the model. */
	@Test
	public void commentExtensibleUnmarshalled() throws Exception {

		CommentsExtensiblePart cep = (CommentsExtensiblePart) pkg.getParts().get(PART_NAME);
		org.docx4j.w16cex.CTCommentsExtensible ce = cep.getJaxbElement();

		assertEquals(1, ce.getCommentExtensible().size());
		org.docx4j.w16cex.CTCommentExtensible one = ce.getCommentExtensible().get(0);
		assertEquals("05546856", one.getDurableId());
		assertNotNull("dateUtc not unmarshalled", one.getDateUtc());
		assertEquals("2026-05-18T23:21:00Z", one.getDateUtc().toXMLFormat());
	}

	/** mc:Ignorable reaches the model too, cr: (the reactions namespace) included. */
	@Test
	public void ignorableUnmarshalled() throws Exception {

		CommentsExtensiblePart cep = (CommentsExtensiblePart) pkg.getParts().get(PART_NAME);
		String ignorable = cep.getJaxbElement().getIgnorable();
		assertNotNull("mc:Ignorable not unmarshalled", ignorable);
		assertTrue(ignorable, ignorable.contains("w16cex"));
		assertTrue(ignorable, ignorable.contains("cr"));
	}

	/** A save writes the part back through JAXB, keeping what Word put there. */
	@Test
	public void roundTrips() throws Exception {

		String xml = savedPart("word/commentsExtensible.xml");

		int start = xml.indexOf("<", xml.indexOf("?>") + 2);
		String root = xml.substring(start, xml.indexOf(">", start) + 1);
		assertTrue("not written as w16cex:commentsExtensible: " + root,
				root.startsWith("<w16cex:commentsExtensible"));
		assertTrue("mc:Ignorable lost: " + root, root.contains("mc:Ignorable="));

		assertTrue("durableId lost: " + xml, xml.contains("durableId=\"05546856\""));
		assertTrue("dateUtc lost: " + xml, xml.contains("dateUtc=\"2026-05-18T23:21:00Z\""));
	}

	/** The saved part loads again as the typed part, with the same values. */
	@Test
	public void reloadsFromTheSavedDocx() throws Exception {

		WordprocessingMLPackage reloaded = WordprocessingMLPackage.load(
				new ByteArrayInputStream(saved));

		CommentsExtensiblePart cep = (CommentsExtensiblePart) reloaded.getParts().get(PART_NAME);
		assertNotNull(cep);
		assertEquals(1, cep.getJaxbElement().getCommentExtensible().size());
		assertEquals("05546856",
				cep.getJaxbElement().getCommentExtensible().get(0).getDurableId());
	}
}
