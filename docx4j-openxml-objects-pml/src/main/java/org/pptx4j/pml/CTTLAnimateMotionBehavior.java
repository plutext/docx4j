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

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLAnimateMotionBehavior complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLAnimateMotionBehavior"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cBhvr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommonBehaviorData"/&gt;
 *         &lt;element name="by" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLPoint" minOccurs="0"/&gt;
 *         &lt;element name="from" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLPoint" minOccurs="0"/&gt;
 *         &lt;element name="to" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLPoint" minOccurs="0"/&gt;
 *         &lt;element name="rCtr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLPoint" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="origin" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateMotionBehaviorOrigin" /&gt;
 *       &lt;attribute name="path" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="pathEditMode" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLAnimateMotionPathEditMode" /&gt;
 *       &lt;attribute name="rAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" /&gt;
 *       &lt;attribute name="ptsTypes" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLAnimateMotionBehavior", propOrder = {
    "cBhvr",
    "by",
    "from",
    "to",
    "rCtr"
})
public class CTTLAnimateMotionBehavior implements Child
{

    @XmlElement(required = true)
    protected CTTLCommonBehaviorData cBhvr;
    protected CTTLPoint by;
    protected CTTLPoint from;
    protected CTTLPoint to;
    protected CTTLPoint rCtr;
    @XmlAttribute(name = "origin")
    protected STTLAnimateMotionBehaviorOrigin origin;
    @XmlAttribute(name = "path")
    protected String path;
    @XmlAttribute(name = "pathEditMode")
    protected STTLAnimateMotionPathEditMode pathEditMode;
    @XmlAttribute(name = "rAng")
    protected Integer rAng;
    @XmlAttribute(name = "ptsTypes")
    protected String ptsTypes;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cBhvr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLCommonBehaviorData }
     *     
     */
    public CTTLCommonBehaviorData getCBhvr() {
        return cBhvr;
    }

    /**
     * Sets the value of the cBhvr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLCommonBehaviorData }
     *     
     */
    public void setCBhvr(CTTLCommonBehaviorData value) {
        this.cBhvr = value;
    }

    /**
     * Gets the value of the by property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLPoint }
     *     
     */
    public CTTLPoint getBy() {
        return by;
    }

    /**
     * Sets the value of the by property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLPoint }
     *     
     */
    public void setBy(CTTLPoint value) {
        this.by = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLPoint }
     *     
     */
    public CTTLPoint getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLPoint }
     *     
     */
    public void setFrom(CTTLPoint value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLPoint }
     *     
     */
    public CTTLPoint getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLPoint }
     *     
     */
    public void setTo(CTTLPoint value) {
        this.to = value;
    }

    /**
     * Gets the value of the rCtr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLPoint }
     *     
     */
    public CTTLPoint getRCtr() {
        return rCtr;
    }

    /**
     * Sets the value of the rCtr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLPoint }
     *     
     */
    public void setRCtr(CTTLPoint value) {
        this.rCtr = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateMotionBehaviorOrigin }
     *     
     */
    public STTLAnimateMotionBehaviorOrigin getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateMotionBehaviorOrigin }
     *     
     */
    public void setOrigin(STTLAnimateMotionBehaviorOrigin value) {
        this.origin = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the pathEditMode property.
     * 
     * @return
     *     possible object is
     *     {@link STTLAnimateMotionPathEditMode }
     *     
     */
    public STTLAnimateMotionPathEditMode getPathEditMode() {
        return pathEditMode;
    }

    /**
     * Sets the value of the pathEditMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLAnimateMotionPathEditMode }
     *     
     */
    public void setPathEditMode(STTLAnimateMotionPathEditMode value) {
        this.pathEditMode = value;
    }

    /**
     * Gets the value of the rAng property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRAng() {
        return rAng;
    }

    /**
     * Sets the value of the rAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRAng(Integer value) {
        this.rAng = value;
    }

    /**
     * Gets the value of the ptsTypes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPtsTypes() {
        return ptsTypes;
    }

    /**
     * Sets the value of the ptsTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPtsTypes(String value) {
        this.ptsTypes = value;
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
