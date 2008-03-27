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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_AdjustHandleList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AdjustHandleList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="ahXY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_XYAdjustHandle"/>
 *         &lt;element name="ahPolar" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PolarAdjustHandle"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AdjustHandleList", propOrder = {
    "ahXYOrAhPolar"
})
public class CTAdjustHandleList {

    @XmlElements({
        @XmlElement(name = "ahPolar", type = CTPolarAdjustHandle.class),
        @XmlElement(name = "ahXY", type = CTXYAdjustHandle.class)
    })
    protected List<Object> ahXYOrAhPolar;

    /**
     * Gets the value of the ahXYOrAhPolar property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ahXYOrAhPolar property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAhXYOrAhPolar().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPolarAdjustHandle }
     * {@link CTXYAdjustHandle }
     * 
     * 
     */
    public List<Object> getAhXYOrAhPolar() {
        if (ahXYOrAhPolar == null) {
            ahXYOrAhPolar = new ArrayList<Object>();
        }
        return this.ahXYOrAhPolar;
    }

}
