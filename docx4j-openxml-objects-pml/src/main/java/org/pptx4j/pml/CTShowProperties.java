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
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColor;


/**
 * <p>Java class for CT_ShowProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShowProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;group ref="{http://schemas.openxmlformats.org/presentationml/2006/main}EG_ShowType" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/presentationml/2006/main}EG_SlideListChoice" minOccurs="0"/>
 *         &lt;element name="penClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="loop" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showNarration" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showAnimation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="useTimings" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShowProperties", propOrder = {
    "present",
    "browse",
    "kiosk",
    "sldAll",
    "sldRg",
    "custShow",
    "penClr",
    "extLst"
})
public class CTShowProperties {

    protected CTEmpty present;
    protected CTShowInfoBrowse browse;
    protected CTShowInfoKiosk kiosk;
    protected CTEmpty sldAll;
    protected CTIndexRange sldRg;
    protected CTCustomShowId custShow;
    protected CTColor penClr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "loop")
    protected Boolean loop;
    @XmlAttribute(name = "showNarration")
    protected Boolean showNarration;
    @XmlAttribute(name = "showAnimation")
    protected Boolean showAnimation;
    @XmlAttribute(name = "useTimings")
    protected Boolean useTimings;

    /**
     * Gets the value of the present property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getPresent() {
        return present;
    }

    /**
     * Sets the value of the present property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setPresent(CTEmpty value) {
        this.present = value;
    }

    /**
     * Gets the value of the browse property.
     * 
     * @return
     *     possible object is
     *     {@link CTShowInfoBrowse }
     *     
     */
    public CTShowInfoBrowse getBrowse() {
        return browse;
    }

    /**
     * Sets the value of the browse property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShowInfoBrowse }
     *     
     */
    public void setBrowse(CTShowInfoBrowse value) {
        this.browse = value;
    }

    /**
     * Gets the value of the kiosk property.
     * 
     * @return
     *     possible object is
     *     {@link CTShowInfoKiosk }
     *     
     */
    public CTShowInfoKiosk getKiosk() {
        return kiosk;
    }

    /**
     * Sets the value of the kiosk property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShowInfoKiosk }
     *     
     */
    public void setKiosk(CTShowInfoKiosk value) {
        this.kiosk = value;
    }

    /**
     * Gets the value of the sldAll property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getSldAll() {
        return sldAll;
    }

    /**
     * Sets the value of the sldAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setSldAll(CTEmpty value) {
        this.sldAll = value;
    }

    /**
     * Gets the value of the sldRg property.
     * 
     * @return
     *     possible object is
     *     {@link CTIndexRange }
     *     
     */
    public CTIndexRange getSldRg() {
        return sldRg;
    }

    /**
     * Sets the value of the sldRg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIndexRange }
     *     
     */
    public void setSldRg(CTIndexRange value) {
        this.sldRg = value;
    }

    /**
     * Gets the value of the custShow property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomShowId }
     *     
     */
    public CTCustomShowId getCustShow() {
        return custShow;
    }

    /**
     * Sets the value of the custShow property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomShowId }
     *     
     */
    public void setCustShow(CTCustomShowId value) {
        this.custShow = value;
    }

    /**
     * Gets the value of the penClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getPenClr() {
        return penClr;
    }

    /**
     * Sets the value of the penClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setPenClr(CTColor value) {
        this.penClr = value;
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
     * Gets the value of the loop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLoop() {
        if (loop == null) {
            return false;
        } else {
            return loop;
        }
    }

    /**
     * Sets the value of the loop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLoop(Boolean value) {
        this.loop = value;
    }

    /**
     * Gets the value of the showNarration property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowNarration() {
        if (showNarration == null) {
            return false;
        } else {
            return showNarration;
        }
    }

    /**
     * Sets the value of the showNarration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowNarration(Boolean value) {
        this.showNarration = value;
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
            return true;
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
     * Gets the value of the useTimings property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseTimings() {
        if (useTimings == null) {
            return true;
        } else {
            return useTimings;
        }
    }

    /**
     * Sets the value of the useTimings property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseTimings(Boolean value) {
        this.useTimings = value;
    }

}
