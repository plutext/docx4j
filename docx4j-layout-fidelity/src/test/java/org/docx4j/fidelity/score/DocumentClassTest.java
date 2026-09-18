/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.docx4j.fonts.FontReport;
import org.junit.Test;

/**
 * The two scoreboard columns of {@link DocumentClass}: the class a document can be
 * evidence for, and the share of its text the class rests on.  No package, no mapper and
 * no PDF: the classification is tested over the families and the counts.
 */
public class DocumentClassTest {

	private static DocumentClass.Used used(String family, long runs, FontReport.Grade grade) {
		return new DocumentClass.Used(family, runs, grade);
	}

	private static List<DocumentClass.Used> list(DocumentClass.Used... u) {
		return new ArrayList<DocumentClass.Used>(Arrays.asList(u));
	}

	// ------------------------------------------------------------------ the four classes

	@Test
	public void wordHadThemAllAndSoDidWe() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Calibri", 90, FontReport.Grade.EXACT), used("Arial", 10, FontReport.Grade.EXACT)),
				100, Arrays.asList("Calibri", "Arial-BoldMT"));
		assertEquals(DocumentClass.CLASS_2, r.getDocClass());
		assertEquals(0.0, r.getShare(), 1e-9);
		assertTrue(r.getSubstituted().isEmpty());
		assertTrue(r.getWordMissing().isEmpty());
	}

	@Test
	public void aNearMetricCloneHereIsClass2n() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Calibri", 75, FontReport.Grade.EXACT), used("Cambria", 25, FontReport.Grade.NEAR)),
				100, Arrays.asList("Calibri", "Cambria"));
		assertEquals(DocumentClass.CLASS_2N, r.getDocClass());
		assertEquals(0.25, r.getShare(), 1e-9);
		assertEquals("NEAR", r.getSubstituted().get("Cambria"));
	}

	@Test
	public void aClassFaceHereIsClass3a() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Calibri", 90, FontReport.Grade.EXACT), used("Consolas", 10, FontReport.Grade.CLASS)),
				100, Arrays.asList("Calibri", "Consolas"));
		assertEquals(DocumentClass.CLASS_3A, r.getDocClass());
		assertEquals(0.10, r.getShare(), 1e-9);
	}

	@Test
	public void nothingMappedItIsAlsoClass3a() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("M_Times", 40, FontReport.Grade.NONE)), 40, Arrays.asList("M_Times"));
		assertEquals(DocumentClass.CLASS_3A, r.getDocClass());
		assertEquals(1.0, r.getShare(), 1e-9);
	}

	@Test
	public void aHardSubstitutionBeatsANearOne() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Cambria", 50, FontReport.Grade.NEAR), used("Symbol", 1, FontReport.Grade.CLASS)),
				100, Arrays.asList("Cambria", "Symbol"));
		assertEquals(DocumentClass.CLASS_3A, r.getDocClass());
		// the share is the substituted text, not the hard part of it
		assertEquals(0.51, r.getShare(), 1e-9);
	}

	@Test
	public void wordsOwnSubstitutionIsClass3bAndWinsOverEverything() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Lato", 80, FontReport.Grade.CLASS), used("Calibri", 20, FontReport.Grade.EXACT)),
				100, Arrays.asList("Calibri", "Ebrima"));
		assertEquals(DocumentClass.CLASS_3B, r.getDocClass());
		assertEquals(Long.valueOf(80), r.getWordMissing().get("Lato"));
		assertEquals(0.80, r.getShare(), 1e-9);
	}

	@Test
	public void theShareIsTheWorseSide() {
		// Word missed one family of 80 runs; docx4j substituted another of 10
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Lato", 80, FontReport.Grade.EXACT), used("Cambria", 10, FontReport.Grade.NEAR),
						used("Calibri", 10, FontReport.Grade.EXACT)),
				100, Arrays.asList("Calibri", "Cambria"));
		assertEquals(DocumentClass.CLASS_3B, r.getDocClass());
		assertEquals(0.80, r.getShare(), 1e-9);
	}

	@Test
	public void aRunSettingTwoFamiliesCannotPushTheShareAboveOne() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Lato", 10, FontReport.Grade.EXACT), used("Nyala", 10, FontReport.Grade.EXACT)),
				10, Collections.<String>emptyList());
		assertEquals(1.0, r.getShare(), 1e-9);
	}

	@Test
	public void aFamilyNoTextIsSetInDoesNotClassifyAnything() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Calibri", 10, FontReport.Grade.EXACT), used("Wingdings", 0, FontReport.Grade.CLASS)),
				10, Arrays.asList("Calibri"));
		assertEquals(DocumentClass.CLASS_2, r.getDocClass());
	}

	@Test
	public void aDocumentWithNoRunsAtAllIsClass2WithNoShare() {
		DocumentClass.Reading r = DocumentClass.of(new ArrayList<DocumentClass.Used>(), 0,
				Collections.<String>emptyList());
		assertEquals(DocumentClass.CLASS_2, r.getDocClass());
		assertEquals(0.0, r.getShare(), 1e-9);
	}

	// ------------------------------------------------------------------ name matching

	@Test
	public void aFaceNameIsTheFamilyWithTheStyleWeldedOn() {
		List<String> faces = DocumentClass.normaliseFaces(
				Arrays.asList("TimesNewRomanPSMT", "TimesNewRomanPS-BoldMT", "Arial-BoldItalicMT"));
		assertTrue(DocumentClass.wordEmbedded("Times New Roman", faces));
		assertTrue(DocumentClass.wordEmbedded("Arial", faces));
		assertFalse(DocumentClass.wordEmbedded("Calibri", faces));
	}

	@Test
	public void theSubsetTagIsAlreadyOffAndACommaIsCut() {
		List<String> faces = DocumentClass.normaliseFaces(Arrays.asList("TrebuchetMS,Bold"));
		assertTrue(DocumentClass.wordEmbedded("Trebuchet MS", faces));
	}

	@Test
	public void theLegacyCodePageSuffixIsDropped() {
		List<String> faces = DocumentClass.normaliseFaces(Arrays.asList("TimesNewRomanPSMT"));
		assertTrue(DocumentClass.wordEmbedded("Times New Roman CYR", faces));
		assertTrue(DocumentClass.wordEmbedded("Times New Roman Baltic", faces));
	}

	@Test
	public void aFamilyWithNoLettersOrDigitsIsNotLookedFor() {
		assertTrue(DocumentClass.wordEmbedded("...", Collections.<String>emptyList()));
		assertTrue(DocumentClass.wordEmbedded(null, Collections.<String>emptyList()));
	}

	@Test
	public void aPdfWhichEmbedsNothingMissesEveryFamily() {
		assertFalse(DocumentClass.wordEmbedded("Calibri", Collections.<String>emptyList()));
	}

	@Test
	public void normalisationKeepsLettersAndDigitsOnly() {
		assertEquals("helveticaneuelt55roman", DocumentClass.norm("HelveticaNeue LT 55 Roman"));
		assertEquals("", DocumentClass.norm("  -  "));
	}

	// ------------------------------------------------------------------ the classes.txt line

	@Test
	public void theClassLineNamesBothSides() {
		DocumentClass.Reading r = DocumentClass.of(
				list(used("Lato", 80, FontReport.Grade.CLASS), used("Cambria", 20, FontReport.Grade.NEAR)),
				100, Arrays.asList("Cambria"));
		String line = r.line("1234");
		assertTrue(line, line.contains("3b"));
		assertTrue(line, line.contains("wordMissing=Lato(80)"));
		assertTrue(line, line.contains("Lato:CLASS"));
		assertTrue(line, line.contains("Cambria:NEAR"));
	}
}
