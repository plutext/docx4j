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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TLCommonMediaNodeData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLCommonMediaNodeData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cTn" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonTimeNodeData"/>
 *         &lt;element name="tgtEl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeTargetElement"/>
 *       &lt;/sequence>
 *       &lt;attribute name="vol" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="50" />
 *       &lt;attribute name="mute" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="numSld" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="showWhenStopped" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLCommonMediaNodeData", propOrder = {
    "cTn",
    "tgtEl"
})
public class CTTLCommonMediaNodeData {

    @XmlElement(required = true)
    protected CTTLCommonTimeNodeData cTn;
    @XmlElement(required = true)
    protected CTTLTimeTargetElement tgtEl;
    @XmlAttribute(name = "vol")
    protected Integer vol;
    @XmlAttribute(name = "mute")
    protected Boolean mute;
    @XmlAttribute(name = "numSld")
    @XmlSchemaType(name = "unsignedInt")
    protected Long numSld;
    @XmlAttribute(name = "showWhenStopped")
    protected Boolean showWhenStopped;

    /**
     * Gets the value of the cTn property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLCommonTimeNodeData }
     *     
     */
    public CTTLCommonTimeNodeData getCTn() {
        return cTn;
    }

    /**
     * Sets the value of the cTn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLCommonTimeNodeData }
     *     
     */
    public void setCTn(CTTLCommonTimeNodeData value) {
        this.cTn = value;
    }

    /**
     * Gets the value of the tgtEl property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeTargetElement }
     *     
     */
    public CTTLTimeTargetElement getTgtEl() {
        return tgtEl;
    }

    /**
     * Sets the value of the tgtEl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeTargetElement }
     *     
     */
    public void setTgtEl(CTTLTimeTargetElement value) {
        this.tgtEl = value;
    }

    /**
     * Gets the value of the vol property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getVol() {
        if (vol == null) {
            return  50;
        } else {
            return vol;
        }
    }

    /**
     * Sets the value of the vol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVol(Integer value) {
        this.vol = value;
    }

    /**
     * Gets the value of the mute property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMute() {
        if (mute == null) {
            return false;
        } else {
            return mute;
        }
    }

    /**
     * Sets the value of the mute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMute(Boolean value) {
        this.mute = value;
    }

    /**
     * Gets the value of the numSld property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getNumSld() {
        if (numSld == null) {
            return  1L;
        } else {
            return numSld;
        }
    }

    /**
     * Sets the value of the numSld property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNumSld(Long value) {
        this.numSld = value;
    }

    /**
     * Gets the value of the showWhenStopped property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowWhenStopped() {
        if (showWhenStopped == null) {
            return true;
        } else {
            return showWhenStopped;
        }
    }

    /**
     * Sets the value of the showWhenStopped property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowWhenStopped(Boolean value) {
        this.showWhenStopped = value;
    }

}
