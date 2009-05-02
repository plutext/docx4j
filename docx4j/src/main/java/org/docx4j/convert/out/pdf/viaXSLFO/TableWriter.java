package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.*;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.table.TableModel;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;
import javax.xml.transform.TransformerException;

/*
 *  @author Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends ModelConverter {
	
  private final static Logger logger = Logger.getLogger(TableWriter.class);

  public Node toNode(Model tableModel) throws TransformerException {
    TableModel table = (TableModel)tableModel;
    logger.debug("Table asXML:\n" + table.debugStr());
    
    org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument();   
	DocumentFragment docfrag = doc.createDocumentFragment();

    Element foTable = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table");
    docfrag.appendChild(foTable);
    
	
//	if (logger.isDebugEnabled()) {					
//		logger.debug(XmlUtils.marshaltoString(tbl, true, true));					
//	}

    int cols = table.getColCount();
    for (int i = 0; i < cols; i++) {
        String s = String.valueOf(i);
        Element foTableColumn = doc.createElementNS("http://www.w3.org/1999/XSL/Format", 
        		"fo:table-column");
        foTable.appendChild(foTableColumn);
        foTableColumn.setAttribute("column-number", s);
        //foTableColumn.setAttribute("colname", table.getColName(i));
      }
	    
	Node foTableBody = doc.createElementNS("http://www.w3.org/1999/XSL/Format", 
	"fo:table-body");			
	foTable.appendChild(foTableBody);
    
    for (List<TableModel.Cell> rows : table.getCells()) {
			// Element row = doc.createElement("tr");
			Element row = doc.createElementNS("http://www.w3.org/1999/XSL/Format", 
			"fo:table-row");			
			foTableBody.appendChild(row);
			for (TableModel.Cell cell : rows) {
				// process cell
				if (!cell.isDummy()) {
					int col = cell.getColumn();
					//Element cellNode = doc.createElement("td");
					Element cellNode = doc.createElementNS("http://www.w3.org/1999/XSL/Format", 
					"fo:table-cell");
					row.appendChild(cellNode);

					cellNode.setAttribute("border-style", "dashed");
					
					if (cell.getExtraCols() > 0) {
						cellNode.setAttribute("number-columns-spanned", Integer.toString(cell
								.getExtraCols() + 1));
						
					}
					if (cell.getExtraRows() > 0) {
						cellNode.setAttribute("number-rows-spanned", Integer.toString(cell
								.getExtraRows() + 1));
					}
					// insert content into cell
					// skipping w:tc node itself, insert only its children
					if (cell.getContent() == null) {
						logger.warn("model cell had no contents!");
					} else {
						logger.debug("copying cell contents..");
						XmlUtils.treeCopy(cell.getContent().getChildNodes(),
								cellNode);
					}
				}
			}
		}
    return docfrag;
  }
}
