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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTAudioCD;
import org.docx4j.dml.CTAudioFile;
import org.docx4j.dml.CTEmbeddedWAVAudioFile;
import org.docx4j.dml.CTQuickTimeFile;
import org.docx4j.dml.CTVideoFile;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ApplicationNonVisualDrawingProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ApplicationNonVisualDrawingProps"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ph" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Placeholder" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_Media" minOccurs="0"/&gt;
 *         &lt;element name="custDataLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_CustomerDataList" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="isPhoto" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="userDrawn" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ApplicationNonVisualDrawingProps", propOrder = {
    "ph",
    "audioCd",
    "wavAudioFile",
    "audioFile",
    "videoFile",
    "quickTimeFile",
    "custDataLst",
    "extLst"
})
public class NvPr implements Child
{

    protected CTPlaceholder ph;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTAudioCD audioCd;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTEmbeddedWAVAudioFile wavAudioFile;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTAudioFile audioFile;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTVideoFile videoFile;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTQuickTimeFile quickTimeFile;
    protected CTCustomerDataList custDataLst;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "isPhoto")
    protected Boolean isPhoto;
    @XmlAttribute(name = "userDrawn")
    protected Boolean userDrawn;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ph property.
     * 
     * @return
     *     possible object is
     *     {@link CTPlaceholder }
     *     
     */
    public CTPlaceholder getPh() {
        return ph;
    }

    /**
     * Sets the value of the ph property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPlaceholder }
     *     
     */
    public void setPh(CTPlaceholder value) {
        this.ph = value;
    }

    /**
     * Gets the value of the audioCd property.
     * 
     * @return
     *     possible object is
     *     {@link CTAudioCD }
     *     
     */
    public CTAudioCD getAudioCd() {
        return audioCd;
    }

    /**
     * Sets the value of the audioCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAudioCD }
     *     
     */
    public void setAudioCd(CTAudioCD value) {
        this.audioCd = value;
    }

    /**
     * Gets the value of the wavAudioFile property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedWAVAudioFile }
     *     
     */
    public CTEmbeddedWAVAudioFile getWavAudioFile() {
        return wavAudioFile;
    }

    /**
     * Sets the value of the wavAudioFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedWAVAudioFile }
     *     
     */
    public void setWavAudioFile(CTEmbeddedWAVAudioFile value) {
        this.wavAudioFile = value;
    }

    /**
     * Gets the value of the audioFile property.
     * 
     * @return
     *     possible object is
     *     {@link CTAudioFile }
     *     
     */
    public CTAudioFile getAudioFile() {
        return audioFile;
    }

    /**
     * Sets the value of the audioFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAudioFile }
     *     
     */
    public void setAudioFile(CTAudioFile value) {
        this.audioFile = value;
    }

    /**
     * Gets the value of the videoFile property.
     * 
     * @return
     *     possible object is
     *     {@link CTVideoFile }
     *     
     */
    public CTVideoFile getVideoFile() {
        return videoFile;
    }

    /**
     * Sets the value of the videoFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVideoFile }
     *     
     */
    public void setVideoFile(CTVideoFile value) {
        this.videoFile = value;
    }

    /**
     * Gets the value of the quickTimeFile property.
     * 
     * @return
     *     possible object is
     *     {@link CTQuickTimeFile }
     *     
     */
    public CTQuickTimeFile getQuickTimeFile() {
        return quickTimeFile;
    }

    /**
     * Sets the value of the quickTimeFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTQuickTimeFile }
     *     
     */
    public void setQuickTimeFile(CTQuickTimeFile value) {
        this.quickTimeFile = value;
    }

    /**
     * Gets the value of the custDataLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomerDataList }
     *     
     */
    public CTCustomerDataList getCustDataLst() {
        return custDataLst;
    }

    /**
     * Sets the value of the custDataLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomerDataList }
     *     
     */
    public void setCustDataLst(CTCustomerDataList value) {
        this.custDataLst = value;
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
     * Gets the value of the isPhoto property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsPhoto() {
        if (isPhoto == null) {
            return false;
        } else {
            return isPhoto;
        }
    }

    /**
     * Sets the value of the isPhoto property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsPhoto(Boolean value) {
        this.isPhoto = value;
    }

    /**
     * Gets the value of the userDrawn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUserDrawn() {
        if (userDrawn == null) {
            return false;
        } else {
            return userDrawn;
        }
    }

    /**
     * Sets the value of the userDrawn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUserDrawn(Boolean value) {
        this.userDrawn = value;
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
