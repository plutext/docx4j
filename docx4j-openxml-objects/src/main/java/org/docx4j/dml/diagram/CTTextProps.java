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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTFlatText;
import org.docx4j.dml.CTShape3D;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextProps"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_Text3D" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextProps", propOrder = {
    "sp3D",
    "flatTx"
})
public class CTTextProps implements Child
{

    @XmlElement(name = "sp3d", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTShape3D sp3D;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTFlatText flatTx;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sp3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTShape3D }
     *     
     */
    public CTShape3D getSp3D() {
        return sp3D;
    }

    /**
     * Sets the value of the sp3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShape3D }
     *     
     */
    public void setSp3D(CTShape3D value) {
        this.sp3D = value;
    }

    /**
     * Gets the value of the flatTx property.
     * 
     * @return
     *     possible object is
     *     {@link CTFlatText }
     *     
     */
    public CTFlatText getFlatTx() {
        return flatTx;
    }

    /**
     * Sets the value of the flatTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFlatText }
     *     
     */
    public void setFlatTx(CTFlatText value) {
        this.flatTx = value;
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
