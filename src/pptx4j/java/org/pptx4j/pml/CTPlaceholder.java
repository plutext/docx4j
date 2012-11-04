/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Placeholder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Placeholder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionListModify" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_PlaceholderType" default="obj" />
 *       &lt;attribute name="orient" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_Direction" default="horz" />
 *       &lt;attribute name="sz" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_PlaceholderSize" default="full" />
 *       &lt;attribute name="idx" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="hasCustomPrompt" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Placeholder", propOrder = {
    "extLst"
})
public class CTPlaceholder {

    protected CTExtensionListModify extLst;
    @XmlAttribute(name = "type")
    protected STPlaceholderType type;
    @XmlAttribute(name = "orient")
    protected STDirection orient;
    @XmlAttribute(name = "sz")
    protected STPlaceholderSize sz;
    @XmlAttribute(name = "idx")
    @XmlSchemaType(name = "unsignedInt")
    protected Long idx;
    @XmlAttribute(name = "hasCustomPrompt")
    protected Boolean hasCustomPrompt;

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionListModify }
     *     
     */
    public CTExtensionListModify getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionListModify }
     *     
     */
    public void setExtLst(CTExtensionListModify value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STPlaceholderType }
     *     
     */
    public STPlaceholderType getType() {
        if (type == null) {
            return STPlaceholderType.OBJ;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPlaceholderType }
     *     
     */
    public void setType(STPlaceholderType value) {
        this.type = value;
    }

    /**
     * Gets the value of the orient property.
     * 
     * @return
     *     possible object is
     *     {@link STDirection }
     *     
     */
    public STDirection getOrient() {
        if (orient == null) {
            return STDirection.HORZ;
        } else {
            return orient;
        }
    }

    /**
     * Sets the value of the orient property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDirection }
     *     
     */
    public void setOrient(STDirection value) {
        this.orient = value;
    }

    /**
     * Gets the value of the sz property.
     * 
     * @return
     *     possible object is
     *     {@link STPlaceholderSize }
     *     
     */
    public STPlaceholderSize getSz() {
        if (sz == null) {
            return STPlaceholderSize.FULL;
        } else {
            return sz;
        }
    }

    /**
     * Sets the value of the sz property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPlaceholderSize }
     *     
     */
    public void setSz(STPlaceholderSize value) {
        this.sz = value;
    }

    /**
     * Gets the value of the idx property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getIdx() {
        if (idx == null) {
            return  0L;
        } else {
            return idx;
        }
    }

    /**
     * Sets the value of the idx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdx(Long value) {
        this.idx = value;
    }

    /**
     * Gets the value of the hasCustomPrompt property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHasCustomPrompt() {
        if (hasCustomPrompt == null) {
            return false;
        } else {
            return hasCustomPrompt;
        }
    }

    /**
     * Sets the value of the hasCustomPrompt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasCustomPrompt(Boolean value) {
        this.hasCustomPrompt = value;
    }

}
