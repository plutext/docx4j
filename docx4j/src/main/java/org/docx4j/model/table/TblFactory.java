/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
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

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;

/**
 * This class is just a convenience; it creates regular tables 
 * (ie no row or column spans), with the default settings
 * which Word 2007 would create, and with the dimensions
 * specified by the user.
 * 
 * @author jharrop
 *
 */
public class TblFactory {
	
	
	public static Tbl createTable(int rows, int cols, int cellWidthTwips) {
		
		Tbl tbl = Context.getWmlObjectFactory().createTbl();		
		
		// w:tblPr
		String strTblPr =  "<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + ">"
			+ "<w:tblStyle w:val=\"TableGrid\"/>"
			+ 	"<w:tblW w:w=\"0\" w:type=\"auto\"/>"
			+   "<w:tblLook w:val=\"04A0\"/>"
			+ "</w:tblPr>";
		TblPr tblPr = null;
		try {
			tblPr = (TblPr)XmlUtils.unmarshalString(strTblPr);
		} catch (JAXBException e) {
			// Shouldn't happen 
			e.printStackTrace();
		}
		tbl.setTblPr(tblPr);
		
		// <w:tblGrid><w:gridCol w:w="4788"/>		
		TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid();
		tbl.setTblGrid(tblGrid);
		// Add required <w:gridCol w:w="4788"/>
		for (int i=1 ; i<=cols; i++) {
			TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol();
			gridCol.setW(BigInteger.valueOf(cellWidthTwips));
			tblGrid.getGridCol().add(gridCol);
		}
				
		// Now the rows
		for (int j=1 ; j<=rows; j++) {
			Tr tr = Context.getWmlObjectFactory().createTr();
			tbl.getEGContentRowContent().add(tr);
			
			// The cells
			for (int i=1 ; i<=cols; i++) {
				
				Tc tc = Context.getWmlObjectFactory().createTc();
				tr.getEGContentCellContent().add(tc);
				
				TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
				tc.setTcPr(tcPr);
				// <w:tcW w:w="4788" w:type="dxa"/>
				TblWidth cellWidth = Context.getWmlObjectFactory().createTblWidth();
				tcPr.setTcW(cellWidth);
				cellWidth.setType("dxa");
				cellWidth.setW(BigInteger.valueOf(cellWidthTwips));
				
				// Cell content - an empty <w:p/>
				tc.getEGBlockLevelElts().add(
						Context.getWmlObjectFactory().createP()
						);
			}
			
		}
		return tbl;
	}

}
