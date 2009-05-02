package org.docx4j.convert.out.html;

import java.util.*;
import org.docx4j.Dom4jUtils;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.table.TableModel;
import org.w3c.dom.Node;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.apache.log4j.Logger;
import javax.xml.transform.TransformerException;

/*
 *  @author Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends ModelConverter {

	// TODO: rewrite without use of dom4j
	
  private final static Logger logger = Logger.getLogger(TableWriter.class);

  public Node toNode(Model tableModel) throws TransformerException {
    TableModel table = (TableModel)tableModel;
    logger.debug("Table asXML:\n" + table.debugStr());
    Document doc = DocumentHelper.createDocument();

    Element tbl = doc.addElement("table");
    int cols = table.getColCount();
    Element tgroup = tbl.addElement("colgroup")
      .addAttribute("span", String.valueOf(cols));
    for (List<TableModel.Cell> rows : table.getCells()) {
			Element row = tbl.addElement("tr");
			for (TableModel.Cell cell : rows) {
				// process cell
				if (!cell.isDummy()) {
					int col = cell.getColumn();
					Element cellNode = row.addElement("td");
					if (cell.getExtraCols() > 0) {
						cellNode.addAttribute("colspan", Integer.toString(cell
								.getExtraCols() + 1));
					}
					if (cell.getExtraRows() > 0) {
						cellNode.addAttribute("rowspan", Integer.toString(cell
								.getExtraRows() + 1));
					}
					// insert content into cell
					// skipping w:tc node itself, insert only its children
					if (cell.getContent() == null) {
						logger.warn("model cell had no contents!");
					} else {
						logger.debug("copying cell contents..");
						Dom4jUtils.treeCopy(cell.getContent().getChildNodes(),
								cellNode);
					}
				}
			}
		}
    return Dom4jUtils.asDocumentFragment(doc);
  }
}
