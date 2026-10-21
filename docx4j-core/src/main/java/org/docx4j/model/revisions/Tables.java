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
package org.docx4j.model.revisions;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.model.revisions.Lister.Row;
import org.docx4j.model.revisions.Trees.View;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblPrEx;
import org.docx4j.wml.CTTblPrExBase;
import org.docx4j.wml.CTTrPrBase;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CTDocGrid;
import org.docx4j.wml.CTCellMergeTrackChange;
import org.docx4j.wml.STAnnotationVMerge;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPrBase;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridBase;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;

/**
 * A table's property records - {@code w:tblPrChange}, {@code w:tblGridChange},
 * {@code w:tblPrExChange}, {@code w:trPrChange}, {@code w:tcPrChange} - which Word writes as one set
 * for one change and Office JS lists as one change per table; cells inserted and deleted; a
 * section's {@code w:sectPrChange} (CR-043 section 3.2, phase 2). A port of docx4j-core-ts's
 * {@code tableRevisions.mts} and its section and cell operations, with what Word's saves of C23 add.
 */
final class Tables {

	private Tables() {}

	private static final Set<String> TABLE_SKIP = new HashSet<String>(Arrays.asList("tblPrChange"));
	private static final Set<String> GRID_SKIP = new HashSet<String>(Arrays.asList("tblGridChange"));
	private static final Set<String> EX_SKIP = new HashSet<String>(Arrays.asList("tblPrExChange"));
	private static final Set<String> ROW_SKIP = new HashSet<String>(Arrays.asList("ins", "del", "trPrChange"));
	private static final Set<String> CELL_SKIP = new HashSet<String>(Arrays.asList("cellIns", "cellDel", "cellMerge", "tcPrChange"));

	// --- what a table's records are ---------------------------------------------------------------------

	/** A table's rows through every branch of a row-level {@code mc:AlternateContent}: its property records are one change, whichever branch holds them (CR-051 phase 2). */
	private static List<Tr> rows(Tbl tbl) {
		List<Tr> out = new ArrayList<Tr>();
		for (Row row : Lister.allRowsOf(tbl)) out.add(row.tr);
		return out;
	}

	/** A row's cells through every branch of a cell-level element, in no grid order; {@link #layouts} where the order matters. */
	private static List<Tc> cells(Tr tr) {
		List<Tc> out = new ArrayList<Tc>();
		for (Row cell : Lister.allCellsOf(tr)) out.add((Tc) Trees.unwrap(cell.item));
		return out;
	}

	/** A row's cells along the grid, one list per branch reading of the row ({@link Lister#cellLayoutsOf}). */
	private static List<List<Tc>> layouts(Tr tr) {
		List<List<Tc>> out = new ArrayList<List<Tc>>();
		for (List<Row> layout : Lister.cellLayoutsOf(tr)) {
			List<Tc> cells = new ArrayList<Tc>();
			for (Row cell : layout) cells.add((Tc) Trees.unwrap(cell.item));
			out.add(cells);
		}
		return out;
	}

	/** A row in the change: one with a {@code w:trPrChange} or a {@code w:tblPrExChange}, or a cell with a {@code w:tcPrChange}. */
	static boolean inChange(Tr tr) {
		if (tr.getTrPr() != null && tr.getTrPr().getTrPrChange() != null) return true;
		if (tr.getTblPrEx() != null && tr.getTblPrEx().getTblPrExChange() != null) return true;
		for (Tc tc : cells(tr)) if (tc.getTcPr() != null && tc.getTcPr().getTcPrChange() != null) return true;
		return false;
	}

	/** True when the table has any property record, changing anything or not. */
	static boolean hasRecords(Tbl tbl) {
		if (tbl.getTblPr() != null && tbl.getTblPr().getTblPrChange() != null) return true;
		if (tbl.getTblGrid() != null && tbl.getTblGrid().getTblGridChange() != null) return true;
		for (Tr tr : rows(tbl)) if (inChange(tr)) return true;
		return false;
	}

	/** True when the table's own properties or its grid are not as recorded. */
	private static boolean tableDiffers(Tbl tbl) {
		TblPr tblPr = tbl.getTblPr();
		if (tblPr != null && tblPr.getTblPrChange() != null
				&& !Props.canonical(tblPr, TblPr.class, "tblPr", TABLE_SKIP).equals(
						Props.canonical(tblPr.getTblPrChange().getTblPr(), CTTblPrBase.class, "tblPr", null))) return true;
		TblGrid grid = tbl.getTblGrid();
		return grid != null && grid.getTblGridChange() != null
				&& !Props.canonical(grid, TblGrid.class, "tblGrid", GRID_SKIP).equals(
						Props.canonical(grid.getTblGridChange().getTblGrid(), TblGridBase.class, "tblGrid", null));
	}

	/** True when a row in the change is not as its records have it (a row without a {@code w:trPrChange} had no properties, check 28). */
	private static boolean rowDiffers(Tr tr) {
		TrPr trPr = tr.getTrPr();
		CTTrPrBase recorded = trPr == null || trPr.getTrPrChange() == null ? null : trPr.getTrPrChange().getTrPr();
		if (!Props.canonical(trPr, TrPr.class, "trPr", ROW_SKIP).equals(Props.canonical(recorded, CTTrPrBase.class, "trPr", null))) return true;
		CTTblPrEx ex = tr.getTblPrEx();
		if (ex != null && ex.getTblPrExChange() != null
				&& !Props.canonical(ex, CTTblPrEx.class, "tblPrEx", EX_SKIP).equals(
						Props.canonical(ex.getTblPrExChange().getTblPrEx(), CTTblPrExBase.class, "tblPrEx", null))) return true;
		for (Tc tc : cells(tr)) {
			TcPr tcPr = tc.getTcPr();
			if (tcPr != null && tcPr.getTcPrChange() != null
					&& !Props.canonical(tcPr, TcPr.class, "tcPr", CELL_SKIP).equals(
							Props.canonical(tcPr.getTcPrChange().getTcPr(), TcPrInner.class, "tcPr", null))) return true;
		}
		return false;
	}

	/**
	 * The record a table's change reports - the table's, else the first changed row's or cell's -
	 * or null when its records change nothing (checks 24 and 26's rule), or are a grid's alone (which
	 * has no author): those the sweep drops.
	 */
	static CTTrackChange recordOf(Tbl tbl) {
		if (!hasRecords(tbl)) return null;
		List<Tr> changed = new ArrayList<Tr>();
		for (Tr tr : rows(tbl)) if (inChange(tr)) changed.add(tr);
		boolean differs = tableDiffers(tbl);
		for (Tr tr : changed) if (!differs && rowDiffers(tr)) differs = true;
		if (!differs) return null;
		if (tbl.getTblPr() != null && tbl.getTblPr().getTblPrChange() != null) return tbl.getTblPr().getTblPrChange();
		for (Tr tr : changed) {
			if (tr.getTrPr() != null && tr.getTrPr().getTrPrChange() != null) return tr.getTrPr().getTrPrChange();
			if (tr.getTblPrEx() != null && tr.getTblPrEx().getTblPrExChange() != null) return tr.getTblPrEx().getTblPrExChange();
			for (Tc tc : cells(tr)) if (tc.getTcPr() != null && tc.getTcPr().getTcPrChange() != null) return tc.getTcPr().getTcPrChange();
		}
		return null;
	}

	/** A table's change's text: every row's where its own properties or grid changed, else the changed rows' (checks 27, 28). */
	static String changeText(Tbl tbl) {
		boolean whole = tableDiffers(tbl);
		StringBuilder out = new StringBuilder();
		for (Tr tr : rows(tbl)) {
			if (whole || (inChange(tr) && rowDiffers(tr))) out.append(Texts.rowText(tr, View.ACCEPTED));
		}
		return out.toString();
	}

	// --- accepting and rejecting them --------------------------------------------------------------------

	/** Accepting keeps the properties as they are and drops every record; a row's {@code w:trPr} left empty goes. */
	static void accept(Tbl tbl) {
		List<Tr> changed = new ArrayList<Tr>();
		for (Tr tr : rows(tbl)) if (inChange(tr)) changed.add(tr);
		if (tbl.getTblPr() != null) tbl.getTblPr().setTblPrChange(null);
		if (tbl.getTblGrid() != null) tbl.getTblGrid().setTblGridChange(null);
		for (Tr tr : rows(tbl)) {
			if (tr.getTrPr() != null) tr.getTrPr().setTrPrChange(null);
			if (tr.getTblPrEx() != null) tr.getTblPrEx().setTblPrExChange(null);
			pruneRow(tr);
			for (Tc tc : cells(tr)) if (tc.getTcPr() != null) tc.getTcPr().setTcPrChange(null);
		}
		for (Tr tr : changed) dropRedundantException(tr, tbl);
	}

	/**
	 * Rejecting puts the recorded table back, wholesale: its properties, its grid, and each row and
	 * cell in the change as recorded - a row in the change without a {@code w:trPrChange} cleared,
	 * having had no properties (check 28) - then collapses a merged grid (check 27, C23). Rows outside
	 * the change are left.
	 */
	static void reject(Tbl tbl) {
		List<Tr> changed = new ArrayList<Tr>();
		for (Tr tr : rows(tbl)) if (inChange(tr)) changed.add(tr);
		TblPr tblPr = tbl.getTblPr();
		if (tblPr != null && tblPr.getTblPrChange() != null) {
			TblPr restored = Props.convert(tblPr.getTblPrChange().getTblPr(), CTTblPrBase.class, "tblPr", TblPr.class);
			if (restored == null) restored = new TblPr();
			restored.setParent(tbl);
			tbl.setTblPr(restored);
		}
		TblGrid grid = tbl.getTblGrid();
		boolean gridChanged = grid != null && grid.getTblGridChange() != null;
		if (gridChanged) {
			TblGrid restored = Props.convert(grid.getTblGridChange().getTblGrid(), TblGridBase.class, "tblGrid", TblGrid.class);
			if (restored == null) restored = new TblGrid();
			restored.setParent(tbl);
			tbl.setTblGrid(restored);
		}
		for (Tr tr : changed) {
			CTTblPrEx ex = tr.getTblPrEx();
			if (ex != null && ex.getTblPrExChange() != null) {
				CTTblPrEx restored = Props.convert(ex.getTblPrExChange().getTblPrEx(), CTTblPrExBase.class, "tblPrEx", CTTblPrEx.class);
				if (restored != null) restored.setParent(tr);
				tr.setTblPrEx(restored);
			}
			TrPr trPr = tr.getTrPr();
			if (trPr != null) {
				CTTrPrBase recorded = trPr.getTrPrChange() == null ? null : trPr.getTrPrChange().getTrPr();
				TrPr restored = recorded == null ? new TrPr() : Props.convert(recorded, CTTrPrBase.class, "trPr", TrPr.class);
				if (restored == null) restored = new TrPr();
				restored.setIns(trPr.getIns());
				restored.setDel(trPr.getDel());
				restored.setTrPrChange(null);
				restored.setParent(tr);
				tr.setTrPr(restored);
			}
			for (Tc tc : cells(tr)) {
				TcPr tcPr = tc.getTcPr();
				if (tcPr == null || tcPr.getTcPrChange() == null) continue;
				TcPr restored = Props.convert(tcPr.getTcPrChange().getTcPr(), TcPrInner.class, "tcPr", TcPr.class);
				if (restored == null) restored = new TcPr();
				restored.setCellIns(tcPr.getCellIns());
				restored.setCellDel(tcPr.getCellDel());
				restored.setCellMerge(tcPr.getCellMerge());
				restored.setTcPrChange(null);
				restored.setParent(tc);
				tc.setTcPr(Trees.isEmpty(restored) ? null : restored);
			}
		}
		if (gridChanged) collapseGrid(tbl);
		for (Tr tr : rows(tbl)) pruneRow(tr);
		for (Tr tr : changed) dropRedundantException(tr, tbl);
	}

	/** A row's {@code w:trPr} with nothing left in it goes, as Word writes none (check 18). */
	static void pruneRow(Tr tr) {
		if (tr.getTrPr() != null && Trees.isEmpty(tr.getTrPr())) tr.setTrPr(null);
	}

	/**
	 * A row's {@code w:tblPrEx} that says only what the table's {@code w:tblPr} says goes: Word wrote
	 * none for C23's indented row once the change was accepted or rejected.
	 */
	private static void dropRedundantException(Tr tr, Tbl tbl) {
		CTTblPrEx ex = tr.getTblPrEx();
		if (ex == null || ex.getTblPrExChange() != null) return;
		if (Trees.isEmpty(ex)) {
			tr.setTblPrEx(null);
			return;
		}
		try {
			org.w3c.dom.Element exXml = dom(ex, CTTblPrEx.class, "tblPrEx");
			org.w3c.dom.Element tblXml = dom(tbl.getTblPr(), TblPr.class, "tblPr");
			for (org.w3c.dom.Node c = exXml.getFirstChild(); c != null; c = c.getNextSibling()) {
				if (c.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
				org.w3c.dom.Element same = null;
				for (org.w3c.dom.Node t = tblXml == null ? null : tblXml.getFirstChild(); t != null; t = t.getNextSibling()) {
					if (t.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && c.getLocalName().equals(t.getLocalName())) same = (org.w3c.dom.Element) t;
				}
				if (same == null) return;
				StringBuilder a = new StringBuilder(), b = new StringBuilder();
				Props.inner((org.w3c.dom.Element) c, null, a);
				Props.inner(same, null, b);
				if (!a.toString().equals(b.toString())) return;
			}
			tr.setTblPrEx(null);
		} catch (Exception e) {
			// left as it is
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static org.w3c.dom.Element dom(Object value, Class<?> type, String local) throws Exception {
		if (value == null) return null;
		jakarta.xml.bind.Marshaller m = org.docx4j.jaxb.Context.jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.getNewDocumentBuilder().newDocument();
		m.marshal(new JAXBElement(new javax.xml.namespace.QName(Trees.W_NS, local), type, value), doc);
		return doc.getDocumentElement();
	}

	// --- a merged grid collapsed -----------------------------------------------------------------------

	private static JAXBElement<?> rowMember(Tr tr, String name) {
		if (tr.getTrPr() == null) return null;
		for (JAXBElement<?> e : tr.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
			if (name.equals(e.getName().getLocalPart())) return e;
		}
		return null;
	}

	private static int gridBefore(Tr tr) {
		JAXBElement<?> e = rowMember(tr, "gridBefore");
		if (e == null || !(e.getValue() instanceof CTTrPrBase.GridBefore)) return 0;
		BigInteger val = ((CTTrPrBase.GridBefore) e.getValue()).getVal();
		return val == null ? 0 : val.intValue();
	}

	private static void setRowMember(Tr tr, String count, String width, int value) {
		JAXBElement<?> e = rowMember(tr, count);
		if (value <= 0) {
			if (tr.getTrPr() != null) {
				tr.getTrPr().getCnfStyleOrDivIdOrGridBefore().remove(e);
				tr.getTrPr().getCnfStyleOrDivIdOrGridBefore().remove(rowMember(tr, width));
			}
			return;
		}
		if (e == null) return; // a row that skipped nothing skips nothing still
		Object v = e.getValue();
		if (v instanceof CTTrPrBase.GridBefore) ((CTTrPrBase.GridBefore) v).setVal(BigInteger.valueOf(value));
		else if (v instanceof CTTrPrBase.GridAfter) ((CTTrPrBase.GridAfter) v).setVal(BigInteger.valueOf(value));
	}

	private static int span(Tc tc) {
		if (tc.getTcPr() == null || tc.getTcPr().getGridSpan() == null || tc.getTcPr().getGridSpan().getVal() == null) return 1;
		int n = tc.getTcPr().getGridSpan().getVal().intValue();
		return n < 1 ? 1 : n;
	}

	/**
	 * Grid columns that no row keeps apart become one: the edges a row's cells (or its
	 * {@code w:gridBefore}) fall on are kept, the others merged, and the columns past every row's last
	 * cell dropped (Word's reject of C23 left two columns where the merged grid had five, the first
	 * row's {@code w:gridAfter} gone); each cell's {@code w:gridSpan} and each row's skipped columns
	 * are counted again over the columns kept.
	 */
	static void collapseGrid(Tbl tbl) {
		if (tbl.getTblGrid() == null) return;
		List<TblGridCol> cols = tbl.getTblGrid().getGridCol();
		if (cols.size() < 2) return;
		List<Tr> rows = rows(tbl);
		TreeSet<Integer> edges = new TreeSet<Integer>();
		edges.add(0);
		for (Tr tr : rows) {
			for (List<Tc> layout : layouts(tr)) {
				int at = gridBefore(tr);
				edges.add(at);
				for (Tc tc : layout) {
					at += span(tc);
					edges.add(at);
				}
			}
		}
		int last = Math.min(edges.last(), cols.size());
		List<Integer> kept = new ArrayList<Integer>();
		for (int edge : edges) if (edge <= last) kept.add(edge);
		if (kept.size() - 1 == cols.size()) return;
		List<TblGridCol> merged = new ArrayList<TblGridCol>();
		for (int i = 1; i < kept.size(); i++) {
			long w = 0;
			for (int c = kept.get(i - 1); c < kept.get(i) && c < cols.size(); c++) {
				if (cols.get(c).getW() != null) w += cols.get(c).getW().longValue();
			}
			TblGridCol col = new TblGridCol();
			col.setW(BigInteger.valueOf(w));
			col.setParent(tbl.getTblGrid());
			merged.add(col);
		}
		cols.clear();
		cols.addAll(merged);
		for (Tr tr : rows) {
			List<List<Tc>> layouts = layouts(tr);
			int before = gridBefore(tr);
			setRowMember(tr, "gridBefore", "wBefore", kept.indexOf(before));
			int end = before;
			// a layout per branch reading of the row: every branch spans the same columns, so the new
			// spans are counted along each, and the row's members from the branch Word reads (the first)
			for (int l = layouts.size() - 1; l >= 0; l--) {
				int at = before;
				for (Tc tc : layouts.get(l)) {
					int next = at + span(tc);
					int count = kept.indexOf(Math.min(next, last)) - kept.indexOf(Math.min(at, last));
					if (tc.getTcPr() != null) {
						if (count > 1) {
							TcPrInner.GridSpan gs = tc.getTcPr().getGridSpan() != null ? tc.getTcPr().getGridSpan() : new TcPrInner.GridSpan();
							gs.setVal(BigInteger.valueOf(count));
							gs.setParent(tc.getTcPr());
							tc.getTcPr().setGridSpan(gs);
						} else {
							tc.getTcPr().setGridSpan(null);
						}
					}
					at = next;
				}
				end = at;
			}
			setRowMember(tr, "gridAfter", "wAfter", kept.size() - 1 - kept.indexOf(Math.min(end, last)));
		}
	}

	// --- cells inserted and deleted --------------------------------------------------------------------

	/**
	 * A cell taken out of its row, the cell before it (else after it) widened over its grid columns,
	 * as Word did rejecting an inserted cell and accepting a deleted one (check 27: {@code w:gridSpan}
	 * added, and the widths added when both are {@code dxa}).
	 */
	static void removeCell(Tc tc, Tr tr, Removals removals, Story story) {
		List<Row> cells = Lister.cellsOf(tr);
		int at = indexOf(cells, tc);
		if (at < 0) {
			// CR-051 phase 2: a cell in a branch of a cell-level mc:AlternateContent that Word does not
			// read (a twin): its neighbours are its own branch's cells
			Object branch = Lister.branchOf(tc, tr);
			if (branch == null) return;
			cells = Lister.cellsIn(branch, tr);
			at = indexOf(cells, tc);
			if (at < 0) return;
		}
		Row self = cells.get(at);
		Tc neighbour = at > 0 ? (Tc) Trees.unwrap(cells.get(at - 1).item) : at + 1 < cells.size() ? (Tc) Trees.unwrap(cells.get(at + 1).item) : null;
		int i = Trees.indexOf(self.owner, self.item);
		if (i < 0) return;
		removals.discard(story, self.item);
		self.owner.remove(i);
		// a twin alone in its branch has no neighbour: the row's neighbour was widened once already,
		// when the read branch's cell went, and every branch spans the same grid columns
		if (neighbour == null) return;
		TcPr kept = neighbour.getTcPr();
		if (kept == null) {
			kept = new TcPr();
			kept.setParent(neighbour);
			neighbour.setTcPr(kept);
		}
		TcPrInner.GridSpan gs = new TcPrInner.GridSpan();
		gs.setVal(BigInteger.valueOf(span(neighbour) + span(tc)));
		gs.setParent(kept);
		kept.setGridSpan(gs);
		TblWidth mine = tc.getTcPr() == null ? null : tc.getTcPr().getTcW();
		TblWidth theirs = kept.getTcW();
		if (mine != null && theirs != null && "dxa".equals(mine.getType()) && "dxa".equals(theirs.getType())
				&& mine.getW() != null && theirs.getW() != null) {
			theirs.setW(theirs.getW().add(mine.getW()));
		}
	}

	private static int indexOf(List<Row> cells, Tc tc) {
		for (int i = 0; i < cells.size(); i++) if (Trees.unwrap(cells.get(i).item) == tc) return i;
		return -1;
	}

	/** A {@code w:cellMerge} resolved (never listed: Office JS cannot list one): rejected, the merge it recorded comes back (docx4j's reading, U8). */
	static void resolveCellMerge(TcPr tcPr, boolean accept) {
		CTCellMergeTrackChange change = tcPr.getCellMerge();
		if (change == null) return;
		tcPr.setCellMerge(null);
		if (accept) return;
		STAnnotationVMerge orig = change.getVMergeOrig();
		if (orig == STAnnotationVMerge.REST) {
			TcPrInner.VMerge vm = new TcPrInner.VMerge();
			vm.setVal("restart");
			vm.setParent(tcPr);
			tcPr.setVMerge(vm);
		} else if (orig == STAnnotationVMerge.CONT) {
			TcPrInner.VMerge vm = new TcPrInner.VMerge();
			vm.setParent(tcPr);
			tcPr.setVMerge(vm);
		} else {
			tcPr.setVMerge(null);
		}
	}

	// --- sections ----------------------------------------------------------------------------------

	/**
	 * A section's properties put back as its {@code w:sectPrChange} recorded them. Word records only
	 * what changed, and rejecting laid those over the section, the rest left as it stood (check 18; a
	 * paragraph's section, C07); a recorded {@code w:docGrid} of pitch 0 stands for none, and Word
	 * wrote none; the header and footer references are not in the record and stay (U13).
	 */
	static void rejectSection(SectPr sectPr) {
		SectPrBase recorded = sectPr.getSectPrChange() == null ? null : sectPr.getSectPrChange().getSectPr();
		sectPr.setSectPrChange(null);
		if (recorded == null) return;
		SectPr restored = Props.convert(recorded, SectPrBase.class, "sectPr", SectPr.class);
		if (restored == null) return;
		for (Field f : Trees.fieldsOf(SectPr.class)) {
			String name = f.getName();
			if ("egHdrFtrReferences".equals(name) || "sectPrChange".equals(name) || name.startsWith("rsid")) continue;
			try {
				Object value = f.get(restored);
				if (value == null || value instanceof List) continue;
				f.set(sectPr, value);
				Trees.setParent(value, sectPr);
			} catch (IllegalAccessException e) {
				// a member this JVM will not let us set: left as it stands
			}
		}
		CTDocGrid grid = sectPr.getDocGrid();
		if (grid != null && (grid.getLinePitch() == null || grid.getLinePitch().signum() == 0)
				&& grid.getType() == null && grid.getCharSpace() == null) {
			sectPr.setDocGrid(null);
		}
	}
}
