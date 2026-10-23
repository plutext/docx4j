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
package org.docx4j.fonts.fop.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.file.Files;

import org.docx4j.Docx4jProperties;
import org.junit.After;
import org.junit.Test;

/**
 * The font cache's location and its failure mode. Measured on 17.3.3 (2026-10-23): a cache file that
 * could not be created threw from the font mapper's static initialiser, so the first PDF conversion
 * failed with ExceptionInInitializerError and every later one with NoClassDefFoundError; and
 * {@code docx4j.fonts.fontcache}, listed in the reference docx4j.properties since 17.0.0, was read only
 * as a JVM system property.
 */
public class FontCacheLocationTest {

	private static final String KEY = "docx4j.fonts.fontcache";

	@After
	public void restore() {
		System.clearProperty(KEY);
		Docx4jProperties.getProperties().remove(KEY);
	}

	@Test
	public void anUnwritableCacheFileIsNotAnError() throws Exception {
		File dir = Files.createTempDirectory("docx4j-fontcache").toFile();
		dir.deleteOnExit();
		File unwritable = new File(dir, "missing/parent/docx4j-fonts.cache");
		FontCache cache = new FontCache();
		cache.registerFailedFont("file:/nowhere/x.ttf", 1L); // the cache changed, so a save writes
		assertTrue(cache.hasChanged());
		cache.saveTo(unwritable); // threw FOPException until 17.3.5
		assertFalse("nothing written", unwritable.exists());
		assertTrue("kept for a later save at a writable location", cache.hasChanged());

		File writable = new File(dir, "docx4j-fonts.cache");
		cache.saveTo(writable);
		assertTrue(writable.exists());
		assertFalse(cache.hasChanged());
	}

	@Test
	public void theLocationIsReadFromDocx4jProperties() throws Exception {
		assumeTrue("no system property set for this JVM", System.getProperty(KEY) == null);
		File dir = Files.createTempDirectory("docx4j-fontcache").toFile();
		dir.deleteOnExit();
		Docx4jProperties.setProperty(KEY, dir.getAbsolutePath());
		assertEquals(new File(dir, "docx4j-fonts.cache"), FontCache.getDefaultCacheFile(false));
	}

	@Test
	public void theSystemPropertyWins() throws Exception {
		File fromProperties = Files.createTempDirectory("docx4j-fontcache-p").toFile();
		File fromSystem = Files.createTempDirectory("docx4j-fontcache-s").toFile();
		fromProperties.deleteOnExit();
		fromSystem.deleteOnExit();
		Docx4jProperties.setProperty(KEY, fromProperties.getAbsolutePath());
		System.setProperty(KEY, fromSystem.getAbsolutePath());
		assertEquals(new File(fromSystem, "docx4j-fonts.cache"), FontCache.getDefaultCacheFile(false));
	}
}
