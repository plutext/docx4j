/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.chart;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.images.WordXmlPictureE20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * The space a chart takes in the FO and HTML outputs, until docx4j draws charts: an empty box
 * of the drawing's extent ({@code wp:extent}), placed as a picture of that size would be, inline
 * in its line or, for a {@code wp:anchor}, floating through the same anchor hints a picture
 * gets.  Until 17.3.3 every output dropped a chart without keeping its space, so the text after
 * it moved up into its place and the pages broke where Word's don't (CR-035 phase 0).
 *
 * <p>In FO the box is an {@code fo:instream-foreign-object} holding an empty SVG, sized as a
 * picture's {@code fo:instream-foreign-object} is, with the chart's name as its
 * {@code fox:alt-text}; the drawing, when docx4j can make one, goes into the same element.  In
 * HTML it is an empty inline {@code <svg>}, sized as a picture's {@code <img>} is (its points as
 * pixels), with the name as its {@code aria-label}.</p>
 *
 * <p>Used for a {@code c:chart} and, where a user prefers chartex's {@code mc:Choice}
 * ({@code docx4j.jaxb.mc.preferChoice}), a {@code cx:chart}.  By default a chartex chart's
 * {@code mc:Fallback} is what the outputs see, and that is drawn as written.</p>
 *
 * @since 17.3.3
 */
public final class ChartBox {

	private static final Logger log = LoggerFactory.getLogger(ChartBox.class);

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final String FOX_NS = "http://xmlgraphics.apache.org/fop/extensions";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	private static final double EMU_PER_PT = 12700;

	private final double widthPt;
	private final double heightPt;
	private final String altText;
	private final Anchor anchor;

	private ChartBox(double widthPt, double heightPt, String altText, Anchor anchor) {
		this.widthPt = widthPt;
		this.heightPt = heightPt;
		this.altText = altText;
		this.anchor = anchor;
	}

	/**
	 * The box of a chart's drawing.
	 *
	 * @param anchorOrInline the chart's {@code wp:inline} or {@code wp:anchor}, as JAXB, or as
	 *        the XSLT's node
	 * @return null where it is neither, or has no extent
	 */
	public static ChartBox of(Object anchorOrInline) {

		Object o = anchorOrInline instanceof NodeIterator ? unmarshal((NodeIterator) anchorOrInline) : anchorOrInline;
		CTPositiveSize2D extent;
		CTNonVisualDrawingProps docPr;
		Anchor anchor = null;
		if (o instanceof Inline) {
			extent = ((Inline) o).getExtent();
			docPr = ((Inline) o).getDocPr();
		} else if (o instanceof Anchor) {
			anchor = (Anchor) o;
			extent = anchor.getExtent();
			docPr = anchor.getDocPr();
		} else {
			log.warn("A chart's drawing is neither wp:inline nor wp:anchor: "
					+ (o == null ? "null" : o.getClass().getName()));
			return null;
		}
		if (extent == null || extent.getCx() <= 0 || extent.getCy() <= 0) {
			log.warn("A chart's drawing has no extent, so it takes no space");
			return null;
		}
		return new ChartBox(extent.getCx() / EMU_PER_PT, extent.getCy() / EMU_PER_PT, altText(docPr), anchor);
	}

	/** The drawing's {@code wp:docPr}: its description, else its name ("Chart 1"), else "Chart" */
	private static String altText(CTNonVisualDrawingProps docPr) {
		if (docPr != null) {
			if (docPr.getDescr() != null && !docPr.getDescr().trim().isEmpty()) return docPr.getDescr().trim();
			if (docPr.getName() != null && !docPr.getName().trim().isEmpty()) return docPr.getName().trim();
		}
		return "Chart";
	}

	private static Object unmarshal(NodeIterator it) {
		Node n = it.nextNode();
		if (n == null) return null;
		try {
			Unmarshaller u = Context.jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			return XmlUtils.unwrap(u.unmarshal(n));
		} catch (Exception e) {
			log.error("Couldn't read a chart's drawing: " + e.getMessage(), e);
			return null;
		}
	}

	/** @return the box's width, in points */
	public double getWidthPt() {
		return widthPt;
	}

	/** @return the box's height, in points */
	public double getHeightPt() {
		return heightPt;
	}

	/** @return the drawing's description, else its name, else "Chart" */
	public String getAltText() {
		return altText;
	}

	/** @return whether the chart floats ({@code wp:anchor}) */
	public boolean isAnchored() {
		return anchor != null;
	}

	/**
	 * The FO for a chart: an {@code fo:instream-foreign-object} of the drawing's extent holding
	 * an empty SVG, and for a {@code wp:anchor} the anchor hints export-fo's WordLayoutFixups
	 * positions it by.
	 *
	 * @return null where the drawing has no extent (nothing is written, as before 17.3.3)
	 */
	public static DocumentFragment createXslFo(AbstractWmlConversionContext context, Object anchorOrInline) {
		ChartBox box = of(anchorOrInline);
		return box == null ? null : box.toXslFo(context);
	}

	/** As {@link #createXslFo(AbstractWmlConversionContext, Object)}, for the XSLT */
	public static DocumentFragment createXslFo(AbstractWmlConversionContext context, NodeIterator wpInline) {
		return createXslFo(context, (Object) wpInline);
	}

	/**
	 * The property which, set true, follows a chart's box in HTML with its data as a table
	 * (CR-035 phase 3, D9): for readers without the picture.  False by default.
	 *
	 * @since 17.3.3
	 */
	public static final String HTML_DATA_TABLE = "docx4j.convert.out.html.chartDataTable";

	/**
	 * The HTML for a chart: an empty inline {@code <svg>} of the drawing's extent; with
	 * {@link #HTML_DATA_TABLE} set, then a {@code <table class="docx4j-chart-data">} of its data
	 * ({@link ChartTable}), its title the caption.  The table follows the box where the chart is,
	 * inside its paragraph, where an HTML parser ends the paragraph before it.
	 *
	 * @return null where the drawing has no extent
	 */
	public static DocumentFragment createHtml(AbstractWmlConversionContext context, Object anchorOrInline) {
		Object o = anchorOrInline instanceof NodeIterator ? unmarshal((NodeIterator) anchorOrInline) : anchorOrInline;
		ChartBox box = of(o);
		if (box == null) return null;
		DocumentFragment f = box.toHtml();
		if (context != null && org.docx4j.Docx4jProperties.getProperty(HTML_DATA_TABLE, false)) {
			Element table = dataTable(f.getOwnerDocument(), context.getCurrentPart(), o);
			if (table != null) f.appendChild(table);
		}
		return f;
	}

	private static Element dataTable(Document d, org.docx4j.openpackaging.parts.Part host, Object anchor) {
		ChartRef ref = ChartRef.of(host, anchor);
		if (ref == null) return null;
		ChartTable t;
		try {
			t = ref.isChartEx() ? ChartTable.of(ref.readChartEx()) : ChartTable.of(ref.read());
		} catch (Exception e) {
			log.warn("Couldn't describe the chart " + ref.getName() + ": " + e.getMessage());
			return null;
		}
		Element table = d.createElement("table");
		table.setAttribute("class", "docx4j-chart-data");
		if (t.getTitle() != null) {
			Element caption = d.createElement("caption");
			caption.setTextContent(t.getTitle());
			table.appendChild(caption);
		}
		Element head = d.createElement("thead");
		head.appendChild(row(d, t.getHeader(), "th"));
		table.appendChild(head);
		Element body = d.createElement("tbody");
		for (java.util.List<String> r : t.getRows()) body.appendChild(row(d, r, "td"));
		table.appendChild(body);
		return table;
	}

	private static Element row(Document d, java.util.List<String> cells, String cellName) {
		Element tr = d.createElement("tr");
		for (String c : cells) {
			Element cell = d.createElement(cellName);
			cell.setTextContent(c);
			tr.appendChild(cell);
		}
		return tr;
	}

	/** As {@link #createHtml(AbstractWmlConversionContext, Object)}, for the XSLT */
	public static DocumentFragment createHtml(AbstractWmlConversionContext context, NodeIterator wpInline) {
		return createHtml(context, (Object) wpInline);
	}

	/** This box as FO; see {@link #createXslFo(AbstractWmlConversionContext, Object)} */
	public DocumentFragment toXslFo(AbstractWmlConversionContext context) {

		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		Element ifo = d.createElementNS(FO_NS, "fo:instream-foreign-object");
		d.appendChild(ifo);
		// as AbstractWordXmlPicture sizes a picture's frame, which WordLayoutFixups and the
		// table writer read back
		ifo.setAttribute("content-width", length(widthPt) + "pt");
		ifo.setAttribute("content-height", length(heightPt) + "pt");
		ifo.setAttribute("scaling", "non-uniform");
		ifo.setAttributeNS(FOX_NS, "fox:alt-text", altText);
		ifo.appendChild(svg(d, length(widthPt) + "pt", length(heightPt) + "pt"));

		WordXmlPictureE20.stampAnchorHints(ifo, anchor, context);

		DocumentFragment f = d.createDocumentFragment();
		f.appendChild(d.getDocumentElement());
		return f;
	}

	/** This box as HTML; see {@link #createHtml(AbstractWmlConversionContext, Object)} */
	public DocumentFragment toHtml() {

		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		// a picture's <img> gives its points as pixels (AbstractWordXmlPicture), so the same here
		Element svg = svg(d, Long.toString(Math.round(widthPt)), Long.toString(Math.round(heightPt)));
		svg.setAttribute("role", "img");
		svg.setAttribute("aria-label", altText);
		d.appendChild(svg);

		DocumentFragment f = d.createDocumentFragment();
		f.appendChild(d.getDocumentElement());
		return f;
	}

	private Element svg(Document d, String width, String height) {
		Element svg = d.createElementNS(SVG_NS, "svg");
		svg.setAttribute("width", width);
		svg.setAttribute("height", height);
		svg.setAttribute("viewBox", "0 0 " + length(widthPt) + " " + length(heightPt));
		return svg;
	}

	/** At most two decimals and no trailing zeros, as a picture's lengths are written */
	private static String length(double v) {
		String s = String.format(java.util.Locale.ROOT, "%.2f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	/**
	 * Whether the object a walk visits is a chart's reference, a {@code c:chart} or a
	 * {@code cx:chart} (each a {@code CTRelId} in its own package).
	 */
	public static boolean isChartReference(Object o) {
		if (o instanceof JAXBElement) o = ((JAXBElement<?>) o).getValue();
		return o instanceof org.docx4j.dml.chart.CTRelId
				|| o instanceof org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTRelId;
	}
}
