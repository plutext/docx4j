package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.InputStream;

import org.docx4j.model.images.MetafileSvgProvider;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.w3c.dom.Document;

/**
 * A WMF picture part.
 *
 * <p>docx4j draws WMF (and EMF, and EMF+) itself, in pure Java, by replaying the
 * recorded GDI calls onto a {@code Graphics2D}: see
 * {@link org.docx4j.model.images.MetafileRenderer} and the repackaged Apache POI
 * HWMF/HEMF code in {@code org.docx4j.org.apache.poi.hwmf}.  Conversion to SVG
 * ({@link #toSVG()}) and to a raster image ({@link #toPNG(double)}) is inherited
 * from {@link MetafilePart}; the pictures in a document are converted for you by
 * the PDF (via XSL-FO) and HTML exporters.</p>
 *
 * <p>Until 17.0.6 this class used the wmf2svg library, which has no EMF support and
 * is no longer a dependency (CR-011).  The signature of {@link #toSVG()} is
 * unchanged, but it now needs {@code docx4j-export-fo} on the classpath, for
 * Batik's SVG generator - see {@link MetafilePart#toSVG()}.</p>
 */
public class MetafileWmfPart extends MetafilePart {


	public MetafileWmfPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public MetafileWmfPart(ExternalTarget externalTarget) {
		super(externalTarget);
		init();
	}

	public void init() {
		// Used if this Part is added to [Content_Types].xml
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType(
				org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_WMF));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.IMAGE);
	}


	/**
	 * A rendered SVG document.
	 *
	 * <p>Kept as a named type, rather than plain {@link Document}, because that is what
	 * {@code MetafileWmfPart.toSVG()} has returned since 2010.</p>
	 */
	public static class SvgDocument {

		Document doc = null;
		public Document getDomDocument() {
			return doc;
		}

		/** @since 17.0.6 */
		public SvgDocument(Document doc) {
			this.doc = doc;
		}

		/**
		 * Renders a metafile stream (WMF, EMF or EMF+) to SVG.
		 *
		 * @throws Exception if the stream cannot be read or drawn, or there is no
		 *         {@link MetafileSvgProvider} on the classpath (add docx4j-export-fo)
		 */
		public SvgDocument(InputStream metafileStream) throws Exception {

			byte[] data = org.apache.commons.io.IOUtils.toByteArray(metafileStream);
			if (MetafileSvgProvider.getProvider() == null) {
				throw new IllegalStateException("No MetafileSvgProvider on the classpath;"
						+ " add docx4j-export-fo (for Batik's SVGGraphics2D).");
			}
			doc = MetafileSvgProvider.toSvg(data, 0, 0);
			if (doc == null) {
				throw new IllegalStateException("Could not render the metafile as SVG");
			}
		}

	}


}
