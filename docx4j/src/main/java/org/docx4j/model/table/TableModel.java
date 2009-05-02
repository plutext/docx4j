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

package org.docx4j.model.table;

import java.util.*;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.docx4j.convert.out.Converter;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.model.Model;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
//import d4n.util.XmlUtils;
import org.docx4j.XmlUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import javax.xml.transform.TransformerException;

/**
 * There are different ways to represent a table with possibly merged
 * cells. <ul>
 * <li>In html, both vertically and horizontally merged cells are
 * represented by one cell only that has a colspan and rowspan
 * attribute.  No dummy cells are used.
 * <li>In docx, horizontally merged cells are represented by one cell
 * with a gridSpan attribute; while vertically merged cells are
 * represented as a top cell containing the actual content and a series
 * of dummy cells having a vMerge tag with "continue" attribute.
 * <li>This table is a regular matrix, dummy cells are added for both
 * merge directions.
 * </ul>
 * The algorithm is as follows,<ul>
 * <li>When a cell is added, its colspan is set.  Even a dummy cell can have
 * a colspan, the same value as its upper has.
 * <li>When a new cell has a colspan greater than 1, the required extra
 * dummy cells are also added
 * <li>When a docx dummy cell is encountered (one with a vMerge
 * continue attribute), the rowspan is incremented in its upper
 * neighbors until a real cell is found.
 * </ul>
 * 
 *  @author Adam Schmideg
 * 
 */
public class TableModel extends Model {
  private final static Logger logger = Logger.getLogger(TableModel.class);

  /**
   * A list of rows
   */
  private List<List<Cell>> cells;
  
  private int row;
  private int col;

  public TableModel() {
    resetIndexes();
    cells = new Vector<List<Cell>>();
  }

  /**
   * Reset <var>row</var> and <var>col</var>.
   */
  public void resetIndexes() {
    row = -1;
    col = -1;
  }

  public void startRow() {
    cells.add(new Vector<Cell>());
    row++;
    col = -1;
  }

  /**
   * Add a new cell to this table and copy processed content of
   * <var>tc</var> to it.
   */
  public void addCell(Tc tc, Node content) {
    col++;
    Cell newCell = new Cell(this, row, col, tc, content);
    cells.get(row).add(newCell);
    // populate table with dummy cells to the right of this one
    for (int i = 0; i < newCell.getExtraCols(); i++)
      addDummyCell();
    // TODO: handle cell's gridBefore/gridAfter attrs by adding dummy cells if needed
  }

  private void addDummyCell() {
    col++;
    cells.get(row).add(new Cell(this, row, col));
  }

  public Cell getCell(int row, int col) {
    return cells.get(row).get(col);
  }

  /**
   * @return "colX" where X is a 1-based index
   */
  public String getColName(int col) {
    return "col" + String.valueOf(col + 1);
  }

  public int getColCount() {
    return cells.get(0).size();
  }

  public List<List<Cell>> getCells() {
    return cells;
  }

  /**
   * Build a table representation from a <var>tbl</var> instance
   */
  public void build(Converter inst, Node node, NodeList children) 
  	throws TransformerException {
	  
    Tbl tbl = null;
		try {
			tbl = (Tbl) XmlUtils.unmarshal(node);
		} catch (JAXBException e) {
			throw new TransformerException("Node: " + node.getNodeName() + "="
					+ node.getNodeValue(), e);
		}
		NodeList cellContents = children.item(0).getChildNodes();
		List<Object> rows = tbl.getEGContentRowContent();
		// int i = 0;
		int r = 0;
		for (Object o : rows) {
			startRow();
			Tr tr = (Tr) o;
			List<Object> cells = tr.getEGContentCellContent();
			int c = 0;
			for (Object o2 : cells) {
				Tc tc = (Tc) ((JAXBElement) o2).getValue();
				Node wtrNode = cellContents.item(r);
				addCell(tc, wtrNode.getChildNodes().item(c));
				// addCell(tc, cellContents.item(i));
				// i++;
				c++;
			}
			r++;
		}
  }

  public String debugStr() {
    StringBuffer buf = new StringBuffer();
    for (List<Cell> rows: cells) {
      for (Cell c: rows) {
        buf.append(c.debugStr());
      }
      buf.append("\n");
    }
    return buf.toString();
  }

  /**
   * A cell in the table holding its own content, too
   */
  public class Cell {
    private TableModel table;
    private int row;
    private int col;
    private int rowspan = 0;
    private int colspan = 0;
    /** If this is a real cell or only a placeholder.  Vertically merged
     * cells are represented as a real cell on the top and dummy cell(s)
     * below */
    private boolean dummy = false;
    private Node content = null;

    /**
     * Create a dummy cell without content
     */
    public Cell(TableModel table, int row, int col) {
      this.table = table;
      this.row = row;
      this.col = col;
      this.dummy = true;
    }

    public Cell(TableModel table, int row, int col, Tc tc, Node content) {
      this(table, row, col);
      dummy = false;
      this.content = content;
      
      logger.debug("Cell content: " + XmlUtils.w3CDomNodeToString(content));
      
      /* xhtmlTc.appendChild(
         document.importNode(tcDoc, true) );
         com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.importNode
         org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation
         */
      // rowspan
      try {
        String vm = tc.getTcPr().getVMerge().getVal();
        if (vm == null)
          dummy = true;
        // dummy cells propagate this call upwards until a real cell is found
        incrementRowSpan();
      }
      catch (NullPointerException ne) {
        // no vMerge
      }
      if (dummy) {
        // set its colspan to the same value as its upper neighbor,
        // so dummy cells will be created to the right if colspan>1
        colspan = table.getCell(row - 1, col).colspan;
      }
      else {
        // real cell
        // colspan
        try {
          int gridSpan = tc.getTcPr().getGridSpan().getVal().intValue();
          colspan = gridSpan;
        }
        catch (NullPointerException ne) {
          // no gridSpan
        }
      }
    }

    /**
     * How many columns are merged into this cell
     * @return 0 if none merged; 1 if two cells are merged so there is one
     * extra; etc.  A dummy cell has the same extraCols value as its upper
     * neighbor.
     */
    public int getExtraCols() {
      if (colspan < 2)
        return 0;
      else
        return colspan - 1;
    }

    public int getExtraRows() {
      if (rowspan > 1)
        return rowspan - 1;
      else
        return 0;
    }

    public boolean isDummy() {
      return dummy;
    }

    public Node getContent() {
      return content;
    }

    public int getColumn() {
      return col;
    }

    /**
     * If this is a real cell, increment rowspan; if this is a dummy,
     * propagate the call to the cell upwards
     */
    protected void incrementRowSpan() {
      if (dummy)
        table.getCell(row - 1, col).incrementRowSpan();
      else
        rowspan++;
    }

    public String debugStr() {
      String s = null;
      if (dummy)
        s = "d";
      else
        s = "r";
      s += "(" + row + "" + col + ")";
      s += colspan;
      s += rowspan;
      return s + " ";
    }
  }
}
