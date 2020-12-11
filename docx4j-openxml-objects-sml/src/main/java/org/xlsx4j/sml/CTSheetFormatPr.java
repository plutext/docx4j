/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SheetFormatPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SheetFormatPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="baseColWidth" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="8" />
 *       &lt;attribute name="defaultColWidth" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="defaultRowHeight" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="customHeight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="zeroHeight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="thickTop" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="thickBottom" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="outlineLevelRow" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="outlineLevelCol" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SheetFormatPr")
@XmlRootElement(name = "sheetFormatPr")
public class CTSheetFormatPr implements Child
{

    @XmlAttribute(name = "baseColWidth")
    @XmlSchemaType(name = "unsignedInt")
    protected Long baseColWidth;
    @XmlAttribute(name = "defaultColWidth")
    protected Double defaultColWidth;
    @XmlAttribute(name = "defaultRowHeight", required = true)
    protected double defaultRowHeight;
    @XmlAttribute(name = "customHeight")
    protected Boolean customHeight;
    @XmlAttribute(name = "zeroHeight")
    protected Boolean zeroHeight;
    @XmlAttribute(name = "thickTop")
    protected Boolean thickTop;
    @XmlAttribute(name = "thickBottom")
    protected Boolean thickBottom;
    @XmlAttribute(name = "outlineLevelRow")
    @XmlSchemaType(name = "unsignedByte")
    protected Short outlineLevelRow;
    @XmlAttribute(name = "outlineLevelCol")
    @XmlSchemaType(name = "unsignedByte")
    protected Short outlineLevelCol;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the baseColWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getBaseColWidth() {
        if (baseColWidth == null) {
            return  8L;
        } else {
            return baseColWidth;
        }
    }

    /**
     * Sets the value of the baseColWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBaseColWidth(Long value) {
        this.baseColWidth = value;
    }

    /**
     * Gets the value of the defaultColWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getDefaultColWidth() {
        return defaultColWidth;
    }

    /**
     * Sets the value of the defaultColWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setDefaultColWidth(Double value) {
        this.defaultColWidth = value;
    }

    /**
     * Gets the value of the defaultRowHeight property.
     * 
     */
    public double getDefaultRowHeight() {
        return defaultRowHeight;
    }

    /**
     * Sets the value of the defaultRowHeight property.
     * 
     */
    public void setDefaultRowHeight(double value) {
        this.defaultRowHeight = value;
    }

    /**
     * Gets the value of the customHeight property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCustomHeight() {
        if (customHeight == null) {
            return false;
        } else {
            return customHeight;
        }
    }

    /**
     * Sets the value of the customHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomHeight(Boolean value) {
        this.customHeight = value;
    }

    /**
     * Gets the value of the zeroHeight property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isZeroHeight() {
        if (zeroHeight == null) {
            return false;
        } else {
            return zeroHeight;
        }
    }

    /**
     * Sets the value of the zeroHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setZeroHeight(Boolean value) {
        this.zeroHeight = value;
    }

    /**
     * Gets the value of the thickTop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isThickTop() {
        if (thickTop == null) {
            return false;
        } else {
            return thickTop;
        }
    }

    /**
     * Sets the value of the thickTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setThickTop(Boolean value) {
        this.thickTop = value;
    }

    /**
     * Gets the value of the thickBottom property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isThickBottom() {
        if (thickBottom == null) {
            return false;
        } else {
            return thickBottom;
        }
    }

    /**
     * Sets the value of the thickBottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setThickBottom(Boolean value) {
        this.thickBottom = value;
    }

    /**
     * Gets the value of the outlineLevelRow property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getOutlineLevelRow() {
        if (outlineLevelRow == null) {
            return ((short) 0);
        } else {
            return outlineLevelRow;
        }
    }

    /**
     * Sets the value of the outlineLevelRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOutlineLevelRow(Short value) {
        this.outlineLevelRow = value;
    }

    /**
     * Gets the value of the outlineLevelCol property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getOutlineLevelCol() {
        if (outlineLevelCol == null) {
            return ((short) 0);
        } else {
            return outlineLevelCol;
        }
    }

    /**
     * Sets the value of the outlineLevelCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOutlineLevelCol(Short value) {
        this.outlineLevelCol = value;
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
