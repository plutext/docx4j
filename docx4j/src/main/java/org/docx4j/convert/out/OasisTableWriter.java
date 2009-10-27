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

import java.util.*;
import org.docx4j.XmlUtils;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.docx4j.model.table.Cell;
import org.docx4j.model.table.TableModel;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
//import org.w3c.dom.DocumentFragment;
import org.apache.log4j.Logger;
import javax.xml.transform.TransformerException;

/*
 *  @author Adam Schmideg
 *  
*/
public class OasisTableWriter extends ModelConverter {
	
  private final static Logger logger = Logger.getLogger(OasisTableWriter.class);

  public Node toNode(Model tableModel, TransformState state) throws TransformerException {
    TableModel table = (TableModel)tableModel;
    logger.debug("Table asXML:\n" + table.debugStr());
    
    org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument();   
	DocumentFragment docfrag = doc.createDocumentFragment();

    Element tbls = doc.createElement("tables");
    tbls.setAttribute("id", "table");
    docfrag.appendChild(tbls);

    Element tbl = doc.createElement("table");
    tbl.setAttribute("frame", "all");
    tbls.appendChild(tbl);

    Element title = doc.createElement("title");
    tbl.appendChild(title);
    
    int cols = table.getColCount();
    Element tgroup = doc.createElement("tgroup");
    tbl.appendChild(tgroup);
    tgroup.setAttribute("cols", String.valueOf(cols));
    
    for (int i = 0; i < cols; i++) {
      String s = String.valueOf(i);
      Element colspec = doc.createElement("colspec");
      tgroup.appendChild(colspec);
      colspec.setAttribute("colnum", s);
      colspec.setAttribute("colname", table.getColName(i));
    }
    Element tbody = doc.createElement("tbody");
    tgroup.appendChild(tbody);
    
    for (List<Cell> rows : table.getCells()) {
			Element row = doc.createElement("row");
			tbody.appendChild(row);
			for (Cell cell : rows) {
				// process cell
				if (!cell.isDummy()) {
					int col = cell.getColumn();
					String start = table.getColName(col);
					String end = table.getColName(col + cell.getExtraCols());
					
				    Element cellNode = doc.createElement("entry");
				    row.appendChild(cellNode);
				    cellNode.setAttribute("namest", start);
				    cellNode.setAttribute("nameend", end);
					int morerows = cell.getExtraRows();
					if (morerows > 0)
						cellNode.setAttribute("morerows", String
								.valueOf(morerows));
					// insert content into cell
					// skipping w:tc node itself, insert only its children
					XmlUtils.treeCopy(cell.getContent().getChildNodes(),
							cellNode);
				}
			}
		}
    return docfrag;
  }
}
