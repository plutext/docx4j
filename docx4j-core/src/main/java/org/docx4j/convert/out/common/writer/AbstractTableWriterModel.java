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

import jakarta.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.finders.TcFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.table.TableModelCell;
import org.docx4j.model.table.TableModelRow;
import org.docx4j.model.table.TableStyleConditions;
import org.docx4j.model.table.TableStyleConditions.Look;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.model.table.TableModel;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTCnf;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.STTblStyleOverrideType;
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

	/**
	 * The {@code w:tbl} this model was built from, so that a writer can ask where in the
	 * document the table sits - in particular which cell a nested table is in
	 * (AbstractTableWriter.containingCellWidthTwips).
	 *
	 * <p>Its parent pointers ({@code org.jvnet.jaxb.lang.Child}) are set only where the
	 * table was reached through the document's own object tree, which is the visitor
	 * pathway; in the XSLT pathway the {@code w:tbl} was unmarshalled on its own from the
	 * DOM and has none.</p>
	 *
	 * @since 17.1.1
	 */
	private Tbl tbl;

	/** @since 17.1.1 */
	public Tbl getTbl() {
		return tbl;
	}

	/** Column widths in twips decided by autofit (see AbstractTableWriter.computeAutofitColumnWidths),
	 *  or null to use the grid.  @since 17.0.5 */
	private int[] autofitColumnWidths;

	public int[] getAutofitColumnWidths() {
		return autofitColumnWidths;
	}

	public void setAutofitColumnWidths(int[] widths) {
		this.autofitColumnWidths = widths;
	}

	/** Whether the column widths came from Word's content-based autofit pass, as opposed
	 *  to the w:tblGrid (scaled to the page or not).  @since 17.1.0 */
	private boolean contentSizedColumns;

	public boolean isContentSizedColumns() {
		return contentSizedColumns;
	}

	public void setContentSizedColumns(boolean contentSized) {
		this.contentSizedColumns = contentSized;
	}

	/**
	 * What the content-based autofit pass worked from: the per-column minimum and maximum
	 * content widths and preferred widths it measured, in twips, and the width it fitted
	 * them into.  Kept so that the page fit can share a shortfall by the columns' content
	 * rather than by proportion ({@code AbstractTableWriter.fitToAvailableWidth}) and so
	 * that the diagnostic dump can record them.  All arrays are one entry per column.
	 *
	 * @since 17.1.1
	 */
	public static final class AutofitInputs {
		/** Widest unbreakable content plus cell margins, per column. */
		public final int[] min;
		/** Content unwrapped plus cell margins, per column. */
		public final int[] max;
		/** w:tcW per column, or -1 for an auto column. */
		public final int[] preferred;
		/** The incompressible part of each column: its cell margins plus the widest picture
		 *  it holds - what a shortfall cannot take. */
		public final int[] floor;
		/** The width the pass fitted the columns into. */
		public final int available;

		public AutofitInputs(int[] min, int[] max, int[] preferred, int[] floor, int available) {
			this.min = min;
			this.max = max;
			this.preferred = preferred;
			this.floor = floor;
			this.available = available;
		}
	}

	/** Null where the content pass did not measure the table (a fixed layout, or content this
	 *  output format cannot measure).  Set whether or not the pass went on to size the columns:
	 *  {@link #isContentSizedColumns()} says whether it did.  @since 17.1.1 */
	private AutofitInputs autofitInputs;

	public AutofitInputs getAutofitInputs() {
		return autofitInputs;
	}

	public void setAutofitInputs(AutofitInputs inputs) {
		this.autofitInputs = inputs;
	}

	/* The table style's conditional formatting (w:tblStylePr), resolved for this table:
	 * the look its w:tblLook asks for, its band sizes, and where each w:tr sits, so that a
	 * row's or cell's conditions can be worked out (TableStyleConditions).  @since 17.1.1 */
	private Look look = Look.DEFAULT;
	private int rowBandSize = 1;
	private int colBandSize = 1;
	private java.util.IdentityHashMap<Tr, Integer> trIndex;
	private int trCount;
	/** whether the header rows come from a conditional w:tblHeader rather than the rows' own */
	private boolean headerFromStyle;

	/** The conditional formats the table's w:tblLook asks for.  @since 17.1.1 */
	public Look getLook() {
		return look;
	}

	/** Whether the effective table style has any conditional formatting at all. */
	private boolean hasConditionalFormatting() {
		return effectiveTableStyle != null && effectiveTableStyle.getTblStylePr() != null
				&& !effectiveTableStyle.getTblStylePr().isEmpty();
	}

	/**
	 * The conditions the row at this index is under (first row, last row, a band), for its
	 * row properties.
	 * @since 17.1.1
	 */
	public java.util.EnumSet<STTblStyleOverrideType> rowConditions(int rowIndex, TrPr trPr) {
		return TableStyleConditions.rowConditions(look, rowBandSize, rowIndex, rows.size(),
				TableStyleConditions.rowCnf(trPr));
	}

	/**
	 * The conditions the cell is under, from the row's and the cell's w:cnfStyle caches
	 * where they have them and from the position where they do not.
	 * @since 17.1.1
	 */
	public java.util.EnumSet<STTblStyleOverrideType> cellConditions(int rowIndex, TableModelCell cell, TrPr trPr) {
		CTCnf cellCnf = cell.getTcPr() == null ? null : cell.getTcPr().getCnfStyle();
		return TableStyleConditions.resolve(look, rowBandSize, colBandSize,
				rowIndex, rows.size(), cell.getColumn(), Math.max(1, cell.getColspan()), getColCount(),
				TableStyleConditions.rowCnf(trPr), cellCnf, null);
	}

	/**
	 * The effective table style's w:tblStylePr entries which apply under these conditions,
	 * in the order they are to be applied; empty where the style has none.
	 * @since 17.1.1
	 */
	public List<CTTblStylePr> applicable(java.util.Set<STTblStyleOverrideType> conditions) {
		if (!hasConditionalFormatting()) return new ArrayList<CTTblStylePr>();
		return TableStyleConditions.applicable(effectiveTableStyle, conditions);
	}

	/**
	 * The rows of the horizontal band containing the row, or null; the columns of the
	 * vertical band containing the column, or null (TableStyleConditions).
	 * @since 17.1.1
	 */
	public int[] bandRows(int rowIndex) {
		return TableStyleConditions.hBandRows(look, rowBandSize, rowIndex, rows.size());
	}

	public int[] bandCols(int col) {
		return TableStyleConditions.vBandCols(look, colBandSize, col, getColCount());
	}

	/**
	 * A row is a header row if its own w:trPr says w:tblHeader, or - since 17.1.1 - if the
	 * table style's conditional formatting for its position does: Word's built-in styles
	 * give the first row {@code <w:trPr><w:tblHeader/></w:trPr>} under {@code firstRow}, so
	 * the header of every table using them repeats across pages, where docx4j repeated
	 * none.  The last row is never a conditional header (fo:table-body needs a row, see
	 * ensureFoTableBody), nor is any row where the style's w:tblLook has the condition off.
	 */
	@Override
	protected boolean isHeaderRow(Tr tr) {
		if (super.isHeaderRow(tr)) return true;
		if (trIndex == null || !hasConditionalFormatting()) return false;
		Integer r = trIndex.get(tr);
		if (r == null || r.intValue() >= trCount - 1) return false;
		java.util.EnumSet<STTblStyleOverrideType> conditions = TableStyleConditions.rowConditions(
				look, rowBandSize, r.intValue(), trCount, TableStyleConditions.rowCnf(tr.getTrPr()));
		TrPr conditional = TableStyleConditions.conditionalTrPr(
				TableStyleConditions.applicable(effectiveTableStyle, conditions));
		if (TableStyleConditions.hasTblHeader(conditional)) {
			headerFromStyle = true;
			return true;
		}
		return false;
	}

	/**
	 * A header row which a cell spans out of cannot be written: FOP rejects a
	 * fo:table-cell whose number-rows-spanned runs past the end of the fo:table-header.  A
	 * row's own w:tblHeader is left as it always was; where the header status came from
	 * the table style alone, it is given up for such a table.
	 */
	private void dropConditionalHeaderIfSpanned() {
		if (!headerFromStyle || headerMaxRow < 0) return;
		for (int r = 0; r <= headerMaxRow && r < rows.size(); r++) {
			for (TableModelCell cell : rows.get(r).getRowContents()) {
				if (!cell.isDummy() && r + cell.getExtraRows() > headerMaxRow) {
					log.debug("a cell of conditional header row " + r + " spans into the body; not repeating the header");
					headerMaxRow = -1;
					return;
				}
			}
		}
	}

	/** The table width in twips: the sum of the autofit widths when set, else the grid's. */
	@Override
	public int getTableWidth() {
		if (autofitColumnWidths != null) {
			int sum = 0;
			for (int w : autofitColumnWidths) sum += w;
			return sum;
		}
		return super.getTableWidth();
	}
	
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
	 * @throws CyclicStylesException 
	 */
	public void build(AbstractWmlConversionContext conversionContext, Object node, Node content) throws TransformerException {
		Tbl tbl = null;
		try {
			tbl = (Tbl)node;
		} catch (ClassCastException e) {
			throw new TransformerException("Node is not of the type Tbl it is " + node.getClass().getName());
		}

		this.tbl = tbl;

		if (tbl.getTblPr()!=null
				&& tbl.getTblPr().getTblStyle()!=null) {
			styleId = tbl.getTblPr().getTblStyle().getVal();
		}


		this.tblGrid = tbl.getTblGrid();
		
		this.tblPr = tbl.getTblPr();
		
		PropertyResolver pr;			
		try {
			pr = conversionContext.getPropertyResolver();
			effectiveTableStyle = pr.getEffectiveTableStyle(tbl.getTblPr() );
		} catch (Docx4JException e) {
			throw new TransformerException(e);
		}
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

		// the table's w:tblLook and band sizes (the effective tblPr has the table's own
		// merged over the style's), and where each row sits, for the conditional formatting
		CTTblPrBase effectiveTblPr = effectiveTableStyle.getTblPr();
		look = TableStyleConditions.look(effectiveTblPr);
		rowBandSize = TableStyleConditions.rowBandSize(effectiveTblPr);
		colBandSize = TableStyleConditions.colBandSize(effectiveTblPr);
		trIndex = new java.util.IdentityHashMap<Tr, Integer>();
		trCount = trFinder.getTrList().size();
		for (int i = 0; i < trCount; i++) {
			trIndex.put(trFinder.getTrList().get(i), Integer.valueOf(i));
		}
		
		ensureFoTableBody(trFinder.getTrList()); // this is currently applied to HTML etc as well
		
		int r = 0;      // index of the w:tr in the converted content
		for (Tr tr : trFinder.getTrList()) {
				startRow(tr);
				handleRow(cellContents, tr, r);
				r++;
				if (rows.get(row).getRowContents().isEmpty()) {
					// a w:tr with no w:tc (Word renders nothing for it); r is not
					// decremented, since it indexes the converted content, in which
					// this row is still present
					rows.remove(row);
					row--;
				}
		}

		// rows which are wholly covered by merges from elsewhere can't be written
		dropFullySpannedRows();

		// and a conditional header row a cell spans out of can't be a header
		dropConditionalHeaderIfSpanned();

		// and where the rows are wider than w:tblGrid, the grid follows the rows
		extendGridToWidestRow();

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
		if (isHeaderRow(lastRow) && lastRow.getTrPr() != null) {
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
