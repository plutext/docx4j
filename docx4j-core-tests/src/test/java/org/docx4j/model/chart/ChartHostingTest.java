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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McMode;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.ChartColorStylePart;
import org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart;
import org.docx4j.openpackaging.parts.DrawingML.ChartStylePart;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.After;
import org.junit.Test;
import org.xlsx4j.model.AreaRef;
import org.xlsx4j.model.CellRef;

/**
 * CR-039 phase 3: {@link ChartCreator}'s host steps opened as {@link ChartHosting}, so that a
 * creator of other charts can put a chart in its host.  Office's own waterfall (CR-035 phase 0's
 * fixtures) is put back through ChartHosting alone: in a new docx and a new pptx, its parts
 * copied, its workbook embedded and filled from its caches, its Fallback a picture as Word and
 * PowerPoint write one; in its own xlsx, its anchor made afresh with Excel's Fallback.  A
 * chartex chart is deleted whole, its Fallback's picture with it.  The re-hosted files are
 * written to {@code target/cr039/} for the Office check (Word, PowerPoint and Excel open them
 * without repair).  That ChartCreator's own charts are unchanged by the move is CR-036's tests.
 */
public class ChartHostingTest {

	private static final String CX1 = "http://schemas.microsoft.com/office/drawing/2015/9/8/chartex";

	@After
	public void reset() {
		ChartExRenderer.setRenderer(null);
	}

	private static OpcPackage load(String name) throws Exception {
		try (InputStream is = ChartHostingTest.class.getClassLoader().getResourceAsStream("charts/" + name)) {
			return OpcPackage.load(is);
		}
	}

	private static ChartRef chartEx(OpcPackage pkg) throws Exception {
		for (ChartRef c : Charts.in(pkg)) {
			if (c.isChartEx()) return c;
		}
		return null;
	}

	/** A part's contents copied into a part of the same kind in another package */
	private static <T extends JaxbXmlPart<?>> T copy(JaxbXmlPart<?> from, T to, OpcPackage pkg) throws Exception {
		to.setPackage(pkg);
		to.unmarshal(new ByteArrayInputStream(from.getXML().getBytes(StandardCharsets.UTF_8)));
		return to;
	}

	private static Part related(Part from, String type) {
		for (Relationship r : from.getRelationshipsPart().getRelationships().getRelationship()) {
			if (type.equals(r.getType())) return from.getRelationshipsPart().getPart(r);
		}
		return null;
	}

	/** The chart's caches as its workbook's grid: its name over its values, its categories down the first column */
	private static List<List<Object>> grid(ChartExModel m) {
		ChartExModel.Series s = m.getSeries().get(0);
		Map<Integer, String> cats = null, vals = null;
		for (ChartExModel.Dimension d : s.getData()) {
			if ("cat".equals(d.getType())) cats = d.getLevels().get(0);
			if ("val".equals(d.getType())) vals = d.getLevels().get(0);
		}
		List<List<Object>> grid = new ArrayList<>();
		List<Object> top = new ArrayList<>();
		top.add(null);
		top.add(s.getName());
		grid.add(top);
		for (int i = 0; i < cats.size(); i++) {
			List<Object> row = new ArrayList<>();
			row.add(cats.get(i));
			row.add(Double.valueOf(vals.get(i)));
			grid.add(row);
		}
		return grid;
	}

	/** An element's mc:AlternateContent, found anywhere in a part's content */
	private static AlternateContent alternateContent(Object content) {
		List<AlternateContent> found = new ArrayList<>();
		new TraversalUtil(content, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent) found.add((AlternateContent) o);
				return null;
			}
		}, McMode.ALL);
		return found.isEmpty() ? null : found.get(0);
	}

	private static String xml(Object o, jakarta.xml.bind.JAXBContext jc) {
		Object u = XmlUtils.unwrap(o);
		return u instanceof org.w3c.dom.Node ? XmlUtils.w3CDomNodeToString((org.w3c.dom.Node) u) : XmlUtils.marshaltoString(o, true, false, jc);
	}

	private static String partXml(OpcPackage pkg, String name) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null;) {
				if (e.getName().equals(name)) return new String(z.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		return null;
	}

	private static File office(String name) {
		File dir = new File("target/cr039");
		dir.mkdirs();
		return new File(dir, name);
	}

	/** The re-hosted chart as Office's: found as a chartex chart in a Choice, its data the original's, drawn where a renderer draws it */
	private static void sameChart(ChartRef original, OpcPackage pkg) throws Exception {
		ChartRef c = chartEx(pkg);
		assertNotNull(c);
		assertTrue(c.isInChoice());
		ChartExModel m = c.readChartEx(), o = original.readChartEx();
		assertEquals("waterfall", m.getSeries().get(0).getLayout());
		assertEquals(o.getSeries().get(0).getData().get(1).getLevels(), m.getSeries().get(0).getData().get(1).getLevels());
		ChartExRenderer.setRenderer(new ChartExRendererTest.TestRenderer(true));
		assertNotNull(ChartImages.toSvg(c, 432, 252));
		ChartExRenderer.setRenderer(null);
	}

	@Test
	public void aWaterfallPutInANewDocx() throws Exception {
		WordprocessingMLPackage src = (WordprocessingMLPackage) load("waterfall.docx");
		ChartRef from = chartEx(src);
		ChartExSpacePart sp = (ChartExSpacePart) from.getChartPart();

		WordprocessingMLPackage dst = WordprocessingMLPackage.createPackage();
		MainDocumentPart main = dst.getMainDocumentPart();
		ChartHosting hosting = new ChartHosting(new Random(39));
		ChartExSpacePart chart = copy(sp, new ChartExSpacePart(new PartName("/word/charts/chartEx1.xml")), dst);
		// in Word's order: the workbook rId1, the style rId2, the colours rId3
		EmbeddedPackagePart wb = hosting.workbook(main, ((EmbeddedPackagePart) related(sp, Namespaces.EMBEDDED_PKG)).getBytes());
		hosting.fill((SpreadsheetMLPackage) wb.getEmbeddedPackage(), grid(from.readChartEx()), null);
		Relationship r = hosting.relate(chart, wb);
		assertEquals("rId1", r.getId());
		chart.getContents().getChartData().getExternalData().setId(r.getId());
		chart.addTargetPart(copy((JaxbXmlPart<?>) related(sp, Namespaces.CHART_STYLE), new ChartStylePart(new PartName("/word/charts/style1.xml")), dst));
		chart.addTargetPart(copy((JaxbXmlPart<?>) related(sp, Namespaces.CHART_COLOR_STYLE),
				new ChartColorStylePart(new PartName("/word/charts/colors1.xml")), dst));
		// the Fallback: Word's picture of the chart, as Word writes it
		AlternateContent ac = alternateContent(src.getMainDocumentPart().getContent());
		Object picture = ac.getFallback().getAny().get(0);
		String embed = xml(picture, org.docx4j.jaxb.Context.jc).replaceAll("(?s).*r:embed=\"([^\"]*)\".*", "$1");
		BinaryPartAbstractImage image = BinaryPartAbstractImage.createImagePart(dst, main,
				((BinaryPartAbstractImage) src.getMainDocumentPart().getRelationshipsPart().getPart(embed)).getBytes());
		String fallback = xml(picture, org.docx4j.jaxb.Context.jc).replace("r:embed=\"" + embed + "\"", "r:embed=\"" + image.getSourceRelationships().get(0).getId() + "\"");
		R run = hosting.docxRun(main, chart, from.getWidth(), from.getHeight(), "cx1", fallback);
		P p = new P();
		p.getContent().add(run);
		main.getContent().add(p);

		// the Fallback's drawing in the chart's place, with its ids
		AlternateContent made = alternateContent(main.getContent());
		org.docx4j.dml.wordprocessingDrawing.Inline choice = (org.docx4j.dml.wordprocessingDrawing.Inline) ((org.docx4j.wml.Drawing) XmlUtils
				.unwrap(made.getChoice().get(0).getAny().get(0))).getAnchorOrInline().get(0);
		org.docx4j.dml.wordprocessingDrawing.Inline fb = (org.docx4j.dml.wordprocessingDrawing.Inline) ((org.docx4j.wml.Drawing) XmlUtils
				.unwrap(made.getFallback().getAny().get(0))).getAnchorOrInline().get(0);
		assertEquals(choice.getDocPr().getId(), fb.getDocPr().getId());
		assertEquals("Chart 1", fb.getDocPr().getName());
		assertEquals(5486400, fb.getExtent().getCx());

		// the Choice's Requires declared, as Word needs it
		String doc = partXml(dst, "word/document.xml");
		assertTrue(doc, doc.contains("Requires=\"cx1\""));
		assertTrue(doc, doc.contains("xmlns:cx1=\"" + CX1 + "\""));
		assertTrue(partXml(dst, "word/embeddings/Microsoft_Excel_Worksheet.xlsx") != null);
		dst.save(office("waterfall-rehosted.docx"));
		sameChart(from, WordprocessingMLPackage.load(office("waterfall-rehosted.docx")));

		// deleted whole: the run, the chart's parts, the picture
		ChartCreator.delete(chartEx(dst));
		assertNull(chartEx(dst));
		assertNull(alternateContent(main.getContent()));
		assertNull(dst.getParts().get(image.getPartName()));
		assertNull(dst.getParts().get(new PartName("/word/charts/chartEx1.xml")));
		assertNull(dst.getParts().get(new PartName("/word/embeddings/Microsoft_Excel_Worksheet.xlsx")));
	}

	@Test
	public void aWaterfallPutOnANewSlide() throws Exception {
		PresentationMLPackage src = (PresentationMLPackage) load("waterfall.pptx");
		ChartRef from = chartEx(src);
		ChartExSpacePart sp = (ChartExSpacePart) from.getChartPart();
		SlidePart srcSlide = (SlidePart) from.getHost();

		PresentationMLPackage dst = PresentationMLPackage.createPackage();
		SlidePart slide = PresentationMLPackage.createSlidePart(dst.getMainPresentationPart(),
				(SlideLayoutPart) dst.getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml")), new PartName("/ppt/slides/slide1.xml"));
		ChartHosting hosting = new ChartHosting(new Random(39));
		ChartExSpacePart chart = copy(sp, new ChartExSpacePart(new PartName("/ppt/charts/chartEx1.xml")), dst);
		EmbeddedPackagePart wb = hosting.workbook(slide, ((EmbeddedPackagePart) related(sp, Namespaces.EMBEDDED_PKG)).getBytes());
		assertEquals("/ppt/embeddings/Microsoft_Excel_Worksheet.xlsx", wb.getPartName().getName());
		hosting.fill((SpreadsheetMLPackage) wb.getEmbeddedPackage(), grid(from.readChartEx()), null);
		chart.getContents().getChartData().getExternalData().setId(hosting.relate(chart, wb).getId());
		chart.addTargetPart(copy((JaxbXmlPart<?>) related(sp, Namespaces.CHART_STYLE), new ChartStylePart(new PartName("/ppt/charts/style1.xml")), dst));
		chart.addTargetPart(copy((JaxbXmlPart<?>) related(sp, Namespaces.CHART_COLOR_STYLE),
				new ChartColorStylePart(new PartName("/ppt/charts/colors1.xml")), dst));
		// the Fallback: PowerPoint's picture of the chart
		AlternateContent ac = null;
		for (Object o : srcSlide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()) {
			if (XmlUtils.unwrap(o) instanceof AlternateContent) ac = (AlternateContent) XmlUtils.unwrap(o);
		}
		String pic = xml(ac.getFallback().getAny().get(0), org.pptx4j.jaxb.Context.jcPML);
		String embed = pic.replaceAll("(?s).*:embed=\"([^\"]*)\".*", "$1");
		BinaryPartAbstractImage image = BinaryPartAbstractImage.createImagePart(dst, slide,
				((BinaryPartAbstractImage) srcSlide.getRelationshipsPart().getPart(embed)).getBytes());
		String fallback = pic.replace("embed=\"" + embed + "\"", "embed=\"" + image.getSourceRelationships().get(0).getId() + "\"");
		Object made = hosting.slideFrame(slide, chart, 1270000, 1270000, from.getWidth(), from.getHeight(), "cx1", fallback);
		assertTrue(made instanceof AlternateContent);

		String xml = partXml(dst, "ppt/slides/slide1.xml");
		assertTrue(xml, xml.contains("Requires=\"cx1\""));
		assertTrue(xml, xml.contains("xmlns:cx1=\"" + CX1 + "\""));
		dst.save(office("waterfall-rehosted.pptx"));
		sameChart(from, OpcPackage.load(office("waterfall-rehosted.pptx")));

		ChartCreator.delete(chartEx(dst));
		assertNull(chartEx(dst));
		assertNull(dst.getParts().get(image.getPartName()));
		assertNull(dst.getParts().get(new PartName("/ppt/charts/chartEx1.xml")));
	}

	@Test
	public void aWaterfallAnchoredAfreshInItsWorkbook() throws Exception {
		SpreadsheetMLPackage pkg = (SpreadsheetMLPackage) load("waterfall.xlsx");
		ChartRef from = chartEx(pkg);
		Drawing drawing = (Drawing) from.getHost();
		org.docx4j.dml.spreadsheetdrawing.CTDrawing wsDr = (org.docx4j.dml.spreadsheetdrawing.CTDrawing) drawing.getContents();
		// Excel's anchor taken out, its place and Fallback kept, the chart left in the package
		org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor anchor = null;
		for (Object o : wsDr.getEGAnchor()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor
					&& ((org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor) u).getAlternateContent() != null) {
				anchor = (org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor) u;
			}
		}
		String fallback = xml(anchor.getAlternateContent().getFallback().getAny().get(0), org.docx4j.jaxb.Context.jc);
		AreaRef at = new AreaRef(new CellRef(null, anchor.getFrom().getCol() + 1, anchor.getFrom().getRow() + 1),
				new CellRef(null, anchor.getTo().getCol(), anchor.getTo().getRow()));
		int before = wsDr.getEGAnchor().size();
		wsDr.getEGAnchor().remove(anchor);
		drawing.getRelationshipsPart().removeRelationship(drawing.getRelationshipsPart().getRelationshipByID(from.getRelationshipId()));

		WorksheetPart sheet = pkg.getWorkbookPart().getWorksheet(0);
		ChartRef placed = new ChartHosting(new Random(39)).sheetAnchor(sheet, from.getChartPart(), at, "cx1", fallback);
		assertTrue(placed.isChartEx());
		assertTrue(placed.isInChoice());
		assertEquals(before, wsDr.getEGAnchor().size());
		String xml = partXml(pkg, "xl/drawings/drawing1.xml");
		assertTrue(xml, xml.contains("Requires=\"cx1\""));
		assertTrue(xml, xml.contains("xmlns:cx1=\"" + CX1 + "\""));
		pkg.save(office("waterfall-rehosted.xlsx"));
		sameChart(from, OpcPackage.load(office("waterfall-rehosted.xlsx")));

		ChartCreator.delete(placed);
		assertEquals(before - 1, wsDr.getEGAnchor().size());
		assertNull(chartEx(pkg));
		// the classic chart beside it kept
		assertFalse(Charts.in(pkg).isEmpty());
	}

	@Test
	public void officesChartexChartsDeletedWhole() throws Exception {
		for (String name : new String[] { "waterfall.docx", "waterfall.pptx", "waterfall.xlsx" }) {
			OpcPackage pkg = load(name);
			ChartRef c = chartEx(pkg);
			Part chart = c.getChartPart();
			int charts = Charts.in(pkg).size();
			ChartCreator.delete(c);
			assertNull(name, chartEx(pkg));
			assertEquals(name, charts - 1, Charts.in(pkg).size());
			assertNull(name, pkg.getParts().get(chart.getPartName()));
			// what's left saves and loads
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pkg.save(out);
			assertNotNull(name, OpcPackage.load(new ByteArrayInputStream(out.toByteArray())));
		}
	}
}
