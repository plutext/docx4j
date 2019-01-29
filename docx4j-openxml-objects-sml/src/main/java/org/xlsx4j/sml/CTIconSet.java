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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_IconSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_IconSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cfvo" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cfvo" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *       &lt;attribute name="iconSet" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_IconSetType" default="3TrafficLights1" />
 *       &lt;attribute name="showValue" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="percent" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="reverse" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_IconSet", propOrder = {
    "cfvo"
})
public class CTIconSet implements Child
{

    @XmlElement(required = true)
    protected List<CTCfvo> cfvo;
    @XmlAttribute(name = "iconSet")
    protected String iconSet;
    @XmlAttribute(name = "showValue")
    protected Boolean showValue;
    @XmlAttribute(name = "percent")
    protected Boolean percent;
    @XmlAttribute(name = "reverse")
    protected Boolean reverse;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cfvo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cfvo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCfvo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCfvo }
     * 
     * 
     */
    public List<CTCfvo> getCfvo() {
        if (cfvo == null) {
            cfvo = new ArrayList<CTCfvo>();
        }
        return this.cfvo;
    }

    /**
     * Gets the value of the iconSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIconSet() {
        if (iconSet == null) {
            return "3TrafficLights1";
        } else {
            return iconSet;
        }
    }

    /**
     * Sets the value of the iconSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIconSet(String value) {
        this.iconSet = value;
    }

    /**
     * Gets the value of the showValue property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowValue() {
        if (showValue == null) {
            return true;
        } else {
            return showValue;
        }
    }

    /**
     * Sets the value of the showValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowValue(Boolean value) {
        this.showValue = value;
    }

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPercent() {
        if (percent == null) {
            return true;
        } else {
            return percent;
        }
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPercent(Boolean value) {
        this.percent = value;
    }

    /**
     * Gets the value of the reverse property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isReverse() {
        if (reverse == null) {
            return false;
        } else {
            return reverse;
        }
    }

    /**
     * Sets the value of the reverse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReverse(Boolean value) {
        this.reverse = value;
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
