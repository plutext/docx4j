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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PrintProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PrintProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="prnWhat" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_PrintWhat" default="slides" /&gt;
 *       &lt;attribute name="clrMode" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_PrintColorMode" default="clr" /&gt;
 *       &lt;attribute name="hiddenSlides" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="scaleToFitPaper" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="frameSlides" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PrintProperties", propOrder = {
    "extLst"
})
public class CTPrintProperties implements Child
{

    protected CTExtensionList extLst;
    @XmlAttribute(name = "prnWhat")
    protected STPrintWhat prnWhat;
    @XmlAttribute(name = "clrMode")
    protected STPrintColorMode clrMode;
    @XmlAttribute(name = "hiddenSlides")
    protected Boolean hiddenSlides;
    @XmlAttribute(name = "scaleToFitPaper")
    protected Boolean scaleToFitPaper;
    @XmlAttribute(name = "frameSlides")
    protected Boolean frameSlides;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the prnWhat property.
     * 
     * @return
     *     possible object is
     *     {@link STPrintWhat }
     *     
     */
    public STPrintWhat getPrnWhat() {
        if (prnWhat == null) {
            return STPrintWhat.SLIDES;
        } else {
            return prnWhat;
        }
    }

    /**
     * Sets the value of the prnWhat property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPrintWhat }
     *     
     */
    public void setPrnWhat(STPrintWhat value) {
        this.prnWhat = value;
    }

    /**
     * Gets the value of the clrMode property.
     * 
     * @return
     *     possible object is
     *     {@link STPrintColorMode }
     *     
     */
    public STPrintColorMode getClrMode() {
        if (clrMode == null) {
            return STPrintColorMode.CLR;
        } else {
            return clrMode;
        }
    }

    /**
     * Sets the value of the clrMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPrintColorMode }
     *     
     */
    public void setClrMode(STPrintColorMode value) {
        this.clrMode = value;
    }

    /**
     * Gets the value of the hiddenSlides property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenSlides() {
        if (hiddenSlides == null) {
            return false;
        } else {
            return hiddenSlides;
        }
    }

    /**
     * Sets the value of the hiddenSlides property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenSlides(Boolean value) {
        this.hiddenSlides = value;
    }

    /**
     * Gets the value of the scaleToFitPaper property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isScaleToFitPaper() {
        if (scaleToFitPaper == null) {
            return false;
        } else {
            return scaleToFitPaper;
        }
    }

    /**
     * Sets the value of the scaleToFitPaper property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setScaleToFitPaper(Boolean value) {
        this.scaleToFitPaper = value;
    }

    /**
     * Gets the value of the frameSlides property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFrameSlides() {
        if (frameSlides == null) {
            return false;
        } else {
            return frameSlides;
        }
    }

    /**
     * Sets the value of the frameSlides property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFrameSlides(Boolean value) {
        this.frameSlides = value;
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
