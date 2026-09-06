package org.docx4j.openpackaging.parts.WordprocessingML;

import java.awt.image.BufferedImage;

import org.docx4j.model.images.MetafileRenderer;
import org.docx4j.model.images.MetafileSvgProvider;
import org.docx4j.model.images.PoiMetafileRenderer;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.PartName;
import org.w3c.dom.Document;

/**
 * A Windows metafile picture part: WMF ({@link MetafileWmfPart}) or EMF / EMF+
 * ({@link MetafileEmfPart}).
 *
 * <p>Since 17.0.6 both can be converted, by replaying the recorded GDI calls onto
 * a {@code Graphics2D} - see {@link org.docx4j.model.images.MetafileRenderer} and
 * CR-011.  {@link #toSVG()} keeps the picture as vectors; {@link #toPNG(double)}
 * rasterises it.</p>
 */
public abstract class MetafilePart extends BinaryPartAbstractImage {

	public MetafilePart(PartName partName) throws InvalidFormatException {
		super(partName);

		// Can't setContentType or setRelationshipType, since
		// these will differ depending on the nature of the data.
		// Common binary parts should extend this class to
		// provide that information.

	}

	public MetafilePart(ExternalTarget externalTarget) {
		super(externalTarget);
	}

	/**
	 * This picture as SVG, at its own size.
	 *
	 * <p>Producing SVG needs a recording {@code Graphics2D}, which docx4j takes from
	 * Batik - a dependency of <b>docx4j-export-fo</b>.  With docx4j-core alone there
	 * is no {@link MetafileSvgProvider} on the classpath and this throws; use
	 * {@link #toPNG(double)}, which needs nothing beyond the JDK.</p>
	 *
	 * <p>The return type is {@link MetafileWmfPart.SvgDocument} for both subclasses,
	 * because that is what {@code MetafileWmfPart.toSVG()} returned before 17.0.6
	 * (when it was implemented with wmf2svg, now retired) and callers compile
	 * unchanged.  Its {@code getDomDocument()} is the SVG.</p>
	 *
	 * @throws Docx4JException if there is no SVG provider, or the metafile cannot be
	 *         drawn
	 * @since 17.0.6 on this class (previously WMF only)
	 */
	public MetafileWmfPart.SvgDocument toSVG() throws Docx4JException {

		byte[] data = getBytes();
		if (MetafileSvgProvider.getProvider() == null) {
			throw new Docx4JException("No MetafileSvgProvider on the classpath;"
					+ " add docx4j-export-fo (for Batik's SVGGraphics2D), or use toPNG(dpi).");
		}
		Document svg = MetafileSvgProvider.toSvg(data, 0, 0);
		if (svg == null) {
			throw new Docx4JException("Could not render " + getPartName() + " as SVG");
		}
		return new MetafileWmfPart.SvgDocument(svg);
	}

	/**
	 * This picture rasterised at the given resolution, at its own size.
	 *
	 * @param dpi pixels per inch; 96 is the Windows screen resolution metafiles are
	 *            usually authored at
	 * @throws Docx4JException if the metafile cannot be drawn
	 * @since 17.0.6
	 */
	public BufferedImage toPNG(double dpi) throws Docx4JException {

		MetafileRenderer renderer = PoiMetafileRenderer.getInstance();
		BufferedImage img = renderer.toImage(getBytes(), dpi);
		if (img == null) {
			throw new Docx4JException("Could not render " + getPartName() + " as an image");
		}
		return img;
	}

	/**
	 * This picture as the bytes of a PNG file, at the given resolution.
	 *
	 * @throws Docx4JException if the metafile cannot be drawn
	 * @since 17.0.6
	 */
	public byte[] toPNGBytes(double dpi) throws Docx4JException {

		byte[] png = PoiMetafileRenderer.getInstance().toPngBytes(getBytes(), dpi);
		if (png == null) {
			throw new Docx4JException("Could not render " + getPartName() + " as a PNG");
		}
		return png;
	}

}
