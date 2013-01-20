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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Consolidation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Consolidation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pages" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Pages" minOccurs="0"/>
 *         &lt;element name="rangeSets" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RangeSets"/>
 *       &lt;/sequence>
 *       &lt;attribute name="autoPage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Consolidation", propOrder = {
    "pages",
    "rangeSets"
})
public class CTConsolidation implements Child
{

    protected CTPages pages;
    @XmlElement(required = true)
    protected CTRangeSets rangeSets;
    @XmlAttribute(name = "autoPage")
    protected Boolean autoPage;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pages property.
     * 
     * @return
     *     possible object is
     *     {@link CTPages }
     *     
     */
    public CTPages getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPages }
     *     
     */
    public void setPages(CTPages value) {
        this.pages = value;
    }

    /**
     * Gets the value of the rangeSets property.
     * 
     * @return
     *     possible object is
     *     {@link CTRangeSets }
     *     
     */
    public CTRangeSets getRangeSets() {
        return rangeSets;
    }

    /**
     * Sets the value of the rangeSets property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRangeSets }
     *     
     */
    public void setRangeSets(CTRangeSets value) {
        this.rangeSets = value;
    }

    /**
     * Gets the value of the autoPage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoPage() {
        if (autoPage == null) {
            return true;
        } else {
            return autoPage;
        }
    }

    /**
     * Sets the value of the autoPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoPage(Boolean value) {
        this.autoPage = value;
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
