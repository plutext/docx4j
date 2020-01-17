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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextNormalAutofit complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextNormalAutofit"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="fontScale" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextFontScalePercent" default="100000" /&gt;
 *       &lt;attribute name="lnSpcReduction" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextSpacingPercent" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextNormalAutofit")
public class CTTextNormalAutofit implements Child
{

    @XmlAttribute(name = "fontScale")
    protected Integer fontScale;
    @XmlAttribute(name = "lnSpcReduction")
    protected Integer lnSpcReduction;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fontScale property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getFontScale() {
        if (fontScale == null) {
            return  100000;
        } else {
            return fontScale;
        }
    }

    /**
     * Sets the value of the fontScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFontScale(Integer value) {
        this.fontScale = value;
    }

    /**
     * Gets the value of the lnSpcReduction property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getLnSpcReduction() {
        if (lnSpcReduction == null) {
            return  0;
        } else {
            return lnSpcReduction;
        }
    }

    /**
     * Sets the value of the lnSpcReduction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLnSpcReduction(Integer value) {
        this.lnSpcReduction = value;
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
