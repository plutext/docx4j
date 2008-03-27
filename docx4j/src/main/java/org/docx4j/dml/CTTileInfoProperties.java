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
 * <p>Java class for CT_TileInfoProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TileInfoProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="tx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="ty" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" />
 *       &lt;attribute name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" />
 *       &lt;attribute name="flip" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TileFlipMode" />
 *       &lt;attribute name="algn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_RectAlignment" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TileInfoProperties")
public class CTTileInfoProperties {

    @XmlAttribute
    protected Long tx;
    @XmlAttribute
    protected Long ty;
    @XmlAttribute
    protected Integer sx;
    @XmlAttribute
    protected Integer sy;
    @XmlAttribute
    protected STTileFlipMode flip;
    @XmlAttribute
    protected STRectAlignment algn;

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

}
