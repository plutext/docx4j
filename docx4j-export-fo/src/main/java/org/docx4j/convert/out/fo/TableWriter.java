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
package org.docx4j.convert.out.fo;

import java.util.List;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractTableWriter;
import org.docx4j.convert.out.common.writer.AbstractTableWriterModel;
import org.docx4j.convert.out.common.writer.AbstractTableWriterModelCell;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.table.tc.TextDir;
import org.docx4j.model.table.TableModelCell;
import org.docx4j.wml.TcPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 *  @author Alberto Zerolo, Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends AbstractTableWriter {
	protected final static Logger logger = LoggerFactory.getLogger(TableWriter.class);
	protected final static String TABLE_BORDER_MODEL = "border-collapse";
	
//	@Override
//	protected Logger getLog() {
//		return logger;
//	}
	
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
		XsltFOFunctions.applyFoAttributes(properties, element);
	}
	
	@Override
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, 
			AbstractTableWriterModel table, TransformState transformState, Element tableRoot) {
		
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
		
		
		// Hebrew: columns appear in reverse order
		// see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/bidiVisual.html
		// @since 3.0.2
		if ((table.getEffectiveTableStyle().getTblPr() != null) 
				&& (table.getEffectiveTableStyle().getTblPr().getBidiVisual()!=null) 
				&& (table.getEffectiveTableStyle().getTblPr().getBidiVisual().isVal()) ) {

			tableRoot.setAttribute("writing-mode", "rl-tb");
			
		}
				
	}

	@Override
	protected void applyColumnCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element column, int columnIndex, int columnWidth) {
        column.setAttribute("column-number", Integer.toString(columnIndex + 1));
		if (columnWidth > -1) {
	        column.setAttribute("column-width", UnitsOfMeasurement.twipToBest(columnWidth) );
		}
	}
  	
  	@Override
	protected void applyTableCellCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, 
			TransformState transformState, 
			TableModelCell tableCell, Element cellNode, boolean isHeader, boolean isDummyCell) {
  		
  		if (isDummyCell) {
			cellNode.setAttribute("border-style", "none");
			cellNode.setAttribute("background-color", "transparent");
			cellNode.appendChild(cellNode.getOwnerDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block"));
			return;
			/* return prevents

				 org.apache.fop.fo.ValidationException: The column-number or number of cells in the row overflows the number of fo:table-columns specified for the table. 
					at org.apache.fop.events.ValidationExceptionFactory.createException(ValidationExceptionFactory.java:38)
					at org.apache.fop.events.EventExceptionManager.throwException(EventExceptionManager.java:58)
					at org.apache.fop.events.DefaultEventBroadcaster$1.invoke(DefaultEventBroadcaster.java:175)
					at $Proxy37.tooManyCells(Unknown Source)
					at org.apache.fop.fo.flow.table.TableCellContainer.addTableCellChild(TableCellContainer.java:75)
					
				review whether this is the correct fix.
					
			*/
  		}
  		
		if (tableCell.getExtraCols() > 0) {
			
			cellNode.setAttribute("number-columns-spanned", Integer.toString(tableCell.getExtraCols() + 1));
			
		}
		if (tableCell.getExtraRows() > 0) {
			cellNode.setAttribute("number-rows-spanned", Integer.toString(tableCell.getExtraRows() + 1));
		}
  	}
	
  	@Override
  	protected void applyTableRowContainerCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, 
  			TransformState transformState, 
  			Element rowContainer, boolean isHeader) {
  		
  		// since start-indent is inherited, we need to counteract any setting on the table itself
  		// see http://stackoverflow.com/questions/12391778/shift-a-fop-table-to-the-right
  		rowContainer.setAttribute("start-indent", "0in");
  		
  	}
  	
    /**
     * In the FO case, if we need to rotate the text, we do that
     * by inserting a block-container.
     * 
     * @param cellNode
     * @return
     */
  	@Override
    protected Element interposeBlockContainer(Document doc, Element cellNode, TcPr tcPr) {
    	
  		if (tcPr==null || tcPr.getTextDirection()==null) {
  			// usual case
  			return cellNode;
  		} else {
  			
  			/* We need block-container, something like:
  			 * 
	          <table-cell>
	            <block-container reference-orientation="90">
	              <block>Hello</block>
	            </block-container>
	          </table-cell>
            */
  			
  			Element ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block-container");
  			
  			TextDir textDir = new TextDir(tcPr.getTextDirection());
  			textDir.setXslFO(ret);
  			
  			cellNode.appendChild(ret);
  			
  			if (cellNode.hasAttribute("reference-orientation")) {
  				// remove it, since it doesn't work at that level
  				cellNode.removeAttribute("reference-orientation");
  			}
  			
  			return ret;
  			
  		}
    }
  	
	
}
