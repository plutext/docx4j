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

import javax.xml.bind.JAXBElement;
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
    
    createRowProperties(rowProperties, table.getEffectiveTableStyle().getTrPr(), true);
    rowPropertiesTableSize = rowProperties.size();
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTrPr());
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTcPr());
	// will apply these as a default on each td, and then override
    createCellProperties(cellProperties, table.getEffectiveTableStyle().getTblPr());
    cellPropertiesTableSize = cellProperties.size();
    
    docfrag.appendChild(tableRoot);
	applyTableStyles(context, table, transformState, tableRoot);
	
	// setup column widths
    createColumns(context, table, transformState, doc, tableRoot);
	
	rowContainer = createNode(doc, tableRoot, (inHeader ? NODE_TABLE_HEADER : NODE_TABLE_BODY));
	tableRoot.appendChild(rowContainer);
	
	applyTableRowContainerCustomAttributes(context, table, transformState, rowContainer, inHeader);
	
    for (int rowIndex = 0; rowIndex < table.getRows().size(); rowIndex++) {
			rowModel = table.getRows().get(rowIndex);
			
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
	
	protected void createColumns(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Document doc, Element tableRoot) throws DOMException {
		List<TblGridCol> gridCols = (table.getTblGrid() != null ? table.getTblGrid().getGridCol() : null);
		Element columnGroup = createNode(doc, tableRoot, NODE_TABLE_COLUMN_GROUP);
		Element column = null;
		
		applyColumnGroupCustomAttributes(context, table, transformState, columnGroup);
    	if ((gridCols != null) && (!gridCols.isEmpty())) {
	    	for(int i=0; i<gridCols.size(); i++) {
		        column = createNode(doc, columnGroup, NODE_TABLE_COLUMN);
	    		applyColumnCustomAttributes(context, table, transformState, column, i, gridCols.get(i).getW().intValue());
	    	}
    	}
    	else {
	    	for(int i=0; i<table.getColCount(); i++) {
		        column = createNode(doc, columnGroup, NODE_TABLE_COLUMN);
	    		applyColumnCustomAttributes(context, table, transformState, column, i, -1);
	    	}
    	}
	}

	protected void applyTableStyles(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element tableRoot) {
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
	  
	
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element tableRoot) {
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
