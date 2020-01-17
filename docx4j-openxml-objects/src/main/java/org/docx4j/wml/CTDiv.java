/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Div complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Div">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="blockQuote" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bodyDiv" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="marLeft" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SignedTwipsMeasure"/>
 *         &lt;element name="marRight" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SignedTwipsMeasure"/>
 *         &lt;element name="marTop" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SignedTwipsMeasure"/>
 *         &lt;element name="marBottom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SignedTwipsMeasure"/>
 *         &lt;element name="divBdr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_DivBdr" minOccurs="0"/>
 *         &lt;element name="divsChild" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Divs" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Div", propOrder = {
    "blockQuote",
    "bodyDiv",
    "marLeft",
    "marRight",
    "marTop",
    "marBottom",
    "divBdr",
    "divsChild"
})
public class CTDiv implements Child
{

    protected BooleanDefaultTrue blockQuote;
    protected BooleanDefaultTrue bodyDiv;
    @XmlElement(required = true)
    protected CTSignedTwipsMeasure marLeft;
    @XmlElement(required = true)
    protected CTSignedTwipsMeasure marRight;
    @XmlElement(required = true)
    protected CTSignedTwipsMeasure marTop;
    @XmlElement(required = true)
    protected CTSignedTwipsMeasure marBottom;
    protected CTDivBdr divBdr;
    protected List<CTDivs> divsChild = new ArrayListWml<CTDivs>(this);
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected BigInteger id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blockQuote property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBlockQuote() {
        return blockQuote;
    }

    /**
     * Sets the value of the blockQuote property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBlockQuote(BooleanDefaultTrue value) {
        this.blockQuote = value;
    }

    /**
     * Gets the value of the bodyDiv property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBodyDiv() {
        return bodyDiv;
    }

    /**
     * Sets the value of the bodyDiv property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBodyDiv(BooleanDefaultTrue value) {
        this.bodyDiv = value;
    }

    /**
     * Gets the value of the marLeft property.
     * 
     * @return
     *     possible object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public CTSignedTwipsMeasure getMarLeft() {
        return marLeft;
    }

    /**
     * Sets the value of the marLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public void setMarLeft(CTSignedTwipsMeasure value) {
        this.marLeft = value;
    }

    /**
     * Gets the value of the marRight property.
     * 
     * @return
     *     possible object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public CTSignedTwipsMeasure getMarRight() {
        return marRight;
    }

    /**
     * Sets the value of the marRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public void setMarRight(CTSignedTwipsMeasure value) {
        this.marRight = value;
    }

    /**
     * Gets the value of the marTop property.
     * 
     * @return
     *     possible object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public CTSignedTwipsMeasure getMarTop() {
        return marTop;
    }

    /**
     * Sets the value of the marTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public void setMarTop(CTSignedTwipsMeasure value) {
        this.marTop = value;
    }

    /**
     * Gets the value of the marBottom property.
     * 
     * @return
     *     possible object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public CTSignedTwipsMeasure getMarBottom() {
        return marBottom;
    }

    /**
     * Sets the value of the marBottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSignedTwipsMeasure }
     *     
     */
    public void setMarBottom(CTSignedTwipsMeasure value) {
        this.marBottom = value;
    }

    /**
     * Gets the value of the divBdr property.
     * 
     * @return
     *     possible object is
     *     {@link CTDivBdr }
     *     
     */
    public CTDivBdr getDivBdr() {
        return divBdr;
    }

    /**
     * Sets the value of the divBdr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDivBdr }
     *     
     */
    public void setDivBdr(CTDivBdr value) {
        this.divBdr = value;
    }

    /**
     * Gets the value of the divsChild property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the divsChild property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDivsChild().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDivs }
     * 
     * 
     */
    public List<CTDivs> getDivsChild() {
        if (divsChild == null) {
            divsChild = new ArrayListWml<CTDivs>(this);
        }
        return this.divsChild;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setId(BigInteger value) {
        this.id = value;
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
