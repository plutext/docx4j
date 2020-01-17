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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GvmlTextShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GvmlTextShape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="txBody" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="useSpRect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlUseShapeRectangle"/&gt;
 *           &lt;element name="xfrm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Transform2D"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GvmlTextShape", propOrder = {
    "txBody",
    "useSpRect",
    "xfrm",
    "extLst"
})
public class CTGvmlTextShape implements Child
{

    @XmlElement(required = true)
    protected CTTextBody txBody;
    protected CTGvmlUseShapeRectangle useSpRect;
    protected CTTransform2D xfrm;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the txBody property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxBody() {
        return txBody;
    }

    /**
     * Sets the value of the txBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxBody(CTTextBody value) {
        this.txBody = value;
    }

    /**
     * Gets the value of the useSpRect property.
     * 
     * @return
     *     possible object is
     *     {@link CTGvmlUseShapeRectangle }
     *     
     */
    public CTGvmlUseShapeRectangle getUseSpRect() {
        return useSpRect;
    }

    /**
     * Sets the value of the useSpRect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGvmlUseShapeRectangle }
     *     
     */
    public void setUseSpRect(CTGvmlUseShapeRectangle value) {
        this.useSpRect = value;
    }

    /**
     * Gets the value of the xfrm property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransform2D }
     *     
     */
    public CTTransform2D getXfrm() {
        return xfrm;
    }

    /**
     * Sets the value of the xfrm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransform2D }
     *     
     */
    public void setXfrm(CTTransform2D value) {
        this.xfrm = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
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
