package org.docx4j.convert.out.html;

import java.util.*;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.table.Cell;
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

    Element tbl = doc.createElement("table");
    docfrag.appendChild(tbl);
    
    int cols = table.getColCount();
    Element tgroup =  doc.createElement("colgroup");
    tbl.appendChild(tgroup);
    tgroup.setAttribute("span",  String.valueOf(cols));
    for (List<Cell> rows : table.getCells()) {
			Element row = doc.createElement("tr");
			tbl.appendChild(row);
			for (Cell cell : rows) {
				// process cell
				if (!cell.isDummy()) {
					int col = cell.getColumn();
					Element cellNode = doc.createElement("td");
					row.appendChild(cellNode);
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
