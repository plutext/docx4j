/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The FO renderer probe (CR-020 phase 0), on whichever renderer the build put on the
 * classpath: Apache FOP 2.11 by default, the docx4j FO renderer under -Pfo-renderer-fork.
 * The assertions hold on both; the ones that depend on which it is check that the probe
 * agrees with the classpath.
 */
public class FopCapabilitiesTest {

	private static boolean markerOnClasspath() {
		try {
			Class.forName(FopCapabilities.MARKER_CLASS);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Test
	public void probeAgreesWithTheClasspath() {
		FopCapabilities c = FopCapabilities.get();
		assertNotNull(c);
		assertEquals(markerOnClasspath(), c.isDocx4jRenderer());
		assertSame("probed once", c, FopCapabilities.get());
	}

	@Test
	public void describesTheRenderer() {
		FopCapabilities c = FopCapabilities.get();
		assertTrue(c.describe(), c.describe().startsWith("FO renderer: "));
		if (c.isDocx4jRenderer()) {
			assertTrue(c.describe(), c.describe().contains("docx4j-fo-renderer "));
			assertTrue(c.getVersion(), c.getVersion().startsWith("2.11-docx4j."));
		} else {
			assertTrue(c.describe(), c.describe().contains("Apache FOP "));
		}
	}

	@Test
	public void theLineIsTheOneBuiltFor() {
		FopCapabilities c = FopCapabilities.get();
		// from a jar the version is known; from an IDE's classes directory it may not be
		if (!"unknown".equals(c.getLine())) {
			assertEquals(FopCapabilities.BUILT_FOR_LINE, c.getLine());
		}
		assertTrue("one FOP on this classpath: " + c.getWarnings(), c.getWarnings().isEmpty());
	}

	@Test
	public void noHooksYet() {
		// phase 0: neither renderer publishes a hook; phase 1 changes this on the fork
		assertTrue(FopCapabilities.get().getCapabilities().toString(),
				FopCapabilities.get().getCapabilities().isEmpty());
		assertFalse(FopCapabilities.Capability.values().length > 0);
	}

	@Test
	public void lineOfAVersionString() {
		assertEquals("2.11", FopCapabilities.lineOf("2.11"));
		assertEquals("2.11", FopCapabilities.lineOf("2.11-docx4j.1"));
		assertEquals("2.11", FopCapabilities.lineOf("2.11-docx4j.1-SNAPSHOT"));
		assertEquals("2.12", FopCapabilities.lineOf("2.12.0-SNAPSHOT"));
		assertEquals("unknown", FopCapabilities.lineOf("SVN"));
		assertEquals("unknown", FopCapabilities.lineOf(null));
	}
}
