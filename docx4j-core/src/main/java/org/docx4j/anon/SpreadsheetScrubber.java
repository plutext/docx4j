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
package org.docx4j.anon;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.anon.JaxbGraphWalker.Action;
import org.xlsx4j.sml.CTAuthors;
import org.xlsx4j.sml.CTCellFormula;
import org.xlsx4j.sml.CTCellSmartTagPr;
import org.xlsx4j.sml.CTCellStyle;
import org.xlsx4j.sml.CTCfRule;
import org.xlsx4j.sml.CTConnection;
import org.xlsx4j.sml.CTControls;
import org.xlsx4j.sml.CTCustomFilter;
import org.xlsx4j.sml.CTCustomWorkbookView;
import org.xlsx4j.sml.CTDataRef;
import org.xlsx4j.sml.CTDataValidation;
import org.xlsx4j.sml.CTDbPr;
import org.xlsx4j.sml.CTDdeItem;
import org.xlsx4j.sml.CTDdeLink;
import org.xlsx4j.sml.CTDefinedName;
import org.xlsx4j.sml.CTExternalCell;
import org.xlsx4j.sml.CTExternalDefinedName;
import org.xlsx4j.sml.CTExternalSheetName;
import org.xlsx4j.sml.CTFileSharing;
import org.xlsx4j.sml.CTFilter;
import org.xlsx4j.sml.CTFunctionGroup;
import org.xlsx4j.sml.CTHeaderFooter;
import org.xlsx4j.sml.CTHyperlink;
import org.xlsx4j.sml.CTInputCells;
import org.xlsx4j.sml.CTMap;
import org.xlsx4j.sml.CTOleItem;
import org.xlsx4j.sml.CTOleObjects;
import org.xlsx4j.sml.CTParameter;
import org.xlsx4j.sml.CTPhoneticRun;
import org.xlsx4j.sml.CTPivotCaches;
import org.xlsx4j.sml.CTQueryTable;
import org.xlsx4j.sml.CTQueryTableField;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSheetProtection;
import org.xlsx4j.sml.CTSmartTagType;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.CTTableFormula;
import org.xlsx4j.sml.CTTableStyle;
import org.xlsx4j.sml.CTTableStyleInfo;
import org.xlsx4j.sml.CTTextPr;
import org.xlsx4j.sml.CTWebPr;
import org.xlsx4j.sml.CTWebPublishItem;
import org.xlsx4j.sml.CTWorkbookProtection;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.Sheet;

/**
 * The third visitor of the anonymiser's walk, for a workbook (CR-019 phase 3):
 * everything SpreadsheetML-typed that carries text, numbers, names or
 * identities. The DrawingML and chart classes a workbook shares with the other
 * formats are {@link ScrambleText}'s and {@link MarkupScrubber}'s.
 * <ul>
 * <li>Text: shared and inline strings (the same string always to the same
 * output, so a table column's name still equals its header cell), formula
 * string results, comments and their authors, headers and footers (codes
 * kept), hyperlink display text, data-validation prompts and errors,
 * conditional-format text, filter values, defined-name comments, custom cell
 * style names, table and query-table names, connection names and descriptions.</li>
 * <li>Numbers: cell values and formula constants have their digits randomised
 * ({@link ScrambleText#number}; decision 3 - kept with
 * {@link ScrambleText#setKeepNumbers}); booleans and errors stay.</li>
 * <li>Formulas ({@link SmlFormulas}): cells, defined names, tables' calculated
 * columns, validations, conditional formats, sparklines, form controls, chart
 * references; sheet names become {@code Sheet<n>} everywhere.</li>
 * <li>Identities and references: the file-sharing user name, comment authors,
 * every password hash and salt (sheet, workbook, file sharing); connection
 * strings, commands, source files and URLs; DDE and OLE link names.</li>
 * <li>STRICT: the pivot-cache list, the slicer, timeline and data-model
 * extensions, OLE objects and ActiveX controls, and a cell's rich-value
 * metadata indexes go (their parts are removed).</li>
 * </ul>
 *
 * @since 17.2.1
 */
public class SpreadsheetScrubber implements JaxbGraphWalker.Visitor {

	private final ScrambleText scrambler;
	private final SmlFormulas formulas;
	private final Names names;
	private final boolean strict;
	private final Consumer<String> notes;
	/** "partName#relId" of the pictures which stood for a removed OLE object: MediaReplacer labels them */
	private final java.util.Set<String> objectPreviews;
	private String currentPartName = "";

	/** Excel's built-in table and pivot style names, which name nothing */
	private static final Pattern BUILT_IN_TABLE_STYLE = Pattern.compile("^(TableStyle|PivotStyle)(Light|Medium|Dark)[0-9]+$");

	public SpreadsheetScrubber(ScrambleText scrambler, Names names, boolean strict, Consumer<String> notes,
			java.util.Set<String> objectPreviews) {
		this.scrambler = scrambler;
		this.formulas = new SmlFormulas(scrambler, names);
		this.names = names;
		this.strict = strict;
		this.notes = notes;
		this.objectPreviews = objectPreviews;
	}

	/** The part being walked: relationship ids are per part. */
	public void setCurrentPart(org.docx4j.openpackaging.parts.Part part) {
		currentPartName = part == null ? "" : part.getPartName().getName();
	}

	public SmlFormulas formulas() {
		return formulas;
	}

	@Override
	public Action visit(Object o, JAXBElement<?> wrapper) {

		// ---- text
		if (o instanceof CTRst) {
			richText((CTRst) o);
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof Cell) {
			Cell c = (Cell) o;
			cellValue(c.getT(), c.getV(), v -> c.setV(v));
			if (strict && (c.getCm() != 0 || c.getVm() != 0)) {
				// rich-value and cell metadata indexes into parts the tool removes
				c.setCm(null);
				c.setVm(null);
			}
			return Action.CONTINUE; // its f and is
		}
		if (o instanceof CTExternalCell) {
			CTExternalCell c = (CTExternalCell) o;
			cellValue(c.getT(), c.getV(), v -> c.setV(v));
			c.setVm(null);
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTCellFormula) {
			CTCellFormula f = (CTCellFormula) o;
			f.setValue(formulas.scrub(f.getValue()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTTableFormula) {
			CTTableFormula f = (CTTableFormula) o;
			f.setValue(formulas.scrub(f.getValue()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTHeaderFooter) {
			CTHeaderFooter hf = (CTHeaderFooter) o;
			hf.setOddHeader(scrambler.headerFooter(hf.getOddHeader()));
			hf.setOddFooter(scrambler.headerFooter(hf.getOddFooter()));
			hf.setEvenHeader(scrambler.headerFooter(hf.getEvenHeader()));
			hf.setEvenFooter(scrambler.headerFooter(hf.getEvenFooter()));
			hf.setFirstHeader(scrambler.headerFooter(hf.getFirstHeader()));
			hf.setFirstFooter(scrambler.headerFooter(hf.getFirstFooter()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTHyperlink) {
			CTHyperlink h = (CTHyperlink) o;
			h.setDisplay(scrambler.scramble(h.getDisplay()));
			h.setTooltip(null);
			h.setLocation(formulas.scrub(h.getLocation())); // Sheet2!A1, or a defined name
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTDataValidation) {
			CTDataValidation dv = (CTDataValidation) o;
			dv.setFormula1(formulas.scrub(dv.getFormula1()));
			dv.setFormula2(formulas.scrub(dv.getFormula2()));
			dv.setPrompt(scrambler.scramble(dv.getPrompt()));
			dv.setPromptTitle(scrambler.scramble(dv.getPromptTitle()));
			dv.setError(scrambler.scramble(dv.getError()));
			dv.setErrorTitle(scrambler.scramble(dv.getErrorTitle()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidation) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidation dv =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidation) o;
			if (dv.getFormula1() != null) dv.getFormula1().setF(formulas.scrub(dv.getFormula1().getF()));
			if (dv.getFormula2() != null) dv.getFormula2().setF(formulas.scrub(dv.getFormula2().getF()));
			dv.setPrompt(scrambler.scramble(dv.getPrompt()));
			dv.setPromptTitle(scrambler.scramble(dv.getPromptTitle()));
			dv.setError(scrambler.scramble(dv.getError()));
			dv.setErrorTitle(scrambler.scramble(dv.getErrorTitle()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTCfRule) {
			CTCfRule rule = (CTCfRule) o;
			List<String> fs = rule.getFormula();
			for (int i = 0; i < fs.size(); i++) fs.set(i, formulas.scrub(fs.get(i)));
			rule.setText(scrambler.scramble(rule.getText()));
			return Action.CONTINUE; // its extLst (x14 rules with xm:f)
		}
		if (o instanceof CTCustomFilter) {
			CTCustomFilter f = (CTCustomFilter) o;
			f.setVal(scrambler.scramble(f.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTFilter) {
			CTFilter f = (CTFilter) o;
			f.setVal(scrambler.scramble(f.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTInputCells) {
			CTInputCells ic = (CTInputCells) o;
			ic.setVal(scrambler.number(ic.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTCellSmartTagPr) {
			CTCellSmartTagPr pr = (CTCellSmartTagPr) o;
			pr.setKey(scrambler.scrambleLetters(pr.getKey()));
			pr.setVal(scrambler.scramble(pr.getVal()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTSmartTagType) {
			CTSmartTagType st = (CTSmartTagType) o;
			st.setName(scrambler.scrambleLetters(st.getName()));
			st.setUrl(MetadataScrubber.EXTERNAL_PLACEHOLDER);
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTListItem) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTListItem li =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTListItem) o;
			li.setVal(scrambler.scramble(li.getVal()));
			return Action.SKIP_CHILDREN;
		}

		// ---- names
		if (o instanceof Sheet) {
			Sheet sheet = (Sheet) o;
			String mapped = names.sheet(sheet.getName());
			sheet.setName(mapped != null ? mapped : scrambler.consistent(sheet.getName()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTDefinedName) {
			CTDefinedName dn = (CTDefinedName) o;
			dn.setName(Names.definedName(dn.getName()));
			dn.setValue(formulas.scrub(dn.getValue()));
			dn.setComment(scrambler.scramble(dn.getComment()));
			dn.setDescription(scrambler.scramble(dn.getDescription()));
			dn.setHelp(scrambler.scramble(dn.getHelp()));
			dn.setStatusBar(scrambler.scramble(dn.getStatusBar()));
			dn.setCustomMenu(scrambler.scramble(dn.getCustomMenu()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTExternalDefinedName) {
			CTExternalDefinedName dn = (CTExternalDefinedName) o;
			dn.setName(Names.definedName(dn.getName()));
			dn.setRefersTo(formulas.scrub(dn.getRefersTo()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTExternalSheetName) {
			CTExternalSheetName sn = (CTExternalSheetName) o;
			sn.setVal(scrambler.consistent(sn.getVal())); // as the formulas' '[1]Sheet'! map it
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTTable) {
			CTTable t = (CTTable) o;
			t.setName(scrambler.consistentIdentifier(t.getName()));
			t.setDisplayName(scrambler.consistentIdentifier(t.getDisplayName())); // as structured references map it; no spaces
			t.setComment(scrambler.scramble(t.getComment()));
			return Action.CONTINUE;
		}
		if (o instanceof CTTableColumn) {
			CTTableColumn col = (CTTableColumn) o;
			col.setName(scrambler.consistent(col.getName())); // must equal the header cell's text
			col.setTotalsRowLabel(scrambler.consistent(col.getTotalsRowLabel()));
			col.setUniqueName(scrambler.consistent(col.getUniqueName()));
			return Action.CONTINUE; // its formulas
		}
		if (o instanceof CTTableStyleInfo) {
			CTTableStyleInfo info = (CTTableStyleInfo) o;
			if (info.getName() != null && !BUILT_IN_TABLE_STYLE.matcher(info.getName()).matches()) {
				info.setName(scrambler.consistent(info.getName()));
			}
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTTableStyle) {
			CTTableStyle ts = (CTTableStyle) o;
			if (ts.getName() != null && !BUILT_IN_TABLE_STYLE.matcher(ts.getName()).matches()) {
				ts.setName(scrambler.consistent(ts.getName()));
			}
			return Action.CONTINUE;
		}
		if (o instanceof CTCellStyle) {
			CTCellStyle cs = (CTCellStyle) o;
			if (cs.getBuiltinId() == null) cs.setName(scrambler.scrambleLetters(cs.getName()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTQueryTable) {
			CTQueryTable qt = (CTQueryTable) o;
			qt.setName(scrambler.consistentIdentifier(qt.getName()));
			return Action.CONTINUE;
		}
		if (o instanceof CTQueryTableField) {
			CTQueryTableField f = (CTQueryTableField) o;
			f.setName(scrambler.consistent(f.getName())); // as the table column of the same name
			return Action.CONTINUE;
		}
		if (o instanceof CTDataRef) {
			CTDataRef ref = (CTDataRef) o;
			if (ref.getSheet() != null) {
				String mapped = names.sheet(ref.getSheet());
				ref.setSheet(mapped != null ? mapped : scrambler.consistent(ref.getSheet()));
			}
			if (ref.getName() != null) ref.setName(Names.definedName(ref.getName()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTCustomWorkbookView) {
			CTCustomWorkbookView v = (CTCustomWorkbookView) o;
			v.setName(scrambler.scramble(v.getName()));
			return Action.CONTINUE;
		}
		if (o instanceof CTFunctionGroup) {
			CTFunctionGroup g = (CTFunctionGroup) o;
			g.setName(scrambler.scramble(g.getName()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTWebPublishItem) {
			CTWebPublishItem item = (CTWebPublishItem) o;
			item.setTitle(scrambler.scramble(item.getTitle()));
			item.setDestinationFile(null);
			item.setDivId(scrambler.scrambleLetters(item.getDivId()));
			item.setSourceObject(scrambler.scrambleLetters(item.getSourceObject()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTMap) {
			CTMap map = (CTMap) o;
			map.setName(scrambler.scrambleLetters(map.getName()));
			map.setRootElement(scrambler.scrambleLetters(map.getRootElement()));
			map.setSchemaID(scrambler.scrambleLetters(map.getSchemaID()));
			return Action.CONTINUE;
		}

		// ---- identities and secrets
		if (o instanceof CTAuthors) {
			List<String> authors = ((CTAuthors) o).getAuthor();
			for (int i = 0; i < authors.size(); i++) authors.set(i, names.author(authors.get(i)));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTFileSharing) {
			CTFileSharing fs = (CTFileSharing) o;
			fs.setUserName(names.author(fs.getUserName()));
			fs.setReservationPassword(null);
			fs.setHashValue(null);
			fs.setSaltValue(null);
			fs.setAlgorithmName(null);
			fs.setSpinCount(null);
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTSheetProtection) {
			CTSheetProtection p = (CTSheetProtection) o;
			p.setPassword(null);
			p.setHashValue(null);
			p.setSaltValue(null);
			p.setAlgorithmName(null);
			p.setSpinCount(null);
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTWorkbookProtection) {
			CTWorkbookProtection p = (CTWorkbookProtection) o;
			p.setWorkbookPassword(null);
			p.setRevisionsPassword(null);
			p.setWorkbookHashValue(null);
			p.setWorkbookSaltValue(null);
			p.setWorkbookAlgorithmName(null);
			p.setWorkbookSpinCount(null);
			p.setRevisionsHashValue(null);
			p.setRevisionsSaltValue(null);
			p.setRevisionsAlgorithmName(null);
			p.setRevisionsSpinCount(null);
			return Action.SKIP_CHILDREN;
		}

		// ---- connections and links
		if (o instanceof CTConnection) {
			CTConnection c = (CTConnection) o;
			if (isModelOrQueryConnection(c)) {
				// the data model (xl/model/item.data) and the Power Query mashup (customXml) are
				// removed whatever the mode; a connection to either would be a repair prompt
				notes.accept("connection removed (data model or Power Query): " + c.getName());
				return Action.REMOVE;
			}
			c.setName(scrambler.scrambleLetters(c.getName()));
			c.setDescription(scrambler.scramble(c.getDescription()));
			c.setSourceFile(null);
			c.setOdcFile(null);
			c.setSingleSignOnId(null);
			if (c.getOlapPr() != null) c.getOlapPr().setLocalConnection(null);
			return Action.CONTINUE;
		}
		if (o instanceof CTDbPr) {
			CTDbPr db = (CTDbPr) o;
			db.setConnection("Provider=None");
			db.setCommand(scrambler.scramble(db.getCommand()));
			db.setServerCommand(scrambler.scramble(db.getServerCommand()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTWebPr) {
			CTWebPr web = (CTWebPr) o;
			if (web.getUrl() != null) web.setUrl(MetadataScrubber.EXTERNAL_PLACEHOLDER);
			web.setPost(null);
			web.setEditPage(null);
			return Action.CONTINUE;
		}
		if (o instanceof CTTextPr) {
			((CTTextPr) o).setSourceFile(null);
			return Action.CONTINUE;
		}
		if (o instanceof CTParameter) {
			CTParameter p = (CTParameter) o;
			p.setName(scrambler.scrambleLetters(p.getName()));
			p.setPrompt(scrambler.scramble(p.getPrompt()));
			p.setString(scrambler.scramble(p.getString()));
			p.setCell(formulas.scrub(p.getCell()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTOledbPr) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTOledbPr pr =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTOledbPr) o;
			pr.setConnection("Provider=None");
			if (pr.getDbCommand() != null) pr.getDbCommand().setText(scrambler.scramble(pr.getDbCommand().getText()));
			return Action.CONTINUE; // its dbTables
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTDataFeedPr) {
			((org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTDataFeedPr) o).setConnection(null);
			return Action.CONTINUE;
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTDbTable) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTDbTable t =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTDbTable) o;
			t.setName(scrambler.scramble(t.getName()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof CTDdeLink) {
			CTDdeLink dde = (CTDdeLink) o;
			dde.setDdeService(scrambler.scrambleLetters(dde.getDdeService()));
			dde.setDdeTopic(scrambler.scrambleLetters(dde.getDdeTopic()));
			return Action.CONTINUE;
		}
		if (o instanceof CTDdeItem) {
			CTDdeItem item = (CTDdeItem) o;
			item.setName(scrambler.scrambleLetters(item.getName()));
			return Action.CONTINUE;
		}
		if (o instanceof CTOleItem) {
			CTOleItem item = (CTOleItem) o;
			item.setName(scrambler.scrambleLetters(item.getName()));
			return Action.SKIP_CHILDREN;
		}

		if (o instanceof org.xlsx4j.sml.CTObjectPr) {
			// an OLE object's properties (KEEP: the object stays)
			org.xlsx4j.sml.CTObjectPr pr = (org.xlsx4j.sml.CTObjectPr) o;
			pr.setAltText(scrambler.scramble(pr.getAltText()));
			pr.setMacro(null);
			return Action.CONTINUE;
		}
		if (o instanceof org.xlsx4j.sml.CTControlPr) {
			// an ActiveX control's properties (KEEP: the control stays)
			org.xlsx4j.sml.CTControlPr pr = (org.xlsx4j.sml.CTControlPr) o;
			pr.setAltText(scrambler.scramble(pr.getAltText()));
			pr.setMacro(null);
			pr.setLinkedCell(formulas.scrub(pr.getLinkedCell()));
			pr.setListFillRange(formulas.scrub(pr.getListFillRange()));
			return Action.CONTINUE;
		}

		// ---- sparklines and form controls (x14)
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklineGroup) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklineGroup g =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklineGroup) o;
			g.setF(formulas.scrub(g.getF()));
			return Action.CONTINUE;
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparkline) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparkline sp =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparkline) o;
			sp.setF(formulas.scrub(sp.getF()));
			return Action.SKIP_CHILDREN;
		}
		if (o instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTFormControlPr) {
			org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTFormControlPr pr =
					(org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTFormControlPr) o;
			pr.setFmlaLink(formulas.scrub(pr.getFmlaLink()));
			pr.setFmlaRange(formulas.scrub(pr.getFmlaRange()));
			pr.setFmlaTxbx(formulas.scrub(pr.getFmlaTxbx()));
			pr.setFmlaGroup(formulas.scrub(pr.getFmlaGroup()));
			return Action.CONTINUE; // its list items
		}

		// ---- STRICT: what goes with the parts the tool removes
		if (strict) {
			if (o instanceof CTPivotCaches) {
				notes.accept("pivotCaches removed (the pivot tables and caches go; their cells stay as values)");
				return Action.REMOVE;
			}
			if (o instanceof CTOleObjects) {
				// the objects' preview pictures (objectPr r:id, the same image the VML shape shows)
				// become the labelled placeholder
				for (org.xlsx4j.sml.CTOleObject ole : ((CTOleObjects) o).getOleObject()) {
					if (ole.getObjectPr() != null && ole.getObjectPr().getId() != null) {
						objectPreviews.add(currentPartName + "#" + ole.getObjectPr().getId());
					}
				}
				notes.accept("oleObjects removed (their pictures stay, as the labelled placeholder)");
				return Action.REMOVE;
			}
			if (o instanceof CTControls) {
				notes.accept("controls removed (ActiveX)");
				return Action.REMOVE;
			}
			if (o instanceof org.xlsx4j.sml.CTExtension) {
				Object any = XmlUtils.unwrap(((org.xlsx4j.sml.CTExtension) o).getAny());
				if (any != null && REMOVED_EXTENSIONS.contains(any.getClass().getSimpleName())) {
					notes.accept(any.getClass().getSimpleName().substring(2) + " extension removed");
					return Action.REMOVE;
				}
			}
			if (o instanceof org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor
					&& holdsSlicerOrTimeline((org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor) o)) {
				notes.accept("slicer or timeline drawing removed");
				return Action.REMOVE;
			}
		}

		return Action.CONTINUE;
	}

	/**
	 * A connection into the data model (x15:connection model="1", or type 102: a worksheet
	 * range fed to the model) or a Power Query connection (type 100, whose mashup lives in
	 * the customXml the tool removes): removed with them. Classic ODBC, OLEDB, web and text
	 * connections stay, scrubbed.
	 */
	private static boolean isModelOrQueryConnection(CTConnection c) {
		if (c.getType() != null && (c.getType() == 100 || c.getType() == 102)) return true;
		if (c.getExtLst() != null) {
			for (org.xlsx4j.sml.CTExtension ext : c.getExtLst().getExt()) {
				Object any = XmlUtils.unwrap(ext.getAny());
				if (any instanceof org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTConnection
						&& Boolean.TRUE.equals(((org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTConnection) any).isModel())) {
					return true;
				}
			}
		}
		return false;
	}

	/** the x14/x15 workbook and sheet extensions which point at removed parts: pivot caches, slicers, timelines, the data model */
	private static final java.util.Set<String> REMOVED_EXTENSIONS = new java.util.HashSet<String>(java.util.Arrays.asList(
			"CTPivotCaches", "CTSlicerCaches", "CTSlicerRefs", "CTTimelineCacheRefs", "CTTimelineRefs", "CTDataModel"));

	private static boolean holdsSlicerOrTimeline(org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor anchor) {
		return holdsSlicerOrTimeline(anchor.getGraphicFrame()) || holdsSlicerOrTimeline(anchor.getAlternateContent());
	}

	private static boolean holdsSlicerOrTimeline(Object o) {
		if (o == null) return false;
		Object u = XmlUtils.unwrap(o);
		if (u instanceof org.docx4j.mce.AlternateContent) {
			org.docx4j.mce.AlternateContent ac = (org.docx4j.mce.AlternateContent) u;
			for (org.docx4j.mce.AlternateContent.Choice c : ac.getChoice()) {
				for (Object x : c.getAny()) if (holdsSlicerOrTimeline(x)) return true;
			}
			return false;
		}
		if (u instanceof org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame) {
			org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame frame = (org.docx4j.dml.spreadsheetdrawing.CTGraphicalObjectFrame) u;
			if (frame.getGraphic() != null && frame.getGraphic().getGraphicData() != null) {
				String uri = frame.getGraphic().getGraphicData().getUri();
				return uri != null && (uri.endsWith("/slicer") || uri.endsWith("/timeslicer"));
			}
		}
		if (u instanceof org.w3c.dom.Element) {
			// an xdr:graphicFrame the mc:Choice left as DOM: look for its graphicData
			org.w3c.dom.Element el = (org.w3c.dom.Element) u;
			org.w3c.dom.NodeList datas = el.getElementsByTagNameNS("http://schemas.openxmlformats.org/drawingml/2006/main", "graphicData");
			for (int i = 0; i < datas.getLength(); i++) {
				String uri = ((org.w3c.dom.Element) datas.item(i)).getAttribute("uri");
				if (uri.endsWith("/slicer") || uri.endsWith("/timeslicer")) return true;
			}
		}
		return false;
	}

	/** a cell value by type: strings scrambled, numbers randomised, booleans and errors kept, dates fixed */
	private void cellValue(STCellType type, String v, Consumer<String> set) {
		if (v == null) return;
		if (type == null || type == STCellType.N) {
			set.accept(scrambler.number(v));
		} else if (type == STCellType.STR) {
			set.accept(scrambler.consistent(v));
		}
		// S: an index into the shared strings; B and E: nothing to hide; INLINE_STR: the is child
		// (t="d", the ISO date cell of the strict edition, is not in docx4j's binding: it would be
		// unmarshalled as a number and its digits randomised)
	}

	/**
	 * A rich text string: scrambled as one string ({@link ScrambleText#consistent}),
	 * then dealt back to its runs by length, so a word split across runs stays one
	 * word and the same text always gives the same output.
	 */
	private void richText(CTRst rst) {
		StringBuilder full = new StringBuilder();
		if (rst.getT() != null && rst.getT().getValue() != null) full.append(rst.getT().getValue());
		for (CTRElt r : rst.getR()) {
			if (r.getT() != null && r.getT().getValue() != null) full.append(r.getT().getValue());
		}
		String out = scrambler.consistent(full.toString());
		int at = 0;
		if (rst.getT() != null && rst.getT().getValue() != null) {
			at = deal(rst.getT(), out, at);
		}
		for (CTRElt r : rst.getR()) {
			if (r.getT() != null && r.getT().getValue() != null) at = deal(r.getT(), out, at);
		}
		for (CTPhoneticRun ph : rst.getRPh()) {
			if (ph.getT() != null) ph.getT().setValue(scrambler.scramble(ph.getT().getValue()));
		}
	}

	private int deal(CTXstringWhitespace t, String out, int at) {
		int len = t.getValue().length();
		int end = Math.min(at + len, out.length());
		String piece = out.substring(Math.min(at, out.length()), end);
		if (piece.length() < len) piece = piece + scrambler.scramble(t.getValue().substring(piece.length()));
		t.setValue(piece);
		return at + len;
	}

}
