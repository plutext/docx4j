package org.docx4j.model.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;

/**
 * docx4j deletes the image files it wrote into the directory it chose itself,
 * and never touches files written to a directory the caller named.
 *
 * <p>A conversion which needs image files writes one per picture per run; with no
 * imageDirPath they go to java.io.tmpdir with a UUID in the name.  Nothing used to
 * delete them, so a process doing conversions filled its temp directory (367,757
 * files, in the case which prompted this).</p>
 *
 * <p>What can be asserted here is the handler's contract and the HTML pathway.
 * That PDF output leaves nothing behind is asserted in docx4j-export-fo
 * (TemporaryImageCleanupPdfTest); this module doesn't have that module on its
 * classpath.</p>
 *
 * @since 17.1.1
 */
public class TemporaryImageCleanupTest {

	/** <uuid>image1.png and the like */
	private static final Pattern UUID_NAMED = Pattern.compile(
			"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}.+");

	private static File tmpDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	private static Set<String> uuidNamedFilesInTmpDir() {
		Set<String> ret = new HashSet<String>();
		String[] names = tmpDir().list();
		if (names != null) {
			for (String name : names) {
				if (UUID_NAMED.matcher(name).matches()) ret.add(name);
			}
		}
		return ret;
	}

	// ---------------------------------------------------------------- the handler

	/** Exposes the protected write, and where it wrote. */
	private static class TestHandler extends FileConversionImageHandler {

		TestHandler(String imageDirPath) {
			super(imageDirPath, true);
		}

		File store(BinaryPart part, byte[] bytes) throws Docx4JException {
			return new File(imageDirPath, createStoredImage(part, bytes));
		}
	}

	@Test
	public void deletesWhatItWroteWhereItChoseTheDirectory() throws Exception {

		TestHandler handler = new TestHandler(null); // no imageDirPath: docx4j's choice
		File image = handler.store(imagePart(), imageBytes());

		assertTrue("written", image.exists());
		assertEquals("written to java.io.tmpdir",
				tmpDir().getCanonicalFile(), image.getParentFile().getCanonicalFile());
		assertTrue("named with a uuid", UUID_NAMED.matcher(image.getName()).matches());

		handler.cleanupTemporaryImages(false);

		assertFalse("deleted", image.exists());
	}

	@Test
	public void neverDeletesFilesInTheCallersDirectory() throws Exception {

		File dir = Files.createTempDirectory("docx4j-imagedir").toFile();
		try {
			TestHandler handler = new TestHandler(dir.getAbsolutePath());
			File image = handler.store(imagePart(), imageBytes());
			assertTrue("written", image.exists());

			handler.cleanupTemporaryImages(false);

			assertTrue("the caller's output, so left alone", image.exists());
		} finally {
			delete(dir);
		}
	}

	/**
	 * Deferred: the output still points at the files (HTML's img/@src, an fo
	 * document's file: URL), so they wait for JVM exit rather than going now.
	 */
	@Test
	public void deferredCleanupLeavesTheFileForTheOutputToPointAt() throws Exception {

		TestHandler handler = new TestHandler(null);
		File image = handler.store(imagePart(), imageBytes());
		try {
			handler.cleanupTemporaryImages(true);
			assertTrue("still there for the html/fo which references it", image.exists());
		} finally {
			image.delete();
		}
	}

	@Test
	public void propertyOptsOut() throws Exception {

		Docx4jProperties.setProperty(FileConversionImageHandler.DELETE_TEMPORARY_PROPERTY, "false");
		File image = null;
		try {
			TestHandler handler = new TestHandler(null);
			image = handler.store(imagePart(), imageBytes());

			handler.cleanupTemporaryImages(false);

			assertTrue("kept, since the property says so", image.exists());
		} finally {
			Docx4jProperties.setProperty(FileConversionImageHandler.DELETE_TEMPORARY_PROPERTY, "true");
			if (image != null) image.delete();
		}
	}

	// ------------------------------------------------------------ end to end, HTML

	@Test
	public void htmlToCallersImageDirWritesNothingToTheTempDirectory() throws Exception {

		Set<String> before = uuidNamedFilesInTmpDir();
		File dir = Files.createTempDirectory("docx4j-imagedir").toFile();
		try {
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(packageWithImage());
			settings.setImageDirPath(dir.getAbsolutePath());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);

			File[] written = dir.listFiles();
			assertNotNull(written);
			assertEquals("the image is in the directory the caller named", 1, written.length);
			assertTrue(written[0].exists());

			assertEquals("nothing left in java.io.tmpdir", before, uuidNamedFilesInTmpDir());
		} finally {
			delete(dir);
		}
	}

	/**
	 * With no imageDirPath the image goes to java.io.tmpdir - and must still be
	 * there when the export returns, since the HTML we handed back names it in an
	 * img/@src.  (It is registered for deletion on JVM exit, which is as much as
	 * docx4j can do without knowing when the caller is finished with the HTML.)
	 */
	@Test
	public void htmlKeepsTheImageItsSrcPointsAt() throws Exception {

		Set<String> before = uuidNamedFilesInTmpDir();

		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(packageWithImage());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
		String html = baos.toString("UTF-8");

		Set<String> after = uuidNamedFilesInTmpDir();
		after.removeAll(before);
		assertEquals("one image written to java.io.tmpdir", 1, after.size());

		String name = after.iterator().next();
		try {
			assertTrue("the html points at it", html.contains(name));
			assertTrue("so it is still there", new File(tmpDir(), name).exists());
		} finally {
			new File(tmpDir(), name).delete();
		}
	}

	// ------------------------------------------------------------------- fixtures

	private static byte[] imageBytes() throws Exception {
		try (InputStream is = ResourceUtils.getResource("images/greentick.png");
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buf = new byte[4096];
			int read;
			while ((read = is.read(buf)) > 0) {
				baos.write(buf, 0, read);
			}
			return baos.toByteArray();
		}
	}

	private static BinaryPart imagePart() throws Exception {
		return BinaryPartAbstractImage.createImagePart(
				WordprocessingMLPackage.createPackage(), imageBytes());
	}

	private static WordprocessingMLPackage packageWithImage() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage imagePart =
				BinaryPartAbstractImage.createImagePart(pkg, imageBytes());

		ObjectFactory factory = Context.getWmlObjectFactory();
		P p = factory.createP();
		R run = factory.createR();
		p.getContent().add(run);
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(
				imagePart.createImageInline("greentick", "a green tick", 1, 2, false));

		pkg.getMainDocumentPart().addObject(p);
		return pkg;
	}

	private static void delete(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) f.delete();
		}
		dir.delete();
	}
}
