package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;

/**
 * The image files a conversion writes to java.io.tmpdir (which is where they go
 * when the caller names no imageDirPath) are docx4j's to delete, and it does.
 *
 * <p>For PDF they can go as soon as the export returns: FOP rendered the FO here,
 * in process, and the image bytes are in the PDF.  For an fo document
 * (INTERNAL_FO_MIME) they cannot: what the caller was handed points at them by
 * {@code file:} URL, so they wait for JVM exit instead.</p>
 *
 * @since 17.1.1
 */
public class TemporaryImageCleanupPdfTest {

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

	@Test
	public void pdfLeavesNoImageFilesBehind() throws Exception {

		Set<String> before = uuidNamedFilesInTmpDir();

		ByteArrayOutputStream pdf = new ByteArrayOutputStream();
		Docx4J.toPDF(packageWithImage(), pdf);

		assertTrue("a pdf was produced", pdf.size() > 0);

		Set<String> after = uuidNamedFilesInTmpDir();
		after.removeAll(before);
		assertEquals("files left in java.io.tmpdir: " + after, 0, after.size());
	}

	/** Also on the exception path: the FO renderer never gets to run. */
	@Test
	public void aFailedPdfLeavesNoImageFilesBehind() throws Exception {

		Set<String> before = uuidNamedFilesInTmpDir();

		FOSettings settings = new FOSettings(packageWithImage());
		settings.setApacheFopMime("application/pdf");
		settings.setCustomFoRenderer(new org.docx4j.convert.out.FORenderer() {
			@Override
			public void render(String foDocument, FOSettings settings, boolean twoPass,
					java.util.List<SectionPageInformation> pageNumberInformation,
					java.io.OutputStream outputStream) throws org.docx4j.openpackaging.exceptions.Docx4JException {
				throw new org.docx4j.openpackaging.exceptions.Docx4JException("as if FOP fell over");
			}
		});

		try {
			Docx4J.toFO(settings, new ByteArrayOutputStream(), Docx4J.FLAG_NONE);
			throw new AssertionError("expected the export to fail");
		} catch (org.docx4j.openpackaging.exceptions.Docx4JException expected) {
			// that's the point
		}

		Set<String> after = uuidNamedFilesInTmpDir();
		after.removeAll(before);
		assertEquals("files left in java.io.tmpdir: " + after, 0, after.size());
	}

	@Test
	public void foOutputKeepsTheFilesItPointsAt() throws Exception {

		Set<String> before = uuidNamedFilesInTmpDir();

		FOSettings settings = new FOSettings(packageWithImage());
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		String fo = baos.toString("UTF-8");

		Set<String> after = uuidNamedFilesInTmpDir();
		after.removeAll(before);
		assertEquals("one image written to java.io.tmpdir", 1, after.size());

		String name = after.iterator().next();
		File image = new File(tmpDir(), name);
		try {
			assertTrue("the fo points at it", fo.contains(name));
			assertTrue("so it must still be there", image.exists());
		} finally {
			// docx4j registered it with File.deleteOnExit(); we're not exiting
			assertTrue("tidy up", image.delete());
			assertFalse(image.exists());
		}
	}

	// ------------------------------------------------------------------- fixtures

	/** A 1x1 png, so that the document has an image part to write out. */
	private static final String PNG_1x1 =
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGA"
			+ "hKmMIQAAAABJRU5ErkJggg==";

	private static WordprocessingMLPackage packageWithImage() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(
				pkg, Base64.getDecoder().decode(PNG_1x1));

		ObjectFactory factory = Context.getWmlObjectFactory();
		P p = factory.createP();
		R run = factory.createR();
		p.getContent().add(run);
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(
				imagePart.createImageInline("dot", "a dot", 1, 2, false));

		pkg.getMainDocumentPart().addObject(p);
		return pkg;
	}
}
