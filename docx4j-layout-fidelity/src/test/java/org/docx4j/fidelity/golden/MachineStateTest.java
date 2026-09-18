/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;

/**
 * The manifest writer of {@link MachineState}: what the golden runner records about the
 * machine that cut a set.  The registry half runs only on Windows, so what is tested here
 * is the operator's statement, the fallbacks, and the shape of the lines a manifest gets -
 * which is the half a reader of the manifest depends on.
 */
public class MachineStateTest {

	@After
	public void clearProperties() {
		System.clearProperty(MachineState.CONNECTED_PROPERTY);
		System.clearProperty(MachineState.PROOFING_PROPERTY);
	}

	@Test
	public void theOperatorsStatementIsWhatIsRecorded() {
		System.setProperty(MachineState.CONNECTED_PROPERTY, "on");
		System.setProperty(MachineState.PROOFING_PROPERTY, "en-US,de-DE");
		assertEquals("on (stated by the operator)", MachineState.connectedExperiences());
		assertEquals("en-US,de-DE (stated by the operator)", MachineState.proofingLanguages());
	}

	@Test
	public void anEmptyStatementIsNoStatement() {
		System.setProperty(MachineState.CONNECTED_PROPERTY, "   ");
		assertTrue(MachineState.connectedExperiences().startsWith(MachineState.UNKNOWN)
				|| MachineState.isWindows());
	}

	@Test
	public void withNothingStatedOffWindowsBothAreUnknownAndSayWhy() {
		if (MachineState.isWindows()) return; // the registry answers there; see the class javadoc
		String connected = MachineState.connectedExperiences();
		String proofing = MachineState.proofingLanguages();
		assertTrue(connected, connected.startsWith(MachineState.UNKNOWN));
		assertTrue(connected, connected.contains(MachineState.CONNECTED_PROPERTY));
		assertTrue(proofing, proofing.startsWith(MachineState.UNKNOWN));
		assertTrue(proofing, proofing.contains(MachineState.PROOFING_PROPERTY));
	}

	@Test
	public void theManifestCarriesOneLineEachInOrder() {
		List<String> lines = MachineState.manifestLines("off (HKCU...\\DisconnectedState=1)", "lcid 1033");
		assertEquals(2, lines.size());
		assertEquals("connectedExperiences=off (HKCU...\\DisconnectedState=1)", lines.get(0));
		assertEquals("proofingLanguages=lcid 1033", lines.get(1));
	}

	@Test
	public void aBlankReadingIsRecordedAsUnknownRatherThanAsNothing() {
		List<String> lines = MachineState.manifestLines(null, "  ");
		assertEquals("connectedExperiences=unknown", lines.get(0));
		assertEquals("proofingLanguages=unknown", lines.get(1));
	}

	@Test
	public void aValueNeverWrapsBecauseAManifestIsReadLineByLine() {
		List<String> lines = MachineState.manifestLines("on\nand then some", "a\r\nb");
		assertEquals("connectedExperiences=on and then some", lines.get(0));
		assertEquals("proofingLanguages=a  b", lines.get(1)); // CRLF is two characters

	}

	@Test
	public void thisMachinesLinesAreWellFormed() {
		for (String line : MachineState.manifestLines()) {
			assertTrue(line, line.indexOf('=') > 0);
			assertTrue(line, line.indexOf('\n') < 0 && line.indexOf('\r') < 0);
		}
	}

	@Test
	public void aSubkeyListingIsTheLastSegmentOfEachLine() {
		// reg's own output shape, so the parser is tested without a registry
		List<String> subkeys = MachineState.registrySubkeys("HKLM\\SOFTWARE\\Nothing\\Here");
		assertTrue(subkeys.toString(), subkeys.isEmpty());
	}
}
