/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14; 

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_SphereCoords complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SphereCoords">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="lat" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" />
 *       &lt;attribute name="lon" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" />
 *       &lt;attribute name="rev" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SphereCoords")
public class CTSphereCoords implements Child
{

    @XmlAttribute(name = "lat", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", required = true)
    protected int lat;
    @XmlAttribute(name = "lon", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", required = true)
    protected int lon;
    @XmlAttribute(name = "rev", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", required = true)
    protected int rev;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the lat property.
     * 
     */
    public int getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     * 
     */
    public void setLat(int value) {
        this.lat = value;
    }

    /**
     * Gets the value of the lon property.
     * 
     */
    public int getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     * 
     */
    public void setLon(int value) {
        this.lon = value;
    }

    /**
     * Gets the value of the rev property.
     * 
     */
    public int getRev() {
        return rev;
    }

    /**
     * Sets the value of the rev property.
     * 
     */
    public void setRev(int value) {
        this.rev = value;
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
