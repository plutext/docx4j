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
	
	private List<TableModelCell> rowContents = new Vector<TableModelCell>();	
	
	private TrPr trPr;
	private CTTblPrEx tblPrEx;
	
	public TrPr getRowProperties() {
		return trPr;
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

//	public TableModelCell getReal(int i) {
//
//		int realCellCount=-1;
//		for (TableModelCell cell :  rowContents) {
//			
//			if (!cell.isDummy() ) {
//				realCellCount++;
//			}
//			
//			if (realCellCount==i) return cell;
//		}
//		return null;
//	}

	public TableModelCell getReal(int i) {

		for (TableModelCell cell :  rowContents) {
						
			if (cell.getColumn()==i) return cell;
		}
		return null;
	}
	
	public int size() {
		return rowContents.size();
	}
}
