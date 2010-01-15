/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PictureLocking complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureLocking">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/main}AG_Locking"/>
 *       &lt;attribute name="noCrop" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureLocking", propOrder = {
    "extLst"
})
public class CTPictureLocking {

    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute
    protected Boolean noCrop;
    @XmlAttribute
    protected Boolean noGrp;
    @XmlAttribute
    protected Boolean noSelect;
    @XmlAttribute
    protected Boolean noRot;
    @XmlAttribute
    protected Boolean noChangeAspect;
    @XmlAttribute
    protected Boolean noMove;
    @XmlAttribute
    protected Boolean noResize;
    @XmlAttribute
    protected Boolean noEditPoints;
    @XmlAttribute
    protected Boolean noAdjustHandles;
    @XmlAttribute
    protected Boolean noChangeArrowheads;
    @XmlAttribute
    protected Boolean noChangeShapeType;

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the noCrop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoCrop() {
        if (noCrop == null) {
            return false;
        } else {
            return noCrop;
        }
    }

    /**
     * Sets the value of the noCrop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoCrop(Boolean value) {
        this.noCrop = value;
    }

    /**
     * Gets the value of the noGrp property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoGrp() {
        if (noGrp == null) {
            return false;
        } else {
            return noGrp;
        }
    }

    /**
     * Sets the value of the noGrp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoGrp(Boolean value) {
        this.noGrp = value;
    }

    /**
     * Gets the value of the noSelect property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoSelect() {
        if (noSelect == null) {
            return false;
        } else {
            return noSelect;
        }
    }

    /**
     * Sets the value of the noSelect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoSelect(Boolean value) {
        this.noSelect = value;
    }

    /**
     * Gets the value of the noRot property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoRot() {
        if (noRot == null) {
            return false;
        } else {
            return noRot;
        }
    }

    /**
     * Sets the value of the noRot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoRot(Boolean value) {
        this.noRot = value;
    }

    /**
     * Gets the value of the noChangeAspect property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoChangeAspect() {
        if (noChangeAspect == null) {
            return false;
        } else {
            return noChangeAspect;
        }
    }

    /**
     * Sets the value of the noChangeAspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoChangeAspect(Boolean value) {
        this.noChangeAspect = value;
    }

    /**
     * Gets the value of the noMove property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoMove() {
        if (noMove == null) {
            return false;
        } else {
            return noMove;
        }
    }

    /**
     * Sets the value of the noMove property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoMove(Boolean value) {
        this.noMove = value;
    }

    /**
     * Gets the value of the noResize property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoResize() {
        if (noResize == null) {
            return false;
        } else {
            return noResize;
        }
    }

    /**
     * Sets the value of the noResize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoResize(Boolean value) {
        this.noResize = value;
    }

    /**
     * Gets the value of the noEditPoints property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoEditPoints() {
        if (noEditPoints == null) {
            return false;
        } else {
            return noEditPoints;
        }
    }

    /**
     * Sets the value of the noEditPoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoEditPoints(Boolean value) {
        this.noEditPoints = value;
    }

    /**
     * Gets the value of the noAdjustHandles property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoAdjustHandles() {
        if (noAdjustHandles == null) {
            return false;
        } else {
            return noAdjustHandles;
        }
    }

    /**
     * Sets the value of the noAdjustHandles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoAdjustHandles(Boolean value) {
        this.noAdjustHandles = value;
    }

    /**
     * Gets the value of the noChangeArrowheads property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoChangeArrowheads() {
        if (noChangeArrowheads == null) {
            return false;
        } else {
            return noChangeArrowheads;
        }
    }

    /**
     * Sets the value of the noChangeArrowheads property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoChangeArrowheads(Boolean value) {
        this.noChangeArrowheads = value;
    }

    /**
     * Gets the value of the noChangeShapeType property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoChangeShapeType() {
        if (noChangeShapeType == null) {
            return false;
        } else {
            return noChangeShapeType;
        }
    }

    /**
     * Sets the value of the noChangeShapeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoChangeShapeType(Boolean value) {
        this.noChangeShapeType = value;
    }

}
