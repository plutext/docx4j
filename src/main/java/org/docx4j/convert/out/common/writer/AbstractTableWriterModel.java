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

package org.docx4j.convert.out.common.writer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.finders.TcFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.table.TableModelRow;
import org.docx4j.model.table.TableModel;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTrPrBase;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * This model captures:
 * - whether the table layout is fixed or auto (Word usually does auto)
 * - whether conflict resolution is required on cell borders (Word usually 
 *   does conflict resolution)
 * 
 *  @author Adam Schmideg
 *  @author Alberto Zerolo
 *  @author Jason Harrop
 * 
 */
public class AbstractTableWriterModel extends TableModel {
	
	private final static Logger log = LoggerFactory.getLogger(AbstractTableWriterModel.class);

	// We don't need this in our table model,
	// at least for HTML. (PropertyFactory takes care of it)
	
//	boolean tableLayoutFixed = false; // default to auto
//	/**
//	 * @return isTableLayoutFixed
//	 */
//	public boolean isTableLayoutFixed() {
//		return tableLayoutFixed;
//	}

	/**
	 * Add a new cell to this table and copy processed content of
	 * <var>tc</var> to it.
	 */
	public void addCell(Tc tc, Node content) {
		log.debug("Add tc row " + row + " col 1+" + col);
		addRow(new AbstractTableWriterModelCell(this, row, ++col, tc, content));
		
		// add dummy cells
		if (tc.getTcPr()!=null 
				&& tc.getTcPr().getGridSpan()!=null 
				&& tc.getTcPr().getGridSpan().getVal() !=null) {
			
			int gridSpan = tc.getTcPr().getGridSpan().getVal().intValue();
			addDummyCell(gridSpan, false, false);
		}
		
	}

	protected void addRow(AbstractTableWriterModelCell cell) {
		rows.get(row).add(cell);
	}
    
	/**
	 * Build a table representation from a <var>tbl</var> instance.
	 * Remember to set wordMLPackage before using this method!
	 */
	public void build(AbstractWmlConversionContext conversionContext, Object node, Node content) throws TransformerException {
		Tbl tbl = null;
		try {
			tbl = (Tbl)node;
		} catch (ClassCastException e) {
			throw new TransformerException("Node is not of the type Tbl it is " + node.getClass().getName());
		}

		if (tbl.getTblPr()!=null
				&& tbl.getTblPr().getTblStyle()!=null) {
			styleId = tbl.getTblPr().getTblStyle().getVal();			
		}
		
		
		this.tblGrid = tbl.getTblGrid();
		
		this.tblPr = tbl.getTblPr();
		
		PropertyResolver pr = conversionContext.getPropertyResolver();
			
		effectiveTableStyle = pr.getEffectiveTableStyle(tbl.getTblPr() );
//	    if (tblPr!=null
//	    		&& tblPr.getTblW()!=null) {
//	    	if (tblPr.getTblW().getType()!=null 
//	    			&& (tblPr.getTblW().getType().equals("auto")
//	    					|| tblPr.getTblW().getType().equals("nil") )) {
//	    		// @w:type
//	    					// nil, per Word 2007 implementation note
//	    		tableLayoutFixed = false;
//	    	} else if (tblPr.getTblW().getW()!=null ){
//	    		// @w:w
//	    		if (tblPr.getTblW().getW() == BigInteger.ZERO) {
//	    			// Word 2007 implementation note
//	    			tableLayoutFixed = false;
//	    		} else {
//	    			tableLayoutFixed = true;
//	    		}
//	    	} else {
//	    		// no attributes!!
//	    		tableLayoutFixed = false;
//	    	}
//	    } else {
//	    	// element omitted, so type is auto (2.4.61)
//	    	tableLayoutFixed = false;
//	    }
		
		
		NodeList cellContents = content.getChildNodes(); // the w:tr
		
		TrFinder trFinder = new TrFinder();
		new TraversalUtil(tbl, trFinder);
		
		ensureFoTableBody(trFinder.getTrList()); // this is currently applied to HTML etc as well
		
		int r = 0;
		for (Tr tr : trFinder.getTrList()) {
				startRow(tr);
				handleRow(cellContents, tr, r);
				r++;
				if (rows.get(row).getRowContents().isEmpty()) {
					rows.remove(row);
					row--;
					r--;
				}
		}
		
		CTTblPrBase tblPr = effectiveTableStyle.getTblPr();
		if (tblPr != null) {
			if (tblPr.getTblCellSpacing()!=null) {
				setBorderConflictResolutionRequired( false);							
			}
		}
		
		width = calcTableWidth();
	}
	
	/**
	 * "fo:table" content model is: (marker*,table-column*,table-header?,table-footer?,table-body+)
	 * ie table-header (if any) must precede table-body
	 * 
	 * The first requirement is that there is a table-body. Since the docx format doesn't
	 * have any equivalent to table-footer, we can always treat the last row as table-body.
	 * 
	 * The second requirement is that there is no table-header after table-body.
	 * We could either treat each t-h after a t-b as t-b,
	 * or we could treat all t-b before t-h as t-h.  
	 * 
	 * If the docx has normal rows before the a t-h row, the user should split the table into 
	 * two.  Since they can do that, we'll treat all rows before last t-h row as t-h rows
	 * 
	 */
	private void ensureFoTableBody(List<Tr> rows) {
		
		int numRows = rows.size();
		
		if (numRows==0) {
			log.warn("Encountered table with no rows");
			return;
		}
		
		// Req 1: Make sure the last row is not a header row
		Tr lastRow = rows.get(numRows-1);
		if (isHeaderRow(lastRow)) {
			List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore = lastRow.getTrPr().getCnfStyleOrDivIdOrGridBefore();
			JAXBElement tblHeader = getElement(cnfStyleOrDivIdOrGridBefore, "tblHeader");
			cnfStyleOrDivIdOrGridBefore.remove(tblHeader);
		}
		
		// Req 2: All rows before last header row become header rows
		// .. find last header row
		int indexOfLastHeaderRow=-1;
		for (int i = rows.size(); i>0; i--) {
			Tr tr = rows.get(i-1);
			if (isHeaderRow(tr)) {
				indexOfLastHeaderRow = i-1;
				break;
			}
		}
		// .. now convert all rows up to that one
		for (int i = 0; i<indexOfLastHeaderRow; i++) {
			Tr tr = rows.get(i);
			if (!isHeaderRow(tr)) {
				// make it so...
				TrPr trpr = tr.getTrPr();
				if (trpr == null) {
					trpr = Context.getWmlObjectFactory().createTrPr(); 
				    tr.setTrPr(trpr); 
				}
		        // Create object for tblHeader (wrapped in JAXBElement) 
		        BooleanDefaultTrue booleandefaulttrue = Context.getWmlObjectFactory().createBooleanDefaultTrue(); 
		        JAXBElement<org.docx4j.wml.BooleanDefaultTrue> booleandefaulttrueWrapped 
		        	= Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(booleandefaulttrue); 
		        trpr.getCnfStyleOrDivIdOrGridBefore().add( booleandefaulttrueWrapped); 					
			}
		}
	}
	

	protected void handleRow(NodeList cellContents, Tr tr, int r) {
		int gridAfter = getGridAfter(tr);
		int gridBefore = getGridBefore(tr);
		boolean headerRow = isHeaderRow(tr);

		log.debug("Processing r " + r);
		
		if (isBorderConflictResolutionRequired() && tr.getTblPrEx() != null
				&& tr.getTblPrEx().getTblCellSpacing() != null) {
			setBorderConflictResolutionRequired(false);
		}
				
		if (headerRow && (headerMaxRow < r)) {
			headerMaxRow = r;
		}
		
		if (drawTableBorder) {
			drawTableBorder = (gridBefore == 0) && (gridAfter == 0);
		}
		
		TcFinder tcFinder = new TcFinder();
		new TraversalUtil(tr, tcFinder);
		
		//add dummy cell for gridBefore
		if (gridBefore > 0) {
			addDummyCell(gridBefore, true, false);
		}
		
		//List<Object> cells = tr.getEGContentCellContent();
		int c = 0;
		log.debug("Processing c " + c);
		for (Tc tc : tcFinder.tcList) {

			Node wtrNode = cellContents.item(r); // w:tr
			if (wtrNode==null ) {
				log.warn("Couldn't find item " + r);
			}
			addCell(tc, getTc(wtrNode, c, new IntRef(0))); // the cell content
			// addCell(tc, cellContents.item(i));
			// i++;
			c++;
		}

		//add dummy cell for gridAfter
		if (gridAfter > 0) {
			addDummyCell(gridAfter, false, true);
		}
	}
	

	/**
	 * The tc could be inside something else, so find it recursively.
	 * @param wtrNode
	 * @param wanted
	 * @param current
	 * @return
	 */
	private Node getTc(Node wtrNode, int wanted, IntRef current) {
				
		for (int i=0; i<wtrNode.getChildNodes().getLength(); i++ ) {
			
			Node thisChild = wtrNode.getChildNodes().item(i);
			
			if (thisChild.getNodeType()!= 1){
				continue;  
			}
			
			log.debug("Looking at " + thisChild.getLocalName() + "; have encountered " + current.i);
			
			if (thisChild.getLocalName().equals("tc") ) {
				if (current.i==wanted) return thisChild;
				current.increment();
			} else {
				// could be inside
				Node n = getTc(thisChild, wanted, current);
				if (n!=null) return n;
			}
		}
		log.error("Couldn't find tc in: " + XmlUtils.w3CDomNodeToString(wtrNode));
		
		return null;
	}
	
	static class IntRef {
		
		IntRef(int i) {
			this.i = i;
		}
		
		int i;
		
		void increment() {
			i++;
		}
		
	}

}
