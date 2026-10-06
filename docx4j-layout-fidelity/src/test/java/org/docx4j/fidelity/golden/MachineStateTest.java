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
		System.clearProperty(MachineState.DRIVER_PROPERTY);
		System.clearProperty(MachineState.PAGE_PROPERTY);
		System.clearProperty(MachineState.WORD_PROPERTY);
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

	/** The default printer's line follows the other two (17.3.1). */
	@Test
	public void thePrinterLineComesThird() {
		List<String> lines = MachineState.manifestLines("on", "lcid 1033", "Microsoft Print to PDF, 600x600 dpi (Win32_Printer)");
		assertEquals(3, lines.size());
		assertEquals("printer=Microsoft Print to PDF, 600x600 dpi (Win32_Printer)", lines.get(2));
		assertEquals("printer=unknown", MachineState.manifestLines("on", "x", null).get(2));
	}

	/** An operator who knows the printer can state it. */
	@Test
	public void aStatedPrinterWins() {
		System.setProperty(MachineState.PRINTER_PROPERTY, "FX DocuPrint C2255 PCL 6, 300x300 dpi");
		try {
			assertEquals("FX DocuPrint C2255 PCL 6, 300x300 dpi (stated by the operator)", MachineState.defaultPrinter());
		} finally {
			System.clearProperty(MachineState.PRINTER_PROPERTY);
		}
	}

	/** The driver, the page and Word follow the printer, in that order (17.3.1). */
	@Test
	public void theDriverPageAndWordLinesFollowThePrinter() {
		List<String> lines = MachineState.manifestLines("on", "lcid 1033", "P, 600x600 dpi", "D", null, "W");
		assertEquals(6, lines.size());
		assertEquals("printer=P, 600x600 dpi", lines.get(2));
		assertEquals("printerDriver=D", lines.get(3));
		assertEquals("printerPage=unknown", lines.get(4));
		assertEquals("word=W", lines.get(5));
	}

	/** Each of the three can be stated, and the statement wins. */
	@Test
	public void theDriverPageAndWordCanBeStated() {
		System.setProperty(MachineState.DRIVER_PROPERTY, "HP OfficeJet Pro 9010 series, version 49.4.4561.21100");
		System.setProperty(MachineState.PAGE_PROPERTY, "A4, hard margins 12pt");
		System.setProperty(MachineState.WORD_PROPERTY, "16.0.19231.20194");
		assertEquals("HP OfficeJet Pro 9010 series, version 49.4.4561.21100 (stated by the operator)", MachineState.printerDriver());
		assertEquals("A4, hard margins 12pt (stated by the operator)", MachineState.printerPage());
		assertEquals("16.0.19231.20194 (stated by the operator)", MachineState.word());
	}

	/** Get-PrinterDriver's line: the version decoded from its UInt64, the date, the maker, the model. */
	@Test
	public void aDriverLineReadsAsItsNameVersionDateMakerAndModel() {
		assertEquals("HP OfficeJet Pro 9010 series, version 49.4.4561.21100, dated 2021-03-04, by HP, v3 driver (Get-PrinterDriver)",
				MachineState.parseDriver("DRIVER|HP OfficeJet Pro 9010 series|49.4.4561.21100|2021-03-04|HP|3"));
		assertEquals("Microsoft Print To PDF, v4 driver (Get-PrinterDriver)",
				MachineState.parseDriver("DRIVER|Microsoft Print To PDF|0.0.0.0|||4"));
		assertEquals(null, MachineState.parseDriver("DRIVER|||||"));
		assertEquals(null, MachineState.parseDriver(null));
	}

	/** System.Drawing's page, in hundredths of an inch, as the paper and four hard margins in
	 *  points: what lies outside the printable area on each side. */
	@Test
	public void aPageLineReadsAsThePaperAndItsFourHardMargins() {
		// A4 827 x 1169, printable from (16.67, 68.33), 793.33 x 1084: margins 12, 49.2, 12.2, 12
		assertEquals("A4 595.4 x 841.7pt, hard margins left 12.0 top 49.2 right 12.2 bottom 12.0 pt (System.Drawing.Printing)",
				MachineState.parsePage("PAGE|HP OfficeJet Pro 9010 series|A4|827|1169|16.67|68.33|793.33|1084|False"));
		// landscape: the paper's sides swap, the printable area is already turned
		assertEquals("Letter 792.0 x 612.0pt landscape, hard margins left 0.0 top 0.0 right 0.0 bottom 0.0 pt (System.Drawing.Printing)",
				MachineState.parsePage("PAGE|Microsoft Print to PDF|Letter|850|1100|0|0|1100|850|True"));
		assertEquals(null, MachineState.parsePage("PAGE|x|A4|wide|1169|0|0|0|0|False"));
		assertEquals(null, MachineState.parsePage(null));
	}

	/** Click-to-Run's version and channel, named where Microsoft documents the GUID, and
	 *  winword.exe's file version; an MSI Word has only the second. */
	@Test
	public void aWordLineReadsAsTheBuildChannelAndFileVersion() {
		assertEquals("16.0.19231.20194 (Click-to-Run, Current Channel 492350f6-3a01-4f97-b9c0-c7c6ddf67d60, x64);"
				+ " winword.exe 16.0.19231.20194",
				MachineState.parseWord("WORD|16.0.19231.20194|16.0.19231.20194|"
						+ "http://officecdn.microsoft.com/pr/492350F6-3A01-4F97-B9C0-C7C6DDF67D60||x64|"
						+ "C:\\Program Files\\Microsoft Office\\Root\\Office16\\WINWORD.EXE"));
		assertEquals("16.0.19231.20194 (Click-to-Run, channel 0000-unknown); winword.exe 16.0.19231.20194",
				MachineState.parseWord("WORD|16.0.19231.20194|16.0.19231.20194||http://officecdn.microsoft.com/pr/0000-unknown||"));
		assertEquals("winword.exe 16.0.5422.1000 (C:\\Office\\WINWORD.EXE)",
				MachineState.parseWord("WORD|16.0.5422.1000|||||C:\\Office\\WINWORD.EXE"));
		assertEquals(null, MachineState.parseWord("WORD||||||"));
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
