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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Columns complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Columns">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="col" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Column" maxOccurs="45"/>
 *       &lt;/sequence>
 *       &lt;attribute name="equalWidth" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="space" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="num" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="sep" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Columns", propOrder = {
    "col"
})
public class CTColumns implements Child
{

    protected List<CTColumn> col = new ArrayListWml<CTColumn>(this);
    @XmlAttribute(name = "equalWidth", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean equalWidth;
    @XmlAttribute(name = "space", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger space;
    @XmlAttribute(name = "num", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger num;
    @XmlAttribute(name = "sep", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean sep;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the col property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the col property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCol().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTColumn }
     * 
     * 
     */
    public List<CTColumn> getCol() {
        if (col == null) {
            col = new ArrayListWml<CTColumn>(this);
        }
        return this.col;
    }

    /**
     * Gets the value of the equalWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEqualWidth() {
        if (equalWidth == null) {
            return true;
        } else {
            return equalWidth;
        }
    }

    /**
     * Sets the value of the equalWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEqualWidth(Boolean value) {
        this.equalWidth = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSpace(BigInteger value) {
        this.space = value;
    }

    /**
     * Gets the value of the num property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNum() {
        return num;
    }

    /**
     * Sets the value of the num property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNum(BigInteger value) {
        this.num = value;
    }

    /**
     * Gets the value of the sep property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSep() {
        if (sep == null) {
            return true;
        } else {
            return sep;
        }
    }

    /**
     * Sets the value of the sep property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSep(Boolean value) {
        this.sep = value;
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
