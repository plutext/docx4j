package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.images.DataUriConversionImageHandler;
import org.docx4j.model.images.MetafileSvgProvider;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafilePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;
import org.junit.Test;

/**
 * What a metafile becomes with an SVG generator available: inline {@code <svg>} in
 * HTML, and a working {@code MetafilePart.toSVG()}.
 *
 * <p>Batik's {@code SVGGraphics2D} is a dependency of docx4j-export-fo, and
 * {@link org.docx4j.convert.out.fo.BatikMetafileSvgProvider} is found through
 * {@link java.util.ServiceLoader} - so this is the module where the SVG paths can be
 * exercised.  docx4j-core-tests pins the core-only behaviour (a PNG) for the same
 * inputs.  See CR-011.</p>
 *
 * @since 17.0.6
 */
public class MetafileHtmlSvgTest {

	private static final int[] PATHWAYS = {
			Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSLT" : "visitor") + ": ";
	}

	private static WordprocessingMLPackage sample(String name) throws Exception {
		try (InputStream is = MetafileHtmlSvgTest.class.getResourceAsStream("/metafiles/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	private static byte[] metafile(String name) throws Exception {
		try (InputStream is = MetafileHtmlSvgTest.class.getResourceAsStream("/metafiles/" + name)) {
			return org.apache.commons.io.IOUtils.toByteArray(is);
		}
	}

	private static String toHtml(WordprocessingMLPackage pkg, int flags,
			org.docx4j.model.images.ConversionImageHandler handler) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		if (handler != null) {
			settings.setImageHandler(handler);
		} else {
			java.io.File dir = new java.io.File(System.getProperty("java.io.tmpdir"),
					"docx4j-metafile-svg-test");
			dir.mkdirs();
			settings.setImageDirPath(dir.getAbsolutePath());
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	@Test
	public void theProviderIsFoundOnTheClasspath() {
		assertNotNull("docx4j-export-fo should supply a MetafileSvgProvider through ServiceLoader",
				MetafileSvgProvider.getProvider());
	}

	/**
	 * A handler which embeds images in the document gets the picture as vectors: an
	 * inline {@code <svg>}, rather than a rasterised PNG data URI.
	 */
	@Test
	public void anEmbeddingHandlerGetsInlineSvg() throws Exception {
		for (String docx : new String[] {"EMF.docx", "WMF.docx"}) {
			for (int flags : PATHWAYS) {
				String impl = pathway(flags) + docx + ": ";
				String html = toHtml(sample(docx), flags, new DataUriConversionImageHandler());

				assertTrue(impl + "the metafile should be inline SVG", html.contains("<svg"));
				assertTrue(impl + "the metafile's own bytes should not be embedded",
						!html.contains("data:image/x-emf") && !html.contains("data:image/x-wmf"));
				assertTrue(impl + "the SVG should be sized in points",
						html.contains("width=\"") && html.contains("pt\""));

				/* Batik references an embedded bitmap as xlink:href, and the prefix has to
				 * be bound or the output is not well-formed XHTML.  Xalan drops the SVG's
				 * own declaration when the XSLT pathway copies it in, so the HTML root
				 * carries one too (as fo:root does). */
				if (html.contains("xlink:href")) {
					assertTrue(impl + "xlink:href is used but the prefix is never bound",
							html.contains("xmlns:xlink"));
				}
			}
		}
	}

	/** A handler which writes files still gets a PNG: it has a file to write. */
	@Test
	public void aFileHandlerStillGetsAPng() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			String html = toHtml(sample("EMF.docx"), flags, null);
			assertTrue(impl + "a file-writing handler should get a PNG, not inline SVG",
					!html.contains("<svg") && html.contains(".png"));
		}
	}

	// ------------------------------------------------------------ the parts' API

	private static MetafilePart part(String name) throws Exception {
		MetafilePart p = name.endsWith(".wmf")
				? new MetafileWmfPart(new PartName("/word/media/image1.wmf"))
				: new MetafileEmfPart(new PartName("/word/media/image1.emf"));
		p.setBinaryData(metafile(name));
		return p;
	}

	/**
	 * {@code MetafileWmfPart.toSVG()} keeps the signature it had when it was
	 * implemented with wmf2svg (retired in 17.0.6), and {@code MetafileEmfPart} - which
	 * could convert nothing at all before - now has it too.
	 */
	@Test
	public void toSvgWorksForBothPartTypes() throws Exception {
		for (String name : new String[] {
				"star_picture_save_as.wmf", "60677.wmf", "file-45.wmf",
				"pptx.emf", "SimpleEMF_windows.emf", "gradient.emf", "vector_image.emf"}) {
			MetafileWmfPart.SvgDocument svg = part(name).toSVG();
			org.w3c.dom.Document doc = svg.getDomDocument();
			assertNotNull(name + ": no SVG", doc);
			assertTrue(name + ": the root should be an svg element in the SVG namespace",
					"svg".equals(doc.getDocumentElement().getLocalName())
					&& "http://www.w3.org/2000/svg".equals(doc.getDocumentElement().getNamespaceURI()));
			assertTrue(name + ": the SVG should state its size",
					doc.getDocumentElement().getAttribute("width").endsWith("pt"));
		}
	}

	/** A metafile that cannot be parsed says so, rather than returning empty SVG. */
	@Test
	public void toSvgReportsAMetafileItCannotDraw() throws Exception {
		String name = "clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-6060921738035200.wmf";
		try {
			part(name).toSVG();
			fail("expected toSVG() to report that the metafile could not be rendered");
		} catch (org.docx4j.openpackaging.exceptions.Docx4JException e) {
			assertTrue("unhelpful message: " + e.getMessage(),
					e.getMessage() != null && e.getMessage().contains("SVG"));
		}
	}
}
