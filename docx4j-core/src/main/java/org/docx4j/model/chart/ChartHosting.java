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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.jaxb.Context;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.ChartExSpacePart;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.R;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2014.revision.CTRevisionPtr;
import org.xlsx4j.model.AreaRef;
import org.xlsx4j.model.CellRef;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.sml.CTBookView;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;

/**
 * The steps that put a chart in its host as Office's <i>Insert Chart</i> does, whatever the chart
 * (CR-039: {@link ChartCreator}'s, opened so that a creator of other charts, a chartex creator,
 * can put its own there): a workbook embedded with the chart in a docx or a pptx, its data a grid
 * at A1 in its table; the chart's frame in each host, a docx's run, a slide's graphic frame, a
 * sheet's two-cell anchor, and where the chart is a chartex chart, the frame as the
 * {@code mc:Choice} of an {@code mc:AlternateContent} whose Fallback the caller gives (Word and
 * PowerPoint write a picture of the chart, Excel a sentence); the parts' names; Office's ids.
 *
 * <p>Office's ids are random; here they come from the {@link Random} given, so that a seeded one
 * makes the same file each time (as {@link ChartCreator}'s).</p>
 *
 * @since 17.3.3 (CR-039)
 */
public final class ChartHosting {

	/** The graphic data's {@code uri} for a classic chart's frame ({@code c:chart}) */
	public static final String CHART = "http://schemas.openxmlformats.org/drawingml/2006/chart";

	/** The graphic data's {@code uri} for a chartex chart's frame ({@code cx:chart}) */
	public static final String CHARTEX = "http://schemas.microsoft.com/office/drawing/2014/chartex";

	private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String MC_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";
	private static final Pattern SHAPE_ID = Pattern.compile("<p:cNvPr[^>]* id=\"(\\d+)\"");

	private final Random ids;

	/** Ids from this source: a seeded one makes the same file each time */
	public ChartHosting(Random ids) {
		if (ids == null) throw new IllegalArgumentException("no source of ids");
		this.ids = ids;
	}

	// ------------------------------------------------------------------ ids and names

	/** Word's anchor and edit ids: eight hex digits, the top bit clear */
	public String wordId() {
		return String.format("%08X", ids.nextInt() & 0x7FFFFFFF);
	}

	/** A GUID in braces, as Office writes its ids ({@code a16:creationId}, {@code xr:uid}) */
	public String guid() {
		return guid(ids);
	}

	static String guid(Random ids) {
		return "{" + new UUID(ids.nextLong(), ids.nextLong()).toString().toUpperCase(Locale.ROOT) + "}";
	}

	/** The lowest number n for which prefix n suffix names no part */
	public static int next(OpcPackage pkg, String prefix, String suffix) throws InvalidFormatException {
		int n = 1;
		while (pkg.getParts().get(new PartName(prefix + n + suffix)) != null) n++;
		return n;
	}

	/** prefix n .xml where it names no part, else the next free number's */
	public static String free(OpcPackage pkg, String prefix, int n) throws InvalidFormatException {
		if (pkg.getParts().get(new PartName(prefix + n + ".xml")) == null) return prefix + n + ".xml";
		return prefix + next(pkg, prefix, ".xml") + ".xml";
	}

	/** Word's name for an embedded workbook: the base, then the base 1, 2 ... where it is taken */
	static String embedding(OpcPackage pkg, String base) throws InvalidFormatException {
		if (pkg.getParts().get(new PartName(base + ".xlsx")) == null) return base + ".xlsx";
		return base + next(pkg, base, ".xlsx") + ".xlsx";
	}

	// ------------------------------------------------------------------ the embedded workbook

	/**
	 * A workbook for a chart in a docx or on a slide, as Word and PowerPoint embed one: named in
	 * the host's embeddings ({@code /word/embeddings/} or {@code /ppt/embeddings/}
	 * {@code Microsoft_Excel_Worksheet.xlsx}, then 1, 2, ...), the template's bytes; not yet
	 * related ({@link #relate})
	 *
	 * @param template an xlsx: the type's workbook as Office embeds it
	 */
	public EmbeddedPackagePart workbook(Part host, byte[] template) throws Docx4JException {
		OpcPackage pkg = host.getPackage();
		String dir = host.getPartName().getName().startsWith("/ppt/") ? "/ppt/" : "/word/";
		EmbeddedPackagePart part;
		try {
			part = new EmbeddedPackagePart(new PartName(embedding(pkg, dir + "embeddings/Microsoft_Excel_Worksheet")));
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		part.setContentType(new ContentType(XLSX));
		part.setBinaryData(template);
		return part;
	}

	/**
	 * The workbook related from the chart (after its style and colours, as Office's rId3), typed
	 * by the {@code xlsx} default as Word and PowerPoint type it
	 *
	 * @return the relationship, for the chart's {@code c:externalData} or {@code cx:externalData}
	 */
	public Relationship relate(Part chart, EmbeddedPackagePart workbook) throws Docx4JException {
		Relationship r;
		try {
			r = chart.addTargetPart(workbook);
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		OpcPackage pkg = chart.getPackage();
		pkg.getContentTypeManager().removeOverrideContentType(workbook.getPartName());
		pkg.getContentTypeManager().addDefaultContentType("xlsx", XLSX);
		return r;
	}

	/**
	 * An embedded workbook's data made a grid's (Word's and PowerPoint's own chart data, a
	 * template's emptied): its first sheet's cells and the shared strings cleared, the grid
	 * written at A1, the sheet's table (the template's, where it has one) set over it; Office's random ids made
	 * afresh; its author and times docx4j.properties' (the template's are its maker's)
	 *
	 * @param grid rows of cells, each a String, a Number, a {@link LocalDate} (a serial number in
	 *     dateFormat) or null (left empty); a treemap's hierarchy is several columns of text
	 */
	public void fill(SpreadsheetMLPackage workbook, List<List<Object>> grid, String dateFormat) throws Docx4JException {
		WorkbookPart book = workbook.getWorkbookPart();
		String name = book.getContents().getSheets().getSheet().get(0).getName();
		WorksheetPart sheet = SheetCells.of(workbook, name).getPart();
		sheet.getContents().getSheetData().getRow().clear();
		if (book.getSharedStrings() != null) book.getSharedStrings().getContents().getSi().clear();
		SheetCells sc = new SheetCells(workbook, sheet, name);
		write(sc, grid, 1, 1, dateFormat);
		// a classic chart's workbook has a table over its data (Word's Insert Chart's); a chartex
		// chart's has none
		List<TablePart> tables = Tables.of(sheet);
		TablePart table = tables.isEmpty() ? null : tables.get(0);
		if (table != null) {
			int columns = 0;
			for (List<Object> row : grid) columns = Math.max(columns, row.size());
			Tables.setRef(table, new AreaRef(new CellRef(null, 1, 1), new CellRef(null, columns, grid.size())), sc);
		}
		sc.tidy();

		sheet.getContents().setUid(guid());
		if (table != null) {
			CTTable t = table.getContents();
			t.setUid(guid());
			if (t.getAutoFilter() != null) t.getAutoFilter().setUid(t.getUid());
			for (CTTableColumn c : t.getTableColumns().getTableColumn()) c.setUid(guid());
		}
		Workbook w = book.getContents();
		if (w.getBookViews() != null) for (CTBookView v : w.getBookViews().getWorkbookView()) if (v.getUid() != null) v.setUid(guid());
		CTRevisionPtr rp = w.getRevisionPtr();
		if (rp != null && rp.getDocumentId() != null) {
			rp.setDocumentId(rp.getDocumentId().replaceFirst("\\{[^}]*\\}", Matcher.quoteReplacement(guid())));
		}
		if (workbook.getDocPropsCorePart() != null) {
			// the template's are the probe's: the author docx4j.properties names for a new document
			// (docx4j.dc.creator.value, where docx4j.dc.write), else none; the current time where
			// docx4j.dcterms.write asks for it, else none (lastModifiedBy, and the time modified, as
			// docx4j's save writes them)
			CoreProperties cp = workbook.getDocPropsCorePart().getContents();
			String creator = Boolean.parseBoolean(Docx4jProperties.getProperty("docx4j.dc.write", "false"))
					? Docx4jProperties.getProperty("docx4j.dc.creator.value") : null;
			SimpleLiteral author = null;
			if (creator != null) {
				author = new SimpleLiteral();
				author.getContent().add(creator);
			}
			cp.setCreator(author);
			cp.setLastModifiedBy(null);
			SimpleLiteral now = DocPropsCorePart.isTimesWritten() ? DocPropsCorePart.now() : null;
			cp.setCreated(now);
			cp.setModified(now == null ? null : (SimpleLiteral) now.copy());
		}
	}

	/**
	 * A grid written at (c0, r0), row by row, as Excel lays out a chart's data: text as shared
	 * strings, numbers, dates as serial numbers in their format; each row's spans as Excel's
	 */
	public static void write(SheetCells sc, List<List<Object>> grid, int c0, int r0, String dateFormat) throws Docx4JException {
		boolean date1904 = sc.getWorkbook().getWorkbookPart().isDate1904();
		for (int r = 0; r < grid.size(); r++) {
			List<Object> row = grid.get(r);
			for (int c = 0; c < row.size(); c++) {
				Object o = row.get(c);
				if (o == null) continue;
				if (o instanceof String) {
					sc.setString(c0 + c, r0 + r, (String) o);
				} else if (o instanceof Number) {
					sc.setNumber(c0 + c, r0 + r, ((Number) o).doubleValue());
				} else if (o instanceof LocalDate) {
					sc.setNumber(c0 + c, r0 + r, ChartCreator.serial((LocalDate) o, date1904));
					sc.setNumberFormat(c0 + c, r0 + r, dateFormat);
				} else {
					throw new IllegalArgumentException("a cell of a chart's data is text, a number or a date, not " + o.getClass().getName());
				}
			}
		}
		// as Excel's rows: their spans, which tidy() keeps once a row has them
		for (int r = r0; r < r0 + grid.size(); r++) {
			Row row = sc.getRow(r);
			if (row != null && row.getSpans().isEmpty()) row.getSpans().add(c0 + ":" + c0);
		}
	}

	// ------------------------------------------------------------------ the frames

	/** The graphic data's uri for a chart part: a chartex chart's or a classic chart's */
	static String uri(Part chart) {
		return chart instanceof ChartExSpacePart ? CHARTEX : CHART;
	}

	/** The graphic data's chart element: a {@code c:chart} or a {@code cx:chart} naming the relationship */
	private static String chartElement(Part chart, String rId) {
		return chart instanceof ChartExSpacePart
				? "<cx:chart xmlns:cx=\"" + CHARTEX + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + rId + "\"/>"
				: "<c:chart xmlns:c=\"" + CHART + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + rId + "\"/>";
	}

	/**
	 * The chart related from a docx part (its main document, a header, ...) and placed in a run
	 * holding its inline drawing, cx by cy EMUs, as Word writes a chart's: its {@code docPr} id the
	 * package's next, named "Chart n" for the part's n charts, its {@code wp14} anchor and edit
	 * ids Word's.  Where requires is given (a chartex chart's: {@code cx1}, ...), the drawing is the
	 * Choice of an {@code mc:AlternateContent} requiring it, and the Fallback's content is the XML
	 * given (Word's is a {@code w:drawing} of a picture of the chart; it may use the prefixes
	 * {@code w}, {@code wp}, {@code wp14}, {@code a}, {@code pic} and {@code r}), its inline given
	 * the chart's extent and ids, as Word gives them.  The run is the caller's to put in a paragraph.
	 */
	public R docxRun(JaxbXmlPart<?> host, Part chart, long cx, long cy, String requires, String fallback) throws Docx4JException {
		Relationship r;
		try {
			r = host.addTargetPart(chart);
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		int n = 0;
		for (Relationship x : host.getRelationshipsPart().getRelationships().getRelationship()) {
			if (Namespaces.SPREADSHEETML_CHART.equals(x.getType()) || Namespaces.CHART_EX.equals(x.getType())) n++;
		}
		long docPr = ((org.docx4j.openpackaging.packages.WordprocessingMLPackage) host.getPackage()).getDrawingPropsIdTracker()
				.generateRandomId(ids);
		String anchorId = wordId(), editId = wordId();
		String drawing = "<w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" "
				+ "wp14:anchorId=\"" + anchorId + "\" wp14:editId=\"" + editId + "\">"
				+ "<wp:extent cx=\"" + cx + "\" cy=\"" + cy + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ "<wp:docPr id=\"" + docPr + "\" name=\"Chart " + n + "\"/><wp:cNvGraphicFramePr/>"
				+ "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<a:graphicData uri=\"" + uri(chart) + "\">" + chartElement(chart, r.getId())
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing>";
		String xml = "<w:r xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
				+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
				+ "xmlns:wp14=\"http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing\""
				+ (requires == null ? ""
						: " xmlns:mc=\"" + MC_NS + "\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
								+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
								+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"")
				+ "><w:rPr><w:noProof/></w:rPr>"
				+ (requires == null ? drawing
						: "<mc:AlternateContent><mc:Choice Requires=\"" + requires + "\">" + drawing + "</mc:Choice><mc:Fallback>"
								+ (fallback == null ? "" : fallback) + "</mc:Fallback></mc:AlternateContent>")
				+ "</w:r>";
		R run;
		try {
			run = (R) XmlUtils.unmarshalString(xml, Context.jc, R.class);
		} catch (jakarta.xml.bind.JAXBException e) {
			throw new Docx4JException("the chart's drawing couldn't be made", e);
		}
		if (requires != null) {
			host.addMcChoiceNamespace(requires);
			// the Fallback's drawing in the chart's place, with its ids, as Word writes it
			for (Object o : run.getContent()) {
				Object u = XmlUtils.unwrap(o);
				if (!(u instanceof AlternateContent) || ((AlternateContent) u).getFallback() == null) continue;
				for (Object f : ((AlternateContent) u).getFallback().getAny()) {
					Object fu = XmlUtils.unwrap(f);
					if (!(fu instanceof org.docx4j.wml.Drawing)) continue;
					for (Object a : ((org.docx4j.wml.Drawing) fu).getAnchorOrInline()) {
						if (!(a instanceof org.docx4j.dml.wordprocessingDrawing.Inline)) continue;
						org.docx4j.dml.wordprocessingDrawing.Inline in = (org.docx4j.dml.wordprocessingDrawing.Inline) a;
						if (in.getExtent() != null) {
							in.getExtent().setCx(cx);
							in.getExtent().setCy(cy);
						}
						if (in.getDocPr() != null) {
							in.getDocPr().setId(docPr);
							in.getDocPr().setName("Chart " + n);
						}
						in.setAnchorId(hex(anchorId));
						in.setEditId(hex(editId));
					}
				}
			}
		}
		return run;
	}

	private static byte[] hex(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
		return b;
	}

	/**
	 * The chart related from a slide and placed in a graphic frame at x, y of cx by cy EMUs, added
	 * to the slide's shape tree, as PowerPoint's <i>Insert Chart</i> places one: its id the next
	 * the slide's shapes leave, named for it, its {@code a16:creationId} and {@code p14:modId}.
	 * Where requires is given (a chartex chart's), the frame is the Choice of an
	 * {@code mc:AlternateContent} requiring it, added in its place, the Fallback's content the XML
	 * given (PowerPoint's is a {@code p:pic} of a picture of the chart, its ids the caller's; it may
	 * use the prefixes {@code p}, {@code a} and {@code r}).
	 *
	 * @return the frame (a {@link org.pptx4j.pml.CTGraphicalObjectFrame}), or the
	 *     {@code mc:AlternateContent} holding it
	 */
	public Object slideFrame(SlidePart slide, Part chart, long x, long y, long cx, long cy, String requires, String fallback)
			throws Docx4JException {
		Relationship r;
		try {
			r = slide.addTargetPart(chart);
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		// the next id the slide's shapes leave, as PowerPoint numbers a new shape; named for it
		long id = 0;
		Matcher m = SHAPE_ID.matcher(XmlUtils.marshaltoString(slide.getJaxbElement(), true, false, org.pptx4j.jaxb.Context.jcPML));
		while (m.find()) id = Math.max(id, Long.parseLong(m.group(1)));
		id++;
		String frame = "<p:graphicFrame xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" "
				+ "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<p:nvGraphicFramePr><p:cNvPr id=\"" + id + "\" name=\"Chart " + (id - 1) + "\"><a:extLst>"
				+ "<a:ext uri=\"{FF2B5EF4-FFF2-40B4-BE49-F238E27FC236}\"><a16:creationId "
				+ "xmlns:a16=\"http://schemas.microsoft.com/office/drawing/2014/main\" id=\"" + guid() + "\"/></a:ext>"
				+ "</a:extLst></p:cNvPr><p:cNvGraphicFramePr/><p:nvPr><p:extLst>"
				+ "<p:ext uri=\"{D42A27DB-BD31-4B8C-83A1-F6EECF244321}\"><p14:modId "
				+ "xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\" val=\"" + (ids.nextInt() & 0xFFFFFFFFL)
				+ "\"/></p:ext></p:extLst></p:nvPr></p:nvGraphicFramePr>"
				+ "<p:xfrm><a:off x=\"" + x + "\" y=\"" + y + "\"/><a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/></p:xfrm>"
				+ "<a:graphic><a:graphicData uri=\"" + uri(chart) + "\">" + chartElement(chart, r.getId())
				+ "</a:graphicData></a:graphic></p:graphicFrame>";
		Object made;
		try {
			if (requires == null) {
				made = XmlUtils.unmarshalString(frame, org.pptx4j.jaxb.Context.jcPML, org.pptx4j.pml.CTGraphicalObjectFrame.class);
			} else {
				made = XmlUtils.unwrap(XmlUtils.unmarshalString("<mc:AlternateContent xmlns:mc=\"" + MC_NS + "\" "
						+ "xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" "
						+ "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
						+ "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
						+ "<mc:Choice Requires=\"" + requires + "\">" + frame + "</mc:Choice><mc:Fallback>" + (fallback == null ? "" : fallback)
						+ "</mc:Fallback></mc:AlternateContent>", org.pptx4j.jaxb.Context.jcPML));
				slide.addMcChoiceNamespace(requires);
			}
		} catch (jakarta.xml.bind.JAXBException e) {
			throw new Docx4JException("the chart's frame couldn't be made", e);
		}
		slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(made);
		return made;
	}

	/**
	 * The chart in the sheet's drawing (made where it has none), a two-cell anchor over the area,
	 * as Excel places a chart: its frame's id the next the drawing's shapes leave, named for it,
	 * its {@code a16:creationId}.  Where requires is given (a chartex chart's), the frame is the
	 * Choice of an {@code mc:AlternateContent} requiring it, the Fallback's content the XML given
	 * (Excel's is an {@code xdr:sp} saying the chart isn't available in this version; it may use the
	 * prefixes {@code xdr}, {@code a} and {@code r}).
	 */
	public ChartRef sheetAnchor(WorksheetPart sheet, Part chart, AreaRef at, String requires, String fallback) throws Docx4JException {
		OpcPackage pkg = sheet.getPackage();
		Worksheet ws = sheet.getContents();
		Drawing drawing = null;
		if (ws.getDrawing() != null && sheet.getRelationshipsPart() != null) {
			Part p = sheet.getRelationshipsPart().getPart(ws.getDrawing().getId());
			if (p instanceof Drawing) drawing = (Drawing) p;
		}
		try {
			if (drawing == null) {
				drawing = new Drawing(new PartName("/xl/drawings/drawing" + next(pkg, "/xl/drawings/drawing", ".xml") + ".xml"));
				drawing.setJaxbElement(new CTDrawing());
				Relationship r = sheet.addTargetPart(drawing);
				org.xlsx4j.sml.CTDrawing d = new org.xlsx4j.sml.CTDrawing();
				d.setId(r.getId());
				d.setParent(ws);
				ws.setDrawing(d);
			}
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		Relationship r = drawing.addTargetPart(chart);
		CTDrawing wsDr = (CTDrawing) drawing.getContents();
		long id = 1;
		for (Object a : wsDr.getEGAnchor()) id = Math.max(id, frameId(XmlUtils.unwrap(a)));
		id++;
		String frame = "<xdr:graphicFrame macro=\"\"><xdr:nvGraphicFramePr><xdr:cNvPr id=\"" + id + "\" name=\"Chart " + (id - 1) + "\">"
				+ "<a:extLst><a:ext uri=\"{FF2B5EF4-FFF2-40B4-BE49-F238E27FC236}\"><a16:creationId "
				+ "xmlns:a16=\"http://schemas.microsoft.com/office/drawing/2014/main\" id=\"" + guid() + "\"/></a:ext></a:extLst>"
				+ "</xdr:cNvPr><xdr:cNvGraphicFramePr/></xdr:nvGraphicFramePr>"
				+ "<xdr:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/></xdr:xfrm>"
				+ "<a:graphic><a:graphicData uri=\"" + uri(chart) + "\">" + chartElement(chart, r.getId())
				+ "</a:graphicData></a:graphic></xdr:graphicFrame>";
		String xml = "<xdr:wsDr xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" "
				+ "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ (requires == null ? "" : " xmlns:mc=\"" + MC_NS + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"")
				+ ">"
				+ "<xdr:twoCellAnchor><xdr:from><xdr:col>" + (at.getFirst().getColumn() - 1) + "</xdr:col><xdr:colOff>0</xdr:colOff>"
				+ "<xdr:row>" + (at.getFirst().getRow() - 1) + "</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:from>"
				+ "<xdr:to><xdr:col>" + at.getLast().getColumn() + "</xdr:col><xdr:colOff>0</xdr:colOff>"
				+ "<xdr:row>" + at.getLast().getRow() + "</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:to>"
				+ (requires == null ? frame
						: "<mc:AlternateContent><mc:Choice Requires=\"" + requires + "\">" + frame + "</mc:Choice><mc:Fallback>"
								+ (fallback == null ? "" : fallback) + "</mc:Fallback></mc:AlternateContent>")
				+ "<xdr:clientData/></xdr:twoCellAnchor></xdr:wsDr>";
		CTDrawing parsed;
		try {
			parsed = (CTDrawing) XmlUtils.unwrap(XmlUtils.unmarshalString(xml, Context.jc));
		} catch (jakarta.xml.bind.JAXBException e) {
			throw new Docx4JException("the chart's anchor couldn't be made", e);
		}
		Object anchor = parsed.getEGAnchor().get(0);
		wsDr.getEGAnchor().add(anchor);
		ChartSettings.invoke(XmlUtils.unwrap(anchor), "setParent", Object.class, wsDr);
		if (requires == null) return ChartRef.of(drawing, anchor);
		drawing.addMcChoiceNamespace(requires);
		AlternateContent ac = ((org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor) XmlUtils.unwrap(anchor)).getAlternateContent();
		return ChartRef.of(drawing, ac.getChoice().get(0).getAny().get(0), true);
	}

	/** A drawing's anchor's frame's id (its shape's, its picture's, ...; a Choice's frame's): 0 for none */
	static long frameId(Object anchor) {
		Object f = ChartSeries.get(anchor, "getGraphicFrame");
		Object nv = f == null ? null : ChartSeries.get(f, "getNvGraphicFramePr");
		Object c = nv == null ? null : ChartSeries.get(nv, "getCNvPr");
		for (String kind : new String[] { "getSp", "getPic", "getCxnSp", "getGrpSp" }) {
			if (c != null) break;
			Object o = ChartSeries.get(anchor, kind);
			if (o == null) continue;
			for (String nvGetter : new String[] { "getNvSpPr", "getNvPicPr", "getNvCxnSpPr", "getNvGrpSpPr" }) {
				Object n = ChartSeries.get(o, nvGetter);
				if (n != null) c = ChartSeries.get(n, "getCNvPr");
			}
		}
		Object id = c == null ? null : ChartSeries.get(c, "getId");
		long found = id instanceof Number ? ((Number) id).longValue() : 0;
		// a chartex chart's frame, in a Choice (as JAXB, or as DOM where docx4j left it unread)
		Object ac = ChartSeries.get(anchor, "getAlternateContent");
		if (ac instanceof AlternateContent) {
			for (AlternateContent.Choice choice : ((AlternateContent) ac).getChoice()) {
				for (Object o : choice.getAny()) {
					Object u = XmlUtils.unwrap(o);
					if (u instanceof org.w3c.dom.Element) {
						org.w3c.dom.NodeList l = ((org.w3c.dom.Element) u).getElementsByTagNameNS("*", "cNvPr");
						if (l.getLength() > 0) {
							try {
								found = Math.max(found, Long.parseLong(((org.w3c.dom.Element) l.item(0)).getAttribute("id")));
							} catch (NumberFormatException e) {
								// none
							}
						}
					} else {
						Object fnv = ChartSeries.get(u, "getNvGraphicFramePr");
						Object fc = fnv == null ? null : ChartSeries.get(fnv, "getCNvPr");
						Object fid = fc == null ? null : ChartSeries.get(fc, "getId");
						if (fid instanceof Number) found = Math.max(found, ((Number) fid).longValue());
					}
				}
			}
		}
		return found;
	}
}
