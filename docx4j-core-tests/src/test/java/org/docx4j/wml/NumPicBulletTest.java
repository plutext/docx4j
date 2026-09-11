package org.docx4j.wml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * Regression test for https://github.com/plutext/docx4j/issues/623 (and #618).
 *
 * Recent Word writes a picture bullet as
 *
 *   w:numPicBullet / mc:AlternateContent / (mc:Choice Requires="v" w:pict | mc:Fallback w:drawing)
 *
 * The mc preprocessor keeps the mc:Fallback, so w:drawing is what reaches JAXB.
 * Before NumPicBullet gained a drawing property, that was dropped and the document
 * saved with an empty w:numPicBullet, which Word refuses to open.
 *
 * Fixtures are the reporter's minimal reproduction documents (four one-letter list
 * items with a stock-image bullet; no author metadata).
 */
public class NumPicBulletTest {

	private static final String WORD365 = "numPicBullet-word365-AlternateContent.docx";
	private static final String WORD2019 = "numPicBullet-word2019-pict.docx";

	@Test
	public void word365DrawingSurvivesRoundTrip() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(ResourceUtils.getResource(WORD365));
		Numbering.NumPicBullet bullet = onlyBullet(pkg);
		assertNotNull("mc:Fallback w:drawing should be unmarshalled", bullet.getDrawing());
		assertNull("the mc:Choice w:pict is not selected", bullet.getPict());

		// save, reload, and the drawing must still be there
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		WordprocessingMLPackage reloaded = WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
		Numbering.NumPicBullet again = onlyBullet(reloaded);
		assertNotNull("w:drawing lost on round trip", again.getDrawing());

		// and the marshalled element is not the empty <w:numPicBullet/> that corrupted the docx
		String xml = XmlUtils.marshaltoString(again, true, true);
		assertTrue(xml, xml.contains("<w:drawing"));
		assertFalse(xml, xml.contains("<w:numPicBullet w:numPicBulletId=\"0\"/>"));
	}

	@Test
	public void word2019PictStillWorks() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(ResourceUtils.getResource(WORD2019));
		Numbering.NumPicBullet bullet = onlyBullet(pkg);
		assertNotNull(bullet.getPict());
		assertNull(bullet.getDrawing());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		WordprocessingMLPackage reloaded = WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
		assertNotNull(onlyBullet(reloaded).getPict());
	}

	private static Numbering.NumPicBullet onlyBullet(WordprocessingMLPackage pkg) throws Exception {
		Numbering numbering = pkg.getMainDocumentPart().getNumberingDefinitionsPart().getJaxbElement();
		assertEquals(1, numbering.getNumPicBullet().size());
		return numbering.getNumPicBullet().get(0);
	}
}
