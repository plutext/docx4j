/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_LightRig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LightRig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SphereCoords" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rig" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LightRigType" />
 *       &lt;attribute name="dir" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LightRigDirection" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LightRig", propOrder = {
    "rot"
})
public class CTLightRig {

    protected CTSphereCoords rot;
    @XmlAttribute(required = true)
    protected STLightRigType rig;
    @XmlAttribute(required = true)
    protected STLightRigDirection dir;

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link CTSphereCoords }
     *     
     */
    public CTSphereCoords getRot() {
        return rot;
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSphereCoords }
     *     
     */
    public void setRot(CTSphereCoords value) {
        this.rot = value;
    }

    /**
     * Gets the value of the rig property.
     * 
     * @return
     *     possible object is
     *     {@link STLightRigType }
     *     
     */
    public STLightRigType getRig() {
        return rig;
    }

    /**
     * Sets the value of the rig property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLightRigType }
     *     
     */
    public void setRig(STLightRigType value) {
        this.rig = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link STLightRigDirection }
     *     
     */
    public STLightRigDirection getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLightRigDirection }
     *     
     */
    public void setDir(STLightRigDirection value) {
        this.dir = value;
    }

}
