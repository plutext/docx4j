package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * An EMF (or EMF+) picture part.
 *
 * <p>Since 17.0.6 docx4j draws EMF and EMF+ itself, in pure Java, by replaying the
 * recorded GDI calls onto a {@code Graphics2D}: see
 * {@link org.docx4j.model.images.MetafileRenderer} and the repackaged Apache POI
 * HEMF code in {@code org.docx4j.org.apache.poi.hemf}.  Conversion to SVG
 * ({@link MetafilePart#toSVG()}) and to a raster image
 * ({@link MetafilePart#toPNG(double)}) is inherited; the pictures in a document are
 * converted for you by the PDF (via XSL-FO) and HTML exporters.</p>
 *
 * <p>Coverage is bounded by what the copied POI code implements: every EMF record
 * type that matters, and most of EMF+.  Office's "dual" EMF+ files (which carry EMF
 * fallback records) render; a few EMF+-only constructs written by GDI+ / .NET
 * applications - ellipses, arcs, curves, {@code DrawString} text, container
 * transforms - do not yet.  The package README in
 * {@code org/docx4j/org/apache/poi/hwmf} lists them; CR-011 phase 4 closes the gap.</p>
 *
 * <p>Historical note.  Until 17.0.6 this class could not convert anything at all, and
 * carried a survey of the options as at February 2010 (com.adobe.dp.office, wmf2svg,
 * batik, freehep, imagemagick, openoffice), concluding that OpenOffice was then the
 * only workable EMF converter.  That is no longer so.</p>
 */
public class MetafileEmfPart extends MetafilePart {

	public MetafileEmfPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public MetafileEmfPart(ExternalTarget externalTarget) {
		super(externalTarget);
		init();
	}

	public void init() {
		// Used if this Part is added to [Content_Types].xml
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType(
				org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_EMF));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.IMAGE);
	}

}
