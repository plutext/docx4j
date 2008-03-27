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
 * <p>Java class for CT_ColorMappingOverride complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorMappingOverride">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="masterClrMapping" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EmptyElement"/>
 *           &lt;element name="overrideClrMapping" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMapping"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorMappingOverride", propOrder = {
    "masterClrMapping",
    "overrideClrMapping"
})
public class CTColorMappingOverride {

    protected CTEmptyElement masterClrMapping;
    protected CTColorMapping overrideClrMapping;

    /**
     * Gets the value of the masterClrMapping property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmptyElement }
     *     
     */
    public CTEmptyElement getMasterClrMapping() {
        return masterClrMapping;
    }

    /**
     * Sets the value of the masterClrMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmptyElement }
     *     
     */
    public void setMasterClrMapping(CTEmptyElement value) {
        this.masterClrMapping = value;
    }

    /**
     * Gets the value of the overrideClrMapping property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getOverrideClrMapping() {
        return overrideClrMapping;
    }

    /**
     * Sets the value of the overrideClrMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setOverrideClrMapping(CTColorMapping value) {
        this.overrideClrMapping = value;
    }

}
