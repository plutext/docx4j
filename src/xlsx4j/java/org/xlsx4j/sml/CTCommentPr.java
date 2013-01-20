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
 * <p>Java class for CT_CommentPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommentPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="anchor" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ObjectAnchor"/>
 *       &lt;/sequence>
 *       &lt;attribute name="locked" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="defaultSize" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="print" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="disabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="autoFill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="autoLine" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="altText" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="textHAlign" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_TextHAlign" default="left" />
 *       &lt;attribute name="textVAlign" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_TextVAlign" default="top" />
 *       &lt;attribute name="lockText" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="justLastX" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="autoScale" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommentPr", propOrder = {
    "anchor"
})
public class CTCommentPr implements Child
{

    @XmlElement(required = true)
    protected CTObjectAnchor anchor;
    @XmlAttribute(name = "locked")
    protected Boolean locked;
    @XmlAttribute(name = "defaultSize")
    protected Boolean defaultSize;
    @XmlAttribute(name = "print")
    protected Boolean print;
    @XmlAttribute(name = "disabled")
    protected Boolean disabled;
    @XmlAttribute(name = "autoFill")
    protected Boolean autoFill;
    @XmlAttribute(name = "autoLine")
    protected Boolean autoLine;
    @XmlAttribute(name = "altText")
    protected String altText;
    @XmlAttribute(name = "textHAlign")
    protected STTextHAlign textHAlign;
    @XmlAttribute(name = "textVAlign")
    protected STTextVAlign textVAlign;
    @XmlAttribute(name = "lockText")
    protected Boolean lockText;
    @XmlAttribute(name = "justLastX")
    protected Boolean justLastX;
    @XmlAttribute(name = "autoScale")
    protected Boolean autoScale;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the anchor property.
     * 
     * @return
     *     possible object is
     *     {@link CTObjectAnchor }
     *     
     */
    public CTObjectAnchor getAnchor() {
        return anchor;
    }

    /**
     * Sets the value of the anchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTObjectAnchor }
     *     
     */
    public void setAnchor(CTObjectAnchor value) {
        this.anchor = value;
    }

    /**
     * Gets the value of the locked property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLocked() {
        if (locked == null) {
            return true;
        } else {
            return locked;
        }
    }

    /**
     * Sets the value of the locked property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLocked(Boolean value) {
        this.locked = value;
    }

    /**
     * Gets the value of the defaultSize property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefaultSize() {
        if (defaultSize == null) {
            return true;
        } else {
            return defaultSize;
        }
    }

    /**
     * Sets the value of the defaultSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultSize(Boolean value) {
        this.defaultSize = value;
    }

    /**
     * Gets the value of the print property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPrint() {
        if (print == null) {
            return true;
        } else {
            return print;
        }
    }

    /**
     * Sets the value of the print property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrint(Boolean value) {
        this.print = value;
    }

    /**
     * Gets the value of the disabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDisabled() {
        if (disabled == null) {
            return false;
        } else {
            return disabled;
        }
    }

    /**
     * Sets the value of the disabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisabled(Boolean value) {
        this.disabled = value;
    }

    /**
     * Gets the value of the autoFill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoFill() {
        if (autoFill == null) {
            return true;
        } else {
            return autoFill;
        }
    }

    /**
     * Sets the value of the autoFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoFill(Boolean value) {
        this.autoFill = value;
    }

    /**
     * Gets the value of the autoLine property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoLine() {
        if (autoLine == null) {
            return true;
        } else {
            return autoLine;
        }
    }

    /**
     * Sets the value of the autoLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoLine(Boolean value) {
        this.autoLine = value;
    }

    /**
     * Gets the value of the altText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAltText() {
        return altText;
    }

    /**
     * Sets the value of the altText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAltText(String value) {
        this.altText = value;
    }

    /**
     * Gets the value of the textHAlign property.
     * 
     * @return
     *     possible object is
     *     {@link STTextHAlign }
     *     
     */
    public STTextHAlign getTextHAlign() {
        if (textHAlign == null) {
            return STTextHAlign.LEFT;
        } else {
            return textHAlign;
        }
    }

    /**
     * Sets the value of the textHAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextHAlign }
     *     
     */
    public void setTextHAlign(STTextHAlign value) {
        this.textHAlign = value;
    }

    /**
     * Gets the value of the textVAlign property.
     * 
     * @return
     *     possible object is
     *     {@link STTextVAlign }
     *     
     */
    public STTextVAlign getTextVAlign() {
        if (textVAlign == null) {
            return STTextVAlign.TOP;
        } else {
            return textVAlign;
        }
    }

    /**
     * Sets the value of the textVAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextVAlign }
     *     
     */
    public void setTextVAlign(STTextVAlign value) {
        this.textVAlign = value;
    }

    /**
     * Gets the value of the lockText property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLockText() {
        if (lockText == null) {
            return true;
        } else {
            return lockText;
        }
    }

    /**
     * Sets the value of the lockText property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLockText(Boolean value) {
        this.lockText = value;
    }

    /**
     * Gets the value of the justLastX property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isJustLastX() {
        if (justLastX == null) {
            return false;
        } else {
            return justLastX;
        }
    }

    /**
     * Sets the value of the justLastX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setJustLastX(Boolean value) {
        this.justLastX = value;
    }

    /**
     * Gets the value of the autoScale property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoScale() {
        if (autoScale == null) {
            return false;
        } else {
            return autoScale;
        }
    }

    /**
     * Sets the value of the autoScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoScale(Boolean value) {
        this.autoScale = value;
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
