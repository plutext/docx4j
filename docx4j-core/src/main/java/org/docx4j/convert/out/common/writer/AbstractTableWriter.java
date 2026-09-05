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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.table.AbstractBorder;
import org.docx4j.model.properties.table.BorderBottom;
import org.docx4j.model.properties.table.BorderLeft;
import org.docx4j.model.properties.table.BorderRight;
import org.docx4j.model.properties.table.BorderTop;
import org.docx4j.model.properties.table.CellMarginBottom;
import org.docx4j.model.properties.table.CellMarginLeft;
import org.docx4j.model.properties.table.CellMarginRight;
import org.docx4j.model.properties.table.CellMarginTop;
import org.docx4j.model.properties.table.tc.Shading;
import org.docx4j.model.properties.table.tc.TextAlignmentVertical;
import org.docx4j.model.properties.table.tr.TrCantSplit;
import org.docx4j.model.properties.table.tr.TrHeight;
import org.docx4j.model.table.TableModelCell;
import org.docx4j.model.table.TableModelRow;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblPrEx;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STShd;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 *  @author Alberto Zerolo, Adam Schmideg, Jason Harrop
 *  @since 3.0.0
 *  
*/
public abstract class AbstractTableWriter extends AbstractSimpleWriter {
	
	private static Logger log = LoggerFactory.getLogger(AbstractTableWriter.class);
	
	
	public static final String WRITER_ID = "w:tbl";
	
  
  protected static final int NODE_TABLE = 0;
  protected static final int NODE_TABLE_COLUMN_GROUP = 1;
  protected static final int NODE_TABLE_COLUMN = 2;
  protected static final int NODE_TABLE_HEADER = 3;
  protected static final int NODE_TABLE_HEADER_ROW = 4;
  protected static final int NODE_TABLE_HEADER_CELL = 5;
  protected static final int NODE_TABLE_BODY = 6;
  protected static final int NODE_TABLE_BODY_ROW = 7;
  protected static final int NODE_TABLE_BODY_CELL = 8;
  
  protected static final Map<String, Integer> PATTERN_PERCENTAGES = new TreeMap<String, Integer>();
  
  static {
	  /*
	   * These patterns cause a reset
	  PATTERN_PERCENTAGES.put("clear", -1);
	  PATTERN_PERCENTAGES.put("nil", -1);

	   * and these can't be aproximated by a background color
	   * so they are ignored
	  PATTERN_PERCENTAGES.put("diagStripe", -1);
	  PATTERN_PERCENTAGES.put("horzStripe", -1);
	  PATTERN_PERCENTAGES.put("thinDiagStripe", -1);
	  PATTERN_PERCENTAGES.put("thinHorzStripe", -1);
	  PATTERN_PERCENTAGES.put("thinReverseDiagStripe", -1);
	  PATTERN_PERCENTAGES.put("thinVertStripe", -1);
	  PATTERN_PERCENTAGES.put("vertStripe", -1);
	   */
	  
	  
	  // These Patterns are aproximated by a background color
	  PATTERN_PERCENTAGES.put("diagCross", 50);
	  PATTERN_PERCENTAGES.put("horzCross", 50);

	  PATTERN_PERCENTAGES.put("thinDiagCross", 25);
	  PATTERN_PERCENTAGES.put("thinHorzCross", 25);

	  PATTERN_PERCENTAGES.put("pct5", 5);
	  PATTERN_PERCENTAGES.put("pct10", 10);
	  PATTERN_PERCENTAGES.put("pct12", 12);
	  PATTERN_PERCENTAGES.put("pct15", 15);
	  PATTERN_PERCENTAGES.put("pct20", 20);
	  PATTERN_PERCENTAGES.put("pct25", 25);
	  PATTERN_PERCENTAGES.put("pct30", 30);
	  PATTERN_PERCENTAGES.put("pct35", 35);
	  PATTERN_PERCENTAGES.put("pct37", 37);
	  PATTERN_PERCENTAGES.put("pct40", 40);
	  PATTERN_PERCENTAGES.put("pct45", 45);
	  PATTERN_PERCENTAGES.put("pct50", 50);
	  PATTERN_PERCENTAGES.put("pct55", 55);
	  PATTERN_PERCENTAGES.put("pct60", 60);
	  PATTERN_PERCENTAGES.put("pct62", 62);
	  PATTERN_PERCENTAGES.put("pct65", 65);
	  PATTERN_PERCENTAGES.put("pct70", 70);
	  PATTERN_PERCENTAGES.put("pct75", 75);
	  PATTERN_PERCENTAGES.put("pct80", 80);
	  PATTERN_PERCENTAGES.put("pct85", 85);
	  PATTERN_PERCENTAGES.put("pct87", 87);
	  PATTERN_PERCENTAGES.put("pct90", 90);
	  PATTERN_PERCENTAGES.put("pct95", 95);
	  PATTERN_PERCENTAGES.put("solid", 100);
  }

	
	protected static class TableModelTransformState implements TransformState {
		
		// The last table number, in document order,
		// which we have processed. 
		// The idea is to be able to write an id (unique within the document) to each
		// table.
		int idx = 0;

		/**
		 * @return the idx
		 */
		public int getIdx() {
			return idx;
		}

		/**
		 * @param idx the idx to set
		 */
		public void incrementIdx() {
			idx++;
		}
	}
	  
	protected AbstractTableWriter() {
		super(WRITER_ID);
	}

	@Override
	public TransformState createTransformState() {
		return new TableModelTransformState();
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, Node content, TransformState transformState, Document doc) throws TransformerException {
		Node ret = null;
	    AbstractTableWriterModel table = new AbstractTableWriterModel();
	    
	    table.build(context, unmarshalledNode, content);
	    if (log.isDebugEnabled()) {
	        log.debug("Table asXML:\n" + table.debugStr());
	    }
	    
	    if (!table.getRows().isEmpty()) {
	    	ret = toNode(context, table, transformState, doc);
	    }
	    return ret;
	}

  protected Node toNode(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Document doc) throws TransformerException {
	  
	DocumentFragment docfrag = doc.createDocumentFragment();
    Element tableRoot = createNode(doc, null, NODE_TABLE);
    List<Property> rowProperties = new ArrayList<Property>();
    int rowPropertiesTableSize = -1;
    
    List<Property> cellProperties = new ArrayList<Property>();
    int cellPropertiesTableSize = -1;
    int cellPropertiesRowSize = -1;
    boolean inHeader = (table.getHeaderMaxRow() > -1);

	TableModelRow rowModel = null;
	Element rowContainer = null;
	Element row = null;
	Element cellNode = null;
    
        int[] autofit = computeAutofitColumnWidths(context, table);
    if (autofit != null) {
    	table.setAutofitColumnWidths(autofit);
    }
    int[] fitted = fitToAvailableWidth(context, table);
    if (fitted != null) {
    	table.setAutofitColumnWidths(fitted);
    }
    createRowProperties(rowProperties, table.getEffectiveTableStyle().getTrPr(), true);
    rowPropertiesTableSize = rowProperties.size();
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTrPr());
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTcPr());
	// will apply these as a default on each td, and then override
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTblPr());
    cellPropertiesTableSize = cellProperties.size();
    
    docfrag.appendChild(tableRoot);
	try {
		applyTableStyles(context, table, transformState, tableRoot);
	} catch (CyclicStylesException e) {
		throw new TransformerException("Cyclic styles detected when processing table styles", e);
	}
	
	// setup column widths
    createColumns(context, table, transformState, doc, tableRoot);
	
	rowContainer = createNode(doc, tableRoot, (inHeader ? NODE_TABLE_HEADER : NODE_TABLE_BODY));
	tableRoot.appendChild(rowContainer);
	
	applyTableRowContainerCustomAttributes(context, table, transformState, rowContainer, inHeader);
	
    for (int rowIndex = 0; rowIndex < table.getRows().size(); rowIndex++) {
			rowModel = table.getRows().get(rowIndex);

			// a row with no cell of its own can't be written (fo:table-row's content
			// model is table-cell+); the model drops such rows, adjusting the merges
			// which cover them, so this is just a guarantee.  @since 17.0.5
			if (!table.rowWritesCells(rowIndex)) {
				log.debug("skipping row " + rowIndex + ": nothing to write");
				continue;
			}

			if ((inHeader) && (rowIndex > table.getHeaderMaxRow())) {
				rowContainer = createNode(doc, tableRoot, NODE_TABLE_BODY);
				tableRoot.appendChild(rowContainer);
				inHeader = false;
				applyTableRowContainerCustomAttributes(context, table, transformState, rowContainer, inHeader);
			}
			row = createNode(doc, rowContainer, (inHeader ? NODE_TABLE_HEADER_ROW : NODE_TABLE_BODY_ROW));
			TrPr trPr = rowModel.getRowProperties();
			CTTblPrEx tblPrEx = rowModel.getRowPropertiesExceptions();
			
			createRowProperties(rowProperties, trPr, false);
			processAttributes(context, rowProperties, row);
			applyTableRowCustomAttributes(context, table, transformState, row, rowIndex, inHeader);
			
			createCellProperties(cellProperties, trPr);
			createCellProperties(cellProperties, tblPrEx);
			cellPropertiesRowSize = cellProperties.size();
				
			
			for (TableModelCell cell : rowModel.getRowContents()) {
				// process cell
				
				if (cell.isDummy()) {
					if (cell.isVMerged()) {

						//Dummy-Cells resulting from vertical merged cells shouldn't be included
						
					} else if (cell.isDummyBefore() || cell.isDummyAfter()) {
						
						cellNode = createNode(doc, row, (inHeader ? NODE_TABLE_HEADER_CELL : NODE_TABLE_BODY_CELL));
						row.appendChild(cellNode);
						applyTableCellCustomAttributes(context, table, transformState, cell, cellNode, inHeader, true);
					}
				}
				else {

					cellNode = createNode(doc, row, (inHeader ? NODE_TABLE_HEADER_CELL : NODE_TABLE_BODY_CELL));
					row.appendChild(cellNode);
					//Apply cell style
					createCellProperties(cellProperties, cell.getTcPr());
					processAttributes(context, cellProperties, cellNode);
					applyTableCellCustomAttributes(context, table, transformState, cell, cellNode, inHeader, false);
					//remove properties defined on cell level
					resetProperties(cellProperties, cellPropertiesRowSize);
					
					// insert content into cell
					// skipping w:tc node itself, insert only its children
					if ( ((AbstractTableWriterModelCell)cell).getContent() == null) {
						log.warn("model cell had no contents!");
					} else {
						log.debug("copying cell contents..");
						
						cellNode = interposeBlockContainer(doc, cellNode, cell.getTcPr());
						
						XmlUtils.treeCopy( ((AbstractTableWriterModelCell)cell).getContent().getChildNodes(),
								cellNode);
					}
				}
			}
			//remove properties defined on row level
			resetProperties(cellProperties, cellPropertiesTableSize);
			resetProperties(rowProperties, rowPropertiesTableSize);
		}
		return docfrag;
  	}
  
    /**
     * In the FO case, if we need to rotate the text, we do that
     * by inserting a block-container.
     * 
     * @param cellNode
     * @return
     */
    protected Element interposeBlockContainer(Document doc, Element cellNode, TcPr tcPr) {
    	
    	return cellNode;    	
    }
  	
  	protected Element createNode(Document doc, Element parent, int nodeType) {
  	Element ret = createNode(doc, nodeType);
  		if ((ret != null) && (parent != null)) {
  			parent.appendChild(ret);
  		}
  		return (ret != null ? ret : parent);
  	}
	
		/**
	 * Word's default table layout is autofit: column widths follow the content
	 * (see {@link org.docx4j.model.table.AutofitLayout}).  Until 17.0.5 docx4j
	 * always used w:tblGrid, which Word only honours when every cell has a
	 * preferred width or the layout is fixed.  Autofit needs text measurement,
	 * which depends on the output format: {@link #measureCellContent} returns null
	 * here, so this base class keeps the grid; the FO writer overrides it.
	 *
	 * @return column widths in twips, or null to use the grid
	 * @since 17.0.5
	 */
	protected int[] computeAutofitColumnWidths(AbstractWmlConversionContext context, AbstractTableWriterModel table) {
		try {
			org.docx4j.wml.CTTblPrBase tblPr = table.getEffectiveTableStyle().getTblPr();
			if (tblPr != null && tblPr.getTblLayout() != null
					&& tblPr.getTblLayout().getType() == org.docx4j.wml.STTblLayoutType.FIXED) {
				return null;
			}
			int cols = table.getColCount();
			if (cols == 0) return null;
			int[] pref = new int[cols];
			java.util.Arrays.fill(pref, -1);
			// a column whose cells declare a width of their own, in any unit: where any
			// column does, widening to the table's preferred width follows the w:tblGrid
			// rather than the columns' content (see widenToPreferredTableWidth)
			boolean[] declared = new boolean[cols];
			double[] min = new double[cols], max = new double[cols];
			boolean anyAuto = false;
						int marginTwips = cellMarginsTwips(tblPr);
			// Pass 1: single-column cells set the columns' minima and maxima.
			// Pass 2: a spanning cell only widens the columns it spans when their sum
			// falls short of its own need, and then in proportion to their flexibility
			// (measured: a 3-column autofit table with two 2-column spanning cells kept
			// its narrow outer columns at the width of their one-word cells, 31 / 385 /
			// 30pt, exactly as the classic algorithm gives).
			java.util.List<Object[]> spanning = new java.util.ArrayList<>();
			for (TableModelRow row : table.getRows()) {
				for (int c = 0; c < row.size() && c < cols; c++) {
					TableModelCell cell = row.get(c);
					if (cell == null || cell.isDummy() || !(cell instanceof AbstractTableWriterModelCell)) continue;
					int span = Math.max(1, cell.getColspan());
					org.docx4j.wml.TblWidth tcW = cell.getTcPr() == null ? null : cell.getTcPr().getTcW();
					boolean hasPref = tcW != null && tcW.getW() != null && tcW.getW().intValue() > 0
							&& (tcW.getType() == null || "dxa".equals(tcW.getType()));
					double[] mm = measureCellContent(context, (AbstractTableWriterModelCell) cell);
					if (mm == null) return null; // cannot measure: keep the grid
					double mn = mm[0] * 20 + marginTwips, mx = mm[1] * 20 + marginTwips;
					if (span == 1) {
						if (tcW != null && tcW.getW() != null && tcW.getW().intValue() > 0
								&& !"auto".equals(tcW.getType())) {
							declared[c] = true;
						}
						if (hasPref) pref[c] = Math.max(pref[c], tcW.getW().intValue());
						else anyAuto = true;
						min[c] = Math.max(min[c], mn);
						max[c] = Math.max(max[c], mx);
					} else {
						spanning.add(new Object[] { c, span, mn, mx });
					}
				}
			}
			for (Object[] sp : spanning) {
				int c = (Integer) sp[0], span = (Integer) sp[1];
				int end = Math.min(cols, c + span);
				double need = (Double) sp[2], needMax = (Double) sp[3];
				spreadShortfall(min, c, end, need, max);
				spreadShortfall(max, c, end, needMax, max);
				for (int k = c; k < end; k++) max[k] = Math.max(max[k], min[k]);
			}
			if (!anyAuto) return null; // every column has a preferred width: the grid is what Word uses
			int available = availableWidthTwips(context, tblPr);
			if (available <= 0) return null;
			int[] mi = new int[cols], ma = new int[cols];
			for (int i = 0; i < cols; i++) {
				mi[i] = (int) Math.ceil(min[i]);
				ma[i] = (int) Math.ceil(max[i]);
			}
			int[] widths = org.docx4j.model.table.AutofitLayout.distribute(mi, ma, pref, available);
			boolean anyDeclared = false;
			for (boolean d : declared) anyDeclared |= d;
			int[] basis = anyDeclared ? gridWidths(table, cols) : null;
			return widenToPreferredTableWidth(widths, pref, basis == null ? widths : basis,
					preferredTableWidthTwips(context, tblPr));
		} catch (Exception e) {
			log.warn("Autofit skipped: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Keep the widths this class chose inside the page: where the autofit pass
	 * (which sizes columns from their content, and so can be wrong) came out wider
	 * than the text column, every column is scaled down in proportion.
	 *
	 * <p>A table's own w:tblGrid is left alone even when it is wider, because that is
	 * what Word does: measured over the real-document corpus, Word draws tables whose
	 * grid is 3% to 19% wider than the text column overhanging the right margin, at
	 * their grid width - one such document matched Word line for line before an
	 * earlier version of this method scaled its tables to fit.  Only widths docx4j
	 * decided for itself are fitted.</p>
	 *
	 * <p>Only formats which paginate do this ({@link #fitsTableToPage()}); in HTML an
	 * over-wide table is the browser's business.</p>
	 *
	 * @return column widths in twips, or null to leave the widths alone
	 * @since 17.0.5
	 */
	protected int[] fitToAvailableWidth(AbstractWmlConversionContext context, AbstractTableWriterModel table) {
		if (!fitsTableToPage()) return null;
		try {
			org.docx4j.wml.CTTblPrBase tblPr = table.getEffectiveTableStyle().getTblPr();
			if (tblPr != null && tblPr.getTblLayout() != null
					&& tblPr.getTblLayout().getType() == org.docx4j.wml.STTblLayoutType.FIXED) {
				return null; // Word overflows a fixed-layout table
			}
			int[] widths = table.getAutofitColumnWidths();
			if (widths == null) return null; // the document's own grid: Word keeps it
			long total = 0;
			for (int w : widths) total += w;
			if (total <= 0) return null;

			int writable = -1;
			try {
				writable = context.getSections().getCurrentSection().getPageDimensions().getWritableWidthTwips();
			} catch (Exception e) {
				log.debug("No section page dimensions to fit the table to: " + e.getMessage());
			}
			if (writable <= 0) return null;
			org.docx4j.wml.TblWidth ind = tblPr == null ? null : tblPr.getTblInd();
			if (ind != null && ind.getW() != null && "dxa".equals(ind.getType()) && ind.getW().intValue() > 0) {
				writable -= ind.getW().intValue();
			}
			if (writable <= 0 || total <= writable) return null;

			int[] out = new int[widths.length];
			long given = 0;
			for (int i = 0; i < widths.length; i++) {
				out[i] = (int) Math.max(1, (long) widths[i] * writable / total);
				given += out[i];
			}
			// the rounding remainder goes to the widest column
			int widest = 0;
			for (int i = 1; i < out.length; i++) if (out[i] > out[widest]) widest = i;
			out[widest] += (int) (writable - given);
			if (out[widest] < 1) out[widest] = 1;
			if (log.isDebugEnabled()) {
				log.debug("Table scaled from " + total + " to " + writable + " twips to fit the page");
			}
			return out;
		} catch (Exception e) {
			log.warn("Table fit skipped: " + e.getMessage(), e);
			return null;
		}
	}

	/** Whether this output format should scale an over-wide table down to the page,
	 *  as Word does; true for paginated output.  @since 17.0.5 */
	protected boolean fitsTableToPage() {
		return false;
	}

		/** Raise the columns [from, to) so that they sum to at least need, sharing the shortfall
	 *  in proportion to (max - current), or evenly when there is no flexibility. */
	private static void spreadShortfall(double[] widths, int from, int to, double need, double[] max) {
		double sum = 0, flex = 0;
		for (int k = from; k < to; k++) {
			sum += widths[k];
			flex += Math.max(0, max[k] - widths[k]);
		}
		double shortfall = need - sum;
		if (shortfall <= 0) return;
		for (int k = from; k < to; k++) {
			double share = flex > 0 ? shortfall * Math.max(0, max[k] - widths[k]) / flex : shortfall / (to - from);
			widths[k] += share;
		}
	}

	/** Left + right cell margins in twips, from the effective tblPr or Word's default. */
	private static int cellMarginsTwips(org.docx4j.wml.CTTblPrBase tblPr) {
		int left = WORD_DEFAULT_CELL_MARGIN_TWIPS, right = WORD_DEFAULT_CELL_MARGIN_TWIPS;
		if (tblPr != null && tblPr.getTblCellMar() != null) {
			CTTblCellMar m = tblPr.getTblCellMar();
			if (m.getLeft() != null && m.getLeft().getW() != null && "dxa".equals(m.getLeft().getType())) left = m.getLeft().getW().intValue();
			if (m.getRight() != null && m.getRight().getW() != null && "dxa".equals(m.getRight().getType())) right = m.getRight().getW().intValue();
		}
		return left + right;
	}

	/** The width the table may take: w:tblW when absolute or a percentage, else the
	 *  writable page width less the table indent. */
	private static int availableWidthTwips(AbstractWmlConversionContext context, org.docx4j.wml.CTTblPrBase tblPr) {
		int preferred = preferredTableWidthTwips(context, tblPr);
		if (preferred > 0) return preferred;
		int writable = -1;
		try {
			writable = context.getSections().getCurrentSection().getPageDimensions().getWritableWidthTwips();
		} catch (Exception e) {
			log.debug("No section page dimensions for autofit: " + e.getMessage());
		}
		if (writable <= 0) return -1;
		org.docx4j.wml.TblWidth ind = tblPr == null ? null : tblPr.getTblInd();
		if (ind != null && ind.getW() != null && "dxa".equals(ind.getType()) && ind.getW().intValue() > 0) {
			writable -= ind.getW().intValue();
		}
		return writable;
	}

	/**
	 * The table's own preferred width in twips: w:tblW as "dxa" (twips) or as "pct"
	 * (fiftieths of a percent of the text column), or -1 when the table has none
	 * (w:tblW absent, or "auto", which is what Word writes for a table sized purely
	 * by its content).
	 *
	 * @since 17.0.5
	 */
	private static int preferredTableWidthTwips(AbstractWmlConversionContext context, org.docx4j.wml.CTTblPrBase tblPr) {
		org.docx4j.wml.TblWidth tblW = tblPr == null ? null : tblPr.getTblW();
		if (tblW == null || tblW.getW() == null || tblW.getW().intValue() <= 0) return -1;
		if ("dxa".equals(tblW.getType())) return tblW.getW().intValue();
		if ("pct".equals(tblW.getType())) {
			try {
				int writable = context.getSections().getCurrentSection().getPageDimensions().getWritableWidthTwips();
				if (writable > 0) return (int) ((long) writable * tblW.getW().intValue() / 5000);
			} catch (Exception e) {
				log.debug("No section page dimensions for the table's preferred width: " + e.getMessage());
			}
		}
		return -1;
	}

	/**
	 * Word's preferred table width is a target, not just a cap: where the columns
	 * sized from their content come to less than w:tblW, Word widens them until the
	 * table is that wide, keeping their proportions.  A column with a preferred
	 * width of its own (w:tcW) keeps it, and the surplus goes to the rest.
	 *
	 * <p>Measured (CR-001, table-indent probes): two auto-width cells whose content
	 * is 67.4 and 74.1pt wide in a table asking for 400pt gave Word columns of
	 * 190.7 and 209.3pt - the content proportions, scaled up - where docx4j left
	 * them at their content widths and wrapped the text.  w:tblLayout "fixed" does
	 * not reach here: the grid is used as it stands.</p>
	 *
	 * <p>The content proportions are Word's only where the cells are all auto-width.
	 * Where any cell declares a width of its own - a w:tcW in "pct" is the common case -
	 * the caller passes the w:tblGrid as the basis instead: measured over the
	 * real-document corpus, Word lays such a table out on its grid, however little
	 * content a column holds (one 100%-wide table's first column came out at its grid
	 * width of 93.5pt where its content proportion would have given it 460 of 481pt).</p>
	 *
	 * @param widths the columns as sized from their content
	 * @param preferred per-column w:tcW in twips, or -1 for none: such a column keeps
	 *        the width the autofit pass gave it and the rest share what is left
	 * @param basis the proportions to widen in (the content widths, or the grid)
	 * @param target the table's preferred width in twips, or -1 for none
	 * @since 17.0.5
	 */
	static int[] widenToPreferredTableWidth(int[] widths, int[] preferred, int[] basis, int target) {
		if (widths == null || widths.length == 0 || target <= 0) return widths;
		if (basis == null || basis.length != widths.length) basis = widths;
		long total = 0;
		for (int w : widths) total += w;
		if (total <= 0 || total >= target) return widths;

		long fixed = 0, flexible = 0, flexNow = 0;
		for (int i = 0; i < widths.length; i++) {
			if (preferred != null && preferred[i] > 0) {
				fixed += widths[i];
			} else {
				flexible += Math.max(0, basis[i]);
				flexNow += widths[i];
			}
		}
		if (flexible <= 0) return widths; // nothing to widen
		long room = target - fixed;
		if (room <= flexNow) return widths; // the columns already fill the table

		int[] out = new int[widths.length];
		long given = 0;
		int last = -1;
		for (int i = 0; i < widths.length; i++) {
			if (preferred != null && preferred[i] > 0) {
				out[i] = widths[i];
				continue;
			}
			out[i] = (int) Math.max(1, room * Math.max(0, basis[i]) / flexible);
			given += out[i];
			last = i;
		}
		if (last >= 0) out[last] += (int) (room - given); // the rounding remainder
		return out;
	}

	/** The table's w:tblGrid as an array of twips, or null when it does not describe
	 *  every column.  @since 17.0.5 */
	private static int[] gridWidths(AbstractTableWriterModel table, int cols) {
		if (table.getTblGrid() == null) return null;
		List<TblGridCol> gridCols = table.getTblGrid().getGridCol();
		if (gridCols == null || gridCols.size() != cols) return null;
		int[] out = new int[cols];
		for (int i = 0; i < cols; i++) {
			java.math.BigInteger w = gridCols.get(i).getW();
			if (w == null || w.intValue() <= 0) return null;
			out[i] = w.intValue();
		}
		return out;
	}

	/**
	 * Minimum and maximum content widths of a cell in points: {widest unbreakable
	 * unit, content unwrapped}; null when this output format cannot measure.
	 * @since 17.0.5
	 */
	protected double[] measureCellContent(AbstractWmlConversionContext context, AbstractTableWriterModelCell cell) {
		return null;
	}

	protected void createColumns(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Document doc, Element tableRoot) throws DOMException {
				List<TblGridCol> gridCols = (table.getTblGrid() != null ? table.getTblGrid().getGridCol() : null);
		Element columnGroup = createNode(doc, tableRoot, NODE_TABLE_COLUMN_GROUP);
		Element column = null;
		applyColumnGroupCustomAttributes(context, table, transformState, columnGroup);
		int[] autofit = table.getAutofitColumnWidths();
		if (autofit != null) {
	    	for(int i=0; i<autofit.length; i++) {
		        column = createNode(doc, columnGroup, NODE_TABLE_COLUMN);
	    		applyColumnCustomAttributes(context, table, transformState, column, i, autofit[i]);
	    	}
		} else if ((gridCols != null) && (!gridCols.isEmpty())) {
	    	for(int i=0; i<gridCols.size(); i++) {
		        column = createNode(doc, columnGroup, NODE_TABLE_COLUMN);
		        // w:gridCol/@w:w is optional (Word tolerates its absence);
		        // -1 means no width, as in the no-tblGrid branch below
		        java.math.BigInteger w = gridCols.get(i).getW();
	    		applyColumnCustomAttributes(context, table, transformState, column, i,
	    				(w == null) ? -1 : w.intValue());
	    	}
    	}
    	else {
	    	for(int i=0; i<table.getColCount(); i++) {
		        column = createNode(doc, columnGroup, NODE_TABLE_COLUMN);
	    		applyColumnCustomAttributes(context, table, transformState, column, i, -1);
	    	}
    	}
	}

	protected void applyTableStyles(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element tableRoot) throws CyclicStylesException {
	List<Property> tableProperties = null;
	
		// This handles:
		// - position (tblPr/tblInd)
		// - table-layout
	
		if (table.getEffectiveTableStyle().getTblPr()==null) {
			log.warn("table.getEffectiveTableStyle().getTblPr() is null, but should never be");
			return;
		}
	
		tableProperties = PropertyFactory.createProperties(table.getEffectiveTableStyle().getTblPr());
		
		// Borders, shading
		if (table.getEffectiveTableStyle().getTcPr()!=null) {
			PropertyFactory.createPropertiesTable(tableProperties, table.getEffectiveTableStyle().getTcPr());
		}
		
		// vAlign fix: match Word's default of top
		if (table.getEffectiveTableStyle().getTcPr()==null
				|| table.getEffectiveTableStyle().getTcPr().getVAlign()==null) {
			tableProperties.add(new TextAlignmentVertical());
		}	
		
		if (!table.isDrawTableBorders()) {
			//isn't nice, but better than passing a lot of flags to the PropertyFactory
			//1. remove any borders and shading
			for (int i=tableProperties.size()-1; i>=0; i--) {
				if ((tableProperties.get(i) instanceof Shading) ||
					(tableProperties.get(i) instanceof AbstractBorder)) {
					tableProperties.remove(i);
				}
			}
			//2. apply explicit none-borders and transparent shading 
			//   (in html there might be borders and shading inherited from the class)
			appendNoneBordersAndShading(tableProperties);
		}
		
		processAttributes(context, tableProperties, tableRoot);

		applyTableCustomAttributes(context, table, transformState, tableRoot);
	}

	protected void appendNoneBordersAndShading(List<Property> tableProperties) {
	CTBorder ctBrdr = null;
	CTShd shd = Context.getWmlObjectFactory().createCTShd();
		ctBrdr = Context.getWmlObjectFactory().createCTBorder();
		ctBrdr.setVal(STBorder.NONE);
		tableProperties.add(new BorderLeft(ctBrdr));
		ctBrdr = Context.getWmlObjectFactory().createCTBorder();
		ctBrdr.setVal(STBorder.NONE);
		tableProperties.add(new BorderRight(ctBrdr));
		ctBrdr = Context.getWmlObjectFactory().createCTBorder();
		ctBrdr.setVal(STBorder.NONE);
		tableProperties.add(new BorderTop(ctBrdr));
		ctBrdr = Context.getWmlObjectFactory().createCTBorder();
		ctBrdr.setVal(STBorder.NONE);
		tableProperties.add(new BorderBottom(ctBrdr));
		shd.setColor("auto");
		shd.setFill("auto");
		shd.setVal(STShd.CLEAR);
		tableProperties.add(new Shading(shd));
	}

	protected void createRowProperties(List<Property> properties, TrPr trPr, boolean includeDefaultHeight) {
		
		// handle <w:trHeight/>
		JAXBElement<CTHeight> trHeight = (trPr != null ? 
				(JAXBElement<CTHeight>)getElement(trPr.getCnfStyleOrDivIdOrGridBefore(), "trHeight") : 
				null);
		if (trHeight != null) {
			properties.add(new TrHeight(trHeight.getValue()));
		}
		
		// handle <w:cantSplit/>
		if (trPr != null) {
			JAXBElement<?> cantSplit = XmlUtils.getListItemByQName(trPr.getCnfStyleOrDivIdOrGridBefore(), new QName(Namespaces.NS_WORD12, "cantSplit"));
			if (cantSplit!=null) {
				BooleanDefaultTrue val = (BooleanDefaultTrue)XmlUtils.unwrap(cantSplit);
				if (val.isVal()) {
					properties.add(new TrCantSplit(cantSplit));					
				}
			}
		}
	}

	
	protected void createCellProperties(List<Property> properties, TrPr trPr) {
		
	}
	

	protected void createCellProperties(List<Property> properties, CTTblPrBase tblPr) {
		
		if (tblPr==null ) {
			log.warn("table.getEffectiveTableStyle().getTblPr() is null, but should never be");
			return;
		}
		
	TblBorders tblBorders = tblPr.getTblBorders();
	CTTblCellMar tblCellMargin = tblPr.getTblCellMar();
		if (tblBorders!=null) {
			if (tblBorders.getInsideH()!=null) {
				properties.add(new BorderTop(tblBorders.getTop()));
				properties.add(new BorderBottom(tblBorders.getBottom()));
			}
			if (tblBorders.getInsideV()!=null) { 
				properties.add(new BorderRight(tblBorders.getRight()));
				properties.add(new BorderLeft(tblBorders.getLeft()));
			}						
		}

				if (tblCellMargin != null) {
			if (tblCellMargin.getTop() != null)
				properties.add(new CellMarginTop(tblCellMargin.getTop()));
			if (tblCellMargin.getBottom() != null)
				properties.add(new CellMarginBottom(tblCellMargin.getBottom()));
			if (tblCellMargin.getLeft() != null)
				properties.add(new CellMarginLeft(tblCellMargin.getLeft()));
			if (tblCellMargin.getRight() != null)
				properties.add(new CellMarginRight(tblCellMargin.getRight()));
		}
		// Word's application default when neither the table nor its style says:
		// 0.08in (108 twips) left and right, 0 top and bottom.  Word's built-in
		// "Normal Table" style carries these, but a document need not define it
		// (docx4j's default styles part does not), and Word applies them anyway.
		// Measured (CR-001 harness, table-fixed): cell text starts at the border
		// centre + half the border width + 5.4pt.  Without this, text sat on the
		// border and every autofit width was 10.8pt too narrow.  @since 17.0.5
		if (tblCellMargin == null || tblCellMargin.getLeft() == null) {
			properties.add(new CellMarginLeft(defaultCellMargin()));
		}
		if (tblCellMargin == null || tblCellMargin.getRight() == null) {
			properties.add(new CellMarginRight(defaultCellMargin()));
		}
	}

	/** 108 twips (0.08in), Word's default left/right cell margin. @since 17.0.5 */
	public static final int WORD_DEFAULT_CELL_MARGIN_TWIPS = 108;

	private static org.docx4j.wml.TblWidth defaultCellMargin() {
		org.docx4j.wml.TblWidth w = org.docx4j.jaxb.Context.getWmlObjectFactory().createTblWidth();
		w.setType("dxa");
		w.setW(java.math.BigInteger.valueOf(WORD_DEFAULT_CELL_MARGIN_TWIPS));
		return w;
	}

	protected void createCellProperties(List<Property> properties, TcPr tcPr) {
		if (tcPr != null) {
			PropertyFactory.createProperties(properties, tcPr);
		}
	}

	protected void createCellProperties(List<Property> properties, CTTblPrEx tblPrEx) {
	}
	
	protected JAXBElement<?> getElement(List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore, String localName) {
		JAXBElement<?> element = null;
		if ((cnfStyleOrDivIdOrGridBefore != null) && (!cnfStyleOrDivIdOrGridBefore.isEmpty())) {
			for (int i=0; i<cnfStyleOrDivIdOrGridBefore.size(); i++) {
				element = cnfStyleOrDivIdOrGridBefore.get(i);
				if (localName.equals(element.getName().getLocalPart())) {
					return element;
				}
			}
		}
		return null;
	}

	protected void processAttributes(AbstractWmlConversionContext context, List<Property> properties, Element element) {
	CTShd shd = null;
	int bgColor = 0xffffff; //the background color of the page is assumed as white
	int fgColor = 0; //the default color of the font is assumed as black
	int pctPattern = -1;
		for (int i=0; i<properties.size(); i++) {
			if (properties.get(i) instanceof Shading) {
				shd = (CTShd)properties.get(i).getObject();
				fgColor = extractColor(shd.getColor(), 0); 
				if ((shd.getVal() != null) &&
					("clear".equals(shd.getVal().value())) &&	
					("auto".equals(shd.getFill()))
					) {
					//This is a reset to the background color of the page, 
					//it is treated as an special case, as the background color 
					//isn't inherited
					bgColor = 0xffffff;
					pctPattern = -2;
				}
				else {
					pctPattern = (shd.getVal() != null ? extractPattern(shd.getVal().value()) : -1);
					bgColor = extractColor(shd.getFill(), bgColor);
				}
			}
		}
		if (pctPattern == -1) {
			applyAttributes(context, properties, element);
		}
		else {
			properties.add(createShading(fgColor, bgColor, pctPattern));
			applyAttributes(context, properties, element);
			properties.remove(properties.size() - 1);
		}
	}

	protected int extractPattern(String pattern) {
		return ((pattern != null) && 
				(PATTERN_PERCENTAGES.containsKey(pattern)) ?
				PATTERN_PERCENTAGES.get(pattern) : -1);
	}
	
	protected int extractColor(String value, int defaultColor) {
	int ret = defaultColor;
		if ((value != null) && (!"auto".equals(value))) {
			try {
				ret = Integer.parseInt(value, 16);
			}
			catch (NumberFormatException nfe){//noop
			}
		}
		return ret;
	}

	protected Property createShading(int fgColor, int bgColor, int pctFg) {
	CTShd shd = null;
	int resColor = UnitsOfMeasurement.combineColors(fgColor, bgColor, pctFg);
		shd = Context.getWmlObjectFactory().createCTShd();
		shd.setVal(STShd.CLEAR);
		shd.setFill(calcHexColor(resColor));
		return new Shading(shd);
	}
	
	protected String calcHexColor(int value) {
	String	ret = Integer.toHexString(value).toUpperCase();
		return (ret.length() < 6 ?
				"000000".substring(0, 6 - ret.length()) + ret :
				ret);
	}

	protected void resetProperties(List<Property> properties, int size) {
		while (properties.size() > size) properties.remove(properties.size() - 1);
	}
	
	/*
	 *  These are the main methods the subclasses have to or should override
	 */
	//protected abstract Logger getLog();
	
  	protected abstract Element createNode(Document doc, int nodeType);

	protected abstract void applyAttributes(AbstractWmlConversionContext context, List<Property> properties, Element element);
	  
	
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element tableRoot) throws CyclicStylesException {
	}
	
	protected void applyColumnGroupCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element columnGroup) {
	}

	protected void applyColumnCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element column, int columnIndex, int columnWidth) {
	}
	
  	protected void applyTableRowContainerCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element rowContainer, boolean isHeader) {
  	}
    
  	protected void applyTableRowCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element row, int rowIndex, boolean isHeader) {  		
  	}
  	
  	protected void applyTableCellCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, 
  			TransformState transformState, TableModelCell tableCell, Element cellNode, boolean isHeader, boolean isDummyCell) {
  	}

}
