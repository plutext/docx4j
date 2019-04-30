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


package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Marker complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Marker"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="col" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}ST_ColID"/&gt;
 *         &lt;element name="colOff" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate"/&gt;
 *         &lt;element name="row" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}ST_RowID"/&gt;
 *         &lt;element name="rowOff" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Marker", propOrder = {
    "col",
    "colOff",
    "row",
    "rowOff"
})
public class CTMarker implements Child
{

    protected int col;
    protected long colOff;
    protected int row;
    protected long rowOff;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the col property.
     * 
     */
    public int getCol() {
        return col;
    }

    /**
     * Sets the value of the col property.
     * 
     */
    public void setCol(int value) {
        this.col = value;
    }

    /**
     * Gets the value of the colOff property.
     * 
     */
    public long getColOff() {
        return colOff;
    }

    /**
     * Sets the value of the colOff property.
     * 
     */
    public void setColOff(long value) {
        this.colOff = value;
    }

    /**
     * Gets the value of the row property.
     * 
     */
    public int getRow() {
        return row;
    }

    /**
     * Sets the value of the row property.
     * 
     */
    public void setRow(int value) {
        this.row = value;
    }

    /**
     * Gets the value of the rowOff property.
     * 
     */
    public long getRowOff() {
        return rowOff;
    }

    /**
     * Sets the value of the rowOff property.
     * 
     */
    public void setRowOff(long value) {
        this.rowOff = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
