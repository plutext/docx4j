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

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.spreadsheetdrawing.CTAbsoluteAnchor;
import org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A chart placed somewhere: its host part, the anchor that places it, its extent, its name and
 * description, and its chart part, a {@link Chart} or, for the Office 2016 kinds, a
 * {@link ChartExSpacePart}.  Found by {@link Charts#in}, or made for one anchor by
 * {@link #of(Part, Object)}.
 *
 * <p>The host is the part whose relationships hold the chart: a docx's main document, header,
 * footer, footnotes, endnotes or comments part; a pptx's slide, layout or master; an xlsx's
 * drawing part (a worksheet's, or a chartsheet's).</p>
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartRef {

	private final Part host;
	private final Object anchor;
	private final Part chartPart;
	private final String relationshipId;
	private final String name, description;
	private final Long width, height;
	private final boolean inChoice;

	private ChartRef(Part host, Object anchor, Part chartPart, String relationshipId, String name, String description,
			Long width, Long height, boolean inChoice) {
		this.host = host;
		this.anchor = anchor;
		this.chartPart = chartPart;
		this.relationshipId = relationshipId;
		this.name = name;
		this.description = description;
		this.width = width;
		this.height = height;
		this.inChoice = inChoice;
	}

	/**
	 * The chart an anchor places: a {@code wp:inline} or {@code wp:anchor}, a pptx's
	 * {@code p:graphicFrame}, an xlsx's {@code xdr:twoCellAnchor}, {@code oneCellAnchor},
	 * {@code absoluteAnchor} or {@code xdr:graphicFrame}.
	 *
	 * @return null where the anchor places no chart, or its chart part is missing
	 */
	public static ChartRef of(Part host, Object anchor) {
		return of(host, anchor, false);
	}

	static ChartRef of(Part host, Object anchor, boolean inChoice) {
		Object a = XmlUtils.unwrap(anchor);
		if (a instanceof Element) return of(host, (Element) a, inChoice);
		Graphic graphic = null;
		CTPositiveSize2D ext = null;
		CTNonVisualDrawingProps nv = null;
		if (a instanceof Inline) {
			graphic = ((Inline) a).getGraphic();
			ext = ((Inline) a).getExtent();
			nv = ((Inline) a).getDocPr();
		} else if (a instanceof Anchor) {
			graphic = ((Anchor) a).getGraphic();
			ext = ((Anchor) a).getExtent();
			nv = ((Anchor) a).getDocPr();
		} else if (a instanceof org.pptx4j.pml.CTGraphicalObjectFrame) {
			org.pptx4j.pml.CTGraphicalObjectFrame f = (org.pptx4j.pml.CTGraphicalObjectFrame) a;
			graphic = f.getGraphic();
			ext = ext(f.getXfrm());
			nv = f.getNvGraphicFramePr() == null ? null : f.getNvGraphicFramePr().getCNvPr();
		} else if (a instanceof org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame) {
			org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame f = (org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame) a;
			graphic = f.getGraphic();
			nv = f.getNvGraphicFramePr() == null ? null : f.getNvGraphicFramePr().getCNvPr();
		} else if (a instanceof CTTwoCellAnchor) {
			return frame(host, a, ((CTTwoCellAnchor) a).getGraphicFrame(), null, inChoice);
		} else if (a instanceof CTOneCellAnchor) {
			return frame(host, a, ((CTOneCellAnchor) a).getGraphicFrame(), ((CTOneCellAnchor) a).getExt(), inChoice);
		} else if (a instanceof CTAbsoluteAnchor) {
			return frame(host, a, ((CTAbsoluteAnchor) a).getGraphicFrame(), ((CTAbsoluteAnchor) a).getExt(), inChoice);
		}
		return make(host, a, graphic, ext, nv, inChoice);
	}

	private static final String R_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	private static final String A_NS = "http://schemas.openxmlformats.org/drawingml/2006/main";

	/**
	 * A frame docx4j left unread: the content of an {@code mc:AlternateContent} Choice it does not
	 * select, in a pptx's shape tree or an xlsx anchor (a chartex chart's), read from the DOM
	 */
	private static ChartRef of(Part host, Element frame, boolean inChoice) {
		Element gd = first(frame, A_NS, "graphicData");
		if (gd == null || host == null) return null;
		String id = null;
		for (Node n = gd.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && "chart".equals(n.getLocalName())) id = ((Element) n).getAttributeNS(R_NS, "id");
		}
		if (id == null || id.isEmpty()) return null;
		RelationshipsPart rp = host.getRelationshipsPart();
		Part chart = rp == null ? null : rp.getPart(id);
		if (!(chart instanceof Chart) && !(chart instanceof ChartExSpacePart)) return null;
		Element nv = first(frame, null, "cNvPr");
		Element ext = null;
		Element xfrm = first(frame, null, "xfrm");
		if (xfrm != null) ext = first(xfrm, A_NS, "ext");
		Long cx = ext == null ? null : number(ext.getAttribute("cx")), cy = ext == null ? null : number(ext.getAttribute("cy"));
		if (cx != null && cx == 0 && cy != null && cy == 0) cx = cy = null;   // an xlsx frame's placeholder extent
		return new ChartRef(host, frame, chart, id, nv == null ? null : nv.getAttribute("name"),
				nv == null || !nv.hasAttribute("descr") ? null : nv.getAttribute("descr"), cx, cy, inChoice);
	}

	private static Element first(Element e, String ns, String localName) {
		NodeList l = ns == null ? e.getElementsByTagNameNS("*", localName) : e.getElementsByTagNameNS(ns, localName);
		return l.getLength() == 0 ? null : (Element) l.item(0);
	}

	private static Long number(String s) {
		try {
			return s == null || s.isEmpty() ? null : Long.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** An xlsx anchor's frame, its extent the anchor's (a two-cell anchor's spans cells: none) */
	private static ChartRef frame(Part host, Object anchor, org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame f,
			CTPositiveSize2D ext, boolean inChoice) {
		if (f == null) return null;
		CTNonVisualDrawingProps nv = f.getNvGraphicFramePr() == null ? null : f.getNvGraphicFramePr().getCNvPr();
		return make(host, anchor, f.getGraphic(), ext, nv, inChoice);
	}

	private static CTPositiveSize2D ext(CTTransform2D xfrm) {
		if (xfrm == null || xfrm.getExt() == null) return null;
		CTPositiveSize2D s = new CTPositiveSize2D();
		s.setCx(xfrm.getExt().getCx());
		s.setCy(xfrm.getExt().getCy());
		return s;
	}

	private static ChartRef make(Part host, Object anchor, Graphic graphic, CTPositiveSize2D ext,
			CTNonVisualDrawingProps nv, boolean inChoice) {
		GraphicData gd = graphic == null ? null : graphic.getGraphicData();
		if (gd == null) return null;
		String id = null;
		for (Object o : gd.getAny()) {
			if (!ChartBox.isChartReference(o)) continue;
			Object u = XmlUtils.unwrap(o);
			id = u instanceof org.docx4j.dml.chart.CTRelId ? ((org.docx4j.dml.chart.CTRelId) u).getId()
					: ((org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTRelId) u).getId();
		}
		if (id == null || host == null) return null;
		RelationshipsPart rp = host.getRelationshipsPart();
		Part chart = rp == null ? null : rp.getPart(id);
		if (!(chart instanceof Chart) && !(chart instanceof ChartExSpacePart)) return null;
		return new ChartRef(host, anchor, chart, id, nv == null ? null : nv.getName(), nv == null ? null : nv.getDescr(),
				ext == null ? null : ext.getCx(), ext == null ? null : ext.getCy(), inChoice);
	}

	/** The part whose relationships hold the chart */
	public Part getHost() {
		return host;
	}

	/**
	 * The anchor: Inline, Anchor, a pptx or xlsx graphic frame, an xlsx cell anchor; or, for a
	 * chart in an {@code mc:AlternateContent} Choice docx4j left unread, the frame's DOM element
	 * To draw, edit or create Office 2016's chart types (waterfall, histogram, Pareto, box and whisker,
	 * treemap, sunburst, funnel), see Docx4j Enterprise ChartEx, https://www.plutext.com/ ({@link ChartExNotice}).
	 */
	public Object getAnchor() {
		return anchor;
	}

	/** The chart part: a {@link Chart}, or a {@link ChartExSpacePart} (chartex) */
	public Part getChartPart() {
		return chartPart;
	}

	/** Whether the chart part is a chartex part (waterfall, funnel, ...) */
	public boolean isChartEx() {
		return chartPart instanceof ChartExSpacePart;
	}

	/** The relationship id the graphic data names */
	public String getRelationshipId() {
		return relationshipId;
	}

	/** The drawing's name ({@code docPr/@name}, {@code cNvPr/@name}): "Chart 1" */
	public String getName() {
		return name;
	}

	/** The drawing's description ({@code @descr}), its alternative text */
	public String getDescription() {
		return description;
	}

	/** The extent's width in EMUs; null where the anchor gives none (an xlsx two-cell anchor) */
	public Long getWidth() {
		return width;
	}

	/** The extent's height in EMUs; null where the anchor gives none */
	public Long getHeight() {
		return height;
	}

	/**
	 * Whether the chart was found in an {@code mc:AlternateContent} Choice: Word and PowerPoint
	 * put a chartex chart there, with a picture of it as the Fallback
	 * To draw, edit or create Office 2016's chart types (waterfall, histogram, Pareto, box and whisker,
	 * treemap, sunburst, funnel), see Docx4j Enterprise ChartEx, https://www.plutext.com/ ({@link ChartExNotice}).
	 */
	public boolean isInChoice() {
		return inChoice;
	}

	/** The theme and colour map the chart is drawn in: its host's (CR-034) */
	public ThemeColors getHostTheme() {
		return ThemeColors.of(host);
	}

	/** The chart read; null for a chartex chart (see {@link #readChartEx()}) */
	public ChartModel read() throws Docx4JException {
		return chartPart instanceof Chart ? ChartModel.read((Chart) chartPart, getHostTheme()) : null;
	}

	/** A chartex chart read; null for any other */
	public ChartExModel readChartEx() throws Docx4JException {
		return chartPart instanceof ChartExSpacePart ? ChartExModel.read((ChartExSpacePart) chartPart) : null;
	}
}
