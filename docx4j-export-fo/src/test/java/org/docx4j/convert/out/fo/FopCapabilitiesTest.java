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
 * classpath: the docx4j FO renderer by default (from 17.3.0), Apache FOP 2.11 under -Papache-fop.
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

	/**
	 * Apache FOP's jars ahead of the docx4j FO renderer's: the marker is found but Apache's layout
	 * managers run (the customer case of 21 October 2026: a page-anchored table's wrap and the measured
	 * footer extent ignored, the body flowing through the boxes). The hooks must then be treated as
	 * absent, with a warning that names both.
	 */
	@Test
	public void apacheClassesAheadOfTheRendererDisableItsHooks() {
		FopCapabilities honest = FopCapabilities.probe(FopCapabilities.class.getClassLoader());
		if (!honest.isDocx4jRenderer()) return;   // Apache FOP alone: nothing to shadow
		FopCapabilities mixed = FopCapabilities.probe(FopCapabilities.class.getClassLoader(), "2.11");
		assertFalse(mixed.describe(), mixed.isDocx4jRenderer());
		assertTrue(mixed.describe(), mixed.getCapabilities().isEmpty());
		assertEquals("2.11", mixed.getVersion());
		assertTrue(mixed.getWarnings().toString(), mixed.getWarnings().toString().contains("Apache FOP 2.11")
				&& mixed.getWarnings().toString().contains(honest.getVersion()));
		// the same version as the marker's is the renderer, hooks and all
		FopCapabilities same = FopCapabilities.probe(FopCapabilities.class.getClassLoader(), honest.getVersion());
		assertTrue(same.isDocx4jRenderer());
		assertEquals(honest.getCapabilities(), same.getCapabilities());
		// a version FOP cannot report (no jar manifest) is no evidence either way
		assertTrue(FopCapabilities.probe(FopCapabilities.class.getClassLoader(), "unknown").isDocx4jRenderer());
	}

	@Test
	public void hooksFollowTheRenderer() {
		FopCapabilities c = FopCapabilities.get();
		if (c.isDocx4jRenderer()) {
			// phase 1's four hooks are in every release; a later one (gsub-features,
			// lookup-fallback, kerning-flag and shared-glyph-tounicode, all from 2.11-docx4j.2)
			// is there exactly when the renderer declares it
			java.util.EnumSet<FopCapabilities.Capability> phase1 = java.util.EnumSet.of(
					FopCapabilities.Capability.PAIR_TABLE, FopCapabilities.Capability.LEADER_PLACEMENT,
					FopCapabilities.Capability.INLINE_ACCESS, FopCapabilities.Capability.GLYF_EMPTY_GLYPH);
			for (FopCapabilities.Capability cap : FopCapabilities.Capability.values()) {
				if (phase1.contains(cap)) {
					assertTrue(cap.key(), FopCapabilities.has(cap));
				} else {
					assertEquals(cap.key(), c.getCapabilities().contains(cap.key()), FopCapabilities.has(cap));
				}
			}
		} else {
			assertTrue(c.getCapabilities().toString(), c.getCapabilities().isEmpty());
			for (FopCapabilities.Capability cap : FopCapabilities.Capability.values()) {
				assertFalse(cap.key(), FopCapabilities.has(cap));
			}
		}
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
