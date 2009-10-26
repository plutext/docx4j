package org.docx4j.convert.out.html;

import java.math.BigInteger;
import java.util.*;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.properties.Property;
import org.docx4j.model.table.Cell;
import org.docx4j.model.table.TableModel;
import org.docx4j.wml.Style;
import org.docx4j.wml.TblGridCol;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;
import javax.xml.transform.TransformerException;

/*
 * Write a w:tbl as an HTML <table>.
 * 
 * This handles merged cells properly, with rowspan
 * and colspan.
 * 
 * As to formatting, priority is to include basic support
 * for the following:
 * 
 * width
 * =====
 * 
 * Both Word and HTML support fixed and auto models,
 * so if the w:tbl/w:tblPr/w:tblW/@w:type="auto",
 * in HTML, we'll use table-layout:auto.  
 * 
 * If w:tblW is missing, default to auto (ECMA-376 2.4.61).
 * ie no need to look at table style.
 * 
 * Row-level tblW is ignored in this implementation (20091026)
 * 
 * position (tblPr/tblInd)
 * ========
 * 
 * Implemented 20091026
 * 
 * vertical alignment (tcPr/vAlign)
 * ==================
 * 
 * Should be easy enough.
 * TODO: inherit property.
 * 
 * borders
 * =======
 * 
 * HTML has 2 models for table borders: separated and collapsing.
 * 
 * Word does as well.  If any row has a non-zero tblCellSpacing,
 * it uses separated.  Otherwise it does conflict resolution,
 * like HTML's collapsing model.  The rules are a bit different
 * though.
 * 
 * So we map to the 2 models as appropriate.
 * 
 * TODO: set the actual borders!
 * 
 * shading
 * =======
 * 
 * Not considered yet.
 * 
 * -----------
 * 
 * ECMA-376 includes row-level "exceptions", which need to be 
 * considered. 
 *  
 * 
 *  @author Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends ModelConverter {
	
  private final static Logger log = Logger.getLogger(TableWriter.class);
  
  public final static String TABLE_LAYOUT_MODE = "table-layout";
  public final static String TABLE_BORDER_MODEL = "border-collapse";
  public final static String TABLE_INDENT = "margin-left"; 

  public Node toNode(Model tableModel) throws TransformerException {
    TableModel table = (TableModel)tableModel;
    log.debug("Table asXML:\n" + table.debugStr());
    
    org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument();   
	DocumentFragment docfrag = doc.createDocumentFragment();

    Element tbl = doc.createElement("table");
    docfrag.appendChild(tbl);
    
    StringBuffer styleVal = new StringBuffer();
    // table-layout
    if (table.isTableLayoutFixed()) {
 		styleVal.append(Property.composeCss(TABLE_LAYOUT_MODE, "fixed"));
    } else {
    	styleVal.append(Property.composeCss(TABLE_LAYOUT_MODE, "auto"));
    }
    
    // position (tblPr/tblInd)
    int indent = 0;
    if (table.getTableStyle().getTblPr()!=null
    		&& table.getTableStyle().getTblPr().getTblInd()!=null) {
    	indent = table.getTableStyle().getTblPr().getTblInd().getW().intValue();
    } else if (table.getTableStyle()!=null  
    			&& table.getTableStyle().getTblPr().getTblInd()!=null
    			&& table.getTableStyle().getTblPr().getTblInd().getW() !=null) {
    	indent = table.getTableStyle().getTblPr().getTblInd().getW().intValue();    		
    }
	styleVal.append(Property.composeCss(TABLE_INDENT, UnitsOfMeasurement.twipToBest(indent)));

	// border model
	if (table.isBorderConflictResolutionRequired() ) {
		styleVal.append(Property.composeCss(TABLE_BORDER_MODEL, "collapse"));		
	} else {
		styleVal.append(Property.composeCss(TABLE_BORDER_MODEL, "separate"));				
	}
	// now the table level border defaults
    //tbl.setAttribute("border", "1" );
	
    
    tbl.setAttribute("style", styleVal.toString() );
    
    // w:tblGrid -> colgroup
    int cols = table.getColCount();
    Element colgroup =  doc.createElement("colgroup");
    tbl.appendChild(colgroup);
    if (table.getTblGrid()!=null) {
    	for( TblGridCol gridCol : table.getTblGrid().getGridCol() ) {
    		Element col =  doc.createElement("col");
    		int width = gridCol.getW().intValue();
    		col.setAttribute("style", Property.composeCss("width", UnitsOfMeasurement.twipToBest(width)));
    		colgroup.appendChild(col);
    	}
    } else {
    	log.warn("No w:tblGrid");
    	colgroup.setAttribute("span",  String.valueOf(cols));
    }
    
    for (List<Cell> rows : table.getCells()) {
			Element row = doc.createElement("tr");
			tbl.appendChild(row);
			for (Cell cell : rows) {
				// process cell
				if (!cell.isDummy()) {
					int col = cell.getColumn();
					Element cellNode = doc.createElement("td");
					row.appendChild(cellNode);
					
					// style
					if (cell.getTcPr()!=null ) {
						StringBuffer inlineStyle =  new StringBuffer();
						HtmlExporterNG.createCss(cell.getTcPr(), inlineStyle);				
						if (!inlineStyle.toString().equals("") ) {
							cellNode.setAttribute("style", inlineStyle.toString() );
						}
					}
					
					if (cell.getExtraCols() > 0) {
						cellNode.setAttribute("colspan", Integer.toString(cell
								.getExtraCols() + 1));
						
					}
					if (cell.getExtraRows() > 0) {
						cellNode.setAttribute("rowspan", Integer.toString(cell
								.getExtraRows() + 1));
					}
					// insert content into cell
					// skipping w:tc node itself, insert only its children
					if (cell.getContent() == null) {
						log.warn("model cell had no contents!");
					} else {
						log.debug("copying cell contents..");
						XmlUtils.treeCopy(cell.getContent().getChildNodes(),
								cellNode);
					}
				}
			}
		}
    return docfrag;
  }
}
