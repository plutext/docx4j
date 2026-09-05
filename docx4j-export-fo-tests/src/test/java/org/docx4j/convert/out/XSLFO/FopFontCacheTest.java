package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * FOP's font cache (~/.fop/fop-fonts.cache) is a serialized object graph shared by every
 * FOP on the machine and written without locking.  A stale or half-written one makes
 * FontCache.loadFrom throw a ClassCastException, which FOP does not catch, so every
 * export failed until someone deleted the file.  docx4j lists its fonts explicitly, so
 * it has no use for the cache: the factories it builds don't read it, and neither does
 * the first pass of a two-pass render with a factory supplied by the caller.
 *
 * @since 17.0.5
 */
public class FopFontCacheTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** NUMPAGES makes the render two-pass, which is where the cache was still read */
	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t xml:space=\"preserve\">page 1 of </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" NUMPAGES \"><w:r><w:t>1</w:t></w:r></w:fldSimple></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	@Test
	public void factoriesDocx4jBuildsDontUseTheFontCache() throws Exception {
		FOSettings settings = Docx4J.createFOSettings();
		settings.setOpcPackage(pkg()); // setOpcPackage, so there is a fop config to build from
		FopFactoryBuilder builder = FORendererApacheFOP.getFopFactoryBuilder(settings);
		assertNull("docx4j's FopFactory should not read or write FOP's font cache",
				builder.getFontManager().getFontCache());
	}

	@Test
	public void aCorruptFontCacheDoesNotBreakTheExport() throws Exception {

		File cache = File.createTempFile("fop-fonts-corrupt", ".cache");
		cache.deleteOnExit();
		OutputStream os = new FileOutputStream(cache);
		os.write("this is not a serialized FontCache".getBytes("UTF-8"));
		os.close();

		// a factory as a caller might supply one: FOP's own defaults, so the cache is on
		FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
		builder.getFontManager().setCacheFile(cache.toURI());
		FopFactory fopFactory = builder.build();

		FOSettings settings = Docx4J.createFOSettings();
		settings.setOpcPackage(pkg());
		settings.getSettings().put(FORendererApacheFOP.FOP_FACTORY, fopFactory);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		assertTrue("a PDF should have been produced", baos.size() > 0);
	}
}
