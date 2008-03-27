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
 * <p>Java class for CT_TextUnderlineFillGroupWrapper complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextUnderlineFillGroupWrapper">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillProperties"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextUnderlineFillGroupWrapper", propOrder = {
    "noFill",
    "solidFill",
    "gradFill",
    "blipFill",
    "pattFill",
    "grpFill"
})
public class CTTextUnderlineFillGroupWrapper {

    protected CTNoFillProperties noFill;
    protected CTSolidColorFillProperties solidFill;
    protected CTGradientFillProperties gradFill;
    protected CTBlipFillProperties blipFill;
    protected CTPatternFillProperties pattFill;
    protected CTGroupFillProperties grpFill;

    /**
     * Gets the value of the noFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTNoFillProperties }
     *     
     */
    public CTNoFillProperties getNoFill() {
        return noFill;
    }

    /**
     * Sets the value of the noFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNoFillProperties }
     *     
     */
    public void setNoFill(CTNoFillProperties value) {
        this.noFill = value;
    }

    /**
     * Gets the value of the solidFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getSolidFill() {
        return solidFill;
    }

    /**
     * Sets the value of the solidFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setSolidFill(CTSolidColorFillProperties value) {
        this.solidFill = value;
    }

    /**
     * Gets the value of the gradFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public CTGradientFillProperties getGradFill() {
        return gradFill;
    }

    /**
     * Sets the value of the gradFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public void setGradFill(CTGradientFillProperties value) {
        this.gradFill = value;
    }

    /**
     * Gets the value of the blipFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public CTBlipFillProperties getBlipFill() {
        return blipFill;
    }

    /**
     * Sets the value of the blipFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public void setBlipFill(CTBlipFillProperties value) {
        this.blipFill = value;
    }

    /**
     * Gets the value of the pattFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public CTPatternFillProperties getPattFill() {
        return pattFill;
    }

    /**
     * Sets the value of the pattFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public void setPattFill(CTPatternFillProperties value) {
        this.pattFill = value;
    }

    /**
     * Gets the value of the grpFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public CTGroupFillProperties getGrpFill() {
        return grpFill;
    }

    /**
     * Sets the value of the grpFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public void setGrpFill(CTGroupFillProperties value) {
        this.grpFill = value;
    }

}
