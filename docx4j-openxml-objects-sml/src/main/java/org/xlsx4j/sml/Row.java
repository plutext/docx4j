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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

// import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Row complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Row">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="c" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cell" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="r" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="spans" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellSpans" />
 *       &lt;attribute name="s" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="customFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ht" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="customHeight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="outlineLevel" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="collapsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="thickTop" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="thickBot" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ph" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Row", propOrder = {
    "c",
    "extLst"
})
@XmlRootElement(name = "row")
public class Row implements Child
{

    protected List<Cell> c = new ArrayListSml<Cell>(this);
    protected CTExtensionList extLst;
    @XmlAttribute(name = "r")
    @XmlSchemaType(name = "unsignedInt")
    protected Long r;
    @XmlAttribute(name = "spans")
    protected List<String> spans;
    @XmlAttribute(name = "s")
    @XmlSchemaType(name = "unsignedInt")
    protected Long s;
    @XmlAttribute(name = "customFormat")
    protected Boolean customFormat;
    @XmlAttribute(name = "ht")
    protected Double ht;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlAttribute(name = "customHeight")
    protected Boolean customHeight;
    @XmlAttribute(name = "outlineLevel")
    @XmlSchemaType(name = "unsignedByte")
    protected Short outlineLevel;
    @XmlAttribute(name = "collapsed")
    protected Boolean collapsed;
    @XmlAttribute(name = "thickTop")
    protected Boolean thickTop;
    @XmlAttribute(name = "thickBot")
    protected Boolean thickBot;
    @XmlAttribute(name = "ph")
    protected Boolean ph;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the c property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the c property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getC().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Cell }
     * 
     * 
     */
    public List<Cell> getC() {
        if (c == null) {
            c = new ArrayListSml<Cell>(this);
        }
        return this.c;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the r property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setR(Long value) {
        this.r = value;
    }

    /**
     * Gets the value of the spans property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spans property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpans().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSpans() {
        if (spans == null) {
            spans = new ArrayList<String>();
        }
        return this.spans;
    }

    /**
     * Gets the value of the s property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getS() {
        if (s == null) {
            return  0L;
        } else {
            return s;
        }
    }

    /**
     * Sets the value of the s property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setS(Long value) {
        this.s = value;
    }

    /**
     * Gets the value of the customFormat property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCustomFormat() {
        if (customFormat == null) {
            return false;
        } else {
            return customFormat;
        }
    }

    /**
     * Sets the value of the customFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomFormat(Boolean value) {
        this.customFormat = value;
    }

    /**
     * Gets the value of the ht property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHt() {
        return ht;
    }

    /**
     * Sets the value of the ht property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setHt(Double value) {
        this.ht = value;
    }

    /**
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
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
     * Gets the value of the outlineLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getOutlineLevel() {
        if (outlineLevel == null) {
            return ((short) 0);
        } else {
            return outlineLevel;
        }
    }

    /**
     * Sets the value of the outlineLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOutlineLevel(Short value) {
        this.outlineLevel = value;
    }

    /**
     * Gets the value of the collapsed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCollapsed() {
        if (collapsed == null) {
            return false;
        } else {
            return collapsed;
        }
    }

    /**
     * Sets the value of the collapsed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCollapsed(Boolean value) {
        this.collapsed = value;
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
     * Gets the value of the thickBot property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isThickBot() {
        if (thickBot == null) {
            return false;
        } else {
            return thickBot;
        }
    }

    /**
     * Sets the value of the thickBot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setThickBot(Boolean value) {
        this.thickBot = value;
    }

    /**
     * Gets the value of the ph property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPh() {
        if (ph == null) {
            return false;
        } else {
            return ph;
        }
    }

    /**
     * Sets the value of the ph property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPh(Boolean value) {
        this.ph = value;
    }

//     /**
//      * Get the WorksheetPart
//      * @return
//      * @since 3.3.3
//      */
//     public WorksheetPart getWorksheetPart() {
//     	return ((SheetData)getParent()).getWorksheetPart();
//     }
    
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
