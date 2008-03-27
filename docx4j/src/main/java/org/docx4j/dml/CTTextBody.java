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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TextBody complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextBody">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bodyPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBodyProperties"/>
 *         &lt;element name="lstStyle" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextListStyle" minOccurs="0"/>
 *         &lt;element name="p" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextParagraph" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextBody", propOrder = {
    "bodyPr",
    "lstStyle",
    "p"
})
public class CTTextBody {

    @XmlElement(required = true)
    protected CTTextBodyProperties bodyPr;
    protected CTTextListStyle lstStyle;
    @XmlElement(required = true)
    protected List<CTTextParagraph> p;

    /**
     * Gets the value of the bodyPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBodyProperties }
     *     
     */
    public CTTextBodyProperties getBodyPr() {
        return bodyPr;
    }

    /**
     * Sets the value of the bodyPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBodyProperties }
     *     
     */
    public void setBodyPr(CTTextBodyProperties value) {
        this.bodyPr = value;
    }

    /**
     * Gets the value of the lstStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextListStyle }
     *     
     */
    public CTTextListStyle getLstStyle() {
        return lstStyle;
    }

    /**
     * Sets the value of the lstStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextListStyle }
     *     
     */
    public void setLstStyle(CTTextListStyle value) {
        this.lstStyle = value;
    }

    /**
     * Gets the value of the p property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the p property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTextParagraph }
     * 
     * 
     */
    public List<CTTextParagraph> getP() {
        if (p == null) {
            p = new ArrayList<CTTextParagraph>();
        }
        return this.p;
    }

}
