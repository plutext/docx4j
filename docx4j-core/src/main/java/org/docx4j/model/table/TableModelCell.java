/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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

package org.docx4j.model.table;

import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
//import org.w3c.dom.Node;

/**
 * There are different ways to represent a table with possibly merged
 * cells. <ul>
 * <li>In html, both vertically and horizontally merged cells are
 * represented by one cell only that has a colspan and rowspan
 * attribute.  No dummy cells are used.
 * <li>In docx, horizontally merged cells are represented by one cell
 * with a gridSpan attribute; while vertically merged cells are
 * represented as a top cell containing the actual content and a series
 * of dummy cells having a vMerge tag with "continue" attribute.
 * <li>This table is a regular matrix, dummy cells are added for both
 * merge directions.
 * </ul>
 * The algorithm is as follows,<ul>
 * <li>When a cell is added, its colspan is set.  Even a dummy cell can have
 * a colspan, the same value as its upper has.
 * <li>When a new cell has a colspan greater than 1, the required extra
 * dummy cells are also added
 * <li>When a docx dummy cell is encountered (one with a vMerge
 * continue attribute), the rowspan is incremented in its upper
 * neighbors until a real cell is found.
 * </ul>
 * 
 *  @author Adam Schmideg
 * 
 */
	/**
 * A cell in the table holding its own content, too
 */
public class TableModelCell {
	
	private final static Logger logger = LoggerFactory.getLogger(TableModelCell.class);
	
	private TableModel table;
	private int row;
	/**
	 * this col number is not intuitive;
	 * it increments for each real cell, and again so that it is the same for 
	 * all following placeholder cells! 
	 */
	private int col;
	protected int rowspan = 0;
	
	private int colspan = 0;
	public int getColspan() {
		return colspan;
	}
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	
	/** If this is a real cell or only a placeholder.  Vertically merged
	 * cells are represented as a real cell on the top and dummy cell(s)
	 * below */
	protected boolean dummy = false;
	
	/** In XSL FO, we need to write cells for before & after, 
	 * but not dummy cells for horizontal merge */
	protected boolean dummyBefore = false;
	public boolean isDummyBefore() {
		return dummyBefore;
	}
	protected boolean dummyAfter = false;
	public boolean isDummyAfter() {
		return dummyAfter;
	}

	
	
//	protected Node content = null;
	
	protected TcPr tcPr;
	public TcPr getTcPr() {
		return tcPr;
	}

	/**
	 * Create a dummy cell without content
	 */
	public TableModelCell(TableModel table, int row, int col) {
		this.table = table;
		this.row = row;
		this.col = col;
		this.dummy = true;
	}

	public TableModelCell(TableModel table, int row, int col, Tc tc) {
		
		this(table, row, col);
		dummy = false;
		
		tcPr = tc.getTcPr();

	      if(tcPr !=null && logger.isDebugEnabled()) {
	    	  
		      logger.debug("Cell props for row " + row + ", col " + col + "\n" 
		      		+ XmlUtils.marshaltoString(tcPr, true, true, Context.jc, 
		      				"http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tcPr", 
		      				org.docx4j.wml.TcPr.class));
		  }
		

		// rowspan
		try {
			String vm = tc.getTcPr().getVMerge().getVal();
			if (vm == null || vm.equals("continue")) {
				dummy = true;				
			}
			// dummy cells propagate this call upwards until a real cell is found
			incrementRowSpan();
		} catch (NullPointerException ne) {
			// no vMerge
		}
		
		if (dummy) {
			// set its colspan to the same value as its upper neighbor,
			// so dummy cells will be created to the right if colspan>1
			try {
				colspan = table.getCell(row - 1, col).colspan;
			} catch (NullPointerException ne) {
				logger.warn("Problem at row " + row + " -1, col " + col);
				logger.warn("model so far: \n" + this.table.debugStr() );
				logger.warn("and this cell: " + this.debugStr() );
				logger.warn(ne.getMessage(), ne);
				logger.warn("Problems with table " + XmlUtils.marshaltoString(table.tbl));
			}
				
		} else {
			// real cell
			// colspan
			try {
				int gridSpan = tc.getTcPr().getGridSpan().getVal().intValue();
				colspan = gridSpan;
			} catch (NullPointerException ne) {
				// no gridSpan
			}
		}
	}

	/**
	 * How many columns are merged into this cell
	 * @return 0 if none merged; 1 if two cells are merged so there is one
	 * extra; etc.  A dummy cell has the same extraCols value as its upper
	 * neighbor.
	 */
	public int getExtraCols() {
		if (getColspan() < 2)
			return 0;
		else
			return getColspan() - 1;
	}

	public int getExtraRows() {
		if (rowspan > 1)
			return rowspan - 1;
		else
			return 0;
	}

	public boolean isDummy() {
		return dummy;
	}

	public int getColumn() {
		return col;
	}

	/**
	 * If this is a real cell, increment rowspan; if this is a dummy,
	 * propagate the call to the cell upwards
	 */
	protected void incrementRowSpan() {
		if (dummy) {
			logger.debug("dummy=true for row " + row + ", col " + col + " so propogate to r-1");
			if (row>0) {
				table.getCell(row - 1, col).incrementRowSpan();
			} else {
				logger.debug(".. but already at row 0; using rowspan=" + rowspan);
			}
		} else {
			logger.debug("incremented rowspan for row " + row + ", col " + col );
			rowspan++;
			
//			if (logger.isDebugEnabled()) {
//				logger.debug("\n\n"+ this.table.debugStr());
//			}
			
		}
	}

	public String debugStr() {
		String s = null;
		if (dummy)
			s = "d";
		else
			s = "r";
		s += "(" + row + "," + col + ")";
		s += getColspan();
		s += rowspan;
		return s + " ";
	}

	/*
	 * @since 3.0.0
	 */
	public boolean isVMerged() {
		return (tcPr != null) && 
			   (tcPr.getVMerge() != null);
	}

}
