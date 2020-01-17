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
 * <p>Java class for CT_Col complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Col">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="min" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="max" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="bestFit" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="customWidth" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="phonetic" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="outlineLevel" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="collapsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Col")
@XmlRootElement(name = "col")
public class Col implements Child
{

    @XmlAttribute(name = "min", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long min;
    @XmlAttribute(name = "max", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long max;
    @XmlAttribute(name = "width")
    protected Double width;
    @XmlAttribute(name = "style")
    @XmlSchemaType(name = "unsignedInt")
    protected Long style;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlAttribute(name = "bestFit")
    protected Boolean bestFit;
    @XmlAttribute(name = "customWidth")
    protected Boolean customWidth;
    @XmlAttribute(name = "phonetic")
    protected Boolean phonetic;
    @XmlAttribute(name = "outlineLevel")
    @XmlSchemaType(name = "unsignedByte")
    protected Short outlineLevel;
    @XmlAttribute(name = "collapsed")
    protected Boolean collapsed;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the min property.
     * 
     */
    public long getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     */
    public void setMin(long value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     * 
     */
    public long getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(long value) {
        this.max = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWidth(Double value) {
        this.width = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getStyle() {
        if (style == null) {
            return  0L;
        } else {
            return style;
        }
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStyle(Long value) {
        this.style = value;
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
     * Gets the value of the bestFit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBestFit() {
        if (bestFit == null) {
            return false;
        } else {
            return bestFit;
        }
    }

    /**
     * Sets the value of the bestFit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBestFit(Boolean value) {
        this.bestFit = value;
    }

    /**
     * Gets the value of the customWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCustomWidth() {
        if (customWidth == null) {
            return false;
        } else {
            return customWidth;
        }
    }

    /**
     * Sets the value of the customWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomWidth(Boolean value) {
        this.customWidth = value;
    }

    /**
     * Gets the value of the phonetic property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPhonetic() {
        if (phonetic == null) {
            return false;
        } else {
            return phonetic;
        }
    }

    /**
     * Sets the value of the phonetic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPhonetic(Boolean value) {
        this.phonetic = value;
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
