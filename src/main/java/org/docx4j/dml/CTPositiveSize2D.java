/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PositiveSize2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PositiveSize2D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="cx" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" />
 *       &lt;attribute name="cy" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PositiveSize2D")
public class CTPositiveSize2D {

    @XmlAttribute(required = true)
    protected long cx;
    @XmlAttribute(required = true)
    protected long cy;

    /**
     * Gets the value of the cx property.
     * 
     */
    public long getCx() {
        return cx;
    }

    /**
     * Sets the value of the cx property.
     * 
     */
    public void setCx(long value) {
        this.cx = value;
    }

    /**
     * Gets the value of the cy property.
     * 
     */
    public long getCy() {
        return cy;
    }

    /**
     * Sets the value of the cy property.
     * 
     */
    public void setCy(long value) {
        this.cy = value;
    }

}
