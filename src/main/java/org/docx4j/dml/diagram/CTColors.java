/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.diagram;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTHslColor;
import org.docx4j.dml.CTPresetColor;
import org.docx4j.dml.CTSRgbColor;
import org.docx4j.dml.CTScRgbColor;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.CTSystemColor;


/**
 * <p>Java class for CT_Colors complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Colors">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorChoice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="meth" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ClrAppMethod" default="span" />
 *       &lt;attribute name="hueDir" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_HueDir" default="cw" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Colors", propOrder = {
    "egColorChoice"
})
public class CTColors {

    @XmlElements({
        @XmlElement(name = "srgbClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSRgbColor.class),
        @XmlElement(name = "prstClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTPresetColor.class),
        @XmlElement(name = "scrgbClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTScRgbColor.class),
        @XmlElement(name = "sysClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSystemColor.class),
        @XmlElement(name = "hslClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTHslColor.class),
        @XmlElement(name = "schemeClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSchemeColor.class)
    })
    protected List<Object> egColorChoice;
    @XmlAttribute
    protected STClrAppMethod meth;
    @XmlAttribute
    protected STHueDir hueDir;

    /**
     * Gets the value of the egColorChoice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egColorChoice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGColorChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSRgbColor }
     * {@link CTPresetColor }
     * {@link CTScRgbColor }
     * {@link CTSystemColor }
     * {@link CTHslColor }
     * {@link CTSchemeColor }
     * 
     * 
     */
    public List<Object> getEGColorChoice() {
        if (egColorChoice == null) {
            egColorChoice = new ArrayList<Object>();
        }
        return this.egColorChoice;
    }

    /**
     * Gets the value of the meth property.
     * 
     * @return
     *     possible object is
     *     {@link STClrAppMethod }
     *     
     */
    public STClrAppMethod getMeth() {
        if (meth == null) {
            return STClrAppMethod.SPAN;
        } else {
            return meth;
        }
    }

    /**
     * Sets the value of the meth property.
     * 
     * @param value
     *     allowed object is
     *     {@link STClrAppMethod }
     *     
     */
    public void setMeth(STClrAppMethod value) {
        this.meth = value;
    }

    /**
     * Gets the value of the hueDir property.
     * 
     * @return
     *     possible object is
     *     {@link STHueDir }
     *     
     */
    public STHueDir getHueDir() {
        if (hueDir == null) {
            return STHueDir.CW;
        } else {
            return hueDir;
        }
    }

    /**
     * Sets the value of the hueDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHueDir }
     *     
     */
    public void setHueDir(STHueDir value) {
        this.hueDir = value;
    }

}
