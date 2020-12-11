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


package org.docx4j.dml.diagram;

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTHslColor;
import org.docx4j.dml.CTPresetColor;
import org.docx4j.dml.CTSRgbColor;
import org.docx4j.dml.CTScRgbColor;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.CTSystemColor;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Colors complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Colors"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorChoice" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="meth" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ClrAppMethod" default="span" /&gt;
 *       &lt;attribute name="hueDir" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_HueDir" default="cw" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Colors", propOrder = {
    "egColorChoice"
})
public class CTColors implements Child
{

    @XmlElements({
        @XmlElement(name = "scrgbClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTScRgbColor.class),
        @XmlElement(name = "srgbClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSRgbColor.class),
        @XmlElement(name = "hslClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTHslColor.class),
        @XmlElement(name = "sysClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSystemColor.class),
        @XmlElement(name = "schemeClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSchemeColor.class),
        @XmlElement(name = "prstClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTPresetColor.class)
    })
    protected List<Object> egColorChoice = new ArrayListDml<Object>(this);
    @XmlAttribute(name = "meth")
    protected STClrAppMethod meth;
    @XmlAttribute(name = "hueDir")
    protected STHueDir hueDir;
    @XmlTransient
    private Object parent;

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
     * {@link CTScRgbColor }
     * {@link CTSRgbColor }
     * {@link CTHslColor }
     * {@link CTSystemColor }
     * {@link CTSchemeColor }
     * {@link CTPresetColor }
     * 
     * 
     */
    public List<Object> getEGColorChoice() {
        if (egColorChoice == null) {
            egColorChoice = new ArrayListDml<Object>(this);
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
