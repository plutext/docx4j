/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_WebProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WebProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="showAnimation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="resizeGraphics" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="allowPng" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="relyOnVml" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="organizeInFolders" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="useLongFilenames" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="imgSz" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_WebScreenSize" default="800x600" />
 *       &lt;attribute name="encoding" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_WebEncoding" default="" />
 *       &lt;attribute name="clr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_WebColorType" default="whiteTextOnBlack" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WebProperties", propOrder = {
    "extLst"
})
public class CTWebProperties {

    protected CTExtensionList extLst;
    @XmlAttribute
    protected Boolean showAnimation;
    @XmlAttribute
    protected Boolean resizeGraphics;
    @XmlAttribute
    protected Boolean allowPng;
    @XmlAttribute
    protected Boolean relyOnVml;
    @XmlAttribute
    protected Boolean organizeInFolders;
    @XmlAttribute
    protected Boolean useLongFilenames;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String imgSz;
    @XmlAttribute
    protected String encoding;
    @XmlAttribute
    protected STWebColorType clr;

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
     * Gets the value of the showAnimation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowAnimation() {
        if (showAnimation == null) {
            return false;
        } else {
            return showAnimation;
        }
    }

    /**
     * Sets the value of the showAnimation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowAnimation(Boolean value) {
        this.showAnimation = value;
    }

    /**
     * Gets the value of the resizeGraphics property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isResizeGraphics() {
        if (resizeGraphics == null) {
            return true;
        } else {
            return resizeGraphics;
        }
    }

    /**
     * Sets the value of the resizeGraphics property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setResizeGraphics(Boolean value) {
        this.resizeGraphics = value;
    }

    /**
     * Gets the value of the allowPng property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAllowPng() {
        if (allowPng == null) {
            return false;
        } else {
            return allowPng;
        }
    }

    /**
     * Sets the value of the allowPng property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowPng(Boolean value) {
        this.allowPng = value;
    }

    /**
     * Gets the value of the relyOnVml property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRelyOnVml() {
        if (relyOnVml == null) {
            return false;
        } else {
            return relyOnVml;
        }
    }

    /**
     * Sets the value of the relyOnVml property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRelyOnVml(Boolean value) {
        this.relyOnVml = value;
    }

    /**
     * Gets the value of the organizeInFolders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOrganizeInFolders() {
        if (organizeInFolders == null) {
            return true;
        } else {
            return organizeInFolders;
        }
    }

    /**
     * Sets the value of the organizeInFolders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOrganizeInFolders(Boolean value) {
        this.organizeInFolders = value;
    }

    /**
     * Gets the value of the useLongFilenames property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseLongFilenames() {
        if (useLongFilenames == null) {
            return true;
        } else {
            return useLongFilenames;
        }
    }

    /**
     * Sets the value of the useLongFilenames property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseLongFilenames(Boolean value) {
        this.useLongFilenames = value;
    }

    /**
     * Gets the value of the imgSz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImgSz() {
        if (imgSz == null) {
            return "800x600";
        } else {
            return imgSz;
        }
    }

    /**
     * Sets the value of the imgSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImgSz(String value) {
        this.imgSz = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncoding() {
        if (encoding == null) {
            return "";
        } else {
            return encoding;
        }
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the clr property.
     * 
     * @return
     *     possible object is
     *     {@link STWebColorType }
     *     
     */
    public STWebColorType getClr() {
        if (clr == null) {
            return STWebColorType.WHITE_TEXT_ON_BLACK;
        } else {
            return clr;
        }
    }

    /**
     * Sets the value of the clr property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWebColorType }
     *     
     */
    public void setClr(STWebColorType value) {
        this.clr = value;
    }

}
