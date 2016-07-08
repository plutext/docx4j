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

import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.finders.TcFinder;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTTrPrBase;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There are different ways to represent a table with possibly merged
 * cells. 
 * 
 * <ul>
 * <li>In html, both vertically and horizontally merged cells are
 * represented by one cell only that has a colspan and rowspan
 * attribute.  No dummy cells are used.
 * 
 * <li>In docx, horizontally merged cells are represented by one cell
 * with a gridSpan attribute; while vertically merged cells are
 * represented as a top cell containing the actual content and a series
 * of dummy cells having a vMerge tag with "continue" attribute.
 * 
 * <li>In the model used in this class, we use a regular matrix; 
 * dummy cells are added for both merge directions.
 * 
 * </ul>
 * 
 * The algorithm is as follows:
 * 
 * <ul>
 * <li>When a cell is added, its colspan is set.  Even a dummy cell can have
 * a colspan, the same value as its upper has.
 * 
 * <li>When a new cell has a colspan greater than 1, the required extra
 * dummy cells are also added
 * 
 * <li>When a docx dummy cell is encountered (one with a vMerge
 * continue attribute), the rowspan is incremented in its upper
 * neighbors until a real cell is found.
 * 
 * </ul>
 * 
 * This model captures:
 * - whether the table layout is fixed or auto (Word usually does auto)
 * - whether conflict resolution is required on cell borders (Word usually 
 *   does conflict resolution)
 * 
 *  @author Adam Schmideg
 *  @author Alberto Zerolo
 *  @author Jason Harrop
 * 
 */
public class TableModel {
	
	private final static Logger log = LoggerFactory.getLogger(TableModel.class);

	public TableModel() {
		resetIndexes();
		rows = new ArrayList<TableModelRow>();
		headerMaxRow = -1;
	}

	// TODO, retire this
	private final static int DEFAULT_PAGE_WIDTH_TWIPS = 12240;  // LETTER; A4 would be 11907
	
	/**
	 * A list of rows
	 */
	protected List<TableModelRow> rows;
	
	protected int headerMaxRow;
	
	protected int row;
	protected int col;
	protected int width = -1;
	
	protected boolean drawTableBorder = true;
	
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
	
	// We don't need this in our table model,
	// at least for HTML. (PropertyFactory takes care of it)
	
//	boolean tableLayoutFixed = false; // default to auto
//	/**
//	 * @return isTableLayoutFixed
//	 */
//	public boolean isTableLayoutFixed() {
//		return tableLayoutFixed;
//	}
	
	
	private boolean borderConflictResolutionRequired = true;
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
	
	/*
	 * @since 3.0.0
	 */
	public boolean isDrawTableBorders() {
		return drawTableBorder;
	}
	
	//Table width in twips, -1 = undefined
	public int getTableWidth() {
		return width;
	}
	
	/**
	 * Reset <var>row</var> and <var>col</var>.
	 */
	public void resetIndexes() {
		row = -1;
		col = -1;
	}

	public void startRow(Tr tr) {
		rows.add(new TableModelRow(tr));
		row++;
		col = -1;
	}

	/**
	 * Add a new cell to this table 
	 */
	public void addCell(Tc tc) {
		++col;
		log.debug("Add tc row " + row + " col " + col);
		addCell(new TableModelCell(this, row, col, tc));
		
		// add dummy horizontal cells
		if (tc.getTcPr()!=null 
				&& tc.getTcPr().getGridSpan()!=null 
				&& tc.getTcPr().getGridSpan().getVal() !=null) {
			
			int gridSpan = tc.getTcPr().getGridSpan().getVal().intValue();
			addDummyCell(gridSpan, false, false);
		}
		
		log.debug("\n\n"+ this.debugStr());
		
	}

//	private void addDummyCell() {
//		addDummyCell(0);
//	}

	protected void addDummyCell(int colSpan, boolean isBefore, boolean isAfter) {
		
		log.debug("gridSpan, so addDummyCell " + colSpan + " " + isBefore + " " + isAfter);
		TableModelCell cell; 
		if (colSpan>1) {
			for (int i=1; i<colSpan; i++) {
				cell = new TableModelCell(this, row, ++col);
				cell.dummyBefore=isBefore;
				cell.dummyAfter=isAfter;
				cell.setColspan(colSpan);
				addCell(cell);				
			}
		}
		
		
	}

	protected void addCell(TableModelCell cell) {
		log.debug("add cell, col " + col);
		rows.get(row).add(cell);
	}
	
	public TableModelCell getCell(int row, int col) {
		log.debug("getting row " + row + "  col " + col);
		return rows.get(row).get(col);
	}
	
	/**
	 * @return "colX" where X is a 1-based index
	 */
	public String getColName(int col) {
		return "col" + String.valueOf(col + 1);
	}

	public int getColCount() {
		return rows.get(0).size();
	}

	public List<TableModelRow> getRows() {
		return rows;
	}

    public int getHeaderMaxRow() {
    	return headerMaxRow;
    }
    
    protected Tbl tbl; // for debug purposes only
    
	public void build(Tbl tbl) {
		
		this.tbl = tbl;

		if (tbl.getTblPr()!=null
				&& tbl.getTblPr().getTblStyle()!=null) {
			styleId = tbl.getTblPr().getTblStyle().getVal();			
		}
		
		
		this.tblGrid = tbl.getTblGrid();
		
		this.tblPr = tbl.getTblPr();
		
//		PropertyResolver pr = conversionContext.getPropertyResolver();
			
//		effectiveTableStyle = pr.getEffectiveTableStyle(tbl.getTblPr() );
		
		
		TrFinder trFinder = new TrFinder();
		new TraversalUtil(tbl, trFinder);
		
		
		int r = 0;
		for (Tr tr : trFinder.getTrList()) {
			
				startRow(tr);
				handleRow(tr, r);
				r++;
				
				if (log.isDebugEnabled()) {
					log.debug("\n\n"+ this.debugStr());
				}
				
				if (rows.get(row).getRowContents().isEmpty()) {
					
					log.debug("removing empty row");
					
					rows.remove(row);
					row--;
					r--;
				}
		}
		
//		CTTblPrBase tblPr = effectiveTableStyle.getTblPr();
//		if (tblPr != null) {
//			if (tblPr.getTblCellSpacing()!=null) {
//				borderConflictResolutionRequired = false;							
//			}
//		}
		
		width = calcTableWidth();
	}

	
	public static class TrFinder extends CallbackImpl {
		
		private List<Tr> trList = new ArrayList<Tr>();  
				
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof Tr ) {
				
				getTrList().add((Tr)o);
			}			
			return null; 
		}
		
		@Override
		public boolean shouldTraverse(Object o) {
			
			// Yes, unless its a nested Tbl
			return !(o instanceof Tbl); 
		}

		public List<Tr> getTrList() {
			return trList;
		}

		public void setTrList(List<Tr> trList) {
			this.trList = trList;
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
	

	protected void handleRow(Tr tr, int r) {
		int gridAfter = getGridAfter(tr);
		int gridBefore = getGridBefore(tr);
		boolean headerRow = isHeaderRow(tr);

		log.debug("Processing r " + r);
		
		if (borderConflictResolutionRequired && tr.getTblPrEx() != null
				&& tr.getTblPrEx().getTblCellSpacing() != null) {
			borderConflictResolutionRequired = false;
		}
				
		if (headerRow && (headerMaxRow < r)) {
			headerMaxRow = r;
		}
		
		if (drawTableBorder) {
			drawTableBorder = (gridBefore == 0) && (gridAfter == 0);
		}
		
		TcFinder tcFinder = new TcFinder();
		new TraversalUtil(tr, tcFinder);
		
		//add dummy cell for gridBefore
		if (gridBefore > 0) {
			addDummyCell(gridBefore, true, false);
		}
		
		//List<Object> cells = tr.getEGContentCellContent();
		int c = 0;
		for (Tc tc : tcFinder.tcList) {
			log.debug("Processing  r " + r + ", docx cell " + c);
			
			addCell(tc); // the cell content
			// addCell(tc, cellContents.item(i));
			// i++;
			c++;
		}

		//add dummy cell for gridAfter
		if (gridAfter > 0) {
			addDummyCell(gridAfter, false, true);
		}
	}
	
	
	protected boolean isHeaderRow(Tr tr) {
		
		List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore = (tr.getTrPr() != null ? tr.getTrPr().getCnfStyleOrDivIdOrGridBefore() : null);
		JAXBElement element = getElement(cnfStyleOrDivIdOrGridBefore, "tblHeader");
		BooleanDefaultTrue boolVal = (element != null ? (BooleanDefaultTrue)element.getValue() : null);
		return (boolVal != null ? boolVal.isVal() : false);
	}
	
	protected int getGridAfter(Tr tr) {
		
		List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore = (tr.getTrPr() != null ? tr.getTrPr().getCnfStyleOrDivIdOrGridBefore() : null);
		JAXBElement element = getElement(cnfStyleOrDivIdOrGridBefore, "gridAfter");
		CTTrPrBase.GridAfter gridAfter = (element != null ? (CTTrPrBase.GridAfter)element.getValue() : null);
		BigInteger val = (gridAfter != null ? gridAfter.getVal() : null);
		return (val != null ? val.intValue() : 0);
	}
	
	protected int getGridBefore(Tr tr) {
		
		List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore = (tr.getTrPr() != null ? tr.getTrPr().getCnfStyleOrDivIdOrGridBefore() : null);
		JAXBElement element = getElement(cnfStyleOrDivIdOrGridBefore, "gridBefore");
		CTTrPrBase.GridBefore gridBefore = (element != null ? (CTTrPrBase.GridBefore)element.getValue() : null);
		BigInteger val = (gridBefore != null ? gridBefore.getVal() : null);
		return (val != null ? val.intValue() : 0);
	}
	
	protected JAXBElement<?> getElement(List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore, String localName) {
		JAXBElement<?> element = null;
		if ((cnfStyleOrDivIdOrGridBefore != null) && (!cnfStyleOrDivIdOrGridBefore.isEmpty())) {
			for (int i=0; i<cnfStyleOrDivIdOrGridBefore.size(); i++) {
				element = cnfStyleOrDivIdOrGridBefore.get(i);
				if (localName.equals(element.getName().getLocalPart())) {
					return element;
				}
			}
		}
		return null;
	}
	
	protected int calcTableWidth() {
		
		int ret = -1;
		List<TblGridCol> gridCols = (getTblGrid() != null ? getTblGrid().getGridCol() : null);
		//The calculation is done the way it was done in the TableWriter. This isn't necesarily correct,
	    //as cell-widths may override column widths.
    	if ((gridCols != null) && (!gridCols.isEmpty())) {
    		ret = 0;
	    	for(int i=0; i<gridCols.size(); i++) {  
	    		
	    		if (gridCols.get(i).getW()==null) {
	    			log.warn("Missing width " + XmlUtils.marshaltoString(getTblGrid()));
	    			continue;
	    		}
	    		
	    		ret += gridCols.get(i).getW().intValue();
	    	}
    	}
    	return ret;
	}

	public String debugStr() {
		StringBuffer buf = new StringBuffer();
		for (TableModelRow row : rows) {
			List<TableModelCell> rowContents = row.getRowContents();
			for (TableModelCell c : rowContents) {
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

	public void setBorderConflictResolutionRequired(
			boolean borderConflictResolutionRequired) {
		this.borderConflictResolutionRequired = borderConflictResolutionRequired;
	}

}
