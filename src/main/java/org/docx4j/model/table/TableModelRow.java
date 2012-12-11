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
		
		trPr = tr.getTrPr();
		tblPrEx = tr.getTblPrEx();
	}
	
	private List<Cell> rowContents = new Vector<Cell>();	
	
	private TrPr trPr;
	private CTTblPrEx tblPrEx;
	
	public TrPr getRowProperties() {
		return trPr;
	}
	
	public CTTblPrEx getRowPropertiesExceptions() {
		return tblPrEx;
	}
	
	
	public List<Cell> getRowContents() {
		return rowContents;
	}
	
	public void add(Cell newCell) {
		rowContents.add(newCell);
	}
	
	public Cell get(int i) {
		return rowContents.get(i);
	}
	
	public int size() {
		return rowContents.size();
	}
}
