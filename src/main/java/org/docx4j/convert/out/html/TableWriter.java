package org.docx4j.convert.out.html;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;

/*
 *  Write a w:tbl as an HTML <table>.
 *  @author Alberto Zerolo, Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends AbstractTableWriter {
	protected final static Logger logger = Logger.getLogger(TableWriter.class);
	
	protected final static String TABLE_BORDER_MODEL = "border-collapse";
	protected final static String TABLE_INDENT = "margin-left"; 
	
	//This map gets used in applyAttributes, it's created here to be able to reuse it
	protected Map<String, Property> tempAttributeMap = new TreeMap<String, Property>();

  	public static String getId(int idx) {
  		return "docx4j_tbl_" + idx;
	}
	
	@Override
	protected Logger getLog() {
		return logger;
	}
	
  	@Override
	protected Element createNode(Document doc, int nodeType) {
  	Element ret = null;
  		switch (nodeType) {
  			case NODE_TABLE:
  				ret = doc.createElement("table");
  				break;
  			case NODE_TABLE_COLUMN_GROUP:
  				ret = doc.createElement("colgroup");
  				break;
  			case NODE_TABLE_COLUMN:
  				ret = doc.createElement("col");
  				break;
  			case NODE_TABLE_HEADER:
  				ret = doc.createElement("thead");
				break;
  			case NODE_TABLE_HEADER_ROW:
  				ret = doc.createElement("tr");
				break;
  			case NODE_TABLE_HEADER_CELL:
  				ret = doc.createElement("th");
				break;
  			case NODE_TABLE_BODY:
  				ret = doc.createElement("tbody");
				break;
  			case NODE_TABLE_BODY_ROW:
  				ret = doc.createElement("tr");
				break;
  			case NODE_TABLE_BODY_CELL:
  				ret = doc.createElement("td");
				break;
  		}
  		return ret;
  	}

	@Override
	protected void applyAttributes(AbstractWmlConversionContext context, List<Property> properties, Element element) {
	StringBuilder buffer = null;
		if ((properties != null) && (!properties.isEmpty())) {
			buffer = new StringBuilder();
			for (int i=0; i<properties.size(); i++) {
				tempAttributeMap.put(properties.get(i).getCssName(), properties.get(i));
			}
			for (Property property : tempAttributeMap.values()) {
				buffer.append(property.getCssProperty());
			}
			tempAttributeMap.clear();
			appendStyle(element, buffer.toString());
		}
	}
	
	@Override
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, TableModel table, TransformState transformState, Element tableRoot) {
	int cellSpacing = ((table.getEffectiveTableStyle().getTblPr() != null) &&
			   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing() != null) &&
			   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW() != null) ?
			   table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW().intValue() : 0);
	
		// Set @class
	    if (table.getStyleId()==null) {
	    	getLog().debug("table has no w:tblStyle?");
	    } else {
			StyleTree styleTree = context.getWmlPackage().getMainDocumentPart().getStyleTree();	
			Tree<AugmentedStyle> tTree = styleTree.getTableStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = tTree.get(table.getStyleId());
			tableRoot.setAttribute("class", 
					StyleTree.getHtmlClassAttributeValue(tTree, asn)			
			);
	    }
	    
	    tableRoot.setAttribute("id", 
	    		getId(((TableModelTransformState)transformState).getIdx()) );

		// border model
		// Handle cellSpacing as in xsl-fo to have a consistent look.
		if (cellSpacing > 0) {
			appendStyle(tableRoot, Property.composeCss(TABLE_BORDER_MODEL, "separate"));
			tableRoot.setAttribute("cellspacing", 
					//WW seems only to store cellSpacing/2 but displays and applies cellSpacing * 2
					convertToPixels(cellSpacing * 2));
		}
		else {
			appendStyle(tableRoot, Property.composeCss(TABLE_BORDER_MODEL, "collapse"));
		}
		
		// table width
		if (table.getTableWidth() > 0) {
			appendStyle(tableRoot, 
					Property.composeCss("width", UnitsOfMeasurement.twipToBest(table.getTableWidth())));
		}
	}

	/**
	 * Conversion of twips to pixels at 96dpi
	 * @param twips
	 * @return pixel-attribute value 
	 */
	protected String convertToPixels(int twips) {
	float pixels = (96f * twips / 1440f);
		if ((twips > 0) && (pixels < 1)) {
			pixels = 1;
		}
		return UnitsOfMeasurement.format2DP.format(pixels) + "px";
	}

	@Override
	protected void applyColumnCustomAttributes(AbstractWmlConversionContext context, TableModel table, TransformState transformState, Element column, int columnIndex, int columnWidth) {
		if ((table.getTableWidth() > 0) && (columnWidth > -1)) {
			appendStyle(column, Property.composeCss("width", 
					UnitsOfMeasurement.format2DP.format((100f * columnWidth)/table.getTableWidth()) + "%"));
		}
	}
  	
  	@Override
	protected void applyTableCellCustomAttributes(AbstractWmlConversionContext context, TableModel table, TransformState transformState, Cell tableCell, Element cellNode, boolean isHeader, boolean isDummyCell) {
  		if (isDummyCell) {
			appendStyle(cellNode, Property.composeCss("border", "none"));
			appendStyle(cellNode, Property.composeCss("background-color:", "transparent"));
  		}
  		
		if (tableCell.getExtraCols() > 0) {
			cellNode.setAttribute("colspan", Integer.toString(tableCell.getExtraCols() + 1));
			
		}
		if (tableCell.getExtraRows() > 0) {
			cellNode.setAttribute("rowspan", Integer.toString(tableCell.getExtraRows() + 1));
		}
  	}

	private void appendStyle(Element element, String newValue) {
	String style = element.getAttribute("style");
		if ((style != null) && (style.length() > 0)) {
			element.setAttribute("style", style + newValue);
		}
		else {
			element.setAttribute("style", newValue);
		}
	}
	
	
}
