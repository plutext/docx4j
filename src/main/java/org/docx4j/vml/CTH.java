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
 * <p>Java class for CT_H complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_H">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="position" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="polar" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="map" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="invx" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="invy" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="switch" type="{urn:schemas-microsoft-com:vml}ST_TrueFalseBlank" />
 *       &lt;attribute name="xrange" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="yrange" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="radiusrange" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_H")
public class CTH
    implements Child
{

    @XmlAttribute(name = "position")
    protected String position;
    @XmlAttribute(name = "polar")
    protected String polar;
    @XmlAttribute(name = "map")
    protected String map;
    @XmlAttribute(name = "invx")
    protected STTrueFalse invx;
    @XmlAttribute(name = "invy")
    protected STTrueFalse invy;
    @XmlAttribute(name = "switch")
    protected String _switch;
    @XmlAttribute(name = "xrange")
    protected String xrange;
    @XmlAttribute(name = "yrange")
    protected String yrange;
    @XmlAttribute(name = "radiusrange")
    protected String radiusrange;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPosition(String value) {
        this.position = value;
    }

    /**
     * Gets the value of the polar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolar() {
        return polar;
    }

    /**
     * Sets the value of the polar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolar(String value) {
        this.polar = value;
    }

    /**
     * Gets the value of the map property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMap() {
        return map;
    }

    /**
     * Sets the value of the map property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMap(String value) {
        this.map = value;
    }

    /**
     * Gets the value of the invx property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getInvx() {
        return invx;
    }

    /**
     * Sets the value of the invx property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setInvx(STTrueFalse value) {
        this.invx = value;
    }

    /**
     * Gets the value of the invy property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getInvy() {
        return invy;
    }

    /**
     * Sets the value of the invy property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setInvy(STTrueFalse value) {
        this.invy = value;
    }

    /**
     * Gets the value of the switch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwitch() {
        return _switch;
    }

    /**
     * Sets the value of the switch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwitch(String value) {
        this._switch = value;
    }

    /**
     * Gets the value of the xrange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXrange() {
        return xrange;
    }

    /**
     * Sets the value of the xrange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXrange(String value) {
        this.xrange = value;
    }

    /**
     * Gets the value of the yrange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYrange() {
        return yrange;
    }

    /**
     * Sets the value of the yrange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYrange(String value) {
        this.yrange = value;
    }

    /**
     * Gets the value of the radiusrange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRadiusrange() {
        return radiusrange;
    }

    /**
     * Sets the value of the radiusrange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRadiusrange(String value) {
        this.radiusrange = value;
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
