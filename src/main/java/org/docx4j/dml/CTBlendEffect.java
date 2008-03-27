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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_BlendEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BlendEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EffectContainer"/>
 *       &lt;/sequence>
 *       &lt;attribute name="blend" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlendMode" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BlendEffect", propOrder = {
    "cont"
})
public class CTBlendEffect {

    @XmlElement(required = true)
    protected CTEffectContainer cont;
    @XmlAttribute(required = true)
    protected STBlendMode blend;

    /**
     * Gets the value of the cont property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectContainer }
     *     
     */
    public CTEffectContainer getCont() {
        return cont;
    }

    /**
     * Sets the value of the cont property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectContainer }
     *     
     */
    public void setCont(CTEffectContainer value) {
        this.cont = value;
    }

    /**
     * Gets the value of the blend property.
     * 
     * @return
     *     possible object is
     *     {@link STBlendMode }
     *     
     */
    public STBlendMode getBlend() {
        return blend;
    }

    /**
     * Sets the value of the blend property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlendMode }
     *     
     */
    public void setBlend(STBlendMode value) {
        this.blend = value;
    }

}
