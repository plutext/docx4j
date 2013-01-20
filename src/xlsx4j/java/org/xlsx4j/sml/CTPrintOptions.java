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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PrintOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PrintOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="horizontalCentered" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="verticalCentered" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="headings" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="gridLines" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="gridLinesSet" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PrintOptions")
public class CTPrintOptions implements Child
{

    @XmlAttribute(name = "horizontalCentered")
    protected Boolean horizontalCentered;
    @XmlAttribute(name = "verticalCentered")
    protected Boolean verticalCentered;
    @XmlAttribute(name = "headings")
    protected Boolean headings;
    @XmlAttribute(name = "gridLines")
    protected Boolean gridLines;
    @XmlAttribute(name = "gridLinesSet")
    protected Boolean gridLinesSet;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the horizontalCentered property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHorizontalCentered() {
        if (horizontalCentered == null) {
            return false;
        } else {
            return horizontalCentered;
        }
    }

    /**
     * Sets the value of the horizontalCentered property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHorizontalCentered(Boolean value) {
        this.horizontalCentered = value;
    }

    /**
     * Gets the value of the verticalCentered property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVerticalCentered() {
        if (verticalCentered == null) {
            return false;
        } else {
            return verticalCentered;
        }
    }

    /**
     * Sets the value of the verticalCentered property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVerticalCentered(Boolean value) {
        this.verticalCentered = value;
    }

    /**
     * Gets the value of the headings property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHeadings() {
        if (headings == null) {
            return false;
        } else {
            return headings;
        }
    }

    /**
     * Sets the value of the headings property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeadings(Boolean value) {
        this.headings = value;
    }

    /**
     * Gets the value of the gridLines property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGridLines() {
        if (gridLines == null) {
            return false;
        } else {
            return gridLines;
        }
    }

    /**
     * Sets the value of the gridLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGridLines(Boolean value) {
        this.gridLines = value;
    }

    /**
     * Gets the value of the gridLinesSet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGridLinesSet() {
        if (gridLinesSet == null) {
            return true;
        } else {
            return gridLinesSet;
        }
    }

    /**
     * Sets the value of the gridLinesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGridLinesSet(Boolean value) {
        this.gridLinesSet = value;
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
