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

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ScRgbColor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ScRgbColor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorTransform" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="r" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" /&gt;
 *       &lt;attribute name="g" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" /&gt;
 *       &lt;attribute name="b" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ScRgbColor", propOrder = {
    "egColorTransform"
})
public class CTScRgbColor implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "gamma", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "shade", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "alpha", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "gray", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "red", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "hueOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "lumOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "blueOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "greenMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "invGamma", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "blue", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "hue", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "inv", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "blueMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "lum", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "redMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "green", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "redOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "greenOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "satOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "lumMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "alphaOff", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "hueMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "satMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "alphaMod", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "tint", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sat", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "comp", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> egColorTransform = new ArrayListDml<JAXBElement<?>>(this);

    @XmlAttribute(name = "r", required = true)
    protected int r;
    @XmlAttribute(name = "g", required = true)
    protected int g;
    @XmlAttribute(name = "b", required = true)
    protected int b;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the egColorTransform property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egColorTransform property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGColorTransform().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTGammaTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTGrayscaleTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAngle }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTInverseGammaTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedAngle }{@code >}
     * {@link JAXBElement }{@code <}{@link CTInverseTransform }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositivePercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPositiveFixedPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPercentage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTComplementTransform }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getEGColorTransform() {
        if (egColorTransform == null) {
            egColorTransform = new ArrayListDml<JAXBElement<?>>(this);
        }
        return this.egColorTransform;
    }

    /**
     * Gets the value of the r property.
     * 
     */
    public int getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     */
    public void setR(int value) {
        this.r = value;
    }

    /**
     * Gets the value of the g property.
     * 
     */
    public int getG() {
        return g;
    }

    /**
     * Sets the value of the g property.
     * 
     */
    public void setG(int value) {
        this.g = value;
    }

    /**
     * Gets the value of the b property.
     * 
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     */
    public void setB(int value) {
        this.b = value;
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
