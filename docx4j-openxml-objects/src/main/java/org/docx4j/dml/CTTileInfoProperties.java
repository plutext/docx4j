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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TileInfoProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TileInfoProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="tx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" /&gt;
 *       &lt;attribute name="ty" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" /&gt;
 *       &lt;attribute name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" /&gt;
 *       &lt;attribute name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" /&gt;
 *       &lt;attribute name="flip" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TileFlipMode" /&gt;
 *       &lt;attribute name="algn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_RectAlignment" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TileInfoProperties")
public class CTTileInfoProperties implements Child
{

    @XmlAttribute(name = "tx")
    protected Long tx;
    @XmlAttribute(name = "ty")
    protected Long ty;
    @XmlAttribute(name = "sx")
    protected Integer sx;
    @XmlAttribute(name = "sy")
    protected Integer sy;
    @XmlAttribute(name = "flip")
    protected STTileFlipMode flip;
    @XmlAttribute(name = "algn")
    protected STRectAlignment algn;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tx property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTx() {
        return tx;
    }

    /**
     * Sets the value of the tx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTx(Long value) {
        this.tx = value;
    }

    /**
     * Gets the value of the ty property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTy() {
        return ty;
    }

    /**
     * Sets the value of the ty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTy(Long value) {
        this.ty = value;
    }

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSx() {
        return sx;
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSx(Integer value) {
        this.sx = value;
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSy() {
        return sy;
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSy(Integer value) {
        this.sy = value;
    }

    /**
     * Gets the value of the flip property.
     * 
     * @return
     *     possible object is
     *     {@link STTileFlipMode }
     *     
     */
    public STTileFlipMode getFlip() {
        return flip;
    }

    /**
     * Sets the value of the flip property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTileFlipMode }
     *     
     */
    public void setFlip(STTileFlipMode value) {
        this.flip = value;
    }

    /**
     * Gets the value of the algn property.
     * 
     * @return
     *     possible object is
     *     {@link STRectAlignment }
     *     
     */
    public STRectAlignment getAlgn() {
        return algn;
    }

    /**
     * Sets the value of the algn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRectAlignment }
     *     
     */
    public void setAlgn(STRectAlignment value) {
        this.algn = value;
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
