package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.AbstractTableWriter;
import org.docx4j.model.TransformState;
import org.docx4j.model.properties.Property;
import org.docx4j.model.table.Cell;
import org.docx4j.model.table.TableModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 *  @author Alberto Zerolo, Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends AbstractTableWriter {
	protected final static Logger logger = Logger.getLogger(TableWriter.class);
	protected final static String TABLE_BORDER_MODEL = "border-collapse";
	
	@Override
	protected Logger getLog() {
		return logger;
	}
	
  	@Override
	protected Element createNode(Document doc, int nodeType) {
  	Element ret = null;
  		switch (nodeType) {
  			case NODE_TABLE:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table");
  				break;
  			case NODE_TABLE_COLUMN_GROUP:
  				break;
  			case NODE_TABLE_COLUMN:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-column");
  				break;
  			case NODE_TABLE_HEADER:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-header");
				break;
  			case NODE_TABLE_HEADER_ROW:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-row");
				break;
  			case NODE_TABLE_HEADER_CELL:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-cell");
				break;
  			case NODE_TABLE_BODY:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-body");
				break;
  			case NODE_TABLE_BODY_ROW:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-row");
				break;
  			case NODE_TABLE_BODY_CELL:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-cell");
				break;
  		}
  		return ret;
  	}

	@Override
	protected void applyAttributes(AbstractWmlConversionContext context, List<Property> properties, Element element) {
		Conversion.applyFoAttributes(properties, element);
	}
	
	@Override
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, TableModel table, TransformState transformState, Element tableRoot) {
	int cellSpacing = ((table.getEffectiveTableStyle().getTblPr() != null) &&
					   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing() != null) &&
					   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW() != null) ?
					   table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW().intValue() : 0);	   
		// border model
	    // borderConflictResolutionRequired in TableModel is correct, but xsl-fo only knows about a
	    // cellSpacing (border-separation) on the table level. For this reason, cellSpacings on row-level
	    // are ignored.
		if (cellSpacing > 0) {
			tableRoot.setAttribute(TABLE_BORDER_MODEL, "separate"); // this is the default in CSS
			tableRoot.setAttribute("border-separation", 
					//WW seems only to store cellSpacing/2 but displays and applies cellSpacing * 2
					UnitsOfMeasurement.twipToBest(cellSpacing * 2));
		}
		else {
			tableRoot.setAttribute(TABLE_BORDER_MODEL, "collapse");
		}
		// table width
		if (table.getTableWidth() > 0) {
			tableRoot.setAttribute("width", UnitsOfMeasurement.twipToBest(table.getTableWidth()) );		
		}
	}

	@Override
	protected void applyColumnCustomAttributes(AbstractWmlConversionContext context, TableModel table, TransformState transformState, Element column, int columnIndex, int columnWidth) {
        column.setAttribute("column-number", Integer.toString(columnIndex + 1));
		if (columnWidth > -1) {
	        column.setAttribute("column-width", UnitsOfMeasurement.twipToBest(columnWidth) );
		}
	}
  	
  	@Override
	protected void applyTableCellCustomAttributes(AbstractWmlConversionContext context, TableModel table, TransformState transformState, Cell tableCell, Element cellNode, boolean isHeader, boolean isDummyCell) {
  		if (isDummyCell) {
			cellNode.setAttribute("border-style", "none");
			cellNode.setAttribute("background-color", "transparent");
			cellNode.appendChild(cellNode.getOwnerDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block"));
  		}
  		
		if (tableCell.getExtraCols() > 0) {
			cellNode.setAttribute("number-columns-spanned", Integer.toString(tableCell.getExtraCols() + 1));
			
		}
		if (tableCell.getExtraRows() > 0) {
			cellNode.setAttribute("number-rows-spanned", Integer.toString(tableCell.getExtraRows() + 1));
		}
  	}
	
	
}
