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
package org.xlsx4j.samples;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.ControlPropertiesPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTCfRule;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTCfvo;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTConditionalFormatting;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTConditionalFormattings;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataBar;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidation;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidationFormula;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidations;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTFormControlPr;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTIconSet;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparkline;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklineGroup;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklineGroups;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklines;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.STCfvoType;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.STDispBlanksAs;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.STObjectType;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.STSparklineType;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.schemas.microsoft.com.office.excel_2006.main.CTSqref;
import org.xlsx4j.sml.CTColor;
import org.xlsx4j.sml.CTControl;
import org.xlsx4j.sml.CTControlPr;
import org.xlsx4j.sml.CTExtension;
import org.xlsx4j.sml.CTExtensionList;
import org.xlsx4j.sml.CTLegacyDrawing;
import org.xlsx4j.sml.CTObjectAnchor;
import org.xlsx4j.sml.CTControls;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.ObjectFactory;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCfType;
import org.xlsx4j.sml.STDataValidationType;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

/**
 * Writes four workbooks that exercise the Excel 2010 extensions docx4j binds since
 * 17.2.0 (CR-022): sparklines, x14 conditional formatting (a data bar and an icon
 * set), a list data validation whose source is on another sheet (which Excel writes
 * only in its x14 form), and a check box form control (a control-properties part,
 * the worksheet's controls inside the mc:AlternateContent Excel wraps them in, and
 * a legacy VML drawing for the shape).
 *
 * The extension content lives in each worksheet's extLst, as an ext with the uri
 * Excel uses for that content; the x14 elements are typed (packages
 * org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main and
 * org.xlsx4j.schemas.microsoft.com.office.excel_2006.main for xm:f and xm:sqref).
 *
 * Usage: Excel2010ExtensionsSamples [outputDir]  (default: the working directory).
 * These are the source of docx4j-core-tests' cr022-*.xlsx resources, after a round
 * through Excel 365 (opened, re-saved) so the committed files are Excel-saved.
 */
public class Excel2010ExtensionsSamples {

	static final ObjectFactory SML = Context.getsmlObjectFactory();
	static final org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.ObjectFactory X14 =
			new org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.ObjectFactory();
	static final String MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

	// the ext uris Excel writes for each kind of x14 content (measured on its output)
	static final String EXT_SPARKLINE_GROUPS = "{05C60535-1F16-4fd2-B633-F4F36F0B64E0}";
	static final String EXT_CONDITIONAL_FORMATTINGS = "{78C0D931-6437-407d-A8EE-F0AAD7539E65}";
	static final String EXT_CF_RULE_ID = "{B025F937-C7B1-47D3-B67F-A62EFF666E3E}";
	static final String EXT_DATA_VALIDATIONS = "{CCE6A557-97BC-4b89-ADB6-D9C93CAAB3DF}";

	public static void main(String[] args) throws Exception {
		File dir = new File(args.length > 0 ? args[0] : ".");
		dir.mkdirs();
		sparklines(new File(dir, "cr022-sparklines.xlsx"));
		conditionalFormatting(new File(dir, "cr022-conditional-formatting.xlsx"));
		dataValidation(new File(dir, "cr022-data-validation.xlsx"));
		checkBox(new File(dir, "cr022-checkbox.xlsx"));
	}

	// ---- 1. sparklines: three groups (line, column, stacked) over three rows of numbers

	static void sparklines(File out) throws Exception {
		SpreadsheetMLPackage pkg = newPackage();
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		SheetData sd = sheet.getContents().getSheetData();
		double[][] series = {
				{ 3, 5, 4, 7, 6, 9, 8, 6, 7, 10, 9, 12 },
				{ 4, -2, 5, -1, 6, 3, -3, 7, 2, -4, 8, 5 },
				{ 1, -1, 1, 1, -1, 1, -1, -1, 1, 1, -1, 1 } };
		for (int i = 0; i < 3; i++) {
			Row r = SML.createRow();
			r.setR((long) (i + 1));
			r.getSpans().add("1:13");
			r.getC().add(text("A" + (i + 1), new String[] { "Line", "Column", "Win/loss" }[i]));
			for (int j = 0; j < 12; j++) {
				r.getC().add(num(column(2 + j) + (i + 1), series[i][j]));
			}
			sd.getRow().add(r);
		}

		CTSparklineGroups groups = X14.createCTSparklineGroups();
		groups.getSparklineGroup().add(group(STSparklineType.LINE, "Sheet1!B1:M1", "N1", true));
		groups.getSparklineGroup().add(group(STSparklineType.COLUMN, "Sheet1!B2:M2", "N2", false));
		groups.getSparklineGroup().add(group(STSparklineType.STACKED, "Sheet1!B3:M3", "N3", false));
		ext(sheet.getContents(), EXT_SPARKLINE_GROUPS, X14.createSparklineGroups(groups));

		pkg.save(out);
		System.out.println("saved " + out);
	}

	static CTSparklineGroup group(STSparklineType type, String formula, String at, boolean markers) {
		CTSparklineGroup g = X14.createCTSparklineGroup();
		if (type != STSparklineType.LINE) g.setType(type);
		g.setDisplayEmptyCellsAs(STDispBlanksAs.GAP);
		g.setHigh(true);
		g.setLow(true);
		g.setNegative(true);
		if (markers) g.setMarkers(true);
		g.setColorSeries(rgb("FF376092"));
		g.setColorNegative(rgb("FFD00000"));
		g.setColorAxis(rgb("FF000000"));
		g.setColorMarkers(rgb("FFD00000"));
		g.setColorFirst(rgb("FFD00000"));
		g.setColorLast(rgb("FFD00000"));
		g.setColorHigh(rgb("FFD00000"));
		g.setColorLow(rgb("FFD00000"));
		CTSparklines lines = X14.createCTSparklines();
		CTSparkline line = X14.createCTSparkline();
		line.setF(formula);
		line.setSqref(sqref(at));
		lines.getSparkline().add(line);
		g.setSparklines(lines);
		return g;
	}

	// ---- 2. conditional formatting: a data bar (written twice, as Excel does - the 2006 rule
	//         plus its x14 twin, joined by the x14:id) and an icon set Excel 2010 added (x14 only)

	static void conditionalFormatting(File out) throws Exception {
		SpreadsheetMLPackage pkg = newPackage();
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		Worksheet ws = sheet.getContents();
		for (int i = 1; i <= 10; i++) {
			Row r = SML.createRow();
			r.setR((long) i);
			r.getSpans().add("1:3");
			r.getC().add(num("A" + i, i * 7 % 10 + 1));
			r.getC().add(num("C" + i, i));
			ws.getSheetData().getRow().add(r);
		}

		// the 2006 data bar rule over A1:A10, carrying the id of its x14 twin
		String barId = "{9AD74F3E-E6C8-4A58-8707-D0A9D6899A40}";
		org.xlsx4j.sml.CTConditionalFormatting cf = SML.createCTConditionalFormatting();
		cf.getSqref().add("A1:A10");
		org.xlsx4j.sml.CTCfRule rule = SML.createCTCfRule();
		rule.setType(STCfType.DATA_BAR);
		rule.setPriority(1);
		org.xlsx4j.sml.CTDataBar bar = SML.createCTDataBar();
		bar.getCfvo().add(cfvo2006(org.xlsx4j.sml.STCfvoType.MIN));
		bar.getCfvo().add(cfvo2006(org.xlsx4j.sml.STCfvoType.MAX));
		bar.setColor(rgb("FF638EC6"));
		rule.setDataBar(bar);
		rule.setExtLst(SML.createCTExtensionList());
		CTExtension idExt = SML.createCTExtension();
		idExt.setUri(EXT_CF_RULE_ID);
		idExt.setAny(X14.createId(barId));
		rule.getExtLst().getExt().add(idExt);
		cf.getCfRule().add(rule);
		ws.getConditionalFormatting().add(cf);

		// the x14 twin: a solid bar with a negative colour and an axis, and an icon set over C1:C10
		CTConditionalFormattings x14cfs = X14.createCTConditionalFormattings();

		CTConditionalFormatting x14bar = X14.createCTConditionalFormatting();
		CTCfRule x14rule = X14.createCTCfRule();
		x14rule.setType(STCfType.DATA_BAR);
		x14rule.setId(barId);
		CTDataBar x14data = X14.createCTDataBar();
		x14data.setMinLength(0L);
		x14data.setMaxLength(100L);
		x14data.setGradient(false);
		x14data.getCfvo().add(cfvo(STCfvoType.AUTO_MIN, null));
		x14data.getCfvo().add(cfvo(STCfvoType.AUTO_MAX, null));
		x14data.setNegativeFillColor(rgb("FFFF0000"));
		x14data.setAxisColor(rgb("FF000000"));
		x14rule.setDataBar(x14data);
		x14bar.getCfRule().add(x14rule);
		x14bar.setSqref(sqref("A1:A10"));
		x14cfs.getConditionalFormatting().add(x14bar);

		CTConditionalFormatting x14icons = X14.createCTConditionalFormatting();
		CTCfRule iconRule = X14.createCTCfRule();
		iconRule.setType(STCfType.ICON_SET);
		iconRule.setPriority(2);
		iconRule.setId("{526FD95A-81FA-4AD7-B006-6799BE0A3164}");
		CTIconSet icons = X14.createCTIconSet();
		icons.setIconSet("3Triangles");  // an Excel 2010 set, so the rule has no 2006 form
		icons.getCfvo().add(cfvo(STCfvoType.PERCENT, "0"));
		icons.getCfvo().add(cfvo(STCfvoType.PERCENT, "33"));
		icons.getCfvo().add(cfvo(STCfvoType.PERCENT, "67"));
		iconRule.setIconSet(icons);
		x14icons.getCfRule().add(iconRule);
		x14icons.setSqref(sqref("C1:C10"));
		x14cfs.getConditionalFormatting().add(x14icons);

		ext(ws, EXT_CONDITIONAL_FORMATTINGS, X14.createConditionalFormattings(x14cfs));

		pkg.save(out);
		System.out.println("saved " + out);
	}

	// ---- 3. data validation: two list validations whose lists are on Sheet2 (x14 form only)

	static void dataValidation(File out) throws Exception {
		SpreadsheetMLPackage pkg = newPackage();
		WorksheetPart sheet1 = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		WorksheetPart sheet2 = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet2.xml"), "Sheet2", 2);

		String[][] lists = { { "Colour", "Red", "Green", "Blue" }, { "Size", "Small", "Medium", "Large" } };
		for (int i = 0; i < 4; i++) {
			Row r = SML.createRow();
			r.setR((long) (i + 1));
			r.getSpans().add("1:2");
			r.getC().add(text("A" + (i + 1), lists[0][i]));
			r.getC().add(text("B" + (i + 1), lists[1][i]));
			sheet2.getContents().getSheetData().getRow().add(r);
		}
		Row r = SML.createRow();
		r.setR(1L);
		r.getSpans().add("1:2");
		r.getC().add(text("A1", "Red"));
		r.getC().add(text("B1", "Small"));
		sheet1.getContents().getSheetData().getRow().add(r);

		CTDataValidations dvs = X14.createCTDataValidations();
		dvs.getDataValidation().add(listValidation("Sheet2!$A$2:$A$4", "A1"));
		dvs.getDataValidation().add(listValidation("Sheet2!$B$2:$B$4", "B1"));
		dvs.setCount(2L);
		ext(sheet1.getContents(), EXT_DATA_VALIDATIONS, X14.createDataValidations(dvs));

		pkg.save(out);
		System.out.println("saved " + out);
	}

	static CTDataValidation listValidation(String source, String at) {
		CTDataValidation dv = X14.createCTDataValidation();
		dv.setType(STDataValidationType.LIST);
		dv.setAllowBlank(true);
		dv.setShowInputMessage(true);
		dv.setShowErrorMessage(true);
		CTDataValidationFormula f = X14.createCTDataValidationFormula();
		f.setF(source);
		dv.setFormula1(f);
		dv.setSqref(sqref(at));
		return dv;
	}

	// ---- 4. a check box form control

	static void checkBox(File out) throws Exception {
		SpreadsheetMLPackage pkg = newPackage();
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		Worksheet ws = sheet.getContents();
		Row r = SML.createRow();
		r.setR(1L);
		r.getSpans().add("1:1");
		r.getC().add(text("A1", "A check box, over B4:D7"));
		ws.getSheetData().getRow().add(r);

		// the shape, as the legacy VML drawing Excel keeps for form controls (xlsx4j has no VML binding;
		// the part is written as bytes)
		BinaryPart vml = new BinaryPart(new PartName("/xl/drawings/vmlDrawing1.vml"));
		vml.setContentType(new ContentType(ContentTypes.VML_DRAWING));
		vml.setRelationshipType(Namespaces.VML);
		vml.setBinaryData(VML_CHECK_BOX.getBytes(StandardCharsets.UTF_8));
		Relationship vmlRel = sheet.addTargetPart(vml);
		CTLegacyDrawing legacy = SML.createCTLegacyDrawing();
		legacy.setId(vmlRel.getId());
		ws.setLegacyDrawing(legacy);

		// the control's properties part (x14:formControlPr)
		ControlPropertiesPart props = new ControlPropertiesPart(new PartName("/xl/ctrlProps/ctrlProp1.xml"));
		CTFormControlPr pr = X14.createCTFormControlPr();
		pr.setObjectType(STObjectType.CHECK_BOX);
		pr.setLockText(true);
		pr.setNoThreeD(true);
		props.setJaxbElement(pr);
		Relationship propsRel = sheet.addTargetPart(props);

		// the control, wrapped as Excel wraps it: worksheet > mc:AlternateContent(x14) > controls >
		// mc:AlternateContent(x14) > control
		CTControl control = SML.createCTControl();
		control.setShapeId(1025L);
		control.setId(propsRel.getId());
		control.setName("Check Box 1");
		CTControlPr controlPr = SML.createCTControlPr();
		controlPr.setDefaultSize(false);
		controlPr.setAutoFill(false);
		controlPr.setAutoLine(false);
		controlPr.setAutoPict(false);
		CTObjectAnchor anchor = SML.createCTObjectAnchor();
		anchor.setMoveWithCells(true);
		anchor.setFrom(marker(1, 209550, 3, 28575));
		anchor.setTo(marker(3, 285750, 6, 9525));
		controlPr.setAnchor(anchor);
		control.setControlPr(controlPr);

		CTControls controls = SML.createCTControls();
		controls.getAlternateContent().add(choice("x14",
				new JAXBElement<CTControl>(new QName(MAIN_NS, "control"), CTControl.class, control)));
		ws.getAlternateContent().add(choice("x14",
				new JAXBElement<CTControls>(new QName(MAIN_NS, "controls"), CTControls.class, controls)));

		pkg.save(out);
		System.out.println("saved " + out);
	}

	static org.docx4j.dml.spreadsheetdrawing.CTMarker marker(int col, int colOff, int row, int rowOff) {
		org.docx4j.dml.spreadsheetdrawing.CTMarker m = new org.docx4j.dml.spreadsheetdrawing.CTMarker();
		m.setCol(col);
		m.setColOff(colOff);
		m.setRow(row);
		m.setRowOff(rowOff);
		return m;
	}

	/** An mc:AlternateContent with one Choice and no Fallback, as Excel writes them for x14 controls. */
	static AlternateContent choice(String requires, Object content) {
		AlternateContent ac = new AlternateContent();
		AlternateContent.Choice c = new AlternateContent.Choice();
		c.setRequires(requires);
		c.getAny().add(content);
		ac.getChoice().add(c);
		return ac;
	}

	/** The VML shape of a check box anchored over B4:D7, as Excel writes it (shape id 1025 = _x0000_s1025). */
	static final String VML_CHECK_BOX =
			"<xml xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:x=\"urn:schemas-microsoft-com:office:excel\">\n"
			+ " <o:shapelayout v:ext=\"edit\"><o:idmap v:ext=\"edit\" data=\"1\"/></o:shapelayout>\n"
			+ " <v:shapetype id=\"_x0000_t201\" coordsize=\"21600,21600\" o:spt=\"201\" path=\"m,l,21600r21600,l21600,xe\">\n"
			+ "  <v:stroke joinstyle=\"miter\"/>\n"
			+ "  <v:path shadowok=\"f\" o:extrusionok=\"f\" strokeok=\"f\" fillok=\"f\" o:connecttype=\"rect\"/>\n"
			+ "  <o:lock v:ext=\"edit\" shapetype=\"t\"/>\n"
			+ " </v:shapetype>\n"
			+ " <v:shape id=\"_x0000_s1025\" type=\"#_x0000_t201\" style='position:absolute;margin-left:64.5pt;margin-top:47.25pt;"
			+ "width:102pt;height:43.5pt;z-index:1;mso-wrap-style:tight' filled=\"f\" fillcolor=\"window [65]\" stroked=\"f\""
			+ " strokecolor=\"windowText [64]\" o:insetmode=\"auto\">\n"
			+ "  <v:path shadowok=\"t\" strokeok=\"t\" fillok=\"t\"/>\n"
			+ "  <o:lock v:ext=\"edit\" rotation=\"t\"/>\n"
			+ "  <v:textbox o:singleclick=\"f\"><div style='text-align:left'><font face=\"Segoe UI\" size=\"160\" color=\"#000000\">Check Box 1</font></div></v:textbox>\n"
			+ "  <x:ClientData ObjectType=\"Checkbox\">\n"
			+ "   <x:SizeWithCells/>\n"
			+ "   <x:Anchor>1, 22, 3, 3, 3, 30, 6, 1</x:Anchor>\n"
			+ "   <x:AutoFill>False</x:AutoFill>\n"
			+ "   <x:AutoLine>False</x:AutoLine>\n"
			+ "   <x:TextVAlign>Center</x:TextVAlign>\n"
			+ "   <x:NoThreeD/>\n"
			+ "  </x:ClientData>\n"
			+ " </v:shape>\n"
			+ "</xml>";

	// ---- helpers

	/** A package with the minimal style sheet Excel expects. */
	static SpreadsheetMLPackage newPackage() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		Styles styles = new Styles(new PartName("/xl/styles.xml"));
		styles.setJaxbElement((CTStylesheet) XmlUtils.unmarshalString("<styleSheet xmlns=\"" + MAIN_NS + "\">"
				+ "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Aptos Narrow\"/><family val=\"2\"/></font></fonts>"
				+ "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
				+ "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
				+ "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
				+ "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>"
				+ "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
				+ "</styleSheet>", Context.jcSML, CTStylesheet.class));
		pkg.getWorkbookPart().addTargetPart(styles);
		return pkg;
	}

	/** Adds an ext of the given uri, holding the given (x14) content, to the worksheet's extLst. */
	static void ext(Worksheet ws, String uri, JAXBElement<?> content) {
		if (ws.getExtLst() == null) ws.setExtLst(SML.createCTExtensionList());
		CTExtension ext = SML.createCTExtension();
		ext.setUri(uri);
		ext.setAny(content);
		ws.getExtLst().getExt().add(ext);
	}

	static CTSqref sqref(String ref) {
		CTSqref s = new CTSqref();
		s.getValue().addAll(Arrays.asList(ref.split(" ")));
		return s;
	}

	static CTCfvo cfvo(STCfvoType type, String formula) {
		CTCfvo v = X14.createCTCfvo();
		v.setType(type);
		if (formula != null) v.setF(formula);
		return v;
	}

	static org.xlsx4j.sml.CTCfvo cfvo2006(org.xlsx4j.sml.STCfvoType type) {
		org.xlsx4j.sml.CTCfvo v = SML.createCTCfvo();
		v.setType(type);
		return v;
	}

	static CTColor rgb(String argb) {
		CTColor c = SML.createCTColor();
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) b[i] = (byte) Integer.parseInt(argb.substring(2 * i, 2 * i + 2), 16);
		c.setRgb(b);
		return c;
	}

	static Cell text(String ref, String s) {
		Cell c = SML.createCell();
		c.setR(ref);
		c.setT(org.xlsx4j.sml.STCellType.INLINE_STR);
		org.xlsx4j.sml.CTRst rst = SML.createCTRst();
		org.xlsx4j.sml.CTXstringWhitespace t = SML.createCTXstringWhitespace();
		t.setValue(s);
		rst.setT(t);
		c.setIs(rst);
		return c;
	}

	static Cell num(String ref, double v) {
		Cell c = SML.createCell();
		c.setR(ref);
		c.setV(v == Math.floor(v) ? Long.toString((long) v) : Double.toString(v));
		return c;
	}

	static String column(int oneBased) {
		return String.valueOf((char) ('A' + oneBased - 1));
	}
}
