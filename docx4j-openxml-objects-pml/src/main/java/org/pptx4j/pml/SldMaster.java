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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColorMapping;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cSld" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_CommonSlideData"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/presentationml/2006/main}EG_TopLevelSlide"/&gt;
 *         &lt;element name="sldLayoutIdLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SlideLayoutIdList" minOccurs="0"/&gt;
 *         &lt;element name="transition" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SlideTransition" minOccurs="0"/&gt;
 *         &lt;element name="timing" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SlideTiming" minOccurs="0"/&gt;
 *         &lt;element name="hf" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_HeaderFooter" minOccurs="0"/&gt;
 *         &lt;element name="txStyles" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SlideMasterTextStyles" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionListModify" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="preserve" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cSld",
    "clrMap",
    "sldLayoutIdLst",
    "transition",
    "timing",
    "hf",
    "txStyles",
    "extLst"
})
@XmlRootElement(name = "sldMaster")
public class SldMaster implements Child
{

    @XmlElement(required = true)
    protected CommonSlideData cSld;
    @XmlElement(required = true)
    protected CTColorMapping clrMap;
    protected SlideLayoutIdList sldLayoutIdLst;
    protected CTSlideTransition transition;
    protected CTSlideTiming timing;
    protected CTHeaderFooter hf;
    protected CTSlideMasterTextStyles txStyles;
    protected CTExtensionListModify extLst;
    @XmlAttribute(name = "preserve")
    protected Boolean preserve;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cSld property.
     * 
     * @return
     *     possible object is
     *     {@link CommonSlideData }
     *     
     */
    public CommonSlideData getCSld() {
        return cSld;
    }

    /**
     * Sets the value of the cSld property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommonSlideData }
     *     
     */
    public void setCSld(CommonSlideData value) {
        this.cSld = value;
    }

    /**
     * Gets the value of the clrMap property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getClrMap() {
        return clrMap;
    }

    /**
     * Sets the value of the clrMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setClrMap(CTColorMapping value) {
        this.clrMap = value;
    }

    /**
     * Gets the value of the sldLayoutIdLst property.
     * 
     * @return
     *     possible object is
     *     {@link SlideLayoutIdList }
     *     
     */
    public SlideLayoutIdList getSldLayoutIdLst() {
        return sldLayoutIdLst;
    }

    /**
     * Sets the value of the sldLayoutIdLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link SlideLayoutIdList }
     *     
     */
    public void setSldLayoutIdLst(SlideLayoutIdList value) {
        this.sldLayoutIdLst = value;
    }

    /**
     * Gets the value of the transition property.
     * 
     * @return
     *     possible object is
     *     {@link CTSlideTransition }
     *     
     */
    public CTSlideTransition getTransition() {
        return transition;
    }

    /**
     * Sets the value of the transition property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSlideTransition }
     *     
     */
    public void setTransition(CTSlideTransition value) {
        this.transition = value;
    }

    /**
     * Gets the value of the timing property.
     * 
     * @return
     *     possible object is
     *     {@link CTSlideTiming }
     *     
     */
    public CTSlideTiming getTiming() {
        return timing;
    }

    /**
     * Sets the value of the timing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSlideTiming }
     *     
     */
    public void setTiming(CTSlideTiming value) {
        this.timing = value;
    }

    /**
     * Gets the value of the hf property.
     * 
     * @return
     *     possible object is
     *     {@link CTHeaderFooter }
     *     
     */
    public CTHeaderFooter getHf() {
        return hf;
    }

    /**
     * Sets the value of the hf property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHeaderFooter }
     *     
     */
    public void setHf(CTHeaderFooter value) {
        this.hf = value;
    }

    /**
     * Gets the value of the txStyles property.
     * 
     * @return
     *     possible object is
     *     {@link CTSlideMasterTextStyles }
     *     
     */
    public CTSlideMasterTextStyles getTxStyles() {
        return txStyles;
    }

    /**
     * Sets the value of the txStyles property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSlideMasterTextStyles }
     *     
     */
    public void setTxStyles(CTSlideMasterTextStyles value) {
        this.txStyles = value;
    }

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
     * Gets the value of the preserve property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPreserve() {
        if (preserve == null) {
            return false;
        } else {
            return preserve;
        }
    }

    /**
     * Sets the value of the preserve property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreserve(Boolean value) {
        this.preserve = value;
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
