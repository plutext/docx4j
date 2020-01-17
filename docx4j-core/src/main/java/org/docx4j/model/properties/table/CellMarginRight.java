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
package org.docx4j.model.properties.table;

import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.TcPrInner;
import org.w3c.dom.css.CSSValue;

/*
 *  @author Alberto Zerolo
 *  @since 3.0.0
 */
public class CellMarginRight extends AbstractCellMargin {

	protected static final String SUFFIX = "right";
	
	public CellMarginRight(TblWidth tblWidth) {
		super(tblWidth, SUFFIX);
	}

	public CellMarginRight(CSSValue value) {
		super(value, SUFFIX);
	}

	@Override
	public void set(TcPrInner tcPr) {
		ensureMargin(tcPr);
		tcPr.getTcMar().setRight((TblWidth)getObject());
	}

	@Override
	public void set(TblPr tblPr) {
		ensureMargin(tblPr);
		tblPr.getTblCellMar().setRight((TblWidth)getObject());
	}

}
