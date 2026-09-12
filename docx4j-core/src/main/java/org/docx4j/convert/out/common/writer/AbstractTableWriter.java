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
import org.docx4j.Docx4jProperties;
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
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STShd;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner;
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
    	table.setContentSizedColumns(true);
    } else {
    	int[] scaled = scaleGridToPercentageWidth(context, table);
    	if (scaled != null) table.setAutofitColumnWidths(scaled);
    }
    int[] fitted = fitToAvailableWidth(context, table);
    if (fitted != null) {
    	table.setAutofitColumnWidths(fitted);
    	// scaled to the page: the columns no longer hold their content by construction
    	table.setContentSizedColumns(false);
    }
    if (Docx4jProperties.getProperty(DUMP_AUTOFIT) != null) {
    	dumpAutofit(context, table, autofit, fitted);
    }
    createRowProperties(rowProperties, table.getEffectiveTableStyle().getTrPr(), true);
    rowPropertiesTableSize = rowProperties.size();
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTrPr());
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTcPr());
	// will apply these as a default on each td, and then override
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTblPr());
    cellPropertiesTableSize = cellProperties.size();
    // resolved per cell below: the outer definitions belong to the table's edges
    TblBorders tableBorders = table.getEffectiveTableStyle().getTblPr()==null ? null
    		: table.getEffectiveTableStyle().getTblPr().getTblBorders();
    
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

			// the table style's conditional formatting for this row (firstRow, a band, ...):
			// below the row's own w:trPr, which is applied after it.  @since 17.1.1
			TrPr conditionalTrPr = org.docx4j.model.table.TableStyleConditions.conditionalTrPr(
					table.applicable(table.rowConditions(rowIndex, trPr)));
			
			createRowProperties(rowProperties, conditionalTrPr, false);
			createRowProperties(rowProperties, trPr, false);
			processAttributes(context, rowProperties, row);
			applyTableRowCustomAttributes(context, table, transformState, row, rowIndex, inHeader);
			
			createCellProperties(cellProperties, conditionalTrPr);
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
					createCellBorderProperties(cellProperties, tableBorders,
							rowIndex, table.getRows().size(), cell, table.getColCount());
					// the table style's conditional formatting for this cell, in precedence
					// order, between the table's own borders and the cell's own w:tcPr
					createConditionalCellProperties(cellProperties, table, rowIndex, cell,
							table.applicable(table.cellConditions(rowIndex, cell, trPr)));
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
						
						cellNode = interposeBlockContainer(context, doc, cellNode, table,
								(AbstractTableWriterModelCell)cell, cellWidthTwips(table, cell));

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

    /**
     * As {@link #interposeBlockContainer(Document, Element, TcPr)}, with the geometry a
     * rotated ({@code w:textDirection}) cell needs: a rotated reference area must be
     * given both of its dimensions, which means knowing how wide the cell is.
     *
     * @param cellWidthTwips the cell's width from the grid (its own column plus the ones
     *        a w:gridSpan covers), or 0 where the widths are not known
     * @since 17.1.0
     */
    protected Element interposeBlockContainer(AbstractWmlConversionContext context, Document doc,
    		Element cellNode, AbstractTableWriterModel table, AbstractTableWriterModelCell cell,
    		int cellWidthTwips) {

    	return interposeBlockContainer(doc, cellNode, cell.getTcPr());
    }

    /** The cell's width in twips from the effective column widths (autofit, else the
     *  w:tblGrid), summed over the columns a w:gridSpan covers; 0 where unknown.
     *  @since 17.1.0 */
    protected static int cellWidthTwips(AbstractTableWriterModel table, TableModelCell cell) {
    	int[] widths = table.getAutofitColumnWidths();
    	if (widths == null) widths = gridWidths(table, table.getColCount());
    	if (widths == null) return 0;
    	int from = cell.getColumn();
    	if (from < 0 || from >= widths.length) return 0;
    	int to = Math.min(widths.length, from + Math.max(1, cell.getColspan()));
    	int sum = 0;
    	for (int i = from; i < to; i++) sum += widths[i];
    	return sum;
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
			int container = containingCellWidthTwips(table, tblPr);
			int tablePreferred = preferredTableWidthTwips(context, tblPr, container);
			/* A w:tcW in pct is a preferred width like a dxa one, stated as a fraction of
			 * the table's own width rather than in twips, and reading only dxa left a pct
			 * table with no column preferences at all: it failed the "every column has a
			 * preferred width" test, fell through to the content pass, and was laid out on
			 * its content rather than on the grid Word cached.  61 documents and 661 tables
			 * of the three corpora state w:tblW pct with w:tcW pct.
			 *
			 * Read per cell, exactly as a dxa w:tcW is.  Requiring the cells to describe
			 * every column instead removes the one document this costs - a table whose
			 * rows are mostly gridSpan cells, which declares two of its nine columns and
			 * whose other seven are better content-sized (0.989 -> 0.940, and a page over
			 * Word's count) - but it also gives back two of the five documents it wins on
			 * that corpus, which is a wash, and it would treat pct more strictly than dxa
			 * for no reason the documents support.  @since 17.1.1 */
			double[] min = new double[cols], max = new double[cols];
			// the incompressible part of each column: its cell margins and the widest
			// picture it holds (see AutofitLayout.squeeze); a column no single cell describes
			// gets the table's margins
			int[] margin = new int[cols];
			java.util.Arrays.fill(margin, cellMarginsTwips(tblPr));
			boolean[] marginSeen = new boolean[cols];
			double[] hard = new double[cols];
			boolean anyAuto = false;
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
					int cellMargins = cellMarginsTwips(tblPr, cell.getTcPr());
					double mn = mm[0] * 20 + cellMargins, mx = mm[1] * 20 + cellMargins;
					if (span == 1) {
						if (tcW != null && tcW.getW() != null && tcW.getW().intValue() > 0
								&& !"auto".equals(tcW.getType())) {
							declared[c] = true;
						}
						if (!hasPref && tcW != null && "pct".equals(tcW.getType())
								&& tcW.getW() != null && tcW.getW().intValue() > 0
								&& tablePreferred > 0) {
							int tw = (int) ((long) tablePreferred * tcW.getW().intValue() / 5000);
							if (tw > 0) pref[c] = Math.max(pref[c], tw);
						}
						if (hasPref) pref[c] = Math.max(pref[c], tcW.getW().intValue());
						else anyAuto = true;
						min[c] = Math.max(min[c], mn);
						max[c] = Math.max(max[c], mx);
						margin[c] = marginSeen[c] ? Math.max(margin[c], cellMargins) : cellMargins;
						marginSeen[c] = true;
						if (mm.length > 2) hard[c] = Math.max(hard[c], mm[2] * 20);
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
			int available = availableWidthTwips(context, tblPr, container);
			// the grid-edge allowance is the text column's; a nested table's grid is not
			// shifted off its container's edge (see TableWriter.isNested), so it gets none
			if (available > 0 && tablePreferred <= 0 && container <= 0) {
				available += autofitGridAllowanceTwips(context, table, tblPr);
			}
			int[] mi = new int[cols], ma = new int[cols], floor = new int[cols];
			for (int i = 0; i < cols; i++) {
				mi[i] = (int) Math.ceil(min[i]) + COLUMN_SLACK_TWIPS;
				ma[i] = (int) Math.ceil(max[i]) + COLUMN_SLACK_TWIPS;
				floor[i] = Math.min(mi[i], margin[i] + (int) Math.ceil(hard[i]));
			}
			// kept whether or not the pass goes on to size the columns: the page fit and the
			// diagnostic dump (DUMP_AUTOFIT) both read it
			table.setAutofitInputs(new AbstractTableWriterModel.AutofitInputs(mi, ma, pref, floor, available));
			if (!anyAuto) {
				// every cell states a width: the grid is what Word uses - unless the grid
				// cannot be a layout of those widths, in which case Word lays the table out
				// on them afresh (see gridContradictsPreferences)
				if (refitStaleGrid() && container <= 0 && tablePreferred <= 0 && available > 0) {
					int[] grid = gridWidths(table, cols);
					if (grid != null && gridContradictsPreferences(grid, pref, mi, available)) {
						log.debug("w:tblGrid contradicts the cells' w:tcW; laying the table out on the preferences");
						return org.docx4j.model.table.AutofitLayout.distributePreferredAsMaximum(mi, pref, available);
					}
				}
				return null;
			}
			if (gridIsAuthoritative(table, tblPr, cols, pref, declared)) return null;
			if (available <= 0) return null;
			int[] widths = org.docx4j.model.table.AutofitLayout.distribute(mi, ma, pref, available);
			boolean anyDeclared = false;
			for (boolean d : declared) anyDeclared |= d;
			int[] basis = anyDeclared ? gridWidths(table, cols) : null;
			return widenToPreferredTableWidth(widths, pref, basis == null ? widths : basis,
					tablePreferred);
		} catch (Exception e) {
			log.warn("Autofit skipped: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * How much wider than the text column a content-autofit table's <em>grid</em> may be,
	 * in twips.  Zero here; the FO writer returns the cell margins the grid edge is
	 * shifted by below compatibility mode 15, where it is the cell <em>content</em>, not
	 * the grid, that spans the text column (see {@code TableWriter.applyStartIndent}).
	 * Only consulted for a table with no preferred width of its own.
	 *
	 * @since 17.1.0
	 */
	protected int autofitGridAllowanceTwips(AbstractWmlConversionContext context,
			AbstractTableWriterModel table, org.docx4j.wml.CTTblPrBase tblPr) {
		return 0;
	}

	/**
	 * Whether the table's own {@code w:tblGrid} is the layout Word uses, so that the
	 * content-based autofit pass must stand aside.
	 *
	 * <p>ECMA-376's {@code w:tcW} is a <em>preferred</em> width, and a row's cells need
	 * neither describe every column nor agree with the grid: Word keeps the grid it
	 * cached and treats the cell widths as a hint.  {@link #computeAutofitColumnWidths}
	 * used to give a column whose cells state a {@code w:tcW} exactly that width, so a
	 * table whose row 1 is stale or partial was laid out on row 1 rather than on its
	 * grid.  Measured on a landscape report whose table is {@code w:tblW 14580 dxa} with
	 * a {@code w:tblGrid} summing to the same 729pt while row 1's {@code w:tcW} sum to
	 * 404.4pt: Word's page-3 cell clips run 43.9..81.9 | 82.6..128.2 | 128.9..182.0 |
	 * 182.4..235.5 | 236.2..327.7 | 328.4..772.3 - 728.4pt, the grid, on a 769.9pt
	 * column - where docx4j wrote {@code width="404.4pt"} and wrapped every cell,
	 * turning Word's 249 lines into 320.  45 documents of the three corpora have a row 1
	 * whose {@code w:tcW} sum differs from the grid by more than a tenth.</p>
	 *
	 * <p>The grid wins when it describes every column of the widest row and either</p>
	 * <ul>
	 * <li>every column has a preferred width of its own - which is Word's own condition
	 *     for honouring the grid (&#xa7;6.3), read per column rather than per cell; or
	 * <li>the table states an absolute {@code w:tblW} which the grid sums to (within 1%),
	 *     and at least one cell declares a width - the grid is then plainly the layout
	 *     the table was written for.
	 * </ul>
	 *
	 * <p>A table whose cells are all auto-width is untouched, so Word's content-based
	 * autofit (&#xa7;6.3) and the widening of &#xa7;6.4 still apply to it.</p>
	 *
	 * @since 17.1.0
	 */
	private static boolean gridIsAuthoritative(AbstractTableWriterModel table,
			org.docx4j.wml.CTTblPrBase tblPr, int cols, int[] pref, boolean[] declared) {

		int[] grid = gridWidths(table, cols);
		if (grid == null) return false;

		boolean everyColumnPreferred = cols > 0;
		for (int i = 0; i < cols; i++) {
			if (pref[i] <= 0) everyColumnPreferred = false;
		}
		if (everyColumnPreferred) return true;

		/* A table which states an absolute width its grid sums to was written for that
		 * grid, whether or not any cell repeats it: the cells of such a table are
		 * commonly all w:tcW auto, and reading "at least one cell declares a width" as a
		 * precondition sent them to the content pass.  Measured on a corpus document
		 * whose w:tblW is 8691 dxa - 434.55pt, the grid exactly - with every cell auto:
		 * the content pass gave 73.6 / 40.9 / 242.35 / 77.7pt against the grid's 79.2 /
		 * 47.65 / 222.4 / 85.3, and its "Booked By:" label does not fit a 40.9pt column
		 * where Word's 47.65pt holds it.  12 documents of the three corpora, 35 tables.
		 * @since 17.1.1 */
		org.docx4j.wml.TblWidth tblW = tblPr == null ? null : tblPr.getTblW();
		if (tblW != null && tblW.getW() != null && "dxa".equals(tblW.getType())) {
			long stated = tblW.getW().longValue();
			if (stated > 0) {
				long sum = 0;
				for (int w : grid) sum += w;
				if (Math.abs(sum - stated) * 100 <= stated) return true;
			}
		}
		return false; // a wholly auto-width table is Word's autofit
	}

	/** docx4j.convert.out.fo.tables.refitStaleGrid: an autofit table of auto width, every
	 *  cell of which states a {@code w:tcW}, whose {@code w:tblGrid} cannot be a layout of
	 *  those widths ({@link #gridContradictsPreferences}) is laid out on the widths, as Word
	 *  lays it out, rather than on the grid.  {@code false} keeps the grid, as 17.1.0 did.
	 *  @since 17.1.1 */
	public static final String REFIT_STALE_GRID = "docx4j.convert.out.fo.tables.refitStaleGrid";

	private static boolean refitStaleGrid() {
		return org.docx4j.Docx4jProperties.getProperty(REFIT_STALE_GRID, true);
	}

	/** How much wider than its {@code w:tcW} a grid column may be, where the content does
	 *  not call for the excess, before the grid is read as no layout of the preferences. */
	static final double STALE_GRID_EXCESS = 1.2;

	/**
	 * Whether a {@code w:tblGrid} cannot be the layout Word made of the cells' preferred
	 * widths, so that the table is laid out on the preferences instead
	 * ({@link org.docx4j.model.table.AutofitLayout#distributePreferredAsMaximum}).
	 *
	 * <p>A {@code w:tcW} is a column's <em>maximum</em> in Word's autofit: a column comes
	 * out wider than its preference only where its content minimum forces it.  So a grid
	 * column more than {@link #STALE_GRID_EXCESS} times its preference, in a column whose
	 * measured minimum is no wider than that preference, is a grid Word did not compute
	 * from these cells - a generator's, or one left over from an earlier state of the
	 * table - and Word, which recomputes an autofit table's layout on open, does not draw
	 * it.  Measured on the three real-document corpora re-saved by Word ({@code RowGridDiff}):
	 * of 1474 tables whose rows state widths disagreeing with the grid, Word draws the grid
	 * in 1443, and this test fires on none of those; it fires on three tables in three
	 * documents, in all of which Word's re-saved grid is the preferences-as-maxima layout
	 * to within 0.2%, and the grid docx4j drew was 30%, 46% and 129% out on a column.  A
	 * looser reading - any grid the rule does not reproduce within 2% - would also fire on
	 * nine grids Word keeps (eight documents, three of them at 1.000 of Word's lines),
	 * where the disagreement is between Word's content minima and ours.</p>
	 *
	 * <p>A grid wider than the width available is left to the page fit
	 * ({@link #fitToAvailableWidth}), which has its own measured rules for over-wide grids.</p>
	 *
	 * @param grid the declared w:tblGrid, one entry per column
	 * @param pref per-column preferred width in twips, -1 where a column has none
	 * @param min per-column content minimum in twips (cell margins included)
	 * @param available the width the table has
	 * @since 17.1.1
	 */
	static boolean gridContradictsPreferences(int[] grid, int[] pref, int[] min, int available) {
		if (grid == null || pref == null || min == null) return false;
		long sum = 0;
		for (int g : grid) sum += g;
		if (sum > available) return false;
		for (int i = 0; i < grid.length && i < pref.length && i < min.length; i++) {
			if (pref[i] <= 0 || min[i] > pref[i]) continue;
			if (grid[i] > pref[i] * STALE_GRID_EXCESS) return true;
		}
		return false;
	}

	/**
	 * docx4j.convert.out.fo.wordLayout.dumpAutofit: a file to which one line per table is
	 * appended recording what the column sizer saw and chose, for measuring the sizer
	 * against the {@code w:tblGrid} Word writes on a re-save (the layout-fidelity harness's
	 * {@code ShortfallFit}).  Unset - the default - nothing is written, and the output is
	 * unaffected either way.  CSV, one record per table the writer lays out:
	 * <pre>
	 * document,table,columns,contentSized,fitted,available,min,max,pref,floor,content,final
	 * </pre>
	 * where {@code document} is the package's name (the file it was loaded from),
	 * {@code table} counts the tables of that package in the order the writer met them
	 * (nested before enclosing), {@code min}/{@code max}/{@code pref}/{@code floor} are the
	 * content pass's measurements in twips ({@link AbstractTableWriterModel.AutofitInputs};
	 * empty where it could not measure), {@code available} the width it would fit them into,
	 * {@code contentSized} whether it sized the columns (else the grid did), {@code content}
	 * the widths it chose, and {@code final} the widths the table was given after the page
	 * fit - the {@code fo:table-column} widths, in twips.  Arrays are space-separated.
	 *
	 * @since 17.1.1
	 */
	public static final String DUMP_AUTOFIT = "docx4j.convert.out.fo.wordLayout.dumpAutofit";

	/** Per package, how many tables have been dumped: the record's table index. */
	private static final Map<Object, int[]> dumpCounters =
			java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Object, int[]>());

	/** See {@link #DUMP_AUTOFIT}.
	 *  @param content the widths the content pass chose, or null where it did not run
	 *  @param fitted the widths the page fit replaced them with, or null where it did not */
	private static void dumpAutofit(AbstractWmlConversionContext context, AbstractTableWriterModel table,
			int[] content, int[] fitted) {
		try {
			String path = Docx4jProperties.getProperty(DUMP_AUTOFIT);
			if (path == null || path.trim().isEmpty()) return;
			Object pkg = context.getWmlPackage();
			String name = pkg == null ? null : context.getWmlPackage().name();
			Object key = pkg == null ? dumpCounters : pkg;
			int[] counter;
			synchronized (dumpCounters) {
				counter = dumpCounters.get(key);
				if (counter == null) {
					counter = new int[1];
					dumpCounters.put(key, counter);
				}
			}
			int index = ++counter[0];
			AbstractTableWriterModel.AutofitInputs in = table.getAutofitInputs();
			int cols = table.getColCount();
			int[] fin = table.getAutofitColumnWidths();
			if (fin == null) fin = gridWidths(table, cols);
			StringBuilder sb = new StringBuilder(256);
			sb.append(csvCell(name == null ? "" : name)).append(',').append(index).append(',').append(cols)
				.append(',').append(content != null).append(',').append(fitted != null)
				.append(',').append(in == null ? "" : String.valueOf(in.available))
				.append(',').append(in == null ? "" : join(in.min))
				.append(',').append(in == null ? "" : join(in.max))
				.append(',').append(in == null ? "" : join(in.preferred))
				.append(',').append(in == null ? "" : join(in.floor))
				.append(',').append(join(content))
				.append(',').append(join(fin))
				.append('\n');
			synchronized (dumpCounters) {
				try (java.io.Writer w = new java.io.OutputStreamWriter(
						new java.io.FileOutputStream(path, true), java.nio.charset.StandardCharsets.UTF_8)) {
					w.write(sb.toString());
				}
			}
		} catch (Exception e) {
			log.warn("Autofit dump skipped: " + e.getMessage(), e);
		}
	}

	private static String join(int[] a) {
		if (a == null) return "";
		StringBuilder sb = new StringBuilder();
		for (int v : a) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(v);
		}
		return sb.toString();
	}

	private static String csvCell(String s) {
		return s.indexOf(',') < 0 && s.indexOf('"') < 0 ? s : '"' + s.replace("\"", "\"\"") + '"';
	}

	/**
	 * Keep the widths this class chose inside the page: where the autofit pass
	 * (which sizes columns from their content, and so can be wrong) came out wider
	 * than the text column, the shortfall is shared as Word shares it - each column
	 * keeps its cell margins and any picture, and the text is squeezed in proportion
	 * ({@link org.docx4j.model.table.AutofitLayout#squeeze}; {@link #SHORTFALL_BY_TEXT}
	 * restores 17.1.0's scaling of every column in proportion).
	 *
	 * <p>A table's own w:tblGrid is left alone even when it is wider, because that is
	 * what Word does: measured over the real-document corpus, Word draws tables whose
	 * grid is 3% to 19% wider than the text column overhanging the right margin, at
	 * their grid width - one such document matched Word line for line before an
	 * earlier version of this method scaled its tables to fit.  Only widths docx4j
	 * decided for itself are fitted.</p>
	 *
	 * <p>That exemption reaches only a table which states a width of its own - a
	 * w:tblW in "dxa" or "pct", or w:tblLayout="fixed" - and, for an <em>autofit</em>
	 * table (w:tblW absent or "auto", the layout not fixed), only up to
	 * {@link #GRID_OVERHANG_LIMIT}.  An autofit table's grid is the width Word cached
	 * from its last layout; Word re-runs its content-based autofit against the page it
	 * is laying out now, so a grid far wider than the column - a table pasted from a
	 * landscape page, say - is not a layout Word would produce.  Measured over the
	 * real-document corpus, autofit grids 1.4 to 2.7 times the text column are drawn
	 * by Word inside it (one 956pt grid on a 453.6pt column came out at 505.3pt),
	 * while docx4j painted half the document past the page edge and lost 7 of Word's
	 * 15 pages; grids a few per cent over are drawn by Word at their grid width, and
	 * scaling those re-broke cells Word does not break.  Such a grid is scaled to the
	 * column here.</p>
	 *
	 * <p>Only formats which paginate do this ({@link #fitsTableToPage()}); in HTML an
	 * over-wide table is the browser's business.</p>
	 *
	 * @return column widths in twips, or null to leave the widths alone
	 * @since 17.0.5
	 */
	protected int[] fitToAvailableWidth(AbstractWmlConversionContext context, AbstractTableWriterModel table) {
		if (!fitsTableToPage()) return null;
		return fitToAvailableWidth(context, table, scaleContentAutofitToPage());
	}

	/** docx4j.convert.out.fo.tables.scaleContentAutofit: whether the columns the
	 *  content-autofit pass computed are scaled down to the page when they overflow it.
	 *  See {@link #fitToAvailableWidth}.  @since 17.1.1 */
	public static final String SCALE_CONTENT_AUTOFIT = "docx4j.convert.out.fo.tables.scaleContentAutofit";

	private static boolean scaleContentAutofitToPage() {
		return org.docx4j.Docx4jProperties.getProperty(SCALE_CONTENT_AUTOFIT, true);
	}

	/** docx4j.convert.out.fo.tables.shortfallByText: where the columns a content pass sized
	 *  do not fit the width the table has, the shortfall is shared in proportion to each
	 *  column's <em>text</em> minimum, its cell margins and any picture kept whole
	 *  ({@link org.docx4j.model.table.AutofitLayout#squeeze}).  {@code false} scales every
	 *  column in proportion, as 17.1.0 did.  @since 17.1.1 */
	public static final String SHORTFALL_BY_TEXT = "docx4j.convert.out.fo.tables.shortfallByText";

	private static boolean shortfallByText() {
		return org.docx4j.Docx4jProperties.getProperty(SHORTFALL_BY_TEXT, true);
	}

	/** docx4j.convert.out.fo.tables.refitGridByContent: an autofit table whose cached
	 *  w:tblGrid is wider than {@link #GRID_OVERHANG_LIMIT} allows, and whose measured content
	 *  minima do not fit the column either, is laid out on those minima squeezed into the
	 *  column - as Word, which re-runs its autofit on open, lays it out - rather than on its
	 *  grid scaled in proportion.  {@code false} scales the grid, as 17.1.0 did.  @since 17.1.1 */
	public static final String REFIT_GRID_BY_CONTENT = "docx4j.convert.out.fo.tables.refitGridByContent";

	private static boolean refitGridByContent() {
		return org.docx4j.Docx4jProperties.getProperty(REFIT_GRID_BY_CONTENT, true);
	}

	private int[] fitToAvailableWidth(AbstractWmlConversionContext context, AbstractTableWriterModel table,
			boolean scaleContentAutofit) {
		try {
			org.docx4j.wml.CTTblPrBase tblPr = table.getEffectiveTableStyle().getTblPr();
			if (tblPr != null && tblPr.getTblLayout() != null
					&& tblPr.getTblLayout().getType() == org.docx4j.wml.STTblLayoutType.FIXED) {
				return null; // Word overflows a fixed-layout table
			}
			/* A w:tblW in pct is a width Word gives the table exactly, and lets it
			 * overhang the right margin.  Measured on table-grid-pct: a w:tblW of 6000
			 * pct - 120 per cent of the 9026-twip text column - is drawn by Word 541.2pt
			 * wide, from the left margin to x=613.2 on a 523.2pt column, and a 100 per
			 * cent table indented by w:tblInd 720 is the full 9026 twips wide starting
			 * at the indent, so the percentage is of the column and not of what a
			 * w:tblInd leaves of it.  An absolute w:tblW does not buy that exemption:
			 * one corpus table whose w:tblW asks for 117pt more than the column is kept
			 * inside it by Word.  @since 17.1.0 */
			int container = containingCellWidthTwips(table, tblPr);
			org.docx4j.wml.TblWidth tblW = tblPr == null ? null : tblPr.getTblW();
			if (tblW != null && "pct".equals(tblW.getType())
					&& preferredTableWidthTwips(context, tblPr, container) > 0) {
				return null;
			}
			/* w:compat/w:growAutofit, "Allow Tables to AutoFit Into Page Margins"
			 * (ECMA-376-1 17.15.1), would let an autofit table grow past the text column
			 * rather than being scaled into it.  It is deliberately NOT read: measured over
			 * the three corpora, Word 365's own PDFs of the mode-11 documents which state
			 * it show the modern layout, and honouring it cost two of them 0.948 -> 0.248
			 * and 0.930 -> 0.842 of Word's lines.  See word-layout-settings.md §4(e).
			 * @since 17.1.0 */
			int[] widths = table.getAutofitColumnWidths();
			boolean ownGrid = widths == null;
			/* Measured and kept (&#xa7;6.5): the hypothesis that Word never squeezes the
			 * columns a content pass has computed - that it keeps them at their minima and
			 * lets the table overhang - is contradicted by Word's own kept w:tblGrid.  A
			 * 36-column corpus table whose content minima sum to 26129 twips has a grid Word
			 * wrote and kept summing to 9356, the text column: Word squeezed every column
			 * below its minimum and broke the words inside (&#xa7;6.11).  Not scaling such
			 * a table (docx4j.convert.out.fo.tables.scaleContentAutofit=false) ran it off
			 * the page again and gave back the whole of that document's gain, 0.8462 ->
			 * 0.7953; over the three corpora it moved the same-page-count figure +1, +1, 0
			 * and lines matched +32, -14, -838 against scaling.  The property stays so the
			 * measurement can be repeated without a build.  @since 17.1.1 */
			if (!ownGrid && table.isContentSizedColumns() && !scaleContentAutofit) return null;
			if (ownGrid) {
				// The document's own grid.  Word keeps an over-wide one only where the
				// table states a width of its own; an autofit table's grid is a cached
				// layout Word recomputes and clamps to the text column.  @since 17.1.0
				if (preferredTableWidthTwips(context, tblPr, container) > 0) return null;
				widths = gridWidths(table, table.getColCount());
				if (widths == null) return null;
			}
			long total = 0;
			for (int w : widths) total += w;
			if (total <= 0) return null;

			// what the table has to fit in: the cell a nested table is in, else the page
			int writable = container > 0 ? container : containerWidthTwips(context);
			if (writable <= 0) return null;
			org.docx4j.wml.TblWidth ind = tblPr == null ? null : tblPr.getTblInd();
			if (ind != null && ind.getW() != null && "dxa".equals(ind.getType()) && ind.getW().intValue() > 0) {
				writable -= ind.getW().intValue();
			}
			if (writable <= 0 || total <= writable) return null;
			if (ownGrid && total < writable * GRID_OVERHANG_LIMIT) return null;

			/* How the shortfall is shared (§6.5).  Word re-runs its content autofit on an
			 * autofit table whose cached grid is far wider than the column, so where the
			 * content pass measured the table and its minima do not fit either, the
			 * columns are those minima squeezed into the column rather than the grid
			 * scaled: measured on a template document whose 956pt grids Word refits to
			 * the 461pt column, Word's grid is the content minima's proportions to within
			 * 1% on three of its four such tables (five equal token columns come back
			 * equal, where the scaled grid gave 113 / 69 / 96 / 70 / 105pt), and the
			 * fourth is a URL our measurement cannot break.  And a shortfall is shared by
			 * the text, the cell margins being a fixed cost (AutofitLayout.squeeze).  Both
			 * are properties so the measurement can be repeated.  @since 17.1.1 */
			AbstractTableWriterModel.AutofitInputs in = table.getAutofitInputs();
			boolean byContent = false;
			if (ownGrid && refitGridByContent() && in != null && in.min.length == widths.length) {
				long minTotal = 0;
				for (int m : in.min) minTotal += m;
				if (minTotal > writable) {
					widths = in.min;
					total = minTotal;
					byContent = true;
				}
			}
			if ((byContent || (!ownGrid && table.isContentSizedColumns())) && shortfallByText()
					&& in != null && in.floor.length == widths.length) {
				if (log.isDebugEnabled()) {
					log.debug("Table squeezed from " + total + " to " + writable + " twips by its text minima");
				}
				return org.docx4j.model.table.AutofitLayout.squeeze(widths, in.floor, writable);
			}

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

	/**
	 * A {@code w:tblW} in <b>pct</b> is a width Word gives the table exactly, and the
	 * {@code w:tblGrid} is scaled to it - the percentage wins over the grid, where an
	 * absolute {@code w:tblW} does not (&#xa7;6.5).
	 *
	 * <p>Measured on {@code table-grid-pct}, whose text column is 9026 twips.  A
	 * {@code w:tblLayout="fixed"} table stating {@code w:tblW 5000 pct} with a grid of
	 * 4614+4614 = 9228 (2.2 per cent over) is drawn by Word with its two columns at
	 * 224.98 and 225.22pt - the grid scaled by 9026/9228 - and the twin whose grid is
	 * 3000+3000 = 6000 comes out at the same two widths, the grid scaled <em>up</em> by
	 * 1.504.  docx4j used the grid as it stood in both, so the first table's cells were
	 * 6pt wide of Word's and the second's 75pt narrow, which broke three extra lines.
	 * A grid which already sums to the percentage width is left alone.</p>
	 *
	 * <p>Only where the grid is what decides the layout: a table whose columns the
	 * content-based autofit pass sized has already been given the percentage width as
	 * its target ({@code availableWidthTwips}).  And only where the layout is not
	 * {@code w:tblLayout="fixed"}, under which the grid <em>is</em> the layout and Word
	 * does not resolve the percentage at all (&#xa7;6.5).</p>
	 *
	 * @return column widths in twips, or null to leave the grid alone
	 * @since 17.1.0
	 */
	protected int[] scaleGridToPercentageWidth(AbstractWmlConversionContext context,
			AbstractTableWriterModel table) {
		if (!fitsTableToPage()) return null;   // in HTML the percentage is the browser's
		try {
			org.docx4j.wml.CTTblPrBase tblPr = table.getEffectiveTableStyle().getTblPr();
			org.docx4j.wml.TblWidth tblW = tblPr == null ? null : tblPr.getTblW();
			if (tblW == null || !"pct".equals(tblW.getType())) return null;
			/* Under w:tblLayout "fixed" the w:tblGrid is the layout and the percentage is
			 * not resolved against anything: measured over the three corpora against the
			 * grid Word wrote on a re-save, of 218 fixed-layout percentage tables Word's
			 * grid total is the authored grid's in 204 and the percentage of the container
			 * in 9, and Word rewrote only 8 of the corpora's 1442 fixed-layout tables at
			 * all.  Scaling such a grid to the percentage was the largest single error in
			 * the corpora after the one-document cases: one landscape document's three
			 * tables state 98% of a 20978-twip text column with a 6693-twip grid the cells
			 * repeat in dxa, and were drawn 3.07 times too wide, 13865 twips per table.
			 *
			 * This is the one place the table-grid-pct probe and the real documents part
			 * company, and the reason is the probe's own warning (see the harness README):
			 * Word refits a percentage table's grid there because the grid is the
			 * harness's, so Word has no cached layout of its own to keep and falls back to
			 * the w:tblW.  A grid Word wrote is a layout it keeps.  Nothing in the file
			 * distinguishes the two, so this follows the documents.  @since 17.1.1 */
			if (tblPr != null && tblPr.getTblLayout() != null
					&& tblPr.getTblLayout().getType() == org.docx4j.wml.STTblLayoutType.FIXED) {
				return null;
			}
			int target = preferredTableWidthTwips(context, tblPr, containingCellWidthTwips(table, tblPr));
			if (target <= 0) return null;
			int[] grid = gridWidths(table, table.getColCount());
			if (grid == null || grid.length == 0) return null;
			long total = 0;
			for (int w : grid) total += w;
			if (total <= 0 || total == target) return null;
			int[] out = new int[grid.length];
			long given = 0;
			for (int i = 0; i < grid.length; i++) {
				out[i] = (int) Math.max(1, (long) grid[i] * target / total);
				given += out[i];
			}
			int widest = 0;
			for (int i = 1; i < out.length; i++) if (out[i] > out[widest]) widest = i;
			out[widest] += (int) (target - given);
			if (out[widest] < 1) out[widest] = 1;
			return out;
		} catch (Exception e) {
			log.warn("Percentage table width skipped: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * How far past the text column an <em>autofit</em> table's own w:tblGrid may reach
	 * before {@link #fitToAvailableWidth} scales it down.  Measured over the
	 * real-document corpus: Word draws autofit grids up to about a fifth over at their
	 * grid width (&#xa7;6.5), and re-fits ones 1.35 to 2.7 times the column.
	 *
	 * @since 17.1.0
	 */
	protected static final double GRID_OVERHANG_LIMIT = 1.25;

	/**
	 * Slack, in twips, added to every column the content-based autofit pass sizes, so
	 * that the line which sized the column still fits once the FO writer has rounded
	 * the cell padding.  Measured on {@code table-cell-measure}: a column of the line's
	 * advance rounded up to a whole twip plus the two 108-twip cell margins came out
	 * 0.03pt short, because 5.4pt of cell margin is written as {@code 1.91mm} =
	 * 5.4152pt at each side, and the line which the column exists to hold broke in two
	 * in all three of that probe's content-autofit tables.
	 *
	 * @since 17.1.0
	 */
	private static final int COLUMN_SLACK_TWIPS = 2;

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

	/**
	 * Left + right cell margins in twips for one cell: the cell's own
	 * {@code w:tcMar} where it declares them, else the table's {@code w:tblCellMar},
	 * else Word's default.
	 *
	 * <p>Measured against Word 365 on a table whose {@code w:tblCellMar} is 0 left and
	 * right and whose every cell overrides with {@code w:tcMar} 30 twips: sizing the
	 * columns to the bare text width gave a column of 68.95pt for a cell whose one line
	 * is 68.9pt wide, and the cell writer's 1.5pt of padding each side then left a
	 * 65.95pt measure - so the very line that sized the column no longer fitted and
	 * broke in two, on every such cell of the table.  Word keeps it on one line
	 * (70.8..149.5).
	 *
	 * @since 17.1.0
	 */
	private static int cellMarginsTwips(org.docx4j.wml.CTTblPrBase tblPr, org.docx4j.wml.TcPr tcPr) {
		org.docx4j.wml.TcMar m = tcPr == null ? null : tcPr.getTcMar();
		int fromTable = cellMarginsTwips(tblPr);
		/* Only where the table gives its cells no margins of its own: measured, taking the
		 * cell's over the table's wherever it states them cost 0.054 of line parity on a
		 * document whose cells say left=120 right=0 against the table's 15/15 (its columns
		 * came out 78 / 44.25 / 230.2pt where the grid gives 73.5 / 40.8 / 242.65).  The
		 * shape the rule is for is a table which states 0 and cells which state the real
		 * margin. */
		if (m == null || fromTable != 0) return fromTable;
		int left = -1, right = -1;
		if (m.getLeft() != null && m.getLeft().getW() != null && "dxa".equals(m.getLeft().getType())) {
			left = m.getLeft().getW().intValue();
		}
		if (m.getRight() != null && m.getRight().getW() != null && "dxa".equals(m.getRight().getType())) {
			right = m.getRight().getW().intValue();
		}
		if (left < 0 && right < 0) return fromTable;
		if (left < 0 || right < 0) {
			// one side only: take the other from the table's own pair
			int half = fromTable / 2;
			if (left < 0) left = half;
			if (right < 0) right = fromTable - half;
		}
		return left + right;
	}

	/** Left + right cell margins in twips, from the effective tblPr (which carries Word's
	 *  built-in Normal Table margins where they apply, since 17.1.1); none otherwise. */
	private static int cellMarginsTwips(org.docx4j.wml.CTTblPrBase tblPr) {
		int left = 0, right = 0;
		if (tblPr != null && tblPr.getTblCellMar() != null) {
			CTTblCellMar m = tblPr.getTblCellMar();
			if (m.getLeft() != null && m.getLeft().getW() != null && "dxa".equals(m.getLeft().getType())) left = m.getLeft().getW().intValue();
			if (m.getRight() != null && m.getRight().getW() != null && "dxa".equals(m.getRight().getType())) right = m.getRight().getW().intValue();
		}
		return left + right;
	}

	/** The width the table may take: w:tblW when absolute or a percentage, else the
	 *  container (the page's text column, or the cell a nested table sits in) less the
	 *  table indent. */
	private static int availableWidthTwips(AbstractWmlConversionContext context,
			org.docx4j.wml.CTTblPrBase tblPr, int container) {
		int preferred = preferredTableWidthTwips(context, tblPr, container);
		if (preferred > 0) return preferred;
		int writable = container > 0 ? container : containerWidthTwips(context);
		if (writable <= 0) return -1;
		org.docx4j.wml.TblWidth ind = tblPr == null ? null : tblPr.getTblInd();
		if (ind != null && ind.getW() != null && "dxa".equals(ind.getType()) && ind.getW().intValue() > 0) {
			writable -= ind.getW().intValue();
		}
		return writable;
	}

	/** The section's text column in twips, or -1 when there is no section to ask. */
	private static int containerWidthTwips(AbstractWmlConversionContext context) {
		try {
			return context.getSections().getCurrentSection().getPageDimensions().getWritableWidthTwips();
		} catch (Exception e) {
			log.debug("No section page dimensions: " + e.getMessage());
			return -1;
		}
	}

	/**
	 * The table's own preferred width in twips: w:tblW as "dxa" (twips) or as "pct"
	 * (fiftieths of a percent of the container), or -1 when the table has none
	 * (w:tblW absent, or "auto", which is what Word writes for a table sized purely
	 * by its content).
	 *
	 * @param container the width the percentage is of - the cell a nested table sits in,
	 *        or -1 for the section's text column
	 * @since 17.0.5
	 */
	private static int preferredTableWidthTwips(AbstractWmlConversionContext context,
			org.docx4j.wml.CTTblPrBase tblPr, int container) {
		org.docx4j.wml.TblWidth tblW = tblPr == null ? null : tblPr.getTblW();
		if (tblW == null || tblW.getW() == null || tblW.getW().intValue() <= 0) return -1;
		if ("dxa".equals(tblW.getType())) return tblW.getW().intValue();
		if ("pct".equals(tblW.getType())) {
			int base = container > 0 ? container : containerWidthTwips(context);
			if (base > 0) {
				int w = (int) ((long) base * tblW.getW().intValue() / 5000);
				/* A percentage which resolves to less than one pair of Word's default cell
				 * margins is not a width, and Word does not treat it as one.  Measured on a
				 * corpus document whose table states w:tblW w:w="1" w:type="pct" - 0.02%
				 * of the text column, 1.9 twips - with a w:tblCellMar of 0: Word draws the
				 * table full width and its "Personal Protective Equipment" on one line,
				 * where docx4j scaled the grid to the percentage and drew four 0.05pt
				 * columns, and once a word wider than its cell is broken (§6.11) the
				 * document gained a page of one-letter lines.  Of the 1,855 pct table
				 * widths in the three corpora the next-smallest is in the 30-39% band, so
				 * a floor of 216 twips (10.8pt, below 3% of any text column) fires on
				 * that one alone.  It is Word's default margins rather than the table's
				 * own because that table's are zero.  @since 17.1.1 */
				if (w < 2 * WORD_DEFAULT_CELL_MARGIN_TWIPS) return -1;
				return w;
			}
		}
		return -1;
	}

	/**
	 * The width in twips a table nested in a {@code w:tc} has to lay itself out in - the
	 * containing cell's width less that cell's margins - or -1 where the table is not
	 * nested, the container cannot be worked out, or the table states an absolute width
	 * of its own.
	 *
	 * <p>Word resolves a nested table's {@code w:tblW pct} against the cell it is in and
	 * autofits it into the same width; docx4j resolved both against the page, so a nested
	 * table came out as wide as the text column however narrow its cell.  Measured
	 * against the {@code w:tblGrid} Word writes when it re-saves the corpora, over their
	 * 631 nested tables: of the 438 which state {@code w:tblW pct}, the grid Word computed
	 * is (cell - the cell's margins) &#xd7; the percentage in every one of them to within
	 * 1%, where the cell alone matches 424 and (cell - 216) 12.  The other two cases stay
	 * as they were:</p>
	 * <ul>
	 * <li>an <em>absolute</em> {@code w:tblW} is a width Word gives the table and lets it
	 *     overhang the cell - of the corpora's 141 such nested tables Word keeps the
	 *     stated width in 133, and in 20 the grid it wrote is wider than the cell;
	 * <li>the 52 nested tables with no preferred width of their own are Word's
	 *     content-based autofit, for which the cell is the target rather than the answer.
	 * </ul>
	 *
	 * <p>Only the visitor pathway can answer this: it walks the document's own object
	 * tree, so the {@code w:tbl}'s parent pointers lead to the cell.  In the XSLT pathway
	 * the {@code w:tbl} was unmarshalled from the DOM on its own, has no parent, and
	 * keeps the page-based behaviour (as does a table this cannot place).</p>
	 *
	 * @since 17.1.1
	 */
	protected static int containingCellWidthTwips(AbstractTableWriterModel table,
			org.docx4j.wml.CTTblPrBase tblPr) {
		try {
			org.docx4j.wml.TblWidth tblW = tblPr == null ? null : tblPr.getTblW();
			if (tblW != null && tblW.getW() != null && tblW.getW().intValue() > 0
					&& "dxa".equals(tblW.getType())) {
				return -1;   // an absolute width Word honours, cell or no cell
			}
			org.docx4j.wml.Tbl tbl = table.getTbl();
			if (tbl == null) return -1;
			org.docx4j.wml.Tc cell = (org.docx4j.wml.Tc) ancestorOfType(tbl, org.docx4j.wml.Tc.class);
			if (cell == null) return -1;   // not nested in a cell
			org.docx4j.wml.Tr row = (org.docx4j.wml.Tr) ancestorOfType(cell, org.docx4j.wml.Tr.class);
			org.docx4j.wml.Tbl outer = row == null ? null
					: (org.docx4j.wml.Tbl) ancestorOfType(row, org.docx4j.wml.Tbl.class);
			int width = cellWidthFromOuterGrid(outer, row, cell);
			if (width <= 0) width = declaredCellWidthTwips(cell);
			if (width <= 0) return -1;
			int available = width - cellMarginsTwips(outer == null ? null : outer.getTblPr(), cell.getTcPr());
			return available > 0 ? available : -1;
		} catch (Exception e) {
			log.debug("Containing cell not determined: " + e.getMessage());
			return -1;
		}
	}

	/**
	 * The nearest ancestor of the given type, following the JAXB parent pointers, or null
	 * where there is none before the containing story (body, header, footer, note or text
	 * box) - so that a table at the top of its story is not given a container from some
	 * cell it is not in.
	 */
	private static Object ancestorOfType(Object from, Class<?> type) {
		Object o = (from instanceof org.jvnet.jaxb.lang.Child)
				? ((org.jvnet.jaxb.lang.Child) from).getParent() : null;
		for (int guard = 0; o != null && guard < 64; guard++) {
			if (type.isInstance(o)) return o;
			if (o instanceof org.docx4j.wml.Body
					|| o instanceof org.docx4j.wml.Hdr
					|| o instanceof org.docx4j.wml.Ftr
					|| o instanceof org.docx4j.wml.CTFtnEdn
					|| o instanceof org.docx4j.wml.CTTxbxContent
					|| o instanceof org.docx4j.wml.Document) {
				return null;
			}
			o = (o instanceof org.jvnet.jaxb.lang.Child)
					? ((org.jvnet.jaxb.lang.Child) o).getParent() : null;
		}
		return null;
	}

	/** The cell's width from the enclosing table's w:tblGrid: its position in the row
	 *  (after any w:gridBefore) expanded by its w:gridSpan.  -1 where the row holds a cell
	 *  this cannot count, so that a mis-counted position is never used. */
	private static int cellWidthFromOuterGrid(org.docx4j.wml.Tbl outer, org.docx4j.wml.Tr row,
			org.docx4j.wml.Tc cell) {
		if (outer == null || row == null || outer.getTblGrid() == null) return -1;
		List<TblGridCol> cols = outer.getTblGrid().getGridCol();
		if (cols == null || cols.isEmpty()) return -1;
		int start = gridBefore(row);
		int span = -1;
		for (Object o : row.getContent()) {
			Object v = XmlUtils.unwrap(o);
			if (v instanceof org.docx4j.wml.Tc) {
				org.docx4j.wml.Tc tc = (org.docx4j.wml.Tc) v;
				if (tc == cell) { span = gridSpan(tc); break; }
				start += gridSpan(tc);
			} else if (v instanceof org.docx4j.wml.CTSdtCell
					|| v instanceof org.docx4j.wml.CTCustomXmlCell) {
				return -1;   // cells this loop cannot see: the position would be wrong
			}
		}
		if (span < 0 || start < 0 || start + span > cols.size()) return -1;
		int sum = 0;
		for (int i = start; i < start + span; i++) {
			java.math.BigInteger w = cols.get(i).getW();
			if (w == null || w.intValue() <= 0) return -1;
			sum += w.intValue();
		}
		return sum;
	}

	/** The cell's w:gridSpan, at least 1. */
	private static int gridSpan(org.docx4j.wml.Tc tc) {
		if (tc.getTcPr() != null && tc.getTcPr().getGridSpan() != null
				&& tc.getTcPr().getGridSpan().getVal() != null) {
			return Math.max(1, tc.getTcPr().getGridSpan().getVal().intValue());
		}
		return 1;
	}

	/** The row's w:gridBefore: grid columns no cell of the row occupies. */
	private static int gridBefore(org.docx4j.wml.Tr row) {
		if (row.getTrPr() == null) return 0;
		JAXBElement<?> el = XmlUtils.getListItemByQName(row.getTrPr().getCnfStyleOrDivIdOrGridBefore(),
				new QName(Namespaces.NS_WORD12, "gridBefore"));
		if (el == null || !(el.getValue() instanceof org.docx4j.wml.CTTrPrBase.GridBefore)) return 0;
		java.math.BigInteger val = ((org.docx4j.wml.CTTrPrBase.GridBefore) el.getValue()).getVal();
		return val == null ? 0 : Math.max(0, val.intValue());
	}

	/** The cell's own w:tcW where it is absolute, else -1. */
	private static int declaredCellWidthTwips(org.docx4j.wml.Tc cell) {
		org.docx4j.wml.TblWidth tcW = cell.getTcPr() == null ? null : cell.getTcPr().getTcW();
		if (tcW == null || tcW.getW() == null || tcW.getW().intValue() <= 0) return -1;
		if (tcW.getType() != null && !"dxa".equals(tcW.getType())) return -1;
		return tcW.getW().intValue();
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
	 * unit, content unwrapped}; null when this output format cannot measure.  A third
	 * element, where present, is the widest <em>incompressible</em> unit - a picture,
	 * which Word does not shrink when the column is squeezed
	 * ({@link org.docx4j.model.table.AutofitLayout#squeeze}).
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
		
	CTTblCellMar tblCellMargin = tblPr.getTblCellMar();
		/* w:tblBorders is no longer applied here: its top/bottom/left/right describe the
		 * table's outer edge and insideH/insideV the rules between cells (ECMA-376
		 * 17.4.39), so it has to be resolved per cell - createCellBorderProperties.
		 * Until 17.1.0 the outer definition went on every cell whenever insideH or
		 * insideV existed, which is right only where the two agree: measured on a table
		 * whose outer borders are nil and whose insideV is single sz=18 color=FFFFFF,
		 * Word draws a 2.25pt white rule between the columns (fill_path
		 * x=186.1..188.2) and every one of our cells came out border-*-style="none".
		 * @since 17.1.0 */

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
		// Word's built-in Normal Table margins (108 twips left and right, 0 top and
		// bottom) now arrive in the effective table style itself, for a table naming no
		// style and for one whose style chain reaches the default table style
		// (PropertyResolver.getEffectiveTableStyle, CR-015 phase 4).  A table whose chain
		// does not reach it has no cell margin in Word (measured, probe
		// styles-table-default: its first cell's text starts at the border), so nothing
		// is added here any more; until 17.1.1 108 was put on every table lacking one.
		// Measured earlier (CR-001 harness, table-fixed): cell text starts at the border
		// centre + half the border width + 5.4pt, which the built-in still gives.
		if (tblCellMargin == null || tblCellMargin.getLeft() == null) {
			properties.add(new CellMarginLeft(noCellMargin()));
		}
		if (tblCellMargin == null || tblCellMargin.getRight() == null) {
			properties.add(new CellMarginRight(noCellMargin()));
		}
	}

	/** 108 twips (0.08in), the left/right cell margin of Word's built-in Normal Table. @since 17.0.5 */
	public static final int WORD_DEFAULT_CELL_MARGIN_TWIPS = org.docx4j.model.PropertyResolver.WORD_DEFAULT_CELL_MARGIN_TWIPS;

	private static org.docx4j.wml.TblWidth noCellMargin() {
		org.docx4j.wml.TblWidth w = org.docx4j.jaxb.Context.getWmlObjectFactory().createTblWidth();
		w.setType("dxa");
		w.setW(java.math.BigInteger.ZERO);
		return w;
	}

	/**
	 * The table's own borders, resolved for one cell: the outer definitions
	 * (top/bottom/left/right) apply to the cells on the table's edges and
	 * insideH/insideV to the sides which face another cell (ECMA-376 17.4.39).  The
	 * cell's own w:tcBorders, applied after this, overrides.
	 *
	 * @since 17.1.0
	 */
	protected void createCellBorderProperties(List<Property> properties, TblBorders tblBorders,
			int rowIndex, int rowCount, TableModelCell cell, int colCount) {
		if (tblBorders == null || cell == null) return;
		boolean firstRow = rowIndex <= 0;
		boolean lastRow  = rowIndex + cell.getExtraRows() >= rowCount - 1;
		int col = cell.getColumn();
		boolean firstCol = col <= 0;
		boolean lastCol  = col + cell.getExtraCols() >= colCount - 1;

		CTBorder top    = firstRow ? tblBorders.getTop()    : tblBorders.getInsideH();
		CTBorder bottom = lastRow  ? tblBorders.getBottom() : tblBorders.getInsideH();
		CTBorder left   = firstCol ? tblBorders.getLeft()   : tblBorders.getInsideV();
		CTBorder right  = lastCol  ? tblBorders.getRight()  : tblBorders.getInsideV();

		if (top != null)    properties.add(new BorderTop(top));
		if (bottom != null) properties.add(new BorderBottom(bottom));
		if (left != null)   properties.add(new BorderLeft(left));
		if (right != null)  properties.add(new BorderRight(right));
	}

	protected void createCellProperties(List<Property> properties, TcPr tcPr) {
		if (tcPr != null) {
			PropertyFactory.createProperties(properties, tcPr);
		}
	}

	/**
	 * The table style's conditional formatting for one cell: the {@code w:tblPr} and
	 * {@code w:tcPr} of each {@code w:tblStylePr} the cell is under, applied in precedence
	 * order (ECMA-376-1 17.7.6, {@link org.docx4j.model.table.TableStyleConditions}).
	 *
	 * <p>Borders are those of the condition's <em>region</em> - the first row, the first
	 * column, the rows of a band, the whole table, one corner cell - resolved for the cell
	 * as the table's own w:tblBorders are ({@link #createCellBorderProperties}): the outer
	 * definitions on the edges of the cell which lie on the region's edges, insideH /
	 * insideV on the edges which face another cell of the region.  That is what Word's
	 * built-in styles are written for: Light List's {@code firstRow} states left, right
	 * and {@code insideV="nil"}, which makes the header one box with no rules between its
	 * cells; its {@code band1Horz} states top and bottom with {@code insideV="nil"}, a row
	 * ruled above and below.  Read per cell instead, every header cell would get a left and
	 * a right rule.  Shading, margins, vertical alignment and text direction are the
	 * cell's own.</p>
	 *
	 * @since 17.1.1
	 */
	protected void createConditionalCellProperties(List<Property> properties, AbstractTableWriterModel table,
			int rowIndex, TableModelCell cell, List<CTTblStylePr> applicable) {
		if (applicable == null || applicable.isEmpty()) return;
		int rowCount = table.getRows().size();
		int colCount = table.getColCount();
		int r0 = rowIndex, r1 = rowIndex + cell.getExtraRows();
		int c0 = cell.getColumn(), c1 = c0 + cell.getExtraCols();
		for (CTTblStylePr pr : applicable) {
			int[] region = regionOf(table, pr.getType(), rowIndex, cell, rowCount, colCount);
			CTTblPrBase tblPr = pr.getTblPr();
			if (tblPr != null) {
				if (tblPr.getTblBorders() != null && region != null) {
					TblBorders b = tblPr.getTblBorders();
					addRegionBorders(properties, region, r0, r1, c0, c1,
							b.getTop(), b.getBottom(), b.getLeft(), b.getRight(), b.getInsideH(), b.getInsideV());
				}
				if (tblPr.getShd() != null) {
					properties.add(new Shading(tblPr.getShd()));
				}
				if (tblPr.getTblCellMar() != null) {
					CTTblCellMar m = tblPr.getTblCellMar();
					if (m.getTop() != null) properties.add(new CellMarginTop(m.getTop()));
					if (m.getBottom() != null) properties.add(new CellMarginBottom(m.getBottom()));
					if (m.getLeft() != null) properties.add(new CellMarginLeft(m.getLeft()));
					if (m.getRight() != null) properties.add(new CellMarginRight(m.getRight()));
				}
			}
			TcPr tcPr = pr.getTcPr();
			if (tcPr != null) {
				if (tcPr.getTcBorders() != null && region != null) {
					TcPrInner.TcBorders b = tcPr.getTcBorders();
					addRegionBorders(properties, region, r0, r1, c0, c1,
							b.getTop(), b.getBottom(), b.getLeft(), b.getRight(), b.getInsideH(), b.getInsideV());
				}
				if (tcPr.getShd() != null) {
					properties.add(new Shading(tcPr.getShd()));
				}
				if (tcPr.getVAlign() != null) {
					properties.add(new TextAlignmentVertical(tcPr.getVAlign()));
				}
				if (tcPr.getTextDirection() != null) {
					properties.add(new org.docx4j.model.properties.table.tc.TextDir(tcPr.getTextDirection()));
				}
				if (tcPr.getTcMar() != null) {
					org.docx4j.wml.TcMar m = tcPr.getTcMar();
					if (m.getTop() != null) properties.add(new CellMarginTop(m.getTop()));
					if (m.getBottom() != null) properties.add(new CellMarginBottom(m.getBottom()));
					if (m.getLeft() != null) properties.add(new CellMarginLeft(m.getLeft()));
					if (m.getRight() != null) properties.add(new CellMarginRight(m.getRight()));
				}
			}
		}
	}

	/**
	 * The rows and columns a conditional format covers, as {firstRow, lastRow, firstCol,
	 * lastCol} inclusive, for the cell it is being applied to; null where it cannot be told.
	 */
	private static int[] regionOf(AbstractTableWriterModel table, STTblStyleOverrideType type,
			int rowIndex, TableModelCell cell, int rowCount, int colCount) {
		if (type == null) return null;
		int r0 = rowIndex, r1 = rowIndex + cell.getExtraRows();
		int c0 = cell.getColumn(), c1 = c0 + cell.getExtraCols();
		switch (type) {
			case WHOLE_TABLE: return new int[] { 0, rowCount - 1, 0, colCount - 1 };
			case FIRST_ROW: return new int[] { 0, 0, 0, colCount - 1 };
			case LAST_ROW: return new int[] { rowCount - 1, rowCount - 1, 0, colCount - 1 };
			case FIRST_COL: return new int[] { 0, rowCount - 1, 0, 0 };
			case LAST_COL: return new int[] { 0, rowCount - 1, colCount - 1, colCount - 1 };
			case BAND_1_HORZ: case BAND_2_HORZ: {
				int[] rows = table.bandRows(rowIndex);
				return rows == null ? new int[] { r0, r1, 0, colCount - 1 } : new int[] { rows[0], rows[1], 0, colCount - 1 };
			}
			case BAND_1_VERT: case BAND_2_VERT: {
				int[] cols = table.bandCols(c0);
				return cols == null ? new int[] { 0, rowCount - 1, c0, c1 } : new int[] { 0, rowCount - 1, cols[0], cols[1] };
			}
			default: // the corners: the cell itself
				return new int[] { r0, r1, c0, c1 };
		}
	}

	/** The region's borders resolved onto one cell's four edges (see createConditionalCellProperties). */
	private static void addRegionBorders(List<Property> properties, int[] region,
			int r0, int r1, int c0, int c1,
			CTBorder top, CTBorder bottom, CTBorder left, CTBorder right, CTBorder insideH, CTBorder insideV) {
		CTBorder t = (r0 <= region[0]) ? top : insideH;
		CTBorder b = (r1 >= region[1]) ? bottom : insideH;
		CTBorder l = (c0 <= region[2]) ? left : insideV;
		CTBorder r = (c1 >= region[3]) ? right : insideV;
		if (t != null) properties.add(new BorderTop(t));
		if (b != null) properties.add(new BorderBottom(b));
		if (l != null) properties.add(new BorderLeft(l));
		if (r != null) properties.add(new BorderRight(r));
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
