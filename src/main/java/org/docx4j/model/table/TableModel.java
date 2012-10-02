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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.model.Model;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CTSdtRow;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Style;
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


import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

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
 * This model captures:
 * - whether the table layout is fixed or auto (Word usually does auto)
 * - whether conflict resolution is required on cell borders (Word usually 
 *   does conflict resolution)
 * 
 *  @author Adam Schmideg
 * 
 */
public class TableModel extends Model {
	private final static Logger log = Logger.getLogger(TableModel.class);

	public TableModel() {
		resetIndexes();
		cells = new Vector<List<Cell>>();
	}

	// TODO, retire this
	private final static int DEFAULT_PAGE_WIDTH_TWIPS = 12240;  // LETTER; A4 would be 11907
	
	/**
	 * A list of rows
	 */
	protected List<List<Cell>> cells;
	
	private int row;
	private int col;
	
	protected String styleId; 
	/**
	 * @return the table's style, if any
	 */
	public String getStyleId() {
		return styleId;
	}
	
	protected Style effectiveTableStyle;
	/**
	 * @return the table's effective Style
	 */
	public Style getEffectiveTableStyle() {
		return effectiveTableStyle;
	}

	/**
	 * Table properties are represented using the
	 * docx model. 
	 */
	protected TblPr tblPr;

	/**
	 * @return the w:tblPr
	 */
	public TblPr getTblPr() {
		return tblPr;
	}
	
	protected TblGrid tblGrid;
	/**
	 * @return the w:tblGrid
	 */
	public TblGrid getTblGrid() {
		return tblGrid;
	}
	

	// TODO - we will eventually need a representation of row properties
	// We could either store these in a list, or
	// keep a reference to the w:tbl itself.

	// We don't need this in our table model,
	// at least for HTML. (PropertyFactory takes care of it)
	
//	boolean tableLayoutFixed = false; // default to auto
//	/**
//	 * @return isTableLayoutFixed
//	 */
//	public boolean isTableLayoutFixed() {
//		return tableLayoutFixed;
//	}
	
	
	boolean borderConflictResolutionRequired = true;
	/**
	 * If borderConflictResolutionRequired is required, we need
	 * to set this explicitly, because in CSS, 'separate' is
	 * the default.  We need to avoid incorrectly
	 * overruling an inherited value (ie where TblCellSpacing
	 * is set), so we do borderConflictResolutionRequired here,
	 * as an explicit \@style value, rather than that in 
	 * conjunction with \@class.		
	 *
	 * @return  borderConflictResolutionRequired
	 */
	public boolean isBorderConflictResolutionRequired() {
		return borderConflictResolutionRequired;
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
	 * Build a table representation from a <var>tbl</var> instance.
	 * Remember to set wordMLPackage before using this method!
	 */
	public void build(Node node, NodeList children) throws TransformerException {

		Tbl tbl = null;
		try {
			tbl = (Tbl) XmlUtils.unmarshal(node);
		} catch (JAXBException e) {
			throw new TransformerException("Node: " + node.getNodeName() + "="
					+ node.getNodeValue(), e);
		}
		build(tbl, children.item(0));
		
	}
	
	/**
	 * Build a table representation from a <var>tbl</var> instance.
	 * Remember to set wordMLPackage before using this method!
	 */
	public void build(Tbl tbl, Node content) throws TransformerException {

		if (tbl.getTblPr()!=null
				&& tbl.getTblPr().getTblStyle()!=null) {
			styleId = tbl.getTblPr().getTblStyle().getVal();			
		}
		
		
		this.tblGrid = tbl.getTblGrid();
		
		this.tblPr = tbl.getTblPr();
		
		PropertyResolver pr;
			try {
				pr = new PropertyResolver(wordMLPackage);
			} catch (Docx4JException e) {
				throw new TransformerException("Hmmm", e);
			} 
			
		effectiveTableStyle = pr.getEffectiveTableStyle(tbl.getTblPr() );
		CTTblPrBase tblPr = effectiveTableStyle.getTblPr();
		if (tblPr!=null && tblPr.getTblCellSpacing()!=null) {
			borderConflictResolutionRequired = false;							
		}
				
//	    if (tblPr!=null
//	    		&& tblPr.getTblW()!=null) {
//	    	if (tblPr.getTblW().getType()!=null 
//	    			&& (tblPr.getTblW().getType().equals("auto")
//	    					|| tblPr.getTblW().getType().equals("nil") )) {
//	    		// @w:type
//	    					// nil, per Word 2007 implementation note
//	    		tableLayoutFixed = false;
//	    	} else if (tblPr.getTblW().getW()!=null ){
//	    		// @w:w
//	    		if (tblPr.getTblW().getW() == BigInteger.ZERO) {
//	    			// Word 2007 implementation note
//	    			tableLayoutFixed = false;
//	    		} else {
//	    			tableLayoutFixed = true;
//	    		}
//	    	} else {
//	    		// no attributes!!
//	    		tableLayoutFixed = false;
//	    	}
//	    } else {
//	    	// element omitted, so type is auto (2.4.61)
//	    	tableLayoutFixed = false;
//	    }
		
		
		NodeList cellContents = content.getChildNodes(); // the w:tr
		
		TrFinder trFinder = new TrFinder();
		new TraversalUtil(tbl, trFinder);
		
		int r = 0;
		for (Tr tr : trFinder.trList) {
				startRow();
				handleRow(cellContents, tr, r);
				r++;
		}
	}
	
	static class TrFinder extends CallbackImpl {
		
		List<Tr> trList = new ArrayList<Tr>();  
				
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof Tr ) {
				
				trList.add((Tr)o);
			}			
			return null; 
		}
	}
	
	static class TcFinder extends CallbackImpl {
		
		List<Tc> tcList = new ArrayList<Tc>();  
				
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof Tc ) {
				
				tcList.add((Tc)o);
			}			
			return null; 
		}
	}
	
	/*
	 * TrFinder and TcFinder can find tr and tc in a complex
	 * case such as the following:
	 * 
		    <w:tbl>
		      <w:tblPr>
		        <w:tblStyle w:val="TableGrid"/>
		        <w:tblW w:type="auto" w:w="0"/>
		        <w:tblLook w:val="04A0"/>
		      </w:tblPr>
		      <w:tblGrid>
		        <w:gridCol w:w="4621"/>
		        <w:gridCol w:w="4621"/>
		      </w:tblGrid>
		      <w:tr w:rsidTr="005051A3" w:rsidR="005051A3">
		        <w:tc>
		          <w:tcPr>
		            <w:tcW w:type="dxa" w:w="4621"/>
		          </w:tcPr>
		          <w:p w:rsidRDefault="005051A3" w:rsidR="005051A3">
		            <w:r>
		              <w:t>Desscription</w:t>
		            </w:r>
		          </w:p>
		        </w:tc>
		        <w:tc>
		          <w:tcPr>
		            <w:tcW w:type="dxa" w:w="4621"/>
		          </w:tcPr>
		          <w:p w:rsidRDefault="005051A3" w:rsidR="005051A3">
		            <w:r>
		              <w:t>Price</w:t>
		            </w:r>
		          </w:p>
		        </w:tc>
		      </w:tr>
		      <w:sdt>
		        <w:sdtPr>
		          <w:alias w:val="REPEAT tr"/>
		          <w:tag w:val="od:rptd=hU9bp&amp;od:RptInst=2"/>
		          <w:id w:val="65990884"/>
		        </w:sdtPr>
		        <w:sdtContent>
		          <w:tr w:rsidTr="005051A3" w:rsidR="005051A3">
		            <w:sdt>
		              <w:sdtPr>
		                <w:alias w:val="desc"/>
		                <w:tag w:val="od:xpath=NFSsi_0"/>
		                <w:dataBinding w:storeItemID="{80261315-781E-4194-879C-F78D116D6A5E}" w:xpath="/oda:answers/oda:repeat[@qref='tr_oB']/oda:row[1][1]/oda:answer[@id='desc_UM']" w:prefixMappings="xmlns:oda='http://opendope.org/answers'"/>
		                <w:text w:multiLine="true"/>
		                <w:id w:val="1529259979"/>
		              </w:sdtPr>
		              <w:sdtContent>
		                <w:tc>
		                  <w:tcPr>
		                    <w:tcW w:type="dxa" w:w="4621"/>
		                  </w:tcPr>
		                  <w:p>
		                    <w:r>
		                      <w:t>banana</w:t>
		                    </w:r>
		                  </w:p>
		                </w:tc>
		              </w:sdtContent>
		            </w:sdt>
		            <w:bookmarkStart w:name="_GoBack" w:displacedByCustomXml="next" w:id="0"/>
		            <w:bookmarkEnd w:displacedByCustomXml="next" w:id="0"/>
		            <w:sdt>
		              <w:sdtPr>
		                <w:alias w:val="price"/>
		                <w:tag w:val="od:xpath=OhZO5_0"/>
		                <w:dataBinding w:storeItemID="{80261315-781E-4194-879C-F78D116D6A5E}" w:xpath="/oda:answers/oda:repeat[@qref='tr_oB']/oda:row[1][1]/oda:answer[@id='price_a7']" w:prefixMappings="xmlns:oda='http://opendope.org/answers'"/>
		                <w:text w:multiLine="true"/>
		                <w:id w:val="494684987"/>
		              </w:sdtPr>
		              <w:sdtContent>
		                <w:tc>
		                  <w:tcPr>
		                    <w:tcW w:type="dxa" w:w="4621"/>
		                  </w:tcPr>
		                  <w:p>
		                    <w:r>
		                      <w:t>10</w:t>
		                    </w:r>
		                  </w:p>
		                </w:tc>
		              </w:sdtContent>
		            </w:sdt>
		          </w:tr>
		        </w:sdtContent>
		      </w:sdt>
		      <w:sdt>
		        <w:sdtPr>
		          <w:alias w:val="REPEAT tr"/>
		          <w:tag w:val="od:rptd=hU9bp&amp;od:RptInst=2"/>
		          <w:id w:val="351087912"/>
		        </w:sdtPr>
		        <w:sdtContent>
		          <w:tr w:rsidTr="005051A3" w:rsidR="005051A3">
		            <w:sdt>
		              <w:sdtPr>
		                <w:alias w:val="desc"/>
		                <w:tag w:val="od:xpath=NFSsi_1"/>
		                <w:dataBinding w:storeItemID="{80261315-781E-4194-879C-F78D116D6A5E}" w:xpath="/oda:answers/oda:repeat[@qref='tr_oB']/oda:row[2][1]/oda:answer[@id='desc_UM']" w:prefixMappings="xmlns:oda='http://opendope.org/answers'"/>
		                <w:text w:multiLine="true"/>
		                <w:id w:val="178426324"/>
		              </w:sdtPr>
		              <w:sdtContent>
		                <w:tc>
		                  <w:tcPr>
		                    <w:tcW w:type="dxa" w:w="4621"/>
		                  </w:tcPr>
		                  <w:p>
		                    <w:r>
		                      <w:t>apple</w:t>
		                    </w:r>
		                  </w:p>
		                </w:tc>
		              </w:sdtContent>
		            </w:sdt>
		            <w:bookmarkStart w:name="_GoBack" w:displacedByCustomXml="next" w:id="0"/>
		            <w:bookmarkEnd w:displacedByCustomXml="next" w:id="0"/>
		            <w:sdt>
		              <w:sdtPr>
		                <w:alias w:val="price"/>
		                <w:tag w:val="od:xpath=OhZO5_1"/>
		                <w:dataBinding w:storeItemID="{80261315-781E-4194-879C-F78D116D6A5E}" w:xpath="/oda:answers/oda:repeat[@qref='tr_oB']/oda:row[2][1]/oda:answer[@id='price_a7']" w:prefixMappings="xmlns:oda='http://opendope.org/answers'"/>
		                <w:text w:multiLine="true"/>
		                <w:id w:val="1236330390"/>
		              </w:sdtPr>
		              <w:sdtContent>
		                <w:tc>
		                  <w:tcPr>
		                    <w:tcW w:type="dxa" w:w="4621"/>
		                  </w:tcPr>
		                  <w:p>
		                    <w:r>
		                      <w:t>20</w:t>
		                    </w:r>
		                  </w:p>
		                </w:tc>
		              </w:sdtContent>
		            </w:sdt>
		          </w:tr>
		        </w:sdtContent>
		      </w:sdt>
		    </w:tbl>
	 */
	

	private void handleRow(NodeList cellContents, Tr tr, int r) {

		log.debug("Processing r " + r);
		
		if (borderConflictResolutionRequired && tr.getTblPrEx() != null
				&& tr.getTblPrEx().getTblCellSpacing() != null) {
			borderConflictResolutionRequired = false;
		}
		
		TcFinder tcFinder = new TcFinder();
		new TraversalUtil(tr, tcFinder);
		
		//List<Object> cells = tr.getEGContentCellContent();
		int c = 0;
		log.debug("Processing c " + c);
		for (Tc tc : tcFinder.tcList) {

			Node wtrNode = cellContents.item(r); // w:tr
			if (wtrNode==null ) {
				log.warn("Couldn't find item " + r);
			}
			addCell(tc, getTc(wtrNode, c, new IntRef(0))); // the cell content
			// addCell(tc, cellContents.item(i));
			// i++;
			c++;
		}

	}
	
	/**
	 * The tc could be inside something else, so find it recursively.
	 * @param wtrNode
	 * @param wanted
	 * @param current
	 * @return
	 */
	private Node getTc(Node wtrNode, int wanted, IntRef current) {
		
		for (int i=0; i<wtrNode.getChildNodes().getLength(); i++ ) {
			
			Node thisChild = wtrNode.getChildNodes().item(i);
			
			log.debug("Looking at " + thisChild.getLocalName() + "; have encountered " + current.i);
			
			if (thisChild.getLocalName().equals("tc") ) {
				if (current.i==wanted) return thisChild;
				current.increment();
			} else {
				// could be inside
				Node n = getTc(thisChild, wanted, current);
				if (n!=null) return n;
			}
		}
		return null;
	}
	
	static class IntRef {
		
		IntRef(int i) {
			this.i = i;
		}
		
		int i;
		
		void increment() {
			i++;
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
		TblPr tblPr = null;
		if (tblPr==null) {
			log.warn("tblPr is null");
			tblPr = factory.createTblPr();
			
			// Default to page width
			TblWidth tblWidth = factory.createTblWidth();
			tblWidth.setW(BigInteger.valueOf(DEFAULT_PAGE_WIDTH_TWIPS));
				// TODO: shouldn't hard code that.  Pass it in?
			tblWidth.setType("dxa"); // twips
			tblPr.setTblW(tblWidth);			
		} 
		tbl.setTblPr(tblPr);
		
		// <w:tblGrid>
		// This specifies the number of columns,
		// and also their width
		if (tblGrid==null) {
			log.warn("tblGrid is null");
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
							
							// Must have an empty paragraph
							P p = factory.createP();
							tc.getEGBlockLevelElts().add(p);
						} else {
							log.error("Encountered phantom dummy cell at (" + i + "," + j + ") " );
							log.debug(debugStr());
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
							log.warn("Both rowspan & colspan set; that will be interesting..");
						}
												
						tr.getEGContentCellContent().add(tc);
						
						// Add the cell content, if we have it.
						// We won't have compatible content if this model has
						// been created via XSLT for an outward bound conversion.
						// But in that case, this method isn't needed
						// because the developer started with the JAXB model. 
						Node foreign = cell.getContent(); // eg a <td>
						for (int n = 0 ; n < foreign.getChildNodes().getLength(); n++) {						
							Object o;
							try {
								o = XmlUtils.unmarshal(foreign.getChildNodes().item(n));
								tc.getEGBlockLevelElts().add(o);
							} catch (JAXBException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			
		}
		
		return tbl;
	}

	private void debugCellContents(NodeList children) {

		for (int i = 0; i < children.getLength(); i++) {

			log.debug(i);

			log.debug(children.item(i).getTextContent());
			log.debug(children.item(i).getLocalName());

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
	
	public static class TableModelTransformState implements TransformState {
		
		// The last table number, in document order,
		// which we have processed. 
		// The idea is to be able to write an id (unique within the document) to each
		// table.
		
		int idx = 0;

		/**
		 * @return the idx
		 */
		public int getIdx() {
			return idx;
		}

		/**
		 * @param idx the idx to set
		 */
		public void incrementIdx() {
			idx++;
		}
		
//		/**
//		 * @param idx the idx to set
//		 */
//		public void setIdx(int idx) {
//			this.idx = idx;
//		}
		
		
	}


}
