package org.docx4j.convert.out.html;

import java.math.BigInteger;
import java.util.*;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.docx4j.model.properties.AdHocProperty;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.table.BorderBottom;
import org.docx4j.model.properties.table.BorderLeft;
import org.docx4j.model.properties.table.BorderRight;
import org.docx4j.model.properties.table.BorderTop;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.table.Cell;
import org.docx4j.model.table.TableModel;
import org.docx4j.model.table.TableModel.TableModelTransformState;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Style;
import org.docx4j.wml.TblBorders;
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
 * 
 * shading
 * =======
 * 
 * Basic implementation 20091027
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

  public static String getId(int idx) {
	  return "docx4j_tbl_" + idx;
  }
  
  public Node toNode(Model tableModel, TransformState transformState) throws TransformerException {

	    org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument();   
	  
		return toNode(tableModel, transformState, doc);
  }
  
  public Node toNode(Model tableModel, TransformState transformState, org.w3c.dom.Document doc) throws TransformerException {
	  
    TableModel table = (TableModel)tableModel;
    
    TableModelTransformState state = (TableModelTransformState)transformState; 
    
    log.debug("Table asXML:\n" + table.debugStr());
    
    Element tbl = doc.createElement("table");

    DocumentFragment docfrag = doc.createDocumentFragment();
    docfrag.appendChild(tbl);
    
	// Set @class
    if (table.getStyleId()==null) {
    	log.debug("table has no w:tblStyle?");
    } else {
		StyleTree styleTree = wordMLPackage.getMainDocumentPart().getStyleTree();	
		Tree<AugmentedStyle> tTree = styleTree.getTableStylesTree();		
		org.docx4j.model.styles.Node<AugmentedStyle> asn = tTree.get(table.getStyleId());
		tbl.setAttribute("class", 
				StyleTree.getHtmlClassAttributeValue(tTree, asn)			
		);
    }
    
    tbl.setAttribute("id", getId(state.getIdx()) );
    
    StringBuffer styleVal = new StringBuffer();
	List<Property> properties = PropertyFactory.createProperties(table.getTblPr());    	
	for( Property p :  properties ) {
		// This handles:
		// - position (tblPr/tblInd)
		// - table-layout
		styleVal.append(p.getCssProperty());
	}  
	// Borders, shading - remember there is both
	// 1. getTblPr().getTblBorders(), and
	// 2. getTcPr().getTcBorders() [in a style or on a tc] 
	// Here, we are only concerned with the table's outer borders.
	// The cell borders are handled below.
	if (table.getTblPr().getTblBorders()!=null) {
		// That, not table.getEffectiveTableStyle(),
		// since those will be applied via the @class styles
		properties = PropertyFactory.createProperties(table.getTblPr() );
		for( Property p :  properties ) {
			if (p!=null) {
				styleVal.append(p.getCssProperty());
			}
		}
	}
	
	
//	// vAlign fix: match Word's default of top
//	if (table.getEffectiveTableStyle().getTcPr()==null
//			|| table.getEffectiveTableStyle().getTcPr().getVAlign()==null) {
//		styleVal.append(Property.composeCss(org.docx4j.model.properties.table.tc.TextAlignmentVertical.CSS_NAME, 
//				"top"));
//	}

	// border model
	if (table.isBorderConflictResolutionRequired() ) {
		styleVal.append(Property.composeCss(TABLE_BORDER_MODEL, "collapse"));		
	} else {
		styleVal.append(Property.composeCss(TABLE_BORDER_MODEL, "separate")); // this is the default in CSS				
	}
	
	// Table level cell border defaults
    // Could do something like tbl.setAttribute("rules", "all" );
	// but instead, these are handled by CSS for td in the stylesheet.
    
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

//    Element tgroup = doc.createElement("tgroup");
//    tbl.appendChild(tgroup);
    
    Element tbody = doc.createElement("tbody");
    tbl.appendChild(tbody);
    
    for (List<Cell> rows : table.getCells()) {
			Element row = doc.createElement("tr");
			tbody.appendChild(row);
			
			// vAlign fix: match Word's default of top
			if (table.getEffectiveTableStyle().getTcPr()==null
					|| table.getEffectiveTableStyle().getTcPr().getVAlign()==null) {
				row.setAttribute("style",(Property.composeCss(org.docx4j.model.properties.table.tc.TextAlignmentVertical.CSS_NAME, 
						"top")));
			}
			
			for (Cell cell : rows) {
				// process cell
				if (!cell.isDummy()) {
					int col = cell.getColumn();
					Element cellNode = doc.createElement("td");
					row.appendChild(cellNode);
					
					// style
					if (cell.getTcPr()!=null ) {
						StringBuffer inlineStyle =  new StringBuffer();
						AbstractHtmlExporter.createCss(cell.getTcPr(), inlineStyle);				
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
    // ready for next table
    state.incrementIdx();
    
    return docfrag;
  }
}
