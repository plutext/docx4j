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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.jaxb.McMode;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.ChartsheetPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Comments;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The charts placed in a package, in document order: a docx's body, then its headers, footers,
 * notes and comments; a pptx's slides, then its layouts and masters; an xlsx's sheets in the
 * workbook's order, worksheets and chartsheets alike.  A chart in an {@code mc:AlternateContent}
 * Choice is found too (Word and PowerPoint put a chartex chart there, with a picture of it as
 * the Fallback), and marked as such.
 *
 * @since 17.3.3 (CR-035)
 */
public final class Charts {

	private static final Logger log = LoggerFactory.getLogger(Charts.class);

	private Charts() {
	}

	public static List<ChartRef> in(OpcPackage pkg) throws Docx4JException {
		List<ChartRef> refs = new ArrayList<ChartRef>();
		if (pkg instanceof WordprocessingMLPackage) docx((WordprocessingMLPackage) pkg, refs);
		else if (pkg instanceof PresentationMLPackage) pptx((PresentationMLPackage) pkg, refs);
		else if (pkg instanceof SpreadsheetMLPackage) xlsx((SpreadsheetMLPackage) pkg, refs);
		return Collections.unmodifiableList(refs);
	}

	// ------------------------------------------------------------------ docx

	private static void docx(WordprocessingMLPackage pkg, List<ChartRef> refs) {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		walkWml(mdp, mdp.getJaxbElement().getBody(), refs);
		RelationshipsPart rp = mdp.getRelationshipsPart();
		if (rp == null) return;
		for (Relationship r : rp.getRelationships().getRelationship()) {
			Part p = rp.getPart(r);
			Object content = null;
			if (p instanceof HeaderPart) content = ((HeaderPart) p).getJaxbElement();
			else if (p instanceof FooterPart) content = ((FooterPart) p).getJaxbElement();
			else if (p instanceof FootnotesPart) content = new ArrayList<Object>(((FootnotesPart) p).getJaxbElement().getFootnote());
			else if (p instanceof EndnotesPart) content = new ArrayList<Object>(((EndnotesPart) p).getJaxbElement().getEndnote());
			else if (p instanceof CommentsPart) {
				List<Object> l = new ArrayList<Object>();
				for (Comments.Comment c : ((CommentsPart) p).getJaxbElement().getComment()) l.addAll(c.getContent());
				content = l;
			}
			if (content != null) walkWml(p, content, refs);
		}
	}

	private static void walkWml(final Part host, Object content, final List<ChartRef> refs) {
		TraversalUtil.visit(content, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof org.docx4j.wml.Drawing) {
					for (Object a : ((org.docx4j.wml.Drawing) o).getAnchorOrInline()) {
						ChartRef r = ChartRef.of(host, a, inChoice(o));
						if (r != null) refs.add(r);
					}
				}
				return null;
			}
		}, McMode.ALL);
	}

	/** Whether an object's parents include an mc:AlternateContent Choice */
	private static boolean inChoice(Object o) {
		Object p = o instanceof Child ? ((Child) o).getParent() : null;
		for (int i = 0; p != null && i < 50; i++) {
			if (p instanceof AlternateContent.Choice) return true;
			p = p instanceof Child ? ((Child) p).getParent() : null;
		}
		return false;
	}

	// ------------------------------------------------------------------ pptx

	private static void pptx(PresentationMLPackage pkg, List<ChartRef> refs) throws Docx4JException {
		List<Part> layouts = new ArrayList<Part>(), masters = new ArrayList<Part>();
		List<SlidePart> slides;
		try {
			slides = pkg.getMainPresentationPart().getSlideParts();
		} catch (org.pptx4j.Pptx4jException e) {
			throw new Docx4JException("Couldn't list the slides: " + e.getMessage(), e);
		}
		for (SlidePart slide : slides) {
			walkShapes(slide, slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(), false, refs);
			SlideLayoutPart layout = slide.getSlideLayoutPart();
			if (layout != null && !layouts.contains(layout)) layouts.add(layout);
		}
		for (Part p : layouts) {
			SlideLayoutPart layout = (SlideLayoutPart) p;
			walkShapes(layout, layout.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(), false, refs);
			SlideMasterPart master = layout.getSlideMasterPart();
			if (master != null && !masters.contains(master)) masters.add(master);
		}
		for (Part p : masters) {
			SlideMasterPart master = (SlideMasterPart) p;
			walkShapes(master, master.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(), false, refs);
		}
	}

	private static void walkShapes(Part host, List<Object> shapes, boolean inChoice, List<ChartRef> refs) {
		if (shapes == null) return;
		for (Object o : shapes) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof org.pptx4j.pml.CTGraphicalObjectFrame) {
				ChartRef r = ChartRef.of(host, u, inChoice);
				if (r != null) refs.add(r);
			} else if (u instanceof org.pptx4j.pml.GroupShape) {
				walkShapes(host, ((org.pptx4j.pml.GroupShape) u).getSpOrGrpSpOrGraphicFrame(), inChoice, refs);
			} else if (u instanceof org.w3c.dom.Element) {
				// the content of a Choice docx4j left unread (a chartex chart's frame)
				ChartRef r = ChartRef.of(host, u, inChoice);
				if (r != null) refs.add(r);
			} else if (u instanceof AlternateContent) {
				AlternateContent ac = (AlternateContent) u;
				for (AlternateContent.Choice c : ac.getChoice()) walkShapes(host, c.getAny(), true, refs);
				if (ac.getFallback() != null) walkShapes(host, ac.getFallback().getAny(), inChoice, refs);
			}
		}
	}

	// ------------------------------------------------------------------ xlsx

	private static void xlsx(SpreadsheetMLPackage pkg, List<ChartRef> refs) throws Docx4JException {
		RelationshipsPart wbRels = pkg.getWorkbookPart().getRelationshipsPart();
		for (org.xlsx4j.sml.Sheet sheet : pkg.getWorkbookPart().getContents().getSheets().getSheet()) {
			Part p = wbRels.getPart(sheet.getId());
			if (!(p instanceof WorksheetPart) && !(p instanceof ChartsheetPart)) continue;
			RelationshipsPart rp = p.getRelationshipsPart();
			if (rp == null) continue;
			for (Relationship r : rp.getRelationships().getRelationship()) {
				if (!Namespaces.SPREADSHEETML_DRAWING.equals(r.getType())) continue;
				Part d = rp.getPart(r);
				if (d instanceof Drawing) walkAnchors((Drawing) d, refs);
			}
		}
	}

	private static void walkAnchors(Drawing drawing, List<ChartRef> refs) throws Docx4JException {
		Object root = ((JaxbXmlPart<?>) drawing).getContents();
		if (!(root instanceof CTDrawing)) return;
		for (Object o : ((CTDrawing) root).getEGAnchor()) {
			Object a = XmlUtils.unwrap(o);
			ChartRef r = ChartRef.of(drawing, a, false);
			if (r != null) {
				refs.add(r);
				continue;
			}
			AlternateContent ac = a instanceof org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor
					? ((org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor) a).getAlternateContent()
					: a instanceof org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor
					? ((org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor) a).getAlternateContent()
					: a instanceof org.docx4j.dml.spreadsheetdrawing.CTAbsoluteAnchor
					? ((org.docx4j.dml.spreadsheetdrawing.CTAbsoluteAnchor) a).getAlternateContent() : null;
			if (ac == null) continue;
			for (AlternateContent.Choice c : ac.getChoice()) {
				for (Object f : c.getAny()) {
					ChartRef cr = ChartRef.of(drawing, f, true);
					if (cr != null) refs.add(cr);
				}
			}
		}
	}
}
