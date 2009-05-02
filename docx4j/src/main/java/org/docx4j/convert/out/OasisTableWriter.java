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

package org.docx4j.convert.out;

//import java.io.*;
import java.util.*;
//import d4n.util.XmlUtils;
import org.docx4j.Dom4jUtils;
import org.docx4j.XmlUtils;
import org.docx4j.model.Model;
import org.docx4j.model.table.TableModel;
import org.w3c.dom.Node;
import org.dom4j.Document;
//import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
//import org.dom4j.io.DOMWriter;
//import org.dom4j.io.XMLWriter;
//import org.dom4j.io.OutputFormat;
//import org.w3c.dom.DocumentFragment;
import org.apache.log4j.Logger;
import javax.xml.transform.TransformerException;

/*
 *  @author Adam Schmideg
 *  
*/
public class OasisTableWriter extends ModelConverter {

	// TODO: rewrite without use of dom4j
	
  private final static Logger logger = Logger.getLogger(OasisTableWriter.class);

  public Node toNode(Model tableModel) throws TransformerException {
    TableModel table = (TableModel)tableModel;
    logger.debug("Table asXML:\n" + table.debugStr());
    Document doc = DocumentHelper.createDocument();
    Element tables = doc.addElement("tables")
      .addAttribute("id", "table");
    Element tbl = tables.addElement("table")
      .addAttribute("frame", "all");
    tbl.addElement("title");
    int cols = table.getColCount();
    Element tgroup = tbl.addElement("tgroup")
      .addAttribute("cols", String.valueOf(cols));
    for (int i = 0; i < cols; i++) {
      String s = String.valueOf(i);
      tgroup.addElement("colspec")
        .addAttribute("colnum", s)
        .addAttribute("colname", table.getColName(i));
    }
    Element tbody = tgroup.addElement("tbody");
    for (List<TableModel.Cell> rows: table.getCells()) {
      Element row = tbody.addElement("row");
      for (TableModel.Cell cell: rows) {
        // process cell
        if (! cell.isDummy()) {
          int col = cell.getColumn();
          String start = table.getColName(col);
          String end = table.getColName(col + cell.getExtraCols());
          Element cellNode = row.addElement("entry")
            .addAttribute("namest", start)
            .addAttribute("nameend", end);
          int morerows = cell.getExtraRows();
          if (morerows > 0)
            cellNode.addAttribute("morerows", String.valueOf(morerows));
          // insert content into cell
          // skipping w:tc node itself, insert only its children
          Dom4jUtils.treeCopy(cell.getContent().getChildNodes(), cellNode);
        }
      }
    }
    return Dom4jUtils.asDocumentFragment(doc);
  }
}
