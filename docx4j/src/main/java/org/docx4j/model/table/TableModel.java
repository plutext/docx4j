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

import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Converter;
import org.docx4j.jaxb.Context;
import org.docx4j.model.Model;

import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.VMerge;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	protected List<List<Cell>> cells;

	private int row;
	private int col;

	/**
	 * Table properties are represented using the
	 * docx model. 
	 */
	private TblPr tblPr;
	
	private TblGrid tblGrid;
	
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
	public void build(Node node, NodeList children) throws TransformerException {

		Tbl tbl = null;
		try {
			tbl = (Tbl) XmlUtils.unmarshal(node);
		} catch (JAXBException e) {
			throw new TransformerException("Node: " + node.getNodeName() + "="
					+ node.getNodeValue(), e);
		}
		
		this.tblPr = tbl.getTblPr();
		this.tblGrid = tbl.getTblGrid();
		
		NodeList cellContents = children.item(0).getChildNodes(); // the w:tr
		List<Object> rows = tbl.getEGContentRowContent();
		// int i = 0;
		int r = 0;
		for (Object o : rows) {
			startRow();
			Tr tr = (Tr) o;
			List<Object> cells = tr.getEGContentCellContent();
			int c = 0;
			for (Object o2 : cells) {

				if (o2 instanceof javax.xml.bind.JAXBElement) {

					//					System.out.println( ((JAXBElement)o2).getName() );
					//					System.out.println( ((JAXBElement)o2).getDeclaredType().getName() + "\n\n");

					Tc tc = (Tc) ((JAXBElement) o2).getValue();
					Node wtrNode = cellContents.item(r); //w:tr
					addCell(tc, wtrNode.getChildNodes().item(c));
					// addCell(tc, cellContents.item(i));
					// i++;
					c++;

				} else {

					logger.warn("Encountered unexpected: "
							+ o2.getClass().getName());
				}
			}
			r++;
		}
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.Model#toJAXB()
	 */
	@Override
	public Object toJAXB() {
		
		ObjectFactory factory = Context.getWmlObjectFactory();		
		Tbl tbl = factory.createTbl();
		
		// <w:tblPr>
		if (tblPr==null) {
			logger.warn("tblPr is null");
			tblPr = factory.createTblPr();
			
			// Default to page width
			TblWidth tblWidth = factory.createTblWidth();
			tblWidth.setW(BigInteger.valueOf(UnitsOfMeasurement.DEFAULT_PAGE_WIDTH_TWIPS));
			tblWidth.setType("dxa"); // twips
			tblPr.setTblW(tblWidth);			
		} 
		tbl.setTblPr(tblPr);
		
		// <w:tblGrid>
		// This specifies the number of columns,
		// and also their width
		if (tblGrid==null) {
			logger.warn("tblGrid is null");
			tblGrid = factory.createTblGrid();
			// Default to equal width
			int width = Math.round( tbl.getTblPr().getTblW().getW().floatValue()/getColCount() );
			for (int i=0; i<getColCount(); i++) {
				TblGridCol tblGridCol = factory.createTblGridCol();
				tblGridCol.setW(BigInteger.valueOf(width)); // twips
				tblGrid.getGridCol().add(tblGridCol);
			}
		} 
		tbl.setTblGrid(tblGrid);
		
		// <w:tr>
		// we need a table row, even if every entry is just a vertical span,
		// so that is easy
		for (int i=0; i<cells.size(); i++) {
			Tr tr = factory.createTr();
			tbl.getEGContentRowContent().add(tr);
			
			// populate the row
			for(int j=0; j<getColCount(); j++) {
				Tc tc = factory.createTc();
				Cell cell = cells.get(i).get(j);
				if (cell==null) {
					// easy, nothing to do.
					// this is just an empty tc	
					tr.getEGContentCellContent().add(tc);
				} else {
					if (cell.isDummy() ) {
						// we need to determine whether this is a result of 
						// a vertical merge or a horizontal merge
						if (j>0 && (cells.get(i).get(j-1).isDummy() 
										|| cells.get(i).get(j-1).colspan >1 ) ) {
							// Its a horizontal merge, so
							// just leave it out
						} else if (i>0 && (cells.get(i-1).get(j).isDummy() 
								|| cells.get(i-1).get(j).rowspan >1 ) ) {
							// Its a vertical merge
							TcPr tcPr = factory.createTcPr();
							VMerge vm = factory.createTcPrInnerVMerge();
							tcPr.setVMerge( vm );
							tc.setTcPr(tcPr);
							tr.getEGContentCellContent().add(tc);
						} else {
							logger.error("Encountered phantom dummy cell at (" + i + "," + j + ") " );
							logger.debug(debugStr());
						}
						
					} else { // a real cell
						TcPr tcPr = factory.createTcPr();
						
						if (cell.colspan>1) {
							// add <w:gridSpan>
							GridSpan gridSpan = factory.createTcPrInnerGridSpan();
							gridSpan.setVal(BigInteger.valueOf(cell.colspan));
							tcPr.setGridSpan(gridSpan);
							tc.setTcPr(tcPr);
						}
						if (cell.rowspan>1) {
							// Its a vertical merge
							VMerge vm = factory.createTcPrInnerVMerge();
							vm.setVal("restart");
							tcPr.setVMerge( vm );
							tc.setTcPr(tcPr);
						}
						
						if (cell.colspan>1 && cell.rowspan>1) {
							logger.warn("Both rowspan & colspan set; that will be interesting..");
						}
												
						tr.getEGContentCellContent().add(tc);
						
						// TODO: add the cell content, if we have it.
						// We won't have compatible content if this model has
						// been created via XSLT for an outward bound conversion.
						// But in that case, this method isn't needed
						// because the developer started with the JAXB model. 
						
					}
				}
			}
			
			
		}
		
		return tbl;
	}

	private void debugCellContents(NodeList children) {

		for (int i = 0; i < children.getLength(); i++) {

			System.out.println(i);

			System.out.println(children.item(i).getTextContent());
			System.out.println(children.item(i).getLocalName());

		}

	}

	public String debugStr() {
		StringBuffer buf = new StringBuffer();
		for (List<Cell> rows : cells) {
			for (Cell c : rows) {
				if (c==null) {
					buf.append("null     ");
				} else {
					buf.append(c.debugStr());
				}
			}
			buf.append("\n");
		}
		return buf.toString();
	}

}
