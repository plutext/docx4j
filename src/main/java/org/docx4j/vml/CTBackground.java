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


package org.docx4j.vml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.officedrawing.STBWMode;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Background complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Background">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}fill" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Fill"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bwmode"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bwpure"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bwnormal"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}targetscreensize"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "urn:schemas-microsoft-com:vml", name = "CT_Background", propOrder = {
    "fill"
})
public class CTBackground
    implements Child
{

    protected CTFill fill;
    @XmlAttribute(name = "bwmode", namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwmode;
    @XmlAttribute(name = "bwpure", namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwpure;
    @XmlAttribute(name = "bwnormal", namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwnormal;
    @XmlAttribute(name = "targetscreensize", namespace = "urn:schemas-microsoft-com:office:office")
    protected String targetscreensize;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlAttribute(name = "filled")
    protected STTrueFalse filled;
    @XmlAttribute(name = "fillcolor")
    protected String fillcolor;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link CTFill }
     *     
     */
    public CTFill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFill }
     *     
     */
    public void setFill(CTFill value) {
        this.fill = value;
    }

    /**
     * Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwmode() {
        return bwmode;
    }

    /**
     * Sets the value of the bwmode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwmode(STBWMode value) {
        this.bwmode = value;
    }

    /**
     * Pure Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwpure() {
        return bwpure;
    }

    /**
     * Sets the value of the bwpure property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwpure(STBWMode value) {
        this.bwpure = value;
    }

    /**
     * Normal Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwnormal() {
        return bwnormal;
    }

    /**
     * Sets the value of the bwnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwnormal(STBWMode value) {
        this.bwnormal = value;
    }

    /**
     * Target Screen Size
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetscreensize() {
        return targetscreensize;
    }

    /**
     * Sets the value of the targetscreensize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetscreensize(String value) {
        this.targetscreensize = value;
    }

    /**
     * Gets the value of the vmlId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmlId() {
        return vmlId;
    }

    /**
     * Sets the value of the vmlId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmlId(String value) {
        this.vmlId = value;
    }

    /**
     * Gets the value of the filled property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getFilled() {
        return filled;
    }

    /**
     * Sets the value of the filled property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setFilled(STTrueFalse value) {
        this.filled = value;
    }

    /**
     * Gets the value of the fillcolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillcolor() {
        return fillcolor;
    }

    /**
     * Sets the value of the fillcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillcolor(String value) {
        this.fillcolor = value;
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
