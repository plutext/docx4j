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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shadow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shadow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="type" type="{urn:schemas-microsoft-com:vml}ST_ShadowType" />
 *       &lt;attribute name="obscured" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="color" type="{urn:schemas-microsoft-com:vml}ST_ColorType" />
 *       &lt;attribute name="opacity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="offset" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="color2" type="{urn:schemas-microsoft-com:vml}ST_ColorType" />
 *       &lt;attribute name="offset2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="origin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="matrix" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shadow")
public class CTShadow implements Child
{

    @XmlAttribute(name = "on")
    protected STTrueFalse on;
    @XmlAttribute(name = "type")
    protected STShadowType type;
    @XmlAttribute(name = "obscured")
    protected STTrueFalse obscured;
    @XmlAttribute(name = "color")
    protected String color;
    @XmlAttribute(name = "opacity")
    protected String opacity;
    @XmlAttribute(name = "offset")
    protected String offset;
    @XmlAttribute(name = "color2")
    protected String color2;
    @XmlAttribute(name = "offset2")
    protected String offset2;
    @XmlAttribute(name = "origin")
    protected String origin;
    @XmlAttribute(name = "matrix")
    protected String matrix;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getOn() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setOn(STTrueFalse value) {
        this.on = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STShadowType }
     *     
     */
    public STShadowType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STShadowType }
     *     
     */
    public void setType(STShadowType value) {
        this.type = value;
    }

    /**
     * Gets the value of the obscured property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getObscured() {
        return obscured;
    }

    /**
     * Sets the value of the obscured property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setObscured(STTrueFalse value) {
        this.obscured = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity(String value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffset(String value) {
        this.offset = value;
    }

    /**
     * Gets the value of the color2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor2() {
        return color2;
    }

    /**
     * Sets the value of the color2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor2(String value) {
        this.color2 = value;
    }

    /**
     * Gets the value of the offset2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffset2() {
        return offset2;
    }

    /**
     * Sets the value of the offset2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffset2(String value) {
        this.offset2 = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Gets the value of the matrix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMatrix() {
        return matrix;
    }

    /**
     * Sets the value of the matrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMatrix(String value) {
        this.matrix = value;
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
