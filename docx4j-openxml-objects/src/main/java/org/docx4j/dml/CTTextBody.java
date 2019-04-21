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


package org.docx4j.dml;

import org.docx4j.dml.ArrayListDml;
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
    protected List<CTTextParagraph> p = new ArrayListDml<CTTextParagraph>(this);

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
            p = new ArrayListDml<CTTextParagraph>(this);
        }
        return this.p;
    }

}
