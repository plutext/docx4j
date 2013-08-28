package org.docx4j.convert.out.common.writer;

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
public class AbstractTableWriterModelRow {
	
	public AbstractTableWriterModelRow(Tr tr) {
		
		trPr = tr.getTrPr();
		tblPrEx = tr.getTblPrEx();
	}
	
	private List<AbstractTableWriterModelCell> rowContents = new Vector<AbstractTableWriterModelCell>();	
	
	private TrPr trPr;
	private CTTblPrEx tblPrEx;
	
	public TrPr getRowProperties() {
		return trPr;
	}
	
	public CTTblPrEx getRowPropertiesExceptions() {
		return tblPrEx;
	}
	
	
	public List<AbstractTableWriterModelCell> getRowContents() {
		return rowContents;
	}
	
	public void add(AbstractTableWriterModelCell newCell) {
		rowContents.add(newCell);
	}
	
	public AbstractTableWriterModelCell get(int i) {
		return rowContents.get(i);
	}
	
	public int size() {
		return rowContents.size();
	}
}
