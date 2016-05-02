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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.sharedtypes.STOnOff;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TblLook complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TblLook">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="firstRow" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_OnOff" />
 *       &lt;attribute name="lastRow" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_OnOff" />
 *       &lt;attribute name="firstColumn" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_OnOff" />
 *       &lt;attribute name="lastColumn" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_OnOff" />
 *       &lt;attribute name="noHBand" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_OnOff" />
 *       &lt;attribute name="noVBand" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_OnOff" />
 *       &lt;attribute name="val" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ShortHexNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TblLook")
public class CTTblLook implements Child
{

    @XmlAttribute(name = "firstRow", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STOnOff firstRow;
    @XmlAttribute(name = "lastRow", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STOnOff lastRow;
    @XmlAttribute(name = "firstColumn", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STOnOff firstColumn;
    @XmlAttribute(name = "lastColumn", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STOnOff lastColumn;
    @XmlAttribute(name = "noHBand", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STOnOff noHBand;
    @XmlAttribute(name = "noVBand", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STOnOff noVBand;
    @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String val;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the firstRow property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOff }
     *     
     */
    public STOnOff getFirstRow() {
        return firstRow;
    }

    /**
     * Sets the value of the firstRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOff }
     *     
     */
    public void setFirstRow(STOnOff value) {
        this.firstRow = value;
    }

    /**
     * Gets the value of the lastRow property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOff }
     *     
     */
    public STOnOff getLastRow() {
        return lastRow;
    }

    /**
     * Sets the value of the lastRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOff }
     *     
     */
    public void setLastRow(STOnOff value) {
        this.lastRow = value;
    }

    /**
     * Gets the value of the firstColumn property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOff }
     *     
     */
    public STOnOff getFirstColumn() {
        return firstColumn;
    }

    /**
     * Sets the value of the firstColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOff }
     *     
     */
    public void setFirstColumn(STOnOff value) {
        this.firstColumn = value;
    }

    /**
     * Gets the value of the lastColumn property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOff }
     *     
     */
    public STOnOff getLastColumn() {
        return lastColumn;
    }

    /**
     * Sets the value of the lastColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOff }
     *     
     */
    public void setLastColumn(STOnOff value) {
        this.lastColumn = value;
    }

    /**
     * Gets the value of the noHBand property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOff }
     *     
     */
    public STOnOff getNoHBand() {
        return noHBand;
    }

    /**
     * Sets the value of the noHBand property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOff }
     *     
     */
    public void setNoHBand(STOnOff value) {
        this.noHBand = value;
    }

    /**
     * Gets the value of the noVBand property.
     * 
     * @return
     *     possible object is
     *     {@link STOnOff }
     *     
     */
    public STOnOff getNoVBand() {
        return noVBand;
    }

    /**
     * Sets the value of the noVBand property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOnOff }
     *     
     */
    public void setNoVBand(STOnOff value) {
        this.noVBand = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVal(String value) {
        this.val = value;
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
