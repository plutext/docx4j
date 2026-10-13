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
import org.docx4j.openpackaging.parts.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * A chart in the FO and HTML outputs: the box of the drawing's extent ({@code wp:extent}),
 * placed as a picture of that size would be, inline in its line or, for a {@code wp:anchor},
 * floating through the same anchor hints a picture gets, and the chart drawn in it by the
 * {@link ChartRenderer} on the classpath (docx4j-chart-render's, which docx4j-export-fo brings;
 * CR-037 phase 3), or a chartex chart by the {@link ChartExRenderer} (CR-039).  Where none draws
 * it (no renderer for its kind, a chart it refuses) the box stays empty, so the chart keeps its
 * space.  Until 17.3.3 every output dropped a chart
 * without keeping its space, so the text after it moved up into its place and the pages broke
 * where Word's don't (CR-035 phase 0).
 *
 * <p>In FO the box is an {@code fo:instream-foreign-object}, sized as a picture's is, with the
 * chart's name as its {@code fox:alt-text}, holding the chart's SVG (FOP draws it as vectors, its
 * text as text) or an empty one.  In HTML it is an inline {@code <svg>} (or, with
 * {@link #HTML_PNG}, an {@code <img>} of the chart's picture), sized as a picture's {@code <img>}
 * is (its points as pixels), with the name as its {@code aria-label}; each chart's SVG ids are
 * prefixed with its part's name, since one document holds them all.</p>
 *
 * <p>Used for a {@code c:chart} and, where the outputs take a chartex chart's {@code mc:Choice}, a
 * {@code cx:chart}, drawn by the {@link ChartExRenderer}.  By default they take it only where a
 * chartex renderer draws the chart, and otherwise see its {@code mc:Fallback}, which is drawn as
 * written ({@link ChartExBranch}, CR-039).</p>
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
	/** the chart drawn (CR-037 phase 3), its box's size; null: the box stays empty */
	private Document drawing;
	/** the chart drawn */
	private ChartRef chart;
	/** the chart drawn: its part's name, for the ids of its SVG in HTML */
	private String chartName;

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
		Object o = anchorOrInline instanceof NodeIterator ? unmarshal((NodeIterator) anchorOrInline) : anchorOrInline;
		ChartBox box = of(o);
		if (box == null) return null;
		box.draw(context == null ? null : context.getCurrentPart(), o);
		return box.toXslFo(context);
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
		box.draw(context == null ? null : context.getCurrentPart(), o);
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

	/**
	 * The chart drawn into this box by the {@link ChartRenderer} on the classpath
	 * (docx4j-chart-render's), where there is one and it draws the chart; else the box stays
	 * empty.  A chart it refuses (a structural feature it doesn't draw) is logged with its
	 * reasons, one it draws simplified likewise (CR-037 phase 3).
	 *
	 * @param host the part whose relationships hold the chart (the conversion's current part)
	 * @return whether the chart was drawn
	 */
	boolean draw(Part host, Object anchorOrInline) {
		return draw(ChartRef.of(host, anchorOrInline));
	}

	private boolean draw(ChartRef ref) {
		drawing = ChartImages.svg(ref, widthPt, heightPt);
		if (drawing != null) {
			chart = ref;
			String n = ref.getChartPart().getPartName().getName();
			chartName = n.substring(n.lastIndexOf('/') + 1).replace(".xml", "").replaceAll("[^A-Za-z0-9_-]", "_");
		}
		return drawing != null;
	}

	/**
	 * The box of the chart a reference names, its size the reference's ({@link ChartImages#getSize}),
	 * drawn where the chart can be: for an output that places the box itself (a pptx's frame in
	 * SVG-in-HTML).
	 *
	 * @return null where the chart has no size
	 * @since 17.3.3 (CR-037 phase 3)
	 */
	public static ChartBox forChart(ChartRef chart) {
		java.awt.geom.Dimension2D size = ChartImages.getSize(chart);
		if (size == null) return null;
		String alt = chart.getDescription() != null && !chart.getDescription().trim().isEmpty() ? chart.getDescription().trim()
				: chart.getName() != null && !chart.getName().trim().isEmpty() ? chart.getName().trim() : "Chart";
		ChartBox box = new ChartBox(size.getWidth(), size.getHeight(), alt, null);
		box.draw(chart);
		return box;
	}

	/** @return whether the chart is drawn in this box (rather than the box left empty) */
	public boolean isDrawn() {
		return drawing != null;
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
		ifo.appendChild(drawing != null ? d.importNode(drawing.getDocumentElement(), true)
				: svg(d, length(widthPt) + "pt", length(heightPt) + "pt"));

		WordXmlPictureE20.stampAnchorHints(ifo, anchor, context);

		DocumentFragment f = d.createDocumentFragment();
		f.appendChild(d.getDocumentElement());
		return f;
	}

	/**
	 * The property which, set true, writes a drawn chart in HTML as a PNG picture (an
	 * {@code <img>} with a data URI, at twice the box's pixels) rather than an inline SVG
	 * (CR-037 D8).  False by default.
	 *
	 * @since 17.3.3
	 */
	public static final String HTML_PNG = "docx4j.convert.out.html.chartAsPng";

	/**
	 * This box as HTML; see {@link #createHtml(AbstractWmlConversionContext, Object)}.  A
	 * drawn chart is its SVG (or, with {@link #HTML_PNG}, its picture), sized as a picture's
	 * {@code <img>} is: its points as pixels.
	 */
	public DocumentFragment toHtml() {
		// a picture's <img> gives its points as pixels (AbstractWordXmlPicture), so the same here
		return toHtml(Long.toString(Math.round(widthPt)), Long.toString(Math.round(heightPt)));
	}

	/**
	 * This box as HTML, as {@link #toHtml()}, its width and height as given (an attribute's
	 * value: pixels, or a length)
	 *
	 * @since 17.3.3 (CR-037 phase 3)
	 */
	public DocumentFragment toHtml(String w, String h) {

		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		Element e = null;
		if (drawing != null && org.docx4j.Docx4jProperties.getProperty(HTML_PNG, false)) {
			e = png(d, w, h);
		}
		if (e == null && drawing != null) {
			e = (Element) d.importNode(drawing.getDocumentElement(), true);
			e.setAttribute("width", w);
			e.setAttribute("height", h);
			// one document holds every chart's SVG, so each chart's ids are its own
			prefixIds(e, "docx4j-" + chartName + "-");
		}
		if (e == null) {
			e = svg(d, w, h);
		}
		boolean img = "img".equals(e.getNodeName());
		if (!img) e.setAttribute("role", "img");
		e.setAttribute(img ? "alt" : "aria-label", altText);
		d.appendChild(e);

		DocumentFragment f = d.createDocumentFragment();
		f.appendChild(d.getDocumentElement());
		return f;
	}

	/** The drawn chart as an {@code <img>} of its picture, a data URI; null where it can't be made */
	private Element png(Document d, String w, String h) {
		java.awt.image.BufferedImage image = ChartImages.image(chart, widthPt, heightPt, 2 * 96);
		if (image == null) return null;
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		try {
			javax.imageio.ImageIO.write(image, "png", out);
		} catch (java.io.IOException e) {
			log.warn("Couldn't write the chart " + chart.getName() + " as PNG: " + e.getMessage());
			return null;
		}
		Element img = d.createElement("img");
		img.setAttribute("src", "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(out.toByteArray()));
		img.setAttribute("width", w);
		img.setAttribute("height", h);
		return img;
	}

	/**
	 * Each id in the SVG prefixed, and each reference to one ({@code url(#id)}, {@code #id}):
	 * Batik numbers a drawing's clip paths from 1, and an HTML document holds every chart's
	 */
	private static void prefixIds(Element svg, String prefix) {
		java.util.Set<String> ids = new java.util.HashSet<>();
		collectIds(svg, ids);
		if (!ids.isEmpty()) renameIds(svg, ids, prefix);
	}

	private static void collectIds(Element e, java.util.Set<String> ids) {
		if (e.hasAttribute("id")) ids.add(e.getAttribute("id"));
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) collectIds((Element) n, ids);
		}
	}

	private static void renameIds(Element e, java.util.Set<String> ids, String prefix) {
		org.w3c.dom.NamedNodeMap attrs = e.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			org.w3c.dom.Attr a = (org.w3c.dom.Attr) attrs.item(i);
			String v = a.getValue();
			if ("id".equals(a.getName())) {
				a.setValue(prefix + v);
			} else if (v.startsWith("#") && ids.contains(v.substring(1))) {
				a.setValue("#" + prefix + v.substring(1));
			} else if (v.contains("url(#")) {
				StringBuilder b = new StringBuilder();
				java.util.regex.Matcher m = java.util.regex.Pattern.compile("url\\(#([^)]*)\\)").matcher(v);
				while (m.find()) {
					m.appendReplacement(b, java.util.regex.Matcher.quoteReplacement(
							ids.contains(m.group(1)) ? "url(#" + prefix + m.group(1) + ")" : m.group()));
				}
				m.appendTail(b);
				a.setValue(b.toString());
			}
		}
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) renameIds((Element) n, ids, prefix);
		}
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
