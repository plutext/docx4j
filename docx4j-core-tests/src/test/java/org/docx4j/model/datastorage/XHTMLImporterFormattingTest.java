package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4jProperties;
import org.docx4j.convert.in.xhtml.FormattingOption;
import org.junit.After;
import org.junit.Test;

/**
 * CR-013 phase 1: the docx4j.properties FormattingOption keys reach the importer's
 * setters by reflection, and bad input is skipped rather than fatal.  Uses a fake
 * importer and the test-only FormattingOption stand-in (ImportXHTML itself is not on
 * this classpath).
 */
public class XHTMLImporterFormattingTest {

	/** Has the three setters ImportXHTML's XHTMLImporterImpl has. */
	public static class FakeImporter {
		public FormattingOption run, paragraph, table;
		public void setRunFormatting(FormattingOption o) { run = o; }
		public void setParagraphFormatting(FormattingOption o) { paragraph = o; }
		public void setTableFormatting(FormattingOption o) { table = o; }
	}

	/** An older importer lacking the table setter. */
	public static class FakeImporterWithoutTables {
		public FormattingOption run, paragraph;
		public void setRunFormatting(FormattingOption o) { run = o; }
		public void setParagraphFormatting(FormattingOption o) { paragraph = o; }
	}

	@After
	public void clearProperties() {
		Docx4jProperties.getProperties().remove(XHTMLImporterFormatting.RUN);
		Docx4jProperties.getProperties().remove(XHTMLImporterFormatting.PARAGRAPH);
		Docx4jProperties.getProperties().remove(XHTMLImporterFormatting.TABLE);
	}

	@Test
	public void unsetKeysLeaveImporterAlone() {
		FakeImporter importer = new FakeImporter();
		assertEquals(0, XHTMLImporterFormatting.apply(importer, FakeImporter.class));
		assertNull(importer.run);
		assertNull(importer.paragraph);
		assertNull(importer.table);
		assertFalse(XHTMLImporterFormatting.isClassToStyleOnly(XHTMLImporterFormatting.RUN));
	}

	@Test
	public void blankKeyCountsAsUnset() {
		Docx4jProperties.setProperty(XHTMLImporterFormatting.RUN, "  ");
		FakeImporter importer = new FakeImporter();
		assertEquals(0, XHTMLImporterFormatting.apply(importer, FakeImporter.class));
		assertNull(importer.run);
	}

	@Test
	public void allThreeKeysApplied() {
		Docx4jProperties.setProperty(XHTMLImporterFormatting.RUN, "CLASS_TO_STYLE_ONLY");
		Docx4jProperties.setProperty(XHTMLImporterFormatting.PARAGRAPH, " CLASS_PLUS_OTHER ");
		Docx4jProperties.setProperty(XHTMLImporterFormatting.TABLE, "IGNORE_CLASS");
		FakeImporter importer = new FakeImporter();
		assertEquals(3, XHTMLImporterFormatting.apply(importer, FakeImporter.class));
		assertEquals(FormattingOption.CLASS_TO_STYLE_ONLY, importer.run);
		assertEquals(FormattingOption.CLASS_PLUS_OTHER, importer.paragraph);
		assertEquals(FormattingOption.IGNORE_CLASS, importer.table);
		assertTrue(XHTMLImporterFormatting.isClassToStyleOnly(XHTMLImporterFormatting.RUN));
		assertFalse(XHTMLImporterFormatting.isClassToStyleOnly(XHTMLImporterFormatting.PARAGRAPH));
	}

	@Test
	public void unknownValueSkippedOthersApplied() {
		Docx4jProperties.setProperty(XHTMLImporterFormatting.RUN, "STYLES_ONLY_PLEASE");
		Docx4jProperties.setProperty(XHTMLImporterFormatting.TABLE, "CLASS_TO_STYLE_ONLY");
		FakeImporter importer = new FakeImporter();
		assertEquals(1, XHTMLImporterFormatting.apply(importer, FakeImporter.class));
		assertNull(importer.run);
		assertEquals(FormattingOption.CLASS_TO_STYLE_ONLY, importer.table);
	}

	@Test
	public void missingSetterSkipped() {
		Docx4jProperties.setProperty(XHTMLImporterFormatting.RUN, "IGNORE_CLASS");
		Docx4jProperties.setProperty(XHTMLImporterFormatting.TABLE, "CLASS_TO_STYLE_ONLY");
		FakeImporterWithoutTables importer = new FakeImporterWithoutTables();
		assertEquals(1, XHTMLImporterFormatting.apply(importer, FakeImporterWithoutTables.class));
		assertEquals(FormattingOption.IGNORE_CLASS, importer.run);
	}

	@Test
	public void customizerHookRoundTrips() {
		XHTMLImporterCustomizer c = (importer, sdtPr, inTc) -> {};
		try {
			BindingHandler.setXHTMLImporterCustomizer(c);
			assertEquals(c, BindingHandler.getXHTMLImporterCustomizer());
		} finally {
			BindingHandler.setXHTMLImporterCustomizer(null);
		}
		assertNull(BindingHandler.getXHTMLImporterCustomizer());
	}
}
