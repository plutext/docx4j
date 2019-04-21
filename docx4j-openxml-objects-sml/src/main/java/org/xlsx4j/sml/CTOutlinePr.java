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
 * <p>Java class for CT_OutlinePr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OutlinePr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="applyStyles" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="summaryBelow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="summaryRight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showOutlineSymbols" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OutlinePr")
public class CTOutlinePr implements Child
{

    @XmlAttribute(name = "applyStyles")
    protected Boolean applyStyles;
    @XmlAttribute(name = "summaryBelow")
    protected Boolean summaryBelow;
    @XmlAttribute(name = "summaryRight")
    protected Boolean summaryRight;
    @XmlAttribute(name = "showOutlineSymbols")
    protected Boolean showOutlineSymbols;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the applyStyles property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isApplyStyles() {
        if (applyStyles == null) {
            return false;
        } else {
            return applyStyles;
        }
    }

    /**
     * Sets the value of the applyStyles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyStyles(Boolean value) {
        this.applyStyles = value;
    }

    /**
     * Gets the value of the summaryBelow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSummaryBelow() {
        if (summaryBelow == null) {
            return true;
        } else {
            return summaryBelow;
        }
    }

    /**
     * Sets the value of the summaryBelow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSummaryBelow(Boolean value) {
        this.summaryBelow = value;
    }

    /**
     * Gets the value of the summaryRight property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSummaryRight() {
        if (summaryRight == null) {
            return true;
        } else {
            return summaryRight;
        }
    }

    /**
     * Sets the value of the summaryRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSummaryRight(Boolean value) {
        this.summaryRight = value;
    }

    /**
     * Gets the value of the showOutlineSymbols property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowOutlineSymbols() {
        if (showOutlineSymbols == null) {
            return true;
        } else {
            return showOutlineSymbols;
        }
    }

    /**
     * Sets the value of the showOutlineSymbols property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowOutlineSymbols(Boolean value) {
        this.showOutlineSymbols = value;
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
