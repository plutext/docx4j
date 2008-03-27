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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TextSpacing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextSpacing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="spcPct" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacingPercent"/>
 *         &lt;element name="spcPts" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacingPoint"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextSpacing", propOrder = {
    "spcPct",
    "spcPts"
})
public class CTTextSpacing {

    protected CTTextSpacingPercent spcPct;
    protected CTTextSpacingPoint spcPts;

    /**
     * Gets the value of the spcPct property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacingPercent }
     *     
     */
    public CTTextSpacingPercent getSpcPct() {
        return spcPct;
    }

    /**
     * Sets the value of the spcPct property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacingPercent }
     *     
     */
    public void setSpcPct(CTTextSpacingPercent value) {
        this.spcPct = value;
    }

    /**
     * Gets the value of the spcPts property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacingPoint }
     *     
     */
    public CTTextSpacingPoint getSpcPts() {
        return spcPts;
    }

    /**
     * Sets the value of the spcPts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacingPoint }
     *     
     */
    public void setSpcPts(CTTextSpacingPoint value) {
        this.spcPts = value;
    }

}
