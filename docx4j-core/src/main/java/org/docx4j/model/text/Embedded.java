/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.text;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.docx4j.dml.CTTable;
import org.docx4j.dml.CTTableCell;
import org.docx4j.dml.CTTableRow;
import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.org.apache.poi.poifs.filesystem.Ole10Native;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Presentation;
import org.pptx4j.pml.Shape;
import org.pptx4j.pml.Sld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.org.apache.poi.ss.usermodel.BuiltinFormats;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.CTNumFmt;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.CTXf;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.STSheetState;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;

/**
 * An embedded object's content, read under {@code embedded: true} (WD REQ-512), one level deep: a
 * WordprocessingML document through the text model; a workbook as each visible sheet's name and
 * used range, a simple table of the cells as Excel displays them; a presentation's visible slides,
 * their shapes in {@code p:spTree} order, groups depth-first, tables as tables, speaker notes left
 * out; an OLE 1.0 package's plain-text file ({@code .txt}) as its lines. Nothing else is opened.
 * Hidden sheets and slides are read as hidden text is, under {@code hidden: true}.
 */
final class Embedded {

	private static final Logger log = LoggerFactory.getLogger(Embedded.class);

	private Embedded() {}

	/**
	 * An embedded OOXML package's blocks (an {@code EmbeddedPackagePart}'s bytes), or none for another
	 * kind.
	 *
	 * @throws Exception where the package, or a workbook or presentation in it, can't be read: then it
	 *         contributes nothing ({@code content.not_read})
	 */
	static List<TextBlock> pkg(Extractor x, Part part, byte[] bytes, String prefix) throws Exception {
		// an inspection's own reading of the package, where it made one (CR-050)
		OpcPackage opc = x.packages != null && x.packages.containsKey(part) ? x.packages.get(part) : OpcPackage.load(new ByteArrayInputStream(bytes));
		if (opc instanceof WordprocessingMLPackage) return x.nested((WordprocessingMLPackage) opc, prefix, TextBlock.Story.EMBEDDED);
		if (opc instanceof SpreadsheetMLPackage) return workbook(x, (SpreadsheetMLPackage) opc, prefix);
		if (opc instanceof PresentationMLPackage) return presentation(x, (PresentationMLPackage) opc, prefix);
		return new ArrayList<TextBlock>();
	}

	static TextBlock paragraph(TextBlock.Story story, String address, String text) {
		TextBlock b = new TextBlock(TextBlock.Kind.PARAGRAPH, story, address);
		if (text != null && !text.isEmpty()) b.runs.add(new TextRun(TextRun.Kind.TEXT, text, null));
		return b;
	}

	// --- a workbook ----------------------------------------------------------------------------------

	private static List<TextBlock> workbook(Extractor x, SpreadsheetMLPackage xl, String prefix) {
		List<TextBlock> out = new ArrayList<TextBlock>();
		WorkbookPart wb = xl.getWorkbookPart();
		Workbook w = wb.getJaxbElement();
		if (w.getSheets() == null) return out;
		boolean date1904 = w.getWorkbookPr() != null && w.getWorkbookPr().isDate1904();
		List<CTRst> strings = wb.getSharedStrings() == null ? new ArrayList<CTRst>() : wb.getSharedStrings().getJaxbElement().getSi();
		CTStylesheet styles = wb.getStylesPart() == null ? null : wb.getStylesPart().getJaxbElement();
		DataFormatter df = new DataFormatter(Locale.US);
		List<Sheet> sheets = w.getSheets().getSheet();
		for (int i = 0; i < sheets.size(); i++) {
			Sheet s = sheets.get(i);
			if (s.getState() != null && s.getState() != STSheetState.VISIBLE && !x.options.isHidden()) continue;
			Part part = s.getId() == null ? null : wb.getRelationshipsPart().getPart(s.getId());
			if (!(part instanceof WorksheetPart)) continue; // a chart sheet
			Worksheet ws = ((WorksheetPart) part).getJaxbElement();
			String address = prefix + "/sheet:" + i;
			out.add(paragraph(TextBlock.Story.EMBEDDED, address, s.getName()));
			TextBlock table = sheet(x, ws, strings, styles, df, date1904, address + "/table");
			if (table != null) out.add(table);
		}
		return out;
	}

	/** A sheet's used range, from the cells that hold values (not {@code dimension}), as a simple table; null for none. */
	private static TextBlock sheet(Extractor x, Worksheet ws, List<CTRst> strings, CTStylesheet styles, DataFormatter df, boolean date1904,
			String address) {
		if (ws == null || ws.getSheetData() == null) return null;
		TreeMap<Integer, TreeMap<Integer, String>> values = new TreeMap<Integer, TreeMap<Integer, String>>();
		TreeMap<Integer, TreeMap<Integer, Cell>> sources = new TreeMap<Integer, TreeMap<Integer, Cell>>();
		int minCol = Integer.MAX_VALUE, maxCol = -1;
		int rowIndex = 0;
		for (Row row : ws.getSheetData().getRow()) {
			rowIndex = row.getR() != null ? row.getR().intValue() : rowIndex + 1;
			int colIndex = 0;
			for (Cell c : row.getC()) {
				int[] ref = c.getR() == null ? null : ref(c.getR());
				colIndex = ref != null ? ref[0] : colIndex + 1;
				int r = ref != null ? ref[1] : rowIndex;
				String v = display(c, strings, styles, df, date1904);
				if (v == null || v.isEmpty()) continue;
				values.computeIfAbsent(r, k -> new TreeMap<Integer, String>()).put(colIndex, v);
				sources.computeIfAbsent(r, k -> new TreeMap<Integer, Cell>()).put(colIndex, c);
				minCol = Math.min(minCol, colIndex);
				maxCol = Math.max(maxCol, colIndex);
			}
		}
		if (values.isEmpty()) return null;
		TextBlock table = new TextBlock(TextBlock.Kind.TABLE, TextBlock.Story.EMBEDDED, address);
		table.simpleForm = true;
		for (int r = values.firstKey(); r <= values.lastKey(); r++) {
			TextRow row = new TextRow(address + "/" + (r - values.firstKey()));
			TreeMap<Integer, String> cells = values.get(r);
			for (int c = minCol; c <= maxCol; c++) {
				TextCell cell = new TextCell(row.address + "/" + (c - minCol));
				cell.blocks.add(paragraph(TextBlock.Story.EMBEDDED, cell.address + "/0", cells == null ? null : cells.get(c)));
				if (cells != null && cells.containsKey(c)) x.record(sources.get(r).get(c), cell.address + "/0");
				row.cells.add(cell);
			}
			table.rows.add(row);
		}
		return table;
	}

	/** A cell reference's column and row, from 1 ({@code B3} gives 2, 3); null if it isn't one. */
	static int[] ref(String r) {
		int col = 0, i = 0;
		while (i < r.length() && Character.isLetter(r.charAt(i))) {
			col = col * 26 + (Character.toUpperCase(r.charAt(i)) - 'A' + 1);
			i++;
		}
		if (i == 0 || i == r.length()) return null;
		try {
			return new int[] { col, Integer.parseInt(r.substring(i)) };
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** A cell as Excel displays it: its cached value through its number format; a formula's cached result, never recomputed. */
	static String display(Cell c, List<CTRst> strings, CTStylesheet styles, DataFormatter df, boolean date1904) {
		STCellType t = c.getT();
		if (t == STCellType.INLINE_STR) return text(c.getIs());
		String v = c.getV();
		if (v == null) return null;
		if (t == STCellType.S) {
			try {
				int i = Integer.parseInt(v.trim());
				return i >= 0 && i < strings.size() ? text(strings.get(i)) : null;
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (t == STCellType.B) return "1".equals(v.trim()) || "true".equalsIgnoreCase(v.trim()) ? "TRUE" : "FALSE";
		if (t == STCellType.STR || t == STCellType.E) return v;
		// a number
		double d;
		try {
			d = Double.parseDouble(v);
		} catch (NumberFormatException e) {
			return v;
		}
		int id = 0;
		String format = null;
		if (styles != null && styles.getCellXfs() != null && c.getS() >= 0 && c.getS() < styles.getCellXfs().getXf().size()) {
			CTXf xf = styles.getCellXfs().getXf().get((int) c.getS());
			if (xf.getNumFmtId() != null) id = xf.getNumFmtId().intValue();
		}
		if (styles != null && styles.getNumFmts() != null) {
			for (CTNumFmt f : styles.getNumFmts().getNumFmt()) if (f.getNumFmtId() == id) format = f.getFormatCode();
		}
		if (format == null) format = BuiltinFormats.getBuiltinFormat(id);
		if (format == null) format = "General";
		try {
			return df.formatRawCellContents(d, id, format, date1904);
		} catch (RuntimeException e) {
			return v;
		}
	}

	private static String text(CTRst rst) {
		if (rst == null) return null;
		if (rst.getT() != null && rst.getT().getValue() != null) return rst.getT().getValue();
		StringBuilder sb = new StringBuilder();
		for (CTRElt r : rst.getR()) if (r.getT() != null && r.getT().getValue() != null) sb.append(r.getT().getValue());
		return sb.toString();
	}

	// --- a presentation ------------------------------------------------------------------------------

	private static List<TextBlock> presentation(Extractor x, PresentationMLPackage pp, String prefix) {
		List<TextBlock> out = new ArrayList<TextBlock>();
		MainPresentationPart main = pp.getMainPresentationPart();
		Presentation p = main.getJaxbElement();
		if (p.getSldIdLst() == null) return out;
		List<Presentation.SldIdLst.SldId> ids = p.getSldIdLst().getSldId();
		for (int i = 0; i < ids.size(); i++) {
			Part part = main.getRelationshipsPart().getPart(ids.get(i).getRid());
			if (!(part instanceof SlidePart)) continue;
			Sld sld = ((SlidePart) part).getJaxbElement();
			if (sld == null || !sld.isShow() && !x.options.isHidden()) continue;
			if (sld.getCSld() == null || sld.getCSld().getSpTree() == null) continue;
			shapes(x, sld.getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(), prefix + "/slide:" + i, out, new int[1]);
		}
		return out;
	}

	/** A shape tree's text in {@code p:spTree} order, group shapes depth-first. */
	private static void shapes(Extractor x, List<Object> items, String prefix, List<TextBlock> out, int[] n) {
		for (Object o : items) {
			Object v = Extractor.unwrap(o);
			if (v instanceof Shape) {
				for (String line : Drawings.lines(((Shape) v).getTxBody())) {
					x.record(v, prefix + "/" + n[0]);
					out.add(paragraph(TextBlock.Story.EMBEDDED, prefix + "/" + n[0]++, line));
				}
			} else if (v instanceof GroupShape) {
				shapes(x, ((GroupShape) v).getSpOrGrpSpOrGraphicFrame(), prefix, out, n);
			} else if (v instanceof CTGraphicalObjectFrame) {
				CTGraphicalObjectFrame f = (CTGraphicalObjectFrame) v;
				if (f.getGraphic() == null || f.getGraphic().getGraphicData() == null) continue;
				for (Object g : f.getGraphic().getGraphicData().getAny()) {
					Object gv = Extractor.unwrap(g);
					if (gv instanceof CTTable) out.add(table((CTTable) gv, prefix + "/" + n[0]++));
				}
			} else if (v instanceof AlternateContent) {
				shapes(x, McSelection.select(McPurpose.TEXT, (AlternateContent) v), prefix, out, n);
			}
		}
	}

	/** A DrawingML table, its cells' paragraphs as blocks; a cell a horizontal merge covers is left out. */
	private static TextBlock table(CTTable tbl, String address) {
		TextBlock table = new TextBlock(TextBlock.Kind.TABLE, TextBlock.Story.EMBEDDED, address);
		for (int r = 0; r < tbl.getTr().size(); r++) {
			CTTableRow tr = tbl.getTr().get(r);
			TextRow row = new TextRow(address + "/" + r);
			for (int c = 0; c < tr.getTc().size(); c++) {
				CTTableCell tc = tr.getTc().get(c);
				if (tc.isHMerge()) continue;
				TextCell cell = new TextCell(row.address + "/" + c);
				cell.gridSpan = Math.max(1, tc.getGridSpan());
				cell.mergeContinue = tc.isVMerge();
				cell.mergeRestart = tc.getRowSpan() > 1;
				List<String> lines = new ArrayList<String>();
				if (tc.getTxBody() != null) for (org.docx4j.dml.CTTextParagraph p : tc.getTxBody().getP()) lines.add(Drawings.text(p));
				if (lines.isEmpty()) lines.add("");
				for (int i = 0; i < lines.size(); i++) cell.blocks.add(paragraph(TextBlock.Story.EMBEDDED, cell.address + "/" + i, lines.get(i)));
				row.cells.add(cell);
			}
			table.rows.add(row);
		}
		return table;
	}

	// --- an OLE 1.0 package ----------------------------------------------------------------------------

	/** An OLE 1.0 package's file when it is plain text ({@code .txt}), a line a paragraph; else none, and nothing else is opened. */
	static List<TextBlock> ole10Native(byte[] bytes, String prefix) {
		List<TextBlock> out = new ArrayList<TextBlock>();
		try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(bytes))) {
			Ole10Native n = Ole10Native.createFromEmbeddedOleObject(fs);
			String name = n.getFileName() != null && !n.getFileName().isEmpty() ? n.getFileName() : n.getLabel();
			if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".txt")) return out;
			String text = decode(n.getDataBuffer());
			String[] lines = text.split("\r\n|\r|\n", -1);
			int count = lines.length;
			if (count > 0 && lines[count - 1].isEmpty()) count--; // the file's last line break
			for (int i = 0; i < count; i++) out.add(paragraph(TextBlock.Story.EMBEDDED, prefix + "/" + i, lines[i]));
		} catch (Exception e) {
			log.debug("No plain-text OLE 1.0 package at " + prefix + ": " + e.getMessage());
		}
		return out;
	}

	/** A text file's bytes: by its byte-order mark, else UTF-8 if they are, else windows-1252. */
	static String decode(byte[] b) {
		if (b == null) return "";
		if (b.length >= 3 && (b[0] & 0xff) == 0xef && (b[1] & 0xff) == 0xbb && (b[2] & 0xff) == 0xbf) {
			return new String(b, 3, b.length - 3, StandardCharsets.UTF_8);
		}
		if (b.length >= 2 && (b[0] & 0xff) == 0xff && (b[1] & 0xff) == 0xfe) return new String(b, 2, b.length - 2, StandardCharsets.UTF_16LE);
		if (b.length >= 2 && (b[0] & 0xff) == 0xfe && (b[1] & 0xff) == 0xff) return new String(b, 2, b.length - 2, StandardCharsets.UTF_16BE);
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b)).toString();
		} catch (CharacterCodingException e) {
			return new String(b, Charset.forName("windows-1252"));
		}
	}
}
