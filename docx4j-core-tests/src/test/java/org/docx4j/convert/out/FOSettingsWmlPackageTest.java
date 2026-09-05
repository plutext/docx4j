package org.docx4j.convert.out;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * setWmlPackage builds the fop configuration, as setOpcPackage does: without it
 * PDF output failed with a NullPointerException in ConfiguredPDFDocumentHandler,
 * which is a long way from the call that caused it.
 *
 * @since 17.0.5
 */
public class FOSettingsWmlPackageTest {

	@SuppressWarnings("deprecation")
	@Test
	public void setWmlPackageBuildsTheFopConfig() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("Hello");

		FOSettings settings = new FOSettings();
		settings.setWmlPackage(pkg);

		assertEquals(pkg, settings.getOpcPackage());
		assertNotNull("no fop configuration, so PDF output would fail", settings.getFopConfig());
	}

	@Test
	public void setOpcPackageStillDoes() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("Hello");

		FOSettings settings = new FOSettings();
		settings.setOpcPackage(pkg);
		assertNotNull(settings.getFopConfig());
	}
}
