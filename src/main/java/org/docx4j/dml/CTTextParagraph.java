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
 * <p>Java class for CT_TextParagraph complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextParagraph">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextParagraphProperties" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextRun" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="endParaRPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextCharacterProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextParagraph", propOrder = {
    "pPr",
    "egTextRun",
    "endParaRPr"
})
public class CTTextParagraph {

    protected CTTextParagraphProperties pPr;
    @XmlElements({
        @XmlElement(name = "br", type = CTTextLineBreak.class),
        @XmlElement(name = "fld", type = CTTextField.class),
        @XmlElement(name = "r", type = CTRegularTextRun.class)
    })
    protected List<Object> egTextRun;
    protected CTTextCharacterProperties endParaRPr;

    /**
     * Gets the value of the pPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextParagraphProperties }
     *     
     */
    public CTTextParagraphProperties getPPr() {
        return pPr;
    }

    /**
     * Sets the value of the pPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextParagraphProperties }
     *     
     */
    public void setPPr(CTTextParagraphProperties value) {
        this.pPr = value;
    }

    /**
     * Gets the value of the egTextRun property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egTextRun property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGTextRun().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTextLineBreak }
     * {@link CTTextField }
     * {@link CTRegularTextRun }
     * 
     * 
     */
    public List<Object> getEGTextRun() {
        if (egTextRun == null) {
            egTextRun = new ArrayList<Object>();
        }
        return this.egTextRun;
    }

    /**
     * Gets the value of the endParaRPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextCharacterProperties }
     *     
     */
    public CTTextCharacterProperties getEndParaRPr() {
        return endParaRPr;
    }

    /**
     * Sets the value of the endParaRPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextCharacterProperties }
     *     
     */
    public void setEndParaRPr(CTTextCharacterProperties value) {
        this.endParaRPr = value;
    }

}
