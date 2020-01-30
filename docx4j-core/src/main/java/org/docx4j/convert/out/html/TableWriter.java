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
package org.docx4j.convert.out.html;

import java.util.List;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractTableWriter;
import org.docx4j.convert.out.common.writer.AbstractTableWriterModel;
import org.docx4j.model.properties.Property;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.table.TableModelCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 *  Write a w:tbl as an HTML <table>.
 *  @author Alberto Zerolo, Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends AbstractTableWriter {
	protected final static Logger log = LoggerFactory.getLogger(TableWriter.class);
	
	protected final static String TABLE_BORDER_MODEL = "border-collapse";
	protected final static String TABLE_INDENT = "margin-left"; 

  	public static String getId(int idx) {
  		return "docx4j_tbl_" + idx;
	}
	
//	@Override
//	protected Logger getLog() {
//		return logger;
//	}
	
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
		HtmlCssHelper.applyAttributes(properties, element);
	}
	
	@Override
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, 
			TransformState transformState, Element tableRoot) {
		
	int cellSpacing = ((table.getEffectiveTableStyle().getTblPr() != null) &&
			   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing() != null) &&
			   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW() != null) ?
			   table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW().intValue() : 0);
	
		// Set @class
	    if (table.getStyleId()==null) {
	    	log.debug("table has no w:tblStyle?");
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
	    ((TableModelTransformState)transformState).incrementIdx();

		// border model
		// Handle cellSpacing as in xsl-fo to have a consistent look.
		if (cellSpacing > 0) {
			HtmlCssHelper.appendStyle(tableRoot, Property.composeCss(TABLE_BORDER_MODEL, "separate"));
			tableRoot.setAttribute("cellspacing", 
					//WW seems only to store cellSpacing/2 but displays and applies cellSpacing * 2
					convertToPixels(cellSpacing * 2));
		}
		else {
			HtmlCssHelper.appendStyle(tableRoot, Property.composeCss(TABLE_BORDER_MODEL, "collapse"));
		}
		
		// table width
		if (table.getTableWidth() > 0) {
			
			if (/* handle % */ table.getEffectiveTableStyle().getTblPr() != null
					&& table.getEffectiveTableStyle().getTblPr().getTblW() != null
					&& "pct".equals(table.getEffectiveTableStyle().getTblPr().getTblW().getType() )) {
				
				HtmlCssHelper.appendStyle(tableRoot, 
						Property.composeCss("width", 
								table.getEffectiveTableStyle().getTblPr().getTblW().getW().intValue()/50 +"%" )); // fiftieths of a percent
				
			} else {
			
				String widthProp = Property.composeCss("width", UnitsOfMeasurement.twipToBest(table.getTableWidth()));
				HtmlCssHelper.appendStyle(tableRoot, widthProp );
			}
		}
		
		// Hebrew: columns appear in reverse order
		// see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/bidiVisual.html
		// @since 3.0.2
		if ((table.getEffectiveTableStyle().getTblPr() != null) 
				&& (table.getEffectiveTableStyle().getTblPr().getBidiVisual()!=null) 
				&& (table.getEffectiveTableStyle().getTblPr().getBidiVisual().isVal()) ) {

			HtmlCssHelper.appendStyle(tableRoot, Property.composeCss("direction", "rtl"));
			
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
	protected void applyColumnCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element column, int columnIndex, int columnWidth) {
		if ((table.getTableWidth() > 0) && (columnWidth > -1)) {
			HtmlCssHelper.appendStyle(column, Property.composeCss("width", 
					UnitsOfMeasurement.format2DP.format((100f * columnWidth)/table.getTableWidth()) + "%"));
		}
	}
  	
  	@Override
	protected void applyTableCellCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, 
			TableModelCell tableCell, Element cellNode, boolean isHeader, boolean isDummyCell) {
  		if (isDummyCell) {
  			HtmlCssHelper.appendStyle(cellNode, Property.composeCss("border", "none"));
  			HtmlCssHelper.appendStyle(cellNode, Property.composeCss("background-color:", "transparent"));
  		}
  		
		if (tableCell.getExtraCols() > 0) {
			cellNode.setAttribute("colspan", Integer.toString(tableCell.getExtraCols() + 1));
			
		}
		if (tableCell.getExtraRows() > 0) {
			cellNode.setAttribute("rowspan", Integer.toString(tableCell.getExtraRows() + 1));
		}
  	}
	
}
