package org.docx4j.fidelity.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The generated conversion scripts: both field modes come from one text, the review-markup
 * display is turned off in both unless asked to be left alone, and the field update is the
 * only thing that separates them.  What Word does with the script needs Word; what the
 * script says does not, and is what a golden set's manifest vouches for.
 */
public class ConversionScriptTest {

	private static final String HIDE = "ActiveWindow.View.ShowRevisionsAndComments = False";
	private static final String UPDATE = "story.Fields.Update";

	@Test
	public void markupIsHiddenInBothFieldModesByDefault() {
		for (boolean update : new boolean[] { false, true }) {
			String s = ConversionScript.script(update, false);
			assertTrue(s, s.contains(HIDE));
			assertTrue(s, s.contains("RevisionsFilter.Markup = WdRevisionsMarkupNone"));
			assertTrue(s, s.contains("wordDocument.PrintRevisions = False"));
			assertEquals("the field update is what separates the two modes", update, s.contains(UPDATE));
		}
	}

	@Test
	public void showMarkupLeavesTheDisplayAlone() {
		for (boolean update : new boolean[] { false, true }) {
			String s = ConversionScript.script(update, true);
			assertFalse(s, s.contains(HIDE));
			assertFalse(s, s.contains("PrintRevisions"));
			assertEquals(update, s.contains(UPDATE));
		}
	}

	/** The markup is hidden after the open and before the update and the conversion. */
	@Test
	public void markupIsHiddenBeforeTheFieldUpdateAndTheConversion() {
		String s = ConversionScript.script(true, false);
		int open = s.indexOf("Documents.Open(");
		int hide = s.indexOf(HIDE);
		int update = s.indexOf(UPDATE);
		int save = s.indexOf("wordDocument.SaveAs");
		assertTrue(open > 0 && open < hide && hide < update && update < save);
	}

	/** It is documents4j's own script otherwise: the same exit codes, the same SaveAs. */
	@Test
	public void itIsStillDocuments4jsScript() {
		String s = ConversionScript.script(false, false);
		for (String line : new String[] {
				"WScript.Quit -6", "WScript.Quit -2", "WScript.Quit -3", "WScript.Quit -4", "WScript.Quit 2",
				"wordDocument.SaveAs outputFile, formatEnumeration",
				"Call ConvertFile( WScript.Arguments.Unnamed.Item(0)" }) {
			assertTrue(line, s.contains(line));
		}
		assertTrue("cscript wants CRLF", s.contains("\r\n"));
		assertFalse("a bare LF would be a mixed-ending script", s.replace("\r\n", "").contains("\n"));
	}
}
