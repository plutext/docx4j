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
package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.docx4j.Docx4jProperties;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * A font read for its metrics and never drawn in.
 *
 * <p>The test copies a face this machine already has into a directory of its own, so that the
 * two things which matter can be asserted separately: that a width comes back from the copy,
 * and that {@link PhysicalFonts} is not touched by the reading.</p>
 *
 * @since 17.1.1 (CR-001 batch 49 item 2)
 */
public class MetricsOnlyFontsTest {

	/** any face this machine has; the mechanism is what is under test, not the font */
	private static final String FAMILY = "Liberation Serif";

	private File dir;
	private String was;
	private PhysicalFont source;

	@Before
	public void setUp() throws Exception {
		// physical font discovery happens in the Mapper's static initialiser
		org.docx4j.openpackaging.packages.WordprocessingMLPackage.createPackage().getFontMapper();
		was = Docx4jProperties.getProperty(MetricsOnlyFonts.DIRS);
		dir = Files.createTempDirectory("docx4j-metrics-only").toFile();
		/* A face this machine already has, copied into a directory of its own: the point is
		 * the mechanism, not which font it is, and taking the file from PhysicalFonts keeps
		 * the test independent of which font jars are on the test classpath. */
		source = PhysicalFonts.get(FAMILY);
		Assume.assumeNotNull(source);
		java.net.URI uri = source.getEmbeddedURI();
		Assume.assumeNotNull(uri);
		// the file may live in a docx4j font jar, so copy the bytes rather than the file
		String name = uri.toString();
		name = name.substring(name.lastIndexOf('/') + 1);
		Assume.assumeTrue("not a font file: " + uri, name.toLowerCase().endsWith(".ttf"));
		try (java.io.InputStream in = uri.toURL().openStream()) {
			Files.copy(in, new File(dir, name).toPath());
		}
		MetricsOnlyFonts.reset();
	}

	@After
	public void tearDown() {
		Docx4jProperties.setProperty(MetricsOnlyFonts.DIRS, was == null ? "" : was);
		MetricsOnlyFonts.reset();
		File[] fs = dir.listFiles();
		if (fs != null) for (File f : fs) f.delete();
		dir.delete();
	}

	/** With no directory named, nothing is read: the feature is off by default. */
	@Test
	public void offByDefault() {
		Docx4jProperties.setProperty(MetricsOnlyFonts.DIRS, "");
		assertNull(MetricsOnlyFonts.get(FAMILY));
		assertEquals(-1, MetricsOnlyFonts.widthPt("x", FAMILY, 11), 0.0001);
	}

	/** Named, the face is found by its family name and measures like the real thing. */
	@Test
	public void aNamedDirectoryIsReadForMetrics() {
		Docx4jProperties.setProperty(MetricsOnlyFonts.DIRS, dir.getAbsolutePath());

		PhysicalFont pf = MetricsOnlyFonts.get(FAMILY);
		assertNotNull(FAMILY + " was not read from " + dir, pf);

		// the ten digits at 11pt, read out of the copied file
		double w = MetricsOnlyFonts.widthPt("0123456789", FAMILY, 11);
		assertTrue("implausible width " + w, w > 40 && w < 70);
		assertEquals("the same as measuring the face directly",
				TextMeasurer.widthPt("0123456789", pf, 11), w, 0.0001);

		assertNull("a face the directory has not got", MetricsOnlyFonts.get("No Such Face"));
	}

	/** And the reading does not register the face: nothing is drawn in it. */
	@Test
	public void theFaceIsNotRegisteredWithPhysicalFonts() {
		PhysicalFont before = PhysicalFonts.get(FAMILY);
		Docx4jProperties.setProperty(MetricsOnlyFonts.DIRS, dir.getAbsolutePath());
		assertNotNull(MetricsOnlyFonts.get(FAMILY));
		assertEquals("PhysicalFonts' answer for " + FAMILY + " changed", before, PhysicalFonts.get(FAMILY));
		assertTrue("the metrics-only face was registered with PhysicalFonts",
				before == null || MetricsOnlyFonts.get(FAMILY) != PhysicalFonts.get(FAMILY));
	}

	/** A directory that is not one is a warning, not a failure. */
	@Test
	public void aMissingDirectoryIsSurvived() {
		Docx4jProperties.setProperty(MetricsOnlyFonts.DIRS, new File(dir, "nope").getAbsolutePath());
		assertNull(MetricsOnlyFonts.get(FAMILY));
	}
}
