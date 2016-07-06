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

package org.docx4j.convert.out.common.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.model.table.TableModelCell;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.w3c.dom.Node;

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
	/**
 * A cell in the table holding its own content, too
 */
public class AbstractTableWriterModelCell extends TableModelCell {
	
	private final static Logger logger = LoggerFactory.getLogger(AbstractTableWriterModelCell.class);
	
	
	protected Node content = null;
	

	public AbstractTableWriterModelCell(AbstractTableWriterModel table, int row, int col, Tc tc, Node content) {
		super(table, row, col, tc);
		this.content = content;
		
		if (content==null) {
            if(logger.isErrorEnabled()) {
                logger.error("No content for row " + row + ", col " + col + "\n"
                        + XmlUtils.marshaltoString(tc, true, true));
            }
		} else {
            if(logger.isDebugEnabled()) {
                logger.debug("Cell content for row " + row + ", col " + col + "\n" + XmlUtils.w3CDomNodeToString(content));
            }
		}

		/* xhtmlTc.appendChild(
		   document.importNode(tcDoc, true) );
		   com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.importNode
		   org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation
		 */
	}


	public Node getContent() {
		return content;
	}

}
