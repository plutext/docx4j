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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PivotSelection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotSelection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pivotArea" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotArea"/>
 *       &lt;/sequence>
 *       &lt;attribute name="pane" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Pane" default="topLeft" />
 *       &lt;attribute name="showHeader" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="data" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="extendable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="axis" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Axis" />
 *       &lt;attribute name="dimension" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="min" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="max" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="activeRow" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="activeCol" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="previousRow" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="previousCol" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="click" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotSelection", propOrder = {
    "pivotArea"
})
public class CTPivotSelection implements Child
{

    @XmlElement(required = true)
    protected CTPivotArea pivotArea;
    @XmlAttribute(name = "pane")
    protected STPane pane;
    @XmlAttribute(name = "showHeader")
    protected Boolean showHeader;
    @XmlAttribute(name = "label")
    protected Boolean label;
    @XmlAttribute(name = "data")
    protected Boolean data;
    @XmlAttribute(name = "extendable")
    protected Boolean extendable;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;
    @XmlAttribute(name = "axis")
    protected STAxis axis;
    @XmlAttribute(name = "dimension")
    @XmlSchemaType(name = "unsignedInt")
    protected Long dimension;
    @XmlAttribute(name = "start")
    @XmlSchemaType(name = "unsignedInt")
    protected Long start;
    @XmlAttribute(name = "min")
    @XmlSchemaType(name = "unsignedInt")
    protected Long min;
    @XmlAttribute(name = "max")
    @XmlSchemaType(name = "unsignedInt")
    protected Long max;
    @XmlAttribute(name = "activeRow")
    @XmlSchemaType(name = "unsignedInt")
    protected Long activeRow;
    @XmlAttribute(name = "activeCol")
    @XmlSchemaType(name = "unsignedInt")
    protected Long activeCol;
    @XmlAttribute(name = "previousRow")
    @XmlSchemaType(name = "unsignedInt")
    protected Long previousRow;
    @XmlAttribute(name = "previousCol")
    @XmlSchemaType(name = "unsignedInt")
    protected Long previousCol;
    @XmlAttribute(name = "click")
    @XmlSchemaType(name = "unsignedInt")
    protected Long click;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pivotArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotArea }
     *     
     */
    public CTPivotArea getPivotArea() {
        return pivotArea;
    }

    /**
     * Sets the value of the pivotArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotArea }
     *     
     */
    public void setPivotArea(CTPivotArea value) {
        this.pivotArea = value;
    }

    /**
     * Gets the value of the pane property.
     * 
     * @return
     *     possible object is
     *     {@link STPane }
     *     
     */
    public STPane getPane() {
        if (pane == null) {
            return STPane.TOP_LEFT;
        } else {
            return pane;
        }
    }

    /**
     * Sets the value of the pane property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPane }
     *     
     */
    public void setPane(STPane value) {
        this.pane = value;
    }

    /**
     * Gets the value of the showHeader property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowHeader() {
        if (showHeader == null) {
            return false;
        } else {
            return showHeader;
        }
    }

    /**
     * Sets the value of the showHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowHeader(Boolean value) {
        this.showHeader = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLabel() {
        if (label == null) {
            return false;
        } else {
            return label;
        }
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLabel(Boolean value) {
        this.label = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isData() {
        if (data == null) {
            return false;
        } else {
            return data;
        }
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setData(Boolean value) {
        this.data = value;
    }

    /**
     * Gets the value of the extendable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isExtendable() {
        if (extendable == null) {
            return false;
        } else {
            return extendable;
        }
    }

    /**
     * Sets the value of the extendable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExtendable(Boolean value) {
        this.extendable = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCount() {
        if (count == null) {
            return  0L;
        } else {
            return count;
        }
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCount(Long value) {
        this.count = value;
    }

    /**
     * Gets the value of the axis property.
     * 
     * @return
     *     possible object is
     *     {@link STAxis }
     *     
     */
    public STAxis getAxis() {
        return axis;
    }

    /**
     * Sets the value of the axis property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAxis }
     *     
     */
    public void setAxis(STAxis value) {
        this.axis = value;
    }

    /**
     * Gets the value of the dimension property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getDimension() {
        if (dimension == null) {
            return  0L;
        } else {
            return dimension;
        }
    }

    /**
     * Sets the value of the dimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDimension(Long value) {
        this.dimension = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getStart() {
        if (start == null) {
            return  0L;
        } else {
            return start;
        }
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStart(Long value) {
        this.start = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getMin() {
        if (min == null) {
            return  0L;
        } else {
            return min;
        }
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMin(Long value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getMax() {
        if (max == null) {
            return  0L;
        } else {
            return max;
        }
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMax(Long value) {
        this.max = value;
    }

    /**
     * Gets the value of the activeRow property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getActiveRow() {
        if (activeRow == null) {
            return  0L;
        } else {
            return activeRow;
        }
    }

    /**
     * Sets the value of the activeRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setActiveRow(Long value) {
        this.activeRow = value;
    }

    /**
     * Gets the value of the activeCol property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getActiveCol() {
        if (activeCol == null) {
            return  0L;
        } else {
            return activeCol;
        }
    }

    /**
     * Sets the value of the activeCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setActiveCol(Long value) {
        this.activeCol = value;
    }

    /**
     * Gets the value of the previousRow property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPreviousRow() {
        if (previousRow == null) {
            return  0L;
        } else {
            return previousRow;
        }
    }

    /**
     * Sets the value of the previousRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPreviousRow(Long value) {
        this.previousRow = value;
    }

    /**
     * Gets the value of the previousCol property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPreviousCol() {
        if (previousCol == null) {
            return  0L;
        } else {
            return previousCol;
        }
    }

    /**
     * Sets the value of the previousCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPreviousCol(Long value) {
        this.previousCol = value;
    }

    /**
     * Gets the value of the click property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getClick() {
        if (click == null) {
            return  0L;
        } else {
            return click;
        }
    }

    /**
     * Sets the value of the click property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setClick(Long value) {
        this.click = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
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
