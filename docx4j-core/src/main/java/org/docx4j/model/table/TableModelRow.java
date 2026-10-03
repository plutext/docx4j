package org.docx4j.model.table;

import java.util.List;
import java.util.Vector;

import org.docx4j.wml.CTTblPrEx;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;

/**
 * This class introduced so we have somewhere to
 * store trPr.
 * 
 * @author jharrop
 *
 */
public class TableModelRow {
	
	public TableModelRow(Tr tr) {
		
		this.tr = tr;
		trPr = tr.getTrPr();
		tblPrEx = tr.getTblPrEx();
	}

	private Tr tr;

	/** The w:tr this row was built from, for its place in the table
	 *  ({@link TableContext#rowConditions}).  @since 17.3.1 */
	public Tr getTr() {
		return tr;
	}
	
	private List<TableModelCell> rowContents = new Vector<TableModelCell>();	
	
	private TrPr trPr;
	private CTTblPrEx tblPrEx;
	
	public TrPr getRowProperties() {
		return trPr;
	}

	/**
	 * Replace this row's properties.  Pass a copy: the TrPr a row starts with belongs
	 * to the document being converted.
	 *
	 * @since 17.0.5
	 */
	public void setRowProperties(TrPr trPr) {
		this.trPr = trPr;
	}
	
	public CTTblPrEx getRowPropertiesExceptions() {
		return tblPrEx;
	}
	
	
	public List<TableModelCell> getRowContents() {
		return rowContents;
	}
	
	public void add(TableModelCell newCell) {
		rowContents.add(newCell);
	}
	
	public TableModelCell get(int i) {
		return rowContents.get(i);
	}

	
	public int size() {
		return rowContents.size();
	}
}
