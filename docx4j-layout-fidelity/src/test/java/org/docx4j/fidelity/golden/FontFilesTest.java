/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link FontFiles}: the files behind a set's faces, found by PostScript name and recorded by
 * hash.  The folders are named here, so it runs off Windows; the font is the OFL Akasia this
 * module depends on (docx4j-export-fo-fonts-theme2023).
 */
public class FontFilesTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File akasia(File dir, String name) throws Exception {
		File out = new File(dir, name);
		try (InputStream in = FontFilesTest.class.getResourceAsStream("/fonts/Akasia/Akasia-Regular.ttf")) {
			Files.copy(in, out.toPath());
		}
		return out;
	}

	private static String sha256(File f) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (byte b : MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(f.toPath()))) sb.append(String.format("%02x", b));
		return sb.toString();
	}

	@Test
	public void aFaceIsItsFileByPostScriptNameAndHash() throws Exception {
		File dir = tmp.newFolder("Fonts");
		File font = akasia(dir, "akasia.ttf");
		List<String> lines = FontFiles.manifestLines(Arrays.asList("Akasia-Regular", "NotInstalled"), Collections.singletonList(dir));
		assertEquals(2, lines.size());
		assertEquals("fontFile.Akasia-Regular=sha256:" + sha256(font) + ", " + font.length() + " bytes, " + font.getPath(), lines.get(0));
		assertTrue(lines.get(1), lines.get(1).startsWith("fontFile.NotInstalled=not found in ["));
	}

	/** Two files carrying the name - a per-user copy beside the system's - are both listed:
	 *  which one Word took cannot be known from here. */
	@Test
	public void everyFileCarryingTheNameIsListed() throws Exception {
		File system = tmp.newFolder("system");
		File user = tmp.newFolder("user");
		File a = akasia(system, "a.ttf");
		File b = akasia(user, "b.ttf");
		List<String> lines = FontFiles.manifestLines(Collections.singletonList("Akasia-Regular"), Arrays.asList(system, user));
		assertEquals(1, lines.size());
		assertTrue(lines.get(0), lines.get(0).contains(a.getPath() + "; sha256:"));
		assertTrue(lines.get(0), lines.get(0).endsWith(b.getPath()));
	}

	/** A face with Word's simulated style is its family's file, said so. */
	@Test
	public void aSimulatedStyleIsTheFamilysFile() throws Exception {
		File dir = tmp.newFolder("Fonts");
		akasia(dir, "akasia.ttf");
		String line = FontFiles.manifestLines(Collections.singletonList("Akasia-Regular,Bold"), Collections.singletonList(dir)).get(0);
		assertTrue(line, line.startsWith("fontFile.Akasia-Regular,Bold=sha256:"));
		assertTrue(line, line.endsWith(" (Word's simulated Bold of Akasia-Regular)"));
	}

	/** Nothing to search is said, not failed; no faces, no lines. */
	@Test
	public void noFoldersSaySoAndNoFacesWriteNothing() throws Exception {
		List<String> lines = FontFiles.manifestLines(Collections.singletonList("X"), Collections.singletonList(new File(tmp.getRoot(), "absent")));
		assertEquals(1, lines.size());
		assertTrue(lines.get(0), lines.get(0).startsWith("fontFiles=unknown (none of "));
		assertTrue(FontFiles.manifestLines(Collections.<String>emptyList(), Collections.singletonList(tmp.getRoot())).isEmpty());
	}

	/** A file which is not a font, or not one FontBox reads, is passed over. */
	@Test
	public void aFileThatIsNotAFontIsPassedOver() throws Exception {
		File dir = tmp.newFolder("Fonts");
		Files.write(new File(dir, "broken.ttf").toPath(), new byte[] { 1, 2, 3, 4, 5 });
		assertTrue(FontFiles.postScriptNames(new File(dir, "broken.ttf")).isEmpty());
		assertTrue(FontFiles.index(Collections.singletonList(dir)).isEmpty());
	}
}
