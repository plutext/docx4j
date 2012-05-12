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


package org.docx4j.vml.wordprocessingDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Wrap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Wrap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" type="{urn:schemas-microsoft-com:office:word}ST_WrapType" />
 *       &lt;attribute name="side" type="{urn:schemas-microsoft-com:office:word}ST_WrapSide" />
 *       &lt;attribute name="anchorx" type="{urn:schemas-microsoft-com:office:word}ST_HorizontalAnchor" />
 *       &lt;attribute name="anchory" type="{urn:schemas-microsoft-com:office:word}ST_VerticalAnchor" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Wrap")
public class CTWrap implements Child
{

    @XmlAttribute(name = "type")
    protected STWrapType type;
    @XmlAttribute(name = "side")
    protected STWrapSide side;
    @XmlAttribute(name = "anchorx")
    protected STHorizontalAnchor anchorx;
    @XmlAttribute(name = "anchory")
    protected STVerticalAnchor anchory;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STWrapType }
     *     
     */
    public STWrapType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWrapType }
     *     
     */
    public void setType(STWrapType value) {
        this.type = value;
    }

    /**
     * Gets the value of the side property.
     * 
     * @return
     *     possible object is
     *     {@link STWrapSide }
     *     
     */
    public STWrapSide getSide() {
        return side;
    }

    /**
     * Sets the value of the side property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWrapSide }
     *     
     */
    public void setSide(STWrapSide value) {
        this.side = value;
    }

    /**
     * Gets the value of the anchorx property.
     * 
     * @return
     *     possible object is
     *     {@link STHorizontalAnchor }
     *     
     */
    public STHorizontalAnchor getAnchorx() {
        return anchorx;
    }

    /**
     * Sets the value of the anchorx property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHorizontalAnchor }
     *     
     */
    public void setAnchorx(STHorizontalAnchor value) {
        this.anchorx = value;
    }

    /**
     * Gets the value of the anchory property.
     * 
     * @return
     *     possible object is
     *     {@link STVerticalAnchor }
     *     
     */
    public STVerticalAnchor getAnchory() {
        return anchory;
    }

    /**
     * Sets the value of the anchory property.
     * 
     * @param value
     *     allowed object is
     *     {@link STVerticalAnchor }
     *     
     */
    public void setAnchory(STVerticalAnchor value) {
        this.anchory = value;
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
